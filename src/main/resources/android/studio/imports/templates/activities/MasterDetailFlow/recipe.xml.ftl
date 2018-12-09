<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+" />
    <dependency mavenUrl="com.android.support:recyclerview-v7:${buildApi}.+" />

    <#if hasAppBar>
      <dependency mavenUrl="com.android.support:design:${buildApi}.+"/>
    </#if>

    <#include "../common/recipe_theme.xml.ftl" />

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <#if minApiLevel lt 13>
      <merge from="root/res/values-w900dp/refs.xml.ftl"
               to="${escapeXmlAttribute(resOut)}/values-w900dp/refs.xml" />
    </#if>

    <#if (isInstantApp!false) || (isDynamicFeature!false)>
      <merge from="root/res/values/strings.xml.ftl"
               to="${escapeXmlAttribute(baseFeatureResOut)}/values/strings.xml" />
    <#else>
      <merge from="root/res/values/strings.xml.ftl"
               to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
    </#if>

    <merge from="root/res/values/dimens.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />
    <#if hasAppBar>
      <#include "../common/recipe_no_actionbar.xml.ftl" />
    </#if>

    <instantiate from="root/res/layout/activity_item_detail.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/activity_${detail_name}.xml" />
    <instantiate from="root/res/layout/fragment_item_list.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${item_list_layout}.xml" />
    <#if minApiLevel lt 13>
      <instantiate from="root/res/layout/fragment_item_list_twopane.xml.ftl"
                     to="${escapeXmlAttribute(resOut)}/layout/${item_list_layout}_twopane.xml" />
    <#else>
      <instantiate from="root/res/layout/fragment_item_list_twopane.xml.ftl"
                     to="${escapeXmlAttribute(resOut)}/layout-w900dp/${item_list_layout}.xml" />
    </#if>
    <instantiate from="root/res/layout/item_list_content.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${item_list_content_layout}.xml" />
    <instantiate from="root/res/layout/fragment_item_detail.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${detail_name}.xml" />
    <#if hasAppBar>
      <instantiate from="root/res/layout/activity_item_list_app_bar.xml.ftl"
                     to="${escapeXmlAttribute(resOut)}/layout/activity_${item_list_layout}.xml" />
    </#if>

    <instantiate from="root/src/app_package/ContentDetailActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${DetailName}Activity.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/ContentDetailFragment.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${DetailName}Fragment.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/ContentListActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${CollectionName}Activity.${ktOrJavaExt}" />
    <#include "../common/recipe_dummy_content.xml.ftl" />

    <open file="${escapeXmlAttribute(srcOut)}/${DetailName}Fragment.${ktOrJavaExt}" />
    <open file="${escapeXmlAttribute(resOut)}/layout/fragment_${detail_name}.xml" />
</recipe>
