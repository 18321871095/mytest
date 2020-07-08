<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%
    String register=response.getHeader("register");
    String time=response.getHeader("time");
%>
<script>
    var register="<%=register %>";
    if(register=='false'){
        alert("插件试用阶段，还剩余<%=time %>天")
    }
</script>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <title>流程</title>
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
                <a id="showpropic"  class="mui-control-item" >查看流程图</a>
            </div>
        </div>
        <div>
            <div id="item1" class="mui-control-content mui-active">
            </div>
            <div id="item2" class="mui-control-content">
            </div>
        </div>
    </div>
    <div id="tijiaoComment" class="processDetailsBtnBoxTextareaBox" style="margin-bottom: 0px">
    </div>
    <div id="shangcuhanFuJian" class="processDetailsBtnBoxTextareaBox" style="margin-top: 0px;"></div>
    <div class="processDetailsBtnBox">
        <div class="processDetailsBtn">
            <button name="submitFormInfo" id="mytijiao_mobile" class="processDetailsBtnSubmit">提交</button>
            <button name="baocunFormInfo1" class="processDetailsBtnKeep">保存</button>
        </div>
    </div>
</div>
</body>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/js/mui.js"></script>
<script src="${ctx}/static/js/muikuozhan.js"></script>
<script src="${ctx}/static/js/selfAdaption.js"></script>

<script>

    $(function () {
        var depid="${requestScope.depid}";
        var proname=decodeURI("${requestScope.proname}");
        var proNameParam=decodeURI("${requestScope.proNameParam}");
        var processDefinitionID="${requestScope.processDefinitionID}";

        var requestid="${param.requestid}";
        var reportName=decodeURI("${param.reportName}");
        var flag="${param.state}";
        var taskName="${param.taskName}";
        var propicInit=false;
        var iswritecomment="${param.iswritecomment}";

        $("#showproinfo").click(function () {
            $("#item1").show();
            $("#item2").hide();
            $("#tijiaoComment").show();
            $(".processDetailsBtnBox").show();
            $("#shangcuhanFuJian").show();
        });
        $("#showpropic").click(function () {
            $("#item1").hide();
            $("#item2").show();
            $("#tijiaoComment").hide();
            $(".processDetailsBtnBox").hide();
            $("#shangcuhanFuJian").hide();
            if(!propicInit){
                propicInit=true;
                inintProPic(processDefinitionID,depid);
            }
        });
        mui.showLoading("获取表单信息","div");
        if(flag=="1"){
            depid="${param.deployid}";
            proname=decodeURI("${param.baocunProname}");
            processDefinitionID="${param.processDefinitionID}";
            var tijiaoName=decodeURI("${param.tijiaoName}")==''?'提交':decodeURI("${param.tijiaoName}");
            $("#mytijiao").text(tijiaoName);
            var srcurl="${ctx}/decision/view/report?viewlet="+encodeURI(reportName)+"&op=write&__cutpage__=v"+"&requestid="+requestid;

            $("#item1").append("<iframe id=\"reportFrame\" frameborder=\"0\" src="+srcurl+" width = 100%   frameborder=\"0\"></iframe>");
            //报表自适应高度
            selfAdaption("reportFrame");
            $("#tijiaoComment").append("<h1 style='font-size:17px;' class='opinion-title'>提交意见</h1><textarea id='commentinfo'>"+decodeURI("${param.comment}")+"</textarea>");
            //var attachment="${param.param0}"=="" ? "" : "${param.param0}"+"\\"+"${param.param1}"+"\\"+decodeURI("${param.attachment}");
            //$("#shangcuhanFuJian").append("<h1 style='font-size:17px;' class='opinion-title'>上传附件</h1>")
              //  .append("<input style='background: transparent;border: none ' id='showFileName' value="+decodeURI("${param.attachment}")+" >" +
              //      "<input onchange='showFile()' type='file' id='file' style='display: none' style='display: none'  />");
            mui.hideLoading();

        }else{
            $.ajax({
                type: "POST",
                dataType: "json",
                data:{processDefinitionID:processDefinitionID},
                url: "${ctx}/mobile/applicationForm",
                success: function (data) {
                    mui.hideLoading();
                    if(data.msg==='success'){
                        requestid=data.result.requestid;
                        reportName=data.result.reportName;
                        taskName=data.result.taskName;

                        iswritecomment=data.result.iswritecomment;

                        var btnName=data.result.tijiaoName==''?'提交':data.result.tijiaoName;
                        $("#mytijiao_mobile").text(btnName);
                        var srcurl="${ctx}/decision/view/report?viewlet="+encodeURI(reportName)+"&op=write&view=h5"+"&requestid="+requestid;

                        $("#item1").append("<iframe id='reportFrame' src="+srcurl+" width = 100%   frameborder='0'></iframe>");
                        //报表自适应高度
                        selfAdaption("reportFrame");
                        $("#tijiaoComment").append("<h1 style='font-size:17px;' class='opinion-title'>提交意见</h1><textarea id=\"commentinfo\" rows=\"5\" placeholder=\"请输入意见\" class=\"processDetailsBtnBoxTextarea\"></textarea>");
                      //  $("#shangcuhanFuJian").append("<h1 style='font-size:17px;' class='opinion-title'>上传附件</h1>")
                      //   .append("<img id='uploadfile' style='width: 30px;height:30px;margin-left: 13px;' src='${ctx}/static/images/uploadfile.png'/><input id='showFileName' readonly='true' style='background: transparent;border: none ' /><input onchange='showFile()' type='file' id='file' style='display: none'   />");
                    }else{
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取表单信息错误："+data.result);
                    }
                },
                error: function (e, jqxhr, settings, exception) {
                    mui.hideLoading();
                    alert('服务器响应失败!!!')
                }
            });
        }

        /*
        * 附件选择
        * */
        $(".mui-content").on("click","#uploadfile",function () {
            alert("很抱歉，移动端上传附件功能暂不支持！");
            //$("#file").click();
        })
        $(".mui-content").on("click","#showFileName",function () {
            alert("很抱歉，移动端上传附件功能暂不支持！");
            $("#file").click();
        })

        /*提交申请信息并与流程相关联*/
        $(document).on("click","${'[name=\'submitFormInfo\']'}",function(){
            if(getiswritecomment(iswritecomment)){
                if(true){
                    document.getElementById('reportFrame').contentWindow.contentPane._doVerify(
                        function () {
                            //校验成功
                            var seesionid=document.getElementById('reportFrame').contentWindow.contentPane.currentSessionID;
                            //流程名带参数
                            var proNameParamVaule=document.getElementById('reportFrame').contentWindow.contentPane.curLGP.getCellValue(proNameParam.toUpperCase());
                            var form = new FormData();
                            // form.append("tiaojian",JSON.stringify(tiaojian_array));
                            form.append("commentinfo",$("#commentinfo").val());
                            form.append("proname",proname+proNameParamVaule);
                            form.append("state",flag);
                            form.append("requestid",requestid);
                            form.append("reportName",reportName);
                            form.append("processDefinitionID",processDefinitionID);
                            form.append("taskid","");
                            form.append("seesionid",seesionid);
                            mui.showLoading("提交","div");
                            $.ajax({
                                type: "POST",
                                data:form,
                                dataType: "json",
                                processData:false,
                                contentType: false,
                                url: "${ctx}/mobile/guanlianproyuyewu",
                                success: function (data) {
                                    mui.hideLoading();
                                    if(data.msg==='success'){
                                        //提交数据入库
                                        document.getElementById('reportFrame').contentWindow.contentPane.writeReport();
                                        mui.alert('提交成功',function(){
                                            window.history.go(-1);
                                           // window.parent.FS.tabPane.closeActiveTab();
                                        });
                                    }else if(data.msg==='001'){
                                        mui.alert("分支条件都不成立，流程无法继续进行");
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
                            document.getElementById('reportFrame').contentWindow.contentPane.verifyReport();
                            mui.alert("模板数据校验不通过请检查");
                        }
                    );
                }
            }else{
                applicationBaoCun_layer.msg("请填写意见！！！",{offset:'200px'});
            }
        });

        /*保存申请信息*/
        $(document).on("click","${'[name=\'baocunFormInfo1\']'}",function(){
            document.getElementById('reportFrame').contentWindow.contentPane._doVerify(function () {
                mui.showLoading("保存","div");
                var form_baocun = new FormData();
                var proNameParamVaule=document.getElementById('reportFrame').contentWindow.contentPane.curLGP.getCellValue(proNameParam.toUpperCase());
                form_baocun.append("processDefinitionID",processDefinitionID);
                form_baocun.append("requestid",requestid);
                form_baocun.append("reportName",reportName);
                form_baocun.append("deployid",depid);
                form_baocun.append("taskName",taskName);
                form_baocun.append("proname",proname+proNameParamVaule);
                form_baocun.append("userName","admin");
                form_baocun.append("userRealName","admin");
                form_baocun.append("commentinfo",$("#commentinfo").val());
                $.ajax({
                    type: "POST",
                    data:form_baocun,
                    dataType: "json",
                    processData:false,
                    contentType: false,
                    url: "${ctx}/mobile/reserveProInfo",
                    success: function (data) {
                        mui.hideLoading();
                        if(data.msg==='success'){
                            document.getElementById('reportFrame').contentWindow.contentPane.writeReport();
                            mui.alert('保存成功，可在待办中查看',"提示",function(){

                               // window.parent.FS.tabPane.closeActiveTab();
                                window.history.go(-1);
                            });
                        }else if(data.msg==='fail'){
                             mui.alert(data.result)
                        }
                        else {
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("保存信息错误："+data.result);
                        }
                    },
                    error: function (e, jqxhr, settings, exception) {
                        mui.hideLoading();
                        alert('服务器响应失败!!!')
                    }
                })
            },function () {
                document.getElementById('reportFrame').contentWindow.contentPane.verifyReport();
                mui.alert("模板数据校验不通过请检查");
            });


            /*   document.getElementById('reportFrame').contentWindow.contentPane.verifyAndWriteReport(true,undefined,function(){

                },function () {});*/

        });
    });

    //第一次渲染流程图
    function inintProPic(processDefinitionID,depid) {
        $("#item2").empty();
        var showAplicationCurrentProProcess="${ctx}/diagram-viewer/index.html?processDefinitionId="+processDefinitionID+"&depid="+depid;
        $("#item2").append("<iframe id='applicationLiuChengtu' frameborder=\"0\"  src="+showAplicationCurrentProProcess+" width = 100%  height = 90%></iframe>");
        var liuchengtu=setInterval(function () {
            var num= $("#applicationLiuChengtu").contents().find("svg").find('text').length;
            if(num>0){
                $("#applicationLiuChengtu").contents().find("svg").find('text').each(function () {
                    if($(this).attr("fill")=='#000000'){
                        var old= $(this).attr('y');
                        $(this).attr('y',parseInt(old)-20.5);
                    }
                });
                clearInterval(liuchengtu);
            }
        },1000);
    }

    function showFile() {
        var str=$("#file").val();
        // var fileName=str.split("\\")[2];
        var fileName=str.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");
        $("#showFileName").val(fileName);
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
