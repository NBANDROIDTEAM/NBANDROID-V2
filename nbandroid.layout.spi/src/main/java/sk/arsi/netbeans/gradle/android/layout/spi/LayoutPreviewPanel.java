/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.layout.spi;

import java.io.File;
import javax.swing.JPanel;

/**
 *
 * @author arsi
 */
public abstract class LayoutPreviewPanel extends JPanel {

    protected final File platformFolder;
    protected final File layoutFile;
    protected final File appResFolder;
    protected final String themeName;

    public LayoutPreviewPanel(File platformFolder, File layoutFile, File appResFolder, String themeName) {
        this.platformFolder = platformFolder;
        this.layoutFile = layoutFile;
        this.appResFolder = appResFolder;
        this.themeName = themeName;
    }

    public LayoutPreviewPanel() {
        this.platformFolder = null;
        this.layoutFile = null;
        this.appResFolder = null;
        this.themeName = null;
    }

}
