/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.layout.spi;

import java.io.File;
import java.io.InputStream;
import java.util.List;
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
    protected final List<File> aars;
    protected final List<File> jars;

    public LayoutPreviewPanel(File platformFolder, File layoutFile, File appResFolder, String themeName, List<File> aars, List<File> jars) {
        this.platformFolder = platformFolder;
        this.layoutFile = layoutFile;
        this.appResFolder = appResFolder;
        this.themeName = themeName;
        this.aars = aars;
        this.jars = jars;
    }

    public LayoutPreviewPanel() {
        this.platformFolder = null;
        this.layoutFile = null;
        this.appResFolder = null;
        this.themeName = null;
        this.aars = null;
        this.jars = null;
    }

    public abstract void refreshPreview(InputStream stream);

    public abstract void showTypingIndicator();

}
