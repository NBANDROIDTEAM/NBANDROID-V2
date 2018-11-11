<?xml version="1.0"?>
<recipe>
    <dependency mavenUrl="com.android.support:support-v4:21.0.2"/>
    <dependency mavenUrl="com.android.support:support-v13:21.0.2"/>
    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <copy from="root/res/xml/automotive_app_desc.xml"
          to="${escapeXmlAttribute(resOut)}/xml/automotive_app_desc.xml" />

    <instantiate from="root/src/app_package/MessagingService.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${serviceName}.java" />

    <instantiate from="root/src/app_package/MessageReadReceiver.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${readReceiverName}.java" />

    <instantiate from="root/src/app_package/MessageReplyReceiver.java.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${replyReceiverName}.java" />

    <open file="${escapeXmlAttribute(srcOut)}/${serviceName}.java" />
    <open file="${escapeXmlAttribute(srcOut)}/${readReceiverName}.java" />
    <open file="${escapeXmlAttribute(srcOut)}/${replyReceiverName}.java" />
</recipe>
