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
package org.netbeans.modules.android.project.layout;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.core.sdk.ManifestParser;
import org.netbeans.modules.android.project.api.AndroidManifestSource;
import org.openide.filesystems.FileObject;

/**
 * Parses information from AndroidManifest.xml that is not available in ManifestData: app title and icon.
 */
public class ManifestInfo {
  private static final Logger LOG = Logger.getLogger(ManifestInfo.class.getName());
  
  private final AndroidManifestSource ams;

  public ManifestInfo(AndroidManifestSource ams) {
    this.ams = ams;
  }
  
  public ManifestParser.ApplicationData parse() {
    FileObject manifest = ams != null ? ams.get() : null;
    try {
      return ManifestParser.getDefault().getApplicationData(manifest != null ? manifest.getInputStream() : null);
    } catch (FileNotFoundException ex) {
      LOG.log(Level.FINE, null, ex);
    }
    return ManifestParser.defaultApplicationData();
  }
}
