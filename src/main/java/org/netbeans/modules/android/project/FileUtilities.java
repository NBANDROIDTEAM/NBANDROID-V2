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

package org.netbeans.modules.android.project;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Some utilities for files/file objects.
 */
public class FileUtilities {

  public static void recursiveDelete(File file) {
    if (file == null) {
      return;
    }
    File[] files = file.listFiles();
    if (files != null) {
      for (File each : files) {
        recursiveDelete(each);
      }
    }
    file.delete();
  }

  public static void recursiveCopy(FileObject sourceDir, FileObject targetDir) throws IOException {
    for(FileObject child : sourceDir.getChildren()) {
      if (child.isFolder()) {
        recursiveCopy(child, targetDir.createFolder(child.getNameExt()));
      } else {
        FileUtil.copyFile(child, targetDir, child.getName());
      }
    }
  }

}
