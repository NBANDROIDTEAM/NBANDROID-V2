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
package org.nbandroid.netbeans.gradle.platform;

import com.android.repository.api.UpdatablePackage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import org.nbandroid.netbeans.gradle.v2.sdk.LocalPlatformChangeListener;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * JavaPlatformProvider for Android platforms and java 1.7
 *
 * @author arsi
 */
@ServiceProvider(service = JavaPlatformProvider.class)
public class AndroidPlatformProviderJava17 implements JavaPlatformProvider, LocalPlatformChangeListener {

    private final Vector<PropertyChangeListener> listeners = new Vector<>();
    private final AtomicReference<JavaPlatform[]> platforms = new AtomicReference<>(new JavaPlatform[0]);
    private final AtomicReference<JavaPlatform> defaultPlatform = new AtomicReference<>(null);

    public AndroidPlatformProviderJava17() {
        SdkManager.getDefault().addLocalPlatformChangeListener(this);
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
    public void platformListChanged(Vector<UpdatablePackage> pkgs) {
        JavaPlatform[] tmp = new JavaPlatform[pkgs.size()];
        for (int i = 0; i < pkgs.size(); i++) {
            tmp[i] = new AndroidPlatform(pkgs.get(i), "1.7");
        }
        platforms.set(tmp);
        if (tmp.length > 0) {
            defaultPlatform.set(tmp[0]);
        }
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(new PropertyChangeEvent(this, PROP_INSTALLED_PLATFORMS, null, platforms));
        }
    }

}
