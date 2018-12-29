<#import "root://gradle-projects/NewAndroidModule/root/shared_macros.ftl" as shared>
apply plugin: 'com.android.dynamic-feature'

<@shared.androidConfig/>

dependencies {
    ${getConfigurationName("compile")} fileTree(dir: 'libs', include: ['*.jar'])
    <#if !improvedTestDeps>
    ${getConfigurationName("androidTestCompile")}('com.android.support.test.espresso:espresso-core:${espressoVersion!"+"}', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    </#if>
    implementation project(':${baseFeatureName}')
}
