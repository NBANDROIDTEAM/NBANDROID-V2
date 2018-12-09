<recipe folder="root://activities/common">

    <#if requireTheme!false>
    <#include "recipe_theme.xml.ftl" />
    </#if>

    <#include "recipe_manifest_strings.xml.ftl" />

    <merge from="root/AndroidManifest.xml.ftl"
           to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />
</recipe>
