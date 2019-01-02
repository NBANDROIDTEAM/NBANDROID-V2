/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.gradle.nbandroid.models;

import java.io.Serializable;
import org.netbeans.gradle.model.api.ProjectInfoBuilder2;

/**
 *
 * @author arsi
 */
public final class GradleAndroidModelBuilders implements Serializable {

    public static final ProjectInfoBuilder2<AndroidNbModel> ANDROID_BUILDER = AndroidProjectInfoBuilder.INSTANCE;

}
