/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import android.studio.imports.templates.TemplateMetadata;
import com.android.SdkConstants;
import com.android.utils.SdkUtils;
import freemarker.template.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Method invoked by FreeMarker to compute the right dependency string to use in
 * the current module. The right string to use depends on the version of Gradle
 * used in the module.
 *
 * <p>
 * Arguments:
 * <ol>
 * <li>The configuration (if left out, defaults to "compile")
 * </ol>
 * <p>
 * Example usage: {@code espresso=getDependency('androidTestCompile')}, which
 * (for Gradle 3.0) will return "androidTestImplementation"
 */
public class FmGetConfigurationNameMethod implements TemplateMethodModelEx {

    private final Map<String, Object> myParamMap;

    public FmGetConfigurationNameMethod(Map<String, Object> paramMap) {
        myParamMap = paramMap;
    }

    @Override
    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() >= 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        String configuration = args.size() == 1 ? ((TemplateScalarModel) args.get(0)).getAsString() : SdkConstants.GRADLE_COMPILE_CONFIGURATION;
        return new SimpleScalar(convertConfiguration(myParamMap, configuration));
    }

    public static String convertConfiguration(@NotNull Map<String, Object> myParamMap, @NotNull String configuration) {
        String gradlePluginVersion = null;
        if (myParamMap.containsKey(TemplateMetadata.ATTR_GRADLE_PLUGIN_VERSION)) {
            Object untyped = myParamMap.get(TemplateMetadata.ATTR_GRADLE_PLUGIN_VERSION);
            if (untyped instanceof String) {
                gradlePluginVersion = (String) untyped;
            }
        }

        return mapConfigurationName(configuration, gradlePluginVersion, false);
    }

    private static final Pattern PLUGIN_VERSION_PATTERN = Pattern.compile("[012]\\..*");

    /**
     * This method converts a configuration name from (for example) "compile" to
     * "implementation" if the Gradle plugin version is 3.0 or higher.
     *
     * @param configuration The original configuration name, such as
     * "androidTestCompile"
     * @param pluginVersion The plugin version number, such as 3.0.0-alpha1. If
     * null, assumed to be current.
     * @param preferApi If true, will use "api" instead of "implementation" for
     * new configurations
     * @return the right configuration name to use
     */
    @NotNull
    public static String mapConfigurationName(@NotNull String configuration,
            @Nullable GradleVersion pluginVersion,
            boolean preferApi) {
        return mapConfigurationName(configuration, pluginVersion != null ? pluginVersion.toString() : null, preferApi);
    }

    /**
     * This method converts a configuration name from (for example) "compile" to
     * "implementation" if the Gradle plugin version is 3.0 or higher.
     *
     * @param configuration The original configuration name, such as
     * "androidTestCompile"
     * @param pluginVersion The plugin version number, such as 3.0.0-alpha1. If
     * null, assumed to be current.
     * @param preferApi If true, will use "api" instead of "implementation" for
     * new configurations
     * @return the right configuration name to use
     */
    @NotNull
    public static String mapConfigurationName(@NotNull String configuration,
            @Nullable String pluginVersion,
            boolean preferApi) {

        boolean compatibilityNames = pluginVersion != null && PLUGIN_VERSION_PATTERN.matcher(pluginVersion).matches();
        return mapConfigurationName(configuration, compatibilityNames, preferApi);
    }

    /**
     * This method converts a configuration name from (for example) "compile" to
     * "implementation" if the Gradle plugin version is 3.0 or higher.
     *
     * @param configuration The original configuration name, such as
     * "androidTestCompile"
     * @param useCompatibilityNames Whether we should use compatibility names
     * @param preferApi If true, will use "api" instead of "implementation" for
     * new configurations
     * @return the right configuration name to use
     */
    @NotNull
    private static String mapConfigurationName(@NotNull String configuration,
            boolean useCompatibilityNames,
            boolean preferApi) {
        if (useCompatibilityNames) {
            return configuration;
        }

        configuration = replaceSuffixWithCase(configuration, "compile", preferApi ? "api" : "implementation");
        configuration = replaceSuffixWithCase(configuration, "provided", "compileOnly");
        configuration = replaceSuffixWithCase(configuration, "apk", "runtimeOnly");

        return configuration;
    }

    /**
     * Replaces the given suffix in the string, preserving the case in the
     * string, e.g. replacing "foo" with "bar" will result in "bar", and
     * replacing "myFoo" with "bar" will result in "myBar". (This is not a
     * general purpose method; it assumes that the only non-lowercase letter is
     * the first letter of the suffix.)
     */
    private static String replaceSuffixWithCase(String s, String suffix, String newSuffix) {
        if (SdkUtils.endsWithIgnoreCase(s, suffix)) {
            int suffixBegin = s.length() - suffix.length();
            if (Character.isUpperCase(s.charAt(suffixBegin))) {
                return s.substring(0, suffixBegin) + Character.toUpperCase(newSuffix.charAt(0)) + newSuffix.substring(1);
            } else if (suffixBegin == 0) {
                return newSuffix;
            } else {
                return s.substring(0, suffixBegin) + suffix;
            }
        }

        return s;
    }
}
