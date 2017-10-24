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

package org.netbeans.modules.android.project;

import org.netbeans.modules.android.project.api.PropertyName;
import com.android.utils.Pair;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.android.project.spi.AndroidProjectDirectory;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

final class AndroidInfoImpl implements AndroidProjectInfo {

  public static final String ANDROID_LIBRARY_PROPERTY = "android.library";

  private static final Logger LOG = Logger.getLogger(AndroidProjectInfo.class.getName());

  private final AndroidProject project;
  private final FileObject projectDir;
  private volatile boolean needsRefresh = false;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  AndroidInfoImpl(AndroidProject project) {
    this.project = project;
    projectDir = project.getProjectDirectory();
    projectDir.addFileChangeListener(new FileChangeAdapter() {
      @Override
      public void fileDataCreated(FileEvent fe) {
        updateRefreshSupport();
      }

      @Override
      public void fileDeleted(FileEvent fe) {
        updateRefreshSupport();
      }
    });
    updateRefreshSupport();
  }

  private boolean isToolsV14OrNewer() {
    return projectDir.getFileObject("project.properties") != null;
  }

  private void updateRefreshSupport() {
    FileObject buildXml = projectDir.getFileObject("build.xml");
    boolean oldNeedsRefresh = needsRefresh;
    needsRefresh = buildXml == null || !isToolsV14OrNewer();
    LOG.log(Level.FINE, "updated needsRefresh to {0}", needsRefresh);
    if (oldNeedsRefresh != needsRefresh) {
      pcs.firePropertyChange(AndroidProjectType.NEEDS_FIX_INFO, oldNeedsRefresh, needsRefresh);
    }
  }

  @Override
  public boolean isNeedsFix() {
    return needsRefresh;
  }

  @Override
  public boolean isLibrary() {
    if (!isToolsV14OrNewer()) {
      // look into legacy default.properties file to check if this is library
      LOG.log(Level.FINE, "using compatibility mode to check is project is library {0}", needsRefresh);
      PropertyProvider legacyProps =
          PropertyUtils.propertiesFilePropertyProvider(new File(FileUtil.toFile(projectDir), "default.properties"));
      if (Boolean.valueOf(legacyProps.getProperties().get(ANDROID_LIBRARY_PROPERTY))) {
        return true;
      }
    }
    return Boolean.valueOf(project.evaluator().getProperty(ANDROID_LIBRARY_PROPERTY));
  }

  public Iterable<String> getDependentProjectDirPaths() {
    List<String> prjDirs = Lists.newArrayList();
    Map<String, String> properties = project.evaluator().getProperties();
    int i = 1;
    while (true) {
      String value = properties.get("android.library.reference." + String.valueOf(i));
      if (value == null) {
        break;
      }
      prjDirs.add(value);
      i++;
    }
    return prjDirs;
  }

  @Override
  public Iterable<FileObject> getDependentProjectDirs() {
    return Iterables.filter(
        Iterables.transform(getDependentProjectDirPaths(), new Function<String, FileObject>() {

          @Override
          public FileObject apply(String input) {
            return FileUtil.toFileObject(FileUtil.normalizeFile(
                new File(project.getLookup().lookup(AndroidProjectDirectory.class).get(), input)));
          }

        }),
        Predicates.notNull());
  }

  @Override
  public String getFixDescription() {
    if (!needsRefresh) {
      return NbBundle.getMessage(AndroidInfoImpl.class, "TXT_ProjectOK");
    }
    return NbBundle.getMessage(AndroidInfoImpl.class, "TXT_ProjectNeedsRefresh");
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener lsnr) {
    pcs.addPropertyChangeListener(lsnr);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener lsnr) {
    pcs.removePropertyChangeListener(lsnr);
  }

  @Override
  public boolean isTest() {
    return !Strings.isNullOrEmpty(
        project.evaluator().getProperty(PropertyName.TEST_PROJECT_DIR.getName()));
  }

  @Override
  public Iterable<FileObject> getSourceDirs() {
    PropertyEvaluator propEvaluator = project.evaluator();
    String srcDirValue = propEvaluator.getProperty(PropertyName.SRC_DIR.getName());
    if (srcDirValue == null) {
      srcDirValue = "src";
    }
    return ensureSourceBelongsToProject(srcDirValue).getSecond();
  }

  @Override
  public Iterable<FileObject> getSourceAndGenDirs() {
    final FileObject genDir = project.getProjectDirectory().getFileObject("gen");
    return genDir != null ? 
        Iterables.concat(getSourceDirs(), Collections.singleton(genDir)) :
        getSourceDirs();
  }

  private Pair<String, Iterable<FileObject>> markedExtSourceDirs = Pair.of(null, null);
  
  private Pair<String, Iterable<FileObject>> ensureSourceBelongsToProject(String srcDir) {
    if (Objects.equal(markedExtSourceDirs.getFirst(), srcDir)) {
      return markedExtSourceDirs;
    }
    List<FileObject> extDirs = Lists.newArrayList();
    for (String extDirPath : Splitter.on(':').split(srcDir)) {
      FileObject src = FileUtil.toFileObject(
          FileUtil.normalizeFile(new File(project.getLookup().lookup(AndroidProjectDirectory.class).get(), extDirPath)));
      if (src != null) {
        extDirs.add(src);
        if (!FileUtil.isParentOf(project.getProjectDirectory(), src)) {
          FileOwnerQuery.markExternalOwner(src, project, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        }
      }
    }
    markedExtSourceDirs = Pair.<String, Iterable<FileObject>>of(srcDir, extDirs);
    return markedExtSourceDirs;
  }
}
