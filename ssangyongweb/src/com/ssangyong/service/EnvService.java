package com.ssangyong.service;

import java.util.HashMap;

import com.ssangyong.dao.EnvDao;

public class EnvService {
    @SuppressWarnings({ "unchecked" })
    public HashMap<String, String> getTCWebEnv() {
        EnvDao dao = new EnvDao();
        return (HashMap<String, String>)dao.getTCWebEnv();
    }
}
