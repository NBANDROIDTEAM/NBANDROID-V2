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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
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
public final class AutoAndroidMavenSourceForBinaryQuery extends AbstractSourceForBinaryQuery {

    private static final FileObject[] NO_ROOTS = new FileObject[0];
    public static final String SOURCES_SUFFIX = "-sources.jar";
    public static final String JAR_SUFFIX = ".jar";
    public static final String CACHE_JARS_FOLDER = "jars";
    public static final String CACHE_OUTPUT_FOLDER = "output";
    public static final String CACHE_IMPUTS_FOLDER = "inputs";
    private static final String FILE_PATH = "FILE_PATH";

    public static FileObject sourceForJar(FileObject binaryRoot) {
        String srcFileName = binaryRoot.getName() + SOURCES_SUFFIX;
        FileObject dir = binaryRoot.getParent();
        if (dir == null) {
            return null;
        }
        if (CACHE_JARS_FOLDER.equals(dir.getName())) {
            dir = dir.getParent();
            if (dir == null) {
                return null;
            }
            if (CACHE_OUTPUT_FOLDER.equals(dir.getName())) {
                dir = dir.getParent();
                if (dir == null) {
                    return null;
                }
                dir = dir.getFileObject(CACHE_IMPUTS_FOLDER);
                if (dir == null || !dir.isData()) {
                    return null;
                }
                Properties properties = new Properties();
                try {
                    properties.load(dir.getInputStream());
                    String path = properties.getProperty(FILE_PATH, null);
                    if (path != null) {
                        FileObject fo = FileUtil.toFileObject(new File(path));
                        if (fo != null) {
                            srcFileName = fo.getName() + SOURCES_SUFFIX;
                            dir = fo.getParent();
                            if (dir == null) {
                                return null;
                            }
                            dir = dir.getFileObject(srcFileName);
                            if (dir == null) {
                                return null;
                            }
                            return FileUtil.getArchiveRoot(dir);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    return null;
                } catch (IOException ex) {
                    return null;
                }
            }
        }

        FileObject result = dir.getFileObject(srcFileName);
        if (result != null) {
            return FileUtil.getArchiveRoot(result);
        } else {
            return null;
        }
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
