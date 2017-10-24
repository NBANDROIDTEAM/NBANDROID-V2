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

import com.android.ide.common.rendering.api.Result;
import com.android.resources.UiMode;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.State;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.layout.ResourceRepositoryManager.LocaleConfig;
import org.netbeans.modules.android.project.layout.ThemeData;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
public class PreviewModel {

  public static final String PROP_DEVICE = "device";
  public static final String PROP_DEVICECONFIG = "deviceConfig";
  public static final String PROP_FILEOBJECT = "fileObject";
  public static final String PROP_IMAGE = "image";
  public static final String PROP_PLATFORM = "platform";
  public static final String PROP_LOCALECONFIG = "localeConfig";
  public static final String PROP_THEME = "theme";
  public static final String PROP_UIMODE = "uiMode";
  public static final String PROP_RESULT = "result";
  
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private FileObject currentFileObject;
  private BufferedImage image;
  private DalvikPlatform platform;
  private Device device;
  private State deviceConfig;
  private UiMode uiMode = UiMode.NORMAL;
  private LocaleConfig localeConfig = LocaleConfig.theOnlyLocale();
  private ThemeData theme = new ThemeData("Theme", false);
  // null means 'loading'
  private Result result;

  public PreviewModel() {
  }

  public ThemeData getTheme() {
    return theme;
  }

  public void setTheme(ThemeData theme) {
    ThemeData oldTheme = this.theme;
    this.theme = theme;
    pcs.firePropertyChange(PROP_THEME, oldTheme, theme);
  }

  public BufferedImage getImage() {
    return image;
  }

  public void setImage(BufferedImage image) {
    BufferedImage oldImage = this.image;
    this.image = image;
    pcs.firePropertyChange(PROP_IMAGE, oldImage, image);
  }

  public FileObject getFileObject() {
    return currentFileObject;
  }

  public void setFileObject(FileObject currentFileObject) {
    this.currentFileObject = currentFileObject;
    pcs.firePropertyChange(PROP_FILEOBJECT, null, null);
  }
  
  public DalvikPlatform getPlatform() {
    return platform;
  }
  
  public void setPlatform(DalvikPlatform platform) {
    DalvikPlatform oldPlatform = this.platform;
    this.platform = platform;
    pcs.firePropertyChange(PROP_PLATFORM, oldPlatform, platform);
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    Device oldDevice = this.device;
    this.device = device;
    pcs.firePropertyChange(PROP_DEVICE, oldDevice, device);
  }
  
  public State getDeviceConfig() {
    return deviceConfig;
  }

  public void setDeviceConfig(State deviceConfig) {
    State oldDeviceConfig = this.deviceConfig;
    this.deviceConfig = deviceConfig;
    pcs.firePropertyChange(PROP_DEVICECONFIG, oldDeviceConfig, deviceConfig);
  }

  public UiMode getUiMode() {
    return uiMode;
  }
  
  public void setUiMode(UiMode uiMode) {
    UiMode oldUiMode = this.uiMode;
    this.uiMode = uiMode;
    pcs.firePropertyChange(PROP_UIMODE, oldUiMode, uiMode);
  }

  public LocaleConfig getLocaleConfig() {
    return localeConfig;
  }

  public void setLocaleConfig(LocaleConfig localeConfig) {
    LocaleConfig oldLocaleConfig = this.localeConfig;
    this.localeConfig = localeConfig;
    pcs.firePropertyChange(PROP_LOCALECONFIG, oldLocaleConfig, localeConfig);
  }

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    Result oldResult = this.result;
    this.result = result;
    pcs.firePropertyChange(PROP_RESULT, oldResult, result);
  }

  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }
  
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  @Override
  public String toString() {
    return "PreviewModel{" + "currentFileObject=" + currentFileObject 
        + ", platform=" + platform 
        + ", device=" + device 
        + ", deviceConfig=" + deviceConfig 
        + ", uiMode=" + uiMode 
        + ", localeConfig=" + localeConfig 
        + ", theme=" + theme + '}';
  }
}
