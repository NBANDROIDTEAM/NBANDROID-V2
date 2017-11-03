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
package org.nbandroid.netbeans.gradle.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.ProjectUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.ImageUtilities;

/**
 * ProjectNode represents a dependent project under the Libraries Node.
 */
public final class ProjectNode extends AbstractNode {

  private static final String PROJECT_ICON = "org/netbeans/modules/android/project/ui/resources/androidProject.png";    //NOI18N

  private final Project project;
  private Image cachedIcon;

  // TODO(radim): project or location or name
  public ProjectNode(Project project) {
    super(Children.LEAF);
    this.project = project;
  }

  @Override
  public String getDisplayName() {
    ProjectInformation info = getProjectInformation();
    if (info != null) {
      return info.getDisplayName();
    } else {
      return NbBundle.getMessage(ProjectNode.class, "TXT_UnknownProjectName");
    }
  }

  @Override
  public String getName() {
    return this.getDisplayName();
  }

  @Override
  public Image getIcon(int type) {
    if (cachedIcon == null) {
      ProjectInformation info = getProjectInformation();
      if (info != null) {
        Icon icon = info.getIcon();
        cachedIcon = ImageUtilities.icon2Image(icon);
      } else {
        cachedIcon = ImageUtilities.loadImage(PROJECT_ICON);
      }
    }
    return cachedIcon;
  }

  @Override
  public Image getOpenedIcon(int type) {
    return this.getIcon(type);
  }

  @Override
  public boolean canCopy() {
    return false;
  }

  @Override
  public Action[] getActions(boolean context) {
    return new Action[]{
      new OpenProjectAction(project), //            SystemAction.get (ShowJavadocAction.class),
    //            SystemAction.get (RemoveClassPathRootAction.class),
    };
  }

  @Override
  public Action getPreferredAction() {
    return getActions(false)[0];
  }

  private ProjectInformation getProjectInformation() {
    return ProjectUtils.getInformation(project);
  }

  private static class OpenProjectAction extends AbstractAction {

    private final Project p;

    public OpenProjectAction(Project p) {
      this.p = p;
      putValue(NAME, NbBundle.getMessage(ProjectNode.class, "NAME_OpenDependentProject"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      OpenProjects.getDefault().open(new Project[]{p}, false);
    }
  }

}
