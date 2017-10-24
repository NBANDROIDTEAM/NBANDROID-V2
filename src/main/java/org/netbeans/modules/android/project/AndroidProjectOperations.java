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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.openide.filesystems.FileObject;

class AndroidProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {

    private final AndroidProject project;

    public AndroidProjectOperations(AndroidProject project) {
        this.project = project;
    }

    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);

        if (file != null) {
            result.add(file);
        }
    }

    public @Override List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "local.properties", files); //NOI18N
        addFile(projectDirectory, "project.properties", files); //NOI18N
        addFile(projectDirectory, "ant.properties", files); //NOI18N
        return files;
    }

    public @Override List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(project.getProjectDirectory(), "AndroidManifest.xml", files); // NOI18N
        addFile(project.getProjectDirectory(), "src", files); // NOI18N
        addFile(project.getProjectDirectory(), "res", files); // NOI18N
        return files;
    }

    public @Override void notifyDeleting() throws IOException {
        // OK
    }

    public @Override void notifyDeleted() throws IOException {
        // OK
    }

    public @Override void notifyCopying() {
        // OK
    }

    public @Override void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        // OK
    }

    public @Override void notifyMoving() throws IOException {
        notifyDeleting();
        // OK
    }

    public @Override void notifyMoved(Project original, File originalPath, String nueName) {        
        if (original == null) {
            return ;
        }                
        // OK
    }

}
