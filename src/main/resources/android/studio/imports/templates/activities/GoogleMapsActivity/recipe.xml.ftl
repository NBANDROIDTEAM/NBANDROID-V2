<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.google.android.gms:play-services-maps:+" />
    <dependency mavenUrl="com.android.support:appcompat-v7:${targetApi}.+" />

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <instantiate from="root/res/layout/activity_map.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />

    <#if isInstantApp || isDynamicFeature>
        <#assign finalResOut="${escapeXmlAttribute(baseFeatureResOut)}">
        <#assign finalDebugResOut="${escapeXmlAttribute(baseFeatureOut)}/src/debug/res">
        <#assign finalReleaseResOut="${escapeXmlAttribute(baseFeatureOut)}/src/release/res">
    <#else>
        <#assign finalResOut="${escapeXmlAttribute(resOut)}">
        <#assign finalDebugResOut="${escapeXmlAttribute(projectOut)}/src/debug/res">
        <#assign finalReleaseResOut="${escapeXmlAttribute(projectOut)}/src/release/res">
    </#if>

    <merge from="root/res/values/strings.xml.ftl"
             to="${finalResOut}/values/strings.xml" />

    <instantiate from="root/src/app_package/MapActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />

    <merge from="root/debugRes/values/google_maps_api.xml.ftl"
             to="${finalDebugResOut}/values/google_maps_api.xml" />

    <merge from="root/releaseRes/values/google_maps_api.xml.ftl"
             to="${finalReleaseResOut}/values/google_maps_api.xml" />

    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />

    <!-- Display the API key instructions. -->
    <open file="${finalDebugResOut}/values/google_maps_api.xml" />
</recipe>
