/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import android.annotation.NonNull;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.Result;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.ResourceValueMap;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.deprecated.FrameworkResources;
import com.android.ide.common.resources.deprecated.ResourceItem;
import com.android.ide.common.resources.deprecated.ResourceRepository;
import com.android.ide.common.util.DisjointUnionMap;
import com.android.ide.common.xml.ManifestData;
import com.android.io.FolderWrapper;
import com.android.layoutlib.bridge.android.RenderParamsFlags;
import com.android.resources.Density;
import com.android.resources.Keyboard;
import com.android.resources.KeyboardState;
import com.android.resources.Navigation;
import com.android.resources.ResourceType;
import com.android.resources.ScreenOrientation;
import com.android.resources.ScreenRatio;
import com.android.resources.ScreenSize;
import com.android.resources.TouchScreen;
import com.android.tools.nbandroid.layoutlib.ConfigGenerator;
import com.android.tools.nbandroid.layoutlib.LayoutLibrary;
import com.android.tools.nbandroid.layoutlib.LayoutLibraryLoader;
import com.android.tools.nbandroid.layoutlib.LogWrapper;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import static sk.arsi.netbeans.gradle.android.layout.impl.ResClassGenerator.parseProjectManifest;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;

/**
 *
 * @author arsi
 */
public class LayoutPreviewPanelImpl extends LayoutPreviewPanel implements Runnable, ComponentListener, ActionListener, ItemListener {

    private BufferedImage image = null;
    private final int dpi;
    private LayoutLibrary layoutLibrary;
    private FrameworkResources sFrameworkRepo;
    private ResourceRepository sProjectResources;
    private final AtomicBoolean refreshLock = new AtomicBoolean(false);
    private final RequestProcessor RP = new RequestProcessor(LayoutPreviewPanel.class);
    private final ImagePanel imagePanel = new ImagePanel();
    private static final String WINDOW_SIZE = "Window size";
    private int imageWidth = 100;
    private int imageHeight = 100;
    private boolean imageFit = true;
    InputStream layoutStream = null;
    private boolean typing = false;
    private String appNamespaceName = "app";
    private ResourceNamespace appNamespace;
    private final AtomicInteger typingProgress = new AtomicInteger(0);
    private final DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{WINDOW_SIZE, "1920x1080", "1920x1200", "1600x2560", "1080x1920", "1280x800", "1280x768"});
    private LayoutClassLoader uRLClassLoader;

    /**
     * Creates new form LayoutPreviewPanelImpl1
     */
    public LayoutPreviewPanelImpl() {
        initComponents();
        dpi = 0;
    }

    public LayoutPreviewPanelImpl(File platformFolder, File layoutFile, File appResFolder, String themeName, List<File> aars, List<File> jars) {
        super(platformFolder, layoutFile, appResFolder, themeName, aars, jars);
        initComponents();
        String projectRoot = appResFolder.getParent();
        File manifest = new File(projectRoot + File.separator + "AndroidManifest.xml");
        if (manifest.exists() && manifest.isFile()) {
            try {
                ManifestData manifestData = parseProjectManifest(new FileInputStream(manifest));
                if (manifestData != null) {
                    appNamespaceName = manifestData.getPackage();
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        appNamespace = ResourceNamespace.fromPackageName(appNamespaceName);
        previewSize.setModel(model);
        previewSize.addActionListener(this);
        previewSize.setEditable(true);
        scrollPane.setViewportView(imagePanel);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
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
            uRLClassLoader = new LayoutClassLoader(urls.toArray(new URL[urls.size()]), aars, moduleClassLoader);
            //load Bridge with arr classpath
            layoutLibrary = LayoutLibraryLoader.load(platformFolder, uRLClassLoader);
        } catch (RenderingException | IOException ex) {
            Logger.getLogger(LayoutPreviewPanelImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        addComponentListener(this);
        scale.addItemListener(this);
        screenOrientation.addItemListener(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolbar = new javax.swing.JToolBar();
        previewSize = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        scale = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        screenOrientation = new javax.swing.JComboBox<>();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        scrollPane = new javax.swing.JScrollPane();

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setMaximumSize(new java.awt.Dimension(65745, 25));
        toolbar.setMinimumSize(new java.awt.Dimension(267, 25));

        previewSize.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        previewSize.setMaximumSize(new java.awt.Dimension(200, 32767));
        previewSize.setMinimumSize(new java.awt.Dimension(200, 24));
        previewSize.setPreferredSize(new java.awt.Dimension(200, 24));
        toolbar.add(previewSize);

        jSeparator1.setOrientation(javax.swing.SwingConstants.HORIZONTAL);
        toolbar.add(jSeparator1);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.jLabel1.text")); // NOI18N
        toolbar.add(jLabel1);

        scale.setSelected(true);
        scale.setText(org.openide.util.NbBundle.getMessage(LayoutPreviewPanelImpl.class, "LayoutPreviewPanelImpl.scale.text")); // NOI18N
        scale.setFocusable(false);
        scale.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        scale.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(scale);

        jSeparator3.setOrientation(javax.swing.SwingConstants.HORIZONTAL);
        toolbar.add(jSeparator3);

        screenOrientation.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Portrait", "Landscape" }));
        screenOrientation.setMaximumSize(new java.awt.Dimension(150, 32767));
        screenOrientation.setMinimumSize(new java.awt.Dimension(150, 24));
        toolbar.add(screenOrientation);

        jSeparator2.setOrientation(javax.swing.SwingConstants.HORIZONTAL);
        toolbar.add(jSeparator2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
            .addComponent(scrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

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
        ScreenOrientation orientation = ScreenOrientation.LANDSCAPE;
        if ("Portrait".equals(screenOrientation.getSelectedItem())) {
            orientation = ScreenOrientation.PORTRAIT;
        }

        ConfigGenerator current = new ConfigGenerator()
                .setScreenHeight(imageHeight)
                .setScreenWidth(imageWidth)
                .setXdpi(dpi)
                .setYdpi(dpi)
                .setOrientation(orientation)
                .setDensity(Density.XXHIGH)
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
            RenderSession session = layoutLibrary.createSession(getSessionParams(platformFolder, new LayoutPullParser(layoutStream, appNamespace), getCurrentConfig(), new LayoutLibTestCallback(new LogWrapper(), aars, uRLClassLoader, appNamespace), themeName, true, SessionParams.RenderingMode.NORMAL, 27));
            Result renderResult = session.render();
            if (renderResult.getException() != null) {
                renderResult.getException().printStackTrace();
                imagePanel.label.setText("Error rendering layout");
                imagePanel.label.setVisible(true);
            } else {
                setImage(session.getImage());
                imagePanel.label.setVisible(false);
                imagePanel.progress.setVisible(false);
            }
        } catch (Exception e) {
            imagePanel.label.setText("Error rendering layout");
            imagePanel.label.setVisible(true);
        }
    }

    protected SessionParams getSessionParams(File platformFolder, LayoutPullParser layoutParser,
            ConfigGenerator configGenerator, LayoutLibTestCallback layoutLibCallback,
            String themeName, boolean isProjectTheme, SessionParams.RenderingMode renderingMode,
            @SuppressWarnings("SameParameterValue") int targetSdk) {
        File data_dir = new File(platformFolder, "data");
        File res = new File(data_dir, "res");
        sFrameworkRepo = FrameworkResourcesCache.getOrCreateFrameworkResources(res);
        sProjectResources = new ResourceRepository(new FolderWrapper(appResFolder),
                false) {
            @NonNull
            @Override
            protected ResourceItem createResourceItem(@NonNull String name) {
                return new ResourceItem(name) {
                };
            }
        };
        sProjectResources.loadResources();
        FolderConfiguration config = configGenerator.getFolderConfig();
        Map<ResourceType, ResourceValueMap> pRes = sProjectResources.getConfiguredResources(config);
        Map<ResourceType, ResourceValueMap> fRes = sFrameworkRepo.getConfiguredResources(config);
        Map<ResourceNamespace, Map<ResourceType, ResourceValueMap>> allResources
                = new DisjointUnionMap<>(Collections.singletonMap(ResourceNamespace.ANDROID, fRes), Collections.singletonMap(appNamespace, pRes));
        ResourceReference theme = new ResourceReference(appNamespace, ResourceType.STYLE, themeName);
        ResourceResolver resourceResolver = ResourceResolver.create(allResources, theme);
        resourceResolver.setDeviceDefaults("Holo");
        resourceResolver.setProjectIdChecker(new Predicate<ResourceReference>() {
            @Override
            public boolean test(ResourceReference t) {
                System.out.println(">>>" + t);
                return true;
            }
        });
        SessionParams sessionParams
                = new SessionParams(layoutParser, renderingMode, null /*used for caching*/,
                        configGenerator.getHardwareConfig(), resourceResolver, layoutLibCallback, 0,
                        targetSdk, new LayoutLog());
        sessionParams.setFlag(RenderParamsFlags.FLAG_DO_NOT_RENDER_ON_CREATE, true);
        sessionParams.setAssetRepository(new TestAssetRepository());
        return sessionParams;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (refreshLock.compareAndSet(false, true)) {
            if (WINDOW_SIZE.equals(model.getSelectedItem())) {
                imageWidth = imagePanel.getWidth();
                imageHeight = imagePanel.getHeight();
            }
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

    private void refreshPreview() {
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JComboBox<String> previewSize;
    private javax.swing.JCheckBox scale;
    private javax.swing.JComboBox<String> screenOrientation;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
