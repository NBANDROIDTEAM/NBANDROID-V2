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
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;

/**
 * Provides Javadoc for classes from SDK and add-ons. It is registered globally.
 *
 * @author radim
 */
public class SDKJavadocQueryImplementation implements JavadocForBinaryQueryImplementation {

  @Override
  public Result findJavadoc(URL binaryRoot) {
    for (DalvikPlatform platform : DalvikPlatformManager.getDefault().getPlatforms()) {
      if (platform.getBootstrapLibraries().contains(binaryRoot)) {
        final URL[] javadocRoots = platform.getJavadocFolders().toArray(new URL[0]);
        return new Result() {

          @Override
          public URL[] getRoots() {
            return javadocRoots;
          }

          @Override
          public void addChangeListener(ChangeListener l) {
            // no-op
          }

          @Override
          public void removeChangeListener(ChangeListener l) {
            // no-op
          }
        };
      }
    }
    return null;
  }

}
