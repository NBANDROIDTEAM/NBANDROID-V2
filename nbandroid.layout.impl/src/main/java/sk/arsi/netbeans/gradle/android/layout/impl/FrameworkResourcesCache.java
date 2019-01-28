/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.io.FolderWrapper;
import com.android.tools.nbandroid.layoutlib.LogWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import sk.arsi.netbeans.gradle.android.layout.impl.android.FrameworkResources;

/**
 *
 * @author arsi
 */
public class FrameworkResourcesCache {

    private static final LoadingCache<File, FrameworkResources> cache = CacheBuilder.newBuilder().softValues().build(new CacheLoader<File, FrameworkResources>() {

        @Override
        public FrameworkResources load(File key) throws Exception {
            FrameworkResources sFrameworkRepo = new FrameworkResources(new FolderWrapper(key));
            sFrameworkRepo.loadResources();
            sFrameworkRepo.loadPublicResources(new LogWrapper());
            return sFrameworkRepo;
        }
    });

    public static FrameworkResources getOrCreateFrameworkResources(File platformResFolder) {
        return cache.getUnchecked(platformResFolder);
    }
}
