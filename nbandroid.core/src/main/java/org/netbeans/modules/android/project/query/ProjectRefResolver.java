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

import com.android.ide.common.xml.ManifestData;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.nbandroid.netbeans.gradle.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.api.AndroidProjects;
import org.nbandroid.netbeans.gradle.api.ReferenceResolver;
import org.nbandroid.netbeans.gradle.api.ResourceRef;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;

/**
 *
 * @author arsi
 */
public class ProjectRefResolver implements ReferenceResolver {

    public static final Logger LOG = Logger.getLogger(ProjectRefResolver.class.getName());

    private final Project prj;

    public ProjectRefResolver(Project prj) {
        this.prj = Preconditions.checkNotNull(prj);
    }

    @Override
    public Iterable<ResourceRef> getReferences() {
        final List<ResourceRef> results = Lists.newArrayList();
        ManifestData manifest = AndroidProjects.parseProjectManifest(prj);
        final String pkg = manifest != null ? manifest.getPackage() : null;
        if (pkg == null) {
            LOG.log(Level.FINE, "cannot find package name to find R.java");
            return Collections.emptyList();
        }
        SourceGroup[] genGroups
                = ProjectUtils.getSources(prj).getSourceGroups(AndroidConstants.SOURCES_TYPE_GENERATED_JAVA);
        FileObject rFile = null;
        if (genGroups != null && genGroups.length > 0) {
            for (SourceGroup genGroup : genGroups) {
                rFile = genGroup.getRootFolder().getFileObject(pkg.replace('.', '/') + "/R.java");
                if (rFile != null) {
                    break;
                }
            }

        } else {
            rFile = prj.getProjectDirectory().getFileObject("gen/" + pkg.replace('.', '/') + "/R.java");
        }
        if (rFile == null) {
            LOG.log(Level.FINE, "no R.java");
            return Collections.emptyList();
        }
        JavaSource javaSource = JavaSource.create(ClasspathInfo.create(rFile), rFile);
        if (javaSource == null) {
            return Collections.emptyList();
        }

        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController parameter) throws IOException {

                    parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                    List<? extends TypeElement> topLevelElements = parameter.getTopLevelElements();
                    for (TypeElement el : topLevelElements) {
                        if (!el.getSimpleName().contentEquals("R")) {
                            continue;
                        }

                        for (TypeElement category : ElementFilter.typesIn(el.getEnclosedElements())) {
                            String catName = category.getSimpleName().toString();
                            for (VariableElement resource : ElementFilter.fieldsIn(category.getEnclosedElements())) {
                                String resName = resource.getSimpleName().toString();
                                Object resValue = resource.getConstantValue();
                                final ResourceRef resourceRef = new ResourceRef(true, pkg, catName, resName, resValue);
                                results.add(resourceRef);
                                LOG.log(Level.FINER, "resource {0}", resourceRef);
                            }
                        }
                    }
                    LOG.log(Level.FINE, "found {0} resources", results.size());
                }
            }, true);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }

        return results;
    }
}
