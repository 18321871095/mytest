package com.fr.tw.util;

import com.fanruan.api.util.TransmissionKit;
import com.fanruan.api.util.shell.EmailBody;
import com.fanruan.api.util.shell.SingleSmsBody;
import com.fr.decision.authority.data.User;
import com.fr.decision.system.bean.message.MessageUrlType;
import com.fr.decision.webservice.v10.message.MessageService;
import com.fr.decision.webservice.v10.user.UserService;
import com.fr.web.core.A.E;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class sendMessage {

    public static void getSendMessageUser(TaskService taskService, String processInstanceId,
     JdbcTemplate jdbcTemplate,String proname,Map<String,String> para,String state, List<Task> lists) throws Exception {
        try {
            //发送消息
            String startPeople = para.get("startPeople") == null ? "" : para.get("startPeople").toString();
            String startTime = para.get("startTime") == null ? "" : para.get("startTime").toString();
            String proDueTime = para.get("proDueTime") == null ? "" : para.get("proDueTime").toString();
            String shenheTime = para.get("shenheTime") == null ? "" : para.get("shenheTime").toString();
            List<Task> list = new ArrayList<>();
            if (lists == null || lists.size() == 0) {
                list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            } else {
                list = lists;
            }
            List<Map<String, String>> assigneeList = new ArrayList<>();

            for (int m = 0; m < list.size(); m++) {
                if (list.get(m).getDescription() == null || "2".equals(state)) {
                    String assignee = list.get(m).getAssignee();
                    Map<String, String> maps = new HashMap<>();
                    if (assignee == null) {
                        //组任务assignee是null的
                        List<Map<String, Object>> list1 = jdbcTemplate.queryForList("select * from act_ru_identitylink where TASK_ID_=?",
                                new Object[]{list.get(m).getId()});
                        for (int j = 0; j < list1.size(); j++) {
                            Map<String, String> maps1 = new HashMap<>();
                            maps1.put("taskid", list1.get(j).get("TASK_ID_").toString());
                            maps1.put("assignee", list1.get(j).get("USER_ID_").toString());
                            maps1.put("proDefinedId", list.get(m).getProcessDefinitionId());
                            maps1.put("proInstanceId", list.get(m).getProcessInstanceId());
                            assigneeList.add(maps1);
                        }
                    } else {
                        maps.put("taskid", list.get(m).getId());
                        maps.put("assignee", assignee);
                        maps.put("proDefinedId", list.get(m).getProcessDefinitionId());
                        maps.put("proInstanceId", list.get(m).getProcessInstanceId());
                        assigneeList.add(maps);
                    }
                }

            }
           List<String> assignes=new ArrayList<>();
            for (int i = 0; i < assigneeList.size(); i++) {
                assignes.add(assigneeList.get(i).get("assignee"));
                if (!"".equals(proDueTime)) {
                    send(assigneeList.get(i).get("assignee"), proname + ":" + assigneeList.get(i).get("taskid"),
                            "您有一个的待处理任务，" + "流程名：" + proname + "，发起人：" + startPeople + "，发起日期：" + startTime
                                    + "，截至日期：" + proDueTime + "，任务到达日期：" + shenheTime + "，请您尽快处理。", assigneeList.get(i).get("taskid"), URLEncoder.encode(proname, "utf-8"),
                            assigneeList.get(i).get("proDefinedId"), assigneeList.get(i).get("proInstanceId"));
                } else {
                    send(assigneeList.get(i).get("assignee"), proname + ":" + assigneeList.get(i).get("taskid"),
                            "您有一个的待处理任务，" + "流程名：" + proname + "，发起人：" + startPeople + "，发起日期：" + startTime +
                                    "，任务到达日期：" + shenheTime + "，请您尽快处理。", assigneeList.get(i).get("taskid"), URLEncoder.encode(proname, "utf-8"),
                            assigneeList.get(i).get("proDefinedId"), assigneeList.get(i).get("proInstanceId"));
                }
                jdbcTemplate.update("update act_ru_task set DESCRIPTION_='1' where ID_=?", new Object[]{assigneeList.get(i).get("taskid")});
            }
            UserService user = UserService.getInstance();
            for(String s:assignes){
                sendSmsAndEmail(s, proname, state, user, startPeople, startTime);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void send(String userName,String title,String content,String taskid,String proname,String proDefinedId,String proInstanceId) {
        MessageService ms = MessageService.getInstance();
        User user = null;
        try {
            user = UserService.getInstance().getUserByUserName(userName);
            //MessageUrlType.OUT   /webroot/        INNER /webroot/decision
            ms.sendMessageWithTitleByUser(user, title, content,
                    "static/jsp/frontEnd/banliTask.jsp?taskid="+taskid+"&proname="+proname+"&proDefinedId="+proDefinedId+"&proInstanceId="+proInstanceId, MessageUrlType.OUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  static void sendSmsAndEmail(String assign,String proname,String state,UserService user,String startPeople,String startTime) throws Exception {
        User userByUserName = user.getUserByUserName(assign);
        if(userByUserName!=null){
            if(userByUserName.getMobile()!=null || "".equals(userByUserName.getMobile())){
               // System.out.println("是否支持短信发送===========>"+TransmissionKit.isSmsFuncSupport());
                if(TransmissionKit.isSmsFuncSupport()){
                    SingleSmsBody.Builder builder = SingleSmsBody.newBuilder();
                    com.fr.json.JSONObject json=new com.fr.json.JSONObject();
                    builder.mobile(userByUserName.getMobile());
                    if("1".equals(state)){
                        builder.templateCode("127");
                        json.put("#proname#",proname);
                        json.put("#startpeople#",startPeople);
                        json.put("#starttime#",startTime);
                    }else{
                        //逾期的
                        builder.templateCode("125");
                        json.put("#proname#",proname);
                    }


                    builder.para(json);
                    builder.needRecord(true);
                    boolean a= TransmissionKit.sendSms(builder.build());

                }
            }
            if(userByUserName.getEmail()!=null || "".equals(userByUserName.getEmail())){
                EmailBody.Builder email = EmailBody.newBuilder();
                if("1".equals(state)){
                    email.subject("任务待办提醒");
                    email.bodyContent("您有个"+proname+"任务需要处理，请您尽快办理");
                }else{
                    email.subject("任务逾期提醒");
                    email.bodyContent("您有个"+proname+"任务即将过期，请您尽快办理");
                }
                email.toAddress("");
                boolean b = TransmissionKit.sendEmail(email.build());
            }
        }

    }




    public static void getSendMessageUserOnZhuanBan(TaskService taskService,String taskid,String proname,
                                                    String startPeople,String startTime) throws Exception {
        try {
            Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
            if (task != null) {
                send(task.getAssignee(), proname + ":" + taskid,
                        "您有一个流程名为：" + proname + "的待处理任务", taskid, URLEncoder.encode(proname, "utf-8"),
                        task.getProcessDefinitionId(), task.getProcessInstanceId());
                UserService user = UserService.getInstance();
                String poeple="";
                User userByUserName = user.getUserByUserName(startPeople);
                if(userByUserName==null){
                    poeple="";
                }else{
                    poeple=userByUserName.getRealName();
                }
                sendSmsAndEmail(task.getAssignee(),proname,"1",user,poeple,startTime);
            }
        }
        catch (Exception e){

        }
    }
    public static void getSendMessageUserOnAddHuiQian(Task task,String proname,JdbcTemplate jdbcTemplate,String startPeople,String startTime) throws Exception {
        try {
            send(task.getAssignee(), proname + ":" + task.getId(),
                    "您有一个流程名为：" + proname + "的待处理任务", task.getId(), URLEncoder.encode(proname, "utf-8"),
                    task.getProcessDefinitionId(), task.getProcessInstanceId());
            jdbcTemplate.update("update act_ru_task set DESCRIPTION_='1' where ID_=?", new Object[]{task.getId()});
            UserService user = UserService.getInstance();
            String poeple="";
            User userByUserName = user.getUserByUserName(startPeople);
            if(userByUserName==null){
                poeple="";
            }else{
                poeple=userByUserName.getRealName();
            }
            sendSmsAndEmail(task.getAssignee(),proname,"1",user,poeple,startTime);
        }
        catch (Exception e){

        }
    }
    public static void getSendMessageDueTimeCreateTask(String assignee,String proname,String taskid,String processDefinitionId,String processInstanceId,
                                                       JdbcTemplate jdbcTemplate,String startPeople,String startTime) throws Exception {
        try {
            send(assignee, proname + ":" + taskid,
                    "您有一个流程名为：" + proname + "的待处理任务", taskid, URLEncoder.encode(proname, "utf-8"),
                    processDefinitionId, processInstanceId);
            jdbcTemplate.update("update act_ru_task set DESCRIPTION_='1' where ID_=?", new Object[]{taskid});
            UserService user = UserService.getInstance();
            String poeple="";
            User userByUserName = user.getUserByUserName(startPeople);
            if(userByUserName==null){
                poeple="";
            }else{
                poeple=userByUserName.getRealName();
            }
            sendSmsAndEmail(assignee,proname,"1",user,poeple,startTime);
        }
        catch (Exception e){

        }
    }
}
