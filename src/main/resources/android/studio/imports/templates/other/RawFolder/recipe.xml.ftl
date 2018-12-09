<?xml version="1.0"?>
<recipe>

  <#if remapFolder>
    <mkdir at="${escapeXmlAttribute(projectOut)}/${escapeXmlAttribute(newLocation)}" />
  <#else>
    <mkdir at="${escapeXmlAttribute(resOut)}/raw/" />
  </#if>

</recipe>
