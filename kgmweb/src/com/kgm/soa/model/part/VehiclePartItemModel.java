package com.kgm.soa.model.part;

import java.util.HashMap;

import com.kgm.common.util.StringUtil;
import com.kgm.soa.biz.TcItemUtil;
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
        
        // DB ������ �ʱ�ȭ
        if(attrMap.containsKey("s7_DRW_STAT")) { // VEHPART - S7_DRW_STAT �Ӽ��� �ʱ�ȭ            
            String drwStat = (String)attrMap.get("s7_DRW_STAT");
            if("".equals(drwStat)) {
                drwStat = "."; 
            } else {
                // ���ڸ� 1���� �����ͷ� ���� (H : Shown On -> H)
                if(drwStat.length() > 1) {
                    drwStat = drwStat.substring(0, 1);
                }
            }
            attrMap.put("s7_DRW_STAT", drwStat);
        } else {
            attrMap.put("s7_DRW_STAT", ".");
        }
        if(attrMap.containsKey("s7_RESPONSIBILITY")) { // MIG_VEHPART - S7_RESPONSIBILITY �Ӽ��� �ʱ�ȭ
            String responsibility = (String)attrMap.get("s7_RESPONSIBILITY");
            if(".".equals(responsibility)) {
                responsibility = ""; 
            }
            attrMap.put("s7_RESPONSIBILITY", responsibility);
        } else {
            attrMap.put("s7_RESPONSIBILITY", "");
        }        
        if(attrMap.containsKey("s7_UNIT")) { // VEHPART - S7_UNIT �Ӽ��� �ʱ�ȭ - Item �Ӽ� uom�� �߰� 
            String s7Unit = (String)attrMap.get("s7_UNIT");
            if("".equals(unit)) {
                s7Unit = "EA";
            }
            unit = s7Unit;
        } else {
            unit = "EA";
            
        }
        attrMap.put("s7_STAGE", "P"); // MIG_VEHPART - S7_STAGE �Ӽ��� �ʱ�ȭ ("P")
        attrMap.put("s7_REGULAR_PART", "R"); // MIG_VEHPART - S7_REGULAR_PART �Ӽ��� �ʱ�ȭ ("R")
    }
    
    /**
     * Item Property �Ӽ��� ���� ��Ų��.
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
            // bufMessage.append("'Part No.'�� �ʼ��Է� �����Դϴ�. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part No." }));
        }

        if (StringUtil.isEmpty(strPartName)) {
            // bufMessage.append("'Part Name'�� �ʼ��Է� �����Դϴ�. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part Name" }));
        }
        if (StringUtil.isEmpty(strProjectCode)) {
            // bufMessage.append("'Project Code'�� �ʼ��Է� �����Դϴ�. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Project Code" }));
        }
        if (StringUtil.isEmpty(strStage)) {
            // bufMessage.append("'Part Stage'�� �ݵ�� ���� �����ؾ� �մϴ�. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part Stage" }));
        }
        if (StringUtil.isEmpty(strRegular)) {
            // bufMessage.append("'Part Stage'�� �ݵ�� ���� �����ؾ� �մϴ�. \n");
            throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Regular" }));
        }
        if ("R".equals(strRegular)) {
            if (StringUtil.isEmpty(strPartKorName)) {
                // bufMessage.append("'Part Kor Name'�� �ʼ��Է� �����Դϴ�. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Part Kor Name" }));
            }

            if (StringUtil.isEmpty(strUnit)) {
                // bufMessage.append("'Unit'�� �ʼ��Է� �����Դϴ�. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Unit" }));
            }
            if (StringUtil.isEmpty(strSysCode)) {
                // bufMessage.append("'System Code'�� �ʼ��Է� �����Դϴ�. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "System Code" }));
            }
            if (StringUtil.isEmpty(strDrwStat)) {
                // bufMessage.append("'Drw Status'�� �ʼ��Է� �����Դϴ�. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Drw Status" }));
            }
            if (StringUtil.isEmpty(strColorID)) {
                // bufMessage.append("'Color ID'�� �ʼ��Է� �����Դϴ�. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Color ID" }));
            }
            if (StringUtil.isEmpty(strResponsibility)) {
                // bufMessage.append("'Responsibility'�� �ʼ��Է� �����Դϴ�. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Responsibility" }));
            }
            if (StringUtil.isEmpty(strEstWeight)) {
                // bufMessage.append("'Est. Weight'�� �ʼ��Է� �����Դϴ�. \n");
                throw new Exception(getStringMsg(REQUIRED_INPUT_VALUE, new String[] { "Est. Weight" }));
            }
            // �̹� DB���� �������Ƿ� ���� ���ʿ� - ��� �ڸ��� üũ�� �ؾ��Ѵ�.
            String strDspNo = this.getDisplayNo(strOrign, strPartNo);
            if (strDspNo == null) {
                // bufMessage.append("Part Origin ���� '" + strOrign +
                // "'�� ��� Part No.�� '" + this.partNoSizeMap.get(strOrign) +
                // "'�ڷ� �Է��ϼž� �մϴ�.");
                throw new Exception(getStringMsg(CASE_INPUT_LIMIT_VALUE, new String[] { "Part Origin", strOrign, " Part No.", this.partNoSizeMap.get(strOrign) + "" }));
            }            
            //attrMap.put("s7_DISPLAY_PART_NO", strDspNo); 
        }
        if (!checkDoubleLimiting82Size(strMatThick)) {
            // bufMessage.append("��� �β��� ���� 8, �Ҽ��� ���� 2�ڸ� ���� �����մϴ�. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "��� �β�", "8", "2" }));
        }
        if (!checkDoubleLimiting82Size(strAltMatThick)) {
            // bufMessage.append("��� �β� (Alter)�� ���� 8, �Ҽ��� ���� 2�ڸ� ���� �����մϴ�. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "��� �β� (Alter)", "8", "2" }));
        }
        // [20131231] ����/���/�� �߷� �ڸ��� ���� 8, �Ҽ��� 10�ڸ��� ����. (From ������C)
        if (!checkDoubleLimiting(8, 10, strEstWeight)) {
            // bufMessage.append("�����߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "�����߷�-Kg", "8", "10" }));
        }
        if (!checkDoubleLimiting(8, 10, strCalWeight)) {
            // bufMessage.append("����߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "����߷�-Kg", "8", "10" }));
        }
        if (!checkDoubleLimiting(8, 10, strActWeight)) {
            // bufMessage.append("���߷�-Kg�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�. \n");
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "���߷�-Kg", "8", "10" }));
        }
        // ǥ���� �ڸ��� ���� 5, 10 �ڸ��� ���� (20130617, �۴뿵C)
//        if (!checkDoubleLimiting84Size(strCalSurface)) {
//            // bufMessage.append("��� ǥ����-M2�� ���� 8, �Ҽ��� ���� 4�ڸ� ���� �����մϴ�. \n");
//            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "��� ǥ����-M2", "8", "4" }));
//        }
		String sTempCalSurface = checkDoubleLimiting510Size(strCalSurface);
		if (RETURN_VALUE_ERROR.equals(sTempCalSurface))
		{
			// ��� ǥ����-M2�� ���� 5, �Ҽ��� ���� 10�ڸ� ���� �����մϴ�.
            throw new Exception(getStringMsg(LIMITED_VALUE, new String[] { "��� ǥ����-M2", "5", "10" }));
		}else
		{
			// reset value.
			attrMap.put("s7_CAL_SURFACE", sTempCalSurface);
		}

        // Shown On Part �ʱ�ȭ
        if ("H".equals(strDrwStat)) {            
            if ("".equals(attrMap.get("s7_SHOWN_PART_NO"))) {
                // bufMessage.append("'Drw Status' ���� 'H'�� ��� 'Shown On No.'�� �Է��ϼž� �մϴ�. \n");
                throw new Exception(getStringMsg(CASE_INPUT_VALUE, new String[] { "Drw Status", "H", "Shown On No." }));
            } else {
                attrMap.put("s7_SHOWN_PART_NO", this.getShownPartItemPuid((String)attrMap.get("s7_SHOWN_PART_NO")));
            }
        }
        
        // Matrial Revision �ʱ�ȭ
        if (!StringUtil.isEmpty((String)attrMap.get("s7_MATERIAL"))) {            
            attrMap.put("s7_MATERIAL", this.getMaterialItemRevPuid((String)attrMap.get("s7_MATERIAL")));
        }
        if (!StringUtil.isEmpty((String)attrMap.get("s7_ALT_MATERIAL"))) {
            attrMap.put("s7_ALT_MATERIAL", this.getMaterialItemRevPuid((String)attrMap.get("s7_ALT_MATERIAL")));
        }     
       
    }
    
    /**
     * Material Item Revision Puid ��ȸ     
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
            throw new Exception(itemId + " : Material Item �� �������� �ʽ��ϴ�.");
        }
        ModelObject[] itemRevisions = item.get_revision_list();
        ModelObject latestRevision = itemRevisions[itemRevisions.length -1];
        return latestRevision.getUid();
    }
    
    /**
     * Shown On Part Item Puid ��ȸ
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
            throw new Exception(itemId + " : Shown Part Item �� �������� �ʽ��ϴ�.");
        }
        return item.getUid();
    }

    /**
     * 
     * Part Orign���� ���� Display No.�� �����մϴ�.
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
     * Double ���� 8�ڸ� ���� �Ҽ��� 2�ڸ� ����
     * 
     * @Copyright : S-PALM
     * @author : �ǿ���
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
     * Double ���� 8�ڸ� ���� �Ҽ��� 4�ڸ� ����
     * 
     * @Copyright : S-PALM
     * @author : �ǿ���
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
     * ����, �Ҽ� �ڸ��� �˻�. ������ ����, �Ҽ� �ڸ����� �ʰ� �ϴ��� �˻��Ͽ� boolean���� ��ȯ. 
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
			
			// 10�ڸ� ���� ����. 
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
