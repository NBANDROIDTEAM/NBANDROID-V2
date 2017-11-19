/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.sdk;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author arsi
 */
public class AndroidSdkPlatform implements Serializable {

    private String displayName;
    private String sdkPath;
    private Map<String, String> properties = Collections.emptyMap();
    private Map<String, String> sysproperties = Collections.emptyMap();
    private boolean defaultSdk = false;
    private PropertyChangeSupport supp;

    public AndroidSdkPlatform(String displayName, String sdkPath, Map<String, String> properties, Map<String, String> sysproperties) {
        this.displayName = displayName;
        this.sdkPath = sdkPath;
        this.properties = properties;
        this.sysproperties = sysproperties;
    }

    public AndroidSdkPlatform(String displayName, String sdkPath) {
        this.displayName = displayName;
        this.sdkPath = sdkPath;
    }


    public String getDisplayName() {
        return displayName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public FileObject getInstallFolder() {
        return FileUtil.toFileObject(new File(sdkPath));
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setSdkRootFolder(String sdkPath) {
        this.sdkPath = sdkPath;
    }

    public void setDefault(boolean defaultSdk) {
        this.defaultSdk = defaultSdk;
    }

    public boolean isDefaultSdk() {
        return defaultSdk;
    }

    public List<FileObject> getInstallFolders() {
        List<FileObject> tmp = new ArrayList<>();
        tmp.add(getInstallFolder());
        return Collections.unmodifiableList(tmp);
    }

    Map<String, String> getSystemProperties() {
        return sysproperties;
    }

    public final void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (supp == null) {
                supp = new PropertyChangeSupport(this);
            }
        }
        supp.addPropertyChangeListener(l);
    }

    /**
     * Removes a listener registered previously
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (supp != null) {
            supp.removePropertyChangeListener(l);
        }
    }

    /**
     * Fires PropertyChange to all registered PropertyChangeListeners
     *
     * @param propName
     * @param oldValue
     * @param newValue
     */
    protected final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        if (supp != null) {
            supp.firePropertyChange(propName, oldValue, newValue);
        }
    }


}
