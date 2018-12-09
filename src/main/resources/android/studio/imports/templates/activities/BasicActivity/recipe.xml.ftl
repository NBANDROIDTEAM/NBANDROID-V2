<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <#include "../common/recipe_manifest.xml.ftl" />

<#if useFragment>
    <#include "recipe_fragment.xml.ftl" />
<#else>
    <#include "../common/recipe_simple.xml.ftl" />
</#if>

<#if hasAppBar>
    <#include "../common/recipe_app_bar.xml.ftl" />
</#if>

    <instantiate from="root/src/app_package/SimpleActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />

<#if useFragment>
    <open file="${escapeXmlAttribute(resOut)}/layout/${fragmentLayoutName}.xml" />
<#else>
    <open file="${escapeXmlAttribute(resOut)}/layout/${simpleLayoutName}.xml" />
</#if>

</recipe>
