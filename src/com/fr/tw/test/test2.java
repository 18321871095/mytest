package com.fr.tw.test;

import com.fr.tw.util.ProcessUtils;
import com.fr.tw.util.ShareniuCommonJumpTaskCmd;
import com.fr.tw.util.ShareniuParallelJumpCmd;
import com.fr.tw.util.jumpTuiHuiNode;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class test2 {

    public static void main(String[] args) {
        ApplicationContext ap = new ClassPathXmlApplicationContext(new String[]{"spring-activiti.xml"});
        RepositoryService repositoryService = (RepositoryService) ap.getBean("repositoryService");
        RuntimeService runtimeService = (RuntimeService) ap.getBean("runtimeService");
        TaskService taskService = (TaskService) ap.getBean("taskService");
        HistoryService historyService = (HistoryService) ap.getBean("historyService");
        IdentityService identityService = (IdentityService) ap.getBean("identityService");
        ManagementService managerService = (ManagementService) ap.getBean("managementService");
        FormService formService = (FormService) ap.getBean("formService");
        JdbcTemplate jdbcTemplate = (JdbcTemplate) ap.getBean("jdbcTemplate");

        ActivityImpl destinationActivity = ProcessUtils.getActivityImplByActivitiId("sid-6C0C967F-A012-4A1F-84A8-3F7665C65FCF",
               "process:1:77507",repositoryService);
        ActivityImpl currentActivity = ProcessUtils.getActivityImplByActivitiId("sid-46D284B2-A716-4111-BA6D-359DF0466889",
                "process:1:77507",repositoryService);
      /* managerService.executeCommand(new ShareniuCommonJumpTaskCmd("162514","162514",
                destinationActivity, null, currentActivity));*/


        managerService.executeCommand(new ShareniuParallelJumpCmd("77521", "77508", destinationActivity,
                null, currentActivity));


       /* jumpTuiHuiNode.jump(runtimeService, repositoryService, taskService, historyService,
                "sid-D30DAE34-3647-4B53-A234-526DCDFB79F8", "32501");*/
        System.out.println("success");
    }
}
