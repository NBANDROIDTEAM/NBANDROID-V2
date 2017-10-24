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

package org.netbeans.modules.android.core.sdk;

import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.resources.FrameworkResources;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkManager;
import com.google.common.base.Supplier;
import java.net.URL;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

public interface DalvikPlatform {
  public static final String PLATFORM_JAR  = "android.jar";   //NOI18N

  IAndroidTarget getAndroidTarget();

  SdkManager getSdkManager();

  List<URL> getBootstrapLibraries();

  FileObject getInstallFolder();

  FileObject getPlatformFolder();

  FileObject findTool(String toolName);

  ClassPath getSourceFolders();

  List<URL> getJavadocFolders();

  LayoutLibrary getLayoutLibrary();
  FrameworkResources getLayoutLibPlatformResources();
  
  // TODO possibly create new interface for platform specific parts and separate target (platforms and add-ons)
  /** List of themes defined for given platform in data/values/res/*.xml. */
  Iterable<String>getThemes();
  
  Supplier<WidgetData> widgetDataSupplier();
}
