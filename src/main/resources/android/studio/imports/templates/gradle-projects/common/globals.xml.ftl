<globals>
    <global id="topOut" value="." />
    <global id="projectOut" value="." />

    <global id="postprocessingSupported" type="boolean" value="false" />
    <global id="unitTestsSupported" type="boolean" value="${(compareVersions(gradlePluginVersion, '1.1.0') >= 0)?string}" />
    <global id="improvedTestDeps" type="boolean" value="${(compareVersionsIgnoringQualifiers(gradlePluginVersion, '3.0.0') >= 0)?string}" />
</globals>
