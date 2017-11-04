/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nbandroid.netbeans.gradle.logcat;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.nbandroid.netbeans.gradle.core.ddm.AndroidDebugBridgeFactory;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Support class for the list of logged devices. Implements a data model, cell
 * renderer and a listener to handle attached and detached devices.
 *
 * @author Christian Fischer
 */
public class LogDevicesComboBoxSupport
        implements ComboBoxModel<String>,
        ListCellRenderer,
        LookupListener,
        PropertyChangeListener,
        AndroidDebugBridge.IDeviceChangeListener {

    private final Set<ListDataListener> dataListeners = new HashSet<>();
    private final List<String> devices = new ArrayList<>();
    private final Map<String, String> deviceLabels = new HashMap<>();
    private final Lookup.Result<IDevice> deviceLookup;
    private final LogReader reader;
    private JComboBox cmb;

    protected LogDevicesComboBoxSupport(LogReader reader) {
        this.reader = reader;

        // get notification for logged devices
        reader.addPropertyChangeListener(WeakListeners.propertyChange(this, reader));

        // get notifications for attached and detached devices
        AndroidDebugBridge.addDeviceChangeListener(this);

        Lookup lookup = Utilities.actionsGlobalContext();
        deviceLookup = lookup.lookupResult(IDevice.class);
        deviceLookup.addLookupListener(this);

        update();
    }

    /**
     * Attaches this support class to a combo box.
     *
     * @param cmb
     */
    public void attach(JComboBox cmb) {
        this.cmb = cmb;
        this.cmb.setModel(this);
        this.cmb.setRenderer(this);
    }

    /**
     * Detach all listeners of this support class.
     */
    public void detach() {
        AndroidDebugBridge.removeDeviceChangeListener(this);

        reader.removePropertyChangeListener(this);

        deviceLookup.removeLookupListener(this);

        return;
    }

    /**
     * Update the content of the list data model and add all currently logged
     * and attached devices.
     */
    private void update() {
        // add all logged devices
        for (String device : reader.getLoggedDevices()) {
            add(device);
        }

        // add all connected devices
        AndroidDebugBridge bridge = AndroidDebugBridgeFactory.getDefault();
        if (bridge != null) {
            for (IDevice device : bridge.getDevices()) {
                add(device);
            }
        }
    }

    /**
     * Add a new device to the data model, or replace any existing.
     */
    private void add(IDevice device) {
        String serial = device.getSerialNumber();

        do {
            // for emulators use their virtual device name as label
            String deviceAvdName = device.getAvdName();
            if (deviceAvdName != null) {
                deviceLabels.put(serial, deviceAvdName + " [" + serial + "]");
                break;
            }

            // for real devices use their model name (something like '<vendor> <device>')
            String deviceModelName = device.getProperty("ro.product.model");
            if (deviceModelName != null) {
                deviceLabels.put(serial, deviceModelName + " [" + serial + "]");
                break;
            }
        } while (false);

        // finally, add the serial.
        add(serial);
    }

    /**
     * Add a new device serial to this data model, if it doesn't already exist.
     */
    private void add(String device) {
        int insertIndex = devices.size();

        // check, if this serial already exists.
        for (int i = devices.size(); --i >= 0;) {
            if (devices.get(i).equals(device)) {

                // refresh this item (the label may changed)
                fireListDataEvent(ListDataEvent.INTERVAL_REMOVED, i);
                fireListDataEvent(ListDataEvent.INTERVAL_ADDED, i);

                return;
            }
        }

        if (insertIndex != -1) {
            devices.add(insertIndex, device);

            // send notification
            fireListDataEvent(ListDataEvent.INTERVAL_ADDED, insertIndex);
        }
    }

    /**
     * Remove a device from the data model, except it has messages in the log
     * cache.
     */
    private void remove(IDevice device) {
        remove(device.getSerialNumber());
    }

    /**
     * Remove a device from the data model, except it has messages in the log
     * cache.
     */
    private void remove(String device) {
        // do we have log events for this device?
        if (reader.getLogEventsForDevice(device) != null) {
            // if true, we want to keep it
            return;
        }

        for (int i = devices.size(); --i >= 0;) {
            if (devices.get(i).equals(device)) {
                devices.remove(i);
                fireListDataEvent(ListDataEvent.INTERVAL_REMOVED, i);
            }
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof String) {
            reader.setCurrentDevice((String) anItem);
        }
    }

    @Override
    public Object getSelectedItem() {
        return reader.getCurrentDevice();
    }

    @Override
    public int getSize() {
        return devices.size();
    }

    @Override
    public String getElementAt(int index) {
        return devices.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        dataListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        dataListeners.remove(l);
    }

    private void fireContentsChangedEvent() {
        fireListDataEvent(ListDataEvent.CONTENTS_CHANGED, -1);
    }

    private void fireListDataEvent(int type, int index) {
        fireListDataEvent(type, index, index);
    }

    private void fireListDataEvent(int type, int index1, int index2) {
        ListDataEvent event = new ListDataEvent(this, type, index1, index2);

        for (ListDataListener listener : dataListeners) {
            switch (type) {
                case ListDataEvent.CONTENTS_CHANGED: {
                    listener.contentsChanged(event);
                    break;
                }

                case ListDataEvent.INTERVAL_ADDED: {
                    listener.intervalAdded(event);
                    break;
                }

                case ListDataEvent.INTERVAL_REMOVED: {
                    listener.intervalRemoved(event);
                    break;
                }
            }

        }
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label;

        if (value != null && value instanceof String) {
            String deviceSerial = (String) value;
            String deviceLabel = deviceLabels.get(deviceSerial);

            // if we don't have a valid label for this device, just use the serial
            if (deviceLabel == null) {
                deviceLabel = deviceSerial;
            }

            label = new JLabel(deviceLabel);
        } else {
            // fallback, if there's something unexpected
            label = new JLabel(String.valueOf(value));
        }

        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
            label.setOpaque(true);
        }

        return label;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (LogReader.PROPERTY_DEVICE_LIST.equals(evt.getPropertyName())) {
            update();
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        // set the first item in the result as "active" device
        for (IDevice device : deviceLookup.allInstances()) {
            if (cmb != null) {
                cmb.setSelectedItem(device.getSerialNumber());
            } else {
                setSelectedItem(device.getSerialNumber());
            }

            break;
        }
    }

    @Override
    public void deviceConnected(IDevice id) {
        add(id);
    }

    @Override
    public void deviceDisconnected(IDevice id) {
        remove(id);
    }

    @Override
    public void deviceChanged(IDevice id, int i) {
        // do nothing
    }
}
