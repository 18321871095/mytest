<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%
    String register=response.getHeader("register");
    String time=response.getHeader("time");
%>
<html>
<head>
    <link rel="stylesheet" href="${ctx}/static/layui/css/layui.css">
    <link rel="stylesheet" href="${ctx}/static/css/proDeployInfo.css">
    <link rel="stylesheet" type="text/css" href="${ctx}/static/css/myPagination.css"/>
    <link rel="stylesheet" href="${ctx}/static/css/ySelect.css"/>
</head>
<body style="background-color:#fff ">

<div class="tab">
    <div class="hd">
        <a href="#" class="active">模型列表</a>
        <a href="#">部署列表</a>
        <a href="#">流程分类管理</a>
        <a href="#">定时任务</a>
    </div>
    <div class="bd">
        <div id="moxing"  class="bd-son" style="display: block;">
            <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                <tr>
                    <th>模型编号</th>
                    <th>模型名称</th>
                    <th>创建时间</th>
                    <th>上次更新时间</th>
                    <th>模型描述</th>
                    <th>创建人</th>
                    <th>操作</th>
                </tr>
                <tbody id="modelTbody">
                </tbody>
            </table>
            <div  id="page_moxing" class="pagination" style="float: right;margin-top: 20px;"></div>
            <div id="noDateDiv_moxing"></div>
        </div>
        <div id="bushu" class="bd-son">
            <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                <tr>
                    <th>部署编号</th>
                    <th>流程名称</th>
                  <%--  <th>流程版本号</th>--%>
                    <th>流程类别</th>
                    <th>部署时间</th>
                    <th>创建人</th>
                    <th>操作</th>
                </tr>
                <tbody id="deployTbody">
                </tbody>
            </table>
            <div  id="page_deploy" class="pagination" style="float: right;margin-top: 20px;"></div>
            <div id="noDateDiv_deploy"></div>
        </div>
        <div id="fenlei" class="bd-son">
            <br/><button id="addRow" class="layui-btn">新增</button>
            <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                <tr>
                    <th style='text-align: center'>分类编号</th>
                    <th style='text-align: center'>分类名称</th>
                    <th style='text-align: center'>新建人</th>
                    <th style='text-align: center'>操作</th>
                </tr>
                <tbody id="fenleiTbody">
                </tbody>
            </table>
        </div>
        <div id="dingshi" class="bd-son">
            <br/><button id="addRow1" class="layui-btn">新增定时任务</button>
            <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                <tr>
                    <th style='text-align: center'>流程名称</th>
                    <th style='text-align: center'>设置频率（cron表达式）</th>
                    <th style='text-align: center'>下一次运行时间</th>
                    <th style='text-align: center'>状态</th>
                    <th style='text-align: center'>操作</th>
                </tr>
                <tbody id="dingshiTbody">
                </tbody>
            </table>
        </div>
    </div>
</div>

<div style="width: 120px;height: 50px;border: 1px solid #00a0e9;display: <%=register.equals("false")?"block":"none" %>;
position: fixed;right: 10px;bottom: 10px;background: #00a0e9;color: #fff;font-size: 15px;text-align: center;">
    <div onclick="this.parentNode.style.display='none'" style="width: 20px;height: 20px; line-height: 20px; background: #999; box-sizing: border-box;border-radius: 50%; text-align: center;
position: absolute; left: -10px; top: -10px; color: #fff;">
        <a href="#" style="color: #fff; display: inline-block;">×</a>
    </div>
  试用阶段，剩余<%=time %>天！
</div>
<div>



</body>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/js/ySelect.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
<script src="${ctx}/static/js/myPagination.js"></script>
<script>

    $('.hd a').click(function(){
        $('.hd a').eq($(this).index()).addClass('active').siblings().removeClass('active');
        if($(this).index()===0){
            $("#moxing").show();
            $("#bushu").hide();
            $("#fenlei").hide();
            $("#dingshi").hide();
            initMoXing(1,true);
        }else if($(this).index()===1){
            $("#moxing").hide();
            $("#bushu").show();
            $("#fenlei").hide();
            $("#dingshi").hide();
            initDeploy(1,true);
        }else if($(this).index()===2){
            $("#moxing").hide();
            $("#bushu").hide();
            $("#fenlei").show();
            $("#dingshi").hide();
            initClassifly();
        }else{
            $("#moxing").hide();
            $("#bushu").hide();
            $("#fenlei").hide();
            $("#dingshi").show();
            initDingShi();
        }
    });
</script>
<script>
    var index_a;
    var deploylayer;
    var userName=parent.Dec.personal.username;
    var daoruquanxianIndex;
    var userRealName=parent.Dec.personal.displayName.split("(")[0];
    $(document).ready(function () {
        layui.use('layer', function() {
            deploylayer  = layui.layer;
            initMoXing(1,true);

            $("#addRow").click(function () {
                $("#fenleiTbody").append("<tr>" +
                    "<td style='text-align: center'>"+uuid().replace(/-/g,"")+"</td>" +
                    "<td style='text-align: center'>" +
                    "<input style='width: 100%;height: 30px;border: none;text-align: center;' /></td>" +
                    "<td style='text-align: center'>"+userName+"("+userRealName+")"+"</td>" +
                    "<td style='text-align: center'>" +
                    "<button name='bancunClassify' class='mybtn' style='color: #009688'>保存</button> &nbsp;|&nbsp;"+
                    "<button name='shanchuClassify' class='mybtn' style='color: #FF5722'>删除</button>" +
                    "</tr>");
            });
            var index_addrow;
            $("#addRow1").click(function () {
                index_addrow= deploylayer.open({
                    type: 1,
                    title:'新增任务',
                    content: '<div style="text-align: center;margin-top: 20px;"><select id="depList" style="width: 200px;height: 32px;">\n' +
                    '</select></div>' +
                    /* '   <div style="text-align: center;margin-top: 20px;"> <input id="cron" style="width: 200px;height: 32px;" placeholder="cron表达式"/>\n' +
                     '</div><div style="text-align: center;margin-top: 50px;"><button class="layui-btn" name="addDingShi">确定</button>' ,*/
                    '<div style="margin-bottom: 20px;margin-top: 20px;margin-left: 136px">\n' +
                    '    执行时间&nbsp;&nbsp;&nbsp;<select style="width: 70px" id="h"></select>&nbsp;&nbsp;\n' +
                    '    时&nbsp;&nbsp;<select id="m" style="width: 70px"></select>&nbsp;&nbsp;分\n' +
                    '</div>\n' +
                    '<div style="margin-left: 148px">\n' +
                    '   <div style="display: inline-block;">\n' +
                    '       执行日&nbsp;&nbsp;&nbsp;\n' +
                    '       <select style="width: 70px" id="d" onchange="myshow(this)">\n' +
                    '           <option>每日</option>\n' +
                    '           <option>每周</option>\n' +
                    '           <option>每月</option>\n' +
                    '       </select>\n' +
                    '   </div>\n' +
                    '    <div style="display: inline-block;">\n' +
                    '        <div id="m2div" style="display: none;">\n' +
                    '            <select id=\'m2\' class="demo1"  multiple="multiple">\n' +
                    '                <option>周一</option>\n' +
                    '                <option>周二</option>\n' +
                    '                <option>周三</option>\n' +
                    '                <option>周四</option>\n' +
                    '                <option>周五</option>\n' +
                    '                <option>周六</option>\n' +
                    '                <option>周日</option>\n' +
                    '            </select>\n' +
                    '        </div>\n' +
                    '        <div id="m3div" style="display: none;">\n' +
                    '            <select id=\'m3\' class="demo1" multiple="multiple" >\n' +
                    '            </select>\n' +
                    '        </div>\n' +
                    '    </div>\n' +
                    '</div>\n' +
                    '<div style="margin-top: 20px;margin-left: 143px;">\n' +
                    '    执行月&nbsp;&nbsp;&nbsp;  <select id=\'m4\' class="demo1" multiple="multiple" >\n' +
                    '</select>\n' +
                    '</div>\n' +
                    '<div style="margin-top: 20px;margin-left: 203px;">\n' +
                    '    <input style="height: 27px;" id="cron"  />\n' +
                    '   <button onclick="shengcheng(this)" class="layui-btn layui-btn-sm">生成cron表达式</button>\n' +
                    '</div>\n'+
                    '<div style="text-align: center;margin-top: 30px;"><button style="width: 100px;font-size: 17px;" class="layui-btn layui-btn-sm" name="addDingShi">确定</button></div>'
                    ,
                    offset:'20px',
                    area: ['600px', '500px'],
                    success: function(layero, index){
                       $.post("${ctx}/processDiagram/selectDeployment1",{},function (data) {
                           $("#depList").empty();
                           $("#depList").append("<option value=\"\">请选择一个流程</option>");
                           for(var i=0;i<data.result.length;i++){
                               $("#depList").append("<option value="+data.result[i].id+">"+data.result[i].name+"</option>");
                           }
                       });
                        for(var k=1;k<=31;k++){
                            $("#m3").append("<option>"+k+"日</option>");
                        }
                        for(var mm=1;mm<=12;mm++){
                            $("#m4").append("<option>"+mm+"月</option>");
                        }
                        for(var i=0;i<=23;i++){
                            $("#h").append("<option>"+i+"</option>");
                        }
                        for(var j=0;j<=59;j++){
                            $("#m").append("<option>"+j+"</option>");
                        }
                        $('#m2').ySelect(
                            {
                                placeholder: '请选择',
                                showSearch: false,
                                numDisplayed: 7,
                                overflowText: '已选中 {n}项',
                                isCheck:false
                            }
                        );
                        $('#m3').ySelect(
                            {
                                placeholder: '请选择',
                                showSearch: false,
                                numDisplayed: 31,
                                overflowText: '已选中 {n}项',
                                isCheck:false
                            }
                        );
                        $('#m4').ySelect(
                            {
                                placeholder: '请选择',
                                showSearch: false,
                                numDisplayed: 12,
                                overflowText: '已选中 {n}项',
                                isCheck:false
                            }
                        );
                    }
                });
            });

            var index_xiugai;
            /*修改定时流程*/
            $(document).on("click","${'[name=\'bianjidingshi\']'}",function(){
                var cron=$(this).parents("tr").find("td").eq(1).text();
                var mythis=$(this);
                var id=mythis.parents("tr").find("input[type='hidden']").eq(0).val();
                $.post("${ctx}/processDiagram/checkXiuGai",{id:id},function (data) {
                    if(data.msg=='success'){
                        index_xiugai= deploylayer.open({
                            type: 1,
                            title:'修改',
                            content:
                            '<input id="hiddenId" type="hidden" /><div style="margin-bottom: 20px;margin-top: 20px;margin-left: 136px">\n' +
                            '    执行时间&nbsp;&nbsp;&nbsp;<select style="width: 70px" id="h"></select>&nbsp;&nbsp;\n' +
                            '    时&nbsp;&nbsp;<select id="m" style="width: 70px"></select>&nbsp;&nbsp;分\n' +
                            '</div>\n' +
                            '<div style="margin-left: 148px">\n' +
                            '   <div style="display: inline-block;">\n' +
                            '       执行日&nbsp;&nbsp;&nbsp;\n' +
                            '       <select style="width: 70px" id="d" onchange="myshow(this)">\n' +
                            '           <option>每日</option>\n' +
                            '           <option>每周</option>\n' +
                            '           <option>每月</option>\n' +
                            '       </select>\n' +
                            '   </div>\n' +
                            '    <div style="display: inline-block;">\n' +
                            '        <div id="m2div" style="display: none;">\n' +
                            '            <select id=\'m2\' class="demo1"  multiple="multiple">\n' +
                            '                <option>周一</option>\n' +
                            '                <option>周二</option>\n' +
                            '                <option>周三</option>\n' +
                            '                <option>周四</option>\n' +
                            '                <option>周五</option>\n' +
                            '                <option>周六</option>\n' +
                            '                <option>周日</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '        <div id="m3div" style="display: none;">\n' +
                            '            <select id=\'m3\' class="demo1" multiple="multiple" >\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div style="margin-top: 20px;margin-left: 143px;">\n' +
                            '    执行月&nbsp;&nbsp;&nbsp;  <select id=\'m4\' class="demo1" multiple="multiple" >\n' +
                            '</select>\n' +
                            '</div>\n' +
                            '<div style="margin-top: 20px;margin-left: 203px;">\n' +
                            '    <input style="height: 27px;" id="xiugaicron"  />\n' +
                            '   <button onclick="shengcheng1(this)" class="layui-btn layui-btn-sm">生成cron表达式</button>\n' +
                            '</div>\n'+
                            '<div style="text-align: center;margin-top: 30px;">' +
                            '<button style="width: 100px;font-size: 17px;" class="layui-btn layui-btn-sm" name="xiugaiCron">修改</button></div>'
                            ,
                            offset:'20px',
                            area: ['600px', '500px'],
                            success:function () {
                                $("#hiddenId").val(id);
                                for(var k=1;k<=31;k++){
                                    $("#m3").append("<option>"+k+"日</option>");
                                }
                                for(var mm=1;mm<=12;mm++){
                                    $("#m4").append("<option>"+mm+"月</option>");
                                }
                                for(var i=0;i<=23;i++){
                                    $("#h").append("<option>"+i+"</option>");
                                }
                                for(var j=0;j<=59;j++){
                                    $("#m").append("<option>"+j+"</option>");
                                }
                                $('#m2').ySelect(
                                    {
                                        placeholder: '请选择',
                                        showSearch: false,
                                        numDisplayed: 7,
                                        overflowText: '已选中 {n}项',
                                        isCheck:false
                                    }
                                );
                                $('#m3').ySelect(
                                    {
                                        placeholder: '请选择',
                                        showSearch: false,
                                        numDisplayed: 31,
                                        overflowText: '已选中 {n}项',
                                        isCheck:false
                                    }
                                );
                                $('#m4').ySelect(
                                    {
                                        placeholder: '请选择',
                                        showSearch: false,
                                        numDisplayed: 12,
                                        overflowText: '已选中 {n}项',
                                        isCheck:false
                                    }
                                );
                            }

                        });

                    }else if(data.msg=='001'){
                        deploylayer.alert("请先停止任务，然后在修改",{offset:'200px'});
                    }
                    else{
                        deploylayer.alert("错误信息："+data.result,{offset:'200px'});
                    }

                });




            });

            $(document).on("click","${'[name=\'xiugaiCron\']'}",function(){
                var cronNew=$("#xiugaicron").val();
                var mythis=$(this);
                var id= $("#hiddenId").val();
                $.post("${ctx}/processDiagram/xiugaiCron",{id:id,cron:cronNew},function (data) {
                    if(data.msg=='success'){
                        deploylayer.msg("成功",{offset:'200px'});
                        mythis.parents("tr").find("td").eq(1).text(cronNew);
                        deploylayer.close(index_xiugai);
                        initDingShi();
                    }
                    else{
                        deploylayer.alert("错误信息："+data.result,{offset:'200px'});
                    }
                });
            });

            $(document).on("click","${'[name=\'addDingShi\']'}",function(){
                var id=$("#depList").val();
                var cron=$("#cron").val();
                if(id!='' && cron!=''){
                    $.post("${ctx}/processDiagram/addDingShi",{id:id,cron:cron},function (data) {
                        if(data.msg=='success'){
                            deploylayer.close(index_addrow);
                            initDingShi();
                        }
                        else {
                            deploylayer.alert("添加失败，"+data.result,{offset:'200px'});
                        }
                    });
                }else{
                    deploylayer.msg("请选择流程或填写cron表达式",{offset:'200px'});
                }

            });

            $(document).on("click","${'[name=\'shanchuClassify\']'}",function(){
                var mythis=$(this);
               var index1 = deploylayer.confirm('是否删除该分类', {
                    btn: ['是','否'],
                    offset:'100px'//按钮
                }, function(){
                   deploylayer.close(index1);
                   var ii=deploylayer.load(1,{offset:'200px'});
                    $.post("${ctx}/processDiagram/shanchuClassify",{id:mythis.parents("tr").find("td").eq(0).text()},
                        function (data) {
                            deploylayer.close(ii);
                        if(data=='1'){
                            deploylayer.msg("成功",{offset:'200px'});
                            mythis.parents("tr").remove();
                        }else{
                            deploylayer.alert("该分类已经绑定了流程模型或流程部署，请解除绑定或更换再删除",{offset:'200px'});
                        }

                    });
                });
            });

            $(document).on("click","${'[name=\'bancunClassify\']'}",function(){
                var mythis=$(this);
                var name=$(this).parents("tr").find("td").eq(1).find("input").eq(0).val().replace(/\s/g,"");
              if(name!='' && typeof(name)!='undefined'){
                  $.post("${ctx}/processDiagram/bancunClassify",{id:$(this).parents("tr").find("td").eq(0).text(),
                      name:name,userName:userName},function (data) {
                     if(data=='1'){
                         mythis.text("修改").css("color","#FFB800").attr("name","xiugaiClassify");
                         deploylayer.msg("成功",{offset:'200px'});
                     }else{
                         mythis.parents("tr").find("td").eq(1).find("input").eq(0).val("");
                         deploylayer.alert("系统中该名称重复了",{offset:'200px'});
                     }
                  });
              }else{
                  deploylayer.msg("名称不能为空值",{offset:'200px'});
              }
            });
            var xiugaiClassifyIndex;
            $(document).on("click","${'[name=\'xiugaiClassify\']'}",function(){
                var name=$(this).parents("tr").find("td").eq(1).find("input").eq(0).val().replace(/\s/g,"");
                var id=$(this).parents("tr").find("td").eq(0).text();
                xiugaiClassifyIndex=deploylayer.open({
                    type: 1,
                    title:'分类名称',
                    area: ['300px', '220px'],
                    offset:'200px',
                    content: '<div style="text-align: center;margin: 20px;">原值：<input id="oldName" readonly /></div>' +
                    '<div style="text-align: center;margin: 20px;">新值：<input id="newName" /><input type="hidden" id="classifyid" /></div>' +
                    '<div style="text-align: center;margin: 20px;"><button name="xiugaiClassifyQueDing" class="layui-btn">确定</button></div>' //这里content是一个普通的String
                    ,success:function () {
                        $("#oldName").val(name);
                        $("#classifyid").val(id);
                    }
                });
            });

            $(document).on("click","${'[name=\'xiugaiClassifyQueDing\']'}",function(){
                var oldname= $("#oldName").val().replace(/\s/g,"");
                var newname= $("#newName").val().replace(/\s/g,"");
                if(newname==''){
                    deploylayer.msg("新值为空！！！",{offset:'200px'});
                }else{
                    $.post("${ctx}/processDiagram/xiugaiClassify",{id: $("#classifyid").val(),
                        name:newname},function (data) {
                        if(data=='1'){
                            deploylayer.msg("成功",{offset:'200px'});
                            deploylayer.close(xiugaiClassifyIndex);
                            initClassifly();
                        }else{
                            deploylayer.alert("系统中该名称重复了",{offset:'200px'});
                        }
                    });
                }

            });

            $(document).on("click","${'[name=\'deploy\']'}",function(){
                var mythis=$(this);
                var modelId=$(this).parents("tr").find("td").eq(0).text();
                var index_deploy = deploylayer.load(3,{offset:'200px'});
                $.post("${ctx}/processDiagram/deploy",{modelId:modelId,userName:userName},function(data){
                    layer.close(index_deploy);
                    if(data.msg==1){
                        mythis.text("已部署").attr("disabled","disabled");
                        mythis.parent("td").find("button[name='xiugai']").text('查看').attr("name","chakan");
                        mythis.parent("td").append( "&nbsp;|&nbsp;<button name='updateModel'  style=\"color: #FFB800;\" class=\"mybtn\">解除发布</button>");
                        deploylayer.msg("部署成功",{offset:'200px'});
                    }else if(data.msg==2){
                        deploylayer.alert("该流程您没有权限部署",{offset:'200px'});
                    }
                    else {
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("部署流程图错误："+data.result);
                    }
                });
            });

            $(document).on("click","${'[name=\'xiugai\']'}",function(){
                var modelid=$(this).parents("tr").find("td").eq(0).text();//
                var myid=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
                window.location.href="${ctx}/modeler.html?modelId="+modelid+"&modelerStatus=true&param="+myid;
               // window.parent.FS.tabPane.addItem({title:"流程图修改"+modelid, src:"${ctx}/modeler.html?modelId="+modelid});
            });

            $(document).on("click","${'[name=\'chakan\']'}",function(){
                var modelid=$(this).parents("tr").find("td").eq(0).text();//"&modelerStatus=false"
                window.parent.FS.tabPane.addItem({title:"流程图查看"+modelid, src:
                        "${ctx}/modeler.html?modelId="+modelid+"&modelerStatus=false"});
            });

            $(document).on("click","${'[name=\'shanchu\']'}",function(){
                var modelId=$(this).parents("tr").find("td").eq(0).text();
                var mythis=$(this);
                deploylayer.confirm('是否删除该模型，请谨慎操作！！！', {
                    btn: ['是','否'],
                    offset:'100px'//按钮
                }, function(){
                    var shanchuIndex=deploylayer.load(3,{offset:'200px'});
                    $.post("${ctx}/processDiagram/deleteMolder",{modelId:modelId,userName:userName},function(data){
                        deploylayer.close(shanchuIndex);
                        if(data.msg=='1')
                        {
                            mythis.parents("tr").remove();
                            deploylayer.msg("删除成功");
                        }else if(data.msg=='2'){
                            deploylayer.msg("必须先删除部署才能再删除模型",{offset:'200px'});
                        }else if(data.msg=='3'){
                            deploylayer.msg("该模型您没有权限删除",{offset:'200px'});
                        }
                        else {
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("删除模型错误："+data.result);
                        }
                    });
                });

            });

            $(document).on("click","${'[name=\'shanchuDeploy\']'}",function(){
                var deployId=$(this).parents("tr").find("td").eq(0).text();
                var mythis=$(this);
                deploylayer.confirm('是否删除该部署，请谨慎操作！！！', {
                    btn: ['是','否'], //按钮
                    offset:'100px'
                }, function(){
                    var shanchuDeploy=deploylayer.load(3,{offset:'200px'});
                    $.post("${ctx}/processDiagram/deleteDeploy",{deployId:deployId,userName:userName},function(data){
                        deploylayer.close(shanchuDeploy);
                        if(data.msg==1)
                        {
                            mythis.parents("tr").remove();
                            deploylayer.msg("删除成功",{ offset:'100px'});
                        }else if(data.msg==2){
                            deploylayer.msg("您不是管理员无法执行删除操作",{offset:'200px'});
                        }
                        else if(data.msg==3){
                            deploylayer.alert("请先删除定时任务中所关联的该流程",{offset:'200px'});
                        }
                        else {
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("删除部署错误："+data.result);
                        }
                    });
                });

            });

            /*设置权限*/
            var quedingSelDepAndRoleIndex;var myprodefinedid="";
            $(document).on("click","${'[name=\'quanxian\']'}",function(){
                var mythis=$(this);
                myprodefinedid=mythis.parents("tr").find("input").eq(0).val();
                quedingSelDepAndRoleIndex= deploylayer.open({
                    type: 1,
                  title:'类别',
                      area: ['300px', '200px'],
                      offset:'20px',
                    content: '<div style="text-align: center;margin-top: 20px;margin-bottom: 20px;"><select id="selDepAndRole" style="height: 30px">\n' +
                    '    <option value="1">部门-岗位-人员</option>\n' +
                    '    <option value="2">角色-人员</option>\n' +
                    '</select></div>'+'<div style="text-align: center;">' +
                    '<button class="layui-btn" name="quedingSelDepAndRole">确定</button></div>'
                });
                /*deploylayer.open({
                    title:'<span style="font-size: 25px">流程名称：'+mythis.parents("tr").find("td").eq(1).text()+'</span>',
                    type: 2,
                    area: ['1000px', '90%'],
                    offset:'20px',
                    content: '${ctx}/static/jsp/util/authorityManagement.jsp?prodefinedid='+mythis.parents("tr").find("input").eq(0).val()
                });*/
            });

            $(document).on("click","${'[name=\'quedingSelDepAndRole\']'}",function(){
                deploylayer.close(quedingSelDepAndRoleIndex);
               var aa= $("#selDepAndRole").val();
               if(aa=='1'){
                  index_a= deploylayer.open({
                       title:'<span style="font-size: 25px">设置权限</span>',
                       type: 2,
                       area: ['1000px', '90%'],
                       offset:'20px',
                       content: '${ctx}/static/jsp/util/authorityManagement.jsp?prodefinedid='+myprodefinedid
                   });
                  console.log(index_a)
               }else if(aa=='2'){
                   index_a=   deploylayer.open({
                       title:'<span style="font-size: 25px">设置权限</span>',
                       type: 2,
                       area: ['1000px', '90%'],
                       offset:'20px',
                       content: '${ctx}/static/jsp/util/authorityManagementRole.jsp?prodefinedid='+myprodefinedid
                   });
               }
            });


            $(document).on("click","${'[name=\'baocunquanxian\']'}",function(){
                var procdefid=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
                deploylayer.open({
                    title:'权限保存名称',
                    type: 1,
                    area:["300px","150px"],
                    offset:"100px",
                    content: '<div style="text-align: center;margin: 10px 0;"><input id="quanxianname" ' +
                    ' placeholder="请输入名称"/></div>' +
                    '<div style="text-align: center;">' +
                    '<button id='+procdefid+' style="width: 100px;cursor: pointer;" name="quedingquanxian">确定</button></div>'
                });
            });

            $(document).on("click","${'[name=\'quedingquanxian\']'}",function(){
                var mythis=$(this);
                if($("#quanxianname").val()==''){
                    alert('名称不能为空')
                }else{
                    var baocunquanxianindex=deploylayer.load(1,{offset:'200px'});
                    $.post("${ctx}/processInfo/baocunAuthority",{
                        procdefid:mythis.attr("id"),name:$("#quanxianname").val()
                    },function (data) {
                        deploylayer.close(baocunquanxianindex);
                        if(data.msg=='success'){
                            deploylayer.closeAll();
                            deploylayer.msg("保存成功",{offset:'200px'});
                        }else if(data.msg=='001'){
                            deploylayer.msg("名称重复",{offset:'200px'});
                        }
                        else {
                            window.location.href="${ctx}/static/jsp/error.jsp?message="+encodeURI(data.result);
                        }
                    });
                }
            });

            $(document).on("click","${'[name=\'daoruquanxian\']'}",function(){
                var procdefid=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
                daoruquanxianIndex= deploylayer.open({
                    title:'权限导入(点击名称显示权限信息)',
                    type: 2,
                    offset:'10px',
                    area:["800px","500px"],
                    content: '${ctx}/static/jsp/util/reserveAuthority.jsp?procdefid='+encodeURI(procdefid)
                });
            });

            $(document).on("click","${'[name=\'updateModel\']'}",function(){
                var index=deploylayer.load(2,{offset:'46%'});
                var modelid=$(this).parents("tr").find("td").eq(0).text();
                var mythis=$(this);
                $.post("${ctx}/processDiagram/updateModel",{modelid:modelid,userName:userName},function (data) {
                    deploylayer.close(index);
                    if(data==='success'){
                        deploylayer.msg("成功",{offset:'46%'});
                        mythis.text("发布").attr("name","fabu");
                        mythis.parent("td").find("button[name='chakan']").text("修改").attr("name","xiugai");
                    }else if(data==='001'){
                        deploylayer.alert("没有权限",{offset:'46%'});
                    }else{
                        deploylayer.msg("失败",{offset:'46%'});
                    }
                    
                });
            });

            $(document).on("click","${'[name=\'fabu\']'}",function(){
                var index=deploylayer.load(2,{offset:'46%'});
                var mythis=$(this);
                var modelid=$(this).parents("tr").find("td").eq(0).text();
                $.post("${ctx}/processDiagram/fabu",{modelid:modelid,userName:userName},function (data) {
                    deploylayer.close(index);
                    if(data.msg==='success'){
                        deploylayer.msg("发布成功");
                        mythis.text("解除发布").attr("name","updateModel");
                        mythis.parent("td").find("button[name='xiugai']").text("查看").attr("name","chakan");
                    }else if(data.msg==='0'){
                        deploylayer.msg("没有权限",{offset:'200px'});
                    }
                    else{
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("发布失败："+data.result);
                    }

                });
            });


            /*启动定时流程*/
            $(document).on("click","${'[name=\'start\']'}",function(){
                var index=deploylayer.load(2,{offset:'46%'});
                var mythis=$(this);
                var id=mythis.parents("tr").find("input[type='hidden']").eq(0).val();
                var myproname=mythis.parents("tr").find("td").eq(0).text();
                $.post("${ctx}/processDiagram/startDingShi",{id:id,proname:myproname},function (data) {
                    deploylayer.close(index);
                    if(data.msg!='fail'){
                        deploylayer.msg("启动成功",{offset:'200px'});
                        mythis.text("停止").attr("name","stop");
                        mythis.parents("tr").find("td").eq(2).text(data.msg);
                        mythis.parents("tr").find("td").eq(3).text("运行中");
                    }else{
                        deploylayer.alert("启动失败,错误信息："+data.result,{offset:'200px'});
                    }

                })
            });

            /*停止定时流程*/
            $(document).on("click","${'[name=\'stop\']'}",function(){
                var index=deploylayer.load(2,{offset:'46%'});
                var mythis=$(this);
                var id=mythis.parents("tr").find("input[type='hidden']").eq(0).val();
                $.post("${ctx}/processDiagram/stopDingShi",{id:id},function (data) {
                    deploylayer.close(index);
                    if(data.msg=='success'){
                        deploylayer.msg("停止成功",{offset:'200px'});
                        mythis.text("启动").attr("name","start");
                        mythis.parents("tr").find("td").eq(2).text("");
                        mythis.parents("tr").find("td").eq(3).text("停止中");
                    }else{
                        deploylayer.alert("停止失败,错误信息："+data.result,{offset:'200px'});
                    }

                })
            });



            /*删除定时流程*/
            $(document).on("click","${'[name=\'shanchudingshi\']'}",function(){
                var index=deploylayer.load(2,{offset:'46%'});
                var mythis=$(this);
                var id=mythis.parents("tr").find("input[type='hidden']").eq(0).val();
                $.post("${ctx}/processDiagram/deleteDingShi",{id:id},function (data) {
                    deploylayer.close(index);
                    if(data.msg=='success'){
                        deploylayer.msg("成功",{offset:'200px'});
                        mythis.parents("tr").remove();
                    }else if(data.msg=='001'){
                        deploylayer.alert("请先停止，在删除",{offset:'200px'});
                    }
                    else{
                        deploylayer.alert("删除失败,错误信息："+data.result,{offset:'200px'});
                    }

                })
            });





        });


    });

    function myshow(obj) {
        var mythis=$(obj);
        var value=mythis.val();
        if(value=='每周'){
            $("#m2div").show();
            $("#m3div").hide();
        }else if(value=='每月'){
            $("#m2div").hide();
            $("#m3div").show();
        }else if(value=='每日'){
            $("#m2div").hide();
            $("#m3div").hide();
        }

    }

    function shengcheng(obj) {
        var temp={};
        var flag=true;
        var m=$("#m").val();
        var h=$("#h").val();
        var cron="";
        temp["m"]=m;temp["h"]=h;
        var d=$("#d").val();
        if(d=='每日'){
            temp["d"]="*";
        }else if(d=='每周'){
            var myw= $("#m2").ySelectedValues(",");
            if(myw==''){
                flag=false;
                alert('请选择周几');
            }else{
                temp["w"]=getW(myw);
            }
        }else if(d=='每月'){
            var mym= $("#m3").ySelectedValues(",");
            if(mym==''){
                flag=false;
                alert('请选择多少日')
            }else{
                temp["d"]=getD(mym);
            }
        }
        var moth=$("#m4").ySelectedValues(",");

        if(moth==''){
            flag=false;
            alert('请选择月份');
        }else{
            temp["M"]=getM(moth);
        }

        if(flag){
            cron="0"+" "+temp["m"]+" "+temp["h"];//showSequenceFlowError
            if(typeof(temp["w"])=='undefined'){
                cron+=" "+temp["d"];
            }else{
                cron+=" "+"?";
            }
            cron+=" "+temp["M"];
            if(typeof(temp["w"])=='undefined'){
                cron+=" "+"?";
            }else{
                cron+=" "+temp["w"];
            }
            $("#cron").val(cron)
        }else{
            $("#cron").val("")
        }


    }

    function shengcheng1(obj) {
        var temp={};
        var flag=true;
        var m=$("#m").val();
        var h=$("#h").val();
        var cron="";
        temp["m"]=m;temp["h"]=h;
        var d=$("#d").val();
        if(d=='每日'){
            temp["d"]="*";
        }else if(d=='每周'){
            var myw= $("#m2").ySelectedValues(",");
            if(myw==''){
                flag=false;
                alert('请选择周几');
            }else{
                temp["w"]=getW(myw);
            }
        }else if(d=='每月'){
            var mym= $("#m3").ySelectedValues(",");
            if(mym==''){
                flag=false;
                alert('请选择多少日')
            }else{
                temp["d"]=getD(mym);
            }
        }
        var moth=$("#m4").ySelectedValues(",");

        if(moth==''){
            flag=false;
            alert('请选择月份');
        }else{
            temp["M"]=getM(moth);
        }

        if(flag){
            cron="0"+" "+temp["m"]+" "+temp["h"];//showSequenceFlowError
            if(typeof(temp["w"])=='undefined'){
                cron+=" "+temp["d"];
            }else{
                cron+=" "+"?";
            }
            cron+=" "+temp["M"];
            if(typeof(temp["w"])=='undefined'){
                cron+=" "+"?";
            }else{
                cron+=" "+temp["w"];
            }
            $("#xiugaicron").val(cron)
        }else{
            $("#xiugaicron").val("")
        }


    }

    function getW(text) {
        var value="";
        if(text.indexOf("周一")>-1){
            value+=2+",";
        }
        if(text.indexOf("周二")>-1){
            value+=3+",";
        }
        if(text.indexOf("周三")>-1){
            value+=4+",";
        }
        if(text.indexOf("周四")>-1){
            value+=5+",";
        }
        if(text.indexOf("周五")>-1){
            value+=6+",";
        }
        if(text.indexOf("周六")>-1){
            value+=7+",";
        }
        if(text.indexOf("周日")>-1){
            value+=1+",";
        }
        return value.substring(0,value.length-1);
    }

    function getD(text) {
        var value="";
        var data=text.split(",");
        for(var ii=1;ii<=31;ii++){
            for(var j=0;j<data.length;j++){
                if(data[j]==(ii+'日')){
                    value+=ii+",";
                }
            }
        }
        return value.substring(0,value.length-1);
    }

    function getM(text) {
        var value="";
        var data=text.split(",");
        for(var ii=1;ii<=31;ii++){
            for(var j=0;j<data.length;j++){
                if(data[j]==(ii+'月')){
                    value+=ii+",";
                }
            }
        }
        return value.substring(0,value.length-1);
    }





    function initMoXing(num,flag) {
        var index=deploylayer.load(2,{offset:'46%'});
        //初始化模型列表
        $.ajax({
            type: "POST",
            data:{num:num,userName:userName},
            dataType: "json",
            async:false,
            url: "${ctx}/processDiagram/selectMolder",
            success: function (data) {
                deploylayer.close(index);
                        if(data.msg==='success') {
                            if(flag) {
                                new myPagination({
                                    id: 'page_moxing',
                                    curPage: 1, //初始页码
                                    pageTotal: data.yeshu, //总页数
                                    pageAmount: 10,  //每页多少条
                                    dataTotal: data.total, //总共多少条数据
                                    pageSize: 5, //可选,分页个数
                                    showPageTotalFlag: true, //是否显示数据统计
                                    getPage: function (page) {
                                        //获取当前页数
                                        initMoXing(page, false);
                                    }
                                });
                        }
                    list_linkMoXing(data);
                } else {
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取模型列表错误："+data.result);
                }
            },
            error: function (e, jqxhr, settings, exception) {
                deploylayer.close(index);
                alert('服务器响应失败!!!')
            }
        });
    }


    function list_linkMoXing(datas) {
        $("#modelTbody").empty();
        $("#noDateDiv_moxing").empty();
        if(datas.result.length==0){
            $("#noDateDiv_moxing").append("<div style=\"width: 250px;height: 200px;margin: 20px 42%;\">\n" +
                "<img src=\"${ctx}/static/images/noDate.jpg\" width=\"100%\" height=\"100%\">\n" +
                "</div>");
            $("#page_moxing").hide();
        }else {
            for (var i = 0; i < datas.result.length; i++) {
             //   var metaInfo = datas.result[i].metaInfo;
               // var description = JSON.parse(metaInfo);
                var authority=datas.result[i].authority;
                var btn = "";
                var btn1 = "";
              /*  if(authority=='1'){*/
                    if (datas.result[i].status == '1') {
                        btn = "<button disabled='disabled' name=\"deploy\" style=\"color: #1E9FFF;\" class=\"mybtn\">已部署</button> &nbsp;|&nbsp;";
                        btn1 = " <button name='chakan' style=\"color: #FFB800;\" class=\"mybtn\">查看</button> &nbsp;|&nbsp;"+
                            " <button name='shanchu' style=\"color: #FF5722;\" class=\"mybtn\">删除</button>&nbsp;|&nbsp;"+
                            " <button name='updateModel'  style=\"color: #FFB800;\" class=\"mybtn\">解除发布</button>";
                    }else if(datas.result[i].status == '2'){
                        btn = "<button disabled='disabled' name=\"deploy\" style=\"color: #1E9FFF;\" class=\"mybtn\">已部署</button> &nbsp;|&nbsp;";
                        btn1 = " <button name='xiugai' style=\"color: #FFB800;\" class=\"mybtn\">修改</button> &nbsp;|&nbsp;"+
                            " <button name='shanchu' style=\"color: #FF5722;\" class=\"mybtn\">删除</button>&nbsp;|&nbsp;"+
                            " <button name='fabu'  style=\"color: #FFB800;\" class=\"mybtn\">发布</button>";
                    }
                    else {
                        btn = " <button name=\"deploy\" style=\"color: #1E9FFF;\" class=\"mybtn\">部署</button> &nbsp;|&nbsp;";
                        btn1 = "<button name='xiugai' style=\"color: #FFB800;\" class=\"mybtn\">修改</button> &nbsp;|&nbsp;"+
                            " <button name='shanchu' style=\"color: #FF5722;\" class=\"mybtn\">删除</button>";
                    }
              /*  }else{
                    btn1 = " <button name='chakan' style=\"color: #FFB800;\" class=\"mybtn\">查看</button>";
                }*/

                $("#modelTbody").append("<tr>" +
                    "<td>" + datas.result[i].molderId + "</td>" +
                    "<td>" + datas.result[i].molderName + "</td>" +
                    "<td>" + datas.result[i].createTime + "</td>" +
                    "<td>" + datas.result[i].lastUpdataTime + "</td>" +
                    "<td>" + datas.result[i].metaInfo + "</td>" +
                    "<td>" + datas.result[i].createPeople + "</td>" +
                    "<td>" + btn + btn1 +"</td>" +
                    "<input type='hidden' value="+datas.result[i].classfifyNameid+" />" +
                    "</tr>");
            }
        }
    }



    function initDeploy(num,flag) {
        var index=deploylayer.load(2,{offset:'46%'});
        //初始化部署列表
        $.ajax({
            type: "POST",
            data:{num:num,userName:userName},
            dataType: "json",
            url: "${ctx}/processDiagram/selectDeployment",
            success: function (data) {
                deploylayer.close(index);
                if(data.msg==='success') {
                    if(flag) {
                        new myPagination({
                            id: 'page_deploy',
                            curPage: 1, //初始页码
                            pageTotal: data.yeshu, //总页数
                            pageAmount: 10,  //每页多少条
                            dataTotal: data.total, //总共多少条数据
                            pageSize: 5, //可选,分页个数
                            showPageTotalFlag: true, //是否显示数据统计
                            getPage: function (page) {
                                //获取当前页数
                                initDeploy(page,false);
                            }
                        });
                    }
                    list_linkdeploy(data);
                } else {
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取部署列表错误："+data.result);
                }
            },
            error: function (e, jqxhr, settings, exception) {
                deploylayer.close(index);
                alert('服务器响应失败!!!')
            }
        });

    }


    function list_linkdeploy(datas) {
        $("#deployTbody").empty();
        $("#noDateDiv_deploy").empty();
        if(datas.result.length==0){
            $("#noDateDiv_deploy").append("<div style=\"width: 250px;height: 200px;margin: 20px 42%;\">\n" +
                "<img src=\"${ctx}/static/images/noDate.jpg\" width=\"100%\" height=\"100%\">\n" +
                "</div>");
            $("#page_deploy").hide();
        }else {
            for (var i = 0; i < datas.result.length; i++) {
                var authority=datas.result[i].authority;
                var btn="";
                if(authority=='1'){
                    btn= "<button name='quanxian' style='color: #009688' class='mybtn'>设置权限</buttton> &nbsp;|&nbsp;"+
                        "<button name='daoruquanxian' style='color: #1E9FFF' class='mybtn'>导入权限</buttton> &nbsp;|&nbsp;"+
                        "<button name='shanchuDeploy' style='color: #FF5722' class='mybtn'>删除</buttton>&nbsp;|&nbsp; "+
                       "<button name='baocunquanxian' style='color: #FFB800' class='mybtn'>保存权限</button>";

                }else{
                    btn="";
                }
                $("#deployTbody").append("<tr>" +
                    "<td>" + datas.result[i].DeploymentId + "</td>" +
                    "<td>" + datas.result[i].DeploymentName + "</td>" +
                  /*  "<td>" + datas.result[i].version + "</td>" +*/
                    "<td>" + datas.result[i].DeploymentProclassify + "</td>"+
                    "<td>"+ datas.result[i].DeploymentTime+"</td>" +
                    "<td>"+ datas.result[i].createPeople+"</td>" +
                    "<td>" + btn + "</td>"+
                    "<input type='hidden' value="+datas.result[i].processDefinitionId+" />" +
                    "</tr>");
            }
        }
    }


    function initClassifly() {
        var index=deploylayer.load(2,{offset:'46%'});
        //初始化模型列表
        $.ajax({
            type: "POST",
            dataType: "json",
            data:{userName:userName},
            url: "${ctx}/processDiagram/getClassify",
            success: function (data) {
                deploylayer.close(index);
                $("#fenleiTbody").empty();
                for (var i = 0; i < data.length; i++) {
                    $("#fenleiTbody").append("<tr>" +
                        "<td style='text-align: center'>"+data[i].id+"</td>"+
                        "<td  style='text-align: center'>"+
                            "<input style='width: 100%;height: 100%;border: none;text-align: center;' value="+data[i].classifyname+"  />"+"</td>"+
                        "<td style='text-align: center'>"+data[i].tenantidname+"</td>"+
                            "<td  style='text-align: center'>" +
                            "<button name='xiugaiClassify' class='mybtn' style='color: #FFB800'>修改</button> &nbsp;|&nbsp;"+
                            "<button name='shanchuClassify' class='mybtn' style='color: #FF5722'>删除</button>" +
                        "</td>"+"<input type='hidden' value="+data[i].tenantid+" />"+
                        "</tr>"
                    );
                }
            },
            error: function (e, jqxhr, settings, exception) {
                deploylayer.close(index);
                alert('服务器响应失败!!!')
            }
        });
    }

    function initDingShi() {
        var index=deploylayer.load(2,{offset:'46%'});
        //初始化定时列表
        $.ajax({
            type: "POST",
            dataType: "json",
            data:{userName:userName},
            url: "${ctx}/processDiagram/getDingshi",
            success: function (data) {
                deploylayer.close(index);
                if(data.msg=='success'){
                    $("#dingshiTbody").empty();
                    var data=data.result;
                    for (var i = 0; i < data.length; i++) {
                        var btn="";var status="";
                        if(data[i].state=='1'){
                            btn= "<button name='stop' class='mybtn' style='color: #FFB800'>停止</button> &nbsp;|&nbsp;";
                            status="运行中";
                        }else{
                            btn= "<button  name='start' class='mybtn' style='color: #FFB800'>启动</button> &nbsp;|&nbsp;";
                            status="停止中";
                        }
                        $("#dingshiTbody").append("<tr>" +
                            "<td style='text-align: center'>"+data[i].name+"</td>"+
                            "<td  style='text-align: center'>"+data[i].cron+"</td>"+
                            "<td  style='text-align: center'>"+data[i].nextTime+"</td>"+
                            "<td  style='text-align: center'>"+status+"</td>"+
                            "<td  style='text-align: center'>" +btn+
                            "<button name='bianjidingshi' class='mybtn' style='color: #FF5722'>编辑</button>&nbsp;|&nbsp;" +
                            "<button name='shanchudingshi' class='mybtn' style='color: #FF5722'>删除</button>" +
                            "</td>"+"<input type='hidden' value="+data[i].id+"  />"+
                            "</tr>"
                        );
                    }
                }else{
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取定时列表错误："+data.result);
                }

            },
            error: function (e, jqxhr, settings, exception) {
                deploylayer.close(index);
                alert('服务器响应失败!!!')
            }
        });
    }

    function uuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }

</script>
</html>
