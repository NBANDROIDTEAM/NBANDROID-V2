<?xml version="1.0"?>
<recipe>

    <#if remapFile>
        <merge from="root/AndroidManifest.xml.ftl"
                 to="${escapeXmlAttribute(projectOut)}/${escapeXmlAttribute(newLocation)}" />
        <merge from="root/build.gradle.ftl"
                 to="${escapeXmlAttribute(projectOut)}/build.gradle" />
    <#else>
        <merge from="root/AndroidManifest.xml.ftl"
                 to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />
    </#if>

</recipe>
