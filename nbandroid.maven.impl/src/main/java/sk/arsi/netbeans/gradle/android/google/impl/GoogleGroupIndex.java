/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            while (tok.hasMoreElements()) {
                String version = tok.nextToken();
                artifacts.add(new MavenDependencyInfo(MavenDependencyInfo.Type.GOOGLE, group, artifactId, version));
            }

        }
        downloaded.clear();
        return this;
    }

    public List<MavenDependencyInfo> getArtifacts() {
        return artifacts;
    }

}
