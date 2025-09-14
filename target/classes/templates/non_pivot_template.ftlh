<table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin:0;">
    <tr>
      <td colspan="2" height="30pt">&nbsp;</td>
    </tr>
    <tr>
      <td colspan="2" class="content-log">
        <ul style="list-style-type:square !important; padding:0; margin:2px 0 10px 30px;">
          <#list legends as legend>
            <li>${legend}</li>
          </#list>
        </ul>
      </td>
    </tr>

    <tr>
      <td colspan="2" height="38pt">&nbsp;</td>
    </tr>

  <tr>
    <td colspan="2">
      <#include "non_pivot_table.ftlh">
    </td>
  </tr>
  <tr>
        <td colspan="2" height="38pt">&nbsp;</td>
      </tr>
  <tr>
    <td colspan="2" class="content-log">${(attachmentMessage)!}</td>
  </tr>
</table>