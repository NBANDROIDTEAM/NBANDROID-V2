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
package org.nbandroid.netbeans.gradle.v2.adb.nodes;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbandroid.netbeans.gradle.core.ddm.AndroidDebugBridgeFactory;
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatformManager;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.RestartADBAction;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;

/**
 *
 * @author tom
 * @author arsi
 */
@ServicesTabNodeRegistration(name = DevicesNode.NAME,
        displayName = "#TXT_Devices",
        shortDescription = "#HINT_Devices",
        iconResource = DevicesNode.ICON_PATH, position = 430)
@Messages({
    "TXT_Devices=Android Devices",
    "HINT_Devices=Android Devices connected via ADB",
    "HINT_DevicesBroken=Android Devices are not accessible. Configure Android SDK?",})
public class DevicesNode extends AbstractNode implements PropertyChangeListener {

    public static final String NAME = "Android Devices";
    public static final String ICON_PATH = "org/netbeans/modules/android/project/resources/android.png";
    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    // TODO(radim): needs large refactoring. Either fix DalvikPlatformManager or use SdkManager

    private static DevicesNode node;
    private volatile boolean broken;

    private DevicesNode() {
        super(new DevicesChildren());
        final DalvikPlatformManager dpm = DalvikPlatformManager.getDefault();
        dpm.addPropertyChangeListener(WeakListeners.propertyChange(this, dpm));
        updateDescription();
        setIconBaseWithExtension(ICON_PATH);
    }

    private void updateDescription() {
        setName(NAME);
        setDisplayName(NAME);
        final AndroidDebugBridge debugBridge = AndroidDebugBridgeFactory.getDefault();
        broken = debugBridge == null;
        String description = broken ? "Debugbridge broken! Please restart ADB!" : NAME;
        setShortDescription(description);
    }

    @Override
    public String getHtmlDisplayName() {
        String dispName = super.getDisplayName();
        try {
            dispName = XMLUtil.toElementContent(dispName);
        } catch (CharConversionException ex) {
            return dispName;
        }
        return broken ? "<font color=\"#A40000\">" + dispName + "</font>" : dispName;           //NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        // TODO add action to restart debug bridge
        return new Action[]{
            new RestartADBAction(),
            SystemAction.get(PropertiesAction.class)
        };
    }

    @Override
    public PropertySet[] getPropertySets() {
        final Sheet.Set set = Sheet.createPropertiesSet();
        set.put(new PropertySupport.ReadOnly<Boolean>(
                "PROP_DebugBridgeConnected",
                Boolean.class,
                NbBundle.getMessage(DevicesNode.class, "PROP_DebugBridgeConnected"),
                NbBundle.getMessage(DevicesNode.class, "DESC_DebugBridgeConnected")) {
            @Override
            public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
                final AndroidDebugBridge jp = AndroidDebugBridgeFactory.getDefault();
                return jp == null ? Boolean.FALSE : jp.isConnected();
            }
        });
        return new PropertySet[]{
            set
        };
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (DalvikPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateDescription();
                    fireDisplayNameChange(null, null);
                    firePropertySetsChange(null, null);
                }
            });
        }
    }

    public static synchronized DevicesNode getInstance() {
        if (node == null) {
            node = new DevicesNode();
        }
        return node;
    }

    private static class DevicesChildren extends Children.Keys<DeviceHolder> implements PropertyChangeListener, AndroidDebugBridge.IDeviceChangeListener {

        public DevicesChildren() {
            final DalvikPlatformManager jpm = DalvikPlatformManager.getDefault();
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
        }

        @Override
        protected void addNotify() {
            AndroidDebugBridge.addDeviceChangeListener(this);
            updateKeys();
        }
        //where


        private static final String IP_PORT_PATTERN
                = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5]):"
                + "([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";
        private static final Pattern PATTERN;

        static {
            PATTERN = Pattern.compile(IP_PORT_PATTERN);
        }

        public static boolean validate(final String s) {
            return PATTERN.matcher(s).matches();
        }

        private void updateKeys() {
            final List<DeviceHolder> keys = new ArrayList<>();
            final AndroidDebugBridge bridge = AndroidDebugBridgeFactory.getDefault();
            java.util.Map<String, IDevice> tmpUSB = new HashMap<>();
            java.util.Map<String, IDevice> tmpEthernet = new HashMap<>();
            if (bridge != null) {
                for (IDevice device : bridge.getDevices()) {
                    String sn = device.getProperty("ro.serialno");
                    if (device.isEmulator()) {
                        keys.add(new DeviceHolder(device, device.getSerialNumber()));
                    } else if (validate(device.getSerialNumber())) {
                        if (sn == null) {
                            sn = device.getSerialNumber();
                        }
                        tmpEthernet.put(sn, device);
                    } else {
                        if (sn == null) {
                            sn = device.getSerialNumber();
                        }
                        tmpUSB.put(sn, device);
                    }
                }
                for (java.util.Map.Entry<String, IDevice> entry : tmpUSB.entrySet()) {
                    String sn = entry.getKey();
                    IDevice usb = entry.getValue();
                    if (tmpEthernet.containsKey(sn)) {
                        keys.add(new MobileDeviceHolder(usb, tmpEthernet.remove(sn), sn));
                    } else {
                        keys.add(new MobileDeviceHolder(usb, null, sn));
                    }
                }
                for (java.util.Map.Entry<String, IDevice> entry : tmpEthernet.entrySet()) {
                    String sn = entry.getKey();
                    IDevice ethernet = entry.getValue();
                    keys.add(new MobileDeviceHolder(null, ethernet, sn));
                }
            }
            Collections.sort(keys, new Comparator<DeviceHolder>() {
                @Override
                public int compare(DeviceHolder o1, DeviceHolder o2) {
                    return o1.getSerialNumber().compareToIgnoreCase(o2.getSerialNumber());
                }
            });
            this.setKeys(keys);
        }

        private void updateKeysAsync() {
            pool.schedule(new Runnable() {
                @Override
                public void run() {
                    updateKeys();
                }
            }, 1, TimeUnit.SECONDS);
        }

        @Override
        protected void removeNotify() {
            AndroidDebugBridge.removeDeviceChangeListener(this);
            this.setKeys(new DeviceHolder[0]);
        }

        @Override
        protected Node[] createNodes(final DeviceHolder key) {
            assert key != null;
            if (key instanceof MobileDeviceHolder) {
                return new Node[]{new MobileDeviceNode((MobileDeviceHolder) key)};
            } else {
                return new Node[]{new EmulatorDeviceNode(key.usb)};
            }

        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(DalvikPlatformManager.PROP_INSTALLED_PLATFORMS)) {
                updateKeysAsync();
            }
        }

        @Override
        public void deviceConnected(IDevice arg0) {
            updateKeysAsync();
        }

        @Override
        public void deviceDisconnected(IDevice arg0) {
            updateKeysAsync();
        }

        @Override
        public void deviceChanged(IDevice arg0, int arg1) {
            //Handled by node itself
        }

    }

    public static class DeviceHolder {

        public final IDevice usb;
        private final String serialNumber;

        public DeviceHolder(final IDevice usb, String serialNumber) {
            this.usb = usb;
            this.serialNumber = serialNumber;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public IDevice getUsb() {
            return usb;
        }

    }

    public static class MobileDeviceHolder extends DeviceHolder {

        public final IDevice ethernet;
        private String ip = "127.0.0.1";

        public MobileDeviceHolder(IDevice usb, IDevice ethernet, String serialNumber) {
            super(usb, serialNumber);
            this.ethernet = ethernet;
        }

        public IDevice getEthernet() {
            return ethernet;
        }

        public IDevice getMasterDevice() {
            if (usb != null) {
                return usb;
            } else {
                return ethernet;
            }
        }

        public boolean hasEthernet() {
            return ethernet != null;
        }

        public boolean hasUSB() {
            return usb != null;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

    }
}
