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
import org.nbandroid.netbeans.gradle.launch.AndroidLauncherImpl.AMReceiver;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.launch.ActivitySelectorPanel;
import org.netbeans.modules.android.spi.MainActivityConfiguration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 * Launch strategy to start an activity.
 *
 * @author radim
 */
class ActivityLaunchAction implements LaunchAction {

    private static final Logger LOG = Logger.getLogger(ActivityLaunchAction.class.getName());
    private InputOutput io;

    @Override
    public boolean doLaunch(LaunchInfo launchInfo, IDevice device, Project project, MainActivityConfiguration mainActivityConfiguration) {
        io = project.getLookup().lookup(InputOutput.class);
        io.getOut().println("============================ RUN ===================================\r\n");

        String activity = null;
        if (mainActivityConfiguration.isUseFromManifest()) {
            Activity launchActivity = launchInfo.manifestData.getLauncherActivity();
            if (launchActivity != null && launchActivity.getName() != null) {
                activity = launchActivity.getName();
            } else {
                boolean root = true;
                try {
                    root = device.isRoot();
                } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException ex) {
                    Exceptions.printStackTrace(ex);
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(new ActivitySelectorPanel(mainActivityConfiguration, project, root), NotifyDescriptor.QUESTION_MESSAGE);
                Object notify = DialogDisplayer.getDefault().notify(nd);
                if (NotifyDescriptor.OK_OPTION.equals(notify)) {
                    activity = mainActivityConfiguration.getMainActivity();
                    if (activity == null) {
                        io.getOut().println("Launch activity not found. Nothing to run.\r\n");
                        return false;
                    }
                } else {
                    io.getOut().println("Canceled by user.\r\n");
                    return false;
                }
            }
        } else if (!mainActivityConfiguration.isAskBeforeLaunch()) {
            activity = mainActivityConfiguration.getMainActivity();
            if (activity == null) {
                io.getOut().println("Launch activity not found. Nothing to run.\r\n");
                return false;
            }
        } else {
            boolean root = true;
            try {
                root = device.isRoot();
            } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException ex) {
                Exceptions.printStackTrace(ex);
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message(new ActivitySelectorPanel(mainActivityConfiguration, project, root), NotifyDescriptor.QUESTION_MESSAGE);
            Object notify = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.OK_OPTION.equals(notify)) {
                activity = mainActivityConfiguration.getMainActivity();
                if (activity == null) {
                    io.getOut().println("Launch activity not found. Nothing to run.\r\n");
                    return false;
                }
            } else {
                io.getOut().println("Canceled by user.\r\n");
                return false;
            }
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
            io.getOut().println("Starting activity " + activity + " on device " + device + "\r\n");

            device.executeShellCommand(command, new AMReceiver(launchInfo, device, io));
        } catch (TimeoutException ex) {
            io.getErr().println("Launch error: timeout\r\n");
            LOG.log(Level.INFO, null, ex);
        } catch (AdbCommandRejectedException ex) {
            io.getErr().println("Launch error: adb rejected command: " + ex.getMessage() + "\r\n");
            LOG.log(Level.INFO, null, ex);
        } catch (ShellCommandUnresponsiveException ex) {
            io.getErr().println(MessageFormat.format("Unresponsive shell when executing {0} on {1}\r\n",
                    command, device));
            LOG.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            io.getErr().println("Launch error: " + ex.getMessage() + "\r\n");
            LOG.log(Level.INFO, null, ex);
        } catch (Throwable t) {
            LOG.log(Level.WARNING, null, t);
            Throwables.propagate(t);
        }
        return true;
    }
}
