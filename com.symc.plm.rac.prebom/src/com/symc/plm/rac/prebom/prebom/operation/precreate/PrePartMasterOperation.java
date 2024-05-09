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
 * Part 생성 Operation
 * VehPart/StdPart/Material/Product/Variant/Function/FunctionMaster/Project
 * 
 */
public class PrePartMasterOperation extends PreAbstractCreateOperation
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
	public PrePartMasterOperation(AbstractSWTDialog dialog, String strItemType, HashMap<String, Object> pMap, HashMap<String, Object> attrMap, FileAttachmentComposite fileComposite)
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
    @Override
	public void createItem() throws Exception
	{
		strPartNo = ((String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMID)).toUpperCase();
		String strPartRev = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMREVID);
		strPartName = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMNAME);

		// 신규 Item 생성
		newComp = CustomUtil.createItem(this.strItemType, strPartNo, strPartRev, strPartName, "");
		String strUOM = (String) attrMap.get(PropertyConstant.ATTR_NAME_UOMTAG);
		attrMap.remove(PropertyConstant.ATTR_NAME_UOMTAG);

		// Unit은 Item 속성
		if (!CustomUtil.isEmpty(strUOM))
		{
			newComp.setProperty(PropertyConstant.ATTR_NAME_UOMTAG, strUOM);
		}
		// Unit 값이 없는 경우 Default EA
		else
		{
			newComp.setProperty(PropertyConstant.ATTR_NAME_UOMTAG, new String("EA"));
		}

		// 상위 Part인 경우 권한 관리를 위해 ip_classification 값 설정
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
	 * Item 생성 후 Revision에 속성값 저장
	 */
	@Override
	public void setProperties() throws Exception
	{
		TCComponentItemRevision itemRev = newComp.getLatestItemRevision();

		if (newComp.isValidPropertyName(PropertyConstant.ATTR_NAME_DISPLAYPARTNO) && attrMap.containsKey(PropertyConstant.ATTR_NAME_DISPLAYPARTNO))
		    newComp.setProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, attrMap.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO).toString());

		// ItemID는 수정 대상이 아님
		attrMap.remove(PropertyConstant.ATTR_NAME_ITEMID);
		// DR Check Flag는 속성이 아님
		attrMap.remove("DR_CHECK_FLAG");
		// ItemType 은 대상이 아님.
		attrMap.remove(PropertyConstant.ATTR_NAME_ITEMTYPE);

		TCComponentItemRevision ccnRevision = (TCComponentItemRevision) attrMap.get(PropertyConstant.ATTR_NAME_CCNNO);
		if (ccnRevision != null)
		{
            // CCN에 연결하기
		    ccnRevision.add(TypeConstant.CCN_SOLUTION_ITEM, itemRev);
		}

		// s7_ACT_WEIGHT/s7_BOUNDINGBOX 속성은 별도 Object로 관리( Release 후 수정 가능해야 함)
		if (this.strItemType.equals(TypeConstant.S7_PREVEHICLEPARTTYPE))
		{
		 // [SR140324-030][20140620] KOG DEV Veh. Part 에SES Spec No. Property Setting 추가. (Typed Reference에대한 고려 필요)
			TCComponent refComp = SYMTcUtil.createApplicationObject(itemRev.getSession(), TypeConstant.S7_PREVEHTYPEDREFERENCE, null, null);

//			attrMap.put(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, refComp);
			itemRev.setReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, refComp);
			itemRev.lock();
			itemRev.save();
			itemRev.unlock();

			attrMap.remove("s7_ACT_WEIGHT");
			attrMap.remove("s7_BOUNDINGBOX");
			attrMap.remove("s7_SES_SPEC_NO");
			
			// [SR없음][20141006][jclee] Save AS 및 신규 Item 생성 시 ALC Code 복사 현상 수정 
			attrMap.put("m7_PG_ID", "");
			attrMap.put("m7_PG_ID_VERSION", "");
		}
		
		// 모든 Revision의 Maturity 속성 초기값 Setting
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

		// 속성 일괄 반영
		itemRev.refresh();
		itemRev.setTCProperties(props);
		itemRev.refresh();

		// File Composite가 존재하는 경우 Dataset 연결
		if (fileComposite != null && fileComposite.isFileModified())
		{
			fileComposite.createDatasetAndMakerelation(itemRev);
		}
	}
}
