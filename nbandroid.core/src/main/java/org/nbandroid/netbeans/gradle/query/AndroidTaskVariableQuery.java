package org.nbandroid.netbeans.gradle.query;

import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.netbeans.gradle.project.api.task.GradleTaskVariableQuery;
import org.netbeans.gradle.project.api.task.TaskVariable;
import org.netbeans.gradle.project.api.task.TaskVariableMap;
import org.openide.util.Lookup;

/**
 *
 * @author radim
 */
public class AndroidTaskVariableQuery implements GradleTaskVariableQuery {

    public static final TaskVariable BUILD_VARIANT_VARIABLE = new TaskVariable("BuildVariant");

    private final BuildVariant buildVariant;

    public AndroidTaskVariableQuery(BuildVariant buildVariant) {
        this.buildVariant = buildVariant;
    }

    @Override
    public TaskVariableMap getVariableMap(Lookup lkp) {
        return new AndroidTaskVariableMap();
    }

    private class AndroidTaskVariableMap implements TaskVariableMap {

        @Override
        public String tryGetValueForVariable(TaskVariable tv) {
            if (BUILD_VARIANT_VARIABLE.equals(tv)) {
                String variant = buildVariant.getVariantName();
                variant = variant != null ? variant : "debug";
                variant = Character.toUpperCase(variant.charAt(0)) + variant.substring(1);
                return variant;
            }
            return null;
        }
    }
}
