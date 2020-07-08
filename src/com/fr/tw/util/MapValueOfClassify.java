package com.fr.tw.util;

import java.util.*;

public class MapValueOfClassify {
    public static  List<List<Map<String,Object>>> getListArrayByMapValueOfClassify(List<Map<String,Object>> list, String mapVaule, Set<String> sets){
        List<List<Map<String,Object>>> resultList=new ArrayList<>();
        for (String s:sets) {
            List<Map<String,Object>> lm=new ArrayList<>();
            for (Map<String,Object> m:list) {
                if(m.get(mapVaule).toString().equals(s)){
                    lm.add(m);
                }
            }
            resultList.add(lm);
        }//for set
        return resultList;
    }
}
