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
package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.Result;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.ide.common.resources.FileStatus;
import com.android.ide.common.resources.MergerResourceRepository;
import com.android.ide.common.resources.MergingException;
import com.android.ide.common.resources.ResourceMerger;
import com.android.ide.common.resources.ResourceSet;
import com.android.ide.common.resources.ResourceValueMap;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.VersionQualifier;
import com.android.ide.common.util.DisjointUnionMap;
import com.android.layoutlib.bridge.android.RenderParamsFlags;
import com.android.resources.Keyboard;
import com.android.resources.KeyboardState;
import com.android.resources.Navigation;
import com.android.resources.ResourceType;
import com.android.resources.ResourceUrl;
import com.android.resources.ScreenOrientation;
import com.android.resources.ScreenRatio;
import com.android.resources.ScreenSize;
import com.android.resources.TouchScreen;
import com.android.tools.nbandroid.layoutlib.ConfigGenerator;
import com.android.tools.nbandroid.layoutlib.LayoutLibrary;
import com.android.tools.nbandroid.layoutlib.LayoutLibraryLoader;
import com.android.tools.nbandroid.layoutlib.RenderingException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import sk.arsi.netbeans.gradle.android.layout.impl.android.ResourceResolver;
import sk.arsi.netbeans.gradle.android.layout.impl.v2.AarResourceSet;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;

/**
 *
 * @author arsi
 */
public class LayoutPreviewPanelImpl extends LayoutPreviewPanel implements Runnable, ComponentListener, ActionListener, ItemListener, FileChangeListener {

    private BufferedImage image = null;
    private int dpi;
    private LayoutLibrary layoutLibrary;
    private final AtomicBoolean refreshLock = new AtomicBoolean(false);
    private static final RequestProcessor RP = new RequestProcessor(LayoutPreviewPanel.class);
    private final ImagePanel imagePanel = new ImagePanel();
    private static final String WINDOW_SIZE = "Window size";
    private int imageWidth = 100;
    private int imageHeight = 100;
    private boolean imageFit = true;
    InputStream layoutStream = null;
    private ResourceNamespace appNamespace;
    private final AtomicInteger typingProgress = new AtomicInteger(0);
    private final DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{WINDOW_SIZE, "1920x1080", "1920x1200", "1600x2560", "1080x1920", "1280x800", "1280x768"});
    private LayoutClassLoader uRLClassLoader;
    private ArrayList<ResourceValue> resourceLookupChain;
    private ResourceMerger projectResourceMerger;
    private ResourceSet projectResourceSet;
    private MergerResourceRepository projectResourceRepository;
    private FileObject layoutFileObject;
    private final File appResFolder;
    private final List<String> themes = new ArrayList<>();
    private DelayedFileChangeListener delayedFileChangeListener;

    /**
     * Creates new form LayoutPreviewPanelImpl1
     */
    public LayoutPreviewPanelImpl() {
        initComponents();
        appResFolder = null;
        dpi = 0;
        this.delayedFileChangeListener = null;
    }

    public LayoutPreviewPanelImpl(File platformFolder, File layoutFile, File appResFolder, String themeName, List<File> aars, List<File> jars, File projectClassesFolder, File projectR, String appPackage) {
        super(platformFolder, layoutFile, appResFolder, themeName, aars, jars, projectClassesFolder, projectR, appPackage);
        initComponents();
        density.setModel(new DefaultComboBoxModel<>(Density.values()));
        density.setSelectedItem(Density.MEDIUM);
        this.appResFolder = appResFolder;
        previewSize.setModel(model);
        previewSize.addActionListener(LayoutPreviewPanelImpl.this);
        previewSize.setEditable(true);
        scrollPane.setViewportView(imagePanel);
        themeMode.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                if (themeMode.isSelected()) {
                    themeMode.setText("Parent");
                } else {
                    themeMode.setText("Theme");
                }
            }
        });
        themeMode.addItemListener(this);
        setLayout(new BoxLayout(LayoutPreviewPanelImpl.this, BoxLayout.PAGE_AXIS));
        //dont block UI
        Runnable runnable = new Runnable() {
            public void run() {
                String projectRoot = appResFolder.getParent();
                appNamespace = ResourceNamespace.RES_AUTO;
                dpi = Toolkit.getDefaultToolkit().getScreenResolution();
                try {
                    layoutStream = new FileInputStream(layoutFile);
                } catch (FileNotFoundException ex) {
                }
                List<URL> urls = new ArrayList<>();
                for (File aar : aars) {
                    File classes = new File(aar.getPath() + File.separator + "jars" + File.separator + "classes.jar");
                    if (classes.exists() && classes.isFile()) {
                        try {
                            urls.add(classes.toURI().toURL());
                        } catch (MalformedURLException ex) {
                        }
                    }
                }
                for (File jar : jars) {
                    if (jar.exists() && jar.isFile()) {
                        try {
                            urls.add(jar.toURI().toURL());
                        } catch (MalformedURLException ex) {
                        }
                    }
                }
                try {
                    ClassLoader moduleClassLoader = LayoutLibrary.class.getClassLoader();
                    uRLClassLoader = new LayoutClassLoader(urls.toArray(new URL[urls.size()]), aars, moduleClassLoader, appNamespace);
                    //load Bridge with arr classpath
                    layoutLibrary = LayoutLibraryLoader.load(platformFolder, uRLClassLoader);
                } catch (RenderingException | IOException ex) {
                    Logger.getLogger(LayoutPreviewPanelImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                addComponentListener(LayoutPreviewPanelImpl.this);
                scale.addItemListener(LayoutPreviewPanelImpl.this);
                density.addItemListener(LayoutPreviewPanelImpl.this);
                FileObject resFo = FileUtil.toFileObject(appResFolder);
                resFo.addRecursiveListener(WeakListeners.create(FileChangeListener.class, LayoutPreviewPanelImpl.this, resFo));
                layoutFileObject = FileUtil.toFileObject(layoutFile);
                refreshPreview();
                //When compiling project classes, a lot of events will occur, but the last one is enough
                delayedFileChangeListener = new DelayedFileChangeListener(LayoutPreviewPanelImpl.this);
                if (projectClassesFolder.exists() && projectClassesFolder.isDirectory()) {
                    FileObject fo = FileUtil.toFileObject(projectClassesFolder);
                    fo.addRecursiveListener(WeakListeners.create(FileChangeListener.class, delayedFileChangeListener, fo));
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
        previewSize = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        scale = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        density = new javax.swing.JComboBox<>();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jPanel1 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        themeCombo = new javax.swing.JComboBox<>();
        reset = new javax.swing.JButton();
        themeMode = new javax.swing.JToggleButton();

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setMaximumSize(new java.awt.Dimension(65745, 25));
        toolbar.setMinimumSize(new java.awt.Dimension(267, 25));

        previewSize.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        previewSize.setMaximumSize(new java.awt.Dimension(200, 32767));
        previewSize.setMinimumSize(new java.awt.Dimension(200, 24));
        previewSize.setPreferredSize(new java.awt.Dimension(200, 24));
        toolbar.add(previewSize);
        toolbar.add(jSeparator1);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.jLabel1.text")); // NOI18N
        toolbar.add(jLabel1);

        scale.setSelected(true);
        scale.setText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.scale.text")); // NOI18N
        scale.setFocusable(false);
        scale.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        scale.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(scale);
        toolbar.add(jSeparator3);

        density.setMaximumSize(new java.awt.Dimension(150, 32767));
        density.setMinimumSize(new java.awt.Dimension(150, 24));
        toolbar.add(density);
        toolbar.add(jSeparator4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(scrollPane)
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        themeCombo.setToolTipText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.themeCombo.toolTipText")); // NOI18N
        themeCombo.setMaximumSize(new java.awt.Dimension(32767, 24));

        reset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/arsi/netbeans/gradle/android/layout/impl/restore.png"))); // NOI18N
        reset.setText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.reset.text")); // NOI18N
        reset.setToolTipText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.reset.toolTipText")); // NOI18N
        reset.setMaximumSize(new java.awt.Dimension(94, 21));
        reset.setMinimumSize(new java.awt.Dimension(94, 21));
        reset.setPreferredSize(new java.awt.Dimension(94, 21));
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetActionPerformed(evt);
            }
        });

        themeMode.setText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.themeMode.text")); // NOI18N
        themeMode.setMaximumSize(new java.awt.Dimension(142, 21));
        themeMode.setMinimumSize(new java.awt.Dimension(142, 21));
        themeMode.setPreferredSize(new java.awt.Dimension(142, 21));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(reset, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(themeMode, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(themeCombo, 0, 539, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(themeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(themeMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetActionPerformed
        // TODO add your handling code here:
        themeCombo.setSelectedItem(themeName);
    }//GEN-LAST:event_resetActionPerformed

    public void setImage(BufferedImage image) {
        this.image = image;
        setPreferredSize(new Dimension(new Dimension(image.getWidth(), image.getHeight())));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, this); // see javadoc for more info on the parameters
        }

    }

    private ConfigGenerator getCurrentConfig() {

        ConfigGenerator current = new ConfigGenerator()
                .setScreenHeight(imageHeight)
                .setScreenWidth(imageWidth)
                .setXdpi(dpi)
                .setYdpi(dpi)
                .setOrientation(ScreenOrientation.PORTRAIT)
                .setDensity(((Density) density.getSelectedItem()).getDensity())
                .setRatio(ScreenRatio.NOTLONG)
                .setSize(ScreenSize.NORMAL)
                .setKeyboard(Keyboard.NOKEY)
                .setTouchScreen(TouchScreen.FINGER)
                .setKeyboardState(KeyboardState.SOFT)
                .setSoftButtons(true)
                .setNavigation(Navigation.NONAV);
        return current;
    }

    @Override
    public void run() {
        refreshLock.set(false);
        LayoutIO.getDefaultIO().reset();
        LayoutIO.logInfo("I'm starting to generate a preview of " + layoutFile.getName());
        ProjectLayoutClassLoader projectLayoutClassLoader = ProjectLayoutClassLoader.getClassloader(projectClassesFolder, projectR, appPackage, uRLClassLoader);
        if (WINDOW_SIZE.equals(model.getSelectedItem())) {
            imageWidth = imagePanel.getWidth();
            imageHeight = imagePanel.getHeight();
        }
        if (projectResourceMerger == null) {
            initProjectRepository();
        } else {
            projectResourceRepository.update(projectResourceMerger);
        }
        imagePanel.label.setText("Loading...");
        imagePanel.label.setVisible(true);
        imagePanel.progress.setVisible(true);
        if (layoutStream instanceof FileInputStream) {
            //first is layout loaded from file and FileInputStream dont supports reset
            try {
                layoutStream.close();
            } catch (IOException ex) {
            }
            try {
                layoutStream = new FileInputStream(layoutFile);
            } catch (FileNotFoundException ex) {
            }
        } else {
            try {
                layoutStream.reset();
            } catch (IOException ex) {
            }

        }

        try {
            RenderSession session = layoutLibrary.createSession(getSessionParams(platformFolder, LayoutFilePullParser.create(layoutStream, appNamespace), getCurrentConfig(), new LayoutLibCallback(new LayoutIO(), aars, uRLClassLoader, appNamespace, projectLayoutClassLoader), themeName, true, SessionParams.RenderingMode.NORMAL, 27));
            Result renderResult = session.render();
            if (renderResult.getException() != null) {
                LayoutIO.getDefaultIO().show();
                LayoutIO.logError("unable to generate layout preview", renderResult.getException());
                renderResult.getException().printStackTrace();
                imagePanel.label.setText("Error rendering layout");
                imagePanel.label.setVisible(true);
            } else if (renderResult.getStatus() == Result.Status.SUCCESS) {
                setImage(session.getImage());
                imagePanel.label.setVisible(false);
                imagePanel.progress.setVisible(false);
            } else {
                LayoutIO.getDefaultIO().show();
                LayoutIO.logError("unable to generate layout preview: " + renderResult.getStatus(), null);
            }
        } catch (Exception e) {
            LayoutIO.getDefaultIO().show();
            e.printStackTrace();
            imagePanel.label.setText("Error rendering layout");
            imagePanel.label.setVisible(true);
        }
        LayoutIO.logInfo("Preview of " + layoutFile.getName() + " is done.");
    }

    protected SessionParams getSessionParams(File platformFolder, ILayoutPullParser layoutParser,
            ConfigGenerator configGenerator, LayoutLibCallback layoutLibCallback,
            String themeName, boolean isProjectTheme, SessionParams.RenderingMode renderingMode,
            @SuppressWarnings("SameParameterValue") int targetSdk) {
        //****************
        FolderConfiguration config = configGenerator.getFolderConfig();
        config.setVersionQualifier(VersionQualifier.getQualifier(VersionQualifier.getFolderSegment(28)));

        //****************
        File platform_data_dir = new File(platformFolder, "data");
        File platform_res_dir = new File(platform_data_dir, "res");
        //****************
        Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> allResources
                = new DisjointUnionMap<>(FrameworkResourcesCache.getOrCreateFrameworkResources(platform_res_dir).getConfiguredResources(config).rowMap(), projectResourceRepository.getConfiguredResources(config).rowMap());

        if (themes.isEmpty()) {
            for (Map.Entry<ResourceNamespace, Map<ResourceType, ResourceValueMap>> entry : allResources.entrySet()) {
                if (entry.getKey().equals(ResourceNamespace.RES_AUTO)) {
                    Map<ResourceType, ResourceValueMap> value = entry.getValue();
                    ResourceValueMap m = value.get(ResourceType.STYLE);
                    for (Map.Entry<String, ResourceValue> entry1 : m.entrySet()) {
                        String key = entry1.getKey();
                        ResourceValue value1 = entry1.getValue();
                        if (value1 instanceof StyleResourceValue) {
                            themes.add("@style/" + value1.getName());
                        }
                    }
                }

            }
            Collections.sort(themes);
            themeCombo.setModel(new DefaultComboBoxModel<>(new Vector<>(themes)));
            themeCombo.setSelectedItem(themeName);
            themeCombo.addItemListener(this);
        }
        ResourceUrl themeUrl;
        if (themeMode.isSelected()) {
            //mode parent
            themeUrl = ResourceUrl.parse(themeName);
        } else {
            //mode theme
            LayoutIO.logInfo("Theme patch, current theme changed to: " + ((String) themeCombo.getSelectedItem()));
            themeUrl = ResourceUrl.parse((String) themeCombo.getSelectedItem());
        }
        ResourceReference theme = null;

        if (themeUrl != null) {
            theme = themeUrl.resolve(appNamespace, new ResourceNamespace.Resolver() {
                @Override
                public String prefixToUri(String namespacePrefix) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
        ResourceResolver resourceResolver = ResourceResolver.create(allResources, theme);
        if (!((String) themeCombo.getSelectedItem()).equals(themeName) && themeMode.isSelected()) {
            //mode parent and another theme is selected
            resourceResolver.patchAutoStyleParent(themeName.replace("@style/", ""), ((String) themeCombo.getSelectedItem()).replace("@style/", ""));
            LayoutIO.logInfo("Theme patch, theme parent changed to: " + ((String) themeCombo.getSelectedItem()));
        }
        resourceResolver.setDeviceDefaults("Material");
        resourceLookupChain = new ArrayList<>();
        SessionParams sessionParams
                = new SessionParams(layoutParser, renderingMode, null /*used for caching*/,
                        configGenerator.getHardwareConfig(), resourceResolver.createRecorder(resourceLookupChain), layoutLibCallback, 0,
                        targetSdk, new LayoutLog());
        sessionParams.setFlag(RenderParamsFlags.FLAG_DO_NOT_RENDER_ON_CREATE, true);
        sessionParams.setAssetRepository(new LayoutAssetRepository());
        return sessionParams;
    }

    private void initProjectRepository() {
        //************
        projectResourceMerger = new ResourceMerger(0);
        for (File aar : aars) {
            File resFolder = new File(aar.getPath() + File.separator + "res");
            if (resFolder.exists() && resFolder.isDirectory()) {
                AarResourceSet aarSet = new AarResourceSet(aar.getName(), ResourceNamespace.RES_AUTO, aar.getName(), false);
                aarSet.addSource(resFolder);
                aarSet.setShouldParseResourceIds(false);
                aarSet.setTrackSourcePositions(true);
                aarSet.setCheckDuplicates(true);
                try {
                    aarSet.loadFromFiles(new LayoutIO());
                    projectResourceMerger.addDataSet(aarSet);
                } catch (MergingException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        projectResourceSet = new ResourceSet("project", ResourceNamespace.RES_AUTO, "project", false);
        projectResourceSet.addSource(appResFolder);
        projectResourceSet.setShouldParseResourceIds(true);
        projectResourceSet.setTrackSourcePositions(false);
        projectResourceSet.setCheckDuplicates(false);
        try {
            projectResourceSet.loadFromFiles(new LayoutIO());
            projectResourceMerger.addDataSet(projectResourceSet);
        } catch (MergingException ex) {
            Exceptions.printStackTrace(ex);
        }
        //**
        projectResourceRepository = new MergerResourceRepository();
        projectResourceRepository.update(projectResourceMerger);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (refreshLock.compareAndSet(false, true)) {
            RP.execute(LayoutPreviewPanelImpl.this);
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    private int selectedIndex = -1;

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = previewSize.getSelectedIndex();
        if (index >= 0) {
            selectedIndex = index;
            Object value = model.getSelectedItem();
            if (WINDOW_SIZE.equals(value)) {
                imageFit = true;
                imagePanel.setMaximumSize(new Dimension(scrollPane.getWidth(), scrollPane.getHeight()));
                imagePanel.setPreferredSize(new Dimension(scrollPane.getWidth(), scrollPane.getHeight()));
                imageWidth = scrollPane.getWidth();
                imageHeight = scrollPane.getHeight();
                scrollPane.updateUI();
                refreshPreview();
            } else {
                StringTokenizer tok = new StringTokenizer((String) value, "x", false);
                if (tok.countTokens() == 2) {
                    imageWidth = Integer.parseInt(tok.nextToken());
                    imageHeight = Integer.parseInt(tok.nextToken());
                    if (scale.isSelected()) {
                        imagePanel.setMaximumSize(new Dimension(scrollPane.getWidth(), scrollPane.getHeight()));
                        imagePanel.setPreferredSize(new Dimension(scrollPane.getWidth(), scrollPane.getHeight()));
                        imageFit = true;
                    } else {
                        imagePanel.setMaximumSize(new Dimension(imageWidth, imageHeight));
                        imagePanel.setPreferredSize(new Dimension(imageWidth, imageHeight));
                        imageFit = false;
                    }
                    imagePanel.updateUI();
                    refreshPreview();
                }
            }
        } else if ("comboBoxEdited".equals(e.getActionCommand())) {
            Object newValue = model.getSelectedItem();
            if ((newValue instanceof String) && ((String) newValue).contains("x")) {
                StringTokenizer tok = new StringTokenizer((String) newValue, "x", false);
                if (tok.countTokens() == 2) {
                    try {
                        int width = Integer.parseInt(tok.nextToken());
                        int height = Integer.parseInt(tok.nextToken());
                        imageWidth = width;
                        imageHeight = height;
                        model.addElement(newValue);
                        previewSize.setSelectedItem(newValue);
                        selectedIndex = model.getIndexOf(newValue);
                        if (scale.isSelected()) {
                            imageFit = true;
                            imagePanel.setMaximumSize(new Dimension(scrollPane.getWidth(), scrollPane.getHeight()));
                            imagePanel.setPreferredSize(new Dimension(scrollPane.getWidth(), scrollPane.getHeight()));
                        } else {
                            imageFit = false;
                            imagePanel.setMaximumSize(new Dimension(imageWidth, imageHeight));
                            imagePanel.setPreferredSize(new Dimension(imageWidth, imageHeight));
                        }
                        imagePanel.updateUI();
                        refreshPreview();
                    } catch (NumberFormatException numberFormatException) {
                    }
                }

            }
        }
    }

    public void refreshPreview() {
        if (refreshLock.compareAndSet(false, true)) {
            RP.execute(LayoutPreviewPanelImpl.this);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        actionPerformed(new ActionEvent(this, 0, ""));
    }

    @Override
    public void refreshPreview(InputStream stream) {
        layoutStream = stream;
        imagePanel.label.setVisible(false);
        typingProgress.set(0);
        refreshPreview();
    }

    @Override
    public void showTypingIndicator() {
        imagePanel.label.setVisible(true);
        int progress = typingProgress.incrementAndGet();
        String tmp = "";
        for (int i = 0; i < progress; i++) {
            tmp += ".";
        }
        imagePanel.label.setText(tmp);
        if (image != null) {
            image = null;
            imagePanel.updateUI();
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        if (fe.getFile().equals(layoutFileObject)) {
            return;
        }
        File createdFile = FileUtil.toFile(fe.getFile());
        try {
            projectResourceSet.updateWith(appResFolder, createdFile, FileStatus.NEW, new LayoutIO());
        } catch (Exception ex) {
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                refreshPreview();
            }
        };
        RP.execute(runnable);
    }

    @Override
    public void fileChanged(FileEvent fe) {
        if (fe.getFile().equals(layoutFileObject)) {
            return;
        }
        File createdFile = FileUtil.toFile(fe.getFile());
        try {
            projectResourceSet.updateWith(appResFolder, createdFile, FileStatus.CHANGED, new LayoutIO());
        } catch (Exception ex) {
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                refreshPreview();
            }
        };
        RP.execute(runnable);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        if (fe.getFile().equals(layoutFileObject)) {
            return;
        }
        File createdFile = FileUtil.toFile(fe.getFile());
        try {
            projectResourceSet.updateWith(appResFolder, createdFile, FileStatus.REMOVED, new LayoutIO());
        } catch (Exception ex) {
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                refreshPreview();
            }
        };
        RP.execute(runnable);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private class ImagePanel extends JPanel implements Scrollable {

        private javax.swing.JPanel jPanel1;
        private javax.swing.JLabel label;
        private javax.swing.JProgressBar progress;

        public ImagePanel() {
            initComponents();
        }

        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;

            jPanel1 = new javax.swing.JPanel();
            label = new javax.swing.JLabel();
            progress = new javax.swing.JProgressBar();

            jPanel1.setOpaque(false);
            jPanel1.setLayout(new java.awt.GridBagLayout());

            label.setFont(new java.awt.Font("Arial", 1, 25)); // NOI18N
            label.setText("Loading..."); // NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            jPanel1.add(label, gridBagConstraints);

            progress.setIndeterminate(true);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            jPanel1.add(progress, gridBagConstraints);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                if (scale.isSelected()) {
                    int imgWidth = image.getWidth(null);
                    int imgHeight = image.getHeight(null);

                    double imgAspect = (double) imgHeight / imgWidth;

                    int canvasWidth = getWidth();
                    int canvasHeight = getHeight();

                    double canvasAspect = (double) canvasHeight / canvasWidth;

                    int x1 = 0; // top left X position
                    int y1 = 0; // top left Y position
                    int x2 = 0; // bottom right X position
                    int y2 = 0; // bottom right Y position

                    if (imgWidth < canvasWidth && imgHeight < canvasHeight) {
                        // the image is smaller than the canvas
                        x1 = (canvasWidth - imgWidth) / 2;
                        y1 = (canvasHeight - imgHeight) / 2;
                        x2 = imgWidth + x1;
                        y2 = imgHeight + y1;

                    } else {
                        if (canvasAspect > imgAspect) {
                            y1 = canvasHeight;
                            // keep image aspect ratio
                            canvasHeight = (int) (canvasWidth * imgAspect);
                            y1 = (y1 - canvasHeight) / 2;
                        } else {
                            x1 = canvasWidth;
                            // keep image aspect ratio
                            canvasWidth = (int) (canvasHeight / imgAspect);
                            x1 = (x1 - canvasWidth) / 2;
                        }
                        x2 = canvasWidth + x1;
                        y2 = canvasHeight + y1;
                    }

                    g.drawImage(image, x1, y1, x2, y2, 0, 0, imgWidth, imgHeight, null);
                } else {
                    g.drawImage(image, 0, 0, this);
                }
            }

        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(imageWidth, imageHeight);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 5;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return imageFit;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return imageFit;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Density> density;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JComboBox<String> previewSize;
    private javax.swing.JButton reset;
    private javax.swing.JCheckBox scale;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JComboBox<String> themeCombo;
    private javax.swing.JToggleButton themeMode;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
