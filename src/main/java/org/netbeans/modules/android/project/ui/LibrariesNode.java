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


import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.AndroidProjectInfo;
import org.netbeans.modules.android.project.api.AndroidClassPath;
import org.netbeans.modules.android.project.api.ui.AndroidNodes;
import org.netbeans.modules.android.project.api.ui.UiUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;


/**
 * LibrariesNode displays the content of classpath and Android platform (if available).
 */
final class LibrariesNode extends AbstractNode {
    private static final Logger LOG = Logger.getLogger(LibrariesNode.class.getName());

    private static final Image ICON_BADGE = ImageUtilities.loadImage(
        "org/netbeans/modules/android/project/ui/resources/libraries-badge.png");    //NOI18N
    static final RequestProcessor rp = new RequestProcessor ();

    private final String displayName;
    private final Action[] librariesNodeActions;


    /**
     * Creates new LibrariesNode named displayName displaying classPathProperty classpath
     * and optionaly Java platform.
     * @param displayName the display name of the node
     * @param project the project owning this node, the proejct is placed into LibrariesNode's lookup
     * @param eval {@link PropertyEvaluator} used for listening
     * @param librariesNodeActions actions which should be available on the created node.
     */
    LibrariesNode (String displayName, AndroidProject project,
                   Action[] librariesNodeActions) {
        super (new LibrariesChildren(project), Lookups.singleton(project));
        this.displayName = displayName;
        this.librariesNodeActions = librariesNodeActions;
    }

    @Override
    public String getDisplayName () {
        return this.displayName; 
    }

    @Override
    public String getName () {
        return this.getDisplayName();
    }    

    @Override
    public Image getIcon( int type ) {        
        return computeIcon( false, type );
    }

    @Override
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }

    @Override
    public Action[] getActions(boolean context) {        
        return this.librariesNodeActions;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    //Static Action Factory Methods
    public static Action createAddProjectAction (Project p, String classPathId) {
        return new AddProjectAction (p, classPathId);
    }

    public static Action createAddLibraryAction (Project p, String classPathId) {
        return new AddLibraryAction (p);
    }

    private Image computeIcon( boolean opened, int type ) {
        Image image = UiUtils.getTreeFolderIcon(opened);
        image = ImageUtilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }

    //Static inner classes
    private static class LibrariesChildren extends Children.Keys<Key> implements PropertyChangeListener {

        private final AndroidProject project;

        LibrariesChildren (AndroidProject project) {
            this.project = project;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              setKeys(getKeys());
            }
          });
        }

        @Override
        protected void addNotify() {
          AndroidClassPath cpProvider =
              project.getLookup().lookup(AndroidClassPath.class);
          if (cpProvider != null) {
            cpProvider.getClassPath(ClassPath.COMPILE).addPropertyChangeListener(this);
          }
          this.setKeys(getKeys ());
        }

        @Override
        protected void removeNotify() {
          AndroidClassPath cpProvider = project.getLookup().lookup(AndroidClassPath.class);
          if (cpProvider != null) {
            cpProvider.getClassPath(ClassPath.COMPILE).removePropertyChangeListener(this);
          }
          this.setKeys(Collections.<Key>emptySet());
        }

        @Override
        protected Node[] createNodes(Key key) {
            Node[] result = null;
            switch (key.getType()) {
                case Key.TYPE_PLATFORM:
                    result = new Node[] { AndroidNodes.createPlatformNode(project) };
                    break;
                case Key.TYPE_PROJECT:
                    result = new Node[] { new ProjectNode(key.getProject()) };
                    break;
                case Key.TYPE_LIBRARY:
                  // TODO wrap to show suitable actions
                    result = new Node[] { PackageView.createPackageView(key.getSourceGroup()) };
                    break;
            }
            if (result == null) {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }

        private List<Key> getKeys () {
            List<Key> result = new ArrayList<Key> ();
            // add referenced lib projects and JARs
            addLibraries(result);

            // add PlatformNode
            result.add (new Key());
            return result;
        }

        private void addLibraries(List<Key> result) {
          AndroidProjectInfo info = project.info();
          if (info != null) {
            Iterable<FileObject> dependentProjects = info.getDependentProjectDirs();
            for (FileObject prjRootFO : dependentProjects) {
              try {
                Project p = ProjectManager.getDefault().findProject(prjRootFO);
                result.add(new Key(p, null));
              } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
              } catch (IllegalArgumentException ex) {
                LOG.log(Level.WARNING, null, ex);
              }
            }
            AndroidClassPath cpProvider = project.getLookup().lookup(AndroidClassPath.class);
            if (cpProvider != null) {
              for (FileObject cpRoot : cpProvider.getClassPath(ClassPath.COMPILE).getRoots()) {
                final FileObject archiveFile = FileUtil.getArchiveFile(cpRoot);
                if (archiveFile != null) {
                  boolean isAndroidLibDependency = Iterables.any(dependentProjects, new Predicate<FileObject>() {

                    @Override
                    public boolean apply(FileObject input) {
                      FileObject prjOutput = input.getFileObject("bin/classes.jar");
                      return Objects.equal(prjOutput, archiveFile);
                    }
                  });
                  if (isAndroidLibDependency) {
                    continue;
                  }
                }
                // TODO we only support JAR archives so all entries should be archives.
                FileObject rootFile = FileUtil.getArchiveFile(cpRoot);
                String name = rootFile != null ? rootFile.getNameExt() : cpRoot.getNameExt();
                  result.add(new Key(AndroidNodes.createLibrarySourceGroup(name, cpRoot, null)));
              }
            }
          }
        }
    }

    private static class Key {
        static final int TYPE_PLATFORM = 0;
        static final int TYPE_LIBRARY = 1;
        static final int TYPE_PROJECT = 2;

        private int type;
        private SourceGroup sg;
        private Project project;
        private URI uri;

        Key () {
            this.type = TYPE_PLATFORM;
        }

        Key (SourceGroup sg) {
            this.type = TYPE_LIBRARY;
            this.sg = sg;
        }

        Key (Project p, URI uri) {
            this.type = TYPE_PROJECT;
            this.project = p;
            this.uri = uri;
        }

        public int getType () {
            return this.type;
        }

        public SourceGroup getSourceGroup () {
            return this.sg;
        }

        public Project getProject () {
            return this.project;
        }

        // TODO delete?
        public URI getArtifactLocation () {
            return this.uri;
        }

        @Override
        public int hashCode() {
            int hashCode = this.type<<16;
            switch (this.type) {
                case TYPE_LIBRARY:
                    hashCode ^= this.sg == null ? 0 : this.sg.hashCode();
                    break;
                case TYPE_PROJECT:
                    hashCode ^= this.project == null ? 0 : this.project.hashCode();
                    break;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (other.type != type) {
                return false;
            }
            switch (type) {
                case TYPE_LIBRARY:
                    return (this.sg == null ? other.sg == null : this.sg.equals(other.sg));
                case TYPE_PROJECT:
                    return (this.project == null ? other.project == null : this.project.equals(other.project));
                case TYPE_PLATFORM:
                    return true;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private static class AddProjectAction extends AbstractAction {

        private final Project project;

        public AddProjectAction (Project project, String classPathId) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddProject_Action" ) );
            this.project = project;
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    private static class AddLibraryAction extends AbstractAction {

        private final Project project;

        public AddLibraryAction (Project project) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddLibrary_Action" ) );
            this.project = project;
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
        }
    }
}
