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
package org.netbeans.modules.android.project.api.nodes.root;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.android.project.api.nodes.NodeFactory;
import org.netbeans.modules.android.project.api.nodes.NodeUtils;
import org.netbeans.modules.project.ui.api.ProjectActionUtils;
import org.netbeans.spi.project.ProjectFactory2;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = NodeFactory.class, path = "Android/RootProject/NodeFactory", position = 1000)
public class ModulesNodeFactory implements NodeFactory {

    private static final @StaticResource
    String MODULES_BADGE = "org/netbeans/modules/android/project/api/nodes/root/modules-badge.png";

    @Override
    public Node createNode(Project p) {
        return new ModulesNode(p);
    }

    public class ModulesNode extends AbstractNode {

        public ModulesNode(Project project) {
            super(new ModuleChildrens(project), project.getLookup());
            setDisplayName("Modules");
        }

        private Image getIcon(boolean opened) {
            Image badge = ImageUtilities.loadImage(MODULES_BADGE, true); //NOI18N
            return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
        }

        @Override
        public Image getIcon(int type) {
            return getIcon(false);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(true);
        }

    }

    public class ModuleChildrens extends Children.Keys<Node> {

        private final Project project;

        public ModuleChildrens(Project project) {
            this.project = project;
            findModules();
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[]{key};
        }

        private void findModules() {
            List<Node> nodes = new ArrayList<>();
            Collection<? extends ProjectFactory2> factory2s = Lookup.getDefault().lookupAll(ProjectFactory2.class);
            Enumeration<? extends FileObject> childrens = project.getProjectDirectory().getChildren(false);
            while (childrens.hasMoreElements()) {
                FileObject fo = childrens.nextElement();
                if (fo.isFolder()) {
                    for (ProjectFactory2 factory2 : factory2s) {
                        ProjectManager.Result result = factory2.isProject2(fo);
                        if (result != null) {
                            nodes.add(new SubProjectNode(result, fo, project));
                            break;
                        }
                    }
                }
            }
            setKeys(nodes);
        }
    }

    private class SubProjectNode extends AbstractNode {

        private final ProjectManager.Result result;
        private final FileObject fo;
        private final Project project;

        public SubProjectNode(ProjectManager.Result result, FileObject fo, Project project) {
            super(Children.LEAF, Lookups.fixed(fo));
            this.result = result;
            this.fo = fo;
            this.project = project;
            if (result.getDisplayName() != null) {
                setDisplayName(result.getDisplayName());
            } else {
                setDisplayName(fo.getName());
            }
        }

        @Override
        public Image getIcon(int type) {
            if (result.getIcon() != null) {
                return ImageUtilities.icon2Image(result.getIcon());
            }
            return super.getIcon(type); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{SystemAction.get(OpenModuleAction.class)};
        }
    }

    public static class OpenModuleAction extends NodeAction {

        @Override
        public String getName() {
            return "Open project";
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            for (Node node : activatedNodes) {
                final FileObject fo = node.getLookup().lookup(FileObject.class);
                if (fo != null) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public @Override
                        void run() {
                            Project projectsArray[] = new Project[]{FileOwnerQuery.getOwner(fo)};
                            OpenProjects.getDefault().open(projectsArray, false, true);
                            RequestProcessor.getDefault().post(new Runnable() {
                                public @Override
                                void run() {
                                    ProjectActionUtils.selectAndExpandProject(projectsArray[0]);
                                }
                            }, 500);
                        }
                    });
                }
            }
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }

}
