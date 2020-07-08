package com.fr.tw.test;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.Iterator;
import java.util.Map;

public class backtest implements Command<Void> {
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

    public backtest(String executionId, String parentId, ActivityImpl destinationActivity, Map<String,Object> paramvar, ActivityImpl currentActivity) {
        this.executioniId=executionId;
        this.parentId=parentId;
        this.desActivity=destinationActivity;
        this.paramvar=paramvar;
        this.currentActivity=currentActivity;
    }

    public Void execute(CommandContext commandContext) {
        //获取当前流程的executioniId 常规流程的executioniId与流程实例parentId相等
        ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();

        ExecutionEntity executionEntity = executionEntityManager.findExecutionById(this.parentId);
        executionEntity.setVariables(this.paramvar);
        executionEntity.setExecutions(null);
        executionEntity.setEventSource(this.currentActivity);
        executionEntity.setActivity(this.currentActivity);

        //获取TaskEntity集合
        Iterator<TaskEntity> iterator = Context.getCommandContext().getTaskEntityManager().findTasksByProcessInstanceId(this.parentId).iterator();
        while (iterator.hasNext()){
            TaskEntity taskEntity = (TaskEntity)iterator.next();
            //触发任务监听器
            taskEntity.fireEvent("complete");
            //删除任务的原因
            Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity,"Shareniucompleted",false);

        }
        //推动流程实例继续向下运转
        executionEntity.executeActivity(this.desActivity);
        return null;
    }
}
