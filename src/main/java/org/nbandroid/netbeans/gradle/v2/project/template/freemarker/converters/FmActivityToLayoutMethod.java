/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import java.util.List;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.AssetNameConverter;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.AssetNameConverter.Type;

/**
 * Method invoked by FreeMarker to convert an Activity class name into a
 * suitable layout name.
 */
public class FmActivityToLayoutMethod implements TemplateMethodModelEx {

    @Override
    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() < 1 || args.size() > 2) {
            throw new TemplateModelException("Wrong arguments");
        }

        String activityName = ((TemplateScalarModel) args.get(0)).getAsString();
        if (activityName.isEmpty()) {
            return new SimpleScalar("");
        }

        String layoutName = args.size() > 1 ? ((TemplateScalarModel) args.get(1)).getAsString() : null;
        return new SimpleScalar(new AssetNameConverter(Type.ACTIVITY, activityName).overrideLayoutPrefix(layoutName).getValue(Type.LAYOUT));
    }
}
