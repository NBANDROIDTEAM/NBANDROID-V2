/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.maven;

import java.io.Serializable;

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
    private String version;

    public MavenDependencyInfo(Type type, String groupId, String artifactId, String version) {
        this.type = type;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
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

    public String getVersion() {
        return version;
    }

    public String getGradleLine() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public String toString() {
        return getGradleLine();
    }


}
