package org.netbeans.modules.android.project.ui.layout;

import com.android.resources.UiMode;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.State;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.layout.ProjectThemes;
import org.netbeans.modules.android.project.layout.ResourceRepositoryManager;
import org.netbeans.modules.android.project.layout.ResourceRepositoryManager.LocaleConfig;
import org.netbeans.modules.android.project.layout.ThemeData;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;

/**
 *
 * @author radim
 */
public class DeviceConfiguratorPanel extends javax.swing.JPanel {
  private static final Logger LOG = Logger.getLogger(DeviceConfiguratorPanel.class.getName());
  private static final String PREF_DEVICE_NAME = "layoutDeviceName";
  // private static final String PREF_CONFIG_NAME = "layoutConfigurationName";
  private static final String PREF_PLATFORM_NAME = "layoutPlatformName";

//  private final static Dimension preferredSize = new Dimension(0, 20);
  /** List cell renderer for various objects used to configure layout rendering. */
  static class LayoutParamCellRenderer implements ListCellRenderer {

    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus) {
      JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
          isSelected, cellHasFocus);
      if (value instanceof Device) {
        renderer.setText(((Device) value).getName());
      } else if (value instanceof State) {
        renderer.setText(((State) value).getName());
      } else if (value instanceof DalvikPlatform) {
        renderer.setText(((DalvikPlatform) value).getAndroidTarget().getName());
      } else if (value instanceof UiMode) {
        renderer.setText(((UiMode) value).getLongDisplayValue());
      } else if (value instanceof LocaleConfig) {
        LocaleConfig l = (LocaleConfig) value;
        renderer.setText(l.getLongDisplayValue());
      } else if (value instanceof ThemeData) {
        ThemeData theme = (ThemeData) value;
        renderer.setText(theme.themeName);
      }
      return renderer;
    }
  }

  private PreviewModel model;
  private PreviewController controller;
  
  /**
   * Creates new form DeviceConfiguratorPanel
   */
  public DeviceConfiguratorPanel() {
    initComponents();
    try {
      // device types
      Iterable<Device> deviceList = DalvikPlatformManager.getDefault().getDevices();
      cbxDevice.setRenderer(new LayoutParamCellRenderer());
      DefaultComboBoxModel devicesModel = new DefaultComboBoxModel(Iterables.toArray(deviceList, Device.class));
      cbxDevice.setModel(devicesModel);
      
      // configuration (those are depending on device)
      cbxConfiguration.setRenderer(new LayoutParamCellRenderer());
      final DefaultComboBoxModel configsModel = new DefaultComboBoxModel();
      cbxConfiguration.setModel(configsModel);
      
      initPlatforms();
      initLocales();
      initThemes();
      
      // modes (normal, car, dock, TV)
      DefaultComboBoxModel modesModel = new DefaultComboBoxModel(UiMode.values());
      cbxMode.setModel(modesModel);
      
    } catch (Exception e) {
      LOG.log(Level.INFO, null, e);
    }
  }
  
  private void initPlatforms() {
    // android platforms
    List<DalvikPlatform> platforms = Lists.newArrayList(
        Iterables.filter(
            DalvikPlatformManager.getDefault().getPlatforms(),
            new Predicate<DalvikPlatform>() {

              @Override
              public boolean apply(DalvikPlatform p) {
                return p.getAndroidTarget().isPlatform();
              }
            }
        ));
    cbxTarget.setRenderer(new LayoutParamCellRenderer());
    DefaultComboBoxModel targetsModel = new DefaultComboBoxModel(platforms.toArray());
    cbxTarget.setModel(targetsModel);
  }

  private void initLocales() {
    cbxLocale.setRenderer(new LayoutParamCellRenderer());
    DefaultComboBoxModel targetsModel = new DefaultComboBoxModel();
    cbxLocale.setModel(targetsModel);
  }

  private void initThemes() {
    cbxTheme.setRenderer(new LayoutParamCellRenderer());
    DefaultComboBoxModel themesModel = new DefaultComboBoxModel();
    cbxTheme.setModel(themesModel);
  }

  void attachToController(PreviewModel model, PreviewController controller) {
    this.model = model;
    this.controller = controller;

    model.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (PreviewModel.PROP_FILEOBJECT.equals(evt.getPropertyName())) {
          updateLocale();
          updateThemes();
        } else if (PreviewModel.PROP_PLATFORM.equals(evt.getPropertyName())) {
          updatePlatform();
          updateThemes();
        } else if (PreviewModel.PROP_DEVICE.equals(evt.getPropertyName())) {
          updateDeviceConfigurations();
        }
      }
    });
    
    cbxDevice.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DeviceConfiguratorPanel.this.controller.updateDevice((Device) cbxDevice.getModel().getSelectedItem());
      }
    });
    cbxConfiguration.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // persist config?
        // tell controller to initiate repaint
        DeviceConfiguratorPanel.this.controller.updateConfiguration((State) cbxConfiguration.getSelectedItem());
      }
    });
    cbxTarget.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // tell controller to initiate repaint
        DalvikPlatform platform = (DalvikPlatform) cbxTarget.getSelectedItem();
        DeviceConfiguratorPanel.this.controller.updatePlatform(platform);
        if (platform != null) {
          NbPreferences.forModule(DeviceConfiguratorPanel.class).put(
              PREF_PLATFORM_NAME, platform.getAndroidTarget().getName());
        }
      }
    });
    cbxLocale.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // tell controller to initiate repaint
        LocaleConfig l = (LocaleConfig) cbxLocale.getSelectedItem();
        if (l != null) {
          DeviceConfiguratorPanel.this.controller.updateLocaleConfig(l);
        }
      }
    });
    cbxMode.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // tell controller to initiate repaint
        DeviceConfiguratorPanel.this.controller.updateUiMode((UiMode) cbxMode.getSelectedItem());
      }
    });
    cbxTheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // tell controller to initiate repaint
        DeviceConfiguratorPanel.this.controller.updateTheme((ThemeData) cbxTheme.getSelectedItem());
      }
    });
    doInitialSetup();
    updateDeviceConfigurations();
    updateLocale();
  }
  
  private void doInitialSetup() {
    if (cbxDevice.getModel().getSize() > 0) {
      // String deviceName = NbPreferences.forModule(DeviceConfiguratorPanel.class).get(PREF_DEVICE_NAME, null);
      Device device = model.getDevice();
      if (device != null) {
        cbxDevice.getModel().setSelectedItem(device);
      }
    }
    if (cbxTarget.getModel().getSize() > 0) {
      String targetName = NbPreferences.forModule(DeviceConfiguratorPanel.class).get(PREF_PLATFORM_NAME, null);
      Object platform = null;
      if (targetName != null) {
        for (int i = 0; i < cbxTarget.getModel().getSize(); i++) {
          Object p = cbxTarget.getModel().getElementAt(i);
          if (p instanceof DalvikPlatform && ((DalvikPlatform) p).getAndroidTarget().getName().equals(targetName)) {
            platform = p;
            break;
          }
        }
      }
      if (platform != null) {
        cbxTarget.getModel().setSelectedItem(platform);
      } else {
        // this will set first config
        cbxTarget.getModel().setSelectedItem(cbxTarget.getModel().getElementAt(0));
      }
    }
    
    cbxMode.getModel().setSelectedItem(model.getUiMode());
  }
  
  private void updateDeviceConfigurations() {
    Device device = model.getDevice();
    State deviceConfig = model.getDeviceConfig();
    if (device != null) {
      NbPreferences.forModule(DeviceConfiguratorPanel.class).put(PREF_DEVICE_NAME, device.getName());
    }
    List<State> configs = device != null ? device.getAllStates() : Collections.<State>emptyList();
    DefaultComboBoxModel configsModel = (DefaultComboBoxModel) cbxConfiguration.getModel();
    configsModel.removeAllElements();
    for (State config : configs) {
      configsModel.addElement(config);
    }
    if (configsModel.getSize() > 0 && deviceConfig != null) {
      configsModel.setSelectedItem(deviceConfig);
    } else {
      configsModel.setSelectedItem(null);
    }
  }
  
  private void updateLocale() {
    Project p = null;
    FileObject fo = model.getFileObject();
    if (fo != null) {
      p = FileOwnerQuery.getOwner(fo);
    }
    List<LocaleConfig> locales = ResourceRepositoryManager.getLocaleConfigs(p);
    DefaultComboBoxModel cbxModel = (DefaultComboBoxModel) cbxLocale.getModel();
    Object oldSelection = cbxModel.getSelectedItem();
    cbxModel.removeAllElements();
    for (LocaleConfig l : locales) {
      cbxModel.addElement(l);
    }
    if (oldSelection != null && locales.contains(oldSelection)) {
      cbxModel.setSelectedItem(oldSelection);
    } else if (cbxModel.getSize() > 0) {
      // Any/Other is the last one
      cbxModel.setSelectedItem(cbxModel.getElementAt(cbxModel.getSize() - 1));
    }
  }
  
  private void updatePlatform() {
    DalvikPlatform platform = model.getPlatform();
    if (platform != null) {
      for (int i = 0; i < cbxTarget.getModel().getSize(); i++) {
        Object p = cbxTarget.getModel().getElementAt(i);
        if (p instanceof DalvikPlatform && ((DalvikPlatform) p).equals(platform)) {
          cbxTarget.getModel().setSelectedItem(p);
          return;
        }
      }
      LOG.log(Level.FINE, "cannot set platform for layout rendering to {0}", platform);
    }
  }
  
  private void updateThemes() {
    Iterable<ThemeData> themes;
    // themes from project
    FileObject fo = model.getFileObject();
    if (fo != null) {
      Project p = FileOwnerQuery.getOwner(fo);
      themes = new ProjectThemes(p).getProjectThemes();
    } else {
      themes = Collections.emptyList();
    }
    ThemeData theme = model.getTheme();
    // themes from platform
    DalvikPlatform platform = model.getPlatform();
    if (platform != null) {
      themes = Iterables.concat(
          themes, 
          Iterables.transform(
              platform.getThemes(),
              new Function<String, ThemeData>() {
                @Override public ThemeData apply(String t) {
                  return new ThemeData(t, false);
                }
              }));
    }
    
    // update the model
    DefaultComboBoxModel cbxModel = (DefaultComboBoxModel) cbxTheme.getModel();
    cbxModel.removeAllElements();
    
    for (ThemeData themeData : Sets.newTreeSet(themes)) {
      cbxModel.addElement(themeData);
      if (themeData.equals(theme)) {
        cbxModel.setSelectedItem(themeData);
      }
    }
  }
  
  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    cbxDevice = new javax.swing.JComboBox();
    cbxTarget = new javax.swing.JComboBox();
    cbxConfiguration = new javax.swing.JComboBox();
    cbxLocale = new javax.swing.JComboBox();
    cbxMode = new javax.swing.JComboBox();
    cbxTime = new javax.swing.JComboBox();
    cbxTheme = new javax.swing.JComboBox();

    cbxDevice.setToolTipText(org.openide.util.NbBundle.getMessage(DeviceConfiguratorPanel.class, "DeviceConfiguratorPanel.cbxDevice.toolTipText")); // NOI18N
    cbxDevice.setPreferredSize(null);
    cbxDevice.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbxDeviceActionPerformed(evt);
      }
    });

    cbxTarget.setToolTipText(org.openide.util.NbBundle.getMessage(DeviceConfiguratorPanel.class, "DeviceConfiguratorPanel.cbxTarget.toolTipText")); // NOI18N
    cbxTarget.setPreferredSize(null);

    cbxConfiguration.setToolTipText(org.openide.util.NbBundle.getMessage(DeviceConfiguratorPanel.class, "DeviceConfiguratorPanel.cbxConfiguration.toolTipText")); // NOI18N
    cbxConfiguration.setPreferredSize(null);

    cbxLocale.setToolTipText(org.openide.util.NbBundle.getMessage(DeviceConfiguratorPanel.class, "DeviceConfiguratorPanel.cbxLocale.toolTipText")); // NOI18N
    cbxLocale.setPreferredSize(null);

    cbxMode.setToolTipText(org.openide.util.NbBundle.getMessage(DeviceConfiguratorPanel.class, "DeviceConfiguratorPanel.cbxMode.toolTipText")); // NOI18N
    cbxMode.setPreferredSize(null);

    cbxTime.setToolTipText(org.openide.util.NbBundle.getMessage(DeviceConfiguratorPanel.class, "DeviceConfiguratorPanel.cbxTime.toolTipText")); // NOI18N
    cbxTime.setPreferredSize(null);

    cbxTheme.setToolTipText(org.openide.util.NbBundle.getMessage(DeviceConfiguratorPanel.class, "DeviceConfiguratorPanel.cbxTheme.toolTipText")); // NOI18N
    cbxTheme.setPreferredSize(null);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(cbxTheme, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cbxLocale, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cbxDevice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(cbxConfiguration, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cbxMode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(cbxTarget, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cbxTime, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(cbxDevice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cbxTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(cbxConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(cbxLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(cbxMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(cbxTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbxTheme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void cbxDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxDeviceActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_cbxDeviceActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox cbxConfiguration;
  private javax.swing.JComboBox cbxDevice;
  private javax.swing.JComboBox cbxLocale;
  private javax.swing.JComboBox cbxMode;
  private javax.swing.JComboBox cbxTarget;
  private javax.swing.JComboBox cbxTheme;
  private javax.swing.JComboBox cbxTime;
  // End of variables declaration//GEN-END:variables
}
