/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.repository.api.RepoPackage;
import com.android.repository.api.UpdatablePackage;
import com.android.repository.impl.meta.TypeDetails;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.SdkVersionInfo;
import com.android.sdklib.repository.meta.DetailsTypes;
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

    private final UpdatablePackage aPackage;

    AndroidPlatformNode(UpdatablePackage aPackage, AndroidSdkImpl platform, XMLDataObject holder) {
        super(Children.LEAF, Lookups.fixed(new Object[]{platform, holder, aPackage}));
        this.aPackage = aPackage;
    }

    @Override
    public String getDisplayName() {
        RepoPackage p = aPackage.getRepresentative();
        TypeDetails details = p.getTypeDetails();
        if (details instanceof DetailsTypes.ApiDetailsType) {
            AndroidVersion androidVersion = ((DetailsTypes.ApiDetailsType) details).getAndroidVersion();
            return SdkVersionInfo.getVersionWithCodename(androidVersion);
        } else {
            return aPackage.getLocal().getVersion().toString();
        }
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
