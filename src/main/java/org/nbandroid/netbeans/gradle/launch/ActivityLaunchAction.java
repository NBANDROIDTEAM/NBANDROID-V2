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

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ide.common.xml.ManifestData.Activity;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.AndroidIO;
import org.nbandroid.netbeans.gradle.launch.AndroidLauncherImpl.AMReceiver;
import org.netbeans.api.project.Project;
import org.openide.windows.InputOutput;

/**
 * Launch strategy to start an activity.
 *
 * @author radim
 */
class ActivityLaunchAction implements LaunchAction {

    private static final Logger LOG = Logger.getLogger(ActivityLaunchAction.class.getName());

    @Override
    public boolean doLaunch(LaunchInfo launchInfo, IDevice device, Project project) {
        InputOutput io = AndroidIO.getDefaultIO();

        String activity = null;
        if (launchInfo.launchConfig.getLaunchAction() == LaunchConfiguration.Action.ACTIVITY) {
            activity = launchInfo.launchConfig.getActivityName();
        } else {
            Activity launchActivity = launchInfo.manifestData.getLauncherActivity();
            if (launchActivity != null) {
                activity = launchActivity.getName();
            }
        }
        if (activity == null) {
            io.getOut().println("Launch activity not found.");
            return false;
        }
        String packageName = launchInfo.manifestData.getPackage();
        String command = "am start"
                + (launchInfo.debug ? " -D " : "")
                + " -n " + packageName + "/" + activity.replaceAll("\\$", "\\\\\\$")
                + " -a android.intent.action.MAIN"
                + " -c android.intent.category.LAUNCHER";
        try {
            // now we actually launch the app.
            LOG.log(Level.FINE, command);
            io.getOut().println("Starting activity " + activity + " on device " + device);

            device.executeShellCommand(command, new AMReceiver(launchInfo, device));
        } catch (TimeoutException ex) {
            io.getErr().println("Launch error: timeout");
            LOG.log(Level.INFO, null, ex);
        } catch (AdbCommandRejectedException ex) {
            io.getErr().println("Launch error: adb rejected command: " + ex.getMessage());
            LOG.log(Level.INFO, null, ex);
        } catch (ShellCommandUnresponsiveException ex) {
            io.getErr().println(MessageFormat.format("Unresponsive shell when executing {0} on {1}",
                    command, device));
            LOG.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            io.getErr().println("Launch error: " + ex.getMessage());
            LOG.log(Level.INFO, null, ex);
        } catch (Throwable t) {
            LOG.log(Level.WARNING, null, t);
            Throwables.propagate(t);
        }
        return true;
    }
}
