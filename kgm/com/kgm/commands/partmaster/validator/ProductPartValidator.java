package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

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

        // [SR140702-058][20140630] KOG Product Part Validate �߰�.
        String strGModelCode = attrMap.get("s7_GMODEL_CODE").toString();
        String strProductType = attrMap.get("s7_PRODUCT_TYPE").toString();

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Product Part Validation Check ����. Part No. �ڸ��� ���� ����8�ڸ����� 7~8�ڸ�
        else if (strItemID.length() > 8 || strItemID.length() < 7) {
            bufMessage.append("Part No.�� 7 ~ 8�ڸ��� �Է��ϼž� �մϴ�." + "\n");
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

        // [SR140702-058][20140630] KOG Product Part G-Model Code Validate �߰�.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code �� 5�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }
        if (strProductType.getBytes().length > 15) {
            bufMessage.append("Product Code �� 15�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }

        return bufMessage.toString();
    }

}
