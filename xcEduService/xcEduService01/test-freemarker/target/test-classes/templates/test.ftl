package templates

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
<table>
    <tr>
        <td>姓名</td>
    </tr>
    <#list list as stu>
        <tr>
            ${stu.name}
        </tr>
    </#list>
</table>
</body>
</html>