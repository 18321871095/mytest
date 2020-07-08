package com.fr.tw.test;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;

public class task  {

    public static void main(String[] args) {
        String a="你好";
        byte[] bytes = a.getBytes();
        System.out.println(new String(bytes));
        for (byte b : bytes ) {
            System.out.println(b);
        }
    }
}
