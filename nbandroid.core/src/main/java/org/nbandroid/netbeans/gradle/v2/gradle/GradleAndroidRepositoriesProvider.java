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
package org.nbandroid.netbeans.gradle.v2.gradle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.gradle.project.NbGradleProject;
import org.netbeans.gradle.project.NbGradleProjectFactory;
import org.netbeans.gradle.project.model.NbGradleModel;

/**
 *
 * @author arsi
 */
public class GradleAndroidRepositoriesProvider {

    private final Project project;

    public GradleAndroidRepositoriesProvider(Project project) {
        this.project = project;
    }

    public List<Repository> getAllRepositories() {
        List<Repository> tmp = new ArrayList<>();
        NbGradleProject proj = project.getLookup().lookup(NbGradleProject.class);
        if (proj != null) {
            NbGradleModel model = proj.currentModel().getValue();
            if (model != null) {
                if (!model.isRootProject()) {
                    File rootProjectDir = model.getSettingsDir().toFile();
                    NbGradleProject rooProject = NbGradleProjectFactory.tryLoadSafeGradleProject(rootProjectDir);
                    NbGradleModel rootModel = rooProject.currentModel().getValue();
                    if (rootModel != null) {
                        File rootBuildFile = rootModel.getBuildFile();
                        try {
                            tmp.addAll(FindRepositoriesVisitor.visit(rootBuildFile));
                        } catch (IOException ex) {
                        }
                    }
                }
                File buildFile = model.getBuildFile();
                try {
                    tmp.addAll(FindRepositoriesVisitor.visit(buildFile));
                } catch (IOException ex) {
                }
                return new ArrayList<>(new LinkedHashSet<>(tmp));
            }
        }
        return Collections.EMPTY_LIST;
    }
}
