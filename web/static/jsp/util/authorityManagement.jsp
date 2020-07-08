<%@ page import="org.activiti.engine.RepositoryService" %>
<%@ page import="com.fr.tw.util.SpringContextUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<link rel="stylesheet" href="${ctx}/static/layui/css/layui.css">
<head>
    <style>
        .myli{
            cursor: pointer;
        }
        .myli:hover{
            color: red;
        }
        .people_span{
            cursor: pointer;
            padding-right: 10px;
        }
        .people_span:hover{
            color: blue;
            text-decoration: underline;
        }

    </style>

    <%
        String Authorization="";
        Cookie[] c=request.getCookies();
        for(Cookie cookie:c){
            if("fine_auth_token".equals(cookie.getName())){
                Authorization=cookie.getValue();
            }
        }
    %>
</head>
<body style="padding: 0px;margin: 0px">
<div style="width: 38%;height: 50%;float: left;overflow-y:auto;border: 1px solid #e6e6e6">
    <span style="font-size: 15px">部门：</span>
    <input id="depName" type="text" placeholder="模糊查询部门名称" /><button onclick="selDep()">搜索</button>
    <hr style="margin: 5px 0px">
    <ul id="department" style="line-height: 37px;padding: 0px 10px;font-size: 18px">
    </ul>
</div>
<div style="width: 18%;height: 50%;float: left;overflow-y:auto;border: 1px solid #e6e6e6">
    <span style="font-size: 15px">职位：</span><hr style="margin: 5px 0px">
    <ul id="position" style="line-height: 37px;padding: 0px 10px;font-size: 18px">
    </ul>
</div>
<div style="width: 43%;height: 50%;float: left;overflow-y:auto;border: 1px solid #e6e6e6">
    <span style="font-size: 15px">人员：</span><hr style="margin: 5px 0px">
    <ul id="user" style="line-height: 37px;padding: 0px 10px;font-size: 18px">
    </ul>
</div>
<div style="float: left;width: 86%" >
    <span>所选部门或岗位</span>
    <div id="depOrPos" style="border: 1px solid #e6e6e6;width: 100%;height: 150px;margin-left: 110px;
    word-wrap:break-word;overflow-y: auto"></div>
</div>
<div style="float: left;width: 86%">
    <span>所选人员</span>
    <div id="people" style="border: 1px solid #e6e6e6;width: 100%;height: 150px;margin-left: 110px;word-wrap:break-word;
overflow-y: auto;">

      <%--  <span onclick="deletePeople(this)" class="people_span">Lily(孙红)</span>
        <span onclick="deletePeople(this)" class="people_span">Mike(麦克)</span>--%>

    </div>
</div>
<div style="float: left;color: red;font-weight: bold;">说明：选择部门默认该部门下的所有岗位都有权限，选择岗位默认该岗位下所有用户有权限</div>
<div style="float: right;margin-top: 35px;margin-right: 50px;">
    <button class="layui-btn" id="reserve">保存</button>
    <br/> <br/>
</div>


<script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
<script src="${ctx}/static/layui/layui.js"></script>
</body>
<script>
    var arr=[];var peolpearr=[];var dep="";var layer;var currentobj;
    var tempdep;
    $(function () {
        //获取部门
        $.ajax({
            type:"get",
            url:"${ctx}/decision/v10/departments/decision-dep-root",
            dataType:'json',
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
            success:function (data) {
                $("#department").empty();
                for(var i=0;i<data.data.length;i++) {
                    var temp="";var isParent="";
                    tempdep=data;
                    if(!data.data[i].isParent){
                        isParent="false";
                        temp="<div name="+isParent+" onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 15px;'>"+data.data[i].text+"</div>";
                    }else {
                        isParent="true";
                        temp="<div name='add'  style='border: 1px solid #000;cursor: pointer;display: inline-block;width: 10px;height: 10px;background-image: url(\"${ctx}/static/images/add.png\");background-size: cover'></div>"
                            +"<div name="+isParent+" onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='margin-left: 5px;display: inline-block'>"+data.data[i].text+"</div>";
                    }
                    //<input onclick='getcheckboxDep(this)' style='width: 16px;height: 16px' type='checkbox'/>
                    $("#department").append("<li><div name="+isParent+" onclick='addValue(this)' style='width:20px;height:20px;display: inline-block;position:relative;top:-1px;cursor: pointer'>"+
                        "<img src='${ctx}/static/images/addpic.png' width='100%' height='100%' /></div>&nbsp;&nbsp;&nbsp;"+
                        temp+"</li>");
                }
            },
            error:function (xhr,text) {
                alert('服务器响应失败!!!')
            }
        });

        //获取权限
       $.ajax({
            type:"POST",
            url:"${ctx}/processInfo/getProAuthoritys",
            data:{prodefinedid:"${param.prodefinedid}"},
            success:function (data) {
                if(data.msg=='success'){
                   /* $("#depOrPos").text(data.result.dep);
                    $("#people").text(data.result.people);*/
                    if(data.result.dep!=''){
                        var d=data.result.dep.split(",");
                        for(var i=0;i<d.length;i++){
                            arr.push(d[i]);
                            $("#depOrPos").append("<span name='deleteDepAndPost' class=\"people_span\">"+d[i]+"</span>");
                        }
                    }
                    if(data.result.people!=''){
                        var p=data.result.people.split(",");
                        for(var i=0;i<p.length;i++){
                            peolpearr.push(p[i]);
                            $("#people").append("<span name='deletePeople' class=\"people_span\">"+p[i]+"</span>");
                        }
                    }
                }else {
                    window.location.href="${ctx}/static/jsp/error.jsp?message="+encodeURI("获取权限错误："+data.result);
                }
            },
            error:function (xhr,text) {
                alert('服务器响应失败!!!')
            }
        });

        layui.use('layer', function() {
            layer=layui.layer;
        });

        $("#reserve").click(function () {
            var deps=$("#depOrPos").text();
            var peoples=$("#people").text();
            if(deps=='' && peoples==''){
                layer.msg("没有指定权限",{offset:'300px'});
            }else {
                var index_set = layer.load(2,{offset:'300px'});
                var result={};
                result["dep"]=getDepAndPost();
                result["people"]=getpeopleValue();
                //console.log("人员："+getpeopleValue()+",岗位："+getDepAndPost())
                //设置权限
                $.ajax({
                    type:"POST",
                    url:"${ctx}/processInfo/setProAuthoritys",
                    data:{prodefinedid:"${param.prodefinedid}",arr:JSON.stringify(result)},
                    success:function (data) {
                        layer.close(index_set);
                        if(data.result=='success'){
                            parent.deploylayer.close(parent.index_a)
                            parent.deploylayer.msg("权限设置成功",{offset:'300px'});

                        }else {
                            layer.alert("设置权限错误："+data.msg,{offset:'200px'});
                          //  window.location.href="${ctx}/static/jsp/error.jsp?message="+encodeURI("设置权限错误："+data.msg);
                        }
                    },
                    error:function (xhr,text) {
                        layer.close(index_set);
                        alert('服务器响应失败!!!')
                    }
                });
            }
        });

        $(document).on("click","${'[name=\'add\']'}",function(){
            $(this).css("background-image","url('${ctx}/static/images/jian.png')").attr("name","jian");
            var mythis=$(this);

            $.ajax({
                type:"get",
                url:"${ctx}/decision/v10/departments/"+$(this).next("div").attr("id"),
                dataType:'json',
                headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
                success:function (data) {
                    for(var i=0;i<data.data.length;i++) {
                        var temp="";var isParent="";
                        if(!data.data[i].isParent){
                            isParent="false";
                            temp="<div name="+isParent+" onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 15px;'>"+data.data[i].text+"</div>";
                        }else {
                            isParent="true";
                            temp="<div name='add'  style='border: 1px solid #000;cursor: pointer;display: inline-block;width: 10px;height: 10px;background-image: url(\"${ctx}/static/images/add.png\");background-size: cover'></div>"
                                +"<div name="+isParent+" onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 5px'>"+data.data[i].text+"</div>";
                        }
                        /*mythis.next().after("<ul style='list-style: none;margin-left: 35px;'><li>"+"<input onclick='getcheckboxDep(this)' style='width: 16px;height: 16px' type='checkbox'/>"+temp+"</li></ul>");*/

                            mythis.next().after("<ul style='list-style: none;margin-left: 30px;'><li>" +
                                "<div name="+isParent+" onclick='addValue(this)' style='width:20px;height:20px;display: inline-block;position:relative;top:-1px;cursor: pointer'>" +
                                "<img src='${ctx}/static/images/addpic.png' width='100%' height='100%' /></div>&nbsp;&nbsp;&nbsp;"+temp+"</li></ul>");

                        // $("#department").append("<li >"+temp+"</li>");
                    }
                },
                error:function (xhr,text) {
                    alert(text);
                }
            });

        });

        $(document).on("click","${'[name=\'jian\']'}",function(){
            $(this).css("background-image","url('${ctx}/static/images/add.png')").attr("name","add")
                .next().nextAll().remove();
        });

        $(document).on("click","${'[name=\'deletePeople\']'}",function(){
            var val=$(this).text();
            //console.log("befor:"+JSON.stringify(peolpearr));
            for(var i=0;i<peolpearr.length;i++){
                if(val===peolpearr[i]){
                    peolpearr.splice(peolpearr.indexOf(val),1);
                }
            }
            $(this).remove();
        });

        $(document).on("click","${'[name=\'deleteDepAndPost\']'}",function(){
            var val=$(this).text();
            //console.log("befor:"+JSON.stringify(peolpearr));
            for(var i=0;i<arr.length;i++){
                if(val===arr[i]){
                    arr.splice(arr.indexOf(val),1);
                }
            }
            $(this).remove();
        });

    });
    function getPosition(obj) {
      //  dep=obj.innerHTML;
        var mythis=$(obj);
        if(mythis.attr("name")=='true'){
            currentobj=mythis.prev().prev();
        }else{
            currentobj=mythis.prev();
        }
        $.ajax({
            type:"get",
            url:"${ctx}/decision/v10/"+obj.id+"/posts",
            dataType:'json',
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
            success:function (data) {
                $("#position").empty();
                for(var i=0;i<data.data.length;i++) {
                  /* $("#position").append("<li onclick="+"getUser(this,"+"\""+obj.id+"\""+")" +" id="+data.data[i].id+" class=\"myli\">"
                        +data.data[i].text+"</li>");*/
               /* <input onclick='getcheckboxPos(this)' style='width: 16px;height: 16px' type='checkbox'/>*/

                    $("#position").append("<li>"+"<div name="+obj.innerHTML+" onclick='addValuePost(this)' style='width:20px;height:20px;display: inline-block;position:relative;top:-1px;cursor: pointer'>"+
                        "<img src='${ctx}/static/images/addpic.png' width='100%' height='100%' /></div>&nbsp;&nbsp;&nbsp;"+
                        "<span id="+data.data[i].id+"  class=\"myli\" onclick="+"getUser(this,"+"\""+obj.id+"\""+")" +">"+data.data[i].text+
                        "</span>"+"</li>");
                }
            },
            error:function (xhr,text) {
                alert(text);
            }
        });
    }

    function getUser(obj,departmentid) {
        var data =  {"page" :"1","count":"50"};
        $.ajax({
            type:"POST",
            url:"${ctx}/decision/v10/"+departmentid+"/"+obj.id+"/users",
            dataType:'json',
            data:JSON.stringify(data),
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"
                ,"Content-Type":"application/json", "Accept": "application/json"},
            success:function (data) {
                $("#user").empty();
           /* <input onclick='getcheckboxPeo(this)' style='width: 16px;height: 16px' type='checkbox'/>*/
                    for(var i=0;i<data.data.items.length;i++) {
                        $("#user").append("<li  class=\"myli\">"
                            +"<div  onclick='addValuePeople(this)' style='width:20px;height:20px;display: inline-block;position:relative;top:-1px;cursor: pointer'>"+
                            "<img src='${ctx}/static/images/addpic.png' width='100%' height='100%' /></div>&nbsp;&nbsp;&nbsp;"+"<span>"+data.data.items[i].username+"</span>"+"<span>"+data.data.items[i].realName+"</span></li>");
                    }
            },
            error:function (xhr,text) {
                alert(text);
            }
        });
    }

/*============================================================================================================================*/

    function getDepAndPost() {
        var value="";
        $("#depOrPos").find("span").each(function () {
            value+=$(this).text()+",";
        });
        return value.substring(0,value.length-1);
    }
    function getpeopleValue() {
        var value="";
        $("#people").find("span").each(function () {
            value+=$(this).text()+",";
        });
        return value.substring(0,value.length-1);
    }

    function addValue(obj) {
        var diguiDep=[];
        var mythis=$(obj);
        var attrName=mythis.attr("name");
        var name="";
        var temp="";
        if(attrName=='true'){
            name=mythis.next().next().text();
        }else{
            name=mythis.next().text();
        }
        digui(mythis,diguiDep);
        for(var i=diguiDep.length;i>0;i--){
            temp+=diguiDep[i-1]+"-";
        }
        var val=temp+name;
        var flag=false;
        for(var i=0;i<arr.length;i++){
            if(arr[i]==val){
                flag=true;
                break;
            }
        }
        if(!flag){
            arr.push(val);
            $("#depOrPos").append("<span name='deleteDepAndPost' class=\"people_span\">"+val+"</span>");
        }else{
            layer.msg("已经添加了",{offset:'300px'});
        }

    }
    function digui(obj,diguiDep) {
        var mythis=$(obj);
        var name=mythis.parent("li").parent("ul").prev("div").text()
        if(name!=''){
            diguiDep.push(name);
            digui(mythis.parent("li").parent("ul").prev("div").prev("div").prev("div"),diguiDep);
        }
    }

    function addValuePost(obj) {
        var diguiPost=[];
        var mythis=$(obj);
        var name=mythis.next().text();
        var temp="";
        digui(currentobj,diguiPost);
        for(var i=diguiPost.length;i>0;i--){
            temp+=diguiPost[i-1]+"-";
        }
        var val=temp+mythis.attr("name")+"-"+name;
        var flag=false;
        for(var i=0;i<arr.length;i++){
            if(arr[i]==val){
                flag=true;
                break;
            }
        }
        if(!flag){
            arr.push(val);
            $("#depOrPos").append("<span name='deleteDepAndPost' class=\"people_span\">"+val+"</span>");
        }else{
            layer.msg("已经添加了",{offset:'300px'});
        }

    }
    function addValuePeople(obj) {
        var mythis=$(obj);
        var val=mythis.parent("li").find("span").eq(0).text()+"("+mythis.parent("li").find("span").eq(1).text()+")";
        //if(obj.checked==true){
        var flag=false;
        for(var i=0;i<peolpearr.length;i++){
            if(peolpearr[i]==val){
                flag=true;
                break;
            }
        }
        if(!flag){
            peolpearr.push(val);
            $("#people").append("<span name='deletePeople' class=\"people_span\">"+val+"</span>");
        }else{
            layer.msg("已经添加了",{offset:'300px'});
        }
    }

    function selDep() {
        var depName=$("#depName").val();
        $("#department").empty();
        for(var i=0;i<tempdep.data.length;i++) {
           if(tempdep.data[i].text.indexOf(depName)>-1){
               var temp="";var isParent="";
               if(!tempdep.data[i].isParent){
                   isParent="false";
                   temp="<div name="+isParent+" onclick='getPosition(this)' id="+tempdep.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 15px;'>"+tempdep.data[i].text+"</div>";
               }else {
                   isParent="true";
                   temp="<div name='add'  style='border: 1px solid #000;cursor: pointer;display: inline-block;width: 10px;height: 10px;background-image: url(\"${ctx}/static/images/add.png\");background-size: cover'></div>"
                       +"<div name="+isParent+" onclick='getPosition(this)' id="+tempdep.data[i].id+" class=\"myli\" style='margin-left: 5px;display: inline-block'>"+tempdep.data[i].text+"</div>";
               }
               //<input onclick='getcheckboxDep(this)' style='width: 16px;height: 16px' type='checkbox'/>
               $("#department").append("<li><div name="+isParent+" onclick='addValue(this)' style='width:20px;height:20px;display: inline-block;position:relative;top:-1px;cursor: pointer'>"+
                   "<img src='${ctx}/static/images/addpic.png' width='100%' height='100%' /></div>&nbsp;&nbsp;&nbsp;"+
                   temp+"</li>");

           }
        }
    }
</script>
</html>
