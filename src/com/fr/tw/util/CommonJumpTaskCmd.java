package com.fr.tw.util;


import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.DefaultHistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.task.Task;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommonJumpTaskCmd implements Command<Void> {
    //执行实例id
    protected  String executioniId;
    //流程实例id
    protected  String proinstanceId;
    //目标节点
    protected ActivityImpl desActivity;
    //变量
    protected Map<String,Object> paramvar;
    //当前节点
    protected ActivityImpl currentActivity;

    protected JdbcTemplate jdbcTemplate;

    protected String type;

    protected String assign;

    public CommonJumpTaskCmd(String executionId, String proinstanceId, ActivityImpl destinationActivity,
      Map<String,Object> paramvar, ActivityImpl currentActivity, JdbcTemplate jdbcTemplate,String type,String assign) {
        this.executioniId=executionId;
        this.proinstanceId=proinstanceId;
        this.desActivity=destinationActivity;
        this.paramvar=paramvar;
        this.currentActivity=currentActivity;
        this.jdbcTemplate=jdbcTemplate;
        this.type=type;
        this.assign=assign;
    }

    public Void execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl pec = commandContext.getProcessEngineConfiguration();
        IdGenerator idGenerator = pec.getIdGenerator();
        RuntimeService runtimeService = pec.getRuntimeService();
        RepositoryService repositoryService = pec.getRepositoryService();

        if("0".equals(type)){
            //获取当前流程的executioniId 常规流程的executioniId与流程实例parentId相等
            ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
            ExecutionEntity executionEntity = executionEntityManager.findExecutionById(proinstanceId);
            executionEntity.setVariables(this.paramvar);
            executionEntity.setExecutions(null);
            executionEntity.setEventSource(this.currentActivity);
            executionEntity.setActivity(this.currentActivity);
            //获取TaskEntity集合
            Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByProcessInstanceId(proinstanceId).iterator();
            while (iterator.hasNext()){
                TaskEntity taskEntity = (TaskEntity)iterator.next();
                //触发任务监听器
                taskEntity.fireEvent("complete");
                //删除任务的原因
                Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);
            }
            //推动流程实例继续向下运转
            executionEntity.executeActivity(this.desActivity);
        }

        //并行中但同根线上单节点退回单节点
        if("1".equals(type)){
            String tenantid="";
            //获取TaskEntity集合
            Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByExecutionId(executioniId).iterator();
            while (iterator.hasNext()){

                TaskEntity taskEntity = (TaskEntity)iterator.next();
                if("".equals(tenantid)){
                    tenantid=taskEntity.getTenantId();
                }
                //触发任务监听器
                taskEntity.fireEvent("complete");
                //删除任务的原因
                Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);
            }
            ExecutionEntity entity = commandContext.getExecutionEntityManager().findExecutionById(executioniId);
            entity.remove();

            ExecutionEntity parent = entity.getParent();
            Map<String, Object> properties = desActivity.getProperties();
            TaskDefinition taskDefinition = (TaskDefinition) properties.get("taskDefinition");
            String executionid=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_ru_execution (ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PARENT_ID_," +
                            " PROC_DEF_ID_, SUPER_EXEC_, ACT_ID_, IS_ACTIVE_,IS_CONCURRENT_, IS_SCOPE_,IS_EVENT_SCOPE_,SUSPENSION_STATE_," +
                            " CACHED_ENT_STATE_, TENANT_ID_, NAME_, LOCK_TIME_) VALUES " +
                            "(?, '1',?, NULL,?,?, NULL, ?, '1', '1', '0', '0', '1', '7', ?, NULL, NULL)",
                    new Object[]{executionid,proinstanceId,parent.getId(),parent.getProcessDefinitionId(), desActivity.getId(),tenantid});

            String taskid=idGenerator.getNextId();
            String taskName = properties.get("name") == null ? "" : properties.get("name").toString();
            jdbcTemplate.update("INSERT INTO act_ru_task (ID_,REV_,EXECUTION_ID_,PROC_INST_ID_,PROC_DEF_ID_,NAME_,PARENT_TASK_ID_," +
                            " DESCRIPTION_, TASK_DEF_KEY_,OWNER_,ASSIGNEE_,DELEGATION_,PRIORITY_,CREATE_TIME_,DUE_DATE_,CATEGORY_,SUSPENSION_STATE_," +
                            " TENANT_ID_, FORM_KEY_)" +
                            " VALUES (?, '1', ?, ?,?, ?, NULL, NULL, ?, NULL, ?, NULL, '50', ?, NULL, NULL, '1', ?,?)",
                    new Object[]{taskid,executionid,proinstanceId,parent.getProcessDefinitionId(),taskName,desActivity.getId(),
                            taskDefinition.getAssigneeExpression().getExpressionText(), new Date(),tenantid,
                            taskDefinition.getFormKeyExpression().getExpressionText()});

            String hi_taskinst_id=idGenerator.getNextId();
            //退回到的目标节点通过后没有结束时间
            jdbcTemplate.update("INSERT INTO act_hi_taskinst (ID_,PROC_DEF_ID_,TASK_DEF_KEY_,PROC_INST_ID_,EXECUTION_ID_,NAME_," +
                            " PARENT_TASK_ID_, DESCRIPTION_, OWNER_, ASSIGNEE_, START_TIME_, CLAIM_TIME_, END_TIME_, DURATION_,DELETE_REASON_," +
                            " PRIORITY_, DUE_DATE_, FORM_KEY_, CATEGORY_, TENANT_ID_)" +
                            " VALUES (?, ?, ?, ?, ?,?, NULL, NULL, NULL, ?, ?, NULL, NULL, NULL, NULL, '50', NULL, ?, NULL, ?)",
                    new Object[]{hi_taskinst_id,parent.getProcessDefinitionId(),desActivity.getId(),proinstanceId,executionid,taskName,
                            taskDefinition.getAssigneeExpression().getExpressionText(),new Date(),
                            taskDefinition.getFormKeyExpression().getExpressionText(),tenantid});

            //退回的该节点没有结束时间
            String hi_actinst_id=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_hi_actinst (ID_, PROC_DEF_ID_,PROC_INST_ID_,EXECUTION_ID_, ACT_ID_,TASK_ID_," +
                            "CALL_PROC_INST_ID_,ACT_NAME_,ACT_TYPE_,ASSIGNEE_,START_TIME_, END_TIME_,DURATION_, TENANT_ID_)" +
                            " VALUES (?, ?,?, ?, ?, ?, NULL, ?, 'userTask', ?, ?, NULL, '3', ?)",
                    new Object[]{hi_actinst_id,parent.getProcessDefinitionId(),proinstanceId,executionid,desActivity.getId(),taskid,
                            taskName,taskDefinition.getAssigneeExpression().getExpressionText(),new Date(),tenantid});

        }
        //并行中但同根线上单节点退回签
        else if("2".equals(type)){
            String tenantid="";
            Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByExecutionId(executioniId).iterator();
            while (iterator.hasNext()){
                TaskEntity taskEntity = (TaskEntity)iterator.next();
                if("".equals(tenantid)){
                    tenantid=taskEntity.getTenantId();
                }
                //触发任务监听器
                taskEntity.fireEvent("complete");
                //删除任务的原因
                Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);
            }
            ExecutionEntity entity = commandContext.getExecutionEntityManager().findExecutionById(executioniId);
            entity.remove();

            ExecutionEntity parent = entity.getParent();
            Map<String, Object> properties = desActivity.getProperties();
            TaskDefinition taskDefinition = (TaskDefinition) properties.get("taskDefinition");
            String huiqian_1_id=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_ru_execution (ID_,REV_,PROC_INST_ID_,BUSINESS_KEY_,PARENT_ID_,PROC_DEF_ID_," +
                    " SUPER_EXEC_, ACT_ID_,IS_ACTIVE_,IS_CONCURRENT_,IS_SCOPE_,IS_EVENT_SCOPE_,SUSPENSION_STATE_,CACHED_ENT_STATE_," +
                    "TENANT_ID_, NAME_,LOCK_TIME_) " +
                    "VALUES (?, '1', ?, NULL, ?, ?, NULL, NULL, '0', '1', '0', '0', '1', '7', ?, NULL, NULL)",
                    new Object[]{huiqian_1_id,proinstanceId,parent.getId(),parent.getProcessDefinitionId(),tenantid});
            String huiqian_2_id=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_ru_execution (ID_,REV_,PROC_INST_ID_,BUSINESS_KEY_,PARENT_ID_,PROC_DEF_ID_," +
                            " SUPER_EXEC_, ACT_ID_,IS_ACTIVE_,IS_CONCURRENT_,IS_SCOPE_,IS_EVENT_SCOPE_,SUSPENSION_STATE_,CACHED_ENT_STATE_," +
                            "TENANT_ID_, NAME_,LOCK_TIME_) " +
                            "VALUES (?, '1', ?, NULL, ?, ?, NULL, ?, '0', '0', '1', '0', '1', '6', ?, NULL, NULL)",
                    new Object[]{huiqian_2_id,proinstanceId,huiqian_1_id,parent.getProcessDefinitionId(),desActivity.getId(),tenantid});
            UserTask userTask = ProcessUtils.getUserTask(desActivity.getId(), parent.getProcessDefinitionId(), repositoryService);
            MultiInstanceLoopCharacteristics loopCharacteristics = userTask.getLoopCharacteristics();
            String inputDataItem = loopCharacteristics.getInputDataItem();
            List<String> value = (List<String>) runtimeService.getVariable(proinstanceId, inputDataItem);
            if(!"".equals(assign)&&assign!=null){
                value=new ArrayList<>();
                String[] split = assign.split(",");
                for(int mm=0;mm<split.length;mm++){
                    value.add(split[mm]);
                }
            }
            int count=0;
            for(String  str:value){
               if(!"".equals(str)){
                   String executionid=idGenerator.getNextId();
                   jdbcTemplate.update("INSERT INTO act_ru_execution (ID_,REV_,PROC_INST_ID_,BUSINESS_KEY_,PARENT_ID_,PROC_DEF_ID_," +
                                   " SUPER_EXEC_, ACT_ID_,IS_ACTIVE_,IS_CONCURRENT_,IS_SCOPE_,IS_EVENT_SCOPE_,SUSPENSION_STATE_,CACHED_ENT_STATE_," +
                                   "TENANT_ID_, NAME_,LOCK_TIME_) " +
                                   "VALUES (?, '1', ?, NULL, ?, ?, NULL, ?, '1', '1', '0', '0', '1', '7', ?, NULL, NULL)",
                           new Object[]{executionid,proinstanceId,huiqian_2_id,parent.getProcessDefinitionId(),desActivity.getId(),tenantid});
                   String taskid=idGenerator.getNextId();
                   String taskName = properties.get("name") == null ? "" : properties.get("name").toString();
                   jdbcTemplate.update("INSERT INTO act_ru_task (ID_,REV_,EXECUTION_ID_,PROC_INST_ID_,PROC_DEF_ID_,NAME_,PARENT_TASK_ID_," +
                                   " DESCRIPTION_, TASK_DEF_KEY_,OWNER_,ASSIGNEE_,DELEGATION_,PRIORITY_,CREATE_TIME_,DUE_DATE_,CATEGORY_,SUSPENSION_STATE_," +
                                   " TENANT_ID_, FORM_KEY_)" +
                                   " VALUES (?, '1', ?, ?,?, ?, NULL, NULL, ?, NULL, ?, NULL, '50', ?, NULL, NULL, '1', ?,?)",
                           new Object[]{taskid,executionid,proinstanceId,parent.getProcessDefinitionId(),taskName,desActivity.getId(),
                                   str, new Date(),tenantid,taskDefinition.getFormKeyExpression().getExpressionText()});
                   String hi_taskinst_id=idGenerator.getNextId();
                   //退回到的目标节点通过后没有结束时间
                   jdbcTemplate.update("INSERT INTO act_hi_taskinst (ID_,PROC_DEF_ID_,TASK_DEF_KEY_,PROC_INST_ID_,EXECUTION_ID_,NAME_," +
                                   " PARENT_TASK_ID_, DESCRIPTION_, OWNER_, ASSIGNEE_, START_TIME_, CLAIM_TIME_, END_TIME_, DURATION_,DELETE_REASON_," +
                                   " PRIORITY_, DUE_DATE_, FORM_KEY_, CATEGORY_, TENANT_ID_)" +
                                   " VALUES (?, ?, ?, ?, ?,?, NULL, NULL, NULL, ?, ?, NULL, NULL, NULL, NULL, '50', NULL, ?, NULL, ?)",
                           new Object[]{hi_taskinst_id,parent.getProcessDefinitionId(),desActivity.getId(),proinstanceId,executionid,taskName,
                                   str,new Date(),taskDefinition.getFormKeyExpression().getExpressionText(),tenantid});
                   String hi_actinst_id=idGenerator.getNextId();
                   jdbcTemplate.update("INSERT INTO act_hi_actinst (ID_, PROC_DEF_ID_,PROC_INST_ID_,EXECUTION_ID_, ACT_ID_,TASK_ID_," +
                                   "CALL_PROC_INST_ID_,ACT_NAME_,ACT_TYPE_,ASSIGNEE_,START_TIME_, END_TIME_,DURATION_, TENANT_ID_)" +
                                   " VALUES (?, ?,?, ?, ?, ?, NULL, ?, 'userTask', ?, ?, NULL, '3', ?)",
                           new Object[]{hi_actinst_id,parent.getProcessDefinitionId(),proinstanceId,executionid,desActivity.getId(),taskid,
                                   taskName,str,new Date(),tenantid});

                   jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                                   "VALUES (?, '1', 'integer', 'loopCounter',?, ?, NULL, NULL, NULL, ?,?, NULL)",
                           new Object[]{idGenerator.getNextId(),executionid,proinstanceId,count,count});

                   jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                                   "VALUES (?, '1', 'string', 'list',?, ?, NULL, NULL, NULL, NULL,?, NULL)",
                           new Object[]{idGenerator.getNextId(),executionid,proinstanceId,str});

                   count++;
               }
            }
            //插入run_变量
          jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                  "VALUES (?, '1', 'integer', 'nrOfInstances',?, ?, NULL, NULL, NULL, ?,?, NULL)",
                  new Object[]{idGenerator.getNextId(),huiqian_2_id,proinstanceId,value.size(),value.size()});

            jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                            "VALUES (?, '1', 'integer', 'nrOfCompletedInstances',?, ?, NULL, NULL, NULL, '0','0', NULL)",
                    new Object[]{idGenerator.getNextId(),huiqian_2_id,proinstanceId});

            jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                            "VALUES (?, '1', 'integer', 'nrOfActiveInstances',?, ?, NULL, NULL, NULL, ?,?, NULL)",
                    new Object[]{idGenerator.getNextId(),huiqian_2_id,proinstanceId,value.size(),value.size()});

        }

        //并行中回签退回单节点
        if("3".equals(type)){
            ExecutionEntity execution = Context.getCommandContext().getExecutionEntityManager().findExecutionById(executioniId);
            ExecutionEntity parent1 = execution.getParent();
            ExecutionEntity parent12 = parent1.getParent();
            ExecutionEntity parent = parent12.getParent();
            List<ExecutionEntity> childExecutions = Context.getCommandContext().getExecutionEntityManager().findChildExecutionsByParentExecutionId(parent1.getId());
            String tenantid="";
            for(ExecutionEntity e:childExecutions){
                Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByExecutionId(executioniId).iterator();
                while (iterator.hasNext()){
                    TaskEntity taskEntity = (TaskEntity)iterator.next();
                    //触发任务监听器
                    taskEntity.fireEvent("complete");
                    if("".equals(tenantid)){
                        tenantid=taskEntity.getTenantId();
                    }
                    //删除任务的原因
                    Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);
                }
                e.remove();
            }
            parent1.remove();
            parent12.remove();
            Context.getCommandContext().getHistoryManager().recordActivityEnd(parent1);
            Context.getCommandContext().getHistoryManager().recordActivityEnd(parent12);

            Map<String, Object> properties = desActivity.getProperties();
            TaskDefinition taskDefinition = (TaskDefinition) properties.get("taskDefinition");
            String executionid=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_ru_execution (ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PARENT_ID_," +
                            " PROC_DEF_ID_, SUPER_EXEC_, ACT_ID_, IS_ACTIVE_,IS_CONCURRENT_, IS_SCOPE_,IS_EVENT_SCOPE_,SUSPENSION_STATE_," +
                            " CACHED_ENT_STATE_, TENANT_ID_, NAME_, LOCK_TIME_) VALUES " +
                            "(?, '1',?, NULL,?,?, NULL, ?, '1', '1', '0', '0', '1', '7', ?, NULL, NULL)",
                    new Object[]{executionid,proinstanceId,parent.getId(),parent.getProcessDefinitionId(), desActivity.getId(),tenantid});

            String taskid=idGenerator.getNextId();
            String taskName = properties.get("name") == null ? "" : properties.get("name").toString();
            jdbcTemplate.update("INSERT INTO act_ru_task (ID_,REV_,EXECUTION_ID_,PROC_INST_ID_,PROC_DEF_ID_,NAME_,PARENT_TASK_ID_," +
                            " DESCRIPTION_, TASK_DEF_KEY_,OWNER_,ASSIGNEE_,DELEGATION_,PRIORITY_,CREATE_TIME_,DUE_DATE_,CATEGORY_,SUSPENSION_STATE_," +
                            " TENANT_ID_, FORM_KEY_)" +
                            " VALUES (?, '1', ?, ?,?, ?, NULL, NULL, ?, NULL, ?, NULL, '50', ?, NULL, NULL, '1',?,?)",
                    new Object[]{taskid,executionid,proinstanceId,parent.getProcessDefinitionId(),taskName,desActivity.getId(),
                            taskDefinition.getAssigneeExpression().getExpressionText(), new Date(),tenantid,
                            taskDefinition.getFormKeyExpression().getExpressionText()});

            String hi_taskinst_id=idGenerator.getNextId();
            //退回到的目标节点通过后没有结束时间
            jdbcTemplate.update("INSERT INTO act_hi_taskinst (ID_,PROC_DEF_ID_,TASK_DEF_KEY_,PROC_INST_ID_,EXECUTION_ID_,NAME_," +
                            " PARENT_TASK_ID_, DESCRIPTION_, OWNER_, ASSIGNEE_, START_TIME_, CLAIM_TIME_, END_TIME_, DURATION_,DELETE_REASON_," +
                            " PRIORITY_, DUE_DATE_, FORM_KEY_, CATEGORY_, TENANT_ID_)" +
                            " VALUES (?, ?, ?, ?, ?,?, NULL, NULL, NULL, ?, ?, NULL, NULL, NULL, NULL, '50', NULL, ?, NULL, ?)",
                    new Object[]{hi_taskinst_id,parent.getProcessDefinitionId(),desActivity.getId(),proinstanceId,executionid,taskName,
                            taskDefinition.getAssigneeExpression().getExpressionText(),new Date(),
                            taskDefinition.getFormKeyExpression().getExpressionText(),tenantid});

            //退回的该节点没有结束时间
            String hi_actinst_id=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_hi_actinst (ID_, PROC_DEF_ID_,PROC_INST_ID_,EXECUTION_ID_, ACT_ID_,TASK_ID_," +
                            "CALL_PROC_INST_ID_,ACT_NAME_,ACT_TYPE_,ASSIGNEE_,START_TIME_, END_TIME_,DURATION_, TENANT_ID_)" +
                            " VALUES (?, ?,?, ?, ?, ?, NULL, ?, 'userTask', ?, ?, NULL, '3', ?)",
                    new Object[]{hi_actinst_id,parent.getProcessDefinitionId(),proinstanceId,executionid,desActivity.getId(),taskid,
                            taskName,taskDefinition.getAssigneeExpression().getExpressionText(),new Date(),tenantid});

        }

         //并行中回签退回并行之外的单节点  退回到会签节点先不考虑
        if("4".equals(type)){
            Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByProcessInstanceId(proinstanceId).iterator();
            String tenantid="";
            while (iterator.hasNext()){
                TaskEntity taskEntity = (TaskEntity)iterator.next();
                //触发任务监听器
                taskEntity.fireEvent("complete");
                if("".equals(tenantid)){
                    tenantid=taskEntity.getTenantId();
                }
                //删除任务的原因
                Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);
            }
            ExecutionEntity execution = Context.getCommandContext().getExecutionEntityManager().findExecutionById(executioniId);
            ExecutionEntity parent1 = execution.getParent();
            ExecutionEntity parent12 = parent1.getParent();
            ExecutionEntity parent = parent12.getParent();
            String requestid=parent.getBusinessKey();
            String name=parent.getName();

            List<ExecutionEntity> childExecutions = Context.getCommandContext().getExecutionEntityManager().findChildExecutionsByParentExecutionId(parent1.getId());
            for(ExecutionEntity e:childExecutions){
                e.remove();
            }
            parent1.remove();
            parent12.remove();

            List<ExecutionEntity> childExecutions1 = Context.getCommandContext().getExecutionEntityManager().findChildExecutionsByParentExecutionId(parent.getId());
            for(ExecutionEntity e:childExecutions1){
                e.remove();
            }

            Map<String, Object> properties = desActivity.getProperties();
            TaskDefinition taskDefinition = (TaskDefinition) properties.get("taskDefinition");
            jdbcTemplate.update("update act_ru_execution set REV_='3',ACT_ID_=?,IS_ACTIVE_='1',CACHED_ENT_STATE_='2'  where ID_=?",
                    new Object[]{desActivity.getId(),parent.getId()});


            String taskid=idGenerator.getNextId();
            String taskName = properties.get("name") == null ? "" : properties.get("name").toString();
            String assignee1 = taskDefinition.getAssigneeExpression().getExpressionText();
            String assignee="";
            if(assignee1.contains("{")){
                String trim = assignee1.substring(assignee1.indexOf("{") + 1, assignee1.indexOf("}")).trim();
                assignee=runtimeService.getVariableInstance(proinstanceId,trim).getTextValue();
            }else{
                assignee=assignee1;
            }
            jdbcTemplate.update("INSERT INTO act_ru_task (ID_,REV_,EXECUTION_ID_,PROC_INST_ID_,PROC_DEF_ID_,NAME_,PARENT_TASK_ID_," +
                            " DESCRIPTION_, TASK_DEF_KEY_,OWNER_,ASSIGNEE_,DELEGATION_,PRIORITY_,CREATE_TIME_,DUE_DATE_,CATEGORY_,SUSPENSION_STATE_," +
                            " TENANT_ID_, FORM_KEY_)" +
                            " VALUES (?, '1', ?, ?,?, ?, NULL, NULL, ?, NULL, ?, NULL, '50', ?, NULL, NULL, '1', ?,?)",
                    new Object[]{taskid,proinstanceId,proinstanceId,parent.getProcessDefinitionId(),taskName,desActivity.getId(),
                            assignee, new Date(), tenantid,taskDefinition.getFormKeyExpression().getExpressionText()});
            String hi_taskinst_id=idGenerator.getNextId();
            //退回到的目标节点通过后没有结束时间
            jdbcTemplate.update("INSERT INTO act_hi_taskinst (ID_,PROC_DEF_ID_,TASK_DEF_KEY_,PROC_INST_ID_,EXECUTION_ID_,NAME_," +
                            " PARENT_TASK_ID_, DESCRIPTION_, OWNER_, ASSIGNEE_, START_TIME_, CLAIM_TIME_, END_TIME_, DURATION_,DELETE_REASON_," +
                            " PRIORITY_, DUE_DATE_, FORM_KEY_, CATEGORY_, TENANT_ID_)" +
                            " VALUES (?, ?, ?, ?, ?,?, NULL, NULL, NULL, ?, ?, NULL, NULL, NULL, NULL, '50', NULL, ?, NULL,?)",
                    new Object[]{hi_taskinst_id,parent.getProcessDefinitionId(),desActivity.getId(),proinstanceId,proinstanceId,taskName,
                            assignee,new Date(),taskDefinition.getFormKeyExpression().getExpressionText(),tenantid});

            //退回的该节点没有结束时间
            String hi_actinst_id=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_hi_actinst (ID_, PROC_DEF_ID_,PROC_INST_ID_,EXECUTION_ID_, ACT_ID_,TASK_ID_," +
                            "CALL_PROC_INST_ID_,ACT_NAME_,ACT_TYPE_,ASSIGNEE_,START_TIME_, END_TIME_,DURATION_, TENANT_ID_)" +
                            " VALUES (?, ?,?, ?, ?, ?, NULL, ?, 'userTask', ?, ?, NULL, '3', ?)",
                    new Object[]{hi_actinst_id,parent.getProcessDefinitionId(),proinstanceId,proinstanceId,desActivity.getId(),taskid,
                            taskName,assignee,new Date(),tenantid});

          /*  //会签退回如果要删除顶级父执行对象，下面必须执行
            commandContext.getIdentityLinkEntityManager().deleteIdentityLinksByProcInstance(proinstanceId);
            parent.remove();*/
        }

        //并行中回签退回并行之外的会签节点
        if("5".equals(type)){

        }
        //并行之外的单节点退回到会签
        if("6".equals(type)){
            String tenantid="";
            Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByExecutionId(executioniId).iterator();
            while (iterator.hasNext()){
                TaskEntity taskEntity = (TaskEntity)iterator.next();
                //触发任务监听器
                taskEntity.fireEvent("complete");
                if("".equals(tenantid)){
                    tenantid=taskEntity.getTenantId();
                }
                //删除任务的原因
                Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);
            }
            ExecutionEntity execution = Context.getCommandContext().getExecutionEntityManager().findExecutionById(executioniId);
            String prodefinedid=execution.getProcessDefinitionId();

            jdbcTemplate.update("update act_ru_execution set REV_='3',ACT_ID_=?,IS_ACTIVE_='0',CACHED_ENT_STATE_='0'  where ID_=?",
                    new Object[]{null,proinstanceId});

            String parentid=idGenerator.getNextId();
            jdbcTemplate.update("INSERT INTO act_ru_execution (ID_,REV_,PROC_INST_ID_,BUSINESS_KEY_,PARENT_ID_,PROC_DEF_ID_,SUPER_EXEC_,ACT_ID_,IS_ACTIVE_,IS_CONCURRENT_,IS_SCOPE_," +
                    " IS_EVENT_SCOPE_,SUSPENSION_STATE_,CACHED_ENT_STATE_,TENANT_ID_,NAME_,LOCK_TIME_) VALUES " +
                    " (?, '1', ?, NULL, ?,?, NULL, ?, '0', '0', '1', '0', '1', '6',?, NULL, NULL)",
                    new Object[]{parentid,proinstanceId,proinstanceId,prodefinedid,desActivity.getId(),tenantid});


            UserTask userTask = ProcessUtils.getUserTask(desActivity.getId(), prodefinedid, repositoryService);
            MultiInstanceLoopCharacteristics loopCharacteristics = userTask.getLoopCharacteristics();
            String inputDataItem = loopCharacteristics.getInputDataItem();
            List<String> value = (List<String>) runtimeService.getVariable(proinstanceId, inputDataItem);
            if(!"".equals(assign)&&assign!=null){
                value=new ArrayList<>();
                String[] split = assign.split(",");
                for(int mm=0;mm<split.length;mm++){
                    value.add(split[mm]);
                }
            }
            Map<String, Object> properties = desActivity.getProperties();
            TaskDefinition taskDefinition = (TaskDefinition) properties.get("taskDefinition");
            int count=0;
            for(String  str:value){
                if(!"".equals(str)){
                    String executionid=idGenerator.getNextId();
                    jdbcTemplate.update("INSERT INTO act_ru_execution (ID_,REV_,PROC_INST_ID_,BUSINESS_KEY_,PARENT_ID_,PROC_DEF_ID_," +
                                    " SUPER_EXEC_, ACT_ID_,IS_ACTIVE_,IS_CONCURRENT_,IS_SCOPE_,IS_EVENT_SCOPE_,SUSPENSION_STATE_,CACHED_ENT_STATE_," +
                                    "TENANT_ID_, NAME_,LOCK_TIME_) " +
                                    "VALUES (?, '1', ?, NULL, ?, ?, NULL, ?, '1', '1', '0', '0', '1', '7',?, NULL, NULL)",
                            new Object[]{executionid,proinstanceId,parentid,prodefinedid,desActivity.getId(),tenantid});
                    String taskid=idGenerator.getNextId();
                    String taskName = properties.get("name") == null ? "" : properties.get("name").toString();
                    jdbcTemplate.update("INSERT INTO act_ru_task (ID_,REV_,EXECUTION_ID_,PROC_INST_ID_,PROC_DEF_ID_,NAME_,PARENT_TASK_ID_," +
                                    " DESCRIPTION_, TASK_DEF_KEY_,OWNER_,ASSIGNEE_,DELEGATION_,PRIORITY_,CREATE_TIME_,DUE_DATE_,CATEGORY_,SUSPENSION_STATE_," +
                                    " TENANT_ID_, FORM_KEY_)" +
                                    " VALUES (?, '1', ?, ?,?, ?, NULL, NULL, ?, NULL, ?, NULL, '50', ?, NULL, NULL, '1', ?,?)",
                            new Object[]{taskid,executionid,proinstanceId,prodefinedid,taskName,desActivity.getId(),
                                    str, new Date(),tenantid,taskDefinition.getFormKeyExpression().getExpressionText()});
                    String hi_taskinst_id=idGenerator.getNextId();
                    //退回到的目标节点通过后没有结束时间
                    jdbcTemplate.update("INSERT INTO act_hi_taskinst (ID_,PROC_DEF_ID_,TASK_DEF_KEY_,PROC_INST_ID_,EXECUTION_ID_,NAME_," +
                                    " PARENT_TASK_ID_, DESCRIPTION_, OWNER_, ASSIGNEE_, START_TIME_, CLAIM_TIME_, END_TIME_, DURATION_,DELETE_REASON_," +
                                    " PRIORITY_, DUE_DATE_, FORM_KEY_, CATEGORY_, TENANT_ID_)" +
                                    " VALUES (?, ?, ?, ?, ?,?, NULL, NULL, NULL, ?, ?, NULL, NULL, NULL, NULL, '50', NULL, ?, NULL, ?)",
                            new Object[]{hi_taskinst_id,prodefinedid,desActivity.getId(),proinstanceId,executionid,taskName,
                                    str,new Date(),taskDefinition.getFormKeyExpression().getExpressionText(),tenantid});
                    String hi_actinst_id=idGenerator.getNextId();
                    jdbcTemplate.update("INSERT INTO act_hi_actinst (ID_, PROC_DEF_ID_,PROC_INST_ID_,EXECUTION_ID_, ACT_ID_,TASK_ID_," +
                                    "CALL_PROC_INST_ID_,ACT_NAME_,ACT_TYPE_,ASSIGNEE_,START_TIME_, END_TIME_,DURATION_, TENANT_ID_)" +
                                    " VALUES (?, ?,?, ?, ?, ?, NULL, ?, 'userTask', ?, ?, NULL, '3', ?)",
                            new Object[]{hi_actinst_id,prodefinedid,proinstanceId,executionid,desActivity.getId(),taskid,
                                    taskName,str,new Date(),tenantid});

                    jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                                    "VALUES (?, '1', 'integer', 'loopCounter',?, ?, NULL, NULL, NULL, ?,?, NULL)",
                            new Object[]{idGenerator.getNextId(),executionid,proinstanceId,count,count});

                    jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                                    "VALUES (?, '1', 'string', 'list',?, ?, NULL, NULL, NULL, NULL,?, NULL)",
                            new Object[]{idGenerator.getNextId(),executionid,proinstanceId,str});
                    count++;
                }
            }

            //插入run_变量
            jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                            "VALUES (?, '1', 'integer', 'nrOfInstances',?, ?, NULL, NULL, NULL, ?,?, NULL)",
                    new Object[]{idGenerator.getNextId(),parentid,proinstanceId,value.size(),value.size()});

            jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                            "VALUES (?, '1', 'integer', 'nrOfCompletedInstances',?, ?, NULL, NULL, NULL, '0','0', NULL)",
                    new Object[]{idGenerator.getNextId(),parentid,proinstanceId});

            jdbcTemplate.update("INSERT INTO act_ru_variable(ID_,REV_,TYPE_,NAME_,EXECUTION_ID_,PROC_INST_ID_,TASK_ID_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_) " +
                            "VALUES (?, '1', 'integer', 'nrOfActiveInstances',?, ?, NULL, NULL, NULL, ?,?, NULL)",
                    new Object[]{idGenerator.getNextId(),parentid,proinstanceId,value.size(),value.size()});


        }
        //并行之外的会签退回到会单节点
        if("7".equals(type)){
            ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
            ExecutionEntity executionEntity = executionEntityManager.findExecutionById(this.executioniId);
            String id = null;
            if (executionEntity.getParentId() != null) {
                executionEntity = executionEntity.getParent();
                if (executionEntity.getParentId() != null) {
                    executionEntity = executionEntity.getParent();
                    id = executionEntity.getId();
                }
                id = executionEntity.getId();
            }
            executionEntity.setVariables(this.paramvar);
            executionEntity.setExecutions(null);
            executionEntity.setEventSource(this.currentActivity);
            executionEntity.setActivity(this.currentActivity);
            Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByProcessInstanceId(id).iterator();
            while (iterator.hasNext()){
                TaskEntity taskEntity = (TaskEntity)iterator.next();
                //触发任务监听器
                taskEntity.fireEvent("complete");
                //删除任务的原因
                Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);
            }

            List<ExecutionEntity> list = executionEntityManager.findChildExecutionsByParentExecutionId(proinstanceId);

            for(ExecutionEntity executionEntity2 : list){
                ExecutionEntity findExecutionById = executionEntityManager.findExecutionById(executionEntity2.getId());

                List<ExecutionEntity> parent = executionEntityManager.findChildExecutionsByParentExecutionId(executionEntity2.getId());

                for(ExecutionEntity executionEntity3 : parent){
                    executionEntity3.remove();
                    Context.getCommandContext().getHistoryManager().recordActivityEnd(executionEntity3);
                }
                executionEntity2.remove();
                Context.getCommandContext().getHistoryManager().recordActivityEnd(executionEntity2);

            }

            //commandContext.getIdentityLinkEntityManager().deleteIdentityLinksByProcInstance(id);
            //推动流程实例继续向下运转
            executionEntity.executeActivity(this.desActivity);
        }
        //并行之中的单节点退出并行之外的单节点

        if("8".equals(type)){
            ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();

            ExecutionEntity executionEntity = executionEntityManager.findExecutionById(this.executioniId);

            String id = null;

            if (executionEntity.getParentId() != null) {
                executionEntity = executionEntity.getParent();
                if (executionEntity.getParentId() != null) {
                    executionEntity = executionEntity.getParent();
                    id = executionEntity.getId();
                }else {
                    id = executionEntity.getId();
                }

            }
            executionEntity.setVariables(this.paramvar);
            executionEntity.setExecutions(null);
            executionEntity.setEventSource(this.currentActivity);
            executionEntity.setActivity(this.currentActivity);
            Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByProcessInstanceId(proinstanceId).iterator();
            while (iterator.hasNext()){
                TaskEntity taskEntity = (TaskEntity)iterator.next();
                //触发任务监听器
                taskEntity.fireEvent("complete");
                //删除任务的原因
                Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);

            }
            List<ExecutionEntity> list = executionEntityManager.findChildExecutionsByParentExecutionId(proinstanceId);
            for(ExecutionEntity ee : list){
                ee.remove();
            }
            //推动流程实例继续向下运转
            executionEntity.executeActivity(this.desActivity);
        }

        return null;
    }
}
