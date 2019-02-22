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
import java.net.URI;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet;
import org.gradle.wrapper.BootstrapMainStarter;
import org.gradle.wrapper.Download;
import org.gradle.wrapper.DownloadProgressListener;
import org.gradle.wrapper.GradleUserHomeLookup;
import org.gradle.wrapper.Install;
import org.gradle.wrapper.Logger;
import org.gradle.wrapper.PathAssembler;
import org.gradle.wrapper.WrapperExecutor;
import org.netbeans.api.io.IOProvider;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author arsi
 */
public class GradleDownloader extends BootstrapMainStarter implements DownloadProgressListener {

    private InputOutput io;
    private ProgressHandle progressHandle;
    private boolean init = false;
    private final InstanceContent instanceContent = new InstanceContent();

    public Lookup findConnector(Project project) {
        FileObject projectDirectory = project.getProjectDirectory();
        io = IOProvider.getDefault().getIO(projectDirectory.getName(), false);
        FileObject wrapperFolder = null;
        int parents = 5;
        do {
            wrapperFolder = projectDirectory.getFileObject("gradle/wrapper");
            if (wrapperFolder == null) {
                projectDirectory = projectDirectory.getParent();
                parents--;
            }
        } while (wrapperFolder == null && parents > 0);
        if (wrapperFolder != null) {
            FileObject propertiesFo = wrapperFolder.getFileObject("gradle-wrapper", "properties");
            if (propertiesFo != null) {
                try {
                    File gradleUserHome = GradleUserHomeLookup.gradleUserHome();
                    WrapperExecutor wrapperExecutor = WrapperExecutor.forWrapperPropertiesFile(FileUtil.toFile(propertiesFo));
                    NbLogger logger = new NbLogger(true);
                    progressHandle = ProgressHandleFactory.createSystemHandle("Loading Gradle project " + projectDirectory.getName());
                    progressHandle.start();
                    wrapperExecutor.execute(null, new Install(logger, new Download(logger, this, "lll", "llll"), new PathAssembler(gradleUserHome)), this);
                } catch (Exception e) {
                    instanceContent.add(new GradleHome(Status.ERROR, null));
                }

            } else {
                instanceContent.add(new GradleHome(Status.ERROR, null));
            }
        } else {
            instanceContent.add(new GradleHome(Status.ERROR, null));
        }
        return new AbstractLookup(instanceContent);
    }

    @Override
    public void start(String[] args, File gradleHome) throws Exception {
        progressHandle.finish();
        instanceContent.add(new GradleHome(Status.OK, gradleHome));
    }

    public static enum Status {
        OK, ERROR;
    }

    public static class GradleHome {

        final Status status;
        final File gradleHome;

        public GradleHome(Status status, File gradleHome) {
            this.status = status;
            this.gradleHome = gradleHome;
        }

        public Status getStatus() {
            return status;
        }

        public File getGradleHome() {
            return gradleHome;
        }

    }


    @Override
    public void downloadStatusChanged(URI address, long contentLength, long downloaded) {
       if(!init){
           progressHandle.setDisplayName("Downloading:" + address.toString());
           progressHandle.switchToDeterminate((int) contentLength);
           init = true;
           progressHandle.progress((int) downloaded);
       }else{
           progressHandle.progress((int) downloaded);
       }
    }

    private class NbLogger extends Logger {

        public NbLogger(boolean quiet) {
            super(quiet);
        }

        @Override
        public void log(String message) {
            io.show(ImmutableSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
            io.getOut().println(message);
        }

        @Override
        public Appendable append(char c) {
            io.show(ImmutableSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
            io.getOut().append(c);
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            io.getOut().append(csq, start, end);
            return this;
        }

        @Override
        public Appendable append(CharSequence csq) {
            io.getOut().append(csq);
            return this;
        }

    }

}
