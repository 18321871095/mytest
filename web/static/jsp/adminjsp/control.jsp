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
    <link rel="stylesheet" href="${ctx}/static/css/control.css">
    <link rel="stylesheet" type="text/css" href="${ctx}/static/css/myPagination.css"/>
    <style type="text/css">
        .laydate_table {
            display: none;
        }
        #laydate_hms{
            display: none !important;
        }
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
<body style="background-color:#fff ">



<div style="padding: 10px 10px;" >
    <div  class="layui-form" style="width: 200px;display: inline-block;">
        <input   type="text" id="people"  placeholder="模糊查询姓名" class="layui-input">
    </div>
    <div  class="layui-form" style="width: 200px;display: inline-block;">
        <select  id="dep">
            <option value="">请选择部门</option>
        </select>
    </div>
    <div  class="layui-form" style="width: 200px;display: inline-block;">
        <input  type="text" id="time"  placeholder="发起时间" class="layui-input">
    </div>
    <div  class="layui-form" style="width: 200px;display: inline-block;">
        <select  id="status">
            <option value="">请选择完成状态</option>
            <option value="1">完成</option>
            <option value="0">未完成</option>
        </select>
    </div>

    <div class="layui-form" style="width: 200px;display: inline-block;">
        <button class="layui-btn" onclick="selControl()" >查询</button>
    </div>
    <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
        <tr>
            <th style="width: 15%">流程名称</th>
            <th style="width: 20%">发起人</th>
            <th style="width: 25%">部门</th>
            <th style="width: 13%">发起时间</th>
            <th style="width: 13%">结束时间</th>
            <th style="width: 7%">状态</th>
            <th style="width: 7%">操作</th>
        </tr>
        <tbody id="controlTbody">
            <%--<tr>
                <td>申请流程</td>
                <td>孙红</td>
                <td>开发部</td>
                <td>2012-12-12 12:12:12</td>
                <td>2012-12-12 12:12:12</td>
                <td>结束</td>
                <td>
                    <button name="controlDetail" style="color: #FFB800;" class="mybtn">详情</button> &nbsp;|&nbsp;
                    <button name="controlDelete" style="color: #FF5722;" class="mybtn">删除</button>
                </td>
            </tr>--%>
        </tbody>
    </table>
    <div  id="page_control" class="pagination" style="float: right;margin-top: 20px;"></div>
    <div id="noDateDivControl"></div>

</div>

<div style="width: 120px;height: 50px;border: 1px solid #00a0e9;display: <%=register.equals("false")?"block":"none" %>;
        position: fixed;right: 10px;bottom: 10px;background: #00a0e9;color: #fff;font-size: 15px;text-align: center;">
    <div onclick="this.parentNode.style.display='none'" style="width: 20px;height: 20px; line-height: 20px; background: #999; box-sizing: border-box;border-radius: 50%; text-align: center;
position: absolute; left: -10px; top: -10px; color: #fff;">
        <a href="#" style="color: #fff; display: inline-block;">×</a>
    </div>
    试用阶段，剩余<%=time %>天！
</div>

</body>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
<script src="${ctx}/static/js/myPagination.js"></script>
<script>
    var control_layer;
    var userName=parent.Dec.personal.username;
    var userRealName=parent.Dec.personal.displayName.split("(")[0];
    $(document).ready(function () {
        layui.use(['layer','form','laydate'], function() {
            control_layer = layui.layer;
            control_form = layui.form;
            control_laydate = layui.laydate;
            control_laydate.render({
                elem: '#time' //指定元素
                ,type: 'month'
            });
            initControl(1,true);

            $(document).on("click","${'[name=\'controlDetail\']'}",function(){
                var proname=$(this).parents("tr").find("td").eq(0).text();
                var requestid=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
                var reportName=$(this).parents("tr").find("input[type='hidden']").eq(1).val();
                var processDefinitionID=$(this).parents("tr").find("input[type='hidden']").eq(2).val();
                var processInstanceId=$(this).parents("tr").find("input[type='hidden']").eq(3).val();
               /* alert(requestid+","+reportName+","+deployid)*/
                window.parent.FS.tabPane.addItem({title:proname,src:"${ctx}/static/jsp/frontEnd/hisProcessDetail.jsp?businessKey="+
                requestid+"&reportName="+encodeURI(reportName)+"&proInsID="+processInstanceId+"&proDefinitionId="+processDefinitionID});
            });

            $(document).on("click","${'[name=\'controlDelete\']'}",function(){
                var mythis=$(this);
                var processInstanceId=$(this).parents("tr").find("input[type='hidden']").eq(3).val();
                control_layer.confirm('是否删除？', {
                    btn: ['确定', '取消'] //可以无限个按钮
                    ,offset:'200px'
                }, function(index, layero){
                    var index1=control_layer.load(2,{offset:'200px'});
                    $.post("${ctx}/processInfo/deleteSelfProcessByAdmin",{
                        proInstanceId:processInstanceId,
                        userName:userName,
                        userRealName:userRealName
                    },function (data) {console.log(JSON.stringify(data))
                        control_layer.closeAll();
                        if(data.msg==='success'){
                            mythis.parents("tr").find("td").eq(4).text(getCurrentTime());
                            mythis.parents("tr").find("td").eq(5).text("已删除");
                            mythis.remove();
                        }else if(data.msg==='1'){
                            control_layer.msg(data.result,{offset:'200px'});
                        }
                        else {
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("管理员流程删除错误："+data.result);
                        }
                    })
                },function(index){

                });
            });

            //初始化部门
            $.ajax({
                type:"get",//http://localhost:8080/webroot/decision/v10/departments/old-platform-department-31
                //{"data":[{"id":"1491023f-8744-4c08-96f2-b3c5c4099567","pId":"old-platform-department-31","text":"人力资源子部门","pText":"","isParent":false,"open":false,"privilegeDetailBeanList":null}]}
                url:"${ctx}/decision/v10/departments/decision-dep-root",
                dataType:'json',
                headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
                success:function (data) {
                    $("#dep").empty();
                    $("#dep").append(" <option value=\"\">请选择部门</option>");
                    for(var i=0;i<data.data.length;i++) {
                        $("#dep").append(" <option value="+data.data[i].text+">"+data.data[i].text+"</option>");
                        /*var temp="";
                        if(!data.data[i].isParent){
                            temp="<div onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 15px;'>"+data.data[i].text+"</div>";
                        }else {
                            temp="<div name='add'  style='border: 1px solid #000;cursor: pointer;display: inline-block;width: 10px;height: 10px;background-image: url(\"${ctx}/static/images/add.png\");background-size: cover'></div>"
                                +"<div onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='margin-left: 5px;display: inline-block'>"+data.data[i].text+"</div>";
                        }
                        $("#department").append("<li >"+temp+"</li>");*/
                    }
                    control_form.render(); //更新全部
                },
                error:function (xhr,text) {
                    alert(text);
                }
            });

        });


    });

    function initControl(num,flag) {
        var index=control_layer.load(2,{offset:'200px'});
        $.ajax({
            type: "POST",
            data:{num:num,userName:userName},
            dataType: "json",
            url: "${ctx}/processInfo/getcontrol",
            success: function (data) {
                control_layer.close(index);
                if(data.msg==='success'){
                    if(flag){
                        new myPagination({
                            id: 'page_control',
                            curPage: 1, //初始页码
                            pageTotal: data.yeshu, //总页数
                            pageAmount: 10,  //每页多少条
                            dataTotal: data.total, //总共多少条数据
                            pageSize: 5, //可选,分页个数
                            showPageTotalFlag: true, //是否显示数据统计
                            getPage: function (page) {
                                //获取当前页数
                                initControl(page,false);
                            }
                        });
                    }
                    link_control(data.result);

                }
                else{
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("管理员获取流程列表错误："+data.result);
                }
            },
            error: function (e, jqxhr, settings, exception) {
                control_layer.close(index);
                alert('服务器响应失败!!!')
            }
        });
    }

    function selControl() {
        var name=$("#people").val();
        var depName=$("#dep").val();
        var time=$("#time").val();
        var year=time.split("-")[0];
        var month=parseInt(time.split("-")[1]);
        var status=$("#status").val();
       // alert(name+","+depName+","+time.split("-")[0]+","+parseInt(time.split("-")[1]));
        initSelControl(1,true,name,depName,time,status);
    }

    function initSelControl(num,flag,name,depName,time,status) {
        var index=control_layer.load(2,{offset:'200px'});
        $.ajax({
            type: "POST",
            data:{num:num,userName:userName,name:name,depName:depName,time:time,status:status},
            dataType: "json",
            url: "${ctx}/processInfo/getcontrol1",
            success: function (data) {
                control_layer.close(index);
                if(data.msg==='success'){
                    if(flag){
                        new myPagination({
                            id: 'page_control',
                            curPage: 1, //初始页码
                            pageTotal: data.yeshu, //总页数
                            pageAmount: 10,  //每页多少条
                            dataTotal: data.total, //总共多少条数据
                            pageSize: 5, //可选,分页个数
                            showPageTotalFlag: true, //是否显示数据统计
                            getPage: function (page) {
                                //获取当前页数
                                initSelControl(page,false,name,depName,time,status);
                            }
                        });
                    }
                    link_control(data.result);

                }
                else{
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("管理员获取流程列表错误："+data.result);
                }
            },
            error: function (e, jqxhr, settings, exception) {
                control_layer.close(index);
                alert('服务器响应失败!!!')
            }
        });
    }
    
    function link_control(datas) {
        $("#controlTbody").empty();
        $("#noDateDivControl").empty();
        if(datas.length==0){
            $("#noDateDivControl").append("<div style=\"width: 250px;height: 200px;margin: 20px 42%;\">\n" +
                "<img src=\"${ctx}/static/images/noDate.jpg\" width=\"100%\" height=\"100%\">\n" +
                "</div>");
            $("#page_control").hide();
        }else{
            $("#page_control").show();
            for(var i=0;i<datas.length;i++){
                $("#controlTbody").append("<tr>" +
                    "<td>"+datas[i].proname+"</td>"+
                    "<td>"+datas[i].startRealName+"</td>"+
                    "<td>"+datas[i].dep+"</td>"+
                    "<td>"+datas[i].startTime+"</td>"+
                    "<td>"+datas[i].endTime+"</td>"+
                    "<td>"+getState(datas[i].status)+"</td>"+
                    "<td>" +getBtnByState(datas[i].status)+"</td>"+
                    "<input type='hidden' value="+datas[i].requestid+" />"+
                    "<input type='hidden' value="+datas[i].reportName+" />"+
                    "<input type='hidden' value="+datas[i].processDefinitionID+" />"+
                    "<input type='hidden' value="+datas[i].processInstanceId+" />"+
                    "<tr>");
            }
        }

    }

    function getState(state) {
        if(state=='0' || state=='1' || state=='2' || state=='5'){
            return "进行中";
        }else if(state=='3' || state=='8'){
            return "已退回";
        }else if(state=='4'){
            return "已撤回";
        }else if(state=='7'){
            return "已删除";
        }else if(state=='6' || state=='9'){
            return "已完成";
        }else{
            return "";
        }
    }

    function getBtnByState(state) {
        var html= "<button name=\"controlDetail\" style=\"color: #FFB800;\" class=\"mybtn\">详情</button>";
        if(state=='6' || state=='7'){
            return html;
        }else{
            return html+" &nbsp;&nbsp;<button name=\"controlDelete\" style=\"color: #FF5722;\" class=\"mybtn\">删除</button>";
        }
    }

    function getCurrentTime() {
        var mydate=new Date();
        return mydate.getFullYear()+"-"+(mydate.getMonth()+1)+"-"+mydate.getDay()+" "+mydate.getHours()+":"+mydate.getMinutes()+":"+mydate.getSeconds();

    }

</script>
</html>
