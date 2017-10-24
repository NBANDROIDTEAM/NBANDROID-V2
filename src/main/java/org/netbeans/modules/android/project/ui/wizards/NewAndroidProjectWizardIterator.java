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

package org.netbeans.modules.android.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.AndroidGeneralData;
import org.netbeans.modules.android.project.AndroidProjectUtil;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Android project.
 */
public class NewAndroidProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N

  public NewAndroidProjectWizardIterator() {
    // XXX may need to switch between app and lib projects?
  }

  private WizardDescriptor.Panel[] createPanels() {
    return new WizardDescriptor.Panel[]{
          new PanelConfigureProject(false)
        };
  }

  private String[] createSteps() {
    return new String[]{
          NbBundle.getMessage(NewAndroidProjectWizardIterator.class, "LAB_ConfigureProject"),};
  }

  @Override
  public Set<?> instantiate() throws IOException {
    assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
    return null;
  }

  @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
    handle.start(4);
    handle.progress(NbBundle.getMessage(NewAndroidProjectWizardIterator.class,
        "LBL_NewAndroidProjectWizardIterator_WizardProgress_ReadingProperties"));
    Set<FileObject> resultSet = new HashSet<FileObject>();
    File dirF = (File) wiz.getProperty("projdir");        //NOI18N
    if (dirF != null) {
      dirF = FileUtil.normalizeFile(dirF);
    }
    String name = (String) wiz.getProperty("name");        //NOI18N
    String activity = (String) wiz.getProperty("activityName");        //NOI18N
    String pkgName = (String) wiz.getProperty("packageName");           //NOI18N
    DalvikPlatform platform = (DalvikPlatform)wiz.getProperty("platform"); // NOI18N
    AndroidGeneralData data = new AndroidGeneralData();
    data.setProjectDirPath(dirF.getAbsolutePath());
    data.setProjectName(name);
    data.setPlatform(platform);

    handle.progress(NbBundle.getMessage(NewAndroidProjectWizardIterator.class, 
        "LBL_NewAndroidProjectWizardIterator_WizardProgress_CreatingProject"), 1);

    AndroidProjectUtil.create(data, pkgName, activity);
    handle.progress(2);

    FileObject dir = FileUtil.toFileObject(dirF);
    dir.refresh();
    handle.progress(3);

    // Returning FileObject of project diretory.
    // Project will be open and set as main
    resultSet.add(dir);
    handle.progress(NbBundle.getMessage(NewAndroidProjectWizardIterator.class,
        "LBL_NewAndroidProjectWizardIterator_WizardProgress_PreparingToOpen"), 4);
    dirF = dirF.getParentFile();
    if (dirF != null && dirF.exists()) {
      ProjectChooser.setProjectsFolder(dirF);
    }

    return resultSet;
  }


    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty("projdir",null);           //NOI18N
            this.wiz.putProperty("name",null);          //NOI18N
            this.wiz = null;
            panels = null;
        }
    }

  @Override
  public String name() {
    return NbBundle.getMessage(NewAndroidProjectWizardIterator.class, "LAB_IteratorName", index + 1, panels.length);
  }

  @Override
  public boolean hasNext() {
    return index < panels.length - 1;
  }

  @Override
  public boolean hasPrevious() {
    return index > 0;
  }
  @Override
  public void nextPanel() {
    if (!hasNext()) throw new NoSuchElementException();
      index++;
  }
  @Override
  public void previousPanel() {
    if (!hasPrevious()) throw new NoSuchElementException();
      index--;
  }

  @Override
  public WizardDescriptor.Panel current () {
    return panels[index];
  }

  // If nothing unusual changes in the middle of the wizard, simply:
  @Override public final void addChangeListener(ChangeListener l) {}
  @Override public final void removeChangeListener(ChangeListener l) {}

  static String getPackageName(String displayName) {
    StringBuilder builder = new StringBuilder();
    boolean firstLetter = true;
    for (int i = 0; i < displayName.length(); i++) {
      char c = displayName.charAt(i);
      if ((!firstLetter && Character.isJavaIdentifierPart(c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
        firstLetter = false;
        if (Character.isUpperCase(c)) {
          c = Character.toLowerCase(c);
        }
        builder.append(c);
      }
    }
    return builder.length() == 0 ? NbBundle.getMessage(NewAndroidProjectWizardIterator.class, "TXT_DefaultPackageName") : builder.toString();
  }
}
