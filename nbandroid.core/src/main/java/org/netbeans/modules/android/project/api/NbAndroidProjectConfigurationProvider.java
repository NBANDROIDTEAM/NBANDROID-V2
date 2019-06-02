/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.project.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectConfigurationProvider;

/**
 *
 * @author arsi
 */
public class NbAndroidProjectConfigurationProvider implements ProjectConfigurationProvider<NbAndroidProjectConfiguration> {

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private NbAndroidProjectConfiguration activeConfiguration;
    private final NbAndroidProjectConfiguration DEFAULT_CONFIGURATION = new NbAndroidProjectConfiguration(NbAndroidProjectConfiguration.DEFAULT_TEXT, this);
    private final AuxiliaryProperties auxiliaryProperties;
    public static final String CONFIGURATION_CURRENT_PROFILE = "CURRENT_PROFILE";
    public static final String CONFIGURATION_PROFILES = "CONFIGURATION_PROFILES";
    private final List<NbAndroidProjectConfiguration> profiles = new ArrayList<>();

    public static final String getSafeProfileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\_\\-]", "_");
    }

    public NbAndroidProjectConfigurationProvider(AuxiliaryProperties auxiliaryProperties) {
        this.auxiliaryProperties = auxiliaryProperties;
        String current = auxiliaryProperties.get(CONFIGURATION_CURRENT_PROFILE, false);
        String all = auxiliaryProperties.get(CONFIGURATION_PROFILES, false);
        if (all != null) {
            StringTokenizer tok = new StringTokenizer(all, ";", false);
            while (tok.hasMoreElements()) {
                profiles.add(new NbAndroidProjectConfiguration(tok.nextToken(), this));
            }
        }
        if (!profiles.contains(DEFAULT_CONFIGURATION)) {
            profiles.add(DEFAULT_CONFIGURATION);
        }
        if (current != null) {
            NbAndroidProjectConfiguration selected = new NbAndroidProjectConfiguration(current, this);
            if (profiles.contains(selected)) {
                activeConfiguration = selected;
            } else {
                activeConfiguration = DEFAULT_CONFIGURATION;
            }
        } else {
            activeConfiguration = DEFAULT_CONFIGURATION;
        }
    }

    @Override
    public Collection<NbAndroidProjectConfiguration> getConfigurations() {

        return profiles;
    }

    public void addConfiguration(NbAndroidProjectConfiguration configuration) {
        profiles.add(configuration);
        save();
        firePropertyChange();
    }

    public void removeConfiguration(NbAndroidProjectConfiguration configuration) {
        profiles.remove(configuration);
        save();
        firePropertyChange();
        if (activeConfiguration.equals(configuration)) {
            try {
                setActiveConfiguration(DEFAULT_CONFIGURATION);
            } catch (IllegalArgumentException ex) {
            } catch (IOException ex) {
            }
        }

    }

    private void save() {
        String tmp = "";
        Iterator<NbAndroidProjectConfiguration> iterator = profiles.iterator();
        while (iterator.hasNext()) {
            NbAndroidProjectConfiguration next = iterator.next();
            tmp += next.getDisplayName();
            if (iterator.hasNext()) {
                tmp += ";";
            }
        }
        auxiliaryProperties.put(CONFIGURATION_PROFILES, tmp, false);
    }

    @Override
    public NbAndroidProjectConfiguration getActiveConfiguration() {
        return activeConfiguration;
    }

    @Override
    public void setActiveConfiguration(NbAndroidProjectConfiguration c) throws IllegalArgumentException, IOException {
        NbAndroidProjectConfiguration prev = activeConfiguration;
        activeConfiguration = c;
        auxiliaryProperties.put(CONFIGURATION_CURRENT_PROFILE, activeConfiguration.getDisplayName(), false);
        changeSupport.firePropertyChange(PROP_CONFIGURATION_ACTIVE, prev, c);
    }

    private void firePropertyChange() {
        changeSupport.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
    }

    @Override
    public boolean hasCustomizer() {
        return false;
    }

    @Override
    public void customize() {
    }

    @Override
    public boolean configurationsAffectAction(String string) {
        return false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        changeSupport.addPropertyChangeListener(pl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        changeSupport.removePropertyChangeListener(pl);
    }

}
