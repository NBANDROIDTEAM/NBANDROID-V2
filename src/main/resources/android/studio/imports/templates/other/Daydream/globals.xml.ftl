<?xml version="1.0"?>
<globals>
    <#include "../common/globals.xml.ftl" />
    <global id="manifestOut" value="${manifestDir}" />
    <global id="resOut" value="${resDir}" />
    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="class_name" value="${classToResource(className)}" />
    <global id="info_name" value="${classToResource(className)}_info" />
    <global id="settingsClassName"  value="${className}SettingsActivity" />
    <global id="prefs_name" value="${classToResource(className)}_prefs" />
</globals>
