<globals>
    <#assign theme=getApplicationTheme()!{ "name": "AppTheme", "isAppCompat": true }>
    <#assign themeName=theme.name!'AppTheme'>
    <#assign themeNameNoActionBar=theme.nameNoActionBar!'AppTheme.NoActionBar'>
    <#assign appCompat=backwardsCompatibility!(theme.isAppCompat)!false>
    <#assign appCompatActivity=appCompat && (buildApi gte 22)>
    <#assign espresso=hasDependency('com.android.support.test.espresso:espresso-core', 'androidTestCompile')>
    <#assign supportRunner=hasDependency('com.android.support.test:runner', 'androidTestCompile')>
    <#assign testSupportLib=espresso && supportRunner>

    <global id="themeName" type="string" value="${themeName}" />
    <global id="implicitParentTheme" type="boolean" value="${(themeNameNoActionBar?starts_with(themeName+'.'))?string}" />
    <global id="themeNameNoActionBar" type="string" value="${themeNameNoActionBar}" />
    <global id="themeExistsNoActionBar" type="boolean" value="${(theme.existsNoActionBar!false)?string}" />
    <global id="themeNameAppBarOverlay" type="string" value="${theme.nameAppBarOverlay!'AppTheme.AppBarOverlay'}" />
    <global id="themeExistsAppBarOverlay" type="boolean" value="${(theme.existsAppBarOverlay!false)?string}" />
    <global id="themeNamePopupOverlay" type="string" value="${theme.namePopupOverlay!'AppTheme.PopupOverlay'}" />
    <global id="themeExistsPopupOverlay" type="boolean" value="${(theme.existsPopupOverlay!false)?string}" />

    <global id="appCompat" type="boolean" value="${appCompat?string}" />
    <global id="appCompatActivity" type="boolean" value="${appCompatActivity?string}" />
    <global id="hasAppBar" type="boolean" value="${appCompatActivity?string}" />
    <global id="hasNoActionBar" type="boolean" value="${appCompatActivity?string}" />
    <global id="testSupportLib" type="boolean" value="${testSupportLib?string}" />

    <global id="manifestOut" value="${manifestDir}" />
    <global id="buildVersion" value="${buildApi}" />

<#if !appCompat>
    <global id="superClass" type="string" value="Activity"/>
    <global id="superClassFqcn" type="string" value="android.app.Activity"/>
    <global id="Support" value="" />
    <global id="actionBarClassFqcn" type = "string" value="android.app.ActionBar" />
<#elseif appCompatActivity>
    <global id="superClass" type="string" value="AppCompatActivity"/>
    <global id="superClassFqcn" type="string" value="android.support.v7.app.AppCompatActivity"/>
    <global id="Support" value="Support" />
    <global id="actionBarClassFqcn" type = "string" value="android.support.v7.app.ActionBar" />
<#else>
    <global id="superClass" type="string" value="ActionBarActivity"/>
    <global id="superClassFqcn" type="string" value="android.support.v7.app.ActionBarActivity"/>
    <global id="Support" value="Support" />
    <global id="actionBarClassFqcn" type = "string" value="android.support.v7.app.ActionBar" />
</#if>

    <global id="srcOut" value="${srcDir}/${slashedPackageName(packageName)}" />
    <global id="resOut" value="${resDir}" />
    <global id="menuName" value="${classToResource(activityClass!'')}" />
    <global id="simpleName" value="${activityToLayout(activityClass!'')}" />
    <global id="relativePackage" value="<#if relativePackage?has_content>${relativePackage}<#else>${packageName}</#if>" />
</globals>
