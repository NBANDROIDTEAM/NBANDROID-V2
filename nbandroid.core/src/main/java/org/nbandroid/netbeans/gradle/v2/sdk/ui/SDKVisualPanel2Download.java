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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.nbandroid.netbeans.gradle.v2.maven.MavenDownloader;
import org.openide.WizardDescriptor;
import org.openide.modules.Places;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * SDK download panel
 *
 * @author arsi
 */
public final class SDKVisualPanel2Download extends JPanel {

    public static final String GOOGLE_INDEX = "https://dl.google.com/android/repository/repository.xml";
    public static final String SDKLICENSE = "sdk:license";
    public static final String SDKURL = "sdk:url";
    public static final String MACOSX = "macosx";
    public static final String WINDOWS = "windows";
    public static final String LINUX = "linux";
    public static final String TOOLS = "tools";
    public static final String GOOGLE_REPO_URL = "https://dl.google.com/android/repository/";
    public static final String NBANDROID_FOLDER = "nbandroid/";
    public static final String PLATFORMTOOLS = "platform-tools";
    public static final String DOWNLOAD_OK = "DOWNLOAD_OK";
    private final File repository = Places.getCacheSubfile("nbandroid/repository.xml");
    private String license = "Unable to download https://dl.google.com/android/repository/repository.xml";
    private final List<String> files = new ArrayList<>();
    private File platformTools;
    private File tools;

    /**
     * Creates new form SDKVisualPanel2
     */
    public SDKVisualPanel2Download() {
        initComponents();
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 1);
        putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
        putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
        putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        try {
            MavenDownloader.downloadFully(new URL(GOOGLE_INDEX), repository);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (repository.exists()) {
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document parse = docBuilder.parse(repository);
                NodeList elementsByTagName = parse.getElementsByTagName(SDKLICENSE);
                int length = elementsByTagName.getLength();
                if (length == 1) {
                    Node item = elementsByTagName.item(0);
                    license = item.getTextContent();
                }
                elementsByTagName = parse.getElementsByTagName(SDKURL);
                length = elementsByTagName.getLength();
                String filter = "**********";
                if (BaseUtilities.isWindows()) {
                    filter = WINDOWS;
                } else if (BaseUtilities.isMac()) {//first test for MACOSX, BaseUtilities.isUnix() has included MACOSX
                    filter = MACOSX;
                } else if (BaseUtilities.isUnix()) {
                    filter = LINUX;
                }
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        Node item = elementsByTagName.item(i);
                        String fileName = item.getTextContent();
                        if (fileName.contains(filter)) {
                            files.add(fileName);
                        }

                    }
                }

            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            jTextPane1.setText(license);
            jTextPane1.setCaretPosition(0);
            if (files.isEmpty()) {
                downloadButton.setEnabled(false);
            } else {
                downloadButton.setEnabled(true);
            }
        }
    }

    public File getTools() {
        return tools;
    }

    public File getPlatformTools() {
        return platformTools;
    }

    @Override
    public String getName() {
        return "Download SDK";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        downloadButton = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        downloadText = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(611, 154));
        setMinimumSize(new java.awt.Dimension(611, 154));
        setPreferredSize(new java.awt.Dimension(611, 154));

        jScrollPane1.setViewportView(jTextPane1);

        org.openide.awt.Mnemonics.setLocalizedText(downloadButton, org.openide.util.NbBundle.getMessage(SDKVisualPanel2Download.class, "SDKVisualPanel2Download.downloadButton.text")); // NOI18N
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downloadText, org.openide.util.NbBundle.getMessage(SDKVisualPanel2Download.class, "SDKVisualPanel2Download.downloadText.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(downloadText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downloadButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(downloadText, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downloadButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        downloadButton.setEnabled(false);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < files.size(); i++) {
                    String fileName = files.get(i);
                    if (fileName.startsWith(PLATFORMTOOLS)) {
                        platformTools = Places.getCacheSubfile(NBANDROID_FOLDER + fileName);
                        downloadText.setText("Downloading " + fileName + "...");
                        if (!platformTools.exists() || platformTools.length() == 0) {
                            try {

                                MavenDownloader.downloadFully(new URL(GOOGLE_REPO_URL + fileName), platformTools, jProgressBar1);
                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        } else {
                            jProgressBar1.setValue(jProgressBar1.getMaximum());
                        }
                        downloadText.setText("Downloading " + fileName + "... Done!");
                    } else if (fileName.startsWith(TOOLS)) {
                        tools = Places.getCacheSubfile(NBANDROID_FOLDER + fileName);
                        downloadText.setText("Downloading " + fileName + "...");
                        if (!tools.exists() || tools.length() == 0) {
                            try {
                                MavenDownloader.downloadFully(new URL(GOOGLE_REPO_URL + fileName), tools, jProgressBar1);
                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        } else {
                            jProgressBar1.setValue(jProgressBar1.getMaximum());
                        }
                        downloadText.setText("Downloading " + fileName + "... Done!");
                    }
                }
                firePropertyChange(DOWNLOAD_OK, true, false);
            }

        };
        MavenDownloader.POOL.execute(runnable);

    }//GEN-LAST:event_downloadButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton downloadButton;
    private javax.swing.JLabel downloadText;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
