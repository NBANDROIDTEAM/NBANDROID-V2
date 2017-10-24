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

import org.netbeans.modules.android.project.api.PropertyName;
import com.android.sdklib.internal.project.ProjectProperties;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 * Project updater that modifies properties related to locations of project libraries.
 * @author radim
 */
public class JarLibsModel implements ProjectUpdater {

  private boolean useDefaultLocation;
  private FileObject libsFO;
  private String libsDir;
  private final AndroidProject project;

  public JarLibsModel(AndroidProject project) {
    this.project = Preconditions.checkNotNull(project);
    libsDir = project.evaluator().getProperty(PropertyName.JAR_LIBS_DIR.getName());
    if (libsDir == null) {
      useDefaultLocation = true;
      libsDir = "libs";
      libsFO = project.getProjectDirectory().getFileObject(libsDir);
    } else {
      useDefaultLocation = false;
      File libs = new File(project.getProjectDirectoryFile(), libsDir);
      libsFO = FileUtil.toFileObject(FileUtil.normalizeFile(libs));
    }

  }

  public void setUseDefaultLocation(boolean useDefaultLocation) {
    this.useDefaultLocation = useDefaultLocation;
    updateData();
  }

  public boolean isUseDefaultLocation() {
    return useDefaultLocation;
  }

  public String getLibsDir() {
    return libsDir;
  }

  public void setLibsDir(String libsDir) {
    this.libsDir = libsDir;
    updateData();
  }

  public Iterable<String> getLibNames() {
    List<String> names = Lists.newArrayList();
    if (libsFO != null) {
      for (FileObject library : libsFO.getChildren()) {
        if (library.hasExt("jar")) {
          names.add(library.getNameExt());
        }
      }
    }
    return names;
  }

  private void updateData() {
    if (useDefaultLocation) {
      libsDir = "libs";
    }
    if (libsDir == null) {
      libsFO = null;
    } else {
      libsFO = FileUtil.toFileObject(FileUtil.normalizeFile(libsDirAsFile()));
    }
  }

  private File libsDirAsFile() {
    File f = new File(libsDir);
    if (!f.isAbsolute()) {
      f = new File(project.getProjectDirectoryFile(), libsDir);
    }
    return f;
  }

  @Override
  public boolean apply(final AndroidProject project) {
    Logger.getLogger(JarLibsModel.class.getName()).log(
        Level.FINE, "Udpating libs information {0} for project {1}", new Object[] {this, project});
    assert this.project == project;
    return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {

      @Override
      public Boolean run() {
        PropertiesHelper propertyHelper = project.getLookup().lookup(PropertiesHelper.class);
        if (useDefaultLocation) {
          for (ProjectProperties.PropertyType type : ProjectProperties.PropertyType.values()) {
            propertyHelper.setProperty(type, PropertyName.JAR_LIBS_DIR.getName(), null);
          }
        } else {
          String relativePath = PropertyUtils.relativizeFile(
              project.getProjectDirectoryFile(),
              FileUtil.normalizeFile(libsDirAsFile()));
          propertyHelper.setProperty(ProjectProperties.PropertyType.ANT,
              PropertyName.JAR_LIBS_DIR.getName(), relativePath);
        }
        return true;
      }

    });
  }

  @Override
  public String toString() {
    return "JarLibsModel{" +
        "useDefaultLocation=" + useDefaultLocation +
        ", libsDir=" + libsDir + '}';
  }
}
