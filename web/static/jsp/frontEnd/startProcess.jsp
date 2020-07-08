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
    <link rel="stylesheet" href="${ctx}/static/css/startProcess.css">

</head>
<body style="background: #F5F7FE;">
      <div id="content">

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
<script>
    $(document).ready(function () {
        var userName=parent.Dec.personal.username;
        var userRealName=parent.Dec.personal.displayName.split("(")[0];
        layui.use('layer', function(){
             layer = layui.layer;
            /*得到流程部署信息*/
            var index=layer.load(2,{offset:['200px','50%']});
            $.ajax({
                 type: "POST",
                 dataType: "json",
                data:{userName:userName},
                 url: "${ctx}/processInfo/selectProList",
                success: function (data) {
                    layer.close(index);
                    if(data.msg==='success'){
                        $("#content").empty();
                        for(var i=0;i<data.result.length;i++){
                            $("#content").append("<div class='listing'> <h4 class=\"listing-title\">"+data.result[i].proclassify+"</h4>" +
                                " <ul id="+i+" class=\"listing-con\"> </ul> </div>");
                            for(var j=0;j<data.result[i].proLists.length;j++){
                                $("#"+i).append(
                                    "<li><a href=\"#\" name='tiaozhuan' >"+data.result[i].proLists[j].deName.replace("_","")+
                                    "<input  type='hidden' value="+data.result[i].proLists[j].depid+" >"+
                                    "<input  type='hidden' value="+data.result[i].proLists[j].deName+" >"+
                                    "<input  type='hidden' value="+data.result[i].proLists[j].processDefinitionID+" >" +
                                    "<input  type='hidden' value="+data.result[i].proLists[j].deNameParam+" >" +
                                    "</a><li>");
                            }
                        }
                    }else{
                        window.location.href="${ctx}/static/jsp/message.jsp?message="+encodeURI("获取流程列表错误："+data.result);
                    }

                },
                error: function (e, jqxhr, settings, exception) {
                    layer.close(index);
                    alert('服务器响应失败!!!')
                }
            });
            /*跳转*/
            $(document).on("click","${'[name=\'tiaozhuan\']'}",function(){
                var depid=$(this).parent("li").find("input[type='hidden']").eq(0).val();
                var name=$(this).parent("li").find( "input[type='hidden']").eq(1).val();
                var processDefinitionID=$(this).parent("li").find("input[type='hidden']").eq(2).val();
                var proNameParam=$(this).parent("li").find( "input[type='hidden']").eq(3).val();
                window.parent.FS.tabPane.addItem({title:name,src:"${ctx}/processInfo/authority?depid="+depid+"&proname="+
                encodeURI(name)+"&processDefinitionID="+processDefinitionID+"&userName="+userName+"&proNameParam="+encodeURI(proNameParam)});
            });

        });

    });
</script>
</html>
