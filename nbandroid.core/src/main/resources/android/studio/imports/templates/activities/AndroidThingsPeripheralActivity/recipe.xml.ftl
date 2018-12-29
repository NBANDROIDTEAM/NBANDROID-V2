<?xml version="1.0"?>
<recipe>

  <#if requireTheme!false>
  <#include "../common/recipe_theme.xml.ftl" />
  </#if>

  <dependency mavenUrl="com.android.support:support-v4:${buildApi}.+" />

  <merge from="root/AndroidManifest.xml.ftl"
           to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />
  <merge from="root/build.gradle.ftl"
           to="${escapeXmlAttribute(projectOut)}/build.gradle" />

    <instantiate from="root/src/app_package/SimpleActivity.${ktOrJavaExt}.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />

    <#if integrateAccelerometer>
      <instantiate from="root/src/app_package/AccelerometerService.${ktOrJavaExt}.ftl"
                     to="${escapeXmlAttribute(srcOut)}/${accelerometerServiceClass}.${ktOrJavaExt}" />
      <open file="${escapeXmlAttribute(srcOut)}/${accelerometerServiceClass}.${ktOrJavaExt}" />
    </#if>

    <#if integrateGps>
      <instantiate from="root/src/app_package/GpsService.${ktOrJavaExt}.ftl"
                     to="${escapeXmlAttribute(srcOut)}/${gpsServiceClass}.${ktOrJavaExt}" />
      <open file="${escapeXmlAttribute(srcOut)}/${gpsServiceClass}.${ktOrJavaExt}" />
    </#if>

    <#if integrateTemperaturePressureSensor>
      <instantiate from="root/src/app_package/TemperaturePressureService.${ktOrJavaExt}.ftl"
                     to="${escapeXmlAttribute(srcOut)}/${temperaturePressureServiceClass}.${ktOrJavaExt}" />
      <open file="${escapeXmlAttribute(srcOut)}/${temperaturePressureServiceClass}.${ktOrJavaExt}" />
    </#if>

</recipe>
