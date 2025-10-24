<table <#if width??>style="width:${width};"<#else>style="width:100%";</#if> align="left" class="contact-data" role="presentation">
    <thead>
        <#if emailBodyReport ?? && emailBodyReport == true>
            <tr class="content colr">
                <th scope="col">
                    <#if pivotedReportData?? && pivotedReportData.pivotedData?? && pivotedReportData.pivotedData?has_content && pivotedReportData.columns??>
                        <#assign numberColumns = pivotedReportData.columns?filter(column -> column.type == "number") />

                        <table border="1" style="max-width:100%;">
                            <thead>
                                <#if pivotedReportData.columnGroupKeys?? && pivotedReportData.columnGroupKeys?has_content>
                                    <#assign processedColumnHeader = [] />
                                    <#list pivotedReportData.columnGroupKeys as item>
                                        <#assign processedColumnHeader += [item?split("~")] />
                                    </#list>

                                    <#assign maxColumnHeaderRowsLevels = processedColumnHeader[0]?size />
                                    <#list 0..(maxColumnHeaderRowsLevels-1) as rowIndex>
                                        <tr>
                                            <th></th>
                                            <#list processedColumnHeader as columnHeader>
                                                <th colspan="${numberColumns?size}">${(columnHeader[rowIndex])}</th>
                                            </#list>

                                            <#if pivotedReportData.grandTotal?? && subTotalConfig?? && subTotalConfig.enableColumnTotal?? && subTotalConfig.enableColumnTotal == true>
                                                <#assign columnHeaderRowsLevels = pivotedReportData.columnGroupKeys[0]?split("~") />
                                                <#if ((columnHeaderRowsLevels?size - 1) == rowIndex) && (numberColumns?size > 1)>
                                                    <th>Grand Total</th>
                                                <#else>
                                                    <th></th>
                                                </#if>
                                            </#if>
                                        </tr>
                                    </#list>

                                    <#if (numberColumns?? && numberColumns?size >1)>
                                        <tr>
                                            <th></th>
                                            <#list pivotedReportData.columnGroupKeys as item>
                                                <#list numberColumns as column>
                                                    <th colspan="1">${column.title!}</th>
                                                </#list>
                                            </#list>

                                            <#if pivotedReportData.grandTotal?? && subTotalConfig?? && subTotalConfig.enableColumnTotal?? && subTotalConfig.enableColumnTotal == true>
                                                <th>Grand Total</th>
                                            </#if>
                                        </tr>
                                    </#if>

                                    <#if pivotedReportData.grandTotal?? && subTotalConfig?? && subTotalConfig.enableRowTotal?? && subTotalConfig.enableRowTotal == true>
                                        <tr>
                                            <td><b>Grand Total</b></td>
                                            <#if (numberColumns?? && numberColumns?has_content)>
                                                <#list pivotedReportData.columnGroupKeys as columnKey>
                                                    <#assign rowGrandTotalKeys = "">
                                                    <#list numberColumns as column>
                                                        <#assign rowGrandTotalKeys = columnKey?string + column.field />
                                                        <#if pivotedReportData.grandTotal.columnGrandTotal[rowGrandTotalKeys]??>
                                                            <td style="text-align:right;"><b>${pivotedReportData.grandTotal.columnGrandTotal[rowGrandTotalKeys]!0}</b></td>
                                                        </#if>
                                                    </#list>
                                                </#list>
                                            </#if>

                                            <#assign totalRowGrandTotal = calculateRowGrandTotal(numberColumns) />
                                            <#if pivotedReportData.grandTotal?? && subTotalConfig?? && subTotalConfig.enableColumnTotal?? && subTotalConfig.enableColumnTotal == true>
                                                <#if totalRowGrandTotal??>
                                                    <td style="text-align:right;"><b>${totalRowGrandTotal!0}</b></td>
                                                </#if>
                                            </#if>
                                        </tr>
                                    </#if>
                                </#if>
                            </thead>

                            <tbody>
                                <#list pivotedReportData.pivotedData?filter(row -> row.level == 1) as row>
                                    <@renderRow row=row numberColumns=numberColumns currentLevel=row.level />
                                </#list>
                            </tbody>
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

<#-- Macro to render a row and its children recursively -->
<#macro renderRow row numberColumns currentLevel>
    <tr>
        <#assign hasChildRows = false />
        <#list pivotedReportData.pivotedData as subRow>
            <#if (subRow.level == (currentLevel + 1)) && (subRow.id?starts_with(row.id))>
                <#assign hasChildRows = true />
            </#if>
        </#list>

        <td style="text-indent: ${(currentLevel - 1) * 20}px;"
            <#if row.breachDetailsByLevel?? && row.breachDetailsByLevel?has_content> class="${row.breachDetailsByLevel}"</#if>>
            <#if hasChildRows><b></#if>${row.id?substring(row.id?last_index_of("~") + 1)}<#if hasChildRows></b></#if>
        </td>

        <#if row.columnGroupValues?? && row.columnGroupValues?has_content>
            <#if pivotedReportData.columnGroupKeys?? && pivotedReportData.columnGroupKeys?has_content>
                <#list pivotedReportData.columnGroupKeys as columnKey>
                    <#if columnKey?? && row.columnGroupValues[columnKey]??>
                        <#if numberColumns?? && numberColumns?has_content>
                            <#list numberColumns as column>
                                <#if (column.format?? && column.format == "ROUND PERCENTAGE")>
                                    <#if !hasChildRows>
                                        <td style="text-align:right;"
                                            <#if row.columnGroupedBreachDetails?? && row.columnGroupedBreachDetails?has_content>
                                                <#list row.columnGroupedBreachDetails as breachStatus>
                                                    <#if breachStatus[columnKey]?? && breachStatus[columnKey]?has_content>
                                                        class="${breachStatus[columnKey][column.field]!}"
                                                    </#if>
                                                </#list>
                                            </#if>>
                                            ${row.columnGroupValues[columnKey][column.field]!}%
                                        </td>
                                    <#else>
                                        <td></td>
                                    </#if>
                                <#else>
                                    <#if hasChildRows><b></#if>
                                        <#if (row.columnGroupValues[columnKey][column.field]?? && row.columnGroupValues[columnKey][column.field]?has_content)>
                                            <td style="text-align:right;"
                                                <#if row.columnGroupedBreachDetails?? && row.columnGroupedBreachDetails?has_content>
                                                    <#list row.columnGroupedBreachDetails as breachStatus>
                                                        <#if breachStatus[columnKey]?? && breachStatus[columnKey]?has_content>
                                                            class="${breachStatus[columnKey][column.field]!}"
                                                        </#if>
                                                    </#list>
                                                </#if>>
                                                ${row.columnGroupValues[columnKey][column.field]!}
                                            </td>
                                        <#else>
                                            <td></td>
                                        </#if>
                                    <#if hasChildRows></b></#if>
                                </#if>
                            </#list>
                        </#if>
                    <#else>
                        <#list numberColumns as column>
                            <#if hasChildRows><b></#if><td></td><#if hasChildRows></b></#if>
                        </#list>
                    </#if>
                </#list>
            </#if>
        </#if>

        <#if pivotedReportData.grandTotal?? && subTotalConfig?? && subTotalConfig.enableColumnTotal?? && subTotalConfig.enableColumnTotal> == true>
            <td style="text-align:right;">
                <#if hasChildRows><b></#if>
                    <#if pivotedReportData.grandTotal.rowGrandTotal?? && pivotedReportData.grandTotal.rowGrandTotal[row.id]??>
                        ${pivotedReportData.grandTotal.rowGrandTotal[row.id]!}
                    </#if>
                <#if hasChildRows></b></#if>
            </td>
        </#if>
    </tr>

    <#list pivotedReportData.pivotedData?filter(subRow -> (subRow.level == (currentLevel + 1)) && (subRow.id?starts_with(row.id))) as subRow>
        <@renderRow row=subRow numberColumns=numberColumns currentLevel=(currentLevel + 1) />
    </#list>
</#macro>

<#-- Function to calculate total of row grand totals across all column group keys and numeric columns -->
<#function calculateRowGrandTotal numberColumns>
    <#assign totalRowGrandTotal = 0 />
    <#if pivotedReportData.grandTotal??>
        <#if (numberColumns?? && numberColumns?has_content) && pivotedReportData.columnGroupKeys?? && pivotedReportData.columnGroupKeys?has_content>
            <#list pivotedReportData.columnGroupKeys as columnKey>
                <#list numberColumns as column>
                    <#assign key = (columnKey?string) + column.field />
                    <#if pivotedReportData.grandTotal.columnGrandTotal[key]??>
                        <#assign totalRowGrandTotal = totalRowGrandTotal + (pivotedReportData.grandTotal.columnGrandTotal[key]!0) />
                    </#if>
                </#list>
            </#list>
        </#if>
    </#if>
    <@return totalRowGrandTotal />
</#function>
