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
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = MultiNodeFactoryProvider.class, path = "Android/Project/ProjectFiles", position = 500)
public class GradleFilesMultiNodeFactoryProvider implements MultiNodeFactoryProvider {

    @Override
    public MultiNodeFactory createMultiNodeFactory(Project p) {
        return new GradleNodeFactory(p);
    }

    public static class GradleNodeFactory implements MultiNodeFactory, FileChangeListener {

        private final Project project;
        private final ChangeSupport cs = new ChangeSupport(this);

        public GradleNodeFactory(Project project) {
            this.project = project;
            project.getProjectDirectory().addRecursiveListener(WeakListeners.create(FileChangeListener.class, this, project.getProjectDirectory()));

        }

        @Override
        public List<Node> createNodes() {
            Enumeration<? extends FileObject> fileObjects = project.getProjectDirectory().getChildren(true);
            List<Node> tmp = new ArrayList<>();
            while (fileObjects.hasMoreElements()) {
                FileObject fileObject = fileObjects.nextElement();
                if (fileObject != null && fileObject.isData() && "gradle".equals(fileObject.getExt())) {
                    try {
                        DataObject dataObject = DataObject.find(fileObject);
                        if (dataObject != null) {

                            tmp.add(dataObject.getNodeDelegate());
                        }
                    } catch (DataObjectNotFoundException ex) {
                    }
                }

            }

            return tmp;
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
