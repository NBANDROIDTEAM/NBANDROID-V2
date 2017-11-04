package org.nbandroid.netbeans.gradle.ui;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbandroid.netbeans.gradle.api.AndroidClassPath;
import org.nbandroid.netbeans.gradle.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.api.ui.AndroidNodes;
import org.nbandroid.netbeans.gradle.api.ui.UiUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * LibrariesNode displays the content of classpath and Android platform (if
 * available).
 */
public final class DependenciesNode extends AbstractNode {

    private static final Logger LOG = Logger.getLogger(DependenciesNode.class.getName());

    private static final Image ICON_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/android/project/ui/resources/libraries-badge.png");    //NOI18N
    static final RequestProcessor rp = new RequestProcessor();

    private final String displayName;
    private final Action[] librariesNodeActions;

    /**
     * Creates new LibrariesNode named displayName displaying classPathProperty
     * classpath and optionally Java platform.
     *
     * @param displayName the display name of the node
     * @param project the project owning this node, the project is placed into
     * LibrariesNode's lookup
     * @param eval {@link PropertyEvaluator} used for listening
     * @param librariesNodeActions actions which should be available on the
     * created node.
     */
    public static Node createCompileDependenciesNode(
            String displayName, final Project project, Action... librariesNodeActions) {
        DependenciesChildren children = new DependenciesChildren(project) {
            protected void addLibraries(List<Key> result) {
                final ClassPathProvider cpProvider = project.getLookup().lookup(ClassPathProvider.class);
                if (cpProvider != null) {
                    Sources srcs = ProjectUtils.getSources(project);
                    SourceGroup[] sourceGroups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    Set<FileObject> roots = Sets.newHashSet();
                    for (SourceGroup sg : sourceGroups) {
                        roots.add(sg.getRootFolder());
                    }
                    Iterable<ClassPath> compileCPs = Iterables.transform(roots, new Function<FileObject, ClassPath>() {

                        @Override
                        public ClassPath apply(FileObject f) {
                            return cpProvider.findClassPath(f, ClassPath.COMPILE);
                        }
                    });
                    ClassPath compileCP
                            = ClassPathSupport.createProxyClassPath(Lists.newArrayList(compileCPs).toArray(new ClassPath[0]));
                    for (FileObject cpRoot : compileCP.getRoots()) {
                        final FileObject archiveFile = FileUtil.getArchiveFile(cpRoot);
                        result.add(new Key(AndroidNodes.createLibrarySourceGroup(archiveFile.getNameExt(), cpRoot)));
                    }
                }
            }
        };
        return new DependenciesNode(children, displayName, project, librariesNodeActions);
    }

    public static Node createTestDependenciesNode(
            String displayName, final Project project, Action... librariesNodeActions) {
        DependenciesChildren children = new DependenciesChildren(project) {
            protected void addLibraries(List<Key> result) {
                final ClassPathProvider cpProvider = project.getLookup().lookup(ClassPathProvider.class);
                if (cpProvider != null) {
                    Sources srcs = ProjectUtils.getSources(project);
                    SourceGroup[] sourceGroups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    Set<FileObject> roots = Sets.newHashSet();
                    for (SourceGroup sg : sourceGroups) {
                        roots.add(sg.getRootFolder());
                    }
                    SourceGroup[] testSourceGroups = srcs.getSourceGroups(AndroidConstants.SOURCES_TYPE_INSTRUMENT_TEST_JAVA);
                    Set<FileObject> testRoots = Sets.newHashSet();
                    for (SourceGroup sg : testSourceGroups) {
                        testRoots.add(sg.getRootFolder());
                    }
                    Iterable<ClassPath> compileCPs = Iterables.transform(roots, new Function<FileObject, ClassPath>() {

                        @Override
                        public ClassPath apply(FileObject f) {
                            return cpProvider.findClassPath(f, ClassPath.COMPILE);
                        }
                    });
                    Iterable<ClassPath> testCompileCPs = Iterables.transform(testRoots, new Function<FileObject, ClassPath>() {

                        @Override
                        public ClassPath apply(FileObject f) {
                            return cpProvider.findClassPath(f, ClassPath.COMPILE);
                        }
                    });
                    ClassPath compileCP
                            = ClassPathSupport.createProxyClassPath(Lists.newArrayList(compileCPs).toArray(new ClassPath[0]));
                    ClassPath testCompileCP
                            = ClassPathSupport.createProxyClassPath(Lists.newArrayList(testCompileCPs).toArray(new ClassPath[0]));
                    Set<FileObject> testCPRoots = Sets.newHashSet(testCompileCP.getRoots());
                    for (FileObject cpRoot : compileCP.getRoots()) {
                        testCPRoots.remove(cpRoot);
                    }
                    for (FileObject cpRoot : testCPRoots) {
                        if (cpRoot.isFolder()) {
                            final FileObject archiveFile = FileUtil.getArchiveFile(cpRoot);
                            if (archiveFile != null) {
                                result.add(new Key(AndroidNodes.createLibrarySourceGroup(archiveFile.getNameExt(), cpRoot)));
                            } else {
                                LOG.log(Level.FINE, "test classpath entry for {0} cannot be mapped to file object", cpRoot);
                            }
                        }
                    }
                }
            }
        };
        return new DependenciesNode(children, displayName, project, librariesNodeActions);
    }

    private DependenciesNode(
            DependenciesChildren children, String displayName, Project project, Action... librariesNodeActions) {
        super(children, Lookups.singleton(project));
        this.displayName = displayName;
        this.librariesNodeActions = librariesNodeActions;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getName() {
        return this.getDisplayName();
    }

    @Override
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    @Override
    public Action[] getActions(boolean context) {
        return this.librariesNodeActions;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    private Image computeIcon(boolean opened, int type) {
        Image image = UiUtils.getTreeFolderIcon(opened);
        image = ImageUtilities.mergeImages(image, ICON_BADGE, 7, 7);
        return image;
    }

    private abstract static class DependenciesChildren extends Children.Keys<Key> implements PropertyChangeListener {

        private final Project project;

        DependenciesChildren(Project project) {
            this.project = project;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setKeys(getKeys());
                }
            });
        }

        @Override
        protected void addNotify() {
            AndroidClassPath cpProvider = project.getLookup().lookup(AndroidClassPath.class);
            if (cpProvider != null) {
                cpProvider.getClassPath(ClassPath.COMPILE).addPropertyChangeListener(this);
            }
            this.setKeys(getKeys());
        }

        @Override
        protected void removeNotify() {
            AndroidClassPath cpProvider = project.getLookup().lookup(AndroidClassPath.class);
            if (cpProvider != null) {
                cpProvider.getClassPath(ClassPath.COMPILE).removePropertyChangeListener(this);
            }
            this.setKeys(Collections.<Key>emptySet());
        }

        @Override
        protected Node[] createNodes(Key key) {
            Node[] result = null;
            switch (key.getType()) {
                case Key.TYPE_PLATFORM:
                    result = new Node[]{AndroidNodes.createPlatformNode(project)};
                    break;
                case Key.TYPE_PROJECT:
                    result = new Node[]{AndroidNodes.createProjectNode(project)};
                    break;
                case Key.TYPE_LIBRARY:
                    // TODO wrap to show suitable actions
                    result = new Node[]{PackageView.createPackageView(key.getSourceGroup())};
                    break;
            }
            if (result == null) {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }

        private List<Key> getKeys() {
            List<Key> result = new ArrayList<Key>();
            // add referenced lib projects and JARs
            addLibraries(result);

            // add PlatformNode
            result.add(new Key());
            return result;
        }

        protected abstract void addLibraries(List<Key> result);
    }

    private static class Key {

        static final int TYPE_PLATFORM = 0;
        static final int TYPE_LIBRARY = 1;
        static final int TYPE_PROJECT = 2;

        private final int type;
        private SourceGroup sg;
        private Project project;
        private URI uri;

        Key() {
            this.type = TYPE_PLATFORM;
        }

        Key(SourceGroup sg) {
            this.type = TYPE_LIBRARY;
            this.sg = sg;
        }

        Key(Project p, URI uri) {
            this.type = TYPE_PROJECT;
            this.project = p;
            this.uri = uri;
        }

        public int getType() {
            return this.type;
        }

        public SourceGroup getSourceGroup() {
            return this.sg;
        }

        public Project getProject() {
            return this.project;
        }

        // TODO delete?
        public URI getArtifactLocation() {
            return this.uri;
        }

        @Override
        public int hashCode() {
            int hashCode = this.type << 16;
            switch (this.type) {
                case TYPE_LIBRARY:
                    hashCode ^= this.sg == null ? 0 : this.sg.hashCode();
                    break;
                case TYPE_PROJECT:
                    hashCode ^= this.project == null ? 0 : this.project.hashCode();
                    break;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (other.type != type) {
                return false;
            }
            switch (type) {
                case TYPE_LIBRARY:
                    return (this.sg == null ? other.sg == null : this.sg.equals(other.sg));
                case TYPE_PROJECT:
                    return (this.project == null ? other.project == null : this.project.equals(other.project));
                case TYPE_PLATFORM:
                    return true;
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
