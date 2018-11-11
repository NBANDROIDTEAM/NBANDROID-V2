<?xml version="1.0"?>
<globals>
    <#include "../common/common_globals.xml.ftl" />
<#if appCompatActivity>
    <global id="resIn" type="string" value="res-buildApi22" />
<#else>
    <global id="resIn" type="string" value="res" />
</#if>
    <global id="menuName" value="${classToResource(activityClass)}" />
    <global id="simpleLayoutName" value="<#if appCompatActivity>${contentLayoutName}<#else>${layoutName}</#if>" />
    <global id="includeImageDrawables" type="boolean" value="${(minApiLevel?number lt 21)?string}" />
</globals>
