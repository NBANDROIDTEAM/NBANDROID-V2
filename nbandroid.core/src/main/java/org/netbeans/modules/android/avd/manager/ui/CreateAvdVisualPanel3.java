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
import com.android.prefs.AndroidLocation;
import com.android.repository.io.FileOp;
import com.android.resources.Density;
import com.android.resources.ScreenSize;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.Storage;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.utils.NullLogger;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jetbrains.annotations.NotNull;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.ANDROID_SDK;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.AVD_MANAGER;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.DEVICE_SELECTED;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.SYSTEM_IMAGE;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;

public final class CreateAvdVisualPanel3 extends JPanel implements DocumentListener {

    private final WizardDescriptor wiz;
    private final CreateAvdWizardPanel3 panel;
    private Device selectedDevice;
    private SystemImageDescription selectedImage;
    private AvdManager avdManager;
    private AndroidSdk defaultSdk;

    /**
     * Creates new form CreateAvdVisualPanel2
     */
    public CreateAvdVisualPanel3(WizardDescriptor wiz, CreateAvdWizardPanel3 panel) {
        initComponents();
        this.panel = panel;
        this.wiz = wiz;

    }

    private void initAvdName() {
        if (selectedDevice != null) {
            avdName.setText(uniquifyDisplayName(String.format(Locale.getDefault(), "%1$s API %2$s", selectedDevice.getDisplayName(), getSelectedApiString())));
            deviceName.setText(selectedDevice.getDisplayName());
            if (HardwareConfigHelper.isWear(selectedDevice)) {
                deviceName.setIcon(CreateAvdVisualPanel1.ICON_WEAR);
            } else if (CreateAvdVisualPanel1.isTv(selectedDevice)) {
                deviceName.setIcon(CreateAvdVisualPanel1.ICON_TV);
            } else if (CreateAvdVisualPanel1.isTablet(selectedDevice)) {
                deviceName.setIcon(CreateAvdVisualPanel1.ICON_TABLET);
            } else if (CreateAvdVisualPanel1.isPhone(selectedDevice)) {
                deviceName.setIcon(CreateAvdVisualPanel1.ICON_MOBILE);
            } else {
                deviceName.setIcon(null);
            }
            imageName.setText(CreateAvdVisualPanel2.releaseDisplayName(selectedImage));
        }

    }

    private void initPlaystore() {
        boolean enable = !isPlayStoreCompatible();
        performanceCores.setEnabled(enable);
        performanceGraphics.setEnabled(enable);
        performanceMulticore.setEnabled(enable);
        memoryRam.setEnabled(enable);
        memoryHeap.setEnabled(enable);
        sdcardExternal.setEnabled(enable);
        sdcardManaged.setEnabled(enable);
        sdcardPath.setEnabled(enable);
        sdcardSelect.setEnabled(enable);
        sdcardSize.setEnabled(enable);
        skinCombo.setEnabled(enable);
        skinSelect.setEnabled(enable);

    }

    private void updateAvdId() {
        if (selectedDevice != null) {
            avdId.setText(cleanAvdName(avdName.getText(), true));
        }

    }

    private void initMemory() {
        memoryHeap.setValue(calculateInitialVmHeap());
        memoryRam.setValue((int)selectedDevice.getDefaultHardware().getRam().getSizeAsUnit(Storage.Unit.MiB));
        memoryStorage.setValue((int)selectedDevice.getDefaultHardware().getInternalStorage().get(0).getSizeAsUnit(Storage.Unit.MiB));
    }

    @Override
    public String getName() {
        return "Verify Configuration";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        avdName = new javax.swing.JTextField();
        avdId = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        deviceName = new javax.swing.JLabel();
        imageName = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        avdOrientation = new javax.swing.JComboBox<>();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cameraFront = new javax.swing.JComboBox<>();
        cameraBack = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        networkSpeed = new javax.swing.JComboBox<>();
        networkLatency = new javax.swing.JComboBox<>();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        performanceGraphics = new javax.swing.JComboBox<>();
        performanceMulticore = new javax.swing.JCheckBox();
        performanceCores = new javax.swing.JComboBox<>();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        memoryRam = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        memoryHeap = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        memoryStorage = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        sdcardManaged = new javax.swing.JRadioButton();
        sdcardSize = new javax.swing.JSpinner();
        jLabel22 = new javax.swing.JLabel();
        sdcardExternal = new javax.swing.JRadioButton();
        sdcardPath = new javax.swing.JTextField();
        sdcardSelect = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel21 = new javax.swing.JLabel();
        deviceFrame = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        skinCombo = new javax.swing.JComboBox<>();
        skinSelect = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel24 = new javax.swing.JLabel();
        keyboard = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel1.text")); // NOI18N

        avdName.setText(org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.avdName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(avdId, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.avdId.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel2.text")); // NOI18N

        deviceName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/android/avd/manager/device-phone.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(deviceName, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.deviceName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(imageName, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.imageName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel4.text")); // NOI18N

        avdOrientation.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Portrait", "Landscape" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel6.text")); // NOI18N

        cameraFront.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Emulated", "None" }));

        cameraBack.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Emulated", "None" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel10.text")); // NOI18N

        networkSpeed.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Full", "LTE", "HSDPA", "UMTS", "EDGE", "GPRS", "HSCSD", "GSM" }));

        networkLatency.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "UMTS", "EDGE", "GPRS" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel12.text")); // NOI18N

        performanceGraphics.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Automatic", "Hardware - GLES 2.0", "Software - GLES 2.0" }));
        performanceGraphics.setEnabled(false);

        performanceMulticore.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(performanceMulticore, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.performanceMulticore.text")); // NOI18N
        performanceMulticore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performanceMulticoreActionPerformed(evt);
            }
        });

        performanceCores.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6" }));
        performanceCores.setSelectedIndex(3);
        performanceCores.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel13.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel14.text")); // NOI18N

        memoryRam.setModel(new javax.swing.SpinnerNumberModel(2048, null, null, 1));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel15.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel16.text")); // NOI18N

        memoryHeap.setModel(new javax.swing.SpinnerNumberModel(2048, null, null, 1));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel17.text")); // NOI18N

        memoryStorage.setModel(new javax.swing.SpinnerNumberModel(512, null, null, 1));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel18.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel19.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel20, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel20.text")); // NOI18N

        buttonGroup1.add(sdcardManaged);
        sdcardManaged.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(sdcardManaged, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.sdcardManaged.text")); // NOI18N

        sdcardSize.setModel(new javax.swing.SpinnerNumberModel(512, null, null, 1));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel22, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel22.text")); // NOI18N

        buttonGroup1.add(sdcardExternal);
        org.openide.awt.Mnemonics.setLocalizedText(sdcardExternal, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.sdcardExternal.text")); // NOI18N
        sdcardExternal.setEnabled(false);

        sdcardPath.setText(org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.sdcardPath.text")); // NOI18N
        sdcardPath.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(sdcardSelect, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.sdcardSelect.text")); // NOI18N
        sdcardSelect.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel21, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel21.text")); // NOI18N

        deviceFrame.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(deviceFrame, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.deviceFrame.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel23, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel23.text")); // NOI18N

        skinCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        skinCombo.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(skinSelect, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.skinSelect.text")); // NOI18N
        skinSelect.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel24, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel24.text")); // NOI18N

        keyboard.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(keyboard, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.keyboard.text")); // NOI18N
        keyboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(avdName)
                                    .addComponent(avdId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jSeparator1)
                            .addComponent(jSeparator2)
                            .addComponent(jSeparator3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(deviceName)
                                    .addComponent(imageName)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(avdOrientation, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel5))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jSeparator6)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cameraFront, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cameraBack, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel8))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(networkSpeed, 0, 194, Short.MAX_VALUE)
                            .addComponent(networkLatency, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel11))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(167, 167, 167)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(performanceMulticore)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(performanceCores, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(performanceGraphics, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel13))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(261, 261, 261)
                        .addComponent(memoryStorage, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jLabel18))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(261, 261, 261)
                        .addComponent(memoryHeap, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jLabel17))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(261, 261, 261)
                        .addComponent(memoryRam, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jLabel15))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(132, 132, 132)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel14)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sdcardManaged)
                            .addComponent(sdcardExternal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(sdcardSize, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel22))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(sdcardPath, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sdcardSelect))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator7)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator8)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(skinCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(deviceFrame))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(skinSelect)
                        .addGap(155, 155, 155))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addGap(138, 138, 138)
                        .addComponent(keyboard)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(avdName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(avdId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deviceName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(avdOrientation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cameraFront, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cameraBack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel9)
                    .addComponent(networkSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel10)
                    .addComponent(networkLatency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(performanceGraphics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(performanceMulticore)
                    .addComponent(performanceCores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel14)
                    .addComponent(memoryRam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel16)
                    .addComponent(memoryHeap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(memoryStorage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(sdcardManaged)
                    .addComponent(sdcardSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sdcardExternal)
                    .addComponent(sdcardPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sdcardSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(deviceFrame)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(skinCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(skinSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(keyboard))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {sdcardPath, sdcardSelect});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {skinCombo, skinSelect});

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void performanceMulticoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performanceMulticoreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_performanceMulticoreActionPerformed

    private void keyboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keyboardActionPerformed

    boolean valid() {
        return true;
    }

    void storeSettings() {
        wiz.putProperty(CreateAvdWizardIterator.AVD_DISPLAY_NAME, avdName.getText());
        wiz.putProperty(CreateAvdWizardIterator.AVD_ID_NAME, avdId.getText());
        Map<String, String> hardwareProperties = new HashMap<>();
        hardwareProperties.put("fastboot.chosenSnapshotFile", "");
        hardwareProperties.put("runtime.network.speed", ((String)networkSpeed.getSelectedItem()).toLowerCase());
        hardwareProperties.put("vm.heapSize", ""+memoryHeap.getValue());
        hardwareProperties.put("skin.dynamic", "yes");
        File skinFile = selectedDevice.getDefaultHardware().getSkinFile();
        String skinPath = defaultSdk.getSdkPath()+File.separator+"skins"+File.separator+skinFile;
        hardwareProperties.put("skin.path", skinPath);
        hardwareProperties.put("hw.initialOrientation", ((String)avdOrientation.getSelectedItem()));
        hardwareProperties.put("showDeviceFrame", deviceFrame.isSelected()?"yes":"no");
        hardwareProperties.put("hw.camera.back", ((String)cameraBack.getSelectedItem()).toLowerCase());
        hardwareProperties.put("AvdId", avdId.getText());
        hardwareProperties.put("hw.camera.front", ((String)cameraFront.getSelectedItem()).toLowerCase());
        hardwareProperties.put("avd.ini.displayname", avdName.getText());
        hardwareProperties.put("hw.gpu.mode", "auto");
        hardwareProperties.put("fastboot.forceChosenSnapshotBoot", "no");
        hardwareProperties.put("fastboot.forceFastBoot", "yes");
        hardwareProperties.put("hw.ramSize", ""+memoryRam.getValue());
        hardwareProperties.put("fastboot.forceColdBoot", "no");
        hardwareProperties.put("hw.cpu.ncore", "4");
        hardwareProperties.put("hw.sdCard", "yes");
        hardwareProperties.put("runtime.network.latency", ((String)networkLatency.getSelectedItem()).toLowerCase());
        hardwareProperties.put("hw.keyboard", keyboard.isSelected()?"yes":"no");
        hardwareProperties.put("disk.dataPartition.size", "2G");
        hardwareProperties.put("hw.gpu.enabled", "yes");
        wiz.putProperty(CreateAvdWizardIterator.AVD_USER_HW_CONFIG, hardwareProperties);
        wiz.putProperty(CreateAvdWizardIterator.AVD_SDCARD, ""+sdcardSize.getValue()+"M");
    }

    void readSettings() {
        defaultSdk = (AndroidSdk) wiz.getProperty(ANDROID_SDK);
        avdManager = (AvdManager) wiz.getProperty(AVD_MANAGER);
        selectedDevice = (Device) wiz.getProperty(DEVICE_SELECTED);
        selectedImage = (SystemImageDescription) wiz.getProperty(SYSTEM_IMAGE);
        initAvdName();
        updateAvdId();
        avdName.getDocument().addDocumentListener(this);
     //   initPlaystore();
        initMemory();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel avdId;
    private javax.swing.JTextField avdName;
    private javax.swing.JComboBox<String> avdOrientation;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cameraBack;
    private javax.swing.JComboBox<String> cameraFront;
    private javax.swing.JCheckBox deviceFrame;
    private javax.swing.JLabel deviceName;
    private javax.swing.JLabel imageName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
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
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JCheckBox keyboard;
    private javax.swing.JSpinner memoryHeap;
    private javax.swing.JSpinner memoryRam;
    private javax.swing.JSpinner memoryStorage;
    private javax.swing.JComboBox<String> networkLatency;
    private javax.swing.JComboBox<String> networkSpeed;
    private javax.swing.JComboBox<String> performanceCores;
    private javax.swing.JComboBox<String> performanceGraphics;
    private javax.swing.JCheckBox performanceMulticore;
    private javax.swing.JRadioButton sdcardExternal;
    private javax.swing.JRadioButton sdcardManaged;
    private javax.swing.JTextField sdcardPath;
    private javax.swing.JButton sdcardSelect;
    private javax.swing.JSpinner sdcardSize;
    private javax.swing.JComboBox<String> skinCombo;
    private javax.swing.JButton skinSelect;
    // End of variables declaration//GEN-END:variables

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateAvdId();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateAvdId();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateAvdId();
    }

    //************methods from android studio*******************
    public boolean isPlayStoreCompatible() {
        return selectedDevice.hasPlayStore() && selectedImage.getSystemImage().hasPlayStore();
    }

    public static String getAvdDisplayName(AvdInfo avdInfo) {
        String displayName = avdInfo.getProperties().get(AvdManager.AVD_INI_DISPLAY_NAME);
        if (displayName == null) {
            displayName = avdInfo.getName().replaceAll("[_-]+", " ");
        }
        return displayName;
    }

    public String uniquifyDisplayName(String name) {
        int suffix = 1;
        String result = name;
        while (findAvdWithName(result)) {
            result = String.format("%1$s %2$d", name, ++suffix);
        }
        return result;
    }

    public boolean findAvdWithName(String name) {
        for (AvdInfo avd : getAvds(false)) {
            if (getAvdDisplayName(avd).equals(name)) {
                return true;
            }
        }
        return false;
    }

    public List<AvdInfo> getAvds(boolean forceRefresh) {
        if (forceRefresh) {
            try {
                avdManager.reloadAvds(new NullLogger());
            } catch (AndroidLocation.AndroidLocationException e) {
                Exceptions.printStackTrace(e);
            }
        }
        ArrayList<AvdInfo> avdInfos = Lists.newArrayList(avdManager.getAllAvds());
        boolean needsRefresh = false;
        for (AvdInfo info : avdInfos) {
            if (info.getStatus() == AvdInfo.AvdStatus.ERROR_DEVICE_CHANGED) {
                updateDeviceChanged(info);
                needsRefresh = true;
            }
        }
        if (needsRefresh) {
            return getAvds(true);
        } else {
            return avdInfos;
        }
    }

    public boolean updateDeviceChanged(@NotNull AvdInfo avdInfo) {
        try {
            return avdManager.updateDeviceChanged(avdInfo, new NullLogger()) != null;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return false;
    }

    public String cleanAvdName(String candidateBase, boolean uniquify) {
        candidateBase = AvdNameVerifier.stripBadCharactersAndCollapse(candidateBase);
        if (candidateBase.isEmpty()) {
            candidateBase = "myavd";
        }
        String candidate = candidateBase;
        if (uniquify) {
            int i = 1;
            while (avdManager.getAvd(candidate, false) != null) {
                candidate = String.format("%1$s_%2$d", candidateBase, i++);
            }
        }
        return candidate;
    }

    private String getSelectedApiString() {
        AndroidVersion version = selectedImage.getVersion();
        return version.getApiString();
    }

    public static boolean doesSystemImageSupportQemu2(SystemImageDescription description, FileOp fileOp) {
        if (description == null) {
            return false;
        }
        ISystemImage systemImage = description.getSystemImage();
        if (systemImage == null) {
            return false;
        }
        File location = systemImage.getLocation();
        if (!fileOp.isDirectory(location)) {
            return false;
        }
        String[] files = fileOp.list(location, null);
        if (files != null) {
            for (String filename : files) {
                if (filename.contains("kernel-ranchu")) {
                    return true;
                }
            }
        }
        return false;
    }

    private int calculateInitialVmHeap() {
        ScreenSize size = selectedDevice.getDefaultHardware().getScreen().getSize();
        Density density = selectedDevice.getDefaultHardware().getScreen().getPixelDensity();
        int vmHeapSize = 32;

        // These values are taken from Android 6.0 Compatibility
        // Definition (dated October 16, 2015), section 3.7,
        // Runtime Compatibility (with ANYDPI and NODPI defaulting
        // to MEDIUM).
        if (HardwareConfigHelper.isWear(selectedDevice)) {
            switch (density.getDpiValue()) {
                case 120:
                case 0xFFFE:
                case 0xFFFF:
                case 160:
                case 213:
                    vmHeapSize = 32;
                    break;
                case 240:
                case 280:
                    vmHeapSize = 36;
                    break;
                case 320:
                case 360:
                    vmHeapSize = 48;
                    break;
                case 400:
                    vmHeapSize = 56;
                    break;
                case 420:
                    vmHeapSize = 64;
                    break;
                case 480:
                    vmHeapSize = 88;
                    break;
                case 560:
                    vmHeapSize = 112;
                    break;
                case 640:
                    vmHeapSize = 154;
                    break;
            }
        } else {
            switch (size) {
                case SMALL:
                case NORMAL:
                    switch (density.getDpiValue()) {
                        case 120:
                        case 0xFFFE:
                        case 0xFFFF:
                        case 160:
                            vmHeapSize = 32;
                            break;
                        case 213:
                        case 240:
                        case 280:
                            vmHeapSize = 48;
                            break;
                        case 320:
                        case 360:
                            vmHeapSize = 80;
                            break;
                        case 400:
                            vmHeapSize = 96;
                            break;
                        case 420:
                            vmHeapSize = 112;
                            break;
                        case 480:
                            vmHeapSize = 128;
                            break;
                        case 560:
                            vmHeapSize = 192;
                            break;
                        case 640:
                            vmHeapSize = 256;
                            break;
                    }
                    break;
                case LARGE:
                    switch (density.getDpiValue()) {
                        case 120:
                            vmHeapSize = 32;
                            break;
                        case 0xFFFE:
                        case 0xFFFF:
                        case 160:
                            vmHeapSize = 48;
                            break;
                        case 213:
                        case 240:
                            vmHeapSize = 80;
                            break;
                        case 280:
                            vmHeapSize = 96;
                            break;
                        case 320:
                            vmHeapSize = 128;
                            break;
                        case 360:
                            vmHeapSize = 160;
                            break;
                        case 400:
                            vmHeapSize = 192;
                            break;
                        case 420:
                            vmHeapSize = 228;
                            break;
                        case 480:
                            vmHeapSize = 256;
                            break;
                        case 560:
                            vmHeapSize = 384;
                            break;
                        case 640:
                            vmHeapSize = 512;
                            break;
                    }
                    break;
                case XLARGE:
                    switch (density.getDpiValue()) {
                        case 120:
                            vmHeapSize = 48;
                            break;
                        case 0xFFFE:
                        case 0xFFFF:
                        case 160:
                            vmHeapSize = 80;
                            break;
                        case 213:
                        case 240:
                            vmHeapSize = 96;
                            break;
                        case 280:
                            vmHeapSize = 144;
                            break;
                        case 320:
                            vmHeapSize = 192;
                            break;
                        case 360:
                            vmHeapSize = 240;
                            break;
                        case 400:
                            vmHeapSize = 288;
                            break;
                        case 420:
                            vmHeapSize = 336;
                            break;
                        case 480:
                            vmHeapSize = 384;
                            break;
                        case 560:
                            vmHeapSize = 576;
                            break;
                        case 640:
                            vmHeapSize = 768;
                            break;
                    }
                    break;
            }
        }
        return vmHeapSize;
    }

}
