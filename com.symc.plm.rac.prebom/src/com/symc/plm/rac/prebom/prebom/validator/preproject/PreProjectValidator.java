package com.symc.plm.rac.prebom.prebom.validator.preproject;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;


/**
 * Project Validation Check Class
 * [20170116] Project Code 'PRE' 를 자동적으로 붙여줘서 글자수 변경함
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
            // 'Project No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project No." }) + "\n");
        }//[20170116] Project Code 'PRE' 를 자동적으로 붙여줘서 글자수 변경함
        else if( strItemID.length() > 9 )
        {
        	bufMessage.append("Part No.는 6자리 이하로 입력하셔야 합니다." + "\n");
        }
        
        if (CustomUtil.isEmpty(strPartName)) {
            // Project Name'은 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Name" }) + "\n");
        }
        return bufMessage.toString();
    }

}
