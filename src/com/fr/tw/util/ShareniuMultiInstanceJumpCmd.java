package com.fr.tw.util;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ShareniuMultiInstanceJumpCmd implements Command<Void> {
    //执行实例id
    protected  String executioniId;
    //流程实例id
    protected  String parentId;
    //目标节点
    protected ActivityImpl desActivity;
    //变量
    protected Map<String,Object> paramvar;
    //当前节点
    protected ActivityImpl currentActivity;

    public ShareniuMultiInstanceJumpCmd(String executionId, String parentId, ActivityImpl destinationActivity, Map<String,Object> paramvar, ActivityImpl currentActivity) {
        this.executioniId=executionId;
        this.parentId=parentId;
        this.desActivity=destinationActivity;
        this.paramvar=paramvar;
        this.currentActivity=currentActivity;
    }

    public Void execute(CommandContext commandContext) {

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

        List<ExecutionEntity> list = executionEntityManager.findChildExecutionsByParentExecutionId(this.parentId);

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

        return null;
    }
}
