package org.nbandroid.netbeans.gradle.query;

import com.android.builder.model.AndroidProject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.AndroidModelAware;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.spi.DalvikPlatformResolver;

/**
 *
 * @author radim
 */
public class GradlePlatformResolver implements DalvikPlatformResolver, AndroidModelAware {
  private static final Logger LOG = Logger.getLogger(GradlePlatformResolver.class.getName());

  private AndroidProject aPrj;
  
  @Override
  public DalvikPlatform findDalvikPlatform() {
    if (aPrj == null) {
      return null;
    }
    LOG.log(Level.FINE, "look for dalvik platform for {0}", aPrj.getCompileTarget());
    return DalvikPlatformManager.getDefault().findPlatformForTarget(aPrj.getCompileTarget());
  }

  @Override
  public void setAndroidProject(AndroidProject aPrj) {
    this.aPrj = aPrj;
  }
  
}
