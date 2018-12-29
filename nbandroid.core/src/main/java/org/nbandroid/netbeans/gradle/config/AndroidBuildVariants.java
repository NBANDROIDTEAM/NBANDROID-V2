package org.nbandroid.netbeans.gradle.config;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import javax.annotation.Nullable;

/**
 *
 * @author radim
 */
public class AndroidBuildVariants {

    @Nullable
    public static AndroidArtifact instrumentTestArtifact(Iterable<AndroidArtifact> artifacts) {
        return artifacts == null
                ? null
                : Iterables.find(
                        artifacts,
                        new Predicate<AndroidArtifact>() {
                    @Override
                    public boolean apply(AndroidArtifact a) {
                        return AndroidProject.ARTIFACT_ANDROID_TEST.equals(a.getName());
                    }
                },
                        null);
    }

    @Nullable
    public static Variant findVariantByName(Iterable<Variant> variants, final String name) {
        return variants == null || name == null
                ? null
                : Iterables.find(
                        variants,
                        new Predicate<Variant>() {

                    @Override
                    public boolean apply(Variant t) {
                        return name.equals(t.getName());
                    }
                },
                        null);
    }

    @Nullable
    public static Variant findDebugVariant(Iterable<Variant> variants) {
        return findVariantByName(variants, "debug");
    }
}
