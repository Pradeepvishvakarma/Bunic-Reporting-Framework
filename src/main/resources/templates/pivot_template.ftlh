<table width="100%" border="0" cellspacing="0" cellpadding="0" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <tr style="margin:2px 0 10px 20px;">
        <h2>${reportTitle!}</h2>
    </tr>
    <#if legends?? && legends?size gt 0>
        <tr>
            <td colspan="2" class="content-log">
                <ul style="list-style-type:square; padding:0; margin:2px 0 10px 20px;">
                    <#list legends as legend>
                        <li>${legend}</li>
                    </#list>
                </ul>
            </td>
        </tr>
    </#if>

    <tr>
      <td colspan="2" height="20pt">&nbsp;</td>
    </tr>
    <#if isPivotEnableRowGrouping?? && isPivotEnableRowGrouping == true>
        <tr>
            <td colspan="2">
                <#include "pivotEnableRowGrouping_table.ftlh">
            </td>
        </tr>
    <#elseif isPivotEnableColumnGrouping?? && isPivotEnableColumnGrouping == true>
        <tr>
            <td colspan="2">
                <#include "pivotEnableColumnGrouping_table.ftlh">
            </td>
        </tr>
    <#elseif isPivotDisableRowGrouping?? && isPivotDisableRowGrouping == true>
        <tr>
            <td colspan="2">
                <#include "pivotDisableRowGrouping_table.ftlh">
            </td>
        </tr>
    </#if>

  <tr>
        <td colspan="2" height="20pt">&nbsp;</td>
      </tr>
  <tr>
    <td colspan="2" class="content-log">${(attachmentMessage)!}</td>
  </tr>
</table>