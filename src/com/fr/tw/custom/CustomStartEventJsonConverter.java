package com.fr.tw.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.StartEventJsonConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomStartEventJsonConverter extends StartEventJsonConverter {

    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        super.convertElementToJson(propertiesNode, baseElement);
        StartEvent startEvent = (StartEvent)baseElement;
    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {

        Map<String, List<ExtensionElement>> extensionElements=new HashMap<>();
        FlowElement flowElement = super.convertJsonToElement(elementNode,
                modelNode, shapeMap);
        StartEvent startEvent = (StartEvent)flowElement;
        String processtime = this.getPropertyValueAsString("processtime", elementNode);
        if(StringUtils.isNotEmpty(processtime)){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("processtime");
            extension.setNamespace("processtime");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(processtime);
            list.add(extension);
            extensionElements.put("jh",list);
            startEvent.setExtensionElements(extensionElements);
        }
        this.convertJsonToFormProperties(elementNode, startEvent);

        return startEvent;
    }

    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_EVENT_START_NONE, CustomStartEventJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(StartEvent.class, CustomStartEventJsonConverter.class);
    }

}
