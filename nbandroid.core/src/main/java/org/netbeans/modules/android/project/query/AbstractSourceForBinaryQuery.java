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
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileUtil;

public abstract class AbstractSourceForBinaryQuery implements SourceForBinaryQueryImplementation2 {

    // This cache cannot shrink because SourceForBinaryQueryImplementation
    // requires that we return the exact same object when the same URL is
    // querried. This is a very limiting constraint but I don't want to risk to
    // violate the constraint.
    private final ConcurrentMap<File, Result> cache;

    public AbstractSourceForBinaryQuery() {
        this.cache = new ConcurrentHashMap<>();
    }

    // TODO: Instead of protected methods, they should be provided as an argument.
    protected File normalizeBinaryPath(File binaryRoot) {
        return binaryRoot;
    }

    protected abstract Result tryFindSourceRoot(File binaryRoot);

    @Override
    public final Result findSourceRoots2(URL binaryRoot) {
        File binaryRootFile = FileUtil.archiveOrDirForURL(binaryRoot);
        if (binaryRootFile == null) {
            return null;
        }

        File normBinaryRoot = normalizeBinaryPath(binaryRootFile);
        if (normBinaryRoot == null) {
            return null;
        }

        Result result = cache.get(normBinaryRoot);
        if (result != null) {
            return result;
        }

        result = tryFindSourceRoot(normBinaryRoot);
        if (result == null) {
            return null;
        }

        Result oldResult = cache.putIfAbsent(normBinaryRoot, result);
        return oldResult != null ? oldResult : result;
    }

    @Override
    public final SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }
}
