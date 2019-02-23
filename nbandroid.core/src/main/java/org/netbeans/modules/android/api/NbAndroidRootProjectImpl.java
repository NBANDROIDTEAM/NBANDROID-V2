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
package org.netbeans.modules.android.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import nbandroid.gradle.spi.GradleHandler;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.netbeans.api.io.IOProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author arsi
 */
public class NbAndroidRootProjectImpl implements Project, Runnable {

    private final FileObject projectDirectory;
    private final InstanceContent ic = new InstanceContent();
    private final AbstractLookup lookup = new AbstractLookup(ic);
    private static final RequestProcessor RP = new RequestProcessor(NbAndroidRootProjectImpl.class.getName(), 1);

    public NbAndroidRootProjectImpl(FileObject projectDirectory, ProjectState ps) {
        this.projectDirectory = projectDirectory;
        ic.add(IOProvider.get("Terminal").getIO(projectDirectory.getName(), false));
        RP.execute(this);
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void run() {
        Lookup modelLookup = GradleHandler.getDefault().getModelLookup(this);
        GradleHandler.getDefault().refreshModelLookup(this, GradleProject.class, GradleBuild.class);
        System.out.println("org.netbeans.modules.android.api.NbAndroidRootProjectImpl.run()");
        GradleHandler.getDefault().refreshModelLookup(this, GradleProject.class);
        List<GradleTask> tasks = new ArrayList<>();
        GradleConnector connector = GradleConnector.newConnector();
        connector.useInstallation(new File("/home/arsi/.gradle/wrapper/dists/gradle-4.10.2-bin/49c0y5oy9a90fibq1gqvcthdn/gradle-4.10.2"));
        connector.forProjectDirectory(FileUtil.toFile(projectDirectory));
        ;
        try (ProjectConnection connection = connector.connect()) {
            GradleProject project = connection.getModel(GradleProject.class);
            for (GradleTask task : project.getTasks()) {
                tasks.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
