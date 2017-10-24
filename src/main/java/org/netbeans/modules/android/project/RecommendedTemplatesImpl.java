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

import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

    private static final String[] APPLICATION_TYPES = {
        "android",
        "java-classes",
        "java-beans",
        "XML",
        "junit",
        "simple-files",
    };

    private static final String[] PRIVILEGED_NAMES = {
        "Templates/Android/Activity.java",
        "Templates/Classes/Class.java",
        "Templates/Classes/Package",
        "Templates/Classes/Interface.java",
    };

    public @Override String[] getRecommendedTypes() {
        return APPLICATION_TYPES;
    }

    public @Override String[] getPrivilegedTemplates() {
        return PRIVILEGED_NAMES;
    }

}
