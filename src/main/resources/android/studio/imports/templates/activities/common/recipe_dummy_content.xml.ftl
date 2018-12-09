<recipe folder="root://activities/common">
    <instantiate from="root/src/app_package/dummy/DummyContent.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/dummy/DummyContent.${ktOrJavaExt}" />
</recipe>
