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
 * [20160712][ymjang] Shown On Part No Validation üũ
 * [20161011][ymjang] Shown On Part No Validation üũ ���� ����
 */
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

 * [SR180130-033][LJG]
 * 1. E-BOM Part Master(Eng. Info) �� "Responsibility" => "DWG Creator" �� ����
   2. Responsibility Filed �� LOV �� �߰� : Supplier, Collaboration, SYMC
   3. �ű� part ���� �� ���� LOV Black BOX, Gray Box, White Box ���úҰ� ó��
   4. Revision Up �� ���� Responsibiliy �� ���� => ���� �������ϵ��� ó��
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
		// Target Weight �߰�
		String strTargetWeight = (String) attrMap.get("s7_TARGET_WEIGHT");
		String strRegulation = (String) attrMap.get("s7_R");
		String strCritical = (String) attrMap.get("s7_C");
		///////////////////////////////////////////////////////////////////////////////
		String strCalSurface = (String) attrMap.get("s7_CAL_SURFACE");
		String strMainName = (String) attrMap.get("s7_MAIN_NAME");
		String strSubName = (String) attrMap.get("s7_SUB_NAME");
		String strSystemCode = (String) attrMap.get("s7_BUDGET_CODE");
		
		//[SR140324-030][20140620] KOG DEV Veh. Part SES Spec No. �� ���� Validation �߰�.
		String strSESSpecNo = (String) attrMap.get("s7_SES_SPEC_NO");
		
		// [SR����][2015.06.16][jclee] Finish �ڸ��� Validate
		String strFinish = (String) attrMap.get("s7_FINISH");
		
		if( CustomUtil.isEmpty(strActWeight))
		{
			strActWeight = "0";
		}
		/////////////////////////////////////////////////////////////////////////////////////////
		// Target Weight Validation ����
		if( CustomUtil.isEmpty(strTargetWeight))
		{
			strTargetWeight = "0";
		}
		
		////////////////////////////////////////////////////////////////////////////////////////

		if (CustomUtil.isEmpty(strOrign))
		{
			// 'Part Origin'�� �ʼ��Է� �����Դϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Origin" }) + "\n");
		}

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
		if (CustomUtil.isEmpty(strRegular))
		{
			// 'Part Stage'�� �ݵ�� ���� �����ؾ� �մϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Regular" }) + "\n");
		}

		if ("R".equals(strRegular))
		{
			if (CustomUtil.isEmpty(strPartKorName))
			{
				// 'Part Kor Name'�� �ʼ��Է� �����Դϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Part Kor Name" }) + "\n");
			}

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
			if (CustomUtil.isEmpty(strDrwStat))
			{
				// 'Drw Status'�� �ʼ��Է� �����Դϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Drw Status" }) + "\n");
			}
			// validatorSkip = true �� ��� validator üũ�� skip�Ѵ�.
			if (!validatorSkip)
			{
				if (CustomUtil.isEmpty(strColorID))
				{
					// 'Color ID'�� �ʼ��Է� �����Դϴ�.
					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Color ID" }) + "\n");
				}

				if (CustomUtil.isEmpty(strResponsibility))
				{
					// 'Responsibility'�� �ʼ��Է� �����Դϴ�.
					//[SR180130-033][LJG] "Responsibility" => "DWG Creator" �� ����
					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "DWG Creator" }) + "\n");
				}
			}
			int iEstWeightNatural = 0;
			int iEstWeightPrime = 0;
			
			if (CustomUtil.isEmpty(strEstWeight))
			{				
				// 'Est. Weight'�� �ʼ��Է� �����Դϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Est. Weight" }) + "\n");
				// [20161011][ymjang] ���߷��� ���� ��츸, �����߷� �ʼ� üũ��.
//				if (strActWeight.equals("0")) {
//					// 'Est. Weight'�� �ʼ��Է� �����Դϴ�.
//					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Est. Weight" }) + "\n");
//				}
			}
			else if ("0".equals(strEstWeight))
			{
				bufMessage.append("Est. Weight '0' Value Is InValid ");				
				// [20161011][ymjang] ���߷��� ���� ��츸, �����߷� �ʼ� üũ��.
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
					// 'Drw Status' ���� 'H'�� ��� 'Shown On No.'�� �Է��ϼž� �մϴ�.
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
						 * [20160712][ymjang] Shown On Part No Validation üũ
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

			// validatorSkip = true �� ��� validator üũ�� skip�Ѵ�.
			if (!validatorSkip)
			{
				// Drw Status ���� '.'�� ��� Drw Size/v4Type�� �ʼ� �Է�
				if (".".equals(strDrwStat))
				{
					String strDrwSize = (String) attrMap.get("s7_DRW_SIZE");
					
					// [SR150521-050][2015.06.25][jclee] CAT V4 �ԷºҰ�
//					String strCatV4Type = (String) attrMap.get("s7_CAT_V4_TYPE");

					if (CustomUtil.isEmpty(strDrwSize))
					{
						bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Drw Status", ".", "Dwg Size" }) + "\n");
					}

					// [SR150521-050][2015.06.25][jclee] CAT V4 �ԷºҰ�
//					if (CustomUtil.isEmpty(strCatV4Type))
//					{
//						bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputValue", new String[] { "Drw Status", ".", "CAT. V4 Type" }) + "\n");
//					}
				}
			}

			String strDspNo = this.getDisplayNo(strOrign, strPartNo);
			if (strDspNo == null)
			{
				// "Part Origin ���� '" + strOrign + "'�� ��� Part No.�� '" + this.partNoSizeMap.get(strOrign) + "'�ڷ� �Է��ϼž� �մϴ�.");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.caseInputLimitValue", new String[] { "Part Origin", strOrign, " Part No.", this.partNoSizeMap.get(strOrign) + "" })
						+ "\n");
			}
			else
			{
				attrMap.put("s7_DISPLAY_PART_NO", strDspNo.toUpperCase());
			}
		}

		// validatorSkip = true �� ��� validator üũ�� skip�Ѵ�.
		if (!validatorSkip)
		{
			if (!checkDoubleLimiting82Size(strMatThick))
			{
				// ��� �β��� ���� 8, �Ҽ��� ���� 2�ڸ� ���� �����մϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "��� �β�", "8", "2" }) + "\n");
			}
			if (!checkDoubleLimiting82Size(strAltMatThick))
			{
				// ��� �β� (Alter)�� ���� 8, �Ҽ��� ���� 2�ڸ� ���� �����մϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "��� �β� (Alter)", "8", "2" }) + "\n");
			}
			if (!checkDoubleLimiting34Size(strEstWeight))
			{
				// bufMessage.append("�����߷�-Kg�� ���� 3, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�. \n");
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "�����߷�-Kg", "3", "4" }) + "\n");
			}
			if (!checkDoubleLimiting84Size(strCalWeight))
			{
				// ����߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "����߷�-Kg", "8", "4" }) + "\n");
			}
			if (!checkDoubleLimiting84Size(strActWeight))
			{
				// ���߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "���߷�-Kg", "8", "4" }) + "\n");
			}
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//[SR180724-011-09]
			// Tartget Weight Validation ����
			if (!checkDoubleLimiting84Size(strTargetWeight))
			{
				// ���߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "��ǥ�߷�-Kg", "8", "4" }) + "\n");
			}
			
			if( strRegulation != null && strRegulation.length() > 10)
			{
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputLimitValueUnder", new String[] { "Regulation �Ӽ���", "10"}) + "\n");
			}
			
			if( strCritical != null && strCritical.length() > 10)
			{
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputLimitValueUnder", new String[] { "Critical �Ӽ���", "10"}) + "\n");
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//			if (!checkDoubleLimiting84Size(strCalSurface))
			String sTempCalSurface = checkDoubleLimiting510Size(strCalSurface);
			if (RETURN_VALUE_ERROR.equals(sTempCalSurface))
			{
				// ��� ǥ����-M2�� ���� 5, �Ҽ��� ���� 10�ڸ� ���� �����մϴ�.
				bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.limitedValue", new String[] { "��� ǥ����-M2", "5", "10" }) + "\n");
			}else
			{
				//System.out.println("## reset value ="+ sTempCalSurface);
				// reset value.
				attrMap.put("s7_CAL_SURFACE", sTempCalSurface);
			}
		}
		
		/**
		 * [SR141126-021][2014.11.27][jclee] Category �ʼ� �Է� ������ ����.
		 */
		String strCategory = (String) attrMap.get("s7_REGULATION");
//		String strDRCheckFlag = (String) attrMap.get("DR_CHECK_FLAG");
		if (CustomUtil.isEmpty(strCategory))
		{
			// 'Category'�� �ݵ�� ���� �����ؾ� �մϴ�.
			bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.requiredInputValue", new String[] { "Category" }) + "\n");
		}
		
//		if (!CustomUtil.isEmpty(strDRCheckFlag) && "Y".equals(strDRCheckFlag) && CustomUtil.isEmpty(strCategory))
//		{
//			bufMessage.append(StringUtil.getString(registry, "�ش� Part Name�� DR Name Master�� �����մϴ�. Category ���� �Է��� �ּ���.", new String[] { "��� ǥ����-M2", "8", "4" }) + "\n");
//		}
		
		// [SR140324-030][20140620] KOG DEV DEV Veh. Part SES Spec No. �� ���� Validation �߰�.
		if (strSESSpecNo != null && strSESSpecNo.getBytes().length > 500) {
		    bufMessage.append("SES Spec No. �� 500 (byte) ���Ϸ� �Է��ؾ� �մϴ�.");
		}
		
		// [SR����][2015.06.16][jclee] Finish �ڸ��� Validate
		if (strFinish != null && strFinish.getBytes().length > 46) {
			bufMessage.append("Finish. �� 46 (byte) ���Ϸ� �Է��ؾ� �մϴ�.");
		}

		if (strMainName != null && !strMainName.equals("")) {
			// [SR150330-040][2015.03.30][jclee] Main Name�� BIP COMPL, BIW COMPL�� �ƴϸ鼭 System Code�� 000���� ������ ��쿡 ���� Validation �߰�
			// [NoSR][20160329][jclee] Sub Name�� �������� �ʴ� ��쿡�� System Code�� 000���� �ֵ��� ���� ����. Sub Name�� ������ ��쿡�� System Code�� 000�� �Ǽ��� �ȵ�.
			if (!((strMainName.equals("BIP COMPL") || strMainName.equals("BIW COMPL")) && (strSubName == null || strSubName.equals("") || strSubName.length() == 0))) {
				if (strSystemCode.equals("000")) {
					// System Code '000'�� "BIP COMPL", "BIW COMPL"���� ������ �� �ֽ��ϴ�.
					bufMessage.append("System Code '000'�� 'BIP COMPL', 'BIW COMPL'���� ������ �� �ֽ��ϴ�.\n");
				}
			} else {
				if (strSubName == null || strSubName.equals("") || strSubName.length() == 0) {
					if (!strSystemCode.equals("000")) {
						// "BIP COMPL", "BIW COMPL"�� System Code '000'�� ���� ����
						bufMessage.append("System Code '000'�� �������ֽʽÿ�.\n");
					}
				}
			}
		}
		
		// [2018.11.20][CSH][SR181119-056] VehPart ���� �� Main Name�� LOV�� �ٸ���� �����߻� �� �� �α��� �������� Item ������ �ȵǴ� ���� ����
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
				bufMessage.append("Part Name�� �ٽ� �����Ͽ��ֽʽÿ�." + strMainName + "�� Main Name LOV���� �����ǰų� ����Ǿ����ϴ�.\n");
			}
		}
		
		// [2018.11.20][CSH][SR181119-056] VehPart ���� �� Sub Name�� LOV�� �ٸ���� �����߻� �� �� �α��� �������� Item ������ �ȵǴ� ���� ����
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
				bufMessage.append("Part Name�� �ٽ� �����Ͽ��ֽʽÿ�." +strSubName + "�� Sub Name LOV���� �����ǰų� ����Ǿ����ϴ�.\n");
			}
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
	 * Double ���� 8�ڸ� ���� �Ҽ��� 2�ڸ� ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
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
	 * Double ���� 3�ڸ� ���� �Ҽ��� 4�ڸ� ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
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
	 * CalSurface ��
	 * Double ���� 5�ڸ� ���� �Ҽ��� 10�ڸ� ����
	 * �Ҽ��� 10�ڸ� ���� ����. (From �۴뿵C, 20130617)
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
			
			// 10�ڸ� ���� ����. 
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
