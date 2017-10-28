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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * Miscellaneous utilities for the android module.
 */
public class AndroidProjectUtil {
  private static final Logger LOG = Logger.getLogger(AndroidProjectUtil.class.getName());
  private static final String ACTIVITY = "android.app.Activity";      //NOI18N

    private AndroidProjectUtil () {}

    public static Collection<ElementHandle<TypeElement>> getActivityClass (final FileObject file) {
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        try {
            JavaSource js = JavaSource.forFileObject(file);
            js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController c) throws Exception {
                    c.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    final TypeElement activity = c.getElements().getTypeElement(ACTIVITY);
                    final List<? extends TypeElement> topElements = c.getTopLevelElements();
                    for (TypeElement e : topElements) {
                        if (e.getModifiers().contains(Modifier.PUBLIC) && isActivityClass(e, activity)) {
                            result.add(ElementHandle.create(e));
                            return;
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    public static boolean isActivityClass (final String className, ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        final boolean[] result = new boolean[] {false};
        try {
            final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
            final JavaSource js = JavaSource.create(cpInfo);            
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController c) throws Exception {
                    final TypeElement activity = c.getElements().getTypeElement(ACTIVITY);
                    if (activity == null) {
                        return;
                    }
                    TypeElement e = c.getElements().getTypeElement(className);
                    result[0] = isActivityClass(e, activity);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);            
        }
        return result[0];
    }

    private static boolean isActivityClass (TypeElement e, final TypeElement activity) {
        while (e != null) {
            if (e.equals(activity)) {
                return true;
            }
            e = (TypeElement)((DeclaredType)e.getSuperclass()).asElement();
       }
        return false;
    }


    public static Collection<ElementHandle<TypeElement>> getActivities (final FileObject[] sourceRoots) {
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        try {            
            final ClasspathInfo cpInfo = ClasspathInfo.create(sourceRoots[0]);
            final JavaSource js = JavaSource.create(cpInfo);
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController c) throws Exception {
                    final TypeElement activity = c.getElements().getTypeElement(ACTIVITY);
                    if (activity == null) {
                        return;
                    }
                    final ElementHandle<TypeElement> activityHandle = ElementHandle.create(activity);
                    final Set<ElementHandle<TypeElement>> impls = c.getClasspathInfo().getClassIndex().getElements(activityHandle, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
                    for (ElementHandle<TypeElement> eh : impls) {
                        TypeElement e = eh.resolve(c);
                        if (isActivityClass(e, activity)) {
                            result.add(eh);
                        }
                    }
                }
            }, true);            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

  /**
   * Creates new Android project using {@code ProjectCreator} from SDK.
   */
    public static void create(AndroidGeneralData data, String packageName, String activityEntry) {
        //TODO remove is for ant
//    SdkManager sdkManager = data.getPlatform().getSdkManager();
//    ProjectCreator prjCreator = new ProjectCreator(
//        sdkManager, sdkManager.getLocation(),
//        ProjectCreator.OutputLevel.NORMAL, SdkLogProvider.createLogger(true));
//    prjCreator.createProject(data.getProjectDirPath(),
//        data.getProjectName(),
//        packageName,
//        activityEntry,
//        data.getPlatform().getAndroidTarget(),
//        /*library*/false,
//        data.getMainProjectDirPath());
  }

  /*@VisibleForTesting*/ static DalvikPlatform toDalvikPlatorm(String targetDir) {
    DalvikPlatformManager dpm = DalvikPlatformManager.getDefault();
    try {
      URL targetDirURL = new URL(targetDir);
      FileObject targetDirFO = URLMapper.findFileObject(targetDirURL);
      if (targetDirFO == null || targetDirFO.getParent() == null || targetDirFO.getParent().getParent() == null) {
        return null;
      }
      FileObject sdkDirFO = targetDirFO.getParent().getParent();
      if (dpm.getSdkLocation() == null) {
        dpm.setSdkLocation(sdkDirFO.getPath());
      } else if (!sdkDirFO.getPath().equals(dpm.getSdkLocation())) {
        // SDK in old platform and new settings do not match
        return null;
      }
      for (DalvikPlatform p : dpm.getPlatforms()) {
        if (targetDirFO.equals(p.getInstallFolder())) {
          return p;
        }
      }
    } catch (MalformedURLException ex) {
      LOG.log(Level.FINE, null, ex);
    }
    return null;
  }
}
