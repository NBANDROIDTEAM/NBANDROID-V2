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

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import nbandroid.gradle.impl.GradleDownloader.GradleHome;
import nbandroid.gradle.spi.GradleHandler;
import org.apache.commons.io.output.WriterOutputStream;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = GradleHandler.class)
public class GradleHandlerImpl implements GradleHandler {

    private final Map<Project, GradleHandlerApi> handlers = new WeakHashMap<>();

    @Override
    public void refreshModelLookup(Project project, Class... classes) {
        GradleHandlerApi handlerApi;
        synchronized (handlers) {
            handlerApi = handlers.get(project);
            if (handlerApi == null) {
                handlerApi = new GradleHandlerApi(project);
                handlers.put(project, handlerApi);
            }
        }
        handlerApi.refreshModelLookup(classes);
    }

    @Override
    public Lookup getModelLookup(Project project) {
        GradleHandlerApi handlerApi;
        synchronized (handlers) {
            handlerApi = handlers.get(project);
            if (handlerApi == null) {
                handlerApi = new GradleHandlerApi(project);
                handlers.put(project, handlerApi);
            }
        }
        return handlerApi.getModelLookup();
    }

    private static class GradleHandlerApi implements LookupListener {

        private final Project project;
        private final InstanceContent modelContent = new InstanceContent();
        private final Lookup modelLookup = new AbstractLookup(modelContent);
        private final GradleDownloader downloader;
        private final Lookup.Result<GradleHome> lookupResult;
        private Class models[] = new Class[0];

        public GradleHandlerApi(Project project) {
            this.project = project;
            downloader = new GradleDownloader(project);
            lookupResult = downloader.getLookup().lookupResult(GradleHome.class);
            lookupResult.addLookupListener(this);
            resultChanged(null);
        }

        public void refreshModelLookup(Class... classes) {
            models = classes;
            resultChanged(null);
        }

        public Lookup getModelLookup() {
            return modelLookup;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            if (!lookupResult.allInstances().isEmpty()) {
                GradleHome gradleHome = lookupResult.allInstances().iterator().next();
                if (gradleHome.getStatus() == GradleDownloader.Status.OK) {
                    modelContent.set(Collections.emptyList(), null);
                    GradleConnector connector = GradleConnector.newConnector();
                    connector.useInstallation(gradleHome.getGradleHome());
                    connector.forProjectDirectory(FileUtil.toFile(project.getProjectDirectory()));
                    try (ProjectConnection connection = connector.connect()) {
                        for (Class model : models) {
                            ModelBuilder modelBuilder = connection.model(model);
                            InputOutput io = project.getLookup().lookup(InputOutput.class);
                            if (io != null) {
                                io.show(ImmutableSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
                                CustomWriterOutputStream cwos = new CustomWriterOutputStream(io.getOut(), "UTF-8");
                                modelBuilder.setStandardOutput(cwos);
                                modelBuilder.setStandardError(cwos);
                                modelBuilder.setColorOutput(true);
                            }
                            final ProgressHandle progressHandle = ProgressHandleFactory.createSystemHandle(project.getProjectDirectory().getName() + ": Loading Gradle model..");
                            progressHandle.start();
                            modelBuilder.addProgressListener(new ProgressListener() {
                                @Override
                                public void statusChanged(ProgressEvent event) {
                                    progressHandle.progress(event.getDisplayName());
                                }
                            });
                            try {
                                modelContent.add(modelBuilder.get());
                            } catch (GradleConnectionException gradleConnectionException) {
                            } catch (IllegalStateException illegalStateException) {
                            }
                            progressHandle.finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
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
                String s = new String(b, off, len, "UTF-8").replaceAll("\u001B(\\[)((\\d+)(;))+(\\d+)m", "").replace("\u001B[0K", "\u001B[0K\u001B[100D");
                writer.write(s);
            }
        }

    }

}
