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
package org.nbandroid.netbeans.gradle.v2.layout;

import com.android.builder.model.AndroidProject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.nbandroid.netbeans.gradle.v2.layout.completion.analyzer.StyleableResultCollector;
import org.nbandroid.netbeans.gradle.v2.layout.parsers.StyleableXmlParser;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatformProvider;
import static org.nbandroid.netbeans.gradle.v2.sdk.ui.SDKVisualPanel2Download.NBANDROID_FOLDER;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.NbAndroidProject;
import org.netbeans.modules.android.project.query.AndroidClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class AndroidStyleableStore {

    public static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
    public static final String LIB_NAMESPACE = "LIB_NAMESPACE";
    public static final String RES_AUTO_NAMESPACE = "http://schemas.android.com/apk/res-auto";
    public static final String TOOLS_NAMESPACE = "http://schemas.android.com/tools";
    private static final List<AndroidStyleableAttrEnum> ATTR_ENUMS = new ArrayList<>();
    private static final List<AndroidStyleableAttrFlag> ATTR_FLAGS = new ArrayList<>();
    private static final List<AndroidStyleableAttr> STYLEABLE_ATTRS = new ArrayList<>();
    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static final Map<String, AndroidStyleableNamespace> PLATFORM_STYLEABLE_NAMESPACES_MAP = new HashMap<>();
    private static final Map<String, AndroidStyleableNamespace> LIBS_STYLEABLE_NAMESPACES_MAP = new HashMap<>();
    private static final String STYLEABLE_CACHE_FILENAME_STRING = "platformStyleableCache.obj";
    private static final File cacheSubfile = Places.getCacheSubfile(NBANDROID_FOLDER + STYLEABLE_CACHE_FILENAME_STRING);
    public static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(1);
    private static final AtomicBoolean saveFlag = new AtomicBoolean(false);

    static {
        if (cacheSubfile.exists()) {
            try (ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(cacheSubfile))) {
                Map<String, AndroidStyleableNamespace> cacheObj = (Map<String, AndroidStyleableNamespace>) objStream.readObject();
                PLATFORM_STYLEABLE_NAMESPACES_MAP.putAll(cacheObj);
                removeInvalidPlatforms();
            } catch (IOException | ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static Map<String, AndroidStyleableNamespace> findNamespaces(FileObject primaryFile) {
        LOCK.lock();
        try {
//            PLATFORM_STYLEABLE_NAMESPACES_MAP.clear();
//            LIBS_STYLEABLE_NAMESPACES_MAP.clear();
            Map<String, AndroidStyleableNamespace> namespaces = new HashMap<>();
            Project owner = FileOwnerQuery.getOwner(primaryFile);
            if (owner instanceof NbAndroidProject) {
                AndroidStyleableNamespace platformNamespace = null;
                AndroidProject androidProject = owner.getLookup().lookup(AndroidProject.class);
                if (androidProject != null) {
                    String next = androidProject.getBootClasspath().iterator().next();
                    AndroidJavaPlatform platform = AndroidJavaPlatformProvider.findPlatform(next, androidProject.getCompileTarget());
                    if (platform != null) {
                        AndroidStyleableNamespace namespace = getPlatformStyleableNamespace(platform);
                        if (namespace != null) {
                            platformNamespace = namespace;
                            namespaces.put(namespace.getNamespace(), namespace);
                        }
                    }
                }
                AndroidClassPathProvider classPathProvider = owner.getLookup().lookup(AndroidClassPathProvider.class);
                if (classPathProvider != null && platformNamespace != null) {
                    AndroidStyleableNamespace namespace = new AndroidStyleableNamespace(RES_AUTO_NAMESPACE, null);
                    namespaces.put(namespace.getNamespace(), namespace);
                    List<ClassPath.Entry> entries = classPathProvider.getCompilePath().entries();
                    for (ClassPath.Entry entrie : entries) {
                        FileObject root = entrie.getRoot();
                        FileObject jarOrAarDir = FileUtil.getArchiveFile(root);
                        if (jarOrAarDir.getPath().contains(".aar")) {
                            //exploded arr structure
                            //find root folder
                            FileObject parent = jarOrAarDir.getParent();
                            FileObject explodedRoot = null;
                            while (parent != null && !parent.getPath().endsWith(".aar")) {
                                explodedRoot = parent;
                                parent = parent.getParent();
                            }
                            handleExplodedRoot(explodedRoot, jarOrAarDir, platformNamespace, root, namespace);

                        }else if ("classes.jar".equalsIgnoreCase(jarOrAarDir.getNameExt())){
                            //handle new exploded aar structure
                            FileObject explodedRoot = null;
                            explodedRoot = jarOrAarDir.getParent();
                            if(explodedRoot!=null){
                                explodedRoot = explodedRoot.getParent();
                                if(explodedRoot!=null){
                                    handleExplodedRoot(explodedRoot, jarOrAarDir, platformNamespace, root, namespace);
                                }
                            }
                        }
                    }
                }
            }
            List<AndroidStyleableNamespace> todos = new ArrayList<>();
            for (Map.Entry<String, AndroidStyleableNamespace> entry : namespaces.entrySet()) {
                AndroidStyleableNamespace namespace = entry.getValue();
                if (namespace.hasTodo()) {
                    todos.add(namespace);
                }
            }
            if (!todos.isEmpty()) {
                Map<String, StyleableResultCollector> fullNameClassMap = new HashMap<>();
                Map<String, AndroidStyleable> fullStyleableMap = new HashMap<>();
                for (Map.Entry<String, AndroidStyleableNamespace> entry : namespaces.entrySet()) {
                    AndroidStyleableNamespace namespace = entry.getValue();
                    namespace.addAllFullClassNamesTo(fullNameClassMap);
                    namespace.addAllStyleablesTo(fullStyleableMap);
                }
                for (AndroidStyleableNamespace namespace : todos) {
                    Iterator<AndroidStyleable> iterator = namespace.getTodo().iterator();
                    while (iterator.hasNext()) {
                        AndroidStyleable styleable = iterator.next();
                        AndroidStyleable superStyleable = fullStyleableMap.get(styleable.getSuperStyleableName());
                        if (superStyleable != null) {
                            styleable.setSuperStyleable(superStyleable);
                            styleable.setAndroidStyleableType(superStyleable.getAndroidStyleableType());
                            iterator.remove();
                            reorderStyleable(styleable, namespace);
                            fullStyleableMap.put(styleable.getFullClassName(), styleable);
                        } else {
                            //handle from full name class map
                            String superClassName = styleable.getSuperStyleableName();
                            if (superClassName == null) {
                                StyleableResultCollector result = fullNameClassMap.get(styleable.getFullClassName());
                                if (result != null) {
                                    superClassName = result.getSuperClassName();
                                }
                            }
                            if (superClassName == null) {
                                iterator.remove();
                                namespace.getUknown().add(styleable);
                            }
                            while (superStyleable == null && superClassName != null) {
                                StyleableResultCollector collector = fullNameClassMap.get(superClassName);
                                if (collector == null) {
                                    iterator.remove();
                                    namespace.getUknown().add(styleable);
                                    break;
                                }
                                superClassName = collector.getSuperClassName();
                                superStyleable = fullStyleableMap.get(superClassName);
                            }
                            if (superStyleable != null) {
                                styleable.setSuperStyleable(superStyleable);
                                styleable.setAndroidStyleableType(superStyleable.getAndroidStyleableType());
                                reorderStyleable(styleable, namespace);
                            }

                        }
                    }
                }
                //TODO check all super classes for known styleables and create AndroidStyleable representation
                // For styleables that have no XML attributes
            }
            return namespaces;
        } finally {
            LOCK.unlock();
        }
    }

    private static void handleExplodedRoot(FileObject explodedRoot, FileObject jarOrAarDir, AndroidStyleableNamespace platformNamespace, FileObject root, AndroidStyleableNamespace namespace) {
        if (explodedRoot != null && explodedRoot.isFolder()) {
            FileObject attrFo = explodedRoot.getFileObject("res/values/attrs.xml");
            if (attrFo == null) {
                attrFo = explodedRoot.getFileObject("res/values/values.xml");
            }
            if (attrFo != null) {
                AndroidStyleableNamespace cachedNamespace = LIBS_STYLEABLE_NAMESPACES_MAP.get(jarOrAarDir.getPath());
                if (cachedNamespace == null) {
                    AndroidStyleableNamespace libNamespace = new AndroidStyleableNamespace(RES_AUTO_NAMESPACE, null);
                    StyleableXmlParser.parseAar(libNamespace, platformNamespace, root, attrFo);
                    LIBS_STYLEABLE_NAMESPACES_MAP.put(jarOrAarDir.getPath(), libNamespace);
                    libNamespace.mergeTo(namespace);
                } else {
                    cachedNamespace.mergeTo(namespace);
                }
            }
        }
    }

    public static void reorderStyleable(AndroidStyleable styleable, AndroidStyleableNamespace namespace) throws AssertionError {
        switch (styleable.getAndroidStyleableType()) {
            case Widget:
                namespace.getWitgets().put(styleable.getFullClassName(), styleable);
                namespace.getWitgetsSimpleNames().put(styleable.getName(), styleable);
                break;
            case Layout:
                namespace.getLayouts().put(styleable.getFullClassName(), styleable);
                namespace.getLayoutsSimpleNames().put(styleable.getName(), styleable);
                break;
            case LayoutParams:
                namespace.getLayoutsParams().put(styleable.getFullClassName(), styleable);
                namespace.getLayoutsParamsSimpleNames().put(styleable.getName(), styleable);
                break;
            case ToBeDetermined:
                break;
            case Other:
                break;
            default:
                throw new AssertionError(styleable.getAndroidStyleableType().name());

        }
    }

    public static final AndroidStyleableNamespace getPlatformStyleableNamespace(AndroidJavaPlatform androidJavaPlatform) {
        LOCK.lock();
        try {
            AndroidStyleableNamespace namespace = PLATFORM_STYLEABLE_NAMESPACES_MAP.get(androidJavaPlatform.getPlatformFolder().toString());
            if (namespace == null) {
                namespace = StyleableXmlParser.parseAndroidPlatform(androidJavaPlatform);
                PLATFORM_STYLEABLE_NAMESPACES_MAP.put(androidJavaPlatform.getPlatformFolder().toString(), namespace);
                if (saveFlag.compareAndSet(false, true)) {
                    POOL.schedule(new Runnable() {
                        @Override
                        public void run() {
                            saveFlag.set(false);
                            removeInvalidPlatforms();
                            try (ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(cacheSubfile))) {
                                oStream.writeObject(PLATFORM_STYLEABLE_NAMESPACES_MAP);
                                oStream.flush();
                            } catch (FileNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }, 30, TimeUnit.SECONDS);
                }
            }

            return namespace;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Remove invalid platforms from map
     */
    private static void removeInvalidPlatforms() {
        Iterator<Map.Entry<String, AndroidStyleableNamespace>> iterator = PLATFORM_STYLEABLE_NAMESPACES_MAP.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AndroidStyleableNamespace> next = iterator.next();
            if (!new File(next.getKey()).exists()) {
                iterator.remove();
            }
        }
    }

    public static AndroidStyleableAttrEnum getOrAddEnum(AndroidStyleableAttrEnum attrEnum) {
        LOCK.lock();
        try {
            if (ATTR_ENUMS.contains(attrEnum)) {
                return ATTR_ENUMS.get(ATTR_ENUMS.indexOf(attrEnum));
            } else {
                ATTR_ENUMS.add(attrEnum);
                return attrEnum;
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static AndroidStyleableAttrFlag getOrAddFlag(AndroidStyleableAttrFlag attrFlag) {
        LOCK.lock();
        try {
            if (ATTR_FLAGS.contains(attrFlag)) {
                return ATTR_FLAGS.get(ATTR_FLAGS.indexOf(attrFlag));
            } else {
                ATTR_FLAGS.add(attrFlag);
                return attrFlag;
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static AndroidStyleableAttr findOrAddAttr(AndroidStyleableAttr attr) {
        LOCK.lock();
        try {
            for (AndroidStyleableAttr a : STYLEABLE_ATTRS) {
                if (a.getName().equals(attr.getName())) {
                    return a;
                }
            }
            STYLEABLE_ATTRS.add(attr);
            return attr;
        } finally {
            LOCK.unlock();
        }
    }

    public static AndroidStyleableAttr getOrAddAttr(AndroidStyleableAttr attr) {
        LOCK.lock();
        try {
            if (STYLEABLE_ATTRS.contains(attr)) {
                return STYLEABLE_ATTRS.get(STYLEABLE_ATTRS.indexOf(attr));
            } else {
                STYLEABLE_ATTRS.add(attr);
                return attr;
            }
        } finally {
            LOCK.unlock();
        }
    }

}
