<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+" />
<#if minApiLevel lt 21>
    <dependency mavenUrl="com.android.support:support-vector-drawable:${buildApi}.+" />

    <merge from="root/build.gradle"
             to="${escapeXmlAttribute(projectOut)}/build.gradle" />
</#if>

    <#include "../common/recipe_manifest.xml.ftl" />

    <copy from="root/res/xml/pref_data_sync.xml"
            to="${escapeXmlAttribute(resOut)}/xml/pref_data_sync.xml" />
    <copy from="root/res/xml/pref_general.xml"
            to="${escapeXmlAttribute(resOut)}/xml/pref_general.xml" />
    <copy from="root/res/xml/pref_notification.xml"
            to="${escapeXmlAttribute(resOut)}/xml/pref_notification.xml" />

    <instantiate from="root/res/xml/pref_headers.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/xml/pref_headers.xml" />

    <copy from="root/res/drawable"
            to="${escapeXmlAttribute(resOut)}/drawable" />

    <merge from="root/res/values/pref_strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

    <instantiate from="root/src/app_package/SettingsActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <#if appCompatActivity>
        <instantiate from="root/src/app_package/AppCompatPreferenceActivity.${ktOrJavaExt}.ftl"
                       to="${escapeXmlAttribute(srcOut)}/AppCompatPreferenceActivity.${ktOrJavaExt}" />
    </#if>
    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
</recipe>
