<recipe>
    <#include "../common/recipe_simple_menu.xml.ftl" />

    <dependency mavenUrl="com.android.support.constraint:constraint-layout:+" />

    <instantiate from="root/res/layout/activity_fragment_container.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${simpleLayoutName}.xml" />

    <instantiate from="root/res/layout/fragment_simple.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${fragmentLayoutName}.xml" />

    <instantiate from="root/src/app_package/SimpleActivityFragment.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${fragmentClass}.${ktOrJavaExt}" />
</recipe>
