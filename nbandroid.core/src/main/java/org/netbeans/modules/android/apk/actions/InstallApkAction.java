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
package org.netbeans.modules.android.apk.actions;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.sdklib.SdkVersionInfo;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.android.project.launch.actions.SelectDeviceAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.xml.XMLUtil;

/**
 *
 * @author arsi
 */
public class InstallApkAction extends NodeAction implements Presenter.Menu, Presenter.Popup{

    private final JMenu menu = new JMenu("Install..");
    
    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Node node = activatedNodes[0];
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo == null || !"apk".equalsIgnoreCase(fo.getExt())) {
            return false;
        }
        refresh(activatedNodes);
        return true;
    }

    @Override
    public String getName() {
        return "Install..";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
     @Override
    public JMenuItem getMenuPresenter() {
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return menu;
    }

    private void refresh(Node[] activatedNodes) {
        menu.removeAll();
        AndroidDebugBridge debugBridge = AndroidSdkProvider.getAdb();
        if(debugBridge!=null){
            for (IDevice device : debugBridge.getDevices()) {
                if (device.isOnline()) {
                    menu.add(new MenuItemWithDevice(device,activatedNodes[0]));
                }
            }
        }
    }
    
     private class MenuItemWithDevice extends JMenuItem implements ActionListener {

         private final IDevice device;
         private final Node node;

        public MenuItemWithDevice(IDevice device,Node node) throws HeadlessException {
            this.device = device;
            this.node=node;
            setText(getHtmlDisplayName());
            if (!device.isEmulator()) {
                setIcon(SelectDeviceAction.PHONE_CENTER_ICON);
            }else{
                setIcon(SelectDeviceAction.EMULATOR_ICON);
            }
            addActionListener(this);
        }
         
         
        @Override
        public void actionPerformed(ActionEvent e) {
            Runnable runnable = new Runnable() {
                public void run() {
                    FileObject fo = node.getLookup().lookup(FileObject.class);
                    if (fo != null ) {
                        try {
                            device.installPackage(fo.getPath(), true);
                        } catch (InstallException ex) {
                            String message = ex.getMessage();
                            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(nd);
                        }
                    }
                }
            };
            RequestProcessor.getDefault().execute(runnable);
        }
        
        private String getHtmlDisplayName() {
            String devName = null;
            if (device.isEmulator()) {
                devName = device.getAvdName().replace("_", " ");
            } else {
                devName = device.getProperty("ro.product.display");
            }
            if (devName == null) {
                devName = device.getProperty("ro.product.name");
            }
            try {
                devName = XMLUtil.toElementContent(devName);
            } catch (CharConversionException ex) {
            }
            String androidVersion = SdkVersionInfo.getVersionWithCodename(device.getVersion());
            try {
                androidVersion = XMLUtil.toElementContent(androidVersion);
            } catch (CharConversionException ex) {
            }
            String serialNumber = device.getSerialNumber();
            try {
                serialNumber = XMLUtil.toElementContent(serialNumber);
            } catch (CharConversionException ex) {
            }

            return "<html>"
                    + "<b>"
                    + serialNumber
                    + "  "
                    + "<font color=\"#0B610B\">"
                    + devName
                    + "</font>"
                    + "</b>"
                    + "  "
                    + "<i>"
                    + androidVersion
                    + "</i>"
                    + "</html>";
        }
         
     }

}
