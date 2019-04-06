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
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.properties.ui.AndroidTestsPanel;
import org.netbeans.modules.android.project.run.AndroidTestRunConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class AndroidTestsProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CUSTOMIZER_TESTS = "AndroidTests";

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "android-project", position = 50)
    public static AndroidTestsProvider createAndroidBuildCustomizerProvider() {
        return new AndroidTestsProvider();
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CUSTOMIZER_TESTS, "Android Tests", null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        final AndroidTestsPanel panel = new AndroidTestsPanel();
        Project prj = context.lookup(Project.class);
        if (prj != null) {
            final AndroidTestRunConfiguration testRunCfg = prj.getLookup().lookup(AndroidTestRunConfiguration.class);
            if (testRunCfg != null) {
                panel.setData(testRunCfg);
                category.setStoreListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        testRunCfg.setTestRunner(panel.getData());
                    }
                });
            }
        }
        return panel;
    }

}
