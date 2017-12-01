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
package org.nbandroid.netbeans.gradle.v2.sdk.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkImpl;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class AndroidPlatformCustomizer extends javax.swing.JPanel {

    private AndroidPlatformInfo aPackage;
    private AndroidSdkImpl sdk;
    private final List<ListDataListener> classListeners = new ArrayList<>();
    private final List<ListDataListener> sourceListeners = new ArrayList<>();
    private final List<ListDataListener> javadocListeners = new ArrayList<>();
    private final ClassListSelectionListener classListSelectionListener = new ClassListSelectionListener();
    private final SourceListSelectionListener sourceListSelectionListener = new SourceListSelectionListener();
    private final JavadocListSelectionListener javadocListSelectionListener = new JavadocListSelectionListener();

    /**
     * Creates new form PlatformCustomizer
     */
    public AndroidPlatformCustomizer() {
        initComponents();

    }

    public AndroidPlatformCustomizer(AndroidPlatformInfo aPackage, AndroidSdkImpl sdk) {
        this();
        this.aPackage = aPackage;
        this.sdk = sdk;
        classPathList.setModel(new ClassPathModel());
        sourcesList.setModel(new SourcePathModel());
        javadocList.setModel(new JavadocPathModel());
        classPathList.getSelectionModel().addListSelectionListener(WeakListeners.create(ListSelectionListener.class, classListSelectionListener, classPathList.getSelectionModel()));
        sourcesList.getSelectionModel().addListSelectionListener(WeakListeners.create(ListSelectionListener.class, sourceListSelectionListener, sourcesList.getSelectionModel()));
        javadocList.getSelectionModel().addListSelectionListener(WeakListeners.create(ListSelectionListener.class, javadocListSelectionListener, javadocList.getSelectionModel()));

    }

    private class ClassListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                AndroidPlatformInfo.PathRecord selectedValue = (AndroidPlatformInfo.PathRecord) classPathList.getSelectedValue();
                removeClasspath.setEnabled(selectedValue == null ? false : selectedValue.isUserRecord());
            }
        }

    }

    private class SourceListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                AndroidPlatformInfo.PathRecord selectedValue1 = (AndroidPlatformInfo.PathRecord) sourcesList.getSelectedValue();
                removeSources.setEnabled(selectedValue1 == null ? false : selectedValue1.isUserRecord());
            }
        }

    }

    private class JavadocListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                AndroidPlatformInfo.PathRecord selectedValue2 = (AndroidPlatformInfo.PathRecord) javadocList.getSelectedValue();
                removeJavadoc.setEnabled(selectedValue2 == null ? false : selectedValue2.isUserRecord());
            }
        }

    }

    private class ClassPathModel implements ListModel<AndroidPlatformInfo.PathRecord> {

        @Override
        public int getSize() {
            return aPackage.getBootPaths().size();
        }

        @Override
        public AndroidPlatformInfo.PathRecord getElementAt(int index) {
            return aPackage.getBootPaths().get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            classListeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            classListeners.remove(l);
        }

    }

    private class SourcePathModel implements ListModel<AndroidPlatformInfo.PathRecord> {

        @Override
        public int getSize() {
            return aPackage.getSrcPaths().size();
        }

        @Override
        public AndroidPlatformInfo.PathRecord getElementAt(int index) {
            return aPackage.getSrcPaths().get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            sourceListeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            sourceListeners.remove(l);
        }

    }

    private class JavadocPathModel implements ListModel<AndroidPlatformInfo.PathRecord> {

        @Override
        public int getSize() {
            return aPackage.getJavadocPaths().size();
        }

        @Override
        public AndroidPlatformInfo.PathRecord getElementAt(int index) {
            return aPackage.getJavadocPaths().get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            javadocListeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            javadocListeners.remove(l);
        }

    }

    private static class ArchiveFileFilter extends FileFilter {

        private final String description;
        private final Collection extensions;

        public ArchiveFileFilter(String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index == name.length() - 1) {
                return false;
            }
            String extension = name.substring(index + 1).toUpperCase();
            if (!this.extensions.contains(extension)) {
                return false;
            }
            try {
                return FileUtil.isArchiveFile(Utilities.toURI(f).toURL());
            } catch (MalformedURLException e) {
                Exceptions.printStackTrace(e);
                return false;
            }
        }

        @Override
        public String getDescription() {
            return this.description;
        }
    }

    private void fireClassPathAdded() {
        for (ListDataListener listener : classListeners) {
            listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, aPackage.getBootPaths().size() - 2, aPackage.getBootPaths().size() - 1));
        }
    }

    private void fireClassPathRemoved() {
        for (ListDataListener listener : classListeners) {
            listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, aPackage.getBootPaths().size() - 1, aPackage.getBootPaths().size() - 1));
        }
    }

    private void fireSourcesAdded() {
        for (ListDataListener listener : sourceListeners) {
            listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, aPackage.getSrcPaths().size() - 2, aPackage.getSrcPaths().size() - 1));
        }
    }

    private void fireSourcesRemoved() {
        for (ListDataListener listener : sourceListeners) {
            listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, aPackage.getSrcPaths().size() - 1, aPackage.getSrcPaths().size() - 1));
        }
    }

    private void fireJavadocAdded() {
        for (ListDataListener listener : javadocListeners) {
            listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, aPackage.getJavadocPaths().size() - 2, aPackage.getJavadocPaths().size() - 1));
        }
    }

    private void fireJavadocRemoved() {
        for (ListDataListener listener : javadocListeners) {
            listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, aPackage.getJavadocPaths().size() - 1, aPackage.getJavadocPaths().size() - 1));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        classPathList = new javax.swing.JList();
        addClasspath = new javax.swing.JButton();
        removeClasspath = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        sourcesList = new javax.swing.JList();
        addSources = new javax.swing.JButton();
        removeSources = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        javadocList = new javax.swing.JList();
        addJavadoc = new javax.swing.JButton();
        removeJavadoc = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.jLabel1.text")); // NOI18N

        classPathList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        classPathList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(classPathList);

        org.openide.awt.Mnemonics.setLocalizedText(addClasspath, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.addClasspath.text")); // NOI18N
        addClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClasspathActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeClasspath, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.removeClasspath.text")); // NOI18N
        removeClasspath.setEnabled(false);
        removeClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClasspathActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addClasspath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeClasspath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addClasspath, removeClasspath});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(addClasspath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeClasspath)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.jLabel2.text")); // NOI18N

        sourcesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        sourcesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(sourcesList);

        org.openide.awt.Mnemonics.setLocalizedText(addSources, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.addSources.text")); // NOI18N
        addSources.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourcesActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeSources, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.removeSources.text")); // NOI18N
        removeSources.setEnabled(false);
        removeSources.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSourcesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addSources, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeSources, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addSources)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSources)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.jLabel3.text")); // NOI18N

        javadocList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        javadocList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(javadocList);

        org.openide.awt.Mnemonics.setLocalizedText(addJavadoc, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.addJavadoc.text")); // NOI18N
        addJavadoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJavadocActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeJavadoc, org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.removeJavadoc.text")); // NOI18N
        removeJavadoc.setEnabled(false);
        removeJavadoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeJavadocActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addJavadoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeJavadoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(addJavadoc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeJavadoc)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AndroidPlatformCustomizer.class, "AndroidPlatformCustomizer.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addJavadocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJavadocActionPerformed
        FileChooserBuilder builder = new FileChooserBuilder(AndroidPlatformCustomizer.class);
        builder.setAcceptAllFileFilterUsed(false);
        builder.setFileFilter(new ArchiveFileFilter("javadoc files", new String[]{"JAR", "ZIP"}));
        builder.setTitle("Select Javadocs to add");
        File[] files = builder.showMultiOpenDialog();
        if (files != null) {
            for (File file : files) {
                aPackage.addJavadocPath(FileUtil.toFileObject(file).toURL(), true);
            }
            sdk.store();
            fireJavadocAdded();
        }
    }//GEN-LAST:event_addJavadocActionPerformed

    private void removeJavadocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeJavadocActionPerformed
        AndroidPlatformInfo.PathRecord selectedValue = (AndroidPlatformInfo.PathRecord) javadocList.getSelectedValue();
        if (selectedValue != null) {
            aPackage.removeJavadocPath(selectedValue);
            sdk.store();
            fireJavadocRemoved();
        }
    }//GEN-LAST:event_removeJavadocActionPerformed

    private void addClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClasspathActionPerformed
        FileChooserBuilder builder = new FileChooserBuilder(AndroidPlatformCustomizer.class);
        builder.setAcceptAllFileFilterUsed(false);
        builder.setFileFilter(new ArchiveFileFilter("Jar files", new String[]{"JAR"}));
        builder.setTitle("Select ClassPath to add");
        File[] files = builder.showMultiOpenDialog();
        if (files != null) {
            for (File file : files) {
                aPackage.addBootPath(FileUtil.toFileObject(file).toURL(), true);
            }
            sdk.store();
            fireClassPathAdded();
        }
    }//GEN-LAST:event_addClasspathActionPerformed

    private void removeClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClasspathActionPerformed
        AndroidPlatformInfo.PathRecord selectedValue = (AndroidPlatformInfo.PathRecord) classPathList.getSelectedValue();
        if (selectedValue != null) {
            aPackage.removeBootPath(selectedValue);
            sdk.store();
            fireClassPathRemoved();
        }
    }//GEN-LAST:event_removeClasspathActionPerformed

    private void addSourcesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourcesActionPerformed
        FileChooserBuilder builder = new FileChooserBuilder(AndroidPlatformCustomizer.class);
        builder.setAcceptAllFileFilterUsed(false);
        builder.setFileFilter(new ArchiveFileFilter("src files", new String[]{"JAR", "ZIP"}));
        builder.setTitle("Select Sources to add");
        File[] files = builder.showMultiOpenDialog();
        if (files != null) {
            for (File file : files) {
                aPackage.addSrcPath(FileUtil.toFileObject(file).toURL(), true);
            }
            sdk.store();
            fireSourcesAdded();
        }
    }//GEN-LAST:event_addSourcesActionPerformed

    private void removeSourcesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSourcesActionPerformed
        AndroidPlatformInfo.PathRecord selectedValue = (AndroidPlatformInfo.PathRecord) sourcesList.getSelectedValue();
        if (selectedValue != null) {
            aPackage.removeSrcPath(selectedValue);
            sdk.store();
            fireSourcesRemoved();
        }
    }//GEN-LAST:event_removeSourcesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClasspath;
    private javax.swing.JButton addJavadoc;
    private javax.swing.JButton addSources;
    private javax.swing.JList classPathList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList javadocList;
    private javax.swing.JButton removeClasspath;
    private javax.swing.JButton removeJavadoc;
    private javax.swing.JButton removeSources;
    private javax.swing.JList sourcesList;
    // End of variables declaration//GEN-END:variables
}
