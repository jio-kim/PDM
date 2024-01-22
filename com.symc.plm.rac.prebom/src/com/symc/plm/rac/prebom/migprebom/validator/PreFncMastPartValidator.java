package com.symc.plm.rac.prebom.migprebom.validator;

import java.util.HashMap;

import com.ssangyong.commands.partmaster.validator.ValidatorAbs;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;

/**
 * PreFunction Part Validation Check Class
 * 
 */
public class PreFncMastPartValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public PreFncMastPartValidator() {
        this(null);
    }

    public PreFncMastPartValidator(String[][] szLovNames) {
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
        
        String strGModelCode = attrMap.get("s7_GMODEL_CODE").toString();
        String strFuncType = attrMap.get("s7_FUNCTION_TYPE").toString();
        
        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        else if (strItemID.length() > 11 || strItemID.length() < 6) {
            bufMessage.append("Part No.는 6 ~ 11자리로 입력하셔야 합니다." + "\n");
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
        
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code 는 5자리 이하로 입력하셔야 합니다." + "\n");
        }
        if (strFuncType.getBytes().length > 15) {
            bufMessage.append("Product Code 는 15자리 이하로 입력하셔야 합니다." + "\n");
        }
        
        return bufMessage.toString();
    }

}
