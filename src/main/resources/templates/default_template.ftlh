<html xmlns:v="urn:schemas-microsoft-com:vml"
      xmlns:o="urn:schemas-microsoft-com:office:office"
      xmlns:w="urn:schemas-microsoft-com:office:word"
      xmlns:m="http://schemas.microsoft.com/office/2004/12/omm1"
      xmlns="http://www.w3.org/TR/REC-html40">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${reportName}</title>
    <style type="text/css">
        <#include "style.css">
    </style>
</head>
<body>
    <table class="outer-border" align="center" role="presentation" cellspacing="0" cellpadding="0" border="0"
                  style="margin:0 auto; background:#ffffff;">
        <tr>
            <td>
                <table style="max-width: 100%" role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin:0;">
                    <tr>
                        <td colspan="3" width="100%" align="center" bgcolor="black">
                            <#include "header.ftlh">
                        </td>
                    </tr>
                    <#if isNonPivotReport?? && isNonPivotReport>
                        <tr>
                            <td width="20">&nbsp;</td>
                            <td>
                                <#include "pivot_template.ftlh">
                            </td>
                            <td width="20">&nbsp;</td>
                        </tr>
                    <#else>
                        <tr>
                            <td width="20">&nbsp;</td>
                            <td>
                                <#include "non_pivot_template.ftlh">
                            </td>
                            <td width="20">&nbsp;</td>
                        </tr>
                    </#if>
                    <tr>
                        <td width="20">&nbsp;</td>
                        <td>
                            <#include "footer.ftlh">
                        </td>
                        <td width="20">&nbsp;</td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td align="left" valign="top">&nbsp;</td>
        </tr>
    </table>
</body>
</html>