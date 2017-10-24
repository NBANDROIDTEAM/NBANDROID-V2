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

package org.netbeans.modules.android.project.ui.customizer;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider.Config;

/**
 * A wrapper around a collection of configurations
 */
public final class ConfigGroup {
  private static final Logger LOGGER = Logger.getLogger(ConfigGroup.class.getName());
  private static Predicate<Config> hasSimilarName(final String name) {
    return new Predicate<Config>(){

        @Override
        public boolean apply(Config input) {
          final String configName = name.replaceAll("[^a-zA-Z0-9_-]", "_"); // NOI18N
          final String comparedConfigName = input.getDisplayName().replaceAll("[^a-zA-Z0-9_-]", "_"); // NOI18N
          return comparedConfigName.equals(configName);
        }
      };
  }

  private final ArrayList<Config> configs;
  private Config currentConfig;

  public ConfigGroup(Collection<Config> configs, Config currentConfig) {
    this.configs = Lists.newArrayList(Preconditions.checkNotNull(configs));
    this.currentConfig = Preconditions.checkNotNull(currentConfig);
  }

  public Collection<Config> getConfigs() {
    return configs;
  }

  public Config getCurrentConfig() {
    return currentConfig;
  }

  public void addConfig(Config cfg) {
    if (Iterables.any(configs, hasSimilarName(cfg.getDisplayName()))) {
      throw new IllegalArgumentException("Cannot add config with this name");
    }

    configs.add(cfg);
  }

  public void setCurrentConfig(Config config) {
    currentConfig = Iterables.find(configs, hasSimilarName(config.getDisplayName()));
  }

  public void updateCurrentConfig(Config config) {
    int idx = Iterables.indexOf(configs, Predicates.equalTo(currentConfig));
    if (idx >= 0) {
      configs.set(idx, config);
    }
  }
}
