<?xml version="1.0"?>
<globals>
    <#include "../common/common_globals.xml.ftl" />
<#if hasDependency('com.android.support:appcompat-v7')>
    <global id="appCompat" type="boolean" value="true" />
    <global id="superClass" type="string" value="<#if buildApi gte 22>AppCompat<#else>ActionBar</#if>Activity"/>
    <global id="superClassFqcn" type="string" value="<#if buildApi gte 22>${getMaterialComponentName('android.support.v7.app.AppCompatActivity', useAndroidX)}<#else>android.support.v7.app.ActionBarActivity</#if>"/>
<#else>
    <global id="appCompat" type="boolean" value="false" />
    <global id="superClass" type="string" value="Activity"/>
    <global id="superClassFqcn" type="string" value="android.app.Activity"/>
</#if>
</globals>
