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
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.sdklib.SdkVersionInfo;
import java.awt.Image;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
public class AndroidPlatformNode extends AbstractNode {

    private final AndroidPlatformInfo aPackage;

    AndroidPlatformNode(AndroidPlatformInfo aPackage, AndroidSdkImpl platform, XMLDataObject holder) {
        super(Children.LEAF, Lookups.fixed(new Object[]{platform, holder, aPackage}));
        this.aPackage = aPackage;
    }

    @Override
    public String getDisplayName() {
        return SdkVersionInfo.getVersionWithCodename(aPackage.getAndroidVersion());
    }

    @Override
    public Image getIcon(int type) {
        return IconProvider.IMG_ANDROID_LIBRARY; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type); //To change body of generated methods, choose Tools | Templates.
    }

}
