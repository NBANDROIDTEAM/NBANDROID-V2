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

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author arsi
 */
@MIMEResolver.Registration(
        resource = "/org/nbandroid/netbeans/gradle/v2/layout/values/StringsResolver.xml",
        displayName = "Android layout.xml"
)
@DataObject.Registration(
        mimeType = "text/x-android-layout+xml",
        iconBase = "org/nbandroid/netbeans/gradle/v2/layout/layout/activity.png",
        position = 300
)
public class LayoutDataObject extends MultiDataObject {

    public static final String LAYOUT_MIME_TYPE = "text/x-android-layout+xml";

    public LayoutDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        final CookieSet cookies = getCookieSet();
        cookies.add(new CheckXMLSupport(DataObjectAdapters.inputSource(this)));
        cookies.add(new ValidateXMLSupport(DataObjectAdapters.inputSource(this)));
        registerEditor(LAYOUT_MIME_TYPE, true);
    }

    @MultiViewElement.Registration(
            displayName = "&Source",
            iconBase = "org/nbandroid/netbeans/gradle/v2/layout/layout/activity.png",
            mimeType = "text/x-android-layout+xml",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "source",
            position = 1
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new LayoutMultiViewEditorElement(lkp);
    }

    public static class LayoutMultiViewEditorElement extends MultiViewEditorElement {

        private final Lookup lookup;

        public LayoutMultiViewEditorElement(Lookup lookup) {
            super(lookup);
            this.lookup = lookup;
        }

        @Override
        public void componentShowing() {
            super.componentShowing();
            LaoutPreviewTopComponent.showLaoutPreview(lookup, LayoutMultiViewEditorElement.this);
        }

        @Override
        public void componentHidden() {
            super.componentHidden();
            LaoutPreviewTopComponent.hideLaoutPreview(LayoutMultiViewEditorElement.this);
        }

    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public Lookup getLookup() {
        return super.getLookup();
    }

}
