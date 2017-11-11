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
package org.nbandroid.netbeans.gradle.v2.nodes;

import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.Library;
import java.awt.Image;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import org.gradle.impldep.org.apache.commons.io.FilenameUtils;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
import org.netbeans.api.project.Project;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
public class DependencyNode extends FilterNode {

    private final Library library;
    public static final String JAVADOC_NAME = "-javadoc.jar";
    public static final String SRC_NAME = "-sources.jar";
    public static final String EXTRAS = "extras";
    public static final String ANDROID = "android";
    public static final String M2REPOSITORY = "m2repository";
    public static final String GOOGLE = "google";
    public static final String GRADLE_CACHES = "caches";
    public static final String GRADLE_REPO = "files-2.1";
    public static final String GRADLE_MODULES = "modules-2";
    public static final String EXTRAS_ANDROID_M2 = EXTRAS + File.separator + ANDROID + File.separator + M2REPOSITORY;
    public static final String EXTRAS_GOOGLE_M2 = EXTRAS + File.separator + GOOGLE + File.separator + M2REPOSITORY;
    public static final String EXTRAS_M2 = EXTRAS + File.separator + M2REPOSITORY;
    public static final String GRADLE_STORE = GRADLE_CACHES + File.separator + GRADLE_MODULES + File.separator + GRADLE_REPO;
    private final Data data;

    private static Lookup createLookup(Library library, Project project) {
        return Lookups.fixed(new Data(library, project));
    }

    public static class Data {

        private final AtomicBoolean javadocLocal = new AtomicBoolean(false);
        private final AtomicBoolean srcLocal = new AtomicBoolean(false);
        private final AtomicBoolean local = new AtomicBoolean(false);
        private final Library library;
        private final Project project;
        private final String mavenLocation;
        private final String javadocFileName;
        private final String srcFileName;

        public Data(Library library, Project project) {
            this.library = library;
            this.project = project;
            this.mavenLocation = getMavenLocation();
            this.javadocFileName = getJavadocFileName();
            this.srcFileName = getSrcFileName();
        }

        private String getJavadocFileName() {
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

        private String getSrcFileName() {
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

        private String getMavenLocation() {
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
            return "";
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

    }

    public Library getLibrary() {
        return library;
    }

    public DependencyNode(Node original, Library library, Project project) {
        super(original, new Children(original), createLookup(library, project));
        this.library = library;
        this.data = getLookup().lookup(Data.class);
    }

    @Override
    public Image getIcon(int type) {
        Image icon = super.getIcon(type);
        if (library instanceof AndroidLibrary) {
            AndroidLibrary lib = (AndroidLibrary) library;
            icon = IconProvider.IMG_ANDROID_LIBRARY;
            File jarFile = lib.getJarFile();
            icon = annotateBroken(jarFile, icon);
            File bundle = lib.getBundle();
            icon = annotateSrcAndJavaDoc(bundle, icon);
        } else if (library instanceof JavaLibrary) {
            JavaLibrary lib = (JavaLibrary) library;
            File jarFile = lib.getJarFile();
            icon = annotateBroken(jarFile, icon);
            icon = annotateSrcAndJavaDoc(jarFile, icon);
        }
        return icon;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> actionsForPath = Utilities.actionsForPath("Actions/NbAndroid/Dependency");
        return actionsForPath.toArray(new Action[actionsForPath.size()]);
    }

    private Image annotateSrcAndJavaDoc(File bundle, Image icon) {
        if (bundle != null) {
            File bundleRoot = bundle.getParentFile();
            if (bundleRoot != null) {
                String javaDoc = data.javadocFileName;
                String src = data.srcFileName;
                if (data.isFromSdk()) {
                    if (new File(bundleRoot, javaDoc).exists()) {
                        icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVADOCINCLUDED, 12, 0);
                        data.javadocLocal.set(true);
                    } else {
                        data.javadocLocal.set(false);
                    }
                    if (new File(bundleRoot, src).exists()) {
                        icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVASRCINCLUDED, 12, 8);
                        data.srcLocal.set(true);
                    } else {
                        data.srcLocal.set(false);
                    }
                } else if (data.isFromGradle()) {
                    data.javadocLocal.set(false);
                    data.srcLocal.set(false);
                    bundleRoot = bundleRoot.getParentFile();
                    if (bundleRoot != null && bundleRoot.isDirectory()) {
                        File[] listFiles = bundleRoot.listFiles();
                        for (File f : listFiles) {
                            if (f.isDirectory()) {
                                if (new File(f, javaDoc).exists()) {
                                    icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVADOCINCLUDED, 12, 0);
                                    data.javadocLocal.set(true);
                                    break;
                                }
                                if (new File(f, src).exists()) {
                                    icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVASRCINCLUDED, 12, 8);
                                    data.srcLocal.set(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return icon;
    }

    private Image annotateBroken(File jarFile, Image icon) {
        if (jarFile != null && !jarFile.exists()) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_BROKEN, 0, 0);
        } else if (jarFile == null) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_BROKEN, 0, 0);
        }
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        String versionlessId = library.getResolvedCoordinates().getVersionlessId();
        if (versionlessId.contains("_local_jars_")) {
            data.local.set(true);
            int lastIndexOf = versionlessId.lastIndexOf('/');
            if (lastIndexOf == -1) {
                lastIndexOf = versionlessId.lastIndexOf('\\');
            }
            if (lastIndexOf > -1) {
                versionlessId = versionlessId.substring(lastIndexOf + 1);
            }
        } else {
            data.local.set(false);
        }
        return versionlessId;
    }

}
