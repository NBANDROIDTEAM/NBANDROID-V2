<?xml version="1.0"?>
<recipe>
    <#include "../common/recipe_manifest.xml.ftl" />

<#if useFragment>
    <#include "recipe_fragment.xml.ftl" />
<#else>
    <#include "../common/recipe_simple.xml.ftl" />
</#if>

<#if hasAppBar>
    <#include "../common/recipe_app_bar.xml.ftl" />
</#if>

    <instantiate from="root/src/app_package/SimpleActivity.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />

    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />
<#if useFragment>
    <open file="${escapeXmlAttribute(resOut)}/layout/${fragmentLayoutName}.xml" />
<#else>
    <open file="${escapeXmlAttribute(resOut)}/layout/${simpleLayoutName}.xml" />
</#if>

</recipe>
