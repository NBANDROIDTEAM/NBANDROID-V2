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
package org.nbandroid.netbeans.gradle.core.ui;

import com.android.SdkConstants;
import com.google.common.base.Strings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.options.OptionsDisplayer;
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatformManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

public final class SdkManagerAction implements ActionListener {

  @Override
  public void actionPerformed(ActionEvent e) {
    String sdkLocation = DalvikPlatformManager.getDefault().getSdkLocation();
    if (Strings.isNullOrEmpty(sdkLocation)) {
      NotifyDescriptor notifyDescriptor = 
          new NotifyDescriptor("Android SDK location has to be set before you can start Android SDK Manager. "
                + "Do you want to set it now?", 
              "Android SDK Manager", 
              NotifyDescriptor.OK_CANCEL_OPTION, 
              NotifyDescriptor.QUESTION_MESSAGE, 
              new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION}, 
              NotifyDescriptor.OK_OPTION);
      if (DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION) {
        OptionsDisplayer.getDefault().open(
            "Advanced/org-netbeans-modules-android-core-ui-AndroidPlatformAdvancedOption");
      }
    } else {
      runSdkManager(sdkLocation);
    }
  }

  private void runSdkManager(String sdkLocation) {
    try {
      /*Process process = */new ExternalProcessBuilder(
          sdkLocation + File.separatorChar + SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.androidCmdName())
          .call();
      // TODO do I need to read stderr/out?
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
  }
}
