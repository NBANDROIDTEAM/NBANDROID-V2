/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
