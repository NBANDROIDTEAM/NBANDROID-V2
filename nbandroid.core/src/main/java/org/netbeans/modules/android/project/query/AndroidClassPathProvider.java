/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.project.query;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.SourceProviderContainer;
import com.android.builder.model.Variant;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.nbandroid.netbeans.gradle.api.AndroidClassPath;
import org.nbandroid.netbeans.gradle.config.AndroidBuildVariants;
import org.nbandroid.netbeans.gradle.config.ProductFlavors;
import org.nbandroid.netbeans.gradle.v2.maven.ArtifactData;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatformProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.android.project.build.BuildVariant;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import static org.netbeans.spi.java.classpath.ClassPathImplementation.PROP_RESOURCES;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import static org.netbeans.spi.java.classpath.PathResourceImplementation.PROP_ROOTS;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class AndroidClassPathProvider implements ClassPathProvider, AndroidClassPath, LookupListener, ChangeListener {

    /**
     * refresh from BuildVariant
     *
     * @param e
     */
    private interface Refreshable {

        void refresh();
    }

    private static final Logger LOG = Logger.getLogger(AndroidClassPathProvider.class.getName());
    private Map<URL, ArtifactData> artifactDatas = new HashMap<>();
    private ClassPath source, compile, execute, test, testCompile, boot;
    private final BuildVariant buildConfig;
    private final Set<Refreshable> refreshables = Sets.newHashSet();
    private transient AndroidProject androidProjectModel;
    private final Project project;
    private transient GradleBuild gradleBuildModel;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final Lookup.Result<AndroidProject> lookupResultProjectModel;

    //ARSI: Create virtual java package to override NB java 8 support check
    public static final File VIRTUALJAVA8ROOT_DIR = Places.getCacheSubdirectory("android_virtual_java8");

    static {
        String absolutePath = VIRTUALJAVA8ROOT_DIR.getAbsolutePath();
        absolutePath = absolutePath + File.separator + "java" + File.separator + "util" + File.separator + "stream";
        File streamDir = new File(absolutePath);
        streamDir.mkdirs();
        absolutePath = absolutePath + File.separator + "Streams.class";
        try {
            new File(absolutePath).createNewFile();
        } catch (IOException ex) {
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends AndroidProject> allInstances = lookupResultProjectModel.allInstances();
        if (!allInstances.isEmpty()) {
            gradleBuildModel = project.getLookup().lookup(GradleBuild.class);
            androidProjectModel = allInstances.iterator().next();
            if (buildConfig.getCurrentVariant() != null) {
                update();
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (buildConfig.getCurrentVariant() != null && androidProjectModel != null) {
            update();
        }
    }

    @Override
    public ArtifactData getArtifactData(URL url) {
        return artifactDatas.get(url);
    }

    public Map<URL, ArtifactData> getArtifactDatas() {
        return artifactDatas;
    }

    public AndroidClassPathProvider(BuildVariant buildConfig, Project project) {
        this.buildConfig = Preconditions.checkNotNull(buildConfig);
        this.project = project;
        lookupResultProjectModel = project.getLookup().lookupResult(AndroidProject.class);
        lookupResultProjectModel.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResultProjectModel));
        buildConfig.addChangeListener(WeakListeners.change(this, buildConfig));
        source = createSource();
        test = createTest();
        compile = createCompile();
        boot = createBoot();
        execute = createExecute(compile);
        testCompile = createTestCompile(execute);
        register();
        resultChanged(null);

    }

    private void update() {
        for (Refreshable refreshable : refreshables) {
            refreshable.refresh();
        }
    }

    public @Override
    ClassPath findClassPath(FileObject file, String type) {
        if (source.findOwnerRoot(file) != null) {
            LOG.log(Level.FINER, "cp owns {0}", file);
            return getClassPath(type);
        }
        if (test.findOwnerRoot(file) != null) {
            LOG.log(Level.FINER, "cp owns {0}", file);
            if (ClassPath.SOURCE.equals(type)) {
                return test;
            } else if (ClassPath.COMPILE.equals(type)) {
                return testCompile;
            }
            return getClassPath(type);
        }

        LOG.log(Level.FINER, "cp not owning {0}", file);
        return null;
    }

    @Override
    public ClassPath getClassPath(String type) {
        if (type.equals(ClassPath.SOURCE)) {
            return source;
        } else if (type.equals(ClassPath.BOOT)) {
            return boot;
        } else if (type.equals(ClassPath.COMPILE)) {
            return compile;
        } else if (type.equals(ClassPath.EXECUTE)) {
            return execute;
        } else {
            return null;
        }
    }

    private class BootPathResources extends AndroidPathResources {

        public BootPathResources() {
            buildConfig.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    refresh();
                }
            });
            lookupResultProjectModel.addLookupListener(new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    refresh();
                }
            });
        }

        @Override
        public URL[] getRoots() {
            List<URL> tmp = new ArrayList<>();
            if (androidProjectModel != null && !androidProjectModel.getBootClasspath().isEmpty()) {
                String next = androidProjectModel.getBootClasspath().iterator().next();
                AndroidJavaPlatform findPlatform = AndroidJavaPlatformProvider.findPlatform(next, androidProjectModel.getCompileTarget());
                if (findPlatform != null) {
                    ClassPath bootstrapLibraries = findPlatform.getBootstrapLibraries();
                    FileObject[] roots = bootstrapLibraries.getRoots();
                    for (FileObject root : roots) {
                        tmp.add(root.toURL());
                    }
                }
            }
            return tmp.toArray(new URL[tmp.size()]);
        }

    }

    @Override
    public void register() {
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[]{source});
        //     GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[]{boot});
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[]{compile});
        GlobalPathRegistry.getDefault().register(ClassPath.EXECUTE, new ClassPath[]{execute});
    }

    @Override
    public void unregister() {
        try {
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[]{source});
        } catch (IllegalArgumentException illegalArgumentException) {
        }
        //     GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, new ClassPath[]{boot});
        try {
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, new ClassPath[]{compile});
        } catch (IllegalArgumentException illegalArgumentException) {
        }
        try {
            GlobalPathRegistry.getDefault().unregister(ClassPath.EXECUTE, new ClassPath[]{execute});
        } catch (IllegalArgumentException illegalArgumentException) {
        }
    }

    public ClassPath getSourcePath() {
        return source;
    }

    public ClassPath getCompilePath() {
        return compile;
    }

    public ClassPath getBootPath() {
        return getClassPath(ClassPath.BOOT);
    }

    private ClassPath createSource() {
        final GradlePathImpl srcPathImpl = new GradlePathImpl(new SourceRootsSupplier());
        refreshables.add(srcPathImpl);
        return ClassPathFactory.createClassPath(srcPathImpl);
    }

    private ClassPath createTest() {
        final GradlePathImpl srcPathImpl = new GradlePathImpl(new TestRootsSupplier());
        refreshables.add(srcPathImpl);
        return ClassPathFactory.createClassPath(srcPathImpl);
    }

    private ClassPath createTestCompile(ClassPath execute) {
        final GradlePathImpl buildPathImpl = new GradlePathImpl(new TestCompileRootsSupplier());
        refreshables.add(buildPathImpl);
        return ClassPathSupport.createProxyClassPath(
                ClassPathFactory.createClassPath(buildPathImpl),
                execute);
    }

    private class CompilePathResources extends AndroidPathResources {

        public CompilePathResources() {
            buildConfig.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    refresh();
                }
            });
            lookupResultProjectModel.addLookupListener(new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    refresh();
                }
            });
        }

        @Override
        public URL[] getRoots() {
            List<URL> roots = new ArrayList<>();
            Map<URL, ArtifactData> tmpArtifactDatas = new HashMap<>();
            if (androidProjectModel != null) {
                Variant variant = buildConfig.getCurrentVariant();
                if (variant != null) {
                    Dependencies dependencies = variant.getMainArtifact().getDependencies();
                    for (AndroidLibrary lib : dependencies.getLibraries()) {
                        addAndroidLibraryDependencies(roots, tmpArtifactDatas, lib);
                    }
                    for (JavaLibrary lib : dependencies.getJavaLibraries()) {
                        addJavaLibraryDependencies(roots, tmpArtifactDatas, lib);
                    }
                    for (String prjPath : dependencies.getProjects()) {
                        if (gradleBuildModel == null) {
                            LOG.log(Level.INFO, "ignored project dependency {0}", prjPath);
                            continue;
                        }
                        BasicGradleProject depProject = findProjectByName(gradleBuildModel.getRootProject(), prjPath);
                        if (depProject == null) {
                            LOG.log(Level.INFO, "cannot find dependency project {0}", prjPath);
                            continue;
                        }
                        FileObject depPrjDir = FileUtil.toFileObject(depProject.getProjectDirectory());
                        try {
                            Project dependencyProject = ProjectManager.getDefault().findProject(depPrjDir);
                            if (dependencyProject == null) {
                                LOG.log(Level.INFO, "cannot find dependency project {0}", prjPath);
                                continue;
                            }
                            SourceGroup[] sourceGroups = ProjectUtils.getSources(dependencyProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                            for (SourceGroup sg : sourceGroups) {
                                BinaryForSourceQuery.Result binaryRoot
                                        = BinaryForSourceQuery.findBinaryRoots(FileUtil.urlForArchiveOrDir(FileUtil.toFile(sg.getRootFolder())));
                                for (URL compiledRoot : binaryRoot.getRoots()) {
                                    URL safeRoot = compiledRoot;
                                    if ("file".equals(compiledRoot.getProtocol())) {
                                        try {
                                            safeRoot = FileUtil.urlForArchiveOrDir(Utilities.toFile(compiledRoot.toURI()));
                                        } catch (Exception ex) {
                                            LOG.log(Level.INFO, "project dependency processing failed for compile dependency " + prjPath + " root " + compiledRoot, ex);
                                        }
                                    }
                                    if (!roots.contains(safeRoot)) {
                                        roots.add(safeRoot);
                                    }
                                }
                            }

                        } catch (IOException | IllegalArgumentException ex) {
                            LOG.log(Level.INFO, "cannot find classpath for project dependency {0}", prjPath);
                        }

                    }
                }
            }
            LOG.log(Level.FINE, "compile CP roots: {0}", roots);
            artifactDatas = tmpArtifactDatas;
            return roots.toArray(new URL[roots.size()]);
        }

        public void addAndroidLibraryDependencies(List<URL> roots, Map<URL, ArtifactData> libs, AndroidLibrary lib) {
            String name = lib.getName();
            URL url = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(lib.getJarFile()));
            if (!roots.contains(url)) {
                roots.add(url);
                libs.put(url, new ArtifactData(lib, project));
            }
            List<? extends AndroidLibrary> libraryDependencies = lib.getLibraryDependencies();
            for (AndroidLibrary libraryDependencie : libraryDependencies) {
                addAndroidLibraryDependencies(roots, libs, libraryDependencie);
            }
            Iterator<File> localJars = lib.getLocalJars().iterator();
            if (localJars != null) {
                while (localJars.hasNext()) {
                    File next = localJars.next();
                    url = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(next));
                    if (!roots.contains(url)) {
                        roots.add(url);
                        libs.put(url, new ArtifactData(lib, project));
                    }

                }
            }

        }

        private void addJavaLibraryDependencies(List<URL> roots, Map<URL, ArtifactData> libs, JavaLibrary lib) {
            URL root = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(lib.getJarFile()));
            if (!roots.contains(root)) {
                roots.add(root);
                libs.put(root, new ArtifactData(lib, project));
            }
            for (JavaLibrary childLib : lib.getDependencies()) {
                addJavaLibraryDependencies(roots, libs, childLib);
            }
        }
    }

    @Nullable
    private BasicGradleProject findProjectByName(BasicGradleProject gradleProject, String prjPath) {
        if (gradleProject == null) {
            return null;
        }
        if (gradleProject.getPath().equals(prjPath)) {
            return gradleProject;
        }
        for (BasicGradleProject childProject : gradleProject.getChildren()) {
            BasicGradleProject result = findProjectByName(childProject, prjPath);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private abstract class AndroidPathResources extends PathResourceBase implements Refreshable {

        public AndroidPathResources() {
        }

        @Override
        public final ClassPathImplementation getContent() {
            // should not be called
            return null;
        }

        @Override
        public final void refresh() {
            // TODO only fire change when set of roots is really different.
            firePropertyChange(PROP_ROOTS, null, null);
        }
    }

    private ClassPath createCompile() {
        List<PathResourceBase> pathResources = Lists.newArrayList();
        CompilePathResources compilePathResources = new CompilePathResources();
        refreshables.add(compilePathResources);
        pathResources.add(compilePathResources);
        return ClassPathSupport.createClassPath(pathResources);
    }

    private ClassPath createBoot() {
        List<PathResourceBase> pathResources = Lists.newArrayList();
        BootPathResources bootPathResources = new BootPathResources();
        refreshables.add(bootPathResources);
        pathResources.add(bootPathResources);
        return ClassPathSupport.createClassPath(pathResources);
    }

    private ClassPath createExecute(ClassPath compile) {
        if (androidProjectModel != null) {
            Variant variant = buildConfig.getCurrentVariant();
            if (variant != null) {
                return ClassPathSupport.createProxyClassPath(compile, ClassPathSupport.createClassPath(
                        FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(variant.getMainArtifact().getClassesFolder()))));
            }
        }
        return compile;
    }

    private class SourceRootsSupplier implements Supplier<Iterable<? extends File>> {

        @Override
        public Iterable<? extends File> get() {
            Collection<File> javaDirs = androidProjectModel != null
                    ? androidProjectModel.getDefaultConfig().getSourceProvider().getJavaDirectories()
                    : Collections.<File>emptySet();
            BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
            Collection<File> typeJavaDirs = buildTypeContainer != null
                    ? buildTypeContainer.getSourceProvider().getJavaDirectories()
                    : Collections.<File>emptySet();
            Variant variant = buildConfig.getCurrentVariant();
            Iterable<File> variantJavaDirs = variant != null
                    ? Iterables.concat(
                            Iterables.transform(
                                    variant.getProductFlavors(),
                                    new Function<String, Collection<File>>() {
                                @Override
                                public Collection<File> apply(String f) {
                                    if (androidProjectModel == null) {
                                        return Collections.<File>emptySet();
                                    }
                                    final ProductFlavorContainer flavor = ProductFlavors.findFlavorByName(androidProjectModel.getProductFlavors(), f);
                                    if (flavor == null) {
                                        return Collections.<File>emptySet();
                                    }
                                    return flavor.getSourceProvider().getJavaDirectories();
                                }
                            }))
                    : Collections.<File>emptySet();
            Collection<File> generatedJavaDirs = variant != null
                    ? variant.getMainArtifact().getGeneratedSourceFolders()
                    : Collections.<File>emptyList();
            return Iterables.concat(
                    javaDirs,
                    typeJavaDirs,
                    variantJavaDirs,
                    generatedJavaDirs);
        }

    }

    // TODO add test for project with dependencies { instrumentTestCompile 'org.mockito:mockito-core:1.9.5' }
    // and check it appears on classpath
    private class TestRootsSupplier implements Supplier<Iterable<? extends File>> {

        @Override
        public Iterable<? extends File> get() {
            SourceProviderContainer spc = androidProjectModel != null
                    ? ProductFlavors.getSourceProviderContainer(androidProjectModel.getDefaultConfig(), AndroidProject.ARTIFACT_ANDROID_TEST)
                    : null;
            Collection<File> javaDirs = spc != null
                    ? spc.getSourceProvider().getJavaDirectories()
                    : Collections.<File>emptySet();
            Variant variant = buildConfig.getCurrentVariant();
            Iterable<File> variantJavaDirs = variant != null
                    ? Iterables.concat(
                            Iterables.transform(
                                    variant.getProductFlavors(),
                                    new Function<String, Collection<File>>() {
                                @Override
                                public Collection<File> apply(String f) {
                                    if (androidProjectModel == null) {
                                        return Collections.<File>emptySet();
                                    }
                                    final ProductFlavorContainer flavor = ProductFlavors.findFlavorByName(androidProjectModel.getProductFlavors(), f);
                                    if (flavor == null) {
                                        return Collections.<File>emptySet();
                                    }
                                    SourceProviderContainer flavorSPC = ProductFlavors.getSourceProviderContainer(
                                            flavor, AndroidProject.ARTIFACT_ANDROID_TEST);
                                    if (flavorSPC == null) {
                                        return Collections.<File>emptySet();
                                    }
                                    return flavorSPC.getSourceProvider().getJavaDirectories();
                                }
                            }))
                    : Collections.<File>emptySet();
            AndroidArtifact testArtifact = variant != null
                    ? AndroidBuildVariants.instrumentTestArtifact(variant.getExtraAndroidArtifacts())
                    : null;
            Collection<File> generatedJavaDirs = testArtifact != null
                    ? testArtifact.getGeneratedSourceFolders()
                    : Collections.<File>emptyList();
            return Iterables.concat(
                    javaDirs,
                    variantJavaDirs,
                    generatedJavaDirs);
        }
    }

    private class TestCompileRootsSupplier implements Supplier<Iterable<? extends File>> {

        @Override
        public Iterable<? extends File> get() {
            Variant variant = buildConfig.getCurrentVariant();
            if (variant != null) {
                AndroidArtifact testArtifact = AndroidBuildVariants.instrumentTestArtifact(variant.getExtraAndroidArtifacts());
                Iterable<File> testCompileCPEntries
                        = Collections.<File>singleton(variant.getMainArtifact().getClassesFolder());
                if (testArtifact != null) {
                    List<File> javaLibs = new ArrayList<>();
                    for (JavaLibrary lib : testArtifact.getDependencies().getJavaLibraries()) {
                        collectJavaLibraries(javaLibs, lib);
                    }
                    testCompileCPEntries = Iterables.concat(
                            testCompileCPEntries,
                            Iterables.transform(
                                    testArtifact.getDependencies().getLibraries(),
                                    new Function<AndroidLibrary, File>() {
                                @Override
                                public File apply(AndroidLibrary f) {
                                    return f.getJarFile();
                                }
                            }),
                            javaLibs);
                }
                return testCompileCPEntries;
            }
            return Collections.<File>emptyList();
        }

        private void collectJavaLibraries(Collection<File> libs, JavaLibrary library) {
            libs.add(library.getJarFile());
            for (JavaLibrary childLib : library.getDependencies()) {
                collectJavaLibraries(libs, childLib);
            }
        }
    }

    /**
     * Source path of Gradle Android project.
     */
    private final class GradlePathImpl implements ClassPathImplementation, Refreshable {

        private final PropertyChangeSupport listeners;
        private final Object cacheLock = new Object();

        private final FSListener fsListener = new FSListener();   //Hack: The FileSystems does not deliver all events even for
        //addRecusriveListener when parent folder above the listening point
        //is deleted. So ve return just exiting cp entries and listen on all.
        private final Supplier<Iterable<? extends File>> rootsSupplier;

        @GuardedBy("cacheLock")
        private volatile List<PathResourceImplementation> cache;

        GradlePathImpl(Supplier<Iterable<? extends File>> rootsSupplier) {
            this.listeners = new PropertyChangeSupport(this);
            this.rootsSupplier = Preconditions.checkNotNull(rootsSupplier);
            buildConfig.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    refresh();
                }
            });
        }

        @Override
        @Nonnull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = cache;
            if (res == null) {
                final Iterable<? extends File> allRoots = rootsSupplier.get();
                res = Lists.newArrayList(
                        Iterables.transform(
                                Iterables.filter(
                                        allRoots,
                                        new Predicate<File>() {
                                    @Override
                                    public boolean apply(File t) {
                                        return t.exists();
                                    }
                                }),
                                new Function<File, PathResourceImplementation>() {
                            @Override
                            public PathResourceImplementation apply(File f) {
                                return ClassPathSupport.createResource(
                                        FileUtil.urlForArchiveOrDir(
                                                FileUtil.normalizeFile(f)));
                            }
                        }));
                synchronized (cacheLock) {
                    fsListener.updateState(allRoots);
                    cache = res;
                }
                LOG.log(Level.FINE, "Source roots: {0}", res);  //NOI18N
            }
            return Collections.unmodifiableList(res);
        }

        @Override
        public void addPropertyChangeListener(@Nonnull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);
            listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@Nonnull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);
            listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void refresh() {
            LOG.log(Level.FINER, "Refresh source roots for: {0}", androidProjectModel);
            cache = null;
            listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }

        private class FSListener extends FileChangeAdapter {

            @GuardedBy("cacheLock")
            private final Map<File, FileChangeListener> listeningOn = Maps.newHashMap();

            void updateState(@Nonnull final Iterable<? extends File> files) {
                assert Thread.holdsLock(cacheLock);
                final Set<File> added = Sets.newHashSet(files);
                final Map<File, FileChangeListener> removed = Maps.newHashMap(listeningOn);
                removed.keySet().removeAll(added);
                added.removeAll(listeningOn.keySet());
                for (Map.Entry<File, FileChangeListener> rf : removed.entrySet()) {
                    FileUtil.removeFileChangeListener(rf.getValue(), rf.getKey());
                    listeningOn.remove(rf.getKey());
                }
                for (File af : added) {
                    final FileChangeListener wfcl = FileUtil.weakFileChangeListener(this, FileUtil.class);
                    FileUtil.addFileChangeListener(wfcl, af);
                    listeningOn.put(af, wfcl);
                }
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                refresh();
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                refresh();
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                refresh();
            }
        }
    }
}
