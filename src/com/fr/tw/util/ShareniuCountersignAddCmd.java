package com.fr.tw.util;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;

import java.util.Date;

public class ShareniuCountersignAddCmd implements Command<Void> {
    protected String executionld;
    protected String assignee;
    public ShareniuCountersignAddCmd(String executionld, String assignee){
    this.executionld=executionld;
    this.assignee=assignee;
}
    public Void execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl pec = commandContext.getProcessEngineConfiguration();
        RuntimeService runtimeService = pec.getRuntimeService();
        TaskService taskService = pec.getTaskService();
        IdGenerator idGenerator = pec.getIdGenerator();
        Execution execution = runtimeService.createExecutionQuery().executionId(executionld).singleResult();
        ExecutionEntity ee = (ExecutionEntity) execution;
        ExecutionEntity parent = ee.getParent();
        ExecutionEntity newExecution = parent.createExecution();
        newExecution.setActive(true);

        newExecution.setConcurrent(true);
        newExecution.setScope(false);
        Task newTask = taskService.createTaskQuery().executionId(executionld).singleResult();
        TaskEntity t = (TaskEntity) newTask;
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setCreateTime(new Date());
        taskEntity.setTaskDefinition(t.getTaskDefinition());
        taskEntity.setProcessDefinitionId(t.getProcessDefinitionId());
        taskEntity.setTaskDefinitionKey(t.getTaskDefinitionKey());
        taskEntity.setProcessInstanceId(t.getProcessInstanceId());
        taskEntity.setExecutionId(newExecution.getId());
        taskEntity.setName(newTask.getName());
        taskEntity.setFormKey(t.getFormKey());
        String taskid = idGenerator.getNextId();
        taskEntity.setId(taskid);
        taskEntity.setExecution(newExecution);
        taskEntity.setAssignee(assignee);
        taskService.saveTask(taskEntity);
        int loopCounter = ShareniuLoopVariableUtils.getLoopVariable(newExecution, "nrOfInstances");
        int nrOfCompletedinstances = ShareniuLoopVariableUtils.getLoopVariable(newExecution, "nrOfActiveInstances");
        ShareniuLoopVariableUtils.setLoopVariable(newExecution, "nrOfInstances", loopCounter + 1);
        ShareniuLoopVariableUtils.setLoopVariable(newExecution, "nrOfActiveInstances", nrOfCompletedinstances + 1);
        return null;

    }
}
