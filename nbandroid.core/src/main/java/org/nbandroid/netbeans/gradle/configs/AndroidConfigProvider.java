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

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.config.AndroidProjectProperties;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration;
import org.nbandroid.netbeans.gradle.launch.LaunchConfiguration.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

public final class AndroidConfigProvider implements ProjectConfigurationProvider<AndroidConfigProvider.Config> {

    private static final Logger LOG = Logger.getLogger(AndroidConfigProvider.class.getName());

    public static final String DEFAULT_CONFIG_NAME
            = NbBundle.getMessage(AndroidConfigProvider.class, "LBL_DefaultConfig");
    private static final Config DEFAULT_CONFIG = new Config(
            DEFAULT_CONFIG_NAME, createLaunchConfig(Action.MAIN));

    private static final Comparator<Config> CONFIG_COMPARATOR = new Comparator<Config>() {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(Config config1, Config config2) {
            return collator.compare(config1.getDisplayName(), config2.getDisplayName());
        }
    };

    final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    final Map<String, Config> configs = new ConcurrentHashMap<String, Config>();
    /**
     * Key to configs maps to mark active configuration.
     */
    String activeConfigName;

    private final AuxiliaryProperties auxProps;

    public AndroidConfigProvider(AuxiliaryProperties auxProps) {
        this.auxProps = Preconditions.checkNotNull(auxProps);
        load();
    }

    @Override
    public Collection<Config> getConfigurations() {
        List<Config> configList = new ArrayList<Config>(configs.size());
        configList.addAll(configs.values());
        Collections.sort(configList, CONFIG_COMPARATOR);
        return configList;
    }

    @Override
    public Config getActiveConfiguration() {
        if (configs.containsKey(activeConfigName)) {
            return configs.get(activeConfigName);
        }
        return DEFAULT_CONFIG;
    }

    @Override
    public void setActiveConfiguration(Config config) throws IOException {
        if (config != DEFAULT_CONFIG && !configs.values().contains(config)) {
            throw new IllegalArgumentException("Unknown config: " + config);
        }
        activeConfigName = config.getDisplayName();
        auxProps.put(AndroidProjectProperties.PROP_ACTIVE_CONFIG, config.name, false);
        propertyChangeSupport.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        // TODO need to make sure it is saved
        //   ProjectManager.getDefault().saveProject(project);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        //  project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(AndroidCompositePanelProvider.RUN);
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return command.equals(ActionProvider.COMMAND_RUN)
                || command.equals(ActionProvider.COMMAND_DEBUG);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    }

    Set<String> getConfigNames() {
        return new HashSet<String>(configs.keySet());
    }

    private void load() {
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                configs.clear();
                String propNames = auxProps.get(AndroidProjectProperties.PROP_CONFIG_NAMES, false);
                if (propNames != null) {
                    for (String key : Splitter.on('|').split(propNames)) {
                        Config cfg = configForKey(key);
                        if (cfg != null) {
                            configs.put(cfg.getDisplayName(), cfg);
                        }
                    }
                }
                String active = auxProps.get(AndroidProjectProperties.PROP_ACTIVE_CONFIG, false);
                fixConfigurations(active);
                return null;
            }
        });
    }

    public void setConfigurations(Collection<Config> configurations) {
        configs.clear();
        for (Config cfg : configurations) {
            configs.put(cfg.getDisplayName(), cfg);
        }
        fixConfigurations(activeConfigName);
        propertyChangeSupport.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
        propertyChangeSupport.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
    }

    private void fixConfigurations(String activeConfig) {
        if (configs.isEmpty()) {
            configs.put(DEFAULT_CONFIG.getDisplayName(), DEFAULT_CONFIG);
        }
        if (activeConfig != null && configs.containsKey(activeConfig)) {
            activeConfigName = activeConfig;
        } else {
            activeConfigName = Iterables.get(configs.keySet(), 0);
        }
    }

    public void save() {
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    for (String key : auxProps.listKeys(false)) {
                        if (key.startsWith(AndroidProjectProperties.PROP_LAUNCH_PREFFIX)) {
                            int lastDot = key.lastIndexOf('.');
                            if (!configs.containsKey(key.substring(lastDot + 1))) {
                                auxProps.put(key, null, false);
                            }
                        }
                    }
                    auxProps.put(AndroidProjectProperties.PROP_CONFIG_NAMES, Joiner.on('|').join(getConfigNames()), false);
                    for (String name : getConfigNames()) {
                        Config cfg = configs.get(name);
                        saveConfig(auxProps, cfg.getLaunchConfiguration(), "." + name);
                    }
                    return null;
                }

            });
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        }
    }

    //~ Inner classes
    public static final class Config implements ProjectConfiguration {

        /**
         * Human readable name of configuration.
         */
        private final String name;
        private final LaunchConfiguration launch;

        Config(String displayName, LaunchConfiguration launch) {
            this.name = displayName;
            this.launch = launch;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        public LaunchConfiguration getLaunchConfiguration() {
            return launch;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Config other = (Config) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("name", name).add("launch", launch).toString();
        }

    }

    public Config configForKey(String key) {
        String suffix = "." + Preconditions.checkNotNull(key);
        String launchAction = auxProps.get(AndroidProjectProperties.PROP_LAUNCH_ACTION + suffix, false);
        if (launchAction == null) {
            return new Config(key, configWithModes(auxProps, key, new LaunchConfigurationBean()));
        }
        if (AndroidProjectProperties.LAUNCH_ACTION_DO_NOTHING.equals(launchAction)) {
            return new Config(
                    key, configWithModes(auxProps, key, createLaunchConfig(LaunchConfiguration.Action.DO_NOTHING)));
        } else if (AndroidProjectProperties.LAUNCH_ACTION_MAIN.equals(launchAction)) {
            return new Config(
                    key, configWithModes(auxProps, key, new LaunchConfigurationBean()));
        }
        LaunchConfigurationBean l = createLaunchConfig(LaunchConfiguration.Action.ACTIVITY);
        l.setActivityName(launchAction);
        return new Config(key, configWithModes(auxProps, key, l));
    }

    private static LaunchConfiguration configWithModes(
            AuxiliaryProperties props, String key, LaunchConfigurationBean c) {
        String suffix = key == null ? "" : "." + key;
        String mode = props.get(AndroidProjectProperties.PROP_LAUNCH_MODE + suffix, false);
        if (mode != null) {
            c.setMode(mode);
        }
        String target = props.get(AndroidProjectProperties.PROP_LAUNCH_TARGET_MODE + suffix, false);
        if (target != null) {
            c.setTargetMode(LaunchConfiguration.TargetMode.valueOf(target));
        }
        String emulatorOptions = props.get(AndroidProjectProperties.PROP_EMULATOR_OPTIONS + suffix, false);
        if (emulatorOptions != null) {
            c.setEmulatorOptions(emulatorOptions);
        }
        c.setInstrumentationRunner(props.get(AndroidProjectProperties.PROP_INSTR_RUNNER + suffix, false));
        return c;
    }

    private static void saveConfig(AuxiliaryProperties props, LaunchConfiguration l, String suffix) {
        Preconditions.checkNotNull(props);
        switch (l.getLaunchAction()) {
            case DO_NOTHING:
                props.put(AndroidProjectProperties.PROP_LAUNCH_ACTION + suffix,
                        AndroidProjectProperties.LAUNCH_ACTION_DO_NOTHING, false);
                break;
            case MAIN:
                props.put(AndroidProjectProperties.PROP_LAUNCH_ACTION + suffix,
                        AndroidProjectProperties.LAUNCH_ACTION_MAIN, false);
                break;
            case ACTIVITY:
                props.put(AndroidProjectProperties.PROP_LAUNCH_ACTION + suffix,
                        l.getActivityName(), false);
                break;
        }
        if (!LaunchConfiguration.MODE_DEBUG.equals(l.getMode())) {
            props.put(AndroidProjectProperties.PROP_LAUNCH_MODE + suffix, l.getMode(), false);
        }
        props.put(AndroidProjectProperties.PROP_LAUNCH_TARGET_MODE + suffix, l.getTargetMode().toString(), false);
        props.put(AndroidProjectProperties.PROP_EMULATOR_OPTIONS + suffix, l.getEmulatorOptions(), false);
        props.put(AndroidProjectProperties.PROP_INSTR_RUNNER + suffix, l.getInstrumentationRunner(), false);
    }

    private static LaunchConfigurationBean createLaunchConfig(
            LaunchConfiguration.Action a) {
        LaunchConfigurationBean config = new LaunchConfigurationBean();
        config.setLaunchAction(a);
        return config;
    }
}
