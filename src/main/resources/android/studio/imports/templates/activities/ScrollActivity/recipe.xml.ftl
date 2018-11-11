<?xml version="1.0"?>
<recipe>
<#if appCompat && !(hasDependency('com.android.support:appcompat-v7'))>
    <dependency mavenUrl="com.android.support:appcompat-v7:${buildApi}.+"/>
</#if>
<#if hasAppBar && !(hasDependency('com.android.support:design'))>
    <dependency mavenUrl="com.android.support:design:${buildApi}.+"/>
</#if>
<#if !appCompat && !(hasDependency('com.android.support:support-v4'))>
    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+" />
</#if>

    <#include "../common/recipe_manifest.xml.ftl" />

    <merge from="root/res/values/strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
    <merge from="root/res/values/dimens.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />

<#if hasAppBar>
    <#include "../common/recipe_no_actionbar.xml.ftl" />
    <#include "../common/recipe_simple_menu.xml.ftl" />
    <instantiate from="root/res/layout/app_bar.xml.ftl"
                 to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
    <instantiate from="root/res/layout/simple.xml.ftl"
                 to="${escapeXmlAttribute(resOut)}/layout/${simpleLayoutName}.xml" />

    <open file="${escapeXmlAttribute(resOut)}/layout/${simpleLayoutName}.xml" />
<#else>
    <instantiate from="root/res/layout/simple.xml.ftl"
                 to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />

    <open file="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
</#if>

    <instantiate from="root/src/app_package/ScrollActivity.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />

    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />
</recipe>
