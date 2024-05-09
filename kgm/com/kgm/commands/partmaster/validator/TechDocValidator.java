package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.commands.partmaster.validator.ValidatorAbs;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Tech Doc Validation Check Class
 * 
 */
public class TechDocValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public TechDocValidator() {
        this(null);
    }

    public TechDocValidator(String[][] szLovNames) {
        super(szLovNames);
    }

    @Override
    public String validate(HashMap<String, Object> attrMap, int nType) throws Exception {
        StringBuffer bufMessage = new StringBuffer();
        bufMessage.append(super.validate(attrMap, nType));
        String strItemID = (String) attrMap.get("item_id");
        String strPartName = (String) attrMap.get("object_name");
        String strRevID = (String) attrMap.get("s7_REVISION");
        String strDesc = (String) attrMap.get("object_desc");
        
        
        if (CustomUtil.isEmpty(strItemID)) {
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "SES No." }) + "\n");
        }

        if (CustomUtil.isEmpty(strRevID)) {
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Revision" }) + "\n");
        }
        
        if (CustomUtil.isEmpty(strPartName)) {
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "SES NAME" }) + "\n");
        }
        
        if (!CustomUtil.isEmpty(strDesc) && strDesc.length() > 240 ) {
            bufMessage.append("'Description' 속성은 240자 이하로 입력하셔야 합니다." + "\n");
        }


        return bufMessage.toString();
    }

}
