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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static nbandroid.gradle.impl.ExecuteGoal.BLACK;
import static nbandroid.gradle.impl.ExecuteGoal.BLUE;
import nbandroid.gradle.spi.BuildMutex;
import nbandroid.gradle.spi.GradleArgsConfiguration;
import nbandroid.gradle.spi.GradleCommandExecutor;
import nbandroid.gradle.spi.GradleCommandTemplate;
import nbandroid.gradle.spi.GradleHandler;
import nbandroid.gradle.spi.GradleJvmConfiguration;
import nbandroid.gradle.spi.ModelRefresh;
import org.apache.commons.io.output.WriterOutputStream;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.initialization.DefaultBuildCancellationToken;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.internal.consumer.CancellationTokenInternal;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author arsi
 */
public class GradleCommandExecutorImpl implements GradleCommandExecutor, CancellationToken, CancellationTokenInternal, Cancellable {

    private final Project project;
    private final BuildMutex buildMutex;
    private static final GradleHandler gradleHandler = Lookup.getDefault().lookup(GradleHandler.class);
    private final GradleJvmConfiguration jvmConfiguration;
    private final GradleArgsConfiguration argsConfiguration;
    private final AtomicBoolean cancellation = new AtomicBoolean();
    private final AtomicReference<DefaultBuildCancellationToken> cancellationReference = new AtomicReference<>();
    private final RequestProcessor rp = new RequestProcessor(GradleCommandExecutor.class);

    public GradleCommandExecutorImpl(Project project) {
        this.project = project;
        buildMutex = project.getLookup().lookup(BuildMutex.class);
        jvmConfiguration = project.getLookup().lookup(GradleJvmConfiguration.class);
        argsConfiguration = project.getLookup().lookup(GradleArgsConfiguration.class);
    }

    @Override
    public void executeCommand(GradleCommandTemplate command) {
        buildMutex.mutex().postWriteRequest(new Runnable() {
            @Override
            public void run() {
                cancellation.set(false);
                cancellationReference.set(new DefaultBuildCancellationToken());
                File gradleHome = gradleHandler.getGradleHome(project);
                if (gradleHome != null) {
                    GradleConnector connector = GradleConnector.newConnector();
                    connector.useInstallation(gradleHome);
                    connector.forProjectDirectory(FileUtil.toFile(project.getProjectDirectory()));
                    try (ProjectConnection connection = connector.connect()) {
                        BuildLauncher buildLauncher = connection.newBuild();
                        buildLauncher.forTasks(command.getTasksArray());
                        buildLauncher.withCancellationToken(GradleCommandExecutorImpl.this);
                        ProjectConfigurationProvider pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
                        buildLauncher.addArguments(command.getArguments());
                        buildLauncher.addJvmArguments(command.getJvmArguments());
                        if (pcp != null && pcp.getActiveConfiguration() != null) {
                            buildLauncher.addArguments("-Pandroid.profile=" + pcp.getActiveConfiguration());
                        }
                        if (argsConfiguration != null) {
                            buildLauncher.addArguments(argsConfiguration.getJvmArguments());
                        }
                        if (jvmConfiguration == null) {
                            buildLauncher.addJvmArguments("-Xms800m", "-Xmx2000m");
                        } else {
                            buildLauncher.addJvmArguments(jvmConfiguration.getJvmArguments());
                        }
                        InputOutput io = project.getLookup().lookup(InputOutput.class);
                        if (io != null) {
                            io.show(ImmutableSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
                            io.getOut().print("\n\r");
                            io.getOut().print("\n\r");
                            io.getOut().print("\n\r");
                            io.getOut().print("\n\r");
                            io.getOut().println(BLUE + "Executing task: " + command.getSafeDisplayName() + BLACK);
                            io.getOut().print("\n\r");
                            io.getOut().print("\n\r");
                            CustomWriterOutputStream cwos = new CustomWriterOutputStream(io.getOut(), "UTF-8");
                            buildLauncher.setStandardOutput(cwos);
                            buildLauncher.setStandardError(cwos);
                            buildLauncher.setColorOutput(true);
                        }
                        final ProgressHandle progressHandle = ProgressHandleFactory.createSystemHandle(project.getProjectDirectory().getName() + ": " + command.getSafeDisplayName(), GradleCommandExecutorImpl.this);
                        progressHandle.start();
                        buildLauncher.addProgressListener(new ProgressListener() {
                            @Override
                            public void statusChanged(ProgressEvent event) {
                                progressHandle.progress(event.getDisplayName());
                            }
                        });
                        try {
                            buildLauncher.run();
                        } catch (Exception e) {
                            if (!(e instanceof BuildCancelledException)) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                        progressHandle.finish();
                        ModelRefresh modelRefresh = project.getLookup().lookup(ModelRefresh.class);
                        if (modelRefresh != null) {
                            modelRefresh.refreshModels();
                        }
                        if (command.getExecuteAfterBuild() != null) {
                            rp.execute(command.getExecuteAfterBuild());
                        }
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }

                }
            }
        });
    }

    @Override
    public boolean isCancellationRequested() {
        return cancellation.get();
    }

    @Override
    public boolean cancel() {
        cancellation.set(true);
        cancellationReference.get().cancel();
        return true;
    }

    @Override
    public BuildCancellationToken getToken() {
        return cancellationReference.get();
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
