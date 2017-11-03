package org.nbandroid.netbeans.gradle.query;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.File;
import org.nbandroid.netbeans.gradle.AndroidModelAware;
import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.netbeans.api.project.Project;
import org.nbandroid.netbeans.gradle.api.AndroidManifestSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 *
 * @author radim
 */
public class GradleAndroidManifest implements AndroidManifestSource, AndroidModelAware {

  private AndroidProject aProject;
  private final BuildVariant buildConfig;
  private final ChangeSupport cs = new ChangeSupport(this);

  public GradleAndroidManifest(Project project, BuildVariant buildConfig) {
    this.buildConfig = Preconditions.checkNotNull(buildConfig);
  }

  @Override
  public FileObject get() {
    if (aProject != null) {
      File manifestFile = aProject.getDefaultConfig().getSourceProvider().getManifestFile();

      BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
      File buildManifestFile = buildTypeContainer != null ?
          buildTypeContainer.getSourceProvider().getManifestFile() : 
          null;
      File m = Iterables.find(Lists.newArrayList(manifestFile, buildManifestFile), Predicates.notNull(), null);
      if (m != null) {
        return FileUtil.toFileObject(m);
      }
    }
    return null;
  }

  @Override
  public void setAndroidProject(AndroidProject aPrj) {
    aProject = aPrj;
    cs.fireChange();
  }
}
