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
package org.netbeans.modules.android.api;

import java.awt.Image;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import org.nbandroid.netbeans.gradle.v2.gradle.FindAndroidVisitor;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = ProjectFactory.class, position = 0)
public class NbAndroidProjectFactory implements ProjectFactory2 {

    @StaticResource
    private static final String PROJECT_ICON = "org/netbeans/modules/android/project/ui/resources/androidProject.png";    //NOI18N
    public static final Image IMG_PROJECT_ICON = ImageUtilities.loadImage(PROJECT_ICON);

    public static final String BUILD_GRADLE = "build.gradle";

    @Override
    public ProjectManager.Result isProject2(FileObject fo) {
        if (isProject(fo)) {
            return new ProjectManager.Result(new ImageIcon(IMG_PROJECT_ICON));
        }
        return null;
    }

    @Override
    public boolean isProject(FileObject fo) {
        if (fo.isFolder()) {
            FileObject buildScript = fo.getFileObject(BUILD_GRADLE);
            if (buildScript != null) {
                try {
                    return FindAndroidVisitor.visit(FileUtil.toFile(buildScript));
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    private boolean isRootProject(FileObject fo) {
        if (fo.isFolder()) {
            FileObject buildScript = fo.getFileObject(BUILD_GRADLE);
            if (buildScript != null) {
                Enumeration<? extends FileObject> children = fo.getChildren(false);
                while (children.hasMoreElements()) {
                    FileObject nextFo = children.nextElement();
                    if (nextFo.isFolder() && isProject(nextFo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Project loadProject(FileObject fo, ProjectState ps) throws IOException {
        if (isProject(fo)) {
            return new NbAndroidProjectImpl(fo, ps);
        } else if (isRootProject(fo)) {
            return new NbAndroidRootProjectImpl(fo, ps);
        }
        return null;
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {
    }

}
