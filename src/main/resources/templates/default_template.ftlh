<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dynamic Report</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>

<#include "header.ftlh">

<div class="container">
    <h2>Dynamic Report</h2>

    <table>
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
