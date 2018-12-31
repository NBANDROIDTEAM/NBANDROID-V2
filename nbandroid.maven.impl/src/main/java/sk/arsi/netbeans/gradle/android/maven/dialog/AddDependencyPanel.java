/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sk.arsi.netbeans.gradle.android.maven.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import sk.arsi.netbeans.gradle.android.maven.MavenDependencyInfo;
import sk.arsi.netbeans.gradle.android.maven.MavenSearchProvider;
import sk.arsi.netbeans.gradle.android.maven.RepoSearchListener;
import sk.arsi.netbeans.gradle.android.maven.repository.Repository;

/**
 * @author mkleint
 * @author arsi NBANDROID MOD
 */
public class AddDependencyPanel extends javax.swing.JPanel {

    private static final Object LOCK = new Object();
    private static AbstractNode searchingNode;
    private static AbstractNode tooGeneralNode;
    private static AbstractNode noResultsNode;
    private NotificationLineSupport nls;
    private static final @StaticResource
    String EMPTY_ICON = "sk/arsi/netbeans/gradle/android/maven/dialog/empty.png";
    private static final @StaticResource
    String WAIT_ICON = "sk/arsi/netbeans/gradle/android/maven/dialog/wait.gif";
    private static final @StaticResource
    String MAVEN_ICON = "sk/arsi/netbeans/gradle/android/maven/dialog/maven.png";
    private static final @StaticResource
    String JFROG_ICON = "sk/arsi/netbeans/gradle/android/maven/dialog/jfrog.png";
    private static final @StaticResource
    String GOOGLE_ICON = "sk/arsi/netbeans/gradle/android/maven/dialog/google.png";
    private static final @StaticResource
    String PACKAGE_ICON = "sk/arsi/netbeans/gradle/android/maven/dialog/package.png";

    private static final RequestProcessor RPofQueryPanel = new RequestProcessor(AddDependencyPanel.QueryPanel.class.getName() + "-Search", 10);
    private static final RequestProcessor UPDATE_PROCESSOR = new RequestProcessor(AddDependencyPanel.QueryPanel.class.getName() + "-Node-Update", 1);
    private QueryPanel queryPanel;
    private static final AtomicLong searchId = new AtomicLong(0);
    private final List<Repository> repositories;
    private final List<String> currentPackages;
    private final List<MavenDependencyInfo> googleDependenciesInfos = new ArrayList<>();
    private final List<MavenDependencyInfo> jcenterDependenciesInfos = new ArrayList<>();
    private final List<MavenDependencyInfo> mavenDependenciesInfos = new ArrayList<>();
    private final List<MavenDependencyInfo> installedDependenciesInfos = new ArrayList<>();
    private boolean jcenterPartial = true;
    private boolean googlePartial = true;
    private boolean mavenPartial = true;
    private String selected = null;

    /**
     * Creates new form AddDependenCy
     */
    public AddDependencyPanel(List<Repository> repositories, List<String> currentPackages) {
        initComponents();
        this.repositories = repositories;
        this.currentPackages = currentPackages;
        for (String currentPackage : currentPackages) {
            StringTokenizer tok = new StringTokenizer(currentPackage, ":", false);
            if (tok.countTokens() > 1) {
                String grp = tok.nextToken();
                String packg = tok.nextToken();
                installedDependenciesInfos.add(new MavenDependencyInfo(MavenDependencyInfo.Type.MAVEN, grp, packg));
                installedDependenciesInfos.add(new MavenDependencyInfo(MavenDependencyInfo.Type.GOOGLE, grp, packg));
                installedDependenciesInfos.add(new MavenDependencyInfo(MavenDependencyInfo.Type.JCENTER, grp, packg));
            }
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                queryPanel = new QueryPanel();
                resultsPanel.add(queryPanel, BorderLayout.CENTER);
                searchField.getDocument().addDocumentListener(
                        DelayedDocumentChangeListener.create(
                                searchField.getDocument(), queryPanel, 1500));
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public AddDependencyPanel(List<Repository> repositories, List<String> currentPackages, List<MavenDependencyInfo> index) {
        initComponents();
        this.repositories = repositories;
        this.currentPackages = currentPackages;
        for (String currentPackage : currentPackages) {
            StringTokenizer tok = new StringTokenizer(currentPackage, ":", false);
            if (tok.countTokens() > 1) {
                String grp = tok.nextToken();
                String packg = tok.nextToken();
                installedDependenciesInfos.add(new MavenDependencyInfo(MavenDependencyInfo.Type.MAVEN, grp, packg));
                installedDependenciesInfos.add(new MavenDependencyInfo(MavenDependencyInfo.Type.GOOGLE, grp, packg));
                installedDependenciesInfos.add(new MavenDependencyInfo(MavenDependencyInfo.Type.JCENTER, grp, packg));
            }
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                queryPanel = new QueryPanel();
                resultsPanel.add(queryPanel, BorderLayout.CENTER);
                labelQ.setVisible(false);
                searchField.setVisible(false);
                labelHelp.setVisible(false);
                googleDependenciesInfos.addAll(index);
                jcenterPartial = false;
                googlePartial = false;
                mavenPartial = false;
                queryPanel.updateResults();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public String getSelected() {
        return selected;
    }

    public void attachDialogDisplayer(DialogDescriptor dd) {
        nls = dd.getNotificationLineSupport();
        if (nls == null) {
            nls = dd.createNotificationLineSupport();
        }
    }

    public JButton getOkButton() {
        return ok;
    }

    public JButton getCancelButton() {
        return cancel;
    }

    private void changeSelection(Lookup lookup) {
        MavenDependencyInfo dependencyInfo = lookup.lookup(MavenDependencyInfo.class);
        MavenDependencyInfo.Version version = lookup.lookup(MavenDependencyInfo.Version.class);
        if (version != null) {
            labelGroup.setText(version.getGroupId());
            labelArtifact.setText(version.getArtifactId());
            labelVersion.setText(version.getVersion());
            selected = version.getGradleLine();
        } else if (dependencyInfo != null) {
            labelGroup.setText(dependencyInfo.getGroupId());
            labelArtifact.setText(dependencyInfo.getArtifactId());
            labelVersion.setText("+");
            selected = dependencyInfo.getGradleLine() + ":+";
        } else {
            labelGroup.setText("...");
            labelArtifact.setText("...");
            labelVersion.setText("...");
            selected = null;
        }
        ok.setEnabled(selected != null);

    }

    @Override
    public void addNotify() {
        super.addNotify();
        assert nls != null : " The notificationLineSupport was not attached to the panel."; //NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelGroup = new javax.swing.JLabel();
        labelArtifact = new javax.swing.JLabel();
        labelVersion = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        labelQ = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        labelHelp = new javax.swing.JLabel();
        resultsLabel = new javax.swing.JLabel();
        resultsPanel = new javax.swing.JPanel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel3.text")); // NOI18N

        labelGroup.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(labelGroup, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.labelGroup.text")); // NOI18N

        labelArtifact.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(labelArtifact, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.labelArtifact.text")); // NOI18N

        labelVersion.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(labelVersion, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.labelVersion.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelQ, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.labelQ.text")); // NOI18N

        searchField.setText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchField.text")); // NOI18N

        labelHelp.setForeground(new java.awt.Color(153, 153, 153));
        org.openide.awt.Mnemonics.setLocalizedText(labelHelp, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.labelHelp.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultsLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.resultsLabel.text")); // NOI18N

        resultsPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(ok, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.ok.text")); // NOI18N
        ok.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(cancel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.cancel.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelQ)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(labelHelp)
                                .addGap(0, 477, Short.MAX_VALUE))
                            .addComponent(searchField)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(resultsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(ok)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancel)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelQ)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelHelp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resultsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ok)
                    .addComponent(cancel))
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelVersion)
                    .addComponent(labelArtifact)
                    .addComponent(labelGroup))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(labelGroup))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelArtifact))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(labelVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelArtifact;
    private javax.swing.JLabel labelGroup;
    private javax.swing.JLabel labelHelp;
    private javax.swing.JLabel labelQ;
    private javax.swing.JLabel labelVersion;
    private javax.swing.JButton ok;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables

    private class QueryPanel extends JPanel implements ExplorerManager.Provider,
            Comparator<MavenDependencyInfo>, PropertyChangeListener, ChangeListener, RepoSearchListener {

        private final BeanTreeView btv;
        private final ExplorerManager manager;
        private final ResultsRootNode resultsRootNode;

        private String inProgressText, lastQueryText, curTypedText;

        private final Color defSearchC;

        private QueryPanel() {
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(true);
            btv.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            manager = new ExplorerManager();
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);
            defSearchC = AddDependencyPanel.this.searchField.getForeground();
            manager.addPropertyChangeListener(this);
            AddDependencyPanel.this.resultsLabel.setLabelFor(btv);
            btv.getAccessibleContext().setAccessibleDescription(AddDependencyPanel.this.resultsLabel.getAccessibleContext().getAccessibleDescription());
            resultsRootNode = new ResultsRootNode();
            manager.setRootContext(resultsRootNode);
        }

        /**
         * delayed change of query text
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            Document doc = (Document) e.getSource();
            try {
                curTypedText = doc.getText(0, doc.getLength()).trim();
            } catch (BadLocationException ex) {
                // should never happen, nothing we can do probably
                return;
            }

            AddDependencyPanel.this.searchField.setForeground(defSearchC);

            if (curTypedText.length() > 0) {
                find(curTypedText);
            }
        }


        void find(String queryText) {
            synchronized (LOCK) {
                changeSelection(Lookup.EMPTY);
                if (queryText.equals(lastQueryText)) {
                    return;
                }
                lastQueryText = queryText;
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    resultsRootNode.setOneChild(getSearchingNode());
                    AddDependencyPanel.this.searchField.setForeground(defSearchC);
                    AddDependencyPanel.this.nls.clearMessages();
                }
            });
            jcenterPartial = true;
            mavenPartial = true;
            googlePartial = true;
            long currentSearchId = searchId.incrementAndGet();
            Collection<? extends MavenSearchProvider> providers = Lookup.getDefault().lookupAll(MavenSearchProvider.class);
            for (MavenSearchProvider next : providers) {
                next.searchPackageName(queryText, "jcenter", WeakListeners.create(RepoSearchListener.class, (RepoSearchListener) this, null), currentSearchId, repositories);
            }
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        void updateResults() {
            final List<MavenDependencyInfo> dependencyInfos = new ArrayList<>();
            dependencyInfos.addAll(googleDependenciesInfos);
            dependencyInfos.addAll(jcenterDependenciesInfos);
            dependencyInfos.addAll(mavenDependenciesInfos);
            dependencyInfos.removeAll(installedDependenciesInfos);
            Collections.sort(dependencyInfos, QueryPanel.this);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateResultNodes(dependencyInfos);
                }
            });
        }

        private void updateResultNodes(List<MavenDependencyInfo> dependencyInfos) {

            if (dependencyInfos.size() > 0) { // some results available

                Map<MavenDependencyInfo, Node> currentNodes = new HashMap<>();
                for (Node nd : resultsRootNode.getChildren().getNodes()) {
                    currentNodes.put(nd.getLookup().lookup(MavenDependencyInfo.class), nd);
                }
                List<Node> newNodes = new ArrayList<Node>(dependencyInfos.size());

                // still searching?
                if (mavenPartial || jcenterPartial || googlePartial) {
                    newNodes.add(getSearchingNode());
                }

                for (MavenDependencyInfo key : dependencyInfos) {
                    Node nd;
                    nd = currentNodes.get(key);
                    if (null != nd) {
                        ((ArtifactNode) ((AddDependencyPanel.FilterNodeWithDefAction) nd).getOriginal()).setVersionInfos(key.getVersions());
                    } else {
                        nd = createFilterWithDefaultAction(new ArtifactNode(key), false);
                    }
                    newNodes.add(nd);
                }

                resultsRootNode.setNewChildren(newNodes);
            } else if (googlePartial || jcenterPartial || mavenPartial) { // still searching, no results yet
                resultsRootNode.setOneChild(getSearchingNode());
            } else { // finished searching with no results
                resultsRootNode.setOneChild(getNoResultsNode());
            }
        }

        /**
         * Impl of comparator, sorts artifacts asfabetically with exception of
         * items that contain current query string, which take precedence.
         */
        @Override
        public int compare(MavenDependencyInfo s1, MavenDependencyInfo s2) {

            return s1.getGradleLine().compareTo(s2.getGradleLine());
        }

        /**
         * PropertyChangeListener impl, stores maven coordinates of selected
         * artifact
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selNodes = manager.getSelectedNodes();
                changeSelection(selNodes.length == 1 ? selNodes[0].getLookup() : Lookup.EMPTY);
            }
        }

        private String key(NBVersionInfo nbvi) {
            return nbvi.getGroupId() + ':' + nbvi.getArtifactId() + ':' + nbvi.getVersion();
        }

        @Override
        public void searchDone(Type type, long searchId, boolean isPartial, List<MavenDependencyInfo> dependencyInfos) {
            //handle only last search
            if (searchId == AddDependencyPanel.searchId.get()) {
                switch (type) {
                    case GOOGLE:
                        googleDependenciesInfos.clear();
                        googleDependenciesInfos.addAll(dependencyInfos);
                        googlePartial = isPartial;
                        break;
                    case JCENTER:
                        jcenterDependenciesInfos.clear();
                        jcenterDependenciesInfos.addAll(dependencyInfos);
                        jcenterPartial = isPartial;
                        break;
                    case MAVEN:
                        mavenDependenciesInfos.clear();
                        mavenDependenciesInfos.addAll(dependencyInfos);
                        mavenPartial = isPartial;
                        break;
                    default:
                        throw new AssertionError(type.name());

                }
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        updateResults();
                    }
                };
                UPDATE_PROCESSOR.execute(runnable);
            }
        }

    } // QueryPanel

    public static class ArtifactNode extends AbstractNode {

        private final MavenDependencyInfo dependencyInfo;
        private List<MavenDependencyInfo.Version> versionInfos;
        private final ArtifactNodeChildren myChildren;

        public ArtifactNode(MavenDependencyInfo dependencyInfo) {
            super(new ArtifactNodeChildren(dependencyInfo.getVersions()), Lookups.fixed(dependencyInfo));
            myChildren = (ArtifactNodeChildren) getChildren();
            this.versionInfos = dependencyInfo.getVersions();
            this.dependencyInfo = dependencyInfo;
            setName(dependencyInfo.getGradleLine());
            setDisplayName(dependencyInfo.getGradleLine());
        }

        @Override
        public Image getIcon(int type) {
            switch(dependencyInfo.getType()){
                case GOOGLE:
                    return ImageUtilities.loadImage(GOOGLE_ICON); //NOI18N
                case JCENTER:
                    return ImageUtilities.loadImage(JFROG_ICON); //NOI18N
                case MAVEN:
                    return ImageUtilities.loadImage(MAVEN_ICON); //NOI18N
                default:
                    return super.getIcon(type);
            }
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }




        public List<MavenDependencyInfo.Version> getVersionInfos() {
            return new ArrayList<>(versionInfos);
        }

        public void setVersionInfos(List<MavenDependencyInfo.Version> versions) {
            versionInfos = versions;
            myChildren.setNewKeys(versions);
        }

        static class ArtifactNodeChildren extends Children.Keys<MavenDependencyInfo.Version> {

            private List<MavenDependencyInfo.Version> keys;

            public ArtifactNodeChildren(List<MavenDependencyInfo.Version> keys) {
                this.keys = keys;
            }

            @Override
            protected Node[] createNodes(MavenDependencyInfo.Version arg0) {
                return new Node[]{new VersionNode(arg0)};
            }

            @Override
            protected void addNotify() {
                setKeys(keys);
            }

            protected void setNewKeys(List<MavenDependencyInfo.Version> keys) {
                this.keys = keys;
                setKeys(keys);
            }
        }
    }

    public static class VersionNode extends AbstractNode {

        private final MavenDependencyInfo.Version nbvi;

        /**
         * Creates a new instance of VersionNode
         */
        public VersionNode(MavenDependencyInfo.Version version) {
            super(Children.LEAF, Lookups.fixed(version));

            this.nbvi = version;

            setName(version.getVersion());
            setDisplayName(version.getVersion());
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(PACKAGE_ICON); //NOI18N
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        public MavenDependencyInfo.Version getVersionInfo() {
            return nbvi;
        }

        @Override
        public String getShortDescription() {
            return nbvi.toString();
        }
    }

    private class ResultsRootNode extends AbstractNode {

        private ResultsRootChildren resultsChildren;

        public ResultsRootNode() {
            this(new InstanceContent());
        }

        private ResultsRootNode(InstanceContent content) {
            super(new ResultsRootChildren(), new AbstractLookup(content));
            content.add(this);
            this.resultsChildren = (ResultsRootChildren) getChildren();
        }

        public void setOneChild(Node n) {
            List<Node> ch = new ArrayList<Node>(1);
            ch.add(n);
            setNewChildren(ch);
        }

        public void setNewChildren(List<Node> ch) {
            resultsChildren.setNewChildren(ch);
        }
    }

    private class ResultsRootChildren extends Children.Keys<Node> {

        List<Node> myNodes;

        public ResultsRootChildren() {
            myNodes = Collections.EMPTY_LIST;
        }

        private void setNewChildren(List<Node> ch) {
            myNodes = ch;
            refreshList();
        }

        @Override
        protected void addNotify() {
            refreshList();
        }

        private void refreshList() {
            List<Node> keys = new ArrayList();
            for (Node node : myNodes) {
                keys.add(node);
            }
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[]{key};
        }

    }

    private static Node getSearchingNode() {
        if (searchingNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(WAIT_ICON); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Searching"); //NOI18N

            nd.setDisplayName("Searching.."); //NOI18N

            searchingNode = nd;
        }

        return new FilterNode(searchingNode, Children.LEAF);
    }

    private static Node getTooGeneralNode() {
        if (tooGeneralNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_ICON); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Too General"); //NOI18N

            nd.setDisplayName("Too General"); //NOI18N

            tooGeneralNode = nd;
        }

        return new FilterNode(tooGeneralNode, Children.LEAF);
    }

    private static Node getNoResultsNode() {
        if (noResultsNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_ICON); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Empty"); //NOI18N

            nd.setDisplayName("Empty"); //NOI18N

            noResultsNode = nd;
        }

        return new FilterNode(noResultsNode, Children.LEAF);
    }

    private Node createFilterWithDefaultAction(final Node nd, boolean leaf) {
        return new FilterNodeWithDefAction(nd, leaf);
    }

    class FilterNodeWithDefAction extends FilterNode {

        public FilterNodeWithDefAction(Node nd, boolean leaf) {
            super(nd, leaf ? Children.LEAF : new FilterNode.Children(nd) {
                @Override
                protected Node[] createNodes(Node key) {
                    return new Node[]{createFilterWithDefaultAction(key, true)};
                }
            });
        }

        @Override
        public Action getPreferredAction() {
            return super.getPreferredAction();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

        @Override
        public Node getOriginal() {
            return super.getOriginal();
        }
    }
}
