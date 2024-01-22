package com.symc.plm.me.common;

/**
 * 
 * TCTextService 클래스의 getTextValue 메소드 파라미터 key 값 정의
 * 해당 key 값들은 TC_ROOT/lang/textserver/en_US(각 언어별)/qry_text_locale.xml 정의되어 있다.
 * ex) textService.getTextValue("k_find_item_name") return 값은 Teamcenter가 영문일 경우 Item... 한글일 경우 아이템...
 *
 */
public class SDVQryTextLocaleConstant {

	public static final String GENERAL = "k_find_general_name";
	public static final String ITEM = "k_find_item_name";
	public static final String ITEM_REVISION = "k_find_itemrevision_name";
	public static final String DATASET = "k_find_dataset_name";

}
