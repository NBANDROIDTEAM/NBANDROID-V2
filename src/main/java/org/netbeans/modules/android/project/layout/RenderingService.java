/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.modules.android.project.layout;

import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.rendering.api.*;
import com.android.ide.common.rendering.api.SessionParams.RenderingMode;
import com.android.ide.common.rendering.legacy.ILegacyPullParser;
import com.android.ide.common.resources.FrameworkResources;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.DensityQualifier;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.ScreenDimensionQualifier;
import com.android.ide.common.resources.configuration.ScreenOrientationQualifier;
import com.android.ide.common.resources.configuration.VersionQualifier;
import com.android.resources.Density;
import com.android.resources.ResourceType;
import com.android.resources.ScreenOrientation;
import com.google.common.base.Preconditions;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.kxml2.io.KXmlParser;
import org.netbeans.modules.android.core.sdk.ManifestParser;
import org.netbeans.modules.android.project.api.AndroidClassPath;
import org.netbeans.modules.android.project.api.AndroidManifestSource;
import org.netbeans.modules.android.project.api.ReferenceResolver;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author radim
 */
public class RenderingService {
  private static final Logger LOG = Logger.getLogger(RenderingService.class.getName());

  private final FolderConfiguration folderConfig;
  private final ReferenceResolver refResolver;
  private final int minSdkVersion;
  private final LayoutLibrary layoutLib;
  private final FrameworkResources frameworkResources;
  private final ResourceRepository /*ProjectResources*/ projectResources;

  // The following fields are optional or configurable using the various chained
  // setters:
  private int mWidth;
  private int mHeight;
  private int mMinSdkVersion = -1;
  private int mTargetSdkVersion = -1;
  private float mXdpi = -1;
  private float mYdpi = -1;
  private RenderingMode mRenderingMode = RenderingMode.NORMAL;
  private LayoutLog mLogger;
  private Integer mOverrideBgColor;
  private boolean mShowDecorations = true;
  private String mAppLabel;
  private String mAppIconName;
  private IImageFactory mImageFactory;

  RenderingService(FolderConfiguration folderConfig, ReferenceResolver refResolver,
      int minSdkVersion, LayoutLibrary layoutLib,
      FrameworkResources frameworkResources,
      ResourceRepository /*ProjectResources*/ projectResources) {
    this.folderConfig = Preconditions.checkNotNull(folderConfig);
    this.refResolver = Preconditions.checkNotNull(refResolver);
    this.minSdkVersion = minSdkVersion;
    this.layoutLib = Preconditions.checkNotNull(layoutLib);
    this.frameworkResources = Preconditions.checkNotNull(frameworkResources);
    this.projectResources = Preconditions.checkNotNull(projectResources);
  }

  private ResourceResolver createResourceResolver(FolderConfiguration config,
      String themeName,
      boolean isProjectTheme) {
    final Map<ResourceType, Map<String, ResourceValue>> configuredProjectRes =
        projectResources.getConfiguredResources(config);
    final Map<ResourceType, Map<String, ResourceValue>> configuredFrameworkRes =
        frameworkResources.getConfiguredResources(config);
    
    LOG.log(Level.FINEST, "configuredProjectRes: {0}", configuredProjectRes);
    LOG.log(Level.FINEST, "configuredFrameworkRes: {0}", configuredFrameworkRes);

    return ResourceResolver.create(configuredProjectRes, configuredFrameworkRes, themeName, isProjectTheme);
  }

  @Nullable
  public RenderSession createRenderSession(@Nullable AndroidManifestSource ams, 
      AndroidClassPath cpProvider, 
      Reader layoutReader, String themeName, 
      boolean isProjectTheme, String layoutName) throws FileNotFoundException,
      XmlPullParserException {

    final Dimension dimension = getDimension();
    if (dimension == null) {
      LOG.log(Level.FINE, "cannot create render session: no dimension");
      return null;
    }

    final VersionQualifier versionQualifier = folderConfig.getVersionQualifier();
    if (versionQualifier == null) {
      LOG.log(Level.FINE, "cannot create render session: no version");
      return null;
    }
    if (folderConfig.getScreenSizeQualifier() == null) {
      LOG.log(Level.FINE, "cannot create render session: no screen size");
      return null;
    }
    if (folderConfig.getScreenOrientationQualifier()== null) {
      LOG.log(Level.FINE, "cannot create render session: no screen orientation");
      return null;
    }

    final int targetSdkVersion = versionQualifier.getVersion();
    final int sdkVersion = minSdkVersion >= 0 ? minSdkVersion : targetSdkVersion;

    final DensityQualifier densityQualifier = folderConfig.getDensityQualifier();
    final Density density = densityQualifier != null ? densityQualifier.getValue() : Density.MEDIUM;
    // TODO resolve this  
//    final float xdpi = Float.isNaN(myXdpi) ? density.getDpiValue() : myXdpi;
//    final float ydpi = Float.isNaN(myYdpi) ? density.getDpiValue() : myYdpi;
    final float xdpi = density.getDpiValue();
    final float ydpi = density.getDpiValue();

    ResourceResolver resourceResolver = createResourceResolver(folderConfig, themeName, isProjectTheme);
    IProjectCallback prjCallback = NbProjectCallback.create(
        layoutLib, cpProvider, refResolver);
    if (resourceResolver == null) {
      // Abort the rendering if the resources are not found.
      return null;
    }

    // find the layout to run
    ResourceValue value = resourceResolver.getProjectResource(ResourceType.LAYOUT, layoutName);
    if (value == null || value.getValue() == null) {
      throw new IllegalArgumentException("layout '" + layoutName + "' does not exist");
    }

    ILayoutPullParser parser = new NullXmlParser();
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
    parser.setInput(layoutReader);

    figureSomeValuesOut();

    // TODO get HW config
    boolean swButtons = true;
    HardwareConfig hwConfig = new HardwareConfig(
        mWidth, mHeight, density, mXdpi, mYdpi, 
        folderConfig.getScreenSizeQualifier().getValue(), 
        folderConfig.getScreenOrientationQualifier().getValue(), 
        swButtons);
    SessionParams params = new SessionParams(
        parser,
        mRenderingMode,
        this /* projectKey */,
        hwConfig,
        resourceResolver,
        prjCallback,
        mMinSdkVersion,
        mTargetSdkVersion,
        mLogger);

    // Request margin and baseline information.
    // TODO: Be smarter about setting this; start without it, and on the first request
    // for an extended view info, re-render in the same session, and then set a flag
    // which will cause this to create extended view info each time from then on in the
    // same session
    params.setExtendedViewInfoMode(false);

    if (!mShowDecorations) {
      params.setForceNoDecor();
    } else {
      if (mAppLabel == null) {
        ManifestParser.ApplicationData appData = new ManifestInfo(ams).parse();
        mAppLabel = appData.appLabel;
        mAppIconName = appData.appIconName;
      }

      params.setAppLabel(mAppLabel);
      params.setAppIcon(mAppIconName); // ok to be null
    }

//    if (mOverrideBgColor != null) {
//      params.setOverrideBgColor(mOverrideBgColor.intValue());
//    }

    // set the Image Overlay as the image factory.
//    params.setImageFactory(mImageFactory);

    try {
      return layoutLib.createSession(params);
    } catch (RuntimeException t) {
      // Exceptions from the bridge
      LOG.log(Level.WARNING, null, t);
      throw t;
    }
  }

  private void figureSomeValuesOut() {
    if (folderConfig == null) {
      return;
    }
    int size1 = folderConfig.getScreenDimensionQualifier() != null ? 
        folderConfig.getScreenDimensionQualifier().getValue1() :
        180;
    int size2 = folderConfig.getScreenDimensionQualifier() != null ?
        folderConfig.getScreenDimensionQualifier().getValue2() :
        180;
    ScreenOrientation orientation = folderConfig.getScreenOrientationQualifier() != null ? 
        folderConfig.getScreenOrientationQualifier().getValue() :
        ScreenOrientation.PORTRAIT;
    switch (orientation) {
      case LANDSCAPE:
        mWidth = size1 < size2 ? size2 : size1;
        mHeight = size1 < size2 ? size1 : size2;
        break;
      case PORTRAIT:
        mWidth = size1 < size2 ? size1 : size2;
        mHeight = size1 < size2 ? size2 : size1;
        break;
      case SQUARE:
        mWidth = mHeight = size1;
        break;
    }

    if (mMinSdkVersion == -1) {
      mMinSdkVersion = folderConfig.getVersionQualifier().getVersion();
    }

    if (mTargetSdkVersion == -1) {
      mTargetSdkVersion = folderConfig.getVersionQualifier().getVersion();
    }
    
    Density value = folderConfig.getDensityQualifier() != null ?
        folderConfig.getDensityQualifier().getValue() :
        null;
    if (value != null) {
      if (mXdpi == -1) {
        mXdpi = value.getDpiValue();
      }

      if (mYdpi == -1) {
        mYdpi = value.getDpiValue();
      }
    }
  }
  
  private Dimension getDimension() {
    final ScreenDimensionQualifier dimensionQualifier = folderConfig.getScreenDimensionQualifier();

    final int size1 = dimensionQualifier != null ? dimensionQualifier.getValue1() : 320;
    final int size2 = dimensionQualifier != null ? dimensionQualifier.getValue2() : 240;

    final ScreenOrientationQualifier orientationQualifier = folderConfig.getScreenOrientationQualifier();

    final ScreenOrientation orientation = orientationQualifier != null
                                          ? orientationQualifier.getValue()
                                          : ScreenOrientation.PORTRAIT;

    switch (orientation) {
      case LANDSCAPE:
        return new Dimension(size1 < size2 ? size2 : size1, size1 < size2 ? size1 : size2);
      case PORTRAIT:
        return new Dimension(size1 < size2 ? size1 : size2, size1 < size2 ? size2 : size1);
      case SQUARE:
        return new Dimension(size1, size1);
      default:
        LOG.log(Level.WARNING, "Unknown screen orientation {0}", orientation);
        return null;
    }
  }

  private static class NullXmlParser extends KXmlParser implements ILayoutPullParser, ILegacyPullParser {

    @Override
    public ILayoutPullParser getParser(String layoutName) {
      return null;
    }

    @Override
    public Object getViewCookie() {
      return null;
    }

    @Override
    public Object getViewKey() {
      return null;
    }
  }
}
