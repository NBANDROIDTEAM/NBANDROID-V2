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
package org.nbandroid.netbeans.gradle.v2.apk.sign.keystore;

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
import org.nbandroid.netbeans.gradle.v2.apk.ApkUtils;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;

/**
 *
 * @author arsi
 */
public class KeystoreSelector extends javax.swing.JPanel implements ActionListener, KeyListener, SigningConfig {

    private final Project project;
    private final FileObject toSign;
    private final String hash;
    private static final String KEY_STORE_PATH = "_KEY_STORE_PATH";
    private static final String KEY_STORE_PASSWORD = "_KEY_STORE_PASSWORD";
    private static final String KEY_ALIAS = "_KEY_ALIAS";
    private static final String KEY_PASSWORD = "_KEY_PASSWORD";
    private static final String APK_V1 = "_APK_V1";
    private static final String APK_V2 = "_APK_V2";
    private static final String REMEMBER_PASSWORDS = "_REMEMBER_PASSWORDS";
    private DialogDescriptor descriptor = null;

    /**
     * Creates new form KeystoreSelector
     */
    public KeystoreSelector(Project project, FileObject toSign) {
        initComponents();
        assert project != null;
        assert toSign != null;
        this.project = project;
        this.toSign = toSign;
        hash = "ANDROID_" + project.getProjectDirectory().getPath().hashCode();
        char[] keystorePasswd = Keyring.read(hash + KEY_STORE_PASSWORD);
        char[] keyPasswd = Keyring.read(hash + KEY_PASSWORD);
        if (keystorePasswd != null) {
            keystorePassword.setText(new String(keystorePasswd));
        }
        if (keyPasswd != null) {
            keyPassword.setText(new String(keyPasswd));
        }
        path.setText(NbPreferences.forModule(KeystoreSelector.class).get(hash + KEY_STORE_PATH, ""));
        alias.setText(NbPreferences.forModule(KeystoreSelector.class).get(hash + KEY_ALIAS, ""));
        v1.setSelected(NbPreferences.forModule(KeystoreSelector.class).getBoolean(hash + APK_V1, true));
        v2.setSelected(NbPreferences.forModule(KeystoreSelector.class).getBoolean(hash + APK_V2, true));
        rememberPasswd.setSelected(NbPreferences.forModule(KeystoreSelector.class).getBoolean(hash + REMEMBER_PASSWORDS, true));
        path.addKeyListener(this);
        alias.addKeyListener(this);
        keystorePassword.addKeyListener(this);
        keyPassword.addKeyListener(this);
    }

    public void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        keyReleased(null);
    }

    public void storeSettings() {
        if (rememberPasswd.isSelected()) {
            Keyring.save(hash + KEY_STORE_PASSWORD, keystorePassword.getPassword(), "NBANDROID Project Keystore Password");
            Keyring.save(hash + KEY_PASSWORD, keyPassword.getPassword(), "NBANDROID Project Keystore Key Password");
        }
        NbPreferences.forModule(KeystoreSelector.class).put(hash + KEY_STORE_PATH, path.getText());
        NbPreferences.forModule(KeystoreSelector.class).put(hash + KEY_ALIAS, alias.getText());
        NbPreferences.forModule(KeystoreSelector.class).putBoolean(hash + APK_V1, v1.isSelected());
        NbPreferences.forModule(KeystoreSelector.class).putBoolean(hash + APK_V2, v2.isSelected());
        NbPreferences.forModule(KeystoreSelector.class).putBoolean(hash + REMEMBER_PASSWORDS, rememberPasswd.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        jLabel5 = new javax.swing.JLabel();
        v1 = new javax.swing.JRadioButton();
        v2 = new javax.swing.JRadioButton();
        selectPath = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.jLabel1.text")); // NOI18N

        path.setText(org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.path.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(createNew, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.createNew.text")); // NOI18N
        createNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.jLabel2.text")); // NOI18N

        keystorePassword.setText(org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.keystorePassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.jLabel3.text")); // NOI18N

        alias.setText(org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.alias.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(changeAlias, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.changeAlias.text")); // NOI18N
        changeAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeAliasActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.jLabel4.text")); // NOI18N

        keyPassword.setText(org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.keyPassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rememberPasswd, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.rememberPasswd.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.jLabel5.text")); // NOI18N

        v1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(v1, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.v1.text")); // NOI18N
        v1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                v1ActionPerformed(evt);
            }
        });

        v2.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(v2, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.v2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectPath, org.openide.util.NbBundle.getMessage(KeystoreSelector.class, "KeystoreSelector.selectPath.text")); // NOI18N
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
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keystorePassword)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(alias)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changeAlias))
                            .addComponent(keyPassword)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rememberPasswd)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(v1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(v2)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(selectPath))))
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
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(v1)
                    .addComponent(v2))
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
        FileChooserBuilder builder = new FileChooserBuilder(KeystoreSelector.class);
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alias;
    private javax.swing.JButton changeAlias;
    private javax.swing.JButton createNew;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPasswordField keyPassword;
    private javax.swing.JPasswordField keystorePassword;
    private javax.swing.JTextField path;
    private javax.swing.JCheckBox rememberPasswd;
    private javax.swing.JButton selectPath;
    private javax.swing.JRadioButton v1;
    private javax.swing.JRadioButton v2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        storeSettings();
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
                    descriptor.setValid(true);
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
