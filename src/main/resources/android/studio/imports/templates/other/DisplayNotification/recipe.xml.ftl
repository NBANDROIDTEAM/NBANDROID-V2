<?xml version="1.0"?>
<recipe>
    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <instantiate from="root/res/layout/activity_display.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/layout/${displayActivityLayout}.xml" />
    <instantiate from="root/src/app_package/StubActivity.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${stubActivityClass}.java" />

    <instantiate from="root/src/app_package/DisplayActivity.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${displayActivityClass}.java" />

    <instantiate from="root/src/app_package/BroadcastReceiver.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${receiverClass}.java" />

    <merge from="root/res/values/strings.xml"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

    <open file="${escapeXmlAttribute(srcOut)}/${displayActivityClass}.java" />
    <open file="${escapeXmlAttribute(resOut)}/layout/${displayActivityLayout}.xml" />
</recipe>
