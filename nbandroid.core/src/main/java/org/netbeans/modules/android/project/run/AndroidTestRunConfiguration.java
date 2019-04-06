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
package org.netbeans.modules.android.project.run;

import com.android.builder.model.AndroidProject;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.android.project.api.NbAndroidProject;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author radim
 * @author arsi
 */
public class AndroidTestRunConfiguration implements LookupListener {

    private static final Logger LOG = Logger.getLogger(AndroidTestRunConfiguration.class.getName());

    private static final String PREFERENCE_TEST_RUNNER = "androidTestRunner";

    @VisibleForTesting
    public static final RequestProcessor RP = new RequestProcessor("gradle-test-runner", 1);

    private final ChangeSupport cs = new ChangeSupport(this);
    private final Object lock = new Object();
    private final AuxiliaryProperties auxProps;
    private String testRunner;
    private String defaultTestRunner;
    private final Lookup.Result<AndroidProject> lookupResult;

    public AndroidTestRunConfiguration(NbAndroidProject project) {
        this.auxProps = project.getAuxiliaryProperties();
        testRunner = auxProps.get(PREFERENCE_TEST_RUNNER, false);
        lookupResult = project.getLookup().lookupResult(AndroidProject.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResult));
        resultChanged(null);
        LOG.log(Level.FINE, "loaded test runner {0}", testRunner);
    }

    @Nullable
    public String getTestRunner() {
        synchronized (lock) {
            if (testRunner == null) {
                testRunner = auxProps.get(PREFERENCE_TEST_RUNNER, false);
                LOG.log(Level.FINE, "re-loaded test runner {0}", testRunner);
            }
            return testRunner != null ? testRunner : defaultTestRunner;
        }
    }

    public void setTestRunner(final String testRunner) {
        synchronized (lock) {
            this.testRunner = testRunner;
            auxProps.put(PREFERENCE_TEST_RUNNER, testRunner, false);
            LOG.log(Level.FINE, "saved test runner {0}", testRunner);
        }
        fireChange();
    }

    private void setDefaultTestRunner(final String testRunner) {
        synchronized (lock) {
            this.defaultTestRunner = testRunner;
        }
        fireChange();
    }

    private void fireChange() {
        RP.post(new Runnable() {

            @Override
            public void run() {
                cs.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends AndroidProject> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            String runner = allInstances.iterator().next().getDefaultConfig().getProductFlavor().getTestInstrumentationRunner();
            if (runner == null) {
                return;
            }
            setDefaultTestRunner(runner);
            fireChange();
        }
    }
}
