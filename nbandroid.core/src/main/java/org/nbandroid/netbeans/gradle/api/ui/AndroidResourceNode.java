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
package org.nbandroid.netbeans.gradle.api.ui;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author radim
 */
public class AndroidResourceNode extends FilterNode {

    private final String nodeName;
    private final Project project;
    Action[] actions;

    public AndroidResourceNode(Node delegate, Project project, String nodeName) {
        super(delegate, new ResourceChildren(delegate, project));
        this.nodeName = nodeName;
        this.project = project;
    }

    @Override
    public String getDisplayName() {
        return nodeName;
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
    }

    private static class ResourceChildren extends FilterNode.Children {

        private final Project project;

        public ResourceChildren(Node or, Project project) {
            super(or);
            this.project = project;
        }

        @Override
        protected Node[] createNodes(Node key) {
            return Iterators.toArray(
                    Iterators.transform(
                            Iterators.forArray(super.createNodes(key)),
                            new Function<Node, Node>() {
                        @Override
                        public Node apply(Node input) {
                            return new NotRootResourceFilterNode(input, project);
                        }
                    }),
                    Node.class);
        }
    }

    private static class NotRootResourceFilterNode extends FilterNode {

        private final Project project;
        Action[] actions;

        public NotRootResourceFilterNode(Node delegate, Project project) {
            super(delegate, new ResourceChildren(delegate, project));
            this.project = project;
        }

        @Override
        public Action[] getActions(boolean context) {
            return super.getActions(context);
        }

    }
}
