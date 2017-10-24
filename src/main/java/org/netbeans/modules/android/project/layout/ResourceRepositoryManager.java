package org.netbeans.modules.android.project.layout;

import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.configuration.LanguageQualifier;
import com.android.ide.common.resources.configuration.RegionQualifier;
import com.android.io.FolderWrapper;
import com.android.io.IAbstractFolder;
import com.android.util.Pair;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.spi.AndroidProjectDirectory;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author radim
 */
public class ResourceRepositoryManager {
  private static final Logger LOG = Logger.getLogger(ResourceRepositoryManager.class.getName());

  public static class LocaleConfig {
    private final Pair<LanguageQualifier, RegionQualifier> localePair;
    private final boolean theOnlyLocale;

    public static LocaleConfig theOnlyLocale() {
      return new LocaleConfig(
        new LanguageQualifier(LanguageQualifier.FAKE_LANG_VALUE), 
        new RegionQualifier(RegionQualifier.FAKE_REGION_VALUE), 
        true);
    }
    public LocaleConfig(LanguageQualifier lang, RegionQualifier region) {
      this(lang, region, false);
    }
    
    private LocaleConfig(LanguageQualifier lang, RegionQualifier region, boolean theOnlyLocale) {
      this.localePair = Pair.of(lang, region);
      this.theOnlyLocale = theOnlyLocale;
    }

    public LanguageQualifier getLanguage() {
      return localePair.getFirst();
    }
    
    public RegionQualifier getRegion() {
      return localePair.getSecond();
    }

    public String getLongDisplayValue() {
      StringBuilder sb = new StringBuilder();
      if (!getLanguage().hasFakeValue()) {
        sb.append(getLanguage().getShortDisplayValue());
        if (!getRegion().hasFakeValue()) {
          sb.append(" / ").append(getRegion().getShortDisplayValue());
        }
      } else {
        sb.append(theOnlyLocale ? "Any" : "Other");
      }
      return sb.toString();
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 17 * hash + (this.localePair != null ? this.localePair.hashCode() : 0);
      hash = 17 * hash + (this.theOnlyLocale ? 1 : 0);
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final LocaleConfig other = (LocaleConfig) obj;
      if (this.localePair != other.localePair && (this.localePair == null || !this.localePair.equals(other.localePair))) {
        return false;
      }
      if (this.theOnlyLocale != other.theOnlyLocale) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "LocaleConfig{" + 
          (theOnlyLocale ? "theOnlyLocale" : "localePair: " + localePair) + '}';
    }
  }
  
  // TODO fix to avoid AndroidProject dependency
  public static ResourceRepositoryManager forProject(Project ap) {
    return new ResourceRepositoryManager(ap);
  }
  
  private final Project ap;

  private ResourceRepositoryManager(Project ap) {
    this.ap = ap;
  }
  
  // TODO possibly cache the result or return updating ResourceRepository
  
  public ResourceRepository getProjectResourceRepository() throws IOException {
    ResourceRepository prjResRepo = new ResourceRepository(
        resFolderForProject(ap), /* isFrameworkRepository */false) {
      @Override
      protected ResourceItem createResourceItem(String name) {
        return new ResourceItem(name);
      }
    };
    prjResRepo.loadResources();
    return prjResRepo;
  }
  
  public static List<LocaleConfig> getLocaleConfigs(Project ap) {
    List<LocaleConfig> locales = Lists.newArrayList();
    if (ap != null) {
      try {
        ResourceRepository repo = forProject(ap).getProjectResourceRepository();
        for (String lang : repo.getLanguages()) {
          LanguageQualifier langQ = new LanguageQualifier(lang);
          for (String region : repo.getRegions(lang)) {
            RegionQualifier regionQ = new RegionQualifier(region);
            locales.add(new LocaleConfig(langQ, regionQ));
          }
          locales.add(new LocaleConfig(langQ, new RegionQualifier(RegionQualifier.FAKE_REGION_VALUE)));
        }
      } catch (IOException ex) {
        LOG.log(Level.FINE, null, ex);
      }
    }
    if (locales.isEmpty()) {
      locales.add(LocaleConfig.theOnlyLocale());
    } else {
      locales.add(new LocaleConfig(
          new LanguageQualifier(LanguageQualifier.FAKE_LANG_VALUE), 
          new RegionQualifier(RegionQualifier.FAKE_REGION_VALUE)));
    }
    return locales;
  }

  @Nonnull
  private static IAbstractFolder resFolderForProject(Project prj) {
    SourceGroup[] resGroups = ProjectUtils.getSources(prj).getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES);
    IAbstractFolder resFolder;
    if (resGroups != null && resGroups.length > 0) {
      resFolder = new FolderWrapper(FileUtil.toFile(resGroups[0].getRootFolder()));
    } else {
      String prjFolder = prj.getLookup().lookup(AndroidProjectDirectory.class).get().getPath();
      resFolder = new FolderWrapper(prjFolder + "/res");
    }
    return resFolder;
  }
}
