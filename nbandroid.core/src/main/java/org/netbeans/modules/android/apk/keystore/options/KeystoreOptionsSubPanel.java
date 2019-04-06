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
package org.netbeans.modules.android.apk.keystore.options;

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
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.android.apk.ApkUtils;
import org.netbeans.modules.android.apk.keystore.*;
import org.netbeans.modules.android.options.AndroidOptionsPanelController;
import org.netbeans.modules.android.spi.AndroidOptionsSubPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbPreferences;

/**
 *
 * @author arsi
 */
public class KeystoreOptionsSubPanel extends AndroidOptionsSubPanel implements ActionListener, KeyListener {

    public static final String KEY_STORE_PATH = "GLOBAL_KEY_STORE_PATH";
    public static final String KEY_STORE_PASSWORD = "GLOBAL_KEY_STORE_PASSWORD";
    public static final String KEY_ALIAS = "GLOBAL_KEY_ALIAS";
    public static final String KEY_PASSWORD = "GLOBAL_KEY_PASSWORD";
    public static final String APK_V1 = "GLOBAL_APK_V1";
    public static final String APK_V2 = "GLOBAL_APK_V2";
    public static final String REMEMBER_PASSWORDS = "GLOBAL_REMEMBER_PASSWORDS";

    /**
     * Creates new form KeystoreSelector
     */
    public KeystoreOptionsSubPanel(AndroidOptionsPanelController controller) {
        super(controller);
        initComponents();
        path.addKeyListener(this);
        alias.addKeyListener(this);
        keystorePassword.addKeyListener(this);
        keyPassword.addKeyListener(this);
        keyReleased(null);
    }

    @Override
    public String getCategory() {
        return "Keystore";
    }

    @Override
    public void load() {
        char[] keystorePasswd = Keyring.read(KEY_STORE_PASSWORD);
        char[] keyPasswd = Keyring.read(KEY_PASSWORD);
        if (keystorePasswd != null) {
            keystorePassword.setText(new String(keystorePasswd));
        }
        if (keyPasswd != null) {
            keyPassword.setText(new String(keyPasswd));
        }
        path.setText(NbPreferences.forModule(KeystoreOptionsSubPanel.class).get(KEY_STORE_PATH, ""));
        alias.setText(NbPreferences.forModule(KeystoreOptionsSubPanel.class).get(KEY_ALIAS, ""));
        rememberPasswd.setSelected(NbPreferences.forModule(KeystoreOptionsSubPanel.class).getBoolean(REMEMBER_PASSWORDS, true));

    }

    @Override
    public void store() {
        if (rememberPasswd.isSelected()) {
            Keyring.save(KEY_STORE_PASSWORD, keystorePassword.getPassword(), "NBANDROID Global Keystore Password");
            Keyring.save(KEY_PASSWORD, keyPassword.getPassword(), "NBANDROID Global Keystore Key Password");
        } else {
            Keyring.delete(KEY_STORE_PASSWORD);
            Keyring.delete(KEY_PASSWORD);
        }
        NbPreferences.forModule(KeystoreOptionsSubPanel.class).put(KEY_STORE_PATH, path.getText());
        NbPreferences.forModule(KeystoreOptionsSubPanel.class).put(KEY_ALIAS, alias.getText());
        NbPreferences.forModule(KeystoreOptionsSubPanel.class).putBoolean(REMEMBER_PASSWORDS, rememberPasswd.isSelected());

    }

    @Override
    public boolean valid() {
        try {
            File f = new File(path.getText());
            if (f.exists()) {
                KeyStore ks = KeyStore.getInstance("jks");
                ks.load(new FileInputStream(f), keystorePassword.getPassword());
                Key key = ks.getKey(alias.getText(), keyPassword.getPassword());
                if (key != null) {
                    return true;
                }
            }
        } catch (Exception ex) {
        }
        return false;
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
        jLabel1 = new javax.swing.JLabel();
        path = new javax.swing.JTextField();
        createNew = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        keystorePassword = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        alias = new javax.swing.JTextField();
        changeAlias = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        keyPassword = new javax.swing.JPasswordField();
        rememberPasswd = new javax.swing.JCheckBox();
        selectPath = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.jLabel1.text")); // NOI18N

        path.setText(org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.path.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(createNew, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.createNew.text")); // NOI18N
        createNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.jLabel2.text")); // NOI18N

        keystorePassword.setText(org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.keystorePassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.jLabel3.text")); // NOI18N

        alias.setText(org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.alias.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(changeAlias, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.changeAlias.text")); // NOI18N
        changeAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeAliasActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.jLabel4.text")); // NOI18N

        keyPassword.setText(org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.keyPassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rememberPasswd, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.rememberPasswd.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectPath, org.openide.util.NbBundle.getMessage(KeystoreOptionsSubPanel.class, "KeystoreOptionsSubPanel.selectPath.text")); // NOI18N
        selectPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPathActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keystorePassword)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(alias)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changeAlias))
                            .addComponent(keyPassword)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(selectPath))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rememberPasswd)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(createNew)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(selectPath, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createNew)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(keystorePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(alias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeAlias, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(keyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rememberPasswd)
                .addContainerGap(67, Short.MAX_VALUE))
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
            keyReleased(null);
        }

    }//GEN-LAST:event_createNewActionPerformed

    private void selectPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPathActionPerformed
        FileChooserBuilder builder = new FileChooserBuilder(KeystoreOptionsSubPanel.class);
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
                        keyReleased(null);
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alias;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton changeAlias;
    private javax.swing.JButton createNew;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPasswordField keyPassword;
    private javax.swing.JPasswordField keystorePassword;
    private javax.swing.JTextField path;
    private javax.swing.JCheckBox rememberPasswd;
    private javax.swing.JButton selectPath;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
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
                if (key != null) {
                    changeAlias.setEnabled(enableChangeAlias);
                    controller.changed();
                    return;
                }
            }
        } catch (Exception ex) {
        }
        changeAlias.setEnabled(enableChangeAlias);
        controller.changed();
    }

}
