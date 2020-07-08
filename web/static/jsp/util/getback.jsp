<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link href="${ctx}/static/layui/css/layui.css" rel="stylesheet">
    <script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
</head>
<body style="padding: 0px;margin: 0px">
<iframe id='myiframe' frameborder="0"
        src="${ctx}/diagram-viewer/index.html?processDefinitionId=${param.processDefinitionId}&processInstanceId=${param.processInstanceId}" width = 100% height = 90%></iframe>

<div style="height: 8%;text-align: center">

    <button id="queding" class="layui-btn" style="width: 100px">确定</button>
</div>

</body>
<script src="${ctx}/static/layui/layui.js"></script>
<script>
$(function () {
    layui.use(['layer'], function() {
        var layer = layui.layer;
       $("#queding").click(function () {
           var activiti = document.getElementById("myiframe").contentWindow.selectActiviti;

          if(activiti.type!='userTask'){
               layer.msg("请选任务节点",{offset:'200px'});
           }else{
               var index1=layer.load(2,{offset:'200px'});
               $.post("${ctx}/processInfo/getLastNode",{taskid:"${param.taskid}"},function (data) {
                   layer.close(index1);
                   if(data.msg=='success'){
                       var id=activiti.id;
                       var count=0;
                       for(var i=0;i<data.result.length;i++){
                           if(id==data.result[i].id){
                                break;
                           }else {
                               count++;
                           }
                       }
                       if(count==data.result.length){
                           layer.msg("请选择当前节点之前的节点",{offset:'200px'});
                       }else{
                           //var index2=layer.load(2,{offset:'200px'});
                            alert('退回功能bug在修复中。。。')
                       }
                   }else {
                       window.parent.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取已办理节点错误："+data.result);
                   }

               })
           }
       });

    });
})
    function queding() {


      //  window.parent.window.parent.FS.tabPane.closeActiveTab();
    }
</script>

</html>
