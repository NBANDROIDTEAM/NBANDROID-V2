/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.repository.api.ProgressIndicatorAdapter;
import com.android.repository.api.RepoManager;
import com.android.sdklib.repository.AndroidSdkHandler;
import com.android.sdklib.repository.targets.AndroidTargetManager;
import com.android.sdklib.repository.targets.SystemImageManager;
import java.io.File;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = SdkManager.class)
public class SdkManagerImpl implements SdkManager {

    public SdkManagerImpl() {
        AndroidSdkHandler sdkHandler = AndroidSdkHandler.getInstance(new File("/jetty/android-studio-sdk"));
        RepoManager sdkManager = sdkHandler.getSdkManager(new ProgressIndicatorAdapter() {
        });
        AndroidTargetManager androidTargetManager = sdkHandler.getAndroidTargetManager(new ProgressIndicatorAdapter() {
        });
        SystemImageManager systemImageManager = sdkHandler.getSystemImageManager(new ProgressIndicatorAdapter() {
        });
    }

}
