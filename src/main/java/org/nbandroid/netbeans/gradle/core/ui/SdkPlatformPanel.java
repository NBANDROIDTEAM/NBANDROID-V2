/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.core.ui;

import com.android.repository.api.UpdatablePackage;
import com.android.sdklib.repository.meta.DetailsTypes;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidVersionNode;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkManager;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkPackageNode;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkPlatformChangeListener;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkPlatformPackagesRootNode;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Visual Android SDK platform installer
 *
 * @author arsi
 */
public class SdkPlatformPanel extends javax.swing.JPanel implements ExplorerManager.Provider, SdkPlatformChangeListener {

    private final ExplorerManager explorerManager = new ExplorerManager();
    private SdkManager manager = null;
    private OutlineModel model = null;
    private SdkPlatformPackagesRootNode platformPackages = null;
    public static final String SHOW_DETAILS = "SHOW_DETAILS";
    private boolean detailsState;
    private final JPopupMenu tableMenu = new JPopupMenu();
    private final JMenuItem installMenuItem = new JMenuItem("Install package");
    private final JMenuItem updateMenuItem = new JMenuItem("Update package");
    private final JMenuItem unInstallMenuItem = new JMenuItem("Uninstall package");


    /**
     * Creates new form SdkPlatformPanel
     */
    public SdkPlatformPanel() {
        initComponents();
        tableMenu.add(installMenuItem);
        tableMenu.add(updateMenuItem);
        tableMenu.add(unInstallMenuItem);
        detailsState = NbPreferences.forModule(SdkPlatformPanel.class).getBoolean(SHOW_DETAILS, false);
        showDetails.setSelected(detailsState);
        showDetails.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean selected = ((AbstractButton) e.getSource()).getModel().isSelected();
                if (detailsState != selected) {
                    detailsState = selected;
                    NbPreferences.forModule(SdkPlatformPanel.class).putBoolean(SHOW_DETAILS, selected);
                    if (model != null && platformPackages != null) {
                        platformPackages.setFlatModel(!selected);
                        model = createModel(platformPackages);
                        packageTreeTableView.setModel(model);
                    }
                }
            }
        });
        packageTreeTableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = packageTreeTableView.rowAtPoint(e.getPoint());
                if (r >= 0 && r < packageTreeTableView.getRowCount()) {
                    packageTreeTableView.setRowSelectionInterval(r, r);
                } else {
                    packageTreeTableView.clearSelection();
                }

                int rowindex = packageTreeTableView.getSelectedRow();
                if (rowindex < 0) {
                    return;
                }
                if (platformPackages != null && e.getButton() == 3 && e.getComponent() instanceof JTable) {
                    Object node = packageTreeTableView.getModel().getValueAt(rowindex, 0);
                    if (rowindex != -1) {
                        if (platformPackages.isFlatModel()) {
                            if (node instanceof AndroidVersionNode) {
                                Vector<SdkPackageNode> packages = ((AndroidVersionNode) node).getPackages();
                                for (SdkPackageNode pkg : packages) {
                                    if (pkg.getPackage().getRepresentative().getTypeDetails() instanceof DetailsTypes.PlatformDetailsType) {
                                        updateMenuItem.setEnabled(pkg.getPackage().isUpdate());
                                        installMenuItem.setEnabled(!pkg.getPackage().hasLocal());
                                        unInstallMenuItem.setEnabled(pkg.getPackage().hasLocal());
                                        break;
                                    } else {
                                        updateMenuItem.setEnabled(false);
                                        installMenuItem.setEnabled(false);
                                        unInstallMenuItem.setEnabled(false);
                                    }
                                }
                                tableMenu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        } else if (node instanceof SdkPackageNode) {
                            updateMenuItem.setEnabled(((SdkPackageNode) node).getPackage().isUpdate());
                            installMenuItem.setEnabled(!((SdkPackageNode) node).getPackage().hasLocal());
                            unInstallMenuItem.setEnabled(((SdkPackageNode) node).getPackage().hasLocal());
                            tableMenu.show(e.getComponent(), e.getX(), e.getY());
                        }

                    }

                }
            }
        });
        installMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowindex = packageTreeTableView.getSelectedRow();
                if (rowindex < 0) {
                    return;
                }
                Object node = packageTreeTableView.getModel().getValueAt(rowindex, 0);
                if (node instanceof AndroidVersionNode) {
                    Vector<SdkPackageNode> packages = ((AndroidVersionNode) node).getPackages();
                    for (SdkPackageNode pkg : packages) {
                        if (pkg.getPackage().getRepresentative().getTypeDetails() instanceof DetailsTypes.PlatformDetailsType) {
                            SdkManager.getDefault().installPackage(pkg.getPackage());
                            break;
                        }
                    }

                } else if (node instanceof SdkPackageNode) {
                    SdkManager.getDefault().installPackage(((SdkPackageNode) node).getPackage());
                }
            }
        });

        updateMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowindex = packageTreeTableView.getSelectedRow();
                if (rowindex < 0) {
                    return;
                }
                Object node = packageTreeTableView.getModel().getValueAt(rowindex, 0);
                if (node instanceof AndroidVersionNode) {
                    Vector<SdkPackageNode> packages = ((AndroidVersionNode) node).getPackages();
                    for (SdkPackageNode pkg : packages) {
                        if (pkg.getPackage().getRepresentative().getTypeDetails() instanceof DetailsTypes.PlatformDetailsType) {
                            SdkManager.getDefault().installPackage(pkg.getPackage());
                            break;
                        }
                    }

                } else if (node instanceof SdkPackageNode) {
                    SdkManager.getDefault().installPackage(((SdkPackageNode) node).getPackage());

                }
            }
        });
        unInstallMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowindex = packageTreeTableView.getSelectedRow();
                if (rowindex < 0) {
                    return;
                }
                Object node = packageTreeTableView.getModel().getValueAt(rowindex, 0);
                if (node instanceof AndroidVersionNode) {
                    Vector<SdkPackageNode> packages = ((AndroidVersionNode) node).getPackages();
                    for (SdkPackageNode pkg : packages) {
                        if (pkg.getPackage().getRepresentative().getTypeDetails() instanceof DetailsTypes.PlatformDetailsType) {
                            SdkManager.getDefault().uninstallPackage(pkg.getPackage().getLocal());
                            break;
                        }
                    }

                } else if (node instanceof SdkPackageNode) {
                    SdkManager.getDefault().uninstallPackage(((SdkPackageNode) node).getPackage().getLocal());
                }
            }

        });
    }


    public void connect(SdkManager manager) {
        this.manager = manager;
        if (manager != null) {
            manager.addSdkPlatformChangeListener(this);
        }
    }

    public void disconnect() {
        if (manager != null) {
            manager.removeSdkPlatformChangeListener(this);
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
        packageTreeTableView = new org.nbandroid.netbeans.gradle.core.ui.PackageTreeTableView();
        showDetails = new javax.swing.JCheckBox();

        jScrollPane1.setViewportView(packageTreeTableView);

        org.openide.awt.Mnemonics.setLocalizedText(showDetails, org.openide.util.NbBundle.getMessage(SdkPlatformPanel.class, "SdkPlatformPanel.showDetails.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showDetails)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showDetails))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private org.nbandroid.netbeans.gradle.core.ui.PackageTreeTableView packageTreeTableView;
    private javax.swing.JCheckBox showDetails;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    private class PackageRenderer implements RenderDataProvider {

        @Override
        public java.awt.Color getBackground(Object o) {
            return null;
        }

        @Override
        public String getDisplayName(Object o) {
            return o.toString();
        }

        @Override
        public java.awt.Color getForeground(Object o) {
            return Color.BLACK;
        }

        @Override
        public javax.swing.Icon getIcon(Object o) {
            if (o instanceof SdkPackageNode) {
                UpdatablePackage aPackage = ((SdkPackageNode) o).getPackage();
                if (aPackage.isUpdate()) {
                    return new ImageIcon(IconProvider.IMG_UPDATE);
                } else if (aPackage.hasLocal()) {
                    return new ImageIcon(IconProvider.IMG_LOCAL);
                } else {
                    return new ImageIcon(IconProvider.IMG_REMOTE);
                }
            } else if (o instanceof AndroidVersionNode) {
                if (((AndroidVersionNode) o).isFlatModel()) {
                    Vector<SdkPackageNode> packages = ((AndroidVersionNode) o).getPackages();
                    for (SdkPackageNode pkg : packages) {
                        if (pkg.getPackage().getRepresentative().getTypeDetails() instanceof DetailsTypes.PlatformDetailsType) {
                            if (pkg.getPackage().isUpdate()) {
                                return new ImageIcon(IconProvider.IMG_UPDATE);
                            } else if (pkg.getPackage().hasLocal()) {
                                return new ImageIcon(IconProvider.IMG_LOCAL);
                            } else {
                                return new ImageIcon(IconProvider.IMG_REMOTE);
                            }
                        }
                    }
                    return new ImageIcon(IconProvider.IMG_REMOTE);
                } else {
                    Vector<SdkPackageNode> packages = ((AndroidVersionNode) o).getPackages();
                    for (SdkPackageNode pkg : packages) {
                        if (pkg.getPackage().getRepresentative().getTypeDetails() instanceof DetailsTypes.PlatformDetailsType) {
                            if (pkg.getPackage().isUpdate()) {
                                return new ImageIcon(IconProvider.IMG_FOLDER_UPDATE);
                            } else if (pkg.getPackage().hasLocal()) {
                                return new ImageIcon(IconProvider.IMG_FOLDER_LOCAL);
                            } else {
                                return new ImageIcon(IconProvider.IMG_FOLDER_REMOTE);
                            }
                        }
                    }
                    return new ImageIcon(IconProvider.IMG_REMOTE);
                }
            }
            return null;
        }

        @Override
        public String getTooltipText(Object o) {
            if (o instanceof SdkPackageNode) {
                UpdatablePackage aPackage = ((SdkPackageNode) o).getPackage();
                if (aPackage.isUpdate()) {
                    return "Update available:" + aPackage.getRemote().getVersion().getMajor();
                } else if (aPackage.hasLocal()) {
                    return "Installed";
                } else {
                    return "Not installed";
                }
            } else {
                return o.toString();
            }
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }
    }

    @Override
    public void packageListChanged(SdkPlatformPackagesRootNode platformPackages) {
        this.platformPackages = platformPackages;
        platformPackages.setFlatModel(!showDetails.isSelected());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                model = createModel(platformPackages);
                packageTreeTableView.setRenderDataProvider(new PackageRenderer());
                packageTreeTableView.setRootVisible(false);
                packageTreeTableView.setModel(model);
            }

        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
    }

    private OutlineModel createModel(SdkPlatformPackagesRootNode pkgs) {
        return DefaultOutlineModel.createOutlineModel(new DefaultTreeModel(pkgs), new RowModel() {
            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getValueFor(Object node, int column) {
                if (node instanceof AndroidVersionNode) {
                    if (((AndroidVersionNode) node).isFlatModel()) {
                        switch (column) {
                            case 0: {
                                return ((AndroidVersionNode) node).getVersion().getApiLevel();
                            }
                            case 1: {
                                int major = 0;
                                Vector<SdkPackageNode> packages = ((AndroidVersionNode) node).getPackages();
                                for (SdkPackageNode pkg : packages) {
                                    if (pkg.getPackage().getRepresentative().getTypeDetails() instanceof DetailsTypes.PlatformDetailsType) {
                                        return pkg.getPackage().getRepresentative().getVersion().getMajor();
                                    }
                                    major = pkg.getPackage().getRepresentative().getVersion().getMajor();
                                }
                                return major;
                            }
                        }
                    } else {
                        return null;
                    }

                } else if (node instanceof SdkPackageNode) {
                    switch (column) {
                        case 0: {
                            return ((AndroidVersionNode) ((SdkPackageNode) node).getParent()).getVersion().getApiLevel();
                        }
                        case 1: {
                            return ((SdkPackageNode) node).getPackage().getRepresentative().getVersion().getMajor();
                        }
                    }
                }
                return "Err";
            }

            @Override
            public Class getColumnClass(int column) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(Object node, int column) {
                return false;
            }

            @Override
            public void setValueFor(Object node, int column, Object value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getColumnName(int column) {
                switch (column) {
                    case 0:
                        return "API";
                    case 1:
                        return "REV";
                    default:
                        return "Err";
                }
            }
        }, false, "Name");
    }

}
