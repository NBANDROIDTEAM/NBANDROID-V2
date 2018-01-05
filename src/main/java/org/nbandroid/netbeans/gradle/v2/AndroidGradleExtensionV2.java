/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.nbandroid.netbeans.ext.navigation.ProjectResourceLocator;
import org.nbandroid.netbeans.gradle.*;
import org.nbandroid.netbeans.gradle.api.TestOutputConsumer;
import org.nbandroid.netbeans.gradle.config.AndroidBuildVariants;
import org.nbandroid.netbeans.gradle.config.AndroidTestRunConfiguration;
import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.nbandroid.netbeans.gradle.core.sdk.StatsCollector;
import org.nbandroid.netbeans.gradle.launch.GradleDebugInfo;
import org.nbandroid.netbeans.gradle.launch.Launches;
import org.nbandroid.netbeans.gradle.query.AndroidTaskVariableQuery;
import org.nbandroid.netbeans.gradle.query.AutoAndroidGradleJavadocForBinaryQuery;
import org.nbandroid.netbeans.gradle.query.AutoAndroidGradleSourceForBinaryQuery;
import org.nbandroid.netbeans.gradle.query.BuiltInCommands;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.nbandroid.netbeans.gradle.query.GradleAndroidManifest;
import org.nbandroid.netbeans.gradle.query.GradleAndroidSources;
import org.nbandroid.netbeans.gradle.query.GradlePlatformResolver;
import org.nbandroid.netbeans.gradle.query.GradleSourceForBinaryQuery;
import org.nbandroid.netbeans.gradle.query.SourceLevelQueryImpl2;
import org.nbandroid.netbeans.gradle.spi.AndroidProjectDirectory;
import org.nbandroid.netbeans.gradle.spi.ProjectRefResolver;
import org.nbandroid.netbeans.gradle.testrunner.TestOutputConsumerLookupProvider;
import org.nbandroid.netbeans.gradle.ui.AndroidTestsProvider;
import org.nbandroid.netbeans.gradle.ui.BuildCustomizerProvider;
import static org.nbandroid.netbeans.gradle.v2.AndroidExtensionDef.COMMENT;
import static org.nbandroid.netbeans.gradle.v2.AndroidExtensionDef.SDK_DIR;
import org.nbandroid.netbeans.gradle.v2.gradle.GradleAndroidRepositoriesProvider;
import org.nbandroid.netbeans.gradle.v2.project.AndroidSdkConfigProvider;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.api.project.Project;
import org.netbeans.gradle.project.NbGradleProject;
import org.netbeans.gradle.project.api.entry.GradleProjectExtension2;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author radim
 */
public class AndroidGradleExtensionV2 implements GradleProjectExtension2<SerializableLookup>, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(AndroidGradleExtensionV2.class.getName());

    private final Project project;
    private final InstanceContent ic;
    private final Lookup projectAddOnLookup;
    private final List<Object> items = Lists.newArrayList();
    private final GradleProjectOpenedHook openHook;
    private final SourceLevelQueryImpl2 levelQuery;
    private GradleAndroidClassPathProvider androidClassPathProvider = null;
    public static final String PROPERTY_CHANGE_SDK = "PROPERTY_CHANGE_SDK";
    // testing support
    @VisibleForTesting
    public final CountDownLatch loadedSignal = new CountDownLatch(1);
    @VisibleForTesting
    volatile AndroidProject aPrj = null;
    @VisibleForTesting
    volatile GradleBuild gradleBuild = null;
    private AndroidSdk sdk;
    private final FileObject localProperties;
    private final BuildVariant buildCfg;

    public AndroidGradleExtensionV2(Project project, AndroidSdk sdk, FileObject localProperties) {
        this.project = Preconditions.checkNotNull(project);
        this.sdk = sdk;
        levelQuery = new SourceLevelQueryImpl2(project);
        this.localProperties = localProperties;
        ic = new InstanceContent();
        projectAddOnLookup = new AbstractLookup(ic);
        final AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        buildCfg = new BuildVariant(props);
        AndroidTestRunConfiguration testCfg = new AndroidTestRunConfiguration(props);
        if (sdk != null) {
            //add SDK  to lookup
            items.add(sdk);
        }
        if (sdk == null || sdk.isDefaultSdk()) {
            AndroidSdkProvider.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, AndroidSdkProvider.PROP_DEFAULT_SDK, AndroidSdkProvider.getDefault()));
        }
        items.add(buildCfg);
        items.add(testCfg);
        items.add(levelQuery);
        items.add(new AndroidSdkConfigProvider());
        items.add(new GradleAndroidRepositoriesProvider(project));
        items.add(new GradlePlatformResolver());
        items.add(new GradleAndroidSources(project, buildCfg));
        items.add(new GradleAndroidManifest(project, buildCfg));

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
        items.add(new AndroidProjectContextActions());
        ic.add(this);
        openHook = new GradleProjectOpenedHook(project);
        ic.add(openHook);
    }

    public List<Object> getItems() {
        return items;
    }

    public AndroidSdk getSdk() {
        return sdk;
    }

    public FileObject getLocalProperties() {
        return localProperties;
    }

    @Override
    public Lookup getPermanentProjectLookup() {
        LOG.log(Level.FINE, "lookup requested");
        return projectAddOnLookup;
    }

    @Override
    public void activateExtension(SerializableLookup parsedModel) {
        if (sdk != null && sdk.isValid()) {
            modelsLoaded(parsedModel.lookup);
        }
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
            if (androidClassPathProvider != null) {
                ic.remove(androidClassPathProvider);
            }
            androidClassPathProvider = new GradleAndroidClassPathProvider(buildCfg, project, aProject, build);
            ic.add(androidClassPathProvider);
            androidClassPathProvider.register();
            AutoAndroidGradleSourceForBinaryQuery sourceForBinaryQuery = Lookup.getDefault().lookup(AutoAndroidGradleSourceForBinaryQuery.class);
            AutoAndroidGradleJavadocForBinaryQuery javadocForBinaryQuery = Lookup.getDefault().lookup(AutoAndroidGradleJavadocForBinaryQuery.class);
            if (aProject != null) {
                updateAndroidProject(aProject, build);
                sourceForBinaryQuery.addClassPathProvider(androidClassPathProvider);
                javadocForBinaryQuery.addClassPathProvider(androidClassPathProvider);
            } else {
                clearAndroidProject();
                sourceForBinaryQuery.removeClassPathProvider(androidClassPathProvider);
                javadocForBinaryQuery.removeClassPathProvider(androidClassPathProvider);
            }
        } finally {
            loadedSignal.countDown();
        }
    }

    private void updateAndroidProject(AndroidProject aPrj, GradleBuild build) {
        ic.add(aPrj);
        ic.add(build);
        if (LOG.isLoggable(Level.FINE)) {
            logLoadedProject(aPrj, build);
        }
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AndroidSdkProvider.PROP_DEFAULT_SDK.equals(evt.getPropertyName())) {
            if (localProperties != null) {
                AndroidSdk defaultSdk = AndroidSdkProvider.getDefaultSdk();
                if (defaultSdk != null) {
                    sdk = defaultSdk;
                    storeLocalProperties(defaultSdk, localProperties);
                    ((NbGradleProject) project).reloadProject();
                }
            }
        } else if (PROPERTY_CHANGE_SDK.equals(evt.getPropertyName())) {
            if (localProperties != null && (evt.getNewValue() instanceof AndroidSdk)) {
                sdk = (AndroidSdk) evt.getNewValue();
                items.replaceAll(new UnaryOperator<Object>() {
                    @Override
                    public Object apply(Object t) {
                        if (t instanceof AndroidSdk) {
                            ic.remove(t);
                            ic.add(sdk);
                            return sdk;
                        }
                        return t;
                    }
                });
                storeLocalProperties((AndroidSdk) evt.getNewValue(), localProperties);
                ((NbGradleProject) project).reloadProject();
            }
        }
    }

    public void storeLocalProperties(AndroidSdk defaultPlatform, FileObject localProperties) {
        FileOutputStream fo = null;
        try {
            Properties properties = new Properties();
            //have default SDK write to properties
            properties.setProperty(SDK_DIR, defaultPlatform.getInstallFolder().getPath());
            fo = new FileOutputStream(FileUtil.toFile(localProperties));
            try {
                properties.store(fo, COMMENT.replace("#DATE", new Date().toString()));
            } finally {
                try {
                    fo.close();
                } catch (IOException iOException) {
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                fo.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
