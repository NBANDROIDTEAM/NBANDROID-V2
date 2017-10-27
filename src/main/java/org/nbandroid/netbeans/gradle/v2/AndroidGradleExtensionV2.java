package org.nbandroid.netbeans.gradle.v2;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.Dependencies;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.Variant;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.nbandroid.netbeans.ext.navigation.ProjectResourceLocator;
import org.nbandroid.netbeans.gradle.*;
import org.nbandroid.netbeans.gradle.config.AndroidBuildVariants;
import org.nbandroid.netbeans.gradle.config.AndroidTestRunConfiguration;
import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.nbandroid.netbeans.gradle.launch.GradleDebugInfo;
import org.nbandroid.netbeans.gradle.query.AndroidTaskVariableQuery;
import org.nbandroid.netbeans.gradle.query.BuiltInCommands;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.nbandroid.netbeans.gradle.query.GradleAndroidManifest;
import org.nbandroid.netbeans.gradle.query.GradleAndroidSources;
import org.nbandroid.netbeans.gradle.query.GradlePlatformResolver;
import org.nbandroid.netbeans.gradle.query.GradleSourceForBinaryQuery;
import org.nbandroid.netbeans.gradle.query.SourceLevelQueryImpl2;
import org.nbandroid.netbeans.gradle.ui.AndroidTestsProvider;
import org.nbandroid.netbeans.gradle.ui.BuildCustomizerProvider;
import org.netbeans.api.project.Project;
import org.netbeans.gradle.project.api.entry.GradleProjectExtension2;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.StatsCollector;
import org.netbeans.modules.android.project.api.PropertyName;
import org.netbeans.modules.android.project.api.TestOutputConsumer;
import org.netbeans.modules.android.project.launch.Launches;
import org.netbeans.modules.android.project.spi.AndroidProjectDirectory;
import org.netbeans.modules.android.project.spi.ProjectRefResolver;
import org.netbeans.modules.android.testrunner.TestOutputConsumerLookupProvider;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author radim
 */
public class AndroidGradleExtensionV2 implements GradleProjectExtension2<SerializableLookup> {

    private static final Logger LOG = Logger.getLogger(AndroidGradleExtensionV2.class.getName());

    private final Project project;
    private final InstanceContent ic;
    private final Lookup projectAddOnLookup;
    private final List<Object> items = Lists.newArrayList();
    private final GradleProjectOpenedHook openHook;
    private final SourceLevelQueryImpl2 levelQuery = new SourceLevelQueryImpl2();
    // testing support
    @VisibleForTesting
    public final CountDownLatch loadedSignal = new CountDownLatch(1);
    @VisibleForTesting
    volatile AndroidProject aPrj = null;
    @VisibleForTesting
    volatile GradleBuild gradleBuild = null;

    public AndroidGradleExtensionV2(Project project) {
        this.project = Preconditions.checkNotNull(project);
        ic = new InstanceContent();
        projectAddOnLookup = new AbstractLookup(ic);
        final AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        BuildVariant buildCfg = new BuildVariant(props);
        AndroidTestRunConfiguration testCfg = new AndroidTestRunConfiguration(props);
        items.add(buildCfg);
        items.add(testCfg);
        items.add(levelQuery);
        items.add(new GradlePlatformResolver());
        items.add(new GradleAndroidSources(project, buildCfg));
        items.add(new GradleAndroidManifest(project, buildCfg));
        items.add(new GradleAndroidClassPathProvider(buildCfg));
        items.add(new GradleSourceForBinaryQuery(buildCfg));
        items.add(new AndroidGradleNodes(project));
        items.add(new ProjectResourceLocator(project));
        items.add(new AndroidTaskVariableQuery(buildCfg));
        items.add(new BuiltInCommands(project, buildCfg));
        items.add(new BuildCustomizerProvider());
        items.add(new AndroidTestsProvider());
        items.add(new GradleDebugInfo(project));
        items.add(new ProjectRefResolver(project));
        items.add(Launches.createLauncher());
        items.add(new TestOutputConsumerLookupProvider().createAdditionalLookup(
                Lookups.singleton(project)).lookup(TestOutputConsumer.class));
        final File prjDir = FileUtil.toFile(project.getProjectDirectory());
        items.add(new AndroidProjectDirectory() {
            @Override
            public File get() {
                return prjDir;
            }
        });
        ic.add(this);
        openHook = new GradleProjectOpenedHook(project);
        ic.add(openHook);
    }

    public List<Object> getItems() {
        return items;
    }

    @Override
    public Lookup getPermanentProjectLookup() {
        LOG.log(Level.FINE, "lookup requested");
        return projectAddOnLookup;
    }

    @Override
    public void activateExtension(SerializableLookup parsedModel) {
        modelsLoaded(parsedModel.lookup);
    }

    @Override
    public void deactivateExtension() {
        modelsLoaded(Lookup.EMPTY);
    }

    public void modelsLoaded(Lookup modelLookup) {

        // start with no-op
        try {
            LOG.log(Level.INFO, "Gradle model for Android project {0} loaded", project.getProjectDirectory());
            if (modelLookup == null) {
                LOG.log(Level.CONFIG, "Gradle model is null");
                return;
            }
            LOG.log(Level.FINE, "lookup: {0}", modelLookup.lookupAll(Object.class));
            AndroidProject aProject = modelLookup.lookup(AndroidProject.class);
            GradleBuild build = modelLookup.lookup(GradleBuild.class);
            if (aProject != null) {
                updateAndroidProject(aProject, build);
            } else {
                clearAndroidProject();
            }
        } finally {
            loadedSignal.countDown();
        }
    }

    private void updateAndroidProject(AndroidProject aPrj, GradleBuild build) {
        if (LOG.isLoggable(Level.FINE)) {
            logLoadedProject(aPrj, build);
        }
        ensurePlatformManager(FileUtil.toFile(project.getProjectDirectory()));
        for (Object item : items) {
            ic.add(item);
        }
        for (AndroidModelAware ama : projectAddOnLookup.lookupAll(AndroidModelAware.class)) {
            ama.setAndroidProject(aPrj);
        }
        for (GradleBuildAware gpa : projectAddOnLookup.lookupAll(GradleBuildAware.class)) {
            gpa.setGradleBuild(build);
        }
        levelQuery.setProject(aPrj);
        this.aPrj = aPrj;
        this.gradleBuild = build;
        StatsCollector.getDefault().incrementCounter("gradleproject");
    }

    // TODO should call with prjRoot of root project - not submodule
    private static void ensurePlatformManager(File prjRoot) {
        DalvikPlatformManager dpm = DalvikPlatformManager.getDefault();
        if (dpm.getSdkLocation() == null) {
            // TODO: can add fixed prop eval with ANDROID_HOME env var
            PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(
                    PropertyUtils.propertiesFilePropertyProvider(new File(prjRoot, "local.properties")));
            String sdkDir = evaluator.getProperty(PropertyName.SDK_DIR.getName());
            if (sdkDir != null) {
                dpm.setSdkLocation(sdkDir);
            } else {
                LOG.log(Level.CONFIG, "Android SDK home is not set in Android plugin");
            }
        }
    }

    private void clearAndroidProject() {
        LOG.log(Level.FINE, "removing android support from {0}", project);
        for (Object item : items) {
            ic.remove(item);
        }
        levelQuery.setProject(null);
        // unregister from global path
        openHook.projectClosed();
    }

    private void logLoadedProject(AndroidProject aPrj, GradleBuild build) {
        if (aPrj != null) {
            LOG.log(Level.FINE, "android {0}", aPrj.getName());
            LOG.log(Level.FINE, "target {0}", aPrj.getCompileTarget());
            LOG.log(Level.FINE, "bootCP {0}", aPrj.getBootClasspath());
            LOG.log(Level.FINE, "variants {0}", aPrj.getVariants());
            LOG.log(Level.FINE, "build types {0}", aPrj.getBuildTypes());
            LOG.log(Level.FINE, "flavors {0}", aPrj.getProductFlavors());
            LOG.log(Level.FINE, "default flavor {0}", aPrj.getDefaultConfig());
            LOG.log(Level.FINE, "default flavor source providers {0}, flavor {1}",
                    new Object[]{aPrj.getDefaultConfig().getSourceProvider(), aPrj.getDefaultConfig().getProductFlavor()});
            Variant defaultConfig = AndroidBuildVariants.findDebugVariant(aPrj.getVariants());
            if (defaultConfig != null) {
                printDep(defaultConfig.getMainArtifact().getDependencies());
                AndroidArtifact testArtifact = AndroidBuildVariants.instrumentTestArtifact(defaultConfig.getExtraAndroidArtifacts());
                if (testArtifact != null) {
                    printDep(testArtifact.getDependencies());
                }
                for (ProductFlavorContainer flavor : aPrj.getProductFlavors()) {
                    LOG.log(Level.FINE, "flavor source providers {0}, flavor {1}",
                            new Object[]{flavor.getSourceProvider(), flavor.getProductFlavor()});
//          printDep(en.getValue().getDependencies());
//          printDep(en.getValue().getTestDependencies());
                }
            }
            for (BuildTypeContainer buildType : aPrj.getBuildTypes()) {
                LOG.log(Level.FINE, "build cont {0}", buildType.getBuildType().getName());
                LOG.log(Level.FINE, "build  {0}, {1}, {2}, {3}",
                        new Object[]{buildType.getBuildType(), buildType.getBuildType().getVersionNameSuffix(),
                            buildType.getBuildType().getVersionNameSuffix(), buildType.getSourceProvider()});
//        printDep(en.getValue().getDependency());
            }
        }
    }

    private static void printDep(Dependencies d) {
        if (d == null) {
            LOG.log(Level.FINE, "null extra dependencies");
            return;
        }
        LOG.log(Level.FINE, "dep: jars {0}, libs {1}, projects {2}",
                new Object[]{d.getJavaLibraries(), d.getLibraries(), d.getProjects()});
        for (AndroidLibrary l : d.getLibraries()) {
            printLib(l);
        }
    }

    private static void printLib(AndroidLibrary lib) {
        LOG.log(Level.FINE, "dep: folder {0}, jar {1}, localJars {2}",
                new Object[]{lib.getFolder(), lib.getJarFile(), lib.getLocalJars()});
    }

    @Override
    public Lookup getProjectLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public Lookup getExtensionLookup() {
        return Lookup.EMPTY;
    }

}
