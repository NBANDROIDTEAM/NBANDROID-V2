package org.nbandroid.netbeans.gradle.launch;

import com.android.ddmlib.Client;
import com.android.ide.common.xml.ManifestData;
import com.google.common.collect.Maps;
import java.util.Map;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.nbandroid.netbeans.gradle.api.AndroidProjects;
import org.nbandroid.netbeans.gradle.spi.AndroidDebugInfo;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author radim
 */
public class GradleDebugInfo implements AndroidDebugInfo {

  private final Project project;

  public GradleDebugInfo(Project project) {
    this.project = project;
  }

  @Override
  public boolean supportsDebugging() {
    return true;
  }

  @Override
  public boolean canDebug(String processName) {
    ManifestData manifest = AndroidProjects.parseProjectManifest(project);
    return manifest != null && 
        (manifest.getPackage().equals(processName) || processName.startsWith(manifest.getPackage() + "."));
  }

  @Override
  public AndroidDebugData data(Client client) {
    final int port = client.getDebuggerListenPort();
    final Map<String, Object> properties = Maps.newHashMap();
    final GradleAndroidClassPathProvider cpp = project.getLookup().lookup(GradleAndroidClassPathProvider.class);
    final ClassPath sourcePath = cpp.getSourcePath();
    final ClassPath compilePath = cpp.getCompilePath();
    final ClassPath bootPath = cpp.getBootPath();
    properties.put("sourcepath",
        ClassPathSupport.createProxyClassPath(sourcePath, Launches.toSourcePath(compilePath)));
    properties.put("name", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N
    properties.put("jdksources", Launches.toSourcePath(bootPath)); // NOI18N
    properties.put("baseDir", FileUtil.toFile(project.getProjectDirectory()));   //NOI18N
    return new AndroidDebugData("localhost", port, properties);
  }

  @Override
  public Project project() {
    return project;
  }
}
