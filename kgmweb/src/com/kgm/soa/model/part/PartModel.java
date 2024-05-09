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

    public static String REQUIRED_INPUT_VALUE = "'{0}'은(는) 필수입력 사항입니다.";
    public static String REQUIRED_INPUT_LIMIT_VALUE = "'{0}'은(는) '{1}'자로 입력하셔야 합니다.";
    public static String NOT_VALIDE_ITEM_ID = "Item ID가 유효하지 않습니다.";
    public static String EXIST_ITEM_ID = "'{0}' Item ID가 존재합니다.";
    public static String NOT_EXIST_ATTR_VALUE = "'{0}' 속성값이 List에 존재하지 않습니다.";
    public static String REQUIRED_VALUE = "'{0}'에 반드시 값을 선택해야 합니다.";
    public static String LIMITED_VALUE = "'{0}'은(는) 정수 '{1}', 소수점 이하 '{2}'자리 까지 가능합니다.";
    public static String CASE_INPUT_VALUE = "'{0}' 값이 '{1}'인 경우 '{2}'을(를) 입력하셔야 합니다.";
    public static String CASE_INPUT_LIMIT_VALUE = "'{0}' 값이 '{1}'인 경우 '{2}'은(는) '{3}'자로 입력하셔야 합니다.";

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
