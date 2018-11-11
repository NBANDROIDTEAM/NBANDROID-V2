apply plugin: 'java'

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
}

<#if javaVersion??>
sourceCompatibility = "${javaVersion}"
targetCompatibility = "${javaVersion}"
</#if>
