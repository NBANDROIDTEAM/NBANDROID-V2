<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />
    <merge from="root/res/values/strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

    <copy from="root/res/layout-v17/dream.xml"
          to="${escapeXmlAttribute(resOut)}/layout-v17/${class_name}.xml" />

    <instantiate from="root/src/app_package/DreamService.java.ftl"
                 to="${escapeXmlAttribute(srcOut)}/${className}.java" />

<#if configurable>
    <copy from="root/res/xml/dream_prefs.xml"
          to="${escapeXmlAttribute(resOut)}/xml/${prefs_name}.xml" />

    <instantiate from="root/src/app_package/SettingsActivity.java.ftl"
                 to="${escapeXmlAttribute(srcOut)}/${settingsClassName}.java" />

    <instantiate from="root/res/xml/xml_dream.xml.ftl"
                 to="${escapeXmlAttribute(resOut)}/xml/${info_name}.xml" />
</#if>

    <open file="${escapeXmlAttribute(srcOut)}/${className}.java" />

</recipe>
