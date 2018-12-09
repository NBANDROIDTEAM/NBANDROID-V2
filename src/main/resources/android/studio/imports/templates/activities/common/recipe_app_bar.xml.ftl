<recipe folder="root://activities/common">

<#if !(hasDependency('com.android.support:appcompat-v7'))>
    <dependency mavenUrl="com.android.support:appcompat-v7:${buildApi}.+"/>
</#if>

<#if !(hasDependency('com.android.support:design'))>
    <dependency mavenUrl="com.android.support:design:${buildApi}.+"/>
</#if>

    <instantiate from="root/res/layout/app_bar.xml.ftl"
                 to="${escapeXmlAttribute(resOut)}/layout/${appBarLayoutName}.xml" />

    <merge from="root/res/values/app_bar_dimens.xml"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />

    <#include "recipe_no_actionbar.xml.ftl" />
</recipe>
