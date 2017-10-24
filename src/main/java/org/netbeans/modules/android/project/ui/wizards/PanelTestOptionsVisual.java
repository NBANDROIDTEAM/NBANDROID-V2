/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.android.project.ui.wizards;

import com.android.ide.common.xml.ManifestData;
import com.android.sdklib.IAndroidTarget;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.api.AndroidProjects;
import org.netbeans.modules.android.project.ui.customizer.AndroidTargetTableModel;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

class PanelTestOptionsVisual extends SettingsPanel {
  private static final Logger LOG = Logger.getLogger(PanelTestOptionsVisual.class.getName());

  private PanelConfigureProject panel;
  private boolean valid = true;
  private String errorKey;
  private final AndroidTargetTableModel tableModel;
  private final DalvikPlatformManager platformManager;
  private final WindowFocusListener focusListener;

  public PanelTestOptionsVisual(PanelConfigureProject panel, DalvikPlatformManager platformManager) {
    initComponents();
    this.platformManager = platformManager;
    this.panel = panel;
    final DocumentListener documentListener = new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent e) {
        checkValidity();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        checkValidity();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        checkValidity();
      }
    };
    this.packageNameTextField.getDocument().addDocumentListener(documentListener);

    final DocumentListener documentListener2 = new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent e) {
        onMainProjectModified();
        checkValidity();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        onMainProjectModified();
        checkValidity();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        onMainProjectModified();
        checkValidity();
      }
    };
    this.txtTestedProject.getDocument().addDocumentListener(documentListener2);

    // TODO update package (and target) if tested project is selected

    jTableTargets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jTableTargets.setRowSelectionAllowed(true);
    jTableTargets.setColumnSelectionAllowed(false);
    tableModel = new AndroidTargetTableModel(getTargets());
    jTableTargets.setModel(tableModel);
    jTableTargets.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {
        checkValidity();
      }
    });
    this.focusListener = new WindowFocusListener() {
      @Override
      public void windowGainedFocus(WindowEvent e) {
        final List<? extends IAndroidTarget> newTargets = getTargets();
        final List<? extends IAndroidTarget> oldTargets = tableModel.getTargets();
        if (!newTargets.equals(oldTargets)) {
          tableModel.setTargets(newTargets);
        }
      }

      @Override
      public void windowLostFocus(WindowEvent e) {          
      }
    };
  }


  @Override
  public final void addNotify() {
    super.addNotify();
    final Window win = SwingUtilities.getWindowAncestor(this);
    if (win != null) {
      win.addWindowFocusListener(focusListener);
    }
  }

  @Override
  public final void removeNotify () {
    super.removeNotify();
    final Window win = SwingUtilities.getWindowAncestor(this);
    if (win != null) {
      win.removeWindowFocusListener(focusListener);
    }
  }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setAsMainCheckBox = new javax.swing.JCheckBox();
    jLabelPkgName = new javax.swing.JLabel();
    packageNameTextField = new javax.swing.JTextField();
    jLabelPlatform = new javax.swing.JLabel();
    jScrollPane2 = new javax.swing.JScrollPane();
    jTableTargets = new javax.swing.JTable();
    jButtonAndroidOption = new javax.swing.JButton();
    lblTestedProject = new javax.swing.JLabel();
    txtTestedProject = new javax.swing.JTextField();
    btnTestedProjectBrowse = new javax.swing.JButton();

    setAsMainCheckBox.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, org.openide.util.NbBundle.getBundle(PanelTestOptionsVisual.class).getString("LBL_setAsMainCheckBox")); // NOI18N

    jLabelPkgName.setDisplayedMnemonic('P');
    jLabelPkgName.setText("Package Name:");

    packageNameTextField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        packageNameTextFieldActionPerformed(evt);
      }
    });

    jLabelPlatform.setDisplayedMnemonic('F');
    jLabelPlatform.setText("Target Platfrom:");

    jTableTargets.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {
        {},
        {},
        {},
        {}
      },
      new String [] {

      }
    ));
    jScrollPane2.setViewportView(jTableTargets);

    jButtonAndroidOption.setText("Manage Android SDK ...");
    jButtonAndroidOption.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonAndroidOptionActionPerformed(evt);
      }
    });

    lblTestedProject.setText("Tested project:");

    btnTestedProjectBrowse.setText("Browse");
    btnTestedProjectBrowse.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnTestedProjectBrowseActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(setAsMainCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
          .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabelPlatform)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 118, Short.MAX_VALUE)
            .addComponent(jButtonAndroidOption))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabelPkgName)
              .addComponent(lblTestedProject))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(txtTestedProject, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTestedProjectBrowse))
              .addComponent(packageNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addComponent(setAsMainCheckBox)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtTestedProject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnTestedProjectBrowse)
          .addComponent(lblTestedProject))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(packageNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabelPkgName))
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabelPlatform)
          .addComponent(jButtonAndroidOption))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    setAsMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelTestOptionsVisual.class).getString("ACSN_setAsMainCheckBox")); // NOI18N
    setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelTestOptionsVisual.class).getString("ACSD_setAsMainCheckBox")); // NOI18N
    packageNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelTestOptionsVisual.class).getString("ASCN_mainClassTextFiled")); // NOI18N
    packageNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelTestOptionsVisual.class).getString("ASCD_mainClassTextFiled")); // NOI18N

    getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelTestOptionsVisual.class, "ACSN_PanelOptionsVisual")); // NOI18N
    getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelTestOptionsVisual.class, "ACSD_PanelOptionsVisual")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents

    private void packageNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_packageNameTextFieldActionPerformed

    private void jButtonAndroidOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAndroidOptionActionPerformed
      OptionsDisplayer.getDefault().open(
          "Advanced/org-netbeans-modules-android-core-ui-AndroidPlatformAdvancedOption");
    }//GEN-LAST:event_jButtonAndroidOptionActionPerformed

    private void btnTestedProjectBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestedProjectBrowseActionPerformed
      JFileChooser projectChooser = ProjectChooser.projectChooser();
      projectChooser.setDialogTitle(NbBundle.getMessage(PanelTestOptionsVisual.class, "TITLE_selectTestedProject"));
      projectChooser.setApproveButtonText(NbBundle.getMessage(PanelTestOptionsVisual.class, "TXT_approveTestedProject"));
      int option = projectChooser.showOpenDialog(null);
      if (option == JFileChooser.APPROVE_OPTION) {
        txtTestedProject.setText(projectChooser.getSelectedFile().getAbsolutePath());
      }
    }//GEN-LAST:event_btnTestedProjectBrowseActionPerformed

  @Override
  boolean valid(WizardDescriptor settings) {

    if (!valid) {
      settings.putProperty("WizardPanel_errorMessage", // NOI18N
          NbBundle.getMessage(PanelTestOptionsVisual.class, errorKey));
    }
    return this.valid;
  }

  @Override
  void read(WizardDescriptor d) {
    //TODO: the only panel - nothing to read probably
    checkValidity();
  }

  @Override
  void validate(WizardDescriptor d) throws WizardValidationException {
    checkValidity();
  }

  @Override
  void store(WizardDescriptor d) {
    d.putProperty( /*XXX Define somewhere */"packageName", packageNameTextField.getText()); // NOI18N
    d.putProperty( /*XXX Define somewhere */"testedProject", txtTestedProject.getText()); // NOI18N

    d.putProperty( /*XXX Define somewhere */"platform", selectedPlatform()); // NOI18N
  }

  private DalvikPlatform selectedPlatform() {
    IAndroidTarget target = tableModel.getTargetAt(jTableTargets.getSelectedRow());
    DalvikPlatform dPlatform = DalvikPlatformManager.getDefault().findPlatformForTarget(
        target != null ? target.hashString() : null);
    return dPlatform;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnTestedProjectBrowse;
  private javax.swing.JButton jButtonAndroidOption;
  private javax.swing.JLabel jLabelPkgName;
  private javax.swing.JLabel jLabelPlatform;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JTable jTableTargets;
  private javax.swing.JLabel lblTestedProject;
  private javax.swing.JTextField packageNameTextField;
  private javax.swing.JCheckBox setAsMainCheckBox;
  private javax.swing.JTextField txtTestedProject;
  // End of variables declaration//GEN-END:variables


   private void checkValidity() {
    valid = true;

    if (!checkPlatformManager() || !checkTarget() || !checkPackage() || !checkTestedProject()) {
      valid = false;
    }

    this.panel.fireChangeEvent();
  }

  private boolean checkPlatformManager() {
    boolean isValid = true;
    if (platformManager.getSdkLocation() == null) {
      isValid = false;
      this.errorKey = "ERROR_NoSDK"; // NOI18N
    }
    return isValid;
  }

  private boolean checkTarget() {
    boolean isValid = true;
    if (selectedPlatform() == null) {
      isValid = false;
      this.errorKey = "ERROR_NoBuildTarget"; // NOI18N
    }
    return isValid;
  }

    private boolean checkPackage() {
    String text = this.packageNameTextField.getText();
    boolean isValid = true;
    int tokenCount = 0;
    if (text.trim().length() == 0) {
      isValid = false;
    } else {
      StringTokenizer tk = new StringTokenizer(text, "."); //NOI18N
      while (tk.hasMoreTokens()) {
        tokenCount++;
        String token = tk.nextToken();
        if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
          isValid = false;
          break;
        }
      }
    }
    if (isValid && tokenCount < 2) {
      isValid = false;
      errorKey = "ERROR_PackageNameTooShort"; // NOI18N
    } else if (!isValid) {
      errorKey = "ERROR_IllegalPackageName"; // NOI18N
    }
    return isValid;
  }

  private Project toProject(String path) {
    File mainPrjFile = new File(path);
    if (!mainPrjFile.exists()) {
      return null;
    }
    FileObject mainPrjFO = FileUtil.toFileObject(mainPrjFile);
    Project mainPrj = null;
    try {
      if (mainPrjFO != null) {
        // TODO better check that this is an android project
        mainPrj = ProjectManager.getDefault().findProject(mainPrjFO);
      }
    } catch (IOException ex) {
      LOG.log(Level.INFO, null, ex);
    } catch (IllegalArgumentException ex) {
      LOG.log(Level.INFO, null, ex);
    }
    return mainPrj;
  }

  private boolean checkTestedProject() {
    boolean isValid = true;
    String text = this.txtTestedProject.getText();
    Project mainPrj = toProject(text);
    if (mainPrj == null) {
      isValid = false;
      errorKey = "ERROR_MissingTestedProject"; // NOI18N
    }
    return isValid;
  }

  private void onMainProjectModified() {
    String text = this.txtTestedProject.getText();
    Project project = toProject(text);
    if (project == null) {
      return;
    }
    ManifestData manifestData = AndroidProjects.parseProjectManifest(project);
    if (manifestData == null) {
      return;
    }
    // If we start from a project then this would be a good default for test project name
    // String appName = project.getName() + "Test";

    String packageName = manifestData.getPackage();
    packageName += ".test";
    packageNameTextField.setText(packageName);
    // TODO set build target to the same target as original project
    IAndroidTarget sdkTarget = null;
    // TODO filter out build targets that do not match to min SDK version
    String minSdkVersion = manifestData.getMinSdkVersionString();
    // TODO support for activity name to generate template test for default activity
  }

  private List<? extends IAndroidTarget> getTargets() {
    List<IAndroidTarget> targets = new ArrayList<IAndroidTarget>();    
    for (DalvikPlatform platform : platformManager.getPlatforms()) {
      targets.add(platform.getAndroidTarget());
    }
    return targets;
  }

}
