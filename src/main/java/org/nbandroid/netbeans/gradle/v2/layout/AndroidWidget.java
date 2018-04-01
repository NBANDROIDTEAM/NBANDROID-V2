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
public class AndroidWidget implements Serializable {

    private final AndroidWidgetNamespace nameSpace;
    private final String nameSpacePath;
    private final String name;
    private final List<AndroidWidgetAttr> attrs = new ArrayList<>();
    private String superWidgetName;
    private AndroidWidget superWidget;
    private String fullClassName;
    private AndroidWidgetType androidWidgetType = AndroidWidgetType.ToBeDetermined;

    public AndroidWidget(AndroidWidgetNamespace nameSpace, String name) {
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

    public AndroidWidgetNamespace getNameSpace() {
        return nameSpace;
    }

    public String getName() {
        return name;
    }

    public List<AndroidWidgetAttr> getAttrs() {
        return attrs;
    }

    public AndroidWidget getSuperWidget() {
        return superWidget;
    }

    public String getSuperWidgetName() {
        return superWidgetName;
    }

    public void setSuperWidget(AndroidWidget superWidget) {
        this.superWidget = superWidget;
    }

    public AndroidWidgetType getAndroidWidgetType() {
        return androidWidgetType;
    }

    public void setAndroidWidgetType(AndroidWidgetType androidWidgetType) {
        if (androidWidgetType != null) {
            this.androidWidgetType = androidWidgetType;
        } else {
            this.androidWidgetType = AndroidWidgetType.ToBeDetermined;
        }
    }

    public void setSuperWidgetName(String superWidgetName) {
        this.superWidgetName = superWidgetName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getNameSpacePath() {
        return nameSpacePath;
    }

    @Override
    public String toString() {
        return "AndroidWidget{" + ", name=" + name + ", type=" + androidWidgetType + ", attrs=" + attrs + "nameSpace=" + nameSpacePath + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.nameSpacePath);
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.attrs);
        hash = 61 * hash + Objects.hashCode(this.superWidgetName);
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
        final AndroidWidget other = (AndroidWidget) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.superWidgetName, other.superWidgetName)) {
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
