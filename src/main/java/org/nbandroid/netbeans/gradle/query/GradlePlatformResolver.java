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
package org.nbandroid.netbeans.gradle.query;

import com.android.builder.model.AndroidProject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.AndroidModelAware;
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatform;
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatformManager;
import org.nbandroid.netbeans.gradle.spi.DalvikPlatformResolver;

/**
 *
 * @author radim
 */
public class GradlePlatformResolver implements DalvikPlatformResolver, AndroidModelAware {

    private static final Logger LOG = Logger.getLogger(GradlePlatformResolver.class.getName());

    private AndroidProject aPrj;

    @Override
    public DalvikPlatform findDalvikPlatform() {
        if (aPrj == null) {
            return null;
        }
        LOG.log(Level.FINE, "look for dalvik platform for {0}", aPrj.getCompileTarget());
        return DalvikPlatformManager.getDefault().findPlatformForTarget(aPrj.getCompileTarget());
    }

    @Override
    public void setAndroidProject(AndroidProject aPrj) {
        this.aPrj = aPrj;
    }

}
