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

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.android.apk.actions.DebugApkAction;
import org.netbeans.modules.android.apk.actions.ReleaseUnsignedApkAction;
import org.netbeans.modules.android.apk.actions.SignApkAction;
import org.nbandroid.netbeans.gradle.v2.ui.IconProvider;
import org.netbeans.api.project.Project;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
public class ApksFilterNode extends AbstractNode {

    public ApksFilterNode(Project p) {
        super(new ApksFilterNodeChildrens(p), Lookups.fixed(p));
    }

    @Override
    public String getDisplayName() {
        return "APKs";
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            SystemAction.get(DebugApkAction.class),
            SystemAction.get(SignApkAction.class),
            SystemAction.get(ReleaseUnsignedApkAction.class),};
    }

    @Override
    public Image getOpenedIcon(int type) {
        return IconProvider.IMG_APKS;
    }

    @Override
    public Image getIcon(int type) {
        return IconProvider.IMG_APKS;
    }

}
