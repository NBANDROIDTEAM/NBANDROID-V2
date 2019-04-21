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
package org.nbandroid.netbeans.gradle.configs;

import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration;

/**
 * Customizable information about launch behavior.
 *
 * @author radim
 */
public class LaunchConfigurationBean implements LaunchConfiguration {

    private Action launchAction = Action.MAIN;
    /**
     * Name of launched activity (only important when {@link Action#ACTIVITY} is
     * used.
     */
    private String activityName;

    /**
     * Build mode: debug or release.
     */
    private String mode = MODE_DEBUG;

    private TargetMode targetMode = TargetMode.AUTO;

    private String emulatorOptions = "";

    private String instrumentationRunner;

    @Override
    public Action getLaunchAction() {
        return launchAction;
    }

    public void setLaunchAction(Action launchAction) {
        this.launchAction = launchAction;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String ActivityName) {
        this.activityName = ActivityName;
    }

    @Override
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public TargetMode getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(TargetMode targetMode) {
        this.targetMode = targetMode;
    }

    @Override
    public String getEmulatorOptions() {
        return emulatorOptions;
    }

    public void setEmulatorOptions(String emulatorOptions) {
        this.emulatorOptions = emulatorOptions;
    }

    @Override
    public String getInstrumentationRunner() {
        return instrumentationRunner;
    }

    public void setInstrumentationRunner(String instrumentationRunner) {
        this.instrumentationRunner = instrumentationRunner;
    }

    @Override
    public String toString() {
        return "LaunchConfigurationBean{" + "launchAction=" + launchAction
                + "mode=" + mode
                + (launchAction == Action.ACTIVITY ? ", activityName=" + activityName : "")
                + (instrumentationRunner != null ? (" testrunner=" + instrumentationRunner) : "")
                + "}";
    }
}
