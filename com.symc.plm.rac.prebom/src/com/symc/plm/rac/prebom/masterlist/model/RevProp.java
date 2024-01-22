package com.symc.plm.rac.prebom.masterlist.model;

import com.symc.plm.rac.prebom.common.PropertyConstant;

/**
 * [20160907][ymjang] 컬럼명 오류 수정
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
public enum RevProp {
	PROJCODE(PropertyConstant.ATTR_NAME_PROJCODE), PRD_PROJCODE(PropertyConstant.ATTR_NAME_PRD_PROJ_CODE)
//	,BUDGETCODE(PropertyConstant.ATTR_NAME_BUDGETCODE)
	, OLD_PART_NO(PropertyConstant.ATTR_NAME_OLD_PART_NO)
	, DISPLAYPARTNO(PropertyConstant.ATTR_NAME_DISPLAYPARTNO), ITEMNAME(PropertyConstant.ATTR_NAME_ITEMNAME)
	, CONTENTS(PropertyConstant.ATTR_NAME_CONTENTS), BOX(PropertyConstant.ATTR_NAME_BOX)
	, CHANGE_DESCRIPTION(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION)
	, CHG_TYPE_NM(PropertyConstant.ATTR_NAME_CHG_TYPE_NM)
	, ESTWEIGHT(PropertyConstant.ATTR_NAME_ESTWEIGHT)
	, ACTWEIGHT(PropertyConstant.ATTR_NAME_ACTWEIGHT)
	, TARGET_WEIGHT(PropertyConstant.ATTR_NAME_TARGET_WEIGHT)
	, DR(PropertyConstant.ATTR_NAME_DR)
	, EST_COST_MATERIAL(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL)
	, TARGET_COST_MATERIAL(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL)
	, SELECTED_COMPANY(PropertyConstant.ATTR_NAME_SELECTED_COMPANY)
	, CON_DWG_PLAN(PropertyConstant.ATTR_NAME_CON_DWG_PLAN)
	, CON_DWG_PERFORMANCE(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE)
	, CON_DWG_TYPE(PropertyConstant.ATTR_NAME_CON_DWG_TYPE)
	, PRD_DWG_PLAN(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN)
	, PRD_DWG_PERFORMANCE(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE)
	, ECO_NO(PropertyConstant.ATTR_NAME_ECO_NO)
	
	/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동 */
//	, DVP_NEEDED_QTY(PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY)
//	, DVP_USE(PropertyConstant.ATTR_NAME_DVP_USE)
//	, DVP_REQ_DEPT(PropertyConstant.ATTR_NAME_DVP_REQ_DEPT)
	
	/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
//	, ENG_DEPT_NM(PropertyConstant.ATTR_NAME_ENG_DEPT_NM)
//	, ENG_RESPONSIBLITY(PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY)
	
	, PRT_TOOLG_INVESTMENT(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT)
	, PRD_TOOL_COST(PropertyConstant.ATTR_NAME_PRD_TOOL_COST)
	, PRD_SERVICE_COST(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST)
	, PRD_SAMPLE_COST(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST)
	, TOTAL(PropertyConstant.ATTR_NAME_TOTAL)
	// [20160907][ymjang] 컬럼명 오류 수정
	, PUR_TEAM(PropertyConstant.ATTR_NAME_PUR_DEPT_NM)
	, PUR_RESPONSIBILITY(PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY)
	// [20160907][ymjang] 컬럼명 오류 수정
//	, EMPLOYEE_NO(PropertyConstant.ATTR_NAME_EMPLOYEE_NO)
	, DWG_DEPLOYABLE_DATE(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE);
	
	private String propName = null;
	private RevProp(String propName){
		this.propName = propName;
	}
	
	public static String[] getPropNames(){
		RevProp[] revProps = RevProp.values();
		String[] propNames = new String[revProps.length];
		for( int i = 0; i < revProps.length; i++){
			RevProp revProp = revProps[i];
			propNames[i] = revProp.propName;
		}
		
		return propNames;
	}

	public String getPropName() {
		return propName;
	}
	
}
