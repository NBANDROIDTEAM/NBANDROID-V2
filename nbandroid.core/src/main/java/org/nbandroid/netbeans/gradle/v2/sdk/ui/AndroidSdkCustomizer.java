/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk.ui;

import javax.swing.JTabbedPane;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.ui.SdkPlatformPanel;
import org.nbandroid.netbeans.gradle.v2.sdk.manager.ui.SdkToolsPanel;
import org.openide.loaders.XMLDataObject;

/**
 *
 * @author arsi
 */
public class AndroidSdkCustomizer extends JTabbedPane {

    public AndroidSdkCustomizer(AndroidSdk platform, XMLDataObject holder) {
        super();
        SdkPlatformPanel panel = new SdkPlatformPanel(platform);
        addTab("SDK Platforms", panel);
        SdkToolsPanel toolsPanel = new SdkToolsPanel(platform);
        addTab("SDK Tools", toolsPanel);
    }
}
