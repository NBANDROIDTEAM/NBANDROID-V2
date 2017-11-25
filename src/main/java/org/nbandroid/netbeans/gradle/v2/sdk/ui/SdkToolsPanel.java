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
package org.nbandroid.netbeans.gradle.v2.sdk.ui;

import com.android.repository.api.LocalPackage;
import com.android.repository.api.UpdatablePackage;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import org.nbandroid.netbeans.gradle.core.ui.*;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkPlatform;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkPlatformImpl;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkToolsChangeListener;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkToolsMultiPackageNode;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkToolsPackageNode;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkToolsRootNode;
import org.nbandroid.netbeans.gradle.v2.sdk.SdkToolsSupportNode;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;

/**
 * Visual Android SDK tools installer
 *
 * @author arsi
 */
public class SdkToolsPanel extends javax.swing.JPanel implements SdkToolsChangeListener {

    private AndroidSdkPlatform manager;
    private boolean detailsState;
    private OutlineModel model;
    private SdkToolsRootNode sdkToolsRootNode;
    private final JPopupMenu tableMenu = new JPopupMenu();
    private final JMenuItem installMenuItem = new JMenuItem("Install package");
    private final JMenuItem updateMenuItem = new JMenuItem("Update package");
    private final JMenuItem unInstallMenuItem = new JMenuItem("Uninstall package");
    public static final String SHOW_DETAILS = "SHOW_DETAILS";

    /**
     * Creates new form SdkToolsPanel
     */
    public SdkToolsPanel(AndroidSdkPlatformImpl platform) {
        initComponents();
        tableMenu.add(installMenuItem);
        tableMenu.add(updateMenuItem);
        tableMenu.add(unInstallMenuItem);
        packageTreeTableView.setRenderDataProvider(new PackageRenderer());
        detailsState = NbPreferences.forModule(SdkToolsPanel.class).getBoolean(SHOW_DETAILS, false);
        showDetails.setSelected(detailsState);
        showDetails.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                boolean selected = ((AbstractButton) e.getSource()).getModel().isSelected();
                if (detailsState != selected) {
                    detailsState = selected;
                    NbPreferences.forModule(SdkToolsPanel.class).putBoolean(SHOW_DETAILS, selected);
                    if (model != null && sdkToolsRootNode != null) {
                        sdkToolsRootNode.setFlatModel(!selected);
                        model = createModel(sdkToolsRootNode);
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
                if (sdkToolsRootNode != null && e.getButton() == 3 && e.getComponent() instanceof JTable) {
                    Object node = packageTreeTableView.getModel().getValueAt(rowindex, 0);
                    if (rowindex != -1) {
                        if (sdkToolsRootNode.isFlatModel()) {
                            if (node instanceof SdkToolsSupportNode) {
                                //Do nothing
                            } else if (node instanceof SdkToolsMultiPackageNode) {
                                if (((SdkToolsMultiPackageNode) node).getParent() instanceof SdkToolsSupportNode) {
                                    //Do nothing
                                } else if (!((SdkToolsMultiPackageNode) node).getNodes().isEmpty()) {
                                    UpdatablePackage aPackage = ((SdkToolsMultiPackageNode) node).getNodes().get(0).getPackage();
                                    updateMenuItem.setEnabled(aPackage.isUpdate());
                                    installMenuItem.setEnabled(!aPackage.hasLocal());
                                    unInstallMenuItem.setEnabled(aPackage.hasLocal());
                                    tableMenu.show(e.getComponent(), e.getX(), e.getY());
                                }

                            } else if (node instanceof SdkToolsPackageNode) {
                                updateMenuItem.setEnabled(((SdkToolsPackageNode) node).getPackage().isUpdate());
                                installMenuItem.setEnabled(!((SdkToolsPackageNode) node).getPackage().hasLocal());
                                unInstallMenuItem.setEnabled(((SdkToolsPackageNode) node).getPackage().hasLocal());
                                tableMenu.show(e.getComponent(), e.getX(), e.getY());
                            }

                        } else if (node instanceof SdkToolsSupportNode) {
                            //Do nothing

                        } else if (node instanceof SdkToolsMultiPackageNode) {
                            //Do nothing

                        } else if (node instanceof SdkToolsPackageNode) {
                            updateMenuItem.setEnabled(((SdkToolsPackageNode) node).getPackage().isUpdate());
                            installMenuItem.setEnabled(!((SdkToolsPackageNode) node).getPackage().hasLocal());
                            unInstallMenuItem.setEnabled(((SdkToolsPackageNode) node).getPackage().hasLocal());
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
                if (node instanceof SdkToolsMultiPackageNode) {
                    if (!((SdkToolsMultiPackageNode) node).getNodes().isEmpty()) {
                        UpdatablePackage aPackage = ((SdkToolsMultiPackageNode) node).getNodes().get(0).getPackage();
                        platform.installPackage(aPackage);
                    }

                } else if (node instanceof SdkToolsPackageNode) {
                    UpdatablePackage aPackage = ((SdkToolsPackageNode) node).getPackage();
                    platform.installPackage(aPackage);
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
                if (node instanceof SdkToolsMultiPackageNode) {
                    if (!((SdkToolsMultiPackageNode) node).getNodes().isEmpty()) {
                        UpdatablePackage aPackage = ((SdkToolsMultiPackageNode) node).getNodes().get(0).getPackage();
                        platform.installPackage(aPackage);
                    }

                } else if (node instanceof SdkToolsPackageNode) {
                    UpdatablePackage aPackage = ((SdkToolsPackageNode) node).getPackage();
                    platform.installPackage(aPackage);
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
                if (node instanceof SdkToolsMultiPackageNode) {
                    if (!((SdkToolsMultiPackageNode) node).getNodes().isEmpty()) {
                        LocalPackage local = ((SdkToolsMultiPackageNode) node).getNodes().get(0).getPackage().getLocal();
                        platform.uninstallPackage(local);
                    }

                } else if (node instanceof SdkToolsPackageNode) {
                    LocalPackage local = ((SdkToolsPackageNode) node).getPackage().getLocal();
                    platform.uninstallPackage(local);
                }
            }

        });
        platform.addSdkToolsChangeListener(WeakListeners.create(SdkToolsChangeListener.class, (SdkToolsChangeListener) this, platform));
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

        org.openide.awt.Mnemonics.setLocalizedText(showDetails, org.openide.util.NbBundle.getMessage(SdkToolsPanel.class, "SdkToolsPanel.showDetails.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(showDetails))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showDetails))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void packageListChanged(SdkToolsRootNode sdkToolsRootNode) {
        this.sdkToolsRootNode = sdkToolsRootNode;
        sdkToolsRootNode.setFlatModel(!showDetails.isSelected());
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                model = createModel(sdkToolsRootNode);
                //     packageTreeTableView.setRenderDataProvider(new SdkPlatformPanel.PackageRenderer());
                packageTreeTableView.setRootVisible(false);
                packageTreeTableView.setModel(model);
            }

        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
    }


    private class PackageRenderer implements RenderDataProvider {

        @Override
        public String getDisplayName(Object o) {
            return o.toString();
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        @Override
        public Color getBackground(Object o) {
            return null;
        }

        @Override
        public Color getForeground(Object o) {
            return null;
        }

        @Override
        public String getTooltipText(Object o) {
            return o.toString();
        }

        @Override
        public Icon getIcon(Object o) {
            //first support node
            if (o instanceof SdkToolsSupportNode) {
                return new ImageIcon(IconProvider.IMG_FOLDER_SUPPORT);
            } else if (o instanceof SdkToolsMultiPackageNode) {
                if (!((SdkToolsMultiPackageNode) o).getNodes().isEmpty()) {
                    if (((SdkToolsMultiPackageNode) o).getNodes().get(0).getPackage().isUpdate()) {
                        return new ImageIcon(IconProvider.IMG_FOLDER_UPDATE);
                    } else if (((SdkToolsMultiPackageNode) o).getNodes().get(0).getPackage().hasLocal()) {
                        return new ImageIcon(IconProvider.IMG_FOLDER_LOCAL);
                    } else {
                        return new ImageIcon(IconProvider.IMG_FOLDER_REMOTE);
                    }
                } else {
                    return new ImageIcon(IconProvider.IMG_FOLDER_LOCAL);
                    //TODO another icon?
                }

            } else if (o instanceof SdkToolsPackageNode) {
                if (((SdkToolsPackageNode) o).getPackage().isUpdate()) {
                    return new ImageIcon(IconProvider.IMG_UPDATE);
                } else if (((SdkToolsPackageNode) o).getPackage().hasLocal()) {
                    return new ImageIcon(IconProvider.IMG_LOCAL);
                } else {
                    return new ImageIcon(IconProvider.IMG_REMOTE);
                }
            }
            return null;
        }

    }

    private OutlineModel createModel(SdkToolsRootNode rootNode) {
        return DefaultOutlineModel.createOutlineModel(new DefaultTreeModel(rootNode), new RowModel() {
            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueFor(Object node, int column) {
                if (node instanceof SdkToolsSupportNode) {
                    return null;
                } else if (node instanceof SdkToolsMultiPackageNode) {
                    if (!((SdkToolsMultiPackageNode) node).getNodes().isEmpty()) {
                        return ((SdkToolsMultiPackageNode) node).getNodes().get(0).getPackage().getRepresentative().getVersion().toString();
                    }
                } else if (node instanceof SdkToolsPackageNode) {
                    return ((SdkToolsPackageNode) node).getPackage().getRepresentative().getVersion().toString();
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
                        return "Version";
                    default:
                        return "Err";
                }
            }
        }, false, "Name");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private org.nbandroid.netbeans.gradle.core.ui.PackageTreeTableView packageTreeTableView;
    private javax.swing.JCheckBox showDetails;
    // End of variables declaration//GEN-END:variables
}
