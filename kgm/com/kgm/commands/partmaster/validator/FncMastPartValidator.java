package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * [SR140828-010][20140828] jclee Function Master Part Validation Check 변경. Part No. 자릿수 변경 기존11자리에서 7 ~ 11자리
 * Function Master Part Validation Check Class
 */
public class FncMastPartValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public FncMastPartValidator() {
        this(null);
    }

    public FncMastPartValidator(String[][] szLovNames) {
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
            // 'Func Master No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Func Master No." }) + "\n");
        }
        // [SR140828-010][20140828] jclee Function Master Part Validation Check 변경. Part No. 자릿수 변경 기존11자리에서 7 ~ 11자리
//        else if (strItemID.length() != 11) {
//            bufMessage.append("Part No.는 11자리로 입력하셔야 합니다." + "\n");
//        } 
        else if (strItemID.length() > 11 || strItemID.length() < 7) {
            bufMessage.append("Part No.는 7 ~ 11자리로 입력하셔야 합니다." + "\n");
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
        // [SR140702-058][20140630] KOG Product Part Product Type Validate 추가.
        if (strFuncType.getBytes().length > 15) {
            bufMessage.append("Product Code 는 15자리 이하로 입력하셔야 합니다." + "\n");
        }

        return bufMessage.toString();
    }

}
