<#import "./shared_macros.ftl" as shared>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<#if isInstantApp>
apply plugin: 'com.android.feature'
<#else>
  <#if isLibraryProject>
apply plugin: 'com.android.library'
  <#elseif isDynamicFeature>
apply plugin: 'com.andorid.dynamic-feature'
  <#else>
apply plugin: 'com.android.application'
  </#if>
</#if>
<@kt.addKotlinPlugins />

<@shared.androidConfig hasApplicationId=isApplicationProject applicationId=packageName isBaseFeature=isBaseFeature hasTests=true canHaveCpp=true canUseProguard=isApplicationProject||isBaseFeature||(isLibraryProject&&!isInstantApp)/>

dependencies {
    ${getConfigurationName("compile")} fileTree(dir: 'libs', include: ['*.jar'])
    <#if !improvedTestDeps>
    ${getConfigurationName("androidTestCompile")}('com.android.support.test.espresso:espresso-core:${espressoVersion!"+"}', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    </#if>
    <@kt.addKotlinDependencies />
<#if isInstantApp||isDynamicFeature>
  <#if isBaseFeature>
    <#if monolithicModuleName?has_content>
    application project(':${monolithicModuleName}')
    <#else>
    // TODO: Add dependency to the main application.
    // application project(':app')
    </#if>
  <#else>
    implementation project(':${baseFeatureName}')
  </#if>
<#else>
  <@shared.watchProjectDependencies/>
</#if>
}
