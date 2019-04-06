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
package org.netbeans.modules.android.project.tasks.actions;

import com.android.builder.model.AndroidProject;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nbandroid.gradle.spi.GradleCommandExecutor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.tasks.UserTask;
import org.netbeans.modules.android.project.tasks.UserTasksConfiguration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "Android/Projects/Project",
        id = "org.netbeans.modules.android.project.tasks.actions.UserTasksAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)

@ActionReference(path = "Android/Projects/Project", position = 48, separatorBefore = 47)
public class UserTasksAction extends AbstractAction implements ContextAwareAction, Presenter.Menu, Presenter.Popup, ChangeListener {

    private JMenu menu = new JMenu("Custom Tasks");
    private UserTasksConfiguration userTasksConfiguration;
    private final Project project;

    public UserTasksAction() {
        this(Utilities.actionsGlobalContext());
    }

    public UserTasksAction(Lookup lkp) {
        super("Custom Tasks");
        project = lkp.lookup(Project.class);
        if (project != null) {
            userTasksConfiguration = project.getLookup().lookup(UserTasksConfiguration.class);
            if (userTasksConfiguration != null) {
                userTasksConfiguration.addChangeListener(WeakListeners.change(this, userTasksConfiguration));
            }
            stateChanged(null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new UserTasksAction(actionContext);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return menu;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        menu.removeAll();
        List<UserTask> userTasks = userTasksConfiguration.getUserTasks();
        for (UserTask userTask : userTasks) {
            JMenuItem menuItem = new JMenuItem(new ExecuteUserAction(userTask));
            menuItem.setToolTipText(getHtmlDescription(userTask));
            menu.add(menuItem);
        }
    }

    private String getHtmlDescription(UserTask userTask) {
        String tmp = "<html><b>" + userTask.getTaskName() + "</b><br>";
        tmp += "Tasks: <b>";
        for (String task : userTask.getTasks()) {
            tmp += task + " ";
        }
        tmp += "</b><br>";
        tmp += "GradleArguments: <b>";
        for (String task : userTask.getGradleArguments()) {
            tmp += task + " ";
        }
        tmp += "</b><br>";
        tmp += "JVM Arguments: <b>";
        for (String task : userTask.getJvmArguments()) {
            tmp += task + " ";
        }
        tmp += "</b><br>";
        tmp += "</html>";
        return tmp;
    }

    private class ExecuteUserAction extends AbstractAction {

        private final UserTask userTask;

        public ExecuteUserAction(UserTask userTask) {
            super(userTask.getTaskName());
            this.userTask = userTask;
        }

        @Override
        public boolean isEnabled() {
            if (project != null) {
                GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
                AndroidProject androidProject = project.getLookup().lookup(AndroidProject.class);
                if (executor != null && androidProject != null) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (project != null) {
                GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
                if (executor != null) {
                    executor.executeCommand(userTask.getCommandTemplate());
                }
            }
        }

    }

    @Override
    public JMenuItem getPopupPresenter() {
        return menu;
    }

}
