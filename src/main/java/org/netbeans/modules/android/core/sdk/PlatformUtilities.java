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

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.utils.ILogger;

/**
 *
 * @author radim
 */
public class PlatformUtilities {

  public static AvdManager createAvdManager(SdkManager sdkMgr) throws AndroidLocationException {
    final ILogger sdkLog = SdkLogProvider.createLogger(true);
    final AvdManager avdMgr = AvdManager.getInstance(sdkMgr.getLocalSdk(), sdkLog);
    return avdMgr;
  }
}
