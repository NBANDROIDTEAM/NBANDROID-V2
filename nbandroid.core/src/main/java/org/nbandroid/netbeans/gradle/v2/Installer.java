/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2;

import com.android.ddmlib.AndroidDebugBridge;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import static org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        NbOptionalDependencySpiLoader.installServiceProvider("com.junichi11.netbeans.modules.color.codes.preview.colors.model.ColorCodesProvider", "org.nbandroid.netbeans.gradle.v2.color.preview.AndroidColorCodesProvider", Installer.class);
        AndroidSdkProvider.getDefault(); //init SDK subsystem
        Runnable runnable = new Runnable() {
            public void run() {
                TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
                instance.putClientProperty(AUTO_OPEN_LOCAL_PROPERTY, Boolean.FALSE);
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
    }

    @Override
    public void close() {
        // TODO(radim): if we really need it then it belongs to core where ADBfactory lives
        AndroidDebugBridge adb = AndroidSdkProvider.getAdb();
        if (adb != null && adb.isConnected()) {
            AndroidDebugBridge.disconnectBridge();
        }
    }

}
