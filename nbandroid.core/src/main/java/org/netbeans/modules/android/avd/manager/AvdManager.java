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
package org.netbeans.modules.android.avd.manager;

import com.android.prefs.AndroidLocation;
import com.android.repository.api.LocalPackage;
import com.android.repository.api.RemotePackage;
import com.android.repository.api.RepoManager;
import com.android.repository.api.RepoPackage;
import com.android.repository.impl.meta.RepositoryPackages;
import com.android.repository.impl.meta.TypeDetails;
import com.android.repository.io.FileOp;
import com.android.repository.io.FileOpUtils;
import com.android.repository.io.FileUtilKt;
import com.android.resources.Density;
import com.android.sdklib.SdkVersionInfo;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.DeviceManager;
import com.android.sdklib.devices.Storage;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.repository.AndroidSdkHandler;
import com.android.sdklib.repository.IdDisplay;
import com.android.sdklib.repository.meta.DetailsTypes;
import com.android.sdklib.repository.targets.SystemImage;
import com.android.sdklib.repository.targets.SystemImageManager;
import com.android.utils.ILogger;
import com.android.utils.NullLogger;
import com.google.common.collect.Multimap;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
import org.nbandroid.netbeans.gradle.configs.LaunchConfigurationBean;
import org.nbandroid.netbeans.gradle.core.sdk.SdkLogProvider;
import org.nbandroid.netbeans.gradle.launch.EmulatorLauncher;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.android.avd.manager.pojo.Devices;
import org.netbeans.modules.android.avd.manager.ui.CreateAvdWizardIterator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author arsi
 */
public class AvdManager extends javax.swing.JPanel {

    public static boolean showCustomizer() {
        AndroidSdk defaultSdk = AndroidSdkProvider.getDefaultSdk();
        if (defaultSdk != null) {
            final ILogger sdkLog = SdkLogProvider.createLogger(false);
            AndroidSdkHandler androidSdkHandler = null;
            com.android.sdklib.internal.avd.AvdManager avdManager = null;
            DeviceManager deviceManager = null;
            SystemImageManager systemImageManager = null;
            RepoManager repoManager = null;
            try {
                androidSdkHandler = defaultSdk.getAndroidSdkHandler();
                avdManager = com.android.sdklib.internal.avd.AvdManager.getInstance(androidSdkHandler, sdkLog);
                deviceManager = DeviceManager.createInstance(androidSdkHandler, sdkLog);
                systemImageManager = defaultSdk.getSystemImageManager();
                repoManager = defaultSdk.getRepoManager();
            } catch (AndroidLocation.AndroidLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (avdManager != null && deviceManager != null && systemImageManager != null && repoManager != null) {
                AvdManager customizer
                        = new AvdManager(defaultSdk, avdManager, deviceManager, systemImageManager, repoManager);
                javax.swing.JButton close = new javax.swing.JButton("Close");
                close.getAccessibleContext().setAccessibleDescription("N/A");
                DialogDescriptor descriptor = new DialogDescriptor(customizer, "Android AVD Manager", true, new Object[]{close}, close, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("org.netbeans.modules.android.avd.manager.AvdManager"), null); // NOI18N
                Dialog dlg = null;
                try {
                    dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                    dlg.setVisible(true);
                } finally {
                    if (dlg != null) {
                        dlg.dispose();
                    }
                }
            } else if (avdManager == null) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(""
                        + "Unable to create AVD Manager instance!", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            } else if (deviceManager == null) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(""
                        + "Unable to create Device Manager instance!", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            } else if (systemImageManager == null) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(""
                        + "Unable to create System Image Manager instance!", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            } else if (repoManager == null) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(""
                        + "Unable to create Repository Manager instance!", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        } else {
            //sdk not found
            NotifyDescriptor nd = new NotifyDescriptor.Message(""
                    + "Default Android SDK not found! Please install at least one  Android SDK..", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        }
        return true;
    }
    private final com.android.sdklib.internal.avd.AvdManager avdManager;
    private final DeviceManager deviceManager;
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final JMenuItem runMenu = new JMenuItem("Run");
    private final JMenuItem stopMenu = new JMenuItem("Stop");
    private final JMenuItem wipeMenu = new JMenuItem("Wipe");
    private ExistingDevicesTableModel model;
    private EmulatorLauncher emulatorLauncher;
    private final AndroidSdk defaultSdk;
    private final SystemImageManager systemImageManager;
    private final RepoManager repoManager;
    private final RequestProcessor RP = new RequestProcessor("AVD Manager", 1);
    private Devices deviceDefs;

    private EmulatorLauncher getEmulatorLauncher() {
        if (emulatorLauncher == null) {
            emulatorLauncher = new EmulatorLauncher(defaultSdk);
        }
        return emulatorLauncher;
    }

    /**
     * Creates new form AvdManager
     */
    public AvdManager(AndroidSdk defaultSdk, com.android.sdklib.internal.avd.AvdManager avdManager, DeviceManager deviceManager, SystemImageManager systemImageManager, RepoManager repoManager) {
        initComponents();
        initTableActions();
        this.defaultSdk = defaultSdk;
        this.avdManager = avdManager;
        this.deviceManager = deviceManager;
        this.systemImageManager = systemImageManager;
        this.repoManager = repoManager;
        AvdInfo[] validAvds = avdManager.getValidAvds();
        Collection<SystemImage> images = systemImageManager.getImages();
        model = new ExistingDevicesTableModel(validAvds);
        table.setModel(model);
        Multimap<LocalPackage, SystemImage> imageMap = systemImageManager.getImageMap();
        RepositoryPackages packages = repoManager.getPackages();
        Set<RemotePackage> newPkgs = packages.getNewPkgs();
        for (RemotePackage info : newPkgs) {
            if (hasSystemImage(info)) {
                String path = info.getPath();
                System.out.println("org.netbeans.modules.android.avd.manager.AvdManager.<init>()");
            }
        }
        deviceDefs = AndroidAvdDevicesLocator.getDeviceDefs();
    }

    private void initTableActions() {
        //RUN
        popupMenu.add(runMenu);
        runMenu.setAction(new AbstractAction("Run Emulator") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    AvdInfo avdInfo = model.getAvdInfos()[selectedRow];
                    LaunchConfigurationBean lcb = new LaunchConfigurationBean();
                    lcb.setLaunchAction(LaunchConfiguration.Action.DO_NOTHING);
                    getEmulatorLauncher().launchEmulator(avdInfo, lcb);
                    RP.schedule(() -> {
                        model.refresh();
                    }, 5, TimeUnit.SECONDS);
                }
            }
        });
        //Stop
        popupMenu.add(stopMenu);
        stopMenu.setAction(new AbstractAction("Stop Emulator") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    AvdInfo avdInfo = model.getAvdInfos()[selectedRow];
                    avdManager.stopAvd(avdInfo);
                    RP.schedule(() -> {
                        model.refresh();
                    }, 5, TimeUnit.SECONDS);
                }
            }
        });
        //Wipe
        popupMenu.add(wipeMenu);
        wipeMenu.setAction(new AbstractAction("Wipe Data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    AvdInfo avdInfo = model.getAvdInfos()[selectedRow];
                    wipeUserData(avdInfo);
                    RP.schedule(() -> {
                        model.refresh();
                    }, 5, TimeUnit.SECONDS);
                }
            }
        });
        //
        table.setComponentPopupMenu(popupMenu);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int rowAtPoint = table.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table));
                        if (rowAtPoint > -1) {
                            table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                            //Only one instance can run
                            boolean running = false;
                            for (int i = 0; i < model.avdInfos.length; i++) {
                                boolean avdRunning = avdManager.isAvdRunning(model.avdInfos[i], new NullLogger());
                                if (avdRunning) {
                                    running = true;
                                }
                            }
                            runMenu.setEnabled(!running);
                            boolean avdRunning = avdManager.isAvdRunning(model.avdInfos[rowAtPoint], new NullLogger());
                            stopMenu.setEnabled(avdRunning);
                            wipeMenu.setEnabled(!avdRunning);
                        } else {
                            runMenu.setEnabled(false);
                            stopMenu.setEnabled(false);
                            wipeMenu.setEnabled(false);
                        }

                    }
                });
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
    }

    String getResolution(AvdInfo info) {
        Device device = deviceManager.getDevice(info.getDeviceName(), info.getDeviceManufacturer());
        Dimension res = null;
        Density density = null;
        if (device != null) {
            res = device.getScreenSize(device.getDefaultState().getOrientation());
            density = device.getDefaultHardware().getScreen().getPixelDensity();
        }
        String resolution;
        String densityString = density == null ? "Unknown Density" : density.getResourceValue();
        if (res != null) {
            resolution = String.format(Locale.getDefault(), "%1$d \u00D7 %2$d: %3$s", res.width, res.height, densityString);
        } else {
            resolution = "Unknown Resolution";
        }
        return resolution;
    }

    public static final List<IdDisplay> TAGS_WITH_GOOGLE_API = ImmutableList.of(SystemImage.GOOGLE_APIS_TAG, SystemImage.GOOGLE_APIS_X86_TAG,
            SystemImage.PLAY_STORE_TAG, SystemImage.TV_TAG, SystemImage.WEAR_TAG, SystemImage.CHROMEOS_TAG);

    static boolean hasSystemImage(RepoPackage p) {
        TypeDetails details = p.getTypeDetails();
        if (!(details instanceof DetailsTypes.ApiDetailsType)) {
            return false;
        }
        int apiLevel = ((DetailsTypes.ApiDetailsType) details).getApiLevel();
        if (details instanceof DetailsTypes.SysImgDetailsType) {
            return true;
        }
        // Platforms up to 13 included a bundled system image
        if (details instanceof DetailsTypes.PlatformDetailsType && apiLevel <= 13) {
            return true;
        }
        // Google APIs addons up to 19 included a bundled system image
        if (details instanceof DetailsTypes.AddonDetailsType && ((DetailsTypes.AddonDetailsType) details).getVendor().getId().equals("google")
                && TAGS_WITH_GOOGLE_API.contains(((DetailsTypes.AddonDetailsType) details).getTag()) && apiLevel <= 19) {
            return true;
        }

        return false;
    }

    private class ExistingDevicesTableModel extends AbstractTableModel {

        private final AvdInfo[] avdInfos;

        public AvdInfo[] getAvdInfos() {
            return avdInfos;
        }

        public ExistingDevicesTableModel(AvdInfo[] avdInfos) {
            this.avdInfos = avdInfos;
        }

        @Override
        public int getRowCount() {
            return avdInfos.length;
        }

        @Override
        public int getColumnCount() {
            return 9;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Type";
                case 1:
                    return "Name";
                case 2:
                    return "Play Store";
                case 3:
                    return "Resolution";
                case 4:
                    return "API";
                case 5:
                    return "Target";
                case 6:
                    return "CPU/ABI";
                case 7:
                    return "Size on Disk";
                case 8:
                    return "Status";
                default:
                    return "err";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 2:
                    return Icon.class;
                default:
                    return String.class;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AvdInfo avdInfo = avdInfos[rowIndex];
            Map<String, String> properties = avdInfo.getProperties();
            switch (columnIndex) {
                case 0:
                    return new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/android/avd/manager/" + getDeviceClassIcon(avdInfo)));
                case 1:
                    return avdInfo.getDeviceName();
                case 2:
                    if (avdInfo.hasPlayStore()) {
                        return new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/android/avd/manager/device-play-store.png"));
                    }
                    return null;
                case 3:
                    return getResolution(avdInfo);
                case 4:
                    return avdInfo.getAndroidVersion().getApiString();
                case 5:
                    return SdkVersionInfo.getVersionWithCodename(avdInfo.getAndroidVersion());
                case 6:
                    return avdInfo.getAbiType();
                case 7:
                    return storageSizeDisplayString(getSize(avdInfo));
                case 8:
                    boolean avdRunning = avdManager.isAvdRunning(avdInfo, new NullLogger());
                    if (avdRunning) {
                        return "Running";
                    }
                    return "Turned off";

                default:
                    return "Err";
            }
        }

        public void refresh() {
            fireTableDataChanged();
        }

    }

    private Storage getSize(AvdInfo avdInfo) {
        long sizeInBytes = 0;
        if (avdInfo != null) {
            File avdDir = new File(avdInfo.getDataFolderPath());
            try {
                sizeInBytes = FileUtilKt.recursiveSize(avdDir.toPath());
            } catch (IOException ee) {
                // Just leave the size as zero
            }
        }
        return new Storage(sizeInBytes);
    }

    static String getDeviceClassIcon(AvdInfo info) {
        String id = info.getTag().getId();
        String path;
        if (id.contains("android-")) {
            path = String.format("device-%s_large.png", id.substring("android-".length()));
        } else {
            // Phone/tablet
            path = "device-mobile_large.png";
        }
        return path;
    }

    public static String storageSizeDisplayString(Storage size) {
        String unitString = "MB";
        double value = size.getPreciseSizeAsUnit(Storage.Unit.MiB);
        if (value >= 1024.0) {
            unitString = "GB";
            value = size.getPreciseSizeAsUnit(Storage.Unit.GiB);
        }
        if (value > 9.94) {
            return String.format(Locale.getDefault(), "%1$.0f %2$s", value, unitString);
        } else {
            return String.format(Locale.getDefault(), "%1$.1f %2$s", value, unitString);
        }
    }

    private final FileOp myFileOp = FileOpUtils.create();

    public boolean wipeUserData(AvdInfo avdInfo) {
        // Delete the current user data file
        File userdataImage = new File(avdInfo.getDataFolderPath(), com.android.sdklib.internal.avd.AvdManager.USERDATA_QEMU_IMG);
        if (myFileOp.exists(userdataImage)) {
            if (!myFileOp.delete(userdataImage)) {
                return false;
            }
        }
        // Delete the snapshots directory
        File snapshotDirectory = new File(avdInfo.getDataFolderPath(), com.android.sdklib.internal.avd.AvdManager.SNAPSHOTS_DIRECTORY);
        myFileOp.deleteFileOrFolder(snapshotDirectory);

        return true;
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
        table = new javax.swing.JTable();
        createDevice = new javax.swing.JButton();

        table.setModel(new javax.swing.table.DefaultTableModel(
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
        table.setRowHeight(30);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(createDevice, org.openide.util.NbBundle.getMessage(AvdManager.class, "AvdManager.createDevice.text")); // NOI18N
        createDevice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createDeviceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1083, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(createDevice)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(createDevice)
                .addGap(0, 12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createDeviceActionPerformed
        // TODO add your handling code here:
        WizardDescriptor wiz = new WizardDescriptor(new CreateAvdWizardIterator());
        wiz.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        wiz.putProperty(CreateAvdWizardIterator.DEVICE_MANAGER, deviceManager);
        wiz.setTitle("Create Virtual Device");
        wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(wiz);
        try {
            dlg.setVisible(true);
            if (wiz.getValue() == WizardDescriptor.FINISH_OPTION) {
//                Set result = wiz.getInstantiatedObjects();
                model.fireTableStructureChanged();
            }
        } finally {
            dlg.dispose();
            wiz.getInstantiatedObjects();
        }
    }//GEN-LAST:event_createDeviceActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createDevice;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
