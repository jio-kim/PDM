package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;


/**
 * Project Validation Check Class
 */
public class ProjectValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public ProjectValidator() {
        this(null);
    }

    public ProjectValidator(String[][] szLovNames) {
        super(szLovNames);
    }

    @Override
    public String validate(HashMap<String, Object> attrMap, int nType) throws Exception {
        StringBuffer bufMessage = new StringBuffer();
        bufMessage.append(super.validate(attrMap, nType));
        String strItemID = (String) attrMap.get("item_id");
        String strPartName = (String) attrMap.get("object_name");

        if (CustomUtil.isEmpty(strItemID)) {
            // 'Project No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project No." }) + "\n");
        }
        else if( strItemID.length() > 6 )
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
