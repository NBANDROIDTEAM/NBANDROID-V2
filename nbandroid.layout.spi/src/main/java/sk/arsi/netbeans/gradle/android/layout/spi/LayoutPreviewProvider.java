/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.spi;

import java.io.File;
import java.util.List;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public abstract class LayoutPreviewProvider {

    public static LayoutPreviewProvider getDefault() {
        return Lookup.getDefault().lookup(LayoutPreviewProvider.class);
    }

    public abstract LayoutPreviewPanel getPreview(File platformFolder, File layoutFile, File appResFolder, String themeName, List<File> aars);

}
