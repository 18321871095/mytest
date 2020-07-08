<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <link href="${ctx}/static/layui/css/layui.css" rel="stylesheet">
    <link href="${ctx}/static/css/comment.css" rel="stylesheet">
    <style>
        .mya{
            color: blue;
        }
        .mya:hover{
            color: red;
        }
    </style>
</head>
<body style="background-color:#fff ">
    <div id="topContent" style="padding: 0px 30px">
            <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                <tr>
                    <th>流程节点名称</th>
                    <th>批注人</th>
                    <th>操作类型</th>
                    <th>意见</th>
                    <th>附件</th>
                    <th>批注时间</th>
                </tr>
                <tbody id="commentTbody">

                </tbody>
            </table>
        <%--content--%>
    </div>
</body>
<script src="${ctx}/static/layui/layui.js"></script>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script>
    $(document).ready(function () {
        var proInstanceId="${param.proInstanceId}";
        var proDefinitionId="${param.proDefinitionId}";
        var activityid="${param.activityid}";
        var attachment="";var opreateType="";
        layui.use('layer', function(){
            var layer = layui.layer;
              var index=layer.load(2,{offset:'200px'});
             $.post("${ctx}/processInfo/getComment",{proInstanceId:proInstanceId,proDefinitionId:proDefinitionId,activityid:activityid},function (data) {
                 layer.close(index);
                 if(data.msg==='success'){
                       $("#commentTbody").empty();
                       for(var i=0;i<data.result.length;i++){
                           if(data.result[i].attachmentId=="" && data.result[i].attachmentName==""){
                               attachment="";
                           }else {
                               attachment=data.result[i].attachmentName;
                           }
                          // 1：申请人提交 2：保存 3：驳回 4：撤回 5：转办 6：删除 7:通过
                           if(data.result[i].opreateType=='1'){
                               opreateType="提交";
                           }else if(data.result[i].opreateType=='3'){
                               opreateType="驳回";
                           }else if(data.result[i].opreateType=='5'){
                               opreateType="转办";
                           }
                           else if(data.result[i].opreateType=='4'){
                               opreateType="撤回";
                           }
                           else if(data.result[i].opreateType=='6'){
                               opreateType="删除";
                           }
                           else if(data.result[i].opreateType=='7'){
                               opreateType="通过";
                           }else if(data.result[i].opreateType=='8'){
                               opreateType="保存";
                           }
                           $("#commentTbody").append("<tr>"+
                                                       "<td>"+data.result[i].nodeName+"</td>"+
                                                       "<td>"+data.result[i].opreateRealName+"</td>"+
                                                       "<td>"+opreateType+"</td>"+
                                                       "<td>"+data.result[i].comment+"</td>"+
                                                       "<td>"+"<a class=\"mya\" href=${ctx}/processInfo/downloadAttachment?path="+encodeURI(data.result[i].attachmentId)+">" +attachment+"</a>"+"</td>"+
                                                       "<td>"+data.result[i].opreateTime+"</td>"+
                                                   "</tr>");
                       }
                   }
                   else{
                       window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取意见异常："+data.result);
                   }
               });
        });
    });
</script>
</html>
