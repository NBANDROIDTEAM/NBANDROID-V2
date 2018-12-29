package org.nbandroid.netbeans.gradle;

import com.android.builder.model.AndroidArtifactOutput;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.nbandroid.netbeans.gradle.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.api.AndroidManifestSource;
import org.nbandroid.netbeans.gradle.api.ui.AndroidPackagesNode;
import org.nbandroid.netbeans.gradle.api.ui.AndroidResourceNode;
import org.nbandroid.netbeans.gradle.ui.DependenciesNode;
import org.nbandroid.netbeans.gradle.ui.GeneratedSourcesNode;
import org.nbandroid.netbeans.gradle.ui.InstrumentTestNode;
import org.nbandroid.netbeans.gradle.v2.apk.actions.DebugApkAction;
import org.nbandroid.netbeans.gradle.v2.apk.actions.ReleaseUnsignedApkAction;
import org.nbandroid.netbeans.gradle.v2.apk.actions.SignApkAction;
import org.nbandroid.netbeans.gradle.v2.ui.IconProvider;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.gradle.project.api.event.NbListenerRef;
import org.netbeans.gradle.project.api.nodes.GradleProjectExtensionNodes;
import org.netbeans.gradle.project.api.nodes.SingleNodeFactory;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author radim
 */
public class AndroidGradleNodes implements GradleProjectExtensionNodes {

    private final Project p;

    public AndroidGradleNodes(Project p) {
        this.p = p;
    }

    @Override
    public NbListenerRef addNodeChangeListener(Runnable listener) {
        // TODO copied from gradle java support
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // FIXME: We currently rely on the undocumented fact, that nodes are
        // always reloaded after a model reload.
        return new NbListenerRef() {
            @Override
            public boolean isRegistered() {
                return false;
            }

            @Override
            public void unregister() {
            }
        };
    }

    @Override
    public List<SingleNodeFactory> getNodeFactories() {
        Sources sources = ProjectUtils.getSources(p);
        return Lists.newArrayList(Iterables.concat(
                Iterables.transform(
                        Iterables.concat(
                                Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)),
                                Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES))),
                        new Function<SourceGroup, SingleNodeFactory>() {

                    @Override
                    public SingleNodeFactory apply(SourceGroup f) {
                        return new GradleNodeFactory(f);
                    }
                }),
                Iterables.transform(
                        Arrays.asList(sources.getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES)),
                        new Function<SourceGroup, SingleNodeFactory>() {

                    @Override
                    public SingleNodeFactory apply(SourceGroup f) {
                        return new GradleResNodeFactory(f);
                    }
                }),
                Lists.newArrayList(
                        new ManifestNodeFactory(),
                        new DependenciesNodeFactory(),
                        new InstrumentTestNodeFactory(),
                        new GeneratedSourcesNodeFactory(),
                        new ApkNodeFactory())));
    }

    private class GradleNodeFactory implements SingleNodeFactory {

        private final SourceGroup sg;

        public GradleNodeFactory(SourceGroup sg) {
            this.sg = sg;
        }

        @Override
        public Node createNode() {
            return new AndroidPackagesNode(sg, p);
        }
    }

    private class GradleResNodeFactory implements SingleNodeFactory {

        private final SourceGroup sg;

        public GradleResNodeFactory(SourceGroup sg) {
            this.sg = sg;
        }

        @Override
        public Node createNode() {
            try {
                DataObject dobj = DataObject.find(sg.getRootFolder());
                return new AndroidResourceNode(dobj.getNodeDelegate(), p, sg.getDisplayName());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }

    private class InstrumentTestNodeFactory implements SingleNodeFactory {

        public InstrumentTestNodeFactory() {
        }

        @Override
        public Node createNode() {
            return new InstrumentTestNode(NbBundle.getMessage(AndroidGradleNodes.class, "LBL_InstrumentTestNode"), p);
        }
    }

    private class DependenciesNodeFactory implements SingleNodeFactory {

        public DependenciesNodeFactory() {
        }

        @Override
        public Node createNode() {
            return DependenciesNode.createCompileDependenciesNode(
                    NbBundle.getMessage(AndroidGradleNodes.class, "LBL_DependenciesNode"), p);
        }
    }

    private class ApkNodeFactory implements SingleNodeFactory {

        @Override
        public Node createNode() {
            return new ApksFilterNode();
        }

    }

    private static final RequestProcessor RP = new RequestProcessor("Refresh APK Nodes", 1);

    private class ApksFilterNodeChildrensV2 extends Children.Keys<DataObject> implements FileChangeListener {

        public ApksFilterNodeChildrensV2() {
            super(true);
            List<FileObject> folders = findApks();

            for (FileObject folder : folders) {
                folder.addRecursiveListener(WeakListeners.create(FileChangeListener.class, this, folder));
            }
        }

        private List<FileObject> findApks() {
            List<DataObject> keys = new ArrayList<>();
            List<FileObject> folders = new ArrayList<>();
            AndroidProject androidProject = p.getLookup().lookup(AndroidProject.class);
            Iterator<Variant> variants = androidProject.getVariants().iterator();
            while (variants.hasNext()) {
                Variant next = variants.next();
                Iterator<AndroidArtifactOutput> outputs = next.getMainArtifact().getOutputs().iterator();
                while (outputs.hasNext()) {
                    AndroidArtifactOutput output = outputs.next();
                    File outputFile = output.getOutputFile();
                    try {
                        if (outputFile.exists() && outputFile.isFile()) {
                            keys.add(DataObject.find(FileUtil.toFileObject(outputFile)));
                        }
                        File folder = outputFile.getParentFile();
                        folder.mkdirs();
                        folders.add(FileUtil.toFileObject(folder));
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            }
            try {
                FileObject release = FileUtil.createFolder(p.getProjectDirectory(), "release");
                folders.add(release);
                FileObject[] childrens = release.getChildren();
                for (FileObject children : childrens) {
                    if ("apk".equals(children.getExt())) {
                        keys.add(DataObject.find(children));
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                FileObject debug = FileUtil.createFolder(p.getProjectDirectory(), "debug");
                folders.add(debug);
                FileObject[] childrens = debug.getChildren();
                for (FileObject children : childrens) {
                    if ("apk".equals(children.getExt())) {
                        keys.add(DataObject.find(children));
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            Collections.sort(keys, new Comparator<DataObject>() {
                @Override
                public int compare(DataObject o1, DataObject o2) {
                    return o1.getPrimaryFile().getName().compareTo(o2.getPrimaryFile().getName());
                }
            });
            setKeys(keys);
            return folders;
        }

        private void refreshFolders(FileEvent fe) {

            Runnable runnable = new Runnable() {
                public void run() {
                    findApks();
                }
            };
            RP.execute(runnable);
        }

        @Override
        protected Node[] createNodes(DataObject key) {
            return new Node[]{new FilterNode(key.getNodeDelegate())};
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            refreshFolders(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            refreshFolders(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            refreshFolders(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            refreshFolders(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            refreshFolders(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

    }

    private class ApksFilterNode extends AbstractNode {

        public ApksFilterNode() {
            super(new ApksFilterNodeChildrensV2(), Lookups.fixed(p));
        }

        @Override
        public String getDisplayName() {
            return "APKs";
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                SystemAction.get(DebugApkAction.class),
                SystemAction.get(SignApkAction.class),
                SystemAction.get(ReleaseUnsignedApkAction.class),};
        }

        @Override
        public Image getOpenedIcon(int type) {
            return IconProvider.IMG_APKS;
        }

        @Override
        public Image getIcon(int type) {
            return IconProvider.IMG_APKS;
        }

    }

    private class GeneratedSourcesNodeFactory implements SingleNodeFactory {

        public GeneratedSourcesNodeFactory() {
        }

        @Override
        public Node createNode() {
            return new GeneratedSourcesNode("Generated Sources", p, Lists.newArrayList(
                    AndroidConstants.SOURCES_TYPE_GENERATED_JAVA, AndroidConstants.SOURCES_TYPE_GENERATED_RESOURCES));
        }
    }

    private class ManifestNodeFactory implements SingleNodeFactory {

        @Override
        public Node createNode() {
            // TODO there can be more manifests
            AndroidManifestSource ams = p.getLookup().lookup(AndroidManifestSource.class);
            FileObject manifestFO = ams != null ? ams.get() : null;
            if (manifestFO != null) {
                try {
                    DataObject dobj = DataObject.find(manifestFO);
                    return new AndroidResourceNode(dobj.getNodeDelegate(), p, AndroidConstants.ANDROID_MANIFEST_XML);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;

        }
    }
}
