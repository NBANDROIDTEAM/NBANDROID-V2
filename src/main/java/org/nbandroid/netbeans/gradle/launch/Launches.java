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

package org.nbandroid.netbeans.gradle.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.nbandroid.netbeans.gradle.configs.AndroidConfigProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;

/**
 * Utilities related to {@link LaunchConfiguration}.
 *
 * @author radim
 */
public class Launches {

  public static AndroidLauncher createLauncher() {
    return new AndroidLauncherImpl();
  }
  
  public static LaunchAction testAction() {
    return new TestLaunchAction();
  }
  
  public static LaunchAction actionForProject(Project p) {

    AndroidConfigProvider cfgProvider = p.getLookup().lookup(AndroidConfigProvider.class);
    Action launchAction = cfgProvider != null ?
        cfgProvider.getActiveConfiguration().getLaunchConfiguration().getLaunchAction() :
        null;
    if (Action.DO_NOTHING == launchAction) {
      return new EmptyLaunchAction();
    }
    return defaultActionForProject(p);
  }

  private static LaunchAction defaultActionForProject(Project p) {
        return new ActivityLaunchAction();//TODO new TestLaunchAction()
    }

  public static boolean isDebugCommand(String command) {
    return ActionProvider.COMMAND_DEBUG.equals(command)
        || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(command);
  }

  public static boolean isTestCommand(String command) {
    return ActionProvider.COMMAND_TEST.equals(command)
        || ActionProvider.COMMAND_TEST_SINGLE.equals(command)
        || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(command);
  }

  public static boolean isLaunchingCommand(String command) {
    return ActionProvider.COMMAND_DEBUG.equals(command)
        || ActionProvider.COMMAND_RUN.equals(command)
        || ActionProvider.COMMAND_TEST.equals(command)
        || ActionProvider.COMMAND_TEST_SINGLE.equals(command)
        || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(command);
  }

  /**
   * Tries to convert a classpath with classes into a classpath containing java source code.
   *
   * @param cp
   * @return
   */
  public static ClassPath toSourcePath(final ClassPath cp) {
    final List<FileObject> resources = new ArrayList<FileObject>();
    for (ClassPath.Entry e : cp.entries()) {
      final FileObject[] srcRoots = SourceForBinaryQuery.findSourceRoots(e.getURL()).getRoots();
      resources.addAll(Arrays.asList(srcRoots));
    }
    return ClassPathSupport.createClassPath(resources.toArray(new FileObject[resources.size()]));
  }
}
