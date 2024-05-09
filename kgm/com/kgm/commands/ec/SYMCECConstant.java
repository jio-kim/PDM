package com.kgm.commands.ec;


/** EC ���� ��� ���� */
public class SYMCECConstant {
	//������ �⺻
	public static final String EPM_TASK_TYPE = "EPMTask";
	public static final String EPM_REVIEW_TASK_TYPE = "EPMReviewTask";
	public static final String EPM_SELECT_SIGNOFF_TASK_TYPE = "EPMSelectSignoffTask";
	public static final String EPM_PERFORM_SIGNOFF_TASK_TYPE = "EPMPerformSignoffTask";
	public static final String EPM_DO_TASK_TYPE = "EPMDoTask";
	public static final String EPM_ACKNOWLEDGE_TASK_TYPE = "EPMAcknowledgeTask";
	public static final String EPM_CONDITION_TASK_TYPE = "EPMConditionTask";
	public static final String EPM_NOTIFY_TASK_TYPE = "EPMNotifyTask";
	public static final String EPM_ROUTE_TASK_TYPE = "EPMRouteTask";
	
	/** �ֿ� ECO Type */
	public static String ECOTYPE = "S7_ECO";
	/** �ֿ� ECI Type */
	public static String ECITYPE = "S7_ECI";
	/** �ֿ� ������ */
	public static String EC_REV_ID = "000";
	/** ECI ID PREFIX */
	public static String ECI_NO_PREFIX = "ECI";
	/** ������ �� �����̼� */
	public static String DATASET_REL = "IMAN_specification";
	/** Item ������ �� �����̼� */
	public static String ITEM_DATASET_REL = "IMAN_reference";
	/** ������ ������ */
	public static String PROBLEM_REL = "CMHasProblemItem";
	/** �ַ�� ������ */
	public static String SOLUTION_REL = "CMHasSolutionItem";
	/** ����[ECI] **/
	public static String IMPLEMENTS_REL = "CMImplements";
	/** ���� ������[���� EC] */
	public static String CONCURRENT_ECO = "CMReferences";
	/** Custom Plug-in Name */
	public static final String PLUGIN_ID = "com.kgm-newplm";
	
	public static final String SEPERATOR = ",";

	//�ֿ��� �߰� ����
	/** ECO ���� Ÿ��ũ ��� */
	public static final String ECO_PROCESS_TEMPLATE = "SYMC_ECO";
	public static final String ECI_PROCESS_TEMPLATE = "SYMC_ECI";
	/** ECO ���� Ÿ��Ʈ ����Ʈ : ���� ��� */
	public static final String[] ECO_TASK_LIST = new String[]{"Related Team Review", "Sub-team Leader", "Design Team Leader", "Technical Management", "Reference Department"}; // , "Deployment"};
	/** ECI ���� Ÿ��Ʈ ����Ʈ : ���� ��� */
	public static final String[] ECI_TASK_LIST = new String[]{"Review 1", "Review 2", "Review 3", "Review 4"};
	/** 0=group, 1=role, 2=user_name, 3=the_user, 4=fnd0objectId */
	public static final String[] ECO_MEMBER_PROPERTIES = new String[]{"group", "the_user", "role", "user_name", "fnd0objectId"};
	public static final String[] ECO_POPUP_PROPERTIES = new String[]{"item_id", "object_desc", "owning_user", "owning_group", "creation_date"};
	public static final String[] ECI_POPUP_PROPERTIES = new String[]{"item_id", "s7_TITLE", "owning_user", "owning_group", "creation_date"};
	/** {"ECO NO.", "ECO Desc", "Creator", "Dept", "Creation Date"} */
	public static String[] ECO_CONCURRENTECO_TABLE_COLS = new String[]{"ECO NO.", "ECO Desc", "Creator", "Dept", "Creation Date"};
	/** {"ECI Approval NO.", "TITLE", "Creator", "Dept", "Creation Date"}
	 *  [SR140923-023][20140923][jclee] ECI NO -> ECI Approval NO�� ��ȯ. 
	 */
	public static String[] ECI_CONCURRENTECO_TABLE_COLS = new String[]{"ECI Approval NO.", "TITLE", "Creator", "Dept", "Creation Date"};
}
