/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.repository.api.LocalPackage;
import com.android.repository.api.RepoManager;
import com.android.repository.api.UpdatablePackage;
import com.android.sdklib.repository.AndroidSdkHandler;
import org.openide.util.Lookup;

/**
 * SDK Manager
 *
 * @author arsi
 */
public abstract class SdkManager {

    /**
     * Get default SDK manager
     *
     * @return SdkManager
     */
    public static SdkManager getDefault() {
        return Lookup.getDefault().lookup(SdkManager.class);
    }

    /**
     * Add SdkPlatformChangeListener to listen of SDK platform pakages list
     * changes On add is listener called with actual package list
     *
     * @param l SdkPlatformChangeListener
     */
    public abstract void addSdkPlatformChangeListener(SdkPlatformChangeListener l);

    /**
     * Remove SdkPlatformChangeListener
     *
     * @param l SdkPlatformChangeListener
     */
    public abstract void removeSdkPlatformChangeListener(SdkPlatformChangeListener l);

    public abstract AndroidSdkHandler getAndroidSdkHandler();

    /**
     * Get Android repo manager
     *
     * @return RepoManager or null if no SDK location is set
     */
    public abstract RepoManager getRepoManager();

    /**
     * Update SDK platform pakages list After update is fired
     * SdkPlatformChangeListener and SdkToolsChangeListener
     */
    public abstract void updateSdkPlatformPackages();

    /**
     * Add addSdkToolsChangeListener to listen of SDK tools pakages list changes
     * On add is listener called with actual package list
     *
     * @param l addSdkToolsChangeListener
     */
    public abstract void addSdkToolsChangeListener(SdkToolsChangeListener l);

    /**
     * Remove SdkToolsChangeListener
     *
     * @param l SdkToolsChangeListener
     */
    public abstract void removeSdkToolsChangeListener(SdkToolsChangeListener l);

    /**
     * Uninstall android package After unistall is called
     * updateSdkPlatformPackages()
     *
     * @param aPackage LocalPackage
     */
    public abstract void uninstallPackage(LocalPackage aPackage);

    /**
     * Install or Update Android package After istall is called
     * updateSdkPlatformPackages()
     *
     * @param aPackage UpdatablePackage
     */
    public abstract void installPackage(final UpdatablePackage aPackage);

    public abstract void addLocalPlatformChangeListener(LocalPlatformChangeListener l);

    public abstract void removeLocalPlatformChangeListener(LocalPlatformChangeListener l);
}
