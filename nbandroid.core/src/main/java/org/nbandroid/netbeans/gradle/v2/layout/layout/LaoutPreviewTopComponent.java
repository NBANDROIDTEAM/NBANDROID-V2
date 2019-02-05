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
package org.nbandroid.netbeans.gradle.v2.layout.layout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.nbandroid.netbeans.gradle.api.AndroidProjects;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.nbandroid.netbeans.gradle.v2.tools.DelayedDocumentChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.editor.BaseDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewProvider;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.nbandroid.netbeans.gradle.v2.layout.layout//LaoutPreview//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "LaoutPreviewTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "commonpalette", openAtStartup = false)
@ActionID(category = "Window", id = "org.nbandroid.netbeans.gradle.v2.layout.layout.LaoutPreviewTopComponent")
@ActionReference(path = "Menu/Window/Android" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LaoutPreviewAction",
        preferredID = "LaoutPreviewTopComponent"
)
@Messages({
    "CTL_LaoutPreviewAction=Android Laout Preview",
    "CTL_LaoutPreviewTopComponent=Laout Preview",
    "HINT_LaoutPreviewTopComponent=This is a LaoutPreview window"
})
public final class LaoutPreviewTopComponent extends TopComponent implements ChangeListener, FileChangeListener, LookupListener {

    static void showLaoutPreview(Lookup lkp, MultiViewEditorElement editorElement) {
        WindowManager wm = WindowManager.getDefault();
        TopComponent topComponent = wm.findTopComponent("LaoutPreviewTopComponent"); // NOI18N
        if (topComponent instanceof LaoutPreviewTopComponent) {
            if (!topComponent.isOpened()) {
                topComponent.open();
            }
            ((LaoutPreviewTopComponent) topComponent).setupPanel(lkp, editorElement);
        }

    }

    static void hideLaoutPreview(MultiViewEditorElement editorElement) {
        WindowManager wm = WindowManager.getDefault();
        TopComponent topComponent = wm.findTopComponent("LaoutPreviewTopComponent"); // NOI18N
        if (null != topComponent) {
            if (topComponent instanceof LaoutPreviewTopComponent) {
                if (((LaoutPreviewTopComponent) topComponent).editorElement == null || ((LaoutPreviewTopComponent) topComponent).editorElement.equals(editorElement)) {
                    topComponent.close();
                }
            }
        }
    }
    private LayoutDataObject dataObject;
    private MultiViewEditorElement editorElement;
    private LayoutPreviewPanel panel;
    private Document document;
    private FileObject resFo;
    private DocumentListener documentListener;
    private Lookup lookup;
    private Lookup.Result<LayoutDataObject> lookupResult;

    public LaoutPreviewTopComponent() {
        initComponents();
        setName(Bundle.CTL_LaoutPreviewTopComponent());
        setToolTipText(Bundle.HINT_LaoutPreviewTopComponent());
    }

    protected void setupPanel(Lookup lkp, MultiViewEditorElement editorElement) {
        stopPanel();
        this.lookup = lkp;
        lookupResult = lkp.lookupResult(LayoutDataObject.class);
        lookupResult.addLookupListener(this);
        this.editorElement = editorElement;
        resultChanged(null);

    }

    protected void stopPanel() {
        removeAll();
        if (resFo != null) {
            resFo.removeRecursiveListener(this);
        }
        if (documentListener != null && document != null) {
            document.removeDocumentListener(documentListener);
        }
        resFo = null;
        document = null;
        documentListener = null;
        if (lookupResult != null) {
            lookupResult.removeLookupListener(this);
        }
    }

    private void initLayoutEditor() {
        setName("Laout Preview [" + dataObject.getPrimaryFile().getNameExt() + "]");
        setToolTipText("Laout Preview [" + dataObject.getPrimaryFile().getNameExt() + "]");
        Project project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        //find res folder
        resFo = AndroidProjects.findResFolder(project);
        if (resFo != null) {
            //refresh preview on res file change
            resFo.addRecursiveListener(this);
        }
        final File resFolderFile = AndroidProjects.findResFolderAsFile(project);
        //find aars folders from classpath
        GradleAndroidClassPathProvider androidClassPathProvider = project.getLookup().lookup(GradleAndroidClassPathProvider.class);
        final List<File> aars = new ArrayList<>();
        ClassPath classPath = androidClassPathProvider.getCompilePath();
        FileObject[] roots = classPath.getRoots();
        final List<File> jars = new ArrayList<>();
        for (FileObject root : roots) {
            if ("classes.jar".equals(FileUtil.getArchiveFile(root).getNameExt())) {
                aars.add(FileUtil.toFile(FileUtil.getArchiveFile(root).getParent().getParent()));
            } else {
                jars.add(FileUtil.toFile(FileUtil.getArchiveFile(root)));
            }
        }
        //find project theme
        final String projectTheme = AndroidProjects.parseProjectTheme(project);
        //find platform
        final File platformFolder = InstalledFileLocator.getDefault().locate("modules/ext/layoutlib_v28_res", "sk-arsi-netbeans-gradle-android-Gradle-Android-support-layout-li", false);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    LayoutPreviewProvider previewProvider = Lookup.getDefault().lookup(LayoutPreviewProvider.class);
                    panel = previewProvider.getPreview(platformFolder,
                            FileUtil.toFile(dataObject.getPrimaryFile()),
                            resFolderFile, projectTheme, aars, jars);
                    final Runnable runnable1 = new Runnable() {
                        @Override
                        public void run() {
                            JEditorPane editorPane = editorElement.getEditorPane();
                            if (editorPane != null) {
                                if (panel != null) {
                                    add(panel);
                                }
                                revalidate();
                                document = editorElement.getEditorPane().getDocument();
                                documentListener = DelayedDocumentChangeListener.create(editorElement.getEditorPane().getDocument(), LaoutPreviewTopComponent.this, 2000);
                            }
                        }
                    };
                    WindowManager.getDefault().invokeWhenUIReady(runnable1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(runnable).start();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.CardLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e != null && e.getSource() instanceof BaseDocument) {
            try {
                if (panel != null) {
                    BaseDocument doc = (BaseDocument) e.getSource();
                    StringWriter writer = new StringWriter();
                    doc.write(writer, 0, doc.getLength());
                    String text = writer.toString();
                    InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
                    panel.refreshPreview(stream);
                }
            } catch (IOException ex) {
            } catch (BadLocationException ex) {
            }
        } else {
            panel.showTypingIndicator();
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        stateChanged(new ChangeEvent(document));
    }

    @Override
    public void fileChanged(FileEvent fe) {
        stateChanged(new ChangeEvent(document));
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        stateChanged(new ChangeEvent(document));
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        stateChanged(new ChangeEvent(document));
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends LayoutDataObject> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            dataObject = allInstances.iterator().next();
            if (dataObject != null) {
                initLayoutEditor();
            } else {
                setName(Bundle.CTL_LaoutPreviewTopComponent());
                setToolTipText(Bundle.HINT_LaoutPreviewTopComponent());
            }
        }
    }

}
