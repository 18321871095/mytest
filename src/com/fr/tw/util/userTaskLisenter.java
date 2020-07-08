package com.fr.tw.util;

import org.activiti.bpmn.model.*;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.codehaus.groovy.runtime.powerassert.SourceText;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class userTaskLisenter implements TaskListener  {
    public Integer count=0;
    public String proInstanceid="";
    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName=delegateTask.getEventName();

        if ("create".endsWith(eventName)) {
            Object lock = new Object();
            synchronized (lock){
                if(!proInstanceid.equals(delegateTask.getProcessInstanceId())){
                    count=0;
                }
                if(count==0){

                    proInstanceid=delegateTask.getProcessInstanceId();
                    //System.out.println("=======>创建任务监听器："+proInstanceid);
                    JdbcTemplate jdbcTemplate=(JdbcTemplate) SpringContextUtil.getBean("jdbcTemplate");
                    RepositoryService repositoryService=(RepositoryService) SpringContextUtil.getBean("repositoryService");
                    String processInstanceId = delegateTask.getProcessInstanceId();
                    String processDefinitionId = delegateTask.getProcessDefinitionId();
                    String activitiId = delegateTask.getTaskDefinitionKey();
                    Collection<FlowElement> flowElements = ProcessUtils.getFlowElements(processDefinitionId, repositoryService);
                    BoundaryEvent boundaryEvent=null;
                    for(FlowElement f:flowElements){
                        if (f instanceof BoundaryEvent){
                            boundaryEvent = (BoundaryEvent) f;
                            if(activitiId.equals(boundaryEvent.getAttachedToRefId())){
                                //PT1S
                                String overdue=ProcessUtils.getBoundaryEventExectionName(boundaryEvent,"overdue");
                               if(!"".equals(overdue)){
                                   String duetime="";
                                   Date date = new Date();
                                   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                   //插入数据数据库，初始化时需要用到
                                   List<EventDefinition> eventDefinitions = boundaryEvent.getEventDefinitions();
                                   if(eventDefinitions.size()>0){
                                       TimerEventDefinition definition =(TimerEventDefinition) eventDefinitions.get(0);
                                       //PT1S
                                       duetime=definition.getTimeDuration();
                                   }
                                 /*  Integer cha=Integer.valueOf(ProcessUtils.getNumInString(duetime))
                                           -Integer.valueOf(ProcessUtils.getNumInString(overdue));*/
                                   //获取到期时间
                                   String time=sdf.format(ProcessUtils.getOverTime(date,duetime,overdue));
                                   System.out.println("逾期时间："+time);
                                   try {
                                       List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from act_scheduler where proInstanceid=? and activityid=?",
                                               new Object[]{processInstanceId,activitiId});
                                       if(list.size()==0){
                                           jdbcTemplate.update("insert into act_scheduler(id,prodefineid,proInstanceid,duedate,tenantid,activityid,type) VALUES (?,?,?,?,?,?,?)",
                                                   new Object[]{ProcessUtils.getUUID(),processDefinitionId,processInstanceId,time,delegateTask.getTenantId(),activitiId,"dueTime"});
                                           JobDetail jobDetail = JobBuilder.newJob(sendMessageJobUtil.class)
                                                   .withIdentity(processInstanceId+"_"+activitiId+"_cronJob_sendMessage")
                                                   .usingJobData("processInstanceId",processInstanceId)
                                                   .usingJobData("activitiId",activitiId)
                                                   .build();
                                           SimpleTrigger cronTrigger =(SimpleTrigger) TriggerBuilder.newTrigger()
                                                   .withIdentity(processInstanceId+"_"+activitiId+"_cronTrigger_sendMessage")
                                                   .startAt(sdf.parse(time))
                                                   .build();
                                           StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                                           Scheduler scheduler = null;
                                           scheduler = stdSchedulerFactory.getScheduler();
                                           scheduler.scheduleJob(jobDetail,cronTrigger);
                                           scheduler.start();
                                       }
                                   } catch (Exception e) {
                                       e.printStackTrace();
                                   }
                               }
                                break;
                            }else{
                                continue;
                            }

                        }
                    }
                    count++;
                }

            }


        }else if ("assignment".endsWith(eventName)) {

        }else if ("complete".endsWith(eventName)) {

        }else if ("delete".endsWith(eventName)) {

        }

    }
}
