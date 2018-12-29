<recipe folder="root://gradle-projects/common">

    <instantiate from="proguard-rules.txt.ftl"
                   to="${escapeXmlAttribute(projectOut)}/proguard-rules.pro" />

</recipe>
