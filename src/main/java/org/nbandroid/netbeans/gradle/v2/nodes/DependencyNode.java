/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.nodes;

import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.Library;
import java.awt.Image;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gradle.impldep.org.apache.commons.io.FilenameUtils;
import org.nbandroid.netbeans.gradle.core.ui.IconProvider;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author arsi
 */
public class DependencyNode extends FilterNode {

    private final Library library;
    public static final String JAVADOC_NAME = "-javadoc.jar";
    public static final String SRC_NAME = "-sources.jar";
    private AtomicBoolean javadocLocal = new AtomicBoolean(false);
    private AtomicBoolean srcLocal = new AtomicBoolean(false);

    public DependencyNode(Node original, Library library) {
        super(original);
        this.library = library;
    }

    @Override
    public Image getIcon(int type) {
        Image icon = super.getIcon(type);
        if (library instanceof AndroidLibrary) {
            AndroidLibrary lib = (AndroidLibrary) library;
            icon = IconProvider.IMG_ANDROID_LIBRARY;
            File jarFile = lib.getJarFile();
            icon = annotateBroken(jarFile, icon);
            File bundle = lib.getBundle();
            icon = annotateSrcAndJavaDoc(bundle, icon);
        } else if (library instanceof JavaLibrary) {
            JavaLibrary lib = (JavaLibrary) library;
            File jarFile = lib.getJarFile();
            icon = annotateBroken(jarFile, icon);
            icon = annotateSrcAndJavaDoc(jarFile, icon);
        }

        return icon;
    }

    private Image annotateSrcAndJavaDoc(File bundle, Image icon) {
        if (bundle != null) {
            File bundleRoot = bundle.getParentFile();
            if (bundleRoot != null) {
                String name = bundle.getName();
                String baseName = FilenameUtils.getBaseName(name);
                String javaDoc = baseName + JAVADOC_NAME;
                String src = baseName + SRC_NAME;
                if (new File(bundleRoot, javaDoc).exists()) {
                    icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVADOCINCLUDED, 12, 0);
                    javadocLocal.set(true);
                } else {
                    javadocLocal.set(false);
                }
                if (new File(bundleRoot, src).exists()) {
                    icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_JAVASRCINCLUDED, 12, 8);
                    srcLocal.set(true);
                } else {
                    srcLocal.set(false);
                }
            }
        }
        return icon;
    }

    private Image annotateBroken(File jarFile, Image icon) {
        if (jarFile != null && !jarFile.exists()) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_BROKEN, 0, 0);
        } else if (jarFile == null) {
            icon = ImageUtilities.mergeImages(icon, IconProvider.IMG_DEPENDENCY_BROKEN, 0, 0);
        }
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        String versionlessId = library.getResolvedCoordinates().getVersionlessId();
        if (versionlessId.contains("_local_jars_")) {
            int lastIndexOf = versionlessId.lastIndexOf('/');
            if (lastIndexOf == -1) {
                lastIndexOf = versionlessId.lastIndexOf('\\');
            }
            if (lastIndexOf > -1) {
                versionlessId = versionlessId.substring(lastIndexOf + 1);
            }
        }
        return versionlessId;
    }

}
