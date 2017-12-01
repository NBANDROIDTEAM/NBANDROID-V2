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
package org.nbandroid.netbeans.gradle.v2.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JProgressBar;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.gradle.impldep.org.apache.commons.io.FileUtils;
import org.gradle.impldep.org.apache.commons.io.IOUtils;
import org.nbandroid.netbeans.gradle.v2.gradle.GradleAndroidRepositoriesProvider;
import org.nbandroid.netbeans.gradle.v2.gradle.Repository;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class MavenDownloader {

    public static final String ANDROID_REPO = "https://dl.google.com/dl/android/maven2";
    public static final String MAVEN_CENTRAL = "http://central.maven.org/maven2";
    public static final String JCENTER = "http://jcenter.bintray.com";
    public static final String MD5 = ".md5";
    public static final String SHA1 = ".sha1";
    private static final int EXEC_MASK = 0111;
    private static final int BUFFER_SIZE = 1024;
    static final ExecutorService downloadPool = Executors.newFixedThreadPool(1); //Single thread!!
    public static final ExecutorService POOL = Executors.newFixedThreadPool(1);

    public enum FileType {
        ARTIFACT,
        SHA1,
        MD5
    }

    public enum ArtifactType {
        DOC,
        SRC
    }

    public static void downloadJavaDoc(ArtifactData data) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                GradleAndroidRepositoriesProvider provider = data.getProject().getLookup().lookup(GradleAndroidRepositoriesProvider.class);
                List<Repository> allRepositories = provider.getAllRepositories();
                for (Repository repo : allRepositories) {
                    downloadPool.execute(new RepoDownloadTask(repo.getUrl(), data, FileType.SHA1, ArtifactType.DOC));
                    downloadPool.execute(new RepoDownloadTask(repo.getUrl(), data, FileType.MD5, ArtifactType.DOC));
                    downloadPool.execute(new RepoDownloadTask(repo.getUrl(), data, FileType.ARTIFACT, ArtifactType.DOC));
                }
            }
        };
        downloadPool.execute(runnable);
    }

    public static void downloadSource(ArtifactData data) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                GradleAndroidRepositoriesProvider provider = data.getProject().getLookup().lookup(GradleAndroidRepositoriesProvider.class);
                List<Repository> allRepositories = provider.getAllRepositories();
                for (Repository repo : allRepositories) {
                    downloadPool.execute(new RepoDownloadTask(repo.getUrl(), data, FileType.SHA1, ArtifactType.SRC));
                    downloadPool.execute(new RepoDownloadTask(repo.getUrl(), data, FileType.MD5, ArtifactType.SRC));
                    downloadPool.execute(new RepoDownloadTask(repo.getUrl(), data, FileType.ARTIFACT, ArtifactType.SRC));
                }
            }
        };
        downloadPool.execute(runnable);
    }

    private static class RepoDownloadTask implements Runnable {

        private File f;
        private String url;
        private final ArtifactData data;
        private final ArtifactType artifactType;
        private final FileType fileType;
        private final String repoUrl;

        public RepoDownloadTask(String repoUrl, ArtifactData data, FileType fileType, ArtifactType artifactType) {
            this.artifactType = artifactType;
            this.fileType = fileType;
            this.data = data;
            this.repoUrl = repoUrl;

        }

        private void computeCoordinates(ArtifactType artifactType1, ArtifactData data1, FileType fileType1) throws AssertionError {
            String fileName;
            String path;
            String mavenLocation;
            switch (artifactType1) {
                case DOC:
                    path = data1.getJavaDocPath();
                    fileName = data1.getJavadocFileName();
                    break;
                case SRC:
                    fileName = data1.getSrcFileName();
                    path = data1.getSrcPath();
                    break;
                default:
                    throw new AssertionError(artifactType1.name());
            }
            mavenLocation = data1.getMavenLocation() + fileName;
            switch (fileType1) {
                case ARTIFACT:
                    this.f = new File(path);
                    break;
                case SHA1:
                    this.f = new File(path + SHA1);
                    mavenLocation = mavenLocation + SHA1;
                    break;
                case MD5:
                    this.f = new File(path + MD5);
                    mavenLocation = mavenLocation + MD5;
                    break;
                default:
                    throw new AssertionError(fileType1.name());
            }
            this.url = repoUrl + mavenLocation;
        }

        @Override
        public void run() {
            computeCoordinates(artifactType, data, fileType);
            if (f.exists()) {
                return;
            }
            try {
                f.getParentFile().mkdir();
                downloadFully(new URL(url), f);
            } catch (Exception ex) {
            }
            if (!f.exists()) {
                return;
            }
            if (data.isFromGradle() && fileType == FileType.SHA1) {
                try {
                    String dirName = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), "UTF-8");
                    File dir = new File(f.getParentFile().getAbsolutePath() + File.separator + dirName);
                    dir.mkdirs();
                    File sha1File = new File(dir.getAbsolutePath() + File.separator + f.getName());
                    FileUtils.copyFile(f, sha1File);
                    f.delete();
                    f = sha1File;
                    if (artifactType == ArtifactType.DOC) {
                        data.updateGradleCacheDocDir(dirName);
                    } else {
                        data.updateGradleCacheSrcDir(dirName);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (fileType == FileType.ARTIFACT) {
                if (artifactType == ArtifactType.DOC) {
                    data.setJavadocLocal(f.exists());
                } else {
                    data.setSrcLocal(f.exists());
                }
            }
        }

    }

    public static void downloadFully(URL url, File target) throws IOException {

        // We don't use the settings here explicitly, since HttpRequests picks up the network settings from studio directly.
        ProgressHandle handle = ProgressHandle.createHandle("Downloading " + url);
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setInstanceFollowRedirects(true);
                ((HttpsURLConnection) connection).setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; PPC; en-US; rv:1.3.1)");
                ((HttpsURLConnection) connection).setRequestProperty("Accept-Charset", "UTF-8");
                ((HttpsURLConnection) connection).setDoOutput(true);
                ((HttpsURLConnection) connection).setDoInput(true);
            }
            connection.setConnectTimeout(3000);
            connection.connect();
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                throw new FileNotFoundException();
            }
            handle.start(contentLength);
            OutputStream dest = new FileOutputStream(target);
            InputStream in = connection.getInputStream();
            int count;
            int done = 0;
            byte data[] = new byte[BUFFER_SIZE];
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                done += count;
                handle.progress(done);
                dest.write(data, 0, count);
            }
            dest.close();
            in.close();
        } finally {
            handle.finish();
            if (target.length() == 0) {
                try {
                    target.delete();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void downloadFully(URL url, File target, JProgressBar progressBar) throws IOException {

        // We don't use the settings here explicitly, since HttpRequests picks up the network settings from studio directly.
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setInstanceFollowRedirects(true);
                ((HttpsURLConnection) connection).setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; PPC; en-US; rv:1.3.1)");
                ((HttpsURLConnection) connection).setRequestProperty("Accept-Charset", "UTF-8");
                ((HttpsURLConnection) connection).setDoOutput(true);
                ((HttpsURLConnection) connection).setDoInput(true);
            }
            connection.setConnectTimeout(3000);
            connection.connect();
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                throw new FileNotFoundException();
            }
            if (progressBar != null) {
                progressBar.setMinimum(0);
                progressBar.setMaximum(contentLength);
            }
            OutputStream dest = new FileOutputStream(target);
            InputStream in = connection.getInputStream();
            int count;
            int done = 0;
            byte data[] = new byte[BUFFER_SIZE];
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                done += count;
                if (progressBar != null) {
                    progressBar.setValue(done);
                }
                dest.write(data, 0, count);
            }
            dest.close();
            in.close();
        } finally {
            if (target.length() == 0) {
                try {
                    target.delete();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void unzip(File zipFile, File destination)
            throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            Enumeration<ZipArchiveEntry> e = zip.getEntries();
            while (e.hasMoreElements()) {
                ZipArchiveEntry entry = e.nextElement();
                File file = new File(destination, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    InputStream is = zip.getInputStream(entry);
                    File parent = file.getParentFile();
                    if (parent != null && parent.exists() == false) {
                        parent.mkdirs();
                    }
                    FileOutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    } finally {
                        os.close();
                        is.close();
                    }
                    file.setLastModified(entry.getTime());
                    int mode = entry.getUnixMode();
                    if ((mode & EXEC_MASK) != 0) {
                        if (!file.setExecutable(true)) {
                        }
                    }
                }
            }
        } finally {
            ZipFile.closeQuietly(zip);
        }
    }

}
