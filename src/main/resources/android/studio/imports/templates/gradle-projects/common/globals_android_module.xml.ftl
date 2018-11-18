<globals>
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="testOut" value="androidTest/${slashedPackageName(packageName)}" />
    <global id="unitTestOut" value="${escapeXmlAttribute(projectOut)}/src/test/java/${slashedPackageName(packageName)}" />
    <global id="nativeSrcOut" value="${escapeXmlAttribute(projectOut)}/src/main/cpp" />
    <global id="manifestOut" value="${manifestDir}" />
    <global id="resOut" value="${resDir}" />
</globals>
