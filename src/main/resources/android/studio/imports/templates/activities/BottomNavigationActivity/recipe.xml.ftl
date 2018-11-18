<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.android.support:design:${buildApi}.+" />
    <dependency mavenUrl="com.android.support.constraint:constraint-layout:+" />

<#if minApiLevel lt 21>
    <dependency mavenUrl="com.android.support:support-vector-drawable:${buildApi}.+" />

    <merge from="root/build.gradle"
             to="${escapeXmlAttribute(projectOut)}/build.gradle" />
</#if>

    <#include "../common/recipe_manifest.xml.ftl" />

    <copy from="root/res/drawable"
            to="${escapeXmlAttribute(resOut)}/drawable" />

    <instantiate from="root/src/app_package/MainActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <instantiate from="root/res/layout/activity_main.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />

    <merge from="root/res/values/dimens.xml"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />
    <merge from="root/res/values/strings.xml"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
    <copy from="root/res/menu/navigation.xml"
            to="${escapeXmlAttribute(resOut)}/menu/navigation.xml" />

    <open file="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
</recipe>
