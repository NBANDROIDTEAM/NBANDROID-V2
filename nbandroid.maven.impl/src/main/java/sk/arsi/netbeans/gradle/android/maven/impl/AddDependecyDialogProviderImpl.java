/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sk.arsi.netbeans.gradle.android.maven.impl;

import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.netbeans.gradle.android.maven.AddDependecyDialogProvider;
import sk.arsi.netbeans.gradle.android.maven.dialog.AddDependencyPanel;
import sk.arsi.netbeans.gradle.android.maven.repository.Repository;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = AddDependecyDialogProvider.class)
public class AddDependecyDialogProviderImpl implements AddDependecyDialogProvider {

    @Override
    public String showAddDependencyDialog(List<Repository> repositories, List<String> currentPackages) {
        AddDependencyPanel panel = new AddDependencyPanel(repositories, currentPackages);
        DialogDescriptor dd = new DialogDescriptor(panel, "Add dependency");
        dd.setClosingOptions(null);
        dd.setOptions(new Object[]{
            panel.getOkButton(),
            panel.getCancelButton(),});
        panel.attachDialogDisplayer(dd);
        Object notify = DialogDisplayer.getDefault().notify(dd);
        if (notify.equals(panel.getOkButton())) {
            return panel.getSelected();
        }
        return null;
    }

}
