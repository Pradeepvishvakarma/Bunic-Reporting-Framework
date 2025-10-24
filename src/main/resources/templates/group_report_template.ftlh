<html xmlns:v="urn:schemas-microsoft-com:vml"
      xmlns:o="urn:schemas-microsoft-com:office:office"
      xmlns:w="urn:schemas-microsoft-com:office:word"
      xmlns:m="http://schemas.microsoft.com/office/2004/12/omm1"
      xmlns="http://www.w3.org/TR/REC-html40">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${reportName!}</title>
    <style type="text/css">
        <#include "style.css">
    </style>
</head>

<body>
    <table class="outer-border" align="center" role="presentation" cellspacing="0" cellpadding="0" border="0" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
        <tr>
            <td>
                <table style="max-width: 100%;" role="presentation" cellspacing="0" cellpadding="0" border="0" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
                    <tr>
                        <td colspan="3" width="100%" align="center" bgcolor="black">
                            <#include "header.ftlh">
                        </td>
                    </tr>

                    <tr>
                        <td colspan="3" width="100%">
                            <#if groupReportContent??>
                                <#list groupReportContent as key, value>
                                    <table width="100%" style="margin:2px 0 10px 20px;" border="0" cellspacing="0" cellpadding="0" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
                                        <tr>
                                            <td>
                                                <div>
                                                    <#list value as htmlContent>
                                                        ${htmlContent?no_esc}
                                                    </#list>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </#list>
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <#include "group_report_footer.ftlh">
                        </td>
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