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

package org.netbeans.modules.android.project.sources;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.SourceProviderContainer;
import com.android.builder.model.Variant;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.config.AndroidBuildVariants;
import org.nbandroid.netbeans.gradle.config.ProductFlavors;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.android.project.build.BuildVariant;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class AndroidSources implements Sources, ChangeListener, LookupListener {

    private final Project project;
    private AndroidProject androidProjectModel;
    private final BuildVariant buildConfig;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final Lookup.Result<AndroidProject> lookupResult;

    public AndroidSources(Project project, BuildVariant buildConfig) {
        this.project = project;
        this.buildConfig = buildConfig;
        buildConfig.addChangeListener(WeakListeners.change(this, buildConfig));
        lookupResult = project.getLookup().lookupResult(AndroidProject.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResult));
        resultChanged(null);
    }

    @Override
    public SourceGroup[] getSourceGroups(String type) {
        if (type.equals(Sources.TYPE_GENERIC)) {
            ProjectInformation info = ProjectUtils.getInformation(project);
            return new SourceGroup[]{GenericSources.group(project, project.getProjectDirectory(), info.getName(), info.getDisplayName(), null, null)};
        } else if (type.equals(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            return groupSrcMainJava();
        } else if (type.equals(AndroidConstants.ANDROID_MANIFEST_XML)) {
            return groupManifest();
        } else if (type.equals(AndroidConstants.SOURCES_TYPE_INSTRUMENT_TEST_JAVA)) {
            return groupSrcInstrumentTestJava();
        } else if (type.equals(AndroidConstants.SOURCES_TYPE_GENERATED_JAVA)) {
            // XXX listen to this being created/deleted
            List<SourceGroup> grps = new ArrayList<SourceGroup>();
            FileObject prjDir = project.getProjectDirectory();
            Variant variant = buildConfig.getCurrentVariant();
            if (variant != null) {
                for (File srcDir : variant.getMainArtifact().getGeneratedSourceFolders()) {
                    if (!srcDir.exists()) {
                        continue;
                    }
                    FileObject src = FileUtil.toFileObject(srcDir);
                    String srcName = FileUtil.isParentOf(prjDir, src)
                            ? FileUtil.getRelativePath(prjDir, src)
                            : srcDir.getAbsolutePath();
                    grps.add(new AnySourceGroup(project, src, srcName, "Generated Source Packages " + srcName, null, null));
                }
            }
            return grps.toArray(new SourceGroup[grps.size()]);
        } else if (type.equals(AndroidConstants.SOURCES_TYPE_INSTRUMENT_TEST_GENERATED_JAVA)) {
            // XXX listen to this being created/deleted
            List<SourceGroup> grps = new ArrayList<SourceGroup>();
            FileObject prjDir = project.getProjectDirectory();
            Variant variant = buildConfig.getCurrentVariant();
            AndroidArtifact testArtifact = AndroidBuildVariants.instrumentTestArtifact(variant.getExtraAndroidArtifacts());
            if (testArtifact != null) {
                for (File srcDir : testArtifact.getGeneratedSourceFolders()) {
                    if (!srcDir.exists()) {
                        continue;
                    }
                    FileObject src = FileUtil.toFileObject(srcDir);
                    String srcName = FileUtil.isParentOf(prjDir, src)
                            ? FileUtil.getRelativePath(prjDir, src)
                            : srcDir.getAbsolutePath();
                    grps.add(new AnySourceGroup(project, src, srcName, "Generated Instrument Test Packages " + srcName, null, null));
                }
            }
            return grps.toArray(new SourceGroup[grps.size()]);
        } else if (type.equals(JavaProjectConstants.SOURCES_TYPE_RESOURCES)) {
            return groupSrcMainResources();
        } else if (type.equals(AndroidConstants.SOURCES_TYPE_GENERATED_RESOURCES)) {
            // XXX listen to this being created/deleted
            List<SourceGroup> grps = new ArrayList<SourceGroup>();
            FileObject prjDir = project.getProjectDirectory();
            Variant variant = buildConfig.getCurrentVariant();
            if (variant != null) {
                for (File srcDir : variant.getMainArtifact().getGeneratedResourceFolders()) {
                    if (!srcDir.exists()) {
                        continue;
                    }
                    FileObject src = FileUtil.toFileObject(srcDir);
                    String srcName = FileUtil.isParentOf(prjDir, src)
                            ? FileUtil.getRelativePath(prjDir, src)
                            : srcDir.getAbsolutePath();
                    grps.add(new AnySourceGroup(project, src, srcName, "Generated Other Sources " + srcName, null, null));
                }
            }
            return grps.toArray(new SourceGroup[grps.size()]);
        } else if (type.equals(AndroidConstants.SOURCES_TYPE_ANDROID_RES)) {
            return groupSrcMainRes();
        } else {
            // XXX consider SOURCES_TYPE_RESOURCES -> res
            return new SourceGroup[0];
        }
    }

    private SourceGroup[] groupSrcMainJava() {
        // XXX listen to this being created/deleted
        List<SourceGroup> grps = new ArrayList<SourceGroup>();
        FileObject prjDir = project.getProjectDirectory();
        if (androidProjectModel != null) {
            for (File srcDir : androidProjectModel.getDefaultConfig().getSourceProvider().getJavaDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "Source Packages " + srcName, null, null));
            }
            Variant variant = buildConfig.getCurrentVariant();
            if (variant != null) {
                for (String f : variant.getProductFlavors()) {
                    final ProductFlavorContainer flavor = ProductFlavors.findFlavorByName(androidProjectModel.getProductFlavors(), f);
                    if (flavor != null) {
                        for (File srcDir : flavor.getSourceProvider().getJavaDirectories()) {
                            if (!srcDir.exists()) {
                                continue;
                            }
                            FileObject src = FileUtil.toFileObject(srcDir);
                            String srcName = FileUtil.isParentOf(prjDir, src)
                                    ? FileUtil.getRelativePath(prjDir, src)
                                    : srcDir.getAbsolutePath();
                            grps.add(GenericSources.group(project, src, srcName, "Source Packages Flavor " + f, null, null));
                        }
                    }
                }
            }
        }
        BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
        if (buildTypeContainer != null) {
            for (File srcDir : buildTypeContainer.getSourceProvider().getJavaDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "Source Packages " + srcName, null, null));
            }
        }
        return grps.toArray(new SourceGroup[grps.size()]);
    }

    private SourceGroup[] groupSrcInstrumentTestJava() {
        // XXX listen to this being created/deleted
        List<SourceGroup> grps = new ArrayList<SourceGroup>();
        FileObject prjDir = project.getProjectDirectory();
        if (androidProjectModel != null) {
            SourceProviderContainer spc = ProductFlavors.getSourceProviderContainer(
                    androidProjectModel.getDefaultConfig(), AndroidProject.ARTIFACT_ANDROID_TEST);
            if (spc != null) {
                for (File srcDir : spc.getSourceProvider().getJavaDirectories()) {
                    if (!srcDir.exists()) {
                        continue;
                    }
                    FileObject src = FileUtil.toFileObject(srcDir);
                    String srcName = FileUtil.isParentOf(prjDir, src)
                            ? FileUtil.getRelativePath(prjDir, src)
                            : srcDir.getAbsolutePath();
                    grps.add(GenericSources.group(project, src, srcName, "Instrument Test Packages " + srcName, null, null));
                }
            }
            Variant variant = buildConfig.getCurrentVariant();
            if (variant != null) {
                for (String f : variant.getProductFlavors()) {
                    final ProductFlavorContainer flavor = ProductFlavors.findFlavorByName(androidProjectModel.getProductFlavors(), f);
                    if (flavor != null) {
                        SourceProviderContainer flavorSPC = ProductFlavors.getSourceProviderContainer(
                                flavor, AndroidProject.ARTIFACT_ANDROID_TEST);
                        if (flavorSPC != null) {
                            for (File srcDir : flavorSPC.getSourceProvider().getJavaDirectories()) {
                                if (!srcDir.exists()) {
                                    continue;
                                }
                                FileObject src = FileUtil.toFileObject(srcDir);
                                String srcName = FileUtil.isParentOf(prjDir, src)
                                        ? FileUtil.getRelativePath(prjDir, src)
                                        : srcDir.getAbsolutePath();
                                grps.add(GenericSources.group(project, src, srcName, "Instrument Test Packages Flavor " + f, null, null));
                            }
                        }
                    }
                }
            }
        }
        return grps.toArray(new SourceGroup[grps.size()]);
    }

    private SourceGroup[] groupSrcMainResources() {
        // XXX listen to this being created/deleted
        List<SourceGroup> grps = new ArrayList<SourceGroup>();
        FileObject prjDir = project.getProjectDirectory();
        if (androidProjectModel != null) {
            for (File srcDir : androidProjectModel.getDefaultConfig().getSourceProvider().getResourcesDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "Other Sources " + srcName, null, null));
            }
        }
        BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
        if (buildTypeContainer != null) {
            for (File srcDir : buildTypeContainer.getSourceProvider().getResourcesDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "Other Sources " + srcName, null, null));
            }
        }
        return grps.toArray(new SourceGroup[grps.size()]);
    }

    private SourceGroup[] groupManifest() {
        List<SourceGroup> grps = new ArrayList<>();
        File manifestFile = androidProjectModel.getDefaultConfig().getSourceProvider().getManifestFile();
        if (manifestFile != null) {
            File rootFolder = manifestFile.getParentFile();
            FileObject rootFo = FileUtil.toFileObject(rootFolder);
            grps.add(GenericSources.group(project, rootFo, AndroidConstants.ANDROID_MANIFEST_XML, AndroidConstants.ANDROID_MANIFEST_XML, null, null));
        }
        return grps.toArray(new SourceGroup[grps.size()]);
    }

    private SourceGroup[] groupSrcMainRes() {
        List<SourceGroup> grps = new ArrayList<>();
        FileObject prjDir = project.getProjectDirectory();
        if (androidProjectModel != null) {
            for (File srcDir : androidProjectModel.getDefaultConfig().getSourceProvider().getResDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "App resources " + srcName, null, null));
            }
            for (File srcDir : androidProjectModel.getDefaultConfig().getSourceProvider().getAssetsDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "App assets " + srcName, null, null));
            }
        }
        BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
        if (buildTypeContainer != null) {
            for (File srcDir : buildTypeContainer.getSourceProvider().getResDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "App resources " + srcName, null, null));
            }
            for (File srcDir : buildTypeContainer.getSourceProvider().getAssetsDirectories()) {
                if (!srcDir.exists()) {
                    continue;
                }
                FileObject src = FileUtil.toFileObject(srcDir);
                String srcName = FileUtil.isParentOf(prjDir, src)
                        ? FileUtil.getRelativePath(prjDir, src)
                        : srcDir.getAbsolutePath();
                grps.add(GenericSources.group(project, src, srcName, "App assets " + srcName, null, null));
            }
        }
        return grps.toArray(new SourceGroup[grps.size()]);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        cs.fireChange();
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

    /**
     * SourceGroup that accept all sources including generated (NOT_SHARABLE).
     */
    private static final class AnySourceGroup implements SourceGroup {

        private final Project p;
        private final FileObject rootFolder;
        private final String name;
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;

        AnySourceGroup(Project p, FileObject rootFolder, String name, String displayName, Icon icon, Icon openedIcon) {
            this.p = p;
            this.rootFolder = rootFolder;
            this.name = name;
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
        }

        @Override
        public FileObject getRootFolder() {
            return rootFolder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }

        @Override
        public boolean contains(FileObject file) {
            if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                return false;
            }
            if (file.isFolder() && file != p.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                // #67450: avoid actually loading the nested project.
                return false;
            }
            if (FileOwnerQuery.getOwner(file) != p) {
                return false;
            }
            // MIXED, UNKNOWN, and SHARABLE -> include it
            return true; // SharabilityQuery.getSharability(file) != SharabilityQuery.Sharability.NOT_SHARABLE;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            // XXX should react to ProjectInformation changes
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            // XXX
        }

        @Override
        public String toString() {
            return "AnySourceGroup[name=" + name + ",rootFolder=" + rootFolder + "]";
        }
    }
}
