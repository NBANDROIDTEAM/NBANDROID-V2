/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.repository.api.UpdatablePackage;
import java.awt.Image;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
import org.nbandroid.netbeans.gradle.v2.sdk.ui.AndroidSdkCustomizer;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
class AndroidSdkNode extends AbstractNode {

    private final AndroidSdkImpl platform;
    private final XMLDataObject holder;

    public AndroidSdkNode(AndroidSdkImpl p, XMLDataObject holder) {
        super(Children.create(new AndroidPlatformChildrenFactory(p, holder), true), Lookups.fixed(new Object[]{p, holder}));
        this.platform = p;
        this.holder = holder;
        //   super.setIconBaseWithExtension("org/netbeans/modules/java/j2seplatform/resources/platform.gif");
    }

    @Override
    public String getDisplayName() {
        return platform.getDisplayName(); //To change body of generated methods, choose Tools | Templates.
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


    private static class AndroidPlatformChildrenFactory extends ChildFactory<UpdatablePackage> implements LocalPlatformChangeListener {

        private final AndroidSdkImpl platformImpl;
        private final XMLDataObject holder;
        private final AtomicReference<Vector<UpdatablePackage>> platforms = new AtomicReference<>(new Vector<UpdatablePackage>());

        public AndroidPlatformChildrenFactory(AndroidSdkImpl platformImpl, XMLDataObject holder) {
            this.platformImpl = platformImpl;
            this.holder = holder;
            platformImpl.addLocalPlatformChangeListener(this);
        }

        @Override
        protected boolean createKeys(List<UpdatablePackage> toPopulate) {
            Vector<UpdatablePackage> tmp = platforms.get();
            toPopulate.addAll(tmp);
            return true;
        }

        @Override
        protected Node createNodeForKey(UpdatablePackage key) {
            return new AndroidPlatformNode(key, platformImpl, holder);
        }

        @Override
        public void platformListChanged(Vector<UpdatablePackage> platforms) {
            this.platforms.set(platforms);
            refresh(true);
        }

    }



}
