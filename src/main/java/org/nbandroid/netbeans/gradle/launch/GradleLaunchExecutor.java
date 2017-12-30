package org.nbandroid.netbeans.gradle.launch;

import com.android.builder.model.AndroidArtifactOutput;
import com.android.ddmlib.Client;
import com.android.ide.common.xml.ManifestData;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.api.AndroidProjects;
import org.nbandroid.netbeans.gradle.config.AndroidTestRunConfiguration;
import org.nbandroid.netbeans.gradle.configs.ConfigBuilder;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 * Executor to handle launching actions.
 */
public class GradleLaunchExecutor {

    private static final Logger LOG = Logger.getLogger(GradleLaunchExecutor.class.getName());

    private final Project project;

    public GradleLaunchExecutor(Project project) {
        this.project = project;
    }

    public void doLaunchAfterBuild(final String command, AndroidArtifactOutput artifactOutput) {
        if (!Launches.isLaunchingCommand(command)) {
            return;
        }
        // TODO(radim): where to get config
        final LaunchConfiguration launchConfig = launchConfig();
        // project.getLookup().lookup(AndroidConfigProvider.class).getActiveConfiguration().getLaunchConfiguration();

        final AndroidPlatformInfo platform = AndroidProjects.projectPlatform(project);
        final AndroidLauncher launcher = Preconditions.checkNotNull(
                project.getLookup().lookup(AndroidLauncher.class));
        final LaunchInfo launchInfo = createLaunchInfo(artifactOutput, command, launchConfig);
        final LaunchAction launchAction = findLaunchAction(command);
        if (!Launches.isDebugCommand(command)) {
            launcher.launch(platform,
                    Lookups.fixed(launchInfo,
                            launchAction,
                            launchConfig,
                            project),
                    command);
        } else {
            try {
                final Future<Client> future = launcher.launch(platform,
                        Lookups.fixed(launchInfo,
                                launchAction,
                                launchConfig,
                                project),
                        command);
                if (future != null) {
                    final Client c = future.get();
                    final int port = c.getDebuggerListenPort();
                    final Map<String, Object> properties = Maps.newHashMap();
                    final GradleAndroidClassPathProvider cpp
                            = project.getLookup().lookup(GradleAndroidClassPathProvider.class);
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
            } catch (InterruptedException | ExecutionException | DebuggerStartException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private LaunchInfo createLaunchInfo(
            AndroidArtifactOutput artifactOutput, final String command, final LaunchConfiguration launchConfig) {
        File manifestFile = artifactOutput.getGeneratedManifest();
        ManifestData manifest = null;
        try {
            manifest = AndroidProjects.parseProjectManifest(new FileInputStream(manifestFile));
        } catch (FileNotFoundException ex) {
            LOG.log(Level.FINE, "Cannot parse manifest for launch of " + project, ex);
        }
        LaunchInfo launchInfo = new LaunchInfo(
                FileUtil.toFileObject(artifactOutput.getMainOutputFile().getOutputFile()),
                true,
                Launches.isDebugCommand(command), launchConfig,
                manifest);
        if (Launches.isTestCommand(command)) {
            // TODO launch test config should hold info which instrumentation to use: runner + target package
            if (manifest != null
                    && manifest.getInstrumentations() != null && manifest.getInstrumentations().length != 0) {
                launchInfo = launchInfo.withClientName(manifest.getInstrumentations()[0].getTargetPackage());
            } else {
                LOG.log(Level.INFO, "Cannot find target package that should be tested by {0}", project);
            }
        }
        return launchInfo;
    }

    private LaunchConfiguration launchConfig() {
        AndroidTestRunConfiguration testCfg = project.getLookup().lookup(AndroidTestRunConfiguration.class);
        return ConfigBuilder.builder().
                withName("debug").
                withLaunchAction(LaunchConfiguration.Action.MAIN).
                withTargetMode(LaunchConfiguration.TargetMode.AUTO).
                withMode(LaunchConfiguration.MODE_DEBUG).
                withTestRunner(testCfg.getTestRunner()).config().getLaunchConfiguration();
    }

    private LaunchAction findLaunchAction(String command) {
        if (Launches.isTestCommand(command)) {
            return Launches.testAction();
        } else {
            return Launches.actionForProject(project);
        }

    }
}
