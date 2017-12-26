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

package org.nbandroid.netbeans.gradle.v2.adb.nodes.actions;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.TimeoutException;
import java.io.IOException;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.DevicesNode;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "ADB/MobileDevice",
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.RebootDeviceAdbAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/MobileDevice", position = 9980),})
public class RebootDeviceAdbAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class
            );
            if (holder != null) {
                try {
                    holder.getMasterDevice().reboot(null);
                } catch (TimeoutException | AdbCommandRejectedException | IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class
            );
            if (holder == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "Reboot the Device";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
