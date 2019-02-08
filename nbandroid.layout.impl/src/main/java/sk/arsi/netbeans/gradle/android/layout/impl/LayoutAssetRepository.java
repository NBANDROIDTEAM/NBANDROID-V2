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
package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.ide.common.rendering.api.AssetRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link AssetRepository} used for render tests.
 */
public class LayoutAssetRepository extends AssetRepository {

    private static InputStream open(String path) throws FileNotFoundException {
        File asset = new File(path);
        if (asset.isFile()) {
            return new FileInputStream(asset);
        }

        return null;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public InputStream openAsset(String path, int mode) throws IOException {
        return open(path);
    }

    @Override
    public InputStream openNonAsset(int cookie, String path, int mode) throws IOException {
        return open(path);
    }
}
