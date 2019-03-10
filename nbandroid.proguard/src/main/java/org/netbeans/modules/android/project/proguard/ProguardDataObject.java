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

package org.netbeans.modules.android.project.proguard;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author arsi
 */
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-proguard/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),})
@MIMEResolver.Registration(
        resource = "StringResolver.xml",
        displayName = "proguard-rules.pro"
)
@DataObject.Registration(
        mimeType = "text/x-proguard",
        iconBase = "org/netbeans/modules/android/project/proguard/proguard.png",
        position = 1
)
public class ProguardDataObject extends MultiDataObject {

    public static final String MIME_TYPE = "text/x-proguard"; //NOI18N

    public ProguardDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(MIME_TYPE, true);
    }

    @MultiViewElement.Registration(
            displayName = "&Source",
            iconBase = "org/netbeans/modules/android/project/proguard/proguard.png",
            mimeType = "text/x-proguard",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "source",
            position = 1
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

}
