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
package sk.arsi.netbeans.gradle.android.layout.impl.v2;

import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.resources.ResourceSet;
import java.io.File;

/**
 *
 * @author arsi
 */
public class AarResourceSet extends ResourceSet {

    public AarResourceSet(String name, ResourceNamespace namespace, String libraryName, boolean validateEnabled) {
        super(name, namespace, libraryName, validateEnabled);
    }

    @Override
    public boolean isIgnored(File file) {
        String fileName = file.getName();
        if (super.isIgnored(file)) {
            return true;
        }

        // TODO: Restrict the following checks to folders only.
        if (fileName.startsWith("values-mcc") || fileName.startsWith("raw-")) {
            return true; // Mobile country codes and raw resources are not used by LayoutLib.
        }

        // Skip locale-specific folders if myWithLocaleResources is false.
        // Skip files that don't contain resources.
        return fileName.equals("public.xml") || fileName.equals("symbols.xml");
    }
}
