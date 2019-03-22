/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.project.api;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.ProjectBuildOutput;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nbandroid.gradle.spi.ModelRefresh;
import nbandroid.gradle.spi.RootGoalsNavigatorHint;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.nbandroid.netbeans.gradle.api.TestOutputConsumer;
import org.nbandroid.netbeans.gradle.launch.Launches;
import org.nbandroid.netbeans.gradle.testrunner.TestOutputConsumerLookupProvider;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.android.project.actions.AndroidProjectActionProvider;
import static org.netbeans.modules.android.project.api.NbAndroidProject.RP;
import org.netbeans.modules.android.project.api.nodes.MultiNodeFactory;
import org.netbeans.modules.android.project.api.nodes.MultiNodeFactoryProvider;
import org.netbeans.modules.android.project.api.nodes.NodeFactory;
import org.netbeans.modules.android.project.build.BuildVariant;
import org.netbeans.modules.android.project.debug.GradleDebugInfo;
import org.netbeans.modules.android.project.launch.GradleLaunchExecutor;
import org.netbeans.modules.android.project.properties.AndroidCustomizerProvider;
import org.netbeans.modules.android.project.query.AndroidClassPathProvider;
import org.netbeans.modules.android.project.query.AutoAndroidJavadocForBinaryQuery;
import org.netbeans.modules.android.project.query.AutoAndroidSourceForBinaryQuery;
import org.netbeans.modules.android.project.query.GradleAndroidManifest;
import org.netbeans.modules.android.project.query.GradlePlatformResolver;
import org.netbeans.modules.android.project.query.GradleSourceForBinaryQuery;
import org.netbeans.modules.android.project.query.ProjectRefResolver;
import org.netbeans.modules.android.project.run.AndroidTestRunConfiguration;
import org.netbeans.modules.android.project.sources.AndroidSources;
import org.netbeans.modules.android.project.sources.SourceLevelQueryImpl;
import org.netbeans.modules.android.spi.DebugActivityConfiguration;
import org.netbeans.modules.android.spi.RunActivityConfiguration;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author arsi
 */
public class NbAndroidProjectImpl extends NbAndroidProject {

    @StaticResource()
    public static final String PROJECT_ICON = "org/netbeans/modules/android/api/android_project.png";
    private final BuildVariant buildVariant;
    private final AndroidClassPathProvider androidClassPathProvider;
    protected static final AutoAndroidSourceForBinaryQuery SOURCE_FOR_BINARY_QUERY = Lookup.getDefault().lookup(AutoAndroidSourceForBinaryQuery.class);
    protected static final AutoAndroidJavadocForBinaryQuery JAVADOC_FOR_BINARY_QUERY = Lookup.getDefault().lookup(AutoAndroidJavadocForBinaryQuery.class);
    protected final GradleLaunchExecutor launchExecutor = new GradleLaunchExecutor(this);
    public NbAndroidProjectImpl(FileObject projectDirectory, ProjectState ps) {
        super(projectDirectory, ps);
        ic.add(new Info());
        ic.add(launchExecutor);
        ic.add(new RunActivityConfiguration(this));
        ic.add(new DebugActivityConfiguration(this));
        ic.add(new NbAndroidProjectConfigurationProvider());
        ic.add(new AndroidCustomizerProvider(this));
        ic.add(new AndroidProjectLogicalView());
        ic.add(new SourceLevelQueryImpl(this));
        ic.add(new AndroidTestRunConfiguration(this));
        buildVariant = new BuildVariant(this);
        ic.add(buildVariant);
        ic.add(new GradleAndroidRepositoriesProvider(this));
        ic.add(new GradlePlatformResolver());
        ic.add(new AndroidSources(this, buildVariant));
        ic.add(new GradleAndroidManifest(this, buildVariant));
        ic.add(new GradleSourceForBinaryQuery(this, buildVariant));
        androidClassPathProvider = new AndroidClassPathProvider(buildVariant, this);
        ic.add(androidClassPathProvider);
        ic.add(new GradleDebugInfo(this));
        ic.add(new PrivilegedTemplatesImpl());
        ic.add(new ProjectRefResolver(this));
        ic.add(Launches.createLauncher());
        ic.add(new TestOutputConsumerLookupProvider().createAdditionalLookup(
                Lookups.singleton(this)).lookup(TestOutputConsumer.class));
        ic.add(new AndroidProjectActionProvider(this));

    }

    @Override
    protected Class[] getGradleModels() {
        return new Class[]{GradleBuild.class, ProjectBuildOutput.class, AndroidProject.class};
    }

    @Override
    public ModelRefresh getModelRefresh() {
        return new ModelRefresh() {
            @Override
            public void refreshModels() {
                RP.execute(NbAndroidProjectImpl.this);
            }
        };
    }

    public BuildVariant getBuildVariant() {
        return buildVariant;
    }

    public final class AndroidProjectLogicalView implements LogicalViewProvider {

        @Override
        public Node createLogicalView() {
            FileObject projectDirectory = NbAndroidProjectImpl.this.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            try {
                return new ProjectNode(nodeOfProjectFolder);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return new AbstractNode(Children.LEAF);
            }
        }

        @Override
        public Node findPath(Node node, Object o) {
            return null;
        }

    }

    private final class ProjectNode extends FilterNode {

        public ProjectNode(Node node)
                throws DataObjectNotFoundException {
            super(node,
                    new ProjectNodes(),
                    new ProxyLookup(
                            new Lookup[]{
                                Lookups.singleton(NbAndroidProjectImpl.this),
                                node.getLookup(), Lookups.singleton(new RootGoalsNavigatorHint()),
                                modelLookup
                            }));
        }

        @Override
        public Action[] getActions(boolean arg0) {
            List<Action> projectActions = new ArrayList<>(32);
            projectActions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, "Clean and Build", null));
            projectActions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, "Clean", null));
            projectActions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, "Buld", null));
            projectActions.add(null);
            projectActions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, "Run", null));
            projectActions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, "Debug", null));
            projectActions.add(null);
            List<? extends Action> actionsForPath = Utilities.actionsForPath("Android/Projects/Project");
            projectActions.addAll(actionsForPath);
            projectActions.add(CommonProjectActions.closeProjectAction());
            projectActions.add(CommonProjectActions.setAsMainProjectAction());
            projectActions.add(null);
            projectActions.addAll(Utilities.actionsForPath("Projects/Actions"));
            projectActions.add(null);
            projectActions.add(CommonProjectActions.customizeProjectAction());
            return projectActions.toArray(new Action[projectActions.size()]);

        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(PROJECT_ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return NbAndroidProjectImpl.this.getProjectDirectory().getName();
        }

    }

    public class ProjectNodes extends Children.Keys<Node> implements ChangeListener {

        private final List<MultiNodeFactory> multiNodeFactories = new ArrayList<>();
        private final List<Node> staticNodes = new ArrayList<>();

        public ProjectNodes() {
            super(true);
            List<MultiNodeFactoryProvider> factoryProviders = MultiNodeFactoryProvider.findAll();
            for (MultiNodeFactoryProvider factoryProvider : factoryProviders) {
                MultiNodeFactory createMultiNodeFactory = factoryProvider.createMultiNodeFactory(NbAndroidProjectImpl.this);
                multiNodeFactories.add(createMultiNodeFactory);
                createMultiNodeFactory.addChangeListener(this);
            }
            List<NodeFactory> findAll = NodeFactory.findAll();
            for (int i = 0; i < findAll.size(); i++) {
                NodeFactory factory = findAll.get(i);
                Node node = factory.createNode(NbAndroidProjectImpl.this);
                if (node != null) {
                    staticNodes.add(node);
                }
            }
            refreshNodes();
        }

        private void refreshNodes() {

            List<Node> childrens = new ArrayList<>();
            for (MultiNodeFactory multiNodeFactory : multiNodeFactories) {
                childrens.addAll(multiNodeFactory.createNodes());
            }
            childrens.addAll(staticNodes);

            setKeys(childrens);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[]{key};
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refreshNodes();
        }

    }

    private final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(PROJECT_ICON));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return NbAndroidProjectImpl.this;
        }

    }

    @Override
    public void resultChanged(LookupEvent ev) {
        super.resultChanged(ev);
        SOURCE_FOR_BINARY_QUERY.removeClassPathProvider(androidClassPathProvider);
        JAVADOC_FOR_BINARY_QUERY.removeClassPathProvider(androidClassPathProvider);
        Set<Class<? extends Object>> allClasses = modelLookupResult.allClasses();
        if (allClasses.contains(GradleBuild.class) && allClasses.contains(AndroidProject.class)) {
            SOURCE_FOR_BINARY_QUERY.addClassPathProvider(androidClassPathProvider);
            JAVADOC_FOR_BINARY_QUERY.addClassPathProvider(androidClassPathProvider);
        }

    }

}
