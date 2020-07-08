package com.fr.tw.custom;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;

import java.util.HashMap;
import java.util.Map;

public class BpmnJsonConverterProperties extends BpmnJsonConverter {
    private  Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> jhconvertersToJsonMap = new HashMap();
    private Map<String, Class<? extends BaseBpmnJsonConverter>> jhconvertersToBpmnMap = new HashMap();
    public Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> getconvertersToJsonMap(){
        return jhconvertersToJsonMap=BpmnJsonConverterProperties.convertersToJsonMap;
    }
    public Map<String, Class<? extends BaseBpmnJsonConverter>> getconvertersToBpmnMap(){
        return jhconvertersToBpmnMap=BpmnJsonConverterProperties.convertersToBpmnMap;
    }
}
