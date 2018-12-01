/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author arsi
 */
public class FmHasDependencyMethod implements TemplateMethodModelEx {

    private final Map<String, Object> myParamMap;

    public FmHasDependencyMethod(Map<String, Object> paramMap) {
        myParamMap = paramMap;
    }

    @Override
    public Object exec(List list) throws TemplateModelException {
        if (list.size() < 1 || list.size() > 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        //TODO detect Dependencies from projekt
        return TemplateBooleanModel.FALSE;
    }

}
