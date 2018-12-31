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
package sk.arsi.netbeans.gradle.android.maven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author arsi
 */
public class MavenDependencyInfo implements Serializable {

    public static enum Type {
        GOOGLE,
        JCENTER,
        MAVEN,
    }

    public MavenDependencyInfo() {
    }

    private Type type;
    private String groupId;
    private String artifactId;
    private final List<Version> versions = new ArrayList<>();

    public MavenDependencyInfo(Type type, String groupId, String artifactId) {
        this.type = type;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public Type getType() {
        return type;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public List<Version> getVersions() {
        return Collections.unmodifiableList(versions);
    }

    public void addVersion(String version) {
        Version tmp = new Version(version);
        if (!versions.contains(tmp)) {
            versions.add(tmp);
        }
    }

    public String getGradleLine() {
        return groupId + ":" + artifactId;
    }

    @Override
    public String toString() {
        return getGradleLine();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.type);
        hash = 73 * hash + Objects.hashCode(this.groupId);
        hash = 73 * hash + Objects.hashCode(this.artifactId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MavenDependencyInfo other = (MavenDependencyInfo) obj;
        if (!Objects.equals(this.groupId, other.groupId)) {
            return false;
        }
        if (!Objects.equals(this.artifactId, other.artifactId)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    public class Version implements Serializable {

        private final String version;

        public Version(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public Type getType() {
            return type;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getGradleLine() {
            return groupId + ":" + artifactId + ":" + version;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Objects.hashCode(this.version);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            if (!Objects.equals(this.version, other.version)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return this.getGradleLine();
        }


    }

}
