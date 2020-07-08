<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="${ctx}/static/css/mui.min.css">
    <link rel="stylesheet" href="${ctx}/static/css/muikuozhan.css">
    <link rel="stylesheet" href="${ctx}/static/css/index.css">
</head>

<body>

<div class="mui-content">
    <div class="processDetails">
        <div style="padding: 10px 10px;">
            <div id="segmentedControl" class="mui-segmented-control">
                <a id="showproinfo" class="mui-control-item mui-active" >流程信息</a>
                <a id="showpropic"  class="mui-control-item" >查看流程图</a
            </div>

        </div>
        <div>
            <div id="item1" class="mui-control-content mui-active">
            </div>
            <div id="item2" class="mui-control-content">
                222
            </div>
        </div>
    </div>
    <div id="BanLiTaskYiJian" class="toDoDetails2">
        <h3 class="newFlowTitle"><span class="newFlowTitleSpan"></span><span class="newFlowTitleTit">流转意见</span></h3>
        <ul id="toDoDetails2List" class="toDoDetails2List"></ul>
    </div>

</div>
</body>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/js/mui.js"></script>
<script src="${ctx}/static/js/muikuozhan.js"></script>
<script src="${ctx}/static/js/selfAdaption.js"></script>
<script>
    $(function () {

        $("#showproinfo").click(function () {
            $("#item1").show();
            $("#item2").hide();
            $(".processDetailsBtnBox").show();
            $("#BanLiTaskYiJian").show();
        });
        $("#showpropic").click(function () {
            $("#item1").hide();
            $("#item2").show();
            $(".processDetailsBtnBox").hide();
            $("#BanLiTaskYiJian").hide();

        });
        var reportName=decodeURI("${param.reportName}");
        var requestid="${param.requestid}";
        var proInsID = "${param.proinsid}";
        var processDefinitionID ="${param.proDefineID}";
        var activityid ="${param.activityid}";
        ininReportFrom(reportName,requestid,proInsID);
        inintProPic(processDefinitionID,proInsID);
        initcomment(proInsID,processDefinitionID,activityid);
    });
    //渲染表单
    function ininReportFrom(reportName,requestid,proInsID){

        var srcurl="${ctx}/decision/view/report?viewlet="+encodeURI(reportName)+"&op=view&__cutpage__=v&requestid="+requestid+"&processInstanceId="+proInsID;
        $("#item1").append("<iframe id=\"reportFrame\" frameborder=\"0\" src="+srcurl+" width=\"100%\" height='80%'  frameborder=\"0\"></iframe>");
        //报表自适应高度
        selfAdaption("reportFrame");
    }

    //第一次渲染流程图
    function inintProPic(processDefinitionID,proInsID) {
        $("#item2").empty();
        var showAplicationCurrentProProcess="${ctx}/diagram-viewer/index.html?processDefinitionId="+processDefinitionID+"&processInstanceId="+proInsID;
        $("#item2").append("<iframe id='applicationLiuChengtu' frameborder=\"0\"  src="+showAplicationCurrentProProcess+" width = 100%  height = 90%></iframe>");
        var liuchengtu=setInterval(function () {
            var num= $("#applicationLiuChengtu").contents().find("svg").find('text').length;
            console.log("num:"+num)
            if(num>0){

                $("#applicationLiuChengtu").contents().find("svg").find('text').each(function () {
                    if($(this).attr("fill")=='#000000'){
                        var old= $(this).attr('y');

                        $(this).attr('y',0);

                    }
                });
                clearInterval(liuchengtu);
            }
        },1000);
    }

    //加载意见
    function initcomment(proInstanceId,proDefinitionId,activityid){
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
                        $(".toDoDetails2List").append(" <li>\n" +
                            "<span>节点："+data.result[i].nodeName+"</span>" +
                            "<span>操作："+opreateType+"</span>" +
                            "<span>操作人："+data.result[i].opreateRealName+"</span>" +
                            "<span>时间："+data.result[i].opreateTime+"</span>\n" +
                            "<span>意见："+data.result[i].comment+"</span>\n" +
                            "    </li>");

                    }
                }
                else{
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取意见异常："+data.result);
                }
            });
    }


    function getiswritecomment(iswritecomment) {
        if("true"==iswritecomment){
            if($("#commentinfo").val()==''){
                return false;
            }else
            {
                return true;
            }
        }else{
            return true;
        }
    }
</script>
</html>
