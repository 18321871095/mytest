<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%
    String register=response.getHeader("register");
    String time=response.getHeader("time");
%>
<html>
<head>
    <title>流程</title>
    <link rel="stylesheet" href="${ctx}/static/layui/css/layui.css">
    <link rel="stylesheet" href="${ctx}/static/css/application.css">
</head>
<body style="margin:0px;padding: 0px">
<div class="tab">
    <div class="hd">
        <a href="#" class="active">表单信息</a>
        <a href="#">流程图</a>
    </div>
    <div class="bd">
        <div id="biaodan" class="bd-son" style="display: block;">
            <div class="my-btn-wrap">
                <ul class="my-btn clearfix">
                    <li  name="submitFormInfo" class="" style="border: 1px solid #3385ff;background: #3385ff;cursor: pointer;">
                        <a id="mytijiao" href="#" style="color: #fff" >提交</a>
                    </li>
                    <li name='baocunFormInfo1' style="border: 1px solid #FFB800;background:#FFB800;cursor: pointer;">
                        <a href="#" style="color: #fff">保存</a>
                    </li>
                </ul>
            </div>
            <div id="applicationForm"></div>
            <div class="opinion" id="tijiaoComment"></div>
            <div class="appendix" id="shangcuhanFuJian"></div>
        </div>
        <div class="bd-son" id="propic">
        </div>
    </div>
</div>
<div style="width: 120px;height: 50px;border: 1px solid #00a0e9;display: <%=register.equals("false")?"block":"none" %>;
        position: fixed;right: 10px;bottom: 70px;background: #00a0e9;color: #fff;font-size: 15px;text-align: center;">
    <div onclick="this.parentNode.style.display='none'" style="width: 20px;height: 20px; line-height: 20px; background: #999; box-sizing: border-box;border-radius: 50%; text-align: center;
position: absolute; left: -10px; top: -10px; color: #fff;">
        <a href="#" style="color: #fff; display: inline-block;">×</a>
    </div>
    试用阶段，剩余<%=time %>天！
</div>
</div>
</body>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
<script src="${ctx}/static/js/selfAdaption.js"></script>

<script>
    $(function () {
        var depid="${requestScope.depid}";
        var proname=decodeURI("${requestScope.proname}");
        var proNameParam=decodeURI("${requestScope.proNameParam}");
        var processDefinitionID="${requestScope.processDefinitionID}";
        var requestid="${param.requestid}";
        var reportName=decodeURI("${param.reportName}");
        var userName=parent.Dec.personal.username;
        var userRealName=parent.Dec.personal.displayName.split("(")[0];
        var flag="${param.state}";
        var taskName="${param.taskName}";
        var propicInit=false;
        var iswritecomment="${param.iswritecomment}";

        $('.hd a').click(function(){
            $('.hd a').eq($(this).index()).addClass('active').siblings().removeClass('active');

            if($(this).index()===0){
                $("#biaodan").show();
                $("#propic").hide();
            }else{
                $("#biaodan").hide();
                $("#propic").show();
                if(!propicInit){
                    propicInit=true;
                    inintProPic(processDefinitionID,depid);
                }
            }
        });

        layui.use(['layer'], function(){
            var applicationBaoCun_layer = layui.layer;
            if(flag==='1'){
                depid="${param.deployid}";
                proname=decodeURI("${param.baocunProname}");
                processDefinitionID="${param.proDefineId}";
                var tijiaoName=decodeURI("${param.tijiaoName}")==''?'提交':decodeURI("${param.tijiaoName}");
                $("#mytijiao").text(tijiaoName);
                var srcurl="${ctx}/decision/view/report?viewlet="+encodeURI(reportName)+"&op=write&__cutpage__=v"+"&requestid="+requestid;
                $("#applicationForm").append("<iframe id=\"reportFrame\" frameborder=\"0\" src="+srcurl+" width = 100%   frameborder=\"0\"></iframe>");
                //报表自适应高度
                selfAdaption("reportFrame");
                $("#tijiaoComment").append("<h1 class='opinion-title'>提交意见</h1><textarea id='commentinfo'>"+decodeURI("${param.comment}")+"</textarea>");
                //var attachment="${param.param0}"=="" ? "" : "${param.param0}"+"\\"+"${param.param1}"+"\\"+decodeURI("${param.attachment}");
                $("#shangcuhanFuJian").append("<h1 onclick='selFile()' style='cursor: pointer;' class='appendix-title'>上传附件</h1>")
                    .append("<input id='showFileName' style='background: transparent;border: none ' value="+decodeURI("${param.attachment}")+" >" +
                        "<input onchange='showFile()' type='file' id='file' style='display: none' />");
                // console.log("${param.param0}"+"\\"+"${param.param1}"+"\\"+decodeURI("${param.attachment}"));
            }else {
                var index=applicationBaoCun_layer.load(2,{offset:'200px'});
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    data:{processDefinitionID:processDefinitionID},
                    url: "${ctx}/processInfo/applicationForm",
                    success: function (data) {
                        applicationBaoCun_layer.close(index);
                        if(data.msg==='success'){
                            requestid=data.result.requestid;
                            reportName=data.result.reportName;
                            taskName=data.result.taskName;
                            iswritecomment=data.result.iswritecomment;
                            var btnName=data.result.tijiaoName==''?'提交':data.result.tijiaoName;
                            $("#mytijiao").text(btnName);
                            var srcurl="${ctx}/decision/view/report?viewlet="+encodeURI(reportName)+"&op=write"+"&requestid="+requestid;
                            $("#applicationForm").append("<iframe id='reportFrame' src="+srcurl+" width = 100%   frameborder='0'></iframe>");
                            //报表自适应高度
                            selfAdaption("reportFrame");
                            $("#tijiaoComment").append("<h1 class='opinion-title'>提交意见</h1><textarea id='commentinfo' placeholder='请输入内容'></textarea>");
                            $("#shangcuhanFuJian").append("<h1 onclick='selFile()' style='cursor: pointer;' class='appendix-title'>上传附件</h1>")
                             .append("<input id='showFileName' style='background: transparent;border: none '/><input onchange='showFile()' type='file' id='file' style='display: none' />");

                        }else{
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取表单信息错误："+data.result);
                        }
                    },
                    error: function (e, jqxhr, settings, exception) {
                        applicationBaoCun_layer.close(index);
                        alert('服务器响应失败!!!')
                    }
                });
            }

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
                           var fileObj = document.getElementById("file").files[0]; // js 获取文件对象
                           var form = new FormData();
                           // form.append("tiaojian",JSON.stringify(tiaojian_array));
                           form.append("commentinfo",$("#commentinfo").val());
                           form.append("proname",proname+proNameParamVaule);
                           form.append("userName",userName);
                           form.append("userRealName",userRealName);
                           form.append("state",flag);
                           form.append("requestid",requestid);
                           form.append("reportName",reportName);
                           form.append("processDefinitionID",processDefinitionID);
                           form.append("taskid","");
                           form.append("seesionid",seesionid);
                           if(typeof (fileObj)!="undefined"){
                               form.append("file", fileObj);
                           }
                           var index=applicationBaoCun_layer.load(2,{offset:'200px'});
                           $.ajax({
                               type: "POST",
                               data:form,
                               dataType: "json",
                               processData:false,
                               contentType: false,
                               url: "${ctx}/processInfo/guanlianproyuyewu",
                               success: function (data) {
                                   applicationBaoCun_layer.close(index);
                                   if(data.msg==='success'){
                                       //提交数据入库
                                       document.getElementById('reportFrame').contentWindow.contentPane.writeReport();
                                       applicationBaoCun_layer.confirm('提交成功', {
                                           btn: ['确定'],
                                           offset:'200px'
                                       },function(){
                                           window.parent.FS.tabPane.closeActiveTab();
                                       });
                                   }else if(data.msg==='001'){
                                       applicationBaoCun_layer.alert("分支条件都不成立，流程无法继续进行",{offset:'200px',icon:2});
                                   }
                                   else{
                                       window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("提交流程错误："+data.result);
                                   }
                               },
                               error: function (e, jqxhr, settings, exception) {
                                   applicationBaoCun_layer.close(index);
                                   alert('服务器响应失败!!!')
                               }
                           });
                       },function () {
                             document.getElementById('reportFrame').contentWindow.contentPane.verifyReport();
                             applicationBaoCun_layer.msg("模板数据校验不通过请检查",{offset:'200px'});
                         }
                     );

                   /*  document.getElementById('reportFrame').contentWindow.contentPane.
                     verifyAndWriteReport(true,undefined,
                         function(){

                     },function () {});*/
                 }
               }else{
                   applicationBaoCun_layer.msg("请填写意见！！！",{offset:'200px'});
               }
            });

            /*保存申请信息*/
            $(document).on("click","${'[name=\'baocunFormInfo1\']'}",function(){
                document.getElementById('reportFrame').contentWindow.contentPane._doVerify(function () {
                    var reserve_idex=applicationBaoCun_layer.load(2,{offset:'200px'});
                    var form_baocun = new FormData();
                    var proNameParamVaule=document.getElementById('reportFrame').contentWindow.contentPane.curLGP.getCellValue(proNameParam.toUpperCase());
                    var fileObj_baocun = document.getElementById("file").files[0]; // js 获取文件对象
                    form_baocun.append("processDefinitionID",processDefinitionID);
                    form_baocun.append("requestid",requestid);
                    form_baocun.append("reportName",reportName);
                    form_baocun.append("deployid",depid);
                    form_baocun.append("taskName",taskName);
                    form_baocun.append("proname",proname+proNameParamVaule);
                    form_baocun.append("userName",userName);
                    form_baocun.append("userRealName",userRealName);
                    form_baocun.append("commentinfo",$("#commentinfo").val());
                    //判断是否有附件文件
                    if(typeof (fileObj_baocun)!="undefined"){
                        form_baocun.append("file", fileObj_baocun);
                    }
                    $.ajax({
                        type: "POST",
                        data:form_baocun,
                        dataType: "json",
                        processData:false,
                        contentType: false,
                        url: "${ctx}/processInfo/reserveProInfo",
                        success: function (data) {
                            applicationBaoCun_layer.close(reserve_idex);
                            if(data.msg==='success'){
                                document.getElementById('reportFrame').contentWindow.contentPane.writeReport();
                                applicationBaoCun_layer.confirm('保存成功，可在待办中查看', {
                                    btn: ['确定'],
                                    offset:'200px'
                                },function(){
                                    window.parent.FS.tabPane.closeActiveTab();
                                });
                            }else if(data.msg==='fail'){
                                applicationBaoCun_layer.alert(data.result,{offset:'200px'})
                            }
                            else {
                                window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("保存信息错误："+data.result);
                            }
                        },
                        error: function (e, jqxhr, settings, exception) {
                            applicationBaoCun_layer.close(reserve_idex);
                            alert('服务器响应失败!!!')
                        }
                    })
                },function () {
                    document.getElementById('reportFrame').contentWindow.contentPane.verifyReport();
                    applicationBaoCun_layer.msg("模板数据校验不通过请检查",{offset:'200px'});
                });


            /*   document.getElementById('reportFrame').contentWindow.contentPane.verifyAndWriteReport(true,undefined,function(){

                },function () {});*/

            });

        });


    });

    function selFile () {
        document.getElementById("file").click();
    }
    function showFile () {
        var str=$("#file").val();
       // var fileName=str.split("\\")[2];
        var fileName=str.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");
        $("#showFileName").val(fileName);
    }
    //第一次渲染流程图
    function inintProPic(processDefinitionID,depid) {
        $("#propic").empty();
        var showAplicationCurrentProProcess="${ctx}/diagram-viewer/index.html?processDefinitionId="+processDefinitionID+"&depid="+depid;
        $("#propic").append("<iframe id='applicationLiuChengtu' frameborder=\"0\"  src="+showAplicationCurrentProProcess+" width = 100%  height = 90%></iframe>");
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
