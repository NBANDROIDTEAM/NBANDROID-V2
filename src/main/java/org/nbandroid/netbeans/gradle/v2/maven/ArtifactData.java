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
import java.util.Collection;
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
 * Dependency detail info for DependencyNode
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
    private String javaDocPath;
    private String srcPath;
    private File gradleArtifactRoot;

    public ArtifactData(Library library, Project project) {
        this.library = library;
        this.project = project;
        Collection<? extends Object> lookupAll = project.getLookup().lookupAll(Object.class);
        this.mavenLocation = makeMavenLocation();
        this.javadocFileName = makeJavadocFileName();
        this.srcFileName = makeSrcFileName();
        analyzeArtifact();
    }

    /**
     * Exist the dependency file?
     *
     * @return
     */
    public boolean isBroken() {
        return broken.get();
    }

    /**
     * Get maven location /com/android/
     *
     * @return
     */
    public String getMavenLocation() {
        return mavenLocation;
    }

    /**
     * Get javadoc filename artifact-javadoc.jar
     *
     * @return
     */
    public String getJavadocFileName() {
        return javadocFileName;
    }

    /**
     * Get src file name artifact-sources.jar
     *
     * @return
     */
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

    /**
     * is javadoc downloaded?
     *
     * @return
     */
    public boolean isJavadocLocal() {
        return javadocLocal.get();
    }

    /**
     * is src downloaded?
     *
     * @return
     */
    public boolean isSrcLocal() {
        return srcLocal.get();
    }

    /**
     * Get library
     *
     * @return
     */
    public Library getLibrary() {
        return library;
    }

    /**
     * get Project
     *
     * @return
     */
    public Project getProject() {
        return project;
    }

    /**
     * is local project dependency file?
     *
     * @return
     */
    public boolean isLocal() {
        return local.get();
    }

    /**
     * is from SDK_DIR/extras/android/m2repository
     *
     * @return
     */
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

    /**
     * is from SDK_DIR/extras/google/m2repository
     *
     * @return
     */
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

    /**
     * is from SDK_DIR/extras/m2repository
     *
     * @return
     */
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

    /**
     * is from gradle cache
     *
     * @return
     */
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

    /**
     * is from SDK dir
     *
     * @return
     */
    public boolean isFromSdk() {
        return isFromAndroidSdk() || isFromGoogleSdk() || isFromMavenSdk();
    }

    /**
     * isFromAndroidSdk() || isFromGoogleSdk() || isFromMavenSdk() ||
     * isFromGradle()
     *
     * @return
     */
    public boolean isDownloadable() {
        return isFromAndroidSdk() || isFromGoogleSdk() || isFromMavenSdk() || isFromGradle();
    }

    /**
     * is AndroidLibrary
     *
     * @return
     */
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
                    File doc = new File(bundleRoot, javaDoc);
                    javaDocPath = doc.getAbsolutePath();
                    if (doc.exists()) {
                        javadocLocal.set(true);
                    } else {
                        javadocLocal.set(false);
                    }
                    File srcFile = new File(bundleRoot, src);
                    srcPath = srcFile.getAbsolutePath();
                    if (srcFile.exists()) {
                        srcLocal.set(true);
                    } else {
                        srcLocal.set(false);
                    }
                } else if (isFromGradle()) {
                    javadocLocal.set(false);
                    srcLocal.set(false);
                    bundleRoot = bundleRoot.getParentFile();
                    if (bundleRoot != null && bundleRoot.isDirectory()) {
                        gradleArtifactRoot = bundleRoot;
                        File[] listFiles = bundleRoot.listFiles();
                        for (File f : listFiles) {
                            if (f.isDirectory()) {
                                File doc = new File(f, javaDoc);
                                if (doc.exists()) {
                                    javadocLocal.set(true);
                                    javaDocPath = doc.getAbsolutePath();
                                    break;
                                }
                                File srcFile = new File(f, src);
                                if (srcFile.exists()) {
                                    srcLocal.set(true);
                                    srcPath = srcFile.getAbsolutePath();
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

    /**
     * Annotate icon: broken/javadoc/src
     *
     * @param icon
     * @return
     */
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

    /**
     * Get absolute path to javadoc jar for gradle if is no local javadoc
     * returns null
     *
     * @return
     */
    public String getJavaDocPath() {
        return javaDocPath;
    }

    /**
     * Get absolute path to src jar for gradle if is no local src returns null
     *
     * @return
     */
    public String getSrcPath() {
        return srcPath;
    }

    /**
     * For gradle artifact returns it's root folder
     *
     * @return
     */
    public File getGradleArtifactRoot() {
        return gradleArtifactRoot;
    }

}
