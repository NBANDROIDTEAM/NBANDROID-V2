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

import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.api.AndroidManifestSource;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
public class AndroidManifestSourceAnt implements AndroidManifestSource {

  private final Project p;

  public AndroidManifestSourceAnt(Project p) {
    this.p = p;
  }
  
  @Override
  public FileObject get() {
    return p.getProjectDirectory().getFileObject(AndroidConstants.ANDROID_MANIFEST_XML);
  }
  
}
