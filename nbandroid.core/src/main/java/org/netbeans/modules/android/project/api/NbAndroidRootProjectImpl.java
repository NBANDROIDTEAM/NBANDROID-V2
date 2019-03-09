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
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import nbandroid.gradle.spi.RootGoalsNavigatorHint;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.android.project.properties.AndroidRootCustomizerProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
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

    public NbAndroidRootProjectImpl(FileObject projectDirectory, ProjectState ps) {
        super(projectDirectory, ps);
        ic.add((ProjectInformation) new Info());
        ic.add(new NbAndroidProjectConfigurationProvider());
        ic.add(new AndroidRootCustomizerProvider(this));
        ic.add(new CustomerProjectLogicalView());
    }

    @Override
    protected Class[] getGradleModels() {
        return new Class[]{GradleBuild.class};
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
                    new FilterProjectChilden(node),
                    new ProxyLookup(
                            new Lookup[]{
                                Lookups.singleton(NbAndroidRootProjectImpl.this),
                                node.getLookup(), Lookups.singleton(new RootGoalsNavigatorHint()),
                                modelLookup
                            }));
        }

        @Override
        public Action[] getActions(boolean arg0) {
            return new Action[]{
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

    public class FilterProjectChilden extends FilterNode.Children {

        private final Node orig;

        public FilterProjectChilden(Node orig) {
            super(orig);
            this.orig = orig;
        }

        @Override
        protected Node[] createNodes(Node key) {
            Node[] nodes = super.createNodes(key);
            List<Node> tmp = new ArrayList<>();
            return tmp.toArray(new Node[tmp.size()]);
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