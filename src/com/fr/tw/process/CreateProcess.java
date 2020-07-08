package com.fr.tw.process;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fr.decision.webservice.v10.login.LoginService;
import com.fr.decision.webservice.v10.user.UserService;
import com.fr.json.JSON;
import com.fr.tw.custom.*;

import com.fr.tw.util.JSONResult;
import com.fr.tw.util.ProcessUtils;
import com.fr.tw.util.jobUtil;
import io.netty.handler.codec.http.HttpResponse;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/processDiagram")
public class CreateProcess {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;


    //画流程图
    @RequestMapping("/create")
    public String create(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String currentUserName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String register=response.getHeader("register");
            String time=response.getHeader("time");

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();
            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "流程图");
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            String description = "";
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName("Activi Molder");
            modelData.setKey("tw");
            modelData.setTenantId(currentUserName);
            //保存模型
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
            return "redirect:/modeler.html?modelId=" + modelData.getId()+"&modelerStatus=true&param=&register="+register+"&time="+time;
           // response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
        } catch (Exception e) {
            request.setAttribute("message","创建模型失败，错误信息："+e.getMessage());
            return "/static/jsp/message.jsp";
        }
    }


    //获取流程图分类
    @RequestMapping("/getClassify")
    @ResponseBody
    public  List<Map<String, Object>> getClassify(HttpServletRequest request) throws Exception {
        String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
        List<Map<String, Object>> list=new ArrayList<>();
        if(ProcessUtils.isAdmin(userName)){
            list = jdbcTemplate.queryForList("SELECT * FROM classify");
        }else{
            list = jdbcTemplate.queryForList("SELECT * FROM classify WHERE tenantid=?",new Object[]{userName});
        }
        List<Map<String, Object>> result=new ArrayList<>();
        UserService instance = UserService.getInstance();
        for(int i=0;i<list.size();i++){
            Map<String, Object> map=new HashMap<>();
           map.put("id",list.get(i).get("id").toString());
            map.put("classifyname",list.get(i).get("classifyname").toString());
            com.fr.decision.authority.data.User u = instance.getUserByUserName(list.get(i).get("tenantid").toString());
            map.put("tenantidname",u==null ? list.get(i).get("tenantid")+"("+""+")" :
                    list.get(i).get("tenantid")+"("+u.getRealName()+")");
            map.put("tenantid",list.get(i).get("tenantid"));
            result.add(map);
        }
        return result;
    }


    @RequestMapping("/shanchuClassify")
    @ResponseBody
    public String shanchuClassify(String id){
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM ACT_RE_DEPLOYMENT WHERE CATEGORY_=?", new Object[]{id});
        String param="%\"description\""+":"+"\""+id+"\""+"%";
       // System.out.println(param);
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList("SELECT * FROM ACT_RE_MODEL WHERE META_INFO_ LIKE ?", new Object[]{param});
        if(list.size()==0 && list1.size()==0){
            jdbcTemplate.update("DELETE FROM classify WHERE id=?",new Object[]{id});
            return "1";
        }else{
            return "0";
        }


    }

    @RequestMapping("/bancunClassify")
    @ResponseBody
    public String bancunClassify(String id,String name,HttpServletRequest request){
        String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
        //
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM classify WHERE  classifyname=?", new Object[]{name});
        if(list.size()>0){
            return "0";
        }else{
            jdbcTemplate.update("INSERT INTO classify(id,classifyname,tenantid) VALUES (?,?,?)",new Object[]{id,name,userName});
            return "1";
        }

    }

    @RequestMapping("/xiugaiClassify")
    @ResponseBody
    public String xiugaiClassify(String id,String name){
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM classify WHERE  classifyname=?", new Object[]{name});
            if (list.size() > 0) {
               return  "0";
            } else {

                jdbcTemplate.update("UPDATE  classify SET classifyname=?  WHERE id=?", new Object[]{name, id});
                return  "1";
            }
    }



    @RequestMapping({"/getzuzhiJson"})
    @ResponseBody
    public Map<String, Object> getzuzhiJson(String state, HttpServletRequest request) throws Exception {
        Map<String, Object> map = new HashMap();
        List<String> adminUserNameList = UserService.getInstance().getAdminUserNameList();
        Map<String, Object> allUsers = UserService.getInstance().getAllUsers(adminUserNameList.get(0), 1, 10000, "", "", false);
        map.put("code", Integer.valueOf(0));
        map.put("msg", "");
        map.put("count", Integer.valueOf(1000));
        map.put("data", allUsers.get("items"));
        return map;
    }



    /**
     * 查询模型此时还没有部署
     */
    @RequestMapping("/selectMolder")
    @ResponseBody
    public JSONResult selectMolder(String num,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        List<Map<String,Object>> list=new ArrayList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            String userRealName = UserService.getInstance().getUserByUserName(userName).getRealName();
            Integer yeshu=Integer.valueOf(num);
            List<Model> listMolderTemp = new ArrayList<>();
            if(ProcessUtils.isAdmin(userName)){
                listMolderTemp= repositoryService.createModelQuery().
                        orderByCreateTime().desc().list();
            }else {
                listMolderTemp= repositoryService.createModelQuery().modelTenantId(userName).
                        orderByCreateTime().desc().list();
            }
            List<Model> listMolderTemp1=new ArrayList<>();
            for (Model model:listMolderTemp) {
                if(!("Activi Molder".equals(model.getName()))){
                    listMolderTemp1.add(model);
                }
            }
            List<Model> listMolder=ProcessUtils.getModelByYeShu(listMolderTemp1,yeshu);
            for (Model model:listMolder) {
                Map<String,Object> map=new HashMap<>();
                if(!("Activi Molder".equals(model.getName()))){
                    map.put("molderId",model.getId());
                    map.put("molderName",model.getName());
                    map.put("createTime",sdf.format(model.getCreateTime()));
                    map.put("lastUpdataTime",sdf.format(model.getLastUpdateTime()));
                    String metaInfo = model.getMetaInfo();
                    JSONObject jsonObject=new JSONObject(metaInfo);
                    List<Map<String, Object>> list1 = jdbcTemplate.queryForList("SELECT * FROM classify WHERE id=?", new Object[]{jsonObject.getString("description")});
                    String classfifyName="";
                    String classfifyNameid="";
                    if(list1.size()!=0){
                        classfifyName=list1.get(0).get("classifyname")==null?"":list1.get(0).get("classifyname").toString();
                        classfifyNameid=list1.get(0).get("id")==null?"":list1.get(0).get("id").toString();
                        map.put("classfifyNameid",classfifyNameid);
                    }
                    map.put("metaInfo",classfifyName);
                    //1：已经部署  0：未部署
                    if(model.getCategory()==null || "".equals(model.getCategory())){
                        map.put("status","0");
                    }else{
                        if("deployed".equals(model.getCategory())){
                            map.put("status","1");
                        }else{
                            map.put("status","2");
                        }
                    }
                    if(model.getTenantId().equals(userName)){
                        map.put("authority","1");
                    }else{
                        map.put("authority","0");
                    }
                    map.put("createPeople",model.getTenantId());
                    map.put("userName",userName);
                    map.put("userRealName",userRealName);
                    list.add(map);
                }

            }
            jr.setMsg("success");
            jr.setResult(list);
            jr.setTotal(listMolderTemp1.size());
            if(listMolderTemp1.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(listMolderTemp1.size()%10==0 ? listMolderTemp1.size()/10 : listMolderTemp1.size()/10+1);
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult(e.getMessage());
        }
        return jr;
    }

    @RequestMapping("/updateModel")
    @ResponseBody
    public String updateModelCategory(String modelid,HttpServletRequest request) throws Exception {
        Model modelData = repositoryService.getModel(modelid);
        String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
        if(modelData.getTenantId().equals(currentUserNameFromRequestCookie) || ProcessUtils.isAdmin(currentUserNameFromRequestCookie)){
           int i=jdbcTemplate.update("UPDATE ACT_RE_MODEL SET CATEGORY_='update' WHERE ID_=?",new Object[]{modelid});
           if(i>0){
               return "success";
           }else{
               return "fail";
           }
       }else{
           return "001";
       }
    }

    @RequestMapping("/fabu")
    @ResponseBody
    @Transactional
    public JSONResult fabu(String modelid,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        try {
            Model modelData = repositoryService.getModel(modelid);
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            if(modelData.getTenantId().equals(currentUserNameFromRequestCookie) || ProcessUtils.isAdmin(currentUserNameFromRequestCookie)) {
                Model model = repositoryService.createModelQuery().modelId(modelid).singleResult();
                String deployid = model.getDeploymentId();
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployid).singleResult();
                String processDefinitionId = processDefinition.getId();
                String proClassify=modelData.getMetaInfo().split("\"description\":")[1].
                        replace("\"","").replace("}","");
                String proName=modelData.getMetaInfo().split("\"name\":")[1].
                        replace("\"","").replace("}","").split(",")[0];
                BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                BpmnJsonConverterProperties bjcp = new BpmnJsonConverterProperties();
                //任务节点
                CustomUserTaskJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                //开始节点
                CustomStartEventJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                //连线
                CustomSequenceFlowJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                //边界定时
                CustomBoundaryEventJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                JsonNode modelNode = (JsonNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
                BpmnModel bpmodel = jsonConverter.convertToBpmnModel(modelNode);

                byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmodel);
                LobHandler lobHandler = new DefaultLobHandler();
                String sql = "UPDATE ACT_GE_BYTEARRAY SET BYTES_=?  WHERE DEPLOYMENT_ID_=? AND  NAME_ LIKE '%bpmn20.xml' ";
                jdbcTemplate.execute(sql, new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                    @Override
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException, DataAccessException {
                        lobCreator.setBlobAsBytes(ps, 1, bpmnBytes);
                        ps.setString(2, deployid);
                    }
                });

                ((ProcessEngineConfigurationImpl) processEngineConfiguration)
                        .getProcessDefinitionCache().remove(processDefinitionId);
                DeploymentCache<BpmnModel> bpmnModelCache = ((ProcessEngineConfigurationImpl) processEngineConfiguration).getDeploymentManager().getBpmnModelCache();
                bpmnModelCache.remove(processDefinitionId);
                jdbcTemplate.update("UPDATE ACT_RE_MODEL SET CATEGORY_='deployed',LAST_UPDATE_TIME_=? WHERE ID_=?", new Object[]{new Date(), modelid});
                jdbcTemplate.update("UPDATE ACT_RE_DEPLOYMENT SET NAME_=?  WHERE ID_=?", new Object[]{proName, deployid});
                repositoryService.setDeploymentCategory(deployid,proClassify);
                jr.setResult("");
                jr.setMsg("success");
            }else {
                jr.setResult("");
                jr.setMsg("0");
            }
        }catch (Exception e){
            jr.setResult(e.getMessage());
            jr.setMsg("fail");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return  jr;
    }


    /**
     * 查询部署
     */
    @RequestMapping("/selectDeployment")
    @ResponseBody
    public JSONResult selectDeployment(String num,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        List<Map<String,Object>> list=new ArrayList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Integer yeshu=Integer.valueOf(num);
        try {
            String userName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            List<Deployment> listDeploymentTemp = new ArrayList<>();
            if(ProcessUtils.isAdmin(userName)){
                listDeploymentTemp=  repositoryService.createDeploymentQuery()
                        .orderByDeploymenTime().desc().list();
            }else{
                listDeploymentTemp=  repositoryService.createDeploymentQuery().deploymentTenantId(userName)
                        .orderByDeploymenTime().desc().list();
            }
            List<Deployment> listDeployment=ProcessUtils.getDeploymentByYeShu(listDeploymentTemp,yeshu);
            for (Deployment deployment:listDeployment) {
                Map<String,Object> map=new HashMap<>();
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                        deploymentId(deployment.getId()).singleResult();
                Model model = repositoryService.createModelQuery().deploymentId(deployment.getId()).singleResult();
                if(processDefinition!=null && model!=null){
                    map.put("DeploymentId",deployment.getId());
                    map.put("DeploymentName",deployment.getName());
                    //map.put("DeploymentKey",processDefinition.getKey());
                    map.put("version",processDefinition.getVersion());
                    map.put("processDefinitionId",processDefinition.getId());
                    List<Map<String, Object>> list1 = jdbcTemplate.queryForList("" +
                            "SELECT * FROM classify WHERE id=?", new Object[]{deployment.getCategory()});
                    String classfifyName="";
                    if(list1.size()!=0){
                        classfifyName=list1.get(0).get("classifyname").toString();
                    }
                    map.put("DeploymentProclassify",classfifyName);
                    map.put("DeploymentTime",sdf.format(deployment.getDeploymentTime()));
                    if(deployment.getTenantId().equals(userName) || ProcessUtils.isAdmin(userName)){
                        map.put("authority","1");
                    }else{
                        map.put("authority","0");
                    }
                    map.put("createPeople",deployment.getTenantId());
                    list.add(map);
                }


            }
            jr.setMsg("success");
            jr.setResult(list);
            jr.setTotal(listDeploymentTemp.size());
            if(listDeploymentTemp.size()<=10){
                jr.setYeshu(1);
            }else{
                jr.setYeshu(listDeploymentTemp.size()%10==0 ? listDeploymentTemp.size()/10 : listDeploymentTemp.size()/10+1);
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }

    /**
     * 添加定时
     */
    @RequestMapping("/addDingShi")
    @ResponseBody
    public JSONResult addDingShi(String id,String cron){
        JSONResult jr=new JSONResult();
        try {
            ProcessDefinition processDefinition =repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
             String cron1="{\"cron\": "+"\""+cron+"\""+",\"nextTime\": \"\",\"state\": \"0\"}";//1:启动运行中，0:停止
            boolean validExpression = CronExpression.isValidExpression(cron);
            if(validExpression){
                String applicationActivitiId = ProcessUtils.getApplicationActivitiId(processDefinition.getId(), repositoryService);
                ActivityImpl activityImpl = ProcessUtils.getActivityImplByActivitiId(applicationActivitiId, processDefinition.getId(), repositoryService);
                TaskDefinition task =(TaskDefinition) activityImpl.getProperty("taskDefinition");
                String expressionText = task.getAssigneeExpression().getExpressionText();
                if(expressionText.contains("$") || expressionText.contains("{") || expressionText.contains("}")){
                    jr.setMsg("002");
                    jr.setResult("定时任务的流程一个任务节点办理人必须是确定的人，不能是${}变量模式");
                }else{
                    jdbcTemplate.update("UPDATE ACT_RE_PROCDEF SET DESCRIPTION_=? WHERE ID_=?",new Object[]{cron1,processDefinition.getId()});
                    jr.setMsg("success");
                    jr.setResult("");
                }

            }else{
                jr.setMsg("001");
                jr.setResult("cron表达式格式错误");
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }

    /**
     * 查询定时
     */
    @RequestMapping("/getDingshi")
    @ResponseBody
    public JSONResult getDingshi(HttpServletRequest request){
        JSONResult jr=new JSONResult();
        List<Map<String,Object>> list=new ArrayList<>();
        try {
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            List<Deployment> listDeploymentTemp = new ArrayList<>();
            if(ProcessUtils.isAdmin(currentUserNameFromRequestCookie)){
                listDeploymentTemp=repositoryService.createDeploymentQuery().orderByDeploymenTime().desc().list();
            }else {
                listDeploymentTemp=repositoryService.createDeploymentQuery().orderByDeploymenTime().
                        deploymentTenantId(currentUserNameFromRequestCookie).desc().list();
            }
            for (Deployment deployment:listDeploymentTemp) {
                Map<String,Object> map=new HashMap<>();
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                        deploymentId(deployment.getId()).singleResult();
                if(processDefinition!=null && processDefinition.getDescription()!=null){
                    String description = processDefinition.getDescription();
                    if(description==null || "".equals(description)){

                    }else{
                        JSONObject json=new JSONObject(description);
                        map.put("id",processDefinition.getId());
                        map.put("name",deployment.getName());
                        map.put("cron",json.get("cron")==null?"":json.get("cron").toString());
                        map.put("nextTime",json.get("nextTime")==null?"":json.get("nextTime").toString());
                        map.put("state",json.get("state")==null?"":json.get("state").toString());
                        list.add(map);
                    }

                }

            }
            jr.setMsg("success");
            jr.setResult(list);
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }

    /**
     * 暂停定时
     */
    @RequestMapping("/stopDingShi")
    @ResponseBody
    public JSONResult stopDingShi(String id){
        JSONResult jr=new JSONResult();
        try {
            if(!"".equals(id) && id!=null){
                StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                Scheduler scheduler = stdSchedulerFactory.getScheduler();
               // scheduler.pauseJob(JobKey.jobKey(id+"_cronJob"));
                scheduler.deleteJob(JobKey.jobKey(id+"_cronJob"));
                ProcessDefinition processDefinition =repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
                String description = processDefinition.getDescription();
                if(description==null || "".equals(description)){
                    description="{}";
                }
                JSONObject json=new JSONObject(description);
                json.put("state","0");
                json.put("nextTime","");
                jdbcTemplate.update("UPDATE ACT_RE_PROCDEF SET DESCRIPTION_=? WHERE ID_=?",new Object[]{json.toString(),
                        processDefinition.getId()});
                jr.setMsg("success");
                jr.setResult("");
            }else{
                jr.setMsg("fail");
                jr.setResult("流程实例id为空");
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }

    /**
     * 启动定时
     */
    @RequestMapping("/startDingShi")
    @ResponseBody
    public JSONResult startDingShi(String id,String proname){
        JSONResult jr=new JSONResult();
        List<Map<String,Object>> list=new ArrayList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(!"".equals(id) && id!=null && !"".equals(proname) && proname!=null){
                ProcessDefinition processDefinition =repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
                String description = processDefinition.getDescription();
                if(description==null || "".equals(description)){
                    description="{}";
                }
                JSONObject json=new JSONObject(description);
                String cron=json.get("cron")==null?"":json.get("cron").toString();
                //启动定时流程
                JobDetail jobDetail = JobBuilder.newJob(jobUtil.class)
                        .withIdentity(id+"_cronJob")
                        .usingJobData("prodefinedid",id)
                        .usingJobData("proname",proname)
                        .build();
                CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(id+"_cronTrigger").
                        withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
                Date startTime = cronTrigger.getStartTime();
                String nextTime=sdf.format(cronTrigger.getFireTimeAfter(startTime));
                StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                Scheduler scheduler = stdSchedulerFactory.getScheduler();
                scheduler.scheduleJob(jobDetail,cronTrigger);
                scheduler.start();

                json.put("state","1");
                json.put("nextTime",nextTime);
                jdbcTemplate.update("UPDATE ACT_RE_PROCDEF SET DESCRIPTION_=? WHERE ID_=?",new Object[]{json.toString(),
                        processDefinition.getId()});
                jr.setMsg(nextTime);
                jr.setResult("");
            }else{
                jr.setMsg("fail");
                jr.setResult("参数流程实例id或流程名称为空");
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }


    /**
     * 删除定时
     */
    @RequestMapping("/deleteDingShi")
    @ResponseBody
    public JSONResult deleteDingShi(String id){
        JSONResult jr=new JSONResult();
        List<Map<String,Object>> list=new ArrayList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(!"".equals(id) && id!=null){
                ProcessDefinition processDefinition =repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
                String description = processDefinition.getDescription();
                if(description==null || "".equals(description)){
                    description="{}";
                }
                JSONObject json=new JSONObject(description);
                if("1".equals(json.get("state").toString())){
                    jr.setMsg("001");
                    jr.setResult("");
                }else{
                    jdbcTemplate.update("UPDATE ACT_RE_PROCDEF SET DESCRIPTION_=? WHERE ID_=?",new Object[]{null,
                            processDefinition.getId()});
                    jr.setMsg("success");
                    jr.setResult("");
                }

            }else{
                jr.setMsg("fail");
                jr.setResult("流程实例id为空");
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }


    /**
     * 检查是否能修改定时时间
     */
    @RequestMapping("/checkXiuGai")
    @ResponseBody
    public JSONResult checkXiuGai(String id){
        JSONResult jr=new JSONResult();
        try {
            if(!"".equals(id) && id!=null){
                ProcessDefinition processDefinition =repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
                String description = processDefinition.getDescription();
                if(description==null || "".equals(description)){
                    description="{}";
                }
                JSONObject json=new JSONObject(description);
                if("1".equals(json.get("state").toString())){
                    jr.setMsg("001");
                    jr.setResult("");
                }else{
                    jr.setMsg("success");
                    jr.setResult("");
                }

            }else{
                jr.setMsg("fail");
                jr.setResult("流程实例id为空");
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }


    /**
     * 修改定时时间
     */
    @RequestMapping("/xiugaiCron")
    @ResponseBody
    public JSONResult xiugaiCron(String id,String cron){
        JSONResult jr=new JSONResult();
        try {
            if(!"".equals(id) && id!=null){
                ProcessDefinition processDefinition =repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
                String description = processDefinition.getDescription();
                if(description==null || "".equals(description)){
                    description="{}";
                }
                JSONObject json=new JSONObject(description);
                json.put("cron",cron);
                jdbcTemplate.update("UPDATE ACT_RE_PROCDEF SET DESCRIPTION_=? WHERE ID_=?",new Object[]{json.toString(),
                            processDefinition.getId()});
                jr.setMsg("success");
                jr.setResult("");


            }else{
                jr.setMsg("fail");
                jr.setResult("流程实例id为空");
            }
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }



    /**
     * 查询部署
     */
    @RequestMapping("/selectDeployment1")
    @ResponseBody
    public JSONResult selectDeployment1(HttpServletRequest request){
        JSONResult jr=new JSONResult();
        String currentUserName = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
        List<Map<String,Object>> list=new ArrayList<>();
        try {
            List<Deployment> listDeploymentTemp=new ArrayList<>();
            if(ProcessUtils.isAdmin(currentUserName)){
                listDeploymentTemp  = repositoryService.createDeploymentQuery().orderByDeploymenTime().desc().list();
            }else{
                listDeploymentTemp  = repositoryService.createDeploymentQuery().
                        deploymentTenantId(currentUserName).orderByDeploymenTime().desc().list();
            }

            for (Deployment deployment:listDeploymentTemp) {
                Map<String,Object> map=new HashMap<>();
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                        deploymentId(deployment.getId()).singleResult();
                if(processDefinition!=null && processDefinition.getDescription()==null){
                    map.put("id",deployment.getId());
                    map.put("name",deployment.getName());
                    map.put("classify",deployment.getCategory());
                    list.add(map);
                }

            }
            jr.setMsg("success");
            jr.setResult(list);
        }catch (Exception e){
            jr.setMsg("fail");
            jr.setResult("异常："+e.getMessage());
        }
        return jr;
    }

    /**
     * 删除模型
     */
    @RequestMapping("/deleteMolder")
    @ResponseBody
    public JSONResult deleteMolder(String modelId,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        try {
            Model modelData = repositoryService.getModel(modelId);
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            if(modelData.getTenantId().equals(currentUserNameFromRequestCookie) || ProcessUtils.isAdmin(currentUserNameFromRequestCookie)){
                Model model = repositoryService.createModelQuery().modelId(modelId).singleResult();
                if(!"".equals(model.getCategory()) && model.getCategory()!=null){
                    jr.setMsg("2");
                }else{
                    repositoryService.deleteModel(modelId);
                    jr.setMsg("1");
                }
            }else{
                jr.setMsg("3");
            }


        }catch (Exception e){
            jr.setMsg("0");
            jr.setResult(e.getMessage());
        }
        return jr;
    }

    /**
     * 删除部署
     */
    @RequestMapping("/deleteDeploy")
    @ResponseBody
    @Transactional
    public JSONResult deleteDeploy(String deployId,HttpServletRequest request){
        JSONResult jr=new JSONResult();
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            if(ProcessUtils.isAdmin(currentUserNameFromRequestCookie) || processDefinition.getTenantId().equals(currentUserNameFromRequestCookie)){
                if(processDefinition.getDescription()==null || "".equals(processDefinition.getDescription())){
                    String modelid = repositoryService.createModelQuery().deploymentId(deployId).singleResult().getId();
                    jdbcTemplate.update("UPDATE ACT_RE_MODEL SET CATEGORY_='' WHERE ID_=?",new Object[]{modelid});
                    repositoryService.deleteDeployment(deployId,true);
                    jr.setMsg("1");
                }else{
                    jr.setMsg("3");
                }

            }else
            {
                jr.setMsg("2");

            }

        }catch (Exception e){
            jr.setMsg("0");
            jr.setResult(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return jr;
    }



    /**
     * 根据Model部署流程,
     */
    @RequestMapping(value = "/deploy")
    @ResponseBody
    @Transactional
    public JSONResult deploy(String modelId,HttpServletRequest request) throws IOException {
        JSONResult jr=new JSONResult();
        try {
            //部署流程
            Model modelData = repositoryService.getModel(modelId);
            String currentUserNameFromRequestCookie = LoginService.getInstance().getCurrentUserNameFromRequestCookie(request);
            if(modelData.getTenantId().equals(currentUserNameFromRequestCookie) || ProcessUtils.isAdmin(currentUserNameFromRequestCookie)){
                String proClassify=modelData.getMetaInfo().split("\"description\":")[1].
                        replace("\"","").replace("}","");
                byte[] bpmnBytes = null;
                BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                BpmnJsonConverterProperties bjcp = new BpmnJsonConverterProperties();
                //扩展属性，成功
                //任务节点
                CustomUserTaskJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                //开始节点
                 CustomStartEventJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                //连线
                CustomSequenceFlowJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                //边界定时
                CustomBoundaryEventJsonConverter.fillTypes(bjcp.getconvertersToBpmnMap(),bjcp.getconvertersToJsonMap());
                JsonNode modelNode = (JsonNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
                BpmnModel model = jsonConverter.convertToBpmnModel(modelNode);
                bpmnBytes = new BpmnXMLConverter().convertToXML(model);
                String processName = modelData.getName() + ".bpmn20.xml";
                Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).
                        addString(processName, new String(bpmnBytes,"UTF-8")).tenantId(currentUserNameFromRequestCookie).deploy();
                //部署成功之后往部署molder表中插入部署Category
                repositoryService.setDeploymentCategory(deployment.getId(),proClassify);
                //流程定义设置是哪个模型部署的（一个模型只能部署一次）
                jdbcTemplate.update("UPDATE ACT_RE_MODEL SET CATEGORY_='deployed',DEPLOYMENT_ID_=? WHERE ID_=?",new Object[]{deployment.getId(),modelId});
            /*repositoryService.setProcessDefinitionCategory(repositoryService.createProcessDefinitionQuery().
                    deploymentId(deployment.getId()).singleResult().getId(),modelId);*/
                //jdbcTemplate.update("insert INTO modelanddeploy(id,modelid,deployid,proclassify) VALUES(?,?,?,?)",new Object[]{id,modelId,deployment.getId(),proClassify});
                jr.setMsg("1");
            }else{
                jr.setMsg("2");
            }
        } catch (Exception e) {
            jr.setResult(e.getMessage());
            jr.setMsg("0");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
       return jr;
    }

}
