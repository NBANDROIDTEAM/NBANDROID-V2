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
package org.nbandroid.netbeans.gradle.v2.nodes.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.nbandroid.netbeans.gradle.api.AndroidClassPath;
import org.nbandroid.netbeans.gradle.v2.gradle.GradleAndroidRepositoriesProvider;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependencyUpdater;
import org.nbandroid.netbeans.gradle.v2.maven.ArtifactData;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.gradle.project.NbGradleProject;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import sk.arsi.netbeans.gradle.android.maven.AddDependecyDialogProvider;
import sk.arsi.netbeans.gradle.android.maven.repository.Repository;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "NbAndroid/Dependencies",
        id = "org.nbandroid.netbeans.gradle.v2.nodes.actions.AddDependencyAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class AddDependencyAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            AddDependecyDialogProvider dialogProvider = Lookup.getDefault().lookup(AddDependecyDialogProvider.class);
            NbGradleProject project = activatedNodes[0].getLookup().lookup(NbGradleProject.class);
            if (project != null && dialogProvider != null) {
                GradleAndroidRepositoriesProvider repositoriesProvider = project.getLookup().lookup(GradleAndroidRepositoriesProvider.class);
                if (repositoriesProvider != null) {
                    List<Repository> repositories = repositoriesProvider.getAllRepositories();
                    List<ArtifactData> compileDependecies = new ArrayList<>();
                    List<String> packages = new ArrayList<>();
                    findCompileTimeDependencies(project, compileDependecies);
                    for (ArtifactData artifactData : compileDependecies) {
                        String mavenLocation = artifactData.getMavenLocation();
                        if (mavenLocation != null) {
                            if (mavenLocation.startsWith("/")) {
                                mavenLocation = mavenLocation.substring(1);
                            }
                            if (mavenLocation.endsWith("/")) {
                                mavenLocation = mavenLocation.substring(0, mavenLocation.length() - 1);
                            }
                            mavenLocation = mavenLocation.replace("/", ":");
                            packages.add(mavenLocation);
                        }
                    }
                    String mavenUrl = dialogProvider.showAddDependencyDialog(repositories, packages);
                    if (mavenUrl != null) {
                        Map<String, List<String>> dependencies = new HashMap<>();
                        List<String> tmp = new ArrayList<>();
                        tmp.add(mavenUrl);
                        dependencies.put("implementation", tmp);
                        String builGradle = project.getProjectDirectoryAsFile().getAbsolutePath() + File.separator + "build.gradle";
                        File builGradleFile = new File(builGradle);
                        if (builGradleFile.exists()) {
                            AndroidGradleDependencyUpdater.insertDependencies(builGradleFile, dependencies);
                            project.reloadProject();
                        }
                    }
                }
            }
        }
    }

    private void findCompileTimeDependencies(NbGradleProject project, List<ArtifactData> compileDependecies) {
        final ClassPathProvider cpProvider = project.getLookup().lookup(ClassPathProvider.class);
        if (cpProvider != null) {
            Sources srcs = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            Set<FileObject> roots = Sets.newHashSet();
            for (SourceGroup sg : sourceGroups) {
                roots.add(sg.getRootFolder());
            }
            Iterable<ClassPath> compileCPs = Iterables.transform(roots, new Function<FileObject, ClassPath>() {

                @Override
                public ClassPath apply(FileObject f) {
                    return cpProvider.findClassPath(f, ClassPath.COMPILE);
                }
            });
            ClassPath compileCP
                    = ClassPathSupport.createProxyClassPath(Lists.newArrayList(compileCPs).toArray(new ClassPath[0]));
            if (cpProvider instanceof AndroidClassPath) {
                for (FileObject cpRoot : compileCP.getRoots()) {
                    ArtifactData lib = ((AndroidClassPath) cpProvider).getArtifactData(cpRoot.toURL());
                    compileDependecies.add(lib);
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "Add dependency";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

}
