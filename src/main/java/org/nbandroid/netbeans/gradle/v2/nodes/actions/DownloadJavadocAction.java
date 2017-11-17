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
package org.nbandroid.netbeans.gradle.v2.nodes.actions;

import org.nbandroid.netbeans.gradle.v2.maven.ArtifactData;
import org.nbandroid.netbeans.gradle.v2.maven.MavenDownloader;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "NbAndroid/Dependency",
        id = "org.nbandroid.netbeans.gradle.v2.nodes.actions.DownloadJavadocAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class DownloadJavadocAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            ArtifactData data = node.getLookup().lookup(ArtifactData.class);
            if (data != null && !data.isJavadocLocal() && !data.isLocal()) {
                MavenDownloader.downloadJavaDoc(data);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            ArtifactData data = n.getLookup().lookup(ArtifactData.class);
            if (data == null) {
                return false;
            }
            if (data.isJavadocLocal() || data.isLocal() || !data.isDownloadable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "Download javadoc";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

}
