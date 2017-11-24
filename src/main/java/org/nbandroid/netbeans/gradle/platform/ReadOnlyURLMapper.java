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
package org.nbandroid.netbeans.gradle.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.ServiceProvider;

/**
 * Hack to make android platform source folder read-only
 *
 * @author arsi
 */
@ServiceProvider(service = URLMapper.class, position = 2000)
public class ReadOnlyURLMapper extends URLMapper implements PropertyChangeListener, LookupListener {

    private static final Map<FileObject, FileObject> cache = new WeakHashMap<>();
    private AtomicReference<AndroidPlatformProviderJava18> platformProvider = new AtomicReference<>(null);
    private final AtomicReference<String[]> reference = new AtomicReference<>(new String[0]);
    private final Lookup.Result<JavaPlatformProvider> result = Lookup.getDefault().lookupResult(JavaPlatformProvider.class);
    private static final File LASTPLATFORMS_FILE = Places.getCacheSubfile("NBANDROID/lastplatforms.xml");

    public ReadOnlyURLMapper() {

        result.addLookupListener(this);
        resultChanged(null);
        if (LASTPLATFORMS_FILE.exists()) {
            try {
                XMLDecoder decoder = new XMLDecoder(new FileInputStream(LASTPLATFORMS_FILE));
                String[] last = (String[]) decoder.readObject();
                if (last != null) {
                    reference.set(last);
                }
            } catch (Exception ex) {
            }
        }

    }

    @Override
    public URL getURL(FileObject fo, int type) {
        return null;
    }

    @Override
    public FileObject[] getFileObjects(URL url) {
        String[] paths = reference.get();
        for (String path : paths) {
            if (url.getPath().startsWith(path)) {
                FileObject fileObject = FileBasedFileSystem.getFileObject(new File(url.getPath()), FileObjectFactory.Caller.ToFileObject);
                if (fileObject != null) {
                    return new FileObject[]{ReadOnlyFileObject.findOrCreate(fileObject)};
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        AndroidPlatformProviderJava18 provider = platformProvider.get();
        if (provider != null) {
            JavaPlatform[] installedPlatforms = provider.getInstalledPlatforms();
            List<String> tmp = new ArrayList<>();
            for (JavaPlatform installedPlatform : installedPlatforms) {
                List<ClassPath.Entry> entries = installedPlatform.getSourceFolders().entries();
                for (ClassPath.Entry entrie : entries) {
                    if (!tmp.contains(entrie.getURL().getPath())) {
                        tmp.add(entrie.getURL().getPath());
                    }
                }
            }
            try {
                XMLEncoder encoder = new XMLEncoder(new FileOutputStream(LASTPLATFORMS_FILE));
                encoder.writeObject(tmp.toArray(new String[tmp.size()]));
                encoder.flush();
                encoder.close();
            } catch (Exception ex) {
            }
            reference.set(tmp.toArray(new String[tmp.size()]));
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        AndroidPlatformProviderJava18 provider = platformProvider.get();
        if (provider == null) {
            Collection<? extends JavaPlatformProvider> lookupAll = result.allInstances();
            Iterator<? extends JavaPlatformProvider> iterator = lookupAll.iterator();
            while (iterator.hasNext()) {
                JavaPlatformProvider next = iterator.next();
                if (next instanceof AndroidPlatformProviderJava18) {
                    platformProvider.set((AndroidPlatformProviderJava18) next);
                    ((AndroidPlatformProviderJava18) next).addPropertyChangeListener(this);
                    propertyChange(null);
                }
            }
        }

    }

}
