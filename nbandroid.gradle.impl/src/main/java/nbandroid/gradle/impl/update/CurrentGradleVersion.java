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
package nbandroid.gradle.impl.update;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author arsi
 */
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "buildTime",
    "current",
    "snapshot",
    "nightly",
    "releaseNightly",
    "activeRc",
    "rcFor",
    "milestoneFor",
    "broken",
    "downloadUrl",
    "checksumUrl",
    "wrapperChecksumUrl"
})
public class CurrentGradleVersion {

    @JsonProperty("version")
    private String version;
    @JsonProperty("buildTime")
    private String buildTime;
    @JsonProperty("current")
    private Boolean current;
    @JsonProperty("snapshot")
    private Boolean snapshot;
    @JsonProperty("nightly")
    private Boolean nightly;
    @JsonProperty("releaseNightly")
    private Boolean releaseNightly;
    @JsonProperty("activeRc")
    private Boolean activeRc;
    @JsonProperty("rcFor")
    private String rcFor;
    @JsonProperty("milestoneFor")
    private String milestoneFor;
    @JsonProperty("broken")
    private Boolean broken;
    @JsonProperty("downloadUrl")
    private String downloadUrl;
    @JsonProperty("checksumUrl")
    private String checksumUrl;
    @JsonProperty("wrapperChecksumUrl")
    private String wrapperChecksumUrl;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("buildTime")
    public String getBuildTime() {
        return buildTime;
    }

    @JsonProperty("buildTime")
    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    @JsonProperty("current")
    public Boolean getCurrent() {
        return current;
    }

    @JsonProperty("current")
    public void setCurrent(Boolean current) {
        this.current = current;
    }

    @JsonProperty("snapshot")
    public Boolean getSnapshot() {
        return snapshot;
    }

    @JsonProperty("snapshot")
    public void setSnapshot(Boolean snapshot) {
        this.snapshot = snapshot;
    }

    @JsonProperty("nightly")
    public Boolean getNightly() {
        return nightly;
    }

    @JsonProperty("nightly")
    public void setNightly(Boolean nightly) {
        this.nightly = nightly;
    }

    @JsonProperty("releaseNightly")
    public Boolean getReleaseNightly() {
        return releaseNightly;
    }

    @JsonProperty("releaseNightly")
    public void setReleaseNightly(Boolean releaseNightly) {
        this.releaseNightly = releaseNightly;
    }

    @JsonProperty("activeRc")
    public Boolean getActiveRc() {
        return activeRc;
    }

    @JsonProperty("activeRc")
    public void setActiveRc(Boolean activeRc) {
        this.activeRc = activeRc;
    }

    @JsonProperty("rcFor")
    public String getRcFor() {
        return rcFor;
    }

    @JsonProperty("rcFor")
    public void setRcFor(String rcFor) {
        this.rcFor = rcFor;
    }

    @JsonProperty("milestoneFor")
    public String getMilestoneFor() {
        return milestoneFor;
    }

    @JsonProperty("milestoneFor")
    public void setMilestoneFor(String milestoneFor) {
        this.milestoneFor = milestoneFor;
    }

    @JsonProperty("broken")
    public Boolean getBroken() {
        return broken;
    }

    @JsonProperty("broken")
    public void setBroken(Boolean broken) {
        this.broken = broken;
    }

    @JsonProperty("downloadUrl")
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @JsonProperty("downloadUrl")
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @JsonProperty("checksumUrl")
    public String getChecksumUrl() {
        return checksumUrl;
    }

    @JsonProperty("checksumUrl")
    public void setChecksumUrl(String checksumUrl) {
        this.checksumUrl = checksumUrl;
    }

    @JsonProperty("wrapperChecksumUrl")
    public String getWrapperChecksumUrl() {
        return wrapperChecksumUrl;
    }

    @JsonProperty("wrapperChecksumUrl")
    public void setWrapperChecksumUrl(String wrapperChecksumUrl) {
        this.wrapperChecksumUrl = wrapperChecksumUrl;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
