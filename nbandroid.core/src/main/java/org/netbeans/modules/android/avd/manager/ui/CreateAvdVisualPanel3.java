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
import com.android.sdklib.internal.avd.GpuMode;
import com.android.utils.NullLogger;
import com.google.common.collect.Lists;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.jetbrains.annotations.NotNull;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.ANDROID_SDK;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.AVD_MANAGER;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.DEVICE_SELECTED;
import static org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator.SYSTEM_IMAGE;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

public final class CreateAvdVisualPanel3 extends JPanel implements DocumentListener, ItemListener {

    static final int MAX_NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors() / 2;
    static final int RECOMMENDED_NUMBER_OF_CORES = Integer.min(4, MAX_NUMBER_OF_CORES);

    private final WizardDescriptor wiz;
    private final CreateAvdWizardPanel3 panel;
    private Device selectedDevice;
    private SystemImageDescription selectedImage;
    private AvdManager avdManager;
    private AndroidSdk defaultSdk;
    private String defaultSkinPath;

    /**
     * Creates new form CreateAvdVisualPanel2
     */
    public CreateAvdVisualPanel3(WizardDescriptor wiz, CreateAvdWizardPanel3 panel) {
        initComponents();
        this.panel = panel;
        this.wiz = wiz;
        sdcardExternal.addItemListener(this);
        sdcardExternal.addItemListener(this);

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
        memoryRam.setValue((int) selectedDevice.getDefaultHardware().getRam().getSizeAsUnit(Storage.Unit.MiB));
        memoryStorage.setValue((int) selectedDevice.getDefaultHardware().getInternalStorage().get(0).getSizeAsUnit(Storage.Unit.MiB));
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
        performanceCores = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();

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

        performanceGraphics.setModel(new DefaultComboBoxModel<GpuMode>(GpuMode.values())
        );

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

        sdcardSize.setModel(new javax.swing.SpinnerNumberModel(512, 16, null, 1));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel22, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel22.text")); // NOI18N

        buttonGroup1.add(sdcardExternal);
        org.openide.awt.Mnemonics.setLocalizedText(sdcardExternal, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.sdcardExternal.text")); // NOI18N

        sdcardPath.setText(org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.sdcardPath.text")); // NOI18N
        sdcardPath.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(sdcardSelect, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.sdcardSelect.text")); // NOI18N
        sdcardSelect.setEnabled(false);
        sdcardSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sdcardSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel21, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel21.text")); // NOI18N

        deviceFrame.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(deviceFrame, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.deviceFrame.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel23, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel23.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(skinSelect, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.skinSelect.text")); // NOI18N
        skinSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel24, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel24.text")); // NOI18N

        keyboard.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(keyboard, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.keyboard.text")); // NOI18N
        keyboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardActionPerformed(evt);
            }
        });

        performanceCores.setModel(new javax.swing.SpinnerNumberModel(4, 1, 5, 1));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel3.class, "CreateAvdVisualPanel3.jLabel3.text")); // NOI18N

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
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(performanceGraphics, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(performanceCores, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
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
                    .addComponent(performanceCores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
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

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {performanceCores, performanceGraphics});

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

    private void keyboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keyboardActionPerformed

    private void sdcardSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sdcardSelectActionPerformed
        // TODO add your handling code here:
        FileChooserBuilder builder = new FileChooserBuilder(CreateAvdVisualPanel3.class);
        builder.setFilesOnly(true);
        File sdcardImage = builder.showOpenDialog();
        if (sdcardImage != null && sdcardImage.exists()) {
            try (FileImageInputStream fi = new FileImageInputStream(sdcardImage)) {
                byte[] boot = new byte[3];
                fi.read(boot);
                if (boot[0] == ((byte) 0xeb) && boot[1] == ((byte) 0x5a) && boot[2] == ((byte) 0x90)) {
                    sdcardPath.setText(sdcardImage.getAbsolutePath());
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation("<html>"
                            + "Signature of selected file does not match Android SD Card image.<br/>"
                            + "Are you sure you want to use the selected file?", "SD Card image problem...",
                            NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                    Object notify = DialogDisplayer.getDefault().notify(nd);
                    if (NotifyDescriptor.YES_OPTION.equals(notify)) {
                        sdcardPath.setText(sdcardImage.getAbsolutePath());
                    }
                }

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }//GEN-LAST:event_sdcardSelectActionPerformed

    private void skinSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinSelectActionPerformed
        // TODO add your handling code here:
        FileChooserBuilder builder = new FileChooserBuilder("ANDROID_SKIN");
        builder.setDirectoriesOnly(true);
        builder.setTitle("Select skin directory...");
        File skinFolder = builder.showOpenDialog();
        if (skinFolder != null && skinFolder.exists()) {
            File layout = new File(skinFolder.getAbsolutePath() + File.separator + "layout");
            if (layout.exists()) {
                skinCombo.setModel(new SkinsComboboxModel(new File(skinFolder.getParent())));
                skinCombo.setSelectedItem(skinFolder);
            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Message("The selected directory does not contain a skin!", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }//GEN-LAST:event_skinSelectActionPerformed

    boolean valid() {
        return true;
    }

    void storeSettings() {
        wiz.putProperty(CreateAvdWizardIterator.AVD_DISPLAY_NAME, avdName.getText());
        wiz.putProperty(CreateAvdWizardIterator.AVD_ID_NAME, avdId.getText());
        Map<String, String> hardwareProperties = new HashMap<>();
        hardwareProperties.put("fastboot.chosenSnapshotFile", "");
        hardwareProperties.put("runtime.network.speed", ((String) networkSpeed.getSelectedItem()).toLowerCase());
        hardwareProperties.put("vm.heapSize", "" + memoryHeap.getValue());
        hardwareProperties.put("skin.dynamic", "yes");
        File skinFile = (File) skinCombo.getSelectedItem();
        hardwareProperties.put("skin.path", skinFile.getAbsolutePath());
        hardwareProperties.put("hw.initialOrientation", ((String) avdOrientation.getSelectedItem()));
        hardwareProperties.put("showDeviceFrame", deviceFrame.isSelected() ? "yes" : "no");
        hardwareProperties.put("hw.camera.back", ((String) cameraBack.getSelectedItem()).toLowerCase());
        hardwareProperties.put("AvdId", avdId.getText());
        hardwareProperties.put("hw.camera.front", ((String) cameraFront.getSelectedItem()).toLowerCase());
        hardwareProperties.put("avd.ini.displayname", avdName.getText());
        hardwareProperties.put("hw.gpu.mode", ((GpuMode) performanceGraphics.getSelectedItem()).getGpuSetting());
        hardwareProperties.put("fastboot.forceChosenSnapshotBoot", "no");
        hardwareProperties.put("fastboot.forceFastBoot", "yes");
        hardwareProperties.put("hw.ramSize", "" + memoryRam.getValue());
        hardwareProperties.put("fastboot.forceColdBoot", "no");
        hardwareProperties.put("hw.cpu.ncore", "" + ((Number) performanceCores.getValue()).intValue());
        hardwareProperties.put("hw.sdCard", "yes");
        hardwareProperties.put("runtime.network.latency", ((String) networkLatency.getSelectedItem()).toLowerCase());
        hardwareProperties.put("hw.keyboard", keyboard.isSelected() ? "yes" : "no");
        hardwareProperties.put("disk.dataPartition.size", "2G");
        hardwareProperties.put("hw.gpu.enabled", "yes");
        wiz.putProperty(CreateAvdWizardIterator.AVD_USER_HW_CONFIG, hardwareProperties);
        if (sdcardManaged.isSelected() || "".equals(sdcardPath.getText()) || !new File(sdcardPath.getText()).exists()) {
            wiz.putProperty(CreateAvdWizardIterator.AVD_SDCARD, "" + sdcardSize.getValue() + "M");
        } else {
            wiz.putProperty(CreateAvdWizardIterator.AVD_SDCARD, sdcardPath.getText());
        }
    }

    void readSettings() {
        defaultSdk = (AndroidSdk) wiz.getProperty(ANDROID_SDK);
        avdManager = (AvdManager) wiz.getProperty(AVD_MANAGER);
        selectedDevice = (Device) wiz.getProperty(DEVICE_SELECTED);
        selectedImage = (SystemImageDescription) wiz.getProperty(SYSTEM_IMAGE);
        initAvdName();
        updateAvdId();
        defaultSkinPath = defaultSdk.getSdkPath() + File.separator + "skins";
        File skinFile = selectedDevice.getDefaultHardware().getSkinFile();
        String skinPath = defaultSdk.getSdkPath() + File.separator + "skins" + File.separator + skinFile;
        skinCombo.setModel(new SkinsComboboxModel(new File(defaultSkinPath)));
        skinCombo.setSelectedItem(new File(skinPath));
        skinCombo.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
                if ((component instanceof JLabel) && (value instanceof File)) {
                    ((JLabel) component).setText(((File) value).getName());
                }
                return component;
            }

        });
        performanceGraphics.setSelectedItem(GpuMode.AUTO);
        avdName.getDocument().addDocumentListener(this);
        performanceCores.setModel(new javax.swing.SpinnerNumberModel(RECOMMENDED_NUMBER_OF_CORES, 1, MAX_NUMBER_OF_CORES, 1));
        //   initPlaystore();
        initMemory();
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
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JCheckBox keyboard;
    private javax.swing.JSpinner memoryHeap;
    private javax.swing.JSpinner memoryRam;
    private javax.swing.JSpinner memoryStorage;
    private javax.swing.JComboBox<String> networkLatency;
    private javax.swing.JComboBox<String> networkSpeed;
    private javax.swing.JSpinner performanceCores;
    private javax.swing.JComboBox<GpuMode> performanceGraphics;
    private javax.swing.JRadioButton sdcardExternal;
    private javax.swing.JRadioButton sdcardManaged;
    private javax.swing.JTextField sdcardPath;
    private javax.swing.JButton sdcardSelect;
    private javax.swing.JSpinner sdcardSize;
    private javax.swing.JComboBox<File> skinCombo;
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

    @Override
    public void itemStateChanged(ItemEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                sdcardSize.setEnabled(sdcardManaged.isSelected());
                sdcardPath.setEnabled(!sdcardManaged.isSelected());
                sdcardSelect.setEnabled(!sdcardManaged.isSelected());
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
    }

}
