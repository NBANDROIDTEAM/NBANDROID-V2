/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.resources.ResourceType;
import static com.android.resources.ResourceType.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author arsi
 */
public class ResourceClassGeneratorConfig {

    private static final int NAMESPACE_START = 0x01000000;//after first add is 0x02000000
    private static final int APP_NAMESPACE = 0x7f000000;
    public static final int ANDROID_NAMESPACE = 0x01000000;
    public static final int NAMESPACE_ADD = 0x01000000;
    public static final int NAMESPACE_MASK = 0xFF000000;

    private final Map<Integer, SingleNamespaceGenerator> namespaceStartToGerator = new HashMap<>();
    private final Map<ResourceNamespace, SingleNamespaceGenerator> namespaceToGerator = new HashMap<>();
    private final AtomicInteger namespaceCounter = new AtomicInteger(NAMESPACE_START);
    private final ResourceNamespace appNamespace;

    public ResourceClassGeneratorConfig(ResourceNamespace appNamespace) {
        this.appNamespace = appNamespace;
    }

    public int getOrCreateId(ResourceNamespace namespace, ResourceType resourceType, String name) {
        SingleNamespaceGenerator generator = namespaceToGerator.get(namespace);
        if (generator == null) {
            if (appNamespace.equals(namespace)) {
                generator = new SingleNamespaceGenerator(APP_NAMESPACE, appNamespace);
                namespaceStartToGerator.put(APP_NAMESPACE, generator);
                namespaceToGerator.put(appNamespace, generator);
            } else if (ResourceNamespace.ANDROID.equals(namespace)) {
                generator = new SingleNamespaceGenerator(ANDROID_NAMESPACE, ResourceNamespace.ANDROID);
                namespaceStartToGerator.put(ANDROID_NAMESPACE, generator);
                namespaceToGerator.put(ResourceNamespace.ANDROID, generator);
            } else {
                int namepacePackageId = namespaceCounter.addAndGet(NAMESPACE_ADD);
                if (APP_NAMESPACE == namepacePackageId || ANDROID_NAMESPACE == namepacePackageId) {
                    namepacePackageId = namespaceCounter.addAndGet(NAMESPACE_ADD);//go to next namespace
                }
                generator = new SingleNamespaceGenerator(namepacePackageId, namespace);
                namespaceStartToGerator.put(namepacePackageId, generator);
                namespaceToGerator.put(namespace, generator);
            }
        }
        return generator.getOrCreateId(namespace, resourceType, name);
    }

    public ResourceReference findReference(int id) {
        int namepacePackageId = id & NAMESPACE_MASK;
        SingleNamespaceGenerator generator = namespaceStartToGerator.get(namepacePackageId);
        if (generator != null) {
            return generator.findReference(id);
        }
        return null;
    }

    public static class SingleNamespaceGenerator {

        private final Map<ResourceType, AtomicInteger> counterMap = new HashMap<>();
        private final int namepacePackageId;
        private final ResourceNamespace namespace;

        public ResourceNamespace getNamespace() {
            return namespace;
        }

        public int getNamepacePackageId() {
            return namepacePackageId;
        }

        public SingleNamespaceGenerator(int namepacePackageId, ResourceNamespace namespace) {
            this.namepacePackageId = namepacePackageId;
            this.namespace = namespace;
            counterMap.put(ANIM, new AtomicInteger(namepacePackageId + 0x010000));
            counterMap.put(ANIMATOR, new AtomicInteger(namepacePackageId + 0x020000));
            counterMap.put(ARRAY, new AtomicInteger(namepacePackageId + 0x030000));
            counterMap.put(ATTR, new AtomicInteger(namepacePackageId + 0x040000));
            counterMap.put(BOOL, new AtomicInteger(namepacePackageId + 0x050000));
            counterMap.put(COLOR, new AtomicInteger(namepacePackageId + 0x060000));
            counterMap.put(DECLARE_STYLEABLE, new AtomicInteger(namepacePackageId + 0x070000));
            counterMap.put(DIMEN, new AtomicInteger(namepacePackageId + 0x080000));
            counterMap.put(DRAWABLE, new AtomicInteger(namepacePackageId + 0x090000));
            counterMap.put(FONT, new AtomicInteger(namepacePackageId + 0x0A0000));
            counterMap.put(FRACTION, new AtomicInteger(namepacePackageId + 0x0B0000));
            counterMap.put(ID, new AtomicInteger(namepacePackageId + 0x0c0000));
            counterMap.put(INTEGER, new AtomicInteger(namepacePackageId + 0x0d0000));
            counterMap.put(INTERPOLATOR, new AtomicInteger(namepacePackageId + 0x0e0000));
            counterMap.put(LAYOUT, new AtomicInteger(namepacePackageId + 0x0f0000));
            counterMap.put(MENU, new AtomicInteger(namepacePackageId + 0x100000));
            counterMap.put(MIPMAP, new AtomicInteger(namepacePackageId + 0x011000));
            counterMap.put(NAVIGATION, new AtomicInteger(namepacePackageId + 0x120000));
            counterMap.put(PLURALS, new AtomicInteger(namepacePackageId + 0x130000));
            counterMap.put(RAW, new AtomicInteger(namepacePackageId + 0x140000));
            counterMap.put(STRING, new AtomicInteger(namepacePackageId + 0x150000));
            counterMap.put(STYLE, new AtomicInteger(namepacePackageId + 0x160000));
            counterMap.put(STYLEABLE, new AtomicInteger(namepacePackageId + 0x170000));
            counterMap.put(TRANSITION, new AtomicInteger(namepacePackageId + 0x180000));
            counterMap.put(XML, new AtomicInteger(namepacePackageId + 0x180000));
        }

        private AtomicInteger getCounter(ResourceType resourceType) {
            return counterMap.get(resourceType);
        }

        private final Map<ResourceType, Map<ResourceReference, Integer>> resources = new HashMap<>();
        private final Map<ResourceType, Map<String, Integer>> resourceNamesToId = new HashMap<>();
        private final Map<Integer, ResourceReference> resourcesReverse = new HashMap<>();

        public int getOrCreateId(ResourceNamespace namespace, ResourceType resourceType, String name) {
            Map<ResourceReference, Integer> references = resources.get(resourceType);
            Map<String, Integer> names = resourceNamesToId.get(resourceType);
            if (references == null) {
                references = new HashMap<>();
                resources.put(resourceType, references);
            }
            if (names == null) {
                names = new HashMap<>();
                resourceNamesToId.put(resourceType, names);
            }
            ResourceReference reference = new ResourceReference(namespace, resourceType, name);
            Integer value = references.get(reference);
            if (value == null) {
                value = getCounter(resourceType).incrementAndGet();
                references.put(reference, value);
                names.put(name, value);
                resourcesReverse.put(value, reference);
            }
            return value;
        }

        public void updateReference(ResourceNamespace namespace, ResourceType resourceType, String name, int value) {
            Map<ResourceReference, Integer> references = resources.get(resourceType);
            if (references != null) {
                ResourceReference reference = new ResourceReference(namespace, resourceType, name);
                references.put(reference, value);
                resourcesReverse.put(value, reference);
            }
        }

        public Integer getIdByName(ResourceType resourceType, String name) {
            Map<String, Integer> names = resourceNamesToId.get(resourceType);
            if (names == null) {
                names = new HashMap<>();
                resourceNamesToId.put(resourceType, names);
            }
            return names.get(name);
        }

        public ResourceReference findReference(int id) {
            return resourcesReverse.get(id);
        }
    }

}
