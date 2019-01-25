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
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.nbandroid.netbeans.gradle.v2.tools.DelayedDocumentChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.editor.BaseDocument;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewProvider;

/**
 *
 * @author arsi
 */
@MultiViewElement.Registration(
        displayName = "&Preview",
        iconBase = "org/nbandroid/netbeans/gradle/v2/layout/layout/activity.png",
        mimeType = "text/x-android-layout+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "preview",
        position = 2
)
public class LayoutPreviewElement extends TopComponent implements MultiViewElement, ChangeListener {

    private transient MultiViewElementCallback callback;
    private LayoutDataObject dataObject;
    private MultiViewEditorElement editorElement;
    private LayoutPreviewPanel panel;

    /**
     * Creates new form LayoutPreviewElement
     */
    public LayoutPreviewElement() {
        initComponents();
    }

    public LayoutPreviewElement(Lookup lkp) {
        initComponents();
        dataObject = lkp.lookup(LayoutDataObject.class);
        editorElement = new MultiViewEditorElement(dataObject.getLookup());
        JComponent visualRepresentation = editorElement.getComponent();
        Project project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        GradleAndroidClassPathProvider androidClassPathProvider = project.getLookup().lookup(GradleAndroidClassPathProvider.class);
        ClassPath classPath = androidClassPathProvider.getCompilePath();
        //find aars folders from classpath
        FileObject[] roots = classPath.getRoots();
        final List<File> aars = new ArrayList<>();
        for (FileObject root : roots) {
            if ("classes.jar".equals(FileUtil.getArchiveFile(root).getNameExt())) {
                aars.add(FileUtil.toFile(FileUtil.getArchiveFile(root).getParent().getParent()));
            }
        }
        editorPanel.add(visualRepresentation);
        editorPanel.invalidate();
        editorPanel.repaint();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    LayoutPreviewProvider previewProvider = Lookup.getDefault().lookup(LayoutPreviewProvider.class);
                    panel = previewProvider.getPreview(new File("/jetty/android-studio-sdk/platforms/android-27"),
                            new File("/projekty/opensource/layoutlib-demo-2/res/testApp/MyApplication/src/main/res/layout/simple_activity.xml"),
                            new File("/projekty/opensource/layoutlib-demo-2/res/testApp/MyApplication/src/main/res"), "AppTheme", aars);
                    Runnable runnable1 = new Runnable() {
                        @Override
                        public void run() {
                            if (panel != null) {
                                split.setRightComponent(panel);
                            }
                            split.revalidate();
                            DelayedDocumentChangeListener.create(editorElement.getEditorPane().getDocument(), LayoutPreviewElement.this, 2000);
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        split = new javax.swing.JSplitPane();
        editorPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        toolbar.setRollover(true);

        split.setDividerLocation(300);
        split.setDividerSize(5);

        editorPanel.setLayout(new java.awt.CardLayout());
        split.setLeftComponent(editorPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 351, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 491, Short.MAX_VALUE)
        );

        split.setRightComponent(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return editorElement.getActions(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Lookup getLookup() {
        return new ProxyLookup(editorElement.getLookup());
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        callback.getTopComponent().setDisplayName(dataObject.getName());
        editorElement.setMultiViewCallback(callback);
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
//        Container parent = this.getParent().getParent().getParent();
//        try {
//            Field field = parent.getClass().getDeclaredField("alreadyAddedElements");
//            field.setAccessible(true);
//            List<MultiViewElement> tmp = new ArrayList<>((Set<MultiViewElement>) field.get(parent));
//            for (MultiViewElement el : tmp) {
//                if (el instanceof MultiViewEditorElement) {
//                    editorElement = (MultiViewEditorElement) el;
//                    editorPanel.add(editorElement.getVisualRepresentation());
//                    editorPanel.invalidate();
//                    editorPanel.repaint();
//                    editorElement.componentActivated();
//                }
//            }
//
//            System.out.println("org.nbandroid.netbeans.gradle.v2.layout.layout.LayoutPreviewElement.componentActivated()");
//        } catch (NoSuchFieldException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (SecurityException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IllegalArgumentException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IllegalAccessException ex) {
//            Exceptions.printStackTrace(ex);
//        }

        editorElement.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        editorElement.componentDeactivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden(); //To change body of generated methods, choose Tools | Templates.
        editorElement.componentHidden();
    }

    @Override
    public void componentShowing() {
        super.componentShowing(); //To change body of generated methods, choose Tools | Templates.
        editorElement.componentShowing();
    }

    @Override
    public void componentClosed() {
        super.componentClosed(); //To change body of generated methods, choose Tools | Templates.
        editorElement.componentClosed();
    }

    @Override
    public void componentOpened() {
        super.componentOpened(); //To change body of generated methods, choose Tools | Templates.
        editorElement.componentOpened();

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel editorPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane split;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    @Override
    public UndoRedo getUndoRedo() {
        return editorElement.getUndoRedo();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e!=null && e.getSource() instanceof BaseDocument) {
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
        }else{
            panel.showTypingIndicator();
        }
    }

}
