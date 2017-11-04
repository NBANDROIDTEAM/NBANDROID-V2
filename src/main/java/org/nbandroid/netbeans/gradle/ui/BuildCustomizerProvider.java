package org.nbandroid.netbeans.gradle.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author radim
 */
public class BuildCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CUSTOMIZER_BUILD_VARIANT = "BuildVariant";

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CUSTOMIZER_BUILD_VARIANT, "Build Variant", null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        final BuildCustomizerPanel panel = new BuildCustomizerPanel();
        Project prj = context.lookup(Project.class);
        if (prj != null) {
            final BuildVariant buildVariant = prj.getLookup().lookup(BuildVariant.class);
            if (buildVariant != null) {
                panel.setData(buildVariant);
                category.setStoreListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        buildVariant.setVariantName(panel.getData());
                    }
                });
            }
        }
        return panel;
    }

}
