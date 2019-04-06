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
package org.netbeans.modules.android.project.gradle;

import java.io.IOException;
import org.netbeans.modules.android.project.api.NbAndroidProjectFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = DataLoader.class, position = 300)
public class GradleDataLoader extends UniFileLoader {

    public GradleDataLoader() {
        super("org.netbeans.modules.android.project.gradle.GradleDataObject");
        ExtensionList exts = new ExtensionList();
        exts.addExtension("gradle");
        setExtensions(exts);
    }

    /**
     * Handle only android project gradle files
     *
     * @param fo
     * @return
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject primaryFile = super.findPrimaryFile(fo);
        if (primaryFile != null) {
            if (NbAndroidProjectFactory.isSubProject(primaryFile.getParent()) || NbAndroidProjectFactory.isRootProject(primaryFile.getParent())) {
                return primaryFile;
            }
        }
        return null;
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new GradleDataObject(primaryFile, this);
    }

    @Override
    protected String actionsContext() {
        return "Loaders/text/x-gradle+x-groovy/Actions/"; //To change body of generated methods, choose Tools | Templates.
    }


}
