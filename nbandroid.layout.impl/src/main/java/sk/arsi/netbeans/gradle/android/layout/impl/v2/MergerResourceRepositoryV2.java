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

package sk.arsi.netbeans.gradle.android.layout.impl.v2;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.resources.AbstractResourceRepository;
import static com.android.ide.common.resources.AbstractResourceRepository.ITEM_MAP_LOCK;
import com.android.ide.common.resources.KnownNamespacesMap;
import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceMerger;
import com.android.ide.common.resources.ResourceRepositories;
import com.android.ide.common.resources.ResourceTable;
import com.android.ide.common.resources.ResourceValueMap;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.resources.ResourceType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author arsi
 */
public class MergerResourceRepositoryV2 extends AbstractResourceRepository {

    private final ResourceTable resourceTable = new ResourceTable();

    @NonNull
    @Override
    protected ResourceTable getFullTable() {
        return resourceTable;
    }

    @Nullable
    @Override
    protected ListMultimap<String, ResourceItem> getMap(
            @NonNull ResourceNamespace namespace, @NonNull ResourceType type, boolean create) {
        ListMultimap<String, ResourceItem> multimap = resourceTable.get(namespace, type);
        if (multimap == null && create) {
            multimap = ArrayListMultimap.create();
            resourceTable.put(namespace, type, multimap);
        }
        return multimap;
    }

    @NonNull
    @Override
    public Set<ResourceNamespace> getNamespaces() {
        return resourceTable.rowKeySet();
    }

    public void update(@NonNull ResourceMerger merger) {
        ResourceRepositories.updateTableFromMerger(merger, resourceTable);
    }

    @Override
    public Table<ResourceNamespace, ResourceType, ResourceValueMap> getConfiguredResources(
            @NonNull FolderConfiguration referenceConfig) {
        synchronized (ITEM_MAP_LOCK) {
            Set<ResourceNamespace> namespaces = getNamespaces();

            Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> backingMap;
            if (KnownNamespacesMap.canContainAll(namespaces)) {
                backingMap = new KnownNamespacesMap<>();
            } else {
                backingMap = new HashMap<>();
            }
            Table<ResourceNamespace, ResourceType, ResourceValueMap> table
                    = Tables.newCustomTable(backingMap, () -> new EnumMap<>(ResourceType.class));

            for (ResourceNamespace namespace : namespaces) {
                // TODO(namespaces): Move this method to ResourceResolverCache.
                // For performance reasons don't mix framework and non-framework resources since
                // they have different life spans.

                for (ResourceType type : ResourceType.values()) {
                    // get the local results and put them in the map
                    table.put(
                            namespace,
                            type,
                            getConfiguredResources(namespace, type, referenceConfig));
                }
            }
            return table;
        }
    }
}
