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
package sk.arsi.netbeans.gradle.android.google.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import sk.arsi.netbeans.gradle.android.maven.MavenDependencyInfo;

/**
 *
 * @author arsi
 */
public class GoogleGroupIndex {

    protected String group;
    protected final Map<String, String> downloaded = new HashMap<>();
    protected String lastArtifactId = null;
    protected String url;
    protected final List<MavenDependencyInfo> artifacts = new ArrayList<>();

    public String getGroup() {
        return group;
    }

    public String getUrl() {
        return url;
    }

    protected GoogleGroupIndex build() {
        for (Map.Entry<String, String> entry : downloaded.entrySet()) {
            String artifactId = entry.getKey();
            String versions = entry.getValue();
            StringTokenizer tok = new StringTokenizer(versions, ",", false);
            MavenDependencyInfo dependencyInfo = new MavenDependencyInfo(MavenDependencyInfo.Type.GOOGLE, group, artifactId);
            artifacts.add(dependencyInfo);
            while (tok.hasMoreElements()) {
                dependencyInfo.addVersion(tok.nextToken());
            }

        }
        downloaded.clear();
        return this;
    }

    public List<MavenDependencyInfo> getArtifacts() {
        return artifacts;
    }

}
