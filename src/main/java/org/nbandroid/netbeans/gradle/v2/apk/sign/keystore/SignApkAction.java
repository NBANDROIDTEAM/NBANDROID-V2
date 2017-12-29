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

package org.nbandroid.netbeans.gradle.v2.apk.sign.keystore;

import org.nbandroid.netbeans.gradle.apk.ApkDataObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
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
            System.out.println("org.nbandroid.netbeans.gradle.v2.apk.sign.keystore.SignApkAction.performAction()");
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
