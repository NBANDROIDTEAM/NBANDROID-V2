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

import javax.swing.JComponent;
import org.netbeans.modules.android.project.properties.ui.ProfilesConfigPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class ProfilesConfigProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CUSTOMIZER_PROFILES = "androidprofiles";

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "android-project", position = 10)
    public static ProfilesConfigProvider createApkSigningConfigProvider() {
        return new ProfilesConfigProvider();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "android-root-project", position = 11)
    public static ProfilesConfigProvider createApkSigningConfigRootProvider() {
        return new ProfilesConfigProvider();
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CUSTOMIZER_PROFILES, "Configurations", null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        return new ProfilesConfigPanel(category, context);
    }

}
