<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <#if useSupport><dependency mavenUrl="com.android.support:support-v4:${buildApi}.+"/></#if>
    <dependency mavenUrl="com.android.support:recyclerview-v7:${buildApi}.+" />

    <instantiate from="root/res/layout/fragment_list.xml"
                 to="${escapeXmlAttribute(resOut)}/layout/${fragment_layout_list}.xml" />
    <instantiate from="root/res/layout/item_list_content.xml"
                 to="${escapeXmlAttribute(resOut)}/layout/${fragment_layout}.xml" />

    <instantiate from="root/src/app_package/ListFragment.${ktOrJavaExt}.ftl"
                 to="${escapeXmlAttribute(srcOut)}/${className}.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/RecyclerViewAdapter.${ktOrJavaExt}.ftl"
                 to="${escapeXmlAttribute(srcOut)}/${adapterClassName}.${ktOrJavaExt}" />
    <#include "../../activities/common/recipe_dummy_content.xml.ftl" />

    <open file="${escapeXmlAttribute(srcOut)}/${className}.${ktOrJavaExt}" />

    <merge from="root/res/values/dimens.xml"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />
</recipe>
