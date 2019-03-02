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
package org.netbeans.modules.android.project.query;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.Collection;
import org.nbandroid.netbeans.gradle.api.AndroidManifestSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.build.BuildVariant;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class GradleAndroidManifest implements AndroidManifestSource, LookupListener {

    private AndroidProject androidProjectModel;
    private final BuildVariant buildConfig;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final Lookup.Result<AndroidProject> lookupResult;

    public GradleAndroidManifest(Project project, BuildVariant buildConfig) {
        this.buildConfig = buildConfig;
        lookupResult = project.getLookup().lookupResult(AndroidProject.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResult));
        resultChanged(null);
    }

    @Override
    public FileObject get() {
        if (androidProjectModel != null) {
            File manifestFile = androidProjectModel.getDefaultConfig().getSourceProvider().getManifestFile();

            BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
            File buildManifestFile = buildTypeContainer != null
                    ? buildTypeContainer.getSourceProvider().getManifestFile()
                    : null;
            File m = Iterables.find(Lists.newArrayList(manifestFile, buildManifestFile), Predicates.notNull(), null);
            if (m != null) {
                return FileUtil.toFileObject(m);
            }
        }
        return null;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends AndroidProject> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            androidProjectModel = allInstances.iterator().next();
        } else {
            androidProjectModel = null;
        }
        cs.fireChange();
    }
}
