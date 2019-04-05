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
package org.netbeans.modules.android.apk.actions;

import com.android.builder.model.AndroidProject;
import java.util.ArrayList;
import java.util.List;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.apk.ApkUtils;
import org.netbeans.modules.android.project.keystore.KeystoreConfiguration;
import org.netbeans.modules.android.project.properties.ui.ApkSigningConfigPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "Android/Projects/Project",
        id = "org.netbeans.modules.android.apk.actions.SignApkAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)

@ActionReference(path = "Android/Projects/Project", position = 100)
public class SignApkAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }
        Project owner = activatedNodes[0].getLookup().lookup(Project.class);
        if (owner != null) {
            KeystoreConfiguration keystoreConfiguration = owner.getLookup().lookup(KeystoreConfiguration.class);
            if (keystoreConfiguration != null) {
                if (keystoreConfiguration.getCurrentKeystoreConfiguration().isAsk() || !keystoreConfiguration.getCurrentKeystoreConfiguration().isValid()) {
                    ApkSigningConfigPanel signingConfigPanel = new ApkSigningConfigPanel(null, owner.getLookup());
                    DialogDescriptor dialogDescriptor = new DialogDescriptor(signingConfigPanel, "Generate Signed APK", true, signingConfigPanel);
                    DialogDisplayer.getDefault().notify(dialogDescriptor);
                }
                if (keystoreConfiguration.getCurrentKeystoreConfiguration().isValid()) {
                    try {
                        List<String> tasks = new ArrayList<>();
                        if (keystoreConfiguration.getCurrentKeystoreConfiguration().isApkRelease()) {
                            tasks.add("assembleRelease");
                        }
                        if (keystoreConfiguration.getCurrentKeystoreConfiguration().isApkDebug()) {
                            tasks.add("assembleDebug");
                        }
                        ApkUtils.gradleSignApk(owner, "", tasks, keystoreConfiguration, FileUtil.toFile(owner.getProjectDirectory()));
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    InputOutput io = owner.getLookup().lookup(InputOutput.class);
                    if (io != null) {
                        io.show(ImmutableSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
                        io.getOut().print("\n\r");
                        io.getOut().print("\u001B[31mKeystore setting is not valid!\u001B[30m\n\r");
                        io.getOut().print("\n\r");
                    }
                }
            }
        }

    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Project owner = activatedNodes[0].getLookup().lookup(Project.class);
        return owner != null && owner.getLookup().lookup(AndroidProject.class) != null;
    }

    @Override
    public String getName() {
        return "Build Signed APK..";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
