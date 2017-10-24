package org.nbandroid.netbeans.ext.navigation;

import org.netbeans.modules.android.project.api.ResourceRef;

/**
 *
 * @author radim
 */
public interface ResourceLocator {

  ResourceLocation findResourceLocation(ResourceRef ref);
}
