package com.symc.plm.rac.prebom.prebom.validator.prevehiclepart;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;

/**
 * Vehicle Part Validator
 * 
 * 1. �ʼ� �Ӽ��� Check
 * 2. Part ID �ߺ� Check
 * 3. Orign ���� ���� Part ID �ڸ��� Check
 * "K" => 10 �ڸ�
 * "D" => 10 �ڸ�
 * "A" => 10 �ڸ�
 * "B" => 10 �ڸ�
 * "N" => 12 �ڸ�
 * "S" => 7 �ڸ�
 * "G" => 8 �ڸ�
 * 
 * 4. Orign ���� ���� Display ID ó��
 * "K" : XXXXXXXXXX => XXXXX XXXXX
 * "D" : XXXXXXXXXX => XXXXX XXXXX
 * "A" : XXXXXXXXXX => XXX XXX XX XX
 * "B" : XXXXXXXXXX => XXX XXX XX XX
 * "N" : XXXXXXXXXXXX => XXXXXX XXXX
 * "S" : XXXXXXX => XXXXXXX
 * "G" : XXXXXXXX => XXXXXXXX
 * 
 * [20180213][ljg] �ý��� �ڵ� ������ �������� bomline������ �̵�
 */
public class PreVehiclePartValidator extends ValidatorAbs
{
	HashMap<String, Integer> partNoSizeMap;
	String[] clearNullPointLovs = { "s7_RESPONSIBILITY" };
	boolean validatorSkip = false;
	
	public PreVehiclePartValidator()
	{
		this(null);
	}

	public PreVehiclePartValidator(String[][] szLovNames)
	{
		super(szLovNames);
		this.setDspPartNoMap();
	}

	/**
	 * Orign ���� ���� Part ID �ڸ��� Setting
	 * "K" => 10 �ڸ�
	 * "D" => 10 �ڸ�
	 * "A" => 10 �ڸ�
	 * "B" => 10 �ڸ�
	 * "N" => 12 �ڸ�
	 * "S" => 7 �ڸ�
	 * "G" => 8 �ڸ�
	 */
	private void setDspPartNoMap()
	{
		this.partNoSizeMap = new HashMap<String, Integer>();
		this.partNoSizeMap.put("K", 10);
		this.partNoSizeMap.put("D", 10);
		this.partNoSizeMap.put("A", 10);
		this.partNoSizeMap.put("B", 10);
		this.partNoSizeMap.put("N", 12);
		this.partNoSizeMap.put("S", 7);
		this.partNoSizeMap.put("G", 8);
	}

	/**
	 * Validation Check
	 */
	@Override
	public String validate(HashMap<String, Object> attrMap, int nType) throws Exception
	{
		StringBuffer bufMessage = new StringBuffer();
		bufMessage.append(super.validate(attrMap, nType));

		String strPartNo = (String) attrMap.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
		String strRevNo = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMREVID);
//		String strOrign = (String) attrMap.get(PropertyConstant.ATTR_NAME_PARTTYPE);
		String strProjectCode = (String) attrMap.get(PropertyConstant.ATTR_NAME_PROJCODE);
		String strStage = (String) attrMap.get(PropertyConstant.ATTR_NAME_STAGE);
//		String strRegular = (String) attrMap.get(PropertyConstant.ATTR_NAME_REGULAR);

		String strPartName = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMNAME);
//		String strPartKorName = (String) attrMap.get(PropertyConstant.ATTR_NAME_KORNAME);
		String strUnit = (String) attrMap.get(PropertyConstant.ATTR_NAME_UOMTAG);
		String strSysCode = (String) attrMap.get(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);
//		Object ccnObj = attrMap.get(PropertyConstant.ATTR_NAME_CCNNO);
		String strColorID = (String) attrMap.get(PropertyConstant.ATTR_NAME_COLORID);
		String strEstWeight = (String) attrMap.get(PropertyConstant.ATTR_NAME_ESTWEIGHT);
		String strCalWeight = (String) attrMap.get(PropertyConstant.ATTR_NAME_CALWEIGHT);
//		String strChangeDesc = (String) attrMap.get(PropertyConstant.ATTR_NAME_ITEMDESC);

//		if (CustomUtil.isEmpty(strOrign))
//		{
//			// 'Part Origin'�� �ʼ��Է� �����Դϴ�.
//			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Origin" }) + "\n");
//		}

		if (CustomUtil.isEmpty(strPartNo) || CustomUtil.isEmpty(strRevNo))
		{
			// 'Part No.'�� �ʼ��Է� �����Դϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
		}

		if (CustomUtil.isEmpty(strPartName))
		{
			// 'Part Name'�� �ʼ��Է� �����Դϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Name" }) + "\n");
		}
		if (CustomUtil.isEmpty(strProjectCode))
		{
			// 'Project Code'�� �ʼ��Է� �����Դϴ�.;
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Code" }) + "\n");
		}
		if (CustomUtil.isEmpty(strStage))
		{
			// 'Part Stage'�� �ݵ�� ���� �����ؾ� �մϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Stage" }) + "\n");
		}
//        if (ccnObj == null || ! (ccnObj instanceof TCComponentChangeItemRevision))
//        {
//            bufMessage.append("Pre Part �� ������ CCN�� ������ �ּ���." + "\n");
//        }
//		if (CustomUtil.isEmpty(strRegular))
//		{
//			// 'Part Stage'�� �ݵ�� ���� �����ؾ� �մϴ�.
//			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Regular" }) + "\n");
//		}

//		if ("R".equals(strRegular))
		{
//			if (CustomUtil.isEmpty(strPartKorName))
//			{
//				// 'Part Kor Name'�� �ʼ��Է� �����Դϴ�.
//				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Kor Name" }) + "\n");
//			}

			if (CustomUtil.isEmpty(strUnit))
			{
				// 'Unit'�� �ʼ��Է� �����Դϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Unit" }) + "\n");
			}
			if (CustomUtil.isEmpty(strSysCode))
			{
				// 'System Code'�� �ʼ��Է� �����Դϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "System Code" }) + "\n");
			}
			if (CustomUtil.isEmpty(strColorID))
			{
				// 'Color ID'�� �ʼ��Է� �����Դϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Color ID" }) + "\n");
			}
			if (CustomUtil.isEmpty(strEstWeight))
			{
				// 'Est. Weight'�� �ʼ��Է� �����Դϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Est. Weight" }) + "\n");
			}
			else if ("0".equals(strEstWeight))
			{
				bufMessage.append("Est. Weight '0' Value Is InValid ");
			}

//			String strDspNo = this.getDisplayNo(strOrign, strPartNo);
//			if (strDspNo == null)
//			{
//				// "Part Origin ���� '" + strOrign + "'�� ��� Part No.�� '" + this.partNoSizeMap.get(strOrign) + "'�ڷ� �Է��ϼž� �մϴ�.");
//				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputLimitValue", new String[] { "Part Origin", strOrign, " Part No.", this.partNoSizeMap.get(strOrign) + "" })
//						+ "\n");
//			}
//			else
//			{
//				attrMap.put("s7_DISPLAY_PART_NO", strPartNo.toUpperCase());
//			}
		}

		if (!checkDoubleLimiting84Size(strEstWeight))
		{
			// bufMessage.append("�����߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "�����߷�-Kg", "8", "4" }) + "\n");
		}
		if (!checkDoubleLimiting84Size(strCalWeight))
		{
			// ����߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "����߷�-Kg", "8", "4" }) + "\n");
		}
		
		/**
		 * [SR141126-021][2014.11.27][jclee] Category �ʼ� �Է� ������ ����.
		 */
		String strCategory = (String) attrMap.get(PropertyConstant.ATTR_NAME_REGULATION);
//		String strDRCheckFlag = (String) attrMap.get("DR_CHECK_FLAG");
		if (CustomUtil.isEmpty(strCategory))
		{
			// 'Category'�� �ݵ�� ���� �����ؾ� �մϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Category" }) + "\n");
		}
		
		return bufMessage.toString();
	}

	/**
	 * 
	 * Part Orign���� ���� Display No.�� �����մϴ�.
	 * 
	 * Orign ���� ���� Display ID ó��
	 * "K" : XXXXXXXXXX => XXXXX XXXXX
	 * "D" : XXXXXXXXXX => XXXXX XXXXX
	 * "A" : XXXXXXXXXX => XXX XXX XX XX
	 * "B" : XXXXXXXXXX => XXX XXX XX XX
	 * "N" : XXXXXXXXXXXX => XXXXXX XXXX
	 * "S" : XXXXXXX => XXXXXXX
	 * "G" : XXXXXXXX => XXXXXXXX
	 * 
	 * @return Display No
	 */
	public String getDisplayNo(String strOrign, String strPartNo)
	{
		if (this.partNoSizeMap.get(strOrign) == null)
		{
			return "";
		}
		if (strPartNo.length() != this.partNoSizeMap.get(strOrign))
		{
			return null;
		}
		StringBuffer bufDspNo = new StringBuffer();
		if ("K".equals(strOrign))
		{
			bufDspNo.append(strPartNo.substring(0, 5));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(5, strPartNo.length()));
		}
		else if ("D".equals(strOrign))
		{
			bufDspNo.append(strPartNo.substring(0, 5));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(5, strPartNo.length()));
		}
		else if ("A".equals(strOrign))
		{
			bufDspNo.append(strPartNo.substring(0, 3));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(3, 6));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(6, 8));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(8, strPartNo.length()));
		}
		else if ("B".equals(strOrign))
		{
			bufDspNo.append(strPartNo.substring(0, 3));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(3, 6));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(6, 8));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(8, strPartNo.length()));
		}
		else if ("N".equals(strOrign))
		{
			bufDspNo.append(strPartNo.substring(0, 6));
			bufDspNo.append(" ");
			bufDspNo.append(strPartNo.substring(6, strPartNo.length()));
		}
		else if ("S".equals(strOrign))
		{
			bufDspNo.append(strPartNo);
		}
		else
		{
			bufDspNo.append(strPartNo);
		}
		return bufDspNo.toString();
	}

	/**
	 * Double ���� 8�ڸ� ���� �Ҽ��� 4�ڸ� ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2013. 1. 8.
	 * @param text
	 * @return
	 */
	private boolean checkDoubleLimiting84Size(String text)
	{
		if (CustomUtil.isEmpty(text))
		{
			return true;
		}
		if (text.contains("."))
		{
			String first = text.substring(0, text.lastIndexOf("."));
			String second = text.substring(text.lastIndexOf(".") + 1, text.length());
			if (first.length() > 8 || second.length() > 4)
			{
				return false;
			}
		}
		else
		{
			if (text.length() > 8)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * ���̱׷��̼��� ��쿡�� �ڸ��� üũ�� skip�Ѵ�.
	 * 
	 * �� �޼ҵ�� �ݵ�� validate ȣ�� ���� �����Ѵ�.
	 */
	@Override
	public void setValidatorSkip(boolean check)
	{
		validatorSkip = check;
	}
}
