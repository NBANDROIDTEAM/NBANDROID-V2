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

import com.android.ddmlib.Client;
import com.android.ide.common.xml.ManifestData;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.api.AndroidProjects;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider;
import org.netbeans.modules.android.project.launch.AndroidLauncher;
import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import org.netbeans.modules.android.project.launch.Launches;
import org.netbeans.modules.android.project.launch.LaunchInfo;
import org.netbeans.modules.android.project.queries.ClassPathProviderImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.lookup.Lookups;

/**
 * Executor to handle launching actions.
 */
class LaunchExecutor {

  private static final Logger LOG = Logger.getLogger(LaunchExecutor.class.getName());

  private final AndroidProject project;

  public LaunchExecutor(AndroidProject project) {
    this.project = project;
  }

  public void doLaunchAfterBuild(final String command, FileObject buildFo, ExecutorTask task, String testClass) {
    if (!Launches.isLaunchingCommand(command)) {
      return;
    }
    // TODO(radim): where to get config
    final LaunchConfiguration launchConfig = 
        project.getLookup().lookup(AndroidConfigProvider.class).getActiveConfiguration().getLaunchConfiguration();

    final DalvikPlatform platform = AndroidProjects.projectPlatform(project);
    final AndroidLauncher launcher = Preconditions.checkNotNull(
        project.getLookup().lookup(AndroidLauncher.class));
    final String appName = AntScriptUtils.getAntScriptName(buildFo) + "-"
        + launchConfig.getMode() + ".apk";
    // when building in release mode and there is no keystore data we get only <app>-unsigned.apk
    final String alternativeAppName = AntScriptUtils.getAntScriptName(buildFo) + "-"
        + "unsigned" + ".apk";
    final LaunchInfo launchInfo = createLaunchInfo(command, appName, launchConfig, testClass);
    if (!Launches.isDebugCommand(command)) {
      task.addTaskListener(new TaskListener() {

        @Override
        public void taskFinished(Task task) {
          if (task instanceof ExecutorTask && ((ExecutorTask) task).result() != 0) {
            LOG.log(Level.FINE, "launch cancelled because build failed with status {0}",
                ((ExecutorTask) task).result());
            return;
          }
          launcher.launch(platform,
              Lookups.fixed(fixLaunchedAppName(launchInfo, appName, alternativeAppName), 
                  Launches.actionForProject(project), 
                  launchConfig,
                  project),
              command);
        }
      });
    } else {
      task.addTaskListener(new TaskListener() {

        @Override
        public void taskFinished(Task task) {
          if (task instanceof ExecutorTask && ((ExecutorTask) task).result() != 0) {
            LOG.log(Level.FINE, "launch cancelled because build failed with status {0}",
                ((ExecutorTask) task).result());
            return;
          }
          try {
            final Future<Client> future = launcher.launch(platform,
                Lookups.fixed(fixLaunchedAppName(launchInfo, appName, alternativeAppName), 
                    Launches.actionForProject(project), 
                    launchConfig,
                    project),
                command);
            if (future != null) {
              final Client c = future.get();
              final int port = c.getDebuggerListenPort();
              final Map<String, Object> properties = Maps.newHashMap();
              final ClassPathProviderImpl cpp = project.getLookup().lookup(ClassPathProviderImpl.class);
              final ClassPath sourcePath = cpp.getSourcePath();
              final ClassPath compilePath = cpp.getCompilePath();
              final ClassPath bootPath = cpp.getBootPath();
              properties.put("sourcepath", 
                  ClassPathSupport.createProxyClassPath(sourcePath, Launches.toSourcePath(compilePath)));
              properties.put("name", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N
              properties.put("jdksources", Launches.toSourcePath(bootPath)); // NOI18N
              properties.put("baseDir", FileUtil.toFile(project.getProjectDirectory()));   //NOI18N
              JPDADebugger.attach("localhost", port, new Object[]{properties}); //NOI18N
            }
          } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
          } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
          } catch (DebuggerStartException ex) {
            Exceptions.printStackTrace(ex);
          }

        }
      });
    }
  }

  private LaunchInfo createLaunchInfo(
      String command, String appName, LaunchConfiguration launchConfig, String testClass) {
    ManifestData manifest = AndroidProjects.parseProjectManifest(project);
    LaunchInfo launchInfo = new LaunchInfo(
        project.getProjectDirectory().getFileObject("bin/" + appName),
        true,
        Launches.isDebugCommand(command), launchConfig,
        manifest).withTestClass(testClass);
    if (Launches.isTestCommand(command)) {
      // TODO launch test config should hold info which instrumentation to use: runner + target package
      if (manifest != null && 
          manifest.getInstrumentations() != null && manifest.getInstrumentations().length != 0) {
        launchInfo = launchInfo.withClientName(manifest.getInstrumentations()[0].getTargetPackage());
      } else {
        LOG.log(Level.INFO, "Cannot find target package that should be tested by {0}", project);
      }
    }
    return launchInfo;
  }
    private LaunchInfo fixLaunchedAppName(LaunchInfo li, String testedAppName, String testedAppName2) {
      if (li.packageFile != null && li.packageFile.isValid()) {
        return li;
      }
      if ((li.packageFile == null || li.packageFile.isValid())) {
        FileObject apk = project.getProjectDirectory().getFileObject("bin/" + testedAppName);
        if (apk != null && apk.isValid()) {
          return li.withPackageFile(apk);
        }
        apk = project.getProjectDirectory().getFileObject("bin/" + testedAppName2);
        if (apk != null && apk.isValid()) {
          return li.withPackageFile(apk);
        }

      }
      return li;
    }
}
