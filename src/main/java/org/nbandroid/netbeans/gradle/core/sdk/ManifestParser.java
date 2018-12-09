package org.nbandroid.netbeans.gradle.core.sdk;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses themes.xml file to get names of themes in given target.
 */
public class ManifestParser {

    private static final Logger LOG = Logger.getLogger(ManifestParser.class.getName());

    private static final String THEME_NAME_EXPR = "resources/style/@name";
    private static final String APP_NAME_EXPR = "/manifest/application/@*[namespace-uri()='http://schemas.android.com/apk/res/android'"
            + " and local-name()='label']";
    private static final String MANIFEST_THEME_NAME_EXPR = "//@*[namespace-uri()='http://schemas.android.com/apk/res/android'"
            + " and local-name()='theme']";

    public static ApplicationData defaultApplicationData() {
        return new ApplicationData("<unknown>", "");
    }

    public static class ApplicationData {

        public final String appLabel;
        public final String appIconName;

        private ApplicationData(String appLabel, String appIconName) {
            this.appLabel = appLabel;
            this.appIconName = appIconName;
        }
    }

    public static ManifestParser getDefault() {
        return new ManifestParser();
    }

    private XPathExpression styleXpathExp;
    private XPathExpression manifestXpathExp;
    private XPathExpression appNameXpathExp;

    ManifestParser() {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            styleXpathExp = xpath.compile(THEME_NAME_EXPR);
            appNameXpathExp = xpath.compile(APP_NAME_EXPR);

            xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {

                @Override
                public String getNamespaceURI(String prefix) {
                    if ("android".equals(prefix)) {
                        return "http://schemas.android.com/apk/res/android";
                    }
                    return null;
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    throw new UnsupportedOperationException("getPrefix(" + namespaceURI + ") not supported yet.");
                }

                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    throw new UnsupportedOperationException("getPrefixes(" + namespaceURI + ") not supported yet.");
                }
            });
            manifestXpathExp = xpath.compile(MANIFEST_THEME_NAME_EXPR);

        } catch (XPathExpressionException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }

    public Iterable<String> getPlatformThemeNames(InputStream is) {
        return Iterables.filter(
                getAttrNames(is, styleXpathExp),
                new Predicate<String>() {
            @Override
            public boolean apply(String name) {
                return ("Theme".equals(name) || name.startsWith("Theme."));
            }
        });
    }

    public Iterable<String> getProjectThemeNames(InputStream is) {
        return getAttrNames(is, styleXpathExp);
    }

    public Iterable<String> getManifestThemeNames(InputStream is) {
        return Iterables.filter(
                Iterables.transform(
                        getAttrNames(is, manifestXpathExp),
                        new Function<String, String>() {
                    @Override
                    public String apply(String name) {
                        return name.startsWith("@style/") ? name.substring("@style/".length()) : null;
                    }
                }),
                Predicates.notNull());
    }

    public ApplicationData getApplicationData(InputStream is) {
        try {
            Document doc = parseStream(is);
            Node node = (Node) appNameXpathExp.evaluate(doc, XPathConstants.NODE);
            if (node != null) {
                return new ApplicationData(node.getTextContent(), "");
            }
        } catch (XPathExpressionException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return defaultApplicationData();
    }

    public static @Nullable
    Document parseStream(InputStream is) {
        if (is != null) {
            try {
                InputSource inputSource = new InputSource(is);
                return XMLUtil.parse(inputSource, false, true, null, null);
            } catch (IOException | SAXException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return null;
    }

    private static Iterable<String> getAttrNames(InputStream is, XPathExpression expr) {
        Set<String> names = Sets.newHashSet();
        if (expr != null) {
            try {
                Document doc = parseStream(is);
                if (doc != null) {
                    NodeList nodelist = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    for (int i = 0; i < nodelist.getLength(); i++) {
                        String name = nodelist.item(i).getNodeValue();
                        names.add(name);
                    }
                }
            } catch (XPathExpressionException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return names;
    }
}
