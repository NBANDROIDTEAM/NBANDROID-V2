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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.android.core.sdk.ManifestParser;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.api.AndroidManifestSource;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
public class ProjectThemes {
  private static final Logger LOG = Logger.getLogger(ProjectThemes.class.getName());

  private final Project prj;

  public ProjectThemes(Project prj) {
    this.prj = prj;
  }
  
  public Iterable<ThemeData> getProjectThemes() {
    if (prj == null) {
      return Collections.emptySet();
    }
    AndroidManifestSource ams = prj.getLookup().lookup(AndroidManifestSource.class);
    FileObject manifest = ams != null ? ams.get() : null;
    Iterable<String> manifests = Collections.emptyList();
    try {
      manifests = ManifestParser.getDefault().getManifestThemeNames(manifest != null ? manifest.getInputStream() : null);
    } catch (FileNotFoundException ex) {
      LOG.log(Level.FINE, null, ex);
    }
    Sources sources = ProjectUtils.getSources(prj);
    if (sources != null) {
      for (SourceGroup sg : sources.getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES)) {
        for (FileObject resSubFolder : sg.getRootFolder().getChildren()) {
          if ("values".equals(resSubFolder.getName()) || resSubFolder.getName().startsWith("values-")) {
            for (FileObject valuesXml : resSubFolder.getChildren()) {
              if ("xml".equals(valuesXml.getExt())) {
                try {
                  Iterable<String> stylesNames = ManifestParser.getDefault().getProjectThemeNames(
                      valuesXml.getInputStream());
                  manifests = Iterables.concat(manifests, stylesNames);
                } catch (FileNotFoundException ex) {
                  LOG.log(Level.FINER, null, ex);
                }
              }
            }
          }
        }
      }
    }
    return Iterables.transform(
        manifests, 
        new Function<String, ThemeData>() {
          @Override public ThemeData apply(String name) {
            return new ThemeData(name, true);
          }
        });
  }
}
