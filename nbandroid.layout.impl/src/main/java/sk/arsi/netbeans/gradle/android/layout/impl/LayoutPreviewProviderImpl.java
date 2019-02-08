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

import java.io.File;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewPanel;
import sk.arsi.netbeans.gradle.android.layout.spi.LayoutPreviewProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = LayoutPreviewProvider.class)
public class LayoutPreviewProviderImpl extends LayoutPreviewProvider {

    @Override
    public LayoutPreviewPanel getPreview(File platformFolder, File layoutFile, File appResFolder, String themeName, List<File> aars, List<File> jars) {
        LayoutPreviewPanelImpl imagePanel = new LayoutPreviewPanelImpl(platformFolder, layoutFile, appResFolder, themeName, aars, jars);
        return imagePanel;
    }

}
