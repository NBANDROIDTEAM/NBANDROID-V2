/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.nodes;

import com.android.builder.model.Library;
import java.awt.Image;
import java.io.File;
import java.util.List;
import javax.swing.Action;
import org.nbandroid.netbeans.gradle.v2.maven.ArtifactData;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
public class DependencyNode extends FilterNode {

    private final Library library;
    public static final String JAVADOC_NAME = "-javadoc.jar";
    public static final String SRC_NAME = "-sources.jar";
    public static final String EXTRAS = "extras";
    public static final String ANDROID = "android";
    public static final String M2REPOSITORY = "m2repository";
    public static final String GOOGLE = "google";
    public static final String GRADLE_CACHES = "caches";
    public static final String GRADLE_REPO = "files-2.1";
    public static final String GRADLE_MODULES = "modules-2";
    public static final String EXTRAS_ANDROID_M2 = EXTRAS + File.separator + ANDROID + File.separator + M2REPOSITORY;
    public static final String EXTRAS_GOOGLE_M2 = EXTRAS + File.separator + GOOGLE + File.separator + M2REPOSITORY;
    public static final String EXTRAS_M2 = EXTRAS + File.separator + M2REPOSITORY;
    public static final String GRADLE_STORE = GRADLE_CACHES + File.separator + GRADLE_MODULES + File.separator + GRADLE_REPO;
    private final ArtifactData data;

    private static Lookup createLookup(ArtifactData library) {
        return Lookups.fixed(library);
    }

    public Library getLibrary() {
        return library;
    }

    public DependencyNode(Node original, ArtifactData library) {
        super(original, new Children(original), createLookup(library));
        this.data = getLookup().lookup(ArtifactData.class);
        this.library = data.getLibrary();
    }

    @Override
    public Image getIcon(int type) {
        Image icon = super.getIcon(type);
        return data.getIcon(icon);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> actionsForPath = Utilities.actionsForPath("Actions/NbAndroid/Dependency");
        return actionsForPath.toArray(new Action[actionsForPath.size()]);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        String versionlessId = library.getResolvedCoordinates().getVersionlessId();
        if (versionlessId.contains("_local_jars_")) {
            int lastIndexOf = versionlessId.lastIndexOf('/');
            if (lastIndexOf == -1) {
                lastIndexOf = versionlessId.lastIndexOf('\\');
            }
            if (lastIndexOf > -1) {
                versionlessId = versionlessId.substring(lastIndexOf + 1);
            }
        }
        return versionlessId;
    }

}
