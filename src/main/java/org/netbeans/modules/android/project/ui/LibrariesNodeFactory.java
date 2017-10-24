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

package org.netbeans.modules.android.project.ui;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public final class LibrariesNodeFactory implements NodeFactory {

    public @Override NodeList<?> createNodes(Project p) {
        AndroidProject project = p.getLookup().lookup(AndroidProject.class);
        assert project != null;
        return new LibrariesNodeList(project);
    }

    private static final String LIB_NODE_KEY = "libNode";

    private static class LibrariesNodeList implements NodeList<String> {
        private final AndroidProject project;

        LibrariesNodeList(AndroidProject proj) {
            project = proj;
        }

        public @Override List<String> keys() {
            return Collections.singletonList(LIB_NODE_KEY);
        }

        public @Override void addChangeListener(ChangeListener l) {}

        public @Override void removeChangeListener(ChangeListener l) {}

        public @Override Node node(String key) {
          if (LIB_NODE_KEY.equals(key)) {
            return new LibrariesNode(NbBundle.getMessage(LibrariesNodeList.class, "LBL_LibrariesNodeDisplayName"), 
                project, new Action[0]);
          }
            assert false: "No node for key: " + key;
            return null;
        }

        public @Override void addNotify() {}

        public @Override void removeNotify() {}

    }

}
