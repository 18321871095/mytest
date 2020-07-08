<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <div style="padding: 10px 10px;">
        <div id="segmentedControl" class="mui-segmented-control">
            <a id="banliInfo" class="mui-control-item mui-active" >表单信息</a>
            <a id="banliPic"  class="mui-control-item" >流程图</a>
        </div>
    </div>
    <div id="item1">
        <div id="BanLiTaskForm" class="toDoDetails">
            流程信息
        </div>
        <div id="tijiaoCommentBanLiTask" class="toDoDetailsBtnBoxTextareaBox">

        </div>
        <div id="BanLiTaskYiJian" class="toDoDetails2">
            <h3 class="newFlowTitle"><span class="newFlowTitleSpan"></span><span class="newFlowTitleTit">流转意见</span></h3>

            <%--<div style="height: 250px;overflow-y: auto;">
            <ul class="toDoDetails2List">
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
                </li>
                <li>
                    <span>节点：技术总监</span><span>操作类型：提交</span><span>发起人：王斗斗</span><span>时间：2019-09-12</span>
                </li>
            </ul>
            </div>--%>
        </div>
        <div class="toDoDetailsBtnBox">
            <div id="BanLiTaskBtn" class="toDoDetailsBtn">
              <%--  <button class="">提交</button>
                <button class="">保存</button>
                <button class="">删除</button>
                <button class="">保存</button>
                <button class="">提交</button>--%>
            </div>
        </div>
    </div>
    <div id="item2">
        <div id="propicBanLiTask">

        </div>
    </div>
</div>
</body>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/js/mui.js"></script>
<script src="${ctx}/static/js/muikuozhan.js"></script>
<script src="${ctx}/static/js/selfAdaption.js"></script>
<script>

</script>
<script>
    $(function () {
        var taskid="${param.taskid}";
        var proname=decodeURI("${param.proname}");
        var proDefinedId="${param.proDefinedId}";
        var proInstanceId="${param.proInstanceId}";
        var propicInit=false;
        var iswritecomment="";
        var reportName="";
        var propicInit_banli=false;
        $("#banliInfo").click(function () {
            $("#item1").show();
            $("#item2").hide();

        });
        $("#banliPic").click(function () {
            $("#item1").hide();
            $("#item2").show();
            if(!propicInit_banli){
                propicInit_banli=true;
                baliTaskinintProPic(proDefinedId,proInstanceId);
            }
        });
        mui.showLoading("获取任务信息","div");
        $.ajax({
            type: "POST",
            dataType: "json",
            data:{taskid:taskid},
            url: "${ctx}/mobile/userTaskForm",
            success: function (data) {
                mui.hideLoading();
                if(data.msg==='success'){
                    iswritecomment=data.result.iswritecomment;
                    reportName=data.result.moban;
                    var src= "${ctx}/decision/view/report?viewlet="+encodeURI(data.result.moban)+"&op=write&__cutpage__=v"+"&requestid="
                        +data.result.yeuwuid+"&processInstanceId="+data.result.processInstanceId;
                    $("#BanLiTaskForm").empty().append("<iframe frameborder=\"0\" id=\"banlireportFrame\" src="+src+" width = 100% ></iframe>");
                    //报表自适应高度
                    selfAdaption("banlireportFrame");
                    $("#tijiaoCommentBanLiTask").append("<textarea id=\"banlicomment\" rows=\"5\" placeholder=\"请输入审批意见\" class=\"toDoDetailsBtnBoxTextarea\"></textarea>");
        /*            $("#shangcuhanFuJianBanLiTask").append("<h1 onclick='selFileBanliTask()' style='cursor: pointer;' class='appendix-title'>上传附件</h1>")
                        .append("<input id='showFileName' style='background: transparent;border: none '/><input onchange='showFileBanliTask()' type='file' id='file' style='display: none' />");
*/
                    //添加意见
                    var showProYiJian="${ctx}/static/jsp/mobile/commentMobile.jsp?proInstanceId="+data.result.processInstanceId+
                        "&proDefinitionId="+data.result.proDefinitionId+"&activityid="+data.result.activityid;
                    $("#BanLiTaskYiJian").append("<iframe  frameborder=\"0\"  src="+showProYiJian+" width = 100%  " +
                        " height = 250px></iframe>");
                    //添加按钮
                    $("#BanLiTaskBtn").empty().append(getBtn(data.result.tijiao,data.result.istuihui,data.result.tuihui,
                        data.result.addHuiQianRen,data.result.zhuanban));
                }else if(data.msg==='2'){
                    $("body").empty().append("该任务已经办理了");
                }
                else{
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取表单信息错误："+data.result);
                }
            },
            error: function (e, jqxhr, settings, exception) {
                mui.hideLoading();
                alert('服务器响应失败!!!')
            }
        });

        /*完成任务*/
        $(document).on("click","${'#banliTaskTiJiao'}",function(){
            if(getiswritecommentBanLiTask(iswritecomment)){
                var banliTaskFlag=true;
                if(banliTaskFlag){
                    document.getElementById('banlireportFrame').contentWindow.contentPane._doVerify(
                        function () {
                            mui.showLoading("提交中","div");
                            var seesionid=document.getElementById('banlireportFrame').contentWindow.contentPane.currentSessionID;
                            var form = new FormData();
                            form.append("taskid",taskid);
                            form.append("proname",proname);
                            form.append("seesionid",seesionid);
                            form.append("commentinfo",$("#banlicomment").val());
                            $.ajax({
                                type: "POST",
                                data:form,
                                dataType: "json",
                                processData:false,
                                contentType: false,
                                url: "${ctx}/mobile/completeTask",
                                success: function (data) {
                                    mui.hideLoading();
                                    if(data.msg==='success'){
                                        document.getElementById('banlireportFrame').contentWindow.contentPane.writeReport();
                                        mui.alert('提交成功',function(){
                                            window.history.go(-1);
                                        });
                                    }else if(data.msg==='001'){
                                        mui.alert("分支条件都不成立，流程无法继续进行");
                                    }else if(data.msg==='002'){
                                        mui.alert("该任务已经不存在(可能流程设置了总时间),请刷新待办任务列表");
                                    }
                                    else{
                                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("提交流程错误："+data.result);
                                    }
                                },
                                error: function (e, jqxhr, settings, exception) {
                                    mui.hideLoading();
                                    alert('服务器响应失败!!!')
                                }
                            });
                        },function () {
                            document.getElementById('banlireportFrame').contentWindow.contentPane.verifyReport();
                            mui.alert("模板数据校验不通过请检查");
                        });
                }
            }else{
                mui.alert("请填写意见！！！");
            }


        });

        /*保存任务*/
        $(document).on("click","${'[name=\'baocunTask\']'}",function(){
            document.getElementById('banlireportFrame').contentWindow.contentPane._doVerify(function () {
                mui.showLoading("保存中","div");
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    data:{userName:"Lily",userRealName:"孙红",taskid:taskid},
                    url: "${ctx}/mobile/banliBaoCun",
                    success: function (data) {
                        mui.hideLoading();
                        if(data.msg==='success'){
                            document.getElementById('banlireportFrame').contentWindow.contentPane.writeReport();
                            mui.alert('保存成功');
                        }else if(data.msg==='001'){
                            mui.alert('任务不存在');
                        }else{
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("保存失败："+data.result);
                        }

                    },
                    error: function (e, jqxhr, settings, exception) {
                        mui.hideLoading();
                        alert('服务器响应失败!!!')
                    }
                });
            },function () {
                document.getElementById('banlireportFrame').contentWindow.contentPane.verifyReport();
                mui.alert("模板数据校验不通过请检查");
            });



            /* document.getElementById('banlireportFrame').contentWindow.contentPane.verifyAndWriteReport(true,undefined,function(){

             },function () {});*/
        });

        /*退回*/
        $(document).on("click","${'[name=\'backTask\']'}",function(){
            alert('backTask')
        })
        /*会签*/
        $(document).on("click","${'[name=\'addHuiQians\']'}",function(){
            alert('addHuiQians')
        })

        /*转办*/
        $(document).on("click","${'[name=\'zhuanbanTask\']'}",function(){
            alert('zhuanbanTask')
        })


    });

    //获取按钮
    function  getBtn(tijiao,istuihui,tuihui,huiqian,zhuanban) {
        var tijiaoHtml,baocunHtml,tuihuiHtml,tianjiahuiqianHtml,zhuanbanHtml;
        tijiaoHtml="<button id=\"banliTaskTiJiao\" name=\"submitFormInfoBanLiTask\" class=\"\">"+tijiao+"</button>";
        baocunHtml=" <button name=\"baocunTask\" class=\"\">保存</button>";
        if(istuihui=='true'){
            var temp=tuihui=='' ? '退回' : tuihui;
            tuihuiHtml="<button  name=\"backTask\" class=\"\">"+temp+"</button>";
        }else{
            tuihuiHtml="";
        }
        if(huiqian!=''){
            tianjiahuiqianHtml="<button name=\"addHuiQians\" name=\"baocunTask\" class=\"\">"+huiqian+"</button>";
        }else{
            tianjiahuiqianHtml="";
        }
        if(zhuanban!=''){
            zhuanbanHtml="<button  name=\"zhuanbanTask\" class=\"\">转办</button>";
        }else{
            zhuanbanHtml="";
        }
        return tijiaoHtml+baocunHtml+tuihuiHtml+tianjiahuiqianHtml+zhuanbanHtml;

    }

    //第一次渲染流程图
    function baliTaskinintProPic(proDefinitionId,proInsID) {
        $("#propicBanLiTask").empty();
        var showProPic="${ctx}/diagram-viewer/index.html?processDefinitionId="+proDefinitionId+"&processInstanceId="+proInsID;
        $("#propicBanLiTask").append("<iframe id='BanLiTaskgLiuChengtu' frameborder=\"0\"  src="+showProPic+" width = 100%  height = 80%></iframe>");
        var liuchengtu=setInterval(function () {
            var num= $("#BanLiTaskgLiuChengtu").contents().find("svg").find('text').length;
            if(num>0){
                $("#BanLiTaskgLiuChengtu").contents().find("svg").find('text').each(function () {
                    if($(this).attr("fill")=='#000000'){
                        var old= $(this).attr('y');
                        $(this).attr('y',parseInt(old)-20.5);
                    }
                });
                clearInterval(liuchengtu);
            }
        },1000);
    }

    function getiswritecommentBanLiTask(iswritecomment) {
        if("true"==iswritecomment){
            if($("#banlicomment").val()==''){
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
