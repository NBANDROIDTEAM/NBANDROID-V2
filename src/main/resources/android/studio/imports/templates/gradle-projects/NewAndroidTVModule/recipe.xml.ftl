<?xml version="1.0"?>
<recipe>
    <dependency mavenUrl="com.android.support:leanback-v17:${buildApi}.+" />
    <mkdir at="${escapeXmlAttribute(srcOut)}" />

    <mkdir at="${escapeXmlAttribute(projectOut)}/libs" />

    <merge from="root/settings.gradle.ftl"
             to="${escapeXmlAttribute(topOut)}/settings.gradle" />
    <instantiate from="root/build.gradle.ftl"
                   to="${escapeXmlAttribute(projectOut)}/build.gradle" />
    <instantiate from="root/AndroidManifest.xml.ftl"
                   to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

<#if copyIcons>
    <mkdir  at="${escapeXmlAttribute(resOut)}/drawable" />
    <copy from="root/res/mipmap-hdpi"
            to="${escapeXmlAttribute(resOut)}/mipmap-hdpi" />
    <copy from="root/res/mipmap-mdpi"
            to="${escapeXmlAttribute(resOut)}/mipmap-mdpi" />
    <copy from="root/res/mipmap-xhdpi"
            to="${escapeXmlAttribute(resOut)}/mipmap-xhdpi" />
    <copy from="root/res/mipmap-xxhdpi"
            to="${escapeXmlAttribute(resOut)}/mipmap-xxhdpi" />
</#if>
<#if makeIgnore>
    <copy from="root://gradle-projects/common/gitignore"
            to="${escapeXmlAttribute(projectOut)}/.gitignore" />
</#if>
    <#include "root://gradle-projects/common/proguard_recipe.xml.ftl"/>
    <instantiate from="root/res/values/styles.xml"
                   to="${escapeXmlAttribute(resOut)}/values/styles.xml" />

    <instantiate from="root/res/values/strings.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

</recipe>
