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

import javax.annotation.Nullable;

/**
 * Description what to do when the project is launched.
 *
 * @author radim
 */
public interface LaunchConfiguration {

    public static final String MODE_DEBUG = "debug";
    public static final String MODE_RELEASE = "release";

    public enum Action {
        MAIN,
        ACTIVITY,
        DO_NOTHING
    }

    /**
     * Mode used to select target device for launch.
     */
    public enum TargetMode {
        AUTO,
        MANUAL;
    }

    @Deprecated
    Action getLaunchAction();

    /**
     * A class name of executed activity or {@code null} if not applicable.
     */
    @Deprecated
    @Nullable
    String getActivityName();

    // TODO extract as this does not belong to project config.
    /**
     * Debug or release mode.
     */
    String getMode();

    TargetMode getTargetMode();

    String getEmulatorOptions();

    @Nullable
    String getInstrumentationRunner();
}
