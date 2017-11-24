/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.gradle.impldep.com.google.common.collect.ImmutableList;
import org.nbandroid.netbeans.gradle.core.sdk.NbDownloader;
import org.nbandroid.netbeans.gradle.core.sdk.NbOutputWindowProgressIndicator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author arsi
 */
public class AndroidSdkPlatformImpl extends AndroidSdkPlatform implements Serializable, RepoManager.RepoLoadedCallback {

    private String displayName;
    private String sdkPath;
    private Map<String, String> properties = Collections.emptyMap();
    private Map<String, String> sysproperties = Collections.emptyMap();
    private boolean defaultSdk = false;
    private AndroidSdkHandler androidSdkHandler;
    private RepoManager repoManager;
    //max 3 paralel downloads
    public static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(3);
    private final Vector<SdkPlatformChangeListener> listeners = new Vector<>();
    private final Vector<SdkToolsChangeListener> toolsListeners = new Vector<>();
    private final Vector<LocalPlatformChangeListener> localListeners = new Vector<>();
    private Vector<UpdatablePackage> platforms = new Vector<>();
    private SdkPlatformPackagesRootNode platformPackages = null;
    private List<UpdatablePackage> toolsPackages = null;
    private static final Set<String> MULTI_VERSION_PREFIXES
            = ImmutableSet.of(SdkConstants.FD_BUILD_TOOLS, SdkConstants.FD_LLDB, SdkConstants.FD_CMAKE,
                    String.join(String.valueOf(RepoPackage.PATH_SEPARATOR),
                            SdkConstants.FD_EXTRAS,
                            SdkConstants.FD_ANDROID_EXTRAS,
                            SdkConstants.FD_GAPID));
    private SdkToolsRootNode toolRoot;

    public AndroidSdkPlatformImpl(String displayName, String sdkPath, Map<String, String> properties, Map<String, String> sysproperties) {
        this.displayName = displayName;
        this.sdkPath = sdkPath;
        this.properties = properties;
        this.sysproperties = sysproperties;
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                androidSdkHandler = AndroidSdkHandler.getInstance(new File(sdkPath));
                if (androidSdkHandler != null) {
                    repoManager = androidSdkHandler.getSdkManager(new NbOutputWindowProgressIndicator());
                    repoManager.registerLocalChangeListener(AndroidSdkPlatformImpl.this);
                    repoManager.registerRemoteChangeListener(AndroidSdkPlatformImpl.this);
                    updateSdkPlatformPackages();
                } else {
                    repoManager = null;
                }
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                AndroidSdkPlatform.pool.submit(runnable);
            }
        });
    }

    public AndroidSdkPlatformImpl(String displayName, String sdkPath) {
        this(displayName, sdkPath, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
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
            l.platformListChanged(platforms);
        }
    }

    @Override
    public void removeLocalPlatformChangeListener(LocalPlatformChangeListener l) {
        localListeners.add(l);
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
    public void addSdkToolsChangeListener(SdkToolsChangeListener l) {
        if (!toolsListeners.contains(l)) {
            toolsListeners.add(l);
            if (toolRoot != null) {
                l.packageListChanged(toolRoot);
            }
        }
    }

    /**
     * Remove SdkToolsChangeListener
     *
     * @param l SdkToolsChangeListener
     */
    @Override
    public void removeSdkToolsChangeListener(SdkToolsChangeListener l) {
        toolsListeners.remove(l);
    }

    /**
     * Add SdkPlatformChangeListener to listen of SDK platform pakages list
     * changes. On add is listener called with actual package list. Listener is
     * called from actual thread
     *
     * @param l SdkPlatformChangeListener
     */
    @Override
    public void addSdkPlatformChangeListener(SdkPlatformChangeListener l) {
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
     * Remove SdkPlatformChangeListener
     *
     * @param l SdkPlatformChangeListener
     */
    @Override
    public void removeSdkPlatformChangeListener(SdkPlatformChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public String getDisplayName() {
        return displayName;
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
        this.defaultSdk = defaultSdk;
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
     * SdkPlatformChangeListener
     */
    @Override
    public void updateSdkPlatformPackages() {
        if (repoManager != null) {
            repoManager.load(5000, ImmutableList.of(),
                    ImmutableList.of(),
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
                    avd.addPackage(new SdkPackageNode(avd, info));
                } else {
                    AndroidVersionNode avd = new AndroidVersionNode(androidVersion);
                    avd.addPackage(new SdkPackageNode(avd, info));
                    tmp.put(androidVersion, avd);
                    tmpPackages.add(avd);
                }
            } else {
                toolsPackages.add(info);
            }
        }
        platforms = platformsTmp;
        for (LocalPlatformChangeListener localListener : localListeners) {
            try {
                localListener.platformListChanged(platforms);
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
        platformPackages = new SdkPlatformPackagesRootNode(tmpPackages);
        for (SdkPlatformChangeListener listener : listeners) {
            try {
                listener.packageListChanged(platformPackages);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        Map<String, SdkToolsMultiPackageNode> tmpMulti = new HashMap<>();
        Map<String, SdkToolsMultiPackageNode> tmpMultiSupport = new HashMap<>();
        toolRoot = new SdkToolsRootNode();
        SdkToolsSupportNode supportNode = new SdkToolsSupportNode(toolRoot);
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
                            SdkToolsMultiPackageNode node = tmpMulti.get(prefix);
                            node.addNode(new SdkToolsPackageNode(node, p));
                        } else {
                            SdkToolsMultiPackageNode node = new SdkToolsMultiPackageNode(toolRoot, prefix);
                            node.addNode(new SdkToolsPackageNode(node, p));
                            tmpMulti.put(prefix, node);
                            toolRoot.addNode(node);
                        }
                    } else if (tmpMultiSupport.containsKey(prefix)) {
                        SdkToolsMultiPackageNode node = tmpMultiSupport.get(prefix);
                        node.addNode(new SdkToolsPackageNode(node, p));
                    } else {
                        SdkToolsMultiPackageNode node = new SdkToolsMultiPackageNode(supportNode, prefix);
                        node.addNode(new SdkToolsPackageNode(node, p));
                        tmpMultiSupport.put(prefix, node);
                        supportNode.addNode(node);
                    }
                    found = true;
                }
            }
            if (!found) {
                if (p.getRepresentative().getPath().endsWith(RepoPackage.PATH_SEPARATOR + MavenInstallListener.MAVEN_DIR_NAME)) {
                    supportNode.addNode(new SdkToolsPackageNode(toolRoot, p));
                } else {
                    toolRoot.addNode(new SdkToolsPackageNode(toolRoot, p));
                }
            }
        }
        toolRoot.addNode(supportNode);

        for (SdkToolsChangeListener toolsListener : toolsListeners) {
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
