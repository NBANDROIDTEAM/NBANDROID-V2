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
package org.nbandroid.netbeans.gradle.symlink;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class GradleSymLinkRemover implements Runnable {

    private final File tempDir;

    public GradleSymLinkRemover() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        tempDir = new File(tmpdir);
    }


    @Override
    public void run() {
        File[] listFiles = tempDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("symlink") && name.endsWith("test_link");
            }
        });
        for (File file : listFiles) {
            if (Files.isSymbolicLink(file.toPath())) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
