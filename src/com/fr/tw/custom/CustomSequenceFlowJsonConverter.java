package com.fr.tw.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.SequenceFlowJsonConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSequenceFlowJsonConverter extends SequenceFlowJsonConverter {
    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        super.convertElementToJson(propertiesNode, baseElement);
        SequenceFlow sequenceFlow = (SequenceFlow)baseElement;

    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        //System.out.println("===============>CustomSequenceFlowJsonConverter被调用");
        Map<String, List<ExtensionElement>> extensionElements=new HashMap<>();
        FlowElement flowElement = super.convertJsonToElement(elementNode,
                modelNode, shapeMap);
        SequenceFlow sequenceFlow = (SequenceFlow)flowElement;
        String frfunction = this.getPropertyValueAsString("frfunction", elementNode);
        if(StringUtils.isNotEmpty(frfunction)){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("frfunction");
            extension.setNamespace("frfunction");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(frfunction));
            list.add(extension);
            extensionElements.put("jh",list);
            sequenceFlow.setExtensionElements(extensionElements);
        }
        this.convertJsonToFormProperties(elementNode, sequenceFlow);

        return sequenceFlow;
    }

    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_SEQUENCE_FLOW, CustomSequenceFlowJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(StartEvent.class, CustomSequenceFlowJsonConverter.class);
    }
}
