<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="style.css">
</head>
<body>

<#include "header.ftlh">
<div style="display: flex; flex-direction: column; align-items: center; margin-top: 40px; margin-bottom: 40px;">
    <table border="1" cellspacing="0" cellpadding="0" style="margin:0; padding:0;">
        <thead>
            <tr>
                <#list columns as col>
                    <th>${col.title!}</th>
                </#list>
            </tr>
        </thead>
        <tbody>
            <#list reportData as row>
                <tr>
                    <#list columns as col>
                        <#if col.type == "number">
                            <td style="text-align: right;"> ${row[col.field]!} </td>
                        <#else>
                            <td> ${row[col.field]!} </td>
                        </#if>
                    </#list>
                </tr>
            </#list>
        </tbody>
    </table>
</div>
<#include "footer.ftlh">

</body>
</html>
