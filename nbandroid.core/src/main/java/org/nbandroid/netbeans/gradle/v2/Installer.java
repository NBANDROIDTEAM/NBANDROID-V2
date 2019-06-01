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
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import static org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        NbOptionalDependencySpiLoader.installServiceProvider("com.junichi11.netbeans.modules.color.codes.preview.spi.ColorCodesProvider","com.junichi11.netbeans.modules.color.codes.preview", "org.nbandroid.netbeans.gradle.v2.color.preview.AndroidColorCodesProvider", Installer.class);
        AndroidSdkProvider.getDefault(); //init SDK subsystem
        Runnable runnable = new Runnable() {
            public void run() {
                TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
                instance.putClientProperty(AUTO_OPEN_LOCAL_PROPERTY, Boolean.FALSE);
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
        RequestProcessor.getDefault().schedule(new Runnable() {
            @Override
            public void run() {
                double version = Double.parseDouble(System.getProperty("java.specification.version"));
                if (version < 10d) {
                    NotificationDisplayer.getDefault().notify("Unsupported Java Version", loadIcon(),
                            "Apache NetBeans is running Java " + version + " that is not supported by NBANDROID-V2. ", new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String text = "<html>";
                            text += "Apache NetBeans is running Java " + version + " that is not supported by NBANDROID-V2. <br/><br/>";
                            text += "Versions supported by NBANDROID-V2: <br/><br/>";
                            text += "Java 10.x <br/>";
                            text += "Java 11.x <br/>";
                            text += "Java 12.x <br/><br/><br/>";
                            text += "<b>Please update your Java version, otherwise NBANDROID will not work properly!</b><br/>";
                            text += "</html>";
                            NotifyDescriptor nd = new NotifyDescriptor.Message(text, NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(nd);
                        }
                    }, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.ERROR);
                }
            }

            private Icon loadIcon() {
                URL resource = MessageType.class.getResource("images/error.png");
                if (resource == null) {
                    return new ImageIcon();
                }
                return new ImageIcon(resource);
            }
        }, 20, TimeUnit.SECONDS);

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
