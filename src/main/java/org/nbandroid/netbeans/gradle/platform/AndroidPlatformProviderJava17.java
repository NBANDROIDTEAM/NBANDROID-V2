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
package org.nbandroid.netbeans.gradle.platform;

import com.android.repository.api.UpdatablePackage;
import java.util.Vector;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * JavaPlatformProvider for Android platforms and java 1.7
 *
 * @author arsi
 */
@ServiceProvider(service = JavaPlatformProvider.class)
public class AndroidPlatformProviderJava17 extends AndroidPlatformProvider {

    @Override
    protected void createPlatform(JavaPlatform[] tmp, int i, Vector<UpdatablePackage> pkgs) {
        tmp[i] = new AndroidPlatform(pkgs.get(i), "1.7");
    }

}
