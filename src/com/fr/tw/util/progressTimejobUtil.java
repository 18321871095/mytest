package com.fr.tw.util;

import com.fr.web.core.A.E;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class progressTimejobUtil implements Job{


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String processDefinitionId = jobDataMap.getString("processDefinitionId");
        String processInstanceId = jobDataMap.getString("processInstanceId");
        String activityid = jobDataMap.getString("activityid");
        RuntimeService runtimeService=(RuntimeService) SpringContextUtil.getBean("runtimeService");
        JdbcTemplate jdbcTemplate=(JdbcTemplate) SpringContextUtil.getBean("jdbcTemplate");
        try {
            ProcessInstance proInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (proInstance != null) {
                runtimeService.setVariable(processInstanceId,"process_state","9");
                runtimeService.deleteProcessInstance(processInstanceId,"expired");
                jdbcTemplate.update("delete from act_scheduler where proInstanceid=? and activityid=?",
                        new Object[]{processInstanceId, activityid});
                System.out.println("===========>提醒流程已经强制结束了");

            } else {
                //流程已经结束
                jdbcTemplate.update("delete from act_scheduler where proInstanceid=? and activityid=?",
                        new Object[]{processInstanceId, activityid});
            }
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = stdSchedulerFactory.getScheduler();
            scheduler.pauseJob(JobKey.jobKey(processDefinitionId+"_"+processInstanceId+"_cronJob_proTime"));
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


}
