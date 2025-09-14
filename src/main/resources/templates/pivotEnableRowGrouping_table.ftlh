<table <#if width??>style="width:${width};"</#if> align="left" class="contact-data" role="presentation">
    <thead>
        <#if emailBodyReport?? && emailBodyReport == true>
            <tr class="content colr">
                <th scope="col">
                    <#if pivotedReportData?? && pivotedReportData['pivotedData']?? && pivotedReportData['pivotedData']?has_content && pivotedReportData["columns"]??>
                        <#assign filteredColumns = pivotedReportData["columns"]?filter(column -> column?? && !column.hidden && column.type == "number")>
                        <table border="1">
                            <tr>
                                <th></th>
                                <#if filteredColumns??>
                                    <#list filteredColumns as column>
                                        <th>${column.title}</th>
                                    </#list>
                                </#if>
                                <#if pivotedReportData['grandTotal']?? && subTotalConfig?? && subTotalConfig['enableColumnTotal']?? && subTotalConfig['enableColumnTotal']>
                                    <th>Grand Total</th>
                                </#if>
                            </tr>
                            <#if pivotedReportData['grandTotal']?? && subTotalConfig?? && subTotalConfig['enableRowTotal']?? && subTotalConfig['enableRowTotal']>
                                <tr>
                                    <td><b>Grand Total</b></td>
                                    <#if filteredColumns??>
                                        <#list filteredColumns as column>
                                            <#if pivotedReportData['grandTotal'].columnGrandTotal[column.field]??>
                                                <td style="text-align: right;"><b>${pivotedReportData['grandTotal'].columnGrandTotal[column.field]}</b></td>
                                            </#if>
                                        </#list>
                                    </#if>
                                    <#assign totalColumnGrandTotal = calculateColumnGrandTotal(filteredColumns)>
                                    <#if pivotedReportData['grandTotal']?? && subTotalConfig?? && subTotalConfig['enableColumnTotal']?? && subTotalConfig['enableColumnTotal']>
                                        <#if totalColumnGrandTotal??>
                                            <td style="text-align: right;"><b>${totalColumnGrandTotal}</b></td>
                                        </#if>
                                    </#if>
                                </tr>
                            </#if>
                            <#-- Iterate over Level 1 Rows -->
                            <#list pivotedReportData['pivotedData']?filter(row -> row.level == 1) as row>
                                <@renderRow row filteredColumns row.level />
                            </#list>
                        </table>
                    <#else>
                        <p>No Data to display for the report</p>
                    </#if>
                </th>
            </tr>
        <#else>
            <tr class="content colr">
                <th>&nbsp;&nbsp; Email Body is not enabled for this report. &nbsp;&nbsp;</th>
            </tr>
        </#if>
    </thead>
</table>

<#-- Recursive function to iterate over rows -->
<#macro renderRow row filteredColumns level>
    <#assign hasChildRows = false>
    <#list pivotedReportData['pivotedData'] as subRow>
        <#if (subRow.level == level + 1) && (subRow.id?starts_with(row.id))>
            <#assign hasChildRows = true>
        </#if>
    </#list>
    <tr>
        <td style="text-indent:${(level-1)*20}px; white-space: nowrap !important;"
        <#if row.breachDetailsByLevel?? && row.breachDetailsByLevel?has_content>
            class="${row.breachDetailsByLevel}"
        </#if>>
            <#if hasChildRows><b></#if>
                ${row.id?substring(row.id?last_index_of("~")+1)}
            <#if hasChildRows></b></#if>
        </td>
        <#if filteredColumns??>
            <#list filteredColumns as column>
                <td style="text-align:right;"
                    <#if row.rowGroupedBreachDetails?? && row.rowGroupedBreachDetails?has_content>
                        <#list row.rowGroupedBreachDetails as breachKey, breachValue>
                            <#if row.rowGroupedBreachDetails[column['field']]?? && breachValue??>
                                class="${row.rowGroupedBreachDetails[column['field']]}"
                            </#if>
                        </#list>
                    </#if>
                >
                    <#if hasChildRows><b></#if>
                    <#if row.rowGroupValues[column.field]??>
                        <#if column['format']?? && column['format']?has_content && column['format'] == "ROUND PERCENTAGE">
                            ${row.rowGroupValues[column.field]}%
                        <#else>
                            ${row.rowGroupValues[column.field]}
                        </#if>
                    </#if>
                    <#if hasChildRows></b></#if>
                </td>
            </#list>
        </#if>
        <#if pivotedReportData['grandTotal']?? && subTotalConfig?? && subTotalConfig['enableColumnTotal']?? && subTotalConfig['enableColumnTotal']>
            <td style="text-align:right;">
                <#if hasChildRows><b></#if>
                <#if pivotedReportData['grandTotal'].rowGrandTotal[row.id]??>
                    ${pivotedReportData['grandTotal'].rowGrandTotal[row.id]}
                </#if>
                <#if hasChildRows></b></#if>
            </td>
        </#if>
    </tr>
    <#-- Recursively render child rows -->
    <#list pivotedReportData['pivotedData']?filter(subRow -> (subRow.level == level + 1) && subRow.id?starts_with(row.id)) as subRow>
        <@renderRow subRow filteredColumns subRow.level />
    </#list>
</#macro>

<#-- Function to calculate total of Column Grand Total -->
<#function calculateColumnGrandTotal filteredColumns>
    <#if pivotedReportData['grandTotal']??>
        <#assign totalColumnGrandTotal = 0>
        <#if filteredColumns??>
            <#list filteredColumns as column>
                <#if pivotedReportData['grandTotal'].columnGrandTotal[column.field]??>
                    <#assign totalColumnGrandTotal += pivotedReportData['grandTotal'].columnGrandTotal[column.field]>
                </#if>
            </#list>
        </#if>
        <#return totalColumnGrandTotal>
    </#if>
</#function>