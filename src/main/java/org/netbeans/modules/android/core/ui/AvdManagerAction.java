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
package org.netbeans.modules.android.core.ui;

import com.android.SdkConstants;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

@ActionID(id = "org.netbeans.modules.android.core.ui.AvdManagerAction", category = "Tools")
@ActionRegistration(iconInMenu = true, displayName = "AVD manager")
public final class AvdManagerAction implements ActionListener {
  
  private static final RequestProcessor RP = new RequestProcessor("AvdManager", 1);

  @Override
  public void actionPerformed(ActionEvent e) {
    final String sdkLocation = DalvikPlatformManager.getDefault().getSdkLocation();
    if (Strings.isNullOrEmpty(sdkLocation)) {
      NotifyDescriptor notifyDescriptor = 
          new NotifyDescriptor("Android SDK location has to be set before you can start AVD Manager. "
                + "Do you want to set it now?", 
              "AVD Manager", 
              NotifyDescriptor.OK_CANCEL_OPTION, 
              NotifyDescriptor.QUESTION_MESSAGE, 
              new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION}, 
              NotifyDescriptor.OK_OPTION);
      if (DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION) {
        OptionsDisplayer.getDefault().open(
            "Advanced/org-netbeans-modules-android-core-ui-AndroidPlatformAdvancedOption");
      }
    } else {
      RP.post(new Runnable() {

        @Override
        public void run() {
          runAvdManager(sdkLocation);
        }
      });
    }
  }

  private void runAvdManager(String sdkLocation) {
    try {
      final Process process = new ExternalProcessBuilder(
          sdkLocation + File.separatorChar + SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.androidCmdName())
          .addArgument("avd").call();
      Executors.newCachedThreadPool().submit(new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          ByteStreams.copy(process.getInputStream(), System.out);
          return null;
        }
      });
      Executors.newCachedThreadPool().submit(new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          ByteStreams.copy(process.getErrorStream(), System.err);
          return null;
        }
      });
      process.waitFor();
    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }
  }
}
