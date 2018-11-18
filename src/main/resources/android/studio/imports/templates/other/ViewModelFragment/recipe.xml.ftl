<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+"/>
    <dependency mavenUrl="android.arch.lifecycle:extensions:1.+"/>

    <instantiate from="root/res/layout/blank_fragment.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${escapeXmlAttribute(layoutName)}.xml" />

    <open file="${escapeXmlAttribute(resOut)}/layout/${escapeXmlAttribute(layoutName)}.xml" />

    <instantiate from="root/src/app_package/BlankFragment.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${className}.${ktOrJavaExt}" />

    <open file="${escapeXmlAttribute(srcOut)}/${className}.${ktOrJavaExt}" />

    <instantiate from="root/src/app_package/BlankViewModel.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${viewModelName}.${ktOrJavaExt}" />

    <open file="${escapeXmlAttribute(srcOut)}/${viewModelName}.${ktOrJavaExt}" />

</recipe>
