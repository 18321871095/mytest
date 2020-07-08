<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" scope="session" value="${pageContext.request.contextPath}"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <style>
        .myli{
            cursor: pointer;
        }
        .myli:hover{
            color: red;
        }

    </style>
    <script src="${ctx}/static/js/jquery-2.1.1.min.js"></script>
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

<%--<div style="display: inline-block" id="my" onclick="aa()" class="add"></div>
<div style="display: inline-block" id="my1" onclick="aa()" class="jian"></div>--%>
<div>
    <select id="mySelect" onchange="showDiv()" style="width: 100px;margin: 5px;">
        <option value="1">部门-人员</option>
        <option value="0">角色-人员</option>
        <%--<c:if test="${param.state=='2'}">
            <option value="2">角色</option>
        </c:if>--%>
    </select>
</div>
<div id="depDiv">
    <div style="width: 38%;height: 90%;float: left;overflow-y:auto;">
        <span style="font-size: 15px">部门：</span>
        <input id="selDepName" type="text" placeholder="搜索部门" /><button onclick="selDep(this)">搜索</button>
        <hr style="margin: 2px 0px">
        <ul id="department" style="line-height: 25px;padding: 0px 10px;font-size: 15px">
        </ul>
    </div>
    <div style="width: 18%;height: 90%;float: left;overflow-y:auto;">
        <span style="font-size: 15px">职位：</span><hr style="margin: 5px 0px">
        <ul id="position" style="line-height: 25px;padding: 0px 10px;font-size: 15px">
        </ul>
    </div>
    <div style="width: 43%;height: 90%;float: left;overflow-y:auto;">
        <span style="font-size: 15px">人员：</span><hr style="margin: 5px 0px">
        <ul id="user" style="line-height: 25px;padding: 0px 15px;font-size: 15px;list-style: none;">

        </ul>
    </div>
</div>
<div id="roleDiv" style="display: none;">
    <div style="width: 38%;height: 90%;float: left;overflow-y:auto;">
        <span style="font-size: 15px">角色：</span>
        <input id="role_name1" type="text" placeholder="搜索角色" /><button onclick="selRole1(this)">搜索</button>
        <hr style="margin: 2px 0px">
        <ul id="role" style="line-height: 25px;padding: 0px 10px;font-size: 15px">
        </ul>
    </div>
    <div style="width: 60%;height: 90%;float: left;overflow-y:auto;">
        <span style="font-size: 15px">人员：</span><hr style="margin: 5px 0px">
        <ul id="role_user" style="line-height: 25px;padding: 0px 15px;font-size: 15px;list-style: none;">
        </ul>
    </div>
</div>
<div id="roleDiv1" style="display: none;">
    <div style="width: 99%;height: 90%;float: left;overflow-y:auto;">
        <span style="font-size: 15px">角色：</span>
        <input id="role_name" type="text" placeholder="搜索角色" /><button onclick="selRole(this)">搜索</button>
        <hr style="margin: 5px 0px">
        <ul id="role1" style="line-height: 40px;padding: 0px 10px;font-size: 15px">
        </ul>
    </div>
</div>

</body>
<script>
    var state="${param.state}";
    var roleData,dep;
    var huiqianUser=[]; var groups=[],huiqianUserByRole=[];
    if(state=='2'){
        var temp_huiqian=parent.document.getElementById("huiqian").value.split(",");
        for(var i=0;i<temp_huiqian.length;i++){
            if(temp_huiqian[i]!=''){
                huiqianUser.push(temp_huiqian[i]);
            }
        }
    }else if(state=='3'){
        var temp_groups=parent.document.getElementById("userField").value.split(",");
        for(var i=0;i<temp_groups.length;i++){
            if(temp_groups[i]!=''){
                groups.push(temp_groups[i]);
            }
        }
    }


    $(function () {

        $.ajax({
            type:"get",//http://localhost:8080/webroot/decision/v10/departments/old-platform-department-31
            //{"data":[{"id":"1491023f-8744-4c08-96f2-b3c5c4099567","pId":"old-platform-department-31","text":"人力资源子部门","pText":"","isParent":false,"open":false,"privilegeDetailBeanList":null}]}
            url:"${ctx}/decision/v10/departments/decision-dep-root",
            dataType:'json',
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
            success:function (data) {
                dep=data;
                $("#department").empty();
                for(var i=0;i<data.data.length;i++) {
                    var temp="";
                    if(!data.data[i].isParent){
                        temp="<div onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 15px;'>"+data.data[i].text+"</div>";
                    }else {
                        temp="<div name='add'  style='border: 1px solid #000;cursor: pointer;display: inline-block;width: 10px;height: 10px;background-image: url(\"${ctx}/static/images/add.png\");background-size: cover'></div>"
                            +"<div onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='margin-left: 5px;display: inline-block'>"+data.data[i].text+"</div>";
                    }
                    $("#department").append("<li >"+temp+"</li>");
                }
            },
            error:function (xhr,text) {
                alert(text);
            }
        });

        $.ajax({
            type:"get",
            url:"${ctx}/decision/v10/roles?page=1&count=100000",
            dataType:'json',
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
            success:function (data) {
                var mydata=data.data.items;
                roleData=mydata;
                $("#role").empty();
                for(var i=0;i<mydata.length;i++) {
                    var temp="";
                    temp="<div onclick='getRole_user(this)' id="+mydata[i].id+" class=\"myli\" " +
                        "style='margin-left: 5px;display: inline-block'>"+mydata[i].text+"</div>";
                    $("#role").append("<li >"+temp+"</li>");
                }
                $("#role1").empty();
                $("#role1").append("<input onchange='selAllHuiQianByRole(this)' value=\"\"  type='checkbox' />全选<br/>");
                for(var i=0;i<mydata.length;i++) {
                    $("#role1").append("<input onclick='addHuiQianByRole(this)' type='checkbox' value="+mydata[i].text+" />" + mydata[i].text+"&nbsp;&nbsp;&nbsp;");
                }
            },
            error:function (xhr,text) {
                alert(text);
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
                        var temp="";
                        if(!data.data[i].isParent){
                            temp="<div onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 15px;'>"+data.data[i].text+"</div>";
                        }else {
                            temp="<div name='add'  style='border: 1px solid #000;cursor: pointer;display: inline-block;width: 10px;height: 10px;background-image: url(\"${ctx}/static/images/add.png\");background-size: cover'></div>"
                                +"<div onclick='getPosition(this)' id="+data.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 5px'>"+data.data[i].text+"</div>";
                        }
                        mythis.next().after("<ul style='list-style: none;margin-left: -22px;'><li>"+temp+"</li></ul>");
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

    });

    function getPosition(obj) {
        parentid=obj.id;
        $.ajax({
            type:"get",
            url:"${ctx}/decision/v10/"+obj.id+"/posts",
            dataType:'json',
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
            success:function (data) {
                $("#position").empty();
                $("#user").empty();
                for(var i=0;i<data.data.length;i++) {
                    $("#position").append("<li onclick="+"getUser(this,"+"\""+obj.id+"\""+")" +" id="+data.data[i].id+" class=\"myli\">"
                        +data.data[i].text+"</li>");
                }
            },
            error:function (xhr,text) {
                alert(text);
            }
        });
    }
    function getUser(obj,departmentid) {
        var data =  {"page" :"1","count":"100000"};
        $.ajax({
            type:"POST",
            url:"${ctx}/decision/v10/"+departmentid+"/"+obj.id+"/users",
            dataType:'json',
            data:JSON.stringify(data),
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"
                ,"Content-Type":"application/json", "Accept": "application/json"},
            success:function (data) {
                $("#user").empty();
               if(state==1){
                   for(var i=0;i<data.data.items.length;i++) {
                       $("#user").append("<li onclick='addAssagin(this)' id="+data.data.items[i].username+" class=\"myli\">"
                           +data.data.items[i].username+"   "+data.data.items[i].realName+"</li>");
                   }
               }else if(state==2){
                   var value = parent.document.getElementById("huiqian").value;
                   //会签
                   $("#user").append("<li><input onchange='selAllHuiQian(this,0)' type='checkbox' />全选</li>");

                   for(var i=0;i<data.data.items.length;i++) {
                       if(value.indexOf(data.data.items[i].username)>-1){
                           $("#user").append("<li id="+data.data.items[i].username+" class=\"myli\">"
                               +"<input onchange='addHuiQian(this)' type='checkbox' value="+data.data.items[i].username+" checked />"+data.data.items[i].username+"   "
                               +data.data.items[i].realName+"</li>");
                       }else{
                           $("#user").append("<li id="+data.data.items[i].username+" class=\"myli\">"
                               +"<input onchange='addHuiQian(this)' type='checkbox' value="+data.data.items[i].username+" />"+data.data.items[i].username+"   "
                               +data.data.items[i].realName+"</li>");
                       }

                   }
               }else if(state==3){
                   var value = parent.document.getElementById("userField").value;
                   //组成员
                   $("#user").append("<li><input onchange='selAllGroup(this,0)' type='checkbox' />全选</li>");

                       for(var i=0;i<data.data.items.length;i++) {
                           if(value.indexOf(data.data.items[i].username)>-1) {
                               $("#user").append("<li id=" + data.data.items[i].username + " class=\"myli\">"
                                   + "<input onchange='addGroup(this)' type='checkbox' value="+data.data.items[i].username+" checked />" + data.data.items[i].username + "   " + data.data.items[i].realName + "</li>");
                           }
                           else{
                               $("#user").append("<li id="+data.data.items[i].username+" class=\"myli\">"
                                   +"<input onchange='addGroup(this)' value="+data.data.items[i].username+" type='checkbox'  />"+data.data.items[i].username+"   "+data.data.items[i].realName+"</li>");
                           }
                       }


               }
            },
            error:function (xhr,text) {
                alert(text);
            }
        });
    }
    function addAssagin(obj) {
            parent.document.getElementById("assigneeField").value = obj.id;
    }

    function addGroup(obj) {
        var group=parent.document.getElementById("userField");
        if(obj.checked)
        {
            var myvalue="";
            if(groups.indexOf(obj.parentNode.id)==-1){
                groups.push(obj.parentNode.id);
            }
            for(var i=0;i<groups.length;i++){
                myvalue+=groups[i]+",";
            }
            group.value=myvalue.substr(0, myvalue.length - 1);
        }
        else {
            var myvalue="";
            if(groups.indexOf(obj.parentNode.id)>-1){
                groups.splice(groups.indexOf(obj.parentNode.id),1);
            }
            for(var i=0;i<groups.length;i++){
                myvalue+=groups[i]+",";
            }
            group.value=myvalue.substr(0, myvalue.length - 1);

        }
    }

    function selAllGroup(obj,id) {
        var mythis=$(obj);
        var group=parent.document.getElementById("userField");
        var myvalue="";
        if(mythis.is(":checked")){
            var temp="";
            $("#"+(id==0?"user":"role_user")).find("input[type='checkbox']").each(function () {
                if(typeof ($(this).parent("li").attr("id"))!='undefined'){
                    if(groups.indexOf($(this).parent("li").attr("id"))==-1){
                        groups.push($(this).parent("li").attr("id"));
                    }
                    $(this).prop("checked",true);
                }
            });
            for(var i=0;i<groups.length;i++){
                myvalue+=groups[i]+",";
            }
            group.value=myvalue.substr(0, myvalue.length - 1);
        }else{
            $("#"+(id==0?"user":"role_user")).find("input[type='checkbox']").each(function () {
                $(this).prop("checked",false);
                if(groups.indexOf($(this).val())>-1){
                    groups.splice(groups.indexOf($(this).val()),1);
                }
                /*for(var j=0;j<huiqianUser.length;j++){
                    if($(this).val()==huiqianUser[j]){
                        huiqianUser.splice(j,1);
                    }
                }*/
            });
            for(var ii=0;ii<groups.length;ii++){
                myvalue+=groups[ii]+",";
            }

            group.value=myvalue.substr(0, myvalue.length - 1);
        }

    }

    function aa() {
        $("#my").removeClass("add::after ")
    }

    function showDiv() {
        var name=$("#mySelect").val();
        if(name=='1'){
            $("#depDiv").show();
            $("#roleDiv").hide();
            $("#roleDiv1").hide();
            if(state==2){
                huiqianUser=[];  groups=[];
                parent.document.getElementById("huiqian").value="";
                $("#user").find("input[type='checkbox']").prop("checked",false);
                $("#role_user").find("input[type='checkbox']").prop("checked",false);
            }else if(state==3){
                huiqianUser=[];  groups=[];
                parent.document.getElementById("userField").value="";
                $("#user").find("input[type='checkbox']").prop("checked",false);
                $("#role_user").find("input[type='checkbox']").prop("checked",false);
            }
        }else if(name=='0'){
            $("#roleDiv").show();
            $("#depDiv").hide();
            $("#roleDiv1").hide();
            if(state==2){
                huiqianUser=[];  groups=[];
                parent.document.getElementById("huiqian").value="";
                $("#user").find("input[type='checkbox']").prop("checked",false);
                $("#role_user").find("input[type='checkbox']").prop("checked",false);
            }else if(state==3){
                huiqianUser=[];  groups=[];
                parent.document.getElementById("userField").value="";
                $("#user").find("input[type='checkbox']").prop("checked",false);
                $("#role_user").find("input[type='checkbox']").prop("checked",false);
            }
        }else{
            $("#roleDiv").hide();
            $("#depDiv").hide();
            $("#roleDiv1").show();

        }
    }

    function getRole_user(obj) {
        var data =  {"page" :"1","count":"100000"};
        $.ajax({
            type:"post",
            url:"${ctx}/decision/v10/"+obj.id+"/users",
            dataType:'json',
            contentType: "application/json",
            data:JSON.stringify(data),
            headers:{"Authorization":"Bearer "+"<%=Authorization %>"},
            success:function (data) {
                var mydata=data.data.items;
                $("#role_user").empty();
                    if(state==1){
                        for(var i=0;i<mydata.length;i++) {
                            $("#role_user").append("<li onclick='addAssagin(this)' id="+mydata[i].username+" class=\"myli\">"
                                +mydata[i].username+"   "+mydata[i].realName+"</li>");
                        }
                    }
                    else if(state==2){
                        var value = parent.document.getElementById("huiqian").value;
                        //会签
                        $("#role_user").append("<li><input onchange='selAllHuiQian(this,1)' type='checkbox' />全选</li>");
                        for(var i=0;i<mydata.length;i++) {
                            if(value.indexOf(mydata[i].username)>-1){
                                $("#role_user").append("<li id="+mydata[i].username+" class=\"myli\">"
                                    +"<input onchange='addHuiQian(this)' value="+mydata[i].username+" type='checkbox' checked />"+mydata[i].username+"   "
                                    +mydata[i].realName+"</li>");
                            }else{
                                $("#role_user").append("<li id="+mydata[i].username+" class=\"myli\">"
                                    +"<input onchange='addHuiQian(this)' value="+mydata[i].username+" type='checkbox' />"+mydata[i].username+"   "
                                    +mydata[i].realName+"</li>");
                            }

                        }
                    }
                    else if(state==3){
                        var value = parent.document.getElementById("userField").value;
                        //组成员
                        $("#role_user").append("<li><input onchange='selAllGroup(this,1)' type='checkbox' />全选</li>");

                            for(var i=0;i<mydata.length;i++) {
                                if(value.indexOf(mydata[i].username)>-1) {
                                    $("#role_user").append("<li id=" + mydata[i].username + " class=\"myli\">"
                                        + "<input onchange='addGroup(this)' value="+mydata[i].username+" type='checkbox' checked  />" + mydata[i].username + "   "
                                        + mydata[i].realName + "</li>");
                                }
                                else{
                                    $("#role_user").append("<li id="+mydata[i].username+" class=\"myli\">"
                                        +"<input onchange='addGroup(this)' value="+mydata[i].username+" type='checkbox'  />"+mydata[i].username+"   "
                                        +mydata[i].realName+"</li>");
                                }
                            }


                    }

            },
            error:function (xhr,text) {
                alert(text);
            }
        });
    }

    function addHuiQian(obj) {
        var huiqian=parent.document.getElementById("huiqian");
        if(obj.checked)
        {
            var myvalue="";
            if(huiqianUser.indexOf(obj.parentNode.id)==-1){
                huiqianUser.push(obj.parentNode.id);
            }
            for(var i=0;i<huiqianUser.length;i++){
                myvalue+=huiqianUser[i]+",";
            }
            huiqian.value=myvalue.substr(0, myvalue.length - 1);
        }
        else {
            var myvalue="";
            if(huiqianUser.indexOf(obj.parentNode.id)>-1){
                huiqianUser.splice(huiqianUser.indexOf(obj.parentNode.id),1);
            }
            for(var i=0;i<huiqianUser.length;i++){
                myvalue+=huiqianUser[i]+",";
            }
            huiqian.value=myvalue.substr(0, myvalue.length - 1);

        }

    }

    function selAllHuiQian(obj,id) {
        var mythis=$(obj);
        var huiqian=parent.document.getElementById("huiqian");
        var myvalue="";
        if(mythis.is(":checked")){
            var temp="";
            $("#"+(id==0?"user":"role_user")).find("input[type='checkbox']").each(function () {
                if(typeof ($(this).parent("li").attr("id"))!='undefined'){
                    if(huiqianUser.indexOf($(this).parent("li").attr("id"))==-1){
                        huiqianUser.push($(this).parent("li").attr("id"));
                    }
                    $(this).prop("checked",true);
                }
            });
            for(var i=0;i<huiqianUser.length;i++){
                myvalue+=huiqianUser[i]+",";
            }
            huiqian.value=myvalue.substr(0, myvalue.length - 1);
        }else{
            $("#"+(id==0?"user":"role_user")).find("input[type='checkbox']").each(function () {
                $(this).prop("checked",false);
                if(huiqianUser.indexOf($(this).val())>-1){
                    huiqianUser.splice(huiqianUser.indexOf($(this).val()),1);
                }
                /*for(var j=0;j<huiqianUser.length;j++){
                    if($(this).val()==huiqianUser[j]){
                        huiqianUser.splice(j,1);
                    }
                }*/
            });
            for(var ii=0;ii<huiqianUser.length;ii++){
                myvalue+=huiqianUser[ii]+",";
            }

            huiqian.value=myvalue.substr(0, myvalue.length - 1);
        }

    }

    function addHuiQianByRole(obj) {
        var huiqian=parent.document.getElementById("huiqian");
        if(obj.checked)
        {
            var myvalue="";
            if(huiqianUserByRole.indexOf(obj.value)==-1){
                huiqianUserByRole.push(obj.value);
            }
            for(var i=0;i<huiqianUserByRole.length;i++){
                myvalue+=huiqianUserByRole[i]+"/";
            }
            huiqian.value=myvalue.substr(0, myvalue.length - 1);
        }
        else {
            var myvalue="";
            if(huiqianUserByRole.indexOf(obj.value)>-1){
                huiqianUserByRole.splice(huiqianUserByRole.indexOf(obj.value),1);
            }
            for(var i=0;i<huiqianUserByRole.length;i++){
                myvalue+=huiqianUserByRole[i]+"/";
            }
            huiqian.value=myvalue.substr(0, myvalue.length - 1);

        }

    }

    function selAllHuiQianByRole(obj) {
        var mythis=$(obj);
        var huiqian=parent.document.getElementById("huiqian");
        var myvalue="";
        if(mythis.is(":checked")) {
            $("#role1").find("input[type='checkbox']").each(function () {
                if($(this).val()!=''){
                    if(huiqianUserByRole.indexOf($(this).val())==-1){
                        huiqianUserByRole.push($(this).val());
                    }
                    $(this).prop("checked",true);
                }
            });
            for(var i=0;i<huiqianUserByRole.length;i++){
                myvalue+=huiqianUserByRole[i]+"/";
            }
            huiqian.value=myvalue.substr(0, myvalue.length - 1);
        }
        else{
            $("#role1").find("input[type='checkbox']").each(function () {
                $(this).prop("checked",false);
                if(huiqianUserByRole.indexOf($(this).val())>-1){
                    huiqianUserByRole.splice(huiqianUserByRole.indexOf($(this).val()),1);
                }
            });
            for(var ii=0;ii<huiqianUserByRole.length;ii++){
                myvalue+=huiqianUserByRole[ii]+",";
            }
            huiqian.value=myvalue.substr(0, myvalue.length - 1);
        }
    }

    function selRole(obj) {
        var huiqian=parent.document.getElementById("huiqian").value;
        var name=$("#role_name").val();
        $("#role1").empty();
        $("#role1").append("<input onchange='selAllHuiQianByRole(this)'  type='checkbox' />全选<br/>");
        for(var i=0;i<roleData.length;i++) {
            var aa=roleData[i].text;
           if(aa.indexOf(name)>-1){
               if(huiqian.indexOf(roleData[i].text)>-1){
                   $("#role1").append("<input onclick='addHuiQianByRole(this)' type='checkbox' value="+roleData[i].text+" checked  />" +
                       roleData[i].text+"&nbsp;&nbsp;&nbsp;");
               }else{
                   $("#role1").append("<input onclick='addHuiQianByRole(this)' type='checkbox' value="+roleData[i].text+"  />" +
                       roleData[i].text+"&nbsp;&nbsp;&nbsp;");
               }
           }
        }
    }
    function selRole1(obj) {
        var name=$("#role_name1").val();
        $("#role").empty();
        for(var i=0;i<roleData.length;i++) {
            var aa=roleData[i].text;
            if(aa.indexOf(name)>-1){
                var temp="";
                temp="<div onclick='getRole_user(this)' id="+roleData[i].id+" class=\"myli\" " +
                    "style='margin-left: 5px;display: inline-block'>"+roleData[i].text+"</div>";
                $("#role").append("<li >"+temp+"</li>");
            }
        }
    }

    function selDep(obj) {
        var name=$("#selDepName").val();
        $("#department").empty();
        console.log(dep)
        for(var i=0;i<dep.data.length;i++) {
            var aa=dep.data[i].text;
            if(aa.indexOf(name)>-1){
                var temp="";
                if(!dep.data[i].isParent){
                    temp="<div onclick='getPosition(this)' id="+dep.data[i].id+" class=\"myli\" style='display: inline-block;margin-left: 15px;'>"+dep.data[i].text+"</div>";
                }else {
                    temp="<div name='add'  style='border: 1px solid #000;cursor: pointer;display: inline-block;width: 10px;height: 10px;background-image: url(\"${ctx}/static/images/add.png\");background-size: cover'></div>"
                        +"<div onclick='getPosition(this)' id="+dep.data[i].id+" class=\"myli\" style='margin-left: 5px;display: inline-block'>"+dep.data[i].text+"</div>";
                }
                $("#department").append("<li >"+temp+"</li>");
            }
        }

    }
</script>
</html>
