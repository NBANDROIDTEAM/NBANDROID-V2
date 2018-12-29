<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+"/>
    <dependency mavenUrl="com.google.android.gms:play-services-plus:+"/>

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <instantiate from="root/res/layout/fragment_plus_one.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/fragment_${classToResource(className)}.xml" />

    <open file="${escapeXmlAttribute(resOut)}/layout/fragment_${classToResource(className)}.xml" />

    <open file="${escapeXmlAttribute(srcOut)}/${className}.java" />

    <instantiate from="root/src/app_package/PlusOneFragment.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${className}.java" />

</recipe>
