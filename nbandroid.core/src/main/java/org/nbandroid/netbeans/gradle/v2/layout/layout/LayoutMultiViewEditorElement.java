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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import javax.xml.parsers.ParserConfigurationException;
import org.nbandroid.netbeans.gradle.api.AndroidProjects;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.nbandroid.netbeans.gradle.v2.manifest.AndroidManifestParser;
import org.nbandroid.netbeans.gradle.v2.manifest.ManifestData;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.AssetNameConverter;
import org.nbandroid.netbeans.gradle.v2.tools.DelayedDocumentChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.WindowManager;
import org.xml.sax.SAXException;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewProvider;

/**
 *
 * @author arsi
 */
public class LayoutMultiViewEditorElement extends MultiViewEditorElement implements ChangeListener, LookupListener {

    private final Lookup lookup;
    private LayoutDataObject dataObject;
    private LayoutPreviewPanel panel;
    private Document document;
    private DocumentListener documentListener;
    private Lookup.Result<LayoutDataObject> lookupResult;

    public LayoutMultiViewEditorElement(Lookup lookup) {
        super(lookup);
        this.lookup = lookup;
        lookupResult = lookup.lookupResult(LayoutDataObject.class);
        lookupResult.addLookupListener(this);
        resultChanged(null);
    }

    protected void stopPanel() {
        if (documentListener != null && document != null) {
            document.removeDocumentListener(documentListener);
        }
        document = null;
        documentListener = null;
        if (lookupResult != null) {
            lookupResult.removeLookupListener(this);
        }
    }

    private void initLayoutEditor() {
        Project project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        //find aars folders from classpath
        GradleAndroidClassPathProvider androidClassPathProvider = project.getLookup().lookup(GradleAndroidClassPathProvider.class);;
        if (androidClassPathProvider == null) {
            //restored from serialized TopComponent wait for NBAndroid to come up
            int safetyCounter = 1200;
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                safetyCounter--;
                androidClassPathProvider = project.getLookup().lookup(GradleAndroidClassPathProvider.class);
            } while (androidClassPathProvider == null && safetyCounter > 0);
        }
        final List<File> aars = new ArrayList<>();
        ClassPath classPath = androidClassPathProvider.getCompilePath();
        FileObject[] roots = classPath.getRoots();
        if (roots.length == 0) {
            //restored from serialized TopComponent wait for NBAndroid to come up
            int safetyCounter = 1200;
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                classPath = androidClassPathProvider.getCompilePath();
                safetyCounter--;
                roots = classPath.getRoots();
            } while (roots.length == 0 && safetyCounter > 0);
        }
        final List<File> jars = new ArrayList<>();
        for (FileObject root : roots) {
            if ("classes.jar".equals(FileUtil.getArchiveFile(root).getNameExt())) {
                aars.add(FileUtil.toFile(FileUtil.getArchiveFile(root).getParent().getParent()));
            } else {
                jars.add(FileUtil.toFile(FileUtil.getArchiveFile(root)));
            }
        }
        //find res folder
        File resFolderFile = AndroidProjects.findResFolderAsFile(project);;
        if (resFolderFile == null) {
            //restored from serialized TopComponent wait for NBAndroid to come up
            int safetyCounter = 1200;
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                safetyCounter--;
                resFolderFile = AndroidProjects.findResFolderAsFile(project);
            } while (resFolderFile == null && safetyCounter > 0);
        }
        //find activity theme
        String projectTheme = null;
        String projectRoot = resFolderFile.getParent();
        File manifest = new File(projectRoot + File.separator + "AndroidManifest.xml");
        if (manifest.exists() && manifest.isFile()) {
            try {
                ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(manifest));
                projectTheme = manifestData.getTheme();
                ManifestData.Activity[] activities = manifestData.getActivities();
                for (ManifestData.Activity activity : activities) {
                    String activityName = activity.getName();
                    String activityNameFromRes = new AssetNameConverter(AssetNameConverter.Type.LAYOUT, dataObject.getName()).getValue(AssetNameConverter.Type.ACTIVITY);
                    activityNameFromRes = manifestData.getPackage() + "." + activityNameFromRes;
                    if (activityName.equals(activityNameFromRes)) {
                        if (activity.getTheme() != null) {
                            projectTheme = activity.getTheme();
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException | SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        //find platform
        final File platformFolder = InstalledFileLocator.getDefault().locate("modules/ext/layoutlib_v28_res", "sk-arsi-netbeans-gradle-android-Gradle-Android-support-layout-li", false);

        LayoutPreviewProvider previewProvider = Lookup.getDefault().lookup(LayoutPreviewProvider.class);
        panel = previewProvider.getPreview(platformFolder,
                FileUtil.toFile(dataObject.getPrimaryFile()),
                resFolderFile, projectTheme, aars, jars);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                JEditorPane editorPane = getEditorPane();
                if (editorPane != null) {
                    document = getEditorPane().getDocument();
                    documentListener = DelayedDocumentChangeListener.create(getEditorPane().getDocument(), LayoutMultiViewEditorElement.this, 2000);
                }

            }
        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);

    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LaoutPreviewTopComponent.showLaoutPreview(panel, dataObject.getPrimaryFile().getNameExt());
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        LaoutPreviewTopComponent.hideLaoutPreview();
    }

    @Override
    public void componentClosed() {
        super.componentClosed(); //To change body of generated methods, choose Tools | Templates.
        stopPanel();
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
    public void resultChanged(LookupEvent ev) {
        Collection<? extends LayoutDataObject> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            dataObject = allInstances.iterator().next();
            if (dataObject != null) {
                initLayoutEditor();
            }
        }
    }

}
