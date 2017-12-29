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

import com.android.builder.core.AndroidBuilder;
import com.android.builder.packaging.SigningException;
import com.android.builder.packaging.ZipAbortException;
import com.android.ide.common.signing.KeytoolException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.nbandroid.netbeans.gradle.apk.ApkDataObject;
import org.nbandroid.netbeans.gradle.v2.apk.sign.keystore.KeystoreSelector;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
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
public class SignApkAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }
        FileObject fo = activatedNodes[0].getLookup().lookup(FileObject.class);
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner != null) {
            KeystoreSelector selector = new KeystoreSelector(owner, fo);
            DialogDescriptor dd = new DialogDescriptor(selector, "Generate Signed APK", true, selector);
            selector.setDescriptor(dd);
            Object notify = DialogDisplayer.getDefault().notify(dd);
            if (DialogDescriptor.OK_OPTION.equals(notify)) {
                try {
                    String name = fo.getName();
                    if (name.contains("-unsigned")) {
                        name = name.replace("-unsigned", "-signed");
                    } else {
                        name = name + "-signed";
                    }
                    FileObject out = owner.getProjectDirectory().createData(name, "apk");
                    AndroidBuilder.signApk(FileUtil.toFile(fo), selector, FileUtil.toFile(out));
                } catch (KeytoolException | SigningException | NoSuchAlgorithmException | ZipAbortException | com.android.builder.signing.SigningException | IOException ex) {
                    Exceptions.printStackTrace(ex);
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
        ApkDataObject.SignInfo signInfo = node.getLookup().lookup(ApkDataObject.SignInfo.class);
        if (signInfo == null) {
            return false;
        } else if (signInfo == ApkDataObject.SignInfo.SIGNED_V1 || signInfo == ApkDataObject.SignInfo.SIGNED_V2 || signInfo == ApkDataObject.SignInfo.SIGNED_V1V2) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "Sign APK";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
