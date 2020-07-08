package com.fr.tw.util;

import org.activiti.engine.task.Task;
import org.apache.commons.collections4.MapUtils;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;

public class sortListByTime {
    public static void taskLists(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Collections.sort(tasks, new Comparator<Task>() {
            public int compare(Task o1, Task o2) {
                Date createTime1 = o1.getCreateTime();
                Date createTime2 = o2.getCreateTime();
                Collator instance = Collator.getInstance(Locale.CHINA);
                if( instance.compare(sdf.format(createTime1), sdf.format(createTime2))<0){
                    return  1;
                }else {
                    return -1;
                }
            }
        });
    }
    public static void listSortByOpreateTime(List<Map<String, Object>> resultList) {
        Collections.sort(resultList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String name1= MapUtils.getString(o1, "opreateTime");
                String name2=MapUtils.getString(o2, "opreateTime");
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);
            }
        });
    }
    //由小到大
    public static void listSortByPizhuTime(List<Map<String, Object>> resultList) {
        Collections.sort(resultList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String name1= MapUtils.getString(o1, "pizhutime");
                String name2=MapUtils.getString(o2, "pizhutime");
                Collator instance = Collator.getInstance(Locale.CHINA);
                if(instance.compare(name1, name2)<0){
                    return  1;
                }else {
                    return -1;
                }
            }
        });
    }

    public static void listSortByProStartTime(List<Map<String, Object>> resultList) {
        Collections.sort(resultList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String name1= MapUtils.getString(o1, "proStartTime");
                String name2=MapUtils.getString(o2, "proStartTime");
                Collator instance = Collator.getInstance(Locale.CHINA);
                if(instance.compare(name1, name2)<0){
                    return  1;
                }else {
                    return -1;
                }
            }
        });
    }

}
