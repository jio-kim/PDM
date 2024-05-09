package com.symc.plm.rac.prebom.prebom.validator.preproduct;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;

/**
 * Product Validation Check Class
 */
public class PreProductValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public PreProductValidator() {
        this(null);
    }

    public PreProductValidator(String[][] szLovNames) {
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
        String strProjectType = (String) attrMap.get(PropertyConstant.ATTR_NAME_PROJECTTYPE);
        String strGateNo = (String) attrMap.get(PropertyConstant.ATTR_NAME_GATENO);

        // [SR140702-058][20140630] KOG Product Part Validate 추가.
        String strGModelCode = attrMap.get(PropertyConstant.ATTR_NAME_GMODELCODE).toString();
//        String strProductType = attrMap.get("s7_PRODUCT_TYPE").toString();

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Product Part Validation Check 변경. Part No. 자릿수 변경 기존8자리에서 7~8자리
        else if (strItemID.length() > 8 || strItemID.length() < 7) {
            bufMessage.append("Part No.는 7 ~ 8자리로 입력하셔야 합니다." + "\n");
        }

        if (CustomUtil.isEmpty(strGateNo)) {
            // 'Part Name'은 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Gate No" }) + "\n");
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
        if (CustomUtil.isEmpty(strProjectType)) {
            // 'Project Type'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Type" }) + "\n");
        }

        // [SR140702-058][20140630] KOG Product Part G-Model Code Validate 추가.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code 는 5자리 이하로 입력하셔야 합니다." + "\n");
        }
//        if (strProductType.getBytes().length > 15) {
//            bufMessage.append("Product Type 는 15자리 이하로 입력하셔야 합니다." + "\n");
//        }

        if (strProjectType.equals("02"))
        {
            bufMessage.append("Project Type 은 \"New Car\" 혹은 \"Model Year\" 만 선택 가능합니다." + "\n");
        }

        return bufMessage.toString();
    }

}
