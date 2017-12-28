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

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.DevicesNode;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "ADB/MobileDevice",
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.WifiSelectAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/MobileDevice", position = 4000, separatorAfter = 4100, separatorBefore = 3990),})
public class WifiSelectAction extends NodeAction implements Action, Presenter.Menu, Presenter.Popup {

    private final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Use Ethernet connection");

    public WifiSelectAction() {
        menuItem.addActionListener(this);
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length > 1 || activatedNodes.length == 0) {
            return;
        }
        Node activatedNode = activatedNodes[0];
        DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class);
        holder.setUseUsb(!menuItem.isSelected());

    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length > 1 || activatedNodes.length == 0) {
            menuItem.setSelected(false);
            menuItem.setEnabled(false);
            return false;
        }
        Node activatedNode = activatedNodes[0];
        DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class);
        if (holder == null || !holder.hasUSB() || !holder.hasEthernet()) {
            if (holder != null) {
                menuItem.setSelected(holder.hasEthernet());
            } else {
                menuItem.setSelected(false);
            }
            menuItem.setEnabled(false);
            return false;
        }
        menuItem.setEnabled(true);
        menuItem.setSelected(!holder.isUseUsb());
        return true;
    }

    @Override
    public String getName() {
        return "Use Ethernet connection";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return menuItem; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getToolbarPresenter() {
        return menuItem; //To change body of generated methods, choose Tools | Templates.
    }

}
