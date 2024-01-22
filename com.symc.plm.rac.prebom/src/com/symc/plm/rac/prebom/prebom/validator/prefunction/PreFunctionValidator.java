package com.symc.plm.rac.prebom.prebom.validator.prefunction;

import java.util.HashMap;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;

/**
 * Function Part Validation Check Class
 * 
 */
public class PreFunctionValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public PreFunctionValidator() {
        this(null);
    }

    public PreFunctionValidator(String[][] szLovNames) {
        super(szLovNames);
    }

    @Override
    public String validate(HashMap<String, Object> attrMap, int nType) throws Exception {
        StringBuffer bufMessage = new StringBuffer();
        bufMessage.append(super.validate(attrMap, nType));
        String strItemID = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMID);
        String strPartName = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMNAME);
        String strProjCode = (String) attrMap.get(PropertyConstant.ATTR_NAME_PROJCODE);
        String strMaturity = (String) attrMap.get(PropertyConstant.ATTR_NAME_MATURITY);
        
        // [SR140702-058][20140630] KOG Product Part Validate 추가.
        String strGModelCode = attrMap.get(PropertyConstant.ATTR_NAME_GMODELCODE).toString();
        String strFuncType = attrMap.get(PropertyConstant.ATTR_NAME_FUNCTIONTYPE).toString();
        
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
        
        // [SR140702-058][20140630] KOG Product Part G-Model Code Validate 추가.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code 는 5자리 이하로 입력하셔야 합니다." + "\n");
        }
        if (strFuncType.getBytes().length > 15) {
            bufMessage.append("Product Type 는 15자리 이하로 입력하셔야 합니다." + "\n");
        }
        
        return bufMessage.toString();
    }

}
