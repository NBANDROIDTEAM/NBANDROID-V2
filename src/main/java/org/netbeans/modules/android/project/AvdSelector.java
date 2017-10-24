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

package org.netbeans.modules.android.project;

import com.android.ddmlib.IDevice;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.repository.local.LocalPlatformPkgInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import org.openide.util.Exceptions;
import org.xml.sax.SAXParseException;

/**
 * A class responsible for selecting right AVD/device combination for an application
 * developed against given target.
 *
 * <p>The result depends on an information from AvdManager and list of current known
 * devices from Android debug bridge.
 *
 * @author radim
 */
public class AvdSelector {

  private static final Logger LOG = Logger.getLogger(AvdSelector.class.getName());

  /**
   * A wrapper for data need to run the application.
   * {@link AvdInfo} is needed to start the emulator if it is not running yet.
   * Then {@code adb} can target its communicates to a {@link IDevice}.
   */
  public static final class LaunchData {
    private final AvdInfo avdInfo;
    private final IDevice device;

    public LaunchData(AvdInfo avdInfo, IDevice device) {
      if (avdInfo == null && device == null) {
        throw new IllegalArgumentException(
            "AVD (" + avdInfo + ") or device (" + device + ") must not be null");
      }
      this.avdInfo = avdInfo;
      this.device = device;
    }

    public AvdInfo getAvdInfo() {
      return avdInfo;
    }

    public IDevice getDevice() {
      return device;
    }

    @Override
    public String toString() {
      return "LaunchData{" + "avdInfo=" + avdInfo + ", device=" + device + '}';
    }
  }

  public interface AvdManagerMock { // TODO AvdManager should be interface
    AvdManager getAvdManager();
    void reloadAvds() throws AndroidLocationException, SAXParseException;
    AvdInfo getAvd(String name, boolean validAvdOnly);
    AvdInfo[] getValidAvds();
  }

  private final LaunchConfiguration.TargetMode mode;
  private final String prefferedAvdName;
  private final AvdManagerMock avdManager;
  private final IAndroidTarget target;
  private final IDevice[] devices;

  /**
   *
   * @param mode auto or manual
   * @param prefferedAvdName if it is auto there can a name of favored device, null otherwise
   * @param avdManager
   * @param target target platform to check compatibility
   * @param adbDevices list of currently running devices from ADB
   */
  public AvdSelector(LaunchConfiguration.TargetMode mode, String prefferedAvdName,
      AvdManagerMock avdManager, IAndroidTarget target, IDevice[] adbDevices) {
    this.mode = mode;
    this.prefferedAvdName = prefferedAvdName;
    this.avdManager = avdManager;
    this.target = target;
    this.devices = adbDevices != null ? adbDevices : new IDevice[0];
  }

  /**
   * Here we want to decide what AVD to use for application deployment.
   * Optionally we may need device too.
   *
   * @param chooser a helper to choose a device if we fail to find one
   * @return
   */
  public LaunchData selectDevice(DeviceChooser chooser) {
    LaunchData launch = null;
    if (LaunchConfiguration.TargetMode.AUTO.equals(mode)) {
      launch = selectAvdName();
    }
    LOG.log(Level.FINE, "pre-selected AVD {0}", launch);
    if (launch == null && chooser != null) {
      launch = chooser.selectDevice(avdManager.getAvdManager(), devices);
    }
    return launch;
  }

  /**
   * Finds a device according to criteria from provided sources.
   * @return
   */
  LaunchData selectAvdName() {
    try {
      // look for all AVDs and find compatible ones
      // if there is just one use it
      // other count -> show UI
      avdManager.reloadAvds();
      if (prefferedAvdName != null) {
        AvdInfo candidateAvd =
            avdManager.getAvd(prefferedAvdName, true /*validAvdOnly*/);
        // be optimistic if target is null
        if (target != null && !target.canRunOn(candidateAvd.getTarget())) {
          candidateAvd = null;
        }
        if (candidateAvd != null) {
          // we can verify if there is a device with that AVD name
          // and start it if it is not but so far we leave it and start it from Ant
          for (IDevice device : devices) {
            String deviceAvd = device.getAvdName();
            if (deviceAvd != null && deviceAvd.equals(candidateAvd.getName())) {
              // it is running and matches to what we want
              return new LaunchData(candidateAvd, device);
            }
          }
          // matchingAVD exists but is not running yet
          return new LaunchData(candidateAvd, null);
        }
      }
      // no preffered AVD or no valid, still it is auto so do our best
      // try ADB to see if there is a good match
      boolean hasUnknownDevice = false; // true if we find a device but don't know if it can be used
      Map<IDevice, AvdInfo> compatibleAvds = new HashMap<IDevice, AvdInfo>();
      for (IDevice device : devices) {
        String deviceAvd = device.getAvdName();
        if (deviceAvd != null) { // physical devices return null.
          AvdInfo info = avdManager.getAvd(deviceAvd, true /*validAvdOnly*/);
          if (info != null && (target == null || target.canRunOn(info.getTarget()))) {
            compatibleAvds.put(device, info);
          }
        } else {
          if (target == null || target.isPlatform()) {
            // means this can run on any device as long
            // as api level is high enough
            String apiString = device.getProperty(LocalPlatformPkgInfo.PROP_VERSION_SDK);
            try {
              int apiNumber = Integer.parseInt(apiString);
              if (target == null || apiNumber >= target.getVersion().getApiLevel()) {
                // device is compatible with project
                compatibleAvds.put(device, null);
                continue;
              }
            } catch (NumberFormatException e) {
              // do nothing, we'll consider it a non compatible device below.
            }
          }
          hasUnknownDevice = true;
        }
      }
      if (!hasUnknownDevice) {
        LOG.log(Level.FINE, "compatible avds: {0}", compatibleAvds);
        if (compatibleAvds.isEmpty()) {
          AvdInfo matchingAvd = findAvdForTarget();
          return matchingAvd != null ? new LaunchData(matchingAvd, null) : null;
        } else if (compatibleAvds.size() == 1) {
          Map.Entry<IDevice, AvdInfo> entry = compatibleAvds.entrySet().iterator().next();
          // there is one known devices and it is compatible
          return new LaunchData(entry.getValue(), entry.getKey());
        }
      }
    } catch (SAXParseException ex) {
      Exceptions.attachMessage(ex, "Cannot initialize list of devices.");
      Exceptions.printStackTrace(ex);
    } catch (AndroidLocationException ex) {
      Exceptions.attachMessage(ex, "Cannot initialize list of devices.");
      Exceptions.printStackTrace(ex);
    }
    return null;
  }

  private AvdInfo findAvdForTarget() {
    AvdInfo[] avds = avdManager.getValidAvds();
    AvdInfo bestMatchAvd = null;
    for (AvdInfo avd : avds) {
      if (target != null && target.canRunOn(avd.getTarget())) {
        if (bestMatchAvd == null ||
            avd.getTarget().getVersion().getApiLevel() < bestMatchAvd.getTarget().getVersion().getApiLevel()) {
          bestMatchAvd = avd;
        }
      }
    }
    return bestMatchAvd;
  }
}
