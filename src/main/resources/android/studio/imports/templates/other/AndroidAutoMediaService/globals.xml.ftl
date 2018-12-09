<?xml version="1.0"?>
<globals>
    <#include "../common/globals.xml.ftl" />
    <global id="manifestOut" value="${manifestDir}" />
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="resOut" value="${resDir}" />
    <#include "root://activities/common/kotlin_globals.xml.ftl" />
</globals>
