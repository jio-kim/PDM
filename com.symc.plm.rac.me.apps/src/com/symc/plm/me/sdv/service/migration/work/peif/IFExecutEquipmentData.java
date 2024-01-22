package com.symc.plm.me.sdv.service.migration.work.peif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.RawDataMap;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.exception.SkipException;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EquipmentData;
import com.symc.plm.me.sdv.service.resource.service.create.CreateEquipmentItemService;
import com.symc.plm.me.sdv.view.resource.CreateResourceViewPane;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

public class IFExecutEquipmentData extends IFExecutDefault {

	private EquipmentData equipmentData;

	private boolean isNeedToBOMLineAdd = false;
	private boolean isNeedToBOMLineReplace = false;

	private static String DEFAULT_REV_ID = "000";
	private static String EMPTY_DATA = "-";

	private TCComponentBOPLine equipmentBOPLine;
	private TCComponentItem equipmentItem;
	private TCComponentItemRevision equipmentItemRevision;
	private String equipmentItemId;

	public IFExecutEquipmentData( NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean createOrUpdate(TCData equipmentData) {

		this.equipmentData = (EquipmentData) equipmentData;

		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		super.createOrUpdate(equipmentData);
		initTargetItem();

		boolean isUpdateTarget = false;

		int changeType = this.equipmentData.getDecidedChagneType();
		boolean bomChanged = this.equipmentData.getBOMAttributeChangeFlag();
		
		if (changeType == TCData.DECIDED_NO_CHANGE && bomChanged==false) {
			// 추가적인 처리 없이 Return
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			// 삭제 처리를 수행 한다.
			boolean haveRemoveException = false;
			try {
				removeTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
				haveRemoveException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveRemoveException = true;
			}
			return !(haveRemoveException);
		} else if (changeType == TCData.DECIDED_ADD) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		} else if (changeType == TCData.DECIDED_REVISE) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		} else if (changeType == TCData.DECIDED_REPLACE) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		}

		if (equipmentBOPLine == null) {
			isNeedToBOMLineAdd = true;
			isUpdateTarget = true;
		} else {
			if (changeType == TCData.DECIDED_REPLACE) {
				isNeedToBOMLineReplace = true;
				isUpdateTarget = true;
			} else if (changeType == TCData.DECIDED_REVISE) {
				isUpdateTarget = true;
			}
		}

		boolean haveCreateException = false;
		if (isNeedToBOMLineAdd || isNeedToBOMLineReplace) {
			try {
				createTargetObject();
				peIFExecution.waite();
				isUpdateTarget = true;
			} catch (TCException e) {
				haveCreateException = true;
				return false;
			} catch (Exception e) {
				haveCreateException = true;
				return false;
			}
		}

		if (haveCreateException==false && isUpdateTarget == true) {
			if (this.equipmentBOPLine == null) {
				addBOMLine();
				peIFExecution.waite();
			}

			try {
				updateTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		boolean isOk = false;
		if(haveCreateException== false){
			isOk = true;
		}
		
		return isOk;
	}

	public void createTargetObject() throws Exception, TCException {

		this.equipmentItem = SYMTcUtil.getItem(equipmentItemId);

		if(this.equipmentItem==null){
			try {
				this.equipmentItem = createResourceEquipmentItem(equipmentData);
			} catch (Exception e) {
				String message = "Create Equipment Item Error ["+operationItemId+"] : "+equipmentData.getItemId()+"\n"+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.equipmentData.setStatus(TCData.STATUS_ERROR, message);
				throw new Exception(message);
			}
		}
		
		this.equipmentItemRevision = this.equipmentItem.getLatestItemRevision();

	}

	public void reviseTargetObject() throws Exception, TCException {
		// Tool/Equipment/Sub Sidiary/End Item Revise 하지 않는다.
	}

	public void removeTargetObject() throws Exception, TCException {

		if (this.equipmentBOPLine != null) {
			if (this.operationBOPLine != null) {
				if (haveWriteAccessRight(this.operationBOPLine) == true) {
					this.equipmentBOPLine.cut();
				} else {
					throw new Exception("You need write permissions. : "
							+ this.operationBOPLine);
				}
			}
			this.operationBOPLine.save();
		}
	}

	public void updateTargetObject() throws Exception, TCException {

		if (this.equipmentBOPLine == null) {
			return;
		}

		boolean isReleased = false;
		boolean isWriteAble = false;
		try {
			isReleased = SYMTcUtil.isReleased(this.equipmentBOPLine);
			// 쓰기 권한이 있는지 Check 한다.
			if (isReleased == false) {
				isWriteAble = haveWriteAccessRight(this.equipmentBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (isReleased == true) {
			System.out.println("[" + this.equipmentData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;
		}else if(isWriteAble == false){
			System.out.println("[" + this.equipmentData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;			
		}

		// BOMLine Attribute 설정
		Element bomLineAttNode = (Element) equipmentData.getBomLineNode();

		// quantity
		String quantityStr = null;
		if (bomLineAttNode.getElementsByTagName("L") != null) {
			if (bomLineAttNode.getElementsByTagName("L").getLength() > 0) {
				quantityStr = bomLineAttNode.getElementsByTagName("L").item(0)
						.getTextContent();
			}
		}

		// sequance
		String sequanceStr = null;
		if (bomLineAttNode.getElementsByTagName("M") != null) {
			if (bomLineAttNode.getElementsByTagName("M").getLength() > 0) {
				sequanceStr = bomLineAttNode.getElementsByTagName("M").item(0)
						.getTextContent();
			}
		}
		
		try {
			this.equipmentBOPLine.setProperty(SDVPropertyConstant.BL_QUANTITY,
					getPropertyString(quantityStr) );
		} catch (Exception e) {
			String message = "Equipment qty set error : "+operationItemId + " -> "+equipmentItemId+" "+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.equipmentData.setStatus(TCData.STATUS_ERROR, message);
		}
		
		try {
			this.equipmentBOPLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO,
					getPropertyString(sequanceStr) );
		} catch (Exception e) {
			String message = "Equipment seq set error : "+operationItemId + " -> "+equipmentItemId+" "+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.equipmentData.setStatus(TCData.STATUS_ERROR, message);
		}

		// BOMLine 설정
		this.equipmentBOPLine.save();
		equipmentData.setBopBomLine(this.equipmentBOPLine);
	}

	private void addBOMLine() {
		
		if(this.equipmentData.getDecidedChagneType()==TCData.DECIDED_REMOVE){
			return;
		}

		// BOMLine Attribute Update
		Element bomLineAttNode = (Element) equipmentData.getBomLineNode();
		// sequance
		String sequanceStr = null;
		if (bomLineAttNode.getElementsByTagName("M") != null) {
			if (bomLineAttNode.getElementsByTagName("M").getLength() > 0) {
				sequanceStr = bomLineAttNode.getElementsByTagName("M").item(0)
						.getTextContent();
			}
		}

		if (this.equipmentItem != null) {

			TCComponentBOMLine[] findedBOMLines = getCurrentBOPLine(
					this.operationBOPLine, equipmentItemId, sequanceStr);
			if (findedBOMLines != null && findedBOMLines.length > 0) {
				this.equipmentBOPLine = (TCComponentBOPLine) findedBOMLines[0];
			} else {
				equipmentData.setResourceItem(this.equipmentItem);
				ArrayList<InterfaceAIFComponent> equipmentDataList = new ArrayList<InterfaceAIFComponent>();
				equipmentDataList.add(this.equipmentItem);

				TCComponent[] resultBOMLineList = null;
				try {
					resultBOMLineList = SDVBOPUtilities.connectObject(
							operationBOPLine, equipmentDataList,
							SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (resultBOMLineList != null && resultBOMLineList.length > 0) {
					this.equipmentBOPLine = (TCComponentBOPLine) resultBOMLineList[0];
				}
			}

			// BOMLine 설정
			equipmentData.setBopBomLine(this.equipmentBOPLine);
		}

	}

	private void initTargetItem() {

		this.equipmentBOPLine = (TCComponentBOPLine) this.equipmentData
				.getBopBomLine();

		if (this.equipmentBOPLine == null) {

			Element equipmentBOMLineAttNode = (Element) this.equipmentData
					.getBomLineNode();
			// toolItemId
			if (equipmentBOMLineAttNode.getElementsByTagName("K") != null) {
				if (equipmentBOMLineAttNode.getElementsByTagName("K")
						.getLength() > 0) {
					equipmentItemId = equipmentBOMLineAttNode
							.getElementsByTagName("K").item(0).getTextContent();
				}
			}

			try {
				equipmentItem = SYMTcUtil.getItem(equipmentItemId);
				if (equipmentItem != null) {
					equipmentItemRevision = equipmentItem
							.getLatestItemRevision();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		} else {
			try {
				equipmentItemRevision = this.equipmentBOPLine.getItemRevision();
				equipmentItem = this.equipmentBOPLine.getItem();
				if (equipmentItem != null) {
					equipmentItemId = equipmentItem
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

	private TCComponentItem createResourceEquipmentItem(
			EquipmentData equipmentData) throws Exception {

		Element equipmentAttributeNode = (Element) equipmentData
				.getMasterDataNode();
		
		if(equipmentAttributeNode==null){
			throw new Exception("Equipment master data not found");
		}

		IDataMap datamap = new RawDataMap();
		datamap.put("createMode", true, IData.BOOLEAN_FIELD);

		// ShopCode
		String shopCode = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("B") != null) {
			if (equipmentAttributeNode.getElementsByTagName("B").getLength() > 0) {
				shopCode = equipmentAttributeNode.getElementsByTagName("B")
						.item(0).getTextContent();
			}
		}
		// 설비번호
		String facilityId = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("C") != null) {
			if (equipmentAttributeNode.getElementsByTagName("C").getLength() > 0) {
				facilityId = equipmentAttributeNode.getElementsByTagName("C")
						.item(0).getTextContent();
			}
		}
		if((equipmentData.getItemId()!=null && equipmentData.getItemId().trim().length()>0) && (facilityId==null || (facilityId!=null && facilityId.trim().length()<1))){
			facilityId = equipmentData.getItemId();
		}
		
		// 설비명 (국문)
		String korName = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("D") != null) {
			if (equipmentAttributeNode.getElementsByTagName("D").getLength() > 0) {
				korName = equipmentAttributeNode.getElementsByTagName("D")
						.item(0).getTextContent();
			}
		}
		// 설비명 (영문)
		String engName = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("E") != null) {
			if (equipmentAttributeNode.getElementsByTagName("E").getLength() > 0) {
				engName = equipmentAttributeNode.getElementsByTagName("E")
						.item(0).getTextContent();
			}
		}
		// 사용 용도-국문
		String purposeKor = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("F") != null) {
			if (equipmentAttributeNode.getElementsByTagName("F").getLength() > 0) {
				purposeKor = equipmentAttributeNode.getElementsByTagName("F")
						.item(0).getTextContent();
			}
		}
		// 사용 용도-영문
		String purposeEng = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("G") != null) {
			if (equipmentAttributeNode.getElementsByTagName("G").getLength() > 0) {
				purposeEng = equipmentAttributeNode.getElementsByTagName("G")
						.item(0).getTextContent();
			}
		}
		// 설비 사양-국문
		String specKor = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("H") != null) {
			if (equipmentAttributeNode.getElementsByTagName("H").getLength() > 0) {
				specKor = equipmentAttributeNode.getElementsByTagName("H")
						.item(0).getTextContent();
			}
		}
		// 설비 사양-영문
		String specEng = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("I") != null) {
			if (equipmentAttributeNode.getElementsByTagName("I").getLength() > 0) {
				specEng = equipmentAttributeNode.getElementsByTagName("I")
						.item(0).getTextContent();
			}
		}
		// 대분류
		String mainClass = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("J") != null) {
			if (equipmentAttributeNode.getElementsByTagName("J").getLength() > 0) {
				mainClass = equipmentAttributeNode.getElementsByTagName("J")
						.item(0).getTextContent();
			}
		}
		// 중분류
		String subClass = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("K") != null) {
			if (equipmentAttributeNode.getElementsByTagName("K").getLength() > 0) {
				subClass = equipmentAttributeNode.getElementsByTagName("K")
						.item(0).getTextContent();
			}
		}
		// 처리능력
		String equipCapacity = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("L") != null) {
			if (equipmentAttributeNode.getElementsByTagName("L").getLength() > 0) {
				equipCapacity = equipmentAttributeNode
						.getElementsByTagName("L").item(0).getTextContent();
			}
		}
		// 제작사
		String maker = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("M") != null) {
			if (equipmentAttributeNode.getElementsByTagName("M").getLength() > 0) {
				maker = equipmentAttributeNode.getElementsByTagName("M")
						.item(0).getTextContent();
			}
		}
		// 도입국가
		String stateOfIntroduction = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("N") != null) {
			if (equipmentAttributeNode.getElementsByTagName("N").getLength() > 0) {
				stateOfIntroduction = equipmentAttributeNode
						.getElementsByTagName("N").item(0).getTextContent();
			}
		}
		// 설치년도
		String installYear = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("O") != null) {
			if (equipmentAttributeNode.getElementsByTagName("O").getLength() > 0) {
				installYear = equipmentAttributeNode.getElementsByTagName("O")
						.item(0).getTextContent();
			}
		}
		// 변경내역문자
		String revDesc = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("P") != null) {
			if (equipmentAttributeNode.getElementsByTagName("P").getLength() > 0) {
				revDesc = equipmentAttributeNode.getElementsByTagName("P")
						.item(0).getTextContent();
			}
		}
		// 차종코드 (Jig)
		String jigVehicleCode = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("Q") != null) {
			if (equipmentAttributeNode.getElementsByTagName("Q").getLength() > 0) {
				jigVehicleCode = equipmentAttributeNode
						.getElementsByTagName("Q").item(0).getTextContent();
			}
		}
		// CAD파일경로
		String cadFilePath = null;
		if (equipmentAttributeNode!=null && equipmentAttributeNode.getElementsByTagName("S") != null) {
			if (equipmentAttributeNode.getElementsByTagName("S").getLength() > 0) {
				cadFilePath = equipmentAttributeNode.getElementsByTagName("S")
						.item(0).getTextContent();
			}
		}

		boolean isJIG = false;
		if (!StringUtils.isEmpty(jigVehicleCode)) {
			isJIG = true;
			datamap.put("itemTCCompType",
					SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM,
					IData.STRING_FIELD);
		} else {
			datamap.put("itemTCCompType",
					SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM,
					IData.STRING_FIELD);
		}
		// Map<String, String> itemProperties
		// public static final String EQUIP_ENG_NAME = "m7_ENG_NAME";
		Map<String, String> itemProperties = new HashMap<String, String>();
		itemProperties.put(SDVPropertyConstant.ITEM_ITEM_ID, facilityId);
		itemProperties.put(SDVPropertyConstant.EQUIP_ENG_NAME, engName);
		datamap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);
		// Map<String, String> revisionProperties
		// public static final String EQUIP_SHOP_CODE = "m7_SHOP";
		// public static final String EQUIP_RESOURCE_CATEGORY =
		// "m7_RESOURCE_CATEGORY";
		// public static final String EQUIP_MAIN_CLASS = "m7_MAIN_CLASS";
		// public static final String EQUIP_SUB_CLASS = "m7_SUB_CLASS";
		// public static final String EQUIP_SPEC_KOR = "m7_SPEC_KOR";
		// public static final String EQUIP_SPEC_ENG = "m7_SPEC_ENG";
		// public static final String EQUIP_CAPACITY = "m7_CAPACITY";
		// public static final String EQUIP_MAKER = "m7_MAKER";
		// public static final String EQUIP_NATION = "m7_NATION";
		// public static final String EQUIP_INSTALL_YEAR = "m7_INSTALL_YEAR";
		// public static final String EQUIP_PURPOSE_KOR = "m7_PURPOSE_KOR";
		// public static final String EQUIP_PURPOSE_ENG = "m7_PURPOSE_ENG";
		// public static final String EQUIP_REV_DESC = "m7_REV_DESC";
		//
		// public static final String EQUIP_VEHICLE_CODE= "m7_VEHICLE_CODE";
		// public static final String EQUIP_STATION_CODE = "m7_STATION_CODE";
		// public static final String EQUIP_POSITION_CODE = "m7_POSITION_CODE";
		// public static final String EQUIP_LINE_CODE = "m7_LINE";
		//
		// public static final String EQUIP_AXIS= "m7_AXIS";
		// public static final String EQUIP_SERVO = "m7_SERVO";
		// public static final String EQUIP_ROBOT_TYPE = "m7_ROBOT_TYPE";
		// public static final String EQUIP_MAKER_NO = "m7_MAKER_NO";

		Map<String, String> revisionProperties = new HashMap<String, String>();
		revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID,
				DEFAULT_REV_ID);
		revisionProperties.put(SDVPropertyConstant.ITEM_OBJECT_NAME, korName);
		revisionProperties.put(SDVPropertyConstant.EQUIP_SHOP_CODE, shopCode);
		revisionProperties.put(SDVPropertyConstant.EQUIP_MAIN_CLASS, mainClass);
		revisionProperties.put(SDVPropertyConstant.EQUIP_SUB_CLASS, subClass);
		revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_KOR, specKor);
		// 제약조건 제외 초기화 ("-")
		if (StringUtils.isEmpty(revisionProperties
				.get(SDVPropertyConstant.EQUIP_SPEC_KOR))) {
			revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_KOR,
					EMPTY_DATA);
		}

		revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_ENG, specEng);
		// 제약조건 제외 초기화 ("-")
		if (StringUtils.isEmpty(revisionProperties
				.get(SDVPropertyConstant.EQUIP_SPEC_ENG))) {
			revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_ENG,
					EMPTY_DATA);
		}
		revisionProperties.put(SDVPropertyConstant.EQUIP_CAPACITY,
				equipCapacity);
		revisionProperties.put(SDVPropertyConstant.EQUIP_MAKER, maker);
		revisionProperties.put(SDVPropertyConstant.EQUIP_NATION,
				stateOfIntroduction);
		revisionProperties.put(SDVPropertyConstant.EQUIP_INSTALL_YEAR,
				installYear);
		revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_KOR,
				purposeKor);
		// 제약조건 제외 초기화 ("-")
		if (StringUtils.isEmpty(revisionProperties
				.get(SDVPropertyConstant.EQUIP_PURPOSE_KOR))) {
			revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_KOR,
					EMPTY_DATA);
		}
		revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG,
				purposeEng);
		// 제약조건 제외 초기화 ("-")
		if (StringUtils.isEmpty(revisionProperties
				.get(SDVPropertyConstant.EQUIP_PURPOSE_ENG))) {
			revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG,
					EMPTY_DATA);
		}
		revisionProperties.put(SDVPropertyConstant.EQUIP_REV_DESC, revDesc);
		try {
			revisionProperties.put(SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY,
					getEquipmentResourceCateGory(facilityId, jigVehicleCode));
		} catch (Exception e) {
			
		}
		// JIG
		if (isJIG) {
			revisionProperties.put(SDVPropertyConstant.EQUIP_VEHICLE_CODE,
					jigVehicleCode);
		}
		// 차체 JIG
		// revisionProperties.put(SDVPropertyConstant.EQUIP_STATION_CODE,
		// masterRowData.get(0));
		// revisionProperties.put(SDVPropertyConstant.EQUIP_POSITION_CODE,
		// masterRowData.get(0));
		// 도장
		// revisionProperties.put(SDVPropertyConstant.EQUIP_LINE_CODE,
		// masterRowData.get(0));

		datamap.put("revisionProperties", revisionProperties,
				IData.OBJECT_FIELD);
		// CAD File List
		RawDataMap fileDataMap = new RawDataMap();
		if (!StringUtils.isEmpty(cadFilePath)) {
			fileDataMap.put("isModified", true, IData.BOOLEAN_FIELD);
			fileDataMap.put("CATPart", cadFilePath, IData.STRING_FIELD);
		} else {
			fileDataMap.put("isModified", false, IData.BOOLEAN_FIELD);
		}
		datamap.put("File", fileDataMap, IData.OBJECT_FIELD);

		TCComponentItem item = null;
		if(facilityId!=null && facilityId.trim().length()>0){
			CreateEquipmentItemService createEquipmentItemService = new CreateEquipmentItemService(
					datamap);
			item = createEquipmentItemService.create().getItem();
		}else{
			this.peIFExecution.writeLogTextLine("FacilityId is null");
		}
		return item;
	}

	/**
	 * 일반설비, JIG설비 확인
	 * 
	 * @method getEquipmentResourceCateGory
	 * @date 2013. 12. 31.
	 * @param
	 * @return String
	 * @exception
	 * @throws
	 * @see
	 */
	private String getEquipmentResourceCateGory(String facilityId,
			String jigVehicleCode) throws Exception {

		if (facilityId!=null || (facilityId!=null && StringUtils.isEmpty(facilityId))) {
			String message = "EQUIPMENT ITEM ID "+facilityId+"가 존재 하지 않습니다."; 
			SkipException skipException = new SkipException(message);
			skipException.setStatus(TCData.STATUS_ERROR);
			throw skipException;
		}
		Registry registry = Registry.getRegistry(CreateResourceViewPane.class);
		if (jigVehicleCode!=null || (jigVehicleCode!=null && StringUtils.isEmpty(jigVehicleCode))) {
			return registry.getString("Resource.Category.EXT");
		} else {
			return registry.getString("Resource.Category.JIG");
		}
	}

	// /**
	// * 할당 대상 공구(Tool) 공법(Operation)에서 검색
	// *
	// * @method findAssignToolBOMLine
	// * @date 2013. 12. 19.
	// * @param
	// * @return HashMap<String,TCComponentBOMLine>
	// * @exception
	// * @throws
	// * @see
	// */
	// private HashMap<String, TCComponentBOMLine>
	// findAssignToolBOMLine(TCComponentBOMLine operationBOMLine, String[]
	// toolIds) throws Exception {
	// HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = new
	// HashMap<String, TCComponentBOMLine>();
	// if (toolIds == null || toolIds.length == 0) {
	// return findedAssignToolBOMLine;
	// }
	// // 초기화
	// for (int i = 0; i < toolIds.length; i++) {
	// toolIds[i] = (toolIds[i] == null) ? "" : toolIds[i].trim();
	// }
	// for (String toolId : toolIds) {
	// findedAssignToolBOMLine.put(toolId, null);
	// }
	// TCComponentBOMLine[] childs =
	// SDVBOPUtilities.getUnpackChildrenBOMLine(operationBOMLine);
	// for (TCComponentBOMLine operationUnderBOMLine : childs) {
	// String itemId =
	// operationUnderBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
	// // PE I/F 공구ID를 가지고 공법하위 공구 검색
	// if (findedAssignToolBOMLine.containsKey(itemId)) {
	// findedAssignToolBOMLine.put(itemId, operationUnderBOMLine);
	// }
	// }
	// return findedAssignToolBOMLine;
	// }

}
