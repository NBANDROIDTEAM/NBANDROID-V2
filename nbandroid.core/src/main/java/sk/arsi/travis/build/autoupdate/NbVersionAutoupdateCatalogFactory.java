/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.travis.build.autoupdate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author arsi
 */
public class NbVersionAutoupdateCatalogFactory {

    public static final String ORIGINAL_URL = "originalUrl"; // NOI18N
    public static final String ORIGINAL_DISPLAY_NAME = "originalDisplayName"; // NOI18N
    public static final String ORIGINAL_ENABLED = "originalEnabled"; // NOI18N
    public static final String ORIGINAL_CATEGORY_NAME = "originalCategoryName"; // NOI18N
    public static final String ORIGINAL_CATEGORY_ICON_BASE = "originalCategoryIconBase"; // NOI18N
    private static final Logger err = Logger.getLogger(NbVersionAutoupdateCatalogFactory.class.getName());

    public static UpdateProvider createUpdateProvider(FileObject fo) {
        String version = NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion");
        URL url = null;
        String name;
        if (version != null && version.contains("8.1")) {
            try {
                url = new URL("http://server.arsi.sk/nbandroid81/updates.xml");
            } catch (MalformedURLException ex) {
                return null;
            }
        } else {
            try {
                url = new URL("http://server.arsi.sk/nbandroid82/updates.xml");
            } catch (MalformedURLException ex) {
                return null;
            }
        }
        name = fo.getName();
        Preferences providerPreferences = getPreferences().node(name);
        ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class loadClass = classLoader.loadClass("org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider");
            Constructor constructor = loadClass.getConstructor(String.class, String.class, URL.class);
            Object au_catalog = constructor.newInstance(name, displayName(fo), url);
            providerPreferences.put(ORIGINAL_URL, url.toExternalForm());
            providerPreferences.put(ORIGINAL_DISPLAY_NAME, displayName(fo));
            providerPreferences.put(ORIGINAL_CATEGORY_NAME, "COMMUNITY");
            providerPreferences.put(ORIGINAL_CATEGORY_ICON_BASE, "org/netbeans/modules/autoupdate/services/resources/icon-standard.png");

            Boolean en = (Boolean) fo.getAttribute("enabled"); // NOI18N
            if (en != null) {
                providerPreferences.putBoolean(ORIGINAL_ENABLED, en);
            }
            return (UpdateProvider) au_catalog;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node("/org/netbeans/modules/autoupdate"); // NOI18N
    }

    private static String displayName(FileObject fo) {
        String displayName = null;

        if (fo != null) {
            try {
                FileSystem fs = fo.getFileSystem();
                String x = fs.getDecorator().annotateName("", Collections.singleton(fo)); // NOI18N
                if (!x.isEmpty()) {
                    displayName = x;
                }
            } catch (FileStateInvalidException e) {
                // OK, never mind.
            }
        }

        return displayName;
    }
}
