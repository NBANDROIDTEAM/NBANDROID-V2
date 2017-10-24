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

package org.netbeans.modules.android.project.queries;

import com.google.common.collect.Lists;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Finds sources corresponding to binaries in an android project.
 */
public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {
    private static final Logger LOG = Logger.getLogger(CompiledSourceForBinaryQuery.class.getName());

    private final AndroidProject project;
    private final URL binRoot;
    private final URL libRoot;
    private final AndroidSourceResult result;

    public CompiledSourceForBinaryQuery(AndroidProject project) {
        this.project = project;
        binRoot = FileUtil.urlForArchiveOrDir(new File(project.getProjectDirectoryFile(), "bin/classes"));
        libRoot = FileUtil.urlForArchiveOrDir(new File(project.getProjectDirectoryFile(), "bin/classes.jar"));
        result = new AndroidSourceResult();
        LOG.log(Level.FINER, "source for binary in prj {0}: {1}, {2}", 
            new Object[] {project.getProjectDirectory(), binRoot, libRoot});
    }

    public @Override Result findSourceRoots(final URL binaryRoot) {
        LOG.log(Level.FINE, "source roots for binary root {0}", binaryRoot);
        if (binaryRoot.equals(binRoot) || binaryRoot.equals(libRoot)) {
          return result;
        } 
        LOG.log(Level.FINER, "no match against {0} or {1}", new Object[]{binRoot, libRoot});
        return null;
    }

    private class AndroidSourceResult implements Result {
      public @Override
      FileObject[] getRoots() {
        List<FileObject> roots = Lists.newArrayList(project.info().getSourceAndGenDirs());
        LOG.log(Level.FINE, "return sources for binary in root {0}: {1}", new Object[]{project, roots});
        return roots.toArray(new FileObject[roots.size()]);
      }

      public @Override
      void addChangeListener(ChangeListener l) {
      }

      public @Override
      void removeChangeListener(ChangeListener l) {
      }
      
    }
}
