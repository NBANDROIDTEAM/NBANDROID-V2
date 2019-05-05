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
package org.netbeans.modules.android.avd.manager.ui;

import com.android.prefs.AndroidLocation;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.DeviceManager;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.utils.StdLogger;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.openide.WizardDescriptor;

// TODO define position attribute
public final class CreateAvdWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private int index;

    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    public static final String DEVICE_MANAGER = "DEVICE_MANAGER";
    public static final String REPO_MANAGER = "REPO_MANAGER";
    public static final String IMAGE_MANAGER = "IMAGE_MANAGER";
    public static final String DEVICE_SELECTED = "DEVICE_SELECTED";
    public static final String ANDROID_SDK = "ANDROID_SDK";
    public static final String SYSTEM_IMAGE = "SYSTEM_IMAGE";
    public static final String AVD_MANAGER = "AVD_MANAGER";
    public static final String AVD_DISPLAY_NAME = "AVD_DISPLAY_NAME";
    public static final String AVD_ID_NAME = "AVD_ID_NAME";
    public static final String AVD_USER_HW_CONFIG = "AVD_USER_HW_CONFIG";
    public static final String AVD_SDCARD = "AVD_SDCARD";
    private final ChangeListener changeListener;

    public CreateAvdWizardIterator(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }
    
    

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new CreateAvdWizardPanel1(wizard));
            panels.add(new CreateAvdWizardPanel2(wizard));
            panels.add(new CreateAvdWizardPanel3(wizard));
            String[] steps = createSteps();
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    @Override
    public Set<?> instantiate() throws IOException {
        // TODO return set of FileObject (or DataObject) you have created

        //path /home/arsi/.android/avd/Nexus_5_API_27.avd
        //avd name Nexus_5_API_27
        //skinFolder /jetty/android-studio-sdk/skins/nexus_5
        //skinname null
        //sdcard 512M
        //
        //hw.dPad => no  x
        //hw.lcd.height => 1920 x
        //fastboot.chosenSnapshotFile => 
        //runtime.network.speed => full
        //hw.accelerometer => yes x
        //hw.device.name => Nexus 5 x
        //vm.heapSize => 128
        //skin.dynamic => yes
        //hw.device.manufacturer => Google x
        //hw.lcd.width => 1080 x
        //skin.path => /jetty/android-studio-sdk/skins/nexus_5
        //hw.gps => yes x
        //hw.initialOrientation => Portrait
        //hw.audioInput => yes x
        //showDeviceFrame => yes
        //hw.camera.back => emulated
        //hw.mainKeys => no x
        //AvdId => Nexus_5_API_27
        //hw.lcd.density => 480 x
        //hw.camera.front => emulated
        //avd.ini.displayname => Nexus 5 API 27
        //hw.gpu.mode => auto/host/software
        //hw.device.hash2 => MD5:1c925b9117dd9f33c5128dac289a0d68 x
        //fastboot.forceChosenSnapshotBoot => no
        //fastboot.forceFastBoot => yes
        //hw.trackBall => no x
        //hw.ramSize => 1536
        //hw.battery => yes x
        //fastboot.forceColdBoot => no
        //hw.cpu.ncore => 4
        //hw.sdCard => yes xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        //runtime.network.latency => none
        //hw.keyboard => yes
        //hw.sensors.proximity => yes x
        //hw.sensors.orientation => yesx
        //disk.dataPartition.size => 2G
        //hw.gpu.enabled => yes
        //
        //dev has playstore true
        //create snapshot false
        //removeprev false
        //editExisting true
        AndroidSdk defaultSdk = (AndroidSdk) wizard.getProperty(ANDROID_SDK);
        AvdManager avdManager = (AvdManager) wizard.getProperty(AVD_MANAGER);
        Device selectedDevice = (Device) wizard.getProperty(DEVICE_SELECTED);
        SystemImageDescription selectedImage = (SystemImageDescription) wizard.getProperty(SYSTEM_IMAGE);
        String displayName = (String) wizard.getProperty(AVD_DISPLAY_NAME);
        String avdId = (String) wizard.getProperty(AVD_ID_NAME);
        Map<String, String> hardwareProperties = DeviceManager.getHardwareProperties(selectedDevice);
        Map<String, String> userHardwareProperties = (Map<String, String>) wizard.getProperty(AVD_USER_HW_CONFIG);
        hardwareProperties.putAll(userHardwareProperties);
        String skinPath = hardwareProperties.get("skin.path");
        String sdcard = (String) wizard.getProperty(AVD_SDCARD);
        try {
            String avdPath = avdManager.getBaseAvdFolder().getAbsoluteFile().getAbsolutePath() + File.separator + avdId + ".avd";
            avdManager.createAvd(new File(avdPath), avdId, selectedImage.getSystemImage(),
                    new File(skinPath), null, sdcard, hardwareProperties, selectedDevice.getBootProps(), selectedDevice.hasPlayStore()&&selectedImage.getSystemImage().hasPlayStore(), false, false, true, new StdLogger(StdLogger.Level.INFO));
        } catch (AndroidLocation.AndroidLocationException androidLocationException) {
        }
        changeListener.stateChanged(null);
        return Collections.emptySet();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] res = new String[panels.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = panels.get(i).getComponent().getName();
        }
        return res;
    }

}
