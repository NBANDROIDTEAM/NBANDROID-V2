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
import org.nbandroid.netbeans.gradle.v2.ui.IconProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.actions.CopyAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
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
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
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

    private final FileObject pf;
    private static final RequestProcessor RP = new RequestProcessor("APK Node update", 1);

    public static enum SignInfo {
        SIGNED_NOT,
        SIGNED_V1,
        SIGNED_V2,
        SIGNED_V1V2
    }

    public ApkDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        this.pf = pf;
    }

    @Override
    protected Node createNodeDelegate() {
        return new ApkFilterNode(new DataNode(this, Children.LEAF, getLookup()));
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    @Override
    public FileObject getDeployableFile() {
        return getPrimaryFile();
    }

    public static class SignInfoHolder {

        private SignInfo info = SignInfo.SIGNED_NOT;

        public SignInfo getInfo() {
            return info;
        }

        public void setInfo(SignInfo info) {
            this.info = info;
        }

    }

    private class ApkFilterNode extends FilterNode implements FileChangeListener {

        private SignInfo signInfo = SignInfo.SIGNED_NOT;
        private final SignInfoHolder holder;
        private final Project project;

        public ApkFilterNode(Node original) {
            super(original, org.openide.nodes.Children.LEAF, new ProxyLookup(original.getLookup(), Lookups.fixed(new SignInfoHolder())));
            holder = getLookup().lookup(SignInfoHolder.class);
            project = FileOwnerQuery.getOwner(pf);
            pf.addFileChangeListener(WeakListeners.create(FileChangeListener.class, this, pf));
            refreshIcons();
        }

        @Override
        public String getShortDescription() {
            return pf.getPath(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                SystemAction.get(SaveAsAction.class),
                SystemAction.get(InstallApkAction.class),
                SystemAction.get(CopyAction.class),
                SystemAction.get(DeleteAction.class),
                SystemAction.get(PropertiesAction.class)
            };
        }

        @Override
        public String getHtmlDisplayName() {
            String name = super.getDisplayName();
            if (project != null) {
                if (pf.getParent() != null && pf.getParent().getParent() != null && pf.getParent().getParent().equals(project.getProjectDirectory())) {
                    return "<html><font color=\"#00802b\"><b>" + name + "</b></font></html>";
                } else {
                    return name;
                }
            } else {
                return name;
            }
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

        private void refreshIcons() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
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
                    holder.setInfo(signInfo);
                    fireIconChange();
                    fireOpenedIconChange();
                }
            };
            RP.execute(runnable);

        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            refreshIcons();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

    }

}
