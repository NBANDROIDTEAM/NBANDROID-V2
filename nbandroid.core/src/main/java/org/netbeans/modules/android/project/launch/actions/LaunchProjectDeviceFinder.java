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
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.repository.AndroidSdkHandler;
import com.android.utils.NullLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.android.project.api.NbAndroidProjectImpl;
import org.netbeans.spi.project.AuxiliaryProperties;

/**
 *
 * @author arsi
 */
public class LaunchProjectDeviceFinder {

    public static final class LaunchData {

        private final AvdInfo avdInfo;
        private final IDevice device;

        public LaunchData(AvdInfo avdInfo, IDevice device) {
            if (avdInfo == null && device == null) {
                throw new IllegalArgumentException(
                        "AVD (" + avdInfo + ") or device (" + device + ") must not be null");
            }
            this.avdInfo = avdInfo;
            this.device = device;
        }

        public AvdInfo getAvdInfo() {
            return avdInfo;
        }

        public IDevice getDevice() {
            return device;
        }

        @Override
        public String toString() {
            return "LaunchData{" + "avdInfo=" + avdInfo + ", device=" + device + '}';
        }
    }

    public static final LaunchData getSelectedDevice(NbAndroidProjectImpl project) {
        AuxiliaryProperties auxiliaryProperties = project.getLookup().lookup(AuxiliaryProperties.class);
        if (auxiliaryProperties == null) {
            return null;
        }
        String lastSelectedDevice = auxiliaryProperties.get(SelectDeviceAction.ADB_SELECTED_DEVICE, false);
        String lastSelectedAvd = auxiliaryProperties.get(SelectDeviceAction.ADB_LAST_SELECTED_AVD, false);
        AndroidDebugBridge debugBridge = AndroidSdkProvider.getAdb();
        if (debugBridge == null) {
            return null;
        } else {
            Map<String, Object> devices = new HashMap<>();
            List<IDevice> devicesReal = new ArrayList<>();
            List<AvdInfo> devicesAvd = new ArrayList<>();
            for (IDevice device : debugBridge.getDevices()) {
                if (!device.isEmulator()) {
                    devices.put("DEVICE." + device.getSerialNumber(), device);
                    devicesReal.add(device);
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
                            devices.put("AVD." + validAvd.getDeviceName(), validAvd);
                            devicesAvd.add(validAvd);
                        }
                    } catch (AndroidLocation.AndroidLocationException ex) {
                    }
                }
            }
            Object device = null;
            if (lastSelectedDevice != null) {
                device = devices.get(lastSelectedDevice);
                if (device == null && lastSelectedAvd != null) {
                    //try to find last AVD device
                    device = devices.get(lastSelectedAvd);
                }
                if (device == null && !devicesReal.isEmpty()) {
                    //try to find first real device
                    device = devicesReal.iterator().next();
                } else if (!devicesAvd.isEmpty()) {
                    //try to find first AVD device
                    device = devicesAvd.iterator().next();
                }
            } else {
                if (!devicesReal.isEmpty()) {
                    //try to find first real device
                    device = devicesReal.iterator().next();
                } else if (!devicesAvd.isEmpty()) {
                    //try to find first AVD device
                    device = devicesAvd.iterator().next();
                }
            }
            if(device==null){
                return null;
            }else if (device instanceof IDevice){
                return new LaunchData(null, (IDevice) device);
            }else{
                return new LaunchData((AvdInfo) device, null);
            }
        }
    }

}
