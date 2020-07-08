package com.fr.tw.util;


import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;

import java.util.List;

public class getHisVariable implements Command<Void> {
    private   List<String> list;

    private String id;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public getHisVariable(String id){
        this.id=id;
    }

    public Void execute(CommandContext commandContext) {
        HistoricVariableInstanceEntity variableInstance = commandContext.getHistoricVariableInstanceEntityManager().
                findHistoricVariableInstanceByVariableInstanceId(this.id);
            List<String> value = (List<String>)variableInstance.getValue();
           this.list=value;
        return  null;
    }
}
