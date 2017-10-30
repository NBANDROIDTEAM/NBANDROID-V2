/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.sdklib.AndroidVersion;
import com.android.sdklib.SdkVersionInfo;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Vector;
import javax.swing.tree.TreeNode;

/**
 *
 * @author arsi
 */
public class AndroidVersionDecorator implements TreeNode {

    private final AndroidVersion version;
    private final String codeName;
    private final Vector<UpdatablePackageDecorator> packages = new Vector<>();
    private boolean flatModel = true;

    public AndroidVersionDecorator(AndroidVersion version) {
        this.version = version;
        codeName = SdkVersionInfo.getVersionWithCodename(version);
    }

    public void addPackage(UpdatablePackageDecorator pkg) {
        packages.add(pkg);
    }

    @Override
    public String toString() {
        return codeName; //To change body of generated methods, choose Tools | Templates.
    }

    public AndroidVersion getVersion() {
        return version;
    }

    public String getCodeName() {
        return codeName;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return packages.elementAt(childIndex);
    }

    @Override
    public int getChildCount() {
        return packages.size();
    }

    @Override
    public TreeNode getParent() {
        return null;
    }

    @Override
    public int getIndex(TreeNode node) {
        return packages.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return packages.isEmpty() || flatModel;
    }

    @Override
    public Enumeration children() {
        return packages.elements();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.version);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AndroidVersionDecorator) {
            if (Objects.equals(this.version, ((AndroidVersionDecorator) obj).getVersion())) {
                return true;
            }
        }

        if (obj instanceof AndroidVersion) {
            if (version.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFlatModel() {
        return flatModel;
    }

    public void setFlatModel(boolean flatModel) {
        this.flatModel = flatModel;
    }

    public Vector<UpdatablePackageDecorator> getPackages() {
        return packages;
    }

}
