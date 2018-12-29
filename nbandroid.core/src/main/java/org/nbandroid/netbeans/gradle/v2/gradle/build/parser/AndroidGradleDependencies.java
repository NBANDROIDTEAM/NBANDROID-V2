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
import java.util.List;

/**
 *
 * @author arsi
 */
public class AndroidGradleDependencies {
    private final int firstLine;
    private final int firstColumn;
    private final int lastLine;
    private final int lastColumn;
    private final List<AndroidGradleDependency> dependencies = new ArrayList<>();

    public AndroidGradleDependencies(int firstLine, int firstColumn, int lastLine, int lastColumn) {
        this.firstLine = firstLine;
        this.firstColumn = firstColumn;
        this.lastLine = lastLine;
        this.lastColumn = lastColumn;
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

    public List<AndroidGradleDependency> getDependencies() {
        return dependencies;
    }

}
