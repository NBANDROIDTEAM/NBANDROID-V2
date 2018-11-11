<?xml version="1.0"?>
<globals>
    <global id="hasViewPager" type="boolean" value="${(features != 'spinner')?string}" />
    <global id="viewContainer" type="string" value="<#if features == 'spinner'>android.support.v4.widget.NestedScrollView<#else>android.support.v4.view.ViewPager</#if>" />
    <#include "../common/common_globals.xml.ftl" />
</globals>
