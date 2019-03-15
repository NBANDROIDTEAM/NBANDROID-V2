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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = MultiNodeFactoryProvider.class, path = "Android/Project/ProjectFiles", position = 1000)
public class GradleRootProjectFilesMultiNodeFactoryProvider implements MultiNodeFactoryProvider {

    @Override
    public MultiNodeFactory createMultiNodeFactory(Project p) {
        return new GradleRootProjectNodeFactory(p);
    }

    public static class GradleRootProjectNodeFactory implements MultiNodeFactory, FileChangeListener {

        private final Project project;
        private final ChangeSupport cs = new ChangeSupport(this);
        private final FileObject root;

        public GradleRootProjectNodeFactory(Project project) {
            this.project = project;
            root = project.getProjectDirectory().getParent();
            if (root.getFileObject("build", "gradle") != null) {
                project.getProjectDirectory().addFileChangeListener(WeakListeners.create(FileChangeListener.class, this, project.getProjectDirectory()));
            }

        }

        @Override
        public List<Node> createNodes() {
            if (root.getFileObject("build", "gradle") != null) {
                Enumeration<? extends FileObject> fileObjects = root.getChildren(false);
                List<Node> tmp = new ArrayList<>();
                while (fileObjects.hasMoreElements()) {
                    FileObject fileObject = fileObjects.nextElement();
                    if (fileObject != null && fileObject.isData() && "gradle".equals(fileObject.getExt())) {
                        try {
                            DataObject dataObject = DataObject.find(fileObject);
                            if (dataObject != null) {
                                Node nodeDelegate = dataObject.getNodeDelegate();
                                Node filter = new GradleRootProjectNode(nodeDelegate);
                                tmp.add(filter);
                            }
                        } catch (DataObjectNotFoundException ex) {
                        }
                    }
                }
                return tmp;
            }

            return Collections.EMPTY_LIST;
        }

        private class GradleRootProjectNode extends FilterNode {

            public GradleRootProjectNode(Node original) {
                super(original, org.openide.nodes.Children.LEAF);
            }

            @Override
            public String getDisplayName() {
                String name = super.getName();
                name = name + " (parent)";
                return name;
            }

            @Override
            public String getHtmlDisplayName() {
                String s = super.getHtmlDisplayName(); //To change body of generated methods, choose Tools | Templates.
                if (s == null) {
                    s = super.getDisplayName();
                }
                s += "<font color=\"#BDBDBD\">&nbsp;(parent)</font>";
                return s;
            }

        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            cs.fireChange();
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            cs.fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            cs.fireChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

    }

}
