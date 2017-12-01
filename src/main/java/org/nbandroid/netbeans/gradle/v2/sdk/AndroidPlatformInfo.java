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
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.repository.api.RepoPackage;
import com.android.repository.api.UpdatablePackage;
import com.android.repository.impl.meta.TypeDetails;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.repository.meta.DetailsTypes;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 *
 * @author arsi
 */
public class AndroidPlatformInfo {

    public static final String SOURCES_DIR = "sources";
    public static final String PLATFORMS_DIR = "platforms";
    public static final String JAR_FILE = "android.jar";
    public static final String JAR = "jar";
    public static final String DATA_FOLDER = "data";
    public static final String OPTIONAL_FOLDER = "optional";

    public final File platformFolder;
    public final String platformName;
    private final List<PathRecord> bootPaths = new ArrayList<>();
    private final List<PathRecord> srcPaths = new ArrayList<>();
    private final List<PathRecord> javadocPaths = new ArrayList<>();
    private final AndroidVersion androidVersion;

    public AndroidPlatformInfo(File platformFolder, String platformName, AndroidVersion androidVersion) {
        this.platformFolder = platformFolder;
        this.platformName = platformName;
        this.androidVersion = androidVersion;
    }

    public AndroidPlatformInfo(UpdatablePackage pkg) throws FileNotFoundException {
        this.platformFolder = pkg.getLocal().getLocation();
        this.platformName = pkg.getLocal().getDisplayName();
        RepoPackage p = pkg.getRepresentative();
        TypeDetails details = p.getTypeDetails();
        androidVersion = ((DetailsTypes.ApiDetailsType) details).getAndroidVersion();
        update();

    }

    public AndroidVersion getAndroidVersion() {
        return androidVersion;
    }

    public final void update() throws FileNotFoundException {
        Iterator<PathRecord> iterator = bootPaths.iterator();
        while (iterator.hasNext()) {
            PathRecord next = iterator.next();
            if (!next.isUserRecord()) {
                iterator.remove();
            }
        }
        iterator = srcPaths.iterator();
        while (iterator.hasNext()) {
            PathRecord next = iterator.next();
            if (!next.isUserRecord()) {
                iterator.remove();
            }
        }
        FileObject binaryRoot = FileUtil.toFileObject(platformFolder);
        if (binaryRoot == null) {
            throw new FileNotFoundException(platformFolder.getAbsolutePath());
        }
        FileObject sourceDir = sourceForJar(binaryRoot);
        if (sourceDir != null) {
            addSrcPath(FileUtil.urlForArchiveOrDir(FileUtil.toFile(sourceDir)), false);
        }
        FileObject dataDir = binaryRoot.getFileObject(DATA_FOLDER);
        if (dataDir != null) {
            Enumeration<? extends FileObject> children = dataDir.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject nextElement = children.nextElement();
                if (JAR.equals(nextElement.getExt())) {
                    addBootPath(FileUtil.urlForArchiveOrDir(FileUtil.toFile(nextElement)), false);
                }
            }
        }

        FileObject optionalDir = binaryRoot.getFileObject(OPTIONAL_FOLDER);
        if (optionalDir != null) {
            Enumeration<? extends FileObject> children = optionalDir.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject nextElement = children.nextElement();
                if (JAR.equals(nextElement.getExt())) {
                    addBootPath(FileUtil.urlForArchiveOrDir(FileUtil.toFile(nextElement)), false);
                }
            }
        }

        Enumeration<? extends FileObject> children = binaryRoot.getChildren(false);
        while (children.hasMoreElements()) {
            FileObject nextElement = children.nextElement();
            if (JAR.equals(nextElement.getExt())) {
                if (!"android-stubs-src.jar".equalsIgnoreCase(nextElement.getNameExt())) {
                    addBootPath(FileUtil.urlForArchiveOrDir(FileUtil.toFile(nextElement)), false);
                } else {
                    FileObject archiveRoot = FileUtil.getArchiveRoot(nextElement);
                    FileObject src = archiveRoot.getFileObject("src");
                    if (src != null) {
                        addSrcPath(src.toURL(), false);
                    }
                }
            }
        }
    }

    public static FileObject sourceForJar(FileObject binaryRoot) {
        FileObject dir = binaryRoot;
        if (dir == null) {
            return null;
        }
        String platformName = dir.getName();
        dir = dir.getParent();
        if (dir == null || !PLATFORMS_DIR.equals(dir.getName())) {
            return null;
        }
        dir = dir.getParent();
        if (dir == null) {
            return null;
        }
        dir = dir.getFileObject(SOURCES_DIR);
        if (dir == null) {
            return null;
        }
        dir = dir.getFileObject(platformName);
        if (dir == null) {
            return null;
        }
        return dir;
    }

    public List<PathRecord> getBootPaths() {
        return bootPaths;
    }

    public URL[] getBootURLs() {
        List<URL> tmp = new ArrayList<>();
        for (PathRecord bootPath : bootPaths) {
            tmp.add(bootPath.getUrl());
        }
        return tmp.toArray(new URL[tmp.size()]);
    }

    public URL[] getSrcURLs() {
        List<URL> tmp = new ArrayList<>();
        for (PathRecord bootPath : srcPaths) {
            tmp.add(bootPath.getUrl());
        }
        return tmp.toArray(new URL[tmp.size()]);
    }

    public List<URL> getJavadocURLs() {
        List<URL> tmp = new ArrayList<>();
        for (PathRecord bootPath : javadocPaths) {
            tmp.add(bootPath.getUrl());
        }
        return tmp;
    }

    public List<PathRecord> getSrcPaths() {
        return srcPaths;
    }

    public List<PathRecord> getJavadocPaths() {
        return javadocPaths;
    }

    public List<PathRecord> getSystemBootPaths() {
        return bootPaths.stream().filter(p -> !p.userRecord).collect(Collectors.toList());
    }

    public List<PathRecord> getSystemSrcPaths() {
        return srcPaths.stream().filter(p -> !p.userRecord).collect(Collectors.toList());
    }

    public List<PathRecord> getSystemJavadocPaths() {
        return javadocPaths.stream().filter(p -> !p.userRecord).collect(Collectors.toList());
    }

    public List<PathRecord> getUserBootPaths() {
        return bootPaths.stream().filter(p -> p.userRecord).collect(Collectors.toList());
    }

    public List<PathRecord> getUserSrcPaths() {
        return srcPaths.stream().filter(p -> p.userRecord).collect(Collectors.toList());
    }

    public List<PathRecord> getUserJavadocPaths() {
        return javadocPaths.stream().filter(p -> p.userRecord).collect(Collectors.toList());
    }

    public final boolean addBootPath(URL url, boolean user) {
        PathRecord record = new PathRecord(user, url);
        if (bootPaths.contains(record)) {
            return false;
        } else {
            bootPaths.add(record);
            return true;
        }
    }

    public boolean addBootPath(PathRecord record) {
        if (bootPaths.contains(record)) {
            return false;
        } else {
            bootPaths.add(record);
            return true;
        }
    }

    public void removeBootPath(PathRecord record) {
        bootPaths.remove(record);
    }

    public final boolean addSrcPath(URL url, boolean user) {
        PathRecord record = new PathRecord(user, url);
        if (srcPaths.contains(record)) {
            return false;
        } else {
            srcPaths.add(record);
            return true;
        }
    }

    public boolean addSrcPath(PathRecord record) {
        if (srcPaths.contains(record)) {
            return false;
        } else {
            srcPaths.add(record);
            return true;
        }
    }

    public void removeSrcPath(PathRecord record) {
        srcPaths.remove(record);
    }

    public boolean addJavadocPath(URL url, boolean user) {
        PathRecord record = new PathRecord(user, url);
        if (javadocPaths.contains(record)) {
            return false;
        } else {
            javadocPaths.add(record);
            return true;
        }
    }

    public boolean addJavadocPath(PathRecord record) {
        if (javadocPaths.contains(record)) {
            return false;
        } else {
            javadocPaths.add(record);
            return true;
        }
    }

    public void removeJavadocPath(PathRecord record) {
        javadocPaths.remove(record);
    }

    public File getPlatformFolder() {
        return platformFolder;
    }

    public String getPlatformName() {
        return platformName;
    }

    void addBootPaths(List<PathRecord> paths) {
        bootPaths.addAll(paths);
    }

    void addSrcPaths(List<PathRecord> paths) {
        srcPaths.addAll(paths);
    }

    void addJavaDocPaths(List<PathRecord> paths) {
        javadocPaths.addAll(paths);
    }

    public static class PathRecord {

        private final boolean userRecord;
        private final URL url;

        public PathRecord(boolean userRecord, URL path) {
            this.userRecord = userRecord;
            this.url = path;
        }

        public boolean isUserRecord() {
            return userRecord;
        }

        public URL getUrl() {
            return url;
        }

        @Override
        public String toString() {
            String begin = "<html>";
            String end = "</html>";
            if (!userRecord) {
                begin += "<b>";
                end = "</b>" + end;
            }
            URL tmp = url;
            if ("jar".equals(tmp.getProtocol())) {      //NOI18N
                URL fileURL = FileUtil.getArchiveFile(tmp);
                if (FileUtil.getArchiveRoot(fileURL).equals(tmp)) {
                    // really the root
                    tmp = fileURL;
                } else {
                    // some subdir, just show it as is
                    FileObject fo = URLMapper.findFileObject(tmp);
                    if (fo == null || !fo.isValid()) {
                        begin += "<font color=#DF0101>";
                        end = "</font>" + end;
                    }
                    return begin + tmp.toExternalForm() + end;
                }
            }
            if ("file".equals(tmp.getProtocol())) {
                File f = Utilities.toFile(URI.create(tmp.toExternalForm()));
                if (!f.exists()) {
                    begin += "<font color=#DF0101>";
                    end = "</font>" + end;
                }
                return begin + f.getAbsolutePath() + end;
            } else {
                return begin + tmp.toExternalForm() + end;
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 31 * hash + Objects.hashCode(this.url);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PathRecord other = (PathRecord) obj;
            if (!Objects.equals(this.url, other.url)) {
                return false;
            }
            return true;
        }

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.platformFolder);
        hash = 17 * hash + Objects.hashCode(this.platformName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AndroidPlatformInfo other = (AndroidPlatformInfo) obj;
        if (!Objects.equals(this.platformName, other.platformName)) {
            return false;
        }
        if (!Objects.equals(this.platformFolder, other.platformFolder)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return platformName; //To change body of generated methods, choose Tools | Templates.
    }

}
