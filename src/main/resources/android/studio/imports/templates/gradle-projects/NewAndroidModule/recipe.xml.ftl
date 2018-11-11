<?xml version="1.0"?>
<recipe>

<#if !(isInstantApp!false) || (isBaseAtom!false)>
    <#if backwardsCompatibility!true>
        <dependency mavenUrl="com.android.support:appcompat-v7:${buildApi}.+" />
    </#if>

    <#if unitTestsSupported>
        <dependency mavenUrl="junit:junit:4.12" gradleConfiguration="testCompile" />
    </#if>
</#if>

<#if !createActivity>
    <mkdir at="${escapeXmlAttribute(srcOut)}" />
</#if>

    <mkdir at="${escapeXmlAttribute(projectOut)}/libs" />

    <merge from="root/settings.gradle.ftl"
             to="${escapeXmlAttribute(topOut)}/settings.gradle" />
    <instantiate from="root/build.gradle.ftl"
                   to="${escapeXmlAttribute(projectOut)}/build.gradle" />
    <instantiate from="root/AndroidManifest.xml.ftl"
                   to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

<#if !(isInstantApp!false) || (isBaseAtom!false)>
    <mkdir at="${escapeXmlAttribute(resOut)}/drawable" />
    <#if copyIcons && !isLibraryProject>
        <#if buildApi gte 25 && targetApi gte 25>
            <copy from="root/res/mipmap-hdpi/"
                    to="${escapeXmlAttribute(resOut)}/mipmap-hdpi/" />
            <copy from="root/res/mipmap-mdpi"
                    to="${escapeXmlAttribute(resOut)}/mipmap-mdpi/" />
            <copy from="root/res/mipmap-xhdpi"
                    to="${escapeXmlAttribute(resOut)}/mipmap-xhdpi/" />
            <copy from="root/res/mipmap-xxhdpi"
                    to="${escapeXmlAttribute(resOut)}/mipmap-xxhdpi/" />
            <copy from="root/res/mipmap-xxxhdpi"
                    to="${escapeXmlAttribute(resOut)}/mipmap-xxxhdpi/" />
        <#else>
            <copy from="root/res/mipmap-hdpi/ic_launcher.png"
                    to="${escapeXmlAttribute(resOut)}/mipmap-hdpi/ic_launcher.png" />
            <copy from="root/res/mipmap-mdpi/ic_launcher.png"
                    to="${escapeXmlAttribute(resOut)}/mipmap-mdpi/ic_launcher.png" />
            <copy from="root/res/mipmap-xhdpi/ic_launcher.png"
                    to="${escapeXmlAttribute(resOut)}/mipmap-xhdpi/ic_launcher.png" />
            <copy from="root/res/mipmap-xxhdpi/ic_launcher.png"
                    to="${escapeXmlAttribute(resOut)}/mipmap-xxhdpi/ic_launcher.png" />
            <copy from="root/res/mipmap-xxxhdpi/ic_launcher.png"
                    to="${escapeXmlAttribute(resOut)}/mipmap-xxxhdpi/ic_launcher.png" />
        </#if>
    </#if>
</#if>
<#if makeIgnore>
    <copy from="root/module_ignore"
            to="${escapeXmlAttribute(projectOut)}/.gitignore" />
</#if>
<#if enableProGuard>
    <instantiate from="root/proguard-rules.txt.ftl"
                   to="${escapeXmlAttribute(projectOut)}/proguard-rules.pro" />
</#if>
<#if !(isLibraryProject!false) || (isBaseAtom!false)>
    <instantiate from="root/res/values/styles.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/values/styles.xml" />
    <#if buildApi gte 22>
        <copy from="root/res/values/colors.xml"
                to="${escapeXmlAttribute(resOut)}/values/colors.xml" />
    </#if>
</#if>

<#if !(isInstantApp!false) || (isBaseAtom!false)>
    <instantiate from="root/res/values/strings.xml.ftl"
                   to="${escapeXmlAttribute(resOut)}/values/strings.xml" />
</#if>

    <instantiate from="root/test/app_package/ExampleInstrumentedTest.java.ftl"
                   to="${escapeXmlAttribute(testOut)}/ExampleInstrumentedTest.java" />

<#if unitTestsSupported>
    <instantiate from="root/test/app_package/ExampleUnitTest.java.ftl"
                   to="${escapeXmlAttribute(unitTestOut)}/ExampleUnitTest.java" />
</#if>
<#if includeCppSupport!false>
    <instantiate from="root/CMakeLists.txt.ftl"
                   to="${escapeXmlAttribute(projectOut)}/CMakeLists.txt" />

    <mkdir at="${nativeSrcOut}" />
    <instantiate from="root/native-lib.cpp.ftl" to="${nativeSrcOut}/native-lib.cpp" />
</#if>

</recipe>
