<recipe folder="root://activities/common">
<#if (isInstantApp!false) || (isDynamicFeature!false)>
    <merge from="root/res/values/manifest_strings.xml.ftl"
             to="${escapeXmlAttribute(baseFeatureResOut)}/values/strings.xml" />
<#else>
    <merge from="root/res/values/manifest_strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
</#if>
</recipe>
