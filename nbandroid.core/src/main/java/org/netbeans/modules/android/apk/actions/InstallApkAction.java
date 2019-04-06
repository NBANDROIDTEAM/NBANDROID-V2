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

import com.android.ddmlib.InstallException;
import org.nbandroid.netbeans.gradle.avd.AvdSelector;
import org.nbandroid.netbeans.gradle.configs.ConfigBuilder;
import org.nbandroid.netbeans.gradle.launch.AndroidLauncher;
import org.nbandroid.netbeans.gradle.launch.AndroidLauncherImpl;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
public class InstallApkAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        FileObject fo = node.getLookup().lookup(FileObject.class);
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner != null) {
            AndroidSdk sdk = owner.getLookup().lookup(AndroidSdk.class);
            if (sdk != null) {
                AndroidLauncher launcher = new AndroidLauncherImpl();
                LaunchConfiguration cfg = ConfigBuilder.builder()
                        .withName("dummy").withTargetMode(LaunchConfiguration.TargetMode.AUTO).config().getLaunchConfiguration();
                AvdSelector.LaunchData launchData = launcher.configAvd(
                        sdk.getAndroidSdkHandler(), AndroidSdkProvider.getDefaultSdk(), null, cfg);
                if (launchData == null || launchData.getDevice() == null) {
                    return;
                }
                try {
                    launchData.getDevice().installPackage(fo.getPath(), true);
                } catch (InstallException ex) {
                    String message = ex.getMessage();
                    NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Node node = activatedNodes[0];
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo == null || !"apk".equalsIgnoreCase(fo.getExt())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "Install..";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
