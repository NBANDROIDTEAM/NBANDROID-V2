<resources>
    <#if !themeExistsNoActionBar>
        <style name="${themeNameNoActionBar}"<#if !implicitParentTheme> parent="${themeName}"</#if>>
            <item name="windowActionBar">false</item>
            <item name="windowNoTitle">true</item>
        </style>
    </#if>
    <#if !themeExistsAppBarOverlay>
        <style name="${themeNameAppBarOverlay}" parent="ThemeOverlay.AppCompat.Dark.ActionBar" />
    </#if>
    <#if !themeExistsPopupOverlay>
        <style name="${themeNamePopupOverlay}" parent="ThemeOverlay.AppCompat.Light" />
    </#if>
</resources>
