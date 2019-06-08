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
package org.nbandroid.netbeans.gradle.v2.nodes.actions;

import java.io.File;
import org.nbandroid.netbeans.gradle.v2.maven.ArtifactData;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "NbAndroid/Dependency",
        id = "org.nbandroid.netbeans.gradle.v2.nodes.actions.ViewPomAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class ViewPomAction extends NodeAction{

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            ArtifactData data = node.getLookup().lookup(ArtifactData.class);
            if(data!=null && data.getPomPath()!=null){
                FileObject fo = FileUtil.toFileObject(new File(data.getPomPath()));
                if(fo!=null){
                    try {
                        DataObject dob = DataObject.find(fo);
                        OpenCookie open = dob.getLookup().lookup(OpenCookie.class);
                        if(open!=null){
                            open.open();
                        }
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if(activatedNodes.length==0){
            return false;
        }
        for (Node node : activatedNodes) {
            ArtifactData data = node.getLookup().lookup(ArtifactData.class);
            if(data==null){
                return false;
            }else if(data.getPomPath()==null){
                return false;
            }else if(!new File(data.getPomPath()).exists()){
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "View POM";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
