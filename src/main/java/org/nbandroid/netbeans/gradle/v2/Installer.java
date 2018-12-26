/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2;

import com.android.ddmlib.AndroidDebugBridge;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.nbandroid.netbeans.gradle.symlink.GradleSymLinkRemover;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import static org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(1);


    @Override
    public void restored() {

        AndroidSdkProvider.getDefault(); //init SDK subsystem
        Runnable runnable = new Runnable() {
            public void run() {
                TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
                instance.putClientProperty(AUTO_OPEN_LOCAL_PROPERTY, Boolean.FALSE);
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
        POOL.scheduleWithFixedDelay(new GradleSymLinkRemover(), 30, 60, TimeUnit.SECONDS);
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
