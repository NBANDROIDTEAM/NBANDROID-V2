/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.project.template;

import android.studio.imports.templates.Template;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelMobileActivityAndroidSettings.PROP_MOBILE_CONFIG;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 */
public class AndroidProjectTemplateWizardPanelMobileActivityAndroidSettings implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private AndroidProjectTemplatePanelMobileActivityAndroidSettings component;

    public AndroidProjectTemplateWizardPanelMobileActivityAndroidSettings() {
    }

    public Component getComponent() {
        if (component == null) {
            component = new AndroidProjectTemplatePanelMobileActivityAndroidSettings(this);
            component.setName("Android platform");
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(AndroidProjectTemplatePanelMobileActivityAndroidSettings.class);
    }

    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        wizardDescriptor.putProperty(PROP_MOBILE_CONFIG, component.getCurrentTemplate() != null);
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<ChangeListener>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }

    public boolean isFinishPanel() {
        Template currentTemplate = component.getCurrentTemplate();
        Boolean wear = (Boolean) wizardDescriptor.getProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_WEAR_ENABLED);
        Boolean tv = (Boolean) wizardDescriptor.getProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_ENABLED);
        if (currentTemplate == null && !wear && !tv) {
            return true;
        }
        return false;
    }

    public void validate() throws WizardValidationException {
        getComponent();
        component.validate(wizardDescriptor);
    }

}
