<?xml version="1.0"?>
<globals>
    <#include "root://activities/common/common_globals.xml.ftl" />
    <#assign useSupport=appCompat>
    <global id="useSupport" type="boolean" value="true" />
    <global id="SupportPackage" value=".support.v4" />
    <global id="resOut" value="${resDir}" />
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
</globals>
