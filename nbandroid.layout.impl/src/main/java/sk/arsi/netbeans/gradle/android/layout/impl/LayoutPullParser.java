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

    /**
     * @param layoutPath Must start with '/' and be relative to test resources.
     */
    public LayoutPullParser(String layoutPath) {
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
    public LayoutPullParser(File layoutFile) {
        try {
            init(new FileInputStream(layoutFile));
        } catch (XmlPullParserException | FileNotFoundException e) {
            throw new IOError(e);
        }
    }

    public LayoutPullParser(InputStream layoutFileStream) {
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
    public ILayoutPullParser getParser(String string) {
        return null;
    }

}
