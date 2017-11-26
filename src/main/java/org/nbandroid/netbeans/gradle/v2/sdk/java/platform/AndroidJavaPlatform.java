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
package org.nbandroid.netbeans.gradle.v2.sdk.java.platform;

import com.android.repository.api.UpdatablePackage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * AndroidJavaPlatform from Android UpdatablePackage
 *
 * @author arsi
 */
public class AndroidJavaPlatform extends JavaPlatform {

    private final UpdatablePackage pkg;
    private final FileObject binaryRoot;
    private final FileObject sourceDir;
    public static final String SOURCES_DIR = "sources";
    public static final String PLATFORMS_DIR = "platforms";
    private static final String JAR_FILE = "android.jar";
    private static final String JAR = "jar";
    private final Specification specification;
    private static final String DATA_FOLDER = "data";
    private static final String OPTIONAL_FOLDER = "optional";
    private final List<URL> standardPackages = new ArrayList<>();

    AndroidJavaPlatform(UpdatablePackage pkg, String javaVersion) {
        this.pkg = pkg;
        this.specification = new Specification("j2se", new SpecificationVersion(javaVersion));
        binaryRoot = FileUtil.toFileObject(pkg.getLocal().getLocation());
        sourceDir = sourceForJar(binaryRoot);
        FileObject dataDir = binaryRoot.getFileObject(DATA_FOLDER);
        if (dataDir != null) {
            Enumeration<? extends FileObject> children = dataDir.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject nextElement = children.nextElement();
                if (JAR.equals(nextElement.getExt())) {
                    standardPackages.add(FileUtil.urlForArchiveOrDir(FileUtil.toFile(nextElement)));
                }
            }
        }

        FileObject optionalDir = binaryRoot.getFileObject(OPTIONAL_FOLDER);
        if (optionalDir != null) {
            Enumeration<? extends FileObject> children = optionalDir.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject nextElement = children.nextElement();
                if (JAR.equals(nextElement.getExt())) {
                    standardPackages.add(FileUtil.urlForArchiveOrDir(FileUtil.toFile(nextElement)));
                }
            }
        }

        if (binaryRoot != null) {
            Enumeration<? extends FileObject> children = binaryRoot.getChildren(false);
            while (children.hasMoreElements()) {
                FileObject nextElement = children.nextElement();
                if (JAR.equals(nextElement.getExt())) {
                    standardPackages.add(FileUtil.urlForArchiveOrDir(FileUtil.toFile(nextElement)));
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

    @Override
    public String getDisplayName() {
        return pkg.getLocal().getDisplayName();
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        return ClassPathSupport.createClassPath(standardPackages.toArray(new URL[standardPackages.size()]));
    }

    @Override
    public ClassPath getStandardLibraries() {
        return ClassPath.EMPTY;
    }

    @Override
    public String getVendor() {
        return "Google Inc.";
    }

    @Override
    public Specification getSpecification() {
        return specification;
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public FileObject findTool(String toolName) {
        return null;
    }

    @Override
    public ClassPath getSourceFolders() {
        if (sourceDir != null) {
            return ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(FileUtil.toFile(sourceDir)));
        } else {
            return ClassPath.EMPTY;
        }
    }

    @Override
    public List<URL> getJavadocFolders() {
        return Collections.EMPTY_LIST;
    }

}
