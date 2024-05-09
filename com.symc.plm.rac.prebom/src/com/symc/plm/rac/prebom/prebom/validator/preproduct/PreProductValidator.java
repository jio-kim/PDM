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

        // [SR140702-058][20140630] KOG Product Part Validate �߰�.
        String strGModelCode = attrMap.get(PropertyConstant.ATTR_NAME_GMODELCODE).toString();
//        String strProductType = attrMap.get("s7_PRODUCT_TYPE").toString();

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Product Part Validation Check ����. Part No. �ڸ��� ���� ����8�ڸ����� 7~8�ڸ�
        else if (strItemID.length() > 8 || strItemID.length() < 7) {
            bufMessage.append("Part No.�� 7 ~ 8�ڸ��� �Է��ϼž� �մϴ�." + "\n");
        }

        if (CustomUtil.isEmpty(strGateNo)) {
            // 'Part Name'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Gate No" }) + "\n");
        }
        if (CustomUtil.isEmpty(strPartName)) {
            // 'Part Name'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Name" }) + "\n");
        }
        if (CustomUtil.isEmpty(strProjCode)) {
            // 'Project Code'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Code" }) + "\n");
        }
        if (CustomUtil.isEmpty(strMaturity)) {
            // 'Maturity'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Maturity" }) + "\n");
        }
        if (CustomUtil.isEmpty(strProjectType)) {
            // 'Project Type'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Type" }) + "\n");
        }

        // [SR140702-058][20140630] KOG Product Part G-Model Code Validate �߰�.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code �� 5�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }
//        if (strProductType.getBytes().length > 15) {
//            bufMessage.append("Product Type �� 15�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
//        }

        if (strProjectType.equals("02"))
        {
            bufMessage.append("Project Type �� \"New Car\" Ȥ�� \"Model Year\" �� ���� �����մϴ�." + "\n");
        }

        return bufMessage.toString();
    }

}
