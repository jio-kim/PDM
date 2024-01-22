package com.symc.plm.rac.prebom.prebom.validator.prevehiclepart;

import java.util.HashMap;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.validator.ValidatorAbs;

/**
 * Vehicle Part Validator
 * 
 * 1. 필수 속성값 Check
 * 2. Part ID 중복 Check
 * 3. Orign 값에 따른 Part ID 자리수 Check
 * "K" => 10 자리
 * "D" => 10 자리
 * "A" => 10 자리
 * "B" => 10 자리
 * "N" => 12 자리
 * "S" => 7 자리
 * "G" => 8 자리
 * 
 * 4. Orign 값에 따른 Display ID 처리
 * "K" : XXXXXXXXXX => XXXXX XXXXX
 * "D" : XXXXXXXXXX => XXXXX XXXXX
 * "A" : XXXXXXXXXX => XXX XXX XX XX
 * "B" : XXXXXXXXXX => XXX XXX XX XX
 * "N" : XXXXXXXXXXXX => XXXXXX XXXX
 * "S" : XXXXXXX => XXXXXXX
 * "G" : XXXXXXXX => XXXXXXXX
 * 
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
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
	 * Orign 값에 따른 Part ID 자리수 Setting
	 * "K" => 10 자리
	 * "D" => 10 자리
	 * "A" => 10 자리
	 * "B" => 10 자리
	 * "N" => 12 자리
	 * "S" => 7 자리
	 * "G" => 8 자리
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
//			// 'Part Origin'는 필수입력 사항입니다.
//			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Origin" }) + "\n");
//		}

		if (CustomUtil.isEmpty(strPartNo) || CustomUtil.isEmpty(strRevNo))
		{
			// 'Part No.'는 필수입력 사항입니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part No." }) + "\n");
		}

		if (CustomUtil.isEmpty(strPartName))
		{
			// 'Part Name'은 필수입력 사항입니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Name" }) + "\n");
		}
		if (CustomUtil.isEmpty(strProjectCode))
		{
			// 'Project Code'는 필수입력 사항입니다.;
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Project Code" }) + "\n");
		}
		if (CustomUtil.isEmpty(strStage))
		{
			// 'Part Stage'에 반드시 값을 선택해야 합니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Stage" }) + "\n");
		}
//        if (ccnObj == null || ! (ccnObj instanceof TCComponentChangeItemRevision))
//        {
//            bufMessage.append("Pre Part 를 연결할 CCN을 선택해 주세요." + "\n");
//        }
//		if (CustomUtil.isEmpty(strRegular))
//		{
//			// 'Part Stage'에 반드시 값을 선택해야 합니다.
//			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Regular" }) + "\n");
//		}

//		if ("R".equals(strRegular))
		{
//			if (CustomUtil.isEmpty(strPartKorName))
//			{
//				// 'Part Kor Name'은 필수입력 사항입니다.
//				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Kor Name" }) + "\n");
//			}

			if (CustomUtil.isEmpty(strUnit))
			{
				// 'Unit'은 필수입력 사항입니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Unit" }) + "\n");
			}
			if (CustomUtil.isEmpty(strSysCode))
			{
				// 'System Code'는 필수입력 사항입니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "System Code" }) + "\n");
			}
			if (CustomUtil.isEmpty(strColorID))
			{
				// 'Color ID'는 필수입력 사항입니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Color ID" }) + "\n");
			}
			if (CustomUtil.isEmpty(strEstWeight))
			{
				// 'Est. Weight'는 필수입력 사항입니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Est. Weight" }) + "\n");
			}
			else if ("0".equals(strEstWeight))
			{
				bufMessage.append("Est. Weight '0' Value Is InValid ");
			}

//			String strDspNo = this.getDisplayNo(strOrign, strPartNo);
//			if (strDspNo == null)
//			{
//				// "Part Origin 값이 '" + strOrign + "'인 경우 Part No.는 '" + this.partNoSizeMap.get(strOrign) + "'자로 입력하셔야 합니다.");
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
			// bufMessage.append("예측중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다. \n");
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "예측중량-Kg", "8", "4" }) + "\n");
		}
		if (!checkDoubleLimiting84Size(strCalWeight))
		{
			// 계산중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "계산중량-Kg", "8", "4" }) + "\n");
		}
		
		/**
		 * [SR141126-021][2014.11.27][jclee] Category 필수 입력 란으로 변경.
		 */
		String strCategory = (String) attrMap.get(PropertyConstant.ATTR_NAME_REGULATION);
//		String strDRCheckFlag = (String) attrMap.get("DR_CHECK_FLAG");
		if (CustomUtil.isEmpty(strCategory))
		{
			// 'Category'에 반드시 값을 선택해야 합니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Category" }) + "\n");
		}
		
		return bufMessage.toString();
	}

	/**
	 * 
	 * Part Orign값에 따라 Display No.를 생성합니다.
	 * 
	 * Orign 값에 따른 Display ID 처리
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
	 * Double 정수 8자리 이하 소수점 4자리 이하
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
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
	 * 마이그레이션인 경우에는 자릿수 체크를 skip한다.
	 * 
	 * 이 메소드는 반드시 validate 호출 전에 실행한다.
	 */
	@Override
	public void setValidatorSkip(boolean check)
	{
		validatorSkip = check;
	}
}
