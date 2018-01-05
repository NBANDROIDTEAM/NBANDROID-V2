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
package org.nbandroid.netbeans.gradle.query;

/**
 *
 * @author arsi
 */
import com.google.common.collect.MapMaker;
import java.io.File;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.v2.maven.ArtifactData;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.gradle.project.query.AbstractSourceForBinaryQuery;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service = SourceForBinaryQueryImplementation2.class),
    @ServiceProvider(service = SourceForBinaryQueryImplementation.class),
    @ServiceProvider(service = AutoAndroidGradleSourceForBinaryQuery.class),
    @ServiceProvider(service = ClassPathProvider.class, position = 100),})
public final class AutoAndroidGradleSourceForBinaryQuery extends AbstractSourceForBinaryQuery implements ClassPathProvider {

    private static final FileObject[] NO_ROOTS = new FileObject[0];
    private final Vector<GradleAndroidClassPathProvider> providers = new Vector<>();
    private static final ConcurrentMap<FileObject, GradleAndroidClassPathProvider> backReferenceMap = new MapMaker().weakKeys().weakValues().makeMap();
    private static final ConcurrentMap<FileObject, ClassPath> cache = new MapMaker().weakKeys().makeMap();

    public void addClassPathProvider(GradleAndroidClassPathProvider classPathProvider) {
        providers.add(classPathProvider);
    }

    public void removeClassPathProvider(GradleAndroidClassPathProvider classPathProvider) {
        providers.remove(classPathProvider);
    }

    @Override
    protected Result tryFindSourceRoot(File binaryRoot) {
        for (GradleAndroidClassPathProvider provider : providers) {
            ArtifactData artifactData = provider.getArtifactData(FileUtil.urlForArchiveOrDir(binaryRoot));
            if (artifactData != null && artifactData.isSrcLocal()) {
                File src = new File(artifactData.getSrcPath());
                FileObject srcFo = FileUtil.toFileObject(src);
                backReferenceMap.put(srcFo, provider);
                backReferenceMap.put(FileUtil.toFileObject(binaryRoot), provider);
                return new AndroidResult(artifactData);
            }
        }
        return null;

    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        FileObject srcRoot = FileUtil.getArchiveFile(file);
        if (srcRoot != null) {
            GradleAndroidClassPathProvider provider = backReferenceMap.get(srcRoot);
            if (provider != null) {
                switch (type) {
                    case ClassPath.SOURCE:

                        ClassPath classPath = cache.get(srcRoot);
                        if (classPath == null) {
                            ClassPath srcClassPath = ClassPathSupport.createClassPath(FileUtil.getArchiveRoot(srcRoot));
                            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[]{srcClassPath});
                            classPath = ClassPathSupport.createProxyClassPath(srcClassPath);
                            cache.put(srcRoot, classPath);
                        }
                        return classPath;
                    case ClassPath.BOOT:
                        return provider.getClassPath(ClassPath.BOOT);
                    case ClassPath.COMPILE:
                        return provider.getClassPath(ClassPath.COMPILE);

                }
            }
        }
        return null;
    }

    private static class AndroidResult implements Result {

        private final ArtifactData artifactData;

        public AndroidResult(ArtifactData artifactData) {
            this.artifactData = artifactData;
        }

        @Override
        public boolean preferSources() {
            return false;
        }

        @Override
        public FileObject[] getRoots() {
            File src = new File(artifactData.getSrcPath());
            FileObject srcFo = FileUtil.toFileObject(src);
            if (srcFo != null) {
                return new FileObject[]{FileUtil.getArchiveRoot(srcFo)};
            }
            return NO_ROOTS;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    }
}
