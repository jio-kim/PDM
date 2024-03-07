package com.ssangyong.commands.partmaster;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.commands.ec.search.FileAttachmentComposite;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.operation.SYMCAbstractCreateOperation;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.soa.client.model.LovValue;

/**
 * Part 생성 Operation
 * VehPart/StdPart/Material/Product/Variant/Function/FunctionMaster/Project
 * 
 */
public class PartMasterOperation extends SYMCAbstractCreateOperation
{

	/** Manage Dialog에서 넘어온 Param Map */
	HashMap<String, Object> pMap;
	/** 속성 AttrMap */
	HashMap<String, Object> attrMap;
	/** Part No. */
	String strPartNo;
	/** Part Name */
	String strPartName;

	/** Part Item Type */
	String strItemType;

	/** File Composite */
	FileAttachmentComposite fileComposite;

	/**
	 * Part 생성 Operation
	 * 
	 * @param dialog : Dialog Instance
	 * @param strItemType : Part Item Type
	 * @param pMap : Manage Dialog에서 넘어온 Param Map
	 * @param attrMap : 속성 AttrMap
	 * @param fileComposite : File Composite
	 */
	public PartMasterOperation(AbstractSWTDialog dialog, String strItemType, HashMap<String, Object> pMap, HashMap<String, Object> attrMap, FileAttachmentComposite fileComposite)
	{
		super(dialog);
		this.pMap = pMap;
		this.attrMap = attrMap;
		this.strItemType = strItemType;
		this.fileComposite = fileComposite;
	}

	/**
	 * Item 생성
	 */
	@SuppressWarnings("unused")
    @Override
	public void createItem() throws Exception
	{
		strPartNo = ((String) attrMap.get(Constants.ATTR_NAME_ITEMID)).toUpperCase();
		String strPartRev = (String) attrMap.get("item_revision_id");
		strPartName = (String) attrMap.get(Constants.ATTR_NAME_ITEMNAME);

		// SaveAs(Different) 인 경우
		if (Constants.ACTIONTYPE_DEFERENT.equals(this.pMap.get(Constants.ATTR_NAME_ACTIONTYPE)))
		{
			TCComponentItemRevision baseItemRev = (TCComponentItemRevision) pMap.get(Constants.ATTR_NAME_BASEITEMID);
			TCSession session = baseItemRev.getSession();

			// SaveAs 기능을 통해 Item 생성
			newComp = baseItemRev.saveAsItem(strPartNo, strPartRev, strPartName, "", false, null);
			String strUOM = (String) attrMap.get("uom_tag");
			attrMap.remove("uom_tag");

			// Unit은 Item 속성
			if (!CustomUtil.isEmpty(strUOM))
				newComp.setProperty("uom_tag", strUOM);
			// Unit 값이 없는 경우 Default EA
			else
			{
				newComp.setProperty("uom_tag", "EA");
			}
		}
		// 그외 신규 Item 생성
		else
		{
			newComp = CustomUtil.createItem(this.strItemType, strPartNo, strPartRev, strPartName, "");
			String strUOM = (String) attrMap.get("uom_tag");
			attrMap.remove("uom_tag");

			// Unit은 Item 속성
			if (!CustomUtil.isEmpty(strUOM))
				newComp.setProperty("uom_tag", strUOM);
			// Unit 값이 없는 경우 Default EA
			else
			{
				newComp.setProperty("uom_tag", "EA");
			}

		}

		// 상위 Part인 경우 권한 관리를 위해 ip_classification 값 설정
		if (this.strItemType.equals(SYMCClass.S7_PRODUCTPARTTYPE) || this.strItemType.equals(SYMCClass.S7_VARIANTPARTTYPE) || this.strItemType.equals(SYMCClass.S7_FNCPARTTYPE))
		{
			newComp.setProperty("ip_classification", "top-secret");
			newComp.getLatestItemRevision().setProperty("ip_classification", "top-secret");
		}

	}

	@Override
	public void startOperation() throws Exception
	{

	}

	@Override
	public void endOperation() throws Exception
	{
		//    NavigatorOpenService openService = new NavigatorOpenService();
		//    openService.open(newComp);
	}

	/**
	 * Item 생성 후 Revision에 속성값 저장
	 */
	@Override
	public void setProperties() throws Exception
	{

		TCComponentItemRevision itemRev = newComp.getLatestItemRevision();

		// ItemID는 수정 대상이 아님
		attrMap.remove("item_id");
		// DR Check Flag는 속성이 아님
		attrMap.remove("DR_CHECK_FLAG");

		// s7_ACT_WEIGHT/s7_BOUNDINGBOX 속성은 별도 Object로 관리( Release 후 수정 가능해야 함)
		if (this.strItemType.equals(SYMCClass.S7_VEHPARTTYPE))
		{
		 // [SR140324-030][20140620] KOG DEV Veh. Part 에SES Spec No. Property Setting 추가. (Typed Reference에대한 고려 필요)
			// s7_TARGET_WEIGHT 속성 추가
			TCComponent refComp = SYMTcUtil.createApplicationObject(itemRev.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX", "s7_SES_SPEC_NO" }, new String[] {
					(String) attrMap.get("s7_ACT_WEIGHT"), (String) attrMap.get("s7_TARGET_WEIGHT"), (String) attrMap.get("s7_BOUNDINGBOX"),  (String) attrMap.get("s7_SES_SPEC_NO")});

			itemRev.setReferenceProperty("s7_Vehpart_TypedReference", refComp);

			attrMap.remove("s7_ACT_WEIGHT");
			attrMap.remove("s7_TARGET_WEIGHT");
			attrMap.remove("s7_BOUNDINGBOX");
			attrMap.remove("s7_SES_SPEC_NO");
			
			// [SR없음][20141006][jclee] Save AS 및 신규 Item 생성 시 ALC Code 복사 현상 수정 
			attrMap.put("m7_PG_ID", "");
			attrMap.put("m7_PG_ID_VERSION", "");
		}
		else if (this.strItemType.equals(SYMCClass.S7_STDPARTTYPE))
        {
		    // [SR140324-030][20140620] KOG DEV Std. Part에 SES Spec No. Property Setting. (Typed Reference에대한 고려 필요)
            TCComponent refComp = SYMTcUtil.createApplicationObject(itemRev.getSession(), "S7_Stdpart_TypedReference", new String[] { "s7_SES_SPEC_NO" }, new String[] {
                    (String) attrMap.get("s7_SES_SPEC_NO")});
            itemRev.setReferenceProperty("s7_Stdpart_TypedReference", refComp);
            attrMap.remove("s7_SES_SPEC_NO");
			
			// [SR없음][20141006][jclee] Save AS 및 신규 Item 생성 시 ALC Code 복사 현상 수정 
			attrMap.put("m7_PG_ID", "");
			attrMap.put("m7_PG_ID_VERSION", "");
        }
		
		// 모든 Revision의 Maturity 속성 초기값 Setting
		attrMap.put("s7_MATURITY", "In Work");
		
		// [2018.11.20][CSH][SR181119-056] VehPart 생성 시 Main / Sub Name이 LOV와 다른경우 오류발생 후 재 로그인 전까지는 Item 생성이 안되는 현상 보완, Validator에서 아래 로직 수행
		// [2015.02.23][jclee]
		// Main Name, Sub Name에 값이 존재하는 경우 LOV의 값과 매칭되는지 확인
//		Object oMainName = attrMap.get("s7_MAIN_NAME");
//		Object oSubName = attrMap.get("s7_SUB_NAME");
//		
//		String sMainName = oMainName == null ? "" : oMainName.toString();
//		String sSubName = oSubName == null ? "" : oSubName.toString();
//		
//		boolean isContainMainName = false;
//		boolean isContainSubName = false;
//		
//		if (!(sMainName == null || sMainName.equals("") || sMainName.length() == 0)) {
//			TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
//	        TCComponentListOfValues[] listofvalues = listofvaluestype.find("S7_MAIN_NAME");
//	        TCComponentListOfValues listofvalue = listofvalues[0];
//	        List<LovValue> lstMainNames = listofvalue.getListOfValues().getValues();
//			
//			for (int inx = 0; inx < lstMainNames.size(); inx++) {
//				System.out.println(listofvalue.getListOfValues().getValues().get(inx).getValue());
//				if (listofvalue.getListOfValues().getValues().get(inx).getValue().equals(sMainName)) {
//					isContainMainName = true;
//					break;
//				}
//			}
//			
//			if (!isContainMainName) {
//				throw new Exception(sMainName + " not contianed in Keys of LOV. Select a part name again.");
//			}
//		}
//		
//		if (!(sSubName == null || sSubName.equals("") || sSubName.length() == 0)) {
//			TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
//	        TCComponentListOfValues[] listofvalues = listofvaluestype.find("S7_SUBNAME");
//	        TCComponentListOfValues listofvalue = listofvalues[0];
//	        List<LovValue> lstSubNames = listofvalue.getListOfValues().getValues();
//			
//			for (int inx = 0; inx < lstSubNames.size(); inx++) {
//				System.out.println(listofvalue.getListOfValues().getValues().get(inx).getValue());
//				if (listofvalue.getListOfValues().getValues().get(inx).getValue().equals(sSubName)) {
//					isContainSubName = true;
//					break;
//				}
//			}
//			
//			if (!isContainSubName) {
//				throw new Exception(sSubName + " not contianed in Keys of LOV. Select a part name again.");
//			}
//		}
		
		String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
		TCProperty[] props = itemRev.getTCProperties(szKey);

		for (int i = 0; i < props.length; i++)
		{

			if (props[i] == null)
			{
				System.out.println(szKey[i] + " is Null");
				continue;
			}

			Object value = attrMap.get(props[i].getPropertyName());

			CustomUtil.setObjectToPropertyValue(props[i], value);
		}

		// 속성 일괄 반영
		itemRev.setTCProperties(props);
		itemRev.refresh();

		// SaveAS(Different) 인경우 BaseItem DataSet를 Copy 함
		if (Constants.ACTIONTYPE_DEFERENT.equals(this.pMap.get(Constants.ATTR_NAME_ACTIONTYPE)))
		{
			TCComponentItemRevision baseItemRev = (TCComponentItemRevision) pMap.get(Constants.ATTR_NAME_BASEITEMID);
			if (baseItemRev != null)
			{
				// Dataset Revision 승계 여부
				//boolean isSucceeded = ((Boolean) this.pMap.get(Constants.ATTR_NAME_DATASETSUCCEED)).booleanValue();
				//[20240307][UPGRADE] Create Product 생성시 오류 수정
				Object isdatasetSucceedObj = pMap.get(Constants.ATTR_NAME_DATASETSUCCEED);
				if(isdatasetSucceedObj !=null)
				{
					// Dataset Revision 승계 여부
					boolean isSucceeded = ((Boolean) isdatasetSucceedObj).booleanValue();
					// BaseItem DataSet를 Copy 함
					CustomUtil.relateDatasetToItemRevision(baseItemRev, itemRev, true, true, true, null, isSucceeded);
				}	
				// Save As(Different)인 경우 Original File Name 변경				
				if (Constants.ACTIONTYPE_DEFERENT.equals(this.pMap.get(Constants.ATTR_NAME_ACTIONTYPE))) {
				    CustomUtil.renameDatasetReferenceFile(itemRev);
				}
			}
		}

		// File Composite가 존재하는 경우 Dataset 연결
		if (fileComposite != null && fileComposite.isFileModified())
		{
			fileComposite.createDatasetAndMakerelation(itemRev);
		}
	}
}
