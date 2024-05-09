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
		
		// [SR140324-030][20140620] KOG DEV Std . Part SES Spec No. �� ���� Validation �߰�.
		String strSESSpecNo = attrMap.get("s7_SES_SPEC_NO").toString();

		String strUOM = (String) attrMap.get("uom_tag");

		if (CustomUtil.isEmpty(strItemID))
		{
			// bufMessage.append("'Part No.'�� �ʼ��Է� �����Դϴ�. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
		}
		if (CustomUtil.isEmpty(strPartName))
		{
			// bufMessage.append("'Part Name'�� �ʼ��Է� �����Դϴ�. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Name" }) + "\n");
		}
		// Validator ����
		if (!validatorSkip)
		{
			if (CustomUtil.isEmpty(strPartKorName))
			{
				// bufMessage.append("'Korean Name'�� �ʼ��Է� �����Դϴ�. \n");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Korean Name" }) + "\n");
			}
			if (CustomUtil.isEmpty(strActWeight))
			{
				// bufMessage.append("'Actual Weight'�� �ʼ��Է� �����Դϴ�. \n");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Actual Weight" }) + "\n");
			}
		}
		if (CustomUtil.isEmpty(strMaturity))
		{
			// bufMessage.append("'Maturity'�� �ʼ��Է� �����Դϴ�. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Maturity" }) + "\n");
		}
		if (CustomUtil.isEmpty(strUOM))
		{
			// bufMessage.append("'Maturity'�� �ʼ��Է� �����Դϴ�. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Unit" }) + "\n");
		}

		if ("".equals(bufMessage.toString()))
		{
			if (strItemID.length() != 10)
			{
				// bufMessage.append("'Part No.'�� 10�ڷ� �Է��ϼž� �մϴ�. \n");
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
		
		// [SR140324-030][20140620] KOG DEV Std . Part SES Spec No. �� ���� Validation �߰�.
        if (strSESSpecNo.getBytes().length > 500) {
            bufMessage.append("SES Spec No. �� 500 (byte) ���Ϸ� �Է��ؾ� �մϴ�.");
        }
		
		return bufMessage.toString();
	}

	@Override
	public void setValidatorSkip(boolean check)
	{
		validatorSkip = check;
	}

}
