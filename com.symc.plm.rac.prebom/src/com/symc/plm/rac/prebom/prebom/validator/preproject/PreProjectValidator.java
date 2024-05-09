package com.symc.plm.rac.prebom.prebom.validator.preproject;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;


/**
 * Project Validation Check Class
 * [20170116] Project Code 'PRE' �� �ڵ������� �ٿ��༭ ���ڼ� ������
 */
public class PreProjectValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public PreProjectValidator() {
        this(null);
    }

    public PreProjectValidator(String[][] szLovNames) {
        super(szLovNames);
    }

    @Override
    public String validate(HashMap<String, Object> attrMap, int nType) throws Exception {
        StringBuffer bufMessage = new StringBuffer();
        bufMessage.append(super.validate(attrMap, nType));
        String strItemID = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMID);
        String strPartName = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMNAME);

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Project No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project No." }) + "\n");
        }//[20170116] Project Code 'PRE' �� �ڵ������� �ٿ��༭ ���ڼ� ������
        else if( strItemID.length() > 9 )
        {
        	bufMessage.append("Part No.�� 6�ڸ� ���Ϸ� �Է��ϼž� �մϴ�." + "\n");
        }
        
        if (CustomUtil.isEmpty(strPartName)) {
            // Project Name'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Name" }) + "\n");
        }
        return bufMessage.toString();
    }

}
