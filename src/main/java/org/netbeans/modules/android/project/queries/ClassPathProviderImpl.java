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

package org.netbeans.modules.android.project.queries;

import org.netbeans.modules.android.project.api.AndroidClassPath;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.api.PropertyName;
import org.netbeans.modules.android.project.api.AndroidProjects;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Defines the various class paths for a Android project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, AndroidClassPath {

    private static final Logger LOG = Logger.getLogger(ClassPathProviderImpl.class.getName());

    private final AndroidProject project;
    private final ClassPath source, boot, compile, execute;
    public ClassPathProviderImpl(AndroidProject project) {
        this.project = project;
        source = createSource();
        boot = createBoot();
        compile = createCompile();
        execute = createExecute(compile);
    }

    public @Override ClassPath findClassPath(FileObject file, String type) {
        // XXX should also provide classpaths for bin/classes?
        if (!isSrcFile(file)) {
            return null;
        }
        return getClassPath(type);
    }

    @Override
    public ClassPath getClassPath(String type) {
      switch (type) {
        case ClassPath.SOURCE:
          return source;
        case ClassPath.BOOT:
          return boot;
        case ClassPath.COMPILE:
          return compile;
        case ClassPath.EXECUTE:
          return execute;
        default:
          return null;
      }
    }

    private boolean isSrcFile(FileObject file) {
        for (FileObject d : project.info().getSourceAndGenDirs()) {
            if (d == null) {
                continue;
            }
            if (file == d || FileUtil.isParentOf(d, file)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void register() {
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {source});
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {boot});
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {compile});
        GlobalPathRegistry.getDefault().register(ClassPath.EXECUTE, new ClassPath[] {execute});
    }
    @Override
    public void unregister() {
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {source});
        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, new ClassPath[] {boot});
        GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, new ClassPath[] {compile});
        GlobalPathRegistry.getDefault().unregister(ClassPath.EXECUTE, new ClassPath[] {execute});
    }

    public ClassPath getSourcePath() {
      return source;
    }

    public ClassPath getCompilePath() {
      return compile;
    }

    public ClassPath getBootPath() {
      return boot;
    }

    private ClassPath createSource() {
      return ClassPathSupport.createClassPath(
            Collections.singletonList(new SourcePathResources()));
    }

  private Iterable<AndroidProject> getAllLibraryProjects() {
    List<AndroidProject> libraries = new ArrayList<>();
    fillLibraryProjects(project, libraries);
    return libraries;
  }


  private static void fillLibraryProjects(AndroidProject aPrj, List<AndroidProject> libraries) {
    for (FileObject libPrjFO : aPrj.info().getDependentProjectDirs()) {
      try {
        Project p = ProjectManager.getDefault().findProject(libPrjFO);
        if (p == null) {
          LOG.log(Level.INFO, "Cannot find library project for {0}", libPrjFO);
          continue;
        }

        // do we have a new project?
        AndroidProject ap = p.getLookup().lookup(AndroidProject.class);
        if (ap == null) {
          continue;
        }
        if (!libraries.contains(ap)) {
          libraries.add(ap);
          // look for more subprojects
          fillLibraryProjects(ap, libraries);
        }
      }
      catch (IOException ex) {
        LOG.log(Level.WARNING, null, ex);
      }
    }
  }

  private class CompilePathResources extends AndroidPathResources {

    public CompilePathResources() {
    }

    @Override
    public URL[] getRoots() {
      List<URL> roots = new ArrayList<>();
      for(AndroidProject library : getAllLibraryProjects()) {
        roots.add(FileUtil.urlForArchiveOrDir(
            FileUtil.normalizeFile(new File(library.getProjectDirectoryFile(), "bin/classes.jar"))));
      }
      LOG.log(Level.FINE, "compile CP roots: {0}", roots);
      return roots.toArray(new URL[0]);
    }
  }

  private class SourcePathResources extends AndroidPathResources {

    public SourcePathResources() {
    }

    @Override
    public URL[] getRoots() {
      File genRootFile = new File(project.getProjectDirectoryFile(), "gen");
      if (!genRootFile.exists()) {
        genRootFile.mkdir();
        LOG.log(Level.CONFIG,
            "Creating {0} directory to workaround classpath problems", genRootFile);
      }
      List<URL> roots = Lists.newArrayList(Iterables.transform(
          project.info().getSourceDirs(), 
          new Function<FileObject, URL>() {
            @Override
            public URL apply(FileObject sourceDir) {
                  return FileUtil.urlForArchiveOrDir(FileUtil.toFile(sourceDir));
            }
          }));
      roots.add(FileUtil.urlForArchiveOrDir(genRootFile));

      LOG.log(Level.FINE, "source roots: {0}", roots);
      return roots.toArray(new URL[0]);
    }
  }

  private abstract class AndroidPathResources extends PathResourceBase implements PropertyChangeListener {

      public AndroidPathResources() {
        project.evaluator().addPropertyChangeListener(this);
      }

    @Override
    public final ClassPathImplementation getContent() {
      // should not be called
      return null;
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
      // TODO only fire change when set of roots is really different.
      firePropertyChange(PROP_ROOTS, null, null);
    }
  }

  private ClassPath createBoot() {
    return ClassPathSupport.createClassPath(
        Collections.singletonList(new URLPathResources(project)));
  }

  private static class URLPathResources extends PathResourceBase {
    private final AndroidProject project;

    public URLPathResources(AndroidProject project) {
      this.project = project;
      project.evaluator().addPropertyChangeListener(new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName() == null
              || PropertyName.SDK_DIR.getName().equals(evt.getPropertyName())
              || PropertyName.TARGET.getName().equals(evt.getPropertyName())) {
            firePropertyChange(PROP_ROOTS, null, null);
          }
        }
      });
    }

    @Override
    public URL[] getRoots() {
      String sdkDir = project.evaluator().getProperty(PropertyName.SDK_DIR.getName());
      if (sdkDir != null) {
        DalvikPlatform platform = AndroidProjects.projectPlatform(project);
        if (platform != null) {
          return platform.getBootstrapLibraries().toArray(new URL[0]);
        }
      }
      // TODO platform for test project needs to be fixed in the similar way?
      FileObject basedir = project.getProjectDirectory();
      if (basedir.getName().equals("tests")) {
        basedir = basedir.getParent();
      }
      FileObject parent = basedir.getParent();
      if (parent != null && parent.getName().matches("android-.+")) {
        FileObject samples = parent.getParent();
        if (samples != null && samples.getName().equals("samples")) {
          FileObject sdk = samples.getParent();
          if (sdk != null) {
            FileObject androidJar = sdk.getFileObject("platforms/" + parent.getName() + "/android.jar");
            if (androidJar != null && FileUtil.isArchiveFile(androidJar)) {
              try {
                return new URL[] { FileUtil.getArchiveRoot(androidJar).getURL() };
              } catch (FileStateInvalidException ex) {
                LOG.log(Level.WARNING, "could not find boot classpath in " + project.getProjectDirectoryFile(), ex);
              }
            }
          }
        }
      }
      LOG.log(Level.WARNING, "could not find any android.jar for {0}", project.getProjectDirectoryFile());
      return new URL[0]; // broken platform
    }

    @Override
    public ClassPathImplementation getContent() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

  }

    private ClassPath createCompile() {
      List<PathResourceBase> pathResources = Lists.newArrayList();
      pathResources.add(new LibPathResources(project));
      pathResources.add(new CompilePathResources());
      return ClassPathSupport.createClassPath(pathResources);
    }

    private class LibPathResources extends PathResourceBase 
        implements FileChangeListener, PropertyChangeListener {
      private class ProjectDirs {
        private File libsDir;
        private File mainLibsDir;
        private File mainPrjDir;
      }

      private AndroidProject project;
      private volatile boolean isInited = false;
      private Map<AndroidProject, ProjectDirs> projectDirs;
      
      public LibPathResources(AndroidProject project) {
        this.project = project;
        PropertyEvaluator propEvaluator = project.evaluator();
        propEvaluator.addPropertyChangeListener(this);
      }

      private void setup() {
        if (projectDirs == null) {
          projectDirs = Maps.newHashMap();
          ProjectDirs dirs = new ProjectDirs();
          setup(project, dirs);
          projectDirs.put(project, dirs);
          for (AndroidProject library : getAllLibraryProjects()) {
            dirs = new ProjectDirs();
            setup(library, dirs);
            projectDirs.put(library, dirs);
          }
        } else {
          for (Map.Entry<AndroidProject, ProjectDirs> entry : projectDirs.entrySet()) {
            setup(entry.getKey(), entry.getValue());
          }
        }
      }
      
      private void setup(AndroidProject aProject, ProjectDirs dirs) {
        if (dirs.libsDir != null) {
          try {
            FileUtil.removeFileChangeListener(this, dirs.libsDir);
          } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINER, null, ex);
          }
        }
        if (dirs.mainLibsDir != null) {
          try {
            FileUtil.removeFileChangeListener(this, dirs.mainLibsDir);
          } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINER, null, ex);
          }
        }
        File basedir = aProject.getProjectDirectoryFile();
        PropertyEvaluator propEvaluator = aProject.evaluator();
        String mainPrjPath = aProject.evaluator().getProperty(PropertyName.TEST_PROJECT_DIR.getName());

        if (mainPrjPath != null) {
          LOG.log(Level.FINE, "setting up CP for {0} that is test for {1}", new Object[]{project, mainPrjPath});
          // test project: ${main.project}/bin/classes
          //               + libs/*.jar (jar.libs.dir property is ignored)
          //               + ${jar.libs.dir}/libs/*.jar
          dirs.mainPrjDir = PropertyUtils.resolveFile(basedir, mainPrjPath);
          //XXX: should refer to the correct "libs" directory in the main project:
          dirs.mainLibsDir = FileUtil.normalizeFile(new File(dirs.mainPrjDir, "libs"));
        }
        
        String libsDirValue = propEvaluator.getProperty(PropertyName.JAR_LIBS_DIR.getName());
        if (libsDirValue == null) {
          libsDirValue = "libs";
        }
        dirs.libsDir = PropertyUtils.resolveFile(basedir, libsDirValue);
        try {
          FileUtil.addFileChangeListener(this, dirs.libsDir);
        } catch (IllegalArgumentException ex) {
          LOG.log(Level.FINER, null, ex);
        }
        if (dirs.mainLibsDir != null) {
          try {
            FileUtil.addFileChangeListener(this, dirs.mainLibsDir);
          } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINER, null, ex);
          }
        }
        isInited = true;
      }

      @Override
      public URL[] getRoots() {
        if (!isInited) {
          setup();
        }
        List<URL> roots = new ArrayList<>();
        if (projectDirs != null) {
          for (ProjectDirs dirs : projectDirs.values()) {
            addJars(dirs.libsDir, roots);
            addJars(dirs.mainLibsDir, roots);
            if (dirs.mainPrjDir != null) {
              roots.add(FileUtil.urlForArchiveOrDir(new File(dirs.mainPrjDir, "bin/classes")));
            }
          }
        }
        LOG.log(Level.FINE, "library roots: {0}", roots);
        return Iterables.toArray(
            Iterables.filter(roots, Predicates.notNull()),
            URL.class);
      }
      
      private void addJars(File from, List<URL> to) {
        if (from == null) return;
        File[] children = from.listFiles();
        if (children == null) return;
        for (File libFile : children) {
          if (libFile.getName().endsWith(".jar")) {
            to.add(FileUtil.urlForArchiveOrDir(libFile));
          }
        }
      }

      @Override
      public ClassPathImplementation getContent() {
        // should not be called
        return null;
      }

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        LOG.log(Level.FINER, "prop evaluator changed " + evt, new Exception("tracing"));
        setup();
        updateCP();
      }

      @Override
      public void fileFolderCreated(FileEvent fe) {
        updateCP();
      }

      @Override
      public void fileDataCreated(FileEvent fe) {
        updateCP();
      }

      @Override
      public void fileChanged(FileEvent fe) {
        // no-op
      }

      @Override
      public void fileDeleted(FileEvent fe) {
        updateCP();
      }

      @Override
      public void fileRenamed(FileRenameEvent fe) {
        updateCP();
      }

      @Override
      public void fileAttributeChanged(FileAttributeEvent fe) {
        // no-op
      }

      private void updateCP() {
        firePropertyChange(PROP_ROOTS, null, null);
      }

    }

    private ClassPath createExecute(ClassPath compile) {
        return ClassPathSupport.createProxyClassPath(compile, ClassPathSupport.createClassPath(
                FileUtil.urlForArchiveOrDir(new File(project.getProjectDirectoryFile(), "bin/classes"))));
    }
}
