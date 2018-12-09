<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
    <@kt.addAllKotlinDependencies />
    <dependency mavenUrl="com.android.support:appcompat-v7:+"/>
    <dependency mavenUrl="com.android.support:leanback-v17:+"/>
    <dependency mavenUrl="com.github.bumptech.glide:glide:3.8.0"/>

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <merge from="root/res/values/strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

    <merge from="root/res/values/colors.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/colors.xml" />

    <copy from="root/res/drawable"
                to="${escapeXmlAttribute(resOut)}/drawable" />

    <instantiate from="root/res/layout/activity_main.xml.ftl"
                  to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />

    <instantiate from="root/res/layout/activity_details.xml.ftl"
                  to="${escapeXmlAttribute(resOut)}/layout/${detailsLayoutName}.xml" />

    <instantiate from="root/src/app_package/MainActivity.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/MainFragment.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/${mainFragment}.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/DetailsActivity.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/${detailsActivity}.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/VideoDetailsFragment.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/${detailsFragment}.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/Movie.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/Movie.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/MovieList.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/MovieList.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/CardPresenter.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/CardPresenter.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/DetailsDescriptionPresenter.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/DetailsDescriptionPresenter.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/PlaybackActivity.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/PlaybackActivity.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/PlaybackVideoFragment.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/PlaybackVideoFragment.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/BrowseErrorActivity.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/BrowseErrorActivity.${ktOrJavaExt}" />
    <instantiate from="root/src/app_package/ErrorFragment.${ktOrJavaExt}.ftl"
                      to="${escapeXmlAttribute(srcOut)}/ErrorFragment.${ktOrJavaExt}" />

    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.${ktOrJavaExt}" />
    <open file="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />

</recipe>
