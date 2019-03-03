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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class ApksFilterNodeChildrens extends Children.Keys<DataObject> implements FileChangeListener {

    private final Project p;
    private static final RequestProcessor RP = new RequestProcessor("Refresh APK Nodes", 1);

    public ApksFilterNodeChildrens(Project p) {
        super(true);
        this.p = p;
        List<FileObject> folders = findApks();

        for (FileObject folder : folders) {
            folder.addRecursiveListener(WeakListeners.create(FileChangeListener.class, this, folder));
        }
    }

    private List<FileObject> findApks() {
        List<DataObject> keys = new ArrayList<>();
        List<FileObject> folders = new ArrayList<>();
        try {
            FileObject release = FileUtil.createFolder(p.getProjectDirectory(), "release");
            folders.add(release);
            FileObject[] childrens = release.getChildren();
            for (FileObject children : childrens) {
                if ("apk".equals(children.getExt())) {
                    keys.add(DataObject.find(children));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            FileObject debug = FileUtil.createFolder(p.getProjectDirectory(), "debug");
            folders.add(debug);
            FileObject[] childrens = debug.getChildren();
            for (FileObject children : childrens) {
                if ("apk".equals(children.getExt())) {
                    keys.add(DataObject.find(children));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        Collections.sort(keys, new Comparator<DataObject>() {
            @Override
            public int compare(DataObject o1, DataObject o2) {
                return o1.getPrimaryFile().getName().compareTo(o2.getPrimaryFile().getName());
            }
        });
        setKeys(keys);
        return folders;
    }

    private void refreshFolders(FileEvent fe) {

        Runnable runnable = new Runnable() {
            public void run() {
                findApks();
            }
        };
        RP.execute(runnable);
    }

    @Override
    protected Node[] createNodes(DataObject key) {
        return new Node[]{new FilterNode(key.getNodeDelegate())};
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        refreshFolders(fe);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        refreshFolders(fe);
    }

    @Override
    public void fileChanged(FileEvent fe) {
        refreshFolders(fe);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        refreshFolders(fe);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        refreshFolders(fe);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

}
