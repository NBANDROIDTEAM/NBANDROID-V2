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

import com.android.ide.common.xml.ManifestData;
import org.openide.filesystems.FileObject;

/**
 * Container to hold data needed for application launching.
 *
 * @author radim
 */
public class LaunchInfo {
    // TODO(radim) add install retry mode, ...

    public final FileObject packageFile;
    public final boolean reinstall;
    public final boolean debug;
    public final LaunchConfiguration launchConfig;
    public final ManifestData manifestData;
    public final String clientName;
    public final String testClass;
    public final String testMethod;

    public LaunchInfo(FileObject packageFile, boolean reinstall, boolean debug,
            LaunchConfiguration launchConfig, ManifestData manifestData) {
        this(packageFile, reinstall, debug, launchConfig, manifestData, null, null, null);
    }

    private LaunchInfo(FileObject packageFile, boolean reinstall, boolean debug,
            LaunchConfiguration launchConfig, ManifestData manifestData,
            String clientName, String testClass, String testMethod) {
        this.packageFile = packageFile;
        this.reinstall = reinstall;
        this.debug = debug;
        this.launchConfig = launchConfig;
        this.manifestData = manifestData;
        this.clientName = clientName == null ? manifestData.getPackage() : clientName;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    public LaunchInfo withPackageFile(FileObject packageFile) {
        return new LaunchInfo(
                packageFile, reinstall, debug, launchConfig, manifestData, clientName, testClass, testMethod);
    }

    /**
     * Creates an instance with modified client name used for attaching to a
     * process.
     */
    public LaunchInfo withClientName(String clientName) {
        return new LaunchInfo(
                packageFile, reinstall, debug, launchConfig, manifestData, clientName, testClass, testMethod);
    }

    public LaunchInfo withTestClass(String testClass) {
        return new LaunchInfo(
                packageFile, reinstall, debug, launchConfig, manifestData, clientName, testClass, testMethod);
    }
}
