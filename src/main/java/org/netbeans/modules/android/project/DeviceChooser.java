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
import com.android.sdklib.internal.avd.AvdManager;
import org.netbeans.modules.android.project.AvdSelector.LaunchData;

/**
 * An interface to select a device
 *
 * @author radim
 */
public interface DeviceChooser {

  /**
   * Selects a device
   *
   * @return device info or {@code null}
   */
  LaunchData selectDevice(AvdManager avdManager, IDevice[] devices);
}
