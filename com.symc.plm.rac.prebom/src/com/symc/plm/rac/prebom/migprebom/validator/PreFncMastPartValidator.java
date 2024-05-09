package com.symc.plm.rac.prebom.migprebom.validator;

import java.util.HashMap;

import com.kgm.commands.partmaster.validator.ValidatorAbs;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

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
            // 'Part No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        else if (strItemID.length() > 11 || strItemID.length() < 6) {
            bufMessage.append("Part No.�� 6 ~ 11�ڸ��� �Է��ϼž� �մϴ�." + "\n");
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
        
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code �� 5�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }
        if (strFuncType.getBytes().length > 15) {
            bufMessage.append("Product Code �� 15�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }
        
        return bufMessage.toString();
    }

}
