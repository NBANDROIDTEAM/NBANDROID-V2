<?xml version="1.0"?>
<recipe>
    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />
    <instantiate from="root/res/actions.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/xml/${escapeXmlAttribute(fileName)}.xml" />
    <open file="${escapeXmlAttribute(resOut)}/xml/${escapeXmlAttribute(fileName)}.xml" />
</recipe>
