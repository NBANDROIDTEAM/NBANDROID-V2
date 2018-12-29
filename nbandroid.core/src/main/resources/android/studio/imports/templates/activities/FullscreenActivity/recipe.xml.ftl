<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+" />

    <#include "../common/recipe_theme.xml.ftl" />
    <#include "../common/recipe_full_screen_actionbar.xml.ftl" />

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <instantiate from="root/res/layout/activity_fullscreen.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />

    <#if (isInstantApp!false) || (isDynamicFeature!false)>
      <merge from="root/res/values/strings.xml.ftl"
               to="${escapeXmlAttribute(baseFeatureResOut)}/values/strings.xml" />
    <#else>
      <merge from="root/res/values/strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
    </#if>

    <instantiate from="root/src/app_package/FullscreenActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />

    <open file="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
</recipe>
