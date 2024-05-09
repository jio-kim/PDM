package com.symc.plm.rac.prebom.prebom.validator.prefuncmaster;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;

/**
 * [SR140828-010][20140828] jclee Function Master Part Validation Check ����. Part No. �ڸ��� ���� ����11�ڸ����� 7 ~ 11�ڸ�
 * Function Master Part Validation Check Class
 */
public class PreFuncMasterValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public PreFuncMasterValidator() {
        this(null);
    }

    public PreFuncMasterValidator(String[][] szLovNames) {
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
        Object ccnObj = attrMap.get(PropertyConstant.ATTR_NAME_CCNNO);

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
            bufMessage.append("Product Type �� 15�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }

        if (ccnObj == null || ! (ccnObj instanceof TCComponentChangeItemRevision))
        {
            bufMessage.append("Pre Function Master�� ������ CCN�� ������ �ּ���." + "\n");
        }

        return bufMessage.toString();
    }

}
