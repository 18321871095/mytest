<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>流程详细信息</title>
    <link href="${ctx}/static/layui/css/layui.css" rel="stylesheet">
    <link href="${ctx}/static/css/hisProcessDetail.css" rel="stylesheet">
</head>
<body style="background-color:#fff ">

<div class="tab">
    <div class="hd">
        <a href="#" class="active">表单信息</a>
        <a href="#">流程图</a>
        <a href="#">审批意见</a>
    </div>
    <div class="bd">
        <div id="yishenqingbiaodan" class="bd-son" style="display: block;">
            <div id="form">

            </div>
        </div>
        <div class="bd-son" id="yishenqingpropic">
        </div>
        <div class="bd-son" id="yishenqingyijian">
        </div>
    </div>
</div>

<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
<script src="${ctx}/static/js/selfAdaption.js"></script>
<script>
    $(function () {
        var propicInit=false;
        var propicyijian=false;
        var businessKey="${param.businessKey}";
        var reportName=decodeURI("${param.reportName}");
        var proInsID="${param.proInsID}";
        var proDefinitionId="${param.proDefinitionId}";
        var activityid="${param.activityid}";
        var src="${ctx}/decision/view/report?viewlet="+encodeURI(reportName)+"&op=read"+"&requestid="+businessKey+"&__cutpage__=v&processInstanceId="+proInsID;
        $("#form").append("<iframe id='yishenqingReportFrame' src="+src+" width = 100%   frameborder='0'></iframe>");
        //报表自适应高度
        selfAdaption("yishenqingReportFrame");
        $('.hd a').click(function(){
            $('.hd a').eq($(this).index()).addClass('active').siblings().removeClass('active');
            if($(this).index()===0){
                $("#yishenqingbiaodan").show();
                $("#yishenqingpropic").hide();
                $("#yishenqingyijian").hide();
            }else if($(this).index()===1){
                $("#yishenqingbiaodan").hide();
                $("#yishenqingpropic").show();
                if(!propicInit){
                    propicInit=true;
                    yishenqinginintProPic(proDefinitionId,proInsID);
                }
            }else{
                $("#yishenqingbiaodan").hide();
                $("#yishenqingpropic").hide();
                $("#yishenqingyijian").show();
                if(!propicyijian){
                    propicyijian=true;
                    yishenqinginintYiJian(proInsID,proDefinitionId,activityid);
                }
            }
        });
    });

    //第一次渲染流程图
    function yishenqinginintProPic(proDefinitionId,proInsID) {
        $("#yishenqingpropic").empty();
        var showProPic="${ctx}/diagram-viewer/index.html?processDefinitionId="+proDefinitionId+"&processInstanceId="+proInsID;
        $("#yishenqingpropic").append("<iframe id='yishenqingLiuChengtu' frameborder=\"0\"  src="+showProPic+" width = 100%  height = 80%></iframe>");
        var liuchengtu=setInterval(function () {
            var num= $("#yishenqingLiuChengtu").contents().find("svg").find('text').length;
            if(num>0){
                $("#yishenqingLiuChengtu").contents().find("svg").find('text').each(function () {
                    if($(this).attr("fill")=='#000000'){
                        var old= $(this).attr('y');
                        $(this).attr('y',parseInt(old)-20.5);
                    }
                });
                clearInterval(liuchengtu);
            }
        },1000);
    }
    //第一次渲染意见
    function yishenqinginintYiJian(proInsID,proDefinitionId,activityid){
        $("#yishenqingyijian").empty();
        var showProYiJian="${ctx}/static/jsp/frontEnd/comment.jsp?proInstanceId="+proInsID+
            "&proDefinitionId="+proDefinitionId+"&activityid="+activityid;
        $("#yishenqingyijian").append("<iframe  frameborder=\"0\"  src="+showProYiJian+" width = 100%  " +
            " height = 80%></iframe>");
    }

</script>

</body>
</html>
