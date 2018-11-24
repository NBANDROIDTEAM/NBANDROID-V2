/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.template.freemarker;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *
 * @author arsi
 */
public class PropertyObjectWrapper extends DefaultObjectWrapper {

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        return super.handleUnknownType(obj);
    }
}
