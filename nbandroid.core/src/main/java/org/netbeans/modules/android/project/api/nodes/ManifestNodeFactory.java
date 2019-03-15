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

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.api.AndroidManifestSource;
import org.nbandroid.netbeans.gradle.api.ui.AndroidResourceNode;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = NodeFactory.class, path = "Android/Project/importantFiles", position = 100)
public class ManifestNodeFactory implements NodeFactory {

    @Override
    public Node createNode(Project p) {
        // TODO there can be more manifests
        AndroidManifestSource ams = p.getLookup().lookup(AndroidManifestSource.class);
        return new ManifestNode(ams, Nodes.createWaitNode(), p, AndroidConstants.ANDROID_MANIFEST_XML);

    }

    private static class ManifestNode extends AndroidResourceNode implements ChangeListener {

        private final AndroidManifestSource ams;

        public ManifestNode(AndroidManifestSource ams, Node delegate, Project project, String nodeName) {
            super(delegate, project, nodeName);
            this.ams = ams;
            ams.addChangeListener(WeakListeners.change(this, ams));
            stateChanged(null);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Node node = Nodes.createWaitNode();
            FileObject manifestFO = ams.get();
            if (manifestFO != null) {
                try {
                    node = DataObject.find(manifestFO).getNodeDelegate();
                } catch (Exception ex) {
                }
            }
            changeOriginal(node, true);
        }


        @Override
        public String getHtmlDisplayName() {
            FileObject fo = ams.get();
            if (fo != null) {
                Set<FileObject> files = new HashSet<FileObject>();
                files.add(fo);
                try {
                    if (super.getDisplayName() != null) {
                        return fo.getFileSystem().getDecorator().annotateNameHtml(super.getDisplayName(), files);
                    }
                } catch (FileStateInvalidException ex) {
                }
            }
            return super.getHtmlDisplayName(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getDisplayName() {
            FileObject fo = ams.get();
            if (fo != null) {
                Set<FileObject> files = new HashSet<FileObject>();
                files.add(fo);
                try {
                    if (super.getDisplayName() != null) {
                        return fo.getFileSystem().getDecorator().annotateName(super.getDisplayName(), files);
                    }
                } catch (FileStateInvalidException ex) {
                }
            }
            return super.getDisplayName(); //To change body of generated methods, choose Tools | Templates.
        }




    }
}
