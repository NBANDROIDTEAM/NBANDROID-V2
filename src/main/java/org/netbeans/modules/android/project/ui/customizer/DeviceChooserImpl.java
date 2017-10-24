/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.netbeans.modules.android.project.ui.customizer;

import com.android.ddmlib.IDevice;
import com.android.sdklib.internal.avd.AvdManager;
import java.awt.Dialog;
import org.netbeans.modules.android.project.AvdSelector.LaunchData;
import org.netbeans.modules.android.project.DeviceChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * An interface to select a device
 *
 * @author radim
 */
public class DeviceChooserImpl implements DeviceChooser {

  /**
   * Selects a device
   *
   * @return device info or {@code null}
   */
  @Override
  public LaunchData selectDevice(AvdManager avdManager, IDevice[] devices) {
      final DeviceUiChooser panel = new DeviceUiChooser(avdManager, devices);
    final Object[] options = new Object[]{
      DialogDescriptor.OK_OPTION,
      DialogDescriptor.CANCEL_OPTION
      };

      final DialogDescriptor desc = new DialogDescriptor(panel,
              "Select device",
        true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
    desc.setMessageType(DialogDescriptor.INFORMATION_MESSAGE);
    desc.setValid(false); // no selection yet
    panel.addLaunchDataListener(new LaunchDeviceListener() {

      @Override
      public void lauchDeviceChanged(LaunchData launchData) {
        desc.setValid(launchData != null);
      }
    });

    final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
    panel.addSelectCallback(new Runnable() {

      @Override
      public void run() {
        dlg.setVisible(false);
        desc.setValue(options[0]);
      }
    });
    dlg.setVisible(true);
    dlg.dispose();
    if (desc.getValue() == options[0]) {
      return panel.getLaunchData();
    }
    return null;
  }
}
