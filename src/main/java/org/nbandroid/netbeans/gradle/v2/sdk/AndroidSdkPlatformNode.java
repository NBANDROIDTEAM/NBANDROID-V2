/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.sdk;

import org.openide.loaders.XMLDataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
class AndroidSdkPlatformNode extends AbstractNode {

    private final AndroidSdkPlatform platform;

    public AndroidSdkPlatformNode(AndroidSdkPlatform p, XMLDataObject holder) {
        super(Children.LEAF, Lookups.fixed(new Object[]{p, holder}));
        this.platform = p;
        //   super.setIconBaseWithExtension("org/netbeans/modules/java/j2seplatform/resources/platform.gif");
    }

    @Override
    public String getDisplayName() {
        return platform.getDisplayName(); //To change body of generated methods, choose Tools | Templates.
    }


}
