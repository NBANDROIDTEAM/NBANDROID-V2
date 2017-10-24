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
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.AndroidProjectInfo;
import org.netbeans.modules.android.project.JarLibsModel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Provider for Android project specific panel in project customizer.
 */
public class AndroidCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
  /** General category in project customizer. */
  static final String GENERAL = "General"; // NOI18N
  /** Android libraries category in project customizer. */
  static final String LIBRARIES = "Libraries";
  /** JAR libraries category in project customizer. */
  static final String JARS = "JARs";
  /** Run category in project customizer. */
  public static final String RUN = "Run";

  private String name;

  public AndroidCompositePanelProvider(String name) {
    this.name = name;
  }

  @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        if (LIBRARIES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LIBRARIES,
                    bundle.getString( "LBL_Config_Libraries" ), // NOI18N
                    null);
        } else if (JARS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JARS,
                    bundle.getString( "LBL_Config_JARs" ), // NOI18N
                    null);
        } else if (GENERAL.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    GENERAL,
                    bundle.getString( "LBL_Config_General" ), // NOI18N
                    null);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    bundle.getString( "LBL_Config_Run" ), // NOI18N
                    null);
        }
        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        AndroidProject project = context.lookup(AndroidProject.class);
        AndroidGeneralData data = context.lookup(AndroidGeneralData.class);
        AndroidProjectInfo info = context.lookup(AndroidProjectInfo.class);
        JarLibsModel jarLibs = context.lookup(JarLibsModel.class);
        ConfigGroup configs = context.lookup(ConfigGroup.class);
        AndroidProjectProperties projectProperties = context.lookup(AndroidProjectProperties.class);
        if (LIBRARIES.equals(nm)) {
            return new CustomizerLibraries(data, project);
        } else if (JARS.equals(nm)) {
            return new CustomizerJARs(jarLibs);
        } else if (GENERAL.equals(nm)) {
            return new CustomizerGeneral(data, info, DalvikPlatformManager.getDefault());
        } else if (RUN.equals(nm)) {
            return info.isTest() ?
                new CustomizerTest(project, configs) :
                new CustomizerRun(project, configs);
        }
        return new JPanel();

    }

  public static AndroidCompositePanelProvider createLibraries() {
    return new AndroidCompositePanelProvider(LIBRARIES);
  }

  public static AndroidCompositePanelProvider createJARs() {
    return new AndroidCompositePanelProvider(JARS);
  }

    public static AndroidCompositePanelProvider createRun() {
        return new AndroidCompositePanelProvider(RUN);
    }

    public static AndroidCompositePanelProvider createGeneral() {
        return new AndroidCompositePanelProvider(GENERAL);
    }
}
