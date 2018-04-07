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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.nbandroid.netbeans.gradle.v2.layout.completion.analyzer.StyleableResultCollector;

/**
 *
 * @author arsi
 */
public class AndroidStyleableNamespace implements Serializable {

    private final String namespace;
    /**
     * Original library namespace for res-auto
     */
    private String primary_namespace = null;
    private final String androidPlatformHashString;
    private final List<AndroidStyleable> all = new ArrayList<>();
    private final List<AndroidStyleable> uknown = new ArrayList<>();
    private final List<AndroidStyleable> todo = new ArrayList<>();
    private final Map<String, AndroidStyleable> layouts = new HashMap<>();
    private final Map<String, AndroidStyleable> layoutsParams = new HashMap<>();
    private final Map<String, AndroidStyleable> witgets = new HashMap<>();
    private final Map<String, AndroidStyleable> layoutsSimpleNames = new HashMap<>();
    private final Map<String, AndroidStyleable> layoutsParamsSimpleNames = new HashMap<>();
    private final Map<String, AndroidStyleable> witgetsSimpleNames = new HashMap<>();
    private final Map<String, AndroidStyleable> other = new HashMap<>();
    private final Map<String, AndroidStyleable> otherSimpleNames = new HashMap<>();
    private final Map<String, StyleableResultCollector> fullClassNameMap = new HashMap<>();

    public AndroidStyleableNamespace(String namespace, String androidPlatformHashString) {
        this.namespace = namespace;
        this.androidPlatformHashString = androidPlatformHashString;
    }

    public boolean isPlatformNamespace() {
        return AndroidStyleableStore.ANDROID_NAMESPACE.equals(namespace);
    }

    public boolean hasTodo() {
        return !todo.isEmpty();
    }

    public void mergeTo(AndroidStyleableNamespace to) {
        to.all.addAll(all);
        to.layouts.putAll(layouts);
        to.layoutsParams.putAll(layoutsParams);
        to.layoutsParamsSimpleNames.putAll(layoutsParamsSimpleNames);
        to.layoutsSimpleNames.putAll(layoutsSimpleNames);
        to.uknown.addAll(uknown);
        to.witgets.putAll(witgets);
        to.witgetsSimpleNames.putAll(witgetsSimpleNames);
        to.todo.addAll(todo);
        to.other.putAll(other);
        to.otherSimpleNames.putAll(otherSimpleNames);
        to.fullClassNameMap.putAll(fullClassNameMap);
    }

    /**
     * get Original library namespace for res-auto
     *
     * @return
     */
    public String getPrimary_namespace() {
        return primary_namespace;
    }

    /**
     * set Original library namespace for res-auto
     *
     * @param primary_namespace
     */
    public void setPrimary_namespace(String primary_namespace) {
        this.primary_namespace = primary_namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<AndroidStyleable> getAll() {
        return all;
    }

    public List<AndroidStyleable> getTodo() {
        return todo;
    }

    public Map<String, StyleableResultCollector> getFullClassNameMap() {
        return fullClassNameMap;
    }

    public void addAllFullClassNamesTo(Map<String, StyleableResultCollector> to) {
        to.putAll(fullClassNameMap);
    }

    public void addAllStyleablesTo(Map<String, AndroidStyleable> to) {
        to.putAll(layouts);
        to.putAll(layoutsParams);
        to.putAll(witgets);
        to.putAll(other);
    }

    public String getAndroidPlatformHashString() {
        return androidPlatformHashString;
    }

    public List<AndroidStyleable> getUknown() {
        return uknown;
    }

    public Map<String, AndroidStyleable> getLayouts() {
        return layouts;
    }

    public Map<String, AndroidStyleable> getLayoutsParams() {
        return layoutsParams;
    }

    public Map<String, AndroidStyleable> getWitgets() {
        return witgets;
    }

    public Map<String, AndroidStyleable> getLayoutsSimpleNames() {
        return layoutsSimpleNames;
    }

    public Map<String, AndroidStyleable> getLayoutsParamsSimpleNames() {
        return layoutsParamsSimpleNames;
    }

    public Map<String, AndroidStyleable> getWitgetsSimpleNames() {
        return witgetsSimpleNames;
    }

    public Map<String, AndroidStyleable> getOther() {
        return other;
    }

    public Map<String, AndroidStyleable> getOtherSimpleNames() {
        return otherSimpleNames;
    }


    @Override
    public String toString() {
        return "AndroidStyleableNamespace{" + "namespace=" + namespace + ", Styleable=" + all + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.namespace);
        hash = 97 * hash + Objects.hashCode(this.androidPlatformHashString);
        hash = 97 * hash + Objects.hashCode(this.all);
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
        final AndroidStyleableNamespace other = (AndroidStyleableNamespace) obj;
        if (!Objects.equals(this.namespace, other.namespace)) {
            return false;
        }
        if (!Objects.equals(this.androidPlatformHashString, other.androidPlatformHashString)) {
            return false;
        }
        if (!Objects.equals(this.all, other.all)) {
            return false;
        }
        return true;
    }

}
