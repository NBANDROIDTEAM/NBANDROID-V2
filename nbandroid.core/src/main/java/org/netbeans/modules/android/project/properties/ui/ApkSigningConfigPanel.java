/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.project.properties.ui;

import com.android.builder.model.SigningConfig;
import java.awt.Component;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.modules.android.apk.ApkUtils;
import org.netbeans.modules.android.apk.keystore.*;
import org.netbeans.modules.android.project.api.NbAndroidProject;
import org.netbeans.modules.android.project.keystore.KeystoreConfiguration;
import org.netbeans.modules.android.project.keystore.KeystoreConfiguration.CurrentKeystoreConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class ApkSigningConfigPanel extends javax.swing.JPanel implements ActionListener, KeyListener, SigningConfig {

    private DialogDescriptor descriptor = null;
    private final KeyEmulatorListener keyEmulatorListener = new KeyEmulatorListener();
    private final NbAndroidProject androidProjectImpl;
    private final KeystoreConfiguration keystoreConfiguration;
    private final KeystoreConfiguration.CurrentKeystoreConfiguration currentKeystoreConfiguration;
    private final KeystoreConfiguration.CurrentKeystoreConfiguration globalKeystoreConfiguration;
    private final KeystoreConfiguration.CurrentKeystoreConfiguration localKeystoreConfiguration;

    /**
     * Creates new form KeystoreSelector
     */
    public ApkSigningConfigPanel(ProjectCustomizer.Category category, Lookup context) {
        initComponents();
        androidProjectImpl = context.lookup(NbAndroidProject.class);
        if (androidProjectImpl != null) {
            keystoreConfiguration = androidProjectImpl.getLookup().lookup(KeystoreConfiguration.class);
            currentKeystoreConfiguration = keystoreConfiguration.getCurrentKeystoreConfiguration();
            globalKeystoreConfiguration = keystoreConfiguration.getGlobalKeystoreConfiguration();
            localKeystoreConfiguration = keystoreConfiguration.getLocalKeystoreConfiguration();
            if (currentKeystoreConfiguration.isUseGlobal()) {

                if (globalKeystoreConfiguration.isValid()) {
                    setupGlobalFound();
                } else {
                    setupGlobalNotFound();
                }
            } else if (true) {
                setupLocalFound();
            } else {
                setupLocalNotFound();
            }
            setupSigningOptions();
        } else {
            keystoreConfiguration = null;
            currentKeystoreConfiguration = null;
            globalKeystoreConfiguration = null;
            localKeystoreConfiguration = null;
        }

        if (category != null) {
            category.setStoreListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    save();
                }
            });
        }

        path.addKeyListener(this);
        alias.addKeyListener(this);
        keystorePassword.addKeyListener(this);
        keyPassword.addKeyListener(this);
        v1.addActionListener(keyEmulatorListener);
        v2.addActionListener(keyEmulatorListener);
        debug.addActionListener(keyEmulatorListener);
        release.addActionListener(keyEmulatorListener);
        keyReleased(null);
    }

    public void save() {
        CurrentKeystoreConfiguration tmp = null;
        if (!globalKeystore.isSelected()) {
            tmp = new CurrentKeystoreConfiguration(false, askBeforeSigningApk.isSelected(), path.getText(), new String(keyPassword.getPassword()), alias.getText(), new String(keyPassword.getPassword()), v1.isSelected(), v2.isSelected(), release.isSelected(), debug.isSelected(), rememberPasswd.isSelected());
        } else {
            tmp = new CurrentKeystoreConfiguration(true, askBeforeSigningApk.isSelected(), "", "", "", "", v1.isSelected(), v2.isSelected(), release.isSelected(), debug.isSelected(), rememberPasswd.isSelected());
        }
        keystoreConfiguration.saveToProject(tmp);
        if (saveAsGlobal.isSelected()) {
            keystoreConfiguration.saveToGlobal(tmp);
        }
    }

    private void setupGlobalNotFound() {
        globalNotFound.setVisible(true);
        globalKeystore.setSelected(true);
        saveAsGlobal.setEnabled(false);
        saveAsGlobal.setSelected(false);
        path.setEnabled(false);
        selectPath.setEnabled(false);
        createNew.setEnabled(false);
        keystorePassword.setEnabled(false);
        alias.setEnabled(false);
        changeAlias.setEnabled(false);
        keyPassword.setEnabled(false);
        rememberPasswd.setEnabled(false);
        path.setText("");
        keystorePassword.setText("");
        alias.setText("");
        keyPassword.setText("");

    }

    private void setupGlobalFound() {
        globalNotFound.setVisible(false);
        globalKeystore.setSelected(true);
        saveAsGlobal.setEnabled(false);
        saveAsGlobal.setSelected(false);
        path.setEnabled(false);
        selectPath.setEnabled(false);
        createNew.setEnabled(false);
        keystorePassword.setEnabled(false);
        alias.setEnabled(false);
        changeAlias.setEnabled(false);
        keyPassword.setEnabled(false);
        rememberPasswd.setEnabled(false);
        rememberPasswd.setSelected(globalKeystoreConfiguration.isRememberPassword());
        path.setText(globalKeystoreConfiguration.getKeystorePath());
        keystorePassword.setText(globalKeystoreConfiguration.getKeystorePassword());
        alias.setText(globalKeystoreConfiguration.getKeyAlias());
        keyPassword.setText(globalKeystoreConfiguration.getKeyPassword());
    }

    private void setupLocalFound() {
        globalNotFound.setVisible(false);
        globalKeystore.setSelected(false);
        saveAsGlobal.setEnabled(true);
        saveAsGlobal.setSelected(false);
        path.setEnabled(true);
        selectPath.setEnabled(true);
        createNew.setEnabled(true);
        keystorePassword.setEnabled(true);
        alias.setEnabled(true);
        changeAlias.setEnabled(true);
        keyPassword.setEnabled(true);
        rememberPasswd.setEnabled(true);
        rememberPasswd.setSelected(localKeystoreConfiguration.isRememberPassword());
        path.setText(localKeystoreConfiguration.getKeystorePath());
        keystorePassword.setText(localKeystoreConfiguration.getKeystorePassword());
        alias.setText(localKeystoreConfiguration.getKeyAlias());
        keyPassword.setText(localKeystoreConfiguration.getKeyPassword());

    }

    private void setupSigningOptions() {
        v1.setSelected(localKeystoreConfiguration.isApkV1());
        v2.setSelected(localKeystoreConfiguration.isApkV2());
        release.setSelected(localKeystoreConfiguration.isApkRelease());
        debug.setSelected(localKeystoreConfiguration.isApkDebug());
        askBeforeSigningApk.setSelected(localKeystoreConfiguration.isAsk());
    }

    private void setupLocalNotFound() {
        globalNotFound.setVisible(false);
        globalKeystore.setSelected(false);
        saveAsGlobal.setEnabled(true);
        saveAsGlobal.setSelected(false);
        path.setEnabled(true);
        selectPath.setEnabled(true);
        createNew.setEnabled(true);
        keystorePassword.setEnabled(true);
        alias.setEnabled(true);
        changeAlias.setEnabled(true);
        keyPassword.setEnabled(true);
        rememberPasswd.setEnabled(true);
        path.setText("");
        keystorePassword.setText("");
        alias.setText("");
        keyPassword.setText("");
    }

    private final class KeyEmulatorListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            keyReleased(null);
        }

    }

    public void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        keyReleased(null);
    }

    public boolean isRelease() {
        return release.isSelected();
    }

    public boolean isDebug() {
        return debug.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        selectPath = new javax.swing.JButton();
        changeAlias = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        alias = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        createNew = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        path = new javax.swing.JTextField();
        rememberPasswd = new javax.swing.JCheckBox();
        keystorePassword = new javax.swing.JPasswordField();
        keyPassword = new javax.swing.JPasswordField();
        globalKeystore = new javax.swing.JCheckBox();
        saveAsGlobal = new javax.swing.JCheckBox();
        globalNotFound = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        debug = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        v2 = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        release = new javax.swing.JRadioButton();
        v1 = new javax.swing.JRadioButton();
        askBeforeSigningApk = new javax.swing.JCheckBox();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectPath, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.selectPath.text")); // NOI18N
        selectPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPathActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(changeAlias, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.changeAlias.text")); // NOI18N
        changeAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeAliasActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jLabel1.text")); // NOI18N

        alias.setText(org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.alias.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(createNew, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.createNew.text")); // NOI18N
        createNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jLabel3.text")); // NOI18N

        path.setText(org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.path.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rememberPasswd, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.rememberPasswd.text")); // NOI18N

        keystorePassword.setText(org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.keystorePassword.text")); // NOI18N

        keyPassword.setText(org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.keyPassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(globalKeystore, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.globalKeystore.text")); // NOI18N
        globalKeystore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalKeystoreActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveAsGlobal, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.saveAsGlobal.text")); // NOI18N
        saveAsGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsGlobalActionPerformed(evt);
            }
        });

        globalNotFound.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/v2/sdk/ui/warning-badge.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(globalNotFound, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.globalNotFound.text")); // NOI18N
        globalNotFound.setToolTipText(org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.globalNotFound.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(444, 444, 444)
                        .addComponent(createNew))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(globalNotFound)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(globalKeystore)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keystorePassword)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(alias)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(changeAlias))
                                    .addComponent(keyPassword)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                                        .addComponent(selectPath))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(saveAsGlobal)
                                            .addComponent(rememberPasswd))
                                        .addGap(0, 0, Short.MAX_VALUE)))))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(globalNotFound)
                    .addComponent(globalKeystore))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(selectPath, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createNew)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(keystorePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(alias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeAlias, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(keyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rememberPasswd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveAsGlobal)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jPanel2.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(debug, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.debug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jLabel5.text")); // NOI18N

        v2.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(v2, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.v2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.jLabel6.text")); // NOI18N

        release.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(release, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.release.text")); // NOI18N

        v1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(v1, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.v1.text")); // NOI18N
        v1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                v1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(askBeforeSigningApk, org.openide.util.NbBundle.getMessage(ApkSigningConfigPanel.class, "ApkSigningConfigPanel.askBeforeSigningApk.text")); // NOI18N
        askBeforeSigningApk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                askBeforeSigningApkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(askBeforeSigningApk)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(v1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(v2))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(release)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(debug)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(v1)
                    .addComponent(v2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(release)
                    .addComponent(debug))
                .addGap(17, 17, 17)
                .addComponent(askBeforeSigningApk)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewActionPerformed
        // TODO add your handling code here:
        NewKeyStore newKeyStore = new NewKeyStore();
        DialogDescriptor dd = new DialogDescriptor(newKeyStore, "New Key Store", true, null);
        newKeyStore.setDescriptor(dd);
        Object notify = DialogDisplayer.getDefault().notify(dd);
        if (DialogDescriptor.OK_OPTION.equals(notify)) {
            String newPath = newKeyStore.getPath();
            char[] password = newKeyStore.getPassword();
            ApkUtils.DN dn = newKeyStore.getDN();
            boolean createNewStore = ApkUtils.createNewStore(null, new File(newPath), password, dn);
            if (!createNewStore) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to create new key store!", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            } else {
                path.setText(newPath);
                keystorePassword.setText(new String(password));
                alias.setText(dn.getAlias());
                keyPassword.setText(new String(dn.getPassword()));
            }
            keyPressed(null);
        }

    }//GEN-LAST:event_createNewActionPerformed

    private void v1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_v1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_v1ActionPerformed

    private void selectPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPathActionPerformed
        FileChooserBuilder builder = new FileChooserBuilder(ApkSigningConfigPanel.class);
        builder.setDirectoriesOnly(false);
        builder.setApproveText("Open");
        builder.setControlButtonsAreShown(true);
        builder.setTitle("Open Key Store...");
        builder.setFilesOnly(true);
        builder.setFileFilter(new FileNameExtensionFilter("Key Store", "jks"));
        JFileChooser chooser = builder.createFileChooser();
        String text = path.getText();
        if (!text.isEmpty()) {
            File f = new File(text);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        int resp = chooser.showOpenDialog(findDialogParent());
        if (JFileChooser.APPROVE_OPTION == resp) {
            File f = chooser.getSelectedFile();
            path.setText(f.getAbsolutePath());
            alias.setText("");
            keyPassword.setText("");
            keystorePassword.setText("");
            keyReleased(null);
        }
    }//GEN-LAST:event_selectPathActionPerformed

    private void changeAliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeAliasActionPerformed
        // TODO add your handling code here:
        try {
            File f = new File(path.getText());
            if (f.exists()) {
                KeyStore ks = KeyStore.getInstance("jks");
                ks.load(new FileInputStream(f), keystorePassword.getPassword());
                EditKeyStore editKeyStore = new EditKeyStore(ks, alias.getText());
                DialogDescriptor dd = new DialogDescriptor(editKeyStore, "Choose Key", true, null);
                editKeyStore.setDescriptor(dd);
                Object notify = DialogDisplayer.getDefault().notify(dd);
                if (DialogDescriptor.OK_OPTION.equals(notify)) {
                    if (editKeyStore.isNewKey()) {
                        ApkUtils.DN dn = editKeyStore.getNewDN();
                        boolean addNewKey = ApkUtils.addNewKey(ks, f, keystorePassword.getPassword(), dn);
                        if (!addNewKey) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to save new alias to key store!", NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(nd);
                        } else {
                            alias.setText(dn.getAlias());
                            keyPassword.setText(new String(dn.getPassword()));
                        }
                        keyPressed(null);
                    } else {
                        alias.setText(editKeyStore.getAliasName());
                        keyPassword.setText("");
                    }
                    keyReleased(null);
                }
            }
        } catch (Exception ex) {
        }

    }//GEN-LAST:event_changeAliasActionPerformed

    private void globalKeystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalKeystoreActionPerformed
        // TODO add your handling code here:
        if (globalKeystore.isSelected()) {
            if (globalKeystoreConfiguration.isValid()) {
                setupGlobalFound();
            } else {
                setupGlobalNotFound();
            }
        } else if (localKeystoreConfiguration.isValid()) {
            setupLocalFound();
        } else {
            setupLocalNotFound();
        }
    }//GEN-LAST:event_globalKeystoreActionPerformed

    private void askBeforeSigningApkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_askBeforeSigningApkActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_askBeforeSigningApkActionPerformed

    private void saveAsGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsGlobalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveAsGlobalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alias;
    private javax.swing.JCheckBox askBeforeSigningApk;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton changeAlias;
    private javax.swing.JButton createNew;
    private javax.swing.JRadioButton debug;
    private javax.swing.JCheckBox globalKeystore;
    private javax.swing.JLabel globalNotFound;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPasswordField keyPassword;
    private javax.swing.JPasswordField keystorePassword;
    private javax.swing.JTextField path;
    private javax.swing.JRadioButton release;
    private javax.swing.JCheckBox rememberPasswd;
    private javax.swing.JCheckBox saveAsGlobal;
    private javax.swing.JButton selectPath;
    private javax.swing.JRadioButton v1;
    private javax.swing.JRadioButton v2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        save();
    }

    private Component findDialogParent() {
        Component parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (parent == null) {
            parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }
        if (parent == null) {
            Frame[] f = Frame.getFrames();
            parent = f.length == 0 ? null : f[f.length - 1];
        }
        return parent;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        boolean enableChangeAlias = false;
        try {
            File f = new File(path.getText());
            if (f.exists()) {
                KeyStore ks = KeyStore.getInstance("jks");
                ks.load(new FileInputStream(f), keystorePassword.getPassword());
                enableChangeAlias = true;
                Key key = ks.getKey(alias.getText(), keyPassword.getPassword());
                if (key != null && descriptor != null && (v1.isSelected() || v2.isSelected())) {
                    descriptor.setValid((v1.isSelected() || v2.isSelected()) && (debug.isSelected() || release.isSelected()));
                    changeAlias.setEnabled(enableChangeAlias);
                    return;
                }
            }
        } catch (Exception ex) {
        }
        if (descriptor != null) {
            descriptor.setValid(false);
            changeAlias.setEnabled(enableChangeAlias);
        }
    }

    @Override
    public File getStoreFile() {
        return new File(path.getText());
    }

    @Override
    public String getStorePassword() {
        return new String(keystorePassword.getPassword());
    }

    @Override
    public String getKeyAlias() {
        return alias.getText();
    }

    @Override
    public String getKeyPassword() {
        return new String(keyPassword.getPassword());
    }

    @Override
    public String getStoreType() {
        try {
            File f = new File(path.getText());
            if (f.exists()) {
                KeyStore ks = KeyStore.getInstance("jks");
                ks.load(new FileInputStream(f), keystorePassword.getPassword());
                return ks.getType();
            }
        } catch (Exception keyStoreException) {
        }
        return "jks";
    }

    @Override
    public boolean isV1SigningEnabled() {
        return v1.isSelected();
    }

    @Override
    public boolean isV2SigningEnabled() {
        return v2.isSelected();
    }

    @Override
    public boolean isSigningReady() {
        return true;
    }
}
