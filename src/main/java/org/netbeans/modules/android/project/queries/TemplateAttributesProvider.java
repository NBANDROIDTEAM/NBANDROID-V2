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

import com.google.common.base.Preconditions;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Builds a map of template attribute used by freemaker to instantiate templates.
 */
public class TemplateAttributesProvider implements CreateFromTemplateAttributesProvider {

  private final FileEncodingQueryImplementation encodingQuery;
  private final AndroidProject aPrj;

  public TemplateAttributesProvider(AndroidProject aPrj, FileEncodingQueryImplementation encodingQuery) {
    this.aPrj = Preconditions.checkNotNull(aPrj);
    this.encodingQuery = Preconditions.checkNotNull(encodingQuery);
  }

  @Override
  public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
    Map<String, String> values = new HashMap<String, String>();
    String license = aPrj.evaluator().getProperty("project.license"); // NOI18N
    if (license != null) {
      values.put("license", license); // NOI18N
    }
    Charset charset = encodingQuery.getEncoding(target.getPrimaryFile());
    String encoding = (charset != null) ? charset.name() : null;
    if (encoding != null) {
      values.put("encoding", encoding); // NOI18N
    }
    try {
      Project prj = ProjectManager.getDefault().findProject(aPrj.getProjectDirectory());
      ProjectInformation info = prj.getLookup().lookup(ProjectInformation.class);
      if (info != null) {
        String pname = info.getName();
        if (pname != null) {
          values.put("name", pname);// NOI18N
        }
        String pdname = info.getDisplayName();
        if (pdname != null) {
          values.put("displayName", pdname);// NOI18N
        }
      }
    } catch (Exception ex) {
      //not really important, just log.
      Logger.getLogger(TemplateAttributesProvider.class.getName()).log(Level.FINE, "", ex);
    }

    if (values.isEmpty()) {
      return null;
    } else {
      return Collections.singletonMap("project", values); // NOI18N
    }
  }
}
