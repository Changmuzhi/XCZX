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
    <#list stu?keys as k>
        <tr>
            <td <#if stu[k].name =='小王'>style="background-color: red"</#if>>
                ${stu[k].name}
            </td>
        </tr>
    </#list>
</table>
</body>
</html>