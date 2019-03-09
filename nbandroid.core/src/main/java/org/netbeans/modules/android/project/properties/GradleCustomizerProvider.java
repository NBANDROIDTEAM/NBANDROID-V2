/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.project.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import nbandroid.gradle.spi.GradleArgsConfiguration;
import nbandroid.gradle.spi.GradleJvmConfiguration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.properties.ui.GradleArgumentsConfigPanel;
import org.netbeans.modules.android.project.properties.ui.GradleJvmConfigPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class GradleCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CUSTOMIZER_GRADLE = "Gradle";
    private static final String CUSTOMIZER_GRADLE_JVM = "GradleJVM";
    private static final String CUSTOMIZER_GRADLE_ARGS = "GradleArgs";

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "android-project", position = 20)
    public static GradleCustomizerProvider createGradleCustomizerProvider() {
        return new GradleCustomizerProvider();
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CUSTOMIZER_GRADLE, "Gradle", null,
                ProjectCustomizer.Category.create(CUSTOMIZER_GRADLE_JVM, "JVM", null),
                ProjectCustomizer.Category.create(CUSTOMIZER_GRADLE_ARGS, "Arguments", null));
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        switch (category.getName()) {
            case CUSTOMIZER_GRADLE:
                return new JPanel();
            case CUSTOMIZER_GRADLE_JVM:
                GradleJvmConfiguration jvmConfiguration = context.lookup(Project.class).getLookup().lookup(GradleJvmConfiguration.class);
                final GradleJvmConfigPanel configPanel = new GradleJvmConfigPanel(jvmConfiguration);
                category.setStoreListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        configPanel.store();
                    }
                });
                return configPanel;
            case CUSTOMIZER_GRADLE_ARGS:
                GradleArgsConfiguration argsConfiguration = context.lookup(Project.class).getLookup().lookup(GradleArgsConfiguration.class);
                final GradleArgumentsConfigPanel argsConfigPanel = new GradleArgumentsConfigPanel(argsConfiguration);
                category.setStoreListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        argsConfigPanel.store();
                    }
                });
                return argsConfigPanel;
        }
        return null;
    }

}
