/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.layout.impl.v2;

import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.resources.ResourceSet;
import java.io.File;

/**
 *
 * @author arsi
 */
public class FrameworkResourceSet extends ResourceSet {

    private final boolean myWithLocaleResources;

    public FrameworkResourceSet(File resourceFolder, boolean withLocaleResources) {
        super("AndroidFramework", ResourceNamespace.ANDROID, null, false);
        myWithLocaleResources = withLocaleResources;
        addSource(resourceFolder);
        setShouldParseResourceIds(true);
        setTrackSourcePositions(false);
        setCheckDuplicates(false);
    }

    @Override
    public boolean isIgnored(File file) {
        if (super.isIgnored(file)) {
            return true;
        }

        String fileName = file.getName();
        // TODO: Restrict the following checks to folders only.
        if (fileName.startsWith("values-mcc") || fileName.startsWith("raw-")) {
            return true; // Mobile country codes and raw resources are not used by LayoutLib.
        }

        // Skip locale-specific folders if myWithLocaleResources is false.
        if (fileName.startsWith("values-")) {
            return true;
        }
        // Skip files that don't contain resources.

        return fileName.equals("public.xml") || fileName.equals("symbols.xml");
    }
}
