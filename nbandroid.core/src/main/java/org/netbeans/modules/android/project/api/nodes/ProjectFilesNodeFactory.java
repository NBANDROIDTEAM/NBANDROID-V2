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
package org.netbeans.modules.android.project.api.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = NodeFactory.class, path = "Android/Project/NodeFactory", position = 20000)
public class ProjectFilesNodeFactory implements NodeFactory {

    @StaticResource
    private static final String ICON_IMPORTANT = "org/netbeans/modules/android/project/api/nodes/config-badge.gif";

    @Override
    public Node createNode(Project p) {
        return new ProjectFilesNode(p);
    }

    public static class ProjectFilesNode extends AbstractNode {

        @NbBundle.Messages("LBL_ProjectFiles=Project Files")
        public ProjectFilesNode(Project p) {
            super(new ProjectFilesChildFactory2(p), p.getLookup());
            setDisplayName(Bundle.LBL_ProjectFiles());
        }

        private Image getIcon(boolean opened) {
            Image badge = ImageUtilities.loadImage(ICON_IMPORTANT, true); //NOI18N
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

    public static class ProjectFilesChildFactory2 extends Children.Keys<Node> implements ChangeListener {

        private final Project project;
        private final List<MultiNodeFactory> factories = new ArrayList<>();

        public ProjectFilesChildFactory2(Project project) {
            this.project = project;
            List<MultiNodeFactoryProvider> providers = MultiNodeFactoryProvider.findAll("ProjectFiles");
            for (MultiNodeFactoryProvider provider : providers) {
                MultiNodeFactory multiNodeFactory = provider.createMultiNodeFactory(project);
                factories.add(multiNodeFactory);
                multiNodeFactory.addChangeListener(this);
            }
            stateChanged(null);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[]{key};
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            List<Node> toPopulate = new ArrayList<>();
            List<NodeFactory> findAll = NodeFactory.findAll("ProjectFiles");
            for (NodeFactory nodeFactory : findAll) {
                Node node = nodeFactory.createNode(project);
                if (node != null) {
                    toPopulate.add(node);
                }
            }
            for (MultiNodeFactory factory : factories) {
                toPopulate.addAll(factory.createNodes());
            }
            setKeys(toPopulate);
        }

    }

}
