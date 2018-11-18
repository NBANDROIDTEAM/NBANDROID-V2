<?xml version="1.0"?>
<recipe>

  <instantiate from="root/src/app_package/interface.aidl.ftl"
                 to="${escapeXmlAttribute(aidlOut)}/${escapeXmlAttribute(interfaceName)}.aidl" />
  <open file="${escapeXmlAttribute(aidlOut)}/${escapeXmlAttribute(interfaceName)}.aidl" />
</recipe>
