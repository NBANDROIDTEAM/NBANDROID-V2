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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.api.ui.AndroidResourceNode;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = MultiNodeFactoryProvider.class, path = "Android/Project/NodeFactory", position = 300)
public class ResNodeFactoryProvider implements MultiNodeFactoryProvider {

    @Override
    public MultiNodeFactory createMultiNodeFactory(Project p) {
        return new ResNodeFactory(p);
    }

    public static class ResNodeFactory implements MultiNodeFactory, ChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final Project p;
        private final Sources sources;
        private final Map<DataObject, Node> cache = new WeakHashMap<>();

        public ResNodeFactory(Project p) {
            this.p = p;
            sources = ProjectUtils.getSources(p);
            sources.addChangeListener(WeakListeners.change(this, sources));
        }


        @Override
        public List<Node> createNodes() {
            List<Node> tmp = new ArrayList<>();
            SourceGroup[] sourceGroups = sources.getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES);
            for (SourceGroup sourceGroup : sourceGroups) {
                tmp.add(createNode(p, sourceGroup));
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
        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }

        public Node createNode(Project p, SourceGroup sg) {
            try {
                DataObject dobj = DataObject.find(sg.getRootFolder());
                Node node = cache.get(dobj);
                if (node == null) {
                    node = new AndroidResourceNode(dobj.getNodeDelegate(), p, sg.getDisplayName());
                    cache.put(dobj, node);
                }
                return node;
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

    }

}
