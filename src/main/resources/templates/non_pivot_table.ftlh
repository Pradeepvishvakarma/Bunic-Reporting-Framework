<table <#if width??>style="width:${width};"</#if> align="left" class="contact-data" role="presentation">
<#if emailBodyReport?? && emailBodyReport == true>
  <#if reportData?? && reportData?has_content && reportData['nonPivotedData']?? && reportData['nonPivotedData']?has_content && reportData["columns"]?? >
    <#assign filteredColumns = reportData["columns"]?filter(column -> column?? && column['hidden']?? && column['hidden']==false)>
    <#assign numberColumns = filteredColumns?filter(column -> column['type']=="number")>
    <thead>
      <tr class="content colr">
        <#if subTotalConfig?? && subTotalConfig['enableRowTotal']?? && subTotalConfig['enableRowTotal']==true>
          <th></th>
        </#if>
        <#list filteredColumns as colHeader>
          <th>${colHeader['title']}</th>
        </#list>
        <#if subTotalConfig?? && subTotalConfig['enableColumnTotal']?? && subTotalConfig['enableColumnTotal']==true>
          <th>Grand Total</th>
        </#if>
      </tr>
    </thead>
    <#if reportData['grandTotal']?? && subTotalConfig?? && subTotalConfig['enableRowTotal']?? && subTotalConfig['enableRowTotal']==true>
      <tr>
        <td id="nonPivotGrandTotal">Grand Total</td>
        <#list filteredColumns as column>
          <#if reportData['grandTotal'].columnGrandTotal[column.field]??>
            <td style="text-align: right;">${reportData['grandTotal'].columnGrandTotal[column.field]}</td>
          <#else>
            <#if column.type == "number">
              <td style="text-align: right;">0</td>
            <#else>
              <td style="text-align: center;"></td>
            </#if>
          </#if>
        </#list>
        <#assign totalColumnGrandTotal = calculateColumnGrandTotal()>
        <#if reportData['grandTotal']?? && subTotalConfig?? && subTotalConfig['enableColumnTotal']?? && subTotalConfig['enableColumnTotal']==true>
          <#if totalColumnGrandTotal??>
            <td style="text-align: right;">${totalColumnGrandTotal}</td>
          </#if>
        </#if>
      </tr>
    </#if>
    <#list reportData['nonPivotedData'] as data>
      <tr>
        <#if subTotalConfig?? && subTotalConfig['enableRowTotal']?? && subTotalConfig['enableRowTotal']==true>
          <td></td>
        </#if>
        <#list filteredColumns as column>
          <#if data?? && data.values?? && data.values?has_content && data.values[column['field']]?? && data.values[column['field']]?has_content>
            <#if column['type'] == "number">
              <td style="text-align: right;"
                <#if data.breachDetails?? && data.breachDetails?has_content>
                  <#list data.breachDetails as breachField, breachStatus>
                    <#if breachField == column['field'] && breachStatus?? && breachStatus?has_content>
                      class="${breachStatus}"
                    </#if>
                  </#list>
                </#if>
              >
                ${data.values[column['field']]}
                <#if column['format']?? && column['format']?has_content && column['format']=="ROUND_PERCENTAGE"></#if>
              </td>
            <#else>
              <td
                <#if data.breachDetails?? && data.breachDetails?has_content>
                  <#list data.breachDetails as breachField, breachStatus>
                    <#if breachField == column['field'] && breachStatus?? && breachStatus?has_content>
                        class="${breachStatus}"
                      </#if>
                  </#list>
                </#if>
              >
                ${data.values[column['field']]}
              </td>
            </#if>
          <#else>
            <td></td>
          </#if>
        </#list>
        <#if reportData['grandTotal']?? && subTotalConfig?? && subTotalConfig['enableColumnTotal']?? && subTotalConfig['enableColumnTotal']==true>
          <#if reportData['grandTotal'].rowGrandTotal??>
            <td style="text-align: right;">${reportData['grandTotal'].rowGrandTotal[data.id]}</td>
          </#if>
        </#if>
      </tr>
    </#list>
  <#else>
    <thead>
      <tr class="content colr">
        <th>&nbsp;&nbsp;No Data to display for this report.&nbsp;&nbsp;</th>
      </tr>
    </thead>
  </#if>
<#else>
  <thead>
    <tr class="content salt">
      <th>&nbsp;&nbsp;Email Body is not enabled for this report.&nbsp;&nbsp;</th>
    </tr>
  </thead>
</#if>
</table>

<#-- Function to calculate total of columnGrandTotal -->
<#function calculateColumnGrandTotal>
  <#if reportData['grandTotal']??>
    <#assign totalColumnGrandTotal = 0>
    <#list numberColumns as column>
      <#if reportData['grandTotal'].columnGrandTotal[column.field]??>
        <#assign totalColumnGrandTotal += reportData['grandTotal'].columnGrandTotal[column.field]>
      </#if>
    </#list>
    <#return totalColumnGrandTotal>
  </#if>
</#function>