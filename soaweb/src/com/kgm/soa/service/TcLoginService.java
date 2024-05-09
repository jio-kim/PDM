package com.kgm.soa.service;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import com.kgm.soa.biz.Session;
import com.kgm.soa.biz.TcSessionUtil;

public class TcLoginService {

    private EnvService envService;

    /**
     * Service를 위해 구동될때 사용자의 Group을 dba Group으로 정의된 Daemon 구동을위해
     * DB에 저장된 사용자 계정으로 Login 한다.
     * @return
     * @throws Exception
     */
    public Session getTcSession() throws Exception {
    	envService = new EnvService();
        HashMap<String, String> env = envService.getTCWebEnv();
        // Get optional host information
        String serverHost = env.get("TC_WEB_URL");
        String userId = env.get("TC_DEMON_ID");
        String password = "";
        Session session = null;
        try {
            password = new String(Base64.decodeBase64(env.get("TC_DEMON_PASSWD").getBytes()));
            session = new Session(serverHost, userId, password, "dba");
            // BYPASS
            TcSessionUtil sessionUtil = new TcSessionUtil(session);
            sessionUtil.setByPass();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return session;
    }
    
    public Session getTcSession(String userId, String password) throws Exception {
    	envService = new EnvService();
        HashMap<String, String> env = envService.getTCWebEnv();
        
        // Get optional host information
        String serverHost = env.get("TC_WEB_URL");
        Session session = null;
        try {
            session = new Session(serverHost, userId, password, "dba");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return session;
    }
    
    /**
     * [NON-SR][2016-11-04] taeku.jeong
     * Report등을 작성할때 Teamcenter RichClient에서 받은 Login 정보를 이용해 Login 하도록 했음.
     * 이때 사용자의 경우 dba Group이 아닌경우가 대부분 이므로 Login할때 Group정보를 설정하도록한
     * Function을 추가 했음.
     * @param userId
     * @param password
     * @param group
     * @return
     * @throws Exception
     */
    public Session getTcSession(String userId, String password, String group) throws Exception {
    	envService = new EnvService();
        HashMap<String, String> env = envService.getTCWebEnv();
        
        // Get optional host information
        String serverHost = env.get("TC_WEB_URL");
        Session session = null;
        try {
            session = new Session(serverHost, userId, password, group);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return session;
    }
    
}
