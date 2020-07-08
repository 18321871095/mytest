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
<!doctype html>
<html>
	<head>
		<meta charset="utf-8">
		<title>新建流程</title>
		<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no" />
		<link href="${ctx}/static/css/mui.min.css" rel="stylesheet" type="text/css" />
		<link href="${ctx}/static/css/index_mobile.css" rel="stylesheet" type="text/css" />
		<style>
			.myinput{
				margin-bottom: 0;
				background: #f7f7f7;
				padding-left: 30px;
				font-size: 14px;
				box-sizing: border-box;
				height: 35px;
				text-align: center;
				border: 0;
				border-radius: 6px;
				color: #333;
			}
		</style>
	</head>

	<body style="background: #f7f7f7;">
		<div class="mui-content">
			  <!-- MUI 搜索框 -->
			<%--<div style="background: #fff;height: 55px;">
				<input class="mui-input-clear" class="myinput">
			</div>--%>

			<div class="searchBox">
				<div class="mui-input-row mui-search" id="searchForm" style="width: 75%;display: inline-block">
			   		 <input type="search" id="searchInput"  class="mui-input-clear" placeholder="流程名称搜索">
			    </div>
				<div style="display: inline-block;overflow: hidden;">
					<button onclick="enterSearch()" type="button" class="mui-btn mui-btn-primary">搜索</button>
				</div>
			</div>

			<!-- 搜索框结束 -->
            <div class="list-content">

			</div>
			
		</div>
		
		<script src="${ctx}/static/js/mui.js"></script>

		<script type="text/javascript">
			mui.init()
            var list = [];
            var temp = [];
            mui.ready(function() {
                getList();
                mui(".list-content").on("tap","li",function () {
                     var depid = this.dataset.depid;
                     var name = this.innerText;
                    var processDefinitionID=this.dataset.processdefinitionid;
                    var deNameParam=this.dataset.denameparam;

                    var src = "${ctx}/mobile/authority?depid="+depid+"&proname="+ encodeURI(name)+"&processDefinitionID="+processDefinitionID+"&proNameParam="+encodeURI(deNameParam)
                    window.open(src,name);
                })

            })
            function getList() {
                mui.ajax("${ctx}/mobile/selectProList", {
                    type: "POST",
                    dataType: "json",
                    success: function(data) {
                        if(data.result !=null){
                            list = data.result;
                            temp = list;
                            initList(list);
                        }else{
                            var  none = '<div class="newFlow"><ul class="mui-table-view mui-table-view-chevron"><li class="mui-table-view-cell">暂无数据！</li> </ul>';
                            mui(".list-content")[0].innerHTML= none;
						}
                    },
                    error: function(xhr, type, errorThrown) {
                        mui.toast("服务器内部错误!");
                        console.log('error:' + type);
                    }
                })

            }
            //键盘按下时查询
            function enterSearch(){
			    var searchinput =  mui("#searchInput")[0].value.replace(/\s+/g,"");
                initList(indexSelect(searchinput));
			}
			//初始化列表
            function initList(list){
                mui(".list-content")[0].innerHTML= '';
                var listHtml = '';
                for(var i =0; i<list.length;i++){
                    listHtml += '<div class="newFlow">';
                    listHtml +='<h3 class="newFlowTitle"><span class="newFlowTitleSpan"></span><span class="newFlowTitleTit">'+list[i].proclassify+'</span></h3>';
                    listHtml +='<ul class="mui-table-view mui-table-view-chevron">';
                    if(list[i].proLists.length>0){
                        var childList = list[i].proLists;
                        for(var n=0;n<childList.length;n++){
                            listHtml +='<li data-depid="'+childList[n].depid+'"  data-processDefinitionID="'+childList[n].processDefinitionID+'"  data-deNameParam="'+childList[n].deNameParam+'" class="mui-table-view-cell">'+childList[n].deName+'</li>';
                        }
                    }
                    listHtml +='</ul></div>';
                }
                mui(".list-content")[0].innerHTML= listHtml;
                if(listHtml ==''){
                    var  none = '<div class="newFlow"><ul class="mui-table-view mui-table-view-chevron"><li class="mui-table-view-cell">暂无数据！</li> </ul>';
                    mui(".list-content")[0].innerHTML= none;
				}
			}
			//模糊查询json
            function indexSelect(index){
                var newJSON=[];
                if(index=="" || index==null||temp.length==0 ){
                    return temp;
                }else{
                    for(var z=0;z<temp.length;z++){
                        var tempJson1=[];
                       for(var m=0;m<temp[z].proLists.length;m++){
                           var deName = temp[z].proLists[m].deName;
                           var tempObject;
                          if(deName.indexOf(index) !=-1){
                              tempObject = {
                                  deNameParam:temp[z].proLists[m].deNameParam,
                                  processDefinitionID:temp[z].proLists[m].processDefinitionID,
                                  deName:temp[z].proLists[m].deName,
                                  proclassify:temp[z].proLists[m].proclassify,
                                  depid:temp[z].proLists[m].depid,
                                  version:temp[z].proLists[m].version
							  }
                              tempJson1.push(tempObject);
						  }
					   }
					   if(tempJson1.length>0){
                           var tempObject1={
                               proclassify:temp[z].proclassify,
                               proLists:tempJson1
                           }
                           newJSON.push(tempObject1)
					   }
					}
                    return newJSON;
                }
            }
		</script>
	</body>

</html>
