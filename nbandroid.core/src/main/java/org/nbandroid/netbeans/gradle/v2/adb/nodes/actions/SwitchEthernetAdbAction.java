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

import com.android.ddmlib.NbAndroidAdbHelper;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.nbandroid.netbeans.gradle.v2.adb.AdbTools;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.DevicesNode;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.MobileDeviceNode;
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
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.SwitchEthernetAdbAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/MobileDevice", position = 2000),})
public class SwitchEthernetAdbAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class);
            if (holder == null || !holder.hasUSB()) {
                continue;
            }
            List<AdbTools.IpRecord> deviceIps = AdbTools.getDeviceIps(holder.getUsb(), false);
            if (deviceIps.isEmpty()) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation("<html>The " + holder.getSerialNumber() + " device does not have an active Ethernet adapter..<br>Please check the Wifi settings..</html>", "Wifi settings", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                AdbTools.openWifiSettings(holder.getUsb());
                Object notify = DialogDisplayer.getDefault().notify(nd);
                if (NotifyDescriptor.OK_OPTION.equals(notify)) {
                    deviceIps = AdbTools.getDeviceIps(holder.getUsb(), false);
                    if (deviceIps.isEmpty()) {
                        NotifyDescriptor nd1 = new NotifyDescriptor.Message("I did not find any active ip address", NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd1);
                        continue;
                    }
                } else {
                    continue;
                }
            }
            String ip;
            if (deviceIps.size() == 1) {
                ip = deviceIps.get(0).getIp();
            } else {
                JTable table = new JTable(new DeviceIpListTablemodel(deviceIps));
                JScrollPane pane = new JScrollPane(table);
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(pane, "Please select Device IP address to use for ADB connection..", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                Object notify = DialogDisplayer.getDefault().notify(nd);
                if (NotifyDescriptor.OK_OPTION.equals(notify)) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        continue;
                    }
                    AdbTools.IpRecord ipRec = deviceIps.get(selectedRow);
                    ip = ipRec.getIp();
                } else {
                    continue;
                }
            }
            holder.setIp(ip.substring(0, ip.indexOf('/')));
            //mark device to connect after ADB/node refresh
            MobileDeviceNode.doConnect.put(holder.getSerialNumber(), holder);
            if (NbAndroidAdbHelper.switchToEthernet(holder.getUsb().getSerialNumber(), ip.substring(0, ip.indexOf('/')), 5555)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }

            }
        }

    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class
            );
            if (holder == null || !holder.hasUSB() || holder.hasEthernet()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "Switch ADB to Ethernet";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.SwitchEthernetAdbAction");
    }

}
