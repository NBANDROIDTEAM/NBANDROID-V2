<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <merge from="root/res/values/attrs.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/attrs_${view_class}.xml" />
    <instantiate from="root/res/layout/sample.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/sample_${view_class}.xml" />

    <instantiate from="root/src/app_package/CustomView.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${viewClass}.${ktOrJavaExt}" />

    <open file="${escapeXmlAttribute(srcOut)}/${viewClass}.${ktOrJavaExt}" />
    <open file="${escapeXmlAttribute(resOut)}/layout/sample_${view_class}.xml" />
</recipe>
