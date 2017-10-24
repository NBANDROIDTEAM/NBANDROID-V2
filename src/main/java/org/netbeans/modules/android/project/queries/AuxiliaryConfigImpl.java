package org.netbeans.modules.android.project.queries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AuxiliaryConfigImpl implements AuxiliaryConfiguration {
  private static final Logger LOG = Logger.getLogger(AuxiliaryConfigImpl.class.getName());

  static final String AUX_CONFIG_ATTR_BASE = AuxiliaryConfiguration.class.getName();
  static final String AUX_CONFIG_FILENAME = ".netbeans.xml"; // NOI18N
  private final Project project;

  public AuxiliaryConfigImpl(Project proj) {
    this.project = proj;
  }

  @Override
  public Element getConfigurationFragment(final String elementName, final String namespace, final boolean shared) {
    return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
      public Element run() {
        assert project != null;
        FileObject dir = project.getProjectDirectory();
        if (shared) {
          FileObject config = dir.getFileObject(AUX_CONFIG_FILENAME);
          if (config != null) {
            try {
              InputStream is = config.getInputStream();
              try {
                InputSource input = new InputSource(is);
                input.setSystemId(config.toURL().toString());
                Element root = XMLUtil.parse(input, false, true, /*XXX*/ null, null).getDocumentElement();
                return XMLUtil.findElement(root, elementName, namespace);
              } finally {
                is.close();
              }
            } catch (Exception x) {
              LOG.log(Level.INFO, "Cannot parse" + config, x);
            }
          }
        } else {
          String attrName = AUX_CONFIG_ATTR_BASE + "." + namespace + "#" + elementName;
          Object attr = dir.getAttribute(attrName);
          if (attr instanceof String) {
            try {
              Element fragment = XMLUtil.parse(new InputSource(new StringReader((String) attr)), false, true,
                  /*XXX #136595: need utility method*/ null, null).getDocumentElement();
              if (elementName.equals(fragment.getLocalName()) && namespace.equals(fragment.getNamespaceURI())) {
                return fragment;
              } else {
                LOG.log(Level.INFO, "Value " + attr + " of " + attrName + " on " + dir + " has the wrong local name or namespace");
              }
            } catch (SAXException x) {
              LOG.log(Level.INFO, "Cannot parse value " + attr + " of " + attrName + " on " + dir + ": " + x.getMessage());
            } catch (IOException x) {
              assert false : x;
            }
          }
        }
        return null;
      }
    });
  }

  @Override
  public void putConfigurationFragment(final Element fragment, final boolean shared) throws IllegalArgumentException {
    ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
      @Override
      public Void run() {
        String elementName = fragment.getLocalName();
        String namespace = fragment.getNamespaceURI();
        if (namespace == null) {
          throw new IllegalArgumentException();
        }
        FileObject dir = project.getProjectDirectory();
        try {
          if (shared) {
            Document doc;
            FileObject config = dir.getFileObject(AUX_CONFIG_FILENAME);
            if (config != null) {
              InputStream is = config.getInputStream();
              try {
                InputSource input = new InputSource(is);
                input.setSystemId(config.toURL().toString());
                doc = XMLUtil.parse(input, false, true, /*XXX*/ null, null);
              } finally {
                is.close();
              }
            } else {
              config = dir.createData(AUX_CONFIG_FILENAME);
              doc = XMLUtil.createDocument("auxiliary-configuration", "http://www.netbeans.org/ns/auxiliary-configuration/1", null, null);
            }
            Element root = doc.getDocumentElement();
            Element oldFragment = XMLUtil.findElement(root, elementName, namespace);
            if (oldFragment != null) {
              root.removeChild(oldFragment);
            }
            Node ref = null;
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
              Node node = list.item(i);
              if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
              }
              int comparison = node.getNodeName().compareTo(elementName);
              if (comparison == 0) {
                comparison = node.getNamespaceURI().compareTo(namespace);
              }
              if (comparison > 0) {
                ref = node;
                break;
              }
            }
            root.insertBefore(root.getOwnerDocument().importNode(fragment, true), ref);
            OutputStream os = config.getOutputStream();
            try {
              XMLUtil.write(doc, os, "UTF-8");
            } finally {
              os.close();
            }
          } else {
            String attrName = AUX_CONFIG_ATTR_BASE + "." + namespace + "#" + elementName;
            dir.setAttribute(attrName, elementToString(fragment));
          }
        } catch (Exception x) {
          LOG.log(Level.WARNING, "Cannot save configuration to " + dir, x);
        }
        return null;
      }
    });
  }

  static String elementToString(Element e) throws ParserConfigurationException {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    DOMImplementationLS ls = (DOMImplementationLS) doc.getImplementation().getFeature("LS", "3.0"); // NOI18N
    assert ls != null : "No DOM 3 LS supported in " + doc.getClass().getName();
    // JAXP bug #6710755: cannot directly serialize fragment in JDK 5, must add to a document.
    doc.appendChild(doc.importNode(e, true));
    LSSerializer serializer = ls.createLSSerializer();
    serializer.getDomConfig().setParameter("xml-declaration", false);
    return serializer.writeToString(doc);
  }

  private boolean removeFallbackImpl(final String elementName, final String namespace, final boolean shared) {
    FileObject dir = project.getProjectDirectory();
    try {
      if (shared) {
        FileObject config = dir.getFileObject(AUX_CONFIG_FILENAME);
        if (config != null) {
          try {
            Document doc;
            InputStream is = config.getInputStream();
            try {
              InputSource input = new InputSource(is);
              input.setSystemId(config.toURL().toString());
              doc = XMLUtil.parse(input, false, true, /*XXX*/ null, null);
            } finally {
              is.close();
            }
            Element root = doc.getDocumentElement();
            Element toRemove = XMLUtil.findElement(root, elementName, namespace);
            if (toRemove != null) {
              root.removeChild(toRemove);
              if (root.getElementsByTagName("*").getLength() > 0) {
                OutputStream os = config.getOutputStream();
                try {
                  XMLUtil.write(doc, os, "UTF-8");
                } finally {
                  os.close();
                }
              } else {
                config.delete();
              }
              return true;
            }
          } catch (SAXException x) {
            LOG.log(Level.INFO, "Cannot parse" + config, x);
          }
        }
      } else {
        String attrName = AUX_CONFIG_ATTR_BASE + "." + namespace + "#" + elementName;
        if (dir.getAttribute(attrName) != null) {
          dir.setAttribute(attrName, null);
          return true;
        }
      }
    } catch (IOException x) {
      LOG.log(Level.WARNING, "Cannot remove configuration from " + dir, x);
    }
    return false;
  }

  @Override
  public boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
    return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
      @Override public Boolean run() {
        return removeFallbackImpl(elementName, namespace, shared);
      }
    });
  }
}
