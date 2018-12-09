android {
    defaultConfig {
        multiDexEnabled true
    }
}

dependencies {
  ${getConfigurationName("provided")} 'com.google.android.wearable:wearable:+'
}
