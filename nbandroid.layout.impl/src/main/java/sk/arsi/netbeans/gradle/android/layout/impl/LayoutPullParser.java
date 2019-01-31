/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import static com.android.SdkConstants.ATTR_IGNORE;
import static com.android.SdkConstants.EXPANDABLE_LIST_VIEW;
import static com.android.SdkConstants.GRID_VIEW;
import static com.android.SdkConstants.LIST_VIEW;
import static com.android.SdkConstants.SPINNER;
import static com.android.SdkConstants.TOOLS_URI;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.resources.ValueXmlHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

public class LayoutPullParser extends KXmlParser implements ILayoutPullParser {

    private final ResourceNamespace appNamespace;
    private String myFragmentLayout = null;
    /**
     * @param layoutPath Must start with '/' and be relative to test resources.
     */
    public LayoutPullParser(String layoutPath, ResourceNamespace appNamespace) {
        this.appNamespace = appNamespace;
        try {
            init(new FileInputStream(new File(layoutPath)));
        } catch (XmlPullParserException e) {
            throw new IOError(e);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LayoutPullParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param layoutFile Path of the layout xml file on disk.
     */
    public LayoutPullParser(File layoutFile, ResourceNamespace appNamespace) {
        this.appNamespace = appNamespace;
        try {
            init(new FileInputStream(layoutFile));
        } catch (XmlPullParserException | FileNotFoundException e) {
            throw new IOError(e);
        }
    }

    public LayoutPullParser(InputStream layoutFileStream, ResourceNamespace appNamespace) {
        this.appNamespace = appNamespace;
        if (layoutFileStream == null) {
            throw new NullPointerException("LayoutStream is null");
        }
        try {
            init(layoutFileStream);
        } catch (XmlPullParserException e) {
            throw new IOError(e);
        }
    }

    private void init(InputStream stream) throws XmlPullParserException {
        setFeature(FEATURE_PROCESS_NAMESPACES, true);
        setInput(stream, null);
    }

    @Override
    public Object getViewCookie() {
        // TODO: Implement this properly.
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
                        map = new HashMap<String, String>(4);
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
        return appNamespace;
    }
    public static final String KEY_FRAGMENT_LAYOUT = "layout";

    @Override
    public String getName() {
        String name = super.getName();

        // At designtime, replace fragments with includes.
        if (com.android.SdkConstants.VIEW_FRAGMENT.equals(name)) {
            myFragmentLayout = getProperty(this, KEY_FRAGMENT_LAYOUT);
            if (myFragmentLayout != null) {
                return com.android.SdkConstants.VIEW_INCLUDE;
            }
        } else {
            myFragmentLayout = null;
        }

        return name;
    }

    public static String getProperty(LayoutPullParser parser, String name) {
        String value = parser.getAttributeValue(TOOLS_URI, name);
        if (value != null && value.isEmpty()) {
            value = null;
        }

        return value;
    }


    @Override
    public String getAttributeValue(String namespace, String localName) {
        if (com.android.SdkConstants.ATTR_LAYOUT.equals(localName) && myFragmentLayout != null) {
            return myFragmentLayout;
        }
        String value = super.getAttributeValue(namespace, localName);

        // on the fly convert match_parent to fill_parent for compatibility with older
        // platforms.
        if (com.android.SdkConstants.VALUE_MATCH_PARENT.equals(value)
                && (com.android.SdkConstants.ATTR_LAYOUT_WIDTH.equals(localName) || com.android.SdkConstants.ATTR_LAYOUT_HEIGHT.equals(localName))
                && com.android.SdkConstants.ANDROID_URI.equals(namespace)) {
            return com.android.SdkConstants.VALUE_FILL_PARENT;
        }

        if (namespace != null) {
            if (namespace.equals(com.android.SdkConstants.ANDROID_URI)) {
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
                value = super.getAttributeValue(com.android.SdkConstants.AUTO_URI, localName);
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
