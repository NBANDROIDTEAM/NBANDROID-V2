<?xml version="1.0"?>
<recipe>
    <merge from="root/settings.gradle.ftl"
             to="${escapeXmlAttribute(topOut)}/settings.gradle" />
    <instantiate from="root/build.gradle.ftl"
                   to="${escapeXmlAttribute(projectOut)}/build.gradle" />
    <instantiate from="root/src/library_package/Placeholder.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${className}.java" />
<#if makeIgnore>
    <copy from="root://gradle-projects/common/gitignore"
            to="${escapeXmlAttribute(projectOut)}/.gitignore" />
</#if>

	<mkdir at="${escapeXmlAttribute(projectOut)}/libs" />
</recipe>
