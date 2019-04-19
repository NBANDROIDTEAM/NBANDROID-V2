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
package org.netbeans.modules.android.project.sources.generated;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Gradle Android plugin 3.3.0+ don`t returns R folder in generated sources
 * @author arsi
 */
public class RTools {
    public static  PluginVersionResult handlePluginVersion(AndroidProject androidProject,Variant variant, FileObject prjDir) {
        File buildFolder = androidProject.getBuildFolder();
        String modelVersion = androidProject.getModelVersion();
        PluginVersion pluginVersion = pluginVersionFromString(modelVersion);
        if (pluginVersion.compareTo(new PluginVersion(3, 3, 0)) >= 0) {
            String variantName = variant.getName();
            String rFolderPath = buildFolder.getAbsolutePath() + File.separator + "generated" + File.separator + "not_namespaced_r_class_sources" + File.separator + variantName+File.separator+"process"+StringUtils.capitalize(variantName)+"Resources"+File.separator+"r";
            File rFolder = new File(rFolderPath);
            if (rFolder.exists() && rFolder.isDirectory()) {
                FileObject src = FileUtil.toFileObject(rFolder);
                String srcName = null;
                if (prjDir!=null) {
                    srcName = FileUtil.isParentOf(prjDir, src)
                            ? FileUtil.getRelativePath(prjDir, src)
                            : rFolder.getAbsolutePath();
                }else{
                    srcName = rFolder.getAbsolutePath();
                }
                return new PluginVersionResult(src, srcName);
            }
        }
        return null;
    }
    
    public static class PluginVersionResult{
        final FileObject src;
        final String srcName;

        public PluginVersionResult(FileObject src, String srcName) {
            this.src = src;
            this.srcName = srcName;
        }

        public FileObject getSrc() {
            return src;
        }

        public String getSrcName() {
            return srcName;
        }
        
        
    }
    
    public static PluginVersion pluginVersionFromString(String version) {
        StringTokenizer tokenizer = new StringTokenizer(version, ".", false);
        switch (tokenizer.countTokens()) {
            case 1:
                return new PluginVersion(Integer.valueOf(tokenizer.nextToken()), 0, 0);
            case 2:
                return new PluginVersion(Integer.valueOf(tokenizer.nextToken()), Integer.valueOf(tokenizer.nextToken()), 0);
            case 3:
                return new PluginVersion(Integer.valueOf(tokenizer.nextToken()), Integer.valueOf(tokenizer.nextToken()), Integer.valueOf(tokenizer.nextToken()));
            default:
                return new PluginVersion(3, 4, 0);
        }
    }

    static class PluginVersion implements Comparable<PluginVersion> {

        final int major;
        final int minor;
        final int rev;

        public PluginVersion(int major, int minor, int rev) {
            this.major = major;
            this.minor = minor;
            this.rev = rev;
        }

        @Override
        public int compareTo(PluginVersion o) {
            if (this.major != o.major) {
                return Integer.compare(this.major, o.major);
            }
            if (this.minor != o.minor) {
                return Integer.compare(this.minor, o.minor);
            }
            if (this.rev != o.rev) {
                return Integer.compare(this.rev, o.rev);
            }
            return 0;
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + rev;
        }
    }
}
