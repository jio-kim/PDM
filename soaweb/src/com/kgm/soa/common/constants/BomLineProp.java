package com.kgm.soa.common.constants;


public enum BomLineProp {
		
	  SYSTEM_ROW_KEY(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY)
	, SEQUENCE_NO(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO)
	, ALTER_PART(PropertyConstant.ATTR_NAME_BL_ALTER_PART)
	, SPEC_DESC(PropertyConstant.ATTR_NAME_BL_SPEC_DESC)
	, MODULE_CODE(PropertyConstant.ATTR_NAME_BL_MODULE_CODE)
	, SUPPLY_MODE(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE)
	, REQ_OPT(PropertyConstant.ATTR_NAME_BL_REQ_OPT)
	, CHG_CD(PropertyConstant.ATTR_NAME_BL_CHG_CD)
	, QUANTITY(PropertyConstant.ATTR_NAME_BL_QUANTITY)
	, CONDITION(PropertyConstant.ATTR_NAME_BL_CONDITION)
	, OCC_THREAD(PropertyConstant.ATTR_NAME_BL_OCC_THREAD)
	, OCC_UID(PropertyConstant.ATTR_NAME_BL_OCC_UID)
	, LEV_M(PropertyConstant.ATTR_NAME_BL_LEV_M)
	, DVP_NEEDED_QTY(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY)
	, DVP_USE(PropertyConstant.ATTR_NAME_BL_DVP_USE)
	, DVP_REQ_DEPT(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT)
	, ENG_DEPT_NM(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM)
	, ENG_RESPONSIBLITY(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY)
	, ITEMID(PropertyConstant.ATTR_NAME_ITEMID)
	, ITEM_REVISION(PropertyConstant.ATTR_NAME_BL_ITEM_REVISION)
	, CHILD_LINES(PropertyConstant.ATTR_NAME_BL_CHILD_LINES)
    , PARENT(PropertyConstant.ATTR_NAME_BL_PARENT)
    , IS_PACKED(PropertyConstant.ATTR_NAME_BL_IS_PACKED)
    , PACKED_LINES(PropertyConstant.ATTR_NAME_BL_PACKED_LINES);
	  
	private String propName = null;
	private BomLineProp(String propName){
		this.propName = propName;
	}
	
	public static String[] getPropNames(){
		BomLineProp[] lineProps = BomLineProp.values();
		String[] propNames = new String[lineProps.length];
		for( int i = 0; i < lineProps.length; i++){
			BomLineProp lineProp = lineProps[i];
			propNames[i] = lineProp.propName;
		}
		
		return propNames;
	}

	public String getPropName() {
		return propName;
	}	
}
