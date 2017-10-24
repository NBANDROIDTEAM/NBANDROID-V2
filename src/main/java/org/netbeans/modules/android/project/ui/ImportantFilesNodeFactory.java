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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.api.ui.AndroidFileNode;
import org.netbeans.modules.android.project.api.ui.UiUtils;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType={"org-netbeans-modules-android-project"}, position=350)
public class ImportantFilesNodeFactory implements NodeFactory {

    /** Package private for unit tests. */
    static final String IMPORTANT_FILES_NAME = "important.files"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(ImportantFilesNodeFactory.class.getName());

    public ImportantFilesNodeFactory() {
    }

    @Override
    public NodeList createNodes(Project p) {
        return new ImportantFilesNodeList(p);
    }

    /**
     * Public RP serving as queue of calls into org.openide.nodes.
     * All such calls must be made outside ProjectManager#mutex(),
     * this (shared) queue ensures ordering of calls.
     * @return Shared RP
     */
    public static RequestProcessor getNodesSyncRP() {
        return RP;
    }

    private static class ImportantFilesNodeList implements NodeList<String> {
        private final Project project;

        public ImportantFilesNodeList(Project p) {
            project = p;
        }

        @Override public List<String> keys() {
            return Collections.singletonList(IMPORTANT_FILES_NAME);
        }

        @Override public void addChangeListener(ChangeListener l) {
            //ignore, doesn't change
        }

        @Override public void removeChangeListener(ChangeListener l) {
            //ignore, doesn't change
        }

        @Override public Node node(String key) {
            assert key.equals(IMPORTANT_FILES_NAME);
            if (project instanceof AndroidProject) {
                return new ImportantFilesNode(project);
            }
            return null;
        }

        @Override public void addNotify() {
        }

        @Override public void removeNotify() {
        }
    }

    /**
     * Show node "Important Files" with various config and docs files beneath it.
     */
    static final class ImportantFilesNode extends AnnotatedNode {

        private static final String DISPLAY_NAME = NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_important_files");

        public ImportantFilesNode(Project project) {
            super(new ImportantFilesChildren(project), org.openide.util.lookup.Lookups.singleton(project));
        }

        @Override public String getName() {
            return IMPORTANT_FILES_NAME;
        }

        private Image getIcon(boolean opened) {
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/android/project/ui/resources/config-badge.gif", true);
            return ImageUtilities.mergeImages(UiUtils.getTreeFolderIcon(opened), badge, 8, 8);
        }

        public @Override String getDisplayName() {
            return annotateName(DISPLAY_NAME);
        }

        public @Override String getHtmlDisplayName() {
            return computeAnnotatedHtmlDisplayName(DISPLAY_NAME, getFiles());
        }

        public @Override Image getIcon(int type) {
            return annotateIcon(getIcon(false), type);
        }

        public @Override Image getOpenedIcon(int type) {
            return annotateIcon(getIcon(true), type);
        }

    }

    /**
     * Actual list of important files.
     */
    private static final class ImportantFilesChildren extends Children.Keys<Object> {

        private List<Object> visibleFiles = null;
        private FileChangeListener fcl;

        // TODO(radim): change to enum
        /** Abstract location to display name. */
        private static final java.util.Map<String,String> FILES = new LinkedHashMap<>();
        static {
            FILES.put("AndroidManifest.xml", NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_AndroidManifest.xml"));
            FILES.put("build.xml", NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_build.xml"));
            FILES.put("ant.properties", NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_ant.properties"));
            FILES.put("project.properties", NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_project.properties"));
            FILES.put("local.properties", NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_local.properties"));
            // FILES.put("proguard.cfg", NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_proguard.cfg"));
        }

        private final Project project;

        public ImportantFilesChildren(Project project) {
            this.project = project;
        }

        protected @Override void addNotify() {
            super.addNotify();
            attachListeners();
            refreshKeys();
        }

        protected @Override void removeNotify() {
            setKeys(Collections.<String>emptyList());
            visibleFiles = null;
            removeListeners();
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes(Object key) {
            if (key instanceof String) {
                String loc = (String) key;
                FileObject file = project.getProjectDirectory().getFileObject(loc);
                try {
                    Node orig = DataObject.find(file).getNodeDelegate();
                    return new Node[] {new AndroidFileNode(orig, FILES.get(loc))};
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
            } else {
                throw new AssertionError(key);
            } 
        }

        private void refreshKeys() {
            Set<FileObject> files = new HashSet<>();
            List<Object> newVisibleFiles = new ArrayList<>();
            for (String loc : FILES.keySet()) {
                FileObject file = project.getProjectDirectory().getFileObject(loc);
                if (file != null) {
                    newVisibleFiles.add(loc);
                    files.add(file);
                }
            }
            if (!newVisibleFiles.equals(visibleFiles)) {
                visibleFiles = newVisibleFiles;
                getNodesSyncRP().post(new Runnable() { // #72471
                    @Override
                    public void run() {
                        setKeys(visibleFiles);
                    }
                });
                ((ImportantFilesNode) getNode()).setFiles(files);
            }
        }

        private void attachListeners() {
            try {
                if (fcl == null) {
                    fcl = new FileChangeAdapter() {
                        public @Override void fileRenamed(FileRenameEvent fe) {
                            refreshKeys();
                        }
                        public @Override void fileDataCreated(FileEvent fe) {
                            refreshKeys();
                        }
                        public @Override void fileDeleted(FileEvent fe) {
                            refreshKeys();
                        }
                    };
                    project.getProjectDirectory().getFileSystem().addFileChangeListener(fcl);
                }
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
        }

        private void removeListeners() {
            if (fcl != null) {
                try {
                    project.getProjectDirectory().getFileSystem().removeFileChangeListener(fcl);
                } catch (FileStateInvalidException e) {
                    assert false : e;
                }
                fcl = null;
            }
        }

    }
  /**
   * Annotates
   * <code>htmlDisplayName</code>, if it is needed, and returns the result;
   * <code>null</code> otherwise.
   */
  public static String computeAnnotatedHtmlDisplayName(
      final String htmlDisplayName, final Set<? extends FileObject> files) {

    String result = null;
    if (files != null && files.iterator().hasNext()) {
      StatusDecorator statusDecorator = Lookup.getDefault().lookup(StatusDecorator.class);
      FileObject fo = (FileObject) files.iterator().next();
      statusDecorator.annotateNameHtml(htmlDisplayName, files);
    }
    return result;
  }
}
