/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.DebugPortManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.v2.adb.DebugPortProvider;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = AndroidSdkProvider.class)
public class AndroidSdkProvider implements FileChangeListener {

    public static final String PLATFORM_STORAGE = "Services/Platforms/org-nbandroid-netbeans-gradle-Platform"; //NOI18N
    public static final String PROP_INSTALLED_SDKS = "PROP_INSTALLED_SDKS";
    public static final String PROP_DEFAULT_SDK = "PROP_DEFAULT_SDK";
    public static final String PROP_DEFAULT_ADB = "PROP_DEFAULT_ADB";
    public static final String PROP_DEFAULT_ADB_PATH = "PROP_DEFAULT_ADB_PATH";
    private static final String ADB_TOOL = "adb";    //NOI18N
    private static FileObject storageCache;
    private static FileObject lastFound;
    private static AndroidSdkProvider instance = null;

    public static AndroidSdk findSdk(File file) {
        AndroidSdk[] installedPlatforms = getDefault().getSDKs();
        for (AndroidSdk installedPlatform : installedPlatforms) {
            if (installedPlatform.getInstallFolder().equals(FileUtil.toFileObject(file))) {
                return installedPlatform;
            }
        }
        return null;
    }
    private FileChangeListener pathListener;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final Logger LOG = Logger.getLogger(AndroidSdkProvider.class.getName());
    private final AtomicReference<AndroidSdk[]> sdks = new AtomicReference<>(new AndroidSdk[0]);
    private final AtomicReference<AndroidSdk> defaultSdk = new AtomicReference<>(null);
    private final AtomicReference<AndroidDebugBridge> adb = new AtomicReference<>(null);
    private final AtomicReference<String> adbPath = new AtomicReference<>(null);

    public AndroidSdk[] getSDKs() {
        return sdks.get();
    }

    /**
     * Get default SDK manager
     *
     * @return AndroidSdk
     */
    public static final AndroidSdk getDefaultSdk() {
        return getDefault().getSdkDefault();
    }

    public static final AndroidSdkProvider getDefault() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(AndroidSdkProvider.class);
        }
        return instance;
    }

    public static final AndroidSdk[] getInstalledSDKs() {
        return getDefault().getSDKs();
    }

    public static final AndroidDebugBridge getAdb() {
        return getDefault().adb.get();
    }

    public static final String getAdbPath() {
        return getDefault().adbPath.get();
    }

    public AndroidSdkProvider() {
        getStorage();
        firePropertyChange();
    }

    public AndroidSdk getSdkDefault() {
        return defaultSdk.get();
    }

    protected AndroidSdk[] updateSDKs() {
        final List<AndroidSdk> platforms = new ArrayList<>();
        final FileObject storage = getStorage();
        if (storage != null) {
            try {
                for (FileObject platformDefinition : storage.getChildren()) {
                    DataObject dobj = DataObject.find(platformDefinition);
                    InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
                    if (ic == null) {
                        FileObject[] childrens = storage.getChildren();
                        for (FileObject children : childrens) {
                            try {
                                DataObject dob = DataObject.find(children);
                                if (dob instanceof XMLDataObject) {
                                    platforms.add(PlatformConvertor.localInstance(dob));
                                }
                            } catch (DataObjectNotFoundException dataObjectNotFoundException) {
                            }
                        }
                        LOG.log(
                                Level.WARNING,
                                "The file: {0} has no InstanceCookie", //NOI18N
                                platformDefinition.getNameExt());
                    } else if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of) ic).instanceOf(AndroidSdk.class)) {
                            platforms.add((AndroidSdk) ic.instanceCreate());
                        } else {
                            LOG.log(
                                    Level.WARNING,
                                    "The file: {0} is not an instance of AndroidSdkPlatform", //NOI18N
                                    platformDefinition.getNameExt());
                        }
                    } else {
                        Object instance = ic.instanceCreate();
                        if (instance instanceof AndroidSdk) {
                            platforms.add((AndroidSdk) instance);
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
        return platforms.toArray(new AndroidSdk[platforms.size()]);
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
        AndroidSdk[] updateSDKs = updateSDKs();
        AndroidSdk[] last = sdks.getAndSet(updateSDKs);
        if (updateSDKs.length == 1 && defaultSdk.get() == null) {
            makeFirstSdkDefault(updateSDKs);
        } else if (updateSDKs.length > 1 && defaultSdk.get() == null) {//more SDKs find default
            boolean err = true;
            for (AndroidSdk updateSDK : updateSDKs) {
                if (updateSDK.isDefaultSdk()) {
                    AndroidSdk lastDef = defaultSdk.getAndSet(updateSDK);
                    updateAdb();
                    pcs.firePropertyChange(PROP_DEFAULT_SDK, lastDef, updateSDK);
                    err = false;
                    break;
                }
            }
            if (err) {// no SDK is marked default, set first
                makeFirstSdkDefault(updateSDKs);
            }

        } else if (updateSDKs.length == 0) {
            AndroidSdk lastDef = defaultSdk.getAndSet(null);
            updateAdb();
            pcs.firePropertyChange(PROP_DEFAULT_SDK, lastDef, null);
        }
        pcs.firePropertyChange(PROP_INSTALLED_SDKS, last, updateSDKs);

    }

    public void makeFirstSdkDefault(AndroidSdk[] updateSDKs) {
        updateSDKs[0].setDefault(true);
        AndroidSdk lastDef = defaultSdk.getAndSet(updateSDKs[0]);
        updateAdb();
        pcs.firePropertyChange(PROP_DEFAULT_SDK, lastDef, updateSDKs[0]);
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

    //***************ADB*********************
    protected void updateAdb() {
        AndroidSdk sdk = defaultSdk.get();
        if (sdk != null) {
            final FileObject path = sdk.findTool(ADB_TOOL);
            if (path != null) {
                ClientData.class.getClassLoader().clearAssertionStatus();      //qattern
                DebugPortManager.setProvider(DebugPortProvider.getDefault());
                AndroidDebugBridge.initIfNeeded(true);
                String adbLocation = FileUtil.toFile(path).getAbsolutePath();
                String lastLocation = adbPath.getAndSet(adbLocation);
                pcs.firePropertyChange(PROP_DEFAULT_ADB_PATH, lastLocation, adbLocation);
                AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adbLocation, false);
                AndroidDebugBridge lastAdb = adb.getAndSet(bridge);
                pcs.firePropertyChange(PROP_DEFAULT_ADB, lastAdb, bridge);
            }
        }
    }

}
