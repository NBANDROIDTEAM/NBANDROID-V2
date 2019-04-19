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
import com.android.repository.io.FileUtilKt;
import com.android.resources.Density;
import com.android.sdklib.SdkVersionInfo;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.DeviceManager;
import com.android.sdklib.devices.Storage;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.repository.IdDisplay;
import com.android.sdklib.repository.meta.DetailsTypes;
import com.android.sdklib.repository.targets.SystemImage;
import com.android.sdklib.repository.targets.SystemImageManager;
import com.android.utils.ILogger;
import com.google.common.collect.Multimap;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
import org.nbandroid.netbeans.gradle.core.sdk.SdkLogProvider;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;

/**
 *
 * @author arsi
 */
public class AvdManager extends javax.swing.JPanel {

    public static boolean showCustomizer() {
        AvdManager customizer
                = new AvdManager();
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
        return true;
    }
    private com.android.sdklib.internal.avd.AvdManager avdManager;
    private DeviceManager deviceManager;

    /**
     * Creates new form AvdManager
     */
    public AvdManager() {
        initComponents();
        AndroidSdk defaultSdk = AndroidSdkProvider.getDefaultSdk();
        if (defaultSdk != null) {
            final ILogger sdkLog = SdkLogProvider.createLogger(false);
            try {
                avdManager = com.android.sdklib.internal.avd.AvdManager.getInstance(defaultSdk.getAndroidSdkHandler(), sdkLog);
                deviceManager = DeviceManager.createInstance(defaultSdk.getAndroidSdkHandler(), sdkLog);
            } catch (AndroidLocation.AndroidLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            SystemImageManager systemImageManager = defaultSdk.getSystemImageManager();
            RepoManager repoManager = defaultSdk.getRepoManager();
            if (systemImageManager != null && repoManager != null && avdManager != null) {
                AvdInfo[] validAvds = avdManager.getValidAvds();
                Collection<SystemImage> images = systemImageManager.getImages();
                table.setModel(new ExistingDevicesTableModel(validAvds));
                Multimap<LocalPackage, SystemImage> imageMap = systemImageManager.getImageMap();
                RepositoryPackages packages = repoManager.getPackages();
                Set<RemotePackage> newPkgs = packages.getNewPkgs();
                for (RemotePackage info : newPkgs) {
                    if (hasSystemImage(info)) {
                        String path = info.getPath();
                        System.out.println("org.netbeans.modules.android.avd.manager.AvdManager.<init>()");
                    }
                }

            }
        }
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

        public ExistingDevicesTableModel(AvdInfo[] avdInfos) {
            this.avdInfos = avdInfos;
        }

        @Override
        public int getRowCount() {
            return avdInfos.length;
        }

        @Override
        public int getColumnCount() {
            return 8;
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
                default:
                    return "Err";
            }
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
        jPanel1 = new javax.swing.JPanel();

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
        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 123, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
