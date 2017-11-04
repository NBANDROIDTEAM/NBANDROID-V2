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
import javax.swing.event.ChangeListener;
import org.netbeans.gradle.project.query.AbstractSourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service = SourceForBinaryQueryImplementation2.class),
    @ServiceProvider(service = SourceForBinaryQueryImplementation.class)})
public final class AutoAndroidSourceForBinaryQuery extends AbstractSourceForBinaryQuery {

    private static final FileObject[] NO_ROOTS = new FileObject[0];
    public static final String SOURCES_DIR = "sources";
    public static final String PLATFORMS_DIR = "platforms";

    public static FileObject sourceForJar(FileObject binaryRoot) {
        FileObject dir = binaryRoot.getParent();
        if (dir == null) {
            return null;
        }
        String platformName = dir.getName();
        dir = dir.getParent();
        if (dir == null || !PLATFORMS_DIR.equals(dir.getName())) {
            return null;
        }
        dir = dir.getParent();
        if (dir == null) {
            return null;
        }
        dir = dir.getFileObject(SOURCES_DIR);
        if (dir == null) {
            return null;
        }
        dir = dir.getFileObject(platformName);
        if (dir == null) {
            return null;
        }
        return dir;
    }

    @Override
    protected Result tryFindSourceRoot(File binaryRoot) {
        final FileObject binaryRootObj = FileUtil.toFileObject(binaryRoot);
        if (binaryRootObj == null) {
            return null;
        }

        if (sourceForJar(binaryRootObj) == null) {
            return null;
        }

        return new Result() {
            @Override
            public boolean preferSources() {
                return false;
            }

            @Override
            public FileObject[] getRoots() {
                FileObject result = sourceForJar(binaryRootObj);
                return result != null
                        ? new FileObject[]{result}
                        : NO_ROOTS;
            }

            @Override
            public void addChangeListener(ChangeListener l) {
            }

            @Override
            public void removeChangeListener(ChangeListener l) {
            }
        };
    }
}
