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
package org.netbeans.modules.android.project.launch.actions;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.prefs.AndroidLocation;
import com.android.sdklib.SdkVersionInfo;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.repository.AndroidSdkHandler;
import com.android.utils.NullLogger;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.android.project.api.NbAndroidProjectImpl;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import org.openide.xml.XMLUtil;

/**
 *
 * @author arsi
 */
public class SelectDeviceProjectAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {

    private final JMenu menu = new JMenu("Device");
    private final NbAndroidProjectImpl project;
    ButtonGroup group = new ButtonGroup();

    public SelectDeviceProjectAction(NbAndroidProjectImpl project) {
        this.project = project;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        refresh();
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        refresh();
        return menu;
    }
    
    private void refresh(){
        menu.removeAll();
        AuxiliaryProperties auxiliaryProperties = project.getLookup().lookup(AuxiliaryProperties.class);
        String lastSelectedDevice = auxiliaryProperties.get(SelectDeviceAction.ADB_SELECTED_DEVICE, false);
        String lastSelectedAvd = auxiliaryProperties.get(SelectDeviceAction.ADB_LAST_SELECTED_AVD, false);
        AndroidDebugBridge debugBridge = AndroidSdkProvider.getAdb();
        if (debugBridge == null) {
            menu.add(new JMenuItem("Broken ADB"));
        } else {
            for (IDevice device : debugBridge.getDevices()) {
                if (!device.isEmulator()) {
                    JRadioButtonMenuItemWithDevice item = new JRadioButtonMenuItemWithDevice(device, project);
                    group.add(item);
                    menu.add(item);
                }
            }
            AndroidSdk defaultSdk = AndroidSdkProvider.getDefaultSdk();
            if (defaultSdk != null) {
                AndroidSdkHandler androidSdkHandler = defaultSdk.getAndroidSdkHandler();
                if (androidSdkHandler != null) {
                    try {
                        AvdManager avdManager = com.android.sdklib.internal.avd.AvdManager.getInstance(androidSdkHandler, new NullLogger());
                        AvdInfo[] validAvds = avdManager.getValidAvds();
                        for (int i = 0; i < validAvds.length; i++) {
                            AvdInfo validAvd = validAvds[i];
                            JRadioButtonMenuItemWithEmulatorDevice item = new JRadioButtonMenuItemWithEmulatorDevice(validAvd, project);
                            group.add(item);
                            menu.add(item);
                        }
                    } catch (AndroidLocation.AndroidLocationException ex) {
                    }
                }
            }
            Component[] components = menu.getPopupMenu().getComponents();
            if (lastSelectedDevice != null) {
                boolean ok = false;
                for (Component component : components) {
                    if (component instanceof JRadioButtonMenuItemWithDevice) {
                        String serialNumber = ((JRadioButtonMenuItemWithDevice) component).getDevice().getSerialNumber();
                        serialNumber = "DEVICE." + serialNumber;
                        if (lastSelectedDevice.equals(serialNumber)) {
                            ok = true;
                            ((JRadioButtonMenuItemWithDevice) component).setSelected(true);
                            ((JRadioButtonMenuItemWithDevice) component).actionPerformed(null);
                            break;
                        }
                    } else if (component instanceof JRadioButtonMenuItemWithEmulatorDevice) {
                        String deviceName = ((JRadioButtonMenuItemWithEmulatorDevice) component).getDevice().getDeviceName();
                        deviceName = "AVD." + deviceName;
                        if (lastSelectedDevice.equals(deviceName)) {
                            ok = true;
                            ((JRadioButtonMenuItemWithEmulatorDevice) component).setSelected(true);
                            ((JRadioButtonMenuItemWithEmulatorDevice) component).actionPerformed(null);
                            break;
                        }
                    }
                }
                if (!ok && lastSelectedAvd != null) {
                    //try to find last AVD device
                    for (Component component : components) {
                        if (component instanceof JRadioButtonMenuItemWithEmulatorDevice) {
                            String deviceName = ((JRadioButtonMenuItemWithEmulatorDevice) component).getDevice().getDeviceName();
                            if (lastSelectedAvd.equals(deviceName)) {
                                ok = true;
                                ((JRadioButtonMenuItemWithEmulatorDevice) component).setSelected(true);
                                ((JRadioButtonMenuItemWithEmulatorDevice) component).actionPerformed(null);
                                break;
                            }
                        }
                    }
                }
                if (!ok) {
                    //try to find first real device
                    for (Component component : components) {
                        if (component instanceof JRadioButtonMenuItemWithDevice) {
                            ok = true;
                            ((JRadioButtonMenuItemWithDevice) component).setSelected(true);
                            ((JRadioButtonMenuItemWithDevice) component).actionPerformed(null);
                            break;
                        }
                    }
                }
                if (!ok) {
                    //try to find first AVD device
                    for (Component component : components) {
                        if (component instanceof JRadioButtonMenuItemWithEmulatorDevice) {
                            ((JRadioButtonMenuItemWithEmulatorDevice) component).setSelected(true);
                            ((JRadioButtonMenuItemWithEmulatorDevice) component).actionPerformed(null);
                            break;
                        }
                    }
                }
            } else {
                boolean ok = false;
                //try to find first real device
                for (Component component : components) {
                    if (component instanceof JRadioButtonMenuItemWithDevice) {
                        ok = true;
                        ((JRadioButtonMenuItemWithDevice) component).setSelected(true);
                        ((JRadioButtonMenuItemWithDevice) component).actionPerformed(null);
                        break;
                    }
                }
                if (!ok) {
                    //try to find first AVD device
                    for (Component component : components) {
                        if (component instanceof JRadioButtonMenuItemWithEmulatorDevice) {
                            ((JRadioButtonMenuItemWithEmulatorDevice) component).setSelected(true);
                            ((JRadioButtonMenuItemWithEmulatorDevice) component).actionPerformed(null);
                            break;
                        }
                    }
                }
            }
            final JCheckBoxMenuItem wipeData = new JCheckBoxMenuItem("Wipe data before Start Emulator");
            wipeData.setSelected(SelectDeviceAction.wipeData.isSelected());
            wipeData.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SelectDeviceAction.wipeData.setSelected(wipeData.isSelected());
                    NbPreferences.forModule(SelectDeviceAction.class).putBoolean("WIPE-DATA", wipeData.isSelected());
                }
            });
            menu.add(wipeData);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private class JRadioButtonMenuItemWithDevice extends JRadioButtonMenuItem implements ActionListener {

        private final IDevice device;
        private final NbAndroidProjectImpl project;

        public JRadioButtonMenuItemWithDevice(IDevice device, NbAndroidProjectImpl project) {
            this.device = device;
            this.project = project;
            setText(getHtmlDisplayName());
            setIcon(SelectDeviceAction.PHONE_CENTER_ICON);
            addActionListener(this);
        }

        public IDevice getDevice() {
            return device;
        }

        public NbAndroidProjectImpl getProject() {
            return project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AuxiliaryProperties auxiliaryProperties = project.getLookup().lookup(AuxiliaryProperties.class);
            auxiliaryProperties.put(SelectDeviceAction.ADB_SELECTED_DEVICE, "DEVICE." + device.getSerialNumber(), false);
            SelectDeviceAction selectDeviceAction = SelectDeviceAction.getDefault();
            if (selectDeviceAction != null) {
                selectDeviceAction.resultChanged(null);
            }
        }

        private String getHtmlDisplayName() {
            String devName = device.getProperty("ro.product.display");
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

    private class JRadioButtonMenuItemWithEmulatorDevice extends JRadioButtonMenuItem implements ActionListener {

        private final AvdInfo device;
        private final NbAndroidProjectImpl project;

        public JRadioButtonMenuItemWithEmulatorDevice(AvdInfo device, NbAndroidProjectImpl project) {
            this.device = device;
            this.project = project;
            setText(getHtmlDisplayName());
            addActionListener(this);
            setIcon(SelectDeviceAction.EMULATOR_ICON);
        }

        public AvdInfo getDevice() {
            return device;
        }

        public NbAndroidProjectImpl getProject() {
            return project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AuxiliaryProperties auxiliaryProperties = project.getLookup().lookup(AuxiliaryProperties.class);
            auxiliaryProperties.put(SelectDeviceAction.ADB_SELECTED_DEVICE, "AVD." + device.getDeviceName(), false);
            auxiliaryProperties.put(SelectDeviceAction.ADB_LAST_SELECTED_AVD, device.getDeviceName(), false);
            SelectDeviceAction selectDeviceAction = SelectDeviceAction.getDefault();
            if (selectDeviceAction != null) {
                selectDeviceAction.resultChanged(null);
            }
        }

        private String getHtmlDisplayName() {
            String devName = device.getDeviceName();
            try {
                devName = XMLUtil.toElementContent(devName);
            } catch (CharConversionException ex) {
            }
            String androidVersion = SdkVersionInfo.getVersionWithCodename(device.getSystemImage().getAndroidVersion());
            try {
                androidVersion = XMLUtil.toElementContent(androidVersion);
            } catch (CharConversionException ex) {
            }

            return "<html>"
                    + "<b>"
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
