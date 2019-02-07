/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.ide.common.resources.MergerResourceRepository;
import com.android.ide.common.resources.MergingException;
import com.android.ide.common.resources.ResourceMerger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import org.openide.util.Exceptions;
import sk.arsi.netbeans.gradle.android.layout.impl.v2.FrameworkResourceSet;

/**
 *
 * @author arsi
 */
public class AarResourcesCache {

    private static final LoadingCache<File, MergerResourceRepository> cache = CacheBuilder.newBuilder().softValues().build(new CacheLoader<File, MergerResourceRepository>() {

        @Override
        public MergerResourceRepository load(File res) throws Exception {
            MergerResourceRepository repo = new MergerResourceRepository();
            ResourceMerger resourceMerger = new ResourceMerger(0);

            FrameworkResourceSet framefork = new FrameworkResourceSet(res, false);
            try {
                framefork.loadFromFiles(new LayoutIO());
            } catch (MergingException ex) {
                Exceptions.printStackTrace(ex);
            }
            resourceMerger.addDataSet(framefork);
            repo.update(resourceMerger);
            return repo;
        }
    });

    public static MergerResourceRepository getOrCreateAarResources(File platformResFolder) {
        return cache.getUnchecked(platformResFolder);
    }
}
