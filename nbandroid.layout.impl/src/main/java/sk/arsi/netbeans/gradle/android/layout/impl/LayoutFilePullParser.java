/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.layout.impl;

import static com.android.SdkConstants.*;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.resources.ValueXmlHelper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Modified {@link KXmlParser} that adds the methods of
 * {@link ILayoutPullParser}, and performs other layout-specific parser behavior
 * like translating fragment tags into include tags.
 */
public class LayoutFilePullParser extends KXmlParser implements ILayoutPullParser {

    private final ResourceNamespace myLayoutNamespace;
    /**
     * The layout to be shown for the current {@code <fragment>} tag. Usually
     * null.
     */
    private String myFragmentLayout = null;

    /**
     * Crates a new {@link LayoutFilePullParser} for the given XML file.
     */
    public static LayoutFilePullParser create(File xml, ResourceNamespace namespace)
            throws XmlPullParserException, IOException {
        String xmlText = Files.toString(xml, Charsets.UTF_8);
        return create(xmlText, namespace);
    }

    /**
     * Crates a new {@link LayoutFilePullParser} for the given XML text.
     */
    public static LayoutFilePullParser create(String xmlText, ResourceNamespace namespace)
            throws XmlPullParserException {
        LayoutFilePullParser parser = new LayoutFilePullParser(namespace);
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new StringReader(xmlText));
        return parser;
    }

    /**
     * Crates a new {@link LayoutFilePullParser} for the given XML text.
     */
    public static LayoutFilePullParser create(InputStream inputStream, ResourceNamespace namespace)
            throws XmlPullParserException {
        LayoutFilePullParser parser = new LayoutFilePullParser(namespace);
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new BufferedInputStream(inputStream), "UTF-8");
        return parser;
    }

    /**
     * Creates a new {@link LayoutFilePullParser}
     *
     * @param layoutlibCallback the associated callback
     */
    private LayoutFilePullParser(ResourceNamespace layoutNamespace) {
        myLayoutNamespace = layoutNamespace;
    }
    // --- Layout lib API methods

    @Override

    public Object getViewCookie() {
        String name = super.getName();
        if (name == null) {
            return null;
        }

        // Store tools attributes if this looks like a layout we'll need adapter view
        // bindings for in the LayoutlibCallback.
        if (LIST_VIEW.equals(name) || EXPANDABLE_LIST_VIEW.equals(name) || GRID_VIEW.equals(name) || SPINNER.equals(name)) {
            Map<String, String> map = null;
            int count = getAttributeCount();
            for (int i = 0; i < count; i++) {
                String namespace = getAttributeNamespace(i);
                if (namespace != null && namespace.equals(TOOLS_URI)) {
                    String attribute = getAttributeName(i);
                    if (attribute.equals(ATTR_IGNORE)) {
                        continue;
                    }
                    if (map == null) {
                        map = Maps.newHashMapWithExpectedSize(4);
                    }
                    map.put(attribute, getAttributeValue(i));
                }
            }

            return map;
        }

        return null;
    }

    @Override
    public ResourceNamespace getLayoutNamespace() {
        return myLayoutNamespace;
    }

    // --- KXMLParser override
    public static final String KEY_FRAGMENT_LAYOUT = "layout";

    @Override
    public String getName() {
        String name = super.getName();

        // At designtime, replace fragments with includes.
        if (VIEW_FRAGMENT.equals(name)) {
            myFragmentLayout = getProperty(this, KEY_FRAGMENT_LAYOUT);
            if (myFragmentLayout != null) {
                return VIEW_INCLUDE;
            }
        } else {
            myFragmentLayout = null;
        }

        return name;
    }

    public static String getProperty(LayoutFilePullParser parser, String name) {
        String value = parser.getAttributeValue(TOOLS_URI, name);
        if (value != null && value.isEmpty()) {
            value = null;
        }

        return value;
    }

    @Override
    public String getAttributeValue(String namespace, String localName) {
        if (ATTR_LAYOUT.equals(localName) && myFragmentLayout != null) {
            return myFragmentLayout;
        }

        String value = super.getAttributeValue(namespace, localName);

        // on the fly convert match_parent to fill_parent for compatibility with older
        // platforms.
        if (VALUE_MATCH_PARENT.equals(value)
                && (ATTR_LAYOUT_WIDTH.equals(localName) || ATTR_LAYOUT_HEIGHT.equals(localName))
                && ANDROID_URI.equals(namespace)) {
            return VALUE_FILL_PARENT;
        }

        if (namespace != null) {
            if (namespace.equals(ANDROID_URI)) {
                // Allow the tools namespace to override the framework attributes at designtime
                String designValue = super.getAttributeValue(TOOLS_URI, localName);
                if (designValue != null) {
                    if (value != null && designValue.isEmpty()) {
                        // Empty when there is a runtime attribute set means unset the runtime attribute
                        value = null;
                    } else {
                        value = designValue;
                    }
                }
            } else if (value == null) {
                // Auto-convert http://schemas.android.com/apk/res-auto resources. The lookup
                // will be for the current application's resource package, e.g.
                // http://schemas.android.com/apk/res/foo.bar, but the XML document will
                // be using http://schemas.android.com/apk/res-auto in library projects:
                value = super.getAttributeValue(AUTO_URI, localName);
            } else {
                System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.LayoutFilePullParser.getAttributeValue()");
            }
        }

        if (value != null) {
            // Handle unicode and XML escapes
            for (int i = 0, n = value.length(); i < n; i++) {
                char c = value.charAt(i);
                if (c == '&' || c == '\\') {
                    value = ValueXmlHelper.unescapeResourceString(value, true, false);
                    break;
                }
            }
        }

        return value;
    }
}
