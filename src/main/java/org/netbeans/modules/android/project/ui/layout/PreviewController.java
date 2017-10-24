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
package org.netbeans.modules.android.project.ui.layout;

import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.Result;
import com.android.ide.common.resources.configuration.DeviceConfigHelper;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.UiModeQualifier;
import com.android.ide.common.xml.ManifestData;
import com.android.resources.UiMode;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.State;
import com.android.utils.Pair;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.StatsCollector;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.api.AndroidClassPath;
import org.netbeans.modules.android.project.api.AndroidFileTypes;
import org.netbeans.modules.android.project.api.AndroidManifestSource;
import org.netbeans.modules.android.project.api.AndroidProjects;
import org.netbeans.modules.android.project.layout.RenderServiceFactory;
import org.netbeans.modules.android.project.layout.RenderingService;
import org.netbeans.modules.android.project.layout.ResourceRepositoryManager;
import org.netbeans.modules.android.project.layout.ThemeData;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author radim
 */
public class PreviewController {
  private static final Logger LOG = Logger.getLogger(PreviewController.class.getName());
  
  private static final RequestProcessor RP_LAYOUT_RENDERER = new RequestProcessor("Android Layout renderer", 1);

  public interface Repainter {
    /** Initiates repaint of a layout described by file-object using passed platform and configuration. */
    void paint(PreviewModel model, FolderConfiguration folderConfiguration);
  }
  
  // confined to EDT (unless running in test runner)
  private final PreviewModel model;
  private final Repainter repainter;
  private final DalvikPlatformManager dpm;

  public PreviewController(PreviewModel model) {
    this(model, DalvikPlatformManager.getDefault(), new RepaintLayoutRunnable(model));
  }
  
  @VisibleForTesting PreviewController(PreviewModel model, DalvikPlatformManager dpm, Repainter repainter) {
    this.model = Preconditions.checkNotNull(model);
    this.dpm = Preconditions.checkNotNull(dpm);
    this.repainter = Preconditions.checkNotNull(repainter);
    initDefaults();
  }
  
  // TODO store settings method?
  
  private void initDefaults() {
    Device defaultDevice = Iterables.find(
        dpm.getDevices(),
        new Predicate<Device>() {
          @Override
          public boolean apply(Device d) {
            return "Nexus 4".equals(d.getName());
          }
        },
        null);
    if (defaultDevice == null) {
      defaultDevice = Iterables.getFirst(dpm.getDevices(), null);
    }
    if (defaultDevice == null) {
      LOG.log(Level.INFO, "No devices - probably won't be able to render layouts");
    }
    updateDevice(defaultDevice);
  }
  
  public void updateFileObject(FileObject fo) {
    FileObject oldFo = model.getFileObject();
    if (Objects.equal(fo, oldFo)) {
      return;
    }
    if (Lookup.getDefault().lookup(AndroidFileTypes.class).isLayoutFile(fo)) {
      StatsCollector.getDefault().incrementCounter("layout_preview");
      model.setFileObject(fo);
      useDefaultPlatformForFile(fo);
      repainter.paint(model, makeFolderConfiguration(model));
    }
  }
  
  public void updateUiMode(UiMode uiMode) {
    model.setUiMode(uiMode);
    repainter.paint(model, makeFolderConfiguration(model));
  }

  void updateTheme(ThemeData themeData) {
    model.setTheme(themeData);
    repainter.paint(model, makeFolderConfiguration(model));
  }
  
  public void updateLocaleConfig(ResourceRepositoryManager.LocaleConfig locale) {
    model.setLocaleConfig(locale);
    repainter.paint(model, makeFolderConfiguration(model));
  }
  
  private void useDefaultPlatformForFile(FileObject fo) {
    if (fo == null) {
      return;
    }
    ManifestData manifest = AndroidProjects.parseProjectManifest(FileOwnerQuery.getOwner(fo));
    if (manifest == null) {
      return;
    }
    int sdkVersion = manifest.getTargetSdkVersion();
    if (sdkVersion <= 0) {
      sdkVersion = manifest.getMinSdkVersion();
    }
    if (sdkVersion <= 0) {
      return;
    }
    DalvikPlatform platform = DalvikPlatformManager.getDefault().findPlatformForSdkVersion(sdkVersion);
    if (platform == null) {
      return;
    }
    DalvikPlatform currentPlatform = model.getPlatform();
    if (currentPlatform == null || 
        platform.getAndroidTarget().getVersion().isGreaterOrEqualThan(currentPlatform.getAndroidTarget().getVersion().getApiLevel())) {
      model.setPlatform(platform);
    }
  }
  
  void updatePlatform(DalvikPlatform dalvikPlatform) {
    model.setPlatform(dalvikPlatform);
    repainter.paint(model, makeFolderConfiguration(model));
  }
  
  void updateDevice(Device device) {
    model.setDevice(device);
    State currentDeviceConfig = model.getDeviceConfig();
    List<State> deviceStates = device.getAllStates();
    if (currentDeviceConfig == null || !deviceStates.contains(currentDeviceConfig)) {
      model.setDeviceConfig(deviceStates.isEmpty() ? null : device.getAllStates().get(0));
    }
  }
  
  void updateConfiguration(State deviceConfig) {
    model.setDeviceConfig(deviceConfig);
    repainter.paint(model, makeFolderConfiguration(model));
  }
  
  @VisibleForTesting static FolderConfiguration makeFolderConfiguration(PreviewModel model) {
    FolderConfiguration folderConfig = new FolderConfiguration();
    if (model.getDeviceConfig() != null) {
      try {
        FolderConfiguration deviceStateConfig = DeviceConfigHelper.getFolderConfig(model.getDeviceConfig());
        folderConfig.set(deviceStateConfig);
      } catch (NullPointerException ex) {
        LOG.log(Level.FINE, null, ex);
      }
      folderConfig.setUiModeQualifier(new UiModeQualifier(model.getUiMode()));
      folderConfig.setLanguageQualifier(model.getLocaleConfig().getLanguage());
      folderConfig.setRegionQualifier(model.getLocaleConfig().getRegion());
    }
    if (model.getFileObject() != null) {
      String folderName = model.getFileObject().getParent().getName();
      FolderConfiguration selectedFileConfig = FolderConfiguration.getConfigForFolder(folderName);
      folderConfig.add(selectedFileConfig);
    }
    return folderConfig;
  }
  
  private static class RepaintLayoutRunnable implements Runnable, Repainter {

    private final RequestProcessor.Task renderingTask = RP_LAYOUT_RENDERER.create(this);
    
    private final PreviewModel model;
    private final Object lock = new Object();
    private FileObject fo;
    private DalvikPlatform platform;
    private FolderConfiguration folderConfig;
    private ThemeData theme;

    public RepaintLayoutRunnable(PreviewModel model) {
      this.model = model;
    }
    
    /** Request layout repaint. */
    @Override
    public void paint(PreviewModel model, FolderConfiguration folderConfig) {
      assert SwingUtilities.isEventDispatchThread();
      LOG.log(Level.FINE, "repaint requested: {0}", model);
      paintImpl(model.getFileObject(), model.getPlatform(), folderConfig, model.getTheme());
    }
    
    private void paintImpl(FileObject fo, DalvikPlatform platform, FolderConfiguration folderConfig, ThemeData theme) {
      assert SwingUtilities.isEventDispatchThread();
      synchronized (lock) {
        this.fo = fo;
        this.platform = platform;
        this.folderConfig = folderConfig;
        this.theme = theme;
        model.setResult(null);
        renderingTask.schedule(0);
      }
    }
    
    @Override
    public void run() {
      assert !SwingUtilities.isEventDispatchThread();
      
      final Pair<RenderSession, Result> renderResult = requestRenderResult();
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          model.setResult(renderResult.getSecond());
          if (!renderResult.getSecond().isSuccess()) {
            LOG.log(Level.INFO, renderResult.getSecond().getErrorMessage());
          } else {
            StatsCollector.getDefault().incrementCounter("layout_preview_OK");
          }
          if (renderResult.getFirst() != null) {
            final BufferedImage img = renderResult.getFirst().getImage();
            model.setImage(img);
          }
        }
      });
    }
    
    @Nonnull
    private Pair<RenderSession, Result> requestRenderResult() {
    
      FileObject lFo;
      DalvikPlatform lPlatform;
      FolderConfiguration lFolderConfig;
      ThemeData lTheme;
      synchronized (lock) {
        lFo = fo;
        lPlatform = platform;
        lFolderConfig = folderConfig;
        lTheme = theme;
      }
      try {
        List<String> missingData = new ArrayList<>();
        if (lPlatform == null) {
          missingData.add("platform");
        }
        if (lFolderConfig == null) {
          missingData.add("device configuration");
        }
        if (lFo == null) {
          missingData.add("layout file");
        }
        if (!missingData.isEmpty()) {
          return Pair.of(null, Result.Status.ERROR_UNKNOWN.createResult("Cannot render - missing " + Joiner.on(", ").join(missingData) + "."));
        }
        Project p = FileOwnerQuery.getOwner(lFo);
        if (p == null) {
          return Pair.of(null, Result.Status.ERROR_UNKNOWN.createResult("Cannot render - no project found."));
        }
        AndroidProject ap = p.getLookup().lookup(AndroidProject.class);
        AndroidClassPath cpProvider = p.getLookup().lookup(AndroidClassPath.class);
        AndroidManifestSource ams = p.getLookup().lookup(AndroidManifestSource.class);
        if (ap == null && cpProvider == null) {
          LOG.log(Level.FINE, "Cannot render preview - not an Android Project");
          return Pair.of(null, Result.Status.ERROR_UNKNOWN.createResult("Cannot render - not an Android project."));
        }
        if (lTheme == null) {
          LOG.log(Level.FINE, "Cannot render preview - no theme selected");
          return Pair.of(null, Result.Status.ERROR_UNKNOWN.createResult("Cannot render - no theme selected."));
        }
        RenderingService service = RenderServiceFactory.createService(
            p,
            lFolderConfig,
            lPlatform);
        Reader layoutReader = new InputStreamReader(lFo.getInputStream());

        try {
          final RenderSession session = service.createRenderSession(ams, cpProvider,
              layoutReader, lTheme.themeName, lTheme.isProjectTheme, lFo.getName());

          // get the status of the render
          if (session != null) {
            return Pair.of(session, session.getResult());
          }
          return Pair.of(null, Result.Status.ERROR_UNKNOWN.createResult("Cannot render."));
        } finally {
          Closeables.closeQuietly(layoutReader);
        }
      } catch (Exception ex) {
        LOG.log(Level.INFO, null, ex);
        return Pair.of(null, Result.Status.ERROR_UNKNOWN.createResult("Cannot render.", ex));
      }
    }
  }
}
