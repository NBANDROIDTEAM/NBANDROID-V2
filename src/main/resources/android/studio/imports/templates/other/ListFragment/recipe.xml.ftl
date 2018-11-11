<?xml version="1.0"?>
<recipe>

    <#if useSupport><dependency mavenUrl="com.android.support:support-v4:${buildApi}.+"/></#if>
    <dependency mavenUrl="com.android.support:recyclerview-v7:${buildApi}.+" />

    <instantiate from="root/res/layout/fragment_list.xml"
                 to="${escapeXmlAttribute(resOut)}/layout/${fragment_layout_list}.xml" />
    <instantiate from="root/res/layout/item_list_content.xml"
                 to="${escapeXmlAttribute(resOut)}/layout/${fragment_layout}.xml" />
    <instantiate from="root/src/app_package/ListFragment.java.ftl"
                 to="${escapeXmlAttribute(srcOut)}/${className}.java" />
    <instantiate from="root/src/app_package/RecyclerViewAdapter.java.ftl"
                 to="${escapeXmlAttribute(srcOut)}/${adapterClassName}.java" />

    <#include "../../activities/common/recipe_dummy_content.xml.ftl" />

    <open file="${escapeXmlAttribute(srcOut)}/${className}.java" />

    <merge from="root/res/values/dimens.xml"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />
</recipe>
