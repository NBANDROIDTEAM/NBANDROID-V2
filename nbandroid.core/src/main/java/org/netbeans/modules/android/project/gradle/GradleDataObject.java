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
package org.netbeans.modules.android.project.gradle;

import java.awt.Image;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUIUtils;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import static org.openide.text.DataEditorSupport.annotateName;
import org.openide.util.ImageUtilities;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author arsi
 */
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),})
public class GradleDataObject extends MultiDataObject {

    public static final String MIME_TYPE = "text/x-gradle+x-groovy"; //NOI18N
    private static final String BUILD_GRADLE = "build.gradle";       //NOI18N

    public GradleDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        getCookieSet().add(new GradleDataEditor());
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    protected Node createNodeDelegate() {
        return new IconNode(super.createNodeDelegate()); //To change body of generated methods, choose Tools | Templates.
    }

    @StaticResource
    private static final String GRADLE_ICON_RES = "org/netbeans/modules/android/project/gradle/gradle.png";
    public static final ImageIcon GRADLE_ICON = new ImageIcon(ImageUtilities.loadImage(GRADLE_ICON_RES));

    private class IconNode extends FilterNode {

        public IconNode(Node original) {
            super(original, org.openide.nodes.Children.LEAF);
        }

        @Override
        public Image getIcon(int type) {
            Image image = GRADLE_ICON.getImage();
            try {
                image = FileUIUtils.getImageDecorator(getPrimaryFile().getFileSystem()).annotateIcon(image, type, new HashSet<>());
            } catch (FileStateInvalidException ex) {
                ex.printStackTrace();
            }
            return image;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private class GradleDataEditor extends DataEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie, CloseCookie {

        private final SaveCookie save = new SaveCookie() {
            public @Override
            void save() throws IOException {
                saveDocument();
            }

            @Override
            public String toString() {
                return getPrimaryFile().getNameExt();
            }
        };

        GradleDataEditor() {
            super(GradleDataObject.this, null, new GradleEnv(GradleDataObject.this));
        }

        @Override
        protected CloneableEditorSupport.Pane createPane() {
            return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(MIME_TYPE, getDataObject());
        }

        @Override
        protected boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            if (getLookup().lookup(SaveCookie.class) == null) {
                getCookieSet().add(save);
                setModified(true);
            }
            return true;
        }

        @Override
        protected void notifyUnmodified() {
            super.notifyUnmodified();
            if (getLookup().lookup(SaveCookie.class) == save) {
                getCookieSet().remove(save);
                setModified(false);
            }
        }

        @Override
        protected String messageName() {
            String title = getFileOrProjectName(getPrimaryFile());
            try {
                StatusDecorator decorator = getPrimaryFile().getFileSystem().getDecorator();
                title = decorator.annotateName(title, Collections.singleton(getPrimaryFile()));
            } catch (FileStateInvalidException ex) {
                // Just fall through
            }
            return annotateName(title, false, isModified(), !getPrimaryFile().canWrite());
        }

        @Override
        protected String messageHtmlName() {
            String title = getFileOrProjectName(getPrimaryFile());
            try {
                StatusDecorator decorator = getPrimaryFile().getFileSystem().getDecorator();
                String annotateNameHtml = decorator.annotateNameHtml(title, Collections.singleton(getPrimaryFile()));
                if (annotateNameHtml != null && !title.equals(annotateNameHtml)) {
                    title = annotateNameHtml;
                }
            } catch (FileStateInvalidException ex) {
                // Just fall through
            }
            return annotateName(title, true, isModified(), !getPrimaryFile().canWrite());
        }

        @Override
        protected boolean asynchronousOpen() {
            return true;
        }

        // XXX override initializeCloneableEditor if needed; see AntProjectDataEditor
    }

    static String getFileOrProjectName(FileObject primaryFile) {
        String ret = primaryFile.getNameExt();

        if (BUILD_GRADLE.equals(ret)) {
            try {
                Project prj = ProjectManager.getDefault().findProject(primaryFile.getParent());
                if (prj != null) {
                    ret = ProjectUtils.getInformation(prj).getName();
                }
            } catch (IOException | IllegalArgumentException ex) {
                Logger.getLogger(GradleDataObject.class.getName()).log(Level.INFO, "Could not determine project and its name", ex);
            }
        }
        return ret;
    }

    private static class GradleEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        GradleEnv(MultiDataObject d) {
            super(d);
        }

        protected @Override
        FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected @Override
        FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override
        CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(GradleDataEditor.class);
        }

    }
}
