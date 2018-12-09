<?xml version="1.0"?>
<recipe>
    <mkdir at="${escapeXmlAttribute(projectOut)}" />
    <mkdir at="${escapeXmlAttribute(srcOut)}" />
    <merge from="root/settings.gradle.ftl"
             to="${escapeXmlAttribute(topOut)}/settings.gradle" />
    <merge from="root/base-build.gradle.ftl"
             to="${baseFeatureDir}/build.gradle" />
    <merge from="root/res/values/strings.xml.ftl"
                   to="${escapeXmlAttribute(baseFeatureResDir)}/values/strings.xml" />
    <instantiate from="root/build.gradle.ftl"
                   to="${escapeXmlAttribute(projectOut)}/build.gradle" />
    <instantiate from="root/AndroidManifest.xml.ftl"
                   to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />
<#if makeIgnore>
    <copy from="root://gradle-projects/common/gitignore"
            to="${escapeXmlAttribute(projectOut)}/.gitignore" />
</#if>

</recipe>
