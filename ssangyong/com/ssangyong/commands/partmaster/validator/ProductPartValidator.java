package com.ssangyong.commands.partmaster.validator;

import java.util.HashMap;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;

/**
 * Product Validation Check Class
 */
public class ProductPartValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public ProductPartValidator() {
        this(null);
    }

    public ProductPartValidator(String[][] szLovNames) {
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
        String strProductType = attrMap.get("s7_PRODUCT_TYPE").toString();

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Product Part Validation Check 변경. Part No. 자릿수 변경 기존8자리에서 7~8자리
        else if (strItemID.length() > 8 || strItemID.length() < 7) {
            bufMessage.append("Part No.는 7 ~ 8자리로 입력하셔야 합니다." + "\n");
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
        if (strProductType.getBytes().length > 15) {
            bufMessage.append("Product Code 는 15자리 이하로 입력하셔야 합니다." + "\n");
        }

        return bufMessage.toString();
    }

}
