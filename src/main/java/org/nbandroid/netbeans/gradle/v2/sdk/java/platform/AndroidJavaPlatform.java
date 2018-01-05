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

import com.android.sdklib.SdkVersionInfo;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.nbandroid.netbeans.gradle.v2.sdk.GlobalAndroidClassPathRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 * AndroidJavaPlatform from Android UpdatablePackage
 *
 * @author arsi
 */
public class AndroidJavaPlatform extends JavaPlatform {

    private final AndroidPlatformInfo pkg;
    private final Specification specification;
    private final ClassPath boot;
    private final ClassPath source;

    AndroidJavaPlatform(AndroidPlatformInfo pkg, String javaVersion) {
        this.pkg = pkg;
        this.specification = new Specification("j2se", new SpecificationVersion(javaVersion));
        this.boot = GlobalAndroidClassPathRegistry.getClassPath(ClassPath.BOOT, pkg.getBootURLs());
        this.source = GlobalAndroidClassPathRegistry.getClassPath(ClassPath.SOURCE, pkg.getSrcURLs());
    }

    public AndroidPlatformInfo getPkg() {
        return pkg;
    }


    @Override
    public String getDisplayName() {
        return SdkVersionInfo.getVersionWithCodename(pkg.getAndroidVersion());
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        return boot;
    }

    @Override
    public ClassPath getStandardLibraries() {
        return ClassPath.EMPTY;
    }

    @Override
    public String getVendor() {
        return "Google Inc.";
    }

    @Override
    public Specification getSpecification() {
        return specification;
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        List<FileObject> tmp = new ArrayList<>();
        tmp.add(pkg.getSDKFolderFo());
        return tmp;
    }

    @Override
    public FileObject findTool(String toolName) {
        return null;
    }

    @Override
    public ClassPath getSourceFolders() {
        return source;
    }

    @Override
    public List<URL> getJavadocFolders() {
        return pkg.getJavadocURLs();
    }

}
