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

package org.netbeans.modules.android.project.debug;

import com.android.ddmlib.Client;
import com.android.ide.common.xml.ManifestData;
import com.google.common.collect.Maps;
import java.util.Map;
import org.nbandroid.netbeans.gradle.api.AndroidProjects;
import org.nbandroid.netbeans.gradle.launch.Launches;
import org.nbandroid.netbeans.gradle.spi.AndroidDebugInfo;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.android.project.query.AndroidClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author arsi
 */
public class GradleDebugInfo implements AndroidDebugInfo {

    private final Project project;

    public GradleDebugInfo(Project project) {
        this.project = project;
    }

    @Override
    public boolean supportsDebugging() {
        return true;
    }

    @Override
    public boolean canDebug(String processName) {
        ManifestData manifest = AndroidProjects.parseProjectManifest(project);
        return manifest != null
                && (manifest.getPackage().equals(processName) || processName.startsWith(manifest.getPackage() + "."));
    }

    @Override
    public AndroidDebugInfo.AndroidDebugData data(Client client) {
        final int port = client.getDebuggerListenPort();
        final Map<String, Object> properties = Maps.newHashMap();
        final AndroidClassPathProvider cpp = project.getLookup().lookup(AndroidClassPathProvider.class);
        final ClassPath sourcePath = cpp.getSourcePath();
        final ClassPath compilePath = cpp.getCompilePath();
        final ClassPath bootPath = cpp.getBootPath();
        properties.put("sourcepath",
                ClassPathSupport.createProxyClassPath(sourcePath, Launches.toSourcePath(compilePath)));
        properties.put("name", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N
        properties.put("jdksources", Launches.toSourcePath(bootPath)); // NOI18N
        properties.put("baseDir", FileUtil.toFile(project.getProjectDirectory()));   //NOI18N
        return new AndroidDebugInfo.AndroidDebugData("localhost", port, properties);
    }

    @Override
    public Project project() {
        return project;
    }
}
