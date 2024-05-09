package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * [SR140828-010][20140828] jclee Function Master Part Validation Check ����. Part No. �ڸ��� ���� ����11�ڸ����� 7 ~ 11�ڸ�
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

        // [SR140702-058][20140630] KOG Product Part Validate �߰�.
        String strGModelCode = attrMap.get("s7_GMODEL_CODE").toString();
        String strFuncType = attrMap.get("s7_FUNCTION_TYPE").toString();

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Func Master No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Func Master No." }) + "\n");
        }
        // [SR140828-010][20140828] jclee Function Master Part Validation Check ����. Part No. �ڸ��� ���� ����11�ڸ����� 7 ~ 11�ڸ�
//        else if (strItemID.length() != 11) {
//            bufMessage.append("Part No.�� 11�ڸ��� �Է��ϼž� �մϴ�." + "\n");
//        } 
        else if (strItemID.length() > 11 || strItemID.length() < 7) {
            bufMessage.append("Part No.�� 7 ~ 11�ڸ��� �Է��ϼž� �մϴ�." + "\n");
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
        // [SR140702-058][20140630] KOG Product Part Product Type Validate �߰�.
        if (strFuncType.getBytes().length > 15) {
            bufMessage.append("Product Code �� 15�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }

        return bufMessage.toString();
    }

}
