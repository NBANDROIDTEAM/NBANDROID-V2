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
package org.nbandroid.netbeans.gradle.v2.sdk.java.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.nbandroid.netbeans.gradle.v2.sdk.LocalPlatformChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author arsi
 */
public abstract class AndroidJavaPlatformProvider implements JavaPlatformProvider, LocalPlatformChangeListener, PropertyChangeListener {

    protected final Vector<PropertyChangeListener> listeners = new Vector<>();
    protected final AtomicReference<JavaPlatform[]> platforms = new AtomicReference<>(new JavaPlatform[0]);
    protected final AtomicReference<JavaPlatform> defaultPlatform = new AtomicReference<>(null);

    public AndroidJavaPlatformProvider() {
        AndroidSdkProvider.getDefault().addPropertyChangeListener(this);
    }

    @Override
    public JavaPlatform[] getInstalledPlatforms() {
        return platforms.get();
    }

    @Override
    public JavaPlatform getDefaultPlatform() {
        return defaultPlatform.get();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void platformListChanged(List<AndroidPlatformInfo> pkgsX) {
        AndroidSdk[] sdKs = AndroidSdkProvider.getDefault().getSDKs();
        Map<FileObject, JavaPlatform> filter = new HashMap<>();
        Map<FileObject, JavaPlatform> filterLast = new HashMap<>();
        List<JavaPlatform> tmp = new ArrayList<>();
        for (AndroidSdk sdk : sdKs) {
            List<AndroidPlatformInfo> pkgs = sdk.getPlatforms();
            for (int i = 0; i < pkgs.size(); i++) {
                createPlatform(tmp, pkgs.get(i));
            }

        }
        if (!tmp.isEmpty()) {
            defaultPlatform.set(tmp.get(0));
            for (JavaPlatform plat : tmp) {
                filter.put(plat.getInstallFolders().iterator().next(), plat);
            }
        }
        JavaPlatform[] current = tmp.toArray(new JavaPlatform[tmp.size()]);
        JavaPlatform[] last = platforms.getAndSet(current);
        if (last != null) {
            for (JavaPlatform plat : last) {
                filterLast.put(plat.getInstallFolders().iterator().next(), plat);
            }
        }
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(new PropertyChangeEvent(this, PROP_INSTALLED_PLATFORMS, last, current));
        }

    }

    protected abstract void createPlatform(List<JavaPlatform> tmp, AndroidPlatformInfo pkg);

    private final AtomicReference<AndroidSdk[]> lastSdks = new AtomicReference<>(null);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AndroidSdkProvider.PROP_INSTALLED_SDKS.equals(evt.getPropertyName())) {
            AndroidSdk[] sdKs = AndroidSdkProvider.getDefault().getSDKs();
            AndroidSdk[] lasts = lastSdks.getAndSet(sdKs);
            if (lasts != null) {
                for (AndroidSdk last : lasts) {
                    last.removeLocalPlatformChangeListener(this);
                }
            }
            for (AndroidSdk sdK : sdKs) {
                sdK.addLocalPlatformChangeListener(this);
            }
        }
    }

}
