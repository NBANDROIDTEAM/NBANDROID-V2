/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.arsi.netbeans.gradle.android.layout.impl.android;

import static com.android.SdkConstants.NEW_ID_PREFIX;
import static com.android.SdkConstants.PREFIX_THEME_REF;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.ide.common.rendering.api.*;
import static com.android.ide.common.resources.AbstractResourceRepository.MAX_RESOURCE_INDIRECTION;
import com.android.ide.common.resources.ResourceValueMap;
import com.android.ide.common.resources.sampledata.SampleDataManager;
import com.android.resources.ResourceType;
import com.android.resources.ResourceUrl;
import com.google.common.base.Joiner;
import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>NOTE:</b> LayoutLib tests depend on this class.
 */
public class ResourceResolver extends RenderResources {

    public static final String THEME_NAME = "Theme";
    public static final String THEME_NAME_DOT = "Theme.";

    /**
     * Constant passed to {@link #setDeviceDefaults(String)} to indicate the
     * DeviceDefault styles should point to the default styles
     */
    public static final String LEGACY_THEME = "";

    public static final Pattern DEVICE_DEFAULT_PATTERN
            = Pattern.compile("(\\p{Alpha}+)?\\.?DeviceDefault\\.?(.+)?");

    private final Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> mResources;
    private final Map<StyleResourceValue, StyleResourceValue> mStyleInheritanceMap
            = new HashMap<>();
    private final Multimap<StyleResourceValue, StyleResourceValue> mReverseStyleInheritanceMap
            = HashMultimap.create();

    @Nullable
    private final StyleResourceValue mDefaultTheme;

    /**
     * The resources should be searched in all the themes in the list in order.
     */
    @NonNull
    private final List<StyleResourceValue> mThemes;

    @NonNull
    private Predicate<ResourceReference> mProjectIdChecker = res -> false;
    private LayoutLog mLogger;

    /**
     * Contains the default parent for DeviceDefault styles (e.g. for API 18,
     * "Holo")
     */
    private String mDeviceDefaultParent;

    private ResourceResolver(
            @NonNull Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> resources,
            @Nullable StyleResourceValue theme) {
        mResources = resources;
        mDefaultTheme = theme;
        mThemes = new LinkedList<>();
    }

    /**
     * Creates a new {@link ResourceResolver} object.
     *
     * @param resources all resources.
     * @param themeReference reference to the theme to be used.
     * @return a new {@link ResourceResolver}
     */
    public static ResourceResolver create(
            @NonNull Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> resources,
            @Nullable ResourceReference themeReference) {
        StyleResourceValue theme = null;
        if (themeReference != null) {
            assert themeReference.getResourceType() == ResourceType.STYLE;
            theme = findTheme(themeReference, resources);
        }
        ResourceResolver resolver = new ResourceResolver(resources, theme);
        resolver.preProcessStyles();
        return resolver;
    }

    @Nullable
    private static StyleResourceValue findTheme(
            @NonNull ResourceReference themeReference,
            @NonNull Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> resources) {
        Map<ResourceType, ResourceValueMap> namespaceMap
                = resources.get(themeReference.getNamespace());
        if (namespaceMap == null) {
            return null;
        }

        ResourceValueMap stylesMap = namespaceMap.get(ResourceType.STYLE);
        if (stylesMap == null) {
            return null;
        }

        ResourceValue resourceValue = stylesMap.get(themeReference.getName());

        return resourceValue instanceof StyleResourceValue
                ? (StyleResourceValue) resourceValue
                : null;
    }

    /**
     * Creates a new {@link ResourceResolver} copied from the given instance.
     *
     * @return a new {@link ResourceResolver} or null if the passed instance is
     * null
     */
    @Nullable
    public static ResourceResolver copy(@Nullable ResourceResolver original) {
        if (original == null) {
            return null;
        }

        ResourceResolver resolver
                = new ResourceResolver(original.mResources, original.mDefaultTheme);
        resolver.mProjectIdChecker = original.mProjectIdChecker;
        resolver.mLogger = original.mLogger;
        resolver.mStyleInheritanceMap.putAll(original.mStyleInheritanceMap);
        resolver.mReverseStyleInheritanceMap.putAll(original.mReverseStyleInheritanceMap);
        resolver.mThemes.addAll(original.mThemes);

        return resolver;
    }

    /**
     * Creates a new {@link ResourceResolver} which contains only the given
     * {@link ResourceValue} objects, indexed by namespace, type and name. There
     * can be no duplicate (namespace, type, name) tuples in the input.
     *
     * <p>
     * This method is meant for testing, where other components need to set up a
     * simple {@link
     * ResourceResolver} with known contents.
     */
    @NonNull
    @VisibleForTesting
    public static ResourceResolver withValues(@NonNull ResourceValue... values) {
        return withValues(Arrays.asList(values), null);
    }

    /**
     * Creates a new {@link ResourceResolver} which contains only the given
     * {@link ResourceValue} objects, indexed by namespace, type and name. There
     * can be no duplicate (namespace, type, name) tuples in the input.
     *
     * <p>
     * This method is meant for testing, where other components need to set up a
     * simple {@link
     * ResourceResolver} with known contents.
     */
    @NonNull
    @VisibleForTesting
    public static ResourceResolver withValues(
            @NonNull Iterable<ResourceValue> values, @Nullable ResourceReference themeReference) {
        Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> resources = new HashMap<>();
        for (ResourceValue value : values) {
            Map<ResourceType, ResourceValueMap> byType
                    = resources.computeIfAbsent(
                            value.getNamespace(), ns -> new EnumMap<>(ResourceType.class));
            ResourceValueMap resourceValueMap
                    = byType.computeIfAbsent(value.getResourceType(), t -> ResourceValueMap.create());
            checkArgument(
                    !resourceValueMap.containsKey(value.getName()), "Duplicate resource: " + value);
            resourceValueMap.put(value);
        }

        return create(resources, themeReference);
    }

    @Nullable
    private ResourceValueMap getResourceValueMap(
            @NonNull ResourceNamespace namespace, @NonNull ResourceType type) {
        Map<ResourceType, ResourceValueMap> row = mResources.get(namespace);
        return row != null ? row.get(type) : null;
    }

    /**
     * This will override the DeviceDefault styles so they point to the given
     * parent styles (e.g. If "Material" is passed, Theme.DeviceDefault parent
     * will become Theme.Material). This patches all the styles (not only
     * themes) and takes care of the light and dark variants. If {@link
     * #LEGACY_THEME} is passed, parents will be directed to the default themes
     * (i.e. Theme).
     */
    public void setDeviceDefaults(@NonNull String deviceDefaultParent) {
        if (deviceDefaultParent.equals(mDeviceDefaultParent)) {
            // No need to patch again with the same parent.
            return;
        }

        mDeviceDefaultParent = deviceDefaultParent;
        // The joiner will ignore nulls so if the caller specified an empty name, we replace it with
        // a null so it gets ignored.
        String parentName = Strings.emptyToNull(deviceDefaultParent);

        // TODO(namespaces): why only framework styles?
        ResourceValueMap frameworkStyles
                = getResourceValueMap(ResourceNamespace.ANDROID, ResourceType.STYLE);
        if (frameworkStyles == null) {
            return;
        }

        for (ResourceValue value : frameworkStyles.values()) {
            // The regexp gets the prefix and suffix if they exist (without the dots).
            Matcher matcher = DEVICE_DEFAULT_PATTERN.matcher(value.getName());
            if (!matcher.matches()) {
                continue;
            }

            String newParentStyle
                    = Joiner.on('.').skipNulls().join(matcher.group(1), parentName,
                    ((matcher.groupCount() > 1) ? matcher.group(2) : null));
            patchFrameworkStyleParent(value.getName(), newParentStyle);
        }
    }

    /**
     * Updates the parent of a given framework style. This method is used to
     * patch DeviceDefault styles when using a CompatibilityTarget.
     */
    private void patchFrameworkStyleParent(String childStyleName, String parentName) {
        // TODO(namespaces): why only framework styles?
        ResourceValueMap map = getResourceValueMap(ResourceNamespace.ANDROID, ResourceType.STYLE);
        if (map != null) {
            StyleResourceValue from = (StyleResourceValue) map.get(childStyleName);
            StyleResourceValue to = (StyleResourceValue) map.get(parentName);

            if (from != null && to != null) {
                StyleResourceValue newStyle
                        = new StyleResourceValueImpl(
                                from.asReference(), parentName, from.getLibraryName());
                newStyle.replaceWith(from);
                mStyleInheritanceMap.put(newStyle, to);
                mReverseStyleInheritanceMap.clear();
            }
        }
    }

    // ---- Methods to help dealing with older LayoutLibs.
    @Nullable
    public StyleResourceValue getTheme() {
        return mDefaultTheme;
    }

    @Deprecated // TODO(namespaces)
    public Map<ResourceType, ResourceValueMap> getProjectResources() {
        return mResources.get(ResourceNamespace.RES_AUTO);
    }

    @Deprecated // TODO(namespaces)
    public Map<ResourceType, ResourceValueMap> getFrameworkResources() {
        return mResources.get(ResourceNamespace.ANDROID);
    }

    /**
     * Provides a mechanism to check if a given resource of type
     * {@link ResourceType#ID} is defined in the project.
     *
     * <p>
     * TODO(namespaces): stop relying on R.txt and remove this.
     */
    public void setProjectIdChecker(@NonNull Predicate<ResourceReference> predicate) {
        mProjectIdChecker = predicate;
    }

    // ---- RenderResources Methods
    @Override
    public void setLogger(LayoutLog logger) {
        mLogger = logger;
    }

    @Override
    public StyleResourceValue getDefaultTheme() {
        return mDefaultTheme;
    }

    @Override
    public void applyStyle(StyleResourceValue theme, boolean useAsPrimary) {
        if (theme == null) {
            return;
        }
        if (useAsPrimary) {
            mThemes.add(0, theme);
        } else {
            mThemes.add(theme);
        }
    }

    @Override
    public void clearStyles() {
        mThemes.clear();
        if (mDefaultTheme != null) {
            mThemes.add(mDefaultTheme);
        }
    }

    @Override
    public List<StyleResourceValue> getAllThemes() {
        return mThemes;
    }

    @Override
    public boolean themeIsParentOf(StyleResourceValue parentTheme, StyleResourceValue childTheme) {
        do {
            childTheme = mStyleInheritanceMap.get(childTheme);
            if (childTheme == null) {
                return false;
            } else if (childTheme == parentTheme) {
                return true;
            }
        } while (true);
    }

    // TODO: Change return type to StyleItemResourceValue.
    @Override
    @Nullable
    public StyleItemResourceValueImpl findItemInStyle(
            @NonNull StyleResourceValue style, @NonNull ResourceReference attr) {
        for (int depth = 0; depth < MAX_RESOURCE_INDIRECTION; depth++) {
            StyleItemResourceValueImpl item = style.getItem(attr);

            // if we didn't find it, we look in the parent style (if applicable)
            //noinspection VariableNotUsedInsideIf
            if (item != null) {
                return item;
            }

            style = mStyleInheritanceMap.get(style);
            if (style == null) {
                return null;
            }
        }

        if (mLogger != null) {
            mLogger.error(
                    LayoutLog.TAG_BROKEN,
                    String.format(
                            "Cyclic style parent definitions: %1$s",
                            computeCyclicStyleChain(style)),
                    null,
                    null,
                    null);
        }

        return null;
    }

    private String computeCyclicStyleChain(StyleResourceValue style) {
        StringBuilder result = new StringBuilder(100);
        Set<StyleResourceValue> seen = new HashSet<>();
        for (int depth = 0; depth < MAX_RESOURCE_INDIRECTION + 1; depth++) {
            if (depth >= MAX_RESOURCE_INDIRECTION) {
                result.append("...");
                break;
            }

            result.append('"');
            result.append(style.getResourceUrl().getQualifiedName());
            result.append('"');

            if (!seen.add(style)) {
                break;
            }

            StyleResourceValue parentStyle = mStyleInheritanceMap.get(style);
            if (parentStyle == null) {
                break;
            }

            if (style.getParentStyleName() != null) {
                result.append(" specifies parent ");
            } else {
                result.append(" implies parent ");
            }

            style = parentStyle;
        }

        return result.toString();
    }

    @Override
    public ResourceValue getUnresolvedResource(ResourceReference reference) {
        if (reference.getResourceType() == ResourceType.SAMPLE_DATA) {
            return findSampleDataValue(reference);
        }
        ResourceValueMap resourceValueMap
                = getResourceValueMap(reference.getNamespace(), reference.getResourceType());
        if (resourceValueMap != null) {
            ResourceValue v = resourceValueMap.get(reference.getName().replace(".", "_"));
            if (v != null) {
                System.out.println("[" + reference + "][" + v.getValue() + "]");
            } else {
                for (Map.Entry<ResourceNamespace, Map<ResourceType, ResourceValueMap>> entry : mResources.entrySet()) {
                    ResourceNamespace key = entry.getKey();
                    Map<ResourceType, ResourceValueMap> value = entry.getValue();
                    ResourceValueMap vv = value.get(reference.getResourceType());
                    if (vv != null) {
                        v = vv.get(reference.getName().replace(".", "_"));
                        if (v != null) {
                            break;
                        }

                    }
                }
                if (v != null) {
                    System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.android.ResourceResolver.getUnresolvedResource()");
                } else {
                    System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.android.ResourceResolver.getUnresolvedResource()");
                }
                System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.android.ResourceResolver.getUnresolvedResource()");
            }
            return v;
        }

        return null;
    }

    @Override
    @Nullable
    public ResourceValue dereference(@NonNull ResourceValue value) {
        ResourceReference reference = value.getReference();

        if (reference == null
                || !ResourceUrl.isValidName(reference.getName(), reference.getResourceType())) {
            // Looks like the value didn't reference anything. Return null.
            return null;
        }

        // It was successfully parsed as a ResourceUrl, so it cannot be null.
        assert value.getValue() != null;

        if (value.getValue().startsWith(PREFIX_THEME_REF)) {
            // No theme? No need to go further!
            if (mDefaultTheme == null) {
                return null;
            }

            if (reference.getResourceType() != ResourceType.ATTR) {
                // At this time, no support for ?type/name where type is not "attr"
                return null;
            }

            // Now look for the item in the theme, starting with the current one.
            return findItemInTheme(reference);
        } else {
            if (reference.getResourceType() == ResourceType.AAPT) {
                // Aapt resources are synthetic references that do not need to be resolved.
                return null;
            } else if (reference.getResourceType() == ResourceType.SAMPLE_DATA) {
                // Sample data resources are only available within the tools namespace
                return findSampleDataValue(reference);
            }

            ResourceValue result = getUnresolvedResource(reference);
            if (result != null) {
                return result;
            }

            if (value.getValue().startsWith(NEW_ID_PREFIX)) {
                return null;
            }

            if (reference.getResourceType() == ResourceType.ID) {
                // If it was not found and the type is an id, it is possible that the ID was
                // generated dynamically (by the '@+' syntax) when compiling the framework
                // resources or in a library, in which case it was not in the repositories.
                // See FileResourceRepository#myAarDeclaredIds.
                //
                // TODO(namespaces): In the past we asked Bridge for the numeric id of the resource
                // in question, which always returned a non-null ID (potentially generating a new
                // one). In effect we assumed {@link ResourceType#ID} resources in the framework
                // namespace always exist. The repo for framework resources needs to know which ids
                // exist and which don't, so we can get rid of this.
                boolean idExists = reference.isFramework() || mProjectIdChecker.test(reference);

                if (idExists) {
                    // TODO(namespaces): Cache these?
                    return new ResourceValueImpl(reference, null);
                }
            }

            // Didn't find the resource anywhere.
            if (mLogger != null) {
                mLogger.warning(
                        LayoutLog.TAG_RESOURCES_RESOLVE,
                        "Couldn't resolve resource " + reference.getResourceUrl(),
                        null,
                        reference);
            }

            return null;
        }
    }

    /**
     * If the given resource value contains a reference, resolves that reference
     * to the actual value.
     *
     * @param resValue the resource value to resolve
     * @return the resolved resource value, or null if {@code resValue} is null
     */
    @Override
    @Nullable
    public ResourceValue resolveResValue(@Nullable ResourceValue resValue) {
        if (resValue == null) {
            return null;
        }

        boolean referenceToItself = false;
        for (int depth = 0; depth < MAX_RESOURCE_INDIRECTION; depth++) {
            String value = resValue.getValue();
            if (value == null || resValue instanceof ArrayResourceValue) {
                // If there's no value or this an array resource (e.g. <string-array>), return.
                return resValue;
            }

            // Else attempt to find another ResourceValue referenced by this one.
            ResourceValue resolvedResValue = dereference(resValue);

            // If the value did not reference anything, then return the input value.
            if (resolvedResValue == null) {
                return resValue;
            }

            if (resolvedResValue.equals(resValue)) {
                referenceToItself = true;
                break; // Resource value referring to itself.
            }
            // Continue resolution with the new value.
            resValue = resolvedResValue;
        }

        if (mLogger != null) {
            String msg = referenceToItself
                    ? "Infinite cycle trying to resolve '%s': Render may not be accurate."
                    : "Potential infinite cycle trying to resolve '%s': Render may not be accurate.";
            mLogger.error(
                    LayoutLog.TAG_BROKEN,
                    String.format(msg, resValue.getValue()),
                    null,
                    null,
                    null);
        }
        return resValue;
    }

    // ---- Private helper methods.
    private SampleDataManager mSampleDataManager = new SampleDataManager();

    private ResourceValue findSampleDataValue(@NonNull ResourceReference value) {
        String name = value.getName();
        return Optional.ofNullable(
                getResourceValueMap(value.getNamespace(), value.getResourceType()))
                .map(t -> t.get(SampleDataManager.getResourceNameFromSampleReference(name)))
                .filter(SampleDataResourceValue.class::isInstance)
                .map(SampleDataResourceValue.class::cast)
                .map(SampleDataResourceValue::getValueAsLines)
                .map(content -> mSampleDataManager.getSampleDataLine(name, content))
                .map(
                        lineContent
                        -> new ResourceValueImpl(
                                value.getNamespace(),
                                ResourceType.SAMPLE_DATA,
                                name,
                                lineContent))
                .orElse(null);
    }

    /**
     * Computes style information, like the inheritance relation.
     */
    private void preProcessStyles() {
        if (mDefaultTheme == null) {
            return;
        }

        // This method will recalculate the inheritance map so any modifications done by
        // setDeviceDefault will be lost. Set mDeviceDefaultParent to null so when setDeviceDefault
        // is called again, it knows that it needs to modify the inheritance map again.
        mDeviceDefaultParent = null;

        for (Map<ResourceType, ResourceValueMap> mapForNamespace : mResources.values()) {
            ResourceValueMap styles = mapForNamespace.get(ResourceType.STYLE);
            if (styles == null) {
                continue;
            }

            for (ResourceValue value : styles.values()) {
                if (!(value instanceof StyleResourceValue)) {
                    continue;
                }

                StyleResourceValue style = (StyleResourceValue) value;
                ResourceReference parent = style.getParentStyle();

                if (parent != null) {
                    ResourceValue parentStyle = getUnresolvedResource(parent);
                    if (parentStyle instanceof StyleResourceValue) {
                        mStyleInheritanceMap.put(style, (StyleResourceValue) parentStyle);
                        continue; // Don't log below.
                    }
                }

                if (mLogger != null) {
                    mLogger.error(
                            LayoutLog.TAG_RESOURCES_RESOLVE,
                            String.format(
                                    "Unable to resolve parent style name: %s",
                                    style.getParentStyleName()),
                            null,
                            null,
                            null);
                }
            }
        }

        clearStyles();
    }

    private void computeReverseStyleInheritance() {
        for (Map.Entry<StyleResourceValue, StyleResourceValue> entry
                : mStyleInheritanceMap.entrySet()) {
            mReverseStyleInheritanceMap.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    @Nullable
    public StyleResourceValue getParent(@NonNull StyleResourceValue style) {
        return mStyleInheritanceMap.get(style);
    }

    @NonNull
    public Collection<StyleResourceValue> getChildren(@NonNull StyleResourceValue style) {
        if (mReverseStyleInheritanceMap.isEmpty()) {
            computeReverseStyleInheritance();
        }
        return mReverseStyleInheritanceMap.get(style);
    }

    public boolean styleExtends(
            @NonNull StyleResourceValue child, @NonNull StyleResourceValue ancestor) {
        StyleResourceValue current = child;
        while (current != null) {
            if (current.equals(ancestor)) {
                return true;
            }

            current = getParent(current);
        }

        return false;
    }

    @Override
    @Nullable
    public StyleResourceValue getStyle(@NonNull ResourceReference styleReference) {
        ResourceValue style = getUnresolvedResource(styleReference);
        if (style == null) {
            return null;
        }

        if (style instanceof StyleResourceValue) {
            return (StyleResourceValue) style;
        }

        if (mLogger != null) {
            mLogger.error(
                    null,
                    String.format(
                            "Style %1$s is not of type STYLE (instead %2$s)",
                            styleReference, style.getResourceType().toString()),
                    null,
                    null,
                    styleReference);
        }

        return null;
    }

    /**
     * Checks if the given {@link ResourceValue} represents a theme.
     */
    public boolean isTheme(
            @NonNull ResourceValue value, @Nullable Map<ResourceValue, Boolean> cache) {
        if (cache != null) {
            Boolean known = cache.get(value);
            if (known != null) {
                return known;
            }
        }

        if (value instanceof StyleResourceValue) {
            StyleResourceValue styleValue = (StyleResourceValue) value;
            for (int depth = 0; depth < MAX_RESOURCE_INDIRECTION; depth++) {
                String name = styleValue.getName();
                if (styleValue.getNamespace() == ResourceNamespace.ANDROID
                        && (name.equals(THEME_NAME) || name.startsWith(THEME_NAME_DOT))) {
                    if (cache != null) {
                        cache.put(value, true);
                    }
                    return true;
                }

                styleValue = mStyleInheritanceMap.get(styleValue);
                if (styleValue == null) {
                    return false;
                }
            }

            if (mLogger != null) {
                mLogger.error(
                        LayoutLog.TAG_BROKEN,
                        String.format(
                                "Cyclic style parent definitions: %1$s",
                                computeCyclicStyleChain(styleValue)),
                        null,
                        null,
                        null);
            }
        }

        return false;
    }

    /**
     * Creates a new {@link ResourceResolver} which records all resource
     * resolution lookups into the given list. Note that it is the
     * responsibility of the caller to clear/reset the list between subsequent
     * lookup operations.
     *
     * @param lookupChain the list to write resource lookups into
     * @return a new {@link ResourceResolver}
     */
    public ResourceResolver createRecorder(List<ResourceValue> lookupChain) {
        ResourceResolver resolver
                = new RecordingResourceResolver(lookupChain, mResources, mDefaultTheme);
        resolver.mLogger = mLogger;
        resolver.mStyleInheritanceMap.putAll(mStyleInheritanceMap);
        resolver.mThemes.addAll(mThemes);
        return resolver;
    }

    private static class RecordingResourceResolver extends ResourceResolver {

        @NonNull
        private List<ResourceValue> mLookupChain;

        private RecordingResourceResolver(
                @NonNull List<ResourceValue> lookupChain,
                @NonNull Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> resources,
                @Nullable StyleResourceValue theme) {
            super(resources, theme);
            mLookupChain = lookupChain;
        }

        @Override
        public ResourceValue resolveResValue(ResourceValue resValue) {
            if (resValue != null) {
                mLookupChain.add(resValue);
            }

            return super.resolveResValue(resValue);
        }

        @Nullable
        @Override
        public ResourceValue dereference(@NonNull ResourceValue value) {
            if (!mLookupChain.isEmpty()
                    && !mLookupChain.get(mLookupChain.size() - 1).equals(value)) {
                mLookupChain.add(value);
            }

            ResourceValue resValue = super.dereference(value);

            if (resValue != null) {
                mLookupChain.add(resValue);
            }

            return resValue;
        }

        // TODO: Change return type to StyleItemResourceValue.
        @Override
        public StyleItemResourceValueImpl findItemInStyle(
                @NonNull StyleResourceValue style, @NonNull ResourceReference attr) {
            StyleItemResourceValueImpl value = super.findItemInStyle(style, attr);
            if (value != null) {
                mLookupChain.add(value);
            }
            return value;
        }

        @Override
        public ResourceValue findItemInTheme(@NonNull ResourceReference attr) {
            ResourceValue value = super.findItemInTheme(attr);
            if (value != null) {
                mLookupChain.add(value);
            }
            return value;
        }
    }

    /**
     * @deprecated Use {@link #getDefaultTheme()} or {@link #getAllThemes()}
     */
    @Deprecated
    public StyleResourceValue getCurrentTheme() {
        // Default theme is same as the current theme was on older versions of the API.
        // With the introduction of applyStyle() "current" theme makes little sense.
        // Hence, simply return defaultTheme.
        return getDefaultTheme();
    }

    /**
     * @deprecated Use {@link #getStyle(ResourceReference)}
     */
    @Deprecated
    public StyleResourceValue getTheme(String name, boolean frameworkTheme) {
        return getStyle(
                new ResourceReference(
                        ResourceNamespace.fromBoolean(frameworkTheme), ResourceType.STYLE, name));
    }

    /**
     * @deprecated Use {@link #getResolvedResource(ResourceReference)}
     */
    @Deprecated
    public ResourceValue getFrameworkResource(ResourceType resourceType, String resourceName) {
        return getResolvedResource(
                new ResourceReference(ResourceNamespace.ANDROID, resourceType, resourceName));
    }

    /**
     * @deprecated Use {@link #getResolvedResource(ResourceReference)}
     */
    @Deprecated
    public ResourceValue getProjectResource(ResourceType resourceType, String resourceName) {
        return getResolvedResource(
                new ResourceReference(ResourceNamespace.RES_AUTO, resourceType, resourceName));
    }

    /**
     * @deprecated Use {@link #findItemInTheme(ResourceReference)}.
     */
    @Deprecated
    @Nullable
    public final ResourceValue findItemInTheme(String attrName, boolean isFrameworkAttr) {
        return findItemInTheme(
                new ResourceReference(
                        ResourceNamespace.fromBoolean(isFrameworkAttr),
                        ResourceType.ATTR,
                        attrName));
    }

    /**
     * @deprecated Use
     * {@link #findItemInStyle(StyleResourceValue, ResourceReference)}.
     */
    @Deprecated
    @Nullable
    public final ResourceValue findItemInStyle(
            StyleResourceValue style, String attrName, boolean isFrameworkAttr) {
        return findItemInStyle(
                style,
                new ResourceReference(
                        ResourceNamespace.fromBoolean(isFrameworkAttr),
                        ResourceType.ATTR,
                        attrName));
    }

    /**
     * Returns the style matching the given name. The name should not contain
     * any namespace prefix.
     *
     * @param styleName Name of the style. For example,
     * "Widget.ListView.DropDown".
     * @return the {link StyleResourceValue} for the style, or null if not
     * found.
     * @deprecated Use {@link #getStyle(ResourceReference)}
     */
    @Deprecated
    public final StyleResourceValue getStyle(String styleName, boolean isFramework) {
        return getStyle(
                new ResourceReference(
                        ResourceNamespace.fromBoolean(isFramework), ResourceType.STYLE, styleName));
    }
}
