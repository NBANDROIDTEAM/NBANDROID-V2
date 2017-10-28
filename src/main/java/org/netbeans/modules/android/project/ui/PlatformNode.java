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
package org.netbeans.modules.android.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.api.AndroidClassPath;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * PlatformNode represents Java platform in the logical view. Listens on the
 * {@link PropertyEvaluator} for change of the ant property holding the platform
 * name. It displays the content of boot classpath.
 *
 * @see JavaPlatform
 */
public final class PlatformNode extends AbstractNode {

    private static final String PLATFORM_ICON = "org/netbeans/modules/android/project/ui/resources/platform.gif";    //NOI18N
    private static final String ARCHIVE_ICON = "org/netbeans/modules/android/project/ui/resources/jar.gif"; //NOI18N
    private final Project project;
    private final DalvikPlatform platform;

    public PlatformNode(@Nonnull Project project, @Nullable DalvikPlatform platform) {
        super(new PlatformContentChildren() /*, Lookups.singleton (new JavadocProvider(pp))*/);
        this.project = project;
        this.platform = platform;
        setIconBaseWithExtension(PLATFORM_ICON);
    }

    @Override
    public String getName() {
        return this.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        String name;
        if (platform != null) {
            // TODO
            name = platform.getAndroidTarget().isPlatform()
                    ? platform.getAndroidTarget().getFullName()
                    : NbBundle.getMessage(PlatformNode.class, "FMT_PlatformDisplayName",
                            platform.getAndroidTarget().getName(), platform.getAndroidTarget().getVersionName());
        } else {
            name = NbBundle.getMessage(PlatformNode.class, "TXT_BrokenPlatform");
        }
        return name;
    }

//    @Override
//    public String getHtmlDisplayName () {
//        if (project.getPlatform() == null) {
//            String displayName = this.getDisplayName();
//            try {
//                displayName = XMLUtil.toElementContent(displayName);
//            } catch (CharConversionException ex) {
//                // OK, no annotation in this case
//                return null;
//            }
//            return "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
//        }
//        else {
//            return null;
//        }                                
//    }
    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            SystemAction.get(ShowJavadocAction.class)
        };
    }

    private static class PlatformContentChildren extends Children.Keys<SourceGroup> implements PropertyChangeListener {

        PlatformContentChildren() {
        }

        @Override
        protected void addNotify() {
            AndroidClassPath cpProvider
                    = ((PlatformNode) getNode()).project.getLookup().lookup(AndroidClassPath.class);
            if (cpProvider != null) {
                cpProvider.getClassPath(ClassPath.BOOT).addPropertyChangeListener(this);
            }
            this.setKeys(this.getKeys());
        }

        @Override
        protected void removeNotify() {
            AndroidClassPath cpProvider
                    = ((PlatformNode) getNode()).project.getLookup().lookup(AndroidClassPath.class);
            if (cpProvider != null) {
                cpProvider.getClassPath(ClassPath.BOOT).removePropertyChangeListener(this);
            }
            this.setKeys(Collections.<SourceGroup>emptySet());
        }

        @Override
        protected Node[] createNodes(SourceGroup sg) {
            return new Node[]{PackageView.createPackageView(sg)};
        }

        private List<SourceGroup> getKeys() {
            AndroidClassPath cpProvider
                    = ((PlatformNode) getNode()).project.getLookup().lookup(AndroidClassPath.class);
            if (cpProvider == null) {
                return Collections.emptyList();
            }
            //Todo: Should listen on returned classpath, but now the bootstrap libraries are read only
            FileObject[] roots = cpProvider.getClassPath(ClassPath.BOOT).getRoots();
            List<SourceGroup> result = new ArrayList<>(roots.length);
            for (FileObject root : roots) {
                if (GradleAndroidClassPathProvider.VIRTUALJAVA8ROOT_DIR.getPath().equals(root.getPath())) {
                    continue;
                }
                FileObject file;
                Icon icon;
                Icon openedIcon;
                if ("jar".equals(root.toURL().getProtocol())) {
                    //NOI18N
                    file = FileUtil.getArchiveFile(root);
                    icon = openedIcon = new ImageIcon(ImageUtilities.loadImage(ARCHIVE_ICON));
                } else {
                    file = root;
                    icon = null;
                    openedIcon = null;
                }
                if (file.isValid()) {
                    result.add(new LibrariesSourceGroup(root, file.getNameExt(), icon, openedIcon, null));
                }
            }
            return result;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // change from boot classpath: rebuild the node
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    PlatformContentChildren.this.setKeys(PlatformContentChildren.this.getKeys());
                    ((PlatformNode) getNode()).fireNameChange(null, null);
                    ((PlatformNode) getNode()).fireDisplayNameChange(null, null);
                }
            });
        }
    }
}
