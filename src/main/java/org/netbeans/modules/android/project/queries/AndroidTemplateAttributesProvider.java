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

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.android.project.AndroidProject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public final class AndroidTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {

    private final AndroidProject project;

    public AndroidTemplateAttributesProvider(AndroidProject project) {
        this.project = project;
    }

    public @Override Map<String,?> attributesFor(DataObject template, DataFolder target, String name) {
        String license = project.evaluator().getProperty("project.license"); // NOI18N
        if (license == null) {
            return null;
        } else {
            return Collections.singletonMap("project", Collections.singletonMap("license", license)); // NOI18N
        }
    }

}
