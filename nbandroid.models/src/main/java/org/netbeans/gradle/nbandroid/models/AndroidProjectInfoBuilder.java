/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.gradle.nbandroid.models;

import org.netbeans.gradle.model.api.ProjectInfoBuilder2;


public enum AndroidProjectInfoBuilder implements ProjectInfoBuilder2<AndroidNbModel> {
    INSTANCE;

    @Override
    public AndroidNbModel getProjectInfo(Object project) {
        return new AndroidNbModel();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
