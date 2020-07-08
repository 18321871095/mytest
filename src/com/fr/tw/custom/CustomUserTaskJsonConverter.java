package com.fr.tw.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.UserTaskJsonConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomUserTaskJsonConverter extends UserTaskJsonConverter {
    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        super.convertElementToJson(propertiesNode, baseElement);
        UserTask userTask = (UserTask) baseElement;

    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
       // System.out.println("===============>CustomUserTaskJsonConverter被调用");
        Map<String, List<ExtensionElement>> extensionElements=new HashMap<>();
        FlowElement flowElement = super.convertJsonToElement(elementNode,
                modelNode, shapeMap);
        UserTask task = (UserTask)flowElement;
        String btnName = this.getPropertyValueAsString("btnname", elementNode);
        String huiQian= this.getPropertyValueAsString("huiqian", elementNode);
        String tuihui= this.getPropertyValueAsString("tuihui", elementNode);
        String issetassgin = this.getPropertyValueAsString("issetassgin", elementNode);
        boolean liuzhuan = this.getPropertyValueAsBoolean("liuzhuan", elementNode);
        boolean zhuanban = this.getPropertyValueAsBoolean("zhuanban", elementNode);
        boolean istuihui = this.getPropertyValueAsBoolean("istuihui", elementNode);
        boolean seccondautobypass = this.getPropertyValueAsBoolean("seccondautobypass", elementNode);
        boolean autobypass = this.getPropertyValueAsBoolean("autobypass", elementNode);
        boolean issetaddhuiqian = this.getPropertyValueAsBoolean("issetaddhuiqian", elementNode);
        boolean iswritecomment = this.getPropertyValueAsBoolean("iswritecomment", elementNode);
        boolean lookcontentbyself = this.getPropertyValueAsBoolean("lookcontentbyself", elementNode);
        if (StringUtils.isNotEmpty(btnName)) {
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("btnName");
            extension.setNamespace("btnName");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(btnName);
            list.add(extension);
            extensionElements.put("jh",list);
            task.setExtensionElements(extensionElements);
        }
        if (StringUtils.isNotEmpty(huiQian)) {
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("huiQian");
            extension.setNamespace("huiQian");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(huiQian);
            list.add(extension);
            extensionElements.put("jh1",list);
            task.setExtensionElements(extensionElements);
        }
        if (StringUtils.isNotEmpty(tuihui)) {
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("tuihui");
            extension.setNamespace("tuihui");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(tuihui);
            list.add(extension);
            extensionElements.put("jh2",list);
            task.setExtensionElements(extensionElements);
        }
        if(liuzhuan){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("liuzhuan");
            extension.setNamespace("liuzhuan");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(liuzhuan));
            list.add(extension);
            extensionElements.put("jh3",list);
            task.setExtensionElements(extensionElements);
        }
        if(zhuanban){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("zhuanban");
            extension.setNamespace("zhuanban");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(zhuanban));
            list.add(extension);
            extensionElements.put("jh4",list);
            task.setExtensionElements(extensionElements);
        }
        if(istuihui){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("istuihui");
            extension.setNamespace("istuihui");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(istuihui));
            list.add(extension);
            extensionElements.put("jh5",list);
            task.setExtensionElements(extensionElements);
        }
        if(StringUtils.isNotEmpty(issetassgin)){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("issetassgin");
            extension.setNamespace("issetassgin");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(issetassgin));
            list.add(extension);
            extensionElements.put("jh6",list);
            task.setExtensionElements(extensionElements);
        }
        if(autobypass){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("autobypass");
            extension.setNamespace("autobypass");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(autobypass));
            list.add(extension);
            extensionElements.put("jh7",list);
            task.setExtensionElements(extensionElements);
        }
        if(issetaddhuiqian){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("issetaddhuiqian");
            extension.setNamespace("issetaddhuiqian");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(issetaddhuiqian));
            list.add(extension);
            extensionElements.put("jh8",list);
            task.setExtensionElements(extensionElements);
        }
        if(iswritecomment){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("iswritecomment");
            extension.setNamespace("iswritecomment");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(iswritecomment));
            list.add(extension);
            extensionElements.put("jh9",list);
            task.setExtensionElements(extensionElements);
        }
        if(seccondautobypass){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("seccondautobypass");
            extension.setNamespace("seccondautobypass");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(seccondautobypass));
            list.add(extension);
            extensionElements.put("jh10",list);
            task.setExtensionElements(extensionElements);
        }
        if(lookcontentbyself){
            List<ExtensionElement> list=new ArrayList<>();
            ExtensionElement extension=new ExtensionElement();
            extension.setName("lookcontentbyself");
            extension.setNamespace("lookcontentbyself");
            extension.setNamespacePrefix("activiti");
            extension.setElementText(String.valueOf(lookcontentbyself));
            list.add(extension);
            extensionElements.put("jh11",list);
            task.setExtensionElements(extensionElements);
        }
        //解析新增属性的业务逻辑
        //System.out.println("===============>convertJsonToElement被调用");
        this.convertJsonToFormProperties(elementNode, task);
        return task;
    }

    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_TASK_USER, CustomUserTaskJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(UserTask.class, CustomUserTaskJsonConverter.class);
    }

}
