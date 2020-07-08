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
        .workflow-state{
            float: right;
            width: 20%;
            margin-top: 14px;
        }
        .mui-table {
            display: table;
            width: 80%;
            table-layout: fixed;
            float: left;
        }
    </style>
</head>
<body>


<div class="mui-content">
    <div id="slider" class="mui-slider mui-fullscreen">
        <div id="sliderSegmentedControl" class="mui-scroll-wrapper  mui-segmented-control ">
            <div class="mui-scroll" style="width: 100%;">
                <a  id="daiban" class="mui-control-item mui-active" <%--href="#item1mobile"--%> style="width: 50%;">
                    已申请(<span id="count1">0</span>)
                </a>
                <a id="yibaocun" class="mui-control-item" <%--href="#item2mobile"--%>  style="width: 50%;">
                    已办理(<span id="count2">0</span>)
                </a>

            </div>
        </div>
      <%--  <div class="searchBox">
            <div class="mui-input-row mui-search" id="searchForm">
                <input type="search" id="searchInput" class="mui-input-clear" placeholder="搜索">
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
<script type="text/javascript" src="/webroot/decision/view/report?op=emb&resource=finereport.js"></script>
<script>

    (function($) {
        //阻尼系数
        var deceleration = mui.os.ios ? 0.003 : 0.0009;
        $('.mui-scroll-wrapper').scroll({
            bounce: false,
            indicators: true, //是否显示滚动条
            deceleration: deceleration
        });
        $.ready(function() {
            var yishenqingCount=1;
            var yichuliCount=1;
            var yichuliFlag=true;
            var slider = mui("#slider").slider();
            slider.stopped = true; //关闭滑动切换

            inityishengqing();


            $("#slider").on("tap","#daiban",function () {
                jQuery("#item1mobile").show();
                jQuery("#item2mobile").hide();

            });
            $("#slider").on("tap","#yibaocun",function () {
                jQuery("#item1mobile").hide()
                jQuery("#item2mobile").show()
                if(yichuliFlag){
                    inityichuli();
                }
            });

            $(".mui-scroll").on("tap",".mui-table-view-cell",function(){
                var reportName=this.dataset.reportname;
                var proinsid=this.dataset.proinsid;
                var requestid=this.dataset.businesskey;
                var proDefineId = this.dataset.prodefinitionid;
                var activityid = this.dataset.activityid;
                var src = "${ctx}/static/jsp/mobile/requestDetailMobile.jsp?requestid="+requestid+"&reportName="+
                    encodeURI(reportName)+"&proDefineID="+proDefineId+"&proinsid="+proinsid+"&activityid="+activityid;
               FR.doHyperlinkByGet({
                   url:src
               })
            })

            //初始化已申请
            function inityishengqing() {
                $.showLoading("","div");
                $("#daibanrefresh").pullToRefresh({
                    up: {//上拉下载
                        auto:true,
                        callback: function() {
                            var self = this;
                            refreshelem1 = this;
                            getYiShenQiList(yishenqingCount,self);
                        }
                    }
                });
            }

            //初始化已处理
            function inityichuli() {
                $.showLoading("","div");
                $("#yibaocunrefresh").pullToRefresh({
                    up: {//上拉下载
                        auto:true,
                        callback: function() {
                            var self = this;
                            refreshelem2 = this;
                            getYiChuLiList(yichuliCount,self);
                        }
                    }
                });
            }

           //获取已申请列表
            function getYiShenQiList(page,self){
                $.ajax("${ctx}/processInfo/selectHisPro",{
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
                                html += "  <li   data-businessKey='"+datas[i].businessKey+"'  data-reportname='"+datas[i].reportName+"'  data-activityid='"+datas[i].activityid+"'  data-proinsid='"+datas[i].proInsID+"'  data-proname='"+datas[i].proname+"' data-prodefinitionid='"+datas[i].proDefinitionId+"'       class=\"mui-table-view-cell\">\n" +
                                    "                <div class=\"mui-table\">\n" +
                                    "                    <h4 class=\"mui-ellipsis\">"+datas[i].proname+"</h4>\n" +
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
                                    "<div class=\"workflow-state\">"+
                                    "<span class=\"state-span\">"+datas[i].proCompleteState+"</span>"+
                                        "</div>"+
                                    "            </li>";
                            }
                            jQuery("#daibanul").append(html);
                            self.endPullUpToRefresh(yishenqingCount>=data.yeshu);
                            yishenqingCount++;
                        }else {
                            $.toast("获取已申请列表错误："+data.result);
                        }
                    },
                    error: function () {
                        $.hideLoading();
                        $.toast('服务器响应失败!!!')
                    }

                });
            }

            //获取已保存任务列表
            function getYiChuLiList(page,self) {
                $.ajax("${ctx}/processInfo/selectHisProYiChuLi",{
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
                                baocunhtml += "  <li  data-businessKey='"+datas[i].businessKey+"'  data-reportname='"+datas[i].proFormKey+"'  data-activityid='"+datas[i].activityid+"'  data-proinsid='"+datas[i].proInstanceId+"'  data-proname='"+datas[i].proname+"' data-prodefinitionid='"+datas[i].proDefineID+"'        class=\"mui-table-view-cell\">\n" +
                                    "                <div class=\"mui-table\">\n" +
                                    "                    <h4 class=\"mui-ellipsis\">"+datas[i].proname+"</h4>\n" +
                                    "                    <div class=\"mui-clearfix toDoBox\">\n" +
                                    "                        <div class=\"mui-table-cell mui-pull-left\">\n" +
                                    "                            <h5>["+datas[i].startPeople+"]</h5>\n" +
                                    "                        </div>\n" +
                                    "                        <div class=\"mui-table-cell mui-pull-left \">\n" +
                                    "                            <span class=\"mui-h5\">提交于"+datas[i].proStartTime+"</span>\n" +
                                    "                        </div>\n" +
                                    "  <div id=\"baoucunBtn\" style=\"display: inline-block;float: right;\">\n" +

                                    "                        </div>"+
                                    "                    </div>\n" +
                                    "                </div>\n"+
                                     "<div class=\"workflow-state\">"+
                                        "<span class=\"state-span\">"+getState(datas[i].proStatus)+"</span>"+
                                      "</div>"+
                                     " </li>";
                            }
                            jQuery("#yibaocunul").append(baocunhtml);
                            yichuliFlag=false;
                            self.endPullUpToRefresh(yichuliCount>=data.yeshu);
                            yichuliCount++;
                        }else {
                           $.toast("获取已处理列表错误："+data.result);
                        }
                    },
                    error: function () {
                        $.hideLoading();
                        $.toast('服务器响应失败!!!')
                    }

                });
            }

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




        });
    })(mui);

</script>
</body>
</html>
