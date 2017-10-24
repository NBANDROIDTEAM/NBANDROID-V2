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
package org.netbeans.modules.android.project.ui.wizards;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ExportPackageCreateKeyWizardPanel implements WizardDescriptor.Panel {

  private WizardDescriptor wizardDescriptor;
  /** The visual component that displays this panel. */
  private ExportPackageCreateKeyVisualPanel component;

  @Override
  public ExportPackageCreateKeyVisualPanel getComponent() {
    if (component == null) {
      component = new ExportPackageCreateKeyVisualPanel(this);
    }
    return component;
  }

  @Override
  public HelpCtx getHelp() {
    return HelpCtx.DEFAULT_HELP;
  }

  @Override
  public boolean isValid() {
    return getComponent().valid(wizardDescriptor);
  }

  /** Called when data are updated to update the state (and fire changes). */
  void onChange() {
    getComponent().validate(wizardDescriptor);
    fireChangeEvent(); // will trigger call to isValid()
  }

  private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

  @Override
  public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
      listeners.add(l);
    }
  }

  @Override
  public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
      listeners.remove(l);
    }
  }

  protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
      it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
      it.next().stateChanged(ev);
    }
  }

  // You can use a settings object to keep track of state. Normally the
  // settings object will be the WizardDescriptor, so you can use
  // WizardDescriptor.getProperty & putProperty to store information entered
  // by the user.
  @Override
  public void readSettings(Object settings) {
    wizardDescriptor = (WizardDescriptor) settings;        
    component.read(wizardDescriptor);
  }

  @Override
  public void storeSettings(Object settings) {
    wizardDescriptor = (WizardDescriptor) settings;        
    component.store(wizardDescriptor);
  }
}
