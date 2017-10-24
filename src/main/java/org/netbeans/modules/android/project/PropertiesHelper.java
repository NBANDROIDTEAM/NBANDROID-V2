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
import com.android.io.StreamException;
import com.android.sdklib.internal.project.ProjectProperties;
import com.android.sdklib.internal.project.ProjectPropertiesWorkingCopy;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;

/**
 *
 * @author radim
 */
public class PropertiesHelper {
  private static final Logger LOG = Logger.getLogger(PropertiesHelper.class.getName());

  private static class ProxyPropertyEvaluator implements PropertyEvaluator, PropertyChangeListener {

    private volatile PropertyEvaluator eval;
    private final PropertyChangeSupport pcs;

    static ProxyPropertyEvaluator create(PropertyEvaluator eval) {
      ProxyPropertyEvaluator newEval = new ProxyPropertyEvaluator();
      newEval.set(eval);
      return newEval;
    }
    
    private ProxyPropertyEvaluator() {
      pcs = new PropertyChangeSupport(this);
    }
    
    void set(PropertyEvaluator eval) {
      PropertyEvaluator old = eval;
      if (old != null) {
        old.removePropertyChangeListener(this);
      }
      eval.addPropertyChangeListener(this);
      this.eval = Preconditions.checkNotNull(eval);
      pcs.firePropertyChange(null, null, null);
    }
    
    @Override
    public String getProperty(String propName) {
      return eval.getProperty(propName);
    }

    @Override
    public String evaluate(String propName) {
      return eval.getProperty(propName);
    }

    @Override
    public Map<String, String> getProperties() {
      return eval.getProperties();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
      pcs.addPropertyChangeListener(pl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
      pcs.removePropertyChangeListener(pl);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      pcs.firePropertyChange(evt);
    }
  }

  static {
    try {
      // patch PropertyType enum(s) to enable property removals
      Field mKnownPropsFld = ProjectProperties.PropertyType.class.getDeclaredField("mKnownProps");
      mKnownPropsFld.setAccessible(true);
      Set<String> mKnownProps = (Set) mKnownPropsFld.get(ProjectProperties.PropertyType.ANT);
      Set<String> mMoreKnownProps = Sets.newHashSet(mKnownProps);
      mMoreKnownProps.add(PropertyName.JAR_LIBS_DIR.getName());
      mKnownPropsFld.set(
          ProjectProperties.PropertyType.ANT, Collections.unmodifiableSet(mMoreKnownProps));
    } catch (IllegalArgumentException ex) {
      Exceptions.printStackTrace(ex);
    } catch (IllegalAccessException ex) {
      Exceptions.printStackTrace(ex);
    } catch (NoSuchFieldException ex) {
      Exceptions.printStackTrace(ex);
    } catch (SecurityException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  private final ProxyPropertyEvaluator eval;
  private final PropertyProvider localProps;
//  private final PropertyProvider buildProps;
//  private final PropertyProvider defaultProps;
  private final AndroidProjectImpl project;
  private final PropertyProvider stockPropertyPreprovider;

  @VisibleForTesting public PropertiesHelper(AndroidProjectImpl project) {
    this.project = project;
    File dirF = project.getProjectDirectoryFile();
    { // XXX copied from org.netbeans.spi.project.support.ant.ProjectProperties, not currently available as a static utility
      Map<String, String> m;
      Properties p = System.getProperties();
      synchronized (p) {
        m = NbCollections.checkedMapByCopy(p, String.class, String.class, false);
      }
      m.put("basedir", dirF.getAbsolutePath()); // NOI18N
      m.put("project.name", dirF.getName()); // NOI18N
      m.put("project.displayName", dirF.getName()); // NOI18N
      File antJar = InstalledFileLocator.getDefault().locate(
          "ant/lib/ant.jar", "org.apache.tools.ant.module", false); // NOI18N
      if (antJar != null) {
        File antHome = antJar.getParentFile().getParentFile();
        m.put("ant.home", antHome.getAbsolutePath()); // NOI18N
        m.put("ant.core.lib", antJar.getAbsolutePath()); // NOI18N
      }
      stockPropertyPreprovider = PropertyUtils.fixedPropertyProvider(m);
    }
    localProps = PropertyUtils.propertiesFilePropertyProvider(new File(dirF, "local.properties"));
    eval = ProxyPropertyEvaluator.create(PropertyUtils.sequentialPropertyEvaluator(
        stockPropertyPreprovider,
        localProps,
        PropertyUtils.propertiesFilePropertyProvider(new File(dirF, "ant.properties")),
        PropertyUtils.propertiesFilePropertyProvider(new File(dirF, "project.properties"))));
  }

  public PropertyEvaluator evaluator() {
    return eval;
  }

  /**
   * Checks if all required properties are set and if some are missing tries to fix it.
   */
  boolean updateProperties(DalvikPlatformManager platformMgr) {

    // if sdk.dir is missing in local.properties we will regenerate this file.
    // We do not do complete project update. Just the small part.
    String sdkDir = platformMgr.getSdkLocation();
    if (sdkDir != null &&
        !sdkDir.equals(localProps.getProperties().get(PropertyName.SDK_DIR.getName()))) {
      return setProperty(
          ProjectProperties.PropertyType.LOCAL, PropertyName.SDK_DIR.getName(), sdkDir);
    }
    return true;
  }

  boolean setProperty(ProjectProperties.PropertyType type, String name, String value) {
    try {
      LOG.log(Level.CONFIG, "{1} in {0} will be regenerated: {2} -> {3}",
          new Object[] {project.getProjectDirectory().getPath(), type, name, value});

      ProjectProperties loadedProps = ProjectProperties.load(
          project.getProjectDirectory().getPath(), type);
      if (value != null) {
        ProjectPropertiesWorkingCopy props = loadedProps != null ?
            loadedProps.makeWorkingCopy() :
            ProjectProperties.create(project.getProjectDirectory().getPath(), type);
        props.setProperty(name, value);
        props.save();
      } else {
        // only remove if the file already exists
        if (project.getProjectDirectory().getFileObject(type.getFilename()) != null) {
          ProjectPropertiesWorkingCopy props = loadedProps != null ?
              loadedProps.makeWorkingCopy() :
              ProjectProperties.create(project.getProjectDirectory().getPath(), type);
          props.removeProperty(name);
          props.save();
        }
      }
    } catch (StreamException ex) {
      Exceptions.printStackTrace(ex);
      return false;
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
      return false;
    }
    project.getProjectDirectory().refresh();
    
    // recreate property evaluator to avoid race conditions.
    File dirF = project.getProjectDirectoryFile();
    eval.set(PropertyUtils.sequentialPropertyEvaluator(
        stockPropertyPreprovider,
        localProps,
        PropertyUtils.propertiesFilePropertyProvider(new File(dirF, "ant.properties")),
        PropertyUtils.propertiesFilePropertyProvider(new File(dirF, "project.properties"))));
    return true;

  }
}
