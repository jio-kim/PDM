package com.symc.plm.rac.prebom.prebom.operation.precreate;

import java.util.HashMap;

import com.kgm.commands.ec.search.FileAttachmentComposite;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;

/**
 * Part ���� Operation
 * VehPart/StdPart/Material/Product/Variant/Function/FunctionMaster/Project
 * 
 */
public class PrePartMasterOperation extends PreAbstractCreateOperation
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
	public PrePartMasterOperation(AbstractSWTDialog dialog, String strItemType, HashMap<String, Object> pMap, HashMap<String, Object> attrMap, FileAttachmentComposite fileComposite)
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
    @Override
	public void createItem() throws Exception
	{
		strPartNo = ((String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMID)).toUpperCase();
		String strPartRev = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMREVID);
		strPartName = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMNAME);

		// �ű� Item ����
		newComp = CustomUtil.createItem(this.strItemType, strPartNo, strPartRev, strPartName, "");
		String strUOM = (String) attrMap.get(PropertyConstant.ATTR_NAME_UOMTAG);
		attrMap.remove(PropertyConstant.ATTR_NAME_UOMTAG);

		// Unit�� Item �Ӽ�
		if (!CustomUtil.isEmpty(strUOM))
		{
			newComp.setProperty(PropertyConstant.ATTR_NAME_UOMTAG, strUOM);
		}
		// Unit ���� ���� ��� Default EA
		else
		{
			newComp.setProperty(PropertyConstant.ATTR_NAME_UOMTAG, new String("EA"));
		}

		// ���� Part�� ��� ���� ������ ���� ip_classification �� ����
		if (this.strItemType.equals(TypeConstant.S7_PREPRODUCTTYPE) || this.strItemType.equals(TypeConstant.S7_PREFUNCTIONTYPE))
		{
			newComp.setProperty(PropertyConstant.ATTR_NAME_CLASSIFICATION, "top-secret");
			newComp.getLatestItemRevision().setProperty(PropertyConstant.ATTR_NAME_CLASSIFICATION, "top-secret");
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

		if (newComp.isValidPropertyName(PropertyConstant.ATTR_NAME_DISPLAYPARTNO) && attrMap.containsKey(PropertyConstant.ATTR_NAME_DISPLAYPARTNO))
		    newComp.setProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, attrMap.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO).toString());

		// ItemID�� ���� ����� �ƴ�
		attrMap.remove(PropertyConstant.ATTR_NAME_ITEMID);
		// DR Check Flag�� �Ӽ��� �ƴ�
		attrMap.remove("DR_CHECK_FLAG");
		// ItemType �� ����� �ƴ�.
		attrMap.remove(PropertyConstant.ATTR_NAME_ITEMTYPE);

		TCComponentItemRevision ccnRevision = (TCComponentItemRevision) attrMap.get(PropertyConstant.ATTR_NAME_CCNNO);
		if (ccnRevision != null)
		{
            // CCN�� �����ϱ�
		    ccnRevision.add(TypeConstant.CCN_SOLUTION_ITEM, itemRev);
		}

		// s7_ACT_WEIGHT/s7_BOUNDINGBOX �Ӽ��� ���� Object�� ����( Release �� ���� �����ؾ� ��)
		if (this.strItemType.equals(TypeConstant.S7_PREVEHICLEPARTTYPE))
		{
		 // [SR140324-030][20140620] KOG DEV Veh. Part ��SES Spec No. Property Setting �߰�. (Typed Reference������ ��� �ʿ�)
			TCComponent refComp = SYMTcUtil.createApplicationObject(itemRev.getSession(), TypeConstant.S7_PREVEHTYPEDREFERENCE, null, null);

//			attrMap.put(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, refComp);
			itemRev.setReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, refComp);
			itemRev.lock();
			itemRev.save();
			itemRev.unlock();

			attrMap.remove("s7_ACT_WEIGHT");
			attrMap.remove("s7_BOUNDINGBOX");
			attrMap.remove("s7_SES_SPEC_NO");
			
			// [SR����][20141006][jclee] Save AS �� �ű� Item ���� �� ALC Code ���� ���� ���� 
			attrMap.put("m7_PG_ID", "");
			attrMap.put("m7_PG_ID_VERSION", "");
		}
		
		// ��� Revision�� Maturity �Ӽ� �ʱⰪ Setting
		attrMap.put(PropertyConstant.ATTR_NAME_MATURITY, "In Work");

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
		itemRev.refresh();
		itemRev.setTCProperties(props);
		itemRev.refresh();

		// File Composite�� �����ϴ� ��� Dataset ����
		if (fileComposite != null && fileComposite.isFileModified())
		{
			fileComposite.createDatasetAndMakerelation(itemRev);
		}
	}
}
