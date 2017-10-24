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

package org.netbeans.modules.android.project.ui.customizer;

import org.netbeans.modules.android.project.AndroidGeneralData;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.AndroidProjectInfo;
import org.netbeans.modules.android.project.JarLibsModel;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * Customization of android project
 */
public class CustomizerProviderImpl implements CustomizerProvider {

  public static final String CUSTOMIZER_FOLDER_PATH =
      "Projects/org-netbeans-modules-android-project/Customizer"; //NO18N

  private final AndroidProject project;

//    private ProjectCustomizer.Category categories[];
//    private ProjectCustomizer.CategoryComponentProvider panelProvider;

  // Option indexes
  private static final int OPTION_OK = 0;
  private static final int OPTION_CANCEL = OPTION_OK + 1;


  public CustomizerProviderImpl(AndroidProject project) {
    this.project = project;
  }

  @Override
  public void showCustomizer() {
    showCustomizer(null);
  }

  public void showCustomizer(String preselectedCategory) {
    showCustomizer(preselectedCategory, null);
  }

  public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {

    final AndroidGeneralData data = AndroidGeneralData.fromProject(project);
    final AndroidProjectInfo info = project.info();
    final JarLibsModel jarLibs = new JarLibsModel(project);
    AndroidConfigProvider cfgProvider = project.getLookup().lookup(AndroidConfigProvider.class);
    final ConfigGroup configs = new ConfigGroup(
        cfgProvider.getConfigurations(), cfgProvider.getActiveConfiguration());

    Lookup context = Lookups.fixed(new Object[] {
        project,
        data,
        info,
        jarLibs,
        configs,
        new SubCategoryProvider(preselectedCategory, preselectedSubCategory)
    });

    OptionListener optionsListener = new OptionListener(project, data, configs, jarLibs);
    Dialog dialog = ProjectCustomizer.createCustomizerDialog(
        CUSTOMIZER_FOLDER_PATH, context, preselectedCategory, optionsListener, null);
    optionsListener.attachDialog(dialog);
    dialog.setTitle( MessageFormat.format(
            NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N
            new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

    dialog.setVisible(true);
  }

  /** Listens to the actions on the Customizer's option buttons */
  private static class OptionListener extends WindowAdapter implements ActionListener {

    private final AndroidProject project;
    private final ConfigGroup configs;
    private final AndroidGeneralData data;
    private final JarLibsModel jarLibs;
    private Dialog dialog;

    private OptionListener(AndroidProject project, AndroidGeneralData data,
        ConfigGroup configs, JarLibsModel jarLibs) {
      this.project = project;
      this.data = data;
      this.configs = configs;
      this.jarLibs = jarLibs;
    }

    private void attachDialog(Dialog dialog) {
      this.dialog = dialog;
      dialog.addWindowListener(this);
    }

    // Listening to OK button ----------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
      // Store the properties into project

      // XXX save modified properties
      project.update(data);
      // TODO(radim) handle errors / return value
      jarLibs.apply(project);
      AndroidConfigProvider cfgProvider = project.getLookup().lookup(AndroidConfigProvider.class);
      cfgProvider.setConfigurations(configs.getConfigs());
      try {
        cfgProvider.setActiveConfiguration(configs.getCurrentConfig());
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
      cfgProvider.save();

      // Close & dispose the the dialog
      if (dialog != null) {
        dialog.setVisible(false);
        dialog.dispose();
      }
    }

    // Listening to window events ------------------------------------------

    @Override
    public void windowClosed( WindowEvent e) {
    }

    @Override
    public void windowClosing (WindowEvent e) {
      //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
      //may not be called
      if ( dialog != null ) {
        dialog.setVisible(false);
        dialog.dispose();
      }
    }
  }

  static final class SubCategoryProvider {

    private String subcategory;
    private String category;

    SubCategoryProvider(String category, String subcategory) {
      this.category = category;
      this.subcategory = subcategory;
    }

    public String getCategory() {
      return category;
    }

    public String getSubcategory() {
      return subcategory;
    }
  }
}
