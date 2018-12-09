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
package org.nbandroid.netbeans.gradle.v2.project.template;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

public class AndroidProjectTemplatePanelVisualBasicSettings extends JPanel implements DocumentListener, PropertyChangeListener, ItemListener {

    public static final String PROP_PROJECT_NAME = "projectName";
    public static final String PROP_PROJECT_DIR = "projectDir";
    public static final String PROP_PROJECT_SDK = "PROP_PROJECT_SDK";
    public static final String PROP_PROJECT_DOMAIN = "PROP_PROJECT_DOMAIN";
    public static final String PROP_PROJECT_PACKAGE = "PROP_PROJECT_PACKAGE";

    private AndroidProjectTemplateWizardPanellBasicSettings panel;

    public AndroidProjectTemplatePanelVisualBasicSettings(AndroidProjectTemplateWizardPanellBasicSettings panel) {
        initComponents();
        this.panel = panel;

        sdkList.setModel(new DefaultComboBoxModel(AndroidSdkProvider.getInstalledSDKs()));
        sdkList.setSelectedItem(AndroidSdkProvider.getDefaultSdk());
        AndroidSdkProvider.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, AndroidSdkProvider.PROP_INSTALLED_SDKS, AndroidSdkProvider.getDefault()));

        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        domain.getDocument().addDocumentListener(this);
        packageName.getDocument().addDocumentListener(this);
        sdkList.addItemListener(this);
        domain.setText("com.company");
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        domain = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        packageName = new javax.swing.JTextField();
        cpp = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        sdkList = new javax.swing.JComboBox<>();
        manageSdks = new javax.swing.JButton();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.projectNameLabel.text")); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.projectLocationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.browseButton.text")); // NOI18N
        browseButton.setActionCommand(org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.browseButton.actionCommand")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.createdFolderLabel.text")); // NOI18N

        createdFolderTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cpp, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.cpp.text")); // NOI18N
        cpp.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.jLabel3.text")); // NOI18N

        sdkList.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(manageSdks, org.openide.util.NbBundle.getMessage(AndroidProjectTemplatePanelVisualBasicSettings.class, "AndroidProjectTemplatePanelVisualBasicSettings.manageSdks.text")); // NOI18N
        manageSdks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageSdksActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameLabel)
                    .addComponent(projectLocationLabel)
                    .addComponent(createdFolderLabel)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameTextField)
                    .addComponent(projectLocationTextField)
                    .addComponent(createdFolderTextField)
                    .addComponent(domain)
                    .addComponent(packageName)
                    .addComponent(sdkList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(browseButton)
                    .addComponent(manageSdks, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(144, 144, 144)
                .addComponent(cpp)
                .addGap(149, 149, 149))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameLabel)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationLabel)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderLabel)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(domain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(packageName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(cpp)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(sdkList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manageSdks))
                .addContainerGap(79, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle("Select Project Location");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            panel.fireChangeEvent();
        }

    }//GEN-LAST:event_browseButtonActionPerformed

    private void manageSdksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageSdksActionPerformed
        Action action = org.openide.awt.Actions.forID("Tools", "org.nbandroid.netbeans.gradle.v2.sdk.ui.SdksCustomizerAction");
        if (action != null) {
            action.actionPerformed(evt);
        }
    }//GEN-LAST:event_manageSdksActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox cpp;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JTextField domain;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton manageSdks;
    private javax.swing.JTextField packageName;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JComboBox<String> sdkList;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    boolean valid(WizardDescriptor wizardDescriptor) {

        if (projectNameTextField.getText().length() == 0) {
            // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_ERROR_MESSAGE:
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Project Name is not a valid folder name.");
            return false; // Display name not specified
        }
        File f = FileUtil.normalizeFile(new File(projectLocationTextField.getText()).getAbsoluteFile());
        if (!f.isDirectory()) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message);
            return false;
        }
        final File destFolder = FileUtil.normalizeFile(new File(createdFolderTextField.getText()).getAbsoluteFile());

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Project Folder cannot be created.");
            return false;
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message);
            return false;
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Project Folder already exists and is not empty.");
            return false;
        }
        wizardDescriptor.putProperty("WizardPanel_errorMessage", "");
        if(domain.getText().isEmpty()){
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Domain Name is not a valid domain name..");
            return false;
        }
        if(packageName.getText().isEmpty()){
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Package Name is not a valid package name..");
            return false;
        }
        if(sdkList.getSelectedItem()==null){
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "No Android SDK selected..");
            return false;
        }
        return true;
    }

    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();

        d.putProperty(PROP_PROJECT_DIR, new File(folder));
        d.putProperty(PROP_PROJECT_NAME, name);
        d.putProperty(PROP_PROJECT_DOMAIN, domain.getText());
        d.putProperty(PROP_PROJECT_PACKAGE, packageName.getText());
        d.putProperty(PROP_PROJECT_SDK, sdkList.getSelectedItem());


    }

    void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty(PROP_PROJECT_DIR);
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());

        String projectName = (String) settings.getProperty(PROP_PROJECT_NAME);
        if (projectName == null) {
            projectName = "NewAndroidProject";
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
        domain.setText((String) settings.getProperty(PROP_PROJECT_DOMAIN));
        packageName.setText((String) settings.getProperty(PROP_PROJECT_PACKAGE));
        sdkList.setSelectedItem(settings.getProperty(PROP_PROJECT_SDK));
    }

    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
        if (this.domain.getDocument() == e.getDocument()) {
            String tmp = domain.getText() + "." + projectNameTextField.getText();
            packageName.setText(tmp.toLowerCase());
            firePropertyChange(PROP_PROJECT_DOMAIN, null, this.domain.getText());
        }
        if (this.packageName.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_PACKAGE, null, this.packageName.getText());
        }
    }

    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
        if (this.domain.getDocument() == e.getDocument()) {
            String tmp = domain.getText() + "." + projectNameTextField.getText();
            packageName.setText(tmp.toLowerCase());
            firePropertyChange(PROP_PROJECT_DOMAIN, null, this.domain.getText());
        }
        if (this.packageName.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_PACKAGE, null, this.packageName.getText());
        }
    }

    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
        if (this.domain.getDocument() == e.getDocument()) {
            String tmp = domain.getText() + "." + projectNameTextField.getText();
            packageName.setText(tmp.toLowerCase());
            firePropertyChange(PROP_PROJECT_DOMAIN, null, this.domain.getText());
        }
        if (this.packageName.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_PACKAGE, null, this.packageName.getText());
        }
    }

    /**
     * Handles changes in the Project name and project directory,
     */
    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name

            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();

            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
            //}

        }
        panel.fireChangeEvent(); // Notify that the panel changed
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        sdkList.setModel(new DefaultComboBoxModel(AndroidSdkProvider.getInstalledSDKs()));
        sdkList.setSelectedItem(AndroidSdkProvider.getDefaultSdk());
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        firePropertyChange(PROP_PROJECT_SDK, null, this.sdkList.getSelectedItem());
        panel.fireChangeEvent(); // Notify that the panel changed
    }

}
