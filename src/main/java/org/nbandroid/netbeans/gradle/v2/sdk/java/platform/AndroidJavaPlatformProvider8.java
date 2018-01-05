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

import java.util.List;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * JavaPlatformProvider for Android platforms and java 1.8
 *
 * @author arsi
 */
@ServiceProviders({
    @ServiceProvider(service = JavaPlatformProvider.class),
    @ServiceProvider(service = AndroidJavaPlatformProvider8.class)})
public class AndroidJavaPlatformProvider8 extends AndroidJavaPlatformProvider {

    @Override
    protected void createPlatform(List<JavaPlatform> tmp, AndroidPlatformInfo pkg) {
        tmp.add(new AndroidJavaPlatform(pkg, "1.8"));
        //add ReadOnlyURLMapper as listener, to avoid lookup chaos when addPropertyChangeListener called directly from ReadOnlyURLMapper
        addPropertyChangeListener(Lookup.getDefault().lookup(ReadOnlyURLMapper.class));
    }
}
