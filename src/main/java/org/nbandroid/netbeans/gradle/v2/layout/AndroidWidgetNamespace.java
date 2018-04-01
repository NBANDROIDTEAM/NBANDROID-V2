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
public class AndroidWidgetNamespace implements Serializable {

    private final String namespace;
    private final String androidPlatformHashString;
    private final List<AndroidWidget> all = new ArrayList<>();
    private final List<AndroidWidget> uknown = new ArrayList<>();
    private final Map<String, AndroidWidget> layouts = new HashMap<>();
    private final Map<String, AndroidWidget> layoutsParams = new HashMap<>();
    private final Map<String, AndroidWidget> witgets = new HashMap<>();
    private final Map<String, AndroidWidget> layoutsSimpleNames = new HashMap<>();
    private final Map<String, AndroidWidget> layoutsParamsSimpleNames = new HashMap<>();
    private final Map<String, AndroidWidget> witgetsSimpleNames = new HashMap<>();

    public AndroidWidgetNamespace(String namespace, String androidPlatformHashString) {
        this.namespace = namespace;
        this.androidPlatformHashString = androidPlatformHashString;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<AndroidWidget> getAll() {
        return all;
    }

    public String getAndroidPlatformHashString() {
        return androidPlatformHashString;
    }

    public List<AndroidWidget> getUknown() {
        return uknown;
    }

    public Map<String, AndroidWidget> getLayouts() {
        return layouts;
    }

    public Map<String, AndroidWidget> getLayoutsParams() {
        return layoutsParams;
    }

    public Map<String, AndroidWidget> getWitgets() {
        return witgets;
    }

    public Map<String, AndroidWidget> getLayoutsSimpleNames() {
        return layoutsSimpleNames;
    }

    public Map<String, AndroidWidget> getLayoutsParamsSimpleNames() {
        return layoutsParamsSimpleNames;
    }

    public Map<String, AndroidWidget> getWitgetsSimpleNames() {
        return witgetsSimpleNames;
    }

    @Override
    public String toString() {
        return "AndroidWidgetNamespace{" + "namespace=" + namespace + ", widgets=" + all + '}';
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
        final AndroidWidgetNamespace other = (AndroidWidgetNamespace) obj;
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
