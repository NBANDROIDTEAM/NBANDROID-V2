dependencies {
<#if integrateButton>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-button:0.3'
</#if>
<#if integrateCapacitiveTouchButton>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-cap12xx:0.3'
</#if>
<#if integrateNumericDisplay>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-tm1637:0.2'
</#if>
<#if integrateAlphanumericDisplay>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-ht16k33:0.3'
</#if>
<#if integrateOledDisplay>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-ssd1306:0.3'
</#if>
<#if integrateLEDStrip>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-apa102:0.3'
</#if>
<#if integrateAccelerometer>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-mma7660fc:0.2'
</#if>
<#if integrateGps>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-gps:0.3'
</#if>
<#if integrateTemperaturePressureSensor>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-bmx280:0.2'
</#if>
<#if integrateServo>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-pwmservo:0.2'
</#if>
<#if integrateSpeakerBuzzer>
    ${getConfigurationName("compile")} 'com.google.android.things.contrib:driver-pwmspeaker:0.2'
</#if>
}
