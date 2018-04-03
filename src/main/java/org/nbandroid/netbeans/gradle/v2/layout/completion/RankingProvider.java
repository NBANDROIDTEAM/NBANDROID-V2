/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.completion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.nbandroid.netbeans.gradle.v2.sdk.ui.SDKVisualPanel2Download.NBANDROID_FOLDER;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import static org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableStore.POOL;

/**
 *
 * @author arsi
 */
public class RankingProvider {

    private static final Map<Integer, Integer> useMap = new ConcurrentHashMap<>();
    private static final String STYLEABLE_CACHE_INSERT_COUNT = "styleableInsertCountCache.obj";
    private static final File cacheSubfile = Places.getCacheSubfile(NBANDROID_FOLDER + STYLEABLE_CACHE_INSERT_COUNT);
    private static final AtomicBoolean saveFlag = new AtomicBoolean(false);

    static {
        if (cacheSubfile.exists()) {
            try (ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(cacheSubfile))) {
                Map<Integer, Integer> cacheObj = (Map<Integer, Integer>) objStream.readObject();
                useMap.putAll(cacheObj);
            } catch (IOException | ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void inserted(int hash) {
        Integer use = useMap.get(hash);
        if (use == null) {
            useMap.put(hash, Integer.MAX_VALUE);
        } else {
            useMap.put(hash, use - 1);
        }
        if (saveFlag.compareAndSet(false, true)) {
            POOL.schedule(new Runnable() {
                @Override
                public void run() {
                    saveFlag.set(false);
                    try (ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(cacheSubfile))) {
                        oStream.writeObject(useMap);
                        oStream.flush();
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, 5, TimeUnit.MINUTES);
        }
    }

    public static int getRank(int hash) {
        Integer use = useMap.get(hash);
        if (use == null) {
            return Integer.MAX_VALUE;
        } else {
            return use;
        }
    }

}
