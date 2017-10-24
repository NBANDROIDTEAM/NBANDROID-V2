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
package org.netbeans.modules.android.project.queries;

import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
public class PlatformSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation2 {

  @Override
  public Result findSourceRoots(URL binaryRoot) {
    return findSourceRoots2(binaryRoot);
  }

  @Override
  public Result findSourceRoots2(URL binaryRoot) {
    for (DalvikPlatform platform : DalvikPlatformManager.getDefault().getPlatforms()) {
      if (platform.getBootstrapLibraries().contains(binaryRoot)) {
        final ClassPath roots = platform.getSourceFolders();
        if (roots != null && !roots.entries().isEmpty()) {
          return new Result() {
              public @Override FileObject[] getRoots() {
                  return roots.getRoots();
              }
              public @Override void addChangeListener(ChangeListener l) {}
              public @Override void removeChangeListener(ChangeListener l) {}

            @Override
            public boolean preferSources() {
              return false;
            }
          };
        }
      }
    }
    return null;
  }
}
