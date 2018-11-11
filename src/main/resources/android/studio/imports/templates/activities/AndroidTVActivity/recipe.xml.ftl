<?xml version="1.0"?>
<recipe>

    <dependency mavenUrl="com.android.support:appcompat-v7:${targetApi}.+"/>
    <dependency mavenUrl="com.android.support:leanback-v17:${targetApi}.+"/>
    <dependency mavenUrl="com.github.bumptech.glide:glide:3.4.+"/>

    <merge from="root/AndroidManifest.xml.ftl"
             to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <merge from="root/res/values/strings.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

    <merge from="root/res/values/colors.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/colors.xml" />

    <merge from="root/res/values/themes.xml.ftl"
             to="${escapeXmlAttribute(resOut)}/values/themes.xml" />

    <copy from="root/res/drawable"
                to="${escapeXmlAttribute(resOut)}/drawable" />
    <copy from="root/res/drawable-hdpi"
                to="${escapeXmlAttribute(resOut)}/drawable-hdpi" />
    <copy from="root/res/drawable-mdpi"
                to="${escapeXmlAttribute(resOut)}/drawable-mdpi" />
    <copy from="root/res/drawable-xhdpi"
                to="${escapeXmlAttribute(resOut)}/drawable-xhdpi" />
    <copy from="root/res/drawable-xxhdpi"
                to="${escapeXmlAttribute(resOut)}/drawable-xxhdpi" />

    <instantiate from="root/res/layout/activity_main.xml.ftl"
                  to="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />

    <instantiate from="root/res/layout/activity_details.xml.ftl"
                  to="${escapeXmlAttribute(resOut)}/layout/${detailsLayoutName}.xml" />

    <instantiate from="root/res/layout/playback_controls.xml.ftl"
                  to="${escapeXmlAttribute(resOut)}/layout/playback_controls.xml" />

    <instantiate from="root/src/app_package/MainActivity.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />

    <instantiate from="root/src/app_package/MainFragment.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/${mainFragment}.java" />

    <instantiate from="root/src/app_package/DetailsActivity.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/${detailsActivity}.java" />

    <instantiate from="root/src/app_package/VideoDetailsFragment.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/${detailsFragment}.java" />

    <instantiate from="root/src/app_package/Movie.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/Movie.java" />

    <instantiate from="root/src/app_package/MovieList.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/MovieList.java" />

    <instantiate from="root/src/app_package/CardPresenter.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/CardPresenter.java" />

    <instantiate from="root/src/app_package/DetailsDescriptionPresenter.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/DetailsDescriptionPresenter.java" />

    <instantiate from="root/src/app_package/PlaybackOverlayActivity.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/PlaybackOverlayActivity.java" />

    <instantiate from="root/src/app_package/PlaybackOverlayFragment.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/PlaybackOverlayFragment.java" />

    <instantiate from="root/src/app_package/Utils.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/Utils.java" />

    <instantiate from="root/src/app_package/ErrorFragment.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/ErrorFragment.java" />

    <instantiate from="root/src/app_package/BrowseErrorActivity.java.ftl"
                  to="${escapeXmlAttribute(srcOut)}/BrowseErrorActivity.java" />

    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />
    <open file="${escapeXmlAttribute(resOut)}/layout/${layoutName}.xml" />
</recipe>
