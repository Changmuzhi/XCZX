package templates

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
<#--Hello ${name}!-->
<table>
    <tr>
        <td>姓名</td>
    </tr>
    <#list students as stu>
        <tr>
            <td>${stu.name}</td>
        </tr>
    </#list>
</table>
</body>
</html>