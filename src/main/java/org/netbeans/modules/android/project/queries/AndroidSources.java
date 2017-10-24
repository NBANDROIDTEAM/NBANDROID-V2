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

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;

/**
 * Implementation of {@link Sources} interface for AndroidProject.
 */
public class AndroidSources implements Sources  {

    private final AndroidProject project;
    public AndroidSources(AndroidProject project) {
        this.project = project;
    }

    public @Override SourceGroup[] getSourceGroups(String type) {
        if (type.equals(Sources.TYPE_GENERIC)) {
            ProjectInformation info = ProjectUtils.getInformation(project);
            return new SourceGroup[] {GenericSources.group(project, project.getProjectDirectory(), info.getName(), info.getDisplayName(), null, null)};
        } else if (type.equals(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            // XXX listen to this being created/deleted
            List<SourceGroup> grps = new ArrayList<SourceGroup>(2);
            for (FileObject src : project.info().getSourceDirs()) {
              grps.add(GenericSources.group(project, src, src.getName(), "Source Packages", null, null));
            }
            FileObject gen = project.getProjectDirectory().getFileObject("gen");
            if (gen != null) {
              grps.add(GenericSources.group(project, gen, "gen", "Generated Source Packages", null, null));
            }
            return grps.toArray(new SourceGroup[grps.size()]);
        } else if (type.equals(AndroidConstants.SOURCES_TYPE_ANDROID_RES)) {
            List<SourceGroup> grps = new ArrayList<SourceGroup>(1);
            FileObject res = project.getProjectDirectory().getFileObject("res");
            if (res != null) {
              grps.add(GenericSources.group(project, res, "gen", "App resources", null, null));
            }
            return grps.toArray(new SourceGroup[grps.size()]);
        } else {
            // XXX consider SOURCES_TYPE_RESOURCES -> res
            return new SourceGroup[0];
        }
    }

    public @Override void addChangeListener(ChangeListener listener) {}

    public @Override void removeChangeListener(ChangeListener listener) {}

}
