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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nbandroid.gradle.impl.update.CurrentGradleVersion;
import nbandroid.gradle.impl.update.GradleUpdateHandler;
import org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet;
import org.gradle.wrapper.BootstrapMainStarter;
import org.gradle.wrapper.Download;
import org.gradle.wrapper.DownloadProgressListener;
import org.gradle.wrapper.GradleUserHomeLookup;
import org.gradle.wrapper.Install;
import org.gradle.wrapper.Logger;
import org.gradle.wrapper.PathAssembler;
import org.gradle.wrapper.WrapperExecutor;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author arsi
 */
public class GradleDownloader extends BootstrapMainStarter implements DownloadProgressListener, Runnable {

    private final InputOutput io;
    private ProgressHandle progressHandle;
    private boolean init = false;
    private final InstanceContent instanceContent = new InstanceContent();
    private final Project project;
    private final Lookup lookup;
    private static final RequestProcessor RP = new RequestProcessor(GradleDownloader.class.getName(), 1);
    private static final String re1 = ".*?";	// Non-greedy match on filler
    private static final String re2 = "(\\d+)";	// Integer Number 1
    private static final String re3 = "(\\.)";	// Any Single Character 1
    private static final String re4 = "(\\d+)";	// Integer Number 2
    private static final Pattern PATTERN = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public GradleDownloader(Project project) {
        this.project = project;
        io = project.getLookup().lookup(InputOutput.class);
        lookup = new AbstractLookup(instanceContent);
        RP.execute(this);
    }

    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void start(String[] args, File gradleHome) throws Exception {
        progressHandle.finish();
        instanceContent.add(new GradleHome(Status.OK, gradleHome));
    }

    @Override
    public void run() {
        FileObject projectDirectory = project.getProjectDirectory();
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
            boolean handleAndroidPluginVersion = handleAndroidPluginVersion(wrapperFolder);
            if (!handleAndroidPluginVersion) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unsupported Gradle plugin Version", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                instanceContent.add(new GradleHome(Status.ERROR, null));
                return;
            }
            FileObject propertiesFo = wrapperFolder.getFileObject("gradle-wrapper", "properties");
            if (propertiesFo != null) {
                Properties properties = new Properties();
                try {
                    properties.load(propertiesFo.getInputStream());
                    String distributionUrl = properties.getProperty("distributionUrl");
                    if (distributionUrl != null && distributionUrl.contains("/")) {
                        distributionUrl = distributionUrl.substring(distributionUrl.lastIndexOf("/") + 1);
                        Matcher m = PATTERN.matcher(distributionUrl);
                        if (m.find()) {
                            String majorVersionStr = m.group(1);
                            String minorVersionStr = m.group(3);
                            Runtime.Version version = Runtime.version();
                            int feature = version.feature();
                            try {
                                int majorVersion = Integer.valueOf(majorVersionStr);
                                int minorVersion = Integer.valueOf(minorVersionStr);
                                GradleVersion currentGradleVersion = new GradleVersion(majorVersion, minorVersion);
                                GradleVersion needVersion;
                                switch (feature) {
                                    case 10:
                                        //Gradle >= 4.7
                                        needVersion = new GradleVersion(4, 7);
                                        break;
                                    case 11:
                                        //Gradle >= 5.0
                                        needVersion = new GradleVersion(5, 0);
                                        break;
                                    case 12:
                                        //Gradle >= 5.4
                                        needVersion = new GradleVersion(5, 4);
                                        break;
                                    default:
                                        NotifyDescriptor nd = new NotifyDescriptor.Message("Unsupported Java Version: " + feature, NotifyDescriptor.ERROR_MESSAGE);
                                        DialogDisplayer.getDefault().notifyLater(nd);
                                        instanceContent.add(new GradleHome(Status.ERROR, null));
                                        return;
                                }
                                if (currentGradleVersion.compareTo(needVersion) < 0) {
                                    //update
                                    CurrentGradleVersion latestGradleVersion = GradleUpdateHandler.getCurrentGradleVersion();
                                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation("This project uses a version of Gradle that is not compatible with Java " + feature + ". Do you want to update Gradle version to: " + latestGradleVersion.getVersion(), NotifyDescriptor.YES_NO_OPTION);
                                    Object notify = DialogDisplayer.getDefault().notify(nd);
                                    if (NotifyDescriptor.YES_OPTION.equals(notify)) {
                                        properties.put("distributionUrl", latestGradleVersion.getDownloadUrl());
                                        try {
                                            properties.store(new FileOutputStream(new File(propertiesFo.getPath())), "Changed by NBANDROID-V2");
                                        } catch (IOException iOException) {
                                            NotifyDescriptor nda = new NotifyDescriptor.Message("Unable to write to file: " + propertiesFo.getPath(), NotifyDescriptor.ERROR_MESSAGE);
                                            DialogDisplayer.getDefault().notifyLater(nda);
                                            instanceContent.add(new GradleHome(Status.ERROR, null));
                                            return;
                                        }

                                    } else {
                                        NotifyDescriptor nda = new NotifyDescriptor.Message("Unsupported Gradle Version: " + majorVersion + "." + minorVersion, NotifyDescriptor.ERROR_MESSAGE);
                                        DialogDisplayer.getDefault().notifyLater(nda);
                                        instanceContent.add(new GradleHome(Status.ERROR, null));
                                        return;
                                    }

                                }
                            } catch (NumberFormatException numberFormatException) {
                                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to decode Mador FGradle version from gradle-wrapper.properties ", NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notifyLater(nd);
                                instanceContent.add(new GradleHome(Status.ERROR, null));
                                return;
                            }
                        } else {
                            NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to decode Gradle version from gradle-wrapper.properties ", NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(nd);
                            instanceContent.add(new GradleHome(Status.ERROR, null));
                            return;
                        }
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to decode Gradle distributionUrl from gradle-wrapper.properties ", NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                        instanceContent.add(new GradleHome(Status.ERROR, null));
                        return;
                    }
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                try {
                    File gradleUserHome = GradleUserHomeLookup.gradleUserHome();
                    WrapperExecutor wrapperExecutor = WrapperExecutor.forWrapperPropertiesFile(FileUtil.toFile(propertiesFo));
                    NbLogger logger = new NbLogger(true);
                    progressHandle = ProgressHandleFactory.createSystemHandle("Loading Gradle project " + projectDirectory.getName());
                    progressHandle.start();
                    wrapperExecutor.execute(null, new Install(logger, new Download(logger, this, "lll", "llll"), new PathAssembler(gradleUserHome)), this);
                } catch (Exception e) {
                    progressHandle.finish();
                    Exceptions.printStackTrace(e);
                    instanceContent.add(new GradleHome(Status.ERROR, null));
                }

            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Message("No Gradle wropper configuration found! Unable to open project..", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                instanceContent.add(new GradleHome(Status.ERROR, null));
            }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message("No Gradle wropper configuration found! Unable to open project..", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            instanceContent.add(new GradleHome(Status.ERROR, null));
        }
    }

    private static final String are1 = "(classpath)";	// Word 1
    private static final String are2 = ".*?";	// Non-greedy match on filler
    private static final String are3 = "(')";	// Any Single Character 1
    private static final String are4 = "(com\\.android\\.tools\\.build)";	// Fully Qualified Domain Name 1
    private static final String are5 = "(:)";	// Any Single Character 2
    private static final String are6 = "((?:[a-z][a-z]+))";	// Word 2
    private static final String are7 = "(:)";	// Any Single Character 3
    private static final String major = "(\\d+)";	// Integer Number 1
    private static final String are9 = "(\\.)";	// Any Single Character 4
    private static final String minor = "(\\d+)";	// Integer Number 2
    private static final String are11 = "(\\.)";	// Any Single Character 5
    private static final String rev1 = "(\\d+)";	// Integer Number 3
    private static final String are13 = "(')";	// Any Single Character 6
    private static final Pattern patternPlugin = Pattern.compile(are1 + are2 + are3 + are4 + are5 + are6 + are7 + major + are9 + minor + are11 + rev1 + are13, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private boolean handleAndroidPluginVersion(FileObject wrapperFolder) {
        FileObject parent = wrapperFolder.getParent();
        if (parent != null) {
            parent = parent.getParent();
            if (parent != null) {
                FileObject buildGradle = parent.getFileObject("build", "gradle");
                if (buildGradle != null) {
                    int majorVersion = 0;
                    int minorVersion = 0;
                    int revision = 0;
                    try {
                        List<String> lines = buildGradle.asLines("UTF-8");
                        String pluginLine = null;
                        for (String line : lines) {
                            Matcher m = patternPlugin.matcher(line);
                            if (m.find()) {
                                pluginLine = line;
                                try {
                                    majorVersion = Integer.parseInt(m.group(7));
                                    minorVersion = Integer.parseInt(m.group(9));
                                    revision = Integer.parseInt(m.group(11));
                                } catch (NumberFormatException numberFormatException) {
                                }
                                break;

                            }
                        }
                        if (pluginLine != null) {
                            PluginVersion current = new PluginVersion(majorVersion, minorVersion, revision);
                            PluginVersion wanted = new PluginVersion(3, 4, 0);
                            if (current.compareTo(wanted) != 0) {
                                NotifyDescriptor nd = new NotifyDescriptor.Confirmation("<html>This project currently uses Gradle Android plugin " + current.toString() + "<br>"
                                        + "NBANDROID-V2 needs version " + wanted.toString() + " for proper functionality.<br>"
                                        + "Do you want to change the configuration of the project and use the version " + wanted.toString() + "?</hmml>", NotifyDescriptor.YES_NO_OPTION);
                                Object notify = DialogDisplayer.getDefault().notify(nd);
                                if (NotifyDescriptor.YES_OPTION.equals(notify)) {
                                    String content = buildGradle.asText("UTF-8");
                                    content = content.replace(pluginLine, "        classpath 'com.android.tools.build:gradle:"+wanted.toString()+"'");
                                    FileUtils.writeStringToFile(FileUtil.toFile(buildGradle), content, "UTF-8");
                                } else {
                                    return false;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return true;
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
        if (!init) {
            progressHandle.setDisplayName("Downloading:" + address.toString());
            progressHandle.switchToDeterminate((int) contentLength);
            init = true;
            progressHandle.progress((int) downloaded);
        } else {
            progressHandle.progress((int) downloaded);
        }
    }

    private class NbLogger extends Logger {

        public NbLogger(boolean quiet) {
            super(quiet);
        }

        @Override
        public void log(String message) {
            String s = message.replaceAll("\u001B(\\[)((\\d+)(;))+(\\d+)m", "");
            if (!s.contains("\n\r") && s.contains("\n")) {
                s = s.replace("\n", "\n\r");
            }
            io.show(ImmutableSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
            io.getOut().println(s);
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

    class GradleVersion implements Comparable<GradleVersion> {

        final int major;
        final int minor;

        public GradleVersion(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        @Override
        public int compareTo(GradleVersion o) {
            if (this.major != o.major) {
                return Integer.compare(this.major, o.major);
            }
            if (this.minor != o.minor) {
                return Integer.compare(this.minor, o.minor);
            }
            return 0;
        }

        @Override
        public String toString() {
            return major + "." + minor;
        }

    }

    static class PluginVersion implements Comparable<PluginVersion> {

        final int major;
        final int minor;
        final int rev;

        public PluginVersion(int major, int minor, int rev) {
            this.major = major;
            this.minor = minor;
            this.rev = rev;
        }

        @Override
        public int compareTo(PluginVersion o) {
            if (this.major != o.major) {
                return Integer.compare(this.major, o.major);
            }
            if (this.minor != o.minor) {
                return Integer.compare(this.minor, o.minor);
            }
            if (this.rev != o.rev) {
                return Integer.compare(this.rev, o.rev);
            }
            return 0;
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + rev;
        }
    }

}
