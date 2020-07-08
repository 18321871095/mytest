package com.fr.tw.util;

import com.fanruan.api.util.TransmissionKit;
import com.fanruan.api.util.shell.BatchSmsBody;
import com.fr.decision.authority.data.User;
import com.fr.decision.webservice.v10.user.UserService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class sendMessageJobUtil implements Job{


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        JdbcTemplate jdbcTemplate=(JdbcTemplate) SpringContextUtil.getBean("jdbcTemplate");
        TaskService taskService=(TaskService) SpringContextUtil.getBean("taskService");
        RuntimeService runtimeService=(RuntimeService) SpringContextUtil.getBean("runtimeService");
        HistoryService historyService=(HistoryService) SpringContextUtil.getBean("historyService");
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String processInstanceId = jobDataMap.getString("processInstanceId");
        String activitiId=jobDataMap.getString("activitiId");
        SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
        try {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if(processInstance==null){
              /*  System.out.println(processInstanceId+"不发短信");*/
                //在时间范围内完成了
                jdbcTemplate.update("delete from act_scheduler where proInstanceid=? and activityid=?", new Object[]{processInstanceId, activitiId});
            }else{
                List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
                Task task=list.get(0);
                if(activitiId.equals(task.getTaskDefinitionKey())){
                   // System.out.println("==========逾期发送短信提醒>" + sdf.format(date));
                    //发送短信
                    HistoricProcessInstance his = historyService.createHistoricProcessInstanceQuery().
                            processInstanceId(task.getProcessInstanceId()).singleResult();
                    String proname = his.getName();
                    User userByUserName11 = UserService.getInstance().getUserByUserName(his.getStartUserId());
                    String realName="";
                    if(userByUserName11!=null){
                        realName=userByUserName11.getRealName();
                    }else{
                        List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().
                                processInstanceId(his.getId()).list();
                        for(HistoricVariableInstance h:hisVarList){
                            if("process_userRealName".equals(h.getVariableName())){
                                realName=h.getValue().toString();
                            }
                        }
                    }
                    Map<String,String> map=new HashMap<>();
                    map.put("startPeople",realName);
                    map.put("startTime",sdf1.format(his.getStartTime()));
                    Object proDueTime = runtimeService.getVariable(processInstanceId, "proDueTime");
                    map.put("proDueTime",proDueTime==null?"":proDueTime.toString());
                    map.put("shenheTime",sdf1.format(new Date()));
                    sendMessage.getSendMessageUser(taskService,task.getProcessInstanceId(),jdbcTemplate,proname,map,"2",list);
                    jdbcTemplate.update("delete from act_scheduler where proInstanceid=? and activityid=?", new Object[]{processInstanceId, activitiId});
                    //System.out.println("==========逾期发送短信成功");
                }else{
                    //在时间范围内完成了
                 /*   System.out.println(processInstanceId+"不发短信");*/
                    jdbcTemplate.update("delete from act_scheduler where proInstanceid=? and activityid=?", new Object[]{processInstanceId, activitiId});
                }
            }
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = stdSchedulerFactory.getScheduler();
            scheduler.pauseJob(JobKey.jobKey(processInstanceId+"_"+activitiId+"_cronJob_sendMessage"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
