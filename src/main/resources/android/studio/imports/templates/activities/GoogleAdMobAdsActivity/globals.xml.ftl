<?xml version="1.0"?>
<globals>
    <global id="manifestOut" value="${manifestDir}" />
<#if hasDependency('com.android.support:appcompat-v7')>
    <global id="appCompat" type="boolean" value="true" />
    <global id="superClass" type="string" value="<#if buildApi gte 22>AppCompat<#else>ActionBar</#if>Activity"/>
    <global id="superClassFqcn" type="string" value="android.support.v7.app.<#if buildApi gte 22>AppCompat<#else>ActionBar</#if>Activity"/>
<#else>
    <global id="appCompat" type="boolean" value="false" />
    <global id="superClass" type="string" value="Activity"/>
    <global id="superClassFqcn" type="string" value="android.app.Activity"/>
</#if>
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="resOut" value="${resDir}" />
    <global id="relativePackage" value="<#if relativePackage?has_content>${relativePackage}<#else>${packageName}</#if>" />
</globals>
