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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkImpl;
import org.nbandroid.netbeans.gradle.v2.sdk.PlatformConvertor;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 * @author ArSi
 */
@TemplateRegistration(folder = "Android", displayName = "#SDKWizardIterator_displayName", iconBase = "org/nbandroid/netbeans/gradle/v2/sdk/ui/sdk_manager.png", description = "sDK.html")
@Messages("SDKWizardIterator_displayName=Android SDK Installer")
public final class SDKWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor>, ChangeListener {

    private int index;

    private WizardDescriptor wizard;
    private final SDKWizardPanelMode panel1 = new SDKWizardPanelMode();
    private final SDKWizardPanelSelect panelLocal = new SDKWizardPanelSelect();
    private final SDKWizardPanelDownload panelDownload = new SDKWizardPanelDownload();
    private final SDKWizardPanelInstall panel3 = new SDKWizardPanelInstall();
    private final List<ChangeListener> listeners = new ArrayList<>();
    WizardDescriptor.Panel<WizardDescriptor> panels[] = new WizardDescriptor.Panel[]{panel1, panelLocal, panel3};

    @Override
    public java.util.Set instantiate() throws IOException {
        AndroidSdkImpl p = new AndroidSdkImpl((String) wizard.getProperty(SDKWizardPanelInstall.SDK_NAME), (String) wizard.getProperty(SDKWizardPanelInstall.SDK_PATH));
        p.setDefault((boolean) wizard.getProperty(SDKWizardPanelInstall.SDK_DEFAULT));
//        InstanceDataObject.create(
//                DataFolder.findFolder(FileUtil.createFolder(FileUtil.getConfigRoot(), "Services/Platforms/org-nbandroid-netbeans-gradle-Platform")),
//                null,
//                p,
//                null,
//                true);
        return Collections.singleton(PlatformConvertor.create(p));
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        panel1.addChangeListener(this);
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panel1.removeChangeListener(this);
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return index + 1 + ". from " + panels.length;
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1 && panels[index + 1] != null;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        switch (panel1.getMode()) {
            case DOWNLOAD:
                panels[1] = panelDownload;
                panels[2] = panel3;
                break;
            default:
                panels[1] = panelLocal;
                panels[2] = null;
                break;
        }
        for (ChangeListener listener : listeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

}
