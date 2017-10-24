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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.AndroidProjectInfo;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Support for creating logical views.
 */
public class AndroidLogicalViewProvider implements LogicalViewProvider {

    private final AndroidProject project;

    public AndroidLogicalViewProvider(AndroidProject project) {
        this.project = project;
        assert project != null;
    }

    @Override
    public Node createLogicalView() {
        return new AndroidLogicalViewRootNode();
    }

    @Override
    public Node findPath(Node root, Object target) {
        Project lookupPrj = root.getLookup().lookup(Project.class);
        if (lookupPrj == null) {
            return null;
        }

        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!lookupPrj.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }

            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null) {
                    return result;
                }
                PathFinder finder = n.getLookup().lookup(PathFinder.class);
                if (finder != null) {
                  result = finder.findPath(n, target);
                }
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private static final Image brokenProjectBadge = ImageUtilities.loadImage(
        "org/netbeans/modules/android/project/ui/resources/brokenProjectBadge.gif", true);
    private static final Image librariesBadge = ImageUtilities.loadImage(
        "org/netbeans/modules/android/project/ui/resources/libraries-badge.png", true);
    private static final Image testBadge = ImageUtilities.loadImage(
        "org/netbeans/modules/android/project/ui/resources/test_badge.png", true);

    private final class AndroidLogicalViewRootNode extends AbstractNode {

      private final AndroidProjectInfo info;

      public AndroidLogicalViewRootNode() {
        super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-android-project/Nodes"), 
              Lookups.singleton(project));
        setIconBaseWithExtension("org/netbeans/modules/android/project/ui/resources/androidProject.png");
        super.setName(ProjectUtils.getInformation(project).getDisplayName() );
        info = project.info();
        info.addPropertyChangeListener(new InfoListener());
      }

      private class InfoListener implements Runnable, PropertyChangeListener {

        @Override
        public void run() {
          AndroidLogicalViewRootNode.this.fireIconChange();
          AndroidLogicalViewRootNode.this.fireOpenedIconChange();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          SwingUtilities.invokeLater(this);
        }

      }

      @Override
      public String getShortDescription() {
          String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());

          if (info.isLibrary()) {
            return info.isNeedsFix() ?
                NbBundle.getMessage(AndroidLogicalViewProvider.class, "HINT_lib-project_root_node_with_warning", 
                    new Object[] { prjDirDispName, info.getFixDescription() }) :
                NbBundle.getMessage(AndroidLogicalViewProvider.class, "HINT_lib-project_root_node", prjDirDispName);
          } else {
            return info.isNeedsFix() ?
                NbBundle.getMessage(AndroidLogicalViewProvider.class, "HINT_project_root_node_with_warning", 
                    new Object[] { prjDirDispName, info.getFixDescription() }) :
                NbBundle.getMessage(AndroidLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName);
          }
      }

      @Override
      public Action[] getActions(boolean context) {
          return CommonProjectActions.forType("org-netbeans-modules-android-project"); // NOI18N
      }

      @Override
      public boolean canRename() {
          return true;
      }

      @Override
      public void setName(String s) {
          DefaultProjectOperations.performDefaultRenameOperation(project, s);
      }

      @Override
      public HelpCtx getHelpCtx() {
          return new HelpCtx(AndroidLogicalViewRootNode.class.getName());
      }

      @Override
      public Image getIcon(int type) {
        Image image = super.getIcon(type);
        return annotateImageIcon(image);
      }

      @Override
      public Image getOpenedIcon(int type) {
        Image image = super.getOpenedIcon(type);
        return annotateImageIcon(image);
      }

      private Image annotateImageIcon(Image image) {
        if (info.isNeedsFix()) {
          image = ImageUtilities.mergeImages(image, brokenProjectBadge, 8, 0);
        }
        if (info.isLibrary()) {
          image = ImageUtilities.mergeImages(image, librariesBadge, 8, 8);
        }
        if (info.isTest()) {
          image = ImageUtilities.mergeImages(image, testBadge, 6, 6);
        }
        return image;
      }
        /*
        public boolean canDestroy() {
            return true;
        }

        public void destroy() throws IOException {
            System.out.println("Destroy " + project.getProjectDirectory());
            LogicalViews.closeProjectAction().actionPerformed(new ActionEvent(this, 0, ""));
            project.getProjectDirectory().delete();
        }
         */
    }
}
