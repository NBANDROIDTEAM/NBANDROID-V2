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

import com.android.SdkConstants;
import com.android.ddmlib.IDevice;
import com.android.prefs.AndroidLocation;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.utils.NullLogger;
import com.google.common.base.Splitter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.progress.ProgressHandle;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.netbeans.modules.android.project.launch.AdbUtils;
import org.netbeans.modules.android.project.launch.actions.SelectDeviceAction;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 * Helper to launch Android emulator.
 *
 * @author radim
 */
public class EmulatorLauncher {

    private static final Logger LOG = Logger.getLogger(EmulatorLauncher.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /**
     * Name of property used to identify launched emulator.
     */
    private static final String LAUNCH_COOKIE = "nbandroid";

    // various emulator flags
    // TODO: customization for emulator launching: options or per project or per launch confing
    // TODO: at least custom args to add set of flags
    private static final String FLAG_AVD = "-avd";
    private static final String FLAG_PROP = "-prop";
    private static final String FLAG_NETDELAY = "-netdelay";
    private static final String FLAG_NETSPEED = "-netspeed";
    private static final String FLAG_WIPE_DATA = "-wipe-data";
    private static final String FLAG_NO_BOOT_ANIM = "-no-boot-anim";

    private final AndroidSdk sdk;
    private final Semaphore semaphore = new Semaphore(1, true);
    private volatile boolean cancel = false;

    public EmulatorLauncher(AndroidSdk sdk) {
        this.sdk = sdk;
    }

    public Future<IDevice> launchEmulator(AvdInfo avdInfo, LaunchConfiguration launchCfg) {
        final String cookie = Long.toString(System.currentTimeMillis());
        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .frontWindow(true).postExecution(new Runnable() {
            @Override
            public void run() {
                cancel = true;
                semaphore.release();
            }
        });

        ProcessBuilder processBuilder = ProcessBuilder.getLocal();
        processBuilder.setExecutable(getEmulatorBinaryPath());
        List<String> arguments = new ArrayList<>();
        arguments.add(FLAG_AVD);
        arguments.add(avdInfo.getName());
        arguments.add(FLAG_PROP);
        arguments.add(LAUNCH_COOKIE + "=" + cookie);
        if(SelectDeviceAction.wipeData.isSelected()){
            arguments.add(FLAG_WIPE_DATA);
        }
        for (String arg : Splitter.on(' ').omitEmptyStrings().split(launchCfg.getEmulatorOptions())) {
            arguments.add(arg);
        }
        processBuilder.setArguments(arguments);
        AndroidSdk defaultSdk = AndroidSdkProvider.getDefaultSdk();
        if (defaultSdk != null) {
            try {
                AvdManager avdManager = AvdManager.getInstance(defaultSdk.getAndroidSdkHandler(), new NullLogger());
                if (avdManager != null) {
                    File baseAvdFolder = avdManager.getBaseAvdFolder();
                    processBuilder.getEnvironment().setVariable("ANDROID_AVD_HOME", baseAvdFolder.getAbsolutePath());
                }
            } catch (AndroidLocation.AndroidLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        ExecutionService service = ExecutionService.newService(
                processBuilder, descriptor, "Android emulator");
        final Future<Integer> taskStatus = service.run();
        return EXECUTOR.submit(new Callable<IDevice>() {

            @Override
            public IDevice call() throws Exception {

                ProgressHandle handle = ProgressHandle.createHandle("Emulator starting", new Cancellable() {
                    @Override
                    public boolean cancel() {
                        cancel = true;
                        semaphore.release();
                        return true;
                    }
                });
                handle.start();
                while (true) {
                    Map<String, IDevice> runningEmulators = AdbUtils.getRunningEmulators();
                    IDevice device = runningEmulators.get(avdInfo.getName());
                    if (device != null && device.isOnline()) {
                        handle.finish();
                        LOG.log(Level.INFO, "Started emulator device connected.");
                        return device;
                    } else if (cancel) {
                        handle.finish();
                        LOG.log(Level.INFO, "Emulator start canceled");
                        return null;
                    }
                    semaphore.tryAcquire(2, TimeUnit.SECONDS);
                }
            }

        });
    }

    private String getEmulatorBinaryPath() {
        //fix #204 Since March 2017 (v25.3.0), the Android Emulator changed location from${ANDROID_SDK_ROOT}/tools/ to its own top-level directory, ${ANDROID_SDK_ROOT}/emulator/
        String newEmulatorPath = sdk.getSdkPath() + File.separator
                + SdkConstants.FD_EMULATOR + File.separator + SdkConstants.FN_EMULATOR;
        if (new File(newEmulatorPath).exists()) {
            return newEmulatorPath;
        }
        return sdk.getSdkPath() + File.separator
                + SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_EMULATOR;

    }
}
