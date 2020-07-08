package com.fr.tw.util;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class progressLisenter implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String eventName = execution.getEventName();
        String processDefinitionId = execution.getProcessDefinitionId();
        String processInstanceId = execution.getProcessInstanceId();
        RepositoryService repositoryService=execution.getEngineServices().getRepositoryService();
        RuntimeService runtimeService = execution.getEngineServices().getRuntimeService();
        JdbcTemplate jdbcTemplate=(JdbcTemplate) SpringContextUtil.getBean("jdbcTemplate");
        //start
        if ("start".equals(eventName)) {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("==========>流程启动监听器:"+sdf.format(date));
            Collection<FlowElement> flowElements = ProcessUtils.getFlowElements(processDefinitionId, repositoryService);
            StartEvent start=null;
            for(FlowElement f:flowElements){
                if (f instanceof StartEvent){
                    start=(StartEvent)f;
                    break;
                }
            }
            //获取流程整个期限,目前先支持s：秒，m：分钟，h：小时，d：天数    PT3M
            String progressTime = ProcessUtils.getStartNodeExectionName(start,"processtime");
            if(!"".equals(progressTime)){
                Date time = ProcessUtils.getProgressTime(date, progressTime);
                runtimeService.setVariable(processInstanceId,"proDueTime",sdf.format(time));
                jdbcTemplate.update("insert into act_scheduler(id,prodefineid,proInstanceid,duedate,tenantid,activityid,type) VALUES (?,?,?,?,?,?,?)",
                        new Object[]{ProcessUtils.getUUID(),processDefinitionId,processInstanceId,sdf.format(time),execution.getTenantId(),start.getId(),"allTime"});
                JobDetail jobDetail = JobBuilder.newJob(progressTimejobUtil.class)
                        .withIdentity(processDefinitionId+"_"+processInstanceId+"_cronJob_proTime")
                        .usingJobData("processInstanceId",processInstanceId)
                        .usingJobData("processDefinitionId",processDefinitionId)
                        .usingJobData("activityid",start.getId())
                        .build();
                SimpleTrigger cronTrigger =(SimpleTrigger)TriggerBuilder.newTrigger()
                        .withIdentity(processDefinitionId+"_"+processInstanceId+"_cronTrigger_proTime")
                        .startAt(time)
                        .build();
                StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                Scheduler scheduler = stdSchedulerFactory.getScheduler();
                scheduler.scheduleJob(jobDetail,cronTrigger);
                scheduler.start();

            }

        }else if ("end".equals(eventName)) {
            System.out.println("end=========");
        }
        else if("take".equals(eventName)){



        }

    }


}
