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
package org.netbeans.modules.android.project.ui;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.AndroidProjectInfo;
import org.netbeans.modules.android.project.ui.customizer.CustomizerProviderImpl;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author radim
 */
public class ProjectNeedsUpdateActionFactory extends AbstractAction implements ContextAwareAction {

  /** for layer registration */
  public ProjectNeedsUpdateActionFactory() {
    putValue(Action.NAME, NbBundle.getMessage(
        ProjectNeedsUpdateActionFactory.class, "LBL_ProjectNeedsUpdate_Action"));
    putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(
        ProjectNeedsUpdateActionFactory.class, "HINT_ProjectNeedsUpdate_Action"));
    setEnabled(false);
    putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
  }

  public @Override
  void actionPerformed(ActionEvent e) {
    assert false;
  }

  public @Override
  Action createContextAwareInstance(Lookup actionContext) {
    Collection<? extends Project> p = actionContext.lookupAll(Project.class);
    if (p.size() != 1) {
      return this;
    }
    Project project = p.iterator().next();
    AndroidLogicalViewProvider lvp = project.getLookup().lookup(AndroidLogicalViewProvider.class);
    if (lvp == null) {
      return this;
    }
    return new ProjectUpdateAction(project);
  }

  /** This action is created only when project has broken references.
   * Once these are resolved the action is disabled.
   */
  private static class ProjectUpdateAction extends AbstractAction {

    private final CustomizerProviderImpl customizerProvider;

    public ProjectUpdateAction(Project project) {
      AndroidProjectInfo info = project.getLookup().lookup(AndroidProjectInfo.class);
      customizerProvider = project.getLookup().lookup(CustomizerProviderImpl.class);

      putValue(Action.NAME, NbBundle.getMessage(
          ProjectNeedsUpdateActionFactory.class, "LBL_ProjectNeedsUpdate_Action"));
      putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(
          ProjectNeedsUpdateActionFactory.class, "HINT_ProjectNeedsUpdate_Action"));
      setEnabled(info.isNeedsFix());
      putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      customizerProvider.showCustomizer("General");
    }
  }
}