<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<html>

<head>
    <title>提示</title>
    <style>
        a{
            color: blue;
            cursor: pointer;
        }
        a:hover{
            color: red;
        }
    </style>
</head>
<body>
<div style="text-align: center;">
    <div style="width: 350px;height: 190px;margin: 0 auto;">
        <img src="${ctx}/static/images/error.png" />
    </div>
    <a onclick="show()">点击我查看异常信息</a>
    <div id="message" style="display: none;padding: 0 100px;height: 390px;overflow-y: auto;">
      ${param.message}
    </div>
</div>
<script>
    function show() {
        document.getElementById("message").style.display="block";
    }
</script>
</body>
</html>
