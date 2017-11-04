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

import com.android.builder.model.AndroidProject;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.netbeans.api.project.Project;
import org.netbeans.gradle.project.api.entry.GradleProjectExtension2;
import org.netbeans.gradle.project.api.entry.GradleProjectExtensionDef;
import org.netbeans.gradle.project.api.entry.ModelLoadResult;
import org.netbeans.gradle.project.api.entry.ParsedModel;
import org.netbeans.gradle.project.api.modelquery.GradleModelDefQuery1;
import org.netbeans.gradle.project.api.modelquery.GradleTarget;
import org.netbeans.gradle.project.java.JavaExtensionDef;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = GradleProjectExtensionDef.class, position = 900)
public class AndroidExtensionDef implements GradleProjectExtensionDef<SerializableLookup> {

    public static final String EXTENSION_NAME = "org.nbandroid.netbeans.gradle.v2.AndroidExtensionDef";

    private final Lookup lookup;

    public AndroidExtensionDef() {
        this.lookup = Lookups.fixed(new Query1(), new Query2());
    }

    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public String getDisplayName() {
        return "Android";
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public Class<SerializableLookup> getModelType() {
        return SerializableLookup.class;
    }

    @Override
    public ParsedModel<SerializableLookup> parseModel(ModelLoadResult retrievedModels) {
        return new ParsedModel<>(new SerializableLookup(retrievedModels.getMainProjectModels()));
    }

    @Override
    public GradleProjectExtension2<SerializableLookup> createExtension(Project project) throws IOException {
        return new AndroidGradleExtensionV2(project);
    }

    @Override
    public Set<String> getSuppressedExtensions() {
        return Collections.<String>singleton(JavaExtensionDef.EXTENSION_NAME);
    }

    private static final class Query1 implements GradleModelDefQuery1 {

        private static final Collection<Class<?>> RESULT = Collections.<Class<?>>singleton(GradleBuild.class);

        @Override
        public Collection<Class<?>> getToolingModels(GradleTarget gradleTarget) {
            return RESULT;
        }
    }

    private static final class Query2 implements GradleModelDefQuery1 {

        private static final Collection<Class<?>> RESULT = Collections.<Class<?>>singleton(AndroidProject.class);

        @Override
        public Collection<Class<?>> getToolingModels(GradleTarget gradleTarget) {
            return RESULT;
        }
    }

}
