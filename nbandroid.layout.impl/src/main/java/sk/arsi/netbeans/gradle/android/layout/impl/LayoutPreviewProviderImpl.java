/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import android.annotation.NonNull;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.resources.FrameworkResources;
import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.io.FolderWrapper;
import com.android.layoutlib.bridge.android.RenderParamsFlags;
import com.android.tools.nbandroid.layoutlib.ConfigGenerator;
import java.io.File;
import javax.swing.JPanel;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = LayoutPreviewProvider.class)
public class LayoutPreviewProviderImpl extends LayoutPreviewProvider {

    private ResourceRepository sProjectResources;
    private FrameworkResources sFrameworkRepo;
    private File appResFolder;

    @Override
    public JPanel getPreview(File platformFolder, File layoutFile, File appResFolder, String themeName) {
        LayoutPreviewPanelImpl imagePanel = new LayoutPreviewPanelImpl(platformFolder, layoutFile, appResFolder, themeName);
//        this.appResFolder = appResFolder;
//        try {
//            LayoutLibrary layoutLibrary = LayoutLibraryLoader.load(platformFolder);
//            RenderSession session = layoutLibrary.createSession(getSessionParams(platformFolder, new LayoutPullParser(layoutFile), ConfigGenerator.NEXUS_7, new LayoutLibTestCallback(new LogWrapper()), themeName, true, SessionParams.RenderingMode.NORMAL, 27));
//            Result renderResult = session.render();
//            if (renderResult.getException() != null) {
//                renderResult.getException().printStackTrace();
//            }
//            imagePanel.setImage(session.getImage());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        return imagePanel;
    }

    protected SessionParams getSessionParams(File platformFolder, LayoutPullParser layoutParser,
            ConfigGenerator configGenerator, LayoutLibTestCallback layoutLibCallback,
            String themeName, boolean isProjectTheme, SessionParams.RenderingMode renderingMode,
            @SuppressWarnings("SameParameterValue") int targetSdk) {
        File data_dir = new File(platformFolder, "data");
        File res = new File(data_dir, "res");
        sFrameworkRepo = FrameworkResourcesCache.getOrCreateFrameworkResources(res);
        sProjectResources = new ResourceRepository(new FolderWrapper(appResFolder),
                false) {
            @NonNull
            @Override
            protected ResourceItem createResourceItem(@NonNull String name) {
                return new ResourceItem(name);
            }
        };
        sProjectResources.loadResources();
        FolderConfiguration config = configGenerator.getFolderConfig();
        ResourceResolver resourceResolver = ResourceResolver.create(sProjectResources.getConfiguredResources(config), sFrameworkRepo.getConfiguredResources(config), themeName, isProjectTheme);

        SessionParams sessionParams
                = new SessionParams(layoutParser, renderingMode, null /*used for caching*/,
                        configGenerator.getHardwareConfig(), resourceResolver, layoutLibCallback, 0,
                        targetSdk, new LayoutLog());
        sessionParams.setFlag(RenderParamsFlags.FLAG_DO_NOT_RENDER_ON_CREATE, true);
        sessionParams.setAssetRepository(new TestAssetRepository());
        return sessionParams;
    }

}
