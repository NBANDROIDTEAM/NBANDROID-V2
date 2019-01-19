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

import android.annotation.NonNull;
import android.annotation.Nullable;
import com.android.SdkConstants;
import com.android.ide.common.rendering.api.ActionBarCallback;
import com.android.ide.common.rendering.api.AdapterBinding;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.LayoutlibCallback;
import com.android.ide.common.rendering.api.ParserFactory;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.SessionParams.Key;
import com.android.ide.common.resources.IntArrayWrapper;
import com.android.layoutlib.bridge.android.RenderParamsFlags;
import com.android.resources.ResourceType;
import com.android.util.Pair;
import com.android.utils.ILogger;
import com.google.android.collect.Maps;
import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@SuppressWarnings("deprecation") // For Pair
public class LayoutLibTestCallback extends LayoutlibCallback {

    private final Map<Integer, Pair<ResourceType, String>> mProjectResources = Maps.newHashMap();
    private final Map<IntArrayWrapper, String> mStyleableValueToNameMap = Maps.newHashMap();
    private final Map<ResourceType, Map<String, Integer>> mResources = Maps.newHashMap();
    private final ILogger mLog;
    private final ActionBarCallback mActionBarCallback = new ActionBarCallback();
    private String package_name;
    private final AtomicInteger counter = new AtomicInteger(0x7F060000);

    public LayoutLibTestCallback(ILogger logger) {
        mLog = logger;
    }

    public void initResources() throws ClassNotFoundException {
    }

    @Override
    public Object loadView(String name, Class[] constructorSignature, Object[] constructorArgs)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNamespace() {
        return String.format(SdkConstants.NS_CUSTOM_RESOURCES_S,
                "aa");
    }

    @Override
    public Pair<ResourceType, String> resolveResourceId(int id) {
        return mProjectResources.get(id);
    }

    @Override
    public String resolveResourceId(int[] id) {
        return mStyleableValueToNameMap.get(new IntArrayWrapper(id));
    }

    @Override
    public Integer getResourceId(ResourceType type, String name) {
        Map<String, Integer> resName2Id = mResources.get(type);
        if (resName2Id == null) {
            int incrementAndGet = counter.incrementAndGet();
            System.out.println(">" + type + ":" + name + " : " + incrementAndGet);
            return incrementAndGet;
        }
        return resName2Id.get(name);
    }

    @Override
    public ILayoutPullParser getParser(String layoutName) {
        return null;
    }

    @Override
    public ILayoutPullParser getParser(ResourceValue layoutResource) {
        return new LayoutPullParser(new File(layoutResource.getValue()));
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

    @NonNull
    @Override
    public ParserFactory getParserFactory() {
        return new ParserFactory() {
            @NonNull
            @Override
            public XmlPullParser createParser(@Nullable String debugName)
                    throws XmlPullParserException {
                return new KXmlParser();
            }
        };
    }

    @Override
    public <T> T getFlag(Key<T> key) {
        if (key.equals(RenderParamsFlags.FLAG_KEY_APPLICATION_PACKAGE)) {
            return (T) "aa";
        }
        return null;
    }
}
