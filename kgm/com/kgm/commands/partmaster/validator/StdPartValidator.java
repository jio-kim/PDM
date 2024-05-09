package com.kgm.commands.partmaster.validator;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;

/**
 * Standard Part Validation Check Class
 */
public class StdPartValidator extends ValidatorAbs
{
	HashMap<String, Integer> partNoSizeMap;

	boolean validatorSkip = false;

	public StdPartValidator()
	{
		this(null);
	}

	public StdPartValidator(String[][] szLovNames)
	{
		super(szLovNames);
	}

	@Override
	public String validate(HashMap<String, Object> attrMap, int nType) throws Exception
	{
		StringBuffer bufMessage = new StringBuffer();
		bufMessage.append(super.validate(attrMap, nType));
		String strItemID = (String) attrMap.get("item_id");
		String strPartName = (String) attrMap.get("object_name");
		String strPartKorName = (String) attrMap.get("s7_KOR_NAME");
		String strActWeight = (String) attrMap.get("s7_ACT_WEIGHT");
		String strMaturity = (String) attrMap.get("s7_MATURITY");
		
		// [SR140324-030][20140620] KOG DEV Std . Part SES Spec No. 에 대한 Validation 추가.
		String strSESSpecNo = attrMap.get("s7_SES_SPEC_NO").toString();

		String strUOM = (String) attrMap.get("uom_tag");

		if (CustomUtil.isEmpty(strItemID))
		{
			// bufMessage.append("'Part No.'는 필수입력 사항입니다. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
		}
		if (CustomUtil.isEmpty(strPartName))
		{
			// bufMessage.append("'Part Name'은 필수입력 사항입니다. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Name" }) + "\n");
		}
		// Validator 제외
		if (!validatorSkip)
		{
			if (CustomUtil.isEmpty(strPartKorName))
			{
				// bufMessage.append("'Korean Name'은 필수입력 사항입니다. \n");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Korean Name" }) + "\n");
			}
			if (CustomUtil.isEmpty(strActWeight))
			{
				// bufMessage.append("'Actual Weight'은 필수입력 사항입니다. \n");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Actual Weight" }) + "\n");
			}
		}
		if (CustomUtil.isEmpty(strMaturity))
		{
			// bufMessage.append("'Maturity'는 필수입력 사항입니다. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Maturity" }) + "\n");
		}
		if (CustomUtil.isEmpty(strUOM))
		{
			// bufMessage.append("'Maturity'는 필수입력 사항입니다. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Unit" }) + "\n");
		}

		if ("".equals(bufMessage.toString()))
		{
			if (strItemID.length() != 10)
			{
				// bufMessage.append("'Part No.'는 10자로 입력하셔야 합니다. \n");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputLimitValue", new String[] { "Part No.", "10" }) + "\n");
			}
			else
			{
				StringBuffer bufDspNo = new StringBuffer();
				bufDspNo.append(strItemID.substring(0, 5));
				bufDspNo.append(" ");
				bufDspNo.append(strItemID.substring(5, strItemID.length()));
				attrMap.put("s7_DISPLAY_PART_NO", bufDspNo.toString());
			}
		}
		
		// [SR140324-030][20140620] KOG DEV Std . Part SES Spec No. 에 대한 Validation 추가.
        if (strSESSpecNo.getBytes().length > 500) {
            bufMessage.append("SES Spec No. 는 500 (byte) 이하로 입력해야 합니다.");
        }
		
		return bufMessage.toString();
	}

	@Override
	public void setValidatorSkip(boolean check)
	{
		validatorSkip = check;
	}

}
