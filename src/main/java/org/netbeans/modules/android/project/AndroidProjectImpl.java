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

import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.project.ProjectCreator;
import com.android.sdklib.internal.project.ProjectProperties;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.SdkLogProvider;
import org.netbeans.modules.android.project.api.PropertyName;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider;
import org.netbeans.modules.android.project.launch.AndroidLauncherImpl;
import org.netbeans.modules.android.project.queries.AndroidAntProjectDebugInfo;
import org.netbeans.modules.android.project.queries.AndroidFileBuiltQuery;
import org.netbeans.modules.android.project.queries.AndroidManifestSourceAnt;
import org.netbeans.modules.android.project.queries.AndroidSharabilityQuery;
import org.netbeans.modules.android.project.queries.AndroidSources;
import org.netbeans.modules.android.project.queries.AndroidTemplateAttributesProvider;
import org.netbeans.modules.android.project.queries.ClassPathProviderImpl;
import org.netbeans.modules.android.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.android.project.queries.SourceLevelQueryImpl;
import org.netbeans.modules.android.project.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.android.project.queries.AndroidProjectEncodingQueryImpl;
import org.netbeans.modules.android.project.queries.AuxiliaryConfigImpl;
import org.netbeans.modules.android.project.queries.AuxiliaryPropertiesImpl;
import org.netbeans.modules.android.project.queries.BinaryForSourceQueryImpl;
import org.netbeans.modules.android.project.queries.TemplateAttributesProvider;
import org.netbeans.modules.android.project.spi.AndroidProjectDirectory;
import org.netbeans.modules.android.project.spi.ProjectRefResolver;
import org.netbeans.modules.android.project.ui.AndroidLogicalViewProvider;
import org.netbeans.modules.android.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Represents one plain android project.
 */
@VisibleForTesting
public final class AndroidProjectImpl implements AndroidProject {
  private static final Logger LOG = Logger.getLogger(AndroidProjectImpl.class.getName());

    private final FileObject dir;
    private final File dirF;
    private final PropertiesHelper propertyHelper;
    private final Lookup lookup;

    AndroidProjectImpl(FileObject dir, File dirF, DalvikPlatformManager dpm) throws IOException {
        this.dir = dir;
        this.dirF = dirF;
        propertyHelper = createPropertiesHelper();
        ensurePlatformManager(dpm);
        lookup = createLookup();
    }

    public @Override FileObject getProjectDirectory() {
        return dir;
    }

  @Override
  public AndroidProjectInfo info() {
    return lookup.lookup(AndroidProjectInfo.class);
  }

  @Override
  public File getProjectDirectoryFile() {
      // XXX what if the project is moved?
      return dirF;
  }

    @Override
  public String toString() {
      return "AndroidProject[" + FileUtil.getFileDisplayName(getProjectDirectory()) + "]"; // NOI18N
  }

    private PropertiesHelper createPropertiesHelper() {
      return new PropertiesHelper(this);
    }

    @Override
    public PropertyEvaluator evaluator() {
        return propertyHelper.evaluator();
    }

    public @Override Lookup getLookup() {
        return lookup;
    }

    // TODO where is the right place for this, has to be done before we create cp provider in lookup
    private void ensurePlatformManager(DalvikPlatformManager dpm) {
      String sdkDir = evaluator().getProperty(PropertyName.SDK_DIR.getName());
      if (sdkDir != null) {
        dpm.setSdkLocation(sdkDir);
      }
    }

    private Lookup createLookup() {
        // want to do QuerySupport.createFileEncodingQuery()
        AndroidProjectEncodingQueryImpl fileEncoding = new AndroidProjectEncodingQueryImpl(propertyHelper.evaluator());
        AuxiliaryPropertiesImpl auxProps = new AuxiliaryPropertiesImpl(this);
        Lookup base = Lookups.fixed(
            this,
            new Info(this),
            new AndroidInfoImpl(this),
            new AndroidActionProvider(this),
            new AndroidProjectOperations(this),
            new AndroidLogicalViewProvider(this),
            new CustomizerProviderImpl(this),
            new ClassPathProviderImpl(this),
            new CompiledSourceForBinaryQuery(this),
            new BinaryForSourceQueryImpl(this),
            new UnitTestForSourceQueryImpl(this),
            new AndroidFileBuiltQuery(this),
            new SourceLevelQueryImpl(propertyHelper.evaluator()),
            new AndroidSources(this),
            new AndroidManifestSourceAnt(this),
            LookupProviderSupport.createSourcesMerger(),
            new AndroidSharabilityQuery(this),
            new AndroidSubprojectProvider(this),
            // XXX SubprojectProvider impl - main for test prj, or libs for app prj
            // XXX consider whether a ProjectConfigurationProvider would be useful (debug vs. release; select AVD)
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl(this, propertyHelper)),
            new RecommendedTemplatesImpl(),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(), 
            fileEncoding,
            new AndroidTemplateAttributesProvider(this),
            new AndroidLauncherImpl(), 
            auxProps,
            new AuxiliaryConfigImpl(this),
            new TemplateAttributesProvider(this, fileEncoding),
            new AndroidConfigProvider(this, auxProps),
            new ProjectRefResolver(this),
            new AndroidAntProjectDebugInfo(this),
            new AndroidProjectDirectory() {
              @Override
              public File get() {
                return dirF;
              }
            },
            propertyHelper
        );
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-android-project/Lookup"); //NOI18N
    }

  @Override
  public void update(AndroidGeneralData data) {
    if (data == null) {
      throw new IllegalArgumentException("No project data for project update");
    }
    AndroidProjectInfo info = info();
    if (AndroidGeneralData.fromProject(this).equals(data)) {
      if (!info.isNeedsFix()) {
        LOG.log(Level.FINE, "No need to update project {0}", data);
        // nothing to update
        return;
      }
    }

    if (!info.isNeedsFix() && data.getPlatform() == null) {
      throw new IllegalArgumentException("Target platform must be selected to update the project in " + data);
    }
    DalvikPlatform platform = data.getPlatform();

    LOG.log(Level.FINE, "Updating project to {0}", data);
    SdkManager sdkManager = platform != null ? 
        platform.getSdkManager() :
        DalvikPlatformManager.getDefault().getSdkManager();
    if (sdkManager == null) {
      throw new IllegalArgumentException("Cannot find SDK manager to update the project");
    }
    ProjectCreator prjCreator = new ProjectCreator(
        sdkManager, sdkManager.getLocation(),
        ProjectCreator.OutputLevel.NORMAL, SdkLogProvider.createLogger(true));
    if (data.getMainProjectDirPath() == null) {
      // regular project
      prjCreator.updateProject(data.getProjectDirPath(),
          platform != null ? platform.getAndroidTarget() : null,
          data.getProjectName(),
          /*library*/null);
      if (data.isLibrary()) {
        propertyHelper.setProperty(
          ProjectProperties.PropertyType.PROJECT, AndroidInfoImpl.ANDROID_LIBRARY_PROPERTY, Boolean.toString(true));
      } else {
        propertyHelper.setProperty(
          ProjectProperties.PropertyType.PROJECT, AndroidInfoImpl.ANDROID_LIBRARY_PROPERTY, null);
      }
      int i = 1;
      for (String referencedPrj : data.getReferencedProjects()) {
        propertyHelper.setProperty(
          ProjectProperties.PropertyType.PROJECT, "android.library.reference." + String.valueOf(i), referencedPrj);
        i++;
      }
      // make a hole: acts as a stopmark
      propertyHelper.setProperty(
        ProjectProperties.PropertyType.PROJECT, "android.library.reference." + String.valueOf(i), null);
    } else {
      prjCreator.updateTestProject(data.getProjectDirPath(), data.getMainProjectDirPath(), sdkManager);
    }
    getProjectDirectory().refresh();
  }
}
