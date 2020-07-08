package com.fr.tw.test;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricProcessInstanceQuery;

public class task1 implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        System.out.println("====================流程启动后");
        System.out.println(delegateExecution.getId());
        System.out.println(delegateExecution.getProcessDefinitionId());
        System.out.println(delegateExecution.getProcessInstanceId());
        System.out.println(delegateExecution.getBusinessKey());
        HistoricProcessInstanceQuery historicProcessInstanceQuery = delegateExecution.getEngineServices().getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(delegateExecution.getProcessInstanceId());
        System.out.println(historicProcessInstanceQuery);
    }
}
