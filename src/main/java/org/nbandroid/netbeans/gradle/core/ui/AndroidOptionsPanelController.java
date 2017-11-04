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
package org.nbandroid.netbeans.gradle.core.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.nbandroid.netbeans.gradle.core.sdk.StatsCollector;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public final class AndroidOptionsPanelController extends OptionsPanelController {

    private AndroidOptionsSubPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    private StatsCollector statsCollector;
    private boolean enableStats;

    public AndroidOptionsPanelController() {
    }

    @Override
    public void update() {
        changed = false;
        statsCollector = StatsCollector.getDefault();
        enableStats = statsCollector.isEnabled();
        getPanel().load();
        // force validation
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    @Override
    public void applyChanges() {
        if (changed) {
            getPanel().store();
            if (statsCollector.isEnabled() != enableStats) {
                statsCollector.setEnabled(enableStats);
            }
            changed = false;
        }
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
        markChanged();
    }

    @Override
    public boolean isValid() {
        return !changed || getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private AndroidOptionsSubPanel getPanel() {
        if (panel == null) {
            panel = new AndroidPlatformPanel(this);
        }
        return panel;
    }

    public void markChanged() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
