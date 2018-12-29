package org.nbandroid.netbeans.gradle.config;

import com.android.builder.model.BuildTypeContainer;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import javax.annotation.Nullable;

/**
 *
 * @author radim
 */
public class BuildTypes {

    @Nullable
    public static BuildTypeContainer findBuildTypeByName(Iterable<BuildTypeContainer> buildTypes, final String name) {
        return buildTypes == null || name == null
                ? null
                : Iterables.find(
                        buildTypes,
                        new Predicate<BuildTypeContainer>() {

                    @Override
                    public boolean apply(BuildTypeContainer t) {
                        return name.equals(t.getBuildType().getName());
                    }
                },
                        null);
    }
}
