package com.fr.tw.util;

import com.fr.web.core.A.E;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class initDataBaseSchemas implements InitializingBean, ServletContextAware {
    @Autowired
    private JdbcTemplate jt;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void setServletContext(ServletContext servletContext) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Properties pro = new Properties();
        InputStream in = initDataBaseSchemas.class.getClassLoader().getResourceAsStream("db.properties");
        pro.load(in);
        if("com.mysql.jdbc.Driver".equals(pro.getProperty("jdbc.driverClass")) || "com.mysql.cj.jdbc.Driver".equals(pro.getProperty("jdbc.driverClass"))){
            System.out.println("================================>mysql数据库");
            createMysql();
        }else if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(pro.getProperty("jdbc.driverClass"))){
            System.out.println("================================>sql server数据库");
            createSqlServer();
        }else {
            System.out.println("================================>orcale数据库");
            createOrcale();
        }
        in.close();
        initSheduler();
    }

    public void createMysql(){
       String proopreateinfo_mysql="CREATE TABLE IF NOT EXISTS proopreateinfo(" +
               "  id varchar(32)   NOT NULL DEFAULT '' ," +
               "  proInstanceId varchar(32)   DEFAULT '' ," +
               " taskid varchar(64)   DEFAULT '' ," +
               "  opreateName varchar(255)   DEFAULT '' ," +
               "  opreateRealName varchar(255)   DEFAULT '' ," +
               "  opreateTime datetime DEFAULT NULL ," +
               "  opreateType int(11) DEFAULT 0 ," +
               "  nodeName varchar(255)   DEFAULT '' ," +
               "  mycomment varchar(2000)   DEFAULT '' ," +
               "  attachment varchar(255)   DEFAULT '' ," +
               "  requestid varchar(32)   DEFAULT '' ," +
               "  reportName varchar(255)   DEFAULT '',"+
               "  deployid varchar(32)   DEFAULT '' ,"+
               "  proname  varchar(255)   DEFAULT '' ,"+
               "  PRIMARY KEY (id)" +
               ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";

       String classify_mysql="CREATE TABLE IF NOT EXISTS classify("+
               " id varchar(32) NOT NULL  ," +
               " classifyname varchar(255) DEFAULT '' ,"+
               " tenantid varchar(255) DEFAULT '' ,"+
               "  PRIMARY KEY (id)" +
               ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
       String act_authority_mysql="CREATE TABLE IF NOT EXISTS act_authority("+
               " procdefid varchar(64)," +
               " groupid varchar(255)  ,"+
               " userid varchar(255)  ,"+
               " name varchar(255)  "+
               ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
       String act_scheduler="CREATE TABLE IF NOT EXISTS act_scheduler("+
               " id varchar(64)," +
               " prodefineid varchar(64)  ,"+
               " proInstanceid varchar(64)  ,"+
               " duedate varchar(64)  ,"+
               " tenantid varchar(64) , "+
               " activityid varchar(64) , "+
               " type varchar(64)  "+
               ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
       try{
           jt.execute(proopreateinfo_mysql);
           jt.execute(classify_mysql);
           jt.execute(act_authority_mysql);
           jt.execute(act_scheduler);
       }
       catch (Exception e){
           System.out.println("初始化数据库异常："+e.getMessage());
       }
   }

    public void createSqlServer(){
        String proopreateinfo_SqlServer="if not exists (select * from sysobjects where id = object_id('proopreateinfo')" +
                "  and OBJECTPROPERTY(id, 'IsUserTable') = 1)"+
                "  CREATE TABLE [dbo].[proopreateinfo] (" +
                "   [id] varchar(32) COLLATE Chinese_PRC_CI_AS DEFAULT '' NOT NULL," +
                "   [proInstanceId] varchar(32) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "   [taskid] varchar(64) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "   [opreateName] varchar(255) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "   [opreateRealName] varchar(255) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "    [opreateTime] datetime NULL," +
                "  [opreateType] int DEFAULT ((0)) NULL," +
                "   [nodeName] varchar(255) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "   [mycomment] varchar(2000) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "   [attachment] varchar(255) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "    [requestid] varchar(32) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "  [reportName] varchar(255) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL," +
                "  [deployid] varchar(32) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL,"+
                "  [proname] varchar(255) COLLATE Chinese_PRC_CI_AS DEFAULT '' NULL,"+
                "  primary key (id) "+
                ")  ";


        String classify_SqlServer="if not exists (select * from sysobjects where id = object_id('classify')" +
                "  and OBJECTPROPERTY(id, 'IsUserTable') = 1)"+
                "  CREATE TABLE [dbo].[classify] (" +
                "   [id] varchar(32)  NOT NULL," +
                "   [classifyname] varchar(255) COLLATE Chinese_PRC_CI_AS NULL," +
                "   [tenantid] varchar(255) COLLATE Chinese_PRC_CI_AS NULL," +
                "  primary key (id) "+
                ")  ";
        String act_authority_SqlServer="if not exists (select * from sysobjects where id = object_id('act_authority')" +
                "  and OBJECTPROPERTY(id, 'IsUserTable') = 1)"+
                "  CREATE TABLE [dbo].[act_authority] (" +
                "   [procdefid] varchar(64) ," +
                "   [groupid] varchar(255) ," +
                "   [userid] varchar(255) ," +
                "   [name] varchar(255) " +
                ")  ";
        String act_scheduler="if not exists (select * from sysobjects where id = object_id('act_scheduler')" +
                "  and OBJECTPROPERTY(id, 'IsUserTable') = 1)"+
                "  CREATE TABLE [dbo].[act_scheduler] (" +
                "   [id] varchar(64) ," +
                "   [prodefineid] varchar(64) ," +
                "   [proInstanceid] varchar(64) ," +
                "   [duedate] varchar(64) ," +
                "   [tenantid] varchar(64), " +
                "   [activityid] varchar(64) ," +
                "   [type] varchar(64) " +
                ")  ";
        try{
            jt.execute(proopreateinfo_SqlServer);
            jt.execute(classify_SqlServer);
            jt.execute(act_authority_SqlServer);
            jt.execute(act_scheduler);
        }
        catch (Exception e){
            System.out.println("初始化数据库异常："+e.getMessage());
        }
    }

    public void createOrcale(){
        String proopreateinfo_Orcale="declare "+"  tableExistproopreateinfo NUMBER; "+
                " begin "+
                " SELECT count(1) INTO tableExistproopreateinfo FROM user_tables WHERE table_name = upper( 'PROOPREATEINFO' ); "+
                " IF  tableExistproopreateinfo = 0  THEN  " +
                " execute immediate 'CREATE TABLE proopreateinfo (" +
                " id VARCHAR2(32 BYTE) NOT NULL ," +
                " proInstanceId VARCHAR2(32 BYTE)   NULL ," +
                " taskid VARCHAR2(64 BYTE)   NULL ," +
                " opreateName VARCHAR2(255 BYTE)   NULL ," +
                " opreateRealName VARCHAR2(255 BYTE)   NULL ," +
                " opreateTime DATE DEFAULT NULL  NULL ," +
                " opreateType NUMBER(11) DEFAULT 0  NULL ," +
                " nodeName VARCHAR2(255 BYTE) DEFAULT NULL  NULL ," +
                " mycomment VARCHAR2 (2000 BYTE)   NULL ," +
                " attachment VARCHAR2(255 BYTE)   NULL ," +
                " requestid VARCHAR2(32 BYTE)   NULL ," +
                " reportName VARCHAR2(255 BYTE)   NULL ," +
                " deployid VARCHAR2(32 BYTE)   NULL ," +
                " proname VARCHAR2(255 BYTE)   NULL, " +
                " primary key (id) "+
                ")'; "+
                " end if; "+
                " end;";


        String classify_Orcale="declare "+"  tableExistclassify NUMBER; "+
                " begin "+
                " SELECT count(1) INTO tableExistclassify FROM user_tables WHERE table_name = upper( 'CLASSIFY' ); "+
                " IF  tableExistclassify = 0  THEN  " +
                " execute immediate 'CREATE TABLE classify (" +
                " id VARCHAR2(32 BYTE) NOT NULL ," +
                " classifyname VARCHAR2(255 BYTE) DEFAULT NULL  NULL ," +
                " tenantid VARCHAR2(255 BYTE) DEFAULT NULL  NULL ," +
                " primary key (id) "+
                ")'; "+
                " end if; "+
                " end;";

        String act_authority_Orcale="declare "+"  tableExistact_authority NUMBER; "+
                " begin "+
                " SELECT count(1) INTO tableExistact_authority FROM user_tables WHERE table_name = upper( 'ACT_AUTHORITY' ); "+
                " IF  tableExistact_authority = 0  THEN  " +
                " execute immediate 'CREATE TABLE act_authority (" +
                " procdefid VARCHAR2(64 BYTE) ," +
                " groupid VARCHAR2(255 BYTE), " +
                " userid VARCHAR2(255 BYTE), " +
                " name VARCHAR2(255 BYTE) " +
                ")'; "+
                " end if; "+
                " end;";

        String act_scheduler="declare "+"  tableExistact_scheduler NUMBER; "+
                " begin "+
                " SELECT count(1) INTO tableExistact_scheduler FROM user_tables WHERE table_name = upper( 'ACT_SCHEDULER' ); "+
                " IF  tableExistact_scheduler = 0  THEN  " +
                " execute immediate 'CREATE TABLE act_scheduler (" +
                " id VARCHAR2(64 BYTE) ," +
                " prodefineid VARCHAR2(64 BYTE), " +
                " proInstanceid VARCHAR2(64 BYTE), " +
                " duedate VARCHAR2(64 BYTE), " +
                " tenantid VARCHAR2(64 BYTE) ," +
                " activityid VARCHAR2(64 BYTE), " +
                " type VARCHAR2(64 BYTE) " +
                ")'; "+
                " end if; "+
                " end;";

        try{
            jt.execute(proopreateinfo_Orcale);
            jt.execute(classify_Orcale);
            jt.execute(act_authority_Orcale);
            jt.execute(act_scheduler);
        }
        catch (Exception e){
            System.out.println("初始化数据库异常："+e.getMessage());
        }
    }

    public void initSheduler() throws Exception {
        Date currentTime=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        for(ProcessDefinition p:list){
            if(p.getDescription()!=null){
                String description = p.getDescription();
                if(description==null || "".equals(description)){
                    description="{}";
                }else {
                    JSONObject json=new JSONObject(description);
                    if("1".equals(json.get("state").toString())){
                        //启动定时流程
                        JobDetail jobDetail = JobBuilder.newJob(jobUtil.class)
                                .withIdentity(p.getId()+"_cronJob")
                                .usingJobData("prodefinedid",p.getId())
                                .usingJobData("proname",repositoryService.createDeploymentQuery()
                                        .deploymentId(p.getDeploymentId()).singleResult().getName())
                                .build();
                        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(p.getId()+"_cronTrigger").
                                withSchedule(CronScheduleBuilder.cronSchedule(json.get("cron").toString())).build();
                        Date startTime = cronTrigger.getStartTime();
                        String nextTime=sdf.format(cronTrigger.getFireTimeAfter(startTime));
                        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                        Scheduler scheduler = stdSchedulerFactory.getScheduler();
                        scheduler.scheduleJob(jobDetail,cronTrigger);
                        scheduler.start();
                        json.put("state","1");
                        json.put("nextTime",nextTime);
                        jt.update("update act_re_procdef set DESCRIPTION_=? where ID_=?",new Object[]{json.toString(),
                                p.getId()});
                    }
                }

            }
        }
        System.out.println("=================>初始化定时启动任务成功");

        List<Map<String, Object>> list1 = jt.queryForList("select * from act_scheduler");
        List<String> deleteList=new ArrayList<>();
        for(int i=0;i<list1.size();i++){
            String type=list1.get(i).get("type")==null?"":list1.get(i).get("type").toString();
            String processInstanceId=list1.get(i).get("proInstanceid").toString();
            String prodefineid=list1.get(i).get("prodefineid").toString();
            String activitiId=list1.get(i).get("activityid").toString();
            if("dueTime".equals(type)){
                Date duedate = sdf.parse(list1.get(i).get("duedate").toString());
                if(duedate.before(currentTime)){
                    deleteList.add(list1.get(i).get("id").toString());
                }else {
                    JobDetail jobDetail = JobBuilder.newJob(sendMessageJobUtil.class)
                            .withIdentity(processInstanceId+"_"+activitiId+"_cronJob_sendMessage")
                            .usingJobData("processInstanceId",processInstanceId)
                            .usingJobData("activitiId",activitiId)
                            .build();
                    SimpleTrigger cronTrigger =(SimpleTrigger) TriggerBuilder.newTrigger()
                            .withIdentity(processInstanceId+"_"+activitiId+"_cronTrigger_sendMessage")
                            .startAt(duedate)
                            .build();
                    StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                    Scheduler scheduler = null;
                    scheduler = stdSchedulerFactory.getScheduler();
                    scheduler.scheduleJob(jobDetail,cronTrigger);
                    scheduler.start();
                }
            }else if("allTime".equals(type)){
                Date duedate = sdf.parse(list1.get(i).get("duedate").toString());
                if(duedate.before(currentTime)){
                    deleteList.add(list1.get(i).get("id").toString());
                    ProcessInstance pro = runtimeService.createProcessInstanceQuery().processInstanceId(
                            processInstanceId).singleResult();
                    if(pro!=null){
                        runtimeService.setVariable(processInstanceId,"process_state","9");
                        runtimeService.deleteProcessInstance(processInstanceId,"expired");
                    }
                }else {
                    JobDetail jobDetail = JobBuilder.newJob(progressTimejobUtil.class)
                            .withIdentity(prodefineid+"_"+processInstanceId+"_cronJob_proTime")
                            .usingJobData("processInstanceId",processInstanceId)
                            .usingJobData("processDefinitionId",prodefineid)
                            .usingJobData("activityid",activitiId)
                            .build();
                    SimpleTrigger cronTrigger =(SimpleTrigger)TriggerBuilder.newTrigger()
                            .withIdentity(prodefineid+"_"+processInstanceId+"_cronTrigger_proTime")
                            .startAt(duedate)
                            .build();
                    StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                    Scheduler scheduler = stdSchedulerFactory.getScheduler();
                    scheduler.scheduleJob(jobDetail,cronTrigger);
                    scheduler.start();

                }
            }
        }
        if(deleteList.size()>0){
            String sql="delete from act_scheduler where id in(";
            String temp="";
            for(String s:deleteList){
                temp+="'"+s+"'"+",";
            }
          /*  System.out.println(sql+temp.substring(0,temp.length()-1)+")");*/
            jt.update(sql+temp.substring(0,temp.length()-1)+")");
        }
        System.out.println("=================>初始化定时逾期任务和流程总时间任务成功");
    }
}
