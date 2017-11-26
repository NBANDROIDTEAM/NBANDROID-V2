/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2;

import com.android.ddmlib.AndroidDebugBridge;
import org.nbandroid.netbeans.gradle.core.ddm.AndroidDebugBridgeFactory;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkPlatformProvider;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        AndroidSdkPlatformProvider.getDefault(); //init SDK subsystem
    }

    @Override
    public void close() {
        // TODO(radim): if we really need it then it belongs to core where ADBfactory lives
        AndroidDebugBridge adb = AndroidDebugBridgeFactory.getDefault();
        if (adb != null && adb.isConnected()) {
            AndroidDebugBridge.disconnectBridge();
        }
    }


}
