package com.symc.plm.me.sdv.operation.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.kgm.dto.EndItemData;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomBOPDao;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.common.BOMTreeTableModel;
import com.teamcenter.rac.util.Registry;

/**
 * [SR150119-032,SR150122-027][20150210] shcho, End Item Master Report 속도 개선 및 replaced end item 비고란 표기 (10버전에서 개발 된 소스 9으로 이식함)
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 * [SR150312-028][20150401] ymjang, E/Item Report 추출 로직 수정 및 보완
 * [SR150326-007][20150401] ymjang, E/Item Master List에 Link 끊어진 속성 표시 의뢰
 */
public class ReportEndItemMasterListOperation extends SimpleSDVExcelOperation {

	private Registry registry;

	private BOMTreeTableModel tableModel = null;

	private TCComponentBOMWindow mProductWindow;

	private HashMap<String, String> lineMap = new HashMap<String, String>();
	private HashMap<String, String> stationMap = new HashMap<String, String>();
	private HashMap<String, String> operationMap = new HashMap<String, String>();

	private String productCode = "";
	private String processType = "";
	private String operationType = "";

	private String previous_operation_id = null;

	private SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
	private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");

//	private ArrayList<EndItemData> replacedEndItemList = null;
	private ArrayList<String> replacedEndItemOccPuidList = null;
	
	public ReportEndItemMasterListOperation() {
	}

	@Override
	public void executeOperation() throws Exception {
		registry = Registry.getRegistry(this);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
		String startTime = df.format(new Date());
		String expandingTime = null;
		String endTime = null;
		try {
			IDataSet dataSet = getData();
			
			expandingTime = df.format(new Date());
			
			if (dataSet != null) {
				String defaultFileName = productCode + "_" + "EndItemMasterList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
				transformer.print(mode, templatePreference, defaultFileName, dataSet);
			}
		} catch (Exception e) {
			e.printStackTrace();
			setExecuteError(e);
		}finally{

			endTime = df.format(new Date());
			System.out.println("Start Time = "+startTime);
			System.out.println("Expand  Time = "+expandingTime);
			System.out.println("End Time = "+endTime);

		}
	}

	@Override
	protected IDataSet getData() throws Exception {
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

		String compID = "";
		String revisionRule = "";
		String revRuleStandardDate = "";
		String variantRule = "";

		InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
		if (component != null && component instanceof TCComponentBOPLine) {
			TCComponentBOPLine comp = (TCComponentBOPLine) component;

			// BOP 윈도우
			TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();

			// M Product 윈도우
			// [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
			mProductWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(bomWindow.getTopBOMLine().getItemRevision());

			// product Code
			productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
			
			// Revision Rule
			TCComponentRevisionRule bomWindowRevisionRule = bomWindow.getRevisionRule();
			revisionRule = bomWindowRevisionRule.toString();

			// Revision Rule 기준일
			Date rule_date = bomWindowRevisionRule.getDateProperty("rule_date");
			if (rule_date != null) {
				revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(rule_date);
			} else {
				revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			}

			// Variant
			variantRule = SDVBOPUtilities.getBOMConfiguredVariantSetToString(bomWindow);

			// shop or line - id
			compID = comp.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

			// process type
			processType = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);

			// operation type(차체(B), 도장(P), 조립(A))
			if (processType.startsWith("B")) {
				operationType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM;
			} else if (processType.startsWith("P")) {
				operationType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM;
			} else if (processType.startsWith("A")) {
				operationType = SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM;
			}

			// BOMTreeTableModel
			MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
			BOMTreeTable treeTable = application.getViewableTreeTable();
			tableModel = (BOMTreeTableModel) treeTable.getTreeTableModel();

			// [SR150326-007][20150402] ymjang, E/Item Master List에 Link 끊어진 속성 표시 의뢰
			// Replaced End Item List 에서 중복을 제거한다.
			ArrayList<EndItemData> tmpReplacedEndItemList = findReplacedEndItems(comp.getProperty(SDVPropertyConstant.BL_ITEM_PUID),
                                                                                 mProductWindow.getTopBOMLine().getProperty(SDVPropertyConstant.BL_ITEM_ID));
			replacedEndItemOccPuidList = new ArrayList<String>();
			for (EndItemData replacedEndItem : tmpReplacedEndItemList) {
				if (!replacedEndItemOccPuidList.contains(replacedEndItem.getOcc_puid())) {
					replacedEndItemOccPuidList.add(replacedEndItem.getOcc_puid());
				}
			}

			/*
			replacedEndItemList = findReplacedEndItems(comp.getProperty(SDVPropertyConstant.BL_ITEM_PUID),
                    mProductWindow.getTopBOMLine().getProperty(SDVPropertyConstant.BL_ITEM_ID));
			*/
			
			dataList = getChildrenList(dataList, (TCComponentBOPLine) component);
			previous_operation_id = null;

			IDataSet dataSet = convertToDataSet("operationList", dataList);
			IDataMap dataMap = new RawDataMap();
			dataMap.put("productCode", productCode);
			dataMap.put("compID", compID);
			dataMap.put("revisionRule", revisionRule);
			dataMap.put("revRuleStandardDate", revRuleStandardDate);
			dataMap.put("variantRule", variantRule);
			dataMap.put("excelExportDate", String.format(registry.getString("report.ExcelExportDate", "출력 일시 : %s"), ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm")));
			dataMap.put("operationType", operationType);
			dataSet.addDataMap("additionalInfo", dataMap);

			return dataSet;
		} else {
			throw new NullPointerException("Target Component is not found or selected!!");
		}
		
	}

	/**
	 * Shop 하위의 자식 Component들의 정보를 가져온다.
	 * 
	 * @method getChildrenList
	 * @date 2013. 10. 28.
	 * @param
	 * @return List<HashMap<String,Object>>
	 * @throws Exception
	 * @exception
	 * @throws
	 * @see
	 */
	private List<HashMap<String, Object>> getChildrenList(List<HashMap<String, Object>> dataList, TCComponentBOPLine parentLine) throws Exception {
		String parent_type = parentLine.getItem().getType();
		if (parent_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || parent_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
			BOMLineNode node = tableModel.getNode(parentLine);
			node.loadChildren();
		}
		
		//[NONE-SR][20151215] taeku.jeong Report에서 Pack된것들에 대한 Quantity가 무조건 1로 나와서 UnPack을 먼저 하도록 수정함.
 		// End Item에 대해 Pack되어 있는것이 있는지 확인하고 Pack을 푼다.
		String parentType = parentLine.getItemRevision().getType();
		if(parentType!=null && (parentType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV)
				|| parentType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV)
				|| parentType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV)
				) ){
			
			AIFComponentContext[] context = parentLine.getChildren();
			boolean havePacked = false;
			for (int i = 0; i < context.length; i++) {
				TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
				
				if(childLine.isPacked()==true){
					System.out.println(childLine + "is Packed");
					childLine.unpack();
					childLine.refresh();
					havePacked = true;
				}
			}
			
			if(havePacked==true){
				System.out.println("Have packed Child Node : "+parentLine);
				parentLine.refresh();
			}
			
		}

		AIFComponentContext[] context = null;
		if(parentLine!=null){
			context = parentLine.getChildren();
		}
		
		for (int i = 0;context!=null && i < context.length; i++) {
			TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
			String type = childLine.getItem().getType();

			// 미할당 Line 필터링
			if (SDVBOPUtilities.isAssyTempLine(childLine))
				continue;

			// 불필요한 작업 필터링
			if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type) || SDVTypeConstant.PLANT_OPAREA_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(type)) {
				continue;
			}

			// Line
			if (parent_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
				lineMap.clear();
				lineMap.put("line_code", parentLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
				lineMap.put("line_rev", parentLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
			}

			// Station
			if (parent_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
				stationMap.clear();
				stationMap.put("station_code", parentLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + parentLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_REV_CODE));
				stationMap.put("station_rev", parentLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
			}

			// Operation
			if (parent_type.equals(operationType)) {
				String operation_id = parentLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				if (previous_operation_id == null || !previous_operation_id.equals(operation_id)) {
					previous_operation_id = operation_id;
					operationMap.clear();
					operationMap = getOperationInfo(parentLine, operationMap);
				}
			}

			if (SDVTypeConstant.EBOM_STD_PART.equals(type) || SDVTypeConstant.EBOM_VEH_PART.equals(type)) {
				
//					String itemID = childLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

				HashMap<String, Object> dataMap = new HashMap<String, Object>();

				// 부모 정보
				dataMap.put("line_code", lineMap.get("line_code"));
				dataMap.put("line_rev", lineMap.get("line_rev"));
				dataMap.put("station_code", stationMap.get("station_code"));
				dataMap.put("station_rev", stationMap.get("station_rev"));
				dataMap.put("operation_id", operationMap.get("operation_id"));
				dataMap.put("operation_rev", operationMap.get("operation_rev"));
				//20201110 seho 공법 명 추가.
				dataMap.put("operation_name", operationMap.get("operation_name"));
				dataMap.put("operation_owning_user", operationMap.get("operation_owning_user"));
				dataMap.put("operation_optionCode", operationMap.get("operation_optionCode"));
				dataMap.put("operation_optionDescription", operationMap.get("operation_optionDescription"));
				dataMap.put("locUBProperty", operationMap.get("locUBProperty"));
				dataMap.put("assy_system", operationMap.get("assy_system"));
				dataMap.put("locLRProperty", operationMap.get("locLRProperty"));
				dataMap.put("mecoNo", operationMap.get("mecoNo"));
				if (processType.startsWith("A")) {
					dataMap.put("station_code", operationMap.get("station_code"));
				}

				String[] sProperties = childLine.getProperties(new String[] { 
						SDVPropertyConstant.BL_ITEM_ID, 
						SDVPropertyConstant.BL_ITEM_REV_ID, 
						SDVPropertyConstant.BL_OBJECT_NAME, 
						SDVPropertyConstant.BL_SEQUENCE_NO, 
						SDVPropertyConstant.BL_OWNING_USER, 
						SDVPropertyConstant.BL_RELEASE_STATUS, 
						SDVPropertyConstant.BL_DATE_RELEASED, 
						SDVPropertyConstant.BL_QUANTITY, 
						SDVPropertyConstant.S7_SUPPLY_MODE, 
						SDVPropertyConstant.S7_ECO_NO, 
						SDVPropertyConstant.BL_OCC_FND_OBJECT_ID });

				dataMap.put(SDVPropertyConstant.BL_ITEM_ID, sProperties[0]);
				dataMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, sProperties[1]);
				dataMap.put(SDVPropertyConstant.BL_OBJECT_NAME, sProperties[2]);
				dataMap.put(SDVPropertyConstant.BL_SEQUENCE_NO, sProperties[3]);
				dataMap.put(SDVPropertyConstant.BL_OWNING_USER, getNameProperty(sProperties[4]));
				dataMap.put(SDVPropertyConstant.BL_RELEASE_STATUS, sProperties[5]);
				String release_date = sProperties[6];
				if (!release_date.isEmpty()) {
					release_date = format2.format(format1.parse(release_date));
					dataMap.put(SDVPropertyConstant.BL_DATE_RELEASED, release_date);
				}
				
				String temp_quantity = sProperties[7];
				String quantity = temp_quantity.split("\\.")[0];
				dataMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);
				
				dataMap.put(SDVPropertyConstant.S7_SUPPLY_MODE, sProperties[8]);
				dataMap.put(SDVPropertyConstant.S7_ECO_NO, sProperties[9]);
				
				// [SR150326-007][20150402] ymjang, E/Item Master List에 Link 끊어진 속성 표시 의뢰
				if (childLine.getProperty(SDVPropertyConstant.BL_OCC_ASSIGNED).equals("Not Found")){
					dataMap.put("replacedEndItem", "Unlinked");
				}
				
				if(replacedEndItemOccPuidList!=null){
					for (String replacedEndItemOccPuid : replacedEndItemOccPuidList) {
						
						if (sProperties[10].equals(replacedEndItemOccPuid)) {
							// [SR150326-007][20150401] ymjang, E/Item Master List에 Link 끊어진 속성 표시 의뢰
							String replatceItemDesc = (String) dataMap.get("replacedEndItem");
							if (replatceItemDesc != null){
								dataMap.put("replacedEndItem", replatceItemDesc + " + Replaced");
							}else{
								dataMap.put("replacedEndItem", "Replaced");
							}
						}
						
					}
				}
				/*
				for (EndItemData replacedEndItem : replacedEndItemList) {
					if (sProperties[10].equals(replacedEndItem.getOcc_puid())) {
						dataMap.put("replacedEndItem", "Replaced");
					}
				}
				*/
				
				if (mProductWindow != null) {
					// 도장 - 편성버전이 00인것만 출력한다.
					if (processType.startsWith("P")) {
						if (childLine.parent() != null) {
							if (parentLine.parent().parent().getItem().getType().equals("M7_BOPLine")) {
								String paintLine = parentLine.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID);
								String paintLineCode = paintLine.substring(paintLine.length() - 2);
								if (paintLineCode.equals("00")) {
									dataMap = getEndItemPropertyFromRevision(childLine, dataMap);
									dataMap = getEndItemPropertyFromMProduct(childLine, dataMap);
								} else {
									break;
								}
							}
						}
					} else {
						dataMap = getEndItemPropertyFromRevision(childLine, dataMap);
						dataMap = getEndItemPropertyFromMProduct(childLine, dataMap);
					}
				}

				dataList.add(dataMap);
			} else {
				if(context[i].getComponent()!=null){
					getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
				}
			}
		}

		return dataList;
	}

	private HashMap<String, String> getOperationInfo(TCComponentBOPLine operation, HashMap<String, String> dataMap) throws TCException {
		String[] sProperties = operation.getProperties(new String[] { SDVPropertyConstant.BL_ITEM_ID, SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.BL_OWNING_USER, SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVPropertyConstant.OPERATION_WORK_UBODY, SDVPropertyConstant.BOPOP_REV_M7_ASSY_SYSTEM, SDVPropertyConstant.OPERATION_REV_STATION_NO
						//20201110 seho 공법명 추가
						,SDVPropertyConstant.BL_REV_OBJECT_NAME});
		dataMap.put("operation_name", sProperties[7]);

		// 공법 ID
		dataMap.put("operation_id", sProperties[0]);

		// 공법 Rev
		dataMap.put("operation_rev", sProperties[1]);

		// 생산 담당자(공법 Owning User)
		dataMap.put("operation_owning_user", getNameProperty(sProperties[2]));

		// option
		HashMap<String, Object> option = SDVBOPUtilities.getVariant(sProperties[3]);

		// option code
		dataMap.put("operation_optionCode", (String) option.get("printValues"));

		// option description
		dataMap.put("operation_optionDescription", (String) option.get("printDescriptions"));

		// 작업위치-U/Body Work
		dataMap.put("locUBProperty", sProperties[4]);

		// 조립시스템
		dataMap.put("assy_system", sProperties[5]);

		if (processType.startsWith("A")) {
			String station_code = sProperties[6];
			dataMap.put("station_code", station_code);
			if (station_code != null && !station_code.isEmpty()) {
				String locLR = station_code.substring(station_code.length() - 1);
				if (locLR.equals("L") || locLR.equals("R")) {
					dataMap.put("locLRProperty", locLR);
				} else {
					dataMap.put("locLRProperty", "");
				}
			}
		}

		// MECO
		TCComponent mecoRevision = operation.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
		if (mecoRevision != null) {
			String mecoNo = ((TCComponentItemRevision) mecoRevision).getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			dataMap.put("mecoNo", mecoNo);
		}

		return dataMap;
	}

	private HashMap<String, Object> getEndItemPropertyFromRevision(TCComponentBOPLine childLine, HashMap<String, Object> dataMap) throws Exception {
		TCComponentItemRevision itemRevision = childLine.getItemRevision();

		String[] sProperties = itemRevision.getProperties(new String[] { "s7_REGULATION", SDVPropertyConstant.S7_EST_WEIGT, SDVPropertyConstant.S7_CAL_WEIGT, SDVPropertyConstant.S7_ACT_WEIGT, SDVPropertyConstant.S7_THICKNESS, SDVPropertyConstant.S7_MATERIAL, SDVPropertyConstant.S7_REFERENCE, SDVPropertyConstant.S7_COLOR, SDVPropertyConstant.S7_COLOR_ID, SDVPropertyConstant.S7_SHOWN_NO });
		String dr = sProperties[0];
		String est_wgt = sProperties[1];
		String cal_wgt = sProperties[2];
		String act_wgt = sProperties[3];
		String thickness = sProperties[4];
		String material = sProperties[5];
		String reference = sProperties[6];
		String color = sProperties[7];
		String colorSection = sProperties[8];
		String shown = sProperties[9];

		dataMap.put("dr", dr);
		dataMap.put("est_wgt", est_wgt);
		dataMap.put("cal_wgt", cal_wgt);
		dataMap.put("act_wgt", act_wgt);
		dataMap.put("thickness", thickness);
		dataMap.put("material", material);
		dataMap.put("reference", reference);
		dataMap.put("color", color);
		dataMap.put("colorSection", colorSection);
		dataMap.put("shown", shown);

		return dataMap;
	}

	/**
	 * 기본 속성 외에 계산 또는 조건에 의해 나오는 값은 별도로 저장한다.
	 * @param bopLine
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	private HashMap<String, Object> getEndItemPropertyFromMProduct(TCComponentBOPLine bopLine, HashMap<String, Object> dataMap) throws Exception {
		// MProduct E/Item
		TCComponentBOMLine srcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductWindow, bopLine);
		if (srcBOMLine != null) {
			// [20151116] taeku.jeong 양권석과장(조립1팀)의 요청으로 할당된 M-BOM BOM Line의 Sequence No를 End Item에 추가
			String[] sProperties = srcBOMLine.getProperties(new String[] { 
						SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVPropertyConstant.S7_PART_TYPE, 
						SDVPropertyConstant.BL_S7_DISPLAY_NO, SDVPropertyConstant.S7_POSITION_DESC,
						SDVPropertyConstant.S7_MODULE_CODE, SDVPropertyConstant.BL_SEQUENCE_NO  
						});
			String itemOption = sProperties[0];
			String partType = sProperties[1];
			String partNo = sProperties[2];
			String posDesc = sProperties[3];
			String module = sProperties[4];
			String blSequenceNo = sProperties[5];

			String DisplayPartNo = partType + " " + partNo;

			if (itemOption != null) {
				// E/ITEM Option & Description
				HashMap<String, Object> option = SDVBOPUtilities.getVariant(itemOption);

				// option code
				dataMap.put("itemOptionCode", option.get("printValues"));

				// option description
				dataMap.put("itemOptionDescription", option.get("printDescriptions"));

				// pos description
				dataMap.put("posDesc", posDesc);

				dataMap.put("module", module);
				
				// [20151116] taeku.jeong 양권석과장(조립1팀)의 요청으로 할당된 M-BOM BOM Line의 Sequence No를 End Item에 추가
				dataMap.put("productSeqNo", blSequenceNo);
				
			}
			dataMap.put("DisplayPartNo", DisplayPartNo);

			// Function Master
			TCComponentBOMLine fcBOMLine = srcBOMLine.parent();
			if (fcBOMLine != null) {
				// Function No
				String funcNo = fcBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				dataMap.put("funcNo", funcNo);
			}
		}

		return dataMap;
	}

	private String getNameProperty(String owning_user) {
		String name = owning_user.split(" ")[0];

		return name;
	}

	private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
		IDataSet dataSet = new DataSet();
		IDataMap dataMap = new RawDataMap();
		dataMap.put(dataName, dataList, IData.TABLE_FIELD);
		dataSet.addDataMap(dataName, dataMap);

		return dataSet;
	}

    public ArrayList<EndItemData> findReplacedEndItems(String puid, String productId) throws Exception {
        CustomBOPDao dao = new CustomBOPDao();
        ArrayList<EndItemData> replacedEndItemList = dao.findReplacedEndItems(puid, productId);

        return replacedEndItemList;
    }

}
