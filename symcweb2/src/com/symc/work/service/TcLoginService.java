package com.symc.work.service;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcSessionUtil;
import com.symc.common.util.ContextUtil;

public class TcLoginService {

    private EnvService envService;

    public Session getTcSession() throws Exception {
        envService = (EnvService) ContextUtil.getBean("envService");
        HashMap<String, String> env = envService.getTCWebEnv();
        // Get optional host information
        String serverHost = env.get("TC_WEB_URL");
        String userID = env.get("TC_DEMON_ID");
        String password = "";
        Session session = null;
        try {
            password = new String(Base64.decodeBase64(env.get("TC_DEMON_PASSWD").getBytes()));
            session = new Session(serverHost, userID, password);
            // BYPASS
            TcSessionUtil sessionUtil = new TcSessionUtil(session);
            sessionUtil.setByPass();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return session;
    }
}
