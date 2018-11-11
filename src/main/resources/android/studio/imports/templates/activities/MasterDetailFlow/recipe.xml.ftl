<?xml version="1.0"?>
<recipe>
    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+" />
    <dependency mavenUrl="com.android.support:recyclerview-v7:${buildApi}.+" />

    <#if hasAppBar>
      <dependency mavenUrl="com.android.support:design:${buildApi}.+"/>
    </#if>

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <#if minApiLevel lt 13>
      <merge from="root/res/values-w900dp/refs.xml.ftl"
               to="${escapeXmlAttribute(resOut)}/values-w900dp/refs.xml" />
    </#if>
    <merge from="root/res/values/strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
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

    <instantiate from="root/src/app_package/ContentDetailActivity.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${DetailName}Activity.java" />
    <instantiate from="root/src/app_package/ContentDetailFragment.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${DetailName}Fragment.java" />
    <instantiate from="root/src/app_package/ContentListActivity.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${CollectionName}Activity.java" />
    <#include "../common/recipe_dummy_content.xml.ftl" />

    <open file="${escapeXmlAttribute(srcOut)}/${DetailName}Fragment.java" />
    <open file="${escapeXmlAttribute(resOut)}/layout/fragment_${detail_name}.xml" />
</recipe>
