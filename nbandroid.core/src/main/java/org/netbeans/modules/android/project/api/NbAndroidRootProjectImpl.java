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
package org.netbeans.modules.android.project.api;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nbandroid.gradle.spi.ModelRefresh;
import nbandroid.gradle.spi.RootGoalsNavigatorHint;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.android.project.actions.RootProjectActionProvider;
import static org.netbeans.modules.android.project.api.NbAndroidProject.RP;
import org.netbeans.modules.android.project.api.nodes.MultiNodeFactory;
import org.netbeans.modules.android.project.api.nodes.MultiNodeFactoryProvider;
import org.netbeans.modules.android.project.api.nodes.NodeFactory;
import org.netbeans.modules.android.project.properties.AndroidRootCustomizerProvider;
import org.netbeans.modules.android.project.properties.actions.ConfigurationsProjectAction;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author arsi
 */
public class NbAndroidRootProjectImpl extends NbAndroidProject {

    @StaticResource()
    public static final String PROJECT_ICON = "org/netbeans/modules/android/api/root_project.png";
     private final ConfigurationsProjectAction configurationsProjectAction ;

    public NbAndroidRootProjectImpl(FileObject projectDirectory, ProjectState ps) {
        super(projectDirectory, ps);
        ic.add((ProjectInformation) new Info());
        configurationsProjectAction = new ConfigurationsProjectAction(this);
    }

    @Override
    protected void registerLookup() {
        ic.add(new NbAndroidProjectConfigurationProvider(auxiliaryProperties));
        ic.add(new AndroidRootCustomizerProvider(this));
    }

    @Override
    protected LogicalViewProvider getLogicalViewProvider() {
        return new CustomerProjectLogicalView();
    }

    @Override
    protected ActionProvider getProjectActionProvider() {
        return new RootProjectActionProvider(this);
    }

    @Override
    protected Class[] getGradleModels() {
        return new Class[]{GradleBuild.class};
    }

    @Override
    public ModelRefresh getModelRefresh() {
        return new ModelRefresh() {
            @Override
            public void refreshModels() {
                RP.execute(NbAndroidRootProjectImpl.this);
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        Enumeration<? extends FileObject> children = getProjectDirectory().getChildren(false);
                        while (children.hasMoreElements()) {
                            FileObject fo = children.nextElement();
                            if (fo.isFolder()) {
                                Project owner = FileOwnerQuery.getOwner(fo);
                                if (owner instanceof NbAndroidProjectImpl) {
                                    RP.execute((NbAndroidProjectImpl) owner);
                                }
                            }
                        }
                    }
                });
            }
        };
    }

    public final class CustomerProjectLogicalView implements LogicalViewProvider {

        @Override
        public Node createLogicalView() {
            FileObject projectDirectory = NbAndroidRootProjectImpl.this.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            try {
                return new ProjectNode(nodeOfProjectFolder);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return new AbstractNode(Children.LEAF);
            }
        }

        @Override
        public Node findPath(Node node, Object o) {
            return null;
        }

    }

    private final class ProjectNode extends FilterNode {

        public ProjectNode(Node node)
                throws DataObjectNotFoundException {
            super(node,
                    new ProjectNodes(),
                    new ProxyLookup(
                            new Lookup[]{
                                Lookups.singleton(NbAndroidRootProjectImpl.this),
                                node.getLookup(), Lookups.singleton(new RootGoalsNavigatorHint()),
                                lookupModels
                            }));
        }

        @Override
        public Action[] getActions(boolean arg0) {
            return new Action[]{
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, "Clean and Build", null),
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, "Clean", null),
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, "Buld", null),
                configurationsProjectAction,
                CommonProjectActions.setAsMainProjectAction(),
                CommonProjectActions.customizeProjectAction(),
                CommonProjectActions.closeProjectAction()
            };
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(PROJECT_ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return NbAndroidRootProjectImpl.this.getProjectDirectory().getName();
        }

    }

    public class ProjectNodes extends Children.Keys<Node> implements ChangeListener {

        private final List<MultiNodeFactory> multiNodeFactories = new ArrayList<>();
        private final List<Node> staticNodes = new ArrayList<>();

        public ProjectNodes() {
            super(true);
            List<MultiNodeFactoryProvider> factoryProviders = MultiNodeFactoryProvider.findAllForRoot();
            for (MultiNodeFactoryProvider factoryProvider : factoryProviders) {
                MultiNodeFactory createMultiNodeFactory = factoryProvider.createMultiNodeFactory(NbAndroidRootProjectImpl.this);
                multiNodeFactories.add(createMultiNodeFactory);
                createMultiNodeFactory.addChangeListener(this);
            }
            List<NodeFactory> findAll = NodeFactory.findAllForRoot();
            for (int i = 0; i < findAll.size(); i++) {
                NodeFactory factory = findAll.get(i);
                Node node = factory.createNode(NbAndroidRootProjectImpl.this);
                if (node != null) {
                    staticNodes.add(node);
                }
            }
            refreshNodes();
        }

        private void refreshNodes() {

            List<Node> childrens = new ArrayList<>();
            for (MultiNodeFactory multiNodeFactory : multiNodeFactories) {
                childrens.addAll(multiNodeFactory.createNodes());
            }
            childrens.addAll(staticNodes);

            setKeys(childrens);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[]{key};
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refreshNodes();
        }

    }

    public final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(PROJECT_ICON));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return NbAndroidRootProjectImpl.this;
        }

    }

}
