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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for simple Android projects.
 */
@ServiceProvider(service = ProjectFactory.class, position =/* after Maven, before autoproject */ 830)
public final class AndroidProjectType implements ProjectFactory2 {
  private static final Logger LOG = Logger.getLogger(AndroidProjectType.class.getName());

  /** Property name fired from AndroidProjectInfo. */
  public static final String NEEDS_FIX_INFO = "needsFix";

  public @Override
  Result isProject2(FileObject projectDirectory) {
    if (isProject(projectDirectory)) {
      return new Result(ImageUtilities.loadImageIcon("org/netbeans/modules/android/project/ui/resources/androidProject.png", true));
    } else {
      return null;
    }
  }

    //ARSI: org.netbeans.modules.android.project.AndroidProjectImpl is created for not gradle folders android cache etc..
  @Override
    public boolean isProject(FileObject prjDir) {
      // XXX perhaps also make sure src/ exists, so we do not pick up resources dir for a Maven-Android project
    if (prjDir.getFileObject(AndroidConstants.ANDROID_MANIFEST_XML) == null) {
      return false;
    }
    if (prjDir.getFileObject("build.gradle") != null) {
      LOG.log(Level.INFO, "Ignoring possible android project in {0}. Use Gradle support.", prjDir);
      // old project migrated to gradle
      return false;
    }
    if ("bin".equals(prjDir.getNameExt())) {
      return false;
    }
    if ("main".equals(prjDir.getNameExt())) {
      // check for manifest in Maven or Gradle project
      // app/build.gradle
      // app/src/main/AndroidManifest.xml
      FileObject parentDir = prjDir.getParent();
      parentDir = (parentDir != null && "src".equals(parentDir.getNameExt())) ? parentDir.getParent() : null;
      if (parentDir != null) {
        if (parentDir.getFileObject("build.gradle") != null
            || parentDir.getFileObject("pom.xml") != null) {
          return false;
        }
      }
    }
    return true;
  }

    public @Override Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (isProject(projectDirectory)) {
            File dirF = FileUtil.toFile(projectDirectory);
            if (dirF != null) {
                return new AndroidProjectImpl(
                    projectDirectory, dirF, DalvikPlatformManager.getDefault());
            }
        }
        return null;
    }

    public @Override void saveProject(Project project) throws IOException, ClassCastException {}

}
