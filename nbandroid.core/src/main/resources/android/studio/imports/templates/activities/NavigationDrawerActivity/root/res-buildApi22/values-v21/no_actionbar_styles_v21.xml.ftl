<resources>
    <#if !themeExistsNoActionBar>
        <style name="${themeNameNoActionBar}"<#if !implicitParentTheme> parent="${themeName}"</#if>>
            <item name="windowActionBar">false</item>
            <item name="windowNoTitle">true</item>
            <item name="android:statusBarColor">@android:color/transparent</item>
        </style>
    </#if>
</resources>
