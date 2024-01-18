package com.ssangyong.soa.model.part;

import java.util.HashMap;

import com.ssangyong.common.util.StringUtil;
import com.ssangyong.soa.biz.TcItemUtil;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Item;

public class VehiclePartItemModel extends PartModel {
    
    TcItemUtil tcItemUtil;
    HashMap<String, Integer> partNoSizeMap;
	
	private final String RETURN_VALUE_ERROR = "~{ERROR}";

    public VehiclePartItemModel(HashMap<String, Object> param) {        
        super(param);
        tcItemUtil = new TcItemUtil(session);
    }

    @Override
    public HashMap<String, Object> getRevProperties() throws Exception {
        setDspPartNoMap();
        setData();
        validator();
        removeItemAttr();
        return attrMap;
    }

    private void setData() {
        attrMap = new HashMap<String, Object>();
        itemId = (String)param.get("ITEM_ID");
        itemRevId = (String)param.get("REVISION_ID");
        itemName = (String)param.get("NAME");        
        attrMap.put("s7_PART_TYPE", param.get("S7_PART_TYPE"));
        attrMap.put("s7_PROJECT_CODE", param.get("S7_PROJECT_CODE"));
        attrMap.put("s7_STAGE", param.get("S7_STAGE"));
        attrMap.put("s7_REGULAR_PART", param.get("S7_REGULAR_PART"));        
        attrMap.put("s7_KOR_NAME", param.get("S7_KOR_NAME"));        
        attrMap.put("s7_BUDGET_CODE", param.get("S7_SYSTEM_CODE"));
        attrMap.put("s7_DRW_STAT", param.get("S7_DRW_STAT"));
        attrMap.put("s7_COLOR", param.get("S7_COLOR"));
        attrMap.put("s7_RESPONSIBILITY", param.get("S7_RESPONSIBILITY"));
        attrMap.put("s7_EST_WEIGHT", param.get("S7_EST_WEIGHT"));
        attrMap.put("s7_THICKNESS", param.get("S7_THICKNESS"));
        attrMap.put("s7_ALT_THICKNESS", param.get("S7_ALT_THICKNESS"));
        attrMap.put("s7_CAL_WEIGHT", param.get("S7_CAL_WEIGHT"));
        attrMap.put("s7_ACT_WEIGHT", param.get("S7_ACT_WEIGHT"));
        attrMap.put("s7_CAL_SURFACE", param.get("S7_CAL_SURFACE"));                
        attrMap.put("s7_DISPLAY_PART_NO", param.get("S7_DISPLAY_PART_NO"));        
        attrMap.put("s7_SHOWN_PART_NO", param.get("S7_SHOW_PART_NO"));
        attrMap.put("s7_DRW_SIZE", param.get("S7_DRW_SIZE"));
        attrMap.put("s7_REFERENCE", param.get("S7_REFERENCE"));        
        attrMap.put("s7_REGULATION", param.get("S7_REGULATION"));
        attrMap.put("s7_COLOR_ID", param.get("S7_COLOR_ID"));
        attrMap.put("s7_MATERIAL", param.get("S7_MATTERIAL"));
        attrMap.put("s7_ALT_MATERIAL", param.get("S7_ALT_MATERIAL"));        
        attrMap.put("s7_FINISH", param.get("S7_FINISH"));
        attrMap.put("s7_BOUNDINGBOX", param.get("S7_BOUNDINGBOX"));
        attrMap.put("s7_AS_END_ITEM", param.get("S7_AS_END_ITEM"));
        attrMap.put("s7_DVP_RESULT", param.get("S7_DVP_RESULT"));
        attrMap.put("s7_CHANGE_DESCRIPTION", param.get("S7_CHANGE_DESCRIPTION"));
        attrMap.put("s7_CAT_V4_TYPE", param.get("S7_CAT_V4_TYPE"));
        attrMap.put("object_desc", param.get("DESCRIPTION"));
        attrMap.put("s7_VPM_ECO_NO", param.get("S7_ECO_NO"));
        
        // DB 데이터 초기화
        if(attrMap.containsKey("s7_DRW_STAT")) { // VEHPART - S7_DRW_STAT 속성값 초기화            
            String drwStat = (String)attrMap.get("s7_DRW_STAT");
            if("".equals(drwStat)) {
                drwStat = "."; 
            } else {
                // 앞자리 1개만 데이터로 인정 (H : Shown On -> H)
                if(drwStat.length() > 1) {
                    drwStat = drwStat.substring(0, 1);
                }
            }
            attrMap.put("s7_DRW_STAT", drwStat);
        } else {
            attrMap.put("s7_DRW_STAT", ".");
        }
        if(attrMap.containsKey("s7_RESPONSIBILITY")) { // MIG_VEHPART - S7_RESPONSIBILITY 속성값 초기화
            String responsibility = (String)attrMap.get("s7_RESPONSIBILITY");
            if(".".equals(responsibility)) {
                responsibility = ""; 
            }
            attrMap.put("s7_RESPONSIBILITY", responsibility);
        } else {
            attrMap.put("s7_RESPONSIBILITY", "");
        }        
        if(attrMap.containsKey("s7_UNIT")) { // VEHPART - S7_UNIT 속성값 초기화 - Item 속성 uom에 추가 
            String s7Unit = (String)attrMap.get("s7_UNIT");
            if("".equals(unit)) {
                s7Unit = "EA";
            }
            unit = s7Unit;
        } else {
            unit = "EA";
            
        }
        attrMap.put("s7_STAGE", "P"); // MIG_VEHPART - S7_STAGE 속성값 초기화 ("P")
        attrMap.put("s7_REGULAR_PART", "R"); // MIG_VEHPART - S7_REGULAR_PART 속성값 초기화 ("R")
    }
    
    /**
     * Item Property 속성은 제외 시킨다.
     * 
     * @method removeItemAttr 
     * @date 2013. 4. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void removeItemAttr() {
        attrMap.remove("item_id");
        attrMap.remove("item_revision_id");        
        attrMap.remove("object_name");
    }

    private void setDspPartNoMap() {
        this.partNoSizeMap = new HashMap<String, Integer>();
        this.partNoSizeMap.put("K", 10);
        this.partNoSizeMap.put("D", 10);
        this.partNoSizeMap.put("A", 10);
        this.partNoSizeMap.put("B", 10);
        this.partNoSizeMap.put("N", 12);
        this.partNoSizeMap.put("S", 7);
        this.partNoSizeMap.put("G", 8);
    }

    @Override
    public void validator() throws Exception {
        String strPartNo = itemId;
        String strRevNo = itemRevId;
        String strPartName = itemName;
        String strOrign = (String) attrMap.get("s7_PART_TYPE");
        String strProjectCode = (String) attrMap.get("s7_PROJECT_CODE");
        String strStage = (String) attrMap.get("s7_STAGE");
        String strRegular = (String) attrMap.get("s7_REGULAR_PART");
        String strPartKorName = (String) attrMap.get("s7_KOR_NAME");
        String strUnit = unit;
        String strSysCode = (String) attrMap.get("s7_BUDGET_CODE");
        String strDrwStat = (String) attrMap.get("s7_DRW_STAT");
        String strColorID = (String) attrMap.get("s7_COLOR");
        String strResponsibility = (String) attrMap.get("s7_RESPONSIBILITY");
        String strEstWeight = (String) attrMap.get("s7_EST_WEIGHT");
        String strMatThick = (String) attrMap.get("s7_THICKNESS");
        String strAltMatThick = (String) attrMap.get("s7_ALT_THICKNESS");
        String strCalWeight = (String) attrMap.get("s7_CAL_WEIGHT");
        String strActWeight = (String) attrMap.get("s7_ACT_WEIGHT");
        String strCalSurface = (String) attrMap.get("s7_CAL_SURFACE");
             
        if (StringUtil.isEmpty(strOrign)) {
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part Origin" }));
        }

        if (StringUtil.isEmpty(strPartNo) || StringUtil.isEmpty(strRevNo)) {
            // bufMessage.append("'Part No.'는 필수입력 사항입니다. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part No." }));
        }

        if (StringUtil.isEmpty(strPartName)) {
            // bufMessage.append("'Part Name'은 필수입력 사항입니다. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part Name" }));
        }
        if (StringUtil.isEmpty(strProjectCode)) {
            // bufMessage.append("'Project Code'는 필수입력 사항입니다. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Project Code" }));
        }
        if (StringUtil.isEmpty(strStage)) {
            // bufMessage.append("'Part Stage'에 반드시 값을 선택해야 합니다. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part Stage" }));
        }
        if (StringUtil.isEmpty(strRegular)) {
            // bufMessage.append("'Part Stage'에 반드시 값을 선택해야 합니다. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Regular" }));
        }
        if ("R".equals(strRegular)) {
            if (StringUtil.isEmpty(strPartKorName)) {
                // bufMessage.append("'Part Kor Name'은 필수입력 사항입니다. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part Kor Name" }));
            }

            if (StringUtil.isEmpty(strUnit)) {
                // bufMessage.append("'Unit'은 필수입력 사항입니다. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Unit" }));
            }
            if (StringUtil.isEmpty(strSysCode)) {
                // bufMessage.append("'System Code'는 필수입력 사항입니다. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "System Code" }));
            }
            if (StringUtil.isEmpty(strDrwStat)) {
                // bufMessage.append("'Drw Status'는 필수입력 사항입니다. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Drw Status" }));
            }
            if (StringUtil.isEmpty(strColorID)) {
                // bufMessage.append("'Color ID'는 필수입력 사항입니다. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Color ID" }));
            }
            if (StringUtil.isEmpty(strResponsibility)) {
                // bufMessage.append("'Responsibility'는 필수입력 사항입니다. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Responsibility" }));
            }
            if (StringUtil.isEmpty(strEstWeight)) {
                // bufMessage.append("'Est. Weight'는 필수입력 사항입니다. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Est. Weight" }));
            }
            // 이미 DB에서 가져오므로 설정 불필요 - 대신 자릿수 체크는 해야한다.
            String strDspNo = this.getDisplayNo(strOrign, strPartNo);
            if (strDspNo == null) {
                // bufMessage.append("Part Origin 값이 '" + strOrign +
                // "'인 경우 Part No.는 '" + this.partNoSizeMap.get(strOrign) +
                // "'자로 입력하셔야 합니다.");
                throw new Exception(getStringMsg(CASE_INPUT_LIMIT_VALUE, new String[] { "Part Origin", strOrign, " Part No.", this.partNoSizeMap.get(strOrign) + "" }));
            }            
            //attrMap.put("s7_DISPLAY_PART_NO", strDspNo); 
        }
        if (!checkDoubleLimiting82Size(strMatThick)) {
            // bufMessage.append("재료 두께는 정수 8, 소수점 이하 2자리 까지 가능합니다. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "재료 두께", "8", "2" }));
        }
        if (!checkDoubleLimiting82Size(strAltMatThick)) {
            // bufMessage.append("재료 두께 (Alter)는 정수 8, 소수점 이하 2자리 까지 가능합니다. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "재료 두께 (Alter)", "8", "2" }));
        }
        // [20131231] 예측/계산/실 중량 자리수 정수 8, 소수점 10자리로 변경. (From 류강하C)
        if (!checkDoubleLimiting(8, 10, strEstWeight)) {
            // bufMessage.append("예측중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "예측중량-Kg", "8", "10" }));
        }
        if (!checkDoubleLimiting(8, 10, strCalWeight)) {
            // bufMessage.append("계산중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "계산중량-Kg", "8", "10" }));
        }
        if (!checkDoubleLimiting(8, 10, strActWeight)) {
            // bufMessage.append("실중량-Kg는 정수 8, 소수점 이하 4자리 까지 가능합니다. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "실중량-Kg", "8", "10" }));
        }
        // 표면적 자리수 제한 5, 10 자리로 변경 (20130617, 송대영C)
//        if (!checkDoubleLimiting84Size(strCalSurface)) {
//            // bufMessage.append("계산 표면적-M2는 정수 8, 소수점 이하 4자리 까지 가능합니다. \n");
//            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "계산 표면적-M2", "8", "4" }));
//        }
		String sTempCalSurface = checkDoubleLimiting510Size(strCalSurface);
		if (RETURN_VALUE_ERROR.equals(sTempCalSurface))
		{
			// 계산 표면적-M2는 정수 5, 소수점 이하 10자리 까지 가능합니다.
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "계산 표면적-M2", "5", "10" }));
		}else
		{
			// reset value.
			attrMap.put("s7_CAL_SURFACE", sTempCalSurface);
		}

        // Shown On Part 초기화
        if ("H".equals(strDrwStat)) {            
            if ("".equals(attrMap.get("s7_SHOWN_PART_NO"))) {
                // bufMessage.append("'Drw Status' 값이 'H'인 경우 'Shown On No.'를 입력하셔야 합니다. \n");
                throw new Exception(getStringMsg(CASE_INPUT_VALUE, new String[] { "Drw Status", "H", "Shown On No." }));
            } else {
                attrMap.put("s7_SHOWN_PART_NO", this.getShownPartItemPuid((String)attrMap.get("s7_SHOWN_PART_NO")));
            }
        }
        
        // Matrial Revision 초기화
        if (!StringUtil.isEmpty((String)attrMap.get("s7_MATERIAL"))) {            
            attrMap.put("s7_MATERIAL", this.getMaterialItemRevPuid((String)attrMap.get("s7_MATERIAL")));
        }
        if (!StringUtil.isEmpty((String)attrMap.get("s7_ALT_MATERIAL"))) {
            attrMap.put("s7_ALT_MATERIAL", this.getMaterialItemRevPuid((String)attrMap.get("s7_ALT_MATERIAL")));
        }     
       
    }
    
    /**
     * Material Item Revision Puid 조회     
     * 
     * @method getMaterialItemRevPuid 
     * @date 2013. 5. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getMaterialItemRevPuid(String itemId) throws Exception {
        Item item = tcItemUtil.getItem(itemId);
        if(item == null) {
            throw new Exception(itemId + " : Material Item 이 존재하지 않습니다.");
        }
        ModelObject[] itemRevisions = item.get_revision_list();
        ModelObject latestRevision = itemRevisions[itemRevisions.length -1];
        return latestRevision.getUid();
    }
    
    /**
     * Shown On Part Item Puid 조회
     * 
     * @method getShownPartItemPuid 
     * @date 2013. 5. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getShownPartItemPuid(String itemId) throws Exception {
        Item item = tcItemUtil.getItem(itemId);
        if(item == null) {
            throw new Exception(itemId + " : Shown Part Item 이 존재하지 않습니다.");
        }
        return item.getUid();
    }

    /**
     * 
     * Part Orign값에 따라 Display No.를 생성합니다.
     * 
     * ID Description Digit DB Display No.
     * ------------------------------------------------------------- K
     * SYMC-General 10 1234534000 12345 34000 D SYMC-Sub Material 10 X300115101
     * X3001 15101 G SYMC-Sequention 8 12345678 12345678 A MB-General 10
     * 1234560011 123 456 00 11 B MB-Sub Material 10 1029890471 102 989 04 71 N
     * MB-Standard 12 123456789000 123456 789000 S SYMC SPEC Part 7 D20A011
     * D20A011 -------------------------------------------------------------
     * 
     * @return Display No
     */
    public String getDisplayNo(String strOrign, String strPartNo) {
        if (this.partNoSizeMap.get(strOrign) == null) {
            return "";
        }
        if (strPartNo.length() != this.partNoSizeMap.get(strOrign)) {
            return null;
        }
        StringBuffer bufDspNo = new StringBuffer();
        if ("K".equals(strOrign)) {
            bufDspNo.append(strPartNo.substring(0, 5));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(5, strPartNo.length()));
        } else if ("D".equals(strOrign)) {
            bufDspNo.append(strPartNo.substring(0, 5));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(5, strPartNo.length()));
        } else if ("A".equals(strOrign)) {
            bufDspNo.append(strPartNo.substring(0, 3));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(3, 6));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(6, 8));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(8, strPartNo.length()));
        } else if ("B".equals(strOrign)) {
            bufDspNo.append(strPartNo.substring(0, 3));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(3, 6));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(6, 8));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(8, strPartNo.length()));
        } else if ("N".equals(strOrign)) {
            bufDspNo.append(strPartNo.substring(0, 6));
            bufDspNo.append(" ");
            bufDspNo.append(strPartNo.substring(6, strPartNo.length()));
        } else if ("S".equals(strOrign)) {
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
    private boolean checkDoubleLimiting82Size(String text) {
        if (StringUtil.isEmpty(text)) {
            return true;
        }
        if (text.contains(".")) {
            String first = text.substring(0, text.lastIndexOf("."));
            String second = text.substring(text.lastIndexOf(".") + 1, text.length());
            if (first.length() > 8 || second.length() > 2) {
                return false;
            }
        } else {
            if (text.length() > 8) {
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
    @SuppressWarnings("unused")
	private boolean checkDoubleLimiting84Size(String text) {
        if (StringUtil.isEmpty(text)) {
            return true;
        }
        if (text.contains(".")) {
            String first = text.substring(0, text.lastIndexOf("."));
            String second = text.substring(text.lastIndexOf(".") + 1, text.length());
            if (first.length() > 8 || second.length() > 4) {
                return false;
            }
        } else {
            if (text.length() > 8) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 정수, 소수 자리수 검사. 지정한 정수, 소수 자리수가 초과 하는지 검사하여 boolean으로 반환. 
     * @param iIntegerSize
     * @param iFloatSize
     * @param sDoubleStr
     * @return
     */
    private boolean checkDoubleLimiting(int iIntegerSize, int iFloatSize, String sDoubleStr) {
        if (StringUtil.isEmpty(sDoubleStr)) {
            return true;
        }
        if (sDoubleStr.contains(".")) {
            String first = sDoubleStr.substring(0, sDoubleStr.lastIndexOf("."));
            String second = sDoubleStr.substring(sDoubleStr.lastIndexOf(".") + 1, sDoubleStr.length());
            if (first.length() > iIntegerSize || second.length() > iFloatSize) {
                return false;
            }
        } else {
            if (sDoubleStr.length() > (iIntegerSize + iFloatSize + 1)) {
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
		if (StringUtil.isEmpty(text))
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
				text = new StringBuilder(first).append('.').append(second.substring(0, 10)).toString();
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
	

}
