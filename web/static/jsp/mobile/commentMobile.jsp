<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
<link rel="stylesheet" href="${ctx}/static/css/index.css">
<body >
<ul id="commentTbody" class="toDoDetails2List">
   <%-- <li>
        <span>节点：技术总监</span><span>操作类型：提交</span><span>发起人：王斗斗</span><span>时间：2019-09-12</span>
    </li>
    <li>
        <span>节点：技术总监</span><span>操作类型：提交</span><span>发起人：王斗斗</span><span>时间：2019-09-12</span>
    </li>
    <li>
        <span>节点：技术总监</span><span>操作类型：提交</span><span>发起人：王斗斗</span><span>时间：2019-09-12</span>
    </li>
    <li>
        <span>节点：技术总监</span><span>操作类型：提交</span><span>发起人：王斗斗</span><span>时间：2019-09-12</span>
    </li>
    <li>
        <span>节点：技术总监</span><span>操作类型：提交</span><span>发起人：王斗斗</span><span>时间：2019-09-12</span>
    </li>
    <li>
        <span>节点：技术总监</span><span>操作类型：提交</span><span>发起人：王斗斗</span><span>时间：2019-09-12</span>
    </li>--%>
</ul>

</body><script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script>
    $(function () {
        var proInstanceId="${param.proInstanceId}";
        var proDefinitionId="${param.proDefinitionId}";
        var activityid="${param.activityid}";
        var attachment="";var opreateType="";
        $.post("${ctx}/processInfo/getComment",
            {proInstanceId:proInstanceId,proDefinitionId:proDefinitionId,activityid:activityid},function (data) {
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
                    $("#commentTbody").append(" <li>\n" +
                        "<span>节点："+data.result[i].nodeName+"</span>" +
                        "<span>操作："+opreateType+"</span>" +
                        "<span>操作人："+data.result[i].opreateRealName+"</span>" +
                        "<span>时间："+data.result[i].opreateTime+"</span>\n" +
                        "<span>意见："+data.result[i].comment+"</span>\n" +
                        "    </li>");

                  /*  $("#commentTbody").append("<tr>"+
                        "<td>"+data.result[i].nodeName+"</td>"+
                        "<td>"+data.result[i].opreateRealName+"</td>"+
                        "<td>"+opreateType+"</td>"+
                        "<td>"+data.result[i].comment+"</td>"+
                        "<td>"+"<a class=\"mya\" href=${ctx}/processInfo/downloadAttachment?path="+encodeURI(data.result[i].attachmentId)+">" +attachment+"</a>"+"</td>"+
                        "<td>"+data.result[i].opreateTime+"</td>"+
                        "</tr>");*/
                }
            }
            else{
                window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取意见异常："+data.result);
            }
        });
    })
</script>

</html>
