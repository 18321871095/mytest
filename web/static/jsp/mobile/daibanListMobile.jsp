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
       // alert("插件试用阶段，还剩余<%=time %>天")
    }
</script>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="${ctx}/static/css/mui.min.css">
    <link rel="stylesheet" href="${ctx}/static/css/muikuozhan.css">
    <link rel="stylesheet" href="${ctx}/static/css/index_mobile.css">
    <style>
        html,
        body {
            background-color: #efeff4;
        }

        .mui-bar~.mui-content .mui-fullscreen {
            top: 44px;
            height: auto;
        }

        .mui-pull-top-tips {
            position: absolute;
            top: -20px;
            left: 50%;
            margin-left: -25px;
            width: 40px;
            height: 40px;
            border-radius: 100%;
            z-index: 1;
        }

        .mui-bar~.mui-pull-top-tips {
            top: 24px;
        }

        .mui-pull-top-wrapper {
            width: 42px;
            height: 42px;
            display: block;
            text-align: center;
            background-color: #efeff4;
            border: 1px solid #ddd;
            border-radius: 25px;
            background-clip: padding-box;
            box-shadow: 0 4px 10px #bbb;
            overflow: hidden;
        }

        .mui-pull-top-tips.mui-transitioning {
            -webkit-transition-duration: 200ms;
            transition-duration: 200ms;
        }

        .mui-pull-top-tips .mui-pull-loading {
            /*-webkit-backface-visibility: hidden;
            -webkit-transition-duration: 400ms;
            transition-duration: 400ms;*/
            margin: 0;
        }

        .mui-pull-top-wrapper .mui-icon,
        .mui-pull-top-wrapper .mui-spinner {
            margin-top: 7px;
        }

        .mui-pull-top-wrapper .mui-icon.mui-reverse {
            /*-webkit-transform: rotate(180deg) translateZ(0);*/
        }

        .mui-pull-bottom-tips {
            text-align: center;
            background-color: #efeff4;
            font-size: 15px;
            line-height: 40px;
            color: #777;
        }

        .mui-pull-top-canvas {
            overflow: hidden;
            background-color: #fafafa;
            border-radius: 40px;
            box-shadow: 0 4px 10px #bbb;
            width: 40px;
            height: 40px;
            margin: 0 auto;
        }

        .mui-pull-top-canvas canvas {
            width: 40px;
        }

        .mui-slider-indicator.mui-segmented-control {
            background-color: #efeff4;
        }
        .mui-fullscreen .mui-segmented-control~.mui-slider-group{
            position: absolute;
            top: 94px;
        }
    </style>
    <style>

        .mui-plus .plus{
            display: inline;
        }

        .plus{
            display: none;
        }

        #topPopover {
            position: fixed;
            top: 16px;
            right: 6px;
        }
        #topPopover .mui-popover-arrow {
            left: auto;
            right: 6px;
        }
        p {

        }
        span.mui-icon {
            font-size: 14px;
            color: #007aff;
            margin-left: -15px;
            padding-right: 10px;
        }
        .mui-popover {
            height: 300px;
        }
        .mui-content {
            padding: 10px;
        }
    </style>
</head>
<body>

<div class="mui-content">
    <div id="slider" class="mui-slider mui-fullscreen">
        <div id="sliderSegmentedControl" class="mui-scroll-wrapper  mui-segmented-control ">
            <div class="mui-scroll" style="width: 100%;">
                <a  id="daiban" class="mui-control-item mui-active"<%-- href="#item1mobile"--%> style="width: 50%;">
                    待办任务(<span id="count1">0</span>)
                </a>
                <a id="yibaocun" class="mui-control-item" <%--href="#item2mobile"--%>  style="width: 50%;">
                    已保存任务(<span id="count2">0</span>)
                </a>

            </div>
        </div>
     <%-- <div class="searchBox">
            <div class="mui-input-row mui-search" id="searchForm" style="width: 75%;display: inline-block;">
                <input type="search" id="searchInput" class="mui-input-clear" placeholder="流程名称搜索">
            </div>
          <div style="display: inline-block;overflow: hidden;">
              <button id="btn"  type="button" class="mui-btn mui-btn-primary">搜索</button>
          </div>
        </div>--%>
        <div class="mui-slider-group toDo" style="top: 45px;">
            <div id="item1mobile" class="mui-slider-item mui-control-content mui-active">
                <div id="scroll1" class="mui-scroll-wrapper">
                    <div  class="mui-scroll" id="daibanrefresh">
                        <ul id="daibanul"  class="mui-table-view">

                        </ul>
                    </div>
                </div>
            </div>
            <div id="item2mobile" class="mui-slider-item mui-control-content">
                <div class="mui-scroll-wrapper">
                    <div class="mui-scroll" id="yibaocunrefresh">
                        <ul id="yibaocunul"  class="mui-table-view">

                        </ul>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/js/mui.js"></script>
<script src="${ctx}/static/js/muikuozhan.js"></script>
<script src="${ctx}/static/js/mui.pullToRefresh.js"></script>
<script src="${ctx}/static/js/mui.pullToRefresh.material.js"></script>
<script>

    (function($) {
        //阻尼系数
        var deceleration = mui.os.ios ? 0.003 : 0.0009;
        $('.mui-scroll-wrapper').scroll({
            bounce: false,
            indicators: true, //是否显示滚动条
            deceleration: deceleration,
        });
        $.ready(function() {
            var daibanCount=1;
            var baocunCount=1;
            var baocunFlag=true;
            var slider = mui("#slider").slider();
            slider.stopped = true; //关闭滑动切换

            initdaiban();


            $("#slider").on("tap","#daiban",function () {
                jQuery("#item1mobile").show();
                jQuery("#item2mobile").hide();
                jQuery("#searchInput").val("");
                btnType=1;
                daibanCountSearch=1;
            });

            $("#slider").on("tap","#yibaocun",function () {
                jQuery("#item1mobile").hide()
                jQuery("#item2mobile").show()
                jQuery("#searchInput").val("");
                btnType=2;
                if(baocunFlag){
                    initbaocun();
                }
            });



            $("#daibanul").on("tap",".mui-table-view-cell",function(){
                var taskid=this.dataset.taskid;
                var proname=this.dataset.proname;
                var proDefinedId=this.dataset.prodefinedid;
                var proInstanceId=this.dataset.proinstanceid;
                window.location.href="${ctx}/static/jsp/mobile/banliTaskMobile.jsp?taskid="+taskid+"&proname="+encodeURI(proname)+
                    "&proDefinedId="+proDefinedId+"&proInstanceId="+proInstanceId;
            })
            $("#yibaocunul").on("tap",".mui-table-view-cell",function(){
                var reportName=this.dataset.reportname;
                var deployid=this.dataset.deployid;
                var baocunProname=this.dataset.proname;
                var proDefineId=this.dataset.prodefineid;

                var comment=this.dataset.comment;
                var attachmentTemp=this.dataset.attachment;
                var iswritecomment=this.dataset.iswritecomment;
                var tijiaoName=this.dataset.tijiaoname;
                var requestid=this.dataset.requestid;
                window.location.href="${ctx}/static/jsp/mobile/applicationMobile.jsp?requestid="+requestid+"&reportName="+
                    encodeURI(reportName)+"&deployid="+deployid+"&baocunProname=" +encodeURI(baocunProname)+"&processDefinitionID="+proDefineId+"&state=1"+"&comment="+encodeURI(comment)
                    +"&tijiaoName="+encodeURI(tijiaoName)+"&iswritecomment="+iswritecomment+"&attachment="+encodeURI(attachmentTemp)+"&param0="+"&param1=";
            })


           /* $("#yibaocunul").on("longtap",".mui-table-view-cell",function(){
                var btnArray = ['确认', '取消'];
                var elem = this;
                var requestid = this.dataset.requestid;
                $.confirm('确认删除该条记录？', '温馨提示', btnArray, function(e) {
                    if (e.index == 0) {
                        deleteBaocun(requestid,elem);

                    }
                });
            })*/

            //初始化待办列表
            function initdaiban(){
                $.showLoading("","div");
                $("#daibanrefresh").pullToRefresh({
                    up: {
                        auto:true,
                        callback: function() {
                            var self = this;
                            refreshelem1 =this;
                            getDaibanList(daibanCount,self);
                        }
                    }
                });
            }

            //初始化保存列表
            function initbaocun() {
                $.showLoading("","div");
                $("#yibaocunrefresh").pullToRefresh({
                    up: {
                        auto:true,
                        callback: function() {
                            var self = this;
                            refreshelem2 =this;
                            getBaoCunList(baocunCount,self);
                        }
                    }
                });
            }

           //删除保存
            function deleteBaocun(requestid,elem){
                $.ajax("${ctx}/mobile/removeBaoCun",{
                    type: "POST",
                    data:{id:requestid},
                    dataType: "json",
                    success: function (data) {
                        $.toast("删除成功！");
                        elem.parentNode.removeChild(elem);
                        jQuery("#count2").text(parseInt(jQuery("#count2").text())-1);
                    },
                    error: function () {
                        $.toast('服务器响应失败!!!')
                    }

                });

            }

           //获取待办列表
            function getDaibanList(page,self){
                $.ajax("${ctx}/processInfo/selectTask",{
                    type: "POST",
                    data:{num:page},
                    dataType: "json",
                    success: function (data) {
                        $.hideLoading();
                        if(data.msg==='success'){
                            jQuery("#count1").text(data.total);
                            var html = '';
                            var datas = data.result;
                            for(var i=0;i<datas.length;i++){
                                html += "  <li   data-proinstanceId='"+datas[i].proInstanceId+"'  data-taskid='"+datas[i].taskId+"'  data-proname='"+datas[i].proname+"' data-prodefinedid='"+datas[i].proDefinedId+"'       class=\"mui-table-view-cell\">\n" +
                                    "                <div class=\"mui-table\">\n" +
                                    "                    <h4  class=\"mui-ellipsis\">"+datas[i].proname+"</h4>\n" +
                                    "                    <div class=\"mui-clearfix toDoBox\">\n" +
                                    "                        <div class=\"mui-table-cell mui-pull-left\">\n" +
                                    "                            <h5>["+datas[i].userRealName+"]</h5>\n" +
                                    "                        </div>\n" +
                                    "                        <div class=\"mui-table-cell mui-pull-left \">\n" +
                                    "                            <span class=\"mui-h5\">提交于"+datas[i].proStartTime+"</span>\n" +
                                    "                        </div>\n" +
                                    "  <div  style=\"display: inline-block;float: right;\">\n" +
                                    "                        </div>"+
                                    "                    </div>\n" +
                                    "                </div>\n" +
                                    "            </li>";
                            }
                            jQuery("#daibanul").append(html);
                            self.endPullUpToRefresh(daibanCount>=data.yeshu);
                            daibanCount++;
                        }else {
                            $.toast("获取代办任务列表错误："+data.result);
                        }
                    },
                    error: function () {
                        $.hideLoading();
                        $.toast('服务器响应失败!!!')
                    }

                });
            }

            //获取已保存任务列表
            function getBaoCunList(page,self) {
                $.ajax("${ctx}/processInfo/selectBaoCun",{
                    type: "POST",
                    data:{num:page},
                    dataType: "json",
                    success: function (data) {
                        $.hideLoading();
                        if(data.msg==='success'){
                            var baocunhtml = '';
                            var datas = data.result;
                            jQuery("#count2").text(data.total);
                            for(var i=0;i<datas.length;i++){
                                baocunhtml += "  <li  data-requestid='"+datas[i].requestid+"'  data-reportname='"+datas[i].reportName+"' data-deployid='"+datas[i].deployid+"' data-proname='"+datas[i].proName+"' data-prodefineid='"+datas[i].proDefineId+"' data-comment='"+datas[i].comment+"' data-attachment='"+datas[i].attachment+"' data-iswritecomment='"+datas[i].iswritecomment+"' data-tijiaoname='"+datas[i].tijiaoName+"'  class=\"mui-table-view-cell\">\n" +
                                    "                <div class=\"mui-table\">\n" +
                                    "                    <h4   class=\"mui-ellipsis\">"+datas[i].proName+"</h4>\n" +
                                    "                    <div class=\"mui-clearfix toDoBox\">\n" +
                                    "                        <div class=\"mui-table-cell mui-pull-left\">\n" +
                                    "                            <h5>["+datas[i].userRealName+"]</h5>\n" +
                                    "                        </div>\n" +
                                    "                        <div class=\"mui-table-cell mui-pull-left \">\n" +
                                    "                            <span class=\"mui-h5\">保存于"+datas[i].opreateTime+"</span>\n" +
                                    "                        </div>\n" +
                                    "  <div id=\"baoucunBtn\" style=\"display: inline-block;float: right;\">\n" +

                                    "                        </div>"+
                                    "                    </div>\n" +
                                    "                </div>\n"+
                                    "            </li>";
                            }
                            jQuery("#yibaocunul").append(baocunhtml);
                             baocunFlag=false;
                              self.endPullUpToRefresh(baocunCount>=data.yeshu);
                            baocunCount++;
                        }else {
                           $.toast("获取保存任务列表错误："+data.result);
                        }
                    },
                    error: function () {
                        $.hideLoading();
                        $.toast('服务器响应失败!!!')
                    }

                });
            }

            //搜索获取待办

        });
    })(mui);
    //键盘按下时查询
    function enterSearch_daiban(){


    }
    function enterSearch_baocun(){
        var searchinput =  mui("#searchInput")[0].value.replace(/\s+/g,"");
        console.log("baocun")
    }
</script>
</body>
</html>
