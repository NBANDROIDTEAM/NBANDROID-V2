/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;

/**
 *
 * @author arsi
 */
public class AndroidWidgetNamespace implements Serializable {

    private final String namespace;
    private final AndroidJavaPlatform androidPlatform;
    private final List<AndroidWidget> widgets = new ArrayList<>();

    public AndroidWidgetNamespace(String namespace, AndroidJavaPlatform androidPlatform) {
        this.namespace = namespace;
        this.androidPlatform = androidPlatform;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<AndroidWidget> getWidgets() {
        return widgets;
    }

    public AndroidJavaPlatform getAndroidPlatform() {
        return androidPlatform;
    }


    @Override
    public String toString() {
        return "AndroidWidgetNamespace{" + "namespace=" + namespace + ", widgets=" + widgets + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.namespace);
        hash = 97 * hash + Objects.hashCode(this.androidPlatform);
        hash = 97 * hash + Objects.hashCode(this.widgets);
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
        if (!Objects.equals(this.androidPlatform, other.androidPlatform)) {
            if (this.androidPlatform != null && other.androidPlatform != null) {
                if (!this.androidPlatform.getDisplayName().equals(other.androidPlatform.getDisplayName())) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (!Objects.equals(this.widgets, other.widgets)) {
            return false;
        }
        return true;
    }


}
