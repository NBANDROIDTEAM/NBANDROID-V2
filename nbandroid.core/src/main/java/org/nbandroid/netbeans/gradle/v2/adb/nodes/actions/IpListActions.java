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

import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.nbandroid.netbeans.gradle.v2.adb.AdbTools;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.DevicesNode;
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
        category = "ADB/MobileDevice",
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.IpListActions"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/MobileDevice", position = 9990),
    @ActionReference(path = "Android/ADB/EmulatorDevice", position = 9990),})
public class IpListActions extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class);
            if (holder != null) {
                List<AdbTools.IpRecord> deviceIps = AdbTools.getDeviceIps(holder.getUsb(), true);
                JTable table = new JTable(new DeviceIpListTablemodel(deviceIps));
                JScrollPane pane = new JScrollPane(table);
                NotifyDescriptor nd = new NotifyDescriptor.Message(pane, NotifyDescriptor.INFORMATION_MESSAGE);
                nd.setTitle(holder.getSerialNumber() + " IP list");
                DialogDisplayer.getDefault().notifyLater(nd);
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
        return "Show device IPs";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
