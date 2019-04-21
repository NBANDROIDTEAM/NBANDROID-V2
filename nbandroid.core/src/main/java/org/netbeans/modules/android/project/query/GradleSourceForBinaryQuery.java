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

import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.Variant;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.config.ProductFlavors;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.build.BuildVariant;
import org.netbeans.modules.android.project.sources.generated.RTools;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * Source lookup for classes found in exploded-bundles directory containing
 * AndroidLibrary dependencies.
 *
 * @author radim
 * @author arsi
 */
public class GradleSourceForBinaryQuery implements SourceForBinaryQueryImplementation2, LookupListener {

    private static final Logger LOG = Logger.getLogger(GradleSourceForBinaryQuery.class.getName());

    private final BuildVariant buildConfig;
    private AndroidProject androidProjectModel;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final Lookup.Result<AndroidProject> lookupResult;

    public GradleSourceForBinaryQuery(Project project, BuildVariant buildConfig) {
        this.buildConfig = buildConfig;
        lookupResult = project.getLookup().lookupResult(AndroidProject.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResult));
        resultChanged(null);
    }

    @Override
    public Result findSourceRoots2(final URL binaryRoot) {
        return new GradleSourceResult(binaryRoot);
    }

    private Iterable<? extends File> sourceRootsForVariant(Variant variant) {
        Collection<File> javaDirs = androidProjectModel != null
                ? androidProjectModel.getDefaultConfig().getSourceProvider().getJavaDirectories()
                : Collections.<File>emptySet();
        BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
        Collection<File> typeJavaDirs = buildTypeContainer != null
                ? buildTypeContainer.getSourceProvider().getJavaDirectories()
                : Collections.<File>emptySet();
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
                                final ProductFlavorContainer flavor
                                        = ProductFlavors.findFlavorByName(androidProjectModel.getProductFlavors(), f);
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
        if (variant!=null) {
            RTools.PluginVersionResult result = RTools.handlePluginVersion(androidProjectModel, variant, null);
            if(result!=null){
                generatedJavaDirs.add(FileUtil.toFile(result.getSrc()));
            }
        }
        return Iterables.concat(
                javaDirs,
                typeJavaDirs,
                variantJavaDirs,
                generatedJavaDirs);
    }

    private Result findAarLibraryRoots(final URL binaryRoot) {
        // FileUtil.getArchiveFile(binaryRoot);
        AndroidLibrary aLib = null;
        Variant variant = buildConfig.getCurrentVariant();
        if (variant != null) {
            aLib = Iterables.find(
                    variant.getMainArtifact().getDependencies().getLibraries(),
                    new Predicate<AndroidLibrary>() {

                @Override
                public boolean apply(AndroidLibrary lib) {
                    URL libUrl = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(lib.getJarFile()));
                    return binaryRoot.equals(libUrl);
                }
            },
                    null);
        }
        if (aLib == null) {
            return null;
        }
//    if (aLib instanceof AndroidLibraryProject) {
//      AndroidLibraryProject libPrj = (AndroidLibraryProject) aLib;
//      LOG.log(Level.FINE, "Found binary from AndroidLibrary {0}", libPrj.getProjectPath());
//    } else {
        LOG.log(Level.FINE, "Found unknown binary from AndroidLibrary {0}", aLib.getJarFile());
//    }
        return null;
    }

    @Override
    public GradleSourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends AndroidProject> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            androidProjectModel = allInstances.iterator().next();
        } else {
            androidProjectModel = null;
        }
        cs.fireChange();
    }

    private class GradleSourceResult implements SourceForBinaryQueryImplementation2.Result {

        private final URL binaryRoot;

        public GradleSourceResult(final URL binaryRoot) {
            this.binaryRoot = binaryRoot;
        }

        private List<FileObject> update() {
            Result r = findAarLibraryRoots(binaryRoot);
            if (r != null) {
                return Collections.EMPTY_LIST;
            }
            if (androidProjectModel == null) {
                return Collections.EMPTY_LIST;
            }
            final File binRootDir = FileUtil.archiveOrDirForURL(binaryRoot);
            if (binRootDir == null) {
                return null;
            }

            Variant variant = Iterables.find(
                    androidProjectModel.getVariants(),
                    new Predicate<Variant>() {
                @Override
                public boolean apply(Variant input) {
                    return binRootDir.equals(input.getMainArtifact().getClassesFolder());
                }
            },
                    null);
            if (variant != null) {
                Iterable<FileObject> srcRoots = Iterables.filter(
                        Iterables.transform(
                                sourceRootsForVariant(variant),
                                new Function<File, FileObject>() {
                            @Override
                            public FileObject apply(File f) {
                                return FileUtil.toFileObject(f);
                            }
                        }),
                        Predicates.notNull());
                return Lists.newArrayList(srcRoots);
            }
            return Collections.EMPTY_LIST;
        }

        public @Override
        FileObject[] getRoots() {
            List<FileObject> roots = Lists.newArrayList(update());
            LOG.log(Level.FINE, "return sources for binary in root {0}: {1}", new Object[]{androidProjectModel, roots});
            return roots.toArray(new FileObject[roots.size()]);
        }

        public @Override
        void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public @Override
        void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        @Override
        public boolean preferSources() {
            return true;
        }

    }
}
