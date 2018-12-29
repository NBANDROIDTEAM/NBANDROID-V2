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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 *
 * @author arsi
 */
public class GlobalAndroidClassPathRegistry {

    private static final Map<URL, ClassPath> cache = new HashMap<>();

    public synchronized static final ClassPath getClassPath(final String id, URL[] urls) {
        if (urls.length == 0) {
            return ClassPath.EMPTY;
        } else {
            ClassPath tmp[] = new ClassPath[urls.length];
            for (int i = 0; i < urls.length; i++) {
                ClassPath classPath = cache.get(urls[i]);
                if (classPath == null) {
                    classPath = ClassPathSupport.createClassPath(urls[i]);
                    cache.put(urls[i], classPath);
                }
                tmp[i] = classPath;
            }
            return ClassPathSupport.createProxyClassPath(tmp);
        }

    }

}
