package com.kgm.commands.partmaster.validator;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Function Part Validation Check Class
 * 
 */
/** [SR190123-018] Function Type Text -> ComboBox 변경
 * 1. Function Master의 Function Type LOV 및 필수 입력으로 변경
 * 2. Power Train 설변일때 Supply Mode에 따라 Vehicle Function이 아니면 VC값의 연관 Vehicle Project Code 추가
 */
public class FncPartValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public FncPartValidator() {
        this(null);
    }

    public FncPartValidator(String[][] szLovNames) {
        super(szLovNames);
    }

    @Override
    public String validate(HashMap<String, Object> attrMap, int nType) throws Exception {
        StringBuffer bufMessage = new StringBuffer();
        bufMessage.append(super.validate(attrMap, nType));
        String strItemID = (String) attrMap.get("item_id");
        String strPartName = (String) attrMap.get("object_name");
        String strProjCode = (String) attrMap.get("s7_PROJECT_CODE");
        String strMaturity = (String) attrMap.get("s7_MATURITY");
        
        // [SR140702-058][20140630] KOG Product Part Validate 추가.
        String strGModelCode = attrMap.get("s7_GMODEL_CODE").toString();
        String strFuncType = attrMap.get("s7_FUNCTION_TYPE").toString();
        
        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Function Part Validation Check 변경. Part No. 자릿수 변경 기존10자리에서 6 ~ 10자리
        else if (strItemID.length() > 10 || strItemID.length() < 6) {
            bufMessage.append("Part No.는 6 ~ 10자리로 입력하셔야 합니다." + "\n");
        }

        if (CustomUtil.isEmpty(strPartName)) {
            // 'Part Name'은 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Name" }) + "\n");
        }
        if (CustomUtil.isEmpty(strProjCode)) {
            // 'Project Code'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Code" }) + "\n");
        }
        if (CustomUtil.isEmpty(strMaturity)) {
            // 'Maturity'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Maturity" }) + "\n");
        }
        
        if (CustomUtil.isEmpty(strFuncType)) {
            // 'strFuncType'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Function Type" }) + "\n");
        } else {
        	ArrayList<String> al = new ArrayList<String>();
        	al.add("Vehicle");
        	al.add("Engine");
        	al.add("Transmission");
        	al.add("AXLE");
        	if(!al.contains(strFuncType)){
        		bufMessage.append("Function Type 값(" +strFuncType+")이 유효하지 않습니다." + "\n");
        	}
        }
        
        // [SR140702-058][20140630] KOG Product Part G-Model Code Validate 추가.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code 는 5자리 이하로 입력하셔야 합니다." + "\n");
        }
//        if (strFuncType.getBytes().length > 15) {
//            //bufMessage.append("Product Code 는 15자리 이하로 입력하셔야 합니다." + "\n");
//        }
        
        return bufMessage.toString();
    }

}
