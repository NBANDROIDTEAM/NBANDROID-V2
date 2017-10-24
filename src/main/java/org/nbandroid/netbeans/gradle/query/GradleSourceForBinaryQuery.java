package org.nbandroid.netbeans.gradle.query;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.Variant;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.AndroidModelAware;
import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.nbandroid.netbeans.gradle.config.ProductFlavors;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Source lookup for classes found in exploded-bundles directory containing 
 * AndroidLibrary dependencies.
 * 
 * @author radim
 */
public class GradleSourceForBinaryQuery implements SourceForBinaryQueryImplementation2, AndroidModelAware {
  private static final Logger LOG = Logger.getLogger(GradleSourceForBinaryQuery.class.getName());

  private final BuildVariant buildConfig;
  private AndroidProject project;

  public GradleSourceForBinaryQuery(BuildVariant buildConfig) {
    this.buildConfig = buildConfig;
  }
  
  @Override
  public void setAndroidProject(AndroidProject project) {
    this.project = project;
  }
  
  @Override
  public Result findSourceRoots2(final URL binaryRoot) {
    Result r = findAarLibraryRoots(binaryRoot);
    if (r != null) {
      return r;
    }
    if (project == null) {
      return null;
    }
    final File binRootDir = FileUtil.archiveOrDirForURL(binaryRoot);
    if (binRootDir == null) {
      return null;
    }
    
    Variant variant = Iterables.find(
        project.getVariants(),
        new Predicate<Variant>() {
          @Override
          public boolean apply(Variant input) {
            return binRootDir.equals(input.getMainArtifact().getClassesFolder());
          }
        },
        null);
    if (variant != null) {
      Iterable<FileObject> srcRoots = Iterables.filter(
          Iterables.transform(
              sourceRootsForVariant(variant),
              new Function<File, FileObject>() {
                @Override
                public FileObject apply(File f) {
                  return FileUtil.toFileObject(f);
                }
              }),
          Predicates.notNull());
      return new GradleSourceResult(srcRoots);
    }
    return null;
  }

  private Iterable<? extends File> sourceRootsForVariant(Variant variant) {
    Collection<File> javaDirs = project != null
        ? project.getDefaultConfig().getSourceProvider().getJavaDirectories()
        : Collections.<File>emptySet();
    BuildTypeContainer buildTypeContainer = buildConfig.getCurrentBuildTypeContainer();
    Collection<File> typeJavaDirs = buildTypeContainer != null
        ? buildTypeContainer.getSourceProvider().getJavaDirectories()
        : Collections.<File>emptySet();
    Iterable<File> variantJavaDirs = variant != null
        ? Iterables.concat(
            Iterables.transform(
                variant.getProductFlavors(),
                new Function<String, Collection<File>>() {
                  @Override
                  public Collection<File> apply(String f) {
                    if (project == null) {
                      return Collections.<File>emptySet();
                    }
                    final ProductFlavorContainer flavor = 
                        ProductFlavors.findFlavorByName(project.getProductFlavors(), f);
                    if (flavor == null) {
                      return Collections.<File>emptySet();
                    }
                    return flavor.getSourceProvider().getJavaDirectories();
                  }
                }))
        : Collections.<File>emptySet();
    Collection<File> generatedJavaDirs = variant != null
        ? variant.getMainArtifact().getGeneratedSourceFolders()
        : Collections.<File>emptyList();
    return Iterables.concat(
        javaDirs,
        typeJavaDirs,
        variantJavaDirs,
        generatedJavaDirs);
  }
  
  private Result findAarLibraryRoots(final URL binaryRoot) {
    // FileUtil.getArchiveFile(binaryRoot);
    AndroidLibrary aLib = null;
    Variant variant = buildConfig.getCurrentVariant();
    if (variant != null) {
      aLib = Iterables.find(
          variant.getMainArtifact().getDependencies().getLibraries(), 
          new Predicate<AndroidLibrary>() {

            @Override
            public boolean apply(AndroidLibrary lib) {
              URL libUrl = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(lib.getJarFile()));
              return binaryRoot.equals(libUrl);
            }
          },
          null);
    }
    if (aLib == null) {
      return null;
    }
//    if (aLib instanceof AndroidLibraryProject) {
//      AndroidLibraryProject libPrj = (AndroidLibraryProject) aLib;
//      LOG.log(Level.FINE, "Found binary from AndroidLibrary {0}", libPrj.getProjectPath());
//    } else {
      LOG.log(Level.FINE, "Found unknown binary from AndroidLibrary {0}", aLib.getJarFile());
//    }
    return null;
  }

  @Override
  public GradleSourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
    return findSourceRoots2(binaryRoot);
  }
  
  private class GradleSourceResult implements SourceForBinaryQueryImplementation2.Result {

    private final Iterable<FileObject> sourceRoots;

    public GradleSourceResult(Iterable<FileObject> sourceRoots) {
      this.sourceRoots = sourceRoots;
    }
    
    public @Override
    FileObject[] getRoots() {
      List<FileObject> roots = Lists.newArrayList(sourceRoots);
      LOG.log(Level.FINE, "return sources for binary in root {0}: {1}", new Object[]{project, roots});
      return roots.toArray(new FileObject[roots.size()]);
    }

    public @Override
    void addChangeListener(ChangeListener l) {
    }

    public @Override
    void removeChangeListener(ChangeListener l) {
    }

    @Override
    public boolean preferSources() {
      return true;
    }

  }
}
