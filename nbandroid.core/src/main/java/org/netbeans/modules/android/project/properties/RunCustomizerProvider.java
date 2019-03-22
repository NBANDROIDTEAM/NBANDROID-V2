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
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.properties.ui.MainActivityConfigurationPanel;
import org.netbeans.modules.android.spi.RunActivityConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class RunCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CUSTOMIZER_RUN = "Run";
    private static final String CUSTOMIZER_RUN_MAIN_ACTIVITY = "Main Activity";

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "android-project", position = 40)
    public static RunCustomizerProvider createRunCustomizerProvider() {
        return new RunCustomizerProvider();
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CUSTOMIZER_RUN, "Run", null,
                ProjectCustomizer.Category.create(CUSTOMIZER_RUN_MAIN_ACTIVITY, "Main Activity", null)
        );
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        switch (category.getName()) {
            case CUSTOMIZER_RUN:
                return new JPanel();
            case CUSTOMIZER_RUN_MAIN_ACTIVITY:
                RunActivityConfiguration runActivityConfiguration = context.lookup(Project.class).getLookup().lookup(RunActivityConfiguration.class);
                final MainActivityConfigurationPanel configPanel = new MainActivityConfigurationPanel(runActivityConfiguration, context.lookup(Project.class));
                category.setStoreListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        configPanel.store();
                    }
                });
                return configPanel;
        }
        return null;
    }

}
