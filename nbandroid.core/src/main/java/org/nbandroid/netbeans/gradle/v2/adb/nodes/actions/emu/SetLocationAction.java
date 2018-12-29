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

package org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.emu;

import org.nbandroid.netbeans.gradle.v2.adb.EmulatorControlSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "ADB/EmulatorDevice",
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.emu.SetLocationAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/EmulatorDevice", position = 1230, separatorAfter = 1290),})
public class SetLocationAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        EmulatorControlSupport emulatorControl = activatedNodes[0].getLookup().lookup(EmulatorControlSupport.class);
        if (emulatorControl != null) {
            GpsPanel gpsPanel = new GpsPanel();
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(gpsPanel, "Set GPS Location", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
            Object notify = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.OK_OPTION.equals(notify)) {
                String ok = emulatorControl.getConsole().sendLocation(gpsPanel.getLo(), gpsPanel.getLa(), gpsPanel.getAl());
                if (ok != null) {
                    NotifyDescriptor nd1 = new NotifyDescriptor.Message(ok, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd1);
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        return activatedNodes[0].getLookup().lookup(EmulatorControlSupport.class) != null;
    }

    @Override
    public String getName() {
        return "Set GPS Location";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
