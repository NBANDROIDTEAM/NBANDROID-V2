<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />

    <#if appCompat && !(hasDependency('com.android.support:appcompat-v7'))>
        <dependency mavenUrl="com.android.support:appcompat-v7:${buildApi}.+"/>
    </#if>

    <#if !appCompat && !(hasDependency('com.android.support:support-v4'))>
        <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+"/>
    </#if>

    <#if !appCompat && !(hasDependency('com.android.support:support-v13'))>
        <dependency mavenUrl="com.android.support:support-v13:${buildApi}.+"/>
    </#if>

    <#if hasAppBar && !(hasDependency('com.android.support:design'))>
        <dependency mavenUrl="com.android.support:design:${buildApi}.+"/>
    </#if>

    <#if !(hasDependency('com.android.support.constraint:constraint-layout'))>
        <dependency mavenUrl="com.android.support.constraint:constraint-layout:+" />
    </#if>

    <#include "../common/recipe_manifest.xml.ftl" />

    <instantiate from="root/res/menu/main.xml.ftl"
            to="${escapeXmlAttribute(resOut)}/menu/${menuName}.xml" />

    <merge from="root/res/values/strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

    <merge from="root/res/values/dimens.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />
    <merge from="root/res/values-w820dp/dimens.xml"
             to="${escapeXmlAttribute(resOut)}/values-w820dp/dimens.xml" />

    <#if hasAppBar>
        <#include "../common/recipe_no_actionbar.xml.ftl" />
    </#if>

    <!-- Decide what kind of layout(s) to add -->
    <#if hasAppBar>
        <instantiate from="root/res/layout/app_bar_activity.xml.ftl"
                       to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
    <#elseif hasViewPager>
        <instantiate from="root/res/layout/activity_pager.xml.ftl"
                       to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
    <#else>
        <instantiate from="root/res/layout/activity_fragment_container.xml.ftl"
                       to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
    </#if>

    <instantiate from="root/res/layout/fragment_simple.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${fragmentLayoutName}.xml" />

    <!-- Decide which activity code to add -->
    <#if hasViewPager || hasAppBar>
        <!-- kotlin android extensions cannot find views from android.R.layout.simple_list_item_1,
             so we need to add a new list_item that contains a TextView -->
        <#if generateKotlin && features == 'spinner'>
             <copy from="root/res/layout/list_item.xml"
                     to="${escapeXmlAttribute(resOut)}/layout/list_item.xml" />
        </#if>
        <instantiate from="root/src/app_package/TabsAndPagerActivity.${ktOrJavaExt}.ftl"
                       to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
        <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <#else>
        <instantiate from="root/src/app_package/DropdownActivity.${ktOrJavaExt}.ftl"
                       to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    </#if>

    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <open file="${escapeXmlAttribute(resOut)}/layout/${fragmentLayoutName}.xml" />
</recipe>
