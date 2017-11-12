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

import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.Library;
import java.awt.Image;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gradle.impldep.org.apache.commons.io.FilenameUtils;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
import static org.nbandroid.netbeans.gradle.v2.nodes.DependencyNode.EXTRAS_ANDROID_M2;
import static org.nbandroid.netbeans.gradle.v2.nodes.DependencyNode.EXTRAS_GOOGLE_M2;
import static org.nbandroid.netbeans.gradle.v2.nodes.DependencyNode.EXTRAS_M2;
import static org.nbandroid.netbeans.gradle.v2.nodes.DependencyNode.GRADLE_STORE;
import static org.nbandroid.netbeans.gradle.v2.nodes.DependencyNode.JAVADOC_NAME;
import static org.nbandroid.netbeans.gradle.v2.nodes.DependencyNode.SRC_NAME;
import org.netbeans.api.project.Project;
import org.openide.util.ImageUtilities;

/**
 *
 * @author arsi
 */
public class ArtifactData {

    private final AtomicBoolean javadocLocal = new AtomicBoolean(false);
    private final AtomicBoolean srcLocal = new AtomicBoolean(false);
    private final AtomicBoolean local = new AtomicBoolean(false);
    private final AtomicBoolean broken = new AtomicBoolean(false);
    private final Library library;
    private final Project project;
    private final String mavenLocation;
    private final String javadocFileName;
    private final String srcFileName;

    public ArtifactData(Library library, Project project) {
        this.library = library;
        this.project = project;
        this.mavenLocation = makeMavenLocation();
        this.javadocFileName = makeJavadocFileName();
        this.srcFileName = makeSrcFileName();
        analyzeArtifact();
    }

    public boolean isBroken() {
        return broken.get();
    }

    public String getMavenLocation() {
        return mavenLocation;
    }

    public String getJavadocFileName() {
        return javadocFileName;
    }

    public String getSrcFileName() {
        return srcFileName;
    }

    private String makeJavadocFileName() {
        File f;
        if (library instanceof AndroidLibrary) {
            f = ((AndroidLibrary) library).getBundle();
        } else {
            f = ((JavaLibrary) library).getJarFile();
        }
        String name = f.getName();
        String baseName = FilenameUtils.getBaseName(name);
        return baseName + JAVADOC_NAME;
    }

    private String makeSrcFileName() {
        File f;
        if (library instanceof AndroidLibrary) {
            f = ((AndroidLibrary) library).getBundle();
        } else {
            f = ((JavaLibrary) library).getJarFile();
        }
        String name = f.getName();
        String baseName = FilenameUtils.getBaseName(name);
        return baseName + SRC_NAME;
    }

    private String makeMavenLocation() {
        File f;
        if (library instanceof AndroidLibrary) {
            f = ((AndroidLibrary) library).getBundle();
        } else {
            f = ((JavaLibrary) library).getJarFile();
        }
        String name = f.getName();
        String path = f.getPath();
        if (isFromAndroidSdk()) {
            int indexOf = path.indexOf(EXTRAS_ANDROID_M2);
            if (indexOf > -1) {
                path = path.substring(indexOf + EXTRAS_ANDROID_M2.length());
            } else {
                return null;
            }
        } else if (isFromGoogleSdk()) {
            int indexOf = path.indexOf(EXTRAS_GOOGLE_M2);
            if (indexOf > -1) {
                path = path.substring(indexOf + EXTRAS_GOOGLE_M2.length());
            } else {
                return null;
            }

        } else if (isFromMavenSdk()) {
            int indexOf = path.indexOf(EXTRAS_M2);
            if (indexOf > -1) {
                path = path.substring(indexOf + EXTRAS_M2.length());
            } else {
                return null;
            }
        } else if (isFromGradle()) {
            int indexOf = path.indexOf(GRADLE_STORE);
            if (indexOf > -1) {
                path = path.substring(indexOf + GRADLE_STORE.length());
            } else {
                return null;
            }
        }
        path = path.replace(name, "");
        path = path.replace('\\', '/');
        return path;
    }

    public boolean isJavadocLocal() {
        return javadocLocal.get();
    }

    public boolean isSrcLocal() {
        return srcLocal.get();
    }

    public Library getLibrary() {
        return library;
    }

    public Project getProject() {
        return project;
    }

    public boolean isLocal() {
        return local.get();
    }

    public boolean isFromAndroidSdk() {
        if (library instanceof AndroidLibrary) {
            AndroidLibrary lib = (AndroidLibrary) library;
            File bundle = lib.getBundle();
            if (bundle != null && bundle.getPath().contains(EXTRAS_ANDROID_M2)) {
                return true;
            }
        } else if (library instanceof JavaLibrary) {
            JavaLibrary lib = (JavaLibrary) library;
            File bundle = lib.getJarFile();
            if (bundle != null && bundle.getPath().contains(EXTRAS_ANDROID_M2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFromGoogleSdk() {
        if (library instanceof AndroidLibrary) {
            AndroidLibrary lib = (AndroidLibrary) library;
            File bundle = lib.getBundle();
            if (bundle != null && bundle.getPath().contains(EXTRAS_GOOGLE_M2)) {
                return true;
            }
        } else if (library instanceof JavaLibrary) {
            JavaLibrary lib = (JavaLibrary) library;
            File bundle = lib.getJarFile();
            if (bundle != null && bundle.getPath().contains(EXTRAS_GOOGLE_M2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFromMavenSdk() {
        if (library instanceof AndroidLibrary) {
            AndroidLibrary lib = (AndroidLibrary) library;
            File bundle = lib.getBundle();
            if (bundle != null && bundle.getPath().contains(EXTRAS_M2)) {
                return true;
            }
        } else if (library instanceof JavaLibrary) {
            JavaLibrary lib = (JavaLibrary) library;
            File bundle = lib.getJarFile();
            if (bundle != null && bundle.getPath().contains(EXTRAS_M2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFromGradle() {
        if (library instanceof AndroidLibrary) {
            AndroidLibrary lib = (AndroidLibrary) library;
            File bundle = lib.getBundle();
            if (bundle != null && bundle.getPath().contains(GRADLE_STORE)) {
                return true;
            }
        } else if (library instanceof JavaLibrary) {
            JavaLibrary lib = (JavaLibrary) library;
            File bundle = lib.getJarFile();
            if (bundle != null && bundle.getPath().contains(GRADLE_STORE)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFromSdk() {
        return isFromAndroidSdk() || isFromGoogleSdk() || isFromMavenSdk();
    }

    public boolean isDownloadable() {
        return isFromAndroidSdk() || isFromGoogleSdk() || isFromMavenSdk() || isFromGradle();
    }

    public boolean isAndroidLibrary() {
        return library instanceof AndroidLibrary;
    }

    private void analyzeArtifact() {
        File bundle = null;
        if (library instanceof AndroidLibrary) {
            AndroidLibrary lib = (AndroidLibrary) library;
            bundle = lib.getBundle();
        } else if (library instanceof JavaLibrary) {
            JavaLibrary lib = (JavaLibrary) library;
            bundle = lib.getJarFile();
        }
        if (bundle != null) {
            File bundleRoot = bundle.getParentFile();
            if (bundleRoot != null) {
                String javaDoc = getJavadocFileName();
                String src = getSrcFileName();
                if (isFromSdk()) {
                    if (new File(bundleRoot, javaDoc).exists()) {
                        javadocLocal.set(true);
                    } else {
                        javadocLocal.set(false);
                    }
                    if (new File(bundleRoot, src).exists()) {
                        srcLocal.set(true);
                    } else {
                        srcLocal.set(false);
                    }
                } else if (isFromGradle()) {
                    javadocLocal.set(false);
                    srcLocal.set(false);
                    bundleRoot = bundleRoot.getParentFile();
                    if (bundleRoot != null && bundleRoot.isDirectory()) {
                        File[] listFiles = bundleRoot.listFiles();
                        for (File f : listFiles) {
                            if (f.isDirectory()) {
                                if (new File(f, javaDoc).exists()) {
                                    javadocLocal.set(true);
                                    break;
                                }
                                if (new File(f, src).exists()) {
                                    srcLocal.set(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (library instanceof AndroidLibrary) {
                AndroidLibrary lib = (AndroidLibrary) library;
                bundle = lib.getJarFile();
            }
            broken.set(!bundle.exists());
            String versionlessId = library.getResolvedCoordinates().getVersionlessId();
            local.set(versionlessId.contains("_local_jars_"));
        }
    }

    public Image getIcon(Image icon) {
        icon = annotateBroken(icon);
        icon = annotateSrcAndJavaDoc(icon);
        return icon;
    }

    private Image annotateSrcAndJavaDoc(Image icon) {
        if (isJavadocLocal()) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVADOCINCLUDED, 12, 0);
        }
        if (isSrcLocal()) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVASRCINCLUDED, 12, 8);
        }
        return icon;
    }

    private Image annotateBroken(Image icon) {
        if (isBroken()) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_BROKEN, 0, 0);
        }
        return icon;
    }

}
