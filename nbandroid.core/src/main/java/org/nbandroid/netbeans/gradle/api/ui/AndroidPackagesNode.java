/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nbandroid.netbeans.gradle.api.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.nodes.FilterNode;

/**
 * Yet another cool filter node just to add properties action
 */
public class AndroidPackagesNode extends FilterNode {

    private String nodeName;
    private Project project;
    Action[] actions;

    public AndroidPackagesNode(SourceGroup sourceGroup, Project project) {
        super(PackageView.createPackageView(sourceGroup));
        this.project = project;
        this.nodeName = "Sources";
    }

    @Override
    public Action[] getActions(boolean context) {
        if (!context) {
            if (actions == null) {
                Action superActions[] = super.getActions(context);
                actions = new Action[superActions.length + 2];
                System.arraycopy(superActions, 0, actions, 0, superActions.length);
                actions[superActions.length] = null;
                actions[superActions.length + 1] = new PreselectPropertiesAction(project, nodeName);
            }
            return actions;
        } else {
            return super.getActions(context);
        }
    }

    /**
     * The special properties action
     */
    static class PreselectPropertiesAction extends AbstractAction {

        private final Project project;
        private final String nodeName;
        private final String panelName;

        public PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
        }

        public PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super("Properties");
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // J2SECustomizerProvider cp = (J2SECustomizerProvider) project.getLookup().lookup(J2SECustomizerProvider.class);
//            CustomizerProviderImpl cp = project.getLookup().lookup(CustomizerProviderImpl.class);
//            if (cp != null) {
//                cp.showCustomizer(nodeName, panelName);
//            }

        }
    }
}
