/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.nbandroid.netbeans.gradle.core.ui;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbandroid.netbeans.gradle.core.ddm.EmulatorControlSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
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
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author tom
 */
public class DeviceNode extends AbstractNode implements AndroidDebugBridge.IDeviceChangeListener {

    private static final Logger LOG = Logger.getLogger(DeviceNode.class.getName());

    private final IDevice device;
    private final EmulatorControlSupport emulatorControl;

    DeviceNode(final IDevice device) {
        super(new DeviceChildren(device), Lookups.fixed(device));
        assert device != null;
        this.device = device;
        emulatorControl = new EmulatorControlSupport(device);
        this.setDisplayName(device.getSerialNumber());
        this.updateDescription();
        this.setIconBaseWithExtension("org/netbeans/modules/android/project/resources/phone.png");
        AndroidDebugBridge.addDeviceChangeListener(this);
        this.addNodeListener(new NodeListener() {

            public void childrenAdded(NodeMemberEvent event) {
            }

            public void childrenRemoved(NodeMemberEvent event) {
            }

            public void childrenReordered(NodeReorderEvent event) {
            }

            public void nodeDestroyed(NodeEvent event) {
                AndroidDebugBridge.removeDeviceChangeListener(DeviceNode.this);
            }

            public void propertyChange(PropertyChangeEvent event) {
            }
        });
    }

    private void updateDescription() {
        final String serNum = this.device.getSerialNumber();
        final IDevice.DeviceState state = this.device.getState();
        this.setShortDescription(NbBundle.getMessage(DeviceNode.class, "HINT_Device",
                serNum, state != null ? state.toString() : "(unknown state)"));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            SystemAction.get(ScreenShotAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }

    @Override
    public PropertySet[] getPropertySets() {
        final Sheet.Set defset = Sheet.createPropertiesSet();
        defset.put(new PropertySupport.ReadOnly<String>(
                "PROP_DeviceId",
                String.class,
                NbBundle.getMessage(DeviceNode.class, "PROP_DeviceId"),
                NbBundle.getMessage(DeviceNode.class, "DESC_DeviceId")) {
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return device.getSerialNumber();
            }
        });
        defset.put(new PropertySupport.ReadOnly<String>(
                "PROP_State",
                String.class,
                NbBundle.getMessage(DeviceNode.class, "PROP_State"),
                NbBundle.getMessage(DeviceNode.class, "DESC_State")) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return device.getState().toString();
            }
        });
        defset.put(new PropertySupport.ReadOnly<Boolean>(
                "PROP_Emulator",
                Boolean.class,
                NbBundle.getMessage(DeviceNode.class, "PROP_Emulator"),
                NbBundle.getMessage(DeviceNode.class, "DESC_Emulator")) {

            @Override
            public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
                return Boolean.valueOf(device.isEmulator());
            }
        });

        addEmulatorControls(defset);

        final Sheet.Set devset = new Sheet.Set();
        devset.setExpert(true);
        devset.setName("SET_DeviceProps");
        devset.setDisplayName(NbBundle.getMessage(DeviceNode.class, "SET_DeviceProps"));

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

        final Map<String, String> props = device.getProperties();
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

    private void addEmulatorControls(Sheet.Set defset) {
        if (!device.isEmulator()) {
            return;
        }

        for (final EmulatorControlSupport.Control ctrl : EmulatorControlSupport.Control.values()) {
            defset.put(new PropertySupport.ReadWrite<String>(
                    ctrl.name(),
                    String.class,
                    ctrl.getDisplayName(),
                    ctrl.getDescription()) {

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return emulatorControl.getData(ctrl);
                }

                @Override
                public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    emulatorControl.setData(ctrl, val);
                }

                @Override
                public PropertyEditor getPropertyEditor() {
                    return PropertyUtils.stringPropertyEditorWithTags(emulatorControl.getTags(ctrl));
                }
            });
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

        public void deviceConnected(IDevice device) {
            //Not important
        }

        public void deviceDisconnected(IDevice device) {
            //Not important
        }

        public void deviceChanged(IDevice device, int eventType) {
            if (this.device.equals(device) && (eventType & IDevice.CHANGE_CLIENT_LIST) == IDevice.CHANGE_CLIENT_LIST) {
                SwingUtilities.invokeLater(new Runnable() {
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

    public static class ScreenShotAction extends NodeAction {

        private static final String PNG = "png";

        @Override
        protected void performAction(Node[] nodes) {
            assert nodes != null;
            assert nodes.length == 1;
            final IDevice device = nodes[0].getLookup().lookup(IDevice.class);
            assert device != null;
            try {
                final RawImage image = device.getScreenshot();
                if (image == null) {
                    throw new IOException("No screenshot"); //NOI18N
                }
                int imageType;

                // determine image format
                switch (image.bpp) {
                    case 16: {
                        imageType = BufferedImage.TYPE_USHORT_565_RGB;
                        break;
                    }

                    default: {
                        imageType = BufferedImage.TYPE_INT_ARGB;
                        break;
                    }
                }

                final BufferedImage img = new BufferedImage(image.width, image.height, imageType);
                final DataBuffer db = img.getRaster().getDataBuffer();
                for (int y = 0; y < image.height; y++) {
                    for (int x = 0; x < image.width; x++) {
                        int color;

                        switch (imageType) {
                            case BufferedImage.TYPE_USHORT_565_RGB: {
                                byte l = image.data[2 * (y * image.width + x)];
                                byte h = image.data[2 * (y * image.width + x) + 1];
                                color = ((h << 8) | (l & 0xff)) & 0xffff;
                                break;
                            }

                            default: {
                                color = image.getARGB((image.bpp / 8) * (y * image.width + x));
                                break;
                            }
                        }

                        db.setElem(y * image.width + x, color);
                    }
                }
                final String tmpVal = System.getProperty("java.io.tmpdir");   //NOI18N
                final File tmpFile = FileUtil.normalizeFile(new File(tmpVal));
                final String name = FileUtil.findFreeFileName(FileUtil.toFileObject(tmpFile),
                        device.getSerialNumber(), PNG);
                final File pictureFile = new File(tmpFile, name + '.' + PNG);
                ImageIO.write(img, PNG, pictureFile);
                final FileObject pictureFileObj = FileUtil.toFileObject(pictureFile);
                DataObject dobj = DataObject.find(pictureFileObj);
                OpenCookie oc = dobj.getCookie(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                }
            } catch (IOException ioe) {
                LOG.log(Level.INFO, null, ioe);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(DeviceNode.class, "ERROR_ScreenShot"),
                        NotifyDescriptor.ERROR_MESSAGE));
            } catch (TimeoutException ex) {
                LOG.log(Level.INFO, null, ex);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(DeviceNode.class, "ERROR_ScreenShot"),
                        NotifyDescriptor.ERROR_MESSAGE));
            } catch (AdbCommandRejectedException ex) {
                LOG.log(Level.INFO, null, ex);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(DeviceNode.class, "ERROR_ScreenShot"),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }

        @Override
        protected boolean enable(Node[] nodes) {
            if (nodes.length != 1) {
                return false;
            }
            return nodes[0].getLookup().lookup(IDevice.class) != null;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(ScreenShotAction.class, "TXT_ScreenShotAction");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(DeviceNode.class.getName());
        }

    }

}
