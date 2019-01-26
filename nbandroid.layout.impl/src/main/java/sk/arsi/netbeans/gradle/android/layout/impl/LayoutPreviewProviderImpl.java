/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = LayoutPreviewProvider.class)
public class LayoutPreviewProviderImpl extends LayoutPreviewProvider {

    @Override
    public LayoutPreviewPanel getPreview(File platformFolder, File layoutFile, File appResFolder, String themeName, List<File> aars) {
        LayoutPreviewPanelImpl imagePanel = new LayoutPreviewPanelImpl(platformFolder, layoutFile, appResFolder, themeName, new ArrayList<>(aars));
        return imagePanel;
    }

}
