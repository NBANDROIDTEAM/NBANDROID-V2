<?xml version="1.0"?>
<globals>
    <#include "root://gradle-projects/common/globals.xml.ftl" />
    <global id="sdkDir" type="string" value="unset" />
    <global id="hasSdkDir" type="boolean" value="<#if sdkDir??>true<#else>false</#if>" />
    <global id="isLowMemory" type="boolean" value="false" />
    <global id="kotlinVersion" type="string" value="${kotlinVersion!'1.1.2'}" />
    <global id="addAndroidXSupport" type="boolean" value="false" />
    <#include "root://activities/common/kotlin_globals.xml.ftl" />
</globals>
