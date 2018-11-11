<resources>
<#if !isNewProject && generateActivityTitle!true>
    <string name="title_${activityToLayout(activityClass)}">${escapeXmlString(activityTitle)}</string>
</#if>
</resources>
