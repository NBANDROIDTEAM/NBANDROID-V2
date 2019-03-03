package org.nbandroid.netbeans.gradle.ui;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.Image;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.api.ui.AndroidPackagesNode;
import org.nbandroid.netbeans.gradle.api.ui.UiUtils;
import org.nbandroid.netbeans.gradle.resources.UiResources;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

// TODO icons, actions
// TODO refresh when build type / flavor changes or files are created/deleted
public class GeneratedSourcesNode extends AbstractNode {

    public GeneratedSourcesNode(String name, Project project, Iterable<String> sourceGroupNames) {
        super(Children.create(new GeneratedSourcesChildFactory(project, sourceGroupNames), true), Lookup.EMPTY);
        setDisplayName(name);
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image img = UiUtils.getTreeFolderIcon(true);
        return ImageUtilities.mergeImages(img, UiResources.GRADLE_BADGE, 6, 6);
    }

    @Override
    public Image getIcon(int type) {
        Image img = UiUtils.getTreeFolderIcon(false);
        return ImageUtilities.mergeImages(img, UiResources.GRADLE_BADGE, 6, 6);
    }

    private static class GeneratedSourcesChildFactory extends ChildFactory<SourceGroup> implements ChangeListener {

        private final Project project;
        private final Iterable<String> sourceGroupNames;
        private final Sources sources;

        public GeneratedSourcesChildFactory(Project project, Iterable<String> sourceGroupNames) {
            this.project = project;
            this.sourceGroupNames = sourceGroupNames;
            sources = ProjectUtils.getSources(project);
            if (sources != null) {
                sources.addChangeListener(WeakListeners.change(this, sources));
            }
        }

        @Override
        protected boolean createKeys(List<SourceGroup> toPopulate) {
            toPopulate.addAll(Lists.newArrayList(Iterables.concat(
                    Iterables.transform(
                            sourceGroupNames,
                            new Function<String, Iterable<SourceGroup>>() {

                        @Override
                        public Iterable<SourceGroup> apply(String f) {
                            return Arrays.asList(sources.getSourceGroups(f));
                        }
                    }))));
            return true;
        }

        @Override
        protected Node createNodeForKey(SourceGroup sg) {
            return new AndroidPackagesNode(sg, project);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
    }
}
