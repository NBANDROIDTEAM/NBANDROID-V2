/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = AndroidSdkPlatformProvider.class)
public class AndroidSdkPlatformProvider implements FileChangeListener {

    public static final String PLATFORM_STORAGE = "Services/Platforms/org-nbandroid-netbeans-gradle-Platform"; //NOI18N
    public static final String PROP_INSTALLED_PLATFORMS = "PROP_INSTALLED_PLATFORMS";
    private static FileObject storageCache;
    private static FileObject lastFound;
    private FileChangeListener pathListener;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final Logger LOG = Logger.getLogger(AndroidSdkPlatformProvider.class.getName());

    /**
     * Get default SDK manager
     *
     * @return AndroidSdkPlatform
     */
    public static final AndroidSdkPlatform getDefaultPlatform() {
        AndroidSdkPlatform[] installedPlatforms = Lookup.getDefault().lookup(AndroidSdkPlatformProvider.class).getInstalledPlatformsInt();
        switch (installedPlatforms.length) {
            case 1:
                return installedPlatforms[0];
            case 0:
                return null;
            default: {
                for (AndroidSdkPlatform installedPlatform : installedPlatforms) {
                    if (installedPlatform.isDefaultSdk()) {
                        return installedPlatform;
                    }
                }
                return installedPlatforms[0];
            }
        }
    }

    public static final AndroidSdkPlatformProvider getDefault() {
        return Lookup.getDefault().lookup(AndroidSdkPlatformProvider.class);
    }

    public static final AndroidSdkPlatform[] getInstalledPlatforms() {
        return Lookup.getDefault().lookup(AndroidSdkPlatformProvider.class).getInstalledPlatformsInt();
    }

    public AndroidSdkPlatformProvider() {
    }

    protected final AndroidSdkPlatform[] getInstalledPlatformsInt() {
        final List<AndroidSdkPlatform> platforms = new ArrayList<>();
        final FileObject storage = getStorage();
        if (storage != null) {
            try {
                for (FileObject platformDefinition : storage.getChildren()) {
                    DataObject dobj = DataObject.find(platformDefinition);
                    InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
                    if (ic == null) {
                        LOG.log(
                                Level.WARNING,
                                "The file: {0} has no InstanceCookie", //NOI18N
                                platformDefinition.getNameExt());
                    } else if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of) ic).instanceOf(AndroidSdkPlatform.class)) {
                            platforms.add((AndroidSdkPlatform) ic.instanceCreate());
                        } else {
                            LOG.log(
                                    Level.WARNING,
                                    "The file: {0} is not an instance of AndroidSdkPlatform", //NOI18N
                                    platformDefinition.getNameExt());
                        }
                    } else {
                        Object instance = ic.instanceCreate();
                        if (instance instanceof AndroidSdkPlatform) {
                            platforms.add((AndroidSdkPlatform) instance);
                        } else {
                            LOG.log(
                                    Level.WARNING,
                                    "The file: {0} is not an instance of AndroidSdkPlatform", //NOI18N
                                    platformDefinition.getNameExt());
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException cnf) {
                Exceptions.printStackTrace(cnf);
            }
        }
        return platforms.toArray(new AndroidSdkPlatform[platforms.size()]);
    }

    private synchronized FileObject getStorage() {
        if (storageCache == null) {
            storageCache = FileUtil.getConfigFile(PLATFORM_STORAGE);
            if (storageCache != null) {
                storageCache.addFileChangeListener(this);
                removePathListener();
            } else {
                final String[] path = PLATFORM_STORAGE.split("/");  //NOI18N
                FileObject lastExist = FileUtil.getConfigRoot();
                String expected = null;
                for (String pathElement : path) {
                    expected = pathElement;
                    FileObject current = lastExist.getFileObject(expected);
                    if (current == null) {
                        break;
                    }
                    lastExist = current;
                }
                assert lastExist != null;
                assert expected != null;
                removePathListener();
                final String expectedFin = expected;
                pathListener = new FileChangeAdapter() {
                    @Override
                    public void fileFolderCreated(FileEvent fe) {
                        if (expectedFin.equals(fe.getFile().getName())) {
                            firePropertyChange();
                        }
                    }
                };
                lastFound = lastExist;
                lastFound.addFileChangeListener(pathListener);
            }
        }
        return storageCache;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        firePropertyChange();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        firePropertyChange();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        firePropertyChange();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        firePropertyChange();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void firePropertyChange() {
        pcs.firePropertyChange(PROP_INSTALLED_PLATFORMS, null, null);
    }

    /**
     * Removes pathListener from lastFound FileObject threading: caller has to
     * own DefaultAndroidSdkPlatformProvider monitor
     */
    private void removePathListener() {
        if (pathListener != null) {
            assert lastFound != null;
            lastFound.removeFileChangeListener(pathListener);
            pathListener = null;
            lastFound = null;
        }
    }

}
