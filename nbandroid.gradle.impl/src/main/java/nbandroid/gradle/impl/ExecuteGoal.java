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
package nbandroid.gradle.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import nbandroid.gradle.spi.GradleHandler;
import nbandroid.gradle.spi.ModelRefresh;
import nbandroid.gradle.tooling.TaskInfo;
import org.apache.commons.io.output.WriterOutputStream;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author arsi
 */
public class ExecuteGoal implements Runnable {

    private final Project project;
    private final TaskInfo taskInfo;
    private static final GradleHandler gradleHandler = Lookup.getDefault().lookup(GradleHandler.class);
    private static final RequestProcessor RP = new RequestProcessor(ExecuteGoal.class);
    private final File gradleHome;

    public ExecuteGoal(Project project, TaskInfo taskInfo) {
        this.project = project;
        this.taskInfo = taskInfo;
        gradleHome = gradleHandler.getGradleHome(project);
        if (gradleHome != null) {
            RP.execute(this);
        }

    }
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String BLUE = "\033[0;34m";    // BLUE

    @Override
    public void run() {
        GradleConnector connector = GradleConnector.newConnector();
        connector.useInstallation(gradleHome);
        connector.forProjectDirectory(FileUtil.toFile(project.getProjectDirectory()));
        try (ProjectConnection connection = connector.connect()) {
            BuildLauncher buildLauncher = connection.newBuild();
            buildLauncher.forTasks(taskInfo.getName());
            ProjectConfigurationProvider pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
            if (pcp != null && pcp.getActiveConfiguration() != null) {
                buildLauncher.addArguments("-Pandroid.profile=" + pcp.getActiveConfiguration());
            }
            buildLauncher.addJvmArguments("-Xms3000m", "-Xmx5000m");
            InputOutput io = project.getLookup().lookup(InputOutput.class);
            if (io != null) {
                io.show(ImmutableSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
                io.getOut().print("\n\r");
                io.getOut().print("\n\r");
                io.getOut().print("\n\r");
                io.getOut().print("\n\r");
                io.getOut().println(BLUE + "Executing task: " + taskInfo.getName() + BLACK);
                io.getOut().print("\n\r");
                io.getOut().print("\n\r");
                CustomWriterOutputStream cwos = new CustomWriterOutputStream(io.getOut(), "UTF-8");
                buildLauncher.setStandardOutput(cwos);
                buildLauncher.setStandardError(cwos);
                buildLauncher.setColorOutput(true);
            }
            final ProgressHandle progressHandle = ProgressHandleFactory.createSystemHandle(project.getProjectDirectory().getName() + ": Loading Gradle model..");
            progressHandle.start();
            buildLauncher.addProgressListener(new ProgressListener() {
                @Override
                public void statusChanged(ProgressEvent event) {
                    progressHandle.progress(event.getDisplayName());
                }
            });
            try {
                buildLauncher.run();
            } catch (GradleConnectionException | IllegalStateException gradleConnectionException) {
                Exceptions.printStackTrace(gradleConnectionException);
            }
            progressHandle.finish();
            ModelRefresh modelRefresh = project.getLookup().lookup(ModelRefresh.class);
            if (modelRefresh != null) {
                modelRefresh.refreshModels();
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private static class CustomWriterOutputStream extends WriterOutputStream {

        private final Writer writer;

        public CustomWriterOutputStream(Writer writer, String charsetName) {
            super(writer, charsetName);
            this.writer = writer;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            synchronized (writer) {
                String s = new String(b, off, len, "UTF-8").replaceAll("\u001B(\\[)((\\d+)(;))+(\\d+)m", "");
                if (!s.contains("\n\r") && s.contains("\n")) {
                    s = s.replace("\n", "\n\r");
                }
                writer.write(s);
            }
        }

    }

}
