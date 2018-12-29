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
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaArtifact;
import com.android.builder.model.Variant;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependencies;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependency;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader;
import static org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader.PROJECT_TEMPLATE_LOADER;
import static org.nbandroid.netbeans.gradle.v2.template.mobile.MobileActivityWizardIterator.BUILD_VARIANT;

/**
 *
 * @author arsi
 */
public class FmHasDependencyMethod implements TemplateMethodModelEx {

    private final Map<String, Object> paramMap;

    public FmHasDependencyMethod(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    @Override
    public Object exec(List list) throws TemplateModelException {
        if (list.size() < 1 || list.size() > 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        String type = null;
        if (list.size() > 1) {
            type = ((SimpleScalar) list.get(1)).getAsString();
        }
        String artifact = ((SimpleScalar) list.get(0)).getAsString();
        ProjectTemplateLoader loader = (ProjectTemplateLoader) paramMap.get(PROJECT_TEMPLATE_LOADER);
        if (loader != null) {
            AndroidGradleDependencies dependencies = loader.getDependencies();
            if (dependencies != null) {
                List<AndroidGradleDependency> dep = dependencies.getDependencies();
                for (AndroidGradleDependency dependency : dep) {
                    if (dependency.getAndroidDependency() instanceof AndroidGradleDependency.AndroidRemoteBinaryDependency) {
                        String mavenUrl = ((AndroidGradleDependency.AndroidRemoteBinaryDependency) dependency.getAndroidDependency()).getRemoteBinary();
                        String[] split = mavenUrl.split(":");
                        if (split.length > 1) {
                            mavenUrl = split[0] + ":" + split[1];
                            if (type != null) {
                                if (type.equalsIgnoreCase(dependency.getType()) && artifact.equalsIgnoreCase(mavenUrl)) {
                                    return TemplateBooleanModel.TRUE;
                                }
                            } else if (artifact.equalsIgnoreCase(mavenUrl)) {
                                return TemplateBooleanModel.TRUE;
                            }
                        }
                    }
                }
            }
        }
        Variant variant = (Variant) paramMap.get(BUILD_VARIANT);
        if (variant instanceof Variant) {
            Collection<AndroidArtifact> androidArtifacts = variant.getExtraAndroidArtifacts();
            Collection<JavaArtifact> javaArtifacts = variant.getExtraJavaArtifacts();
            for (AndroidArtifact next : androidArtifacts) {
                Dependencies dependencies = next.getDependencies();
                Collection<AndroidLibrary> libraries = dependencies.getLibraries();
                for (AndroidLibrary lib : libraries) {
                    String artifactId = lib.getResolvedCoordinates().getArtifactId();
                    String groupId = lib.getResolvedCoordinates().getGroupId();
                    String mavenUrl = groupId + ":" + artifactId;
                    if (artifact.equalsIgnoreCase(mavenUrl)) {
                        return TemplateBooleanModel.TRUE;
                    }
                }
            }
            for (JavaArtifact next : javaArtifacts) {
                Dependencies dependencies = next.getDependencies();
                Collection<AndroidLibrary> libraries = dependencies.getLibraries();
                for (AndroidLibrary lib : libraries) {
                    String artifactId = lib.getResolvedCoordinates().getArtifactId();
                    String groupId = lib.getResolvedCoordinates().getGroupId();
                    String mavenUrl = groupId + ":" + artifactId;
                    if (artifact.equalsIgnoreCase(mavenUrl)) {
                        return TemplateBooleanModel.TRUE;
                    }
                }
            }
        }

        //TODO detect Dependencies from projekt
        return TemplateBooleanModel.FALSE;
    }

}
