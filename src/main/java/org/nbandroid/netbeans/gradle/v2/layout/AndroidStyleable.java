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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author arsi
 */
public class AndroidStyleable implements Serializable {

    private final AndroidStyleableNamespace nameSpace;
    private final String nameSpacePath;
    private final String name;
    private final List<AndroidStyleableAttr> attrs = new ArrayList<>();
    private String superStyleableName;
    private AndroidStyleable superStyleable;
    private String fullClassName;
    private AndroidStyleableType androidStyleableType = AndroidStyleableType.ToBeDetermined;

    public AndroidStyleable(AndroidStyleableNamespace nameSpace, String name) {
        this.nameSpace = nameSpace;
        this.name = name;

        if (nameSpace != null) {
            this.nameSpacePath = nameSpace.getNamespace();
        } else {
            this.nameSpacePath = null;
        }
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public AndroidStyleableNamespace getNameSpace() {
        return nameSpace;
    }

    public String getName() {
        return name;
    }

    public List<AndroidStyleableAttr> getAttrs() {
        return attrs;
    }

    public AndroidStyleable getSuperStyleable() {
        return superStyleable;
    }

    public String getSuperStyleableName() {
        return superStyleableName;
    }

    public void setSuperStyleable(AndroidStyleable superStyleable) {
        this.superStyleable = superStyleable;
    }

    public AndroidStyleableType getAndroidStyleableType() {
        return androidStyleableType;
    }

    public void setAndroidStyleableType(AndroidStyleableType androidStyleableType) {
        if (androidStyleableType != null) {
            this.androidStyleableType = androidStyleableType;
        } else {
            this.androidStyleableType = AndroidStyleableType.ToBeDetermined;
        }
    }

    public void setSuperStyleableName(String superStyleableName) {
        this.superStyleableName = superStyleableName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getNameSpacePath() {
        return nameSpacePath;
    }

    @Override
    public String toString() {
        return "AndroidStyleable{" + ", name=" + name + ", type=" + androidStyleableType + ", attrs=" + attrs + "nameSpace=" + nameSpacePath + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.nameSpacePath);
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.attrs);
        hash = 61 * hash + Objects.hashCode(this.superStyleableName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AndroidStyleable other = (AndroidStyleable) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.superStyleableName, other.superStyleableName)) {
            return false;
        }
        if (!Objects.equals(this.nameSpacePath, other.nameSpacePath)) {
            return false;
        }
        if (!Objects.equals(this.attrs, other.attrs)) {
            return false;
        }
        return true;
    }

}
