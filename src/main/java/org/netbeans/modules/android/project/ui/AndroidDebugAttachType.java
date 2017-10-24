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

package org.netbeans.modules.android.project.ui;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.android.core.ddm.AndroidDebugBridgeFactory;
import org.netbeans.modules.android.project.spi.AndroidDebugInfo;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Registers an attach type to debug android processes via the attach debugger submenu.
 */
@AttachType.Registration(displayName="#LBL_DebugAttachType")
public class AndroidDebugAttachType extends AttachType {

  private Reference<AndroidDebugAttachCustomizerPanel> customizerRef = new WeakReference<AndroidDebugAttachCustomizerPanel>(null);

  @Override
  public JComponent getCustomizer () {
      AndroidDebugAttachCustomizerPanel panel = new AndroidDebugAttachCustomizerPanel ();
      customizerRef = new WeakReference<AndroidDebugAttachCustomizerPanel>(panel);
      return panel;
  }

  @Override
  public Controller getController() {
      AndroidDebugAttachCustomizerPanel panel = customizerRef.get();
      if (panel != null) {
          return panel.getController();
      } else {
          return null;
      }
  }


  private static class AndroidDebugAttachController implements Controller
  {
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private AndroidDebugAttachCustomizerPanel panel;
    private boolean valid = false;

    private AndroidDebugAttachController(AndroidDebugAttachCustomizerPanel panel) {
      this.panel = panel;
    }

    @Override
    public boolean ok() {
      AndroidDebugInfo debugInfo = panel.getSelectedProject();
      Client client = panel.getSelectedClient();
      assert debugInfo != null : "no project selected";
      assert client  != null : "no process selected";

      try {
        AndroidDebugInfo.AndroidDebugData data = debugInfo.data(client);
        JPDADebugger.attach(data.hostname, data.port, new Object[]{data.properties});

        // successful
        return true;
      }
      catch(DebuggerStartException e) {
        Exceptions.printStackTrace(e);
      }

      return false;
    }

    @Override
    public boolean cancel() {
      return true;
    }

    @Override
    public boolean isValid() {
      return valid;
    }

    private void checkIfValid() {
      if (
           panel.getSelectedDevice()  != null
        && panel.getSelectedClient()  != null
        && panel.getSelectedProject() != null
      ) {
        setValid(true);
      }
      else {
        setValid(false);
      }
    }

    void setValid(boolean valid) {
      this.valid = valid;
      firePropertyChange(PROP_VALID, !valid, valid);
    }

    void setErrorMessage(String msg) {
      firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, msg);
    }

    void setInformationMessage(String msg) {
      firePropertyChange(NotifyDescriptor.PROP_INFO_NOTIFICATION, null, msg);
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
      changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
      changeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
      changeSupport.removePropertyChangeListener(l);
    }
  }


  private static class AndroidDebugAttachCustomizerPanel extends JPanel
  {
    private AndroidDebugAttachController controller = new AndroidDebugAttachController(this);

    private JLabel    lblDevices  = new JLabel(NbBundle.getMessage(AndroidDebugAttachType.class, "LBL_Device"));
    private JLabel    lblClients  = new JLabel(NbBundle.getMessage(AndroidDebugAttachType.class, "LBL_Process"));
    private JLabel    lblProjects = new JLabel(NbBundle.getMessage(AndroidDebugAttachType.class, "LBL_Project"));

    private JComboBox cmbDevices  = new JComboBox();
    private JComboBox cmbClients  = new JComboBox();
    private JComboBox cmbProjects = new JComboBox();

    public AndroidDebugAttachCustomizerPanel() {
      GridBagLayout layout = new GridBagLayout();
      setLayout(layout);

      GridBagConstraints cLeft  = new GridBagConstraints();
      cLeft.anchor = GridBagConstraints.WEST;
      cLeft.insets = new Insets(6, 6, 0, 6);

      GridBagConstraints cRight = new GridBagConstraints();
      cRight.gridwidth = GridBagConstraints.REMAINDER;
      cRight.fill = GridBagConstraints.HORIZONTAL;
      cRight.weightx = 1.0;
      cLeft.insets = new Insets(6, 0, 0, 6);

      // create a ListCellRenderer
      ListCellRenderer renderer = new ListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          JLabel cmp = null;

          // device?
          if (value instanceof IDevice) {
            IDevice device = (IDevice)value;
            cmp = new JLabel(device.getAvdName());
          }

          // client?
          if (value instanceof Client) {
            Client client = (Client)value;
            String desc = client.getClientData().getClientDescription();

            if (desc == null) {
              desc = client.getClientData().getVmIdentifier();
            }

            cmp = new JLabel(desc);
          }

          // project?
          if (value instanceof AndroidDebugInfo) {
            Project project = ((AndroidDebugInfo) value).project();
            ProjectInformation info = ProjectUtils.getInformation(project);

            if (info != null) {
              cmp = new JLabel(info.getDisplayName());
              cmp.setIcon(info.getIcon());
            }
          }

          // default toString, if value is unknown
          if (cmp == null) {
            cmp = new JLabel(value!=null ? value.toString() : "");
          }

          if (isSelected) {
            cmp.setForeground(list.getSelectionForeground());
            cmp.setBackground(list.getSelectionBackground());
            cmp.setOpaque(true);
          } else {
            cmp.setForeground(list.getForeground());
            cmp.setBackground(list.getBackground());
            cmp.setOpaque(false);
          }

          return cmp;
        }
      };

      // create a listener
      ActionListener listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == cmbDevices) {
            IDevice device = (IDevice)cmbDevices.getSelectedItem();

            if (device == null) {
              cmbClients.setModel(new DefaultComboBoxModel());
            }
            else {
              cmbClients.setModel(new DefaultComboBoxModel(device.getClients()));
            }

            cmbClients.setSelectedItem(null);
          }

          getController().checkIfValid();
        }
      };

      // devices list
      {
        final AndroidDebugBridge bridge = AndroidDebugBridgeFactory.getDefault();
        if (bridge != null) {                
          cmbDevices.setModel(new DefaultComboBoxModel(bridge.getDevices()));
        }

        lblDevices.setLabelFor(cmbDevices);
        layout.setConstraints(lblDevices, cLeft);
        add(lblDevices);

        cmbDevices.addActionListener(listener);
        cmbDevices.setRenderer(renderer);
        layout.setConstraints(cmbDevices, cRight);
        add(cmbDevices);
      }

      // clients list
      {
        lblClients.setLabelFor(cmbDevices);
        layout.setConstraints(lblClients, cLeft);
        add(lblClients);

        cmbClients.addActionListener(listener);
        cmbClients.setRenderer(renderer);
        layout.setConstraints(cmbClients, cRight);
        add(cmbClients);

        IDevice device = (IDevice)cmbDevices.getSelectedItem();
        if (device != null) {
          cmbClients.setModel(new DefaultComboBoxModel(device.getClients()));
          cmbClients.setSelectedItem(null);
        }
      }

      // project list
      {
        List<AndroidDebugInfo> projects = Lists.newArrayList(
            Iterables.filter(
                Iterables.transform(
                    Arrays.asList(OpenProjects.getDefault().getOpenProjects()),
                    new Function<Project, AndroidDebugInfo>() {
                      @Override
                      public AndroidDebugInfo apply(Project p) {
                        return p.getLookup().lookup(AndroidDebugInfo.class);
                      }
                    }),
                new Predicate<AndroidDebugInfo>() {

                  @Override
                  public boolean apply(AndroidDebugInfo t) {
                    return t != null && t.supportsDebugging();
                  }
                }));

        lblProjects.setLabelFor(cmbProjects);
        layout.setConstraints(lblProjects, cLeft);
        add(lblProjects);

        cmbProjects.setModel(new DefaultComboBoxModel(projects.toArray()));
        cmbProjects.addActionListener(listener);
        cmbProjects.setRenderer(renderer);
        layout.setConstraints(cmbProjects, cRight);
        add(cmbProjects);
      }

      getController().checkIfValid();

      return;
    }

    public IDevice getSelectedDevice() {
      Object o = cmbDevices.getSelectedItem();
      if (o instanceof IDevice) {
        return (IDevice)o;
      }

      return null;
    }

    public Client getSelectedClient() {
      Object o = cmbClients.getSelectedItem();
      if (o instanceof Client) {
        return (Client)o;
      }

      return null;
    }

    public AndroidDebugInfo getSelectedProject() {
      Object o = cmbProjects.getSelectedItem();
      if (o instanceof AndroidDebugInfo) {
        return (AndroidDebugInfo) o;
      }

      return null;
    }

    public final AndroidDebugAttachController getController() {
      return controller;
    }
  }
}

