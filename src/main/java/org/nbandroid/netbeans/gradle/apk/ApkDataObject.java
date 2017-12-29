/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nbandroid.netbeans.gradle.apk;

import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import org.nbandroid.netbeans.gradle.v2.apk.actions.InstallApkAction;
import org.nbandroid.netbeans.gradle.v2.apk.actions.SaveAsAction;
import org.nbandroid.netbeans.gradle.v2.apk.sign.keystore.SignApkAction;
import org.nbandroid.netbeans.gradle.v2.ui.IconProvider;
import org.openide.actions.CopyAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

@NbBundle.Messages("APKMIME_DisplayName=Android Packages (.apk)")
@DataObject.Registration(displayName = "#APKDataObject_DisplayName",
        iconBase = "org/nbandroid/netbeans/gradle/apk/apk.png",
        mimeType = "application/vnd.android.package-archive",
        position = 1023)
// register before JAR/ZIP
@MIMEResolver.ExtensionRegistration(
        displayName = "#APKMIME_DisplayName", extension = "apk", mimeType = "application/vnd.android.package-archive",
        position = 479)
public class ApkDataObject extends MultiDataObject implements Deployable {

    public static enum SignInfo {
        SIGNED_NOT,
        SIGNED_V1,
        SIGNED_V2,
        SIGNED_V1V2
    }

    private final SignInfo signInfo;

    public ApkDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        int fastSignInfo = org.nbandroid.netbeans.gradle.v2.apk.ApkUtils.fastSignInfo(FileUtil.toFile(pf));
        switch (fastSignInfo) {
            case org.nbandroid.netbeans.gradle.v2.apk.ApkUtils.SIGNED_NOT:
                signInfo = SignInfo.SIGNED_NOT;
                break;
            case org.nbandroid.netbeans.gradle.v2.apk.ApkUtils.SIGNED_V1:
                signInfo = SignInfo.SIGNED_V1;
                break;
            case org.nbandroid.netbeans.gradle.v2.apk.ApkUtils.SIGNED_V2:
                signInfo = SignInfo.SIGNED_V2;
                break;
            case org.nbandroid.netbeans.gradle.v2.apk.ApkUtils.SIGNED_V1V2:
                signInfo = SignInfo.SIGNED_V1V2;
                break;
            default:
                signInfo = SignInfo.SIGNED_NOT;

        }
    }

    @Override
    protected Node createNodeDelegate() {
        return new ApkFilterNode(new DataNode(this, Children.LEAF, getLookup()));
    }

    @Override
    public Lookup getLookup() {
        return new ProxyLookup(getCookieSet().getLookup(), Lookups.fixed(signInfo));
    }

    @Override
    public FileObject getDeployableFile() {
        return getPrimaryFile();
    }

    private class ApkFilterNode extends FilterNode {

        public ApkFilterNode(Node original) {
            super(original);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                SystemAction.get(SignApkAction.class),
                SystemAction.get(SaveAsAction.class),
                SystemAction.get(InstallApkAction.class),
                SystemAction.get(CopyAction.class),
                SystemAction.get(PropertiesAction.class)
            };
        }

        @Override
        public Image getIcon(int type) {
            return makeIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return makeIcon();
        }

        private Image makeIcon() {
            switch (signInfo) {
                case SIGNED_NOT:
                    return IconProvider.IMG_APK;
                case SIGNED_V1:
                    return IconProvider.IMG_V1_BADGE;
                case SIGNED_V2:
                    return IconProvider.IMG_V2_BADGE;
                case SIGNED_V1V2:
                    return IconProvider.IMG_V12_BADGE;
                default:
                    return IconProvider.IMG_APK;

            }
        }


    }
}
