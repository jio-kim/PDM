package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Vehicle Part Validator
 * 
 * 1. �ʼ� �Ӽ��� Check
 * 2. Part ID �ߺ� Check
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

        // [SR140702-058][20140630] KOG Variant Part Validate �߰�.
        String strGModelCode = attrMap.get("s7_GMODEL_CODE").toString();
        String strVariantType = attrMap.get("s7_VARIANT_TYPE").toString();
        String strEngineNo = attrMap.get("s7_ENGINE_NO").toString();
        String strLoc = attrMap.get("s7_LOCATION").toString();
        String strBodyType = attrMap.get("s7_BODY_TYPE").toString();
        String strSeater = attrMap.get("s7_SEATER").toString();
        String strTrimLevel = attrMap.get("s7_TRIM_LEVEL").toString();

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Part No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
        }
        // [SR140702-058][20140619] KOG DEV Variant Part Validation Check ����. Part No. �ڸ��� ���� ����10�ڸ����� 8 ~ 11�ڸ�
        else if (strItemID.length() > 11 || strItemID.length() < 8) {
            bufMessage.append("Part No.�� 8 ~ 11 �ڸ��� �Է��ϼž� �մϴ�." + "\n");
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

        // [SR140702-058][20140630] KOG Variant Part G-Model Code Validate �߰�.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code�� 5�ڸ� ���Ϸ� �Է��ϼž��մϴ�.");
        }
        if (strVariantType.getBytes().length > 15) {
            bufMessage.append("Variant Type�� 15�ڸ� ���Ϸ� �Է��ϼž��մϴ�.");
        }
        if (strEngineNo.getBytes().length > 7) {
            bufMessage.append("Engine No�� 7�ڸ� ���Ϸ� �Է��ϼž��մϴ�.");
        }
        if (strLoc.getBytes().length > 10) {
            bufMessage.append("Location�� 10�ڸ� ���Ϸ� �Է��ϼž��մϴ�.");
        }
        if (strBodyType.getBytes().length > 15) {
            bufMessage.append("Body Type�� 15�ڸ� ���Ϸ� �Է��ϼž��մϴ�.");
        }
        if (strSeater.getBytes().length > 5) {
            bufMessage.append("Seater�� 5�ڸ� ���Ϸ� �Է��ϼž��մϴ�.");
        }
        if (strTrimLevel.getBytes().length > 10) {
            bufMessage.append("Trim Level�� 10�ڸ� ���Ϸ� �Է��ϼž��մϴ�.");
        }
        // END [20140630]
        return bufMessage.toString();
    }

}
