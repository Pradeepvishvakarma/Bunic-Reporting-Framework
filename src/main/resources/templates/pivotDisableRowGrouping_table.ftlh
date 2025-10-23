<table <#if width??>style="width:${width};"</#if> align="left" class="contact-data" role="presentation">
    <thead>
        <#if emailBodyReport == true>
            <tr class="content colr">
                <th scope="col">
                    <#if pivotedReportData?? && pivotedReportData.pivotedData?? && pivotedReportData.pivotedData?has_content && pivotedReportData.columns??>
                        <#-- filter visible columns -->
                        <#assign filteredColumns = pivotedReportData.columns?filter(column -> !column.hidden)>

                        <table border="1">
                            <tr>
                                <th></th>
                                <#list filteredColumns as column>
                                    <th>${column.title}</th>
                                </#list>
                            </tr>

                            <#-- Iterate over Level 1 Rows -->
                            <#list pivotedReportData.pivotedData?filter(row -> row.level == 1) as row>
                                <@renderRow row filteredColumns row.level/>
                            </#list>
                        </table>
                    <#else>
                        <p>No data to display for the report</p>
                    </#if>
                </th>
            </tr>
        <#else>
            <tr class="content gelr">
                <th>&nbsp;&nbsp;Email Body is not enabled for this report. &nbsp;&nbsp;</th>
            </tr>
        </#if>
    </thead>
</table>

<#-- Recursive macro to iterate over rows -->
<#macro renderRow row filteredColumns level>
    <#assign hasChildRows = false>
    <#list pivotedReportData.pivotedData as subRow>
        <#if subRow.level == (level + 1) && subRow.id?starts_with(row.id)>
            <#assign hasChildRows = true>
        </#if>
    </#list>

    <tr>
        <td style="text-indent:${(level-1)*20}px; white-space: nowrap !important;"
            <#if row.breachDetailsByLevel?? && row.breachDetailsByLevel?has_content>
                class=${row.breachDetailsByLevel}
            </#if>>
            <#if hasChildRows><b></#if>
                ${row.id?substring(row.id?last_index_of("~") + 1)}
            <#if hasChildRows></b></#if>
        </td>

        <#list filteredColumns as column>
            <td></td>
        </#list>


        <#if row.pivotDisableRowGroupValues?? && row.pivotDisableRowGroupValues?has_content>
            <#list row.pivotDisableRowGroupValues as mainRow>
                <tr>
                    <#if mainRow?? && mainRow?has_content && mainRow.values?? && mainRow.values?has_content>
                        <td></td>
                        <#list filteredColumns as column>
                            <#assign cellValue = mainRow.values[column.field]!>
                            <#if cellValue?has_content>
                                <#if column.type == "string">
                                    <td style="text-align: left;">
                                        <#if hasChildRows><b></#if>${cellValue}<#if hasChildRows></b></#if>
                                    </td>
                                <#elseif column.type == "number">
                                    <td style="text-align: right;"
                                        <#if mainRow.breachDetails?? && mainRow.breachDetails?has_content>
                                            <#list mainRow.breachDetails as breachField, breachStatus>
                                                <#if breachField == column['field'] && breachStatus?? && breachStatus?has_content>
                                                    class="${breachStatus}"
                                                </#if>
                                            </#list>
                                        </#if>>
                                        <#if hasChildRows><b></#if>
                                           ${cellValue}
                                        <#if hasChildRows></b></#if>
                                    </td>
                                <#else>
                                    <td>${cellValue}</td>
                                </#if>
                            <#else>
                                <td></td>
                            </#if>
                        </#list>
                    </#if>
                </tr>
            </#list>
        </#if>
    </tr>

    <#-- Recursively render child rows -->
    <#list pivotedReportData.pivotedData?filter(subRow -> subRow.level == (level + 1) && subRow.id?starts_with(row.id)) as subRow>
        <@renderRow subRow filteredColumns subRow.level/>
    </#list>
</#macro>