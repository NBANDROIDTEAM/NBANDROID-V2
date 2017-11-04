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
package org.nbandroid.netbeans.gradle.config;

public final class AndroidProjectProperties {

    /**
     * Property used find keystore file during release build.
     */
    public static final String PROP_KEY_STORE = "key.store";
    /**
     * Property used identify key alias in keystore file during release build.
     */
    public static final String PROP_KEY_ALIAS = "key.alias";
    /**
     * Property holding keystore file access password.
     */
    public static final String PROP_KEY_STORE_PASSWD = "key.store.password";
    /**
     * Property holding password for an alias in keystore file.
     */
    public static final String PROP_KEY_ALIAS_PASSWD = "key.alias.password";
    /**
     * Location of output file when building signed package.
     */
    public static final String PROP_RELEASE_FILE = "out.release.file";

//    public static final String SCREEN_SIZE = "screen.skin";       //NOI18N
    public static final String PROP_EMULATOR_OPTIONS = "emulator.options";   //NOI18N
    /**
     * Launch action property: {@code main}, {@code test} or activity name or
     * {@code none}
     */

    /**
     * '|'-separated list of configuration names stored in AuxiliaryProperties.
     */
    public static final String PROP_CONFIG_NAMES = "config.names";
    /**
     * Property name for active configuration.
     */
    public static final String PROP_ACTIVE_CONFIG = "config.active"; // NOI18N
    /**
     * Common prefix for all launch configuration related properties.
     */
    public static final String PROP_LAUNCH_PREFFIX = "launch.";

    /**
     * Name of a launch configuration.
     */
    public static final String PROP_LAUNCH_CONFIG_NAME = "launch.name";
    /**
     * Launch action property: {@code main}, {@code test} or activity name or
     * {@code none}
     */
    public static final String PROP_LAUNCH_ACTION = "launch.action";
    public static final String LAUNCH_ACTION_MAIN = "main";
    public static final String LAUNCH_ACTION_DO_NOTHING = "none";
    public static final String LAUNCH_ACTION_TEST = "test";
    public static final String PROP_LAUNCH_ACTION_ACTIVITY = "launch.action.activity";
    /**
     * Name of property for build mode of a launch configuration
     * (debug/release).
     */
    public static final String PROP_LAUNCH_MODE = "launch.mode";

    /**
     * Name of property to control selection of target device.
     */
    public static final String PROP_LAUNCH_TARGET_MODE = "launch.target";

    /**
     * Test instrumentation runner class name.
     */
    public static final String PROP_INSTR_RUNNER = "test.instrumentation.runner";
    public static final String INSTR_RUNNER_DEFAULT = "android.test.InstrumentationTestRunner";

    /**
     * - Manual Mode Always display a UI that lets a user see the current
     * running emulators/devices. The UI must show which devices are
     * compatibles, and allow launching new emulators with compatible (and not
     * yet running) AVD. - Automatic Way * Preferred AVD set. If Preferred AVD
     * is not running: launch it. Launch the application on the preferred AVD. *
     * No preferred AVD. Count the number of compatible emulators/devices. If !=
     * 1, display a UI similar to manual mode. If == 1, launch the application
     * on this AVD/device.
     */
    public static final String PROP_TARGET_MODE = "android.target.mode";
    public static final String TARGET_MODE_AUTO = "auto";
    public static final String TARGET_MODE_MANUAL = "manual";

    public static final String PROP_TARGET_PREFFERED_AVD = "android.target.avd";
    /**
     * Holds a set of arguments to use for {@code adb} calls
     */
    public static final String PROP_TARGET_DEVICE_ARGS = "android.target.device";
}
