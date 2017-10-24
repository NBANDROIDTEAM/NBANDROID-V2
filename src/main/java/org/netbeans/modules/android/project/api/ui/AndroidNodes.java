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
package org.netbeans.modules.android.project.api.ui;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.android.project.api.AndroidProjects;
import org.netbeans.modules.android.project.ui.LibrariesSourceGroup;
import org.netbeans.modules.android.project.ui.PlatformNode;
import org.netbeans.modules.android.project.ui.ProjectNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author radim
 */
public class AndroidNodes {
  private static final String ARCHIVE_ICON = "org/netbeans/modules/android/project/ui/resources/jar.gif";//NOI18N        
  
  public static Node createPlatformNode(@Nonnull Project project) {
    return new PlatformNode(project, AndroidProjects.projectPlatform(project));
  }

  public static Node createProjectNode(@Nonnull Project project) {
    return new ProjectNode(project);
  }
  
    public static SourceGroup createLibrarySourceGroup(String name, FileObject cpRoot, final Project project) {
    Icon openedIcon = cpRoot != null
        ? ImageUtilities.loadImageIcon(ARCHIVE_ICON, true)
        : ImageUtilities.image2Icon(UiUtils.getTreeFolderIcon(true));
    Icon closedIcon = cpRoot != null
        ? ImageUtilities.loadImageIcon(ARCHIVE_ICON, true)
        : ImageUtilities.image2Icon(UiUtils.getTreeFolderIcon(false));
        return new LibrariesSourceGroup(cpRoot, name, closedIcon, openedIcon, project);
  }
}
