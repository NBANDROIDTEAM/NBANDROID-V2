<?xml version="1.0"?>
<globals>
    <#include "../common/globals.xml.ftl" />
    <global id="resOut" value="${resDir}" />
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="view_class" value="${camelCaseToUnderscore(viewClass)}" />
    <#include "root://activities/common/kotlin_globals.xml.ftl" />
</globals>
