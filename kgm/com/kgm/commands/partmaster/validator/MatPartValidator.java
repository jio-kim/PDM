package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Material Part Validation Check Class
 * 
 * Material Source 유형에 따라 Part ID 발번 로직이 다름
 * ex) Material Source 유형에 해당하는 Code(Spec No.)
 * 
 */
public class MatPartValidator extends ValidatorAbs {
    HashMap<String, Integer> partNoSizeMap;

    public MatPartValidator() {
        this(null);
    }

    public MatPartValidator(String[][] szLovNames) {
        super(szLovNames);
    }

    @SuppressWarnings("unused")
	@Override
    public String validate(HashMap<String, Object> attrMap, int nType) throws Exception {
        StringBuffer bufMessage = new StringBuffer();
        String strItemID = (String) attrMap.get("item_id");
        String strActivation = (String) attrMap.get("s7_ACTIVATION");
        String strMatType = (String) attrMap.get("s7_TYPE");

        String strMaturity = (String) attrMap.get("s7_MATURITY");
        String strMatSource = (String) attrMap.get("s7_SOURCE");
        String strSpecNo = (String) attrMap.get("s7_SPEC_NUMBER");
        String strDinCode = (String) attrMap.get("s7_DIN_CODE");
        String strDensity = (String) attrMap.get("s7_DENSITY");
        
        
        
        
        if (CustomUtil.isEmpty(strSpecNo)) {
            // 'Spec No.'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Spec No." }) + "\n");
        }
        
        
        // Material Source 유형에 따라 Part ID 발번 로직이 다름
        String strPreNo = "";

        if( "SES".equals(strMatSource) )
        {
        	strPreNo = (String)attrMap.get("s7_SES_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "SES", "SES Code" }) + "\n");
            }
        	
        }
        else if( "KS".equals(strMatSource) )
        {

        	strPreNo = (String)attrMap.get("s7_KS_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "KS", "KS Code" }) + "\n");
            }
        }
        else if( "JIS".equals(strMatSource) )
        {
        	strPreNo = (String)attrMap.get("s7_JIS_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "JIS", "JIS Code" }) + "\n");
            }
        }
        else if( "DIN".equals(strMatSource) )
        {

        	strPreNo = (String)attrMap.get("s7_DIN_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "DIN", "DIN Code" }) + "\n");
            }
        }
        else if( "MB".equals(strMatSource) )
        {

        	strPreNo = (String)attrMap.get("s7_MB_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "MB", "MB Code" }) + "\n");
            }
        }
        else if( "SAE".equals(strMatSource) )
        {
        	strPreNo = (String)attrMap.get("s7_SAE_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "SAE", "SAE Code" }) + "\n");
            }
        	
        }
        else if( "SUPPLY".equals(strMatSource) )
        {
        	strPreNo = (String)attrMap.get("s7_SUP_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "SUPPLY", "Supplier Code" }) + "\n");
            }
        	
        }
        else if( "OTHERS".equals(strMatSource) )
        {
        	strPreNo = (String)attrMap.get("s7_OTHER_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "OTHERS", "Other Code" }) + "\n");
            }
        }
        else if( "GB".equals(strMatSource) )
        {
        	strPreNo = (String)attrMap.get("s7_GB_CODE");
        	if (CustomUtil.isEmpty(strPreNo)) 
            {
              bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Material Source", "GB", "GB Code" }) + "\n");
            }
        }
        
        // Part No. 발번
        strItemID = strPreNo + "(" + strSpecNo + ")";
        
        //  [SR140610-43][20140609] KOG 
//        Part No Validation 체크 48자리이하
        if (strItemID.getBytes().length >  48) {
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputLimitValueUnder", new String[] { "Part No", "48" }) + "\n");
        } 
        
         attrMap.put("item_id", strItemID);
         bufMessage.append(super.validate(attrMap, nType));

        
        
        if (CustomUtil.isEmpty(strActivation)) {
            // 'Activity'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Activity" }) + "\n");
        }
        if (CustomUtil.isEmpty(strMatType)) {
            // 'Material Type'은 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Material Type" }) + "\n");
        }
        if (CustomUtil.isEmpty(strMaturity)) {
            // 'Maturity'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Maturity" }) + "\n");
        }

        if (CustomUtil.isEmpty(strMatSource)) {
            // 'Material Source'는 필수입력 사항입니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Material Source" }) + "\n");
        }
        if (!checkDoubleLimiting85Size(strDensity)) {
            // 'Density'는 정수 8, 소수점 이하5자리 까지 가능합니다.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "Density", "8", "5" }) + "\n");
        }
        return bufMessage.toString();
    }

    private boolean checkDoubleLimiting85Size(String text) {
        if (CustomUtil.isEmpty(text)) {
            return true;
        }
        if (text.contains(".")) {
            String first = text.substring(0, text.lastIndexOf("."));
            String second = text.substring(text.lastIndexOf(".") + 1, text.length());
            if (first.length() > 8 || second.length() > 5) {
                return false;
            }
        } else {
            if (text.length() > 8) {
                return false;
            }
        }
        return true;
    }

}
