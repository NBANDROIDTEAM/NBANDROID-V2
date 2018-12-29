dependencies {
<#if buildApi gte 26>
  ${getConfigurationName("compile")} 'com.android.support:wear:+'
</#if>
  ${getConfigurationName("provided")} 'com.google.android.wearable:wearable:+'
}
