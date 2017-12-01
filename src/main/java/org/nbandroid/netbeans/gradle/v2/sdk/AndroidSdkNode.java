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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
import org.nbandroid.netbeans.gradle.v2.sdk.ui.AndroidSdkCustomizer;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
class AndroidSdkNode extends AbstractNode implements PropertyChangeListener {

    private final AndroidSdkImpl platform;
    private final XMLDataObject holder;

    public AndroidSdkNode(AndroidSdkImpl p, XMLDataObject holder) {
        super(Children.create(new AndroidPlatformChildrenFactory(p, holder), true), Lookups.fixed(new Object[]{p, holder}));
        this.platform = p;
        p.addPropertyChangeListener(WeakListeners.propertyChange(this, AndroidSdkImpl.DEFAULT_PLATFORM, p));
        this.holder = holder;
        //   super.setIconBaseWithExtension("org/netbeans/modules/java/j2seplatform/resources/platform.gif");
    }

    @Override
    public String getDisplayName() {
        return platform.getDisplayName(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getHtmlDisplayName() {
        return platform.getHtmlDisplayName(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Image getIcon(int type) {
        return IconProvider.IMG_ANDROID_SDK_ICON; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public java.awt.Component getCustomizer() {
        return new AndroidSdkCustomizer(platform, holder);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Boolean.TRUE.equals(evt.getNewValue())) {
            fireDisplayNameChange(getDisplayName(), getHtmlDisplayName());
        } else {
            fireDisplayNameChange(getHtmlDisplayName(), getDisplayName());
        }
    }

    private static class AndroidPlatformChildrenFactory extends ChildFactory<AndroidPlatformInfo> implements LocalPlatformChangeListener {

        private final AndroidSdkImpl platformImpl;
        private final XMLDataObject holder;
        private final AtomicReference<List<AndroidPlatformInfo>> platforms = new AtomicReference<>(new ArrayList<AndroidPlatformInfo>());

        public AndroidPlatformChildrenFactory(AndroidSdkImpl platformImpl, XMLDataObject holder) {
            this.platformImpl = platformImpl;
            this.holder = holder;
            platformImpl.addLocalPlatformChangeListener(this);
        }

        @Override
        protected boolean createKeys(List<AndroidPlatformInfo> toPopulate) {
            List<AndroidPlatformInfo> tmp = platforms.get();
            toPopulate.addAll(tmp);
            return true;
        }

        @Override
        protected Node createNodeForKey(AndroidPlatformInfo key) {
            return new AndroidPlatformNode(key, platformImpl, holder);
        }

        @Override
        public void platformListChanged(List<AndroidPlatformInfo> platforms) {
            this.platforms.set(platforms);
            refresh(true);
        }

    }

}
