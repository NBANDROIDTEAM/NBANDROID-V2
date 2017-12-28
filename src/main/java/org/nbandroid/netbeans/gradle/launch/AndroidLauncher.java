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
package org.nbandroid.netbeans.gradle.launch;

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.repository.AndroidSdkHandler;
import java.util.concurrent.Future;
import org.nbandroid.netbeans.gradle.avd.AvdSelector;
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatform;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.openide.util.Lookup;

/**
 * Support for android application deployment and running or debugging on device
 * or an emulator.
 *
 * Cf. related functionality in Eclipse plugins located in
 * {@code sdk/eclipse/plugins/com.android.ide.eclipse.adt/src/com/android/ide/eclipse/adt/internal/launch},
 * namely {@code LaunchConfigDelegate} and {@code AndroidLaunchController}.
 *
 * @author radim
 */
public interface AndroidLauncher {

    /**
     * Deploys an application on AVD (emulator or real device) and optionally
     * runs main or a selected activity.
     *
     * @param platform
     * @param context must contain LaunchInfo, LaunchAction and Project.
     * optionally contains {@code AvdSelector.LaunchData},
     * {@code LaunchConfiguration.TargetMode}
     * @param mode run/debug
     * @return the {@link Future} reference to {@link Client} or null when
     * execution fails. The returned future's get method returns a
     * {@link Client} when activity is started. When no activity is started it
     * returns null.
     */
    Future<Client> launch(DalvikPlatform platform, Lookup context, String mode);

    AvdSelector.LaunchData configAvd(
            AndroidSdkHandler sdkManager, AndroidSdk sdk, IAndroidTarget target, LaunchConfiguration launchCfg);

    /**
     * Simple launch that can be used to launch a file from filesystem (APK).
     */
    boolean simpleLaunch(LaunchInfo launchInfo, IDevice device);
}
