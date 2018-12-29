/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.sdk.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author ArSi
 */
public class SDKWizardPanelMode implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    private final List<ChangeListener> listeners = new ArrayList<>();
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SDKVisualPanelMode component;
    private Mode mode = Mode.INIT;

    public enum Mode {
        LOCAL,
        DOWNLOAD,
        INIT
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public SDKVisualPanelMode getComponent() {
        if (component == null) {
            component = new SDKVisualPanelMode();
            component.addPropertyChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return getComponent().isDownloadSelected() || getComponent().isLocalSelected();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        // use wiz.getProperty to retrieve previous panel state
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SDKVisualPanelMode.TYPE_CHANGE.equals(evt.getPropertyName())) {
            if (component.isDownloadSelected()) {
                component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[]{"Select mode", "Download SDK", "Install SDK"});
                mode = Mode.DOWNLOAD;
            } else if (component.isLocalSelected()) {
                mode = Mode.LOCAL;
                component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[]{"Select mode", "Select SDK location"});
            } else {
                mode = Mode.INIT;
                component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[]{"Select mode"});
            }
            for (ChangeListener listener : listeners) {
                listener.stateChanged(new ChangeEvent(this));
            }
        }
    }

}
