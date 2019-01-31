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
public class AarResourceSet extends ResourceSet {

    public AarResourceSet(String name, ResourceNamespace namespace, String libraryName, boolean validateEnabled) {
        super(name, namespace, libraryName, validateEnabled);
    }

    @Override
    public boolean isIgnored(File file) {
        String fileName = file.getName();
        if (super.isIgnored(file)) {
            return true;
        }

        // TODO: Restrict the following checks to folders only.
        if (fileName.startsWith("values-mcc") || fileName.startsWith("raw-")) {
            return true; // Mobile country codes and raw resources are not used by LayoutLib.
        }

        // Skip locale-specific folders if myWithLocaleResources is false.
        // Skip files that don't contain resources.

        return fileName.equals("public.xml") || fileName.equals("symbols.xml");
    }
}
