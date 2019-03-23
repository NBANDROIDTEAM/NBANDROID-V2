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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.android.apk.ApkUtils;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "Android/Projects/Project",
        id = "org.netbeans.modules.android.apk.actions.DebugApkAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)

@ActionReference(path = "Android/Projects/Project", position = 50, separatorBefore = 49)
public class DebugApkAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }
        Project owner = activatedNodes[0].getLookup().lookup(Project.class);
        if (owner != null) {
            List<String> tasks = new ArrayList<>();
            tasks.add("assembleDebug");
            ApkUtils.gradleBuild(owner, "Build Debug APK..", tasks, Collections.EMPTY_LIST, Collections.EMPTY_LIST, true);
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
        return "Build Debug APK..";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
