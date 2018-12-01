/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.Map;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependencies;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependency;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader;
import static org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader.PROJECT_TEMPLATE_LOADER;

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

        //TODO detect Dependencies from projekt
        return TemplateBooleanModel.FALSE;
    }

}
