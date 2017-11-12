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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import org.netbeans.api.progress.ProgressHandle;

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
    private static final int BUFFER_SIZE = 1024;
    private static final ExecutorService downloadPool = Executors.newFixedThreadPool(1);

    public static void downloadJavaDoc(ArtifactData data) {

        String javadocFileName = data.getJavadocFileName();
        String mavenLocation = data.getMavenLocation();
        File f = new File("/tmp/" + javadocFileName + MD5);
        downloadFromRepo(f, ANDROID_REPO + mavenLocation + javadocFileName);

    }

    public static void downloadSource(ArtifactData data) {
        String srcFileName = data.getSrcFileName();
        String mavenLocation = data.getMavenLocation();
        File f = new File("/tmp/" + srcFileName + MD5);
        downloadFromRepo(f, ANDROID_REPO + mavenLocation + srcFileName);
    }

    private static boolean downloadFromRepo(File f, String url) {
        try {
            downloadFully(new URL(url), f);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private static void downloadFully(URL url, File target) throws IOException {

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
        }
    }

}
