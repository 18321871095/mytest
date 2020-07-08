<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%
    String register=response.getHeader("register");
    String time=response.getHeader("time");
%>
<html>
<head>
    <link href="${ctx}/static/layui/css/layui.css" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${ctx}/static/css/myPagination.css"/>
    <link href="${ctx}/static/css/daibanTask.css" rel="stylesheet">
</head>
<body style="background-color:#fff ">
<div style="margin: 15px 20px;">
    <div id="topContent">
        <div class="tab">
            <div class="hd">
                <a href="#" class="active">待办任务列表</a>
                <a href="#">待办保存列表</a>
            </div>
            <div class="bd">
                <div class="bd-son" style="display: block;" id="daiban">
                    <div  class="layui-form" style="width: 200px;display: inline-block;">
                        <input   type="text" id="proName"  placeholder="模糊查询了流程名" class="layui-input">
                    </div>
                    <div  class="layui-form" style="width: 200px;display: inline-block;">
                        <input   type="text" id="startPeople"  placeholder="模糊查询发起人" class="layui-input">
                    </div>
                    <div  class="layui-form" style="width: 200px;display: inline-block;">
                        <input  type="text" id="time1"  placeholder="发起时间" class="layui-input">
                    </div>
                    <div class="layui-form" style="width: 200px;display: inline-block;">
                        <button class="layui-btn" onclick="selDaiBanTask()" >查询</button>
                    </div>
                    <%--代办--%>
                  <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                        <tr>
                            <th>流程名</th>
                            <th>发起人</th>
                            <th>任务编号</th>
                            <th>节点名称</th>
                            <th>发起时间</th>
                            <th>操作</th>
                        </tr>
                      <tbody id="daibanTbody">
                      </tbody>
                    </table>
                        <div  id="page_daiban" class="pagination" style="float: right;margin-top: 20px;"></div>
                        <div id="noDateDiv_daiban"></div>
                </div>
                <%--保存--%>
                <div id="baocun" class="bd-son">
                    <%--保存--%>
                    <table border="0" class="tab-table" cellspacing="0" cellpadding="0">
                        <tr>
                            <th>流程名称</th>
                            <th>操作人</th>
                            <th>节点名称</th>
                            <th>保存时间</th>
                            <th>操作</th>
                        </tr>
                        <tbody id="baocunTbody">
                        </tbody>
                    </table>
                    <!--翻页-->
                        <div  id="page_baocun" class="pagination" style="float: right;margin-top: 20px;"></div>
                        <div id="noDateDiv_baocun"></div>
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
<script src="${ctx}/static/js/myPagination.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
<script>
    var daiban_layer;
    var userName=parent.Dec.personal.username;
    var userRealName=parent.Dec.personal.displayName.split("(")[0];
    var mybanlibtn;
$(function () {
    $('.hd a').click(function(){
        $('.hd a').eq($(this).index()).addClass('active').siblings().removeClass('active');

        if($(this).index()===0){
            $("#daiban").show();
            $("#baocun").hide();
            daiban(1,true);
        }else{
            $("#daiban").hide();
            $("#baocun").show();
            baocun(1,true);
        }
    });
    layui.use(['laypage', 'layer','element','table','laydate'], function() {

         daiban_layer = layui.layer;
       var  daiban_laydate = layui.laydate;
        daiban_laydate.render({
            elem: '#time1' //指定元素
        });

         daiban(1,true);

        $(document).on("click","${'[name=\'banliTask\']'}",function(){
            var taskid=$(this).parents("tr").find("td").eq(2).text();
            var proname=$(this).parents("tr").find("td").eq(0).text();
            var proDefinedId=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
            var proInstanceId=$(this).parents("tr").find("input[type='hidden']").eq(1).val();
            window.parent.FS.tabPane.addItem({title:proname+":"+taskid,src:
            "${ctx}/static/jsp/frontEnd/banliTask.jsp?taskid="+taskid+"&proname="+encodeURI(proname)+
            "&proDefinedId="+proDefinedId+"&proInstanceId="+proInstanceId});
        })

          $(document).on("click","${'[name=\'baocun_application\']'}",function(){
            var requestid=$(this).parents("tr").find("input[type='hidden']").eq(0).val();
            var index=daiban_layer.load(1,{offset:'200px'});
            var mythis=$(this);
            $.post("${ctx}/processInfo/checkBaoCun",{requestid:requestid},function (data) {
                daiban_layer.close(index);
                if(data.msg=='success'){
                    var reportName=mythis.parents("tr").find("input[type='hidden']").eq(1).val();
                    var deployid=mythis.parents("tr").find("input[type='hidden']").eq(2).val();
                    var baocunProname=mythis.parents("tr").find("input[type='hidden']").eq(3).val();
                    var proDefineId=mythis.parents("tr").find("input[type='hidden']").eq(4).val();
                    var comment=mythis.parents("tr").find("input[type='hidden']").eq(5).val();
                    var attachmentTemp=mythis.parents("tr").find("input[type='hidden']").eq(6).val();
                    var iswritecomment=mythis.parents("tr").find("input[type='hidden']").eq(7).val();
                    var tijiaoName=mythis.parents("tr").find("input[type='hidden']").eq(8).val();
                    // 9\5\文档.txt
                    if(attachmentTemp!=''){
                        //var attachment= attachmentTemp.split("\\");
                        window.parent.FS.tabPane.addItem({title:baocunProname,src:"${ctx}/static/jsp/frontEnd/application.jsp?requestid="+requestid+"&reportName="+
                        encodeURI(reportName)+"&deployid="+deployid+"&baocunProname=" +encodeURI(baocunProname)+"&proDefineId="+proDefineId+"&state=1"+"&comment="+encodeURI(comment)
                        +"&tijiaoName="+encodeURI(tijiaoName)+"&iswritecomment="+iswritecomment+"&attachment="+encodeURI(attachmentTemp)+"&param0="+"&param1="});
                    }else{
                        window.parent.FS.tabPane.addItem({title:baocunProname,src:"${ctx}/static/jsp/frontEnd/application.jsp?requestid="+requestid+"&reportName="+
                        encodeURI(reportName)+"&deployid="+deployid+"&baocunProname=" +encodeURI(baocunProname)+"&proDefineId="+proDefineId+"&state=1"+"&comment="+encodeURI(comment)
                        +"&tijiaoName="+encodeURI(tijiaoName) +"&iswritecomment="+iswritecomment+"&attachment=&param0=&param1="});
                    }
                }else{
                    daiban_layer.alert("该保存记录可能您已经办理了，请刷新待办保存列表");
                }
            });


        });


        $(document).on("click","${'[name=\'baocun_shanchu\']'}",function() {
            var mythis=$(this);
            layer.confirm('是否删除?', {
                btn: ['是','否'], //按钮
                offset:'200px'
            }, function(){
                var shanchubaocun=daiban_layer.load(2,{offset:'200px'});
                $.post("${ctx}/processInfo/removeBaoCun",{id:mythis.parents("tr").find("input").eq(0).val()},function(data){
                    daiban_layer.close(shanchubaocun);
                    if(data.result==='success')
                    {
                        //mythis.parents("tr").remove();
                        mythis.parent("td").empty().text("已删除");
                        daiban_layer.msg("成功");
                    }else {
                        daiban_layer.msg("删除失败");
                    }
                });
            });

        } );

        });

    //console.log($('iframe', window.parent.document))
    var myiframe= $('iframe', window.parent.document);
    for(var i=0;i<myiframe.length;i++){
        if(myiframe[i].src.indexOf("daibanTask.jsp")>-1){
            myiframe[i].id='daibanTask_iframe';
        }
    }

    });


        function daiban(num,flag) {
            var index=daiban_layer.load(2,{offset:['200px','46%']});
            $.ajax({
                type: "POST",
                data:{userName:userName,num:num},
                dataType: "json",
                url: "${ctx}/processInfo/selectTask",
                success: function (data) {
                    daiban_layer.close(index);
                    if(data.msg==='success'){
                        if(flag) {
                            new myPagination({
                                id: 'page_daiban',
                                curPage: 1, //初始页码
                                pageTotal: data.yeshu, //总页数
                                pageAmount: 10,  //每页多少条
                                dataTotal: data.total, //总共多少条数据
                                pageSize: 5, //可选,分页个数
                                showPageTotalFlag: true, //是否显示数据统计
                                //  showSkipInputFlag:true, //是否支持跳转
                                getPage: function (page) {
                                    //获取当前页数
                                    daiban(page,false);
                                }
                            });
                        }
                        linksList_daiban(data.result);
                    }else {
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取代办任务列表错误："+data.result);
                    }
                },
                error: function (e, jqxhr, settings, exception) {
                    daiban_layer.close(index);
                    alert('服务器响应失败!!!')
                }

            });
        }

    function selDaiBanTask() {
        var proName=$("#proName").val();
        var startPeople=$("#startPeople").val();
        var time=$("#time1").val();
        // alert(name+","+depName+","+time.split("-")[0]+","+parseInt(time.split("-")[1]));
        seldaiban(1,true,proName,startPeople,time);
    }
    function seldaiban(num,flag,proName,startPeople,time) {
        var index=daiban_layer.load(2,{offset:['200px','46%']});
        $.ajax({
            type: "POST",
            data:{userName:userName,num:num,proName:proName,startPeople:startPeople,time:time},
            dataType: "json",
            url: "${ctx}/processInfo/selectTask1",
            success: function (data) {
                daiban_layer.close(index);
                if(data.msg==='success'){
                    console.log(flag)
                    console.log("页数："+data.yeshu+"  总数："+data.total)
                    if(flag) {
                        new myPagination({
                            id: 'page_daiban',
                            curPage: 1, //初始页码
                            pageTotal: data.yeshu, //总页数
                            pageAmount: 10,  //每页多少条
                            dataTotal: data.total, //总共多少条数据
                            pageSize: 5, //可选,分页个数
                            showPageTotalFlag: true, //是否显示数据统计
                            //  showSkipInputFlag:true, //是否支持跳转
                            getPage: function (page) {
                                //获取当前页数
                                seldaiban(page,false,proName,startPeople,time);
                            }
                        });
                    }
                    linksList_daiban(data.result);
                }else {
                    window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取代办任务列表错误："+data.result);
                }
            },
            error: function (e, jqxhr, settings, exception) {
                daiban_layer.close(index);
                alert('服务器响应失败!!!')
            }

        });
    }

        function linksList_daiban(datas) {
            $("#daibanTbody").empty();
            $("#noDateDiv_daiban").empty();
            if(datas.length==0){
                $("#noDateDiv_daiban").append("<div style=\"width: 250px;height: 200px;margin: 20px 42%;\">\n" +
                    "<img src=\"${ctx}/static/images/noDate.jpg\" width=\"100%\" height=\"100%\">\n" +
                    "</div>");
                $("#page_daiban").hide();
            }else{
                $("#page_daiban").show();
                for(var i=0;i<datas.length;i++){
                    $("#daibanTbody").append("<tr>" +
                        "<td>"+datas[i].proname+"</td>"+
                        "<td>"+datas[i].userRealName+"</td>"+
                        "<td>"+datas[i].taskId+"</td>"+
                        "<td>"+datas[i].taskName+"</td>"+
                        "<td>"+datas[i].proStartTime+"</td>"+
                        "<td><span name='banliTask' style='color: #FFB800' class='myspandaiban'>办理</span></td>"+
                        "<input type='hidden' value="+datas[i].proDefinedId+" />"+
                        "<input type='hidden' value="+datas[i].proInstanceId+" />"+
                        "<tr>");
                }
            }

        }

        function baocun(num,flag) {
            var index=daiban_layer.load(2,{offset:['200px','46%']});
            $.ajax({
                type: "POST",
                data:{userName:userName,num:num},
                dataType: "json",
                url: "${ctx}/processInfo/selectBaoCun",
                success: function (data) {
                    daiban_layer.close(index);
                    if(data.msg==='success'){
                        if(flag) {
                            new myPagination({
                                id: 'page_baocun',
                                curPage: 1, //初始页码
                                pageTotal: data.yeshu, //总页数
                                pageAmount: 10,  //每页多少条
                                dataTotal: data.total, //总共多少条数据
                                pageSize: 5, //可选,分页个数
                                showPageTotalFlag: true, //是否显示数据统计
                                //  showSkipInputFlag:true, //是否支持跳转
                                getPage: function (page) {
                                    //获取当前页数
                                    baocun(page,false);
                                }
                            });
                        }
                        linksList_baocun(data.result);
                    }else {
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取保存任务列表错误："+data.result);
                    }
                },
                error: function (e, jqxhr, settings, exception) {
                    daiban_layer.close(index);
                    alert('服务器响应失败!!!')
                }

            });
        }

    function linksList_baocun(datas) {
        $("#baocunTbody").empty();
        $("#noDateDiv_baocun").empty();
        if(datas.length==0){
            $("#noDateDiv_baocun").append("<div style=\"width: 250px;height: 200px;margin: 20px 42%;\">\n" +
                "<img src=\"${ctx}/static/images/noDate.jpg\" width=\"100%\" height=\"100%\">\n" +
                "</div>");
            $("#page_baocun").hide();
        }else{
            $("#page_baocun").show();
            for(var i=0;i<datas.length;i++){
                var state=datas[i].state;
                var btn="";
                if('2'==state){
                    btn="<span name='baocun_application' style='color: #FFB800' class='myspandaiban'>办理</span>&nbsp;|&nbsp;" +
                        "<span name='baocun_shanchu' style='color: #FF5722' class='myspandaiban'>删除</span>";
                }else{
                    btn="已删除";
                }
                $("#baocunTbody").append("<tr>" +
                    "<td>"+datas[i].proName+"</td>"+
                    "<td>"+userRealName+"</td>"+
                    "<td>"+datas[i].taskName+"</td>"+
                    "<td>"+datas[i].opreateTime+"</td>"+
                    "<td>" + btn+
                    "</td>"
                    +"<input type='hidden' value="+datas[i].requestid+">"
                    +"<input type='hidden' value="+ datas[i].reportName+">"
                    +"<input type='hidden' value="+datas[i].deployid+">"
                    +"<input type='hidden' value="+datas[i].proName+">"
                    +"<input type='hidden' value="+datas[i].proDefineId+">"
                    +"<input type='hidden' value="+datas[i].comment+">"
                    +"<input type='hidden' value="+datas[i].attachment+">"
                    +"<input type='hidden' value="+datas[i].iswritecomment+">"
                    +"<input type='hidden' value="+datas[i].tijiaoName+">" +
                    "<tr>");
            }
        }

    }


</script>
</body>
</html>
