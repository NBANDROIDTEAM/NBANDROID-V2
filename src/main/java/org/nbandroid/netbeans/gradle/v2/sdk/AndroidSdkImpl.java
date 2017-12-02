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
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.SdkConstants;
import com.android.repository.api.Installer;
import com.android.repository.api.LocalPackage;
import com.android.repository.api.RepoManager;
import com.android.repository.api.RepoPackage;
import com.android.repository.api.Uninstaller;
import com.android.repository.api.UpdatablePackage;
import com.android.repository.impl.installer.BasicInstallerFactory;
import com.android.repository.impl.meta.RepositoryPackages;
import com.android.repository.impl.meta.TypeDetails;
import com.android.repository.io.FileOpUtils;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.repository.AndroidSdkHandler;
import com.android.sdklib.repository.installer.MavenInstallListener;
import com.android.sdklib.repository.meta.DetailsTypes;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.gradle.impldep.com.google.common.collect.ImmutableList;
import org.nbandroid.netbeans.gradle.core.sdk.NbDownloader;
import org.nbandroid.netbeans.gradle.core.sdk.NbOutputWindowProgressIndicator;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerPackageNode;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerPlatformChangeListener;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerPlatformPackagesRootNode;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerToolsChangeListener;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerToolsMultiPackageNode;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerToolsPackageNode;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerToolsRootNode;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.SdkManagerToolsSupportNode;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author arsi
 */
public class AndroidSdkImpl extends AndroidSdk implements Serializable, RepoManager.RepoLoadedCallback {

    public static final String DEFAULT_PLATFORM = "DEFAULT";
    private String displayName;
    private String sdkPath;
    private Map<String, String> properties = new HashMap<>();
    private Map<String, String> sysproperties = new HashMap<>();
    private boolean defaultSdk;
    private AndroidSdkHandler androidSdkHandler;
    private RepoManager repoManager;
    //max 3 paralel downloads
    public static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(3);
    private final Vector<SdkManagerPlatformChangeListener> listeners = new Vector<>();
    private final Vector<SdkManagerToolsChangeListener> toolsListeners = new Vector<>();
    private final Vector<LocalPlatformChangeListener> localListeners = new Vector<>();
    private Vector<UpdatablePackage> platforms = new Vector<>();
    private SdkManagerPlatformPackagesRootNode platformPackages = null;
    private List<UpdatablePackage> toolsPackages = null;
    private static final Set<String> MULTI_VERSION_PREFIXES
            = ImmutableSet.of(SdkConstants.FD_BUILD_TOOLS, SdkConstants.FD_LLDB, SdkConstants.FD_CMAKE,
                    String.join(String.valueOf(RepoPackage.PATH_SEPARATOR),
                            SdkConstants.FD_EXTRAS,
                            SdkConstants.FD_ANDROID_EXTRAS,
                            SdkConstants.FD_GAPID));
    private SdkManagerToolsRootNode toolRoot;
    private final Map<String, AndroidPlatformInfo> platformsList = new ConcurrentHashMap<>();
    public AndroidSdkImpl(String displayName, String sdkPath, Map<String, String> properties, Map<String, String> sysproperties, List<AndroidPlatformInfo> platforms, boolean defaultSdk) {
        this.displayName = displayName;
        this.sdkPath = sdkPath;
        this.defaultSdk = defaultSdk;
        if (properties != null) {
            this.properties = properties;
        } else {
            this.properties = new HashMap<>();
        }
        if (sysproperties != null) {
            this.sysproperties = sysproperties;
        } else {
            this.sysproperties = new HashMap<>();
        }
        if (platforms != null) {
            AndroidSdkTools.orderByApliLevel(platforms);
            for (AndroidPlatformInfo platform : platforms) {
                platformsList.put(platform.getPlatformFolder().getAbsolutePath(), platform);
            }
        }
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                androidSdkHandler = AndroidSdkHandler.getInstance(new File(sdkPath));
                if (androidSdkHandler != null) {
                    repoManager = androidSdkHandler.getSdkManager(new NbOutputWindowProgressIndicator());
                    repoManager.registerLocalChangeListener(AndroidSdkImpl.this);
                    repoManager.registerRemoteChangeListener(AndroidSdkImpl.this);
                    updateSdkPlatformPackages();
                } else {
                    repoManager = null;
                }
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                AndroidSdk.pool.submit(runnable);
            }
        });
    }

    public AndroidSdkImpl(String displayName, String sdkPath) {
        this(displayName, sdkPath, Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_LIST, false);
    }

    public List<AndroidPlatformInfo> getPlatforms() {
        return new ArrayList<>(platformsList.values());
    }

    @Override
    public AndroidSdkHandler getAndroidSdkHandler() {
        return androidSdkHandler;
    }

    @Override
    public void addLocalPlatformChangeListener(LocalPlatformChangeListener l) {
        if (!localListeners.contains(l)) {
            localListeners.add(l);
        }
        if (!platforms.isEmpty()) {
            l.platformListChanged(new ArrayList<>(platformsList.values()));
        }
    }

    @Override
    public void removeLocalPlatformChangeListener(LocalPlatformChangeListener l) {
        localListeners.add(l);
    }

    public void store() {
        firePropertyChange("STORE", false, true);
    }

    /**
     * Get Android repo manager
     *
     * @return RepoManager or null if no SDK location is set
     */
    @Override
    public RepoManager getRepoManager() {
        return repoManager;
    }

    /**
     * Add addSdkToolsChangeListener to listen of SDK tools pakages list changes
     * On add is listener called with actual package list
     *
     * @param l addSdkToolsChangeListener
     */
    @Override
    public void addSdkToolsChangeListener(SdkManagerToolsChangeListener l) {
        if (!toolsListeners.contains(l)) {
            toolsListeners.add(l);
            if (toolRoot != null) {
                l.packageListChanged(toolRoot);
            }
        }
    }

    /**
     * Remove SdkManagerToolsChangeListener
     *
     * @param l SdkManagerToolsChangeListener
     */
    @Override
    public void removeSdkToolsChangeListener(SdkManagerToolsChangeListener l) {
        toolsListeners.remove(l);
    }

    /**
     * Add SdkManagerPlatformChangeListener to listen of SDK platform pakages
     * list changes. On add is listener called with actual package list.
     * Listener is called from actual thread
     *
     * @param l SdkManagerPlatformChangeListener
     */
    @Override
    public void addSdkPlatformChangeListener(SdkManagerPlatformChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
            if (platformPackages != null) {
                try {
                    l.packageListChanged(platformPackages);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    /**
     * Remove SdkManagerPlatformChangeListener
     *
     * @param l SdkManagerPlatformChangeListener
     */
    @Override
    public void removeSdkPlatformChangeListener(SdkManagerPlatformChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public String getDisplayName() {
        return displayName;

    }

    @Override
    public String getHtmlDisplayName() {
        if (isDefaultSdk()) {
            return "<html><b>" + displayName + " (default)</b></html>";
        }
        return null;

    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public FileObject getInstallFolder() {
        return FileUtil.toFileObject(new File(sdkPath));
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public void setSdkRootFolder(String sdkPath) {
        this.sdkPath = sdkPath;
    }

    @Override
    public void setDefault(boolean defaultSdk) {
        boolean last = this.defaultSdk;
        this.defaultSdk = defaultSdk;
        firePropertyChange(DEFAULT_PLATFORM, last, defaultSdk);
    }

    @Override
    public boolean isDefaultSdk() {
        return defaultSdk;
    }

    @Override
    public List<FileObject> getInstallFolders() {
        List<FileObject> tmp = new ArrayList<>();
        tmp.add(getInstallFolder());
        return Collections.unmodifiableList(tmp);
    }

    @Override
    Map<String, String> getSystemProperties() {
        return sysproperties;
    }

    /**
     * Update SDK platform pakages list After update is called
     * SdkManagerPlatformChangeListener
     */
    @Override
    public void updateSdkPlatformPackages() {
        if (repoManager != null) {
            repoManager.load(0, ImmutableList.of(this),
                    ImmutableList.of(this),
                    ImmutableList.of(new DownloadErrorCallback()),
                    new NbProgressRunner(), new NbDownloader(), new NbSettingsController(), false);
        }
    }

    @Override
    public void doRun(RepositoryPackages packages) {
        loadPackages(packages);
    }

    private void loadPackages(RepositoryPackages packages) {
        List<AndroidVersionNode> tmpPackages = new ArrayList<>();
        Map<AndroidVersion, AndroidVersionNode> tmp = new HashMap<>();
        toolsPackages = new ArrayList<>();
        Vector<UpdatablePackage> platformsTmp = new Vector<>();
        for (UpdatablePackage info : packages.getConsolidatedPkgs().values()) {
            RepoPackage p = info.getRepresentative();
            TypeDetails details = p.getTypeDetails();
            if ((details instanceof DetailsTypes.PlatformDetailsType) && info.hasLocal()) {
                platformsTmp.add(info);
            }

            if (details instanceof DetailsTypes.ApiDetailsType) {
                AndroidVersion androidVersion = ((DetailsTypes.ApiDetailsType) details).getAndroidVersion();
                if (tmp.containsKey(androidVersion)) {
                    AndroidVersionNode avd = tmp.get(androidVersion);
                    avd.addPackage(new SdkManagerPackageNode(avd, info));
                } else {
                    AndroidVersionNode avd = new AndroidVersionNode(androidVersion);
                    avd.addPackage(new SdkManagerPackageNode(avd, info));
                    tmp.put(androidVersion, avd);
                    tmpPackages.add(avd);
                }
            } else {
                toolsPackages.add(info);
            }
        }
        platforms = platformsTmp;
        for (UpdatablePackage up : platformsTmp) {
            AndroidPlatformInfo info = platformsList.get(up.getLocal().getLocation().getAbsolutePath());
            if (info != null) {
                try {
                    info.update();
                } catch (FileNotFoundException ex) {
                    platformsList.remove(up.getLocal().getLocation().getAbsolutePath());
                }
            } else {
                try {
                    info = new AndroidPlatformInfo(up);
                    platformsList.put(up.getLocal().getLocation().getAbsolutePath(), info);
                } catch (FileNotFoundException ex) {
                }
            }
        }
        firePropertyChange("test", true, false);

        List<AndroidPlatformInfo> tmpPlatformList = new ArrayList<>(platformsList.values());
        AndroidSdkTools.orderByApliLevel(tmpPlatformList);
        for (LocalPlatformChangeListener localListener : localListeners) {
            try {
                localListener.platformListChanged(tmpPlatformList);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        Collections.sort(tmpPackages, new Comparator<AndroidVersionNode>() {
            @Override
            public int compare(AndroidVersionNode o1, AndroidVersionNode o2) {
                return o2.getCodeName().compareTo(o1.getCodeName());
            }
        });
        platformPackages = new SdkManagerPlatformPackagesRootNode(tmpPackages);
        for (SdkManagerPlatformChangeListener listener : listeners) {
            try {
                listener.packageListChanged(platformPackages);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        Map<String, SdkManagerToolsMultiPackageNode> tmpMulti = new HashMap<>();
        Map<String, SdkManagerToolsMultiPackageNode> tmpMultiSupport = new HashMap<>();
        toolRoot = new SdkManagerToolsRootNode();
        SdkManagerToolsSupportNode supportNode = new SdkManagerToolsSupportNode(toolRoot);
        for (UpdatablePackage p : toolsPackages) {
            String prefix = p.getRepresentative().getPath();
            int lastSegmentIndex = prefix.lastIndexOf(';');
            boolean found = false;
            if (lastSegmentIndex > 0) {
                prefix = prefix.substring(0, lastSegmentIndex);
                if (prefix.equals("patcher")) {
                    // We don't want to show the patcher in the UI
                    continue;
                }
                if (MULTI_VERSION_PREFIXES.contains(prefix) || p.getRepresentative().getTypeDetails() instanceof DetailsTypes.MavenType) {
                    if (!(p.getRepresentative().getTypeDetails() instanceof DetailsTypes.MavenType)) {
                        if (tmpMulti.containsKey(prefix)) {
                            SdkManagerToolsMultiPackageNode node = tmpMulti.get(prefix);
                            node.addNode(new SdkManagerToolsPackageNode(node, p));
                        } else {
                            SdkManagerToolsMultiPackageNode node = new SdkManagerToolsMultiPackageNode(toolRoot, prefix);
                            node.addNode(new SdkManagerToolsPackageNode(node, p));
                            tmpMulti.put(prefix, node);
                            toolRoot.addNode(node);
                        }
                    } else if (tmpMultiSupport.containsKey(prefix)) {
                        SdkManagerToolsMultiPackageNode node = tmpMultiSupport.get(prefix);
                        node.addNode(new SdkManagerToolsPackageNode(node, p));
                    } else {
                        SdkManagerToolsMultiPackageNode node = new SdkManagerToolsMultiPackageNode(supportNode, prefix);
                        node.addNode(new SdkManagerToolsPackageNode(node, p));
                        tmpMultiSupport.put(prefix, node);
                        supportNode.addNode(node);
                    }
                    found = true;
                }
            }
            if (!found) {
                if (p.getRepresentative().getPath().endsWith(RepoPackage.PATH_SEPARATOR + MavenInstallListener.MAVEN_DIR_NAME)) {
                    supportNode.addNode(new SdkManagerToolsPackageNode(toolRoot, p));
                } else {
                    toolRoot.addNode(new SdkManagerToolsPackageNode(toolRoot, p));
                }
            }
        }
        toolRoot.addNode(supportNode);

        for (SdkManagerToolsChangeListener toolsListener : toolsListeners) {
            try {
                toolsListener.packageListChanged(toolRoot);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }

    }

    /**
     * Uninstall android package After unistall is called
     * updateSdkPlatformPackages()
     *
     * @param aPackage LocalPackage
     */
    @Override
    public void uninstallPackage(LocalPackage aPackage) {
        if (aPackage == null) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BasicInstallerFactory installerFactory = new BasicInstallerFactory();
                Uninstaller uninstaller = installerFactory.createUninstaller(aPackage, getRepoManager(), FileOpUtils.create());
                NbOutputWindowProgressIndicator indicator = new NbOutputWindowProgressIndicator();
                if (uninstaller.prepare(indicator)) {
                    uninstaller.complete(indicator);
                }
                updateSdkPlatformPackages();
            }
        };
        DOWNLOAD_POOL.execute(runnable);
    }

    /**
     * Install or Update Android package After istall is called
     * updateSdkPlatformPackages()
     *
     * @param aPackage UpdatablePackage
     */
    @Override
    public void installPackage(final UpdatablePackage aPackage) {
        if (aPackage == null) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BasicInstallerFactory installerFactory = new BasicInstallerFactory();
                Installer installer = installerFactory.createInstaller(aPackage.getRemote(), getRepoManager(), new NbDownloader(), FileOpUtils.create());
                NbOutputWindowProgressIndicator indicator = new NbOutputWindowProgressIndicator();
                if (installer.prepare(indicator)) {
                    installer.complete(indicator);
                }
                updateSdkPlatformPackages();
            }
        };

        DOWNLOAD_POOL.execute(runnable);
    }

}
