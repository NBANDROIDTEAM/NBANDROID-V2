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

import com.android.repository.api.RepoManager;
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

    /**
     * Get Android repo manager
     *
     * @return RepoManager or null if no SDK location is set
     */
    public abstract RepoManager getRepoManager();

    /**
     * Update SDK platform pakages list After update is called
     * SdkPlatformChangeListener
     */
    public abstract void updateSdkPlatformPackages();

    public abstract void addSdkToolsChangeListener(SdkToolsChangeListener l);

    public abstract void removeSdkToolsChangeListener(SdkToolsChangeListener l);
}
