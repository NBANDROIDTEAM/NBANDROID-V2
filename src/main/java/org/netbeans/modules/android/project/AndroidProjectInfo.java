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

import java.beans.PropertyChangeListener;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
public interface AndroidProjectInfo {

  boolean isLibrary();

  /** Answers if the project looks like test project.
   * To be determined from tested.project.dir property in build.properties.
   * If this file is missing then AndroidManifest.xml is scanned for instrumentation element.
   * @return 
   */
  boolean isTest();

  /** Iterable of dependencies on project libraries. */
  Iterable<FileObject> getDependentProjectDirs();

  /** Source directories. Usually {@code src} unless it is overriden by setting 
   * {@code source.dir} property. */
  Iterable<FileObject> getSourceDirs();
  Iterable<FileObject> getSourceAndGenDirs();
  
  // TODO - maybe move update methods here from AndroidProject?

  /** Getter to find whether project needs to be updated using SDK tools. */
  boolean isNeedsFix();

  String getFixDescription();

  void addPropertyChangeListener(PropertyChangeListener lsnr);
  void removePropertyChangeListener(PropertyChangeListener lsnr);
}
