<recipe folder="root://activities/common">
    <#if isInstantApp || isDynamicFeature>
        <#assign finalResOut="${escapeXmlAttribute(baseFeatureResOut)}">
    <#else>
        <#assign finalResOut="${escapeXmlAttribute(resOut)}">
    </#if>

    <merge from="root/res/values/full_screen_styles.xml.ftl"
             to="${finalResOut}/values/styles.xml" />
    <merge from="root/res/values/full_screen_attrs.xml"
             to="${finalResOut}/values/attrs.xml" />
    <merge from="root/res/values/full_screen_colors.xml"
             to="${finalResOut}/values/colors.xml" />
</recipe>
