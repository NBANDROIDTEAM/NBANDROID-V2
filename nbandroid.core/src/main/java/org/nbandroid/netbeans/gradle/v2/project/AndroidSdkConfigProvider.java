/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project;

import javax.swing.JComponent;
import org.nbandroid.netbeans.gradle.v2.project.ui.AndroidProjectSdkConfig;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class AndroidSdkConfigProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CUSTOMIZER_SDKS = "androidsdks";

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CUSTOMIZER_SDKS, "Android SDK", null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        return new AndroidProjectSdkConfig(category, context);
    }

}
