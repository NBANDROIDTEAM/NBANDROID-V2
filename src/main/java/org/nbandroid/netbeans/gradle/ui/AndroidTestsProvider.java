package org.nbandroid.netbeans.gradle.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.nbandroid.netbeans.gradle.config.AndroidTestRunConfiguration;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author radim
 */
public class AndroidTestsProvider implements ProjectCustomizer.CompositeCategoryProvider {
  private static final String CUSTOMIZER_TESTS = "AndroidTests";

  @Override
  public ProjectCustomizer.Category createCategory(Lookup context) {
    return ProjectCustomizer.Category.create(CUSTOMIZER_TESTS, "Android Tests", null);
  }

  @Override
  public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
    final AndroidTestsPanel panel = new AndroidTestsPanel();
    Project prj = context.lookup(Project.class);
    if (prj != null) {
      final AndroidTestRunConfiguration testRunCfg = prj.getLookup().lookup(AndroidTestRunConfiguration.class);
      if (testRunCfg != null) {
        panel.setData(testRunCfg);
        category.setStoreListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            testRunCfg.setTestRunner(panel.getData());
          }
        });
      }
    }
    return panel;
  }
  
}
