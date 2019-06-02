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
package org.netbeans.modules.android.project.properties.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.NbAndroidProjectConfiguration;
import org.netbeans.modules.android.project.api.NbAndroidProjectConfigurationProvider;
import org.openide.util.actions.Presenter;

/**
 *
 * @author arsi
 */
public class ConfigurationsProjectAction extends AbstractAction implements Presenter.Menu, Presenter.Popup{
    private final JMenu menu = new JMenu("Configuration");
    private final Project project;
    ButtonGroup group = new ButtonGroup();

    public ConfigurationsProjectAction(Project project) {
        this.project = project;
    }
    
    
    @Override
    public JMenuItem getMenuPresenter() {
        refresh();
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        refresh();
        return menu;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private void refresh() {
        menu.removeAll();
        NbAndroidProjectConfigurationProvider configurationProvider = project.getLookup().lookup(NbAndroidProjectConfigurationProvider.class);
        Collection<NbAndroidProjectConfiguration> configurations = configurationProvider.getConfigurations();
        for (Iterator<NbAndroidProjectConfiguration> iterator = configurations.iterator(); iterator.hasNext();) {
            NbAndroidProjectConfiguration next = iterator.next();
            JRadioButtonMenuItemWithProfile withProfile =  new JRadioButtonMenuItemWithProfile(next,configurationProvider);
            menu.add(withProfile);
            group.add(withProfile);
        }
        Component[] components = menu.getPopupMenu().getComponents();
        for (Component component : components) {
            if(component instanceof JRadioButtonMenuItemWithProfile){
                if(configurationProvider.getActiveConfiguration().equals(((JRadioButtonMenuItemWithProfile) component).getConfiguration())){
                    ((JRadioButtonMenuItemWithProfile) component).setSelected(true);
                }
            }
        }
    }
    
    private class JRadioButtonMenuItemWithProfile extends JRadioButtonMenuItem implements ActionListener {
        
        private final NbAndroidProjectConfiguration configuration;
        private final NbAndroidProjectConfigurationProvider configurationProvider;

        public JRadioButtonMenuItemWithProfile(NbAndroidProjectConfiguration configuration,NbAndroidProjectConfigurationProvider configurationProvider) {
            this.configuration = configuration;
            this.configurationProvider=configurationProvider;
            setText(configuration.getDisplayName());
            addActionListener(this);
        }

        public NbAndroidProjectConfiguration getConfiguration() {
            return configuration;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                configurationProvider.setActiveConfiguration(configuration);
            } catch (IllegalArgumentException ex) {
            } catch (IOException ex) {
            }
        }
        
    }
}
