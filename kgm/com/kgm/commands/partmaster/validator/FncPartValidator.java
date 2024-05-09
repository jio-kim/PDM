package com.kgm.commands.partmaster.validator;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Function Part Validation Check Class
 * 
 */
/** [SR190123-018] Function Type Text -> ComboBox ����
 * 1. Function Master�� Function Type LOV �� �ʼ� �Է����� ����
 * 2. Power Train �����϶� Supply Mode�� ���� Vehicle Function�� �ƴϸ� VC���� ���� Vehicle Project Code �߰�
 */
public class FncPartValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public FncPartValidator() {
        this(null);
    }

    public FncPartValidator(String[][] szLovNames) {
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
        
        if (CustomUtil.isEmpty(strFuncType)) {
            // 'strFuncType'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Function Type" }) + "\n");
        } else {
        	ArrayList<String> al = new ArrayList<String>();
        	al.add("Vehicle");
        	al.add("Engine");
        	al.add("Transmission");
        	al.add("AXLE");
        	if(!al.contains(strFuncType)){
        		bufMessage.append("Function Type ��(" +strFuncType+")�� ��ȿ���� �ʽ��ϴ�." + "\n");
        	}
        }
        
        // [SR140702-058][20140630] KOG Product Part G-Model Code Validate �߰�.
        if (strGModelCode.getBytes().length > 5) {
            bufMessage.append("G-Model Code �� 5�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }
//        if (strFuncType.getBytes().length > 15) {
//            //bufMessage.append("Product Code �� 15�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
//        }
        
        return bufMessage.toString();
    }

}
