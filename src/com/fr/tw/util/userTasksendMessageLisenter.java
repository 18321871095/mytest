package com.fr.tw.util;

import com.fr.decision.authority.data.User;
import com.fr.decision.webservice.v10.user.UserService;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class userTasksendMessageLisenter implements TaskListener  {
    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName=delegateTask.getEventName();
        JdbcTemplate jdbcTemplate=(JdbcTemplate) SpringContextUtil.getBean("jdbcTemplate");
        TaskService taskService=(TaskService) SpringContextUtil.getBean("taskService");
        HistoryService historyService=(HistoryService) SpringContextUtil.getBean("historyService");
        RuntimeService runtimeService=(RuntimeService) SpringContextUtil.getBean("runtimeService");

        SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
        if ("create".endsWith(eventName)) {
            try {
                //System.out.println("超期发送短信提醒====>" + delegateTask.getAssignee());
                HistoricProcessInstance his = historyService.createHistoricProcessInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).singleResult();
                /*sendMessage.getSendMessageDueTimeCreateTask(delegateTask.getAssignee(), historicProcessInstance.getName(), delegateTask.getId(),
                        delegateTask.getProcessDefinitionId(), delegateTask.getProcessInstanceId());*/
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
                Object proDueTime = runtimeService.getVariable(delegateTask.getProcessInstanceId(), "proDueTime");
                map.put("proDueTime",proDueTime==null?"":proDueTime.toString());
                map.put("shenheTime",sdf1.format(new Date()));
                sendMessage.getSendMessageUser(taskService,delegateTask.getProcessInstanceId(),jdbcTemplate,proname,map,"1",null);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }else if ("assignment".endsWith(eventName)) {

        }else if ("complete".endsWith(eventName)) {

        }else if ("delete".endsWith(eventName)) {

        }

    }
}
