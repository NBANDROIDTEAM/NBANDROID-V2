<?xml version="1.0"?>
<recipe>

  <#if remapFolder>
    <mkdir at="${escapeXmlAttribute(projectOut)}/${escapeXmlAttribute(newLocation)}" />
    <#if isGradleComponentPluginUsed>
      <merge from="root/component-build.gradle.ftl"
               to="${escapeXmlAttribute(projectOut)}/build.gradle" />
    <#else>
      <merge from="root/build.gradle.ftl"
               to="${escapeXmlAttribute(projectOut)}/build.gradle" />
    </#if>
  <#else>
      <mkdir at="${escapeXmlAttribute(manifestOut)}/aidl/" />
  </#if>

</recipe>
