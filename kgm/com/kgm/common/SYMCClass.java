package com.kgm.common;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class SYMCClass {
	/**************************************** �ֿ� ItemType ���� String **************************************************************/
  
  /** �ֿ� Product Type */
  public static String S7_PRODUCTPARTTYPE = "S7_Product";
  /** �ֿ� Product Revision Type */
  public static String S7_PRODUCTPARTREVISIONTYPE =  "S7_ProductRevision";

  
  /** �ֿ� Function Type */
  public static String S7_FNCPARTTYPE = "S7_Function";
  /** �ֿ� Function Revision Type */
  public static String S7_FNCPARTREVISIONTYPE =  "S7_FunctionRevision";

  
  /** �ֿ� Function Master Type */
  public static String S7_FNCMASTPARTTYPE = "S7_FunctionMast";
  /** �ֿ� Function Master Revision Type */
  public static String S7_FNCMASTPARTREVISIONTYPE =  "S7_FunctionMastRevision";

  
  /** �ֿ� Variant Type */
  public static String S7_VARIANTPARTTYPE = "S7_Variant";
  /** �ֿ� Variant Revision Type */
  public static String S7_VARIANTPARTREVISIONTYPE =  "S7_VariantRevision";

  
  
	/** �ֿ� Vehpart Type */
	public static String S7_VEHPARTTYPE = "S7_Vehpart";
	/** �ֿ� Vehpart Revision Type */
	public static String S7_VEHPARTREVISIONTYPE =  "S7_VehpartRevision";
	
	
  /** �ֿ� Material Type */
  public static String S7_MATPARTTYPE = "S7_Material";
  /** �ֿ� Material Revision Type */
  public static String S7_MATPARTREVISIONTYPE =  "S7_MaterialRevision";

  /** �ֿ� Standard Type */
  public static String S7_STDPARTTYPE = "S7_Stdpart";
  /** �ֿ� Standard Revision Type */
  public static String S7_STDPARTREVISIONTYPE =  "S7_StdpartRevision";

  /** �ֿ� Standard Type */
  public static String S7_SOFTPARTTYPE = "S7_Software";
  /** �ֿ� Standard Revision Type */
  public static String S7_SOFTPARTREVISIONTYPE =  "S7_SoftwareRevision";
  

  /** �ֿ� TechDoc Type */
  public static String S7_TECHDOCTYPE = "S7_ENGDOC";
  /** �ֿ� TechDoc Revision Type */
  public static String S7_TECHDOCREVISIONTYPE =  "S7_ENGDOCRevision";
  
  
  /** �ֿ� Project Type */
  public static String S7_PROJECTTYPE = "S7_PROJECT";
  /** �ֿ� Project Revision Type */
  public static String S7_PROJECTREVISIONTYPE =  "S7_PROJECTRevision";

	
	
	/** �ֿ� Item Type */
	public static String ITEM_TYPE = "Item";
	/** �ֿ� Item Revision ID */
	public static String ITEM_REV_ID = "000";

	/**************************************** �ֿ� Query ���� String **************************************************************/
	/** Item Search Name */
	public static String ITEMS_SEARCH = "Item...";
	
	public static String[] ITEMS_IDNAMETYPE_SEARCH_KEY = {"ItemID", "Name", "Type"};
	
	/**************************************** Value ���� String **************************************************************/
	/** ItemID field �� */
	public static String ITEMIDFIELD = "Item ID";
	
	/** CIS NO field �� */
	public static String CISNOFIELD = "CIS No";
	
	/** Name field �� */
	public static String NAMEFIELD = "Name";
	
	/** �ֿ� ���� ����Ʈ ���� */
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	/** �ֿ� ����Ʈ ���� */
	public static SimpleDateFormat DATE_FORMAT_MM = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	/** �ֿ� IMAN_reference Ÿ�� */
	public static String REFERENCE_REL = "IMAN_reference";
	
	/** �ֿ� Contents Ÿ�� */
	public static String CONTENT_REL = "contents";
	
	/** �ֿ� ��ǰ ������ �ؿ� ���� �����̼� */
	public static String RELATED_DWG_REL = "IMAN_specification";
	
	public static int PROBLEM = 0;
	public static int SOLUTION = 1;
	
	/** WEBJDBC URL */
	public static String SYMC_WEBJDBC_URL = "SYMC_WEBJDBC_URL";
	
	/** FTP INFO [0=URL,1=PORT,2=USER,3=PASS,4=ROOT] �����ڴ� �޸�(,) */
	public static String SYMC_FTP_INFO = "SYMC_FTP_INFO";
	
	/** �ֿ� INFODBA ���� Ű ��. */
	public static String INFODBA = "infodba";
	/** �ֿ� IF_USER ���� Ű ��. */
	public static String IF_USER = "if_user";
	
	/**************************************** ECR ���� LOV **************************************************************/
	/** �ֿ� ECR ���� ��� LOV */
	public static String ECR_REVIEW_RESULT_LOV = "PK_ECR_REVIEW_RESULT.LOV";
	
	/**************************************** ���� ��� **************************************************************/
	/** ǳ�� ǥ�ؾ���� add�ϴ� relation Ÿ�� */
	public static String STANDARD_DOC_REL = "PK4_standard_doc_rel";
	/** ǳ�� ���� Dummy Item Ÿ�� */
	public static String Equipment_Dummy_TYPE = "PK4_dummy";
	/** ǳ�� ��ǰ �˻� ���� */
	public static String QryProductSearch = "PK_PRODUCT_SEARCH";
	/** ǳ�� ������ �˻� ���� */
	public static String QryRawMaterialsSearch = "PK_RAWMATERIALS_SEARCH";
	/** ǳ�� ���� Item Ÿ�� */
	public static String Processing_TYPE = "PK4_Processing";
	/** ǳ�� ���� ETC �˻� ���� */
	public static String QryEquipmentESearch = "PK_EQUIPMENTE_SEARCH";
	/** ǳ�� ������ �˻� ���� */
	public static String QrySubMaterialsSearch = "PK_SUBMATERIALS_SEARCH";
	/** ǳ�� ���� Former �˻� ���� */
	public static String QryEquipmentFSearch = "PK_EQUIPMENTF_SEARCH";
	/** ǳ�� ���� Tapping �˻� ���� */
	public static String QryEquipmentTSearch = "PK_EQUIPMENTT_SEARCH";
	/** ǳ�� ���� Former �˻� ���� */
	public static String QryMoldFormerSearch = "PK_MOLD_FORMER_SEARCH";
	/** ǳ�� ���� Tapping �˻� ���� */
	public static String QryMoldTappingSearch = "PK_MOLD_TAPPING_SEARCH";
	/** ǳ�� ���� ��Ÿ �˻� ���� */
	public static String QryMoldEtcSearch = "PK_MOLD_ETC_SEARCH";
	/** ǳ�� ���� TAP �˻� ���� */
	public static String QryMoldTapSearch = "PK_MOLD_TAP_SEARCH";
	
	/** �ֿ� Temp ���. */
	public static String TEMPDIRECTORY = "C:/Siemens/ssangyong/temp";
	/** �ֿ� ���� �˻� ���� */
	public static String QryUserSearch = "PK_USER_SEARCH";
	/** �ֿ� Item... �˻� ���� */
	public static String QryItemSearch = "Item...";
	/** �ֿ� ���ǹ������� Ÿ�� �̸� */
	public static String CONSENSUS_DOC_WORKFLOW = "���ǹ�������";
	/** �ֿ� IMAN_specification Ÿ�� */
	public static String SPECIFICATION_REL = "IMAN_specification";
	
	/**************************************** SPAlM DATASET TYPE **************************************************************/
	/** ÷�� �Ǵ� �����ͼ� Ÿ�� ���� */
	public static String SYMC_DATASET_TYPE = "SYMC_DATASET_TYPE_LIST";
	/** ���� �Ǵ� ������ Ÿ�� ���� */
	public static String SYMC_FOLDER_TYPE = "SYMC_FOLDER_TYPE_CHECK";
	/** ���� �Ǵ� ������ Role ���� ���� */
	public static String SYMC_FOLDER_ROLE = "SYMC_FOLDER_ROLE_CHECK";
	/** ���� �Ǵ� ������ Type ���� */
	public static String SYMC_CREATEFOLDERTYPE = "SYMC_CREATE_FOLDER_TYPE";
	
	public static Registry registry = Registry.getRegistry("com.teamcenter.rac.common.common");
	
	/** 
	 * �ֿ� ����Ÿ�� ÷�� �� ���� Ÿ��
	 * �ʿ�� �߰�
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since  : 2012. 3. 26.
	 * @return filterHash
	 */
	public static Hashtable<String, String> DATASET_FILTER_DOC(){
		
		Hashtable<String, String> filterHash = new Hashtable<String, String>();
        
        TCSession session = CustomUtil.getTCSession();
        TCPreferenceService preferenceService = session.getPreferenceService();
        //String[] lov = preferenceService.getStringArray(TCPreferenceService.TC_preference_all, SYMC_DATASET_TYPE);
        String[] lov = preferenceService.getStringValues(SYMC_DATASET_TYPE);
        
        if(lov == null || lov.length == 0){
//			MessageBox.post("�����ͼ� ���� Preference ���� �����ϴ�. SYMC_DATASET_TYPE_LIST�� ��� �Ͻʽÿ�.(key(Ȯ����);value(��) <Array>)", "�˸�", MessageBox.INFORMATION);
        	MessageBox.post(registry.getString("DatasetDialog.Message.NoDatasetPreference"), registry.getString("DatasetDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return null;
		}
		
		int lovSize = lov.length;
		for(int i=0; i<lovSize; i++){
			String[] lovArr = lov[i].split(";");
			if(lovArr.length > 1){
				filterHash.put(lovArr[0], lovArr[1]);
			}
		}
        return filterHash;
    }
	
	/**
	 * �ֿ� Create Folder Type �� ��ȯ
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2012. 12. 13.
	 * @return
	 */
	public static String[] FOLDERTYPECHECK(){
		TCSession session = CustomUtil.getTCSession();
        TCPreferenceService preferenceService = session.getPreferenceService();
        
        //String[] lov = preferenceService.getStringArray(TCPreferenceService.TC_preference_all, SYMC_FOLDER_TYPE);
        String[] lov = preferenceService.getStringValues(SYMC_FOLDER_TYPE);
        
		if(lov == null || lov.length == 0){
//			MessageBox.post("���� Ÿ�� ���� Preference ���� �����ϴ�. SYMC_FOLDER_TYPE_CHECK�� ��� �Ͻʽÿ�.(value(Folder Type) <Array>)", "�˸�", MessageBox.INFORMATION);
			MessageBox.post(registry.getString("DatasetDialog.Message.FolderTypeCheckPreference"), registry.getString("DatasetDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			
			return null;
		}
		
		return lov;
	}
	
	/**
	 * �ֿ� ���� ���� Role �� ���� ��ȯ.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2012. 12. 13.
	 * @return
	 */
	public static String[] FOLDERCREATEROLECHECK(){
		TCSession session = CustomUtil.getTCSession();
        TCPreferenceService preferenceService = session.getPreferenceService();
        
        // TODO �α��� ����ڿ� ���� ������ ����� �̸��� �ٸ� ��� ���, �ƴϸ� ������ ��.
//      TCComponentFolder homeFolder = session.getUser().getHomeFolder();
//
//      AbstractAIFUIApplication application = AIFDesktop.getActiveDesktop().getCurrentApplication();
//      AIFComponentContext comps[] = application.getTargetContexts();
//      if(comps != null)
//      {
//      	TCComponentFolder folderComp = ( TCComponentFolder )comps[0].getComponent();
//      	System.out.println(" ------- result = " + folderComp.getProperty("owning_user"));
//      }
        
        //String[] lov = preferenceService.getStringArray(TCPreferenceService.TC_preference_all, SYMC_FOLDER_ROLE);
        String[] lov = preferenceService.getStringValues(SYMC_FOLDER_ROLE);
        
		if(lov == null || lov.length == 0){
//			MessageBox.post("���� Ÿ�� ���� Preference ���� �����ϴ�. SYMC_FOLDER_ROLE_CHECK�� ��� �Ͻʽÿ�.(value(Role Name) <Array>)", "�˸�", MessageBox.INFORMATION);
			MessageBox.post(registry.getString("DatasetDialog.Message.FolderRoleCheckPreference"), registry.getString("DatasetDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return null;
		}
		
		return lov;
	}
	
	/**
	 * �ֿ� Interface DataBase Was WebServer Name.
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2013. 1. 10.
	 * @return
	 */
	public static String WASSERVERWEBNAME() {
//		ssangyongweb
		TCSession session = CustomUtil.getTCSession();
        TCPreferenceService preferenceService = session.getPreferenceService();
        
        //String lov = preferenceService.getString(TCPreferenceService.TC_preference_all, SYMC_WEBJDBC_URL);
        String lov = preferenceService.getStringValue(SYMC_WEBJDBC_URL);
		if(lov == null){
//			MessageBox.post("���� Ÿ�� ���� Preference ���� �����ϴ�. SYMC_WASSERVER_NAME�� ��� �Ͻʽÿ�.(value(WAS Service Name))", "�˸�", MessageBox.INFORMATION);
			MessageBox.post(registry.getString("DatasetDialog.Message.WasServerPreference"), registry.getString("DatasetDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return null;
		}
		
		return lov;
	}
	
	/**
	 * �ֿ� Create Folder Type.
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2013. 1. 10.
	 * @return
	 */
	public static String[] CREATEFOLDERTYPE() {
		TCSession session = CustomUtil.getTCSession();
        TCPreferenceService preferenceService = session.getPreferenceService();
        
        //String[] lov = preferenceService.getStringArray(TCPreferenceService.TC_preference_all, SYMC_CREATEFOLDERTYPE);
        String[] lov = preferenceService.getStringValues(SYMC_CREATEFOLDERTYPE);
		if(lov == null){
//			MessageBox.post("���� Ÿ�� ���� Preference ���� �����ϴ�. SYMC_CREATE_FOLDER_TYPE ��� �Ͻʽÿ�.(key(ǥ�� �̸�);value(���� Folder Type))", "�˸�", MessageBox.INFORMATION);
			MessageBox.post(registry.getString("DatasetDialog.Message.CreateFolderPreference"), registry.getString("DatasetDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return null;
		}
		
		return lov;
	}
	
	/**
	 * Login User ���� ��� ���� ���� ���� üũ �޼ҵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 12. 12.
	 * @return
	 */
	@SuppressWarnings("unused")
    public static boolean userACLCheck() {
		try {
			TCSession session = CustomUtil.getTCSession();
			TCComponentGroup[] groups = session.getUser().getGroups();
			boolean flag = false;
			int groupsSize = groups.length;
			
			String[] roleValues = SYMCClass.FOLDERCREATEROLECHECK();
			int roleValuesSize = roleValues.length;
			
			if(roleValues == null){
				return false;
			}
			
			for(int z=0; z<roleValuesSize; z++){
				for(int j=0; j<groupsSize; j++){
					TCComponentRole[] roles = session.getUser().getRoles(groups[j]);
					
					if(roles == null){
						break;
					}
					
					int rolesSize = roles.length;
					
					for(int i=0; i<rolesSize; i++){
						TCComponentRole role = roles[i];		// Login ����ڿ��� �ο��� role : Designer, DBA
						String name = role.getProperty("object_name");
						
						if(name.equals(roleValues[z].toString())){
							flag = true;
							break;
						} else {
							flag = false;
							break;
						}
					} // for end
					
					if(flag){
						break;
					}
				} // for end
				
				if(flag){
					break;
				}
			} // for end
			return flag;
		} catch (TCException e) {
			e.printStackTrace();
		}
		return true;
	}
}
