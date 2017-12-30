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
package org.nbandroid.netbeans.gradle.v2.ui;

import java.awt.Image;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 *
 * @author arsi
 */
public class IconProvider {

    @StaticResource
    private static final String RESOURCE_UPDATE = "org/nbandroid/netbeans/gradle/ui/update.png";
    public static final Image IMG_UPDATE = ImageUtilities.loadImage(RESOURCE_UPDATE);

    @StaticResource
    private static final String RESOURCE_LOCAL = "org/nbandroid/netbeans/gradle/ui/local.png";
    public static final Image IMG_LOCAL = ImageUtilities.loadImage(RESOURCE_LOCAL);

    @StaticResource
    private static final String RESOURCE_REMOTE = "org/nbandroid/netbeans/gradle/ui/remote.png";
    public static final Image IMG_REMOTE = ImageUtilities.loadImage(RESOURCE_REMOTE);

    @StaticResource
    private static final String RESOURCE_FOLDER_UPDATE = "org/nbandroid/netbeans/gradle/ui/folder_update.png";
    public static final Image IMG_FOLDER_UPDATE = ImageUtilities.loadImage(RESOURCE_FOLDER_UPDATE);

    @StaticResource
    private static final String RESOURCE_FOLDER_LOCAL = "org/nbandroid/netbeans/gradle/ui/folder_local.png";
    public static final Image IMG_FOLDER_LOCAL = ImageUtilities.loadImage(RESOURCE_FOLDER_LOCAL);

    @StaticResource
    private static final String RESOURCE_FOLDER_REMOTE = "org/nbandroid/netbeans/gradle/ui/folder.png";
    public static final Image IMG_FOLDER_REMOTE = ImageUtilities.loadImage(RESOURCE_FOLDER_REMOTE);

    @StaticResource
    private static final String RESOURCE_FOLDER_SUPPORT = "org/nbandroid/netbeans/gradle/ui/folder_support.png";
    public static final Image IMG_FOLDER_SUPPORT = ImageUtilities.loadImage(RESOURCE_FOLDER_SUPPORT);

    @StaticResource
    private static final String RESOURCE_ANDROID_LIBRARY = "org/nbandroid/netbeans/gradle/ui/AndroidDependencyIcon.png";
    public static final Image IMG_ANDROID_LIBRARY = ImageUtilities.loadImage(RESOURCE_ANDROID_LIBRARY);

    @StaticResource
    private static final String RESOURCE_DEPENDENCY_JAVADOCINCLUDED = "org/nbandroid/netbeans/gradle/ui/DependencyJavadocIncluded.png";
    public static final Image IMG_DEPENDENCY_JAVADOCINCLUDED = ImageUtilities.loadImage(RESOURCE_DEPENDENCY_JAVADOCINCLUDED);

    @StaticResource
    private static final String RESOURCE_DEPENDENCY_JAVASRCINCLUDED = "org/nbandroid/netbeans/gradle/ui/DependencySrcIncluded.png";
    public static final Image IMG_DEPENDENCY_JAVASRCINCLUDED = ImageUtilities.loadImage(RESOURCE_DEPENDENCY_JAVASRCINCLUDED);

    @StaticResource
    private static final String RESOURCE_DEPENDENCY_BROKEN = "org/nbandroid/netbeans/gradle/ui/brokenProjectBadge.png";
    public static final Image IMG_DEPENDENCY_BROKEN = ImageUtilities.loadImage(RESOURCE_DEPENDENCY_BROKEN);

    @StaticResource
    private static final String RESOURCE_ANDROID_SDK_ICON = "org/nbandroid/netbeans/gradle/v2/sdk/ui/android-sdk.png";
    public static final Image IMG_ANDROID_SDK_ICON = ImageUtilities.loadImage(RESOURCE_ANDROID_SDK_ICON);

    @StaticResource
    private static final String RESOURCE_ANDROID_SDK_BROKEN_ICON = "org/nbandroid/netbeans/gradle/v2/sdk/ui/android-sdk-broken.png";
    public static final Image IMG_ANDROID_SDK_BROKEN_ICON = ImageUtilities.loadImage(RESOURCE_ANDROID_SDK_BROKEN_ICON);

    @StaticResource
    private static final String RESOURCE_USB = "org/netbeans/modules/android/project/ui/resources/usb_badge.png";
    public static final Image IMG_USB_BADGE = ImageUtilities.loadImage(RESOURCE_USB);

    @StaticResource
    private static final String RESOURCE_WIFI = "org/netbeans/modules/android/project/ui/resources/wifi_badge.png";
    public static final Image IMG_WIFI_BADGE = ImageUtilities.loadImage(RESOURCE_WIFI);

    @StaticResource
    private static final String RESOURCE_APKS = "org/nbandroid/netbeans/gradle/v2/apk/apks.png";
    public static final Image IMG_APKS = ImageUtilities.loadImage(RESOURCE_APKS);

    @StaticResource
    private static final String RESOURCE_APK = "org/nbandroid/netbeans/gradle/apk/apk.png";
    public static final Image IMG_APK = ImageUtilities.loadImage(RESOURCE_APK);

    @StaticResource
    private static final String RESOURCE_V1 = "org/nbandroid/netbeans/gradle/apk/apkv1.png";
    public static final Image IMG_V1_BADGE = ImageUtilities.loadImage(RESOURCE_V1);

    @StaticResource
    private static final String RESOURCE_V12 = "org/nbandroid/netbeans/gradle/apk/apkv12.png";
    public static final Image IMG_V12_BADGE = ImageUtilities.loadImage(RESOURCE_V12);

    @StaticResource
    private static final String RESOURCE_V2 = "org/nbandroid/netbeans/gradle/apk/apkv2.png";
    public static final Image IMG_V2_BADGE = ImageUtilities.loadImage(RESOURCE_V2);
}
