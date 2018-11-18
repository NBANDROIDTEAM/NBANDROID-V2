<globals>
    <global id="projectOut" value="." />
    <global id="manifestOut" value="${manifestDir}" />
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="debugResOut" value="${escapeXmlAttribute(projectOut)}/src/debug/res" />
    <global id="releaseResOut" value="${escapeXmlAttribute(projectOut)}/src/release/res" />
    <global id="resOut" value="${resDir}" />

    <#if Mobileincluded!false>
        <global id="appManifestOut" value="${escapeXmlAttribute(topOut)}/${MobileprojectName}/${manifestDir}" />
    <#else>
        <#assign appManifestDir=getAppManifestDir()!"">
        <#if appManifestDir?length gt 0>
            <global id="appManifestOut" value="${escapeXmlAttribute(appManifestDir)}" />
        </#if>
    </#if>
    <#include "root://activities/common/kotlin_globals.xml.ftl" />
</globals>
