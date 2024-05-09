package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Material Part Validation Check Class
 * 
 * Material Source ������ ���� Part ID �߹� ������ �ٸ�
 * ex) Material Source ������ �ش��ϴ� Code(Spec No.)
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
            // 'Spec No.'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Spec No." }) + "\n");
        }
        
        
        // Material Source ������ ���� Part ID �߹� ������ �ٸ�
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
        
        // Part No. �߹�
        strItemID = strPreNo + "(" + strSpecNo + ")";
        
        //  [SR140610-43][20140609] KOG 
//        Part No Validation üũ 48�ڸ�����
        if (strItemID.getBytes().length >  48) {
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputLimitValueUnder", new String[] { "Part No", "48" }) + "\n");
        } 
        
         attrMap.put("item_id", strItemID);
         bufMessage.append(super.validate(attrMap, nType));

        
        
        if (CustomUtil.isEmpty(strActivation)) {
            // 'Activity'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Activity" }) + "\n");
        }
        if (CustomUtil.isEmpty(strMatType)) {
            // 'Material Type'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Material Type" }) + "\n");
        }
        if (CustomUtil.isEmpty(strMaturity)) {
            // 'Maturity'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Maturity" }) + "\n");
        }

        if (CustomUtil.isEmpty(strMatSource)) {
            // 'Material Source'�� �ʼ��Է� �����Դϴ�.
            bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Material Source" }) + "\n");
        }
        if (!checkDoubleLimiting85Size(strDensity)) {
            // 'Density'�� ���� 8, �Ҽ��� ����5�ڸ� ���� �����մϴ�.
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
