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

package org.netbeans.modules.android.core.ui;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.android.core.ddm.AndroidDebugBridgeFactory;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
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
 */
@ServicesTabNodeRegistration(name=DevicesNode.NAME, 
    displayName="#TXT_Devices", 
    shortDescription="#HINT_Devices", 
    iconResource=DevicesNode.ICON_PATH, position=430)
@Messages({
    "TXT_Devices=Android Devices",
    "HINT_Devices=Android Devices connected via ADB",
    "HINT_DevicesBroken=Android Devices are not accessible. Configure Android SDK?",
})
public class DevicesNode extends AbstractNode implements PropertyChangeListener {

  public static final String NAME = "AndroidDevices";
  public static final String ICON_PATH = "org/netbeans/modules/android/core/resources/android.png";
  // TODO(radim): needs large refactoring. Either fix DalvikPlatformManager or use SdkManager

    private static DevicesNode node;
    private volatile boolean broken;

    private DevicesNode () {
        super(new DevicesChildren());
        final DalvikPlatformManager dpm = DalvikPlatformManager.getDefault();
        dpm.addPropertyChangeListener(WeakListeners.propertyChange(this, dpm));
        updateDescription ();
        setIconBaseWithExtension(ICON_PATH);
    }

    private void updateDescription () {
      setName(NAME);
        setDisplayName("ddd");
      final AndroidDebugBridge debugBridge = AndroidDebugBridgeFactory.getDefault();
      broken = debugBridge == null;
        String description = broken ? "brk" : "nbrk";
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
        return broken ? "<font color=\"#A40000\">"+dispName+"</font>" : dispName;           //NOI18N
    }


    @Override
    public Action[] getActions(boolean context) {
      // TODO add action to restart debug bridge
        return new Action [] {
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
        return new PropertySet[] {
            set
        };
    }

    @Override
    public void propertyChange (final PropertyChangeEvent event) {
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

        public DevicesChildren () {
            final DalvikPlatformManager jpm = DalvikPlatformManager.getDefault();
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));            
        }

        @Override
        protected void addNotify() {
            AndroidDebugBridge.addDeviceChangeListener(this);
            updateKeys();
        }
        //where

        private void updateKeys() {
            final Set<DeviceHolder> keys = new HashSet<DeviceHolder>();
            final AndroidDebugBridge bridge = AndroidDebugBridgeFactory.getDefault();
            if (bridge != null) {                
                for (IDevice device : bridge.getDevices()) {
                    keys.add(new DeviceHolder(device));
                }
            }
            this.setKeys(keys);
        }

        private void updateKeysAsync() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateKeys();
                }
            });
        }


        @Override
        protected void removeNotify() {
            AndroidDebugBridge.removeDeviceChangeListener(this);
            this.setKeys(new DeviceHolder[0]);
        }



        @Override
        protected Node[] createNodes(final DeviceHolder key) {
            assert key != null;
            return new Node[] { new DeviceNode (key.device)};
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

    private static class DeviceHolder {

        public final IDevice device;

        public DeviceHolder (final IDevice device) {
            assert device != null;
            this.device = device;
        }

        @Override
        public String toString() {
            return device.getSerialNumber();
        }

        @Override
        public int hashCode() {
            return device.getSerialNumber().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DeviceHolder) {
                DeviceHolder other = (DeviceHolder) obj;
                return this.device.getSerialNumber().equals(other.device.getSerialNumber());
            }
            return false;
        }

    }
}
