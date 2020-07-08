package com.fr.tw.process;

import com.fr.decision.authority.data.User;
import com.fr.decision.webservice.bean.user.UserDetailInfoBean;
import com.fr.decision.webservice.v10.login.LoginService;
import com.fr.decision.webservice.v10.user.UserService;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.parser.FRLexer;
import com.fr.parser.FRParser;
import com.fr.plugin.chart.gantt.data.chartdata.Process;
import com.fr.stable.script.Expression;
import com.fr.tw.test.backtest;
import com.fr.tw.test.task;
import com.fr.tw.util.*;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.json.HTTP;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.*;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static oracle.net.aso.C01.i;
import static oracle.net.aso.C01.s;

@Controller
@RequestMapping("/processInfo")
public class processInfo {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ManagementService managerService;


   @RequestMapping("/backtest")// duedatedefinitionpackage
    @ResponseBody
    public String backtest( ) throws Exception {
       Task task = taskService.createTaskQuery().taskId("582551").singleResult();
       ReadOnlyProcessDefinition processDefinitionEntity = (ReadOnlyProcessDefinition) repositoryService.
               getProcessDefinition(task.getProcessDefinitionId());

       ActivityImpl destinationActivity = (ActivityImpl) processDefinitionEntity.findActivity("sid-B44F329C-D5DE-41CB-8D6F-59BF03A4E159");
       ActivityImpl currentActivity = (ActivityImpl) processDefinitionEntity.findActivity(task.getTaskDefinitionKey());
           managerService.executeCommand(new backtest(task.getExecutionId(), task.getProcessInstanceId(),
                   destinationActivity, null, currentActivity));

       return "1";

    }


    @RequestMapping("/jiaoyan")
    @ResponseBody
    public String jiaoyan( String mytext, HttpServletRequest request){
        try {
           // String data1 = request.getParameter("text");
            java.io.StringReader in =new java.io.StringReader(mytext);
            FRLexer frLexer=new FRLexer(in);
            FRParser frParser=new FRParser(frLexer);
            Expression parse = frParser.parse();
            return "1";

        }
        catch (Exception e){
            return "0";
        }
    }



    @RequestMapping("/getcontrol")
    @ResponseBody
    public JSONResult getcontrol(HttpServletRequest request,String num) throws Exception {
        JSONResult jr=new JSONResult();
        Integer yeshu=Integer.valueOf(num);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List<HistoricProcessInstance> list_zong =new ArrayList<>();
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            if(ProcessUtils.isAdmin(currentUserNameFromRequestCookie)){
                list_zong = historyService.createHistoricProcessInstanceQuery().
                        orderByProcessInstanceStartTime().desc().list();
             }else{
                list_zong = historyService.createHistoricProcessInstanceQuery().
                        processInstanceTenantId(currentUserNameFromRequestCookie).
                        orderByProcessInstanceStartTime().processInstanceTenantId(currentUserNameFromRequestCookie)
                        .desc().list();
            }
                List<HistoricProcessInstance> list = ProcessUtils.getHistoricProcessInstanceByYeShu(list_zong, yeshu);
                for(int i=0;i<list.size();i++){
                    String process_formKey="";
                    String username="";
                    String dep="";
                    Map<String, Object> map = new HashMap<>();
                    map.put("proname", list.get(i).getName());
                    map.put("processDefinitionID", list.get(i).getProcessDefinitionId());
                    map.put("processInstanceId", list.get(i).getId());
                    map.put("requestid",list.get(i).getBusinessKey());
                    map.put("startTime",sdf.format(list.get(i).getStartTime()));
                    Date endTime = list.get(i).getEndTime();
                    if(endTime==null){
                        map.put("endTime", "");
                        Map<String, VariableInstance> mapVariable = runtimeService.getVariableInstances(list.get(i).getId());
                        String process_state = mapVariable.get("process_state").getTextValue();
                        process_formKey = mapVariable.get("process_formKey").getTextValue();
                        username=mapVariable.get("process_userRealName").getTextValue();
                        map.put("status", process_state);

                    }else{
                        map.put("status", "6");
                        map.put("endTime", sdf.format(list.get(i).getEndTime()));
                        List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().
                                processInstanceId(list.get(i).getId()).list();
                        for(HistoricVariableInstance h:hisVarList){
                            if("process_formKey".equals(h.getVariableName())){
                                process_formKey=h.getValue().toString();
                            }
                            if("process_userRealName".equals(h.getVariableName())){
                                username=h.getValue().toString();
                            }
                        }

                    }

                    map.put("reportName",process_formKey);
                    map.put("startRealName",username);
                    List<String> deps = ProcessUtils.getDepsAndPostByUserName(list.get(i).getStartUserId());
                    for(int j=0;j<deps.size();j++){
                        dep+=deps.get(j)+",";
                    }
                    map.put("dep","".equals(dep) ? dep : dep.substring(0,dep.length()-1));
                    result.add(map);
                }

                jr.setMsg("success");
                jr.setResult(result);
                jr.setTotal(list_zong.size());
                if(list_zong.size()<=10){
                    jr.setYeshu(1);
                }else{
                    jr.setYeshu(list_zong.size()%10==0 ? list_zong.size()/10 : list_zong.size()/10+1);
                }
            return jr;
        }
        catch (Exception e){
            jr.setMsg("err");
            jr.setResult(e.getMessage());
            return jr;
        }
    }

    @RequestMapping("/getcontrol1")
    @ResponseBody
    public JSONResult getcontrol1(HttpServletRequest request,String num,
                                 String name,String depName,String time,String status) throws Exception {
        JSONResult jr=new JSONResult();
        Integer yeshu=Integer.valueOf(num);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List<HistoricProcessInstance> list =new ArrayList<>();
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            if(ProcessUtils.isAdmin(currentUserNameFromRequestCookie)){
                list = historyService.createHistoricProcessInstanceQuery().
                        orderByProcessInstanceStartTime().desc().list();
            }else{
                list = historyService.createHistoricProcessInstanceQuery().
                        processInstanceTenantId(currentUserNameFromRequestCookie).
                        orderByProcessInstanceStartTime().processInstanceTenantId(currentUserNameFromRequestCookie)
                        .desc().list();
            }
            for(int i=0;i<list.size();i++){
                String process_formKey="";
                String username="";
                String dep="";
                Map<String, Object> map = new HashMap<>();
                map.put("proname", list.get(i).getName());
                map.put("processDefinitionID", list.get(i).getProcessDefinitionId());
                map.put("processInstanceId", list.get(i).getId());
                map.put("requestid",list.get(i).getBusinessKey());
                map.put("startTime",sdf.format(list.get(i).getStartTime()));
                Date endTime = list.get(i).getEndTime();
                if(endTime==null){
                    map.put("endTime", "");
                    Map<String, VariableInstance> mapVariable = runtimeService.getVariableInstances(list.get(i).getId());
                    String process_state = mapVariable.get("process_state").getTextValue();
                    process_formKey = mapVariable.get("process_formKey").getTextValue();
                    username=mapVariable.get("process_userRealName").getTextValue();
                    map.put("status", process_state);

                }else{
                    map.put("status", "6");
                    map.put("endTime", sdf.format(list.get(i).getEndTime()));
                    List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().
                            processInstanceId(list.get(i).getId()).list();
                    for(HistoricVariableInstance h:hisVarList){
                        if("process_formKey".equals(h.getVariableName())){
                            process_formKey=h.getValue().toString();
                        }
                        if("process_userRealName".equals(h.getVariableName())){
                            username=h.getValue().toString();
                        }
                    }

                }

                map.put("reportName",process_formKey);
                map.put("startRealName",username);
                List<String> deps = ProcessUtils.getDepsAndPostByUserName(list.get(i).getStartUserId());
                for(int j=0;j<deps.size();j++){
                    dep+=deps.get(j)+",";
                }
                map.put("dep","".equals(dep) ? dep : dep.substring(0,dep.length()-1));
                result.add(map);
            }
            List<Map<String, Object>> result1 = new ArrayList<>();
            //条件查询
            if((name!=null&&!"".equals(name))||(depName!=null&&!"".equals(depName))||(time!=null&&!"".equals(time))
                     || (status!=null&&!"".equals(status)) ){
                    if(name!=null&&!"".equals(name)){
                        for(int k=0;k<result.size();k++) {
                            String s = result.get(k).get("startRealName").toString();
                            if (s.contains(name)) {
                                if(!result1.contains(result.get(k))){
                                    result1.add(result.get(k));
                                }
                            }
                        }
                        result=result1;
                        result1 = new ArrayList<>();
                    }
                    if(depName!=null&&!"".equals(depName)){
                        for(int k1=0;k1<result.size();k1++) {
                            String s1 = result.get(k1).get("dep").toString();
                            if (s1.contains(depName)) {
                                if(!result1.contains(result.get(k1))){
                                    result1.add(result.get(k1));
                                }
                            }
                        }
                        result=result1;
                        result1 = new ArrayList<>();
                    }
                    if(time!=null&&!"".equals(time)){
                        for(int k2=0;k2<result.size();k2++) {
                            Date startTime = sdf.parse(result.get(k2).get("startTime").toString());
                            int mymonth = startTime.getMonth() + 1;
                            int myyear=startTime.getYear()+1900;
                            String s_year = time.split("-")[0];
                            String s_month = time.split("-")[1];
                            if(Integer.valueOf(s_year)==myyear && Integer.valueOf(s_month)==mymonth){
                                if(!result1.contains(result.get(k2))){
                                    result1.add(result.get(k2));
                                }
                            }
                        }
                        result=result1;
                        result1 = new ArrayList<>();
                    }
                if(status!=null&&!"".equals(status)){
                        if("1".equals(status)){//完成
                            for(int k3=0;k3<result.size();k3++) {
                                String s11 = result.get(k3).get("status").toString();
                                if("6".equals(s11) || "9".equals(s11)){
                                    if(!result1.contains(result.get(k3))){
                                        result1.add(result.get(k3));
                                    }
                                }
                            }
                            result=result1;
                            result1 = new ArrayList<>();
                        }else  if("0".equals(status)){//未完成
                            for(int k31=0;k31<result.size();k31++) {
                                String s22 = result.get(k31).get("status").toString();
                                if(!"6".equals(s22) && !"9".equals(s22)){
                                    if(!result1.contains(result.get(k31))){
                                        result1.add(result.get(k31));
                                    }
                                }
                            }
                            result=result1;
                            result1 = new ArrayList<>();
                        }
                }

            }
            List<Map<String, Object>> resultByYeShu = ProcessUtils.getResultByYeShu(result, yeshu);
            jr.setMsg("success");
            jr.setResult(resultByYeShu);

            jr.setTotal(result.size());
            if(result.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(result.size()%10==0 ? result.size()/10 : result.size()/10+1);
            }
            return jr;
        }
        catch (Exception e){
            jr.setMsg("err");
            jr.setResult(e.getMessage());
            return jr;
        }
    }



    @RequestMapping("/shanchubaocunquanxian")
    @ResponseBody
    public JSONResult shanchubaocunquanxian(String procdefid){
        JSONResult jr=new JSONResult();
        int update = jdbcTemplate.update("DELETE FROM act_authority WHERE procdefid=?", new Object[]{procdefid});
        if(update>0){
            jr.setResult("1");
        }else {
            jr.setResult("0");
        }
        return  jr;
    }


    @RequestMapping("/getReserveAuthority")
    @ResponseBody
    public JSONResult getReserveAuthority(){
        JSONResult jr = new JSONResult();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM act_authority");
            Set<String> set = new HashSet<>();
            for (Map<String, Object> map : list) {
                set.add(map.get("procdefid").toString());
            }
            List<Map<String, Object>> list1 = new ArrayList<>();
            for (String s : set) {
                Map<String, Object> map = new HashMap<>();
                String groupid = "";
                String userid = "";
                String name = "";
                String procdefid="";
                for (Map<String, Object> map1 : list) {
                    if (s.equals(map1.get("procdefid")==null?"":map1.get("procdefid").toString())) {
                        name = map1.get("name")==null?"":map1.get("name").toString();
                        procdefid=map1.get("procdefid")==null?"":map1.get("procdefid").toString();
                        if(!"".equals(map1.get("groupid")==null?"":map1.get("groupid").toString())){
                            groupid += map1.get("groupid")==null?"": map1.get("groupid").toString()+ ",";
                        }
                       if(!"".equals( map1.get("userid")==null?"": map1.get("userid").toString())){
                           userid += map1.get("userid")==null?"":map1.get("userid").toString() + ",";
                       }
                    }
                }
                map.put("name", name);
                map.put("procdefid", procdefid);
                map.put("groupid", groupid.substring(0,groupid.length()==0?0:groupid.length()-1));
                map.put("userid", userid.substring(0,userid.length()==0?0:userid.length()-1));
                list1.add(map);
            }
            jr.setResult(list1);
            jr.setMsg("success");
            return jr;
        }
        catch (Exception e){
            jr.setMsg("err");
            jr.setResult(e.getMessage());
            return jr;
        }
    }

    @RequestMapping(value = "/baocunAuthority")
    @ResponseBody
    public JSONResult baocunAuthority(String procdefid,String name) {
        JSONResult jr=new JSONResult();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM act_authority WHERE name=?", new Object[]{name});
            if(list.size()==0){
                List<IdentityLink> identityLinks= repositoryService.getIdentityLinksForProcessDefinition(procdefid);
                if (identityLinks.size() > 0) {
                    jdbcTemplate.update("DELETE FROM act_authority WHERE procdefid=?",new Object[]{procdefid});
                    for (IdentityLink link : identityLinks) {
                        if (StringUtils.isNotBlank(link.getGroupId())) {
                            jdbcTemplate.update("INSERT INTO act_authority(procdefid, groupid, userid,name) VALUES (?,?,?,?)",
                                    new Object[]{procdefid, link.getGroupId(), "",name});
                        }
                        if (StringUtils.isNotBlank(link.getUserId())) {
                            jdbcTemplate.update("INSERT INTO act_authority(procdefid, groupid, userid,name) VALUES (?,?,?,?)",
                                    new Object[]{procdefid, "", link.getUserId(),name});
                        }
                    }
                }
                jr.setMsg("success");
            }else{
                jr.setMsg("001");
            }

            return jr;
        }
        catch (Exception e){
            jr.setMsg("err");
            jr.setMsg(e.getMessage());
            return jr;
        }
    }

    @RequestMapping(value = "/daoruAuthority")
    @ResponseBody
    public JSONResult daoruAuthority(String arr,String prodefinedid,HttpServletRequest request)  {
        JSONResult jr=new JSONResult();
        try{
                List<IdentityLink> identityLinks= repositoryService.getIdentityLinksForProcessDefinition(prodefinedid);
                for(IdentityLink link :identityLinks){
                    if(StringUtils.isNotBlank(link.getUserId())){
                        repositoryService.deleteCandidateStarterUser(prodefinedid,link.getUserId());
                    }
                    if(StringUtils.isNotBlank(link.getGroupId())){
                        repositoryService.deleteCandidateStarterGroup(prodefinedid,link.getGroupId());
                    }
                }
                org.json.JSONObject json=new org.json.JSONObject(arr);
                String[] dep = json.getString("dep").split(",");
                String[] people = json.getString("people").split(",");
                for(String d:dep){
                    if(!"".equals(d)){
                        repositoryService.addCandidateStarterGroup(prodefinedid,d);
                    }
                }
                for(String p:people){
                    if(!"".equals(p)) {
                        repositoryService.addCandidateStarterUser(prodefinedid, p);
                    }
                }
                jr.setMsg("success");
                jr.setResult("success");

        }
        catch (Exception e){
            jr.setMsg(e.getMessage());
            jr.setResult("err");
        }
        return  jr;

    }


   /* @RequestMapping("/isadmin")
    public void isadmin() throws Exception {
        User admin = UserService.getInstance().getUserByUserName("Mike");
        System.out.println("adminId:"+admin.getId());
        System.out.println(UserService.getInstance().isAdmin(admin.getId()));
    }*/

    //点击申请流程时候判断
    @RequestMapping("/authority")
    public String authority(String processDefinitionID,String depid,String proname,String proNameParam,HttpServletRequest request) throws Exception {
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            List<IdentityLink> identityLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinitionID);
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionID).singleResult();
            if (identityLinks.size() > 0) {
                if (ProcessUtils.isAdmin(userName) || userName.equals(processDefinition.getTenantId())) {
                    //判断流程是否解除发布了
                    if (ProcessUtils.isFaBu(depid, repositoryService)) {
                        request.setAttribute("depid", depid);
                        request.setAttribute("proname", proname);
                        request.setAttribute("processDefinitionID", processDefinitionID);
                        request.setAttribute("proNameParam", proNameParam);
                        return "/static/jsp/frontEnd/application.jsp";
                    } else {
                        return "/static/jsp/message.jsp?message="+
                                URLEncoder.encode("该流程处于解除发布状态，无法新建", "utf-8");
                    }

                } else {
                    List<String> deps = ProcessUtils.getDepsAndPostByUserName(userName);
                    if (ProcessUtils.getAuthority(identityLinks, userName, deps) || userName.equals(processDefinition.getTenantId())) {
                        if (ProcessUtils.isFaBu(depid, repositoryService)) {
                            request.setAttribute("depid", depid);
                            request.setAttribute("proname", proname);
                            request.setAttribute("processDefinitionID", processDefinitionID);
                            request.setAttribute("proNameParam", proNameParam);
                            return "/static/jsp/frontEnd/application.jsp";
                        } else {
                            return "/static/jsp/message.jsp?message="+
                                    URLEncoder.encode("该流程处于解除发布状态，无法新建", "utf-8");
                        }

                    } else {
                        return "/static/jsp/message.jsp?message=" +  URLEncoder.encode("您没有权限启动该流程", "utf-8");

                    }

                }

            } else {
                if (ProcessUtils.isAdmin(userName) || userName.equals(processDefinition.getTenantId())) {
                    if (ProcessUtils.isFaBu(depid, repositoryService)) {
                        request.setAttribute("depid", depid);
                        request.setAttribute("proname", proname);
                        request.setAttribute("processDefinitionID", processDefinitionID);
                        request.setAttribute("proNameParam", proNameParam);
                        return "/static/jsp/frontEnd/application.jsp";
                    } else {
                        return "/static/jsp/message.jsp?message="+
                                URLEncoder.encode("该流程处于解除发布状态，无法新建", "utf-8");
                    }

                } else {
                    return "/static/jsp/message.jsp?message=" +  URLEncoder.encode("您没有权限启动该流程", "utf-8");
                }

            }
        }catch (Exception e){
            if(e instanceof ActivitiObjectNotFoundException){
                return "/static/jsp/message.jsp?message=" + URLEncoder.encode("该流程已更新请刷新新建页面", "utf-8");
            }else{
                return "/static/jsp/message.jsp?message=" +  URLEncoder.encode(e.getMessage(), "utf-8");
            }
        }

    }



    @RequestMapping(value = "/test1")
    @ResponseBody
    public String test1() throws Exception {
        //[{jobTitle=部长, departments=人力资源}, {jobTitle=部长, departments=开发}]
        UserService instance = UserService.getInstance();
        User qweqwewqewqe = instance.getUserByUserName("Li12312321ly");
        System.out.println(qweqwewqewqe);
        //System.out.println(qweqwewqewqe.getRealName());
        return "";

    }

   //设置权限
    @RequestMapping("/setProAuthoritys")
    @ResponseBody
    public JSONResult setProAuthoritys(String arr,String prodefinedid,HttpServletRequest request) throws Exception {
       //人力资源，开发，领导部
        JSONResult jr=new JSONResult();
        try{
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(prodefinedid).singleResult();
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            if(processDefinition.getTenantId().equals(currentUserNameFromRequestCookie) || ProcessUtils.isAdmin(currentUserNameFromRequestCookie) ){
                List<IdentityLink> identityLinks= repositoryService.getIdentityLinksForProcessDefinition(prodefinedid);
                for(IdentityLink link :identityLinks){
                    //isNotEmpty(str)等价于 str != null && str.length > 0
                    // isNotBlank(str) 等价于 str != null && str.length > 0 && str.trim().length > 0
                    if(StringUtils.isNotBlank(link.getUserId())){
                        repositoryService.deleteCandidateStarterUser(prodefinedid,link.getUserId());
                    }
                    if(StringUtils.isNotBlank(link.getGroupId())){
                        repositoryService.deleteCandidateStarterGroup(prodefinedid,link.getGroupId());
                    }
                }
                org.json.JSONObject json=new org.json.JSONObject(arr);
                String[] dep = json.getString("dep").split(",");
                String[] people = json.getString("people").split(",");
                for(String d:dep){
                    if(!"".equals(d)){
                        repositoryService.addCandidateStarterGroup(prodefinedid,d);
                    }
                }
                for(String p:people){
                    if(!"".equals(p)) {
                        repositoryService.addCandidateStarterUser(prodefinedid, p);
                    }
                }
                jr.setMsg("success");
                jr.setResult("success");
            }else{
                jr.setMsg("err");
                jr.setResult("该流程你没有权限");
            }

        }
        catch (Exception e){
            jr.setMsg(e.getMessage());
            jr.setResult("err");
        }
        return  jr;

    }

    //获取权限
    @RequestMapping("/getProAuthoritys")
    @ResponseBody
    public JSONResult getProAuthoritys(String prodefinedid) throws Exception {
        JSONResult jr=new JSONResult();
        Map<String,Object> map=new HashMap<String,Object>();
        StringBuffer dep_sb=new StringBuffer();
        StringBuffer people_sb=new StringBuffer();
        try{
            List<IdentityLink> identityLinks= repositoryService.getIdentityLinksForProcessDefinition(prodefinedid);
            if(identityLinks.size()>0) {
                for (IdentityLink link : identityLinks) {
                    //isNotEmpty(str)等价于 str != null && str.length > 0
                    // isNotBlank(str) 等价于 str != null && str.length > 0 && str.trim().length > 0
                    if (StringUtils.isNotBlank(link.getGroupId())) {
                        dep_sb.append(link.getGroupId() + ",");
                    }
                    if (StringUtils.isNotBlank(link.getUserId())) {
                        people_sb.append(link.getUserId() + ",");
                    }
                }
                map.put("dep",  dep_sb.length()>0 ? dep_sb.deleteCharAt(dep_sb.length() - 1).toString() : "");
                map.put("people", people_sb.length()>0 ? people_sb.deleteCharAt(people_sb.length() - 1).toString() : "");
                jr.setResult(map);
                jr.setMsg("success");
            }else {
                map.put("dep","");
                map.put("people","");
                jr.setResult(map);
                jr.setMsg("success");
            }
        }
        catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("err");
        }
        return  jr;
    }



    /*开始节点的流程撤回*/
    @RequestMapping(value = "/chehui")
    @ResponseBody
    @Transactional
    public JSONResult chehui(String processDefinitionID,String proInstanceId,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        try {
            String userName =LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
           if(proInstanceId==null || "".equals(proInstanceId) || "".equals(processDefinitionID) || processDefinitionID==null){
               jr.setResult("0");
               jr.setMsg("流程实例ID或流程定义ID为空");
           }else{
               List<Task> list = taskService.createTaskQuery().processInstanceId(proInstanceId).list();
               Task task = list.get(0);

               String applicationActivitiId = ProcessUtils.getApplicationActivitiId(processDefinitionID, repositoryService);

               ActivityImpl destinationActivity = ProcessUtils.getActivityImplByActivitiId(applicationActivitiId,task.getProcessDefinitionId(),repositoryService);

               ActivityImpl currentActivity = ProcessUtils.getActivityImplByActivitiId(task.getTaskDefinitionKey(),task.getProcessDefinitionId(),repositoryService);

               if (ProcessUtils.isHuiQianNode(task.getTaskDefinitionKey(),processDefinitionID,repositoryService)) {
                   //申请下一个结点为会签节点
                   managerService.executeCommand(new ShareniuMultiInstanceJumpCmd(task.getExecutionId(), task.getProcessInstanceId(),
                           destinationActivity, null, currentActivity));
               } else if (ProcessUtils.isParallelGatewayByCheHui( task.getTaskDefinitionKey(),task.getProcessDefinitionId(),repositoryService)) {
                   //申请下一个结点为并行节点
                   managerService.executeCommand(new ShareniuParallelJumpCmd(task.getExecutionId(), task.getProcessInstanceId(),
                           destinationActivity, null, currentActivity));
               } else {
                   managerService.executeCommand(new ShareniuCommonJumpTaskCmd(task.getExecutionId(), task.getProcessInstanceId(),
                           destinationActivity, null, currentActivity));
               }
               //标记流程为撤回
               runtimeService.setVariable(task.getProcessInstanceId(),"process_state","4");
               //保存操作信息
               jdbcTemplate.update("INSERT INTO proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,attachment) VALUES(?,?,?,?,?,?,?,?,?,?)",
                       new Object[]{ ProcessUtils.getUUID(),proInstanceId,task.getId(),userName,userRealName,new Date(),4,task.getName(),"",""});
               jr.setMsg("success");
               jr.setResult("success");
           }
        }
        catch (Exception e){
            jr.setResult("fail");
            jr.setMsg(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }

    /*开始节点的流程删除*/
    @RequestMapping(value = "/deleteSelfProcess")
    @ResponseBody
    @Transactional
    public JSONResult deleteSelfProcess(String proInstanceId,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        try {
            String userName =LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
            String taskid="";  String taskName="";
            List<Task> task = taskService.createTaskQuery().processInstanceId(proInstanceId).list();
            if(task.size()>0){
                taskid=task.get(0).getId();
                taskName=task.get(0).getName();
            }
            // 0：申请人提交可撤回状态  1：通过 2：查看 3：被退回（不包括申请节点） 4：撤回 5：转办 6:完成 7：删除 8：被退回到申请节点
            runtimeService.setVariable(proInstanceId,"process_state","7");
            runtimeService.deleteProcessInstance(proInstanceId,"selfDelete");
            //保存操作信息
            // 1：申请人提交 2：申请人保存 3：驳回 4：撤回 5：转办 6：删除流程 7:办理人通过 8：办理人保存
            jdbcTemplate.update("INSERT INTO proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,attachment) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{ ProcessUtils.getUUID(),proInstanceId,taskid,userName,userRealName,new Date(),6,taskName,"",""});
            jr.setMsg("success");
            jr.setResult("success");
        }
        catch (Exception e){
            jr.setResult("fail");
            jr.setMsg(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }

    /*开始节点的流程删除*/
    @RequestMapping(value = "/deleteSelfProcessByAdmin")
    @ResponseBody
    @Transactional
    public JSONResult deleteSelfProcessByAdmin(String proInstanceId, String userRealName, HttpServletRequest request){
        JSONResult jr=new JSONResult();
        try {//ProcessUtils.isAdmin(userName)
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(proInstanceId).singleResult();
            if(historicProcessInstance.getTenantId().equals(currentUserNameFromRequestCookie) || ProcessUtils.isAdmin(currentUserNameFromRequestCookie)){
                List<Task> task = taskService.createTaskQuery().processInstanceId(proInstanceId).list();
                String taskid="";  String taskName="";
                if(task.size()>0){
                    taskid=task.get(0).getId();
                    taskName=task.get(0).getName();
                }
                runtimeService.setVariable(proInstanceId,"process_state","7");
                runtimeService.deleteProcessInstance(proInstanceId,"adminDelete");
                //保存操作信息
                jdbcTemplate.update("INSERT INTO proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,attachment) VALUES(?,?,?,?,?,?,?,?,?,?)",
                        new Object[]{ ProcessUtils.getUUID(),proInstanceId,taskid,currentUserNameFromRequestCookie,userRealName,new Date(),6,taskName,"",""});
                jr.setMsg("success");
                jr.setResult("success");
            }else{
                jr.setMsg("1");
                jr.setResult("没有权限");
            }
        }
        catch (Exception e){
            jr.setMsg("fail");
            jr.setResult(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }

    /**
     * 查看流程图片
     */
    @RequestMapping(value = "/lookProPic")
    public String  lookProPic(String depid,String taskid, HttpServletResponse response)throws  Exception{
        List<String> names=null;String mydepid=null;
        try {
            if(taskid==null){
                taskid="";
            }
            int a = taskid.length();
            if (taskid.length() > 0) {
                Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                String depid1 = processInstance.getDeploymentId();
                names = repositoryService.getDeploymentResourceNames(depid1);
                mydepid = depid1;
            } else {
                names = repositoryService.getDeploymentResourceNames(depid);
                mydepid = depid;
            }
            String imageName = null;
            for (String name : names) {
                if (name.indexOf(".png") >= 0) {
                    imageName = name;
                }
            }
            if (imageName != null) {
                //获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
                InputStream in = repositoryService.getResourceAsStream(mydepid, imageName);
                //从response对象获取输出流
                OutputStream out = response.getOutputStream();
                //将输入流中的数据读取出来，写到输出流中
                for (int b = -1; (b = in.read()) != -1; ) {
                    out.write(b);
                }
                out.close();
                in.close();
                //将图写到页面上，用输出流写
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
            return null;

    }

    /**
     * 查看当前流程图片取做标值
     */
        @RequestMapping(value = "/lookCurrentPro")
        @ResponseBody
        public  Map<String, Object>  lookCurrentPro(String taskId,String proInsID,String depid,String status,String start,String end){
            //存放坐标
            Map<String, Object> map = new HashMap<String,Object>();
         if(status==null||status==""){
             String processInstanceId=null;
             ProcessDefinitionEntity processDefinitionEntity=null;
             if(taskId.length()>0){
                 //使用任务ID，查询任务对象
                 Task task = taskService.createTaskQuery()
                         .taskId(taskId)//使用任务ID查询
                         .singleResult();
                 //获取流程定义的ID
                 String processDefinitionId = task.getProcessDefinitionId();
                 //获取流程定义的实体对象（对应.bpmn文件中的数据）
                 processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
                 //流程实例ID
                 processInstanceId = task.getProcessInstanceId();
             }else {
                 processInstanceId=proInsID;
                 ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                 processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
             }
             //使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
             ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
                     .processInstanceId(processInstanceId)//使用流程实例ID查询
                     .singleResult();
             //获取当前活动的ID
             String activityId = pi.getActivityId();
             //如果是用户节点并行时activityId为null，
             //runtimeService.getActiveActivityIds(processInstanceId)获取的时当前运行节点的包括并行的activityId
             if(activityId==null){
                 activityId=runtimeService.getActiveActivityIds(processInstanceId).get(0);
             }
             //获取当前活动对象
             ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//活动ID
             //获取坐标
             map.put("x", activityImpl.getX());
             map.put("y", activityImpl.getY());
             map.put("width", activityImpl.getWidth());
             map.put("height", activityImpl.getHeight());
         }else {
             ProcessDefinition processDefinition =  repositoryService.createProcessDefinitionQuery().deploymentId(depid).singleResult();
             ProcessDefinitionEntity  processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinition.getId());
             List<ActivityImpl> activities = processDefinitionEntity.getActivities();
                 if("1".equals(start)){
                     for (ActivityImpl a : activities) {
                         if(a.getProperty("type").equals("startEvent")){
                             map.put("x", a.getX());
                             map.put("y", a.getY());
                             map.put("width", a.getWidth());
                             map.put("height", a.getHeight());
                         }
                     }
                 }
                 if("1".equals(end)){
                     for (ActivityImpl a : activities) {
                         if(a.getProperty("type").equals("endEvent")){
                             map.put("x", a.getX());
                             map.put("y", a.getY());
                             map.put("width", a.getWidth());
                             map.put("height", a.getHeight());
                         }
                     }
                 }
         }
            return map;
        }

    /**
     * 查询流程列表
     */
   /* List<IdentityLink> identityLinks= repositoryService.getIdentityLinksForProcessDefinition(prodefinedid);
            for(IdentityLink link :identityLinks){
        if(StringUtils.isNotBlank(link.getUserId())){
            repositoryService.deleteCandidateStarterUser(prodefinedid,link.getUserId());
        }
        if(StringUtils.isNotBlank(link.getGroupId())){
            repositoryService.deleteCandidateStarterGroup(prodefinedid,link.getGroupId());
        }
    }*/
    @RequestMapping(value = "/selectProList")
    @ResponseBody
    public JSONResult selectProList(HttpServletRequest request) throws Exception {
        JSONResult jr=new JSONResult();
    try {
        String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
        List<Map<String,Object>> dataList=new ArrayList<Map<String, Object>>();
        Set<String> set=new HashSet<>();
        List<Map<String,Object>> dataList1=new ArrayList<Map<String, Object>>();
        List<String> deps = ProcessUtils.getDepsAndPostByUserName(userName);
        List<Deployment> delist=repositoryService.createDeploymentQuery().list();
            for (Deployment deployment:delist) {
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
                Model model = repositoryService.createModelQuery().deploymentId(deployment.getId()).singleResult();
                List<IdentityLink> identityLinks= repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
                if(ProcessUtils.getAuthority(identityLinks,userName,deps) || ProcessUtils.isAdmin(userName) || deployment.getTenantId().equals(userName)){
                    if(processDefinition.getDescription()==null && model!=null){
                        Map<String,Object> maps=new HashMap<String,Object>();
                        maps.put("depid",deployment.getId());
                        maps.put("deName",ProcessUtils.getProName(deployment.getName()));
                        maps.put("deNameParam",ProcessUtils.getProNameParam(deployment.getName()));
                        maps.put("processDefinitionID",processDefinition.getId());
                        List<Map<String, Object>> list1 = jdbcTemplate.queryForList("" +
                                "SELECT * FROM classify WHERE id=?", new Object[]{deployment.getCategory()});
                        String classfifyName="";
                        if(list1.size()!=0){
                            classfifyName=list1.get(0).get("classifyname").toString();
                        }
                        maps.put("proclassify",classfifyName);
                        maps.put("version",processDefinition.getVersion());
                        dataList.add(maps);
                    }
                }
            }
            for (Map<String, Object> map:dataList) {
                set.add(map.get("proclassify").toString());
            }
            List<List<Map<String,Object>>> proclassifys = MapValueOfClassify.getListArrayByMapValueOfClassify(dataList, "proclassify", set);
            for (List<Map<String,Object>> objectList:proclassifys) {
                Map<String,Object> m=new HashMap<>();
                m.put("proclassify",objectList.get(0).get("proclassify").toString());
                m.put("proLists",objectList);
                dataList1.add(m);
            }

            jr.setMsg("success");
            jr.setResult(dataList1);
        } catch (Exception e) {
            jr.setMsg("fail");
            jr.setResult(e.getMessage());
        }
        return jr;
    }


    /**
     *获取初始化流程formkey
     */
    @RequestMapping("/applicationForm")
    @ResponseBody
    public JSONResult applicationForm(String processDefinitionID) throws Exception {
        //session.setAttribute("myrequestid",id);
        Map<String,String> map=new HashMap<String, String>();
        JSONResult jr=new JSONResult();
        try{
            //这个id是用来存模板上业务数据用的唯一标识id
            String id =ProcessUtils.getUUID();
            Map<String, String> mapFormKeyAndName = ProcessUtils.getApplicationFormKeyAndName(processDefinitionID, repositoryService);
            //session.setAttribute("reportName",reportName);//process:3:7530
            map.put("reportName",mapFormKeyAndName.get("formkey").toString());
            map.put("requestid",id);
            map.put("taskName",mapFormKeyAndName.get("name").toString());
            map.put("iswritecomment",mapFormKeyAndName.get("iswritecomment").toString());
            map.put("tijiaoName",mapFormKeyAndName.get("tijiaoName").toString());
            jr.setMsg("success");
            jr.setResult(map);
        }
        catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
        }
        return jr;
    }

   //获取条件
   @RequestMapping("/getElementCondition")
   @ResponseBody
   public JSONResult getElementCondition(String processDefinitionID,String taskid){
       JSONResult jr=new JSONResult();
       List<Map<String, Object>> list=new ArrayList<>();
       try{
           if("".equals(taskid)&&!"".equals(processDefinitionID)){
               list = ProcessUtils.getElementCondition("",processDefinitionID, repositoryService,taskService);
               jr.setResult(list);
               jr.setMsg("success");
           }else if(!"".equals(taskid)&&"".equals(processDefinitionID)){
               Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
               list = ProcessUtils.getElementCondition(taskid,task.getProcessDefinitionId(), repositoryService,taskService);
               jr.setResult(list);
               jr.setMsg("success");
           }else{
               //参数错误
               jr.setResult(null);
               jr.setMsg("1");
           }
           return jr;
       }catch (Exception e){
           jr.setResult(e.getMessage());
           jr.setMsg("fail");
           return jr;
       }
   }


    /**
     *关联业务与流程时，启动流程
     */
    @RequestMapping("/guanlianproyuyewu")
    @ResponseBody
    @Transactional
    public JSONResult guanlianproyuyewu(@RequestParam(value="file",required=false)
            MultipartFile file,String state,HttpServletRequest request){

        String processDefinitionID=request.getParameter("processDefinitionID");
        String commentinfo=request.getParameter("commentinfo");
        String proname=request.getParameter("proname");
        String requestid= request.getParameter("requestid");
        String reportName= request.getParameter("reportName");
        String taskid= request.getParameter("taskid");
        String seesionid= request.getParameter("seesionid");
        String attachmentid="";
        String startTime="";
        String type="";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Object> map=new HashMap<>();
        Map<String, Object> resultMap=new HashMap<>();
        JSONResult jr=new JSONResult();
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionID).singleResult();
            String username=LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName=UserService.getInstance().getUserByUserName(username).getRealName();
             //初始化会签节点操作者信息
             map = ProcessUtils.initHuiQian(processDefinitionID,repositoryService);
             //添加流程判断条件
            ProcessUtils.getCondition(processDefinitionID,taskid,repositoryService,taskService,seesionid,request,map,resultMap,username);

            //0：申请人提交可撤回状态  1：通过 2：查看 3：被退回（不包括申请节点） 4：撤回 5：转办 6:完成 7：删除 8：被退回到申请节点
            map.put("process_state","0");
            //添加模板,这个一定要放在变量中
            map.put("process_formKey",reportName);
            //添加流程发起人
            map.put("process_userRealName",userRealName);


             //启动流程
            Authentication.setAuthenticatedUserId(username);
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(),requestid,map);
            startTime=sdf.format(new Date());
           runtimeService.setProcessInstanceName(processInstance.getId(),proname);
             //启动后默认通过第一个任务节点（申请节点）
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            TaskEntity taskEntity = (TaskEntity) task;
            String temp_taskid=task.getId();
            String temp_applicationNodeName=task.getName();
            ActivityImpl activityImpl = ProcessUtils.getActivityImplByActivitiId(task.getTaskDefinitionKey(), task.getProcessDefinitionId(), repositoryService);
            type=activityImpl.getProperty("multiInstance")==null ? "" : activityImpl.getProperty("multiInstance").toString();
            taskService.complete(task.getId());
            //上传附件
           if(file!=null){
                attachmentid=ProcessUtils.uploadAttachment(request,file);
            }


            if("1".equals(state)){//
                if("".equals(attachmentid)){
                    attachmentid=jdbcTemplate.queryForObject("SELECT attachment FROM proopreateinfo WHERE requestid=?",new Object[]{requestid},String.class);
                }
                jdbcTemplate.update("DELETE FROM proopreateinfo WHERE requestid=?",new Object[]{requestid});
            }
            //保存操作信息
            jdbcTemplate.update("INSERT INTO proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,attachment,reportName,proname) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{ProcessUtils.getUUID(),processInstance.getId(),temp_taskid,username,userRealName,new Date(),1,temp_applicationNodeName,PreventXSS.delHTMLTag(commentinfo),attachmentid,taskEntity.getFormKey(),proname});

          ProcessInstance pro = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
            if(pro!=null){
                //自动流传与第二次默认通过
                Object proDueTime = runtimeService.getVariable(pro.getId(), "proDueTime");
                if(!"parallel".equals(type) && !"sequential".equals(type)){
                    ProcessUtils.autopass(taskService,processInstance.getId(),repositoryService,
                            runtimeService,jdbcTemplate,username,historyService);
                }
                //发送消息
                Map<String,String> para=new HashMap<>();
                para.put("startPeople",userRealName);
                para.put("startTime",startTime);

                para.put("proDueTime",proDueTime==null?"":proDueTime.toString());
                para.put("shenheTime",startTime);
                sendMessage.getSendMessageUser(taskService,processInstance.getId(),jdbcTemplate,proname,para,"1",null);
            }else{
                jdbcTemplate.update("UPDATE ACT_HI_VARINST SET TEXT_='6' WHERE PROC_INST_ID_=? AND NAME_='process_state'",
                        new Object[]{processInstance.getId()});
            }
            jr.setMsg("success");
            jr.setResult(resultMap);
        }catch (Exception e){
            if((e.getMessage()+"").indexOf("No outgoing sequence flow of the exclusive gateway")>-1
                    && (e.getMessage()+"").indexOf("could be selected for continuing the process")>-1){
                jr.setResult("");
                jr.setMsg("001");
            }else{
                e.printStackTrace();
                jr.setResult(e.getMessage());
                jr.setMsg("fail");
            }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }


  /*
     *转办任务
     */
    @RequestMapping("/zhuanbanTask")
    @ResponseBody
    @Transactional
    public JSONResult zhuanbanTask(String zhuanbanName,String taskid,String info,String reportName,HttpServletRequest request) {
        JSONResult jr=new JSONResult();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        try{
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
            Authentication.setAuthenticatedUserId(userRealName);
            taskService.setAssignee(taskid,zhuanbanName);
            Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
            String moban="";
            if("".equals(reportName) || reportName==null){
                UserTask userTask = ProcessUtils.getUserTask(task.getId(), task.getProcessDefinitionId(), repositoryService);
                moban=userTask.getFormKey()==null?"":userTask.getFormKey();
            }else{
                moban=reportName;
            }
            //保存操作信息
            runtimeService.setVariable(task.getProcessInstanceId(),"process_state","5");
            jdbcTemplate.update("INSERT INTO proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,reportName) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{ProcessUtils.getUUID(),task.getProcessInstanceId(),taskid,userName,userRealName,new Date(),5,task.getName(),PreventXSS.delHTMLTag(info),moban});
            HistoricProcessInstance hisProIn = historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            sendMessage.getSendMessageUserOnZhuanBan(taskService,task.getId(),hisProIn.getName(),hisProIn.getStartUserId(),sdf.format(hisProIn.getStartTime()));
           jr.setResult("success");
        }
        catch (Exception e){
            jr.setResult("fail");
            jr.setMsg(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }

    /**
     *根据任务id查布局
     */
    @RequestMapping("/userTaskForm")
    @ResponseBody
    public JSONResult userTaskForm(String taskid) {
        JSONResult jr=new JSONResult();
        String zhuanban="";
        String btnname="";
        String tuihui="";
        String istuihui="";
        String addHuiQianRen="";
        String issetaddhuiqian="";
        String iswritecomment="";
        Map<String,Object> map=new HashMap<>();
        try {
            Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
             if(task!=null){
                 ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().
                         processInstanceId(task.getProcessInstanceId()).singleResult();
                 // 0：申请人提交可撤回状态  1：通过 2：查看 3：被退回（不包括申请节点） 4：撤回 5：转办 6:完成 7：删除 8：被退回到申请节点
                 String applicationActivitiId = ProcessUtils.getApplicationActivitiId(task.getProcessDefinitionId(), repositoryService);
                 if(!applicationActivitiId.equals(task.getTaskDefinitionKey())){
                     runtimeService.setVariable(task.getProcessInstanceId(),"process_state","2");
                 }
                 UserTask userTask = ProcessUtils.getUserTask(task.getTaskDefinitionKey(), task.getProcessDefinitionId(), repositoryService);

                  String btnName = ProcessUtils.getTaskExectionName(userTask, "btnName");
                  btnname="".equals(btnName) ? "通过" : btnName;

                 //true false
                  zhuanban = ProcessUtils.getTaskExectionName(userTask, "zhuanban");
                  istuihui = ProcessUtils.getTaskExectionName(userTask, "istuihui");
                  issetaddhuiqian = ProcessUtils.getTaskExectionName(userTask, "issetaddhuiqian");
                  iswritecomment = ProcessUtils.getTaskExectionName(userTask, "iswritecomment");
                 //判断是否为会签节点
                 if(ProcessUtils.isHuiQianNodePallel(task.getTaskDefinitionKey(),task.getProcessDefinitionId(),repositoryService)) {
                     addHuiQianRen="true".equals(issetaddhuiqian) ? "加签" : "";
                 }
                 if("true".equals(istuihui)){
                     String tuiHui = ProcessUtils.getTaskExectionName(userTask, "tuihui");
                     tuihui="".equals(tuiHui) ? "退回" : tuiHui;
                 }
                 if(!ProcessUtils.isHuiQianNodePallel(task.getTaskDefinitionKey(),task.getProcessDefinitionId(),repositoryService)
                         && task.getAssignee()!=null){
                     if("true".equals(zhuanban)){
                         zhuanban="转办";
                     }
                 }
                 map.put("moban",userTask.getFormKey());
                 map.put("yeuwuid",processInstance.getBusinessKey());
                 map.put("zhuanban",zhuanban);
                 map.put("tijiao",btnname);
                 map.put("tuihui",tuihui);
                 map.put("istuihui",istuihui);
                 map.put("addHuiQianRen",addHuiQianRen);
                 map.put("proDefinitionId",task.getProcessDefinitionId());
                 map.put("processInstanceId",task.getProcessInstanceId());
                 map.put("iswritecomment",iswritecomment);
                 map.put("activityid",task.getTaskDefinitionKey());
                 jr.setResult(map);
                 jr.setMsg("success");
             }else{
                 jr.setResult(map);
                 jr.setMsg("2");
             }
        }catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
        }
        return jr;
    }



    /**
     *完成任务
     */
    @RequestMapping("/completeTask")
    @ResponseBody
    @Transactional
    public JSONResult completeTask(String taskid,String commentinfo,HttpServletRequest request,String seesionid,
                                   @RequestParam(value="file",required=false) MultipartFile file,String proname) {
        JSONResult jr=new JSONResult();
        Map<String,Object> map=new HashMap<>();
        Map<String,Object> resultMap=new HashMap<>();
        String attachmentid = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String type="";
        try {
            String userName=LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName=UserService.getInstance().getUserByUserName(userName).getRealName();
            Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
          if(task!=null){
              TaskEntity taskEntity = (TaskEntity) task;
              ProcessUtils.getCondition("",taskid,repositoryService,taskService,
                      seesionid,request,map,resultMap,userName);
              String proInstanceId=task.getProcessInstanceId();
            /*  System.out.println(map);
              System.out.println(resultMap);*/

              //上传附件
              if(file!=null){
                  attachmentid=ProcessUtils.uploadAttachment(request,file);
              }
              ActivityImpl activityImpl = ProcessUtils.getActivityImplByActivitiId(task.getTaskDefinitionKey(), task.getProcessDefinitionId(), repositoryService);
              type=activityImpl.getProperty("multiInstance")==null ? "" : activityImpl.getProperty("multiInstance").toString();
              taskService.complete(taskid,map);
              //保存操作信息
              // 1：申请人提交 2：保存 3：驳回 4：撤回 5：转办 6：删除 7:通过
              jdbcTemplate.update("insert into proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,attachment,reportName) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                      new Object[]{ ProcessUtils.getUUID(),proInstanceId,taskid,userName,userRealName,new Date(),7,task.getName(),PreventXSS.delHTMLTag(commentinfo),attachmentid,taskEntity.getFormKey()});

              ProcessInstance proInstance = runtimeService.createProcessInstanceQuery().processInstanceId(proInstanceId).singleResult();
              if(proInstance==null){
                  jdbcTemplate.update("UPDATE ACT_HI_VARINST SET TEXT_='6' WHERE PROC_INST_ID_=? AND NAME_='process_state'",
                          new Object[]{proInstanceId});
              }else {
                  runtimeService.setVariable(proInstance.getId(),"process_state","1");
                  Object proDueTime = runtimeService.getVariable(proInstance.getId(), "proDueTime");
                  //自动流传与第二次默认通过
                  if(!"parallel".equals(type) && !"sequential".equals(type)){
                      ProcessUtils.autopass(taskService,proInstance.getId(),repositoryService,
                              runtimeService,jdbcTemplate,userName,historyService);
                  }
                  //推送消息
                  Map<String,String> para=new HashMap<>();
                  HistoricProcessInstance proInstanceHis = historyService.createHistoricProcessInstanceQuery().
                          processInstanceId(proInstance.getId()).singleResult();
                  User userByUserName = UserService.getInstance().getUserByUserName(proInstanceHis.getStartUserId());
                  String realName="";
                  if(userByUserName!=null){
                      realName=userByUserName.getRealName();
                  }else{
                      List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().
                              processInstanceId(proInstance.getId()).list();
                      for(HistoricVariableInstance h:hisVarList){
                          if("process_userRealName".equals(h.getVariableName())){
                              realName=h.getValue().toString();
                          }
                      }
                  }

                  para.put("startPeople",realName);
                  para.put("startTime",sdf.format(proInstanceHis.getStartTime()));

                  para.put("proDueTime",proDueTime==null?"":proDueTime.toString());
                  para.put("shenheTime",sdf.format(new Date()));
                  sendMessage.getSendMessageUser(taskService,proInstanceId,jdbcTemplate,proname,para,"1",null);
              }

              jr.setResult(resultMap);
              jr.setMsg("success");
          }else{
              jr.setResult("");
              jr.setMsg("002");
          }
        }
        catch (Exception e){
            if((e.getMessage()+"").indexOf("No outgoing sequence flow of the exclusive gateway")>-1
                    && (e.getMessage()+"").indexOf("could be selected for continuing the process")>-1){
                jr.setResult("");
                jr.setMsg("001");
            }else{
                jr.setResult(e.getMessage());
                jr.setMsg("fail");
            }

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }

    /**
     *查看历史流程
     */
    @RequestMapping("/selectHisPro")
    @ResponseBody
    public JSONResult selectHisPro1(String num,HttpServletRequest request) {
        JSONResult jr=new JSONResult();
        List<Map<String, Object>> HisList=new ArrayList<Map<String, Object>>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Integer yeshu=Integer.valueOf(num);
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName =UserService.getInstance().getUserByUserName(userName).getRealName();
            List<HistoricProcessInstance> resultList = historyService.createHistoricProcessInstanceQuery()
                    .startedBy(userName).orderByProcessInstanceStartTime().desc().list();
            List<HistoricProcessInstance> list=ProcessUtils.getHistoricProcessInstanceByYeShu(resultList,yeshu);
            //申请节点的activityid
            for (int i = 0; i < list.size(); i++) {
                String endTime="";
                String completeState="";
                String process_state="";
                String process_formKey="";
                Map<String, Object> HisMap = new HashMap<String, Object>();
                if (list.get(i).getEndTime() == null) {
                    endTime = "";
                    completeState = "进行中";
                    Map<String, VariableInstance> map = runtimeService.getVariableInstances(list.get(i).getId());
                    process_state=map.get("process_state").getTextValue();
                    process_formKey=map.get("process_formKey").getTextValue();
                } else {
                    endTime = sdf.format(list.get(i).getEndTime());
                    if(list.get(i).getDeleteReason()==null){
                        completeState = "完成";
                        process_state="6";

                    }else {
                        //已删除流程
                        if("expired".equals(list.get(i).getDeleteReason().trim())){
                            completeState="完成";
                            process_state="9";
                        }else{
                            completeState="已删除";
                            process_state="7";
                        }

                    }
                    List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().
                            processInstanceId(list.get(i).getId()).list();
                    for(HistoricVariableInstance h:hisVarList){
                        if("process_formKey".equals(h.getVariableName())){
                            process_formKey=h.getValue().toString();
                        }
                    }

                }
                HisMap.put("businessKey", list.get(i).getBusinessKey());
                HisMap.put("reportName",  process_formKey);
                HisMap.put("proInsID", list.get(i).getId());
                HisMap.put("proDefinitionId", list.get(i).getProcessDefinitionId());
                HisMap.put("proStartTime", sdf.format(list.get(i).getStartTime()));
                HisMap.put("proEndTime", endTime);
                HisMap.put("proCompleteState", completeState);
                HisMap.put("prostate", process_state);
                HisMap.put("proname", list.get(i).getName());
                HisMap.put("userRealName",userRealName);
                String applicationId=ProcessUtils.getApplicationActivitiId(list.get(i).getProcessDefinitionId(),
                        repositoryService);
                HisMap.put("activityid", applicationId);

                HisList.add(HisMap);
            }
          //  sortListByTime.listSortByProStartTime(HisList);
            jr.setMsg("success");
            jr.setResult(HisList);
            jr.setTotal(resultList.size());
            if(resultList.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(resultList.size()%10==0 ? resultList.size()/10 : resultList.size()/10+1);
            }
            return jr;
        }
        catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
            return jr;
        }
    }

    /**
     *查看历史流程已处理的
     */
    @RequestMapping("/selectHisProYiChuLi")
    @ResponseBody
    public JSONResult selectHisProYiChuLi(String num,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        Integer yeshu=Integer.valueOf(num);
        String message="";
        String process_formKey="";
        try {
            boolean flag=true;
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            List<Map<String, Object>> list_temp = jdbcTemplate.queryForList("SELECT  proInstanceId,taskid,reportName  " +
                            "FROM proopreateinfo WHERE opreateName=? AND opreateType  IN('3','5','7','10') ORDER BY opreateTime DESC",
                    new Object[]{userName});
            List<String> list1 = new ArrayList<>();
            List<Map<String, Object>> mapList=new ArrayList<>();
            for(int i=0;i<list_temp.size();i++){
                String proInstanceId=list_temp.get(i).get("proInstanceId")==null?"":list_temp.get(i).get("proInstanceId").toString();
                if(!list1.contains(proInstanceId)){
                    list1.add(proInstanceId);
                    mapList.add(list_temp.get(i));
                }
            }
            List<Map<String, Object>> list11=new ArrayList<>();
            for(int i=0;i<mapList.size();i++){
                HistoricProcessInstance hisProInstance = historyService.createHistoricProcessInstanceQuery().
                        processInstanceId(mapList.get(i).get("proInstanceId").toString()).singleResult();
                if(hisProInstance!=null){
                    list11.add(mapList.get(i));
                }
            }

            List<Map<String, Object>> list = ProcessUtils.getYiChuLiByYeShu(list11, yeshu);
            List<Map<String, Object>> result=new ArrayList<>();
            System.out.println(list);
            for (int i=0;i<list.size();i++) {
                Map<String, Object> map = new HashMap<>();
                HistoricProcessInstance hisProInstance = historyService.createHistoricProcessInstanceQuery().
                        processInstanceId(list.get(i).get("proInstanceId").toString()).singleResult();
               if(hisProInstance!=null){
                   if(hisProInstance.getEndTime()==null){
                       Map<String, VariableInstance> mapVariable = runtimeService.getVariableInstances(list.get(i).get("proInstanceId").toString());
                       String process_state=mapVariable.get("process_state").getTextValue();
                       process_formKey=mapVariable.get("process_formKey").getTextValue();
                       String process_userRealName=mapVariable.get("process_userRealName").getTextValue();
                       map.put("startPeople",process_userRealName);
                       map.put("proEndTime", "");
                       map.put("proStatus",process_state);

                   }else{
                       if(hisProInstance.getDeleteReason()==null){
                           map.put("proStatus","6");
                       }else {
                           //已删除流程
                           //已删除流程
                           if("expired".equals(hisProInstance.getDeleteReason().trim())){
                               map.put("proStatus","9");
                           }else{
                               map.put("proStatus","7");
                           }
                       }
                       List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().
                               processInstanceId(list.get(i).get("proInstanceId").toString()).list();
                       for(HistoricVariableInstance h:hisVarList){
                          if("process_userRealName".equals(h.getVariableName())){
                               map.put("startPeople",h.getValue().toString());
                           }else if("process_formKey".equals(h.getVariableName())){
                              process_formKey=h.getValue().toString();
                          }
                       }
                       map.put("proEndTime", sdf.format(hisProInstance.getEndTime()));

                   }
                   String reportName= list.get(i).get("reportName") == null ? "" : list.get(i).get("reportName").toString();
                   if("".equals(reportName)){
                       List<HistoricTaskInstance> list2 = historyService.createHistoricTaskInstanceQuery().processInstanceId(list.get(i).get("proInstanceId").toString()).
                               taskAssignee(userName).orderByHistoricTaskInstanceStartTime().desc().list();
                       if(list2.size()==0){
                            //flag=false;
                           String taskid=list.get(i).get("taskid")==null?"":list.get(i).get("taskid").toString();
                           List<HistoricTaskInstance> list3 = historyService.createHistoricTaskInstanceQuery().taskId(taskid).list();
                           if(list3.size()>0){
                               map.put("proFormKey",list3.get(0).getFormKey());
                           }else{
                               map.put("proFormKey",process_formKey);
                           }
                            //break;
                       }else{
                           String formKey = list2.get(0).getFormKey();
                           if(formKey==null || "".equals(formKey)){
                               String taskDefinitionKey = list2.get(0).getTaskDefinitionKey();
                               UserTask activityImpl = ProcessUtils.getUserTask(taskDefinitionKey, list2.get(0).getProcessDefinitionId(), repositoryService);
                               map.put("proFormKey",activityImpl.getFormKey());
                           }else{
                               map.put("proFormKey",formKey);
                           }
                       }
                   }else{
                       map.put("proFormKey",reportName);
                   }

                   map.put("businessKey", hisProInstance.getBusinessKey());
                   map.put("proname", hisProInstance.getName());
                   map.put("proStartTime", sdf.format(hisProInstance.getStartTime()));
                   map.put("proDefineID", hisProInstance.getProcessDefinitionId());
                   map.put("proInstanceId", hisProInstance.getId());
                   String s = list.get(i).get("taskid").toString();
                   String taskDefinitionKey="";
                   List<Map<String, Object>> list2 = jdbcTemplate.queryForList("SELECT * FROM ACT_HI_ACTINST WHERE TASK_ID_=?",
                           new Object[]{s});
                   if(list2.size()>0){
                       taskDefinitionKey=list2.get(0).get("ACT_ID_").toString();
                   }
                 else{
                   taskDefinitionKey="";
                   }
                   map.put("activityid", taskDefinitionKey);
                   result.add(map);
               }
            }

            ProcessUtils.SortByStringTime(result,"proStartTime");
            if(flag){
                jr.setResult(result);
                jr.setMsg("success");
            }else{
                jr.setResult("流程实例"+message+","+"用户名"+userName+",查询出错");
                jr.setMsg("fail");
            }

            jr.setTotal(list11.size());
            if(list11.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(list11.size()%10==0 ? list11.size()/10 : list11.size()/10+1);
            }
            return jr;
        }catch ( Exception e){
                jr.setResult(e.getMessage());
                jr.setMsg("fail");
                return jr;
        }
    }


    /*
    *查询自己的任务
     */
    @RequestMapping("/selectTask")
    @ResponseBody
    public JSONResult selectTask(String num,HttpServletRequest request) {
        JSONResult jr=new JSONResult();
        Integer yeshu=Integer.valueOf(num);
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            //组任务办理人为null
            List<Task> zuList = taskService.createTaskQuery().taskCandidateUser(userName).orderByTaskCreateTime().desc().list();
            //有办理人的任务
            List<Task> assignList = taskService.createTaskQuery().taskAssignee(userName).orderByTaskCreateTime().desc().list();
            assignList.addAll(zuList);
            //排序
            sortListByTime.taskLists(assignList);
            List<Task> list=ProcessUtils.getTaskByYeShu(assignList,yeshu);

            List<Map<String, Object>> listmaps = new ArrayList<Map<String, Object>>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Task t : list) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("taskId", t.getId());
                map.put("taskName", t.getName());
                HistoricProcessInstance hps = historyService.createHistoricProcessInstanceQuery().
                        processInstanceId(t.getProcessInstanceId()).singleResult();
                map.put("proStartTime", sdf.format(hps.getStartTime()));
                map.put("proname", hps.getName());
                map.put("userName", hps.getStartUserId());
                //process_userRealName
                map.put("userRealName",runtimeService.getVariableInstance(t.getProcessInstanceId(),
                        "process_userRealName").getValue());
                map.put("proDefinedId", t.getProcessDefinitionId());
                map.put("proInstanceId", t.getProcessInstanceId());
                listmaps.add(map);
            }

            jr.setResult(listmaps);
            jr.setTotal(assignList.size());
            if(assignList.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(assignList.size()%10==0 ? assignList.size()/10 : assignList.size()/10+1);
            }
            jr.setMsg("success");

        }
        catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
        }
        return jr;
    }

    /*
    *查询自己的任务
     */
    @RequestMapping("/selectTask1")
    @ResponseBody
    public JSONResult selectTask1(String num,String proName,String startPeople,String time,HttpServletRequest request) {
        JSONResult jr=new JSONResult();
        Integer yeshu=Integer.valueOf(num);
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            //组任务办理人为null
            List<Task> zuList = taskService.createTaskQuery().taskCandidateUser(userName).orderByTaskCreateTime().desc().list();
            //有办理人的任务
            List<Task> list = taskService.createTaskQuery().taskAssignee(userName).orderByTaskCreateTime().desc().list();
            list.addAll(zuList);
            //排序
            sortListByTime.taskLists(list);
           // List<Task> list=ProcessUtils.getTaskByYeShu(assignList,yeshu);

            List<Map<String, Object>> listmaps = new ArrayList<Map<String, Object>>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Task t : list) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("taskId", t.getId());
                map.put("taskName", t.getName());
                HistoricProcessInstance hps = historyService.createHistoricProcessInstanceQuery().
                        processInstanceId(t.getProcessInstanceId()).singleResult();
                map.put("proStartTime", sdf.format(hps.getStartTime()));
                map.put("proname", hps.getName());
                map.put("userName", hps.getStartUserId());
                //process_userRealName
                map.put("userRealName",runtimeService.getVariableInstance(t.getProcessInstanceId(),
                        "process_userRealName").getValue());
                map.put("proDefinedId", t.getProcessDefinitionId());
                map.put("proInstanceId", t.getProcessInstanceId());
                listmaps.add(map);
            }
            List<Map<String, Object>> result1 = new ArrayList<>();
            //条件查询
            if((proName!=null&&!"".equals(proName))||(startPeople!=null&&!"".equals(startPeople))||(time!=null&&!"".equals(time)))
            {
                if(proName!=null&&!"".equals(proName)){
                    for(int k=0;k<listmaps.size();k++) {
                        String s = listmaps.get(k).get("proname").toString();
                        if (s.contains(proName)) {
                            if(!result1.contains(listmaps.get(k))){
                                result1.add(listmaps.get(k));
                            }
                        }
                    }
                    listmaps=result1;
                    result1 = new ArrayList<>();
                }
                if(startPeople!=null&&!"".equals(startPeople)){
                    for(int k1=0;k1<listmaps.size();k1++) {
                        String s1 = listmaps.get(k1).get("userRealName").toString();
                        if (s1.contains(startPeople)) {
                            if(!result1.contains(listmaps.get(k1))){
                                result1.add(listmaps.get(k1));
                            }
                        }
                    }
                    listmaps=result1;
                    result1 = new ArrayList<>();
                }
                if(time!=null&&!"".equals(time)){
                    for(int k2=0;k2<listmaps.size();k2++) {
                        String s1 = listmaps.get(k2).get("proStartTime").toString();
                        if (s1.contains(time)) {
                            if(!result1.contains(listmaps.get(k2))){
                                result1.add(listmaps.get(k2));
                            }
                        }
                    }
                    listmaps=result1;
                    result1 = new ArrayList<>();
                }
            }
            List<Map<String, Object>> resultByYeShu = ProcessUtils.getResultByYeShu(listmaps, yeshu);
            jr.setResult(resultByYeShu);
            jr.setTotal(listmaps.size());
            if(listmaps.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(listmaps.size()%10==0 ? listmaps.size()/10 : listmaps.size()/10+1);
            }
            jr.setMsg("success");

        }
        catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
        }
        return jr;
    }

    /**
     * 申请人保存未提交的流程
     */
    @RequestMapping(value = "/reserveProInfo")
    @ResponseBody
    @Transactional
    public JSONResult reserveProInfo(String requestid,String commentinfo,String taskName,
                                     @RequestParam(value="file",required=false) MultipartFile file,
                                     String reportName,String proname,String processDefinitionID
            ,String deployid,HttpServletRequest request) {
        JSONResult jr=new JSONResult();
        String attachmentid="";int update=0;
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
            Integer integer = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM proopreateinfo WHERE requestid=?", new Object[]{requestid}, Integer.class);
            if(integer==1){
                //更新
                if(file!=null){
                    attachmentid=ProcessUtils.uploadAttachment(request,file);
                    update = jdbcTemplate.update("UPDATE proopreateinfo SET opreateTime=?,mycomment=?,attachment=? WHERE requestid=?",
                            new Object[]{new Date(),commentinfo,attachmentid,requestid});
                }else {
                    update = jdbcTemplate.update("UPDATE proopreateinfo SET opreateTime=?,mycomment=? WHERE requestid=?",
                            new Object[]{new Date(),commentinfo,requestid});
                }
                if(update>0) {
                    jr.setMsg("success");
                }
                else {
                    jr.setMsg("fail");
                    jr.setResult("保存失败");
                }
            }else if(integer==0){
                //保存操作者操作信息
                if(file!=null){
                    attachmentid=ProcessUtils.uploadAttachment(request,file);
                }
                String sql="insert into proopreateinfo(id,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,attachment,requestid,reportName,deployid,proname) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
                update=jdbcTemplate.update(sql,new Object[]{ProcessUtils.getUUID(),userName,userRealName,new Date(),2,taskName,commentinfo,
                        attachmentid,requestid,reportName,deployid,proname});
                if(update>0) {
                    jr.setMsg("success");
                }
                else {
                    jr.setMsg("fail");
                    jr.setResult("保存失败");
                }
            }
            else {
                jr.setMsg("fail");
                jr.setResult("保存失败:预期一条记录，实际查出多条记录");
            }
        } catch (Exception e) {
            jr.setMsg("0");
            jr.setResult(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }

    //办理人保存
    @RequestMapping(value = "/banliBaoCun")
    @ResponseBody
    public JSONResult banliBaoCun(String taskid,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
            if(task!=null){
                jdbcTemplate.update("INSERT INTO proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment) " +
                        " VALUES(?,?,?,?,?,?,?,?,?)", new Object[]{ProcessUtils.getUUID(), task.getProcessInstanceId(),taskid, userName, userRealName, new Date(),8,task.getName(),""});
                jr.setMsg("success");
                jr.setResult("");
            }
            else{
                jr.setMsg("001");
                jr.setResult("");
            }

            return  jr;
        }
        catch ( Exception e){
            jr.setMsg("fail");
            jr.setResult(e.getMessage());
            return  jr;
        }
    }


    //查询保存列表
    @RequestMapping("/selectBaoCun")
    @ResponseBody
    public JSONResult selectBaoCun(String num,HttpServletRequest request) {
        Integer yeshu=Integer.valueOf(num);
        JSONResult jr=new JSONResult();
        List<Map<String, Object>> baocun_list=new ArrayList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String userName =LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
            //1：申请人提交 2：申请人保存 3：驳回 4：撤回 5：转办 6：删除 7:办理人通过 8：办理人保存
            List<Map<String, Object>> list_zong = jdbcTemplate.queryForList("SELECT * FROM proopreateinfo WHERE  opreateName=?  AND opreateType='2' ORDER BY opreateTime DESC",
                    new Object[]{userName});
            List<Map<String, Object>> list1= ProcessUtils.getBaoCunByYeShu(list_zong,yeshu);
            for (int i = 0; i < list1.size(); i++) {
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(list1.get(i).get("deployid") == null ? "" :
                        list1.get(i).get("deployid").toString()).singleResult();
                if (processDefinition != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("proName", list1.get(i).get("proname") == null ? "" : list1.get(i).get("proname").toString());
                    map.put("taskName", list1.get(i).get("nodeName") == null ? "" : list1.get(i).get("nodeName").toString());
                    map.put("opreateTime", list1.get(i).get("opreateTime") == null ? "" : sdf.format(list1.get(i).get("opreateTime")));
                    map.put("requestid", list1.get(i).get("requestid") == null ? "" : list1.get(i).get("requestid").toString());
                    map.put("reportName", list1.get(i).get("reportName") == null ? "" : list1.get(i).get("reportName").toString());
                    map.put("proName", list1.get(i).get("proName") == null ? "" : list1.get(i).get("proName").toString());
                    map.put("deployid", list1.get(i).get("deployid") == null ? "" : list1.get(i).get("deployid").toString());
                    map.put("proDefineId", processDefinition.getId());
                    map.put("comment", list1.get(i).get("mycomment") == null ? "" : list1.get(i).get("mycomment").toString());
                    String s = list1.get(i).get("attachment") == null ? "" : list1.get(i).get("attachment").toString();
                    if("".equals(s)){
                        map.put("attachment","");
                    }else{
                        if(s.contains("\\")){
                            map.put("attachment",s.split("\\\\")[s.split("\\\\").length-1]);
                        }else{
                            map.put("attachment",s.split("/")[s.split("/").length-1]);
                        }
                    }

                    map.put("state", list1.get(i).get("opreateType") == null ? "" : list1.get(i).get("opreateType").toString());
                    Map<String, String> applicationFormKeyAndName = ProcessUtils.getApplicationFormKeyAndName(processDefinition.getId(), repositoryService);
                    map.put("iswritecomment", applicationFormKeyAndName.get("iswritecomment").toString());
                    map.put("tijiaoName", applicationFormKeyAndName.get("tijiaoName").toString());
                    map.put("userRealName",userRealName);
                    baocun_list.add(map);
                }
            }
            jr.setResult(baocun_list);
            jr.setTotal(list_zong.size());
            if(list_zong.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(list_zong.size()%10==0 ? list_zong.size()/10 : list_zong.size()/10+1);
            }
            jr.setMsg("success");
            return jr;
        }catch ( Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
            return jr;
        }

    }

    //检查保存列表
    @RequestMapping("/checkBaoCun")
    @ResponseBody
    public JSONResult checkBaoCun(String requestid){
        JSONResult jr=new JSONResult();
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM  proopreateinfo WHERE requestid=?", new Object[]{requestid});
        if(list.size()==0){
            jr.setMsg("001");
        }else{
            jr.setMsg("success");
        }
        return  jr;
    }

    //删除保存
    @RequestMapping("/removeBaoCun")
    @ResponseBody
    public JSONResult removeBaoCun(String id){
        JSONResult jr=new JSONResult();
        int update = jdbcTemplate.update("UPDATE proopreateinfo SET opreateType='9' WHERE requestid=?", new Object[]{id});
        if(update>0){
            jr.setResult("success");
        }else {
            jr.setResult("fail");
        }
        return  jr;
    }


    /**
     *查询历史批注信息
     */
    @RequestMapping("/getComment")
    @ResponseBody
    public JSONResult getComment(String proInstanceId,HttpServletRequest request,String proDefinitionId,String activityid){
        JSONResult jr=new JSONResult();
        List<Map<String, Object>> result=new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> result11=new ArrayList<Map<String, Object>>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
        try {
            List<Map<String, Object>> opreateList = jdbcTemplate.queryForList("SELECT * FROM proopreateinfo WHERE proInstanceId=?  ORDER BY opreateTime DESC", new Object[]{proInstanceId});
            for (int i = 0; i < opreateList.size(); i++) {
                String s = opreateList.get(i).get("opreateType") == null ? "" : opreateList.get(i).get("opreateType").toString();
                //去掉申请人保存和申请人保存,和动态添加会签人
                if(!"2".equals(s) && !"9".equals(s) && !"10".equals(s)){
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("nodeName",opreateList.get(i).get("nodeName"));
                    map.put("opreateRealName", opreateList.get(i).get("opreateRealName"));
                    map.put("opreateType",  opreateList.get(i).get("opreateType"));
                    map.put("comment", opreateList.get(i).get("mycomment"));
                    map.put("opreateName", opreateList.get(i).get("opreateName"));
                    String attachmentid=opreateList.get(i).get("attachment")==null?"":opreateList.get(i).get("attachment").toString();
                    if("".equals(attachmentid)){
                        map.put("attachmentId","");
                        map.put("attachmentName", "");
                    }else {
                        map.put("attachmentId",attachmentid);
                        if(attachmentid.contains("\\")){
                            map.put("attachmentName",attachmentid.split("\\\\")[attachmentid.split("\\\\").length-1]);
                        }else{
                            map.put("attachmentName",attachmentid.split("/")[attachmentid.split("/").length-1]);
                        }

                    }
                    map.put("opreateTime", sdf.format(opreateList.get(i).get("opreateTime")));
                    map.put("proInstanceId",opreateList.get(i).get("proInstanceId"));
                    map.put("taskid",opreateList.get(i).get("taskid"));
                    result.add(map);
                }
            }
           if(activityid!=null && !"".equals(activityid)){
               UserTask userTask = ProcessUtils.getUserTask(activityid, proDefinitionId, repositoryService);
               String lookcontentbyself = ProcessUtils.getTaskExectionName(userTask, "lookcontentbyself");
               List<Map<String, Object>> result1=new ArrayList<Map<String, Object>>();
               if("true".equals(lookcontentbyself)){
                   //获取该节点下会签人
                   MultiInstanceLoopCharacteristics loopCharacteristics = userTask.getLoopCharacteristics();
                   String inputDataItem = loopCharacteristics.getInputDataItem();
                   List<String> value=new ArrayList<>();
                   ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(proInstanceId);

                   if(processInstanceQuery!=null){
                       value  = (List<String>) runtimeService.getVariable(proInstanceId, inputDataItem);
                   }else{
                       HistoricVariableInstance his = historyService.createHistoricVariableInstanceQuery().processInstanceId(proInstanceId).variableName(inputDataItem).singleResult();
                       getHisVariable gv=new getHisVariable(his.getId());
                       managerService.executeCommand(gv);
                       value= gv.getList();
                   }
                   List<String> value1 = new ArrayList<>();
                   for(String str:value){
                       if(!str.equals(userName)){
                           value1.add(str);
                       }
                   }
                   for(int j = 0; j < result.size(); j++){
                       String opreateName=result.get(j).get("opreateName")==null?"":result.get(j).get("opreateName").toString();
                       for(String str1:value1){
                           if(str1.equals(opreateName)){
                               result1.add(result.get(j));
                               break;
                           }
                       }
                   }

                   for(Map<String, Object> mm:result1){
                       result.remove(mm);
                   }

               }
           }

            jr.setMsg("success");
            jr.setResult(result);

        }
        catch (Exception e){
            jr.setMsg("fail");
            jr.setResult(e.getMessage());
        }
        return  jr;
    }


     //退回指定节点

    @RequestMapping("/backTaskNode")
    @ResponseBody
    @Transactional
    public JSONResult backTaskNode(String targetActivitiID,String taskid,String commentinfo,
               String assign,String reportName,HttpServletRequest request) {
        JSONResult jr=new JSONResult();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
            Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
            String selfActivitiID = task.getTaskDefinitionKey();
            String proinstanceId = task.getProcessInstanceId();
            if (targetActivitiID != null && proinstanceId != null && selfActivitiID != null) {
                ReadOnlyProcessDefinition processDefinitionEntity = (ReadOnlyProcessDefinition) repositoryService.
                        getProcessDefinition(task.getProcessDefinitionId());

                ActivityImpl destinationActivity = (ActivityImpl) processDefinitionEntity.findActivity(targetActivitiID);
                ActivityImpl currentActivity = (ActivityImpl) processDefinitionEntity.findActivity(selfActivitiID);

                String backState = ProcessUtils.getBackState(currentActivity, destinationActivity, task, historyService, runtimeService);
                //System.out.println("===========>流程回退类型别："+backState);
               if("000".equals(backState)){
                   jr.setMsg("001");
                   jr.setResult("不支持该类型的退回，请联系管理员");
               }else{
                   managerService.executeCommand(new CommonJumpTaskCmd(task.getExecutionId(), task.getProcessInstanceId(),
                           destinationActivity, null, currentActivity,jdbcTemplate,backState,assign));
                   String applicationActivitiId = ProcessUtils.getApplicationActivitiId(task.getProcessDefinitionId(), repositoryService);
                   if(applicationActivitiId.equals(destinationActivity.getId())){
                       //被退回到申请节点
                       runtimeService.setVariable(proinstanceId,"process_state","8");
                   }else{
                       runtimeService.setVariable(proinstanceId,"process_state","3");
                   }
                   //推送消息
                   HistoricProcessInstance hisProInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(proinstanceId).singleResult();
                   String  proname = hisProInstance==null ? "" : hisProInstance.getName();
                   //回退消息
                   Map<String,String> para=new HashMap<>();
                   HistoricProcessInstance proInstanceHis = historyService.createHistoricProcessInstanceQuery().
                           processInstanceId(task.getProcessInstanceId()).singleResult();
                   User userByUserName = UserService.getInstance().getUserByUserName(proInstanceHis.getStartUserId());
                   String realName="";
                   if(userByUserName!=null){
                       realName=userByUserName.getRealName();
                   }else{
                       List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().
                               processInstanceId(task.getProcessInstanceId()).list();
                       for(HistoricVariableInstance h:hisVarList){
                           if("process_userRealName".equals(h.getVariableName())){
                               realName=h.getValue().toString();
                           }
                       }
                   }

                   para.put("startPeople",realName);
                   para.put("startTime",sdf.format(proInstanceHis.getStartTime()));
                   Object proDueTime = runtimeService.getVariable(task.getProcessInstanceId(), "proDueTime");
                   para.put("proDueTime",proDueTime==null?"":proDueTime.toString());
                   para.put("shenheTime",sdf.format(new Date()));
                   sendMessage.getSendMessageUser(taskService,proinstanceId,jdbcTemplate,proname,para,"1",null);
                   //保存流程操作信息
                   jdbcTemplate.update(
                           "INSERT INTO proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime," +
                                   "opreateType,nodeName,mycomment,attachment,reportName) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                           new Object[]{ ProcessUtils.getUUID(),proinstanceId,taskid,userName,userRealName,new Date(),3,task.getName(),
                                   PreventXSS.delHTMLTag(commentinfo),"",reportName});
                   jr.setMsg("success");
                   jr.setResult("");

               }

           }else {
                jr.setMsg("000");
                jr.setResult("目标id为空");
            }

        }
        catch (Exception e){
            jr.setMsg("fail");
            jr.setResult(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }


    @RequestMapping("/downloadAttachment")
    public void downloadAttachment(String path,HttpServletResponse response,HttpServletRequest request)  throws IOException {
        OutputStream out = response.getOutputStream();
        String filePath=request.getSession().getServletContext().getRealPath("/").split(":")[0]+":"+File.separator
                +"attachment"+File.separator;
       String fileName="";
       if(path.contains("\\")){
           fileName=path.split("\\\\")[path.split("\\\\").length-1];
        }else{
           fileName=path.split("/")[path.split("/").length-1];
        }
        response.setContentType("text/html; charset=UTF-8"); //设置编码字符
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=" +
                URLEncoder.encode(fileName,"UTF-8"));
       /* response.setHeader("Content-disposition", "attachment;filename=" +
                URLEncoder.encode(path.split(File.separator)[path.split(File.separator).length-1],"UTF-8"));*/
        FileInputStream in = new FileInputStream(filePath+path);
        //创建缓冲区
        byte buffer[] = new byte[1024];
        int len = 0;
        //循环将输入流中的内容读取到缓冲区当中
        while ((len = in.read(buffer)) > 0) {
            //输出缓冲区的内容到浏览器，实现文件下载
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }


    /*当前节点添加会签人*/
    @RequestMapping("/addHuiQianAssgin")
    @ResponseBody
    @Transactional
    public JSONResult addHuiQianAssgin(String huiqians,String taskid,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName =UserService.getInstance().getUserByUserName(userName).getRealName();
            Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
            String executionId =task .getExecutionId();
            String[] split = huiqians.split(",");
            HistoricProcessInstance hisProIn = historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            for (String str : split) {
                managerService.executeCommand(new ShareniuCountersignAddCmd(executionId, str));
                Task task1 = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).
                        taskCandidateOrAssigned(str).singleResult();
                sendMessage.getSendMessageUserOnAddHuiQian(task1,hisProIn.getName(),jdbcTemplate,hisProIn.getStartUserId(),sdf.format(hisProIn.getStartTime()));
            }
            jdbcTemplate.update("insert into proopreateinfo(id,proInstanceId,taskid,opreateName,opreateRealName,opreateTime,opreateType,nodeName,mycomment,attachment,proname) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{ProcessUtils.getUUID(),task.getProcessInstanceId(),taskid,userName,userRealName,new Date(),10,task.getName(),"","",""});
            /*jr.setResult(processInstanceId+","+proname);*/

            jr.setResult("success");
        }
        catch (Exception e){
            jr.setResult("fail");
            jr.setMsg(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }


    //判断是否是并行节点中的一个（只有一对并行网关）
    public boolean isParallelGateway(String processDefinitionId,String activitiId){
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
        List<ActivityImpl> activities = processDefinitionEntity.getActivities();
        boolean flag=false;int count=1;
        List<String> result=new ArrayList<String>();
        for(int i=0;i<activities.size();i++){
            if("parallelGateway".equals(activities.get(i).getProperty("type"))){
                if(count==1){
                    flag=true;
                    count++;
                    continue;
                }
                else {
                    flag=false;
                    count++;
                    continue;
                }
            }
            if(flag){
                result.add(activities.get(i).getId());
            }

        }
     return result.contains(activitiId);


    }



    ///////////////////////////////////////////////
    @RequestMapping("/getbackTaskNodeInfoPeople")
    @ResponseBody
    public JSONResult getbackTaskNodeInfoPeople(String activityid,String ProcessInstanceId) {
        JSONResult jr=new JSONResult();
        try {
            List< Map<String,String> > result=new ArrayList<>();
            if (!"".equals(activityid)&&!"".equals(ProcessInstanceId)) {
                    UserService instance = UserService.getInstance();
                    //这里的代码只适应国药项目需要求
                List<HistoricTaskInstance> list11 = historyService.createHistoricTaskInstanceQuery().
                        processInstanceId(ProcessInstanceId).taskDefinitionKey(activityid).list();
                List<HistoricTaskInstance> list1=new ArrayList<>();
                List<String> name=new ArrayList<>();
                for(HistoricTaskInstance h:list11){
                    if(!name.contains(h.getAssignee())){
                        name.add(h.getAssignee());
                        list1.add(h);
                    }
                }
                    for(int k=0;k<list1.size();k++){
                        if(!"deleted".equals(list1.get(k).getDeleteReason())){
                            Map<String,String> map=new HashMap<>();
                            User userByUserName = instance.getUserByUserName(list1.get(k).getAssignee());
                            String realName = userByUserName==null?"":userByUserName.getRealName();
                            map.put("userid",list1.get(k).getAssignee());
                            map.put("user",list1.get(k).getAssignee()+"("+realName+")");
                            UserDetailInfoBean customRoleInfo = instance.getCustomRoleInfo(userByUserName);
                            String role="";
                           if(customRoleInfo!=null){
                               List<String> customRoleNames =customRoleInfo .getCustomRoleNames();
                               if(customRoleNames!=null){
                                   for(int m=0;m<customRoleNames.size();m++){
                                       role+=customRoleNames.get(m)+",";
                                   }
                               }

                           }
                            map.put("role",role.length()==0?"":role.substring(0,role.length()-1));
                           //获取部门
                            UserDetailInfoBean depAndPostInfo = instance.getDepAndPostInfo(userByUserName);
                            List<Map<String, String>> departmentPosts = depAndPostInfo.getDepartmentPosts()==null?new ArrayList<Map<String, String>>()
                                    :depAndPostInfo.getDepartmentPosts();
                            List<String> depByUserName = ProcessUtils.getDepByUserName(departmentPosts);
                            String dep="";
                            for(int n=0;n<depByUserName.size();n++){
                                dep+=depByUserName.get(n)+",";
                            }
                            map.put("dep",dep.length()==0?"":dep.substring(0,dep.length()-1));
                            result.add(map);
                        }
                    }
               // System.out.println(result);

                jr.setMsg("success");
                jr.setResult(result);
            } else {
                jr.setMsg("fail");
                jr.setResult("参数为空");
            }
        }
        catch (Exception e){
            jr.setMsg("fail");
            jr.setResult(e.getMessage());
        }

        return  jr;
    }
    @RequestMapping("/getbackTaskNodeInfoTest")
    @ResponseBody
    public JSONResult getbackTaskNodeInfoTest(String taskid) {
        JSONResult jr=new JSONResult();
        try {
            Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(task.getProcessDefinitionId());
            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                    .findActivity(task.getTaskDefinitionKey());
            List<ActivityImpl> rtnList = new ArrayList<>();
            List<ActivityImpl> tempList = new ArrayList<>();
            List<ActivityImpl> activities = iteratorBackActivity(taskid, currActivity, rtnList, tempList);
            List<Map<String,Object>> resultLilst=new ArrayList<>();
            for(ActivityImpl a:activities){
                Map<String,Object> map=new HashMap<>();
                map.put("id",a.getId());
                map.put("name",a.getProperty("name"));
                if(ProcessUtils.isHuiQianNodePallel(a.getId(),task.getProcessDefinitionId(),repositoryService)){
                    map.put("state","1");
                }else{
                    map.put("state","0");
                }
              /*  if(ProcessUtils.isHuiQianNode(a.getId(),task.getProcessDefinitionId(),repositoryService)){
                    UserService instance = UserService.getInstance();
                    //这里的代码只适应国药项目需要求
                    List<HistoricTaskInstance> list1 = historyService.createHistoricTaskInstanceQuery().
                            processInstanceId(task.getProcessInstanceId()).taskDefinitionKey(a.getId()).list();
                    for(int k=0;k<list1.size();k++){
                        if(!"deleted".equals(list1.get(k).getDeleteReason())){
                            if(!result.contains(list1.get(k).getAssignee())){
                                User userByUserName = instance.getUserByUserName(list1.get(k).getAssignee());
                                String realName = userByUserName.getRealName();
                                result.add(list1.get(k).getAssignee()+"("+realName+")");
                            }
                        }
                    }
                }*/
               // map.put("assign",result);
                resultLilst.add(map);
               // System.out.println(result);
            }
            jr.setResult(resultLilst);
            jr.setMsg("success");
        }
        catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
        }
        return jr;
    }


    public   List<ActivityImpl> getBackElementInfo(String processDefinitionId, String cruuentId, String taskid) throws Exception {
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinitionId);
        ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(cruuentId);
        List<ActivityImpl> rtnList = new ArrayList<>();
        List<ActivityImpl> tempList = new ArrayList<>();
        List<ActivityImpl> activities = iteratorBackActivity(taskid, currActivity, rtnList, tempList);
        return activities;
    }



    private  List<ActivityImpl> iteratorBackActivity(String taskId,
                                                     ActivityImpl currActivity, List<ActivityImpl> rtnList,
                                                     List<ActivityImpl> tempList) throws Exception {
        //System.out.println("iteratorBackActivity");
        // 查询流程定义，生成流程树结构
        ProcessInstance processInstance = findProcessInstanceByTaskId(taskId);

        // 当前节点的流入来源
        List<PvmTransition> incomingTransitions = currActivity
                .getIncomingTransitions();
        // 条件分支节点集合，userTask节点遍历完毕，迭代遍历此集合，查询条件分支对应的userTask节点
        List<ActivityImpl> exclusiveGateways = new ArrayList<ActivityImpl>();
        // 并行节点集合，userTask节点遍历完毕，迭代遍历此集合，查询并行节点对应的userTask节点
        List<ActivityImpl> parallelGateways = new ArrayList<ActivityImpl>();
        // 遍历当前节点所有流入路径
        for (PvmTransition pvmTransition : incomingTransitions) {
           if(pvmTransition.getId()!=null){
               TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
               ActivityImpl activityImpl = transitionImpl.getSource();
               String type = (String) activityImpl.getProperty("type");
               /**
                * 并行节点配置要求：<br>
                * 必须成对出现，且要求分别配置节点ID为:XXX_start(开始)，XXX_end(结束)
                */
               if ("parallelGateway".equals(type)) {// 并行路线
                   String gatewayId = activityImpl.getId();
                   String gatewayType = gatewayId.substring(gatewayId
                           .lastIndexOf("_") + 1);
                   if ("START".equals(gatewayType.toUpperCase())) {// 并行起点，停止递归
                       return rtnList;
                   } else {// 并行终点，临时存储此节点，本次循环结束，迭代集合，查询对应的userTask节点
                       parallelGateways.add(activityImpl);
                   }
               } else if ("startEvent".equals(type)) {// 开始节点，停止递归
                   return rtnList;
               } else if ("userTask".equals(type)) {// 用户任务
                   tempList.add(activityImpl);
               } else if ("exclusiveGateway".equals(type)) {// 分支路线，临时存储此节点，本次循环结束，迭代集合，查询对应的userTask节点
                   currActivity = transitionImpl.getSource();
                   exclusiveGateways.add(currActivity);
               }
           }
        }

        /**
         * 迭代条件分支集合，查询对应的userTask节点
         */
        for (ActivityImpl activityImpl : exclusiveGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);
        }

        /**
         * 迭代并行集合，查询对应的userTask节点
         */
        for (ActivityImpl activityImpl : parallelGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);
        }

        /**
         * 根据同级userTask集合，过滤最近发生的节点
         */
        currActivity = filterNewestActivity(processInstance, tempList);
        if (currActivity != null) {
            // 查询当前节点的流向是否为并行终点，并获取并行起点ID
            String id = findParallelGatewayId(currActivity);
            if (id == null || id.equals("")) {// 并行起点ID为空，此节点流向不是并行终点，符合驳回条件，存储此节点
                rtnList.add(currActivity);
            } else {// 根据并行起点ID查询当前节点，然后迭代查询其对应的userTask任务节点
                currActivity = findActivitiImpl(taskId, id);
            }

            // 清空本次迭代临时集合
            tempList.clear();
            // 执行下次迭代
            iteratorBackActivity(taskId, currActivity, rtnList, tempList);
        }
        return rtnList;
    }






    private  ActivityImpl findActivitiImpl(String taskId, String activityId)
            throws Exception {
        // 取得流程定义
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);

        // 获取当前活动节点ID
        if (activityId == null || "".equals(activityId)) {
            activityId = findTaskById(taskId).getTaskDefinitionKey();
        }

        // 根据流程定义，获取该流程实例的结束节点
        if (activityId.toUpperCase().equals("END")) {
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {
                List<PvmTransition> pvmTransitionList = activityImpl
                        .getOutgoingTransitions();
                if (pvmTransitionList.isEmpty()) {
                    return activityImpl;
                }
            }
        }

        // 根据节点ID，获取对应的活动节点
        ActivityImpl activityImpl = ((ProcessDefinitionImpl) processDefinition)
                .findActivity(activityId);

        return activityImpl;
    }

    private  ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(
            String taskId) throws Exception {
        // 取得流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(findTaskById(taskId)
                        .getProcessDefinitionId());

        if (processDefinition == null) {
            throw new Exception("流程定义未找到!");
        }

        return processDefinition;
    }




    private  String findParallelGatewayId(ActivityImpl activityImpl) {
        List<PvmTransition> incomingTransitions = activityImpl
                .getOutgoingTransitions();
        for (PvmTransition pvmTransition : incomingTransitions) {
            TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
            activityImpl = transitionImpl.getDestination();
            String type = (String) activityImpl.getProperty("type");
            if ("parallelGateway".equals(type)) {// 并行路线
                String gatewayId = activityImpl.getId();
                String gatewayType = gatewayId.substring(gatewayId
                        .lastIndexOf("_") + 1);
                if ("END".equals(gatewayType.toUpperCase())) {
                    return gatewayId.substring(0, gatewayId.lastIndexOf("_"))
                            + "_start";
                }
            }
        }
        return null;
    }


    private  ActivityImpl filterNewestActivity(ProcessInstance processInstance,
                                               List<ActivityImpl> tempList) {
        while (tempList.size() > 0) {
            ActivityImpl activity_1 = tempList.get(0);
            HistoricActivityInstance activityInstance_1 = findHistoricUserTask(
                    processInstance, activity_1.getId());
            if (activityInstance_1 == null) {
                tempList.remove(activity_1);
                continue;
            }

            if (tempList.size() > 1) {
                ActivityImpl activity_2 = tempList.get(1);
                HistoricActivityInstance activityInstance_2 = findHistoricUserTask(
                        processInstance, activity_2.getId());
                if (activityInstance_2 == null) {
                    tempList.remove(activity_2);
                    continue;
                }

                if (activityInstance_1.getEndTime().before(
                        activityInstance_2.getEndTime())) {
                    tempList.remove(activity_1);
                } else {
                    tempList.remove(activity_2);
                }
            } else {
                break;
            }
        }
        if (tempList.size() > 0) {
            return tempList.get(0);
        }
        return null;
    }

    private  HistoricActivityInstance findHistoricUserTask(
            ProcessInstance processInstance, String activityId) {
        HistoricActivityInstance rtnVal = null;
        // 查询当前流程实例审批结束的历史节点
        List<HistoricActivityInstance> historicActivityInstances =historyService
                .createHistoricActivityInstanceQuery().activityType("userTask")
                .processInstanceId(processInstance.getId()).activityId(
                        activityId).finished()
                .orderByHistoricActivityInstanceEndTime().desc().list();
        if (historicActivityInstances.size() > 0) {
            rtnVal = historicActivityInstances.get(0);
        }

        return rtnVal;
    }


    private  ProcessInstance findProcessInstanceByTaskId(String taskId)
            throws Exception {
        // 找到流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(
                findTaskById(taskId).getProcessInstanceId())
                .singleResult();
        if (processInstance == null) {
            throw new Exception("流程实例未找到!");
        }
        return processInstance;
    }

    private TaskEntity findTaskById(String taskId) throws Exception {
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(
                taskId).singleResult();
        if (task == null) {
            throw new Exception("任务实例未找到!");
        }
        return task;
    }



}
