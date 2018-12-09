<?xml version="1.0"?>
<globals>
    <#include "../common/common_globals.xml.ftl" />
    <global id="hasViewPager" type="boolean" value="${(features != 'spinner')?string}" />
    <global id="viewContainer" type="string" value="<#if features == 'spinner'>${getMaterialComponentName('android.support.v4.widget.NestedScrollView', useAndroidX)}<#else>${getMaterialComponentName('android.support.v4.view.ViewPager', useAndroidX)}</#if>" />
    <global id="requireTheme" type="boolean" value="true" />
</globals>
