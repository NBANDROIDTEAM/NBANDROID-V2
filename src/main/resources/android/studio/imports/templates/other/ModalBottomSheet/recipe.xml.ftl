<?xml version="1.0"?>
<recipe>

    <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+"/>
    <dependency mavenUrl="com.android.support:design:${buildApi}.+" />
    <dependency mavenUrl="com.android.support:recyclerview-v7:${buildApi}.+" />

    <instantiate from="root/res/layout/fragment_item_list_dialog.xml"
                 to="${escapeXmlAttribute(resOut)}/layout/${listLayout}.xml" />
    <instantiate from="root/res/layout/fragment_item_list_dialog_item.xml"
                 to="${escapeXmlAttribute(resOut)}/layout/${itemLayout}.xml" />
    <instantiate from="root/src/app_package/ItemListDialogFragment.java.ftl"
                 to="${escapeXmlAttribute(srcOut)}/${className}.java" />

    <open file="${escapeXmlAttribute(srcOut)}/${className}.java" />

    <merge from="root/res/values/dimens.xml"
             to="${escapeXmlAttribute(resOut)}/values/dimens.xml" />

</recipe>
