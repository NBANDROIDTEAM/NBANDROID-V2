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
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;

public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private AndroidProject project;
    public UnitTestForSourceQueryImpl(AndroidProject project) {
        this.project = project;
    }

    public @Override URL[] findUnitTests(FileObject source) {
        return new URL[0]; // XXX look for tests/ subproject
    }

    public @Override URL[] findSources(FileObject unitTest) {
        return new URL[0]; // XXX look up main project
    }

}
