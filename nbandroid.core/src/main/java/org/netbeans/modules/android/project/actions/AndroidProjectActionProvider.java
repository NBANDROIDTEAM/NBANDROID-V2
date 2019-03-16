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

import com.android.builder.model.AndroidProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.api.NbAndroidProjectImpl;
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
public class AndroidProjectActionProvider implements ActionProvider, LookupListener {

    private final NbAndroidProjectImpl project;
    private final Lookup.Result<AndroidProject> modelLookupResult;

    public AndroidProjectActionProvider(NbAndroidProjectImpl project) {
        this.project = project;
        modelLookupResult = project.getLookup().lookupResult(AndroidProject.class);
        modelLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, project.getLookup()));
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_REBUILD, ActionProvider.COMMAND_BUILD,
            AndroidConstants.COMMAND_BUILD_TEST,
            ActionProvider.COMMAND_CLEAN,
            ActionProvider.COMMAND_RUN,
            ActionProvider.COMMAND_TEST,
            ActionProvider.COMMAND_DEBUG,};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (project.getLookup().lookup(AndroidProject.class) == null) {
            return false;
        }
        return true;
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

}
