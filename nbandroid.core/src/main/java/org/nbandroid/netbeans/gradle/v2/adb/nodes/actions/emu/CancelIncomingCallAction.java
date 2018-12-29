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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.nbandroid.netbeans.gradle.v2.adb.EmulatorControlSupport;
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
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.emu.CancelIncomingCallAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/EmulatorDevice", position = 1210),})
public class CancelIncomingCallAction extends NodeAction {

    private static final Map<String, String> calledNumbers = new ConcurrentHashMap<>();

    public static final void addCalledNumber(String serial, String no) {
        calledNumbers.put(serial, no);
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        EmulatorControlSupport emulatorControl = activatedNodes[0].getLookup().lookup(EmulatorControlSupport.class);
        if (emulatorControl != null) {
            String no = calledNumbers.remove(emulatorControl.getDevice().getSerialNumber());
            if (no != null) {
                emulatorControl.getConsole().cancelCall(no);
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
        return "Cancel incoming call";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
