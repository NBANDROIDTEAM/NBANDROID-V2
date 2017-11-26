/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author arsi
 */
public class AndroidPlatformInfo {

    public final File platformFolder;
    public final String platformName;
    private final List<PathRecord> bootPaths = new ArrayList<>();
    private final List<PathRecord> srcPaths = new ArrayList<>();
    private final List<PathRecord> javadocPaths = new ArrayList<>();

    public AndroidPlatformInfo(File platformFolder, String platformName, List<PathRecord> bootPaths) {
        this.platformFolder = platformFolder;
        this.platformName = platformName;
        this.bootPaths.addAll(bootPaths);
    }

    public AndroidPlatformInfo(File platformFolder, String platformName, List<PathRecord> bootPaths, List<PathRecord> srcPaths) {
        this(platformFolder, platformName, bootPaths);
        this.srcPaths.addAll(srcPaths);
    }

    public AndroidPlatformInfo(File platformFolder, String platformName, List<PathRecord> bootPaths, List<PathRecord> srcPaths, List<PathRecord> javadocPaths) {
        this(platformFolder, platformName, bootPaths, srcPaths);
        this.javadocPaths.addAll(javadocPaths);
    }

    public List<PathRecord> getBootPaths() {
        return bootPaths;
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

    public boolean addBootPath(String path, boolean user) {
        PathRecord record = new PathRecord(user, new File(path));
        if (bootPaths.contains(record)) {
            return false;
        } else {
            bootPaths.add(record);
            return true;
        }
    }

    public boolean addBootPath(File path, boolean user) {
        PathRecord record = new PathRecord(user, path);
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

    public boolean addSrcPath(String path, boolean user) {
        PathRecord record = new PathRecord(user, new File(path));
        if (srcPaths.contains(record)) {
            return false;
        } else {
            srcPaths.add(record);
            return true;
        }
    }

    public boolean addSrcPath(File path, boolean user) {
        PathRecord record = new PathRecord(user, path);
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

    public boolean addJavadocPath(String path, boolean user) {
        PathRecord record = new PathRecord(user, new File(path));
        if (javadocPaths.contains(record)) {
            return false;
        } else {
            javadocPaths.add(record);
            return true;
        }
    }

    public boolean addJavadocPath(File path, boolean user) {
        PathRecord record = new PathRecord(user, path);
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

    public File getPlatformFolder() {
        return platformFolder;
    }

    public String getPlatformName() {
        return platformName;
    }

    public class PathRecord {

        private final boolean userRecord;
        private final File path;

        public PathRecord(boolean userRecord, File path) {
            this.userRecord = userRecord;
            this.path = path;
        }

        public boolean isUserRecord() {
            return userRecord;
        }

        public File getPath() {
            return path;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 31 * hash + Objects.hashCode(this.path);
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
            if (!Objects.equals(this.path, other.path)) {
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

}
