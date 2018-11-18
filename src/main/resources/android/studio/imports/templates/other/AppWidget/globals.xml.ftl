<?xml version="1.0"?>
<globals>
    <#include "../common/globals.xml.ftl" />
    <global id="manifestOut" value="${manifestDir}" />
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="resOut" value="${resDir}" />
    <global id="class_name" value="${camelCaseToUnderscore(className)}" />
</globals>
