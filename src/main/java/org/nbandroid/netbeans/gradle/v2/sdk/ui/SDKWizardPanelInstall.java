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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static org.nbandroid.netbeans.gradle.v2.sdk.ui.SDKWizardPanelDownload.PLATFORM_TOOLS;
import static org.nbandroid.netbeans.gradle.v2.sdk.ui.SDKWizardPanelDownload.SDK_TOOLS;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author ArSi
 */
public class SDKWizardPanelInstall implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SDKVisualPanelInstall component;
    public static final String LAST_PATH = "LAST_PATH";
    private final List<ChangeListener> listeners = new ArrayList<>();
    public static final String SDK_PATH = "SDK_PATH";
    public static final String SDK_NAME = "SDK_NAME";

    @Override
    public SDKVisualPanelInstall getComponent() {
        if (component == null) {
            component = new SDKVisualPanelInstall();
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
        return getComponent().isSdkInstalled();
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
        File platformTools = (File) wiz.getProperty(PLATFORM_TOOLS);
        File tools = (File) wiz.getProperty(SDK_TOOLS);
        getComponent().setZipFiles(tools, platformTools);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        if (getComponent().isSdkInstalled()) {
            wiz.putProperty(SDK_PATH, getComponent().getSdkPath());
            wiz.putProperty(SDKWizardPanelInstall.SDK_NAME, getComponent().getSdkName());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SDKVisualPanelInstall.SDK_INSTALLED.equals(evt.getPropertyName())) {
            for (ChangeListener listener : listeners) {
                listener.stateChanged(new ChangeEvent(this));
            }
        }
    }

}
