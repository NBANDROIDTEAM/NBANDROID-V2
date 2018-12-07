/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import android.studio.imports.templates.TemplateMetadata;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author arsi
 */
public class FmGetAppManifestDirMethod implements TemplateMethodModelEx {

    private final Map<String, Object> myParamMap;

    public FmGetAppManifestDirMethod(Map<String, Object> paramMap) {
        myParamMap = paramMap;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        Object o = myParamMap.get(TemplateMetadata.ATTR_MANIFEST_OUT);
        if (o instanceof File) {
            return new SimpleScalar(((File) o).getAbsolutePath());
        }
        return null;
    }

}
