package com.fr.tw.util;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class ShareniuLoopVariableUtils {
    public static void setLoopVariable(ExecutionEntity execution,
                                       String variableName, Object value) {
        ActivityExecution parent = execution.getParent();
        parent.setVariableLocal(variableName, value);
    }

    public static Integer getLoopVariable(ExecutionEntity execution, String variableName) {
        Object value = execution.getVariableLocal(variableName);
        ActivityExecution parent = execution.getParent();
        while (value == null && parent != null) {
            value = parent.getVariableLocal(variableName);
            parent = parent.getParent();
        }
        return (Integer) (value != null ? value : 0 );
    }
}
