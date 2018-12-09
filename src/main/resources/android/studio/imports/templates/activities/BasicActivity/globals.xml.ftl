<?xml version="1.0"?>
<globals>
    <global id="requireTheme" type="boolean" value="true" />
    <#include "../common/common_globals.xml.ftl" />
    <global id="simpleLayoutName" value="<#if appCompatActivity>${contentLayoutName}<#else>${layoutName}</#if>" />
    <global id="appBarLayoutName" value="${layoutName}" />
    <global id="fragmentClass" value="${activityClass}Fragment" />
</globals>
