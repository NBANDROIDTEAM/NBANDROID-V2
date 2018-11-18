<?xml version="1.0"?>
<globals>
    <#include "root://gradle-projects/common/globals.xml.ftl" />
    <#include "root://gradle-projects/common/globals_android_module.xml.ftl" />
    <#assign useAndroidX=isAndroidxEnabled() && (buildApi gte 28)>

    <global id="isLibraryProject" type="boolean" value="${(!(isInstantApp!false) && (isLibraryProject!false))?string}" />
    <global id="isApplicationProject" type="boolean" value="${(!(isInstantApp!false) && !(isLibraryProject!false))?string}" />
    <global id="isInstantAppProject" type="boolean" value="${(!(isInstantApp!false) && !(isLibraryProject!false))?string}" />

    <global id="hasInstantAppWrapper" type="boolean" value="${(isInstantApp!false)?string}" />
    <global id="hasMonolithicAppWrapper" type="boolean" value="${(isInstantApp!false)?string}" />

    <global id="baseFeatureName" type="string" value="base" />
    <global id="isBaseFeature" type="boolean" value="false" />
    <global id="instantAppProjectName" type="string" value="instantapp" />
    <global id="monolithicAppProjectName" type="string" value="app" />
    <global id="monolithicModuleName" type="string" value="" />
    <global id="instantAppPackageName" type="string" value="${packageName}.instantapp" />

    <global id="instantAppOut" type="string" value="${escapeXmlAttribute(instantAppDir!'./' + (instantAppProjectName!'instantapp'))}" />
    <global id="monolithicAppOut" type="string" value="${escapeXmlAttribute(monolithicAppDir!'./' + (monolithicAppProjectName!'app'))}" />
    <global id="baseFeatureOut" type="string" value="${escapeXmlAttribute(baseFeatureDir!'./base')}" />
    <global id="baseFeatureResOut" type="string" value="${escapeXmlAttribute(baseFeatureResDir!'./base/src/main/res')}" />
    <global id="useAndroidX" type="boolean" value="${useAndroidX?string}" />
    <#include "root://activities/common/kotlin_globals.xml.ftl" />
</globals>
