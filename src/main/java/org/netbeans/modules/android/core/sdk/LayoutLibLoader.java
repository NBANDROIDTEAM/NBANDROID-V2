package org.netbeans.modules.android.core.sdk;

import com.android.SdkConstants;
import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.rendering.api.AttrResourceValue;
import com.android.ide.common.rendering.api.DeclareStyleableResourceValue;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.resources.FrameworkResources;
import com.android.ide.common.resources.ResourceValueMap;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.sdk.LoadStatus;
import com.android.io.FileWrapper;
import com.android.io.FolderWrapper;
import com.android.resources.ResourceType;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.internal.project.ProjectProperties;
import com.android.utils.ILogger;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author radim
 */
class LayoutLibLoader {

    private static final Logger LOG = Logger.getLogger(LayoutLibLoader.class.getName());

    private final IAndroidTarget target;
    private boolean loaded = false;
    private LayoutLibrary layoutLib;
    private FrameworkResources platformResources;

    public LayoutLibLoader(IAndroidTarget target) {
        this.target = target;
    }

    synchronized LayoutLibrary getLayoutLibrary() {
        if (!loaded) {
            loadLibrary();
        }
        return layoutLib;
    }

    synchronized FrameworkResources getPlatformResources() {
        if (!loaded) {
            loadLibrary();
        }
        return platformResources;
    }

    private void loadLibrary() {
        try {
            final ILogger iLogger = SdkLogProvider.createLogger(true);
            final LayoutLog layoutLogger = null; // SdkLogProvider.createLogger(true);
            LayoutLibrary lib = LayoutLibrary.load(
                    target.getPath(IAndroidTarget.LAYOUT_LIB), iLogger, "NBAndroid");
            if (lib.getStatus() != LoadStatus.LOADED) {
                LOG.log(Level.INFO, "Cannot load layout library: {0}", lib.getLoadMessage());
                return;
            }

            final String resFolderPath = target.getPath(IAndroidTarget.RESOURCES);
            final String fontFolderPath = target.getPath(IAndroidTarget.FONTS);
            final String platformFolderPath = target.isPlatform() ? target.getLocation() : target.getParent().getLocation();
            final File platformFolder = new File(platformFolderPath);
            final File buildProp = new File(platformFolder, SdkConstants.FN_BUILD_PROP);
            LOG.log(Level.FINE, "Target platform {0} uses resource folder {1}, fonts {2}",
                    new Object[]{target, resFolderPath, fontFolderPath});
            platformResources = loadPlatformResources(new File(resFolderPath), iLogger);
            Map<String, Map<String, Integer>> enumMap = loadEnumFlagValues(platformResources);

            final Map<String, String> buildPropMap = ProjectProperties.parsePropertyFile(new FileWrapper(buildProp), iLogger);
            boolean inited = lib.init(buildPropMap, new File(fontFolderPath), enumMap, layoutLogger);
            if (!inited) {
                LOG.log(Level.WARNING, "Cannot initialize layout library");
                return;
            }
            layoutLib = lib;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot load and initialize layout library", ex);
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Returns a map of all enum and flag constants sorted by parent attribute
     * name. The map is attribute_name => (constant_name => integer_value).
     */
    private Map<String, Map<String, Integer>> loadEnumFlagValues(FrameworkResources frameworkResources) {
        // get all the attr values.
        Map<String, Map<String, Integer>> enumMap = Maps.newHashMap();

        FolderConfiguration config = new FolderConfiguration();
        Map<ResourceType, ResourceValueMap> res
                = frameworkResources.getConfiguredResources(config);

        // get the ATTR values
        Map<String, ResourceValue> attrItems = res.get(ResourceType.ATTR);
        Collection<ResourceValue> values = attrItems.values();
        for (ResourceValue value : values) {
            if (value instanceof AttrResourceValue) {
                AttrResourceValue attr = (AttrResourceValue) value;
                Map<String, Integer> values1 = attr.getAttributeValues();
                if (values1 != null) {
                    enumMap.put(attr.getName(), values1);
                }
            }
        }

        // get the declare-styleable values
        Map<String, ResourceValue> styleableItems = res.get(ResourceType.DECLARE_STYLEABLE);
        Collection<ResourceValue> values1 = styleableItems.values();
        // get the attr from the styleable
        for (ResourceValue value : values1) {
            if (value instanceof DeclareStyleableResourceValue) {
                DeclareStyleableResourceValue dsrc = (DeclareStyleableResourceValue) value;
                List<AttrResourceValue> attrs = dsrc.getAllAttributes();
                if (attrs != null && attrs.size() > 0) {
                    for (AttrResourceValue attr : attrs) {
                        Map<String, Integer> values2 = attr.getAttributeValues();
                        if (values != null) {
                            enumMap.put(attr.getName(), values2);
                        }
                    }
                }
            }
        }
        return enumMap;
    }

    private static FrameworkResources loadPlatformResources(
            File resFolder, ILogger log) throws IOException {

        FolderWrapper path = new FolderWrapper(resFolder);
        FrameworkResources resources = new FrameworkResources(path);
        resources.loadResources();
        resources.loadPublicResources(log);
        return resources;
    }
}
