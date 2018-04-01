/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author arsi
 */
public class AndroidStyleableNamespace implements Serializable {

    private final String namespace;
    private final String androidPlatformHashString;
    private final List<AndroidStyleable> all = new ArrayList<>();
    private final List<AndroidStyleable> uknown = new ArrayList<>();
    private final Map<String, AndroidStyleable> layouts = new HashMap<>();
    private final Map<String, AndroidStyleable> layoutsParams = new HashMap<>();
    private final Map<String, AndroidStyleable> witgets = new HashMap<>();
    private final Map<String, AndroidStyleable> layoutsSimpleNames = new HashMap<>();
    private final Map<String, AndroidStyleable> layoutsParamsSimpleNames = new HashMap<>();
    private final Map<String, AndroidStyleable> witgetsSimpleNames = new HashMap<>();

    public AndroidStyleableNamespace(String namespace, String androidPlatformHashString) {
        this.namespace = namespace;
        this.androidPlatformHashString = androidPlatformHashString;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<AndroidStyleable> getAll() {
        return all;
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
