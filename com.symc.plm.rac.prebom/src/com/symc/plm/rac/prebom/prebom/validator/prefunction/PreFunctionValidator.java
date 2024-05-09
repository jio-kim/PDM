package com.symc.plm.rac.prebom.prebom.validator.prefunction;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
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
        
        // [SR140702-058][20140630] KOG Product Part Validate �߰�.
        String strGModelCode = attrMap.get(PropertyConstant.ATTR_NAME_GMODELCODE).toString();
        String strFuncType = attrMap.get(PropertyConstant.ATTR_NAME_FUNCTIONTYPE).toString();
        
        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Function Part Validation Check ����. Part No. �ڸ��� ���� ����10�ڸ����� 6 ~ 10�ڸ�
        else if (strItemID.length() > 10 || strItemID.length() < 6) {
            bufMessage.append("Part No.�� 6 ~ 10�ڸ��� �Է��ϼž� �մϴ�." + "\n");
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
        if (strFuncType.getBytes().length > 15) {
            bufMessage.append("Product Type �� 15�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }
        
        return bufMessage.toString();
    }

}
