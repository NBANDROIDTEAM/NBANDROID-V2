<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="androidx.annotation:annotation:+" />
    <dependency mavenUrl="androidx.slice:slice-builders:+" />
    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />
    <instantiate from="root/src/app_package/SliceProvider.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${className}.${ktOrJavaExt}" />
    <open file="${escapeXmlAttribute(srcOut)}/${className}.${ktOrJavaExt}" />
</recipe>
