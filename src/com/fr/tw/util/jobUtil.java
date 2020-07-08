package com.fr.tw.util;

import com.fr.decision.authority.data.User;
import com.fr.decision.webservice.v10.user.UserService;
import com.fr.web.core.A.E;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.quartz.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class jobUtil implements Job{


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String startTime="";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String prodefinedid = jobDataMap.getString("prodefinedid");
        String proname=jobDataMap.getString("proname");
        JdbcTemplate jdbcTemplate=(JdbcTemplate) SpringContextUtil.getBean("jdbcTemplate");
        RepositoryService repositoryService=(RepositoryService) SpringContextUtil.getBean("repositoryService");
        RuntimeService runtimeService=(RuntimeService) SpringContextUtil.getBean("runtimeService");
        TaskService taskService=(TaskService) SpringContextUtil.getBean("taskService");
        /*System.out.println("现在的时间是："+ sdf.format(date));
        System.out.println(prodefinedid+"=====>启动流程。。。。。");
*/
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(prodefinedid).singleResult();
        //初始化会签节点操作者信息
        Map<String,Object> map = ProcessUtils.initHuiQian(prodefinedid,repositoryService);
        Map<String, String> mapFormKeyAndName = ProcessUtils.getApplicationFormKeyAndName(prodefinedid, repositoryService);
        map.put("process_state","1");
        //添加模板,这个一定要放在变量中
        map.put("process_formKey",mapFormKeyAndName.get("formkey"));
        //添加流程发起人
        try {
            User userByUserName = UserService.getInstance().getUserByUserName(processDefinition.getTenantId());
        String userRealName=userByUserName==null?"":userByUserName.getRealName();
        map.put("process_userRealName",userRealName);
        Authentication.setAuthenticatedUserId(processDefinition.getTenantId());
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(prodefinedid, ProcessUtils.getUUID(), map);
        startTime=sdf1.format(new Date());
        runtimeService.setProcessInstanceName(processInstance.getId(),proname);
        //发送消息
            Map<String,String> para=new HashMap<>();
            para.put("startPeople",userRealName);
            para.put("startTime",startTime);
            Object proDueTime = runtimeService.getVariable(processInstance.getId(), "proDueTime");
            para.put("proDueTime",proDueTime==null?"":proDueTime.toString());
            para.put("shenheTime",startTime);
        sendMessage.getSendMessageUser(taskService,processInstance.getId(),jdbcTemplate,proname,para,"1",null);
        JSONObject json=new JSONObject(processDefinition.getDescription());
        json.put("nextTime",sdf.format(jobExecutionContext.getNextFireTime()));
        jdbcTemplate.update("update act_re_procdef set DESCRIPTION_=? where ID_=?",new Object[]{json.toString(),
                processDefinition.getId()});
        //添加整个流程结束时间


        System.out.println(prodefinedid+"=====>定时启动流程成功。。。。。");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
