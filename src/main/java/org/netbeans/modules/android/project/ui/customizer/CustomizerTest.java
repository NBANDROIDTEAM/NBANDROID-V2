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

package org.netbeans.modules.android.project.ui.customizer;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.repository.AndroidSdkHandler;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.PlatformUtilities;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider.Config;
import org.netbeans.modules.android.project.configs.ConfigBuilder;
import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CustomizerTest extends JPanel implements HelpCtx.Provider {
  private static final Logger LOG = Logger.getLogger(CustomizerTest.class.getName());

  private static final Font PLAIN_FONT = new JLabel().getFont().deriveFont(Font.PLAIN);
  private static final Font BOLD_FONT = PLAIN_FONT.deriveFont(Font.BOLD);

  private final ConfigGroup configs;

  private final ConfigListModel comboListModel;
  private final AndroidProject project;


  // TODO on OK we need to
  // apply changes - add new, remove deleted, update changed
  // set active config

  public CustomizerTest(AndroidProject project, ConfigGroup configs) {
    this.project = project;
    this.configs = configs;
    comboListModel = new ConfigListModel();

    initComponents();
    try {
        final AndroidSdkHandler sdkManager = DalvikPlatformManager.getDefault().getSdkManager();
      if (sdkManager != null) {
        AvdManager avdManager = PlatformUtilities.createAvdManager(sdkManager);
        avdUISelector.setAvdInfos(avdManager.getAllAvds());
      } else {
        avdUISelector.setAvdInfos(new AvdInfo[0]);
      }
    } catch (AndroidLocationException ex) {
      LOG.log(Level.INFO, "Cannot load AVD infos", ex);
    }
    
    configCombo.setModel(comboListModel);
    configCombo.setRenderer(new ConfigListRenderer());
    initFromConfig();
    addListeners();
  }

   @Override
   public HelpCtx getHelpCtx() {
     return new HelpCtx(CustomizerTest.class.getName());
   }

  private void addListeners() {
    // launch
    Enumeration<AbstractButton> launchRadios = launchButtonGroup.getElements();
    final ItemListener launchListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        if (selected) {
          updateLaunchAction();
        }
      }
    };
    while (launchRadios.hasMoreElements()) {
      launchRadios.nextElement().addItemListener(launchListener);
    }
    runnerCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateLaunchAction();
      }
    });

    // emulator
    emulatorOptionsTextField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        update();
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
        update();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        update();
      }
      private void update() {
        updateEmulatorOptions();
      }
    });

    // target
    Enumeration<AbstractButton> targetRadios = targetButtonGroup.getElements();
    final ItemListener targetListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          updateTargetMode();
        }
      }
    };
    while (targetRadios.hasMoreElements()) {
      targetRadios.nextElement().addItemListener(targetListener);
    }
  }

  private void selectCurrentConfig() {
    final Config config = configs.getCurrentConfig();
    configCombo.setSelectedItem(config);
    configDelete.setEnabled(configs.getConfigs().size() > 1);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    targetButtonGroup = new javax.swing.ButtonGroup();
    launchButtonGroup = new javax.swing.ButtonGroup();
    configLabel = new javax.swing.JLabel();
    configCombo = new javax.swing.JComboBox();
    configNew = new javax.swing.JButton();
    configDelete = new javax.swing.JButton();
    configSeparator = new javax.swing.JSeparator();
    runnerLabel = new javax.swing.JLabel();
    runnerCombo = new javax.swing.JComboBox();
    emulatorOptionsLabel = new javax.swing.JLabel();
    emulatorOptionsTextField = new javax.swing.JTextField();
    targetGroupLabel = new javax.swing.JLabel();
    targetAutoRadio = new javax.swing.JRadioButton();
    targetManualRadio = new javax.swing.JRadioButton();
    avdPanel = new javax.swing.JPanel();
    avdLabel = new javax.swing.JLabel();
    avdUISelector = new org.netbeans.modules.android.core.ui.AvdUISelector();

    org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(CustomizerTest.class, "LBL_Configuration")); // NOI18N

    configCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        configComboActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(CustomizerTest.class, "LBL_New")); // NOI18N
    configNew.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        configNewActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(configDelete, org.openide.util.NbBundle.getMessage(CustomizerTest.class, "LBL_Delete")); // NOI18N
    configDelete.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        configDeleteActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(runnerLabel, "Instrumentation runner (optional):");

    runnerCombo.setEditable(true);

    emulatorOptionsLabel.setLabelFor(emulatorOptionsTextField);
    org.openide.awt.Mnemonics.setLocalizedText(emulatorOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerTest.class, "LBL_RunAdditionalOptions")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(targetGroupLabel, "Target Device");

    targetButtonGroup.add(targetAutoRadio);
    org.openide.awt.Mnemonics.setLocalizedText(targetAutoRadio, "Automatic");
    targetAutoRadio.setActionCommand("auto");

    targetButtonGroup.add(targetManualRadio);
    org.openide.awt.Mnemonics.setLocalizedText(targetManualRadio, "Manual");
    targetManualRadio.setActionCommand("manual");

    org.openide.awt.Mnemonics.setLocalizedText(avdLabel, "Select preffered AVD or nothing to let IDE choose one:");

    avdUISelector.setAutoscrolls(true);
    avdUISelector.setPreferredSize(null);

    javax.swing.GroupLayout avdPanelLayout = new javax.swing.GroupLayout(avdPanel);
    avdPanel.setLayout(avdPanelLayout);
    avdPanelLayout.setHorizontalGroup(
      avdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(avdLabel)
      .addComponent(avdUISelector, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
    );
    avdPanelLayout.setVerticalGroup(
      avdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(avdPanelLayout.createSequentialGroup()
        .addComponent(avdLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(avdUISelector, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(configLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(configCombo, 0, 224, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(configNew)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(configDelete))
      .addComponent(configSeparator, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
      .addGroup(layout.createSequentialGroup()
        .addComponent(emulatorOptionsLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(emulatorOptionsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(avdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addComponent(targetManualRadio)))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(runnerLabel)
              .addComponent(targetGroupLabel)
              .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(targetAutoRadio)))
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(runnerCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(configCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(configDelete)
          .addComponent(configNew)
          .addComponent(configLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(configSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(runnerLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(runnerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(emulatorOptionsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(emulatorOptionsLabel))
        .addGap(18, 18, 18)
        .addComponent(targetGroupLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(targetAutoRadio)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(targetManualRadio)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(avdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
    Config config = (Config) configCombo.getSelectedItem();
    configs.setCurrentConfig(config);
    configDelete.setEnabled(configs.getConfigs().size() > 1);
    initFromConfig();
}//GEN-LAST:event_configComboActionPerformed

private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
    NotifyDescriptor.InputLine descriptor = new NotifyDescriptor.InputLine(
        NbBundle.getMessage(CustomizerTest.class, "LBL_ConfigurationName"),
        NbBundle.getMessage(CustomizerTest.class, "LBL_CreateNewConfiguration"));

    if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION) {
      String name = descriptor.getInputText();
      if (name.trim().isEmpty()) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            NbBundle.getMessage(CustomizerTest.class, "MSG_ConfigurationNameBlank"),
            NotifyDescriptor.WARNING_MESSAGE));
        return;
      }
      Config config = ConfigBuilder.builder().withName(name).config();
      try {
        configs.addConfig(config);
        comboListModel.addElement(config);
        selectCurrentConfig();
      } catch (IllegalArgumentException ex) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            NbBundle.getMessage(CustomizerTest.class, "MSG_ConfigurationExists", name),
            NotifyDescriptor.WARNING_MESSAGE));
      }
    }
}//GEN-LAST:event_configNewActionPerformed

private void configDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDeleteActionPerformed
    String config = (String) configCombo.getSelectedItem();
    assert config != null;
    comboListModel.removeElement(config);
    selectCurrentConfig();
}//GEN-LAST:event_configDeleteActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel avdLabel;
  private javax.swing.JPanel avdPanel;
  private org.netbeans.modules.android.core.ui.AvdUISelector avdUISelector;
  private javax.swing.JComboBox configCombo;
  private javax.swing.JButton configDelete;
  private javax.swing.JLabel configLabel;
  private javax.swing.JButton configNew;
  private javax.swing.JSeparator configSeparator;
  private javax.swing.JLabel emulatorOptionsLabel;
  private javax.swing.JTextField emulatorOptionsTextField;
  private javax.swing.ButtonGroup launchButtonGroup;
  private javax.swing.JComboBox runnerCombo;
  private javax.swing.JLabel runnerLabel;
  private javax.swing.JRadioButton targetAutoRadio;
  private javax.swing.ButtonGroup targetButtonGroup;
  private javax.swing.JLabel targetGroupLabel;
  private javax.swing.JRadioButton targetManualRadio;
  // End of variables declaration//GEN-END:variables

  private void initFromConfig() {
    Config currentConfig = configs.getCurrentConfig();
    configCombo.setSelectedItem(currentConfig);
    initTestRunner(currentConfig);
    initEmulatorOptions(currentConfig);
    initTargetMode(currentConfig);
  }

  private void initTestRunner(Config config) {
    LaunchConfiguration launch = config.getLaunchConfiguration();
    String runner = launch.getInstrumentationRunner();
    runnerCombo.setSelectedItem(runner != null ? runner : "");
    // TODO clear the model and start background runner to load runner classes
  }

  private void initEmulatorOptions(Config config) {
    LaunchConfiguration launch = config.getLaunchConfiguration();
    String emulatorOptions = launch.getEmulatorOptions();
    emulatorOptionsTextField.setText(emulatorOptions);
  }

  private void initTargetMode(Config config) {
    LaunchConfiguration.TargetMode targetMode = config.getLaunchConfiguration().getTargetMode();
    switch (targetMode) {
      case AUTO:
        targetAutoRadio.setSelected(true);
        break;
      case MANUAL:
        targetManualRadio.setSelected(true);
        break;
      default:
        throw new IllegalStateException("Unknown target mode: " + targetMode);
     }
   }

  void updateLaunchAction() {
    String runnerClass = (String) runnerCombo.getSelectedItem();
    configs.updateCurrentConfig(
        ConfigBuilder.builderForConfig(configs.getCurrentConfig())
            .withTestRunner(runnerClass).config());
  }

  void updateEmulatorOptions() {
    String emulatorOptions = emulatorOptionsTextField.getText().trim();
    configs.updateCurrentConfig(
        ConfigBuilder.builderForConfig(configs.getCurrentConfig())
            .withEmulatorOption(emulatorOptions).config());
  }

  void updateTargetMode() {
    LaunchConfiguration.TargetMode targetMode;
    if (targetAutoRadio.isSelected()) {
      targetMode = LaunchConfiguration.TargetMode.AUTO;
    } else if (targetManualRadio.isSelected()) {
      targetMode = LaunchConfiguration.TargetMode.MANUAL;
    } else {
      throw new IllegalStateException("Unknown target mode selected");
    }
    configs.updateCurrentConfig(
        ConfigBuilder.builderForConfig(configs.getCurrentConfig())
            .withTargetMode(targetMode).config());
  }

  //~ Inner classes

  private final class ConfigListModel extends DefaultComboBoxModel {

    public ConfigListModel() {
//      Set<String> configNames = new TreeSet<String>(configManager.getConfigNameComparator());
//      configNames.addAll(configManager.getConfigNames());
      for (Config config : configs.getConfigs()) {
        addElement(config);
      }
    }

  }

  private final class ConfigListRenderer extends JLabel implements ListCellRenderer, UIResource {

    public ConfigListRenderer() {
      setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      setName("ComboBox.listRenderer"); // NOI18N
      if (value instanceof Config) {
        Config cfg = (Config)value;
        setText(cfg.getDisplayName());
      } else {
        Logger.getLogger(CustomizerTest.class.getName()).log(Level.INFO, "Value is not Config {0}", value);
      }
//      setText(configManager.getConfigFor((String) value).getDisplayName());
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      return this;
    }

    @Override
    public String getName() {
      String name = super.getName();
      return name == null ? "ComboBox.renderer" : name;  // NOI18N
    }
  }
}
