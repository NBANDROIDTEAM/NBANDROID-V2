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

package org.netbeans.modules.android.project.query;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;

public abstract class AbstractJavadocForBinaryQuery implements JavadocForBinaryQueryImplementation {

    // This cache cannot shrink because JavadocForBinaryQueryImplementation
    // requires that we return the exact same object when the same URL is
    // querried. This is a very limiting constraint but I don't want to risk to
    // violate the constraint.
    private final ConcurrentMap<File, JavadocForBinaryQuery.Result> cache;

    public AbstractJavadocForBinaryQuery() {
        this.cache = new ConcurrentHashMap<>();
    }

    protected abstract JavadocForBinaryQuery.Result tryFindJavadoc(File binaryRoot);

    @Override
    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        File binaryRootFile = FileUtil.archiveOrDirForURL(binaryRoot);
        if (binaryRootFile == null) {
            return null;
        }

        JavadocForBinaryQuery.Result result = cache.get(binaryRootFile);
        if (result != null) {
            return result;
        }

        result = tryFindJavadoc(binaryRootFile);
        if (result == null) {
            return null;
        }

        JavadocForBinaryQuery.Result oldResult = cache.putIfAbsent(binaryRootFile, result);
        return oldResult != null ? oldResult : result;
    }
}
