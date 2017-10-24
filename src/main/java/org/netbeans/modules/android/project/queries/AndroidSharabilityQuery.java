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

import java.io.File;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.queries.SharabilityQueryImplementation;

public class AndroidSharabilityQuery implements SharabilityQueryImplementation {

    private final AndroidProject project;
    public AndroidSharabilityQuery(AndroidProject project) {
        this.project = project;
    }

    public @Override int getSharability(File file) {
        if (!file.getParentFile().equals(project.getProjectDirectoryFile())) {
            return SharabilityQuery.UNKNOWN;
        }
        // XXX should pay attention to ${out.dir}
        if (file.getName().matches("bin|gen|local[.]properties|nbandroid")) {
            return SharabilityQuery.NOT_SHARABLE;
        }
        return SharabilityQuery.SHARABLE;
    }

}
