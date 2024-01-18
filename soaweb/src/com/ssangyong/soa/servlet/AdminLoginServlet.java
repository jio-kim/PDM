package com.ssangyong.soa.servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import com.ssangyong.soa.service.EnvService;

@SuppressWarnings("serial")
public class AdminLoginServlet extends HttpServlet {
    
    ServletConfig config;

    public void init(ServletConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.distMethod(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.distMethod(req, resp);
    }

    private void distMethod(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
        String method = req.getParameter("method");
        if ("login".equals(method)) {
            login(req, resp);           
           
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loginMsg = "";
        EnvService envService = new EnvService();
        HashMap<String, String> env = envService.getTCWebEnv();        
        String confirmLoginId = env.get("TC_DEMON_ID");
        String confirmLoginPasswd = "";        
        try {
            confirmLoginPasswd = new String(Base64.decodeBase64(env.get("TC_DEMON_PASSWD").getBytes()));
        } catch (Exception e1) {
            e1.printStackTrace();
            loginMsg = "로그인 서버 오류입니다.";
        }
        String id = req.getParameter("id");
        String passwd = req.getParameter("passwd");                
        
        try {            
            if (confirmLoginId.equals(id) && confirmLoginPasswd.equals(passwd)) {
                loginMsg = "SUCESS";
            } else {           
                loginMsg = "ERROR : ID 또는 Password를 확인바랍니다.";
            }       
            
        } catch(Exception e) {
            loginMsg = e.getMessage();
            e.printStackTrace();                    
        } finally {                       
           
        }
        HttpSession httpSession = req.getSession();
        httpSession.setAttribute("LOGIN_ERROR", loginMsg);        
        if(!"SUCESS".equals(loginMsg)) {
            resp.sendRedirect("/soaweb/damon/index.jsp");
        } else {
            resp.sendRedirect("/soaweb/damon/process.jsp");
        }
    }

}
