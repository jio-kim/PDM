package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Vehicle Part Validator
 * 
 * 1. 필수 속성값 Check
 * 2. Part ID 중복 Check
 * 
 */
public class VariantPartValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public VariantPartValidator() {
        this(null);
    }

    public VariantPartValidator(String[][] szLovNames) {
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

        // [SR140702-058][20140630] KOG Variant Part Validate 추가.
        String strGModelCode = attrMap.get("s7_GMODEL_CODE").toString();
        String strVariantType = attrMap.get("s7_VARIANT_TYPE").toString();
        String strEngineNo = attrMap.get("s7_ENGINE_NO").toString();
        String strLoc = attrMap.get("s7_LOCATION").toString();
        String strBodyType = attrMap.get("s7_BODY_TYPE").toString();
        String strSeater = attrMap.get("s7_SEATER").toString();
        String strTrimLevel = attrMap.get("s7_TRIM_LEVEL").toString();

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Variant Part Validation Check 변경. Part No. 자릿수 변경 기존10자리에서 8 ~ 11자리
        else if (strItemID.length() > 11 || strItemID.length() < 8) {
            bufMessage.append("Part No.는 8 ~ 11 자리로 입력하셔야 합니다." + "\n");
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

        // [SR140702-058][20140630] KOG Variant Part G-Model Code Validate 추가.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code는 5자리 이하로 입력하셔야합니다.");
        }
        if (strVariantType.getBytes().length > 15) {
            bufMessage.append("Variant Type은 15자리 이하로 입력하셔야합니다.");
        }
        if (strEngineNo.getBytes().length > 7) {
            bufMessage.append("Engine No는 7자리 이하로 입력하셔야합니다.");
        }
        if (strLoc.getBytes().length > 10) {
            bufMessage.append("Location은 10자리 이하로 입력하셔야합니다.");
        }
        if (strBodyType.getBytes().length > 15) {
            bufMessage.append("Body Type은 15자리 이하로 입력하셔야합니다.");
        }
        if (strSeater.getBytes().length > 5) {
            bufMessage.append("Seater은 5자리 이하로 입력하셔야합니다.");
        }
        if (strTrimLevel.getBytes().length > 10) {
            bufMessage.append("Trim Level은 10자리 이하로 입력하셔야합니다.");
        }
        // END [20140630]
        return bufMessage.toString();
    }

}
