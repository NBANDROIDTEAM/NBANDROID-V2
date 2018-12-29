apply plugin: 'java-library'

dependencies {
    ${getConfigurationName("compile")} fileTree(dir: 'libs', include: ['*.jar'])
}

<#if javaVersion??>
sourceCompatibility = "${javaVersion}"
targetCompatibility = "${javaVersion}"
</#if>
