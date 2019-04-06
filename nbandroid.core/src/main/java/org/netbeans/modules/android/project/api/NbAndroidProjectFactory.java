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

import java.awt.Image;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @StaticResource()
    public static final String PROJECT_ICON = "org/netbeans/modules/android/api/android_project.png";
    public static final Image IMG_PROJECT_ICON = ImageUtilities.loadImage(PROJECT_ICON);
    @StaticResource()
    public static final String PROJECT_ROOT_ICON = "org/netbeans/modules/android/api/root_project.png";
    public static final Image IMG_PROJECT_ROOT_ICON = ImageUtilities.loadImage(PROJECT_ROOT_ICON);

    public static final String BUILD_GRADLE = "build.gradle";
    public static final String re1 = "(v)";	// Any Single Character 1
    public static final String re2 = "(c)";	// Any Single Character 2
    public static final String re3 = "(s)";	// Any Single Character 3
    public static final String re4 = "(-)";	// Any Single Character 4
    public static final String re5 = "(\\d+)";	// Integer Number 1
    private static final Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public ProjectManager.Result isProject2(FileObject fo) {
        if (isProject(fo)) {
            return new ProjectManager.Result(new ImageIcon(IMG_PROJECT_ICON));
        } else if (isRootProject(fo)) {
            return new ProjectManager.Result(new ImageIcon(IMG_PROJECT_ROOT_ICON));
        }
        return null;
    }

    @Override
    public boolean isProject(FileObject fo) {
        return isSubProject(fo);
    }

    public static boolean isSubProject(FileObject fo) {
        Matcher m = p.matcher(fo.getPath());
        if (m.find()) {
            return false;
        }
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

    public static boolean isRootProject(FileObject fo) {
        Matcher m = p.matcher(fo.getPath());
        if (m.find()) {
            return false;
        }
        if (fo.isFolder()) {
            FileObject buildScript = fo.getFileObject(BUILD_GRADLE);
            if (buildScript != null) {
                Enumeration<? extends FileObject> children = fo.getChildren(false);
                while (children.hasMoreElements()) {
                    FileObject nextFo = children.nextElement();
                    if (nextFo.isFolder() && isSubProject(nextFo)) {
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
