package com.kgm.soa.common.constants;


/**
 * 
 * Desc : define message in the class, only for returning specific result.
 * @author yunjae.jung
 */
public class TcMessage {
    
    /** 검색 결과가 없는 경우 */
    public static final String MSG_NO_RESULT = "Not found result!!";
    /** 비교 대상 혹은 처리에 적합하지 않은 오브젝트의 경우 */
    public static final String MSG_INVAID_OBJECT_TYPE = "Invalid object type.";
    /** 참조하고 있는 오브젝트가 없는 경우*/
    public static final String MSG_NO_REFERENCE_OBJECT = "No referenced object.";
    /** 대상 오브젝트가 체크아웃 되어 있는 경우*/
    public static final String MSG_CHECKOUT_EXIST ="Object is already checkout.";
    /** 대상 오브젝트의 생성자, 소유자 혹은 최종 수정자 정보가 비교하는 사용자 정보와 일치하지 않는 경우*/
    public static final String MSG_NOT_EQUAL_USER = "Not equal user of target obect";
    /** 검증 처리에 실패한 경우, 체크아웃, 상태, 소유자를 확인이 필요한 안내 메시지의 경우*/
    public static final String MSG_NEED_VERIFY = "Failed to validate. Must verify check-out, status, owner of below items";
    /** 명명된 참조가 존재하지 않는 경우*/
    public static final String MSG_NO_EXIST_NR = "NamedReference Object does not exist.";
    /** 함수 처리에 필요한 매개변수가 없는 경우*/
    public static final String MSG_REQUIRED_PARAM = "Does not exist required parameter.";
    /** 로그인 시 사용자의 접속 정보가 적합하지 않은 경우*/
    public static final String MSG_INVALID_CREDENTIAL = "The login attempt failed: either the user ID or the password is invalid.";
    /** User 생성 실패하였으나, Person은 생성 된 경우*/
    public static final String MSG_MAKE_USER_FAIL_USER = "Failed to create user, but successfully created person";
    /** User 생성 성공하였으나, Person 생성 실패 한 경우*/
    public static final String MSG_MAKE_USER_FAIL_PERSON = "Failed to create person, but successfully created user";
    /** User, Person 생성 모두 실패 한 경우*/
    public static final String MSG_MAKE_USER_FAIL_ALL = "Failed to perform make_user";
    /** 명시적인 SQL로 Update 실패한 경우*/ 
    public static final String MSG_DB_UPDATE_FAIL = "Failed to update object on dbms";
    /** Preference에서 LG_Telnet_Connection_Info의 정보(접속IP/Host, id, password)를 가져오기에 실패하거나, 정보가 비정상적인 경우 */
    public static final String MSG_TELNTE_CONNECT_INFO_FAIL = "Failed to retrieve information of connecting on telnet from preference.";
    /** Telnet 접속 실패한 경우*/
    public static final String MSG_TELNET_CONNECT_TRY_FAIL = "Failed to connect on telnet.";

    //===============================================
    // Returen Message Attribute
    //===============================================
    public static final String TC_RETURN_MESSAGE = "ReturnMsg";
    public static final String TC_RETURN_OK = "OK";
    public static final String TC_RETURN_FAIL    = "FAIL";
    public static final String TC_RETURN_FAIL_REASON = "FAIL_REASON";
    public static final String TC_RETURN_STOP_ROW = "STOP_ROW";
    
}
