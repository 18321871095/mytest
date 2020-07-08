package com.fr.tw.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BoundaryEventJsonConverter;
import org.activiti.editor.language.json.converter.SequenceFlowJsonConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBoundaryEventJsonConverter extends BoundaryEventJsonConverter {
    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        super.convertElementToJson(propertiesNode, baseElement);
        BoundaryEvent  boundaryEvent = ( BoundaryEvent)baseElement;

    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        //System.out.println("===============>CustomSequenceFlowJsonConverter被调用");
        Map<String, List<ExtensionElement>> extensionElements=new HashMap<>();
        FlowElement flowElement = super.convertJsonToElement(elementNode,
                modelNode, shapeMap);
        BoundaryEvent boundaryEvent = (BoundaryEvent)flowElement;
        String overdue = this.getPropertyValueAsString("overdue", elementNode);
        if(StringUtils.isNotEmpty(overdue)){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("overdue");
            extension.setNamespace("overdue");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(overdue);
            list.add(extension);
            extensionElements.put("jh",list);
            boundaryEvent.setExtensionElements(extensionElements);
        }
        this.convertJsonToFormProperties(elementNode, boundaryEvent);

        return boundaryEvent;
    }

    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_EVENT_BOUNDARY_TIMER, CustomBoundaryEventJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(StartEvent.class, CustomBoundaryEventJsonConverter.class);
    }
}
