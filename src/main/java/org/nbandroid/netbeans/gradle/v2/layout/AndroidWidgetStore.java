/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout;

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
import org.nbandroid.netbeans.gradle.v2.layout.parsers.WidgetPlatformXmlParser;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import static org.nbandroid.netbeans.gradle.v2.sdk.ui.SDKVisualPanel2Download.NBANDROID_FOLDER;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class AndroidWidgetStore {

    private static final List<AndroidWidgetAttrEnum> ATTR_ENUMS = new ArrayList<>();
    private static final List<AndroidWidgetAttrFlag> ATTR_FLAGS = new ArrayList<>();
    private static final List<AndroidWidgetAttr> WIDGET_ATTRS = new ArrayList<>();
    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static final Map<String, AndroidWidgetNamespace> PLATFORM_WIDGET_NAMESPACES = new HashMap<>();
    private static final String WIDGET_CACHE_FILENAME = "platformWidgetCache.obj";
    private static final File cacheSubfile = Places.getCacheSubfile(NBANDROID_FOLDER + WIDGET_CACHE_FILENAME);
    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    private static final AtomicBoolean saveFlag = new AtomicBoolean(false);

    static {
        if (cacheSubfile.exists()) {
            try (ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(cacheSubfile))) {
                Map<String, AndroidWidgetNamespace> cacheObj = (Map<String, AndroidWidgetNamespace>) objStream.readObject();
                PLATFORM_WIDGET_NAMESPACES.putAll(cacheObj);
                removeInvalidPlatforms();
            } catch (IOException | ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    public static final AndroidWidgetNamespace getPlatformWidgetNamespace(AndroidJavaPlatform androidJavaPlatform) {
        LOCK.lock();
        try {
            AndroidWidgetNamespace namespace = PLATFORM_WIDGET_NAMESPACES.get(androidJavaPlatform.getPlatformFolder().toString());
            if (namespace == null) {
                namespace = WidgetPlatformXmlParser.parseAndroidPlatform(androidJavaPlatform);
                PLATFORM_WIDGET_NAMESPACES.put(androidJavaPlatform.getPlatformFolder().toString(), namespace);
                if (saveFlag.compareAndSet(false, true)) {
                    pool.schedule(new Runnable() {
                        @Override
                        public void run() {
                            saveFlag.set(false);
                            removeInvalidPlatforms();
                            try (ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(cacheSubfile))) {
                                oStream.writeObject(PLATFORM_WIDGET_NAMESPACES);
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
        Iterator<Map.Entry<String, AndroidWidgetNamespace>> iterator = PLATFORM_WIDGET_NAMESPACES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AndroidWidgetNamespace> next = iterator.next();
            if (!new File(next.getKey()).exists()) {
                iterator.remove();
            }
        }
    }

    public static AndroidWidgetAttrEnum getOrAddEnum(AndroidWidgetAttrEnum attrEnum) {
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

    public static AndroidWidgetAttrFlag getOrAddFlag(AndroidWidgetAttrFlag attrFlag) {
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

    public static AndroidWidgetAttr getOrAddAttr(AndroidWidgetAttr attr) {
        LOCK.lock();
        try {
            if (WIDGET_ATTRS.contains(attr)) {
                return WIDGET_ATTRS.get(WIDGET_ATTRS.indexOf(attr));
            } else {
                WIDGET_ATTRS.add(attr);
                return attr;
            }
        } finally {
            LOCK.unlock();
        }
    }

}
