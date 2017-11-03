/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import com.android.repository.api.Channel;
import com.android.repository.api.Installer;
import com.android.repository.api.LocalPackage;
import com.android.repository.api.ProgressRunner;
import com.android.repository.api.RepoManager;
import com.android.repository.api.RepoManager.RepoLoadedCallback;
import com.android.repository.api.RepoPackage;
import com.android.repository.api.SettingsController;
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
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatformManager;
import org.nbandroid.netbeans.gradle.core.sdk.NbDownloader;
import org.nbandroid.netbeans.gradle.core.sdk.NbOutputWindowProgressIndicator;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Default SDK Manager implementation
 *
 * @author arsi
 */
@ServiceProvider(service = SdkManager.class)
public class SdkManagerImpl extends SdkManager implements RepoLoadedCallback {

    private RepoManager repoManager;
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    //max 3 paralel downloads
    public static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(3);
    private final Vector<SdkPlatformChangeListener> listeners = new Vector<>();
    private final Vector<SdkToolsChangeListener> toolsListeners = new Vector<>();
    private SdkPlatformPackagesRootNode platformPackages = null;
    private List<UpdatablePackage> toolsPackages = null;
    private static final Set<String> MULTI_VERSION_PREFIXES
            = ImmutableSet.of(SdkConstants.FD_BUILD_TOOLS, SdkConstants.FD_LLDB, SdkConstants.FD_CMAKE,
                    String.join(String.valueOf(RepoPackage.PATH_SEPARATOR),
                            SdkConstants.FD_EXTRAS,
                            SdkConstants.FD_ANDROID_EXTRAS,
                            SdkConstants.FD_GAPID));
    private SdkToolsRootNode toolRoot;

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

    public SdkManagerImpl() {
        //TODO listen to sdk dir change
        final Runnable runnable = new Runnable() {
            public void run() {
                AndroidSdkHandler sdkManager = DalvikPlatformManager.getDefault().getSdkManager();
                if (sdkManager != null) {
                    repoManager = sdkManager.getSdkManager(new NbOutputWindowProgressIndicator());
                    repoManager.registerLocalChangeListener(SdkManagerImpl.this);
                    repoManager.registerRemoteChangeListener(SdkManagerImpl.this);
                    updateSdkPlatformPackages();
                } else {
                    repoManager = null;
                }
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                pool.submit(runnable);
            }
        });
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
        for (UpdatablePackage info : packages.getConsolidatedPkgs().values()) {
            RepoPackage p = info.getRepresentative();
            TypeDetails details = p.getTypeDetails();
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
            toolsListener.packageListChanged(toolRoot);
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
                Uninstaller uninstaller = installerFactory.createUninstaller(aPackage, SdkManager.getDefault().getRepoManager(), FileOpUtils.create());
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
                Installer installer = installerFactory.createInstaller(aPackage.getRemote(), SdkManager.getDefault().getRepoManager(), new NbDownloader(), FileOpUtils.create());
                NbOutputWindowProgressIndicator indicator = new NbOutputWindowProgressIndicator();
                if (installer.prepare(indicator)) {
                    installer.complete(indicator);
                }
                updateSdkPlatformPackages();
            }
        };

        DOWNLOAD_POOL.execute(runnable);
    }

    private class DownloadErrorCallback implements Runnable {

        @Override
        public void run() {
        }

    }

    private class NbProgressRunner implements ProgressRunner {

        @Override
        public void runAsyncWithProgress(ProgressRunner.ProgressRunnable r) {
            r.run(new NbOutputWindowProgressIndicator(), this);
        }

        @Override
        public void runSyncWithProgress(ProgressRunner.ProgressRunnable r) {
            r.run(new NbOutputWindowProgressIndicator(), this);
        }

        @Override
        public void runSyncWithoutProgress(Runnable r) {
            pool.submit(r);
        }

    }

    private class NbSettingsController implements SettingsController {

        boolean force = false;

        @Override
        public boolean getForceHttp() {
            return force;
        }

        @Override
        public void setForceHttp(boolean force) {
            this.force = force;
        }

        @Override
        public Channel getChannel() {
            //TODO add channel selection to Android settings
            return Channel.create(0);
        }

    }

}
