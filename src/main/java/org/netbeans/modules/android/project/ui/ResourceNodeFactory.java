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

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.api.ui.AndroidResourceNode;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 */
public final class ResourceNodeFactory implements NodeFactory {
  public ResourceNodeFactory() {
  }

  @Override
  public NodeList createNodes(Project p) {
      AndroidProject project = p.getLookup().lookup(AndroidProject.class);
      assert project != null;
      return new SourcesNodeList(project);
  }

  private static enum ResFolder {
    ASSETS("assets.dir", "assets", "Assets"),
    RES("resource.dir", "res", "Resources");

    public final String propertyName;
    public final String folderName;
    public final String nodeName;

    private ResFolder(String propertyName, String folderName, String nodeName) {
      this.propertyName = propertyName;
      this.folderName = folderName;
      this.nodeName = nodeName;
    }
  }

  private static class NamedFileObject {
    public final FileObject fo;
    public final String name;

    public NamedFileObject(FileObject fo, String name) {
      this.fo = fo;
      this.name = name;
    }
  }

    private static class SourcesNodeList implements NodeList<NamedFileObject>, ChangeListener {

        private AndroidProject project;

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public SourcesNodeList(AndroidProject proj) {
            project = proj;
        }

        @Override
        public List<NamedFileObject> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.emptyList();
            }

            return getResources();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(NamedFileObject key) {
            try {
                DataObject folderDO = DataObject.find(key.fo);
                return new AndroidResourceNode(folderDO.getNodeDelegate(), project, key.name);

            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public void addNotify() {
            // getResources().addChangeListener(this);
        }

        @Override
        public void removeNotify() {
            // getResources().removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        private List<NamedFileObject> getResources() {
          List<NamedFileObject> folders = Lists.newArrayList();
          for (ResFolder rf : ResFolder.values()) {

            String resDirName = project.evaluator().getProperty(rf.propertyName);
            if (resDirName == null) {
                resDirName = rf.folderName;
            }


            FileObject folder = project.getProjectDirectory();
            FileObject resDir = folder.getFileObject(resDirName);
            if (resDir != null && resDir.isValid()) {
              folders.add(new NamedFileObject(resDir, rf.nodeName));
            }
          }
          return folders;
        }
    }
}
