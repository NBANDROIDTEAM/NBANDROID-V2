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

package org.netbeans.modules.android.core.ddm;

import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.EmulatorConsole.GsmMode;
import com.android.ddmlib.IDevice;
import com.google.common.base.Objects;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author radim
 */
public class EmulatorControlSupport {

  /**
   * Map between the display GSMmode and the internal tag used by the display.
   */
  private static final String[][] GSM_MODES = new String[][]{
      {"unregistered", GsmMode.UNREGISTERED.getTag()},
      {"home", GsmMode.HOME.getTag()},
      {"roaming", GsmMode.ROAMING.getTag()},
      {"searching", GsmMode.SEARCHING.getTag()},
      {"denied", GsmMode.DENIED.getTag()},
  };

  private static final String[] NETWORK_SPEEDS = new String[]{
      "Full",
      "GSM",
      "HSCSD",
      "GPRS",
      "EDGE",
      "UMTS",
      "HSDPA",
  };

  private static final String[] NETWORK_LATENCIES = new String[]{
      "None",
      "GPRS",
      "EDGE",
      "UMTS",
  };

  private final IDevice device;
  private EmulatorConsole console;

  /** Device properties that can be controlled by tools. */
  public enum Control {
    VOICE(NbBundle.getMessage(EmulatorControlSupport.class, "PROP_Voice"), 
        NbBundle.getMessage(EmulatorControlSupport.class, "DESC_Voice")),
    DATA(NbBundle.getMessage(EmulatorControlSupport.class, "PROP_Data"), 
        NbBundle.getMessage(EmulatorControlSupport.class, "DESC_Data")),
    SPEED(NbBundle.getMessage(EmulatorControlSupport.class, "PROP_Speed"), 
        NbBundle.getMessage(EmulatorControlSupport.class, "DESC_Speed")),
    LATENCY(NbBundle.getMessage(EmulatorControlSupport.class, "PROP_Latency"), 
        NbBundle.getMessage(EmulatorControlSupport.class, "DESC_Latency"));

    private final String displayName;
    private final String description;

    private Control(String displayName, String description) {
      this.displayName = displayName;
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  public EmulatorControlSupport(IDevice device) {
    this.device = device;
    // TODO init lazily?
    console = EmulatorConsole.getConsole(device);
  }

  public boolean hasConsole() {
    return console != null;
  }

  public String [] getTags(Control ctrl) {
    switch (ctrl) {
    // first two are equal
    case VOICE:
    case DATA:
      String[] tags = new String[GSM_MODES.length];
      for (int i = 0; i < GSM_MODES.length; i++) {
        tags[i] = GSM_MODES[i][0];
      }
      return tags;
    case LATENCY:
      return NETWORK_LATENCIES;
    case SPEED:
      return NETWORK_SPEEDS;
    default:
      throw new IllegalStateException();
    }
  }

  public String getData(Control ctrl) {
    switch (ctrl) {
    // first two are equal
    case VOICE:
      GsmMode gsmModeVoice = console.getGsmStatus().voice;
      if (gsmModeVoice != null) {
        for (int i = 0; i < GSM_MODES.length; i++) {
          if (GSM_MODES[i][1].equals(gsmModeVoice.getTag())) {
            return GSM_MODES[i][0];
          }
        }
      }
      break;
    case DATA:
      GsmMode gsmModeData = console.getGsmStatus().data;
      if (gsmModeData != null) {
        for (int i = 0; i < GSM_MODES.length; i++) {
          if (GSM_MODES[i][1].equals(gsmModeData.getTag())) {
            return GSM_MODES[i][0];
          }
        }
      }
      break;
    case LATENCY:
      int latency = console.getNetworkStatus().latency;
      return latency != -1 ? NETWORK_LATENCIES[latency] : "";
    case SPEED:
      int speed = console.getNetworkStatus().speed;
      return speed != -1 ? NETWORK_SPEEDS[speed] : "";
    default:
      throw new IllegalStateException();
    }
    return "";
  }

  public void setData(Control ctrl, String val) {
    switch (ctrl) {
    // first two are equal
    case VOICE:
      if ("".equals(val)) {
        // no-op
        return;
      }
      for (int i = 0; i < GSM_MODES.length; i++) {
        if (GSM_MODES[i][0].equals(val)) {
          processCommandResult(console.setGsmVoiceMode(GsmMode.getEnum(GSM_MODES[i][1])));
          return;
        }
      }
      break;
    case DATA:
      if ("".equals(val)) {
        // no-op
        return;
      }
      for (int i = 0; i < GSM_MODES.length; i++) {
        if (GSM_MODES[i][0].equals(val)) {
          processCommandResult(console.setGsmDataMode(GsmMode.getEnum(GSM_MODES[i][1])));
          return;
        }
      }
      break;
    case LATENCY:
      for (int i = 0; i < NETWORK_LATENCIES.length; i++) {
        if (NETWORK_LATENCIES[i].equals(val)) {
          processCommandResult(console.setNetworkLatency(i));
          return;
        }
      }
      break;
    case SPEED:
      for (int i = 0; i < NETWORK_SPEEDS.length; i++) {
        if (NETWORK_SPEEDS[i].equals(val)) {
          processCommandResult(console.setNetworkSpeed(i));
          return;
        }
      }
      break;
    default:
      throw new IllegalStateException();
    }
  }

  private void processCommandResult(String result) {
    if (Objects.equal(EmulatorConsole.RESULT_OK, result)) {
      return;
    }
    DialogDisplayer.getDefault().notify(
        new NotifyDescriptor.Message("Emulator console : " + result, NotifyDescriptor.WARNING_MESSAGE));
  }
}
