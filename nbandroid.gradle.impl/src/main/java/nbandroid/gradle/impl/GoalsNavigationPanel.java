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
package nbandroid.gradle.impl;

import java.util.Collection;
import javax.swing.JComponent;
import nbandroid.gradle.spi.GradleUserTaskProvider;
import nbandroid.gradle.tooling.AndroidProjectInfo;
import org.netbeans.api.project.Project;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 *
 * @author arsi
 */
@NavigatorPanel.Registrations({
    @NavigatorPanel.Registration(mimeType = "application/gradle-root-project", position = 250, displayName = "Gradle navigator")
})
public class GoalsNavigationPanel implements NavigatorPanel {

    private GoalsPanel component;

    protected Lookup.Result<AndroidProjectInfo> selection;
    protected Lookup.Result<GradleUserTaskProvider> selectionUserTasks;

    protected final LookupListener selectionListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            if (selection == null) {
                return;
            }
            GradleUserTaskProvider gradleUserTaskProvider = null;
            if (selectionUserTasks != null) {
                Collection<? extends GradleUserTaskProvider> allInstances = selectionUserTasks.allInstances();
                if (!allInstances.isEmpty()) {
                    gradleUserTaskProvider = allInstances.iterator().next();
                }
            }

            navigate(selection.allInstances(), gradleUserTaskProvider);
        }
    };

    @Override
    public String getDisplayName() {
        return "Gradle navigator";
    }

    private GoalsPanel getNavigatorUI() {
        if (component == null) {
            component = new GoalsPanel();
        }
        return component;
    }

    @NbBundle.Messages("GOALS_HINT=View what goals are available based on content of the build.gradle")
    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(GoalsNavigationPanel.class, "GOALS_HINT");
    }

    @Override
    public JComponent getComponent() {
        return getNavigatorUI();
    }

    @Override
    public void panelActivated(Lookup context) {
        getNavigatorUI().showWaitNode();
        selection = context.lookupResult(AndroidProjectInfo.class);
        selection.addLookupListener(selectionListener);
        Project project = context.lookup(Project.class);
        if (project != null) {
            selectionUserTasks = project.getLookup().lookupResult(GradleUserTaskProvider.class);
            selectionUserTasks.addLookupListener(selectionListener);
        }
        selectionListener.resultChanged(null);
    }

    @Override
    public void panelDeactivated() {
        getNavigatorUI().showWaitNode();
        if (selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }
        if (selectionUserTasks != null) {
            selectionUserTasks.removeLookupListener(selectionListener);
            selectionUserTasks = null;
        }
        getNavigatorUI().release();
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public void navigate(Collection<? extends AndroidProjectInfo> selectedFiles, GradleUserTaskProvider gradleUserTaskProvider) {
        if (selectedFiles.size() == 1) {
            AndroidProjectInfo d = (AndroidProjectInfo) selectedFiles.iterator().next();
            getNavigatorUI().navigate(d, gradleUserTaskProvider);
        } else {
            getNavigatorUI().release();
        }
    }

}
