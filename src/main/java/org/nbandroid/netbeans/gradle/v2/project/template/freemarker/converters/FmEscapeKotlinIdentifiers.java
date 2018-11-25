/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;

/**
 *
 * @author arsi
 */
public class FmEscapeKotlinIdentifiers implements TemplateMethodModelEx {

    @Override
    public Object exec(List list) throws TemplateModelException {
        return new SimpleScalar("");
    }

}
