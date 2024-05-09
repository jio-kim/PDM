package com.symc.plm.rac.prebom.ccn.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;

public class PreCCNValidator extends ValidatorAbs {

    HashMap<String, Integer> partNoSizeMap;

    public PreCCNValidator() {
        this(null);
    }

    public PreCCNValidator(String[][] szLovNames) {
        super(szLovNames);
    }
    
    @Override
    public String validate(HashMap<String, Object> attrMap, int nType) throws Exception {
        StringBuffer bufMessage = new StringBuffer();
//        bufMessage.append(super.validate(attrMap, nType));
        String projectCode = (String) attrMap.get(PropertyConstant.ATTR_NAME_PROJCODE);
        String systemCode = (String) attrMap.get(PropertyConstant.ATTR_NAME_SYSTEMCODE);
        String projType = (String) attrMap.get(PropertyConstant.ATTR_NAME_PROJECTTYPE);
//        String descText = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMDESC);
        String ospecNo = (String) attrMap.get(PropertyConstant.ATTR_NAME_OSPECNO);
        String gateNo = (String) attrMap.get(PropertyConstant.ATTR_NAME_GATENO);
        
        boolean regulation = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_REGULATION);
        boolean costDown = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_COSTDOWN);
        boolean orderingSpec = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_ORDERINGSPEC);
        boolean qualityImprovement = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT);
        boolean correctionOfEpl = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL);
        boolean stylingUpDate = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_STYLINGUPDATE);
        boolean weightChange = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_WEIGHTCHANGE);
        boolean materialCostChange = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE);
        boolean theOthers = (boolean) attrMap.get(PropertyConstant.ATTR_NAME_THEOTHERS);
        
        if (CustomUtil.isEmpty(ospecNo) && !"02".equals(projType)) {
            // 'O-Spec NO.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "O-Spec No." }) + "\n");
        }
        if (CustomUtil.isEmpty(gateNo) && !"02".equals(projType)) {
            // 'Gate NO.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Gate No." }) + "\n");
        }        
        if (CustomUtil.isEmpty(projectCode)) {
            // 'Project Code'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Code" }) + "\n");
        }
        if (CustomUtil.isEmpty(systemCode)) {
            // 'System Code'은 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "System Code" }) + "\n");
        }
//        if (CustomUtil.isEmpty(descText)) {
//            // 'Description'는 필수입력 사항입니다.
//            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Description" }) + "\n");
//        }
        if (!regulation && !costDown && !orderingSpec && !qualityImprovement && !correctionOfEpl && !stylingUpDate && !weightChange && !materialCostChange && !theOthers) {
            // 'Change Reason'는 필수 선택 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredValue", new String[] { "Change Reason" }) + "\n");
        }
        
        return bufMessage.toString();
    }
}
