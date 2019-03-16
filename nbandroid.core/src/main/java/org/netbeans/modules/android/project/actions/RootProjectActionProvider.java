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
package org.netbeans.modules.android.project.actions;

import com.google.common.collect.Lists;
import nbandroid.gradle.spi.GradleCommandExecutor;
import nbandroid.gradle.spi.GradleCommandTemplate;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.api.NbAndroidRootProjectImpl;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class RootProjectActionProvider implements ActionProvider, LookupListener {

    private final NbAndroidRootProjectImpl project;
    private final Lookup.Result<GradleBuild> modelLookupResult;

    public RootProjectActionProvider(NbAndroidRootProjectImpl project) {
        this.project = project;
        modelLookupResult = project.getLookup().lookupResult(GradleBuild.class);
        modelLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, project.getLookup()));
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_REBUILD, ActionProvider.COMMAND_BUILD,
            AndroidConstants.COMMAND_BUILD_TEST,
            ActionProvider.COMMAND_CLEAN,};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (project.getLookup().lookup(GradleBuild.class) != null) {
            switch (command) {
                case ActionProvider.COMMAND_REBUILD:
                    callGradleRebuild();
                    break;
                case ActionProvider.COMMAND_BUILD:
                    callGradleBuild();
                    break;
                case ActionProvider.COMMAND_CLEAN:
                    callGradleClean();
                    break;
                case AndroidConstants.COMMAND_BUILD_TEST:
                    callAndroidTest();
                    break;
            }
        }

    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return project.getLookup().lookup(GradleBuild.class) != null;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        //Refresh Build(Run) Toolbar, before model loading Actions are disabled
        //Stupid but I didn't find another solution
        Project mainProject = OpenProjectList.getDefault().getMainProject();
        if (project.equals(mainProject)) {
            OpenProjectList.getDefault().setMainProject(project);
        }
    }

    private void callGradleRebuild() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
        if (executor != null) {
            GradleCommandTemplate.Builder builder = new GradleCommandTemplate.Builder("Clean and Build project", Lists.newArrayList("clean", "assemble"));
            executor.executeCommand(builder.create());
        }
    }

    private void callGradleBuild() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
        if (executor != null) {
            GradleCommandTemplate.Builder builder = new GradleCommandTemplate.Builder("Build project", Lists.newArrayList("assemble"));
            executor.executeCommand(builder.create());
        }
    }

    private void callGradleClean() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
        if (executor != null) {
            GradleCommandTemplate.Builder builder = new GradleCommandTemplate.Builder("Clean project", Lists.newArrayList("clean"));
            executor.executeCommand(builder.create());
        }
    }

    private void callAndroidTest() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
    }

}
