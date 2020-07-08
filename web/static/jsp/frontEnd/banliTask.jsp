<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>办理</title>
    <link href="${ctx}/static/layui/css/layui.css" rel="stylesheet">
    <link href="${ctx}/static/css/banliTask.css" rel="stylesheet">
    <style>
        .laytable-cell-checkbox, .laytable-cell-numbers, .laytable-cell-radio, .laytable-cell-space{
            padding: 10px;!important;
        }
        .layui-table-view .layui-table{
            width: 100%;
        }
      /*  .mybtn{
            width: 80px;height: 30px;cursor: pointer;background:transparent;
            border: 1px solid #1ab394;text-align: center;font-weight: bold;
            margin-top: 10px;margin-left: 10px;
        }
        .mybtn:hover{
            background:#1ab394;
            color: #fff;
        }*/
        input[type="radio"] {
            width: 20px;
            height: 20px;
            opacity: 0;
        }
        .mylabel {
            position: absolute;
            left: 20px;
            top: 6px;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            border: 1px solid #999;
        }
        input:checked + label {
            background-color: #54c11a;
        }
        /*白色勾的样式*/
        input:checked + label::after {
            position: absolute;
            content: "";
            width: 5px;
            height: 10px;
            top: 3px;
            left: 7px;
            border: 3px solid #fff;
            border-top: none;
            border-left: none;
            transform: rotate(45deg)
        }
        .selBtn{
            width: 58px;
            background: #54c11a;
            border: 1px solid #54c11a;
            color: #fff;
            cursor: pointer;
        }


        table tr{ border-bottom: 1px solid #666; }
        table tr:first-child{ border-top: 1px solid #666;}
        table tr th{ border-left: 1px solid #666;}
        table tr th:last-child{ border-right: 1px solid #666;}
        table tr td{ border-left: 1px solid #666;}
        table tr td:last-child{ border-right: 1px solid #666;}
    </style>
</head>
<%
    String Authorization="";
    Cookie[] c=request.getCookies();
    for(Cookie cookie:c){
        if("fine_auth_token".equals(cookie.getName())){
            Authorization=cookie.getValue();
        }
    }
%>
<body>
<div class="tab">
    <div class="hd">
        <a href="#" class="active">表单信息</a>
        <a href="#">流程图</a>
    </div>
    <div class="bd">
        <div id="biaodanBanLiTask" class="bd-son" style="display: block;">
            <div class="my-btn-wrap" id="BanLiTaskBtn"></div>
            <div id="BanLiTaskForm"></div>
            <div class="opinion" id="tijiaoCommentBanLiTask"></div>
            <div class="appendix" id="shangcuhanFuJianBanLiTask"></div>
            <div  id="BanLiTaskYiJian"></div>
        </div>
        <div class="bd-son" id="propicBanLiTask">
        </div>
    </div>
</div>

<input type="hidden" id="zhuanbanrenid">

</body>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
<script src="${ctx}/static/js/selfAdaption.js"></script>
<script>

    var mytable;
    var banli_layer;
    var tempdata;
    $(document).ready(function () {
        var taskid="${param.taskid}";
        var proname=decodeURI("${param.proname}");
        var proDefinedId="${param.proDefinedId}";
        var proInstanceId="${param.proInstanceId}";
        var userName=parent.Dec.personal.username;
        var userRealName=parent.Dec.personal.displayName.split("(")[0];
        var propicInit=false;
        var iswritecomment="";
        var reportName="";
        $('.hd a').click(function(){
            $('.hd a').eq($(this).index()).addClass('active').siblings().removeClass('active');
            if($(this).index()===0){
                $("#biaodanBanLiTask").show();
                $("#propicBanLiTask").hide();
            }else{
                $("#biaodanBanLiTask").hide();
                $("#propicBanLiTask").show();
                if(!propicInit){
                    propicInit=true;
                    baliTaskinintProPic(proDefinedId,proInstanceId);
                }
            }
        });
        layui.use(['layer','form','table'], function(){
            banli_layer   = layui.layer,form = layui.form;
            mytable=layui.table;
            /*初始化页面*/
            var index=banli_layer.load(2,{offset:'200px'});

            $.ajax({
                type: "POST",
                dataType: "json",
                data:{taskid:taskid},
                url: "${ctx}/processInfo/userTaskForm",
                success: function (data) {
                    banli_layer.close(index);
                    if(data.msg==='success'){
                        iswritecomment=data.result.iswritecomment;
                        reportName=data.result.moban;
                        var src= "${ctx}/decision/view/report?viewlet="+encodeURI(data.result.moban)+"&op=write&__cutpage__=v"+"&requestid="
                            +data.result.yeuwuid+"&processInstanceId="+data.result.processInstanceId;
                        $("#BanLiTaskForm").empty().append("<iframe frameborder=\"0\" id=\"banlireportFrame\" src="+src+" width = 100% ></iframe>");
                        //报表自适应高度
                        selfAdaption("banlireportFrame");
                        $("#tijiaoCommentBanLiTask").append("<h1 class='opinion-title'>提交意见</h1><textarea id='banlicomment' placeholder='请输入内容'></textarea>");
                        $("#shangcuhanFuJianBanLiTask").append("<h1 onclick='selFileBanliTask()' style='cursor: pointer;' class='appendix-title'>上传附件</h1>")
                            .append("<input id='showFileName' style='background: transparent;border: none '/><input onchange='showFileBanliTask()' type='file' id='file' style='display: none' />");

                        //添加意见
                        var showProYiJian="${ctx}/static/jsp/frontEnd/comment.jsp?proInstanceId="+data.result.processInstanceId+
                            "&proDefinitionId="+data.result.proDefinitionId+"&activityid="+data.result.activityid;
                        $("#BanLiTaskYiJian").append("<iframe  frameborder=\"0\"  src="+showProYiJian+" width = 100%  " +
                            " height = 500px></iframe>");
                        //添加按钮

                        $("#BanLiTaskBtn").empty().append(getBtn(data.result.tijiao,data.result.istuihui,data.result.tuihui,
                            data.result.addHuiQianRen,data.result.zhuanban));
                    }else if(data.msg==='2'){
                        $("body").empty().append("<div style='text-align: center;'><img src='${ctx}/static/images/noTask.png' /></div>");
                    }
                    else{
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取表单信息错误："+data.result);
                    }
                },
                error: function (e, jqxhr, settings, exception) {
                    banli_layer.close(index);
                    alert('服务器响应失败!!!')
                }
            });

            mytable.on('radio(test)', function(obj){
                $("#zhuanbanrenid").val(obj.data.username);
            });

            /*完成任务*/
            $(document).on("click","${'#banliTaskTiJiao'}",function(){
                   if(getiswritecommentBanLiTask(iswritecomment)){
                       var banliTaskFlag=true;
                      if(banliTaskFlag){
                          document.getElementById('banlireportFrame').contentWindow.contentPane._doVerify(
                              function () {
                              var index=banli_layer.load(2,{offset:'200px'});
                              var seesionid=document.getElementById('banlireportFrame').contentWindow.contentPane.currentSessionID;
                              var fileObj = document.getElementById("file").files[0]; // js 获取文件对象
                              var form = new FormData();
                              form.append("taskid",taskid);
                              form.append("proname",proname);
                              form.append("seesionid",seesionid);
                              //判断是否有附件文件
                              if(typeof (fileObj)!="undefined"){
                                  form.append("file", fileObj);
                              }
                              form.append("commentinfo",$("#banlicomment").val());
                              $.ajax({
                                  type: "POST",
                                  data:form,
                                  dataType: "json",
                                  processData:false,
                                  contentType: false,
                                  url: "${ctx}/processInfo/completeTask",
                                  success: function (data) {
                                      banli_layer.close(index);
                                      if(data.msg==='success'){
                                          document.getElementById('banlireportFrame').contentWindow.contentPane.writeReport();
                                          banli_layer.confirm('提交成功', {
                                              btn: ['确定'],
                                              offset:'200px'
                                          },function(){
                                              var iframe=parent.window.document.getElementById("daibanTask_iframe");
                                              if(typeof(iframe)!='undefined' && iframe!=null){
                                                  iframe.contentWindow.daiban(1,true);
                                              }
                                              window.parent.FS.tabPane.closeActiveTab();
                                          });
                                      }else if(data.msg==='001'){
                                          banli_layer.alert("分支条件都不成立，流程无法继续进行",{offset:'200px',icon:2});
                                      }else if(data.msg==='002'){
                                          banli_layer.alert("该任务已经不存在(可能流程设置了总时间),请刷新待办任务列表",{offset:'200px',icon:2});
                                      }
                                      else{
                                          window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("提交流程错误："+data.result);
                                      }
                                  },
                                  error: function (e, jqxhr, settings, exception) {
                                      banli_layer.close(index);
                                      alert('服务器响应失败!!!')
                                  }
                              });
                          },function () {
                             document.getElementById('banlireportFrame').contentWindow.contentPane.verifyReport();
                              banli_layer.msg("模板数据校验不通过请检查",{offset:'200px'});
                          });

                      /*
                          document.getElementById('banlireportFrame').contentWindow.
                          contentPane.verifyAndWriteReport(true,undefined,function(){
                          },function () {});*/
                      }
                   }else{
                       banli_layer.msg("请填写意见！！！",{offset:'200px'});
                   }


            });

            /*保存任务*/
            $(document).on("click","${'[name=\'baocunTask\']'}",function(){

                document.getElementById('banlireportFrame').contentWindow.contentPane._doVerify(function () {
                    var index=banli_layer.load(2,{offset:'200px'});
                    $.ajax({
                        type: "POST",
                        dataType: "json",
                        data:{userName:userName,userRealName:userRealName,taskid:taskid},
                        url: "${ctx}/processInfo/banliBaoCun",
                        success: function (data) {
                            banli_layer.close(index);
                            if(data.msg==='success'){
                                document.getElementById('banlireportFrame').contentWindow.contentPane.writeReport();
                                banli_layer.msg('保存成功',{offset:'200px'});
                            }else if(data.msg==='001'){
                                banli_layer.msg('任务不存在',{offset:'200px'});
                            }else{
                                window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("保存失败："+data.result);
                            }

                        },
                        error: function (e, jqxhr, settings, exception) {
                            banli_layer.close(index);
                            alert('服务器响应失败!!!')
                        }
                    });
                },function () {
                    document.getElementById('banlireportFrame').contentWindow.contentPane.verifyReport();
                    banli_layer.msg("模板数据校验不通过请检查",{offset:'200px'});
                });



               /* document.getElementById('banlireportFrame').contentWindow.contentPane.verifyAndWriteReport(true,undefined,function(){

                },function () {});*/
            });


            /*查询退回任务节点*/
            $(document).on("click","${'[name=\'backTask\']'}",function(){
             //   alert('退回功能还在开发。。。')
                var index=banli_layer.load(1,{offset:'200px'});
                $.post("${ctx}/processInfo/getbackTaskNodeInfoTest",{taskid:"${param.taskid}"},function (data) {
                    var backNodeHtml="";
                    if(data.msg==='success'){
                        banli_layer.close(index);
                       var backNodeHtml2="<div id='node' style='height: 150px;overflow-y:auto;'>";
                        for(var i=0;i<data.result.length;i++){
                            if(data.result[i].state=='1'){
                                backNodeHtml2+="<div name='1' style=\"position: relative;line-height: 36px;\">" +
                                    "<input name='myNode' id="+i+" type=\"radio\"  value="+data.result[i].id+" >" +
                                    "<label onclick='showAssign(this)' class='mylabel' for="+i+"></label>" +
                                    "<span style='padding-left: 25px;font-size: 16px'>"+data.result[i].name+"</span>" +
                                    "</div>" ;
                            }else{
                                backNodeHtml2+="<div name='0' style=\"position: relative;line-height: 36px;\">" +
                                    "<input name='myNode' id="+i+" type=\"radio\"  value="+data.result[i].id+" >" +
                                    "<label onclick='showAssign(this)' class='mylabel' for="+i+"></label>" +
                                    "<span style='padding-left: 25px;font-size: 16px'>"+data.result[i].name+"</span>" +
                                    "</div>" ;
                            }

                        }
                        backNodeHtml2+="</div>";
                       backNodeHtml2+="<div style='height: 68px;text-align: center'>" +
                            "<textarea id='yijian' rows='4' cols='90' placeholder='请填写退回意见'></textarea>" +
                            "</div>"
                        +"<div style='height: 48px;padding: 0 10px;margin-top: 15px;'>"+
                            "<input id='selDep' type='text' placeholder='请输入部门' />&nbsp;&nbsp;&nbsp;" +
                           "<input  id='selRole' type='text' placeholder='请输入角色' />&nbsp;&nbsp;&nbsp;" +
                           "<button class='selBtn' onclick='searchDepAndRole()'>查询</button>"+
                            "<table border='1' width='100%'  style='margin-top: 15px'>" +
                                "<tr>" +
                                    "<td style='width: 6%;text-align: center;'></td>" +
                                    "<td style='width: 24%;text-align: center;'>用户名</td>" +
                                    "<td style='width: 35%;text-align: center;'>部门</td>" +
                                    "<td style='width: 35%;text-align: center;'>角色</td>" +
                                "</tr>"+
                            "</table>"
                            +"</div>"
                            +"<div style='height: 200px;overflow-y:auto;padding: 0 10px;margin-top: 12px;'>" +
                           "<table border='1' width='100%' >" +
                               "<tbody id='peopleTbody'>" +
                                  /* "<tr>" +
                                       "<td style='width: 6%;text-align: center;'><input type='checkbox' /></td>" +
                                       "<td style='width: 24%;text-align: center;'>Lily(孙红)</td>" +
                                       "<td style='width: 35%;text-align: center;'>人事部门</td>" +
                                       "<td style='width: 35%;text-align: center;'>普通角色</td>" +
                                   "</tr>"+*/
                               "</tbody>"+
                           "</table></div>";
                        var backNode_index=banli_layer.open({
                            type: 1,
                            title:'请选择退回节点(<span style="color: red;">注意：如果不选择人，退回按照整个节点退回)</span>',
                            content:backNodeHtml2,
                            area: ['700px', '600px'],
                            offset: '50px',
                            btn: ['确定', '取消'],
                            yes: function(index, layero){
                                var targetActivitiID=$(layero.selector).find("input[type='radio']:checked").val();
                                if($("#yijian").val()!=='' && targetActivitiID!=='' && typeof($("#yijian").val())!=='undefined'
                                && typeof(targetActivitiID)!=='undefined' ){
                                       var index=banli_layer.load(2,{offset:'200px'});
                                //注意如果用户名中出线（，则单任务回退会失败，办理人的名字不全了，也没有提醒
                                  /*  console.log(targetActivitiID)
                                    console.log(getAssigns())*/
                                $.post("${ctx}/processInfo/backTaskNode",{targetActivitiID:targetActivitiID,taskid:taskid,
                                        userName:userName,userRealName:userRealName,commentinfo:$("#yijian").val(),
                                        assign:getAssigns(),reportName:reportName},function(data){
                                        banli_layer.close(index);
                                        if(data.msg==='success') {
                                            alert('退回成功');
                                            window.parent.FS.tabPane.closeActiveTab();
                                        }else if(data.msg==='fail'){
                                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("退回错误："+data.result);
                                        }else{
                                            banli_layer.msg(""+data.result,{offset:'200px'});
                                        }
                                    });
                                }else{
                                    banli_layer.msg("请选择节点和填写驳回意见",{offset:'200px'});
                                }
                            }
                            ,btn2: function(index, layero){
                            }
                        })
                    }else{
                        banli_layer.close(index);
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取退回节点信息错误："+data.result);
                    }
                });
            });

            /*转办任务*/
            $(document).on("click","${'[name=\'zhuanbanTask\']'}",function(){
                var zhuanban_index=banli_layer.open({
                    type: 1,
                    title:'请选择转办人',
                    content:'<div style="padding: 0px 10px;">' +
                        '<input id="searchPeople" /><button class="layui-btn layui-btn-xs" onclick="searchPeople()">姓名模糊搜索</button>'+
                    '<table class="layui-hide" id="test" lay-filter="test"></table></div>' +
                    '<textarea id="zhuanbanyijian" rows="3" cols="100" placeholder="请填写转发原因" style="margin-left: 100px"/>',
                    area: ['1000px', '600px'],
                    offset: '10px',
                    success: function(layero, index){
                        getPeoples();
                    },
                    btn: ['确定', '取消'],
                    yes: function(index, layero){
                       if($("#zhuanbanrenid").val()=='' || typeof($("#zhuanbanrenid").val())=='undefined'){
                           banli_layer.msg("请选择转办人",{offset:'200px'});
                       }else  if($("#zhuanbanyijian").val()=='' || typeof($("#zhuanbanyijian").val())=='undefined'){
                           banli_layer.msg("请填写转办原因",{offset:'200px'});
                       }else  if($("#zhuanbanrenid").val()==userName){
                           banli_layer.msg("转办人不能选择自己",{offset:'200px'});
                       }
                       else {
                           var zhuanban_index=banli_layer.load(2,{offset:'200px'});
                           $.post("${ctx}/processInfo/zhuanbanTask",{
                               taskid:taskid,
                               zhuanbanName:$("#zhuanbanrenid").val(),
                               info:$("#zhuanbanyijian").val(),
                               userName:userName,
                               userRealName:userRealName,
                               reportName:reportName
                           },function (data) {
                               if(data.result==='success'){
                                   banli_layer.close(zhuanban_index);
                                   window.parent.FS.tabPane.closeActiveTab();
                                   alert('转办成功')
                               }
                               else {
                                   window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("转办错误："+data.msg);
                               }
                           });
                       }
                    }
                    ,btn2: function(index, layero){
                    }
                })

            });

            /*添加会签人*/
            $(document).on("click","${'[name=\'addHuiQians\']'}",function(){
                //alert($("#huiqians").val())   zhangsan,lisi
                var huiqian_index=banli_layer.open({
                    type: 1,
                    title:'请添加会签人',
                    content:'<div style="padding: 0px 10px;"><table class="layui-hide" id="test1" lay-filter="test"></table></div>',
                    area: ['1000px', '650px'],
                    offset: '10px',
                    success: function(layero, index){
                        getPeoples_huiqian();
                    },
                    btn: ['确定', '取消'],
                    yes: function(index, layero){
                        var huiqians='';
                        var flag=true;
                        var checkStatus = mytable.checkStatus('test1')
                            ,data = checkStatus.data;
                        for(var i=0;i<data.length;i++){
                            if(data[i].username!=userName){
                                huiqians+=data[i].username+','
                            }else{
                                flag=false;
                                break;
                            }
                        }
                      if(flag){
                          huiqians=huiqians.substr(0,huiqians.length-1);
                          console.log(huiqians)
                          if(huiqians=='' || typeof(huiqians)=='undefined'){
                              banli_layer.msg('请选择会签人',{offset:'200px'})
                          }else {
                               var index11=banli_layer.load(2,{offset:'200px'});
                               $.post("${ctx}/processInfo/addHuiQianAssgin",{
                                   huiqians:huiqians,
                                   taskid:taskid,
                                   userName:userName,
                                   userRealName:userRealName
                               },function (data) {
                                  banli_layer.close(index11);
                                  if(data.result==='success'){
                                      banli_layer.close(huiqian_index);
                                      banli_layer.msg('添加成功',{offset:'200px'})
                                  }else {
                                      window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("添加会签人错误"+data.msg);
                                  }
                              });
                          }
                      }else{
                          banli_layer.msg('选择的人中不能包含自己本身',{offset:'200px'})
                      }
                    }
                    ,btn2: function(index, layero){
                    }
                })


            });


        })
    });

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
    
    //获取按钮
    function  getBtn(tijiao,istuihui,tuihui,huiqian,zhuanban) {
        console.log(tijiao+","+istuihui+","+tuihui
            +","+huiqian+","+zhuanban)
        var ulHtml,tijiaoHtml,baocunHtml,tuihuiHtml,tianjiahuiqianHtml,zhuanbanHtml;
        var count=2;
        tijiaoHtml=" <li id=\"banliTaskTiJiao\" class=\"\" style=\"border: 1px solid #3385ff;background: #3385ff;cursor: pointer;\">\n" +
            " <a href=\"#\" style=\"color: #fff\" name=\"submitFormInfoBanLiTask\">"+tijiao +"</a></li>"
        if(istuihui=='true'){
            count++;
            var temp=tuihui=='' ? '退回' : tuihui;
            tuihuiHtml="<li name=\"backTask\" style=\"border: 1px solid #FF5722;background:#FF5722;cursor: pointer;\">\n" +
                "<a href=\"#\" style=\"color: #fff\">"+temp+"</a></li>"
        }else{
            tuihuiHtml="";
        }
        if(huiqian!=''){
            count++;
            tianjiahuiqianHtml=" <li name=\"addHuiQians\" style=\"border: 1px solid #1E9FFF;background:#1E9FFF;cursor: pointer;\">\n" +
                "<a href=\"#\" style=\"color: #fff\">"+huiqian+"</a></li>";
        }else{
            tianjiahuiqianHtml="";
        }
        if(zhuanban!=''){
            count++;
            zhuanbanHtml=" <li name=\"zhuanbanTask\" style=\"border: 1px solid #FF5722;background:#FF5722;cursor: pointer;\">\n" +
                "<a href=\"#\" style=\"color: #fff\">转办</a></li>";
        }else{
            zhuanbanHtml="";
        }

        ulHtml= ulHtml=" <ul class=\"my-btn clearfix\" style=\"width: 98%\">";
       /* if(count==2){
            ulHtml=" <ul class=\"my-btn clearfix\" style=\"width: 15%\">";
        }else if(count==3){
            ulHtml=" <ul class=\"my-btn clearfix\" style=\"width: 20%\">";
        }else if(count==4){
            ulHtml=" <ul class=\"my-btn clearfix\" style=\"width: 22%\">";
        }else if(count==5){
            ulHtml=" <ul class=\"my-btn clearfix\" style=\"width: 27%\">";
        }*/
        baocunHtml="<li name=\"baocunTask\" style=\"border: 1px solid #FFB800;background:#FFB800;cursor: pointer;\">\n" +
            "<a href=\"#\" style=\"color: #fff\">保存</a>";
        //console.log(ulHtml+tijiaoHtml+baocunHtml+tuihuiHtml+tianjiahuiqianHtml+zhuanbanHtml+"</ul>")
       // console.log(tuihuiHtml);
        /*return ulHtml+tijiaoHtml+baocunHtml+tuihuiHtml+tianjiahuiqianHtml+zhuanbanHtml+"</ul>";*/
        return ulHtml+zhuanbanHtml+tianjiahuiqianHtml+tuihuiHtml+baocunHtml+tijiaoHtml+"</ul>";

    }


    function selFileBanliTask () {
        document.getElementById("file").click();
    }
    function showFileBanliTask () {
        var str=$("#file").val();
        //var fileName=str.split("\\")[2];
        var fileName= str.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");
        $("#showFileName").val(fileName);
    }
var mydata=[];
function getPeoples() {
    $.ajax({
        type:"post",
        url:"${ctx}/processDiagram/getzuzhiJson",
        dataType:'json',
        headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
        success:function (data) {
            mydata=data.data;
            mytable.render({
                elem: '#test'
                ,data:data.data
                ,cols: [[
                    {type:'radio'}
                    ,{field:'username', width:100, title: '用户名'}
                    ,{field:'realName', width:100, title: '姓名'}
                    ,{field:'depPostNames', width:200, title: '部门-职务'}
                    ,{field:'roleNames',width:100,title: '角色'}
                    ,{field:'email', width:200, title: '邮箱'}
                    ,{field:'mobile', width:200, title: '手机'}
                ]]
                ,page: true
            });
        },
        error:function (xhr,text) {
            alert(text);
        }
    });
}

function searchPeople() {
    var name=$("#searchPeople").val();
    if(name==''){
        mytable.render({
            elem: '#test'
            ,data:mydata
            ,cols: [[
                {type:'radio'}
                ,{field:'username', width:100, title: '用户名'}
                ,{field:'realName', width:100, title: '姓名'}
                ,{field:'depPostNames', width:200, title: '部门-职务'}
                ,{field:'roleNames',width:100,title: '角色'}
                ,{field:'email', width:200, title: '邮箱'}
                ,{field:'mobile', width:200, title: '手机'}
            ]]
            ,page: true
        });
    }else{
        var result=[];
        for(var i=0;i<mydata.length;i++){
            if(mydata[i].realName.indexOf(name)>-1){
                result.push(mydata[i]);
                console.log(mydata[i].realName)
            }
        }
        mytable.render({
            elem: '#test'
            ,data:result
            ,cols: [[
                {type:'radio'}
                ,{field:'username', width:100, title: '用户名'}
                ,{field:'realName', width:100, title: '姓名'}
                ,{field:'depPostNames', width:200, title: '部门-职务'}
                ,{field:'roleNames',width:100,title: '角色'}
                ,{field:'email', width:200, title: '邮箱'}
                ,{field:'mobile', width:200, title: '手机'}
            ]]
            ,page: true
        });
    }

   /* mytable.reload('children', {
        url: '/children/selChildrenInfoByDoctor.action',
        where: {}
        ,page: {
            curr: 1
        }
    });*/

}

function getPeoples_huiqian() {
    $.ajax({
        type:"post",
        url:"${ctx}/processDiagram/getzuzhiJson",
        dataType:'json',
        headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
        success:function (data) {
            mytable.render({
                elem: '#test1'
                ,data:data.data
                ,cols: [[
                    {type:'checkbox'}
                    ,{field:'username', width:100, title: '用户名'}
                    ,{field:'realName', width:100, title: '姓名'}
                    ,{field:'depPostNames', width:200, title: '部门-职务'}
                    ,{field:'roleNames',width:100,title: '角色'}
                    ,{field:'email', width:200, title: '邮箱'}
                    ,{field:'mobile', width:200, title: '手机'}
                ]]
                ,page: true
            });
        },
        error:function (xhr,text) {
            alert(text);
        }
    });
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

    function showAssign(obj) {
        var mythis=$(obj);
      if(mythis.prev().parent("div").attr("name")=='1'){
          var index=banli_layer.load(2,{offset:'200px'});
          $.post("${ctx}/processInfo/getbackTaskNodeInfoPeople",{ProcessInstanceId:"${param.proInstanceId}",
              activityid:mythis.prev("input").val()},function (data) {
              banli_layer.close(index);
              if(data.msg=='success'){
                  $("#peopleTbody").empty();
                  tempdata=data;
                  for(var i=0;i<data.result.length;i++){
                    $("#peopleTbody").append( "<tr>" +
                        "<td style='width: 6%;text-align: center;'>" +
                             "<input style='width:15px;height: 15px;' type='checkbox' value="+data.result[i].userid+" />" +
                        "</td>" +
                        "<td style='width: 24%;text-align: center;'>"+data.result[i].user+"</td>" +
                        "<td style='width: 35%;text-align: center;'>"+data.result[i].dep+"</td>" +
                        "<td style='width: 35%;text-align: center;'>"+data.result[i].role+"</td>" +
                        "</tr>");
                  }
              }else{
                  window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("根据节点查询用户信息错误："+data.result);
              }
          });
      }else{
          $("#peopleTbody").empty();
      }
    }

    function getAssigns(obj) {
    var data="";
    $("#peopleTbody").find("input[type='checkbox']").each(function () {
        if($(this).is(":checked")){
            data+=$(this).val()+",";
        }
    });
    return data.length==0?"":data.substring(0,data.length-1);
   /* var data="";
        var div=$(obj);
        div.parent("div").find("input[type='checkbox']:checked").each(function () {
            var a=$(this).val();
            var vlue=a.substr(0,a.indexOf("("));
            data+=vlue+",";
        });
        if(data==''){
            return data;
        }else{
            return data.substring(0,data.length-1);
        }*/


    }

    function searchDepAndRole() {
        var dep=$("#selDep").val();
        var ro=$("#selRole").val();
        if(typeof(tempdata)!='undefined'){
            $("#peopleTbody").empty();
            for(var i=0;i<tempdata.result.length;i++){
                var d = tempdata.result[i].dep;
                var r = tempdata.result[i].role;
                if(d.indexOf(dep)>-1 && r.indexOf(ro)>-1){
                    $("#peopleTbody").append( "<tr>" +
                        "<td style='width: 6%;text-align: center;'>" +
                            "<input  style='width:15px;height: 15px;' type='checkbox' value="+tempdata.result[i].userid+" />" +
                        "</td>" +
                        "<td style='width: 24%;text-align: center;'>"+tempdata.result[i].user+"</td>" +
                        "<td style='width: 35%;text-align: center;'>"+tempdata.result[i].dep+"</td>" +
                        "<td style='width: 35%;text-align: center;'>"+tempdata.result[i].role+"</td>" +
                        "</tr>");
                }

            }
        }

    }

</script>
</html>
