/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.modules.android.project.api.ui;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.AndroidFileTypes;
import org.netbeans.modules.android.project.ui.AndroidLogicalViewProvider;
import org.netbeans.modules.android.project.ui.PathFinder;
import org.netbeans.modules.android.project.ui.PreviewLayoutAction;
import org.netbeans.modules.android.project.ui.customizer.CustomizerProviderImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author radim
 */
public class AndroidResourceNode extends FilterNode implements PathFinder {

    private String nodeName;
    private Project project;
    Action[] actions;

    public AndroidResourceNode(Node delegate, Project project, String nodeName) {
        super(delegate, new ResourceChildren(delegate, project));
        this.nodeName = nodeName;
        this.project = project;
    }

    @Override
    public String getDisplayName() {
        return nodeName;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (!context) {
            if (actions == null) {
                Action superActions[] = super.getActions(context);
                FileObject fo = getLookup().lookup(FileObject.class);
                if ("AndroidManifest.xml".equals(fo.getNameExt())) {
                    return superActions;
                }
                boolean isLayout = Lookup.getDefault().lookup(AndroidFileTypes.class).isLayoutFile(fo);
                int previewLen = isLayout ? 1 : 0;
                actions = new Action[superActions.length + previewLen + 2];
                if (isLayout) {
                    actions[0] = new PreviewLayoutAction(fo);
                }
                System.arraycopy(superActions, 0, actions, previewLen, superActions.length);
                actions[superActions.length] = null;
                actions[superActions.length + previewLen + 1] = new PreselectPropertiesAction(project, nodeName);
            }
            return actions;
        } else {
            return super.getActions(context);
        }
    }

    // a copy from PhysicalView.PathFinder
    @Override
    public Node findPath(Node root, Object object) {

        if (!(object instanceof FileObject)) {
            return null;
        }

        FileObject fo = (FileObject) object;
        FileObject resRoot = project.getProjectDirectory().getFileObject("res");
        if (FileUtil.isParentOf(resRoot, fo) /* && group.contains( fo ) */) {
            // The group contains the object

            String relPath = FileUtil.getRelativePath(resRoot, fo);

            ArrayList<String> path = new ArrayList<String>();
            StringTokenizer strtok = new StringTokenizer(relPath, "/");
            while (strtok.hasMoreTokens()) {
                path.add(strtok.nextToken());
            }

            if (path.size() > 0) {
                path.remove(path.size() - 1);
            } else {
                return null;
            }
            try {
                //#75205
                Node parent = NodeOp.findPath(root, Collections.enumeration(path));
                if (parent != null) {
                    //not nice but there isn't a findNodes(name) method.
                    Node[] nds = parent.getChildren().getNodes(true);
                    for (int i = 0; i < nds.length; i++) {
                        DataObject dobj = nds[i].getLookup().lookup(DataObject.class);
                        if (dobj != null && fo.equals(dobj.getPrimaryFile())) {
                            return nds[i];
                        }
                    }
                    String name = fo.getName();
                    try {
                        DataObject dobj = DataObject.find(fo);
                        name = dobj.getNodeDelegate().getName();
                    } catch (DataObjectNotFoundException ex) {
                    }
                    return parent.getChildren().findChild(name);
                }
            } catch (NodeNotFoundException e) {
                return null;
            }
        } else if (resRoot.equals(fo)) {
            return root;
        }

        return null;
    }

    private static class ResourceChildren extends FilterNode.Children {

        private final Project project;

        public ResourceChildren(Node or, Project project) {
            super(or);
            this.project = project;
        }

        @Override
        protected Node[] createNodes(Node key) {
            return Iterators.toArray(
                    Iterators.transform(
                            Iterators.forArray(super.createNodes(key)),
                            new Function<Node, Node>() {
                        @Override
                        public Node apply(Node input) {
                            return new NotRootResourceFilterNode(input, project);
                        }
                    }),
                    Node.class);
        }
    }

    /**
     * Yet another cool filter node to add preview layout to layout XML files
     * (hooking into XML data object is a pain)
     */
    private static class NotRootResourceFilterNode extends FilterNode {

        private final Project project;
        Action[] actions;

        public NotRootResourceFilterNode(Node delegate, Project project) {
            super(delegate, new ResourceChildren(delegate, project));
            this.project = project;
        }

        @Override
        public Action[] getActions(boolean context) {
            System.err.println("getActions " + context);
            if (!context) {
                if (actions == null) {
                    actions = checkLayoutPreviewAction(super.getActions(context));
                }
                return actions;
            } else {
                return super.getActions(context);
            }
        }

        private Action[] checkLayoutPreviewAction(Action[] origActions) {
            FileObject fo = getOriginal().getLookup().lookup(FileObject.class);
            boolean isLayout = Lookup.getDefault().lookup(AndroidFileTypes.class).isLayoutFile(fo);
            if (!isLayout) {
                return origActions;
            }
            actions = new Action[origActions.length + 1];
            System.arraycopy(origActions, 1, actions, 2, origActions.length - 1);
            actions[0] = origActions[0]; // open
            actions[1] = new PreviewLayoutAction(fo);
            return actions;
        }
    }

    /**
     * The special properties action
     */
    static class PreselectPropertiesAction extends AbstractAction {

        private final Project project;
        private final String nodeName;
        private final String panelName;

        public PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
        }

        public PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super(NbBundle.getMessage(AndroidLogicalViewProvider.class, "LBL_Properties_Action"));
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // J2SECustomizerProvider cp = (J2SECustomizerProvider) project.getLookup().lookup(J2SECustomizerProvider.class);
            CustomizerProviderImpl cp = project.getLookup().lookup(CustomizerProviderImpl.class);
            if (cp != null) {
                cp.showCustomizer(nodeName, panelName);
            }

        }
    }
}
