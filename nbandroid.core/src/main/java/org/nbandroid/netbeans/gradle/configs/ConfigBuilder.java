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

import com.google.common.base.Preconditions;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration.Action;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration.TargetMode;

/**
 * Builder pattern for configs.
 */
public final class ConfigBuilder {

    private static final Logger LOGGER = Logger.getLogger(ConfigBuilder.class.getName());

    public static ConfigBuilder builderForConfig(AndroidConfigProvider.Config cfg) {
        ConfigBuilder b = new ConfigBuilder();
        return b.withLaunchAction(cfg.getLaunchConfiguration().getLaunchAction())
                .withActivityName(cfg.getLaunchConfiguration().getActivityName())
                .withMode(cfg.getLaunchConfiguration().getMode())
                .withTargetMode(cfg.getLaunchConfiguration().getTargetMode())
                .withName(cfg.getDisplayName())
                .withTestRunner(cfg.getLaunchConfiguration().getInstrumentationRunner());
    }

    public static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    private String name;
    private LaunchConfigurationBean l;

    private ConfigBuilder() {
        l = new LaunchConfigurationBean();
    }

    public ConfigBuilder withLaunchAction(Action a) {
        l.setLaunchAction(a);
        return this;
    }

    public ConfigBuilder withActivityName(String activity) {
        l.setActivityName(activity);
        return this;
    }

    public ConfigBuilder withMode(String mode) {
        l.setMode(mode);
        return this;
    }

    public ConfigBuilder withTargetMode(TargetMode targetMode) {
        l.setTargetMode(targetMode);
        return this;
    }

    public ConfigBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ConfigBuilder withEmulatorOption(String emulatorOptions) {
        l.setEmulatorOptions(emulatorOptions);
        return this;
    }

    public ConfigBuilder withTestRunner(String testRunner) {
        l.setInstrumentationRunner(testRunner);
        return this;
    }

    public AndroidConfigProvider.Config config() {
        return new AndroidConfigProvider.Config(Preconditions.checkNotNull(name), l);
    }
}
