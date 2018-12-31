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
package sk.arsi.netbeans.gradle.android.bintray.impl;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import sk.arsi.netbeans.gradle.android.maven.MavenDependencyInfo;

public class BintraySearchResult implements Serializable {

    private String name;
    private String repo;
    private String owner;
    private String desc;
    private List<Object> labels = new ArrayList<Object>();
    private List<Object> attributeNames = new ArrayList<Object>();
    private List<Object> licenses = new ArrayList<Object>();
    private List<Object> customLicenses = new ArrayList<Object>();
    private int followersCount;
    private String created;
    private Object websiteUrl;
    private Object issueTrackerUrl;
    private List<Object> linkedToRepos = new ArrayList<Object>();
    private List<Object> permissions = new ArrayList<Object>();
    private List<String> versions = new ArrayList<String>();
    private String latestVersion;
    private String updated;
    private int ratingCount;
    private List<String> systemIds = new ArrayList<String>();
    private Object vcsUrl;
    private String maturity;
    private final static long serialVersionUID = 7351046534551199948L;

    /**
     * No args constructor for use in serialization
     *
     */
    public BintraySearchResult() {
    }

    /**
     *
     * @param attributeNames
     * @param maturity
     * @param desc
     * @param ratingCount
     * @param labels
     * @param vcsUrl
     * @param systemIds
     * @param linkedToRepos
     * @param licenses
     * @param issueTrackerUrl
     * @param latestVersion
     * @param versions
     * @param customLicenses
     * @param updated
     * @param created
     * @param repo
     * @param name
     * @param permissions
     * @param owner
     * @param followersCount
     * @param websiteUrl
     */
    public BintraySearchResult(String name, String repo, String owner, String desc, List<Object> labels, List<Object> attributeNames, List<Object> licenses, List<Object> customLicenses, int followersCount, String created, Object websiteUrl, Object issueTrackerUrl, List<Object> linkedToRepos, List<Object> permissions, List<String> versions, String latestVersion, String updated, int ratingCount, List<String> systemIds, Object vcsUrl, String maturity) {
        super();
        this.name = name;
        this.repo = repo;
        this.owner = owner;
        this.desc = desc;
        this.labels = labels;
        this.attributeNames = attributeNames;
        this.licenses = licenses;
        this.customLicenses = customLicenses;
        this.followersCount = followersCount;
        this.created = created;
        this.websiteUrl = websiteUrl;
        this.issueTrackerUrl = issueTrackerUrl;
        this.linkedToRepos = linkedToRepos;
        this.permissions = permissions;
        this.versions = versions;
        this.latestVersion = latestVersion;
        this.updated = updated;
        this.ratingCount = ratingCount;
        this.systemIds = systemIds;
        this.vcsUrl = vcsUrl;
        this.maturity = maturity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BintraySearchResult withName(String name) {
        this.name = name;
        return this;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public BintraySearchResult withRepo(String repo) {
        this.repo = repo;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BintraySearchResult withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public BintraySearchResult withDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public List<Object> getLabels() {
        return labels;
    }

    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    public BintraySearchResult withLabels(List<Object> labels) {
        this.labels = labels;
        return this;
    }

    public List<Object> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(List<Object> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public BintraySearchResult withAttributeNames(List<Object> attributeNames) {
        this.attributeNames = attributeNames;
        return this;
    }

    public List<Object> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<Object> licenses) {
        this.licenses = licenses;
    }

    public BintraySearchResult withLicenses(List<Object> licenses) {
        this.licenses = licenses;
        return this;
    }

    public List<Object> getCustomLicenses() {
        return customLicenses;
    }

    public void setCustomLicenses(List<Object> customLicenses) {
        this.customLicenses = customLicenses;
    }

    public BintraySearchResult withCustomLicenses(List<Object> customLicenses) {
        this.customLicenses = customLicenses;
        return this;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public BintraySearchResult withFollowersCount(int followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public BintraySearchResult withCreated(String created) {
        this.created = created;
        return this;
    }

    public Object getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(Object websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public BintraySearchResult withWebsiteUrl(Object websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }

    public Object getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(Object issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
    }

    public BintraySearchResult withIssueTrackerUrl(Object issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
        return this;
    }

    public List<Object> getLinkedToRepos() {
        return linkedToRepos;
    }

    public void setLinkedToRepos(List<Object> linkedToRepos) {
        this.linkedToRepos = linkedToRepos;
    }

    public BintraySearchResult withLinkedToRepos(List<Object> linkedToRepos) {
        this.linkedToRepos = linkedToRepos;
        return this;
    }

    public List<Object> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Object> permissions) {
        this.permissions = permissions;
    }

    public BintraySearchResult withPermissions(List<Object> permissions) {
        this.permissions = permissions;
        return this;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public BintraySearchResult withVersions(List<String> versions) {
        this.versions = versions;
        return this;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public BintraySearchResult withLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public BintraySearchResult withUpdated(String updated) {
        this.updated = updated;
        return this;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public BintraySearchResult withRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
        return this;
    }

    public List<String> getSystemIds() {
        return systemIds;
    }

    public void setSystemIds(List<String> systemIds) {
        this.systemIds = systemIds;
    }

    public BintraySearchResult withSystemIds(List<String> systemIds) {
        this.systemIds = systemIds;
        return this;
    }

    public Object getVcsUrl() {
        return vcsUrl;
    }

    public void setVcsUrl(Object vcsUrl) {
        this.vcsUrl = vcsUrl;
    }

    public BintraySearchResult withVcsUrl(Object vcsUrl) {
        this.vcsUrl = vcsUrl;
        return this;
    }

    public String getMaturity() {
        return maturity;
    }

    public void setMaturity(String maturity) {
        this.maturity = maturity;
    }

    public BintraySearchResult withMaturity(String maturity) {
        this.maturity = maturity;
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Transient
    List<MavenDependencyInfo> build() {
        List<MavenDependencyInfo> tmp = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(name, ":", false);
        if (tok.countTokens() == 2) {
            MavenDependencyInfo dependencyInfo = new MavenDependencyInfo(MavenDependencyInfo.Type.JCENTER, tok.nextToken(), tok.nextToken());
            tmp.add(dependencyInfo);
            for (String version : versions) {
                dependencyInfo.addVersion(version);
            }
        }
        return tmp;
    }

}
