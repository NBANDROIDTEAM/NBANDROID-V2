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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.io.File;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class BinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation {        

    private final AndroidProject project;
    public BinaryForSourceQueryImpl(AndroidProject project) {
        this.project = project;
    }

    public @Override Result findBinaryRoots(final URL sourceRoot) {
      if (Iterables.any(project.info().getSourceAndGenDirs(), new Predicate<FileObject>() {
            @Override
            public boolean apply(final FileObject sourceDir) {
              return sourceRoot.equals(FileUtil.urlForArchiveOrDir(FileUtil.toFile(sourceDir)));
            }
          })) {
        return new Result() {
            public @Override URL[] getRoots() {
                return new URL[] {FileUtil.urlForArchiveOrDir(new File(project.getProjectDirectoryFile(), "bin/classes"))};
            }
            public @Override void addChangeListener(ChangeListener l) {}
            public @Override void removeChangeListener(ChangeListener l) {}
        };
      } else {
        return null;
      }
    }

}
