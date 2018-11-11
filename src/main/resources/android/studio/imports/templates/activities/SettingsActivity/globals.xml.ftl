<?xml version="1.0"?>
<globals>
    <global id="projectOut" value="." />
    <global id="hasNoActionBar" type="boolean" value="false" />
    <#include "../common/common_globals.xml.ftl" />
    <global id="isLauncher" type="boolean" value="${isNewProject?string}" />
<#if appCompatActivity>
    <global id="preferenceSuperClass" type="string" value="AppCompatPreferenceActivity" />
    <global id="PreferenceSupport" type = "string" value="Support" />
    <global id="PreferenceActionBarClassFqcn" type = "string" value="android.support.v7.app.ActionBar" />
<#else>
    <global id="preferenceSuperClass" type="string" value="PreferenceActivity" />
    <global id="PreferenceSupport" type = "string" value="" />
    <global id="PreferenceActionBarClassFqcn" type = "string" value="android.app.ActionBar" />
</#if>
</globals>
