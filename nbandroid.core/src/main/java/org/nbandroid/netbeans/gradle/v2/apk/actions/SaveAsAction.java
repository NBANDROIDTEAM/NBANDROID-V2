/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.apk.actions;

import java.awt.Component;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
public class SaveAsAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo != null) {
            FileChooserBuilder builder = new FileChooserBuilder(SaveAsAction.class);
            builder.setDirectoriesOnly(false);
            builder.setApproveText("Save");
            builder.setControlButtonsAreShown(true);
            builder.setTitle("Save As...");
            builder.setFilesOnly(true);
            builder.setFileFilter(new FileNameExtensionFilter(fo.getExt(), fo.getExt()));
            JFileChooser chooser = builder.createFileChooser();
            chooser.setSelectedFile(new File(fo.getNameExt()));
            int resp = chooser.showSaveDialog(findDialogParent());
            if (JFileChooser.APPROVE_OPTION == resp) {
                File saveFile = chooser.getSelectedFile();
                if (saveFile != null) {
                    try {
                        saveFile.getParentFile().mkdirs();
                        FileObject dfo = FileUtil.toFileObject(saveFile.getParentFile());
                        if (dfo == null) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to Save file!", NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(nd);
                            return;
                        }
                        if (saveFile.exists()) {
                            saveFile.delete();
                        }
                        fo.copy(dfo, saveFile.getName(), "");
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Node node = activatedNodes[0];
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo == null) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "Save As...";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    private Component findDialogParent() {
        Component parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (parent == null) {
            parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }
        if (parent == null) {
            Frame[] f = Frame.getFrames();
            parent = f.length == 0 ? null : f[f.length - 1];
        }
        return parent;
    }

}
