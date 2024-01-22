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
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ToolData;
import com.symc.plm.me.sdv.service.resource.service.create.CreateToolItemService;
import com.symc.plm.me.sdv.view.resource.CreateResourceViewPane;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

public class IFExecutToolData extends IFExecutDefault {

	private ToolData toolData;

	private String toolItemId = null;
	private TCComponentItem toolItem;
	private TCComponentItemRevision toolItemRevision;
	private TCComponentBOPLine toolBOPLine;

	private boolean isNeedToBOMLineAdd = false;
	private boolean isNeedToBOMLineReplace = false;

	private static String DEFAULT_REV_ID = "000";
	private static String EMPTY_DATA = "-";

	public IFExecutToolData(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean createOrUpdate(TCData toolData) {
		this.toolData = (ToolData) toolData;

		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		super.createOrUpdate(toolData);
		initTargetItem();

		boolean isUpdateTarget = false;
		boolean bomChanged = this.toolData.getBOMAttributeChangeFlag();
		
		int changeType = toolData.getDecidedChagneType();
		if (changeType == TCData.DECIDED_NO_CHANGE && bomChanged==false) {
			// 추가적인 처리 없이 Return
			System.out.println("changeType : DECIDED_NO_CHANGE");
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			System.out.println("changeType : DECIDED_REMOVE");
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
			System.out.println("changeType : DECIDED_ADD");
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		} else if (changeType == TCData.DECIDED_REVISE) {
			System.out.println("changeType : DECIDED_REVISE");
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		} else if (changeType == TCData.DECIDED_REPLACE) {
			System.out.println("changeType : DECIDED_REPLACE");
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		}

		System.out.println("operationItemId = " + operationItemId);
		System.out.println("toolItemId = " + toolItemId);

		if (toolBOPLine == null) {
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

		if (this.toolItemRevision!=null && isUpdateTarget == true) {
			if (this.toolBOPLine == null) {
				addBOMLine();
				peIFExecution.waite();
			}
		}

		boolean haveUpdateException = false;
		try {
			updateTargetObject();
			peIFExecution.waite();
		} catch (TCException e) {
			e.printStackTrace();
			haveUpdateException = true;
		} catch (Exception e) {
			e.printStackTrace();
			haveUpdateException = true;
		}
		
		boolean isOk = false;
		if(haveCreateException== false && haveUpdateException==false){
			isOk = true;
		}

		return isOk;
	}

	public void createTargetObject() throws Exception, TCException {

		System.out.println("---------------------\nTool Creation : "+toolItemId+"\n---------------------\n");
		
		this.toolItem = SYMTcUtil.getItem(toolItemId);
		
		if(this.toolItem==null){
			try {
				this.toolItem = createResourceToolItem(toolData);
			} catch (Exception e) {
				String message = "Create Tool Item Error ["+operationItemId+"] : "+toolData.getItemId()+"\n"+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.toolData.setStatus(TCData.STATUS_ERROR, message);
				throw new Exception(message);
			}
		}
		
		if(this.toolItem!=null){
			this.toolItemRevision = this.toolItem.getLatestItemRevision();
		}

	}

	public void reviseTargetObject() throws Exception, TCException {
		// Tool/Equipment/Sub Sidiary/End Item Revise 하지 않는다.
	}

	public void removeTargetObject() throws Exception, TCException {

		if (this.operationBOPLine != null) {
			if (this.toolBOPLine != null) {
				if (haveWriteAccessRight(this.operationBOPLine) == true) {
					this.toolBOPLine.cut();
				} else {
					throw new Exception("You need write permissions. : "
							+ this.operationBOPLine);
				}
			}

			this.operationBOPLine.save();
		}
	}

	public void updateTargetObject() throws Exception, TCException {

		if (toolBOPLine == null) {
			return;
		}

		boolean isReleased = false;
		boolean isWriteAble = false;
		try {
			isReleased = SYMTcUtil.isReleased(this.toolBOPLine);
			// 쓰기 권한이 있는지 Check 한다.
			if (isReleased == false) {
				isWriteAble = haveWriteAccessRight(this.toolBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (isReleased == true) {
			System.out.println("[" + this.toolData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;
		}else if(isWriteAble == false){
			System.out.println("[" + this.toolData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;			
		}

		// BOMLine Attribute Update
		Element bomLineAttNode = (Element) toolData.getBomLineNode();

		// quantity
		String quantityStr = null;
		if (bomLineAttNode.getElementsByTagName("L") != null) {
			if (bomLineAttNode.getElementsByTagName("L").getLength() > 0) {
				quantityStr = bomLineAttNode.getElementsByTagName("L").item(0)
						.getTextContent();
			}
		}
		quantityStr = getPropertyString(quantityStr);

		// sequance
		String sequanceStr = null;
		if (bomLineAttNode.getElementsByTagName("N") != null) {
			if (bomLineAttNode.getElementsByTagName("N").getLength() > 0) {
				sequanceStr = bomLineAttNode.getElementsByTagName("N").item(0)
						.getTextContent();
			}
		}
		sequanceStr = getPropertyString(sequanceStr);

		try {
			toolBOPLine.setProperty(SDVPropertyConstant.BL_QUANTITY, quantityStr);
		} catch (Exception e) {
			String message = "Tool qty setting error ["+operationItemId+" -> "+toolItemId+"]"+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.toolData.setStatus(TCData.STATUS_ERROR, message);
		}
		
		try {
			toolBOPLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, sequanceStr);
		} catch (Exception e) {
			String message = "Tool seq setting error ["+operationItemId+" -> "+toolItemId+"]"+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.toolData.setStatus(TCData.STATUS_ERROR, message);
		}

		toolBOPLine.save();
		toolData.setBopBomLine(toolBOPLine);
	}

	private void addBOMLine() {
		
		if(this.toolData.getDecidedChagneType()==TCData.DECIDED_REMOVE){
			return;
		}

		// BOMLine Attribute Update
		Element bomLineAttNode = (Element) toolData.getBomLineNode();
		// sequance
		String sequanceStr = null;
		if (bomLineAttNode.getElementsByTagName("N") != null) {
			if (bomLineAttNode.getElementsByTagName("N").getLength() > 0) {
				sequanceStr = bomLineAttNode.getElementsByTagName("N").item(0)
						.getTextContent();
			}
		}

		if (this.toolItem != null) {

			TCComponentBOMLine[] findedBOMLines = getCurrentBOPLine(
					this.operationBOPLine, toolItemId, sequanceStr);
			if (findedBOMLines != null && findedBOMLines.length > 0) {
				this.toolBOPLine = (TCComponentBOPLine) findedBOMLines[0];
			} else {
				toolData.setResourceItem(this.toolItem);
				ArrayList<InterfaceAIFComponent> toolDataList = new ArrayList<InterfaceAIFComponent>();
				toolDataList.add(this.toolItem);

				TCComponent[] resultBOMLineList = null;
				try {
					resultBOMLineList = SDVBOPUtilities.connectObject(
							operationBOPLine, toolDataList,
							SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (resultBOMLineList != null && resultBOMLineList.length > 0) {
					this.toolBOPLine = (TCComponentBOPLine) resultBOMLineList[0];
				}
			}

			// BOMLine 설정
			toolData.setBopBomLine(this.toolBOPLine);
		}

	}

	private void initTargetItem() {

		this.toolBOPLine = (TCComponentBOPLine) this.toolData.getBopBomLine();

		if (this.toolBOPLine == null) {

			Element toolBOMLineAttNode = (Element) this.toolData
					.getBomLineNode();
			// toolItemId
			if (toolBOMLineAttNode.getElementsByTagName("K") != null) {
				if (toolBOMLineAttNode.getElementsByTagName("K").getLength() > 0) {
					toolItemId = toolBOMLineAttNode.getElementsByTagName("K")
							.item(0).getTextContent();
				}
			}

			try {
				toolItem = SYMTcUtil.getItem(toolItemId);
				if (toolItem != null) {
					toolItemRevision = toolItem.getLatestItemRevision();
				}
			} catch (Exception e1) {
				String message = "Tool Item Not Found! ["+operationItemId+" -> "+toolItemId+"] <- need to create";
				this.peIFExecution.writeLogTextLine(message);
			}

		} else {
			try {
				toolItemRevision = this.toolBOPLine.getItemRevision();
				toolItem = this.toolBOPLine.getItem();
				if (toolItem != null) {
					toolItemId = toolItem
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 공구(Tool) Resource Item 생성 등록된 Tool이 없는 경우 Tool을 생성해야 하는데 이때 사용하는
	 * Function임.
	 * 
	 * @method createResourceToolItem
	 * @date 2013. 12. 26.
	 * @param
	 * @return TCComponentItem
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings("unchecked")
	public TCComponentItem createResourceToolItem(ToolData toolData)
			throws Exception {

		Element toolMasterElement = (Element) toolData.getMasterDataNode();

		// Tool의 경우 BOMLine 정보를 가진 N/F Data에는 있으나
		// Tool 생성을 위한 N/F Data에는 정보가 없는 경우가 있다.
		// 이런경우 이미 생성된 Tool Revision을 그대로 사용 하도록 대처 한다.
		if (toolMasterElement == null) {
			toolItem = SYMTcUtil.getItem(toolData.getItemId());
			if (toolItem != null) {
				toolItemRevision = SYMTcUtil.getLatestReleasedRevision(toolItem);
				if (toolItemRevision == null) {
					throw new Exception("Can't find released tool revision "+this.toolItemId);
				} else {
					return toolItem;
				}
			}
		}
		
		if(toolItemRevision==null && toolMasterElement==null){
			throw new Exception("Tool master data not found");
		}

		IDataMap datamap = new RawDataMap();
		datamap.put("createMode", true, IData.BOOLEAN_FIELD);
		datamap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_TOOL_ITEM,
				IData.STRING_FIELD);

		// Map<String, String> itemProperties
		// public static final String TOOL_ENG_NAME = "m7_ENG_NAME";
		Map<String, String> itemProperties = new HashMap<String, String>();

		// ToolId
		String toolId = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("B") != null) {
			if (toolMasterElement.getElementsByTagName("B").getLength() > 0) {
				toolId = toolMasterElement.getElementsByTagName("B").item(0)
						.getTextContent();
			}
		}
		toolId = getPropertyString(toolId);
		if((toolData.getItemId()!=null && toolData.getItemId().trim().length()>0) && (toolId==null || (toolId!=null && toolId.trim().length()<1))){
			toolId = toolData.getItemId();
		}

		// KorName
		String korName = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("C") != null) {
			if (toolMasterElement.getElementsByTagName("C").getLength() > 0) {
				korName = toolMasterElement.getElementsByTagName("C").item(0)
						.getTextContent();
			}
		}
		korName = getPropertyString(korName);

		// EngName
		String engName = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("D") != null) {
			if (toolMasterElement.getElementsByTagName("D").getLength() > 0) {
				engName = toolMasterElement.getElementsByTagName("D").item(0)
						.getTextContent();
			}
		}
		engName = getPropertyString(engName);

		// MainClass
		String mainClass = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("E") != null) {
			if (toolMasterElement.getElementsByTagName("E").getLength() > 0) {
				mainClass = toolMasterElement.getElementsByTagName("E").item(0)
						.getTextContent();
			}
		}
		mainClass = getPropertyString(mainClass);

		// MainClass
		String subClass = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("F") != null) {
			if (toolMasterElement.getElementsByTagName("F").getLength() > 0) {
				subClass = toolMasterElement.getElementsByTagName("F").item(0)
						.getTextContent();
			}
		}
		subClass = getPropertyString(subClass);

		// toolPurpose
		String toolPurpose = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("G") != null) {
			if (toolMasterElement.getElementsByTagName("G").getLength() > 0) {
				toolPurpose = toolMasterElement.getElementsByTagName("G")
						.item(0).getTextContent();
			}
		}
		toolPurpose = getPropertyString(toolPurpose);

		// specCode
		String specCode = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("H") != null) {
			if (toolMasterElement.getElementsByTagName("H").getLength() > 0) {
				specCode = toolMasterElement.getElementsByTagName("H").item(0)
						.getTextContent();
			}
		}
		specCode = getPropertyString(specCode);

		// specKor
		String specKor = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("I") != null) {
			if (toolMasterElement.getElementsByTagName("I").getLength() > 0) {
				specKor = toolMasterElement.getElementsByTagName("I").item(0)
						.getTextContent();
			}
		}
		specKor = getPropertyString(specKor);

		// specEng
		String specEng = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("J") != null) {
			if (toolMasterElement.getElementsByTagName("J").getLength() > 0) {
				specEng = toolMasterElement.getElementsByTagName("J").item(0)
						.getTextContent();
			}
		}
		specEng = getPropertyString(specEng);

		// unitUsage
		String unitUsage = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("K") != null) {
			if (toolMasterElement.getElementsByTagName("K").getLength() > 0) {
				unitUsage = toolMasterElement.getElementsByTagName("K").item(0)
						.getTextContent();
			}
		}
		unitUsage = getPropertyString(unitUsage);

		// material
		String material = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("L") != null) {
			if (toolMasterElement.getElementsByTagName("L").getLength() > 0) {
				material = toolMasterElement.getElementsByTagName("L").item(0)
						.getTextContent();
			}
		}
		material = getPropertyString(material);

		// torque
		String torque = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("M") != null) {
			if (toolMasterElement.getElementsByTagName("M").getLength() > 0) {
				torque = toolMasterElement.getElementsByTagName("M").item(0)
						.getTextContent();
			}
		}
		torque = getPropertyString(torque);

		// maker
		String maker = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("N") != null) {
			if (toolMasterElement.getElementsByTagName("N").getLength() > 0) {
				maker = toolMasterElement.getElementsByTagName("N").item(0)
						.getTextContent();
			}
		}
		maker = getPropertyString(maker);

		// 업체/AF
		String companyAF = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("O") != null) {
			if (toolMasterElement.getElementsByTagName("O").getLength() > 0) {
				companyAF = toolMasterElement.getElementsByTagName("O").item(0)
						.getTextContent();
			}
		}
		companyAF = getPropertyString(companyAF);

		// 형상분류
		String toolShape = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("P") != null) {
			if (toolMasterElement.getElementsByTagName("P").getLength() > 0) {
				toolShape = toolMasterElement.getElementsByTagName("P").item(0)
						.getTextContent();
			}
		}
		toolShape = getPropertyString(toolShape);

		// 길이
		String toolLength = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("Q") != null) {
			if (toolMasterElement.getElementsByTagName("Q").getLength() > 0) {
				toolLength = toolMasterElement.getElementsByTagName("Q")
						.item(0).getTextContent();
			}
		}
		toolLength = getPropertyString(toolLength);

		// connectionSize
		String connectionSize = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("R") != null) {
			if (toolMasterElement.getElementsByTagName("R").getLength() > 0) {
				connectionSize = toolMasterElement.getElementsByTagName("R")
						.item(0).getTextContent();
			}
		}
		connectionSize = getPropertyString(connectionSize);

		// includeMagnet
		String includeMagnet = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("S") != null) {
			if (toolMasterElement.getElementsByTagName("S").getLength() > 0) {
				includeMagnet = toolMasterElement.getElementsByTagName("S")
						.item(0).getTextContent();
			}
		}
		includeMagnet = getPropertyString(includeMagnet);

		itemProperties.put(SDVPropertyConstant.ITEM_ITEM_ID, toolId);
		itemProperties.put(SDVPropertyConstant.TOOL_ENG_NAME, engName);
		datamap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);

		// Map<String, String> revisionProperties
		// public static final String TOOL_RESOURCE_CATEGORY =
		// "m7_RESOURCE_CATEGORY";
		// public static final String TOOL_MAIN_CLASS = "m7_MAIN_CLASS";
		// public static final String TOOL_SUB_CLASS = "m7_SUB_CLASS";
		// public static final String TOOL_PURPOSE = "m7_PURPOSE_KOR";
		// public static final String TOOL_SPEC_CODE = "m7_SPEC_CODE";
		// public static final String TOOL_SPEC_KOR = "m7_SPEC_KOR";
		// public static final String TOOL_SPEC_ENG = "m7_SPEC_ENG";
		// public static final String TOOL_TORQUE_VALUE = "m7_TORQUE_VALUE";
		// public static final String TOOL_UNIT_USAGE = "m7_UNIT_USAGE";
		// public static final String TOOL_MATERIAL = "m7_MATERIAL";
		// public static final String TOOL_MAKER = "m7_MAKER";
		// public static final String TOOL_MAKER_AF_CODE = "m7_MAKER_AF_CODE";
		// public static final String TOOL_TOOL_SHAPE = "m7_TOOL_SHAPE";
		// public static final String TOOL_TOOL_LENGTH = "m7_TOOL_LENGTH";
		// public static final String TOOL_TOOL_SIZE = "m7_TOOL_SIZE";
		// public static final String TOOL_TOOL_MAGNET = "m7_TOOL_MAGNET";
		// public static final String TOOL_VEHICLE_CODE = "m7_VEHICLE_CODE";
		// public static final String TOOL_STAY_TYPE = "m7_STAY_TYPE";
		// public static final String TOOL_STAY_AREA = "m7_STAY_AREA";

		Map<String, String> revisionProperties = new HashMap<String, String>();
		revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID,
				DEFAULT_REV_ID);
		revisionProperties.put(SDVPropertyConstant.ITEM_OBJECT_NAME, korName);
		
		try {
			revisionProperties.put(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY,
					getToolResourceCateGory(toolId));
		} catch (Exception e) {
			// Log에 출력되도록 getToolResourceCateGory()에서 처리 했음. 
		}
		
		revisionProperties.put(SDVPropertyConstant.TOOL_MAIN_CLASS, mainClass);
		revisionProperties.put(SDVPropertyConstant.TOOL_SUB_CLASS, subClass);
		revisionProperties.put(SDVPropertyConstant.TOOL_PURPOSE, toolPurpose);
		// 제약조건 제외 초기화 ("-")
		if (StringUtils.isEmpty(revisionProperties
				.get(SDVPropertyConstant.TOOL_PURPOSE))) {
			revisionProperties
					.put(SDVPropertyConstant.TOOL_PURPOSE, EMPTY_DATA);
		}
		revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_KOR, specKor);
		// 제약조건 제외 초기화 ("-")
		if (StringUtils.isEmpty(revisionProperties
				.get(SDVPropertyConstant.TOOL_SPEC_KOR))) {
			revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_KOR,
					EMPTY_DATA);
		}
		revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_ENG, specEng);
		// 제약조건 제외 초기화 ("-")
		if (StringUtils.isEmpty(revisionProperties
				.get(SDVPropertyConstant.TOOL_SPEC_ENG))) {
			revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_ENG,
					EMPTY_DATA);
		}
		revisionProperties.put(SDVPropertyConstant.TOOL_TORQUE_VALUE, torque);
		revisionProperties.put(SDVPropertyConstant.TOOL_UNIT_USAGE, unitUsage);
		revisionProperties.put(SDVPropertyConstant.TOOL_MATERIAL, material);
		revisionProperties.put(SDVPropertyConstant.TOOL_MAKER, maker);
		// Socket 공구 처리
		if (isToolSocket(revisionProperties
				.get(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY))) {
			revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_SHAPE,
					toolShape);
			revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_LENGTH,
					toolLength);
			revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_SIZE,
					connectionSize);
			revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_MAGNET,
					includeMagnet);
		}
		// Socket 공구가 아닌 경우에만 처리
		else {
			revisionProperties
					.put(SDVPropertyConstant.TOOL_SPEC_CODE, specCode);
			String makerAf = BundleUtil.nullToString(companyAF).trim();
			// empty -> 00
			if (StringUtils.isEmpty(makerAf)) {
				revisionProperties.put(SDVPropertyConstant.TOOL_MAKER_AF_CODE,
						"00");
			}
			// 3자리에서 앞의 두자리만 처리 - 000 -> 00
			else if (makerAf.length() == 3) {
				revisionProperties.put(SDVPropertyConstant.TOOL_MAKER_AF_CODE,
						makerAf.substring(0, 1));
			}
		}

		// 업체/AF
		String cadFilePath = null;
		if (toolMasterElement!=null && toolMasterElement.getElementsByTagName("U") != null) {
			if (toolMasterElement.getElementsByTagName("U").getLength() > 0) {
				cadFilePath = toolMasterElement.getElementsByTagName("U")
						.item(0).getTextContent();
			}
		}

		// 도장
		// revisionProperties.put(SDVPropertyConstant.TOOL_VEHICLE_CODE,
		// masterRowData.get(0));
		// revisionProperties.put(SDVPropertyConstant.TOOL_STAY_TYPE,
		// masterRowData.get(0));
		// revisionProperties.put(SDVPropertyConstant.TOOL_STAY_AREA,
		// masterRowData.get(0));
		datamap.put("revisionProperties", revisionProperties,
				IData.OBJECT_FIELD);
		
		// CAD File List
		RawDataMap fileDataMap = new RawDataMap();
		if (StringUtils.isEmpty(cadFilePath)==false) {
			fileDataMap.put("isModified", true, IData.BOOLEAN_FIELD);
			fileDataMap.put("CATPart", cadFilePath, IData.STRING_FIELD);
		} else {
			fileDataMap.put("isModified", false, IData.BOOLEAN_FIELD);
		}
		datamap.put("File", fileDataMap, IData.OBJECT_FIELD);

		if(toolId!=null && toolId.trim().length()>0){
			CreateToolItemService createItemService = new CreateToolItemService(datamap);
			toolItem = createItemService.create().getItem();
		}
		if(toolItem!=null){
			toolItemRevision = toolItem.getLatestItemRevision();
		}else{
			String message = "Tool creation error : "+operationItemId + " -> "+toolItemId+"!";
			this.peIFExecution.writeLogTextLine(message);
			throw new Exception(message);
		}
		
		return toolItem;
	}

	/**
	 * 일반공구, 소켓공구 확인
	 * 
	 * @method getToolResourceCateGory
	 * @date 2013. 12. 31.
	 * @param
	 * @return String
	 * @exception
	 * @throws
	 * @see
	 */
	private String getToolResourceCateGory(String tooId) throws Exception {
		if (StringUtils.isEmpty(tooId)) {
			String message = "Catecory get : TOOL ITEM ID ("+tooId+")가 존재 하지 않습니다.";
			SkipException skipException = new SkipException(
					"TOOL ITEM ID가 존재 하지 않습니다.");
			skipException.setStatus(TCData.STATUS_ERROR);
			this.peIFExecution.writeLogTextLine(message);
			throw skipException;
		}
		Registry registry = Registry.getRegistry(CreateResourceViewPane.class);
		if (tooId.split("-").length == 6) {
			return registry.getString("Resource.Category.SOC");
		} else {
			return registry.getString("Resource.Category.EXT");
		}
	}

	/**
	 * 일반공구, 소켓공구 확인
	 * 
	 * @method isToolSocket
	 * @date 2014. 1. 24.
	 * @param
	 * @return boolean
	 * @exception
	 * @throws
	 * @see
	 */
	private boolean isToolSocket(String category) {
		Registry registry = Registry.getRegistry(CreateResourceViewPane.class);
		if (category!=null && category.equals(registry.getString("Resource.Category.SOC"))) {
			return true;
		} else {
			return false;
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
