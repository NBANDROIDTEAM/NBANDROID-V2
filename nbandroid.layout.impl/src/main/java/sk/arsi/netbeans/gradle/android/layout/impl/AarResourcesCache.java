/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import android.annotation.NonNull;
import com.android.io.FolderWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import sk.arsi.netbeans.gradle.android.layout.impl.android.ResourceItem;
import sk.arsi.netbeans.gradle.android.layout.impl.android.ResourceRepository;

/**
 *
 * @author arsi
 */
public class AarResourcesCache {

    private static final LoadingCache<File, ResourceRepository> cache = CacheBuilder.newBuilder().softValues().build(new CacheLoader<File, ResourceRepository>() {
        @Override
        public ResourceRepository load(File key) throws Exception {
            File resFolder = new File(key.getPath() + File.separator + "res");
            if (resFolder.exists() && resFolder.isDirectory()) {
                ResourceRepository aarResources = new ResourceRepository(new FolderWrapper(resFolder), false, ResourceClassGenerator.findAarNamespace(key)) {
                    @NonNull
                    @Override
                    protected ResourceItem createResourceItem(@NonNull String name) {
                        return new ResourceItem(name) {

                        };
                    }

                };
                aarResources.loadResources();
                return aarResources;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    });

    public static ResourceRepository getOrCreateAarResources(File platformResFolder) {
        try {
            return cache.getUnchecked(platformResFolder);
        } catch (Exception e) {
            return null;
        }
    }
}
