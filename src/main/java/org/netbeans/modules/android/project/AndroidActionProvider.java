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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider;
import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Action provider of the Android project.
 */
class AndroidActionProvider implements ActionProvider {

  private static final Logger LOG = Logger.getLogger(AndroidActionProvider.class.getName());

  // Commands available from Android project
  private static final String[] supportedActions = {
      COMMAND_BUILD,
      COMMAND_CLEAN,
      COMMAND_REBUILD,
      COMMAND_RUN,
      COMMAND_DEBUG,
//        JavaProjectConstants.COMMAND_JAVADOC,
//      COMMAND_TEST,
      COMMAND_DELETE,
      COMMAND_COPY,
      COMMAND_MOVE,
      COMMAND_RENAME,
  };

  private AndroidProject project;

  /** Mapping between command and Ant target(s). */
  private enum CommandTarget {
    CLEAN(COMMAND_CLEAN, "clean"),
    RUN(COMMAND_RUN, "debug"),
    DEBUG(COMMAND_DEBUG, "debug"),
    BUILD(COMMAND_BUILD, "debug"),
    TEST(COMMAND_TEST, "debug"),
    TEST_SINGLE(COMMAND_TEST_SINGLE, new String[] {"debug"}, true),
    DEBUG_TEST_SINGLE(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug"}, true),
    REBUILD(COMMAND_REBUILD, new String[] { "clean", "debug" }, false);

    public final String command;
    private final String[] prjCmds;
    private final boolean needsTest;

    private CommandTarget(String command, String prjTarget) {
      this(command, new String[] { prjTarget }, false);
    }

    private CommandTarget(String command, String[] prjTargets, boolean needsTest) {
      this.command = command;
      this.prjCmds = prjTargets;
      this.needsTest = needsTest;
    }
    
    public boolean isEnabled(AndroidProject project, Lookup context) {
      if (needsTest) {
        FileObject[] fos = findTestSources(project, context);
        return fos != null && fos.length == 1;
      }
      return true;
    }

    public ActionCommand getTargets(String mode, AndroidProject project, Lookup context) {
      String [] targets = new String[prjCmds.length];
      System.arraycopy(prjCmds, 0, targets, 0, prjCmds.length);
      if (!LaunchConfiguration.MODE_DEBUG.equals(mode)) {
        for (int i = 0; i < targets.length; i++) {
          if (LaunchConfiguration.MODE_DEBUG.equals(targets[i])) {
            targets[i] = mode;
          }
        }
      }
      List<String> clzNames = Lists.newArrayList();
      if (needsTest) {
        FileObject[] fos = findTestSources(project, context);
        for (FileObject fo : fos) {
          ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
          if (cp != null) {
            clzNames.add(cp.getResourceName(fo, '.', false));
          }
        }
      }
      return new ActionCommand(targets, Joiner.on(',').join(clzNames));
    }
  }

    public AndroidActionProvider(AndroidProject project) {
        this.project = project;
    }

    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject("build.xml");
    }

    @Override
    public String[] getSupportedActions() {
      return project.info().isTest() ?
          ObjectArrays.concat(
              supportedActions, 
              new String[] {COMMAND_TEST, COMMAND_TEST_SINGLE, COMMAND_DEBUG_TEST_SINGLE},
              String.class) :
          supportedActions;
    }

    @Override
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }

        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }

        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }

        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }

        // XXX ActionUtils.runTarget, or call android tool
        final Runnable action = new Runnable () {
            @Override
            public void run () {
                Properties p = new Properties();
                ActionCommand ac = getTargetNames(command, context, p);
                if (ac.targetNames == null || ac.targetNames.length == 0) {
                    return;
                }
                try {
                  FileObject buildFo = findBuildXml();
                  ExecutorTask task = ActionUtils.runTarget(buildFo, ac.targetNames, null);

                  // TODO use properties to pass test names 
                  new LaunchExecutor(project).doLaunchAfterBuild(command, buildFo, task, ac.testClass);
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };

        // TODO(radim): handle bkgScanSensitiveActions
        action.run();
    }

  static class ActionCommand {
    final String[] targetNames;
    final String testClass;

    ActionCommand(String[] targetNames, String testClass) {
      this.targetNames = targetNames;
      this.testClass = testClass;
    }
  }
  
  /**
   * @return array of targets or null to stop execution; can return empty array
   */
  /*private*/ ActionCommand getTargetNames(final String command, final Lookup context, Properties p) throws IllegalArgumentException {
    final LaunchConfiguration launch =
        project.getLookup().lookup(AndroidConfigProvider.class).getActiveConfiguration().getLaunchConfiguration();
    return Iterables.find(
        Iterables.transform(
            Lists.newArrayList(CommandTarget.values()),
            new Function<CommandTarget, ActionCommand>() {
              @Override
              public ActionCommand apply(CommandTarget t) {
                return t.command.equals(command) && t.isEnabled(project, context) ? 
                    t.getTargets(launch.getMode(), project, context) : 
                    null;
              }
            }),
        Predicates.notNull(),
        new ActionCommand(new String[0], null));
  }

  @Override
  public boolean isActionEnabled(final String command, final Lookup context) {
    FileObject buildXml = findBuildXml();
    LOG.log(Level.FINER, "action {0} on: {1}", new Object[]{command, buildXml});
    if (buildXml == null || !buildXml.isValid()) {
      return false;
    }
    return Iterables.any(
        Lists.newArrayList(CommandTarget.values()),
        new Predicate<CommandTarget>() {

          @Override
          public boolean apply(CommandTarget t) {
            return t.command.equals(command) && t.isEnabled(project, context);
          }
        });
  }
    
  /**
   * Find either selected tests or tests which belong to selected source files
   */
  @CheckForNull
  private static FileObject[] findTestSources(AndroidProject project, Lookup context) {
    //XXX: Ugly, should be rewritten
    // TODO make sure this is test project
    if (!project.info().isTest()) {
      return null;
    }
    Sources sources = ProjectUtils.getSources(project);
    SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    for (SourceGroup sg : sourceGroups) {
      FileObject[] files = ActionUtils.findSelectedFiles(context, sg.getRootFolder(), ".java", true);
      if (files != null) {
        return files;
      }
    }
    return null;
  }
}
