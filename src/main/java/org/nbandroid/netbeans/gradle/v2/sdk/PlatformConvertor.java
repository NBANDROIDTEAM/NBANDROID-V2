/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.sdklib.AndroidVersion;
import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.*;

/**
 * Reads and writes the standard platform format implemented by PlatformImpl2.
 *
 * @author Svata Dedic
 * @author arsi - android mod
 */
public class PlatformConvertor implements Environment.Provider, InstanceCookie.Of, PropertyChangeListener, Runnable, InstanceContent.Convertor<Class<Node>, Node> {

    private static final Logger LOG = Logger.getLogger(PlatformConvertor.class.getName());

    private static final String PLATFORM_DTD_ID = "-//NetBeans//DTD Android SdkDefinition 1.0//EN"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PlatformConvertor.class.getName(), 1, false, false);

    private PlatformConvertor() {
    }

    public static PlatformConvertor createProvider(FileObject reg) {
        return new PlatformConvertor();
    }

    @Override
    public Lookup getEnvironment(DataObject obj) {
        if (obj instanceof XMLDataObject) {
            return new PlatformConvertor((XMLDataObject) obj).getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }

    private InstanceContent cookies = new InstanceContent();

    private XMLDataObject holder;

    private boolean defaultPlatform;

    private Lookup lookup;

    private RequestProcessor.Task saveTask;

    private Reference<AndroidSdk> refPlatform = new WeakReference<>(null);

    private LinkedList<PropertyChangeEvent> keepAlive = new LinkedList<>();

    private PlatformConvertor(@NonNull final XMLDataObject object) {
        Parameters.notNull("object", object);
        this.holder = object;
        this.holder.getPrimaryFile().addFileChangeListener(new FileChangeAdapter() {
            @Override
            public void fileDeleted(final FileEvent fe) {

            }
        });
        cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
        cookies.add(Node.class, this);
    }

    Lookup getLookup() {
        return lookup;
    }

    @Override
    public Class instanceClass() {
        return AndroidSdk.class;
    }

    @Override
    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refPlatform.get();
            if (o != null) {
                return o;
            }
            H handler = new H();
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new org.xml.sax.InputSource(
                        holder.getPrimaryFile().getInputStream());
                is.setSystemId(holder.getPrimaryFile().toURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(handler);

                reader.parse(is);
            } catch (SAXException ex) {
                final Exception cause = ex.getException();
                if (cause instanceof java.io.IOException) {
                    throw (IOException) cause;
                } else {
                    throw new java.io.IOException(cause);
                }
            }
            AndroidSdk inst = createPlatform(handler);
            refPlatform = new WeakReference<>(inst);
            return inst;
        }
    }

    AndroidSdk createPlatform(H handler) {
        AndroidSdkImpl p;

        p = new AndroidSdkImpl(handler.name, handler.installFolder, handler.properties, handler.sysProperties, handler.platforms, handler.isDefault);
        for (AndroidPlatformInfo platform : handler.platforms) {
            platform.setSdk(p);
        }
        defaultPlatform = handler.isDefault;
        p.addPropertyChangeListener(this);
        return p;
    }

    @Override
    public String instanceName() {
        return holder.getName();
    }

    @Override
    public boolean instanceOf(Class<?> type) {
        return (type.isAssignableFrom(AndroidSdk.class));
    }

    private static final int DELAY = 2000;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (this) {
            if (saveTask == null) {
                saveTask = RP.create(this);
            }
        }
        synchronized (this) {
            keepAlive.add(evt);
        }
        saveTask.schedule(DELAY);
    }

    @Override
    public void run() {
        PropertyChangeEvent e;

        synchronized (this) {
            e = keepAlive.removeFirst();
        }
        AndroidSdk plat = (AndroidSdk) e.getSource();
        try {
            holder.getPrimaryFile().getFileSystem().runAtomicAction(
                    new W(plat, holder, defaultPlatform));
        } catch (java.io.IOException ex) {
            Exceptions.printStackTrace(Exceptions.attachSeverity(ex, Level.INFO));
        }
    }

    @Override
    public Node convert(Class<Node> key) {
        try {
            AndroidSdk p = (AndroidSdk) instanceCreate();
            return new AndroidSdkNode(p, this.holder);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public String displayName(Class<Node> key) {
        return key.getName();
    }

    @Override
    public String id(Class<Node> key) {
        return key.getName();
    }

    @Override
    public Class<Node> type(Class<Node> key) {
        return key;
    }

    public static AndroidSdk create(final AndroidSdk prototype) throws IOException, IllegalArgumentException {
        Parameters.notNull("prototype", prototype);
        final String systemName = prototype.getDisplayName();
        if (systemName == null) {
            throw new IllegalArgumentException("No name");
        }
        final FileObject platformsFolder = FileUtil.getConfigFile(AndroidSdkProvider.PLATFORM_STORAGE);
        if (platformsFolder.getFileObject(systemName, "xml") != null) {   //NOI18N
            throw new IllegalArgumentException(systemName);
        }
        final DataObject dobj = create(prototype, DataFolder.findFolder(platformsFolder), systemName);
        return dobj.getNodeDelegate().getLookup().lookup(AndroidSdk.class);
    }

    @NonNull
    public static String getFreeAntName(@NonNull final String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException();
        }
        final FileObject platformsFolder = FileUtil.getConfigFile(AndroidSdkProvider.PLATFORM_STORAGE);
        String antName = PropertyUtils.getUsablePropertyName(name);
        if (platformsFolder.getFileObject(antName, "xml") != null) { //NOI18N
            String baseName = antName;
            int index = 1;
            antName = baseName + Integer.toString(index);
            while (platformsFolder.getFileObject(antName, "xml") != null) {  //NOI18N
                index++;
                antName = baseName + Integer.toString(index);
            }
        }
        return antName;
    }

    public static String createName(String platName, String propType) {
        return "platforms." + platName + "." + propType;        //NOI18N
    }

    private static DataObject create(final AndroidSdk plat, final DataFolder f, final String idName) throws IOException {
        W w = new W(plat, f, idName);
        f.getPrimaryFile().getFileSystem().runAtomicAction(w);
        return w.holder;
    }

    public static class BrokenPlatformException extends IOException {

        private final String toolName;

        public BrokenPlatformException(final String toolName) {
            super("Cannot locate " + toolName + " command");   //NOI18N
            this.toolName = toolName;
        }

        public String getMissingTool() {
            return this.toolName;
        }

    }

    private static final class W implements FileSystem.AtomicAction {

        AndroidSdk instance;
        MultiDataObject holder;
        String name;
        DataFolder f;
        boolean defaultPlatform;

        W(AndroidSdk instance, MultiDataObject holder, boolean defaultPlatform) {
            this.instance = instance;
            this.holder = holder;
            this.defaultPlatform = instance.isDefaultSdk();
        }

        W(AndroidSdk instance, DataFolder f, String n) {
            this.instance = instance;
            this.name = n;
            this.f = f;
            this.defaultPlatform = instance.isDefaultSdk();
        }

        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;

            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                write(buffer);
            } finally {
                buffer.close();
            }
            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                FileObject folder = f.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, name, "xml");
                data = folder.createData(fn, "xml");
                lck = data.lock();
            }
            try (OutputStream out = data.getOutputStream(lck)) {
                out.write(buffer.toByteArray());
                out.flush();
            } finally {
                lck.releaseLock();
            }
            if (holder == null) {
                holder = (MultiDataObject) DataObject.find(data);
            }
        }

        void write(final OutputStream out) throws IOException {
            final Map<String, String> props = instance.getProperties();
            final Map<String, String> sysProps = instance.getSystemProperties();
            List<AndroidPlatformInfo> platforms = instance.getPlatforms();
            final Document doc = XMLUtil.createDocument(ELEMENT_SDK_ROOT, null, PLATFORM_DTD_ID, "http://www.netbeans.org/dtds/java-platformdefinition-1_0.dtd"); //NOI18N
            final Element platformElement = doc.getDocumentElement();
            platformElement.setAttribute(ATTR_PLATFORM_NAME, instance.getDisplayName());
            platformElement.setAttribute(ATTR_PLATFORM_DEFAULT, defaultPlatform ? "yes" : "no"); //NOI18N

            final Element jdkHomeElement = doc.createElement(ELEMENT_SDKHOME);
            final Element resourceElement = doc.createElement(ELEMENT_RESOURCE);
            resourceElement.appendChild(doc.createTextNode(instance.getInstallFolder().getPath()));
            jdkHomeElement.appendChild(resourceElement);
            platformElement.appendChild(jdkHomeElement);
            final Element propsElement = doc.createElement(ELEMENT_PROPERTIES);
            writeProperties(props, propsElement, doc);
            platformElement.appendChild(propsElement);
            if (!defaultPlatform) {
                final Element sysPropsElement = doc.createElement(ELEMENT_SYSPROPERTIES);
                writeProperties(sysProps, sysPropsElement, doc);
                platformElement.appendChild(sysPropsElement);
            }
            final Element platformsElement = doc.createElement(ELEMENT_PLATFORMS);
            writePlatforms(platforms, platformsElement, doc);
            platformElement.appendChild(platformsElement);
            XMLUtil.write(doc, out, "UTF8");                                                    //NOI18N
        }

        private void writePlatforms(List<AndroidPlatformInfo> platforms, Element platformsElement, Document doc) {
            for (AndroidPlatformInfo platform : platforms) {
                final Element platformElement = doc.createElement(ELEMENT_PLATFORM);
                try {
                    platformElement.setAttribute(ATTR_PROPERTY_NAME, XMLUtil.toAttributeValue(platform.getPlatformName()));
                    platformElement.setAttribute(ATTR_PLATFORM_PATH, XMLUtil.toAttributeValue(platform.getSDKFolder().getPath()));
                    platformElement.setAttribute(ATTR_PLATFORM_APILEVEL, XMLUtil.toAttributeValue("" + platform.getAndroidVersion().getApiLevel()));
                    platformElement.setAttribute(ATTR_PLATFORM_HASH, XMLUtil.toAttributeValue(platform.getHashString()));
                } catch (CharConversionException ex) {
                    LOG.log(
                            Level.WARNING,
                            "Cannot store attr: {0} value: {1}", //NOI18N
                            new Object[]{
                                platform.getPlatformName(),
                                platform.getSDKFolder().getPath()
                            });
                }

                writeBootSrcDoc(platform.getBootPaths(), ELEMENT_BOOT, platformElement, doc);
                writeBootSrcDoc(platform.getSrcPaths(), ELEMENT_SRC, platformElement, doc);
                writeBootSrcDoc(platform.getJavadocPaths(), ELEMENT_DOC, platformElement, doc);
                platformsElement.appendChild(platformElement);
            }
        }

        private void writeBootSrcDoc(List<AndroidPlatformInfo.PathRecord> paths, String elementType, Element platformElement, Document doc) {
            final Element element = doc.createElement(elementType);
            for (AndroidPlatformInfo.PathRecord path : paths) {
                final Element fileElement = doc.createElement(ELEMENT_FILE);
                try {
                    fileElement.setAttribute(ATTR_FILE_USER, XMLUtil.toAttributeValue("" + path.isUserRecord()));
                } catch (CharConversionException ex) {
                    Exceptions.printStackTrace(ex);
                }
                fileElement.appendChild(doc.createTextNode(path.getUrl().toExternalForm()));
                element.appendChild(fileElement);
            }
            platformElement.appendChild(element);

        }

        void writeProperties(final Map<String, String> props, final Element element, final Document doc) throws IOException {
            final Collection<String> sortedProps = new TreeSet<>(props.keySet());
            for (Iterator<String> it = sortedProps.iterator(); it.hasNext();) {
                final String n = it.next();
                final String val = props.get(n);
                try {
                    XMLUtil.toAttributeValue(n);
                    XMLUtil.toAttributeValue(val);
                    final Element propElement = doc.createElement(ELEMENT_PROPERTY);
                    propElement.setAttribute(ATTR_PROPERTY_NAME, n);
                    propElement.setAttribute(ATTR_PROPERTY_VALUE, val);
                    element.appendChild(propElement);
                } catch (CharConversionException e) {
                    LOG.log(
                            Level.WARNING,
                            "Cannot store property: {0} value: {1}", //NOI18N
                            new Object[]{
                                n,
                                val
                            });
                }
            }
        }

    }

    static final String ELEMENT_PROPERTIES = "properties"; // NOI18N
    static final String ELEMENT_SYSPROPERTIES = "sysproperties"; // NOI18N
    static final String ELEMENT_PROPERTY = "property"; // NOI18N
    static final String ELEMENT_SDK_ROOT = "sdk"; // NOI18N
    static final String ELEMENT_SDKHOME = "sdkhome";    //NOI18N
    static final String ELEMENT_RESOURCE = "resource";  //NOI18N
    static final String ATTR_PLATFORM_NAME = "name"; // NOI18N
    static final String ATTR_PLATFORM_DEFAULT = "default"; // NOI18N
    static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
    static final String ELEMENT_PLATFORMS = "platforms";
    static final String ELEMENT_PLATFORM = "platform";
    static final String ATTR_PLATFORM_PATH = "path"; // NOI18N
    static final String ATTR_PLATFORM_HASH = "hash"; // NOI18N
    static final String ELEMENT_BOOT = "boot";
    static final String ELEMENT_SRC = "src";
    static final String ELEMENT_DOC = "doc";
    static final String ELEMENT_FILE = "file";
    static final String ATTR_FILE_USER = "user"; // NOI18N
    static final String ATTR_PLATFORM_APILEVEL = "apilevel"; // NOI18N

    private static final class H extends org.xml.sax.helpers.DefaultHandler implements EntityResolver {

        Map<String, String> properties;
        Map<String, String> sysProperties;
        String installFolder;
        String name;
        boolean isDefault;
        boolean isUser;
        final List<AndroidPlatformInfo> platforms = new ArrayList<>();
        AndroidPlatformInfo platformInfo;

        private Map<String, String> propertyMap;
        private StringBuffer buffer;
        private List<String> path;
        List<AndroidPlatformInfo.PathRecord> paths;

        @Override
        public void startDocument() throws org.xml.sax.SAXException {
        }

        @Override
        public void endDocument() throws org.xml.sax.SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attrs)
                throws org.xml.sax.SAXException {
            if (qName != null) {
                switch (qName) {
                    case ELEMENT_SDK_ROOT:
                        name = attrs.getValue(ATTR_PLATFORM_NAME);
                        isDefault = "yes".equals(attrs.getValue(ATTR_PLATFORM_DEFAULT));
                        break;
                    case ELEMENT_PROPERTIES:
                        if (properties == null) {
                            properties = new HashMap<>(17);
                        }
                        propertyMap = properties;
                        break;
                    case ELEMENT_PLATFORMS:
                        platforms.clear();
                        break;
                    case ELEMENT_PLATFORM: {
                        String nm = attrs.getValue(ATTR_PROPERTY_NAME);
                        if (nm == null || "".equals(nm)) {
                            throw new SAXException("missing name");
                        }
                        String val = attrs.getValue(ATTR_PLATFORM_PATH);
                        String apiLevel = attrs.getValue(ATTR_PLATFORM_APILEVEL);
                        String hash = attrs.getValue(ATTR_PLATFORM_HASH);
                        AndroidVersion androidVersion;
                        try {
                            androidVersion = new AndroidVersion("" + apiLevel);
                        } catch (AndroidVersion.AndroidVersionException ex) {
                            throw new SAXException("bad Android API level");
                        }
                        if (platformInfo == null) {
                            platformInfo = new AndroidPlatformInfo(new File(val), nm, androidVersion, hash);
                        }
                    }
                    break;
                    case ELEMENT_BOOT:
                    case ELEMENT_SRC:
                    case ELEMENT_DOC:
                        paths = new ArrayList<>();
                        break;
                    case ELEMENT_SYSPROPERTIES:
                        if (sysProperties == null) {
                            sysProperties = new HashMap<>(17);
                        }
                        propertyMap = sysProperties;
                        break;
                    case ELEMENT_PROPERTY: {
                        if (propertyMap == null) {
                            throw new SAXException("property w/o properties or sysproperties");
                        }
                        String name = attrs.getValue(ATTR_PROPERTY_NAME);
                        if (name == null || "".equals(name)) {
                            throw new SAXException("missing name");
                        }
                        String val = attrs.getValue(ATTR_PROPERTY_VALUE);
                        propertyMap.put(name, val);
                        break;
                    }
                    case ELEMENT_SDKHOME:
                        this.installFolder = "";
                        this.path = new ArrayList<>();
                        break;
                    case ELEMENT_FILE: {
                        String val = attrs.getValue(ATTR_FILE_USER);
                        isUser = Boolean.valueOf(val);
                    }
                    case ELEMENT_RESOURCE:
                        this.buffer = new StringBuffer();
                        break;

                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws org.xml.sax.SAXException {
            if (qName != null) {
                switch (qName) {
                    case ELEMENT_PROPERTIES:
                    case ELEMENT_SYSPROPERTIES:
                        propertyMap = null;
                        break;
                    case ELEMENT_PLATFORMS:
                        break;
                    case ELEMENT_PLATFORM:
                        if (platforms != null) {
                            platforms.add(platformInfo);
                        }
                        platformInfo = null;
                        break;
                    case ELEMENT_RESOURCE:
                        this.path.add(this.buffer.toString());
                        this.installFolder = this.path.get(0);
                        this.buffer = null;
                        break;
                    case ELEMENT_FILE: {
                        try {
                            paths.add(new AndroidPlatformInfo.PathRecord(isUser, new URL(buffer.toString())));
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    this.buffer = null;
                    break;
                    case ELEMENT_BOOT:
                        if (platformInfo != null) {
                            platformInfo.addBootPaths(paths);
                        }
                        break;
                    case ELEMENT_SRC:
                        if (platformInfo != null) {
                            platformInfo.addSrcPaths(paths);
                        }
                        break;
                    case ELEMENT_DOC:
                        if (platformInfo != null) {
                            platformInfo.addJavaDocPaths(paths);
                        }
                        break;
                }
            }
        }

        @Override
        public void characters(char chars[], int start, int length) throws SAXException {
            if (this.buffer != null) {
                this.buffer.append(chars, start, length);
            }
        }

        @Override
        public org.xml.sax.InputSource resolveEntity(String publicId, String systemId)
                throws SAXException {
            if (PLATFORM_DTD_ID.equals(publicId)) {
                return new org.xml.sax.InputSource(new ByteArrayInputStream(new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }

    }

}
