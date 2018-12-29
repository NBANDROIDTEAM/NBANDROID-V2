/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.distributions;

/**
 *
 * @author arsi
 */
public class DistributionPOJO implements Comparable<DistributionPOJO> {

    private String distributionPercentage;

    private String apiLevel;

    private String name;

    private DescriptionBlocksPOJO[] descriptionBlocks;

    private String url;

    private String version;

    public String getDistributionPercentage() {
        return distributionPercentage;
    }

    public void setDistributionPercentage(String distributionPercentage) {
        this.distributionPercentage = distributionPercentage;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DescriptionBlocksPOJO[] getDescriptionBlocks() {
        return descriptionBlocks;
    }

    public void setDescriptionBlocks(DescriptionBlocksPOJO[] descriptionBlocks) {
        this.descriptionBlocks = descriptionBlocks;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ClassPojo [distributionPercentage = " + distributionPercentage + ", apiLevel = " + apiLevel + ", name = " + name + ", descriptionBlocks = " + descriptionBlocks + ", url = " + url + ", version = " + version + "]";
    }

    @Override
    public int compareTo(DistributionPOJO o) {
        try {
            return Integer.valueOf(o.getApiLevel()).compareTo(Integer.valueOf(getApiLevel()));
        } catch (NumberFormatException numberFormatException) {
            return o.getApiLevel().compareToIgnoreCase(o.getApiLevel());
        }
    }
}
