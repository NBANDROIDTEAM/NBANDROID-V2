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

package org.netbeans.modules.android.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Phil Lello
 */
public class AndroidSubprojectProvider implements SubprojectProvider {

  private static final Logger LOG = Logger.getLogger(AndroidSubprojectProvider.class.getName());
  private AndroidProject project;
  private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

  public AndroidSubprojectProvider(AndroidProject project) {
     this.project = project;
     // TODO: May need some way to track changes in referenced projects.
  }

  @Override
  public Set<? extends Project> getSubprojects() {
    HashSet<Project> projectSet = new HashSet<Project>();
    /* TODO: Abstract this code lifted from LibrariesNode::addProjectLibs
     * to share it for better maintenance - maybe use getSubprojects there,
     * and convert this Set to List<Key>?
     */
    AndroidProjectInfo info = project.info();
    if (info != null) {
      for (FileObject prjRootFO : info.getDependentProjectDirs()) {
        try {
          Project p = ProjectManager.getDefault().findProject(prjRootFO);
          projectSet.add(p);
        } catch (IOException ex) {
          LOG.log(Level.WARNING, null, ex);
        } catch (IllegalArgumentException ex) {
          LOG.log(Level.WARNING, null, ex);
        }
      }
    }
    return Collections.unmodifiableSet(projectSet);
  }

  @Override
  public void addChangeListener(ChangeListener listener) {
    changeListeners.add(listener);
  }

  @Override
  public void removeChangeListener(ChangeListener listener) {
    changeListeners.remove(listener);
  }
}
