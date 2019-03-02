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
package org.netbeans.modules.android.project.build;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.Variant;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.config.AndroidBuildVariants;
import org.nbandroid.netbeans.gradle.config.BuildTypes;
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
 * @author arsi
 */
public class BuildVariant implements LookupListener {

    private static final Logger LOG = Logger.getLogger(BuildVariant.class.getName());

    private static final String PREFERENCE_BUILD_VARIANT = "buildVariant";

    @VisibleForTesting
    public static final RequestProcessor RP = new RequestProcessor("gradle-build-variant", 1);

    private final ChangeSupport cs = new ChangeSupport(this);
    private final Object lock = new Object();
    private final AuxiliaryProperties auxProps;
    private AndroidProject androidProjectModel;
    private String variant;
    private final Lookup.Result<AndroidProject> lookupResult;

    // TODO: use non-shared AuxiliaryProperties
    public BuildVariant(NbAndroidProject androidProject) {
        this.auxProps = androidProject.getAuxiliaryProperties();
        variant = auxProps.get(PREFERENCE_BUILD_VARIANT, false);
        lookupResult = androidProject.getLookup().lookupResult(AndroidProject.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, androidProject));
        resultChanged(null);
    }

    @Nullable
    public String getVariantName() {
        synchronized (lock) {
            if (androidProjectModel == null) {
                return null;
            }
            if (AndroidBuildVariants.findVariantByName(androidProjectModel.getVariants(), variant) != null) {
                return variant;
            }
            // first fallback is 'debug'
            if (AndroidBuildVariants.findVariantByName(androidProjectModel.getVariants(), "debug") != null) {
                return "debug";
            }
            // or use first flavored debug variant
            List<String> debugVariants = Lists.newArrayList(
                    Iterables.filter(
                            Iterables.transform(
                                    androidProjectModel.getVariants(),
                                    new Function<Variant, String>() {
                                @Override
                                public String apply(Variant f) {
                                    return f.getName();
                                }
                            }),
                            new Predicate<String>() {

                        @Override
                        public boolean apply(String t) {
                            return t.endsWith("debug") || t.endsWith("Debug");
                        }
                    }));
            Collections.sort(debugVariants);
            return debugVariants.isEmpty() ? null : debugVariants.get(0);
        }
    }

    public void setVariantName(final String variant) {
        synchronized (lock) {
            this.variant = variant;
            auxProps.put(PREFERENCE_BUILD_VARIANT, variant, false);
            LOG.log(Level.FINE, "saved build variant {0}", variant);
        }
        fireChange();
    }

    public Iterable<String> getAllVariantNames() {
        synchronized (lock) {
            return androidProjectModel != null
                    ? Iterables.transform(
                            androidProjectModel.getVariants(),
                            new Function<Variant, String>() {
                        @Override
                        public String apply(Variant f) {
                            return f.getName();
                        }
                    })
                    : Collections.<String>emptyList();
        }
    }

    public BuildTypeContainer getCurrentBuildTypeContainer() {
        synchronized (lock) {
            Variant v = getCurrentVariant();
            return v != null && androidProjectModel != null
                    ? BuildTypes.findBuildTypeByName(androidProjectModel.getBuildTypes(), v.getBuildType())
                    : null;
        }
    }

    public Variant getCurrentVariant() {
        synchronized (lock) {
            return androidProjectModel != null ? AndroidBuildVariants.findVariantByName(androidProjectModel.getVariants(), getVariantName()) : null;
        }
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
            androidProjectModel = allInstances.iterator().next();
            cs.fireChange();
        }
    }
}
