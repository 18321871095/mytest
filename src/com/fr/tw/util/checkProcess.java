package com.fr.tw.util;

import com.fr.tw.rsa.Base64Utils;
import com.fr.tw.rsa.LicenseGenerator;
import com.fr.tw.rsa.RSAUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class checkProcess {
    public static String check(String path, HttpServletRequest request, HttpServletResponse response,List<String> mydate) throws Exception {
        try {
            String myresult="";
            byte[] encodedData = Base64Utils.fileToByte(path);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(encodedData, LicenseGenerator.privateKey);
            String data = new String(decodedData);
           // System.out.println(data);
            if ("".equals(data)) {
                //没有lic文件或文件位置放错了
                myresult= "000";
            } else {
                if (data.indexOf("MACAddress") == -1 || data.indexOf("IdentificationCode") == -1) {
                    //读取免费lic，判断时间
                    for (String str : data.split(";")) {
                        String[] split = str.split("=");
                        if ("TimeEnd".equals(split[0])) {
                            Date current = new Date();
                            Date current1 = new Date(Long.valueOf(split[1]));
                            long difference =  (current.getTime()-current1.getTime())/86400000;
                            mydate.add(Math.abs(difference)+"");
                            if (!current.after(current1)) {
                                //放行
                                myresult= "100";
                            } else {
                                //lic过期
                                myresult= "001";
                            }
                        }
                        else {
                            myresult="002";
                        }
                    }
                } else {
                    if(data.indexOf("EnterpriseName") != -1 && data.indexOf("MACAddress") != -1 &&
                            data.indexOf("TimeEnd") != -1 && data.indexOf("IdentificationCode") != -1){
                        //表明都有值，lic文件正常
                        String MACAddress=null;
                        List<String> result=new ArrayList<>();
                        String TimeEnd=null;String IdentificationCode=null;
                        for (String str : data.split(";")) {
                            String[] split = str.split("=");
                            if ("MACAddress".equals(split[0])) {
                                MACAddress=split[1];
                            }else if("TimeEnd".equals(split[0])){
                                TimeEnd=split[1];
                            }else if("IdentificationCode".equals(split[0])) {
                                IdentificationCode=split[1];
                            }
                        }
                        Date current = new Date();
                        Date current1 = new Date(Long.valueOf(TimeEnd));
                        if (!current.after(current1)) {
                            result.add("1");
                        }
                        InetAddress ia = InetAddress.getLocalHost();
                       // System.out.println("本地mac："+getMac.getLocalMac(ia));
                     //   System.out.println("lic mac："+MACAddress);
                        if(getMac.getLocalMac(ia).equals(MACAddress)){
                            result.add("1");
                        }
                        //System.out.println(getMac.getLocalMac(ia).equals(IdentificationCode.split("TongWaveBPM")[1]));
                        if(IdentificationCode.indexOf("TongWaveBPM")!=-1){
                            if(getMac.getLocalMac(ia).equals(IdentificationCode.split("TongWaveBPM")[1])){
                                result.add("1");
                            }
                        }
                        if(result.size()==3){
                            myresult= "101";
                        }else {
                            myresult= "002";
                        }
                    }else {
                        //表明有lic但是文件损坏，没有以上键值
                        myresult= "002";
                    }

                }
            }//else 文件读取成功
            return  myresult;
        }
        catch (Exception e){
            return "异常信息:"+e.getMessage();
        }

    }
}
