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
package org.nbandroid.netbeans.gradle.v2.layout.values;

import java.io.IOException;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleable;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
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
        resource = "StringsResolver.xml",
        displayName = "Android dimens.xml"
)
@DataObject.Registration(
        mimeType = "text/x-android-dimens+xml",
        iconBase = "org/nbandroid/netbeans/gradle/v2/layout/icons-dimens-16.png",
        displayName = "dimens.xml",
        position = 300
)
public class DimensDataObject extends MultiDataObject {

    public static final String SETTINGS_MIME_TYPE = "text/x-android-dimens+xml";

    public DimensDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        final CookieSet cookies = getCookieSet();
        registerEditor(SETTINGS_MIME_TYPE, false);
        cookies.add(new ResourceXsdValidateXMLSupport(DataObjectAdapters.inputSource(this), AndroidStyleable.class.getResource("dimens.xsd")));
    }

    @MultiViewElement.Registration(
            displayName = "Dimens",
            iconBase = "org/nbandroid/netbeans/gradle/v2/layout/icons-dimens-16.png",
            mimeType = "text/x-android-dimens+xml",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "dimens",
            position = 1
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

}
