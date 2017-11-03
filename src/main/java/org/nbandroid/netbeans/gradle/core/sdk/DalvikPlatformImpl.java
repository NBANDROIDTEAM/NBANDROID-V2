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

package org.nbandroid.netbeans.gradle.core.sdk;

import com.android.SdkConstants;
import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.resources.FrameworkResources;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.repository.AndroidSdkHandler;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

class DalvikPlatformImpl implements DalvikPlatform {

  private static final Logger LOG = Logger.getLogger(DalvikPlatformImpl.class.getName());

  private static URL findPlatformJar(IAndroidTarget androidTarget)
      throws MalformedURLException {
    IAndroidTarget platformTarget = androidTarget;
    while (platformTarget != null && !platformTarget.isPlatform()) {
      platformTarget = platformTarget.getParent();
    }
    if (platformTarget == null) {
      throw new IllegalStateException("Cannot find platform.jar for " + androidTarget);
    }
    File platformJar = FileUtil.normalizeFile(new File(platformTarget.getPath(IAndroidTarget.ANDROID_JAR)));
    URL archiveRoot = FileUtil.getArchiveRoot(Utilities.toURI(platformJar).toURL());
    return archiveRoot;
  }

    private static URL findAnnotationsLib(AndroidSdkHandler sdkManager)
            throws MalformedURLException {
    File annotationsJar = FileUtil.normalizeFile(new File(sdkManager.getLocation(), SdkConstants.FD_TOOLS
        + File.separator + SdkConstants.FD_SUPPORT
        + File.separator + SdkConstants.FN_ANNOTATIONS_JAR));
    URL archiveRoot = FileUtil.getArchiveRoot(Utilities.toURI(annotationsJar).toURL());
    return archiveRoot;
  }

  private static List<URL> findTargetLibraries(IAndroidTarget target) {
    if (target == null) {
      return Collections.emptyList();
    }
      List<IAndroidTarget.OptionalLibrary> libs = target.getOptionalLibraries();
    if (libs == null) {
      return Collections.emptyList();
    }
    List<URL> libUrls = new ArrayList<>();
      for (IAndroidTarget.OptionalLibrary lib : libs) {
      try {
        LOG.log(Level.FINER, "Adding standard lib {0} to AndroidTarget@{1}",
                  new Object[]{lib.getJar(), target.getLocation()});
          libUrls.add(FileUtil.getArchiveRoot(Utilities.toURI(lib.getJar()).toURL()));
      } catch (MalformedURLException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    return libUrls;
  }

    private static List<URL> findAllLibraries(AndroidSdkHandler sdkManager, IAndroidTarget target) throws MalformedURLException {
    List<URL> libUrls = new ArrayList<>();
    libUrls.add(findPlatformJar(target));
    libUrls.addAll(findTargetLibraries(target));
    if (target.getVersion().getApiLevel() <= 15) {
      libUrls.add(findAnnotationsLib(sdkManager));
    }
    return libUrls;
  }

  private final FileObject installFolder;
  private final List<URL> bootLibs;
    private final AndroidSdkHandler sdkManager;
  private final IAndroidTarget androidTarget;
  private final LayoutLibLoader layoutLibLoader;
  private final Supplier<WidgetData> widgetDataLoader;
  //@GuardedBy (this)
  private ClassPath sources;

    DalvikPlatformImpl(AndroidSdkHandler sdkManager, IAndroidTarget androidTarget)
            throws IOException {
    Preconditions.checkNotNull(androidTarget);
    this.installFolder = FileUtil.toFileObject(new File(androidTarget.getLocation()));

    this.sdkManager = sdkManager;
    this.androidTarget = androidTarget;

    this.bootLibs = findAllLibraries(sdkManager, androidTarget);
    layoutLibLoader = new LayoutLibLoader(androidTarget);
    FileObject attrsLayoutFO = findTool(Tool.WIDGETS.getSystemName());
    widgetDataLoader = Suppliers.memoize(new LayoutClassesParser(URLMapper.findURL(attrsLayoutFO, URLMapper.INTERNAL)));

    LOG.log(Level.CONFIG, "DalvikPlatform created: install folder = {0}, installTarget = {1}, null = {2}",
        new Object[]{installFolder, androidTarget, null});
  }

  @Override
  public IAndroidTarget getAndroidTarget() {
    return androidTarget;
  }

  @Override
    public AndroidSdkHandler getSdkManager() {
    return sdkManager;
  }

  @Override
  public List<URL> getBootstrapLibraries() {
    return this.bootLibs;
  }

  @Override
  public FileObject getInstallFolder() {
    return installFolder;
  }

  @Override
  public FileObject getPlatformFolder() {
    IAndroidTarget platformTarget = androidTarget;
    while (platformTarget != null && !platformTarget.isPlatform()) {
      platformTarget = platformTarget.getParent();
    }
    FileObject platformDir = platformTarget == null
        ? null
        : FileUtil.toFileObject(FileUtil.normalizeFile(new File(platformTarget.getLocation())));
    return platformDir;
  }

  @Override
  public FileObject findTool(String toolName) {
    return Util.findTool(toolName, getPlatformFolder());
  }

  @Override
  public synchronized ClassPath getSourceFolders() {
    if (this.sources == null) {
      List<URL> srcURLs = Lists.newArrayList();
      fillSourceFolders(sdkManager, androidTarget, srcURLs);
      this.sources = ClassPathSupport.createClassPath(srcURLs.toArray(new URL[0]));
    }
    return this.sources;
  }

    private static void fillSourceFolders(AndroidSdkHandler sdkManager, IAndroidTarget androidTarget, List<URL> srcURLs) {
    if (androidTarget != null) {
      String srcs = androidTarget.getPath(IAndroidTarget.SOURCES);
      if (srcs != null) {
        LOG.log(Level.FINE, "Found sources {0} for target {1}", new Object[]{srcs, androidTarget});
        srcURLs.add(FileUtil.urlForArchiveOrDir(new File(srcs)));
      }
      String name = new File(androidTarget.getLocation()).getName();
      File altSourceFolder = new File(sdkManager.getLocation(), "sources" + File.separatorChar + name);
      if (altSourceFolder.isDirectory()) {
        LOG.log(Level.FINE, "Found alternative source folder {0} for target {1}", new Object[]{altSourceFolder, androidTarget});
        srcURLs.add(FileUtil.urlForArchiveOrDir(altSourceFolder));
      }
      fillSourceFolders(sdkManager, androidTarget.getParent(), srcURLs);
    }

  }

  @Override
  public synchronized List<URL> getJavadocFolders() {
    List<URL> javadocs = new ArrayList<>();
    try {
      javadocs.add(Utilities.toURI(new File(sdkManager.getLocation() + "/docs/reference")).toURL());
      if (androidTarget != null) { // XXX(radim): when does this happen?
        String docs = androidTarget.getPath(IAndroidTarget.DOCS);
        if (docs != null) {
          final File docsFolder = new File(docs);
          if (docsFolder.exists()) {
            javadocs.add(Utilities.toURI(docsFolder).toURL());
          } else {
            File docsParent = docsFolder.getParentFile();
            for (File child : docsParent.listFiles()) {
              if (!child.isDirectory()) {
                continue;
              }
              javadocs.add(Utilities.toURI(child).toURL());
            }

          }
        }
      }
    } catch (MalformedURLException ex) {
      Exceptions.printStackTrace(ex);
    }
    return javadocs;
  }

  @Override
  public LayoutLibrary getLayoutLibrary() {
    return layoutLibLoader.getLayoutLibrary();
  }

  @Override
  public FrameworkResources getLayoutLibPlatformResources() {
    return layoutLibLoader.getPlatformResources();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DalvikPlatformImpl other = (DalvikPlatformImpl) obj;
    if (this.sdkManager != other.sdkManager && (this.sdkManager == null || !this.sdkManager.equals(other.sdkManager))) {
      return false;
    }
    if (this.androidTarget != other.androidTarget && (this.androidTarget == null || !this.androidTarget.equals(other.androidTarget))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + (this.sdkManager != null ? this.sdkManager.hashCode() : 0);
    hash = 37 * hash + (this.androidTarget != null ? this.androidTarget.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "DalvikPlatform{" + "installFolder=" + installFolder + ", androidTarget=" + androidTarget + '}';
  }

  @Override
  public Iterable<String> getThemes() {
    File deviceXml = new File(androidTarget.getLocation(), 
        SdkConstants.FD_DATA + File.separatorChar + SdkConstants.FD_RES + File.separatorChar + "values");
    final ManifestParser parser = ManifestParser.getDefault();
    List<String> names = Lists.newArrayList();
    for (File valueFile : deviceXml.listFiles()) {
      if (valueFile.getName().endsWith(".xml")) {
        InputStream is;
        try {
          is = new FileInputStream(valueFile);
          for (String themeName : parser.getPlatformThemeNames(is)) {
            names.add(themeName);
          }
          Closeables.closeQuietly(is);
        } catch (IOException ex) {
          Exceptions.printStackTrace(ex);
        }
      }
    }
    return names;
  }

  @Override
  public Supplier<WidgetData> widgetDataSupplier() {
    return widgetDataLoader;
  }
}
