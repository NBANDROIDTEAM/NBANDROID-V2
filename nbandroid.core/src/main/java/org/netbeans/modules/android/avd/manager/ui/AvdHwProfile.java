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

import com.android.ide.common.rendering.HardwareConfigHelper;
import com.android.resources.Keyboard;
import com.android.resources.Navigation;
import com.android.resources.ScreenOrientation;
import com.android.sdklib.devices.ButtonType;
import com.android.sdklib.devices.CameraLocation;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.Sensor;
import com.android.sdklib.devices.State;
import com.android.sdklib.devices.Storage;
import com.android.sdklib.repository.targets.SystemImage;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;

/**
 *
 * @author arsi
 */
public class AvdHwProfile extends javax.swing.JPanel {

    private static final Device.Builder deviceBuilder = new Device.Builder();

    public static Device showDeviceProfiler(Device device) {
        final AvdHwProfile hwProfile = new AvdHwProfile(device);
        DialogDescriptor dd = new DialogDescriptor(hwProfile, "Device profile editor", true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.CANCEL_OPTION, null);
        Object notify = DialogDisplayer.getDefault().notify(dd);
        if (DialogDescriptor.OK_OPTION.equals(notify)) {
            return hwProfile.buildDevice();
        }
        return null;
    }

    private Device buildDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private enum DeviceType {
        MOBILE("Phone/Tablet", null),
        TV("Android TV", SystemImage.TV_TAG.getId()),
        WEAR("Android Wear", SystemImage.WEAR_TAG.getId());
        private final String description;
        private final String id;

        private DeviceType(String description, String id) {
            this.description = description;
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return description; //To change body of generated methods, choose Tools | Templates.
        }

    }

    /**
     * Creates new form AvdHwProfile
     */
    public AvdHwProfile(Device device) {
        initComponents();
        deviceType.setModel(new DefaultComboBoxModel<>(DeviceType.values()));
        if (device != null) {
            String tagId = device.getTagId();
            if (tagId == null) {
                deviceType.setSelectedItem(DeviceType.MOBILE);
            } else if (DeviceType.TV.id.equals(tagId)) {
                deviceType.setSelectedItem(DeviceType.TV);
            } else if (DeviceType.WEAR.id.equals(tagId)) {
                deviceType.setSelectedItem(DeviceType.WEAR);
            }
        }
        if (device != null) {
            deviceName.setText(device.getDisplayName());
            if (CreateAvdVisualPanel1.isTv(device)) {
                deviceType.setSelectedItem(DeviceType.TV);
            } else if (HardwareConfigHelper.isWear(device)) {
                deviceType.setSelectedItem(DeviceType.WEAR);
            } else {
                deviceType.setSelectedItem(DeviceType.MOBILE);
            }
            screenSize.setValue(device.getDefaultHardware().getScreen().getDiagonalLength());
            Dimension dimension = device.getScreenSize(device.getDefaultState().getOrientation());
            resolutionX.setValue(dimension.width);
            resolutionY.setValue(dimension.height);
            round.setSelected(device.isScreenRound());
            ram.setValue(device.getDefaultHardware().getRam().getSizeAsUnit(Storage.Unit.MiB));
            hwButt.setSelected(device.getDefaultHardware().getButtonType() == ButtonType.HARD);
            hwKeyb.setSelected(device.getDefaultHardware().getKeyboard() != Keyboard.NOKEY);
            List<State> states = device.getAllStates();
            portrait.setSelected(false);
            landscape.setSelected(false);
            for (State state : states) {
                if (state.getOrientation().equals(ScreenOrientation.PORTRAIT)) {
                    portrait.setSelected(true);
                }
                if (state.getOrientation().equals(ScreenOrientation.LANDSCAPE)) {
                    landscape.setSelected(true);
                }
            }
            Navigation nav = device.getDefaultHardware().getNav();
            switch (nav) {
                case NONAV:
                    navigationStyle.setSelectedIndex(0);
                    break;
                case DPAD:
                    navigationStyle.setSelectedIndex(1);
                    break;
                case TRACKBALL:
                    navigationStyle.setSelectedIndex(2);
                    break;
                case WHEEL:
                    navigationStyle.setSelectedIndex(3);
                    break;
            }
            cameraFront.setSelected(device.getDefaultHardware().getCamera(CameraLocation.FRONT) != null);
            cameraBack.setSelected(device.getDefaultHardware().getCamera(CameraLocation.BACK) != null);
            accelerometer.setSelected(device.getDefaultHardware().getSensors().contains(Sensor.ACCELEROMETER));
            gyroscope.setSelected(device.getDefaultHardware().getSensors().contains(Sensor.GYROSCOPE));
            gps.setSelected(device.getDefaultHardware().getSensors().contains(Sensor.GPS));
            proximity.setSelected(device.getDefaultHardware().getSensors().contains(Sensor.PROXIMITY_SENSOR));
            File skinFile = device.getDefaultHardware().getSkinFile();
            AndroidSdk defaultSdk = AndroidSdkProvider.getDefaultSdk();
            if (defaultSdk != null) {
                String skinPath = defaultSdk.getSdkPath() + File.separator + "skins";
                skin.setModel(new SkinsComboboxModel(new File(skinPath)));
            }
            skin.setSelectedItem(skinFile);
        }else{
            deviceType.setSelectedItem(DeviceType.MOBILE);
            navigationStyle.setSelectedIndex(0);
        }
        skin.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
                if ((component instanceof JLabel) && (value instanceof File)) {
                    ((JLabel) component).setText(((File) value).getName());
                }
                return component;
            }

        });
    }

    private class SkinsComboboxModel implements ComboBoxModel<File> {

        private final List<File> skins = new ArrayList<>();
        private File selected = null;

        public SkinsComboboxModel(File rootDirectory) {
            File[] listFiles = rootDirectory.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                File listFile = listFiles[i];
                if (listFile.isDirectory()) {
                    File layout = new File(listFile.getAbsolutePath() + File.separator + "layout");
                    if (layout.exists()) {
                        skins.add(listFile);
                    }
                }
            }
            Collections.sort(skins);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            this.selected = (File) anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public int getSize() {
            return skins.size();
        }

        @Override
        public File getElementAt(int index) {
            return skins.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        deviceName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        deviceType = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        screenSize = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        resolutionX = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        resolutionY = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        round = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        ram = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        hwButt = new javax.swing.JCheckBox();
        hwKeyb = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        navigationStyle = new javax.swing.JComboBox<>();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        portrait = new javax.swing.JCheckBox();
        landscape = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel15 = new javax.swing.JLabel();
        cameraFront = new javax.swing.JCheckBox();
        cameraBack = new javax.swing.JCheckBox();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        accelerometer = new javax.swing.JCheckBox();
        gyroscope = new javax.swing.JCheckBox();
        gps = new javax.swing.JCheckBox();
        proximity = new javax.swing.JCheckBox();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel17 = new javax.swing.JLabel();
        skin = new javax.swing.JComboBox<>();
        skinButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel1.text")); // NOI18N

        deviceName.setText(org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.deviceName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel4.text")); // NOI18N

        screenSize.setModel(new javax.swing.SpinnerNumberModel(5.0d, 0.0d, null, 0.0d));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel6.text")); // NOI18N

        resolutionX.setModel(new javax.swing.SpinnerNumberModel(1080, 10, null, 10));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel7.text")); // NOI18N

        resolutionY.setModel(new javax.swing.SpinnerNumberModel(1920, 10, null, 10));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(round, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.round.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel10.text")); // NOI18N

        ram.setModel(new javax.swing.SpinnerNumberModel(2048L, 16L, null, 1L));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hwButt, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.hwButt.text")); // NOI18N
        hwButt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hwButtActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(hwKeyb, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.hwKeyb.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel13.text")); // NOI18N

        navigationStyle.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No navigation", "D-pad navigation", "Trackball navigation", "Wheel navigation" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel14.text")); // NOI18N

        portrait.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(portrait, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.portrait.text")); // NOI18N

        landscape.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(landscape, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.landscape.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel15.text")); // NOI18N

        cameraFront.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cameraFront, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.cameraFront.text")); // NOI18N

        cameraBack.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cameraBack, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.cameraBack.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel16.text")); // NOI18N

        accelerometer.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accelerometer, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.accelerometer.text")); // NOI18N

        gyroscope.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(gyroscope, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.gyroscope.text")); // NOI18N

        gps.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(gps, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.gps.text")); // NOI18N

        proximity.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(proximity, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.proximity.text")); // NOI18N
        proximity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proximityActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.jLabel17.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(skinButton, org.openide.util.NbBundle.getMessage(AvdHwProfile.class, "AvdHwProfile.skinButton.text")); // NOI18N
        skinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator7)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(108, 108, 108)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(landscape)
                                    .addComponent(portrait)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel15)
                                .addGap(37, 37, 37)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cameraBack)
                                    .addComponent(cameraFront))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator4)
                            .addComponent(jSeparator2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deviceName))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator3)
                            .addComponent(jSeparator6)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(deviceType, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(52, 52, 52)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel6)
                                            .addComponent(round))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(screenSize, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, 0)
                                                .addComponent(jLabel5))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(resolutionX, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, 0)
                                                .addComponent(jLabel7)
                                                .addGap(0, 0, 0)
                                                .addComponent(resolutionY, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, 0)
                                                .addComponent(jLabel8))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addGap(46, 46, 46)
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ram, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel11))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addGap(58, 58, 58)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(hwKeyb)
                                            .addComponent(hwButt)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel13)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(navigationStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jLabel14)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel16)
                                        .addGap(43, 43, 43)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(proximity)
                                            .addComponent(gps)
                                            .addComponent(gyroscope)
                                            .addComponent(accelerometer))))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(skin, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(skinButton)
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(deviceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(deviceType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(screenSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(resolutionX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(resolutionY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(round)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(ram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(hwButt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hwKeyb)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(navigationStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(portrait)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(landscape)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cameraFront)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cameraBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(accelerometer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gyroscope)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gps)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(proximity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(skin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(skinButton)
                    .addComponent(jLabel17))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void hwButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hwButtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hwButtActionPerformed

    private void proximityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proximityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_proximityActionPerformed

    private void skinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinButtonActionPerformed
        // TODO add your handling code here:
        FileChooserBuilder builder = new FileChooserBuilder("ANDROID_SKIN");
        builder.setDirectoriesOnly(true);
        builder.setTitle("Select skin directory...");
        File skinFolder = builder.showOpenDialog();
        if (skinFolder != null && skinFolder.exists()) {
            File layout = new File(skinFolder.getAbsolutePath() + File.separator + "layout");
            if (layout.exists()) {
                skin.setModel(new SkinsComboboxModel(new File(skinFolder.getParent())));
                skin.setSelectedItem(skinFolder);
            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Message("The selected directory does not contain a skin!", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }//GEN-LAST:event_skinButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox accelerometer;
    private javax.swing.JCheckBox cameraBack;
    private javax.swing.JCheckBox cameraFront;
    private javax.swing.JTextField deviceName;
    private javax.swing.JComboBox<DeviceType> deviceType;
    private javax.swing.JCheckBox gps;
    private javax.swing.JCheckBox gyroscope;
    private javax.swing.JCheckBox hwButt;
    private javax.swing.JCheckBox hwKeyb;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JCheckBox landscape;
    private javax.swing.JComboBox<String> navigationStyle;
    private javax.swing.JCheckBox portrait;
    private javax.swing.JCheckBox proximity;
    private javax.swing.JSpinner ram;
    private javax.swing.JSpinner resolutionX;
    private javax.swing.JSpinner resolutionY;
    private javax.swing.JCheckBox round;
    private javax.swing.JSpinner screenSize;
    private javax.swing.JComboBox<File> skin;
    private javax.swing.JButton skinButton;
    // End of variables declaration//GEN-END:variables
}
