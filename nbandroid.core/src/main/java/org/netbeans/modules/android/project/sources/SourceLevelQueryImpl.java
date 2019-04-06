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
package org.netbeans.modules.android.project.sources;

import com.android.builder.model.AndroidProject;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation2, SourceLevelQueryImplementation2.Result2, LookupListener {

    private final Project project;
    private final Lookup.Result<AndroidProject> lookupResult;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final AtomicReference<AndroidProject> androidProject = new AtomicReference<>(null);

    public SourceLevelQueryImpl(Project project) {
        this.project = project;
        lookupResult = project.getLookup().lookupResult(AndroidProject.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, project));
        resultChanged(null);
    }

    @Override
    public Result getSourceLevel(FileObject fo) {
        final Project owner = FileOwnerQuery.getOwner(fo);
        if (owner != null && project.equals(owner)) {
            return this;
        }
        return null;
    }

    @Override
    public SourceLevelQuery.Profile getProfile() {
        return SourceLevelQuery.Profile.DEFAULT;
    }

    @Override
    public String getSourceLevel() {
        final AndroidProject andrProject = androidProject.get();
        if (andrProject != null) {
            return andrProject.getJavaCompileOptions().getSourceCompatibility();
        }
        //TODO read from project properties
        return "1.7";
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        changeSupport.addChangeListener(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        changeSupport.removeChangeListener(cl);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends AndroidProject> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            androidProject.set(allInstances.iterator().next());
        }
        changeSupport.fireChange();
    }

}
