<recipe folder="root://activities/common">
    <instantiate from="root/res/menu/simple_menu.xml.ftl"
                 to="${escapeXmlAttribute(resOut)}/menu/${menuName}.xml" />

    <merge from="root/res/values/simple_menu_strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
</recipe>

