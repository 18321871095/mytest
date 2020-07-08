package com.fr.tw.util;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class jumpTuiHuiNode {
    public static void  jump(RuntimeService runtimeService,RepositoryService repositoryService,TaskService taskService
    ,HistoryService historyService,String targetActivitiId,String proinstanceId){
        Map<String, Object> variables;
        ExecutionEntity entity = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(proinstanceId).singleResult();
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(entity.getProcessDefinitionId());
        variables = entity.getProcessVariables();
//当前活动环节
        ActivityImpl currActivityImpl = definition.findActivity(entity.getActivityId());
//目标活动节点
        ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition).findActivity(targetActivitiId);
        if (currActivityImpl != null) {
//所有的出口集合
            List<PvmTransition> pvmTransitions = currActivityImpl.getOutgoingTransitions();
            List<PvmTransition> oriPvmTransitions = new ArrayList<PvmTransition>();
            for (PvmTransition transition : pvmTransitions) {
                oriPvmTransitions.add(transition);
            }
//清除所有出口
            pvmTransitions.clear();
//建立新的出口
            List<TransitionImpl> transitionImpls = new ArrayList<TransitionImpl>();
            TransitionImpl tImpl = currActivityImpl.createOutgoingTransition();
            tImpl.setDestination(nextActivityImpl);
            transitionImpls.add(tImpl);

            List<Task> list = taskService.createTaskQuery().processInstanceId(entity.getProcessInstanceId())
                    .taskDefinitionKey(entity.getActivityId()).list();
            for (Task task : list) {
                taskService.complete(task.getId(), variables);
                historyService.deleteHistoricTaskInstance(task.getId());
            }

            for (TransitionImpl transitionImpl : transitionImpls) {
                currActivityImpl.getOutgoingTransitions().remove(transitionImpl);
            }

            for (PvmTransition pvmTransition : oriPvmTransitions) {
                pvmTransitions.add(pvmTransition);
            }
        }
    }
}
