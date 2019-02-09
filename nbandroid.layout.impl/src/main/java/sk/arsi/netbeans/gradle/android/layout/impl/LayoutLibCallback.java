/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.SdkConstants;
import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_LAYOUT_HEIGHT;
import static com.android.SdkConstants.ATTR_LAYOUT_WIDTH;
import static com.android.SdkConstants.AUTO_URI;
import static com.android.SdkConstants.TOOLS_URI;
import static com.android.SdkConstants.VALUE_FILL_PARENT;
import static com.android.SdkConstants.VALUE_MATCH_PARENT;
import com.android.annotations.Nullable;
import com.android.ide.common.rendering.api.ActionBarCallback;
import com.android.ide.common.rendering.api.AdapterBinding;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.LayoutlibCallback;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.SessionParams.Key;
import com.android.ide.common.resources.ValueXmlHelper;
import com.android.layoutlib.bridge.android.RenderParamsFlags;
import static com.android.layoutlib.bridge.android.RenderParamsFlags.FLAG_KEY_RECYCLER_VIEW_SUPPORT;
import static com.android.layoutlib.bridge.android.RenderParamsFlags.FLAG_KEY_XML_FILE_PARSER_SUPPORT;
import com.android.resources.ResourceType;
import com.android.tools.layoutlib.annotations.NotNull;
import com.android.util.Pair;
import com.android.utils.ILogger;
import com.google.android.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import sk.arsi.netbeans.gradle.android.layout.impl.v2.AndroidXRemmaper;

@SuppressWarnings("deprecation") // For Pair
public class LayoutLibCallback extends LayoutlibCallback {

    private final Map<Integer, Pair<ResourceType, String>> mProjectResources = Maps.newHashMap();
    private final Map<ResourceType, Map<String, Integer>> mResources = Maps.newHashMap();
    private final ILogger mLog;
    private final ActionBarCallback mActionBarCallback = new ActionBarCallback();
    private String package_name;
    private final AtomicInteger counter = new AtomicInteger(0x7F060000);
    private final List<File> aars;
    private final LayoutClassLoader classLoader;
    private final ResourceNamespace appNamespace;
    private final ProjectLayoutClassLoader projectLayoutClassLoader;
    private boolean hasLegacyAppCompat = false;
    private boolean hasAndroidXAppCompat = false;

    public LayoutLibCallback(ILogger logger, List<File> aars, LayoutClassLoader classLoader, ResourceNamespace appNamespace, ProjectLayoutClassLoader projectLayoutClassLoader) {
        mLog = logger;
        this.aars = aars;
        this.classLoader = classLoader;
        this.appNamespace = appNamespace;
        this.projectLayoutClassLoader = projectLayoutClassLoader;
        try {
            projectLayoutClassLoader.loadClass("androidx.appcompat.app.WindowDecorActionBar");
            hasAndroidXAppCompat = true;
            hasLegacyAppCompat = false;
        } catch (ClassNotFoundException ex) {
            hasAndroidXAppCompat = false;
            hasLegacyAppCompat = false;
        }
        if (!hasAndroidXAppCompat) {
            try {
                projectLayoutClassLoader.loadClass("android.support.v7.app.WindowDecorActionBar");
                hasAndroidXAppCompat = false;
                hasLegacyAppCompat = true;
            } catch (ClassNotFoundException ex) {
                hasAndroidXAppCompat = false;
                hasLegacyAppCompat = false;
            }

        }
    }

    @Override
    public Object loadView(String name, Class[] constructorSignature, Object[] constructorArgs)
            throws Exception {
        if (hasAndroidXAppCompat) {
            name = AndroidXRemmaper.toAndroidX(name);
        } else if (hasLegacyAppCompat) {
            name = AndroidXRemmaper.fromAndroidX(name);
        }
        try {
            Class<?> viewClass = projectLayoutClassLoader.loadClass(name);
            Constructor<?> viewConstructor = viewClass.getConstructor(constructorSignature);
            viewConstructor.setAccessible(true);
            return viewConstructor.newInstance(constructorArgs);
        } catch (Exception e) {
            LayoutIO.logError("unable to load view " + name, e);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object loadClass(String name, Class[] constructorSignature, Object[] constructorArgs) throws ClassNotFoundException {
        try {
            if (hasAndroidXAppCompat) {
                name = AndroidXRemmaper.toAndroidX(name);
            } else if (hasLegacyAppCompat) {
                name = AndroidXRemmaper.fromAndroidX(name);
            }
            Class<?> viewClass = projectLayoutClassLoader.loadClass(name);
            Constructor<?> viewConstructor = viewClass.getConstructor(constructorSignature);
            viewConstructor.setAccessible(true);
            return viewConstructor.newInstance(constructorArgs);
        } catch (Exception e) {
            LayoutIO.logError("unable to load class " + name, e);

        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            if (hasAndroidXAppCompat) {
                name = AndroidXRemmaper.toAndroidX(name);
            } else if (hasLegacyAppCompat) {
                name = AndroidXRemmaper.fromAndroidX(name);
            }
            return projectLayoutClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            LayoutIO.logError("unable to load view " + name, e);
            throw e;
        }
    }

    @Override
    public ResourceNamespace.Resolver getImplicitNamespaces() {
        final ResourceNamespace.Resolver implicitNamespaces = super.getImplicitNamespaces(); //To change body of generated methods, choose Tools | Templates.
        return new ResourceNamespace.Resolver() {
            @Override
            public String prefixToUri(String namespacePrefix) {
                return implicitNamespaces.prefixToUri(namespacePrefix);
            }

            @Override
            public String uriToPrefix(String namespaceUri) {
                return null;
            }

        };
    }

    @Override
    public String getNamespace() {
        return String.format(SdkConstants.NS_CUSTOM_RESOURCES_S,
                ResourceNamespace.RES_AUTO.getPackageName());
    }

    @Override
    public boolean hasLegacyAppCompat() {
        return hasLegacyAppCompat;
    }

    @Override
    public boolean hasAndroidXAppCompat() {
        return hasAndroidXAppCompat;
    }

    @Override
    public ILayoutPullParser getParser(ResourceValue layoutResource) {
        if (layoutResource.getValue() != null) {
            return new LayoutPullParser(new File(layoutResource.getValue()), ResourceNamespace.RES_AUTO);
        }
        return null;
    }

    @Override
    public Object getAdapterItemValue(ResourceReference adapterView, Object adapterCookie,
            ResourceReference itemRef, int fullPosition, int positionPerType,
            int fullParentPosition, int parentPositionPerType, ResourceReference viewRef,
            ViewAttribute viewAttribute, Object defaultValue) {
        return null;
    }

    @Override
    public AdapterBinding getAdapterBinding(ResourceReference adapterViewRef, Object adapterCookie,
            Object viewObject) {
        return null;
    }

    @Override
    public ActionBarCallback getActionBarCallback() {
        return mActionBarCallback;
    }

    @Override
    public boolean supports(int ideFeature) {
        return true;
    }

    @Override
    public <T> T getFlag(Key<T> key) {
        if (key.equals(RenderParamsFlags.FLAG_KEY_APPLICATION_PACKAGE)) {
            return (T) ResourceNamespace.RES_AUTO;
        }
        if (key.equals(FLAG_KEY_RECYCLER_VIEW_SUPPORT)) {
            return (T) Boolean.TRUE;
        }
        if (key.equals(FLAG_KEY_XML_FILE_PARSER_SUPPORT)) {
            return (T) Boolean.TRUE;
        }
        return null;
    }
    //0x7f0f0005

    @Override
    public int getOrGenerateResourceId(ResourceReference resource) {
        int id = classLoader.getClassGeneratorConfig().getOrCreateId(resource.getNamespace(), resource.getResourceType(), resource.getName());
        return id;
    }

    @Override
    public XmlPullParser createXmlParserForPsiFile(String fileName) {
        return null;
    }

    public static final byte PROTO_XML_LEAD_BYTE = 0x0A;

    @Override
    public XmlPullParser createXmlParserForFile(String fileName) {
        try {
            ByteArrayInputStream stream = openFile(fileName);
            // Instantiate an XML pull parser based on the contents of the stream.
            XmlPullParser parser;
            int c = stream.read();
            stream.reset();
            if (c == PROTO_XML_LEAD_BYTE) {
                parser = new NamedXmlParser(fileName); // Parser for regular text XML.
            } else {
                parser = new NamedXmlParser(fileName); // Parser for regular text XML.
            }
            parser.setInput(stream, null);
            return parser;
        } catch (IOException | XmlPullParserException e) {
            return null;
        }
    }

    private static ByteArrayInputStream openFile(String filePath) throws IOException {
        try (FileInputStream fileStream = new FileInputStream(filePath)) {
            // Read data fully to memory to be able to close the file stream.
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ByteStreams.copy(fileStream, byteOutputStream);
            return new ByteArrayInputStream(byteOutputStream.toByteArray());
        }
    }

    @Override
    public XmlPullParser createXmlParser() {
        return new NamedXmlParser(null);
    }

    @Override
    public ResourceReference resolveResourceId(int id) {
        ResourceReference reference = classLoader.getClassGeneratorConfig().findReference(id);
        if (reference == null) {
            System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.LayoutLibTestCallback.resolveResourceId()");
        }
        return reference;
    }

    @Override
    public boolean isResourceNamespacingRequired() {
        return false; //To change body of generated methods, choose Tools | Templates.
    }

    private static class NamedXmlParser extends KXmlParser {

        @Nullable
        private final String myName;
        /**
         * Attribute that caches whether the tools prefix has been defined or
         * not. This allows us to save unnecessary checks in the most common
         * case ("tools" is not defined).
         */
        private boolean hasToolsNamespace;

        public NamedXmlParser(@Nullable String name) {
            myName = name;
            try {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            } catch (XmlPullParserException e) {
                throw new Error("Internal error", e);
            }
        }

        @Override
        public int next() throws XmlPullParserException, IOException {
            int tagType = super.next();

            // We check if the tools namespace is still defined in two cases:
            // - If it's a start tag and it was defined by a previous tag
            // - If it WAS defined by a previous tag, and we are closing a tag (going out of scope)
            if ((!hasToolsNamespace && tagType == XmlPullParser.START_TAG)
                    || (hasToolsNamespace && tagType == XmlPullParser.END_TAG)) {
                hasToolsNamespace = getNamespace("tools") != null;
            }

            return tagType;
        }

        @Override
        public String getAttributeValue(@Nullable String namespace, @NotNull String localName) {
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
            System.out.println(">>> " + myName + " >>> " + localName + " >>>>> " + value);
            return value;
        }

        @Override
        public String toString() {
            return myName != null ? myName : super.toString();
        }
    }

}
