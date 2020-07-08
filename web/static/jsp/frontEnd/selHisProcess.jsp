<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%
    String register=response.getHeader("register");
    String time=response.getHeader("time");
%>
<html>
<head>
    <title>历史流程信息</title>
    <link href="${ctx}/static/layui/css/layui.css" rel="stylesheet">
    <link href="${ctx}/static/css/selHisProcess.css" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${ctx}/static/css/myPagination.css"/>

 <%--   <style>
        img{
            padding-left: 38%;
        }
    </style>--%>
</head>
<body style="background-color:#fff ">
<div style="margin: 15px 20px;">
    <div id="topContent">
        <div class="tab">
            <div class="hd">
                <a href="#" class="active">已申请</a>
                <a href="#">已处理</a>
            </div>
            <div class="bd">
                <div class="bd-son" style="display: block;" id="yishenqing">
                    <%--已申请--%>
                     <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                          <tr>
                              <th>流程名</th>
                              <th>发起人</th>
                              <th>流程编号</th>
                              <th>发起时间</th>
                              <th>结束时间</th>
                              <th>流程状态</th>
                              <th>操作</th>
                          </tr>
                         <tbody id="yishenqingTbody">
                         </tbody>
                      </table>
                        <div  id="page_yishenqing" class="pagination" style="float: right;margin-top: 20px;"></div>
                        <div id="noDateDiv"></div>
                </div>
                <%--已处理--%>
                <div class="bd-son" id="yichuli">
                    <%--已处理--%>
                    <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                        <tr>
                            <th>流程名</th>
                            <th>发起人</th>
                            <th>流程编号</th>
                            <th>发起时间</th>
                            <th>结束时间</th>
                            <th>流程状态</th>
                            <th>操作</th>
                        </tr>
                        <tbody id="yichuliTbody">
                        </tbody>
                    </table>
                        <div  id="page_yichuli" class="pagination" style="float: right;margin-top: 20px;"></div>
                        <div id="noDateDivYiChuLi"></div>
                        <!--翻页-->
                        <%--<div  id="page_daiban" class="pagination" style="float: right;margin-top: 20px;"></div>--%>
                        <%-- <div style="width: 250px;height: 200px;margin: 20px auto;">
                             <img src="${ctx}/static/images/noDate.jpg" width="100%" height="100%">
                         </div>--%>
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

    </div>
</div>

<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
<script src="${ctx}/static/js/myPagination.js"></script>

<script>
    var his_layer;
    $(function(){
        var proProcessUrl="";var proInsatnceID="";var prodepid="";var proProcessUrlyiban="";var proInsatnceIDyiban="";
        var userName=parent.Dec.personal.username;
        var userRealName=parent.Dec.personal.displayName.split("(")[0];
        var propicInit=false;
        $('.hd a').click(function(){
            $('.hd a').eq($(this).index()).addClass('active').siblings().removeClass('active');
            if($(this).index()===0){
                $("#yishenqing").show();
                $("#yichuli").hide();
                inintyishenqing(1,true);
            }else{
                $("#yishenqing").hide();
                $("#yichuli").show();
                inintyichuli(1,true);
            }
        });

        layui.use(['laypage', 'layer','element','table'], function() {
              his_layer = layui.layer;
              var index=his_layer.load(2,{offset:['200px','46%']});

            inintyishenqing(1,true);

            //已申请详情
            $(document).on("click","${'[name=\'yiShengQingDetail\']'}",function(){
                var name=$(this).parents("tr").find("td").eq(0).text()+":"+ $(this).parents("tr").find("td").eq(2).text();
                var businessKey=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
                var reportName=$(this).parents("tr").find("input[type='hidden']").eq(1).val();
                var proInsID=$(this).parents("tr").find("input[type='hidden']").eq(2).val();
                var proDefinitionId=$(this).parents("tr").find("input[type='hidden']").eq(3).val();
                var activityid=$(this).parents("tr").find("input[type='hidden']").eq(4).val();
                window.parent.FS.tabPane.addItem({title:name,src:"${ctx}/static/jsp/frontEnd/hisProcessDetail.jsp?businessKey="+
                businessKey+"&reportName="+encodeURI(reportName)+"&proInsID="+proInsID+"&proDefinitionId="+proDefinitionId+"&activityid="+activityid});
            });
            //已申请撤回
            $(document).on("click","${'[name=\'yiShengQingCheHui\']'}",function(){
                var mythis=$(this);
                his_layer.confirm('是否撤回？', {
                    btn: ['确定', '取消'] //可以无限个按钮
                    ,offset:'200px'
                }, function(index, layero){
                    var proInstanceId=mythis.parents("tr").find("input[type='hidden']").eq(2).val();
                    var processDefinitionID = mythis.parents("tr").find("input[type='hidden']").eq(3).val();
                    var chehui_index=his_layer.load(3,{offset:'200px'});
                    $.post("${ctx}/processInfo/chehui",{processDefinitionID: processDefinitionID,proInstanceId:proInstanceId,
                        userName:userName,userRealName:userRealName},function (data) {
                        his_layer.close(chehui_index);
                        if(data.result==='success'){
                            mythis.parents("tr").find("td").eq(5).text("已撤回");
                            mythis.attr("name","yiShengQingDelete").css("color","#FF5722").text("删除");
                            his_layer.msg('撤回成功',{offset:'200px'});
                        }else if(data.result==='0'){
                            alert(data.msg);
                        }
                        else {
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("流程撤回异常："+data.msg);
                        }
                    })
                }, function(index){

                });
            });

            //已申请的删除
            $(document).on("click","${'[name=\'yiShengQingDelete\']'}",function(){
                var mythis=$(this);
                var proInstanceId=$(this).parents("tr").find("input[type='hidden']").eq(2).val();
                his_layer.confirm('是否删除？', {
                    btn: ['确定', '取消'] //可以无限个按钮
                    ,offset:'200px'
                }, function(index, layero){
                    var index=his_layer.load(3,{offset:'200px'});
                    $.post("${ctx}/processInfo/deleteSelfProcess",{proInstanceId:proInstanceId,
                        userName:userName,userRealName:userRealName},function (data) {
                        his_layer.close(index);
                        if(data.result==='success'){
                            mythis.parents("tr").find("td").eq(5).text("已删除");
                            mythis.remove();
                            his_layer.msg('删除成功',{offset:'200px'});
                        }
                        else {
                            window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("流程删除异常："+data.msg);
                        }
                    })
                },function(index){

                });

            });

            /*==========================================================================================*/

            //已处理详情
            $(document).on("click","${'[name=\'yiban_details\']'}",function(){
                var name=$(this).parents("tr").find("td").eq(0).text()+":"+ $(this).parents("tr").find("td").eq(2).text();
                var businessKey=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
                var reportName=$(this).parents("tr").find("input[type='hidden']").eq(1).val();
                var proInsID=$(this).parents("tr").find("td").eq(2).text();
                var proDefinitionId=$(this).parents("tr").find("input[type='hidden']").eq(2).val();
                var activityid=$(this).parents("tr").find("input[type='hidden']").eq(3).val();
                console.log(activityid)
                window.parent.FS.tabPane.addItem({title:name,src:"${ctx}/static/jsp/frontEnd/hisProcessDetail.jsp?businessKey="+
                businessKey+"&reportName="+encodeURI(reportName)+"&proInsID="+proInsID+"&proDefinitionId="+proDefinitionId+"&activityid="+activityid});
            });

    });

        function getState(state) {
            // 0：申请人提交可撤回状态  1：通过 2：查看 3：被退回（不包括申请节点） 4：撤回 5：转办 6:完成 7：删除 8：被退回到申请节点
            if(state=='1' || state=='0'){
                return "进行中";
            }else if(state=='2'){
                return "已被查看";
            }
            else if(state=='3' || state=='8'){
                return "被退回";
            }
            else if(state=='4'){
                return "已撤回";
            }
            else if(state=='5'){
                return "已转办";
            }
            else if(state=='6' || state=='9'){
                return "已完成";
            }else if(state=='7'){
                return "已删除";
            }else {
                return "其他";
            }
        }

        function  getBtnByProState(state) {
            // 0：申请人提交可撤回状态  1：通过 2：查看 3：被退回（不包括申请节点） 4：撤回 5：转办 6:完成 7：删除 8：被退回到申请节点
            var html="<span name=\"yiShengQingDetail\" class=\"myspan\" style=\"color: #FFB800\">详情</span>&nbsp;&nbsp;&nbsp;";
            if(state=='0'){
                return html+" <span name=\"yiShengQingCheHui\" class=\"myspan\" style=\"color: #FF5722\">撤回</span>&nbsp;&nbsp;&nbsp;";
            }else if(state=='2' || state=='1' || state=='5' || state=='6' ||state=='7' || state=='3' || state=='9' ){
                return html;
            }
            else if(state=='4' || state=='8'){
                return html+" <span name=\"yiShengQingDelete\" class=\"myspan\" style=\"color: #FF5722\">删除</span>&nbsp;&nbsp;&nbsp;";
            }

        }


        function inintyishenqing(num,flag) {
            var index=his_layer.load(2,{offset:['200px','46%']});
            $.ajax({
                type: "POST",
                data:{userName:userName,num:num},
                dataType: "json",
                url: "${ctx}/processInfo/selectHisPro",
                success: function (data) {
                    his_layer.close(index);
                    if(data.msg==='success'){
                        if(flag) {
                            new myPagination({
                                id: 'page_yishenqing',
                                curPage: 1, //初始页码
                                pageTotal: data.yeshu, //总页数
                                pageAmount: 10,  //每页多少条
                                dataTotal: data.total, //总共多少条数据
                                pageSize: 5, //可选,分页个数
                                showPageTotalFlag: true, //是否显示数据统计
                                //  showSkipInputFlag:true, //是否支持跳转
                                getPage: function (page) {
                                    //获取当前页数
                                    inintyishenqing(page, false);

                                }
                            });
                        }
                        linksList_yishenqing(data.result);
                    }else {
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取已申请流程列表错误："+data.result);
                    }
                },
                error: function (e, jqxhr, settings, exception) {
                    his_layer.close(index);
                    alert('服务器响应失败!!!')
                }

            });
        }


        function linksList_yishenqing(datas) {
            $("#yishenqingTbody").empty();
            $("#noDateDiv").empty();
            if(datas.length==0){
                $("#noDateDiv").append("<div style=\"width: 250px;height: 200px;margin: 20px 39%;\">\n" +
                    "<img src=\"${ctx}/static/images/noDate.jpg\" width=\"100%\" height=\"100%\">\n" +
                    "</div>");
                $("#page_yishenqing").hide();
            }else{
                $("#page_yishenqing").show();
                for(var i=0;i<datas.length;i++){
                    $("#yishenqingTbody").append("<tr>" +
                        "<td>"+datas[i].proname+"</td>"+
                        "<td>"+userRealName+"</td>"+
                        "<td>"+datas[i].proInsID+"</td>"+
                        "<td>"+datas[i].proStartTime+"</td>"+
                        "<td>"+datas[i].proEndTime+"</td>"+
                        "<td>"+getState(datas[i].prostate)+"</td>"+
                        "<td>"+getBtnByProState(datas[i].prostate)+"</td>"+
                        "<input type='hidden' value="+datas[i].businessKey+" />"+
                        "<input type='hidden' value="+datas[i].reportName+" />"+
                        "<input type='hidden' value="+datas[i].proInsID+" />"+
                        "<input type='hidden' value="+datas[i].proDefinitionId+" />"+
                        "<input type='hidden' value="+datas[i].activityid+" />"+
                    "<tr>");
                }
            }

        }




        function inintyichuli(num,flag) {
            var index=his_layer.load(2,{offset:['200px','46%']});
           $.ajax({
                type: "POST",
                data:{userName:userName,num:num},
                dataType: "json",
                url: "${ctx}/processInfo/selectHisProYiChuLi",
                success: function (data) {
                    his_layer.close(index);
                    if(data.msg==='success'){
                        if(flag) {
                            new myPagination({
                                id: 'page_yichuli',
                                curPage: 1, //初始页码
                                pageTotal: data.yeshu, //总页数
                                pageAmount: 10,  //每页多少条
                                dataTotal: data.total, //总共多少条数据
                                pageSize: 5, //可选,分页个数
                                showPageTotalFlag: true, //是否显示数据统计
                                //  showSkipInputFlag:true, //是否支持跳转
                                getPage: function (page) {
                                    //获取当前页数
                                    inintyichuli(page,false);

                                }
                            });
                        }
                        linksList_yichuli(data.result);
                    }else {
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取已处理流程列表错误："+data.result);
                    }
                },
                error: function (e, jqxhr, settings, exception) {
                    his_layer.close(index);
                    alert('服务器响应失败!!!')
                }

            });
        }



        function linksList_yichuli(datas) {
            $("#yichuliTbody").empty();
            $("#noDateDivYiChuLi").empty();
            if(datas.length==0){
                $("#noDateDivYiChuLi").append("<div style=\"width: 250px;height: 200px;margin: 20px 39%;\">\n" +
                    "<img src=\"${ctx}/static/images/noDate.jpg\" width=\"100%\" height=\"100%\">\n" +
                    "</div>");
                $("#page_yichuli").hide();
            }else{
                $("#page_yichuli").show();
                for(var i=0;i<datas.length;i++){
                    $("#yichuliTbody").append("<tr>" +
                        "<td>"+datas[i].proname+"</td>"+
                        "<td style='color: blue;'>"+datas[i].startPeople+"</td>"+
                        "<td>"+datas[i].proInstanceId+"</td>"+
                        "<td>"+datas[i].proStartTime+"</td>"+
                        "<td>"+datas[i].proEndTime+"</td>"+
                        "<td>"+getState(datas[i].proStatus)+"</td>"+
                        "<td>"+"<span name='yiban_details' style='color: #FFB800' class='myspan'>详情</span>"+"</td>"+
                        "<input type='hidden' value="+datas[i].businessKey+" />"+
                        "<input type='hidden' value="+datas[i].proFormKey+" />"+
                        "<input type='hidden' value="+datas[i].proDefineID+" />"+
                        "<input type='hidden' value="+datas[i].activityid+" >"+
                        "<tr>");
                }
            }
        }



    })
</script>
</body>
</html>
