/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.sdk;

import java.awt.Image;
import java.util.List;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
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
class AndroidSdkPlatformNode extends AbstractNode {

    private final AndroidSdkPlatformImpl platform;

    public AndroidSdkPlatformNode(AndroidSdkPlatformImpl p, XMLDataObject holder) {
        super(Children.create(new AndroidPlatformChildrenFactory(), false), Lookups.fixed(new Object[]{p, holder}));
        this.platform = p;
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

    private static class AndroidPlatformChildrenFactory extends ChildFactory<String> {

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            toPopulate.add("aaa");
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            return new AndroidPlatformNode();
        }

    }



}
