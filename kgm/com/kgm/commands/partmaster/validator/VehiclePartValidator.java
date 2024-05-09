package com.kgm.commands.partmaster.validator;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.client.model.LovValue;

/**
 * [20160712][ymjang] Shown On Part No Validation 체크
 * [20161011][ymjang] Shown On Part No Validation 체크 오류 수정
 */
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

 * [SR180130-033][LJG]
 * 1. E-BOM Part Master(Eng. Info) 중 "Responsibility" => "DWG Creator" 로 변경
   2. Responsibility Filed 내 LOV 값 추가 : Supplier, Collaboration, SYMC
   3. 신규 part 생성 시 기존 LOV Black BOX, Gray Box, White Box 선택불가 처리
   4. Revision Up 시 기존 Responsibiliy 값 삭제 => 설계 재지정하도록 처리
 */
public class VehiclePartValidator extends ValidatorAbs
{
	HashMap<String, Integer> partNoSizeMap;
	String[] clearNullPointLovs = { "s7_RESPONSIBILITY" };
	boolean validatorSkip = false;
	
	private final String RETURN_VALUE_ERROR = "~{ERROR}";
	private TCSession session = (TCSession) AIFUtility.getSessionManager().getDefaultSession();

	public VehiclePartValidator()
	{
		this(null);
	}

	public VehiclePartValidator(String[][] szLovNames)
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

		String strPartNo = (String) attrMap.get("item_id");
		String strRevNo = (String) attrMap.get("item_revision_id");
		String strOrign = (String) attrMap.get("s7_PART_TYPE");
		String strProjectCode = (String) attrMap.get("s7_PROJECT_CODE");
		String strStage = (String) attrMap.get("s7_STAGE");
		String strRegular = (String) attrMap.get("s7_REGULAR_PART");

		String strPartName = (String) attrMap.get("object_name");
		String strPartKorName = (String) attrMap.get("s7_KOR_NAME");
		String strUnit = (String) attrMap.get("uom_tag");
		String strSysCode = (String) attrMap.get("s7_BUDGET_CODE");
		String strDrwStat = (String) attrMap.get("s7_DRW_STAT");
		String strColorID = (String) attrMap.get("s7_COLOR");
		String strResponsibility = (String) attrMap.get("s7_RESPONSIBILITY");
		String strEstWeight = (String) attrMap.get("s7_EST_WEIGHT");
		String strMatThick = (String) attrMap.get("s7_THICKNESS");
		String strAltMatThick = (String) attrMap.get("s7_ALT_THICKNESS");
		String strCalWeight = (String) attrMap.get("s7_CAL_WEIGHT");
		String strActWeight = (String) attrMap.get("s7_ACT_WEIGHT");
		/////////////////////////////////////////////////////////////////////////////////
		//[SR180724-011-09]
		// Target Weight 추가
		String strTargetWeight = (String) attrMap.get("s7_TARGET_WEIGHT");
		String strRegulation = (String) attrMap.get("s7_R");
		String strCritical = (String) attrMap.get("s7_C");
		///////////////////////////////////////////////////////////////////////////////
		String strCalSurface = (String) attrMap.get("s7_CAL_SURFACE");
		String strMainName = (String) attrMap.get("s7_MAIN_NAME");
		String strSubName = (String) attrMap.get("s7_SUB_NAME");
		String strSystemCode = (String) attrMap.get("s7_BUDGET_CODE");
		
		//[SR140324-030][20140620] KOG DEV Veh. Part SES Spec No. 에 대한 Validation 추가.
		String strSESSpecNo = (String) attrMap.get("s7_SES_SPEC_NO");
		
		// [SR없음][2015.06.16][jclee] Finish 자리수 Validate
		String strFinish = (String) attrMap.get("s7_FINISH");
		
		if( CustomUtil.isEmpty(strActWeight))
		{
			strActWeight = "0";
		}
		/////////////////////////////////////////////////////////////////////////////////////////
		// Target Weight Validation 설정
		if( CustomUtil.isEmpty(strTargetWeight))
		{
			strTargetWeight = "0";
		}
		
		////////////////////////////////////////////////////////////////////////////////////////

		if (CustomUtil.isEmpty(strOrign))
		{
			// 'Part Origin'는 필수입력 사항입니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Origin" }) + "\n");
		}

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
		if (CustomUtil.isEmpty(strRegular))
		{
			// 'Part Stage'에 반드시 값을 선택해야 합니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Regular" }) + "\n");
		}

		if ("R".equals(strRegular))
		{
			if (CustomUtil.isEmpty(strPartKorName))
			{
				// 'Part Kor Name'은 필수입력 사항입니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Kor Name" }) + "\n");
			}

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
			if (CustomUtil.isEmpty(strDrwStat))
			{
				// 'Drw Status'는 필수입력 사항입니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Drw Status" }) + "\n");
			}
			// validatorSkip = true 인 경우 validator 체크를 skip한다.
			if (!validatorSkip)
			{
				if (CustomUtil.isEmpty(strColorID))
				{
					// 'Color ID'는 필수입력 사항입니다.
					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Color ID" }) + "\n");
				}

				if (CustomUtil.isEmpty(strResponsibility))
				{
					// 'Responsibility'는 필수입력 사항입니다.
					//[SR180130-033][LJG] "Responsibility" => "DWG Creator" 로 변경
					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "DWG Creator" }) + "\n");
				}
			}
			int iEstWeightNatural = 0;
			int iEstWeightPrime = 0;
			
			if (CustomUtil.isEmpty(strEstWeight))
			{				
				// 'Est. Weight'는 필수입력 사항입니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Est. Weight" }) + "\n");
				// [20161011][ymjang] 실중량이 없을 경우만, 예상중량 필수 체크함.
//				if (strActWeight.equals("0")) {
//					// 'Est. Weight'는 필수입력 사항입니다.
//					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Est. Weight" }) + "\n");
//				}
			}
			else if ("0".equals(strEstWeight))
			{
				bufMessage.append("Est. Weight '0' Value Is InValid ");				
				// [20161011][ymjang] 실중량이 없을 경우만, 예상중량 필수 체크함.
//				if (strActWeight.equals("0")) {
//					bufMessage.append("Est. Weight '0' Value Is InValid ");
//				}
			}
			else {
				String sTempEstWeightNatural = strEstWeight.substring(0, strEstWeight.contains(".") ? strEstWeight.indexOf(".") : strEstWeight.length());
				if (sTempEstWeightNatural == null || sTempEstWeightNatural.equals("") || sTempEstWeightNatural.length() == 0) {
					iEstWeightNatural = 0;
				} else {
					iEstWeightNatural = Integer.parseInt(sTempEstWeightNatural);
				}
				if (strEstWeight.contains(".")) {
					String sTempEstWeightPrime = strEstWeight.substring(strEstWeight.indexOf(".") + 1, strEstWeight.length());
					if (sTempEstWeightPrime == null || sTempEstWeightPrime.equals("") || sTempEstWeightPrime.length() == 0) {
						iEstWeightPrime = 0;
					} else {
						iEstWeightPrime = Integer.parseInt(sTempEstWeightPrime);
					}
				}
				
				if (iEstWeightNatural == 0 && iEstWeightPrime == 0) {
					bufMessage.append("Est. Weight '0' Value Is InValid ");
				}
			}

			if ("H".equals(strDrwStat))
			{
				Object value = attrMap.get("s7_SHOWN_PART_NO");
				if (value == null)
				{
					// 'Drw Status' 값이 'H'인 경우 'Shown On No.'를 입력하셔야 합니다.
					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Drw Status", "H", "Shown On No." }) + "\n");
				} else {
					if (value instanceof TCComponent) {
						String sShownNoPartDrawingSize = "";
						
						if (value instanceof TCComponentItem) {
							TCComponentItem itemShownOnPart = (TCComponentItem) value;
							TCComponentItemRevision revShownOnPart = itemShownOnPart.getLatestItemRevision();
							sShownNoPartDrawingSize = revShownOnPart.getProperty("s7_DRW_SIZE");
						} else {
							TCComponent cShownOnPart = (TCComponent) value;
							sShownNoPartDrawingSize = cShownOnPart.getProperty("s7_DRW_SIZE");
						}
						
						if (sShownNoPartDrawingSize == null || sShownNoPartDrawingSize.equals("") || sShownNoPartDrawingSize.length() == 0 || sShownNoPartDrawingSize.equals(".")) {
							bufMessage.append("Shown On Part has not Drawing Size.");
						}
					}
					else if (value instanceof String) {
						/**
						 * [20160712][ymjang] Shown On Part No Validation 체크
						 */
						String shown_on_no = null;
						if (value.toString().indexOf("-") == -1) {
							shown_on_no = value.toString();
						} else {
							shown_on_no =  value.toString().substring(0, value.toString().indexOf("-"));
						}
							
			        	TCComponentItem shown_on_item = SYMTcUtil.findItem(session, shown_on_no);
			        	if (shown_on_item == null) {
			        		bufMessage.append(shown_on_no + " Shown On Part does not exsits.");
			        	 }
			        }
					
				}
			}

			// validatorSkip = true 인 경우 validator 체크를 skip한다.
			if (!validatorSkip)
			{
				// Drw Status 값이 '.'인 경우 Drw Size/v4Type은 필수 입력
				if (".".equals(strDrwStat))
				{
					String strDrwSize = (String) attrMap.get("s7_DRW_SIZE");
					
					// [SR150521-050][2015.06.25][jclee] CAT V4 입력불가
//					String strCatV4Type = (String) attrMap.get("s7_CAT_V4_TYPE");

					if (CustomUtil.isEmpty(strDrwSize))
					{
						bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Drw Status", ".", "Dwg Size" }) + "\n");
					}

					// [SR150521-050][2015.06.25][jclee] CAT V4 입력불가
//					if (CustomUtil.isEmpty(strCatV4Type))
//					{
//						bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Drw Status", ".", "CAT. V4 Type" }) + "\n");
//					}
				}
			}

			String strDspNo = this.getDisplayNo(strOrign, strPartNo);
			if (strDspNo == null)
			{
				// "Part Origin 값이 '" + strOrign + "'인 경우 Part No.는 '" + this.partNoSizeMap.get(strOrign) + "'자로 입력하셔야 합니다.");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputLimitValue", new String[] { "Part Origin", strOrign, " Part No.", this.partNoSizeMap.get(strOrign) + "" })
						+ "\n");
			}
			else
			{
				attrMap.put("s7_DISPLAY_PART_NO", strDspNo.toUpperCase());
			}
		}

		// validatorSkip = true 인 경우 validator 체크를 skip한다.
		if (!validatorSkip)
		{
			if (!checkDoubleLimiting82Size(strMatThick))
			{
				// 재료 두께는 정수 8, 소수점 이하 2자리 까지 가능합니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "재료 두께", "8", "2" }) + "\n");
			}
			if (!checkDoubleLimiting82Size(strAltMatThick))
			{
				// 재료 두께 (Alter)는 정수 8, 소수점 이하 2자리 까지 가능합니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "재료 두께 (Alter)", "8", "2" }) + "\n");
			}
			if (!checkDoubleLimiting34Size(strEstWeight))
			{
				// bufMessage.append("예측중량-Kg는 정수 3, 소수점 이하 4자리 까지 가능합니다. \n");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "예측중량-Kg", "3", "4" }) + "\n");
			}
			if (!checkDoubleLimiting84Size(strCalWeight))
			{
				// 계산중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "계산중량-Kg", "8", "4" }) + "\n");
			}
			if (!checkDoubleLimiting84Size(strActWeight))
			{
				// 실중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "실중량-Kg", "8", "4" }) + "\n");
			}
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//[SR180724-011-09]
			// Tartget Weight Validation 설정
			if (!checkDoubleLimiting84Size(strTargetWeight))
			{
				// 실중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "목표중량-Kg", "8", "4" }) + "\n");
			}
			
			if( strRegulation != null && strRegulation.length() > 10)
			{
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputLimitValueUnder", new String[] { "Regulation 속성값", "10"}) + "\n");
			}
			
			if( strCritical != null && strCritical.length() > 10)
			{
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputLimitValueUnder", new String[] { "Critical 속성값", "10"}) + "\n");
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//			if (!checkDoubleLimiting84Size(strCalSurface))
			String sTempCalSurface = checkDoubleLimiting510Size(strCalSurface);
			if (RETURN_VALUE_ERROR.equals(sTempCalSurface))
			{
				// 계산 표면적-M2는 정수 5, 소수점 이하 10자리 까지 가능합니다.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "계산 표면적-M2", "5", "10" }) + "\n");
			}else
			{
				//System.out.println("## reset value ="+ sTempCalSurface);
				// reset value.
				attrMap.put("s7_CAL_SURFACE", sTempCalSurface);
			}
		}
		
		/**
		 * [SR141126-021][2014.11.27][jclee] Category 필수 입력 란으로 변경.
		 */
		String strCategory = (String) attrMap.get("s7_REGULATION");
//		String strDRCheckFlag = (String) attrMap.get("DR_CHECK_FLAG");
		if (CustomUtil.isEmpty(strCategory))
		{
			// 'Category'에 반드시 값을 선택해야 합니다.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Category" }) + "\n");
		}
		
//		if (!CustomUtil.isEmpty(strDRCheckFlag) && "Y".equals(strDRCheckFlag) && CustomUtil.isEmpty(strCategory))
//		{
//			bufMessage.append(StringUtil.getString(registry, "해당 Part Name에 DR Name Master에 존재합니다. Category 값을 입력해 주세요.", new String[] { "계산 표면적-M2", "8", "4" }) + "\n");
//		}
		
		// [SR140324-030][20140620] KOG DEV DEV Veh. Part SES Spec No. 에 대한 Validation 추가.
		if (strSESSpecNo != null && strSESSpecNo.getBytes().length > 500) {
		    bufMessage.append("SES Spec No. 는 500 (byte) 이하로 입력해야 합니다.");
		}
		
		// [SR없음][2015.06.16][jclee] Finish 자리수 Validate
		if (strFinish != null && strFinish.getBytes().length > 46) {
			bufMessage.append("Finish. 는 46 (byte) 이하로 입력해야 합니다.");
		}

		if (strMainName != null && !strMainName.equals("")) {
			// [SR150330-040][2015.03.30][jclee] Main Name이 BIP COMPL, BIW COMPL이 아니면서 System Code를 000으로 선택한 경우에 대한 Validation 추가
			// [NoSR][20160329][jclee] Sub Name이 존재하지 않는 경우에만 System Code를 000으로 넣도록 로직 변경. Sub Name이 존재할 경우에는 System Code가 000이 되서는 안됨.
			if (!((strMainName.equals("BIP COMPL") || strMainName.equals("BIW COMPL")) && (strSubName == null || strSubName.equals("") || strSubName.length() == 0))) {
				if (strSystemCode.equals("000")) {
					// System Code '000'은 "BIP COMPL", "BIW COMPL"에만 선택할 수 있습니다.
					bufMessage.append("System Code '000'은 'BIP COMPL', 'BIW COMPL'에만 선택할 수 있습니다.\n");
				}
			} else {
				if (strSubName == null || strSubName.equals("") || strSubName.length() == 0) {
					if (!strSystemCode.equals("000")) {
						// "BIP COMPL", "BIW COMPL"은 System Code '000'만 선택 가능
						bufMessage.append("System Code '000'을 선택해주십시요.\n");
					}
				}
			}
		}
		
		// [2018.11.20][CSH][SR181119-056] VehPart 생성 시 Main Name이 LOV와 다른경우 오류발생 후 재 로그인 전까지는 Item 생성이 안되는 현상 보완
		if (!(strMainName == null || strMainName.equals("") || strMainName.length() == 0)) {
			TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
	        TCComponentListOfValues[] listofvalues = listofvaluestype.find("S7_MAIN_NAME");
	        TCComponentListOfValues listofvalue = listofvalues[0];
	        List<LovValue> lstMainNames = listofvalue.getListOfValues().getValues();
	        boolean isContainMainName = false;
			for (int inx = 0; inx < lstMainNames.size(); inx++) {
//				System.out.println(listofvalue.getListOfValues().getValues().get(inx).getValue());
				if (listofvalue.getListOfValues().getValues().get(inx).getValue().equals(strMainName)) {
					isContainMainName = true;
					break;
				}
			}
			
			if (!isContainMainName) {
				bufMessage.append("Part Name을 다시 선택하여주십시오." + strMainName + "은 Main Name LOV에서 삭제되거나 변경되었습니다.\n");
			}
		}
		
		// [2018.11.20][CSH][SR181119-056] VehPart 생성 시 Sub Name이 LOV와 다른경우 오류발생 후 재 로그인 전까지는 Item 생성이 안되는 현상 보완
		if (!(strSubName == null || strSubName.equals("") || strSubName.length() == 0)) {
			TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
	        TCComponentListOfValues[] listofvalues = listofvaluestype.find("S7_SUBNAME");
	        TCComponentListOfValues listofvalue = listofvalues[0];
	        List<LovValue> lstSubNames = listofvalue.getListOfValues().getValues();
	        boolean isContainSubName = false;
			for (int inx = 0; inx < lstSubNames.size(); inx++) {
//				System.out.println(listofvalue.getListOfValues().getValues().get(inx).getValue());
				if (listofvalue.getListOfValues().getValues().get(inx).getValue().equals(strSubName)) {
					isContainSubName = true;
					break;
				}
			}
			
			if (!isContainSubName) {
				bufMessage.append("Part Name을 다시 선택하여주십시오." +strSubName + "은 Sub Name LOV에서 삭제되거나 변경되었습니다.\n");
			}
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
	 * Double 정수 8자리 이하 소수점 2자리 이하
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 8.
	 * @param text
	 * @return
	 */
	private boolean checkDoubleLimiting82Size(String text)
	{
		if (CustomUtil.isEmpty(text))
		{
			return true;
		}
		if (text.contains("."))
		{
			String first = text.substring(0, text.lastIndexOf("."));
			String second = text.substring(text.lastIndexOf(".") + 1, text.length());
			if (first.length() > 8 || second.length() > 2)
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
	 * Double 정수 3자리 이하 소수점 4자리 이하
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 8.
	 * @param text
	 * @return
	 */
	private boolean checkDoubleLimiting34Size(String text)
	{
		if (CustomUtil.isEmpty(text))
		{
			return true;
		}
		if (text.contains("."))
		{
			String first = text.substring(0, text.lastIndexOf("."));
			String second = text.substring(text.lastIndexOf(".") + 1, text.length());
			if (first.length() > 3 || second.length() > 4)
			{
				return false;
			}
		}
		else
		{
			if (text.length() > 3)
			{
				return false;
			}
		}
		return true;
	}


	/**
	 * CalSurface 용
	 * Double 정수 5자리 이하 소수점 10자리 이하
	 * 소수점 10자리 이하 절삭. (From 송대영C, 20130617)
	 * 
	 * @Copyright : plm
	 * @author : bskwak
	 * @since : 2013. 6. 17.
	 * @param text
	 * @return
	 */
	private String checkDoubleLimiting510Size(String text)
	{
		if (CustomUtil.isEmpty(text))
		{
			return text;
		}
		if (text.contains("."))
		{
			String first = text.substring(0, text.lastIndexOf("."));
			String second = text.substring(text.lastIndexOf(".") + 1, text.length());
			if (first.length() > 5)
			{
				return RETURN_VALUE_ERROR;
			}
			
			// 10자리 이하 절삭. 
			if (second.length() > 10)
			{
//				second = second.substring(0, 10);
				text = new StringBuilder(first).append('.').append(second.substring(0, 10)).toString();
				System.out.println("substring===>"+ text);
			}
		}
		else
		{
			if (text.length() > 16)
			{
				return RETURN_VALUE_ERROR;
			}
		}
		return text;
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
