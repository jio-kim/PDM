package com.kgm.soa.model.part;

import java.util.HashMap;

import com.kgm.soa.biz.Session;

public abstract class PartModel {
    public Session session;
    public String itemId;
    public String itemRevId;
    public String itemName;
    public String unit;
    
    public HashMap<String, Object> attrMap;
    public HashMap<String, Object> param;

    public static String REQUIRED_INPUT_VALUE = "'{0}'��(��) �ʼ��Է� �����Դϴ�.";
    public static String REQUIRED_INPUT_LIMIT_VALUE = "'{0}'��(��) '{1}'�ڷ� �Է��ϼž� �մϴ�.";
    public static String NOT_VALIDE_ITEM_ID = "Item ID�� ��ȿ���� �ʽ��ϴ�.";
    public static String EXIST_ITEM_ID = "'{0}' Item ID�� �����մϴ�.";
    public static String NOT_EXIST_ATTR_VALUE = "'{0}' �Ӽ����� List�� �������� �ʽ��ϴ�.";
    public static String REQUIRED_VALUE = "'{0}'�� �ݵ�� ���� �����ؾ� �մϴ�.";
    public static String LIMITED_VALUE = "'{0}'��(��) ���� '{1}', �Ҽ��� ���� '{2}'�ڸ� ���� �����մϴ�.";
    public static String CASE_INPUT_VALUE = "'{0}' ���� '{1}'�� ��� '{2}'��(��) �Է��ϼž� �մϴ�.";
    public static String CASE_INPUT_LIMIT_VALUE = "'{0}' ���� '{1}'�� ��� '{2}'��(��) '{3}'�ڷ� �Է��ϼž� �մϴ�.";

    public PartModel(HashMap<String, Object> param) {
        this.param = param;
    }  

    abstract public HashMap<String, Object> getRevProperties() throws Exception;

    abstract public void validator() throws Exception;

    public static String getStringMsg(String targetMsg, String[] paramData) {
        if (targetMsg == null) {
            return "";
        }
        for (int i = 0; i < paramData.length; i++) {
            targetMsg = targetMsg.replaceAll("\\{" + i + "\\}", paramData[i]);
        }
        return targetMsg;
    }
}
