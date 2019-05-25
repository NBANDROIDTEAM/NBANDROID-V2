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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.NbAndroidProjectImpl;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DropDownButtonFactory;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.xml.XMLUtil;

@ActionID(
        category = "Run",
        id = "org.netbeans.modules.android.project.launch.actions.SelectDeviceAction"
)
@ActionRegistration(
        displayName = "Select device", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Toolbars/Build", position = 375)
})
/**
 *
 * @author arsi
 */
public class SelectDeviceAction extends AbstractAction implements Presenter.Toolbar, LookupListener, PropertyChangeListener, AndroidDebugBridge.IDeviceChangeListener {

    private final Lookup.Result<Node> lookupResultNode;
    private final Lookup.Result<DataObject> lookupResultDob;
    public static final JPopupMenu menu = new JPopupMenu();
    ButtonGroup group = new ButtonGroup();
    public static final String ADB_SELECTED_DEVICE = "ADB_SELECTED_DEVICE";
    public static final String ADB_LAST_SELECTED_AVD = "ADB_LAST_SELECTED_AVD";

    @StaticResource
    private static final String PHONE_ICON_RES = "org/netbeans/modules/android/project/resources/phone32.png";
    public static final ImageIcon PHONE_ICON = new ImageIcon(ImageUtilities.loadImage(PHONE_ICON_RES));

    private static final String PHONE_CENTER_ICON_RES = "org/netbeans/modules/android/project/resources/phone24_center.png";
    public static final ImageIcon PHONE_CENTER_ICON = new ImageIcon(ImageUtilities.loadImage(PHONE_CENTER_ICON_RES));

    private static final String EMULATOR_ICON_RES = "org/netbeans/modules/android/project/resources/emulator32.png";
    public static final ImageIcon EMULATOR_ICON = new ImageIcon(ImageUtilities.loadImage(EMULATOR_ICON_RES));

    private static final String BROKEN_ICON_RES = "org/netbeans/modules/android/project/resources/broken_adb.png";
    public static final ImageIcon BROKEN_ICON = new ImageIcon(ImageUtilities.loadImage(BROKEN_ICON_RES));
    private AndroidDebugBridge debugBridge;
    private final JButton toolbarButton = DropDownButtonFactory.createDropDownButton(BROKEN_ICON, menu);

    public SelectDeviceAction() {
        lookupResultNode = Utilities.actionsGlobalContext().lookupResult(Node.class);
        lookupResultNode.addLookupListener(this);
        lookupResultDob = Utilities.actionsGlobalContext().lookupResult(DataObject.class);
        lookupResultDob.addLookupListener(this);
        AndroidSdkProvider sdkProvider = AndroidSdkProvider.getDefault();
        sdkProvider.addPropertyChangeListener(this);
        debugBridge = AndroidSdkProvider.getAdb();
        AndroidDebugBridge.addDeviceChangeListener(this);
        resultChanged(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Component getToolbarPresenter() {
        return toolbarButton;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends Node> allInstances = lookupResultNode.allInstances();
        if (!allInstances.isEmpty()) {
            Node abstractNode = allInstances.iterator().next();
            while (abstractNode != null) {
                NbAndroidProjectImpl project = abstractNode.getLookup().lookup(NbAndroidProjectImpl.class);
                if (project != null) {
                    updateMenu(project);
                    return;
                }
                abstractNode = abstractNode.getParentNode();
            }
        }
        Collection<? extends DataObject> allInstancesDob = lookupResultDob.allInstances();
        if (!allInstancesDob.isEmpty()) {
            Project owner = FileOwnerQuery.getOwner(allInstancesDob.iterator().next().getPrimaryFile());
            if (owner instanceof NbAndroidProjectImpl) {
                updateMenu((NbAndroidProjectImpl) owner);
                return;
            }
        }

        toolbarButton.setVisible(false);
    }

    public void updateMenu(NbAndroidProjectImpl project) {
        toolbarButton.setVisible(true);
        AuxiliaryProperties auxiliaryProperties = project.getLookup().lookup(AuxiliaryProperties.class);
        String lastSelectedDevice = auxiliaryProperties.get(ADB_SELECTED_DEVICE, false);
        String lastSelectedAvd = auxiliaryProperties.get(ADB_LAST_SELECTED_AVD, false);
        if (debugBridge == null) {
            toolbarButton.setToolTipText("Broken ADB!");
            toolbarButton.setIcon(BROKEN_ICON);
        } else {
            menu.removeAll();
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
            Component[] components = menu.getComponents();
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
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        debugBridge = AndroidSdkProvider.getAdb();
        resultChanged(null);
    }

    @Override
    public void deviceConnected(IDevice id) {
        resultChanged(null);
    }

    @Override
    public void deviceDisconnected(IDevice id) {
        resultChanged(null);
    }

    @Override
    public void deviceChanged(IDevice id, int i) {
        resultChanged(null);
    }

    private class JRadioButtonMenuItemWithDevice extends JRadioButtonMenuItem implements ActionListener {

        private final IDevice device;
        private final NbAndroidProjectImpl project;

        public JRadioButtonMenuItemWithDevice(IDevice device, NbAndroidProjectImpl project) {
            this.device = device;
            this.project = project;
            setText(getHtmlDisplayName());
            setIcon(PHONE_CENTER_ICON);
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
            toolbarButton.setToolTipText(getHtmlDisplayName());
            toolbarButton.setIcon(PHONE_CENTER_ICON);
            AuxiliaryProperties auxiliaryProperties = project.getLookup().lookup(AuxiliaryProperties.class);
            auxiliaryProperties.put(ADB_SELECTED_DEVICE, "DEVICE." + device.getSerialNumber(), false);
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
            setIcon(EMULATOR_ICON);
        }

        public AvdInfo getDevice() {
            return device;
        }

        public NbAndroidProjectImpl getProject() {
            return project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toolbarButton.setToolTipText(getHtmlDisplayName());
            toolbarButton.setIcon(EMULATOR_ICON);
            AuxiliaryProperties auxiliaryProperties = project.getLookup().lookup(AuxiliaryProperties.class);
            auxiliaryProperties.put(ADB_SELECTED_DEVICE, "AVD." + device.getDeviceName(), false);
            auxiliaryProperties.put(ADB_LAST_SELECTED_AVD, device.getDeviceName(), false);
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
