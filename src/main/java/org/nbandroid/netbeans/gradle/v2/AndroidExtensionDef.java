/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        return Collections.emptySet();
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
