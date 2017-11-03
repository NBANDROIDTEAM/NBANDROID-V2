package org.nbandroid.netbeans.gradle.query;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidArtifactOutput;
import org.nbandroid.netbeans.gradle.config.AndroidBuildVariants;
import com.android.builder.model.Variant;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.nbandroid.netbeans.gradle.launch.GradleLaunchExecutor;
import org.netbeans.api.project.Project;
import org.netbeans.gradle.project.api.config.ProfileDef;
import org.netbeans.gradle.project.api.task.BuiltInGradleCommandQuery;
import org.netbeans.gradle.project.api.task.CommandCompleteListener;
import org.netbeans.gradle.project.api.task.CustomCommandActions;
import org.netbeans.gradle.project.api.task.GradleCommandTemplate;
import org.netbeans.gradle.project.api.task.TaskKind;
import org.nbandroid.netbeans.gradle.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.launch.Launches;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
public class BuiltInCommands implements BuiltInGradleCommandQuery {
  private static final Logger LOG = Logger.getLogger(BuiltInCommands.class.getName());

  private final BuildVariant buildConfig;
  private final Project project;

  public BuiltInCommands(Project project, BuildVariant buildConfig) {
    this.buildConfig = Preconditions.checkNotNull(buildConfig);
    this.project = Preconditions.checkNotNull(project);
  }
  
  /*
   * <ul>
   *  <li>{@code ActionProvider.COMMAND_BUILD}</li>
   *  <li>{@code ActionProvider.COMMAND_TEST}</li>
   *  <li>{@code ActionProvider.COMMAND_CLEAN}</li>
   *  <li>{@code ActionProvider.COMMAND_RUN}</li>
   *  <li>{@code ActionProvider.COMMAND_DEBUG}</li>
   *  <li>{@code ActionProvider.COMMAND_REBUILD}</li>
   *  <li>{@code ActionProvider.COMMAND_TEST_SINGLE}</li>
   *  <li>{@code ActionProvider.COMMAND_DEBUG_TEST_SINGLE}</li>
   *  <li>{@code ActionProvider.COMMAND_RUN_SINGLE}</li>
   *  <li>{@code ActionProvider.COMMAND_DEBUG_SINGLE}</li>
   *  <li>{@code JavaProjectConstants.COMMAND_JAVADOC}</li>
   *  <li>{@code JavaProjectConstants.COMMAND_DEBUG_FIX}</li>
   * </ul>
   */
  
  @Override
  public Set<String> getSupportedCommands() {
    return Sets.newHashSet(
        ActionProvider.COMMAND_BUILD,
        AndroidConstants.COMMAND_BUILD_TEST,
        ActionProvider.COMMAND_CLEAN,
        ActionProvider.COMMAND_RUN,
        ActionProvider.COMMAND_TEST,
        ActionProvider.COMMAND_DEBUG);
  }

  @Override
  public String tryGetDisplayNameOfCommand(String command) {
    return null;
  }

  @Override
  public GradleCommandTemplate tryGetDefaultGradleCommand(@Nullable ProfileDef profileDef, @Nonnull String command) {
    LOG.log(Level.FINE, "get Gradle command {0} {1}", new Object[]{profileDef, command});
    // TODO reflect current profile
    if (ActionProvider.COMMAND_BUILD.equals(command) ||
        ActionProvider.COMMAND_DEBUG.equals(command)) {
      GradleCommandTemplate.Builder builder = 
          new GradleCommandTemplate.Builder(Collections.singletonList("assembleDebug"));
      builder.setBlocking(false);
      return builder.create();
    } else if (ActionProvider.COMMAND_RUN.equals(command)) {
      GradleCommandTemplate.Builder builder = 
          new GradleCommandTemplate.Builder(Collections.singletonList("assembleDebug"));
      builder.setBlocking(false);
      return builder.create();
    } else if (ActionProvider.COMMAND_CLEAN.equals(command)) {
      GradleCommandTemplate.Builder builder = 
          new GradleCommandTemplate.Builder(Collections.singletonList("clean"));
      builder.setBlocking(false);
      return builder.create();
    } else if (ActionProvider.COMMAND_TEST.equals(command) ||
        AndroidConstants.COMMAND_BUILD_TEST.equals(command)) {
      GradleCommandTemplate.Builder builder = 
          new GradleCommandTemplate.Builder(Lists.newArrayList(
              "assemble" + AndroidTaskVariableQuery.BUILD_VARIANT_VARIABLE.getScriptReplaceConstant(), 
              "assemble" + AndroidTaskVariableQuery.BUILD_VARIANT_VARIABLE.getScriptReplaceConstant() + "Test"));
      builder.setBlocking(false);
      return builder.create();
    }
    return null;
  }

  private static TaskKind forActionCommand(String command) {
    if (ActionProvider.COMMAND_RUN.equals(command)) {
      return TaskKind.RUN;
    } else if (ActionProvider.COMMAND_DEBUG.equals(command)) {
      return TaskKind.DEBUG;
    }
    return TaskKind.OTHER;
  }
  
  @Override
  public CustomCommandActions tryGetCommandDefs(@Nullable ProfileDef profileDef, final @Nonnull String command) {
    LOG.log(Level.FINE, "get Gradle command def {0} {1}", new Object[]{profileDef, command});
    Variant variant = buildConfig.getCurrentVariant();
    AndroidArtifact apk = variant != null ? variant.getMainArtifact() : null;
    AndroidArtifact testApk = variant != null ? 
        AndroidBuildVariants.instrumentTestArtifact(variant.getExtraAndroidArtifacts()) : null;
    final AndroidArtifact launchedApk = Launches.isTestCommand(command) ? testApk : apk;
    if (ActionProvider.COMMAND_RUN.equals(command) ||
        ActionProvider.COMMAND_DEBUG.equals(command) ||
        ActionProvider.COMMAND_TEST.equals(command)) {
      CustomCommandActions.Builder builder = new CustomCommandActions.Builder(forActionCommand(command));
      builder.setCommandCompleteListener(new CommandCompleteListener() {

        @Override
        public void onComplete(Throwable error) {
          refreshBuildDir();
          if (error != null) {
            LOG.log(Level.INFO, "build failed", error);
            return;
          }
          boolean launched = false;
          for (AndroidArtifactOutput aaOutput : launchedApk.getOutputs()) {
            LOG.log(Level.FINE, "Attempt to launch {0}", aaOutput);
            if (aaOutput.getMainOutputFile().getOutputFile().exists()) {
              launched = true;
              new GradleLaunchExecutor(project).doLaunchAfterBuild(command, aaOutput);
            } else {
              LOG.log(Level.INFO, "Attempt to launch {0}: skipped because file is missing", aaOutput);
            }
          }
          if (!launched) {
            LOG.log(Level.INFO, "nothing to run/debug: {0}", launchedApk);
          }
        }

      });
      return builder.create();
    } else if (ActionProvider.COMMAND_BUILD.equals(command) ||
        AndroidConstants.COMMAND_BUILD_TEST.equals(command)) {
      CustomCommandActions.Builder builder = new CustomCommandActions.Builder(forActionCommand(command));
      builder.setCommandCompleteListener(new CommandCompleteListener() {

        @Override
        public void onComplete(Throwable error) {
          refreshBuildDir();
        }

      });
      return builder.create();
    } else 
    return null;
  }

  private void refreshBuildDir() {
    FileObject buildDir = project.getProjectDirectory().getFileObject("build");
    if (buildDir == null) {
      return;
    }
    buildDir.refresh();
  }
  
  private static class Command {
    private final String command;
    private final GradleCommandTemplate cmdTemplate;
    private final CustomCommandActions cmdActions;

    public Command(String command, GradleCommandTemplate cmdTemplate, CustomCommandActions cmdActions) {
      this.command = command;
      this.cmdTemplate = cmdTemplate;
      this.cmdActions = cmdActions;
    }
  }
}
