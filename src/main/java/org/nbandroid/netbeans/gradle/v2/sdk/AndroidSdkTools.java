/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author arsi
 */
public class AndroidSdkTools {

    public static List<AndroidPlatformInfo> orderByApliLevel(List<AndroidPlatformInfo> tmpPlatformList) {
        Collections.sort(tmpPlatformList, new Comparator<AndroidPlatformInfo>() {
            @Override
            public int compare(AndroidPlatformInfo o1, AndroidPlatformInfo o2) {
                return Integer.compare(o2.getAndroidVersion().getApiLevel(), o1.getAndroidVersion().getApiLevel());
            }
        });
        return tmpPlatformList;
    }

    public static boolean isSdkFolder(File folder) {
        if (!folder.exists()) {
            return false;
        } else if (!folder.isDirectory()) {
            return false;
        } else if (folder.listFiles().length == 0) {
            return false;
        } else if (folder.listFiles().length > 0) {
            File sdkTools = new File(folder, "tools");
            if (!sdkTools.exists() || !sdkTools.isDirectory()) {
                return false;
            }
        }
        return true;
    }
}
