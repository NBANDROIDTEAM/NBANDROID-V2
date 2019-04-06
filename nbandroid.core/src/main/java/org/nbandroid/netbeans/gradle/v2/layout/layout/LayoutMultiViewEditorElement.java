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

import com.android.SdkConstants;
import com.android.builder.model.AndroidProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import org.nbandroid.netbeans.gradle.v2.manifest.AndroidManifestParser;
import org.nbandroid.netbeans.gradle.v2.manifest.ManifestData;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.AssetNameConverter;
import org.nbandroid.netbeans.gradle.v2.tools.DelayedDocumentChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.android.project.build.BuildVariant;
import org.netbeans.modules.android.project.query.AndroidClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;
import org.xml.sax.SAXException;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewProvider;

/**
 *
 * @author arsi
 */
public class LayoutMultiViewEditorElement extends MultiViewEditorElement implements ChangeListener, LookupListener, PropertyChangeListener {

    private LayoutDataObject dataObject;
    private LayoutPreviewPanel panel;
    private Document document;
    private DocumentListener documentListener;
    private Project project;
    private Lookup.Result<AndroidProject> lookupResultAndroidProject;
    private Lookup.Result<AndroidClassPathProvider> lookupResulClassPathProvider;
    private Lookup.Result<BuildVariant> lookupResulBuildVariant;
    private Loader loader = null;
    private volatile AndroidClassPathProvider androidClassPathProvider = null;
    private volatile BuildVariant buildVariant;
    private volatile File projectClassesFolder;
    private volatile ClassPath classPath;
    private volatile FileObject[] roots;
    private volatile File resFolderFile;

    public LayoutMultiViewEditorElement(Lookup lookup) {
        super(lookup);
        dataObject = lookup.lookup(LayoutDataObject.class);
        start();
    }

    private void start() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
                lookupResultAndroidProject = project.getLookup().lookupResult(AndroidProject.class);
                lookupResulClassPathProvider = project.getLookup().lookupResult(AndroidClassPathProvider.class);
                lookupResulBuildVariant = project.getLookup().lookupResult(BuildVariant.class);
                lookupResultAndroidProject.addLookupListener(WeakListeners.create(LookupListener.class, LayoutMultiViewEditorElement.this, lookupResultAndroidProject));
                lookupResulClassPathProvider.addLookupListener(WeakListeners.create(LookupListener.class, LayoutMultiViewEditorElement.this, lookupResulClassPathProvider));
                lookupResulBuildVariant.addLookupListener(WeakListeners.create(LookupListener.class, LayoutMultiViewEditorElement.this, lookupResulBuildVariant));
                resultChanged(null);
            }
        };
        ProjectManager.mutex().postWriteRequest(runnable);
    }

    protected void stopPanel() {
        if (documentListener != null && document != null) {
            document.removeDocumentListener(documentListener);
        }
        document = null;
        documentListener = null;
    }

    private void initLayoutEditor() {
        //find aars folders from classpath
        String variant = projectClassesFolder.getName();
        File buildRoot = projectClassesFolder.getParentFile().getParentFile();
        File projectR = new File(buildRoot, SdkConstants.FD_SYMBOLS + File.separator + variant + File.separator + "R.txt");
        final List<File> aars = new ArrayList<>();
        final List<File> jars = new ArrayList<>();
        for (FileObject root : roots) {
            if ("classes.jar".equals(FileUtil.getArchiveFile(root).getNameExt())) {
                aars.add(FileUtil.toFile(FileUtil.getArchiveFile(root).getParent().getParent()));
            } else {
                jars.add(FileUtil.toFile(FileUtil.getArchiveFile(root)));
            }
        }
        //find activity theme
        String appPackage = null;
        String projectTheme = null;
        String projectRoot = resFolderFile.getParent();
        File manifest = new File(projectRoot + File.separator + "AndroidManifest.xml");
        if (manifest.exists() && manifest.isFile()) {
            try {
                ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(manifest));
                appPackage = manifestData.getPackage();
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
                resFolderFile, projectTheme, aars, jars, projectClassesFolder, projectR, appPackage);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                JEditorPane editorPane = getEditorPane();
                if (editorPane != null) {
                    document = getEditorPane().getDocument();
                    documentListener = DelayedDocumentChangeListener.create(getEditorPane().getDocument(), LayoutMultiViewEditorElement.this, 2000);
                    LaoutPreviewTopComponent.hideLaoutPreview();
                    LaoutPreviewTopComponent.showLaoutPreview(panel, dataObject.getPrimaryFile().getNameExt());
                    //updateUI dont works, close and reopen
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
    private transient boolean done = false;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (roots.length == 0) {
            roots = classPath.getRoots();
        }
        if (roots.length > 0 && projectClassesFolder != null && !done) {
            initLayoutEditor();
            done = true;
        }
    }

    private class Loader implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (buildVariant.isValid() && projectClassesFolder == null) {
                projectClassesFolder = buildVariant.getCurrentVariant().getMainArtifact().getClassesFolder();
            }
            if (roots.length > 0 && projectClassesFolder != null && !done) {
                initLayoutEditor();
                done = true;
            }
        }

    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends AndroidClassPathProvider> allInstances = lookupResulClassPathProvider.allInstances();
        Collection<? extends BuildVariant> allInstances1 = lookupResulBuildVariant.allInstances();
        Collection<? extends AndroidProject> allInstances2 = lookupResultAndroidProject.allInstances();
        if (!allInstances2.isEmpty() && !allInstances.isEmpty() && androidClassPathProvider == null && !allInstances1.isEmpty()) {
            AndroidProject androidProject = allInstances2.iterator().next();
            Collection<File> resDirectories = androidProject.getDefaultConfig().getSourceProvider().getResDirectories();
            resFolderFile = resDirectories.iterator().next();
            androidClassPathProvider = allInstances.iterator().next();
            classPath = androidClassPathProvider.getCompilePath();
            classPath.addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, this, classPath));
            roots = classPath.getRoots();
            buildVariant = allInstances1.iterator().next();
            loader = new Loader();
            buildVariant.addChangeListener(WeakListeners.change(loader, buildVariant));
            loader.stateChanged(null);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(dataObject.getPrimaryFile().getPath());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object readObject = in.readObject();
        if (readObject instanceof String) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    FileObject fo = FileUtil.toFileObject(new File((String) readObject));
                    if (fo != null) {
                        try {
                            dataObject = (LayoutDataObject) DataObject.find(fo);
                            start();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            };
            SwingUtilities.invokeLater(runnable);
        } else {
            throw new IOException("Error restore layout component");
        }
    }

}
