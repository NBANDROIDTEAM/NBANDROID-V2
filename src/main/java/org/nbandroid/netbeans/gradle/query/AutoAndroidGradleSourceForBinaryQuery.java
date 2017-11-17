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
import java.io.File;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.v2.maven.ArtifactData;
import org.netbeans.gradle.project.query.AbstractSourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service = SourceForBinaryQueryImplementation2.class),
    @ServiceProvider(service = SourceForBinaryQueryImplementation.class),
    @ServiceProvider(service = AutoAndroidGradleSourceForBinaryQuery.class)})
public final class AutoAndroidGradleSourceForBinaryQuery extends AbstractSourceForBinaryQuery {

    private static final FileObject[] NO_ROOTS = new FileObject[0];
    private final Vector<GradleAndroidClassPathProvider> providers = new Vector<>();

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
                return new AndroidResult(artifactData);
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
