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
package org.nbandroid.netbeans.gradle.apk;

import com.android.ide.common.xml.ManifestData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.nbandroid.netbeans.gradle.avd.AvdSelector;
import org.nbandroid.netbeans.gradle.configs.ConfigBuilder;
import org.nbandroid.netbeans.gradle.launch.AndroidLauncher;
import org.nbandroid.netbeans.gradle.launch.AndroidLauncherImpl;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration.TargetMode;
import org.nbandroid.netbeans.gradle.launch.LaunchInfo;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

public final class InstallApkAction implements ActionListener {

    private static final RequestProcessor RP = new RequestProcessor("APK deployer", 1);
    private final Deployable context;

    public InstallApkAction(Deployable context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                Project owner = FileOwnerQuery.getOwner(context.getDeployableFile());
                if (owner != null) {
                    AndroidSdk sdk = owner.getLookup().lookup(AndroidSdk.class);
                    if (sdk != null) {
                        AndroidLauncher launcher = new AndroidLauncherImpl();
                        LaunchConfiguration cfg = ConfigBuilder.builder()
                                .withName("dummy").withTargetMode(TargetMode.AUTO).config().getLaunchConfiguration();
                        AvdSelector.LaunchData launchData = launcher.configAvd(
                                sdk.getAndroidSdkHandler(), null, cfg);
                        if (launchData == null || launchData.getDevice() == null) {
                            return;
                        }
                        ManifestData manifest = new ApkUtils().apkPackageName(FileUtil.toFile(context.getDeployableFile()));
                        launcher.simpleLaunch(new LaunchInfo(context.getDeployableFile(),
                                /*reinstall*/ true, /*debug*/ false, null, manifest), launchData.getDevice());
                    }
                }
            }
        });
    }
}
