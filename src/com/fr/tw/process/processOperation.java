package com.fr.tw.process;

import com.fr.tw.util.checkProcess;
import com.fr.tw.util.getFilePathName;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@WebFilter(filterName="processFilter",
        urlPatterns={"/static/jsp/frontEnd/*","/static/jsp/adminjsp/*","/processDiagram/create","/processInfo/authority",
                "/static/jsp/mobile/*","/mobile/authority"})
public class processOperation implements Filter{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse Response, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request1=(HttpServletRequest) request;
        HttpServletResponse response1=(HttpServletResponse) Response;
        HttpSession session=request1.getSession();
        String url="http://" + request1.getServerName() //服务器地址
                + ":"
                + request1.getServerPort()           //端口号
                + request1.getContextPath();
        try {
                String realPath = getFilePathName.getFileName(request1);
                List<String> mydate=new ArrayList<>();
                String checkResult=checkProcess.check(realPath,request1,response1,mydate);
                if(checkResult.length()==3){
                    if("100".equals(checkResult)){
                        ((HttpServletResponse) Response).setHeader("register","false");
                        ((HttpServletResponse) Response).setHeader("time",mydate.size()==0?"":mydate.get(0));
                        filterChain.doFilter(request,Response);
                    }else  if("101".equals(checkResult)){
                        ((HttpServletResponse) Response).setHeader("register","true");
                        filterChain.doFilter(request,Response);
                    }
                    else  if("000".equals(checkResult)){
                        response1.sendRedirect(url+"/static/jsp/message.jsp?message=No LIC files or misplaced files");
                    }else if("001".equals(checkResult)){
                        response1.sendRedirect(url+"/static/jsp/message.jsp?message=LIC file has expired");
                    }else if("002".equals(checkResult)){
                        response1.sendRedirect(url+"/static/jsp/message.jsp?message=LIC file corruption");
                    }

                }else {
                    //异常
                    response1.sendRedirect(url+"/static/jsp/message.jsp?message="+checkResult);
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void destroy() {

    }
}
