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
package org.nbandroid.netbeans.gradle.v2.layout.completion.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.StringTokenizer;

public class StyleableResultCollector {

    private String superClassName = null;
    private String className = null;
    private final List<String> styleables = new ArrayList<>();

    protected void setSuperClassName(String superClass) {
        if (superClass != null) {
            this.superClassName = superClass.replace("/", ".");
        } else {
            this.superClassName = null;
        }
    }

    public String getSuperClassName() {
        return superClassName;
    }

    protected void addStyleable(String styleable) {
        StringTokenizer tokenizer = new StringTokenizer(styleable, "_", false);
        StringJoiner joiner = new StringJoiner("_");
        while (tokenizer.hasMoreElements()) {
            String nextToken = tokenizer.nextToken();
            if (nextToken.length() > 0 && Character.isUpperCase(nextToken.charAt(0))) {
                joiner.add(nextToken);
            } else {
                break;
            }

        }
        String outName = joiner.toString();
        if (!styleables.contains(outName)) {
            styleables.add(outName);
        }
    }

    public List<String> getStyleables() {
        return styleables;
    }

    public String getClassName() {
        return className;
    }

    protected void setClassName(String className) {
        if (className != null) {
            this.className = className.replace("/", ".");
        } else {
            this.className = null;
        }
    }

    @Override
    public String toString() {
        return className + " [ " + superClassName + " ]"; //To change body of generated methods, choose Tools | Templates.
    }


}
