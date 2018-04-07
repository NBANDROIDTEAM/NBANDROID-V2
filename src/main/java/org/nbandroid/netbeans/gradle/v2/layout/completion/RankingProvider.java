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
package org.nbandroid.netbeans.gradle.v2.layout.completion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableStore.POOL;
import static org.nbandroid.netbeans.gradle.v2.sdk.ui.SDKVisualPanel2Download.NBANDROID_FOLDER;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

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
            if (use > 1) {
                useMap.put(hash, use - 1);
            }
        }
        if (saveFlag.compareAndSet(false, true)) {
            POOL.schedule(new Runnable() {
                @Override
                public void run() {
                    saveFlag.set(false);
                    try (ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(cacheSubfile))) {
                        oStream.writeObject(new HashMap<>(useMap));
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
