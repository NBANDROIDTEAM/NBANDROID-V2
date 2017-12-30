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
package org.nbandroid.netbeans.gradle.v2.apk.actions;

import java.util.ArrayList;
import java.util.List;
import org.nbandroid.netbeans.gradle.v2.apk.ApkUtils;
import org.nbandroid.netbeans.gradle.v2.apk.sign.keystore.KeystoreSelector;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.gradle.project.NbGradleProject;
import org.netbeans.gradle.project.api.nodes.GradleActionType;
import org.netbeans.gradle.project.api.nodes.GradleProjectAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@GradleProjectAction(GradleActionType.BUILD_ACTION)
public class SignApkAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }
        FileObject fo = activatedNodes[0].getLookup().lookup(FileObject.class);
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner != null) {
            NbGradleProject gradleProject = owner.getLookup().lookup(NbGradleProject.class);
            KeystoreSelector selector = new KeystoreSelector(owner, fo);
            DialogDescriptor dd = new DialogDescriptor(selector, "Generate Signed APK", true, selector);
            selector.setDescriptor(dd);
            Object notify = DialogDisplayer.getDefault().notify(dd);
            if (DialogDescriptor.OK_OPTION.equals(notify)) {
                try {
                    List<String> tasks = new ArrayList<>();
                    if (selector.isRelease()) {
                        tasks.add("assembleRelease");
                    }
                    if (selector.isDebug()) {
                        tasks.add("assembleDebug");
                    }
                    ApkUtils.gradleSignApk(gradleProject, "", tasks, FileUtil.toFile(fo), selector, FileUtil.toFile(owner.getProjectDirectory()));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        return true;
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
