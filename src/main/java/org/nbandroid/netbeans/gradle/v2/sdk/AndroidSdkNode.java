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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JPanel;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
import org.nbandroid.netbeans.gradle.v2.sdk.ui.AndroidSdkCustomizer;
import org.nbandroid.netbeans.gradle.v2.sdk.ui.BrokenPlatformCustomizer;
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
class AndroidSdkNode extends AbstractNode implements PropertyChangeListener, BrokenPlatformCustomizer.SdkValidListener {

    private final AndroidSdkImpl platform;
    private final XMLDataObject holder;

    public AndroidSdkNode(AndroidSdkImpl p, XMLDataObject holder) {
        super(Children.create(new AndroidPlatformChildrenFactory(p, holder), false), Lookups.fixed(new Object[]{p, holder}));
        this.platform = p;
        p.addPropertyChangeListener(WeakListeners.propertyChange(this, AndroidSdkImpl.DEFAULT_PLATFORM, p));
        this.holder = holder;
        //   super.setIconBaseWithExtension("org/netbeans/modules/java/j2seplatform/resources/platform.gif");
    }

    @Override
    public String getDisplayName() {
        return platform.getDisplayName();
    }

    @Override
    public String getHtmlDisplayName() {
        return platform.getHtmlDisplayName();
    }

    @Override
    public Image getIcon(int type) {
        if (platform.isValid()) {
            return IconProvider.IMG_ANDROID_SDK_ICON;
        } else {
            return IconProvider.IMG_ANDROID_SDK_BROKEN_ICON;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    private final AtomicReference<JPanel> lastBrokenPanel = new AtomicReference<>(null);

    @Override
    public java.awt.Component getCustomizer() {
        if (platform.isValid()) {
            return new AndroidSdkCustomizer(platform, holder);
        } else {
            JPanel tmp = new JPanel();
            tmp.setLayout(new java.awt.CardLayout());
            tmp.add(new BrokenPlatformCustomizer(platform, holder, this));
            tmp.invalidate();
            tmp.repaint();
            lastBrokenPanel.set(tmp);
            return tmp;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Boolean.TRUE.equals(evt.getNewValue())) {
            fireDisplayNameChange(getDisplayName(), getHtmlDisplayName());
        } else {
            fireDisplayNameChange(getHtmlDisplayName(), getDisplayName());
        }
    }

    @Override
    public void sdkValid() {
        fireIconChange();
        fireOpenedIconChange();
        setChildren(Children.create(new AndroidPlatformChildrenFactory(platform, holder), false));
        JPanel tmp = lastBrokenPanel.get();
        if (tmp != null) {
            tmp.removeAll();
            tmp.invalidate();
            tmp.repaint();
            tmp.setLayout(new java.awt.CardLayout());
            tmp.add(new AndroidSdkCustomizer(platform, holder));
            tmp.revalidate();
            tmp.repaint();
            tmp.requestFocus();
        }
    }

    private static class AndroidPlatformChildrenFactory extends ChildFactory<AndroidPlatformInfo> implements LocalPlatformChangeListener {

        private final AndroidSdkImpl platformImpl;
        private final XMLDataObject holder;
        private final AtomicReference<List<AndroidPlatformInfo>> platforms = new AtomicReference<>();

        public AndroidPlatformChildrenFactory(AndroidSdkImpl platformImpl, XMLDataObject holder) {
            this.platformImpl = platformImpl;
            platforms.set(AndroidSdkTools.orderByApliLevel(platformImpl.getPlatforms()));
            this.holder = holder;
            platformImpl.addLocalPlatformChangeListener(this);
        }

        @Override
        protected boolean createKeys(List<AndroidPlatformInfo> toPopulate) {
            if (platformImpl.isValid()) {
                List<AndroidPlatformInfo> tmp = platforms.get();
                toPopulate.addAll(tmp);
            }
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
