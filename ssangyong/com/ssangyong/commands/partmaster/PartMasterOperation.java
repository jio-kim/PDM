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
 * Part ���� Operation
 * VehPart/StdPart/Material/Product/Variant/Function/FunctionMaster/Project
 * 
 */
public class PartMasterOperation extends SYMCAbstractCreateOperation
{

	/** Manage Dialog���� �Ѿ�� Param Map */
	HashMap<String, Object> pMap;
	/** �Ӽ� AttrMap */
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
	 * Part ���� Operation
	 * 
	 * @param dialog : Dialog Instance
	 * @param strItemType : Part Item Type
	 * @param pMap : Manage Dialog���� �Ѿ�� Param Map
	 * @param attrMap : �Ӽ� AttrMap
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
	 * Item ����
	 */
	@SuppressWarnings("unused")
    @Override
	public void createItem() throws Exception
	{
		strPartNo = ((String) attrMap.get(Constants.ATTR_NAME_ITEMID)).toUpperCase();
		String strPartRev = (String) attrMap.get("item_revision_id");
		strPartName = (String) attrMap.get(Constants.ATTR_NAME_ITEMNAME);

		// SaveAs(Different) �� ���
		if (Constants.ACTIONTYPE_DEFERENT.equals(this.pMap.get(Constants.ATTR_NAME_ACTIONTYPE)))
		{
			TCComponentItemRevision baseItemRev = (TCComponentItemRevision) pMap.get(Constants.ATTR_NAME_BASEITEMID);
			TCSession session = baseItemRev.getSession();

			// SaveAs ����� ���� Item ����
			newComp = baseItemRev.saveAsItem(strPartNo, strPartRev, strPartName, "", false, null);
			String strUOM = (String) attrMap.get("uom_tag");
			attrMap.remove("uom_tag");

			// Unit�� Item �Ӽ�
			if (!CustomUtil.isEmpty(strUOM))
				newComp.setProperty("uom_tag", strUOM);
			// Unit ���� ���� ��� Default EA
			else
			{
				newComp.setProperty("uom_tag", "EA");
			}
		}
		// �׿� �ű� Item ����
		else
		{
			newComp = CustomUtil.createItem(this.strItemType, strPartNo, strPartRev, strPartName, "");
			String strUOM = (String) attrMap.get("uom_tag");
			attrMap.remove("uom_tag");

			// Unit�� Item �Ӽ�
			if (!CustomUtil.isEmpty(strUOM))
				newComp.setProperty("uom_tag", strUOM);
			// Unit ���� ���� ��� Default EA
			else
			{
				newComp.setProperty("uom_tag", "EA");
			}

		}

		// ���� Part�� ��� ���� ������ ���� ip_classification �� ����
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
	 * Item ���� �� Revision�� �Ӽ��� ����
	 */
	@Override
	public void setProperties() throws Exception
	{

		TCComponentItemRevision itemRev = newComp.getLatestItemRevision();

		// ItemID�� ���� ����� �ƴ�
		attrMap.remove("item_id");
		// DR Check Flag�� �Ӽ��� �ƴ�
		attrMap.remove("DR_CHECK_FLAG");

		// s7_ACT_WEIGHT/s7_BOUNDINGBOX �Ӽ��� ���� Object�� ����( Release �� ���� �����ؾ� ��)
		if (this.strItemType.equals(SYMCClass.S7_VEHPARTTYPE))
		{
		 // [SR140324-030][20140620] KOG DEV Veh. Part ��SES Spec No. Property Setting �߰�. (Typed Reference������ ��� �ʿ�)
			// s7_TARGET_WEIGHT �Ӽ� �߰�
			TCComponent refComp = SYMTcUtil.createApplicationObject(itemRev.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX", "s7_SES_SPEC_NO" }, new String[] {
					(String) attrMap.get("s7_ACT_WEIGHT"), (String) attrMap.get("s7_TARGET_WEIGHT"), (String) attrMap.get("s7_BOUNDINGBOX"),  (String) attrMap.get("s7_SES_SPEC_NO")});

			itemRev.setReferenceProperty("s7_Vehpart_TypedReference", refComp);

			attrMap.remove("s7_ACT_WEIGHT");
			attrMap.remove("s7_TARGET_WEIGHT");
			attrMap.remove("s7_BOUNDINGBOX");
			attrMap.remove("s7_SES_SPEC_NO");
			
			// [SR����][20141006][jclee] Save AS �� �ű� Item ���� �� ALC Code ���� ���� ���� 
			attrMap.put("m7_PG_ID", "");
			attrMap.put("m7_PG_ID_VERSION", "");
		}
		else if (this.strItemType.equals(SYMCClass.S7_STDPARTTYPE))
        {
		    // [SR140324-030][20140620] KOG DEV Std. Part�� SES Spec No. Property Setting. (Typed Reference������ ��� �ʿ�)
            TCComponent refComp = SYMTcUtil.createApplicationObject(itemRev.getSession(), "S7_Stdpart_TypedReference", new String[] { "s7_SES_SPEC_NO" }, new String[] {
                    (String) attrMap.get("s7_SES_SPEC_NO")});
            itemRev.setReferenceProperty("s7_Stdpart_TypedReference", refComp);
            attrMap.remove("s7_SES_SPEC_NO");
			
			// [SR����][20141006][jclee] Save AS �� �ű� Item ���� �� ALC Code ���� ���� ���� 
			attrMap.put("m7_PG_ID", "");
			attrMap.put("m7_PG_ID_VERSION", "");
        }
		
		// ��� Revision�� Maturity �Ӽ� �ʱⰪ Setting
		attrMap.put("s7_MATURITY", "In Work");
		
		// [2018.11.20][CSH][SR181119-056] VehPart ���� �� Main / Sub Name�� LOV�� �ٸ���� �����߻� �� �� �α��� �������� Item ������ �ȵǴ� ���� ����, Validator���� �Ʒ� ���� ����
		// [2015.02.23][jclee]
		// Main Name, Sub Name�� ���� �����ϴ� ��� LOV�� ���� ��Ī�Ǵ��� Ȯ��
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

		// �Ӽ� �ϰ� �ݿ�
		itemRev.setTCProperties(props);
		itemRev.refresh();

		// SaveAS(Different) �ΰ�� BaseItem DataSet�� Copy ��
		if (Constants.ACTIONTYPE_DEFERENT.equals(this.pMap.get(Constants.ATTR_NAME_ACTIONTYPE)))
		{
			TCComponentItemRevision baseItemRev = (TCComponentItemRevision) pMap.get(Constants.ATTR_NAME_BASEITEMID);
			if (baseItemRev != null)
			{
				// Dataset Revision �°� ����
				//boolean isSucceeded = ((Boolean) this.pMap.get(Constants.ATTR_NAME_DATASETSUCCEED)).booleanValue();
				//[20240307][UPGRADE] Create Product ������ ���� ����
				Object isdatasetSucceedObj = pMap.get(Constants.ATTR_NAME_DATASETSUCCEED);
				if(isdatasetSucceedObj !=null)
				{
					// Dataset Revision �°� ����
					boolean isSucceeded = ((Boolean) isdatasetSucceedObj).booleanValue();
					// BaseItem DataSet�� Copy ��
					CustomUtil.relateDatasetToItemRevision(baseItemRev, itemRev, true, true, true, null, isSucceeded);
				}	
				// Save As(Different)�� ��� Original File Name ����				
				if (Constants.ACTIONTYPE_DEFERENT.equals(this.pMap.get(Constants.ATTR_NAME_ACTIONTYPE))) {
				    CustomUtil.renameDatasetReferenceFile(itemRev);
				}
			}
		}

		// File Composite�� �����ϴ� ��� Dataset ����
		if (fileComposite != null && fileComposite.isFileModified())
		{
			fileComposite.createDatasetAndMakerelation(itemRev);
		}
	}
}
