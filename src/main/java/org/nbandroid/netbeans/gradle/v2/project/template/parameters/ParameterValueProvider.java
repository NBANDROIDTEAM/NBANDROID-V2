/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.template.parameters;

import android.studio.imports.templates.Parameter;
import java.util.Map;

/**
 *
 * @author arsi
 */
public interface ParameterValueProvider {

    public Parameter getParameter();

    public Object getValue();

    public void update(Map<Parameter, Object> values);
}
