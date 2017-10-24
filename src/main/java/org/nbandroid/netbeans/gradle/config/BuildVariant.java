package org.nbandroid.netbeans.gradle.config;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.Variant;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.event.ChangeListener;
import com.google.common.collect.Iterables;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbandroid.netbeans.gradle.AndroidModelAware;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 * Manages set of existing BuildType instances and currently selected one.
 */
public class BuildVariant implements AndroidModelAware {
  private static final Logger LOG = Logger.getLogger(BuildVariant.class.getName());
  
  private static final String PREFERENCE_BUILD_VARIANT = "buildVariant";
  
  @VisibleForTesting
  public static final RequestProcessor RP = new RequestProcessor("gradle-build-variant", 1);

  private final ChangeSupport cs = new ChangeSupport(this);
  private final Object lock = new Object();
  private final AuxiliaryProperties auxProps;
  private AndroidProject aPrj;
  private String variant;

  // TODO: use non-shared AuxiliaryProperties
  
  public BuildVariant(AuxiliaryProperties auxProps) {
    this.auxProps = Preconditions.checkNotNull(auxProps);
    variant = auxProps.get(PREFERENCE_BUILD_VARIANT, false);
  }

  @Nullable
  public String getVariantName() {
    synchronized (lock) {
      if (aPrj == null) {
        return null;
      }
      if (AndroidBuildVariants.findVariantByName(aPrj.getVariants(), variant) != null) {
        return variant;
      }
      // first fallback is 'debug'
      if (AndroidBuildVariants.findVariantByName(aPrj.getVariants(), "debug") != null) {
        return "debug";
      }
      // or use first flavored debug variant
      List<String> debugVariants = Lists.newArrayList(
          Iterables.filter(
            Iterables.transform(
                aPrj.getVariants(),
                new Function<Variant, String>() {
                  @Override
                  public String apply(Variant f) {
                    return f.getName();
                  }
                }),
            new Predicate<String>() {

              @Override
              public boolean apply(String t) {
                return t.endsWith("debug") || t.endsWith("Debug");
              }
            }));
      Collections.sort(debugVariants);
      return debugVariants.isEmpty() ? null : debugVariants.get(0);
    }
  }
  
  public void setVariantName(final String variant) {
    synchronized (lock) {
      this.variant = variant;
      auxProps.put(PREFERENCE_BUILD_VARIANT, variant, false);
      LOG.log(Level.FINE, "saved build variant {0}", variant);
    }
    fireChange();
  }
  
  public Iterable<String> getAllVariantNames() {
    synchronized (lock) {
      return aPrj != null ?
          Iterables.transform(
              aPrj.getVariants(),
              new Function<Variant, String>() {
                @Override
                public String apply(Variant f) {
                  return f.getName();
                }
              }) :
          Collections.<String>emptyList();
    }
  }
  
  public BuildTypeContainer getCurrentBuildTypeContainer() {
    synchronized (lock) {
      Variant v = getCurrentVariant();
      return v != null && aPrj != null ?
          BuildTypes.findBuildTypeByName(aPrj.getBuildTypes(), v.getBuildType()) :
          null;
    }
  }

  public Variant getCurrentVariant() {
    synchronized (lock) {
      return aPrj != null ? AndroidBuildVariants.findVariantByName(aPrj.getVariants(), getVariantName()) : null;
    }
  }

  @Override
  public void setAndroidProject(AndroidProject aPrj) {
    synchronized (lock) {
      this.aPrj = aPrj;
    }
    fireChange();
  }
  
  private void fireChange() {
    RP.post(new Runnable() {

      @Override
      public void run() {
        cs.fireChange();
      }
    });
  }
  public void addChangeListener(ChangeListener listener) {
    cs.addChangeListener(listener);
  }
  
  public void removeChangeListener(ChangeListener listener) {
    cs.removeChangeListener(listener);
  }
}
