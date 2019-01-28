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

import com.android.SdkConstants;
import com.android.ide.common.rendering.api.ActionBarCallback;
import com.android.ide.common.rendering.api.AdapterBinding;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.LayoutlibCallback;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.SessionParams.Key;
import com.android.layoutlib.bridge.android.RenderParamsFlags;
import com.android.resources.ResourceType;
import com.android.util.Pair;
import com.android.utils.ILogger;
import com.google.android.collect.Maps;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

@SuppressWarnings("deprecation") // For Pair
public class LayoutLibTestCallback extends LayoutlibCallback {

    private final Map<Integer, Pair<ResourceType, String>> mProjectResources = Maps.newHashMap();
    private final Map<ResourceType, Map<String, Integer>> mResources = Maps.newHashMap();
    private final ILogger mLog;
    private final ActionBarCallback mActionBarCallback = new ActionBarCallback();
    private String package_name;
    private final AtomicInteger counter = new AtomicInteger(0x7F060000);
    private final List<File> aars;
    private final LayoutClassLoader classLoader;
    private final ResourceNamespace appNamespace;

    public LayoutLibTestCallback(ILogger logger, List<File> aars, LayoutClassLoader classLoader, ResourceNamespace appNamespace) {
        mLog = logger;
        this.aars = aars;
        this.classLoader = classLoader;
        this.appNamespace = appNamespace;
        initResources();
    }

    public void initResources() {
    }

    @Override
    public Object loadView(String name, Class[] constructorSignature, Object[] constructorArgs)
            throws Exception {
        try {
            Class<?> viewClass = classLoader.loadClass(name);
            Constructor<?> viewConstructor = viewClass.getConstructor(constructorSignature);
            viewConstructor.setAccessible(true);
            return viewConstructor.newInstance(constructorArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException();
    }


    @Override
    public String getNamespace() {
        return String.format(SdkConstants.NS_CUSTOM_RESOURCES_S,
                appNamespace.getPackageName());
    }


    @Override
    public boolean hasLegacyAppCompat() {
        return true; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasAndroidXAppCompat() {
        return false; //To change body of generated methods, choose Tools | Templates.
    }



    @Override
    public ILayoutPullParser getParser(ResourceValue layoutResource) {
        return new LayoutPullParser(new File(layoutResource.getValue()), appNamespace);
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
            return (T) appNamespace.getPackageName();
        }
        return null;
    }
    //0x7f0f0005

    @Override
    public int getOrGenerateResourceId(ResourceReference resource) {
        Integer value = classLoader.getResourceTypeToId().get(resource.getResourceType());
        if (value != null) {
            return value;
        }
        int incrementAndGet = counter.incrementAndGet();
        classLoader.getResourceTypeToId().put(resource.getResourceType(), incrementAndGet);
        classLoader.getIdToReferences().put(incrementAndGet, resource);
        System.out.println(">" + resource);
        return incrementAndGet;
    }

    @Override
    public XmlPullParser createXmlParserForPsiFile(String fileName) {
        return null;
    }

    @Override
    public XmlPullParser createXmlParserForFile(String fileName) {
        System.out.println(fileName);
        return new LayoutPullParser(fileName, ResourceNamespace.ANDROID);
    }

    @Override
    public XmlPullParser createXmlParser() {
        return new KXmlParser();
    }


    @Override
    public ResourceReference resolveResourceId(int id) {
        ResourceReference reference = classLoader.getIdToReferences().get(id);
        System.out.println(">>>>" + reference);
        return reference;
    }

    @Override
    public boolean isResourceNamespacingRequired() {
        return true; //To change body of generated methods, choose Tools | Templates.
    }

}
