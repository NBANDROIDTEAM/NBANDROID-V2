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
import com.google.common.base.Objects;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.api.AndroidProjects;

/**
 * Data container for general data about Android project edited in project customizer.
 * Shared in lookup available to customizers.
 *
 * @author radim
 */
public class AndroidGeneralData {

  public static AndroidGeneralData fromProject(AndroidProject project) {
    final AndroidGeneralData data = new AndroidGeneralData();
    data.setProjectName(project.getLookup().lookup(ProjectInformation.class).getName());
    data.setProjectDirPath(project.getProjectDirectoryFile().getAbsolutePath());
    data.setPlatform(AndroidProjects.projectPlatform(project));
    data.setMainProjectDirPath(project.evaluator().getProperty(PropertyName.TEST_PROJECT_DIR.getName()));
    data.setLibrary(project.info().isLibrary());
    data.setReferencedProjects(project.getLookup().lookup(AndroidInfoImpl.class).getDependentProjectDirPaths());
    return data;
  }

  private String projectName;
  private String projectDirPath;
  private DalvikPlatform platform;
  private String mainProjectDirPath;
  private boolean library = false;
  private Iterable<String> referencedProjects;

  public AndroidGeneralData() {
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectDirPath() {
    return projectDirPath;
  }

  public void setProjectDirPath(String projectDirPath) {
    this.projectDirPath = projectDirPath;
  }

  public DalvikPlatform getPlatform() {
    return platform;
  }

  public void setPlatform(DalvikPlatform platform) {
    this.platform = platform;
  }

  /**
   * Path to a main project if this is a test project or {@code null}
   */
  public String getMainProjectDirPath() {
    return mainProjectDirPath;
  }

  /**
   * Set path to a main project if this is a test project or {@code null}
   */
  public void setMainProjectDirPath(String mainProjectDirPath) {
    this.mainProjectDirPath = mainProjectDirPath;
  }

  public boolean isLibrary() {
    return library;
  }

  public void setLibrary(boolean isLib) {
    this.library = isLib;
  }

  public Iterable<String> getReferencedProjects() {
    return referencedProjects;
  }

  public void setReferencedProjects(Iterable<String> referencedProjects) {
    this.referencedProjects = referencedProjects;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AndroidGeneralData other = (AndroidGeneralData) obj;
    if ((this.projectName == null) ?
      (other.projectName != null) : !this.projectName.equals(other.projectName)) {
      return false;
    }
    if ((this.projectDirPath == null) ?
      (other.projectDirPath != null) : !this.projectDirPath.equals(other.projectDirPath)) {
      return false;
    }
    if (this.platform != other.platform &&
        (this.platform == null || !this.platform.equals(other.platform))) {
      return false;
    }
    if (!Objects.equal(this.mainProjectDirPath, other.mainProjectDirPath)) {
      return false;
    }
    if (this.library != other.library) {
      return false;
    }
    if (!Objects.equal(this.referencedProjects, other.referencedProjects)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.projectName != null ? this.projectName.hashCode() : 0);
    hash = 67 * hash + (this.projectDirPath != null ? this.projectDirPath.hashCode() : 0);
    hash = 67 * hash + (this.platform != null ? this.platform.hashCode() : 0);
    hash = 67 * hash + (this.mainProjectDirPath != null ? this.mainProjectDirPath.hashCode() : 0);
    hash = 67 * hash + (this.library ? 1 : 0);
    hash = 67 * hash + (this.referencedProjects != null ? this.referencedProjects.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "AndroidGeneralData{" + 
        "projectName=" + projectName + 
        ", projectDirPath=" + projectDirPath + 
        ", platform=" + platform + 
        ", mainProjectDirPath=" + mainProjectDirPath + 
        ", library=" + library + 
        ", referencedProjects=" + referencedProjects + '}';
  }

}
