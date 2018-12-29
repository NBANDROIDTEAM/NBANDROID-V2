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
package org.nbandroid.netbeans.gradle.v2.gradle.build.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author arsi
 */
public class AndroidGradleDependency {

    private final String type;
    private AndroidDependency androidDependency;
    private final List<AndroidDependencyExclude> exclude = new ArrayList<>();
    private final Map<String, String> variables = new HashMap<>();
    private final int firstLine;
    private final int firstColumn;
    private final int lastLine;
    private final int lastColumn;

    public AndroidGradleDependency(String type, int firstLine, int firstColumn, int lastLine, int lastColumn) {
        this.type = type;
        this.firstLine = firstLine;
        this.firstColumn = firstColumn;
        this.lastLine = lastLine;
        this.lastColumn = lastColumn;
    }

    public String getType() {
        return type;
    }

    public List<AndroidDependencyExclude> getExclude() {
        return exclude;
    }

    public AndroidDependency getAndroidDependency() {
        return androidDependency;
    }

    public void setAndroidDependency(AndroidDependency androidDependency) {
        this.androidDependency = androidDependency;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public int getFirstLine() {
        return firstLine;
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public int getLastLine() {
        return lastLine;
    }

    public int getLastColumn() {
        return lastColumn;
    }


    public static class AndroidDependencyExclude {

        private final String group;
        private final String module;

        public AndroidDependencyExclude(String group, String module) {
            this.group = group;
            this.module = module;
        }

        public String getGroup() {
            return group;
        }

        public String getModule() {
            return module;
        }

        public boolean hasGroup() {
            return group != null;
        }

    }

    public static enum AndroidDependencyType {
        LOCAL_LIBRARY_MODULE,
        LOCAL_BINARY_TREE,
        LOCAL_BINARY_FILES,
        REMOTE_BINARY;
    }

    public static class AndroidDependency {

        private final AndroidDependencyType type;

        public AndroidDependency(AndroidDependencyType type) {
            this.type = type;
        }

        public AndroidDependencyType getType() {
            return type;
        }

    }

    public static class AndroidLocalLibraryModuleDependency extends AndroidDependency {

        private String localLibraryModule = null;

        public AndroidLocalLibraryModuleDependency() {
            super(AndroidDependencyType.LOCAL_LIBRARY_MODULE);
        }

        public String getLocalLibraryModule() {
            return localLibraryModule;
        }

        public void setLocalLibraryModule(String localLibraryModule) {
            this.localLibraryModule = localLibraryModule;
        }

    }

    public static class AndroidLocalBinaryTreeDependency extends AndroidDependency {

        private final Map<String, String> localLibrary = new HashMap<>();

        public AndroidLocalBinaryTreeDependency() {
            super(AndroidDependencyType.LOCAL_BINARY_TREE);
        }

        public Map<String, String> getLocalLibrary() {
            return localLibrary;
        }

    }

    public static class AndroidLocalBinaryFilesDependency extends AndroidDependency {

        private final List<String> localFiles = new ArrayList<>();

        public AndroidLocalBinaryFilesDependency() {
            super(AndroidDependencyType.LOCAL_BINARY_FILES);
        }

        public List<String> getLocalFiles() {
            return localFiles;
        }

    }


    public static class AndroidRemoteBinaryDependency extends AndroidDependency {

        public final String remoteBinary;

        public AndroidRemoteBinaryDependency(String remoteBinary) {
            super(AndroidDependencyType.REMOTE_BINARY);
            this.remoteBinary = remoteBinary;
        }

        public String getRemoteBinary() {
            return remoteBinary;
        }

    }

}
