<?xml version="1.0"?>
<recipe>
    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <#if useCustomTheme>
      <merge from="root/res/values-v21/styles.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values-v21/styles.xml" />
    </#if>

    <copy from="root/res/xml/automotive_app_desc.xml"
          to="${escapeXmlAttribute(resOut)}/xml/automotive_app_desc.xml" />

    <instantiate from="root/src/app_package/MusicService.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${mediaBrowserServiceName}.java" />

    <open file="${escapeXmlAttribute(srcOut)}/${mediaBrowserServiceName}.java" />

    <#if useCustomTheme>
      <open file="${escapeXmlAttribute(resOut)}/values-v21/styles.xml" />
    </#if>
</recipe>
