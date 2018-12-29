package org.nbandroid.netbeans.ext.navigation;

import org.nbandroid.netbeans.gradle.api.ResourceRef;

/**
 *
 * @author radim
 */
public interface ResourceLocator {

    ResourceLocation findResourceLocation(ResourceRef ref);
}
