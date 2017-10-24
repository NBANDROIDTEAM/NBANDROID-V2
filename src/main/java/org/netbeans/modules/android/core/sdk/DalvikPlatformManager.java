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
package org.netbeans.modules.android.core.sdk;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkManager;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.DeviceManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * A registry of known Android development platforms.
 *
 * Matches to {@link SdkManager} but has to be initialized first.
 * No platform is returned until SDK location is set (usually when project is opened or from UI).
 *
 * @author jesse, radim
 */
public final class DalvikPlatformManager {

  public static final String PROP_INSTALLED_PLATFORMS = "installedPlatforms";
  public static final String PROP_SDK_LOCATION = "sdkLocation";

  private static final Logger LOG = Logger.getLogger(DalvikPlatformManager.class.getName());
  private static final DalvikPlatformManager INSTANCE = new DalvikPlatformManager();

  public static DalvikPlatformManager getDefault() {
    return INSTANCE;
  }

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private String sdkLocation;
  private SdkManager mgr;
  private DeviceManager deviceManager;
  private List<DalvikPlatform> platforms = Collections.emptyList();

  @VisibleForTesting
  DalvikPlatformManager() {
    try {
      sdkLocation = NbPreferences.forModule(DalvikPlatformManager.class).get(PROP_SDK_LOCATION, null);
      LOG.log(Level.CONFIG, "DalvikPlatformManager initialized with location {0}", sdkLocation);
      if (sdkLocation != null) {
        readPlatforms();
      }
    } catch (RuntimeException ex) {
      LOG.log(Level.INFO, "DalvikPlatformManager cannot be restored", ex);
      NbPreferences.forModule(DalvikPlatformManager.class).remove(PROP_SDK_LOCATION);
      sdkLocation = null;
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener propertyChange) {
    pcs.addPropertyChangeListener(propertyChange);
  }

  public void removePropertyChangeListener(PropertyChangeListener propertyChange) {
    pcs.removePropertyChangeListener(propertyChange);
  }

  public Iterable<DalvikPlatform> getPlatforms() {
    return platforms;
  }

  public String getSdkLocation() {
    return sdkLocation;
  }

  public SdkManager getSdkManager() {
    return mgr;
  }

  public DeviceManager getDeviceManager() {
    if (deviceManager == null) {
      deviceManager = DeviceManager.createInstance(
          sdkLocation != null ? new File(sdkLocation) : null, SdkLogProvider.createLogger(true));
    }
    return deviceManager;
  }

  public Iterable<Device> getDevices() {
    return getDeviceManager().getDevices(DeviceManager.ALL_DEVICES);
  }

  public void setSdkLocation(String sdkLocation) {
    if (Objects.equal(sdkLocation, this.sdkLocation)) {
      return;
    }
    // XXX rewriting is dangerous, must be done atomically and may affect existing projects
    String old = this.sdkLocation;
    this.sdkLocation = sdkLocation;
    try {
      NbPreferences.forModule(DalvikPlatformManager.class).put(PROP_SDK_LOCATION, sdkLocation);
      LOG.log(Level.CONFIG, "DalvikPlatformManager updated SDK location to {0}", sdkLocation);
      readPlatforms();
      pcs.firePropertyChange(PROP_SDK_LOCATION, old, sdkLocation);
    } catch (RuntimeException ex) {
      LOG.log(Level.INFO, "Cannot set SDK location", ex);
      NbPreferences.forModule(DalvikPlatformManager.class).remove(PROP_SDK_LOCATION);
      this.sdkLocation = null;
    }
  }

  private void readPlatforms() {
    StatsCollector.getDefault().incrementCounter("platform");
    
    mgr = SdkManager.createManager(
        sdkLocation, SdkLogProvider.createLogger(true));
    if (mgr == null) {
      throw new IllegalArgumentException("Error parsing sdk.");
    }
    if (mgr.getPlatformToolsVersion() == null && mgr.getLatestBuildTool() == null) {
      throw new IllegalArgumentException("Error parsing sdk - missing build tool and platform tools.");
    }
    List<DalvikPlatform> oldPlatforms = platforms;
    List<DalvikPlatform> readPlatforms = new ArrayList<>();
    for (IAndroidTarget t : mgr.getTargets()) {
      try {
        readPlatforms.add(new DalvikPlatformImpl(mgr, t));
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex); // XXX just log it?
      }
    }
    platforms = Collections.unmodifiableList(readPlatforms);
    pcs.firePropertyChange(PROP_INSTALLED_PLATFORMS, oldPlatforms, platforms);
  }

  @Nullable
  public DalvikPlatform findPlatformForTarget(String target) {
    if (target == null) {
      return null;
    }
    if (mgr != null) {
      for (DalvikPlatform dp : platforms) {
        if (dp.getAndroidTarget().hashString().equals(target)) {
          return dp;
        }
      }
    }
    return null;
  }
  
  @Nullable
  public DalvikPlatform findPlatformForSdkVersion(final int sdkVersion) {
    return Iterables.find(
        platforms, 
        new Predicate<DalvikPlatform>() {

          @Override
          public boolean apply(DalvikPlatform p) {
            return p.getAndroidTarget().getVersion().getApiLevel() == sdkVersion;
          }
        }, 
        null);
  }
}
