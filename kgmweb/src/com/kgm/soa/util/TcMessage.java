package com.kgm.soa.util;



/**
 * 
 * Desc : define message in the class, only for returning specific result.
 * @author yunjae.jung
 */
public class TcMessage {

    
    /** �˻� ����� ���� ��� */
    public static final String MSG_NO_RESULT = "Not found result!!";
    /** �� ��� Ȥ�� ó���� �������� ���� ������Ʈ�� ��� */
    public static final String MSG_INVAID_OBJECT_TYPE = "Invalid object type.";
    /** �����ϰ� �ִ� ������Ʈ�� ���� ���*/
    public static final String MSG_NO_REFERENCE_OBJECT = "No referenced object.";
    /** ��� ������Ʈ�� üũ�ƿ� �Ǿ� �ִ� ���*/
    public static final String MSG_CHECKOUT_EXIST ="Object is already checkout.";
    /** ��� ������Ʈ�� ������, ������ Ȥ�� ���� ������ ������ ���ϴ� ����� ������ ��ġ���� �ʴ� ���*/
    public static final String MSG_NOT_EQUAL_USER = "Not equal user of target obect";
    /** ���� ó���� ������ ���, üũ�ƿ�, ����, �����ڸ� Ȯ���� �ʿ��� �ȳ� �޽����� ���*/
    public static final String MSG_NEED_VERIFY = "Failed to validate. Must verify check-out, status, owner of below items";
    /** ���� ������ �������� �ʴ� ���*/
    public static final String MSG_NO_EXIST_NR = "NamedReference Object does not exist.";
    /** �Լ� ó���� �ʿ��� �Ű������� ���� ���*/
    public static final String MSG_REQUIRED_PARAM = "Does not exist required parameter.";
    /** �α��� �� ������� ���� ������ �������� ���� ���*/
    public static final String MSG_INVALID_CREDENTIAL = "The login attempt failed: either the user ID or the password is invalid.";
    /** User ���� �����Ͽ�����, Person�� ���� �� ���*/
    public static final String MSG_MAKE_USER_FAIL_USER = "Failed to create user, but successfully created person";
    /** User ���� �����Ͽ�����, Person ���� ���� �� ���*/
    public static final String MSG_MAKE_USER_FAIL_PERSON = "Failed to create person, but successfully created user";
    /** User, Person ���� ��� ���� �� ���*/
    public static final String MSG_MAKE_USER_FAIL_ALL = "Failed to perform make_user";
    /** ������� SQL�� Update ������ ���*/ 
    public static final String MSG_DB_UPDATE_FAIL = "Failed to update object on dbms";
    /** Preference���� LG_Telnet_Connection_Info�� ����(����IP/Host, id, password)�� �������⿡ �����ϰų�, ������ ���������� ��� */
    public static final String MSG_TELNTE_CONNECT_INFO_FAIL = "Failed to retrieve information of connecting on telnet from preference.";
    /** Telnet ���� ������ ���*/
    public static final String MSG_TELNET_CONNECT_TRY_FAIL = "Failed to connect on telnet.";
}
