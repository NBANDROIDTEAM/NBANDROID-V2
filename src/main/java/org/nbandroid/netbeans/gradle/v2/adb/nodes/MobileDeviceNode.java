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
import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NbAndroidAdbHelper;
import com.android.sdklib.SdkVersionInfo;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.io.CharConversionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbandroid.netbeans.gradle.v2.ui.IconProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 *
 * @author tom
 * @author arsi
 */
public class MobileDeviceNode extends AbstractNode implements AndroidDebugBridge.IDeviceChangeListener {

    private static final Logger LOG = Logger.getLogger(MobileDeviceNode.class.getName());
    public static final Map<String, DevicesNode.MobileDeviceHolder> doConnect = new ConcurrentHashMap<>();

    private final DevicesNode.MobileDeviceHolder device;

    MobileDeviceNode(final DevicesNode.MobileDeviceHolder device) {
        super(new DeviceChildren(device.getMasterDevice()), Lookups.fixed(device, device.getMasterDevice()));
        assert device != null;
        this.device = device;
        this.updateDescription();
        this.setIconBaseWithExtension("org/netbeans/modules/android/project/resources/phone.png");
        AndroidDebugBridge.addDeviceChangeListener(this);
        this.addNodeListener(new NodeListener() {

            @Override
            public void childrenAdded(NodeMemberEvent event) {
            }

            @Override
            public void childrenRemoved(NodeMemberEvent event) {
            }

            @Override
            public void childrenReordered(NodeReorderEvent event) {
            }

            @Override
            public void nodeDestroyed(NodeEvent event) {
                AndroidDebugBridge.removeDeviceChangeListener(MobileDeviceNode.this);
            }

            @Override
            public void propertyChange(PropertyChangeEvent event) {
            }
        });
        //after change to tcp mode connect to device
        if (device.getSerialNumber() != null) {
            DevicesNode.MobileDeviceHolder connect = doConnect.remove(device.getSerialNumber());
            if (connect != null) {
                NbAndroidAdbHelper.connectEthernet(connect.getUsb().getSerialNumber(), connect.getIp(), 5555);
            }
        }
    }

    @Override
    public String getHtmlDisplayName() {
        String devName = device.getMasterDevice().getProperty("ro.product.display");
        if (devName == null) {
            devName = device.getMasterDevice().getProperty("ro.product.name");
        }
        try {
            devName = XMLUtil.toElementContent(devName);
        } catch (CharConversionException ex) {
        }
        String androidVersion = SdkVersionInfo.getVersionWithCodename(device.getMasterDevice().getVersion());
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

    private void updateDescription() {
        final String serNum = this.device.getSerialNumber();
        final IDevice.DeviceState state = this.device.getMasterDevice().getState();
        this.setShortDescription(NbBundle.getMessage(MobileDeviceNode.class, "HINT_Device",
                serNum, state != null ? state.toString() : "(unknown state)"));
    }

    private Image annotateUsbWifi(Image icon) {
        if (device.hasEthernet()) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_WIFI_BADGE, 12, 0);
        }
        if (device.hasUSB()) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_USB_BADGE, 12, 8);
        }
        return icon;
    }

    @Override
    public Image getIcon(int type) {
        return annotateUsbWifi(super.getIcon(type));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return annotateUsbWifi(super.getIcon(type));
    }

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> actionsForPath = Utilities.actionsForPath("Android/ADB/MobileDevice");
        return actionsForPath.toArray(new Action[actionsForPath.size()]);
    }

    @Override
    public PropertySet[] getPropertySets() {
        final Sheet.Set defset = Sheet.createPropertiesSet();
        defset.put(new PropertySupport.ReadOnly<String>(
                "PROP_DeviceId",
                String.class,
                NbBundle.getMessage(MobileDeviceNode.class, "PROP_DeviceId"),
                NbBundle.getMessage(MobileDeviceNode.class, "DESC_DeviceId")) {
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return device.getSerialNumber();
            }
        });
        defset.put(new PropertySupport.ReadOnly<String>(
                "PROP_State",
                String.class,
                NbBundle.getMessage(MobileDeviceNode.class, "PROP_State"),
                NbBundle.getMessage(MobileDeviceNode.class, "DESC_State")) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return device.getMasterDevice().getState().toString();
            }
        });

        final Sheet.Set devset = new Sheet.Set();
        devset.setExpert(true);
        devset.setName("SET_DeviceProps");
        devset.setDisplayName(NbBundle.getMessage(MobileDeviceNode.class, "SET_DeviceProps"));

        class DeviceProperty extends PropertySupport.ReadOnly<String> {

            private final String name;
            private final String value;

            public DeviceProperty(String name, String value) {
                super(name, String.class, name, name);
                assert name != null;
                assert value != null;
                this.name = name;
                this.value = value;
            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return this.value;
            }

        }

        final Map<String, String> props = device.getMasterDevice().getProperties();
        for (Map.Entry<String, String> prop : props.entrySet()) {
            devset.put(new DeviceProperty(prop.getKey(), prop.getValue()));
        }

        return new PropertySet[]{
            defset,
            devset
        };
    }

    public void deviceConnected(IDevice device) {
    }

    public void deviceDisconnected(IDevice device) {
    }

    public void deviceChanged(IDevice device, int changeType) {
        if (this.device.equals(device) && (changeType & IDevice.CHANGE_STATE) == IDevice.CHANGE_STATE) {
            this.updateDescription();
            firePropertySetsChange(null, null);
        }
    }

    private static class DeviceChildren extends Children.Keys<ClientHolder> implements AndroidDebugBridge.IDeviceChangeListener {

        private final IDevice device;

        public DeviceChildren(final IDevice device) {
            assert device != null;
            this.device = device;
        }

        @Override
        protected void addNotify() {
            AndroidDebugBridge.addDeviceChangeListener(this);
            updateKeys();
        }

        private void updateKeys() {
            Set<ClientHolder> keys = new HashSet<ClientHolder>();
            final Client[] clients = device.getClients();
            for (Client client : clients) {
                keys.add(new ClientHolder(client));
            }
            this.setKeys(keys);
        }

        @Override
        protected void removeNotify() {
            AndroidDebugBridge.removeDeviceChangeListener(this);
            this.setKeys(new ClientHolder[0]);
        }

        @Override
        protected Node[] createNodes(ClientHolder key) {
            assert key != null;
            return new Node[]{new ClientNode(key.client)};
        }

        @Override
        public void deviceConnected(IDevice device) {
            //Not important
        }

        @Override
        public void deviceDisconnected(IDevice device) {
            //Not important
        }

        @Override
        public void deviceChanged(IDevice device, int eventType) {
            if (this.device.equals(device) && (eventType & IDevice.CHANGE_CLIENT_LIST) == IDevice.CHANGE_CLIENT_LIST) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateKeys();
                    }
                });
            }
        }

    }

    private static class ClientHolder {

        public final Client client;

        public ClientHolder(final Client client) {
            assert client != null;
            this.client = client;
        }

        @Override
        public String toString() {
            return this.client.getClientData().getClientDescription();
        }

        @Override
        public int hashCode() {
            return this.client.getClientData().getPid();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClientHolder) {
                final ClientHolder other = (ClientHolder) obj;
                return this.client.getClientData().getPid() == other.client.getClientData().getPid();
            }
            return false;
        }

    }

}
