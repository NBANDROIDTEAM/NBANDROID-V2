<resources>
    <#if !isNewProject>
    <string name="title_${activityToLayout(activityClass)}">${escapeXmlString(activityTitle)}</string>
    </#if>

    <string name="action_settings">Settings</string>

    <#if adFormat == "banner">
    <string name="hello_world">Hello world!</string>
    <!-- -
        This is an ad unit ID for a banner test ad. Replace with your own banner ad unit id.
        For more information, see https://support.google.com/admob/answer/3052638
    <!- -->
    <string name="banner_ad_unit_id">ca-app-pub-3940256099942544/6300978111</string>
    <#elseif adFormat == "interstitial">
    <string name="interstitial_ad_sample">Interstitial Ad Sample</string>
    <string name="start_level">Level 1</string>
    <string name="next_level">Next Level</string>
    <!-- -
        This is an ad unit ID for an interstitial test ad. Replace with your own interstitial ad unit id.
        For more information, see https://support.google.com/admob/answer/3052638
    <!- -->
    <string name="interstitial_ad_unit_id">ca-app-pub-3940256099942544/1033173712</string>
    </#if>

</resources>
