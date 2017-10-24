package org.netbeans.modules.android.project.layout;

import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.resources.FrameworkResources;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.VersionQualifier;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.api.ReferenceResolver;

/**
 *
 * @author radim
 */
public class RenderServiceFactory {
  
  public static RenderingService createService(Project prj,
      FolderConfiguration folderCfg, DalvikPlatform platform) throws Exception {
    ReferenceResolver refResolver = prj.getLookup().lookup(ReferenceResolver.class);
    
    folderCfg.setVersionQualifier(new VersionQualifier(platform.getAndroidTarget().getVersion().getApiLevel()));
    LayoutLibrary layoutLib = platform.getLayoutLibrary();
    FrameworkResources frameworkResources = platform.getLayoutLibPlatformResources();
    ResourceRepository prjResRepo = ResourceRepositoryManager.forProject(prj).getProjectResourceRepository();
    return new RenderingService(
        folderCfg, refResolver, platform.getAndroidTarget().getVersion().getApiLevel(), 
        layoutLib, frameworkResources, prjResRepo);
  }
}
