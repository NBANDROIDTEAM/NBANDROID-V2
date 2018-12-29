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
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.emu.IncomingCallAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/EmulatorDevice", separatorBefore = 1190, position = 1200),})
public class IncomingCallAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        EmulatorControlSupport emulatorControl = activatedNodes[0].getLookup().lookup(EmulatorControlSupport.class);
        if (emulatorControl != null) {
            NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine("Phone number:", "Incoming call", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
            Object notify = DialogDisplayer.getDefault().notify(inputLine);
            if (NotifyDescriptor.OK_OPTION.equals(notify)) {
                String phoneNo = inputLine.getInputText();
                String ok = emulatorControl.getConsole().call(phoneNo);
                if (ok == null) {
                    CancelIncomingCallAction.addCalledNumber(emulatorControl.getDevice().getSerialNumber(), phoneNo);
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ok, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
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
        return "Incoming call";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
