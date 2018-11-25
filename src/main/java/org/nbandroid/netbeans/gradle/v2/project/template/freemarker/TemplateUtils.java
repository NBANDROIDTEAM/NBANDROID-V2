/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker;

import android.studio.imports.templates.TemplateMetadata;
import com.google.common.collect.LinkedHashMultimap;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmActivityToLayoutMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmCamelCaseToUnderscoreMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmClassNameToResourceMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmCompareVersionsIgnoringQualifiers;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmEscapeKotlinIdentifiers;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmEscapePropertyValueMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmEscapeXmlAttributeMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmEscapeXmlStringMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmExtractLettersMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmGetConfigurationNameMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmGetMaterialComponentName;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmIsAndroidxEnabled;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmLayoutToActivityMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmSlashedPackageNameMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmTruncateStringMethod;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmUnderscoreToCamelCaseMethod;

/**
 *
 * @author arsi
 */
public class TemplateUtils {

    /**
     * Creates a Java class name out of the given string, if possible. For
     * example, "My Project" becomes "MyProject", "hello" becomes "Hello",
     * "Java's" becomes "Java", and so on.
     *
     * @param string the string to be massaged into a Java class
     * @return the string as a Java class, or null if a class name could not be
     * extracted
     */
    public static String extractClassName(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        int n = string.length();

        int i = 0;
        for (; i < n; i++) {
            char c = Character.toUpperCase(string.charAt(i));
            if (Character.isJavaIdentifierStart(c)) {
                sb.append(c);
                i++;
                break;
            }
        }
        if (sb.length() > 0) {
            for (; i < n; i++) {
                char c = string.charAt(i);
                if (Character.isJavaIdentifierPart(c)) {
                    sb.append(c);
                }
            }

            return sb.toString();
        }

        return null;
    }

    /**
     * Converts an underlined_word into a CamelCase word
     *
     * @param string the underlined word to convert
     * @return the CamelCase version of the word
     */
    public static String underlinesToCamelCase(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        int n = string.length();

        int i = 0;
        boolean upcaseNext = true;
        for (; i < n; i++) {
            char c = string.charAt(i);
            if (c == '_') {
                upcaseNext = true;
            } else {
                if (upcaseNext) {
                    c = Character.toUpperCase(c);
                }
                upcaseNext = false;
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Converts a CamelCase word into an underlined_word
     *
     * @param string the CamelCase version of the word
     * @return the underlined version of the word
     */
    public static String camelCaseToUnderlines(String string) {
        if (string.isEmpty()) {
            return string;
        }

        StringBuilder sb = new StringBuilder(2 * string.length());
        int n = string.length();
        boolean lastWasUpperCase = Character.isUpperCase(string.charAt(0));
        for (int i = 0; i < n; i++) {
            char c = string.charAt(i);
            boolean isUpperCase = Character.isUpperCase(c);
            if (isUpperCase && !lastWasUpperCase) {
                sb.append('_');
            }
            lastWasUpperCase = isUpperCase;
            c = Character.toLowerCase(c);
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Strip off the end portion of the name. The user might be typing the
     * activity name such that only a portion has been entered so far (e.g.
     * "MainActivi") and we want to chop off that portion too such that we don't
     * offer a layout name partially containing the activity suffix (e.g.
     * "main_activi").
     */
    public static String stripSuffix(String name, String suffix, boolean recursiveStrip) {
        if (name.length() < 2) {
            return name;
        }

        int suffixStart = name.lastIndexOf(suffix.charAt(0));
        if (suffixStart != -1 && name.regionMatches(suffixStart, suffix, 0,
                name.length() - suffixStart)) {
            name = name.substring(0, suffixStart);
        }
        // Recursively continue to strip the suffix (catch the FooActivityActivity case)
        if (recursiveStrip && name.endsWith(suffix)) {
            return stripSuffix(name, suffix, recursiveStrip);
        }

        return name;
    }

    /**
     * Strips the given suffix from the given file, provided that the file name
     * ends with the suffix.
     *
     * @param file the file to strip from
     * @param suffix the suffix to strip out
     * @return the file without the suffix at the end
     */
    public static File stripSuffix(@NotNull File file, @NotNull String suffix) {
        if (file.getName().endsWith(suffix)) {
            String name = file.getName();
            name = name.substring(0, name.length() - suffix.length());
            File parent = file.getParentFile();
            if (parent != null) {
                return new File(parent, name);
            } else {
                return new File(name);
            }
        }

        return file;
    }

    public static Map<String, Object> createParameterMap(@NotNull Map<String, Object> args) {
        // Create the data model.
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        // Builtin conversion methods
        paramMap.put("activityToLayout", new FmActivityToLayoutMethod());
        paramMap.put("camelCaseToUnderscore", new FmCamelCaseToUnderscoreMethod());
        paramMap.put("classToResource", new FmClassNameToResourceMethod());
        paramMap.put("escapePropertyValue", new FmEscapePropertyValueMethod());
        paramMap.put("escapeXmlAttribute", new FmEscapeXmlAttributeMethod());
        paramMap.put("escapeXmlString", new FmEscapeXmlStringMethod());
        paramMap.put("escapeXmlText", new FmEscapeXmlStringMethod());
        paramMap.put("extractLetters", new FmExtractLettersMethod());
        paramMap.put("layoutToActivity", new FmLayoutToActivityMethod());
        paramMap.put("slashedPackageName", new FmSlashedPackageNameMethod());
        paramMap.put("truncate", new FmTruncateStringMethod());
        paramMap.put("underscoreToCamelCase", new FmUnderscoreToCamelCaseMethod());
        paramMap.put("compareVersionsIgnoringQualifiers", new FmCompareVersionsIgnoringQualifiers());
        paramMap.put("getConfigurationName", new FmGetConfigurationNameMethod(paramMap));
        paramMap.put("getMaterialComponentName", new FmGetMaterialComponentName());
        paramMap.put("compareVersions", new FmCompareVersionsIgnoringQualifiers());
        paramMap.put("escapeKotlinIdentifiers", new FmEscapeKotlinIdentifiers());
        paramMap.put("isAndroidxEnabled", new FmIsAndroidxEnabled());

        // Dependencies multimap. Doesn't store duplicates, preserves insertion order.
        paramMap.put(TemplateMetadata.ATTR_DEPENDENCIES_MULTIMAP, LinkedHashMultimap.create());

        // Parameters supplied by user
        paramMap.putAll(args);

        return paramMap;
    }
}
