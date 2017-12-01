/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.sdk.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkImpl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author tom
 * @author arsi - android mod
 */
public class SdksCustomizer extends javax.swing.JPanel implements PropertyChangeListener, VetoableChangeListener, ExplorerManager.Provider {

    private static final Logger LOG = Logger.getLogger(SdksCustomizer.class.getName());

    private static final String TEMPLATE = "Templates/Services/Platforms/org-netbeans-api-java-Platform/javaplatform.xml";  //NOI18N
    private static final String STORAGE = "Services/Platforms/org-nbandroid-netbeans-gradle-Platform";  //NOI18N
    private static final String ATTR_CAN_REMOVE = "can-remove"; //NOI18N

    private PlatformCategoriesChildren children;
    private ExplorerManager manager;
    private final AndroidSdkImpl initialPlatform;
    private AndroidSdkImpl selectedPlatform;
    private static final Vector<AndroidSdkImpl> platformsList = new Vector<>();

    /**
     * Shows platforms customizer
     *
     * @param platform which should be seelcted, may be null
     * @return boolean for future extension, currently always true
     */
    public static boolean showCustomizer() {
        SdksCustomizer customizer
                = new SdksCustomizer();
        javax.swing.JButton close = new javax.swing.JButton(NbBundle.getMessage(SdksCustomizer.class, "CTL_Close"));
        close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SdksCustomizer.class, "AD_Close"));
        DialogDescriptor descriptor = new DialogDescriptor(customizer, NbBundle.getMessage(SdksCustomizer.class,
                "TXT_PlatformsManager"), true, new Object[]{close}, close, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("org.nbandroid.netbeans.gradle.v2.sdk.ui.PlatformsCustomizer"), null); // NOI18N
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        return true;
    }

    /**
     * Creates new form PlatformsCustomizer
     */
    public SdksCustomizer() {
        this.initialPlatform = null;
        initComponents();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length != 1) {
                selectPlatform(null);
            } else {
                selectPlatform(nodes[0]);
                Dialog dialog = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, this);
                if (dialog != null) {
                    dialog.pack();
                }
            }
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length > 1) {
                throw new PropertyVetoException("Invalid length", evt);   //NOI18N
            }
        }
    }

    @Override
    public synchronized ExplorerManager getExplorerManager() {
        if (this.manager == null) {
            this.manager = new ExplorerManager();
            this.manager.setRootContext(new AbstractNode(getChildren()));
            this.manager.addPropertyChangeListener(this);
            this.manager.addVetoableChangeListener(this);
        }
        return manager;
    }

    public @Override
    void addNotify() {
        super.addNotify();
        this.expandPlatforms(this.initialPlatform);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        platforms = new PlatformsView ();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        cards = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        platformName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        platformHome = new javax.swing.JTextField();
        clientArea = new javax.swing.JPanel();
        messageArea = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        mkDefault = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel4.setText(org.openide.util.NbBundle.getMessage(SdksCustomizer.class, "TXT_PlatformsHint")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 12, 6);
        add(platforms, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/nbandroid/netbeans/gradle/v2/sdk/ui/Bundle"); // NOI18N
        platforms.getAccessibleContext().setAccessibleName(bundle.getString("AN_PlatformsCustomizerPlatforms")); // NOI18N
        platforms.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_PlatformsCustomizerPlatforms")); // NOI18N

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/nbandroid/netbeans/gradle/v2/sdk/ui/Bundle").getString("MNE_AddPlatform").charAt(0));
        addButton.setText(bundle.getString("CTL_AddPlatform")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewPlatform(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 6);
        add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_AddPlatform")); // NOI18N

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("org/nbandroid/netbeans/gradle/v2/sdk/ui/Bundle").getString("MNE_Remove").charAt(0));
        removeButton.setText(bundle.getString("CTL_Remove")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePlatform(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_Remove")); // NOI18N

        cards.setLayout(new java.awt.CardLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/nbandroid/netbeans/gradle/v2/sdk/ui/Bundle").getString("MNE_PlatformName").charAt(0));
        jLabel1.setLabelFor(platformName);
        jLabel1.setText(bundle.getString("CTL_PlatformName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jLabel1, gridBagConstraints);

        platformName.setEditable(false);
        platformName.setColumns(25);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(platformName, gridBagConstraints);
        platformName.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_PlatformName")); // NOI18N

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/nbandroid/netbeans/gradle/v2/sdk/ui/Bundle").getString("MNE_PlatformHome").charAt(0));
        jLabel2.setLabelFor(platformHome);
        jLabel2.setText(bundle.getString("CTL_PlatformHome")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 12, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        platformHome.setEditable(false);
        platformHome.setColumns(25);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 0);
        jPanel1.add(platformHome, gridBagConstraints);
        platformHome.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_PlatformHome")); // NOI18N

        clientArea.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(clientArea, gridBagConstraints);

        cards.add(jPanel1, "card2");

        messageArea.setLayout(new java.awt.GridBagLayout());
        cards.add(messageArea, "card3");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 12, 12);
        add(cards, gridBagConstraints);

        jLabel3.setLabelFor(platforms);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("TXT_PlatformsList")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jLabel3, gridBagConstraints);

        mkDefault.setMnemonic('D');
        mkDefault.setText("Default");
        mkDefault.setEnabled(false);
        mkDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mkDefaultActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(mkDefault, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_PlatformsCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void removePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePlatform
        Node[] nodes = getExplorerManager().getSelectedNodes();
        if (nodes.length != 1) {
            return;
        }
        DataObject dobj = nodes[0].getLookup().lookup(DataObject.class);
        if (dobj == null) {
            assert false : "Can not find platform definition for node: " + nodes[0].getDisplayName();      //NOI18N
            return;
        }
        try {
            dobj.delete();
            this.getChildren().refreshPlatforms();
            this.expandPlatforms(null);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }//GEN-LAST:event_removePlatform

    private void addNewPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewPlatform
        final SdksCustomizer self = this;
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {

//                try {
                WizardDescriptor wiz = new WizardDescriptor(new SDKWizardIterator());
//                    final FileObject templateFo = FileUtil.getConfigFile(TEMPLATE);
//                    if (templateFo == null) {
//                        final StringBuilder sb = new StringBuilder("Broken system filesystem: ");   //NOI18N
//                        final String[] parts = TEMPLATE.split("/");   //NOI18N
//                        FileObject f = FileUtil.getConfigRoot();
//                        for (int i = 0; f != null && i < parts.length; i++) {
//                            sb.append(f.getName()).append('/'); //NOI18N;
//                            f = f.getFileObject(parts[i]);
//                        }
//                        throw new IllegalStateException(sb.toString());
//                    }
//                    DataObject template = DataObject.find (templateFo);
//                    wiz.putProperty("targetTemplate", template);    //NOI18N
//                    DataFolder folder = DataFolder.findFolder(FileUtil.getConfigFile(STORAGE));
//                    wiz.putProperty("targetFolder",folder); //NOI18N
                wiz.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
                wiz.setTitle(NbBundle.getMessage(SdksCustomizer.class, "CTL_AddPlatformTitle"));
                wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
                Dialog dlg = DialogDisplayer.getDefault().createDialog(wiz);
                try {
                    dlg.setVisible(true);
                    if (wiz.getValue() == WizardDescriptor.FINISH_OPTION) {
                        self.getChildren().refreshPlatforms();
                        Set result = wiz.getInstantiatedObjects();
                        self.expandPlatforms(result.isEmpty() ? null : (AndroidSdkImpl) result.iterator().next());
                    }
                } finally {
                    dlg.dispose();
                    wiz.getInstantiatedObjects();
                }
//                } catch (DataObjectNotFoundException dfne) {
//                    Exceptions.printStackTrace(dfne);
//                }
//                catch (IOException ioe) {
//                    Exceptions.printStackTrace(ioe);
//                }
            }
        });
    }//GEN-LAST:event_addNewPlatform

    private void mkDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mkDefaultActionPerformed
        for (AndroidSdkImpl plt : platformsList) {
            if (plt.isDefaultSdk()) {
                plt.setDefault(false);
            }
        }
        if (selectedPlatform != null) {
            selectedPlatform.setDefault(true);
        }
        this.getChildren().refreshPlatforms();
    }//GEN-LAST:event_mkDefaultActionPerformed

    private synchronized PlatformCategoriesChildren getChildren() {
        if (this.children == null) {
            this.children = new PlatformCategoriesChildren();
        }
        return this.children;
    }

    private void selectPlatform(Node pNode) {
        Component active = null;
        for (Component c : cards.getComponents()) {
            if (c.isVisible()
                    && (c == jPanel1 || c == messageArea)) {
                active = c;
                break;
            }
        }
        final Dimension lastSize = active == null
                ? null
                : active.getSize();
        this.clientArea.removeAll();
        this.messageArea.removeAll();
        this.removeButton.setEnabled(false);
        if (pNode == null) {
            ((CardLayout) cards.getLayout()).last(cards);
            return;
        }
        JComponent target = messageArea;
        JComponent owner = messageArea;
        selectedPlatform = pNode.getLookup().lookup(AndroidSdkImpl.class);
        if (pNode != getExplorerManager().getRootContext()) {
            if (selectedPlatform != null) {
                mkDefault.setEnabled(!selectedPlatform.isDefaultSdk());
                this.removeButton.setEnabled(!selectedPlatform.isDefaultSdk());
                if (!selectedPlatform.getInstallFolders().isEmpty()) {
                    this.platformName.setText(pNode.getDisplayName());
                    for (FileObject installFolder : selectedPlatform.getInstallFolders()) {
                        File file = FileUtil.toFile(installFolder);
                        if (file != null) {
                            this.platformHome.setText(file.getAbsolutePath());
                        }
                    }
                    target = clientArea;
                    owner = jPanel1;
                }
            } else {
                removeButton.setEnabled(false);
                mkDefault.setEnabled(false);
            }
            Component component = null;
            if (pNode.hasCustomizer()) {
                component = pNode.getCustomizer();
            }
            if (component == null) {
                final PropertySheet sp = new PropertySheet();
                sp.setNodes(new Node[]{pNode});
                component = sp;
            }
            addComponent(target, component);
        }
        if (lastSize != null) {
            final Dimension newSize = owner.getPreferredSize();
            final Dimension updatedSize = new Dimension(
                    Math.max(lastSize.width, newSize.width),
                    Math.max(lastSize.height, newSize.height));
            if (!newSize.equals(updatedSize)) {
                owner.setPreferredSize(updatedSize);
            }
        }
        target.revalidate();
        CardLayout cl = (CardLayout) cards.getLayout();
        if (target == clientArea) {
            cl.first(cards);
        } else {
            cl.last(cards);
        }
    }

    private static void addComponent(Container container, Component component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = GridBagConstraints.RELATIVE;
        c.gridheight = c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = c.weighty = 1.0;
        ((GridBagLayout) container.getLayout()).setConstraints(component, c);
        container.add(component);
    }

    private boolean canRemove(final AndroidSdkImpl platform, final DataObject dobj) {
        if (isDefaultPlatform(platform)) {
            return false;
        }
        if (dobj != null) {
            final FileObject fo = dobj.getPrimaryFile();
            Object attr = fo.getAttribute(ATTR_CAN_REMOVE);  //NOI18N
            if (attr instanceof Boolean && ((Boolean) attr) == Boolean.FALSE) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDefaultPlatform(AndroidSdkImpl platform) {
        return platform.isDefaultSdk();
    }

    private void expandPlatforms(AndroidSdkImpl platform) {
        ExplorerManager mgr = this.getExplorerManager();
        Node node = mgr.getRootContext();
        expandAllNodes(this.platforms, node, mgr, platform);
    }

    private static void expandAllNodes(BeanTreeView btv, Node node, ExplorerManager mgr, AndroidSdkImpl platform) {
        btv.expandNode(node);
        Children ch = node.getChildren();
        if (ch == Children.LEAF) {
            if (platform != null && platform.equals(node.getLookup().lookup(AndroidSdkImpl.class))) {
                try {
                    mgr.setSelectedNodes(new Node[]{node});
                } catch (PropertyVetoException e) {
                    //Ignore it
                }
            }
            return;
        }
        Node nodes[] = ch.getNodes(true);
        for (int i = 0; i < nodes.length; i++) {
            expandAllNodes(btv, nodes[i], mgr, platform);
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel cards;
    private javax.swing.JPanel clientArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel messageArea;
    private javax.swing.JButton mkDefault;
    private javax.swing.JTextField platformHome;
    private javax.swing.JTextField platformName;
    private org.openide.explorer.view.BeanTreeView platforms;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    private static class PlatformsView extends BeanTreeView {

        public PlatformsView() {
            super();
            this.setPopupAllowed(false);
            this.setDefaultActionAllowed(false);
            this.setRootVisible(false);
            this.tree.setEditable(false);
            this.tree.setShowsRootHandles(false);
            this.setBorder(UIManager.getBorder("Nb.ScrollPane.border")); // NOI18N
            setPreferredSize(new java.awt.Dimension(200, 334));
        }

    }

    private static class PlatformCategoriesDescriptor implements Comparable<PlatformCategoriesDescriptor> {

        private final String categoryName;
        private final List<Node> platforms;
        private boolean changed = false;

        public PlatformCategoriesDescriptor(String categoryName) {
            assert categoryName != null;
            this.categoryName = categoryName;
            this.platforms = new ArrayList<Node>();
        }

        public String getName() {
            return this.categoryName;
        }

        public List<Node> getPlatform() {
            if (changed) {
                //SortedSet can't be used, there can be platforms with the same
                //display name
                Collections.sort(platforms, new PlatformNodeComparator());
                changed = false;
            }
            return Collections.unmodifiableList(this.platforms);
        }

        public void add(Node node) {
            this.platforms.add(node);
            this.changed = true;
        }

        public @Override
        int hashCode() {
            return this.categoryName.hashCode();
        }

        public @Override
        boolean equals(Object other) {
            if (other instanceof PlatformCategoriesDescriptor) {
                PlatformCategoriesDescriptor desc = (PlatformCategoriesDescriptor) other;
                return this.categoryName.equals(desc.categoryName)
                        && this.platforms.size() == desc.platforms.size();
            }
            return false;
        }

        @Override
        public int compareTo(PlatformCategoriesDescriptor desc) {
            return this.categoryName.compareTo(desc.categoryName);
        }

    }

    private static class PlatformsChildren extends Children.Keys<Node> {

        private final List<Node> platforms;

        public PlatformsChildren(List<Node> platforms) {
            this.platforms = platforms;
        }

        protected @Override
        void addNotify() {
            super.addNotify();
            this.setKeys(this.platforms);
        }

        protected @Override
        void removeNotify() {
            super.removeNotify();
            this.setKeys(new Node[0]);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[]{new FilterNode(key, new FilterNode.Children(key))};
        }
    }

    private static class PlatformCategoryNode extends AbstractNode {

        private final PlatformCategoriesDescriptor desc;
        private final Node iconDelegate;

        public PlatformCategoryNode(PlatformCategoriesDescriptor desc) {
            super(new PlatformsChildren(desc.getPlatform()));
            this.desc = desc;
            this.iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        }

        public @Override
        String getName() {
            return this.desc.getName();
        }

        public @Override
        String getDisplayName() {
            return this.getName();
        }

        public @Override
        Image getIcon(int type) {
            return this.iconDelegate.getIcon(type);
        }

        public @Override
        Image getOpenedIcon(int type) {
            return this.iconDelegate.getOpenedIcon(type);
        }

        @Override
        public boolean hasCustomizer() {
            return true;
        }

        @Override
        public Component getCustomizer() {
            return new JPanel();
        }
    }

    private static class PlatformCategoriesChildren extends Children.Keys<PlatformCategoriesDescriptor> {

        protected @Override
        void addNotify() {
            super.addNotify();
            this.refreshPlatforms();
        }

        protected @Override
        void removeNotify() {
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes(PlatformCategoriesDescriptor key) {
            return new Node[]{new PlatformCategoryNode(key)};
        }

        private void refreshPlatforms() {
            platformsList.clear();
            FileObject storage = FileUtil.getConfigFile(STORAGE);
            if (storage != null) {
                java.util.Map<String, PlatformCategoriesDescriptor> categories = new HashMap<String, PlatformCategoriesDescriptor>();
                for (FileObject child : storage.getChildren()) {
                    try {
                        final DataObject dobj = DataObject.find(child);
                        Node node = dobj.getNodeDelegate();
                        AndroidSdkImpl platform = node.getLookup().lookup(AndroidSdkImpl.class);
                        if (platform != null) {
                            platformsList.add(platform);
                            final String platformType = "Android";
                            final String platformTypeDisplayName = "Android";
                            PlatformCategoriesDescriptor platforms = categories.get(platformType);
                            if (platforms == null) {
                                platforms = new PlatformCategoriesDescriptor(platformTypeDisplayName);
                                categories.put(platformType, platforms);
                            }
                            platforms.add(node);
                        } else {
                            LOG.log(Level.WARNING, "Platform node for : {0} has no platform in its lookup.", node.getDisplayName());   //NOI18N
                        }
                    } catch (DataObjectNotFoundException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
                List<PlatformCategoriesDescriptor> keys = new ArrayList<SdksCustomizer.PlatformCategoriesDescriptor>(categories.values());
                Collections.sort(keys);
                setKeys(keys);
            }
        }

    }

    private static class PlatformNodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node n1, Node n2) {
            return n1.getDisplayName().compareTo(n2.getDisplayName());
        }
    }

}
