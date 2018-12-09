<?xml version="1.0"?>
<globals>
    <#assign Collection=extractLetters(objectKind)>
    <#assign collection_name=Collection?lower_case + "_list">
    <#include "../common/common_globals.xml.ftl" />
    <global id="CollectionName" value="${Collection}List" />
    <global id="collection_name" value="${collection_name}" />
    <global id="DetailName" value="${Collection}Detail" />
    <global id="detail_name" value="${Collection?lower_case}_detail" />
    <global id="item_list_layout" value="<#if !appCompatActivity>activity_</#if>${collection_name}" />
    <global id="item_list_content_layout" value="${collection_name}_content" />
</globals>
