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
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.DeviceManager;
import com.android.sdklib.devices.DeviceParser;
import com.google.common.collect.Table;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ImageUtilities;

public final class CreateAvdVisualPanel1 extends JPanel implements ListSelectionListener, MouseListener {

    private final WizardDescriptor wiz;
    private final DeviceManager deviceManager;
    private List<Device> mobileDevices;
    private List<Device> tabletDevices;
    private List<Device> tvDevices;
    private List<Device> wearDevices;
    private static final DecimalFormat DF = new DecimalFormat(".##");
    private static final double PHONE_SIZE_CUTOFF = 6.0;
    private static final double TV_SIZE_CUTOFF = 15.0;
    private DevicesTableModel modelMobile;
    private DevicesTableModel modelTablet;
    private DevicesTableModel modelTv;
    private DevicesTableModel modelWear;
    private Device selectedDevice;
    public static final Icon ICON_MOBILE = new javax.swing.ImageIcon(CreateAvdVisualPanel1.class.getResource("/org/netbeans/modules/android/avd/manager/device-phone.png"));
    public static final Icon ICON_TABLET = new javax.swing.ImageIcon(CreateAvdVisualPanel1.class.getResource("/org/netbeans/modules/android/avd/manager/device-tablet.png"));
    public static final Icon ICON_TV = new javax.swing.ImageIcon(CreateAvdVisualPanel1.class.getResource("/org/netbeans/modules/android/avd/manager/device-tv_large.png"));
    public static final Icon ICON_WEAR = new javax.swing.ImageIcon(CreateAvdVisualPanel1.class.getResource("/org/netbeans/modules/android/avd/manager/device-wear_large.png"));
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final JMenuItem editMenu = new JMenuItem("Edit");
    private final CreateAvdWizardPanel1 wizPanel;
    private List<Device> userDevices = new ArrayList<>();

    /**
     * Creates new form CreateAvdVisualPanel1
     */
    public CreateAvdVisualPanel1(WizardDescriptor wiz, CreateAvdWizardPanel1 wizPanel) {
        initComponents();
        this.wizPanel = wizPanel;
        devName.setText("");
        devName.setIcon(null);
        devSize.setText("");
        devRatio.setText("");
        devDensity.setText("");
        devDiagonal.setText("");
        devWidth.setText("");
        devHeight.setText("");
        this.wiz = wiz;
        deviceManager = (DeviceManager) wiz.getProperty(CreateAvdWizardIterator.DEVICE_MANAGER);
        tableMobile.getSelectionModel().addListSelectionListener(this);
        tableTablet.getSelectionModel().addListSelectionListener(this);
        tableTv.getSelectionModel().addListSelectionListener(this);
        tableWear.getSelectionModel().addListSelectionListener(this);
        refreshDevices();
        installPopupMenu();
    }

    private void refreshDevices() {
        List<Device> allDevices = new ArrayList<>(deviceManager.getDevices(EnumSet.of(DeviceManager.DeviceFilter.DEFAULT, DeviceManager.DeviceFilter.USER, DeviceManager.DeviceFilter.VENDOR, DeviceManager.DeviceFilter.SYSTEM_IMAGES)));
        userDevices = new ArrayList<>(deviceManager.getDevices(EnumSet.of(DeviceManager.DeviceFilter.USER)));
        tvDevices = allDevices.stream().filter((t) -> {
            return isTv(t);
        }).collect(Collectors.toList());
        allDevices.removeAll(tvDevices);
        wearDevices = allDevices.stream().filter((t) -> {
            return HardwareConfigHelper.isWear(t);
        }).collect(Collectors.toList());
        allDevices.removeAll(wearDevices);
        mobileDevices = allDevices.stream().filter((t) -> {
            return isPhone(t);
        }).collect(Collectors.toList());
        allDevices.removeAll(mobileDevices);
        tabletDevices = allDevices.stream().filter((t) -> {
            return isTablet(t);
        }).collect(Collectors.toList());
        allDevices.removeAll(tabletDevices);
        modelMobile = new DevicesTableModel(mobileDevices);
        modelTablet = new DevicesTableModel(tabletDevices);
        modelTv = new DevicesTableModel(tvDevices);
        modelWear = new DevicesTableModel(wearDevices);
        tableMobile.setModel(modelMobile);
        tableTablet.setModel(modelTablet);
        tableTv.setModel(modelTv);
        tableWear.setModel(modelWear);
    }

    public static boolean isPhone(Device d) {
        return d.getDefaultHardware().getScreen().getDiagonalLength() < PHONE_SIZE_CUTOFF;
    }

    public static boolean isTablet(Device d) {
        return d.getDefaultHardware().getScreen().getDiagonalLength() >= PHONE_SIZE_CUTOFF;
    }

    public static boolean isTv(Device d) {
        return HardwareConfigHelper.isTv(d) || d.getDefaultHardware().getScreen().getDiagonalLength() >= TV_SIZE_CUTOFF;
    }

    public static String getDiagonalSize(Device device) {
        return DF.format(device.getDefaultHardware().getScreen().getDiagonalLength()) + '"';
    }

    public static String getDimensionString(Device device) {
        Dimension size = device.getScreenSize(device.getDefaultState().getOrientation());
        return size == null ? "Unknown Resolution" : String.format(Locale.getDefault(), "%dx%d", size.width, size.height);
    }

    public static String getDensityString(Device device) {
        return device.getDefaultHardware().getScreen().getPixelDensity().getResourceValue();
    }

    @Override
    public String getName() {
        return "Choose a device definition";
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            Object source = e.getSource();
            if (tableMobile.getSelectionModel().equals(source)) {
                if (tableMobile.getSelectedRow() > -1) {
                    selectedDevice = modelMobile.getDevices().get(tableMobile.getRowSorter().convertRowIndexToModel(tableMobile.getSelectedRow()));
                    devName.setIcon(ICON_MOBILE);
                    tableTablet.clearSelection();
                    tableTv.clearSelection();
                    tableWear.clearSelection();
                }

            } else if (tableTablet.getSelectionModel().equals(source)) {
                if (tableTablet.getSelectedRow() > -1) {
                    selectedDevice = modelTablet.getDevices().get(tableTablet.getRowSorter().convertRowIndexToModel(tableTablet.getSelectedRow()));
                    devName.setIcon(ICON_TABLET);
                    tableMobile.clearSelection();
                    tableTv.clearSelection();
                    tableWear.clearSelection();
                }

            } else if (tableTv.getSelectionModel().equals(source)) {
                if (tableTv.getSelectedRow() > -1) {
                    selectedDevice = modelTv.getDevices().get(tableTv.getRowSorter().convertRowIndexToModel(tableTv.getSelectedRow()));
                    devName.setIcon(ICON_TV);
                    tableTablet.clearSelection();
                    tableMobile.clearSelection();
                    tableWear.clearSelection();
                }

            } else if (tableWear.getSelectionModel().equals(source)) {
                if (tableWear.getSelectedRow() > -1) {
                    selectedDevice = modelWear.getDevices().get(tableWear.getRowSorter().convertRowIndexToModel(tableWear.getSelectedRow()));
                    devName.setIcon(ICON_WEAR);
                    tableTablet.clearSelection();
                    tableTv.clearSelection();
                    tableMobile.clearSelection();
                }

            }
            if (selectedDevice != null) {
                devName.setText(selectedDevice.getDisplayName());
                devSize.setText("Size: " + getDiagonalSize(selectedDevice));
                devRatio.setText("Ratio: " + selectedDevice.getDefaultHardware().getScreen().getRatio().getShortDisplayValue());
                devDensity.setText("Density: " + selectedDevice.getDefaultHardware().getScreen().getPixelDensity().getShortDisplayValue());
                devDiagonal.setText(getDiagonalSize(selectedDevice));
                devWidth.setText(selectedDevice.getDefaultHardware().getScreen().getXDimension() + "px");
                devHeight.setText(selectedDevice.getDefaultHardware().getScreen().getYDimension() + "px");
            } else {
                devName.setText("");
                devName.setIcon(null);
                devSize.setText("");
                devRatio.setText("");
                devDensity.setText("");
                devDiagonal.setText("");
                devWidth.setText("");
                devHeight.setText("");
            }
            wizPanel.refreshWizard();
        }
    }

    public boolean valid() {
        return selectedDevice != null;
    }

    private void installPopupMenu() {
        popupMenu.add(editMenu);
        editMenu.setAction(new AbstractAction("Modify device") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedDevice != null && userDevices.contains(selectedDevice)) {
                    AvdHwProfile.showDeviceProfiler(selectedDevice);
                }
            }
        });
        tableMobile.addMouseListener(this);
        tableTablet.addMouseListener(this);
        tableTv.addMouseListener(this);
        tableWear.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Component comp = e.getComponent();
            if (comp instanceof JTable) {
                JTable table = (JTable) comp;
                int rowIndex = table.rowAtPoint(e.getPoint());
                table.setRowSelectionInterval(rowIndex, rowIndex);
                 editMenu.setEnabled(selectedDevice!=null && userDevices.contains(selectedDevice));
            }
            popupMenu.show(comp, e.getX(), e.getY());
        }
    }

    void storeSettings() {
        wiz.putProperty(CreateAvdWizardIterator.DEVICE_SELECTED, selectedDevice);
    }

    void readSettings() {
    }

    private static class DevicesTableModel extends AbstractTableModel {

        private final List<Device> devices;

        public List<Device> getDevices() {
            return devices;
        }

        public DevicesTableModel(List<Device> devices) {
            this.devices = devices;
        }

        @Override
        public int getRowCount() {
            return devices.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 1:
                    return Icon.class;
                default:
                    return String.class;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Play Store";
                case 2:
                    return "Size";
                case 3:
                    return "Resolution";
                case 4:
                    return "Density";
                default:
                    return "Err";
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Device device = devices.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return device.getDisplayName();
                case 1:
                    if (device.hasPlayStore()) {
                        return new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/android/avd/manager/device-play-store.png"));
                    }
                    return null;
                case 2:
                    return getDiagonalSize(device);
                case 3:
                    return getDimensionString(device);
                case 4:
                    return getDensityString(device);
                default:
                    return "Err";
            }
        }

    }

    private void unimplemented() {
        NotifyDescriptor nd = new NotifyDescriptor.Message("The feature is not yet implemented", NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(nd);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableMobile = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableTablet = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableTv = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableWear = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        devName = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        devDiagonal = new javax.swing.JLabel();
        devWidth = new javax.swing.JLabel();
        devHeight = new javax.swing.JLabel();
        devSize = new javax.swing.JLabel();
        devRatio = new javax.swing.JLabel();
        devDensity = new javax.swing.JLabel();
        cloneButton = new javax.swing.JButton();
        createProfileButton = new javax.swing.JButton();
        importProfileButton = new javax.swing.JButton();

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        tableMobile.setAutoCreateRowSorter(true);
        tableMobile.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableMobile.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableMobile.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tableMobile);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.jPanel2.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/android/avd/manager/device-phone.png")), jPanel2); // NOI18N

        tableTablet.setAutoCreateRowSorter(true);
        tableTablet.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableTablet.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableTablet.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(tableTablet);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.jPanel3.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/android/avd/manager/device-tablet.png")), jPanel3); // NOI18N

        tableTv.setAutoCreateRowSorter(true);
        tableTv.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableTv.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableTv.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(tableTv);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.jPanel4.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/android/avd/manager/device-tv_large.png")), jPanel4); // NOI18N

        tableWear.setAutoCreateRowSorter(true);
        tableWear.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableWear.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableWear.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(tableWear);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.jPanel5.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/android/avd/manager/device-wear_large.png")), jPanel5); // NOI18N

        devName.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        devName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/android/avd/manager/device-phone.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(devName, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.devName.text")); // NOI18N

        jPanel6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        devDiagonal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(devDiagonal, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.devDiagonal.text")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(devDiagonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(devDiagonal)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        devWidth.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(devWidth, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.devWidth.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(devHeight, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.devHeight.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(devSize, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.devSize.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(devRatio, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.devRatio.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(devDensity, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.devDensity.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cloneButton, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.cloneButton.text")); // NOI18N
        cloneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloneButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(devWidth, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                                .addGap(0, 0, 0)
                                .addComponent(devHeight))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(devSize)))
                        .addGap(0, 20, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(devName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(devRatio)
                                    .addComponent(devDensity))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cloneButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(devName)
                .addGap(40, 40, 40)
                .addComponent(devWidth)
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(devHeight)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addComponent(devSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(devRatio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(devDensity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cloneButton)
                .addGap(6, 6, 6))
        );

        org.openide.awt.Mnemonics.setLocalizedText(createProfileButton, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.createProfileButton.text")); // NOI18N
        createProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createProfileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(importProfileButton, org.openide.util.NbBundle.getMessage(CreateAvdVisualPanel1.class, "CreateAvdVisualPanel1.importProfileButton.text")); // NOI18N
        importProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importProfileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(createProfileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importProfileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createProfileButton)
                    .addComponent(importProfileButton))
                .addGap(6, 6, 6))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createProfileButtonActionPerformed
        // TODO add your handling code here:
        unimplemented();
    }//GEN-LAST:event_createProfileButtonActionPerformed

    private void importProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importProfileButtonActionPerformed
        // TODO add your handling code here:
        FileChooserBuilder builder = new FileChooserBuilder("android-devices");
        builder.setFilesOnly(true);
        builder.setFileFilter(new FileNameExtensionFilter("Android devide definition", "xml", "XML"));
        File[] files = builder.showMultiOpenDialog();
        if (files != null) {
            for (File file : files) {
                try {
                    Table<String, String, Device> devices = DeviceParser.parse(file);
                    Map<String, Map<String, Device>> rowMap = devices.rowMap();
                    System.out.println("org.netbeans.modules.android.avd.manager.ui.CreateAvdVisualPanel1.importProfileButtonActionPerformed()");
                } catch (Exception ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message("Error while parsing Android device definition!", NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
        }
    }//GEN-LAST:event_importProfileButtonActionPerformed

    private void cloneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cloneButtonActionPerformed
        // TODO add your handling code here:
        unimplemented();
    }//GEN-LAST:event_cloneButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cloneButton;
    private javax.swing.JButton createProfileButton;
    private javax.swing.JLabel devDensity;
    private javax.swing.JLabel devDiagonal;
    private javax.swing.JLabel devHeight;
    private javax.swing.JLabel devName;
    private javax.swing.JLabel devRatio;
    private javax.swing.JLabel devSize;
    private javax.swing.JLabel devWidth;
    private javax.swing.JButton importProfileButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable tableMobile;
    private javax.swing.JTable tableTablet;
    private javax.swing.JTable tableTv;
    private javax.swing.JTable tableWear;
    // End of variables declaration//GEN-END:variables
}
