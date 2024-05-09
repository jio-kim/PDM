package com.symc.plm.rac.prebom.masterlist.operation;

import java.awt.Dimension;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.kgm.common.remote.DataSet;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.commands.variantconditionset.ConditionVector;
import com.kgm.common.SYMCClass;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMStringUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.dcs.common.DCSCommonUtil;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.ValidationDlg;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.model.MasterListDataMapper;
import com.symc.plm.rac.prebom.masterlist.model.SimpleTcObject;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.masterlist.util.WebUtil;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.SYMCBOMWindow;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.services.internal.rac.structuremanagement.RestructureService;
import com.teamcenter.services.internal.rac.structuremanagement._2014_12.Restructure.ReplaceItemsParameter;


/**
 * [20160602][ymjang] Carry Over Part 인데 Part Unique No 가 없을 경우, Validation Check
 * [20160602][ymjang] Carry Over Part 의 경우는 CCN No 속성이 없음 --> 오류 개선
 * [20160622][ymjang] 대표수량과 USAGE 수량이 다를 경우 Validation
 * [20160622][ymjang] Number Formaat 이 아닐 경우, Validation 체크
 * [20160715][ymjang] CCN NO 저장 안된 Part 가 존재함. --> 오류 개선
 * [20160705][ymjang] Lock/Save/Unlock 구문 삭제
 * [20160707][ymjang] BOM Saving 시 Validation 최소화
 * [20160713][ymjang] Part Name 존재 여부 Validation 삭제
 * [20160907][ymjang] 컬럼명 오류 수정
 * [20161004][ymjang] S/Mode 필수입력 체크 추가
 * [20161017][ymjang] 아이템 생성 및 Revise 이후 Refresh 구문 추가 (setProperties 시에 저장이 안됨)
 * [20161018][ymjang] 저장후 MLM 초기화 오류 수정(DCS 항목이 수정이 된 것처럼 Orange 색상으로 표현됨.)
 * [20161019][ymjang] FMP Revise 될 경우, BOM Line 속성 Update 시 Invalid Tag 오류 발생
 * [20161209][ymjang] not null --> null 로 변경시 저장오류 수정
 * [20170215] CARRY OVER 로 변경될 경우, Carry Over Revision 으로 Replace 함
 * [20170215] 옵션이 있으면 Usage 수량 필수 입력 체크 PASS
 * [20170312][ymjang] SPEC Desc. 길이 제한 체크
 * [20170312][ymjang] DPV NEED QTY Number Formaat 이 아닐 경우, Validation 체크
 * [20170513][ljg] nmcd 값이 null이면 그전 리비전의 nmcd를 가져오도록
 * [20170519][ljg] 다른 프로젝트의 CarryOver 파트를 리바이즈 하면, 프로젝트 코드가 변경 되는 문제 가 있어서 수정 함
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가 대응
 * [20180104][ljg]cd 값이 있을땐 nm은 null이어야함(반대로 nm이있을경우 CD는 null이어야함)
 * [SR171227-049][LJG] N,M,C,D에서 M을 -> M1,M2로 세분화
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 * [20180223][LJG] 무조건 system code 는 필수 - 송대영 차장 요청
 * [SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
 * @author slobbie
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BOMUpdateOperation extends AbstractAIFOperation {

	private MasterListDlg dlg = null;
	private MasterListTablePanel masterListTablePanel = null;
	private WaitProgressBar waitBar = null;
	private TCComponentItemRevision fmpRevision = null, ccnRevision = null;
	private String fmpId = null;
	private DefaultTableModel model = null;
	private String project = null;
	private MasterListDataMapper releaseDataMapper = null;
	private MasterListDataMapper dataMapper = null;
	private TCSession session = null;
	private HashMap<String, Vector> keyRowMapper = null;
	private HashMap<String, StoredOptionSet> storedOptionSetMap = null;
	private TCComponentBOMLine topLine = null;

	private HashMap<String, HashMap<String, Object>> changedData = new HashMap();
	private HashMap<String, HashMap<String, Object>> addedData = new HashMap();
	private ArrayList<String> deleteData = new ArrayList<String>();
	private HashMap<String, Vector> masterKeyRowMapper = new HashMap();
	private HashMap<String, Integer> systemKeyMapper = new HashMap();

	private static ValidationDlg validationDlg = null;

	//	private static final String MSG_INPUT_PART_ID = "Input a Part ID.";
	private static final String MSG_NOT_EXIST_PART_ID = "Not exist Part ID.";
	private static final String MSG_DUPLICATED_PART_ID = "Duplicated Part ID.";
	private static final String MSG_INPUT_SYSTEM_CODE = "Input a System Code.";
	private static final String MSG_INPUT_LEV_MAN = "Input a LEV(M).";
	private static final String MSG_INPUT_SEQ_NO = "Input a Sequence No.";
	private static final String MSG_INPUT_SUPPLY_MODE = "Input a Supply Mode.";
	private static final String MSG_DUPLICATED_SEQ_NO = "Duplicated Sequence No.";
	private static final String MSG_INVALID_SEQ_NO = "Invalid Sequence No.";
	private static final String MSG_INPUT_DISPLAY_PART_ID = "Input a Display Part No.";
	private static final String MSG_INPUT_PART_NAME = "Input a Part Name.";
	private static final String MSG_INVALID_PART_NAME = "Invalid Part Name.";
	private static final String MSG_SELECT_TYPE = "Select a Type.";
	private static final String MSG_SELECT_PROJECT_TYPE = "Select a Project Type.";
	private static final String MSG_EXCEED_MAXIMUM_SIZE  = "Exceed Maximum Size of Spec Description.";
	private static final String MSG_INPUT_REPRESENTATIVE_QUANTITY = "Input a Representative quantity.";
	private static final String MSG_NOT_FOUND_PARENT_NO = "Could not found Parent No.";
	private static final String MSG_IS_STD_PART = "Parent Part can not be Standard Part.";
	private static final String MSG_IS_VEH_PART = "Parent Part can not be Vehicle Part.";
	private static final String MSG_INPUT_USAGE_QUANTITY = "Input a Usage Quantity.";
	private static final String MSG_INPUT_EST_WEIGHT = "Input a Estimate weight.";
	private static final String MSG_INVALID_DATE_TYPE = "Invalid Date type. EX) 2015-05-05";
	private static final String MSG_CIRCLE_REFERENCE = "There was a circular reference.";
	private static final String MSG_INVALID_UOM = "Invalid Unit of Measure.";
	private static final String MSG_INPUT_RESPONSIBILITY = "Input a Responsibility.";
	private static final String MSG_INPUT_RESPONSIBILITY_DEPT = "Input a responsibility department.";
	private static final String MSG_INPUT_PERSON_IN_CHARGE = "Input a person in charge.";
	private static final String MSG_INVALID_DVP_NEEDED_QTY = "DPV Sample Necessary Qty is invalid Number.";
	private static final String MSG_INVALID_PERSON_IN_CHARGE = "Invalid a person or team in charge.";
	private static final String MSG_INPUT_CARRY_OVER_PART_ID = "Carry Over Part Input Error!. Click RMB and seleect Carry Over Menu in Part No Cell.";
	private static final String MSG_INPUT_ERROR_USAGE_QUANTITY = "Usage Quantity and Representative Quantity does not match.";
	private static final String MSG_IS_STD_VEH_PART = "Parent Part can not be Standard or Vehicle Part.";
	//20201021 seho EJS Column 오류 메시지.
	private static final String MSG_INPUT_EJS = "EJS is required. When the NMCD field is 'C' and the supply mode is C0, C1, C7, CD, C7UC8, C7YC8, P0, P1, P7, PD, P7UP8, P7YP8, P7MP8 and the DR is DR1, DR2, DR3";
	//20201105 seho EJS Column 입력못하도록 메시지.
	private static final String MSG_INPUT_NOT_EJS = "Do not enter EJS.";

	private HashMap<String, Object> resultData = new HashMap();
	private String product_project_code;

	public BOMUpdateOperation(MasterListDlg dlg, WaitProgressBar waitBar, 
			MasterListDataMapper dataMapper, MasterListDataMapper releaseDataMapper, 
			HashMap<String, StoredOptionSet> storedOptionSetMap, String product_project_code){
		this.dlg = dlg;
		this.masterListTablePanel = dlg.getMasterListTablePanel();
		this.waitBar = waitBar;
		this.dataMapper = dataMapper;
		this.releaseDataMapper = releaseDataMapper;
		this.storedOptionSetMap = storedOptionSetMap;
		this.keyRowMapper = dlg.getKeyRowMapper();
		this.product_project_code = product_project_code;
	}

	@Override
	public void executeOperation() throws Exception {

		/* ************************************************************************************************
		 * 1. Start
		 * ************************************************************************************************ */
		storeOperationResult(resultData);
		waitBar.setStatus("Checking Validation....");

		TCComponentItemRevision curFmpRevision = dlg.getFmpRevision();
		fmpRevision = curFmpRevision;
		session = curFmpRevision.getSession();
		fmpId = fmpRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);

		InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
		if( targets != null && targets.length > 0){
			if( targets[0] instanceof TCComponentBOMLine){
				waitBar.setShowButton(true);
				waitBar.setStatus("Close the opened BOM Window.");
				return;
			}
		}

		/* ************************************************************************************************
		 * 2. Validation
		 * ************************************************************************************************ */
		
		Vector<Vector> validateResult = new Vector();
		if( !validateModelInfo(validateResult)){

			resultData.put(BOMLoadWithDateOperation.DATA_ERROR, new Exception("Validation Fail!"));

			waitBar.dispose();
			if( validationDlg != null){
				validationDlg.dispose();
				validationDlg = null;
			}

			validationDlg = new ValidationDlg( dlg, validateResult);
			validationDlg.setPreferredSize(new Dimension(100, 100));
			validationDlg.setVisible(true);
			return;
		}

		//		TCComponentBOMLine pseFmpLine = dlg.getPseFmpLine();
		//		if( pseFmpLine.window().isModified()){
		//			waitBar.setShowButton(true);
		//			waitBar.setStatus("BOM Window is modified. Can not update...");
		//			return;
		//		}

		/* ************************************************************************************************
		 * 3. Save
		 * ************************************************************************************************ */
		//Markpoint markPoint = new Markpoint(session);

		model = (DefaultTableModel)dlg.getMasterListTablePanel().getTable().getModel();
		SimpleTcObject simpleCCN = (SimpleTcObject)dlg.getCbCCN().getSelectedItem();

		if (waitBar != null) {
			waitBar.setStatus("CCN Loading...");
		}

		ccnRevision = (TCComponentItemRevision)session.stringToComponent( simpleCCN.getPuid());
		project = dlg.getProject();

		try{

			// Release BOM 과 MasterList의 BOM을 비교하고 변경된 내역을 가져온다.
			ArrayList<String> mapperKeyList = (ArrayList<String>)dataMapper.getKeyList().clone();
			ArrayList<String> masterKeyList = new ArrayList();
			HashMap<String, Object> changedProp = null;

			if (waitBar != null) {
				waitBar.setStatus("Loading Change Information...");
			}
			

			/* ************************************************************************************************
			 * 3-1. 변경되었거나 추가된 목록 생성
			 * ************************************************************************************************ */
			masterKeyRowMapper.clear();
			for( int row = 0; row < model.getRowCount(); row++){

				changedProp = null;
				HashMap<String, Object> masterPropMap = getProp(model, row);
				
				
				String key = getBomKey(masterPropMap);
				systemKeyMapper.put(key, row);
				Vector<CellValue> rowVec = MasterListDataMapper.getMasterListRow(masterPropMap, dlg.getOspec(), null, null, 0);
				if( rowVec != null){
					//저장되는 값이 아닌 로직에 의한 값은 복사를 해둠.
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SYSTEM_NAME_IDX))
							, MasterListTablePanel.MASTER_LIST_SYSTEM_NAME_IDX + 1);//System Name copy
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_FUNCTION_IDX))
							, MasterListTablePanel.MASTER_LIST_FUNCTION_IDX + 1);//Function copy
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_LEV_A_IDX))
							, MasterListTablePanel.MASTER_LIST_LEV_A_IDX + 1);//Level copy
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX))
							, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX + 1);//Parent copy
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX))
							, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX + 1);//필수 옵션

					//[20161018][ymjang] 저장후 MLM 초기화 오류 수정(DCS 항목이 수정이 된 것처럼 Orange 색상으로 표현됨.)
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DESIGN_DOC_NO_IDX))
							, masterListTablePanel.MASTER_LIST_DESIGN_DOC_NO_IDX + 1);//DCS 번호
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DESIGN_REL_DATE_IDX))
							, masterListTablePanel.MASTER_LIST_DESIGN_REL_DATE_IDX + 1);// DCS 승인일자
					rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, masterListTablePanel.MASTER_LIST_OSPEC_NO_IDX))
							, masterListTablePanel.MASTER_LIST_OSPEC_NO_IDX + 1);// OSPEC NO

					masterKeyRowMapper.put(key, rowVec);
				}

				masterKeyList.add(key);

				if( dataMapper != null){
					HashMap<String, Object> propMap = dataMapper.getPropertyMap(key);

					//새로 추가되었거나 BOM Key값(ParentNO + Sequence + Part No)이 바뀐 경우
					if( propMap == null){
						addedData.put(key, masterPropMap);
						continue;
					}

					changedProp = getChangedProp(masterPropMap, propMap);

					//Parent No가 바뀐 경우는 삭제도 포함됨.
					//기존 BOM Line는 삭제되어야함.
					if( changedProp.containsKey("PARENT_NO")){

						keyRowMapper.remove(key);
						masterKeyList.remove(key);
						masterKeyRowMapper.remove(key);
						systemKeyMapper.remove(key);

						String newSystemRowKey = BomUtil.getNewSystemRowKey();
						masterPropMap.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, newSystemRowKey);
						addedData.put(newSystemRowKey, masterPropMap);
						masterKeyList.add(newSystemRowKey);
						systemKeyMapper.put(newSystemRowKey, row);

						rowVec = MasterListDataMapper.getMasterListRow(masterPropMap, dlg.getOspec(), null, null, 0);
						if( rowVec != null){
							//저장되는 값이 아닌 로직에 의한 값은 복사를 해둠.

							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SYSTEM_NAME_IDX))
									, MasterListTablePanel.MASTER_LIST_SYSTEM_NAME_IDX + 1);//System Name copy
							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_FUNCTION_IDX))
									, MasterListTablePanel.MASTER_LIST_FUNCTION_IDX + 1);//Function copy
							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_LEV_A_IDX))
									, MasterListTablePanel.MASTER_LIST_LEV_A_IDX + 1);//Level copy
							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX))
									, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX + 1);//Parent copy
							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX))
									, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX + 1);//필수 옵션

							//[20161018][ymjang] 저장후 MLM 초기화 오류 수정(DCS 항목이 수정이 된 것처럼 Orange 색상으로 표현됨.) 
							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DESIGN_DOC_NO_IDX))
									, masterListTablePanel.MASTER_LIST_DESIGN_DOC_NO_IDX + 1);//DCS 번호
							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DESIGN_REL_DATE_IDX))
									, masterListTablePanel.MASTER_LIST_DESIGN_REL_DATE_IDX + 1);// DCS 승인일자
							rowVec.setElementAt( MasterListDataMapper.convertToCellValue( model.getValueAt(row, masterListTablePanel.MASTER_LIST_OSPEC_NO_IDX))
									, masterListTablePanel.MASTER_LIST_OSPEC_NO_IDX + 1);// OSPEC NO
							masterKeyRowMapper.put(newSystemRowKey, rowVec);
						}

						continue;
					}
				}

				/* [20160715][ymjang] CCN NO 저장 안된 Part 가 존재함. --> 오류 개선
				/* Release BOM 과 비교시 변경된 내역이 없을 경우, CCN NO 를 초기화 수행
				 * --> BOM Line 정보 수정 -> 원복 -> 리비전 속성 변경 순으로 작업을 진행할 경우, Parent 의 Latest Working Revision 의 CCN NO 가 초기화됨. 
				 */ 
				/*
				if( releaseDataMapper != null){
					HashMap<String, Object> releasePropMap = releaseDataMapper.getPropertyMap(key);
					if( releasePropMap != null){
						//현재의 MasterList와 Release상태의 MasterList를 비교.
						HashMap<String, Object> releaseChangedProp = getChangedProp(masterPropMap, releasePropMap);
						if( releaseChangedProp.isEmpty()){
							//BOM Line 및 해당 Revision의 속성이 릴리즈 전과 같다.
							//Revision을 Revise 했을 경우, CCN을 삭제한다.
							//변경된 Property 내역이 없고, 현재의 리비전(Latest Revision)이 Working상태 일 경우,
							//해당 리비전의 CCN을 Null로 셋팅한다.
							HashMap<String, ArrayList<TCComponentBOMLine>> bomLineMap = dataMapper.getBomlineMap().get(key);
							String[] occThreads = bomLineMap.keySet().toArray(new String[bomLineMap.size()]);
							ArrayList<TCComponentBOMLine> tmpBaseLines = bomLineMap.get(occThreads[0]);
							TCComponentBOMLine tmpBaseLine = tmpBaseLines.get(0);
							TCComponentItemRevision curRevision = tmpBaseLine.getItem().getLatestItemRevision();
							// [20160602][ymjang] Carry Over Part 의 경우는 CCN No 속성이 없음 --> 오류 개선
							String objectType = curRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMTYPE);
							if (!objectType.equals("Vehicle Part") && !objectType.equals("Standard Part")) {
								if( !CustomUtil.isReleased(curRevision)){
									TCProperty tcProp = curRevision.getTCProperty("s7_CCN_NO");
									tcProp.setReferenceValue(null);
								}
							}
							TCComponentBOMLine parentLine = tmpBaseLine.parent();
							// [20160602][ymjang] Carry Over Part 의 경우는 CCN No 속성이 없음 --> 오류 개선
							TCComponentItemRevision parentRevision = parentLine.getItem().getLatestItemRevision();
							objectType = parentRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMTYPE);
							if (!objectType.equals("Vehicle Part") && !objectType.equals("Standard Part")) {
								if( !CustomUtil.isReleased(parentRevision)){
									TCProperty tcProp = parentRevision.getTCProperty("s7_CCN_NO");
									tcProp.setReferenceValue(null);
								}
							}

							//변경된 BOM Line속성 변경 내역이 없고, Parent 리비전(Latest Revision)이 Working상태 일 경우,
							//Parent 리비전의 CCN을 Null로 셋팅한다.
						}else{
							//Release와 비교하여 변경된 내용이 발생.
							//BOM Line 속성이 변경되었을 경우, Parent Revision이 Revise되고,
							//revision property가 변경 되었을 경우, 해당 Revision이 Revise된다. 
						}
					}
				}
				 */

				if( !changedProp.isEmpty()){
					changedData.put(key, changedProp);
				}

			}

			/* ************************************************************************************************
			 * 3-2. 삭제된 목록 생성
			 * ************************************************************************************************ */
			//삭제될 DATA
			mapperKeyList.removeAll(masterKeyList);
			deleteData.addAll( mapperKeyList );

			topLine = dlg.getWorkingFmpTopLine();

			/* ************************************************************************************************
			 * 3-3. Change
			 * ************************************************************************************************ */
			// 3-3-1. 수량이 변경된 경우를 먼저 처리하여 이후에 처리되는 BOM Line속성 변경이 반영되도록 함.

			//BOM Line정보가 변경되어야 하고, 해당 Parent Line이 Release상태라면 Revise해야한다.
			//FMP Revision을 Top으로 BOM Window 생성(Latest Working)
			//BOM Line을 순차적으로 읽고, 변경 내용이 존재하면 이를 반영한다.
			changeQty(changedData);

			// 3-3-2. Change
			// BOM Line 정보가 변경된 경우에만 처리해야 하며, Working 상태의 FMP Revision이어야함.
			// 체크 필요.
			change(changedData);

			/* ************************************************************************************************
			 * 3-4. Add
			 * ************************************************************************************************ */
			// 3-4-1. add될 아이템들 중에 Create될 아이템을 먼저 생성함.
			createItems(addedData);

			// 3-4-2. 추가된 아이템을 구성(ADD).
			HashMap<String, ArrayList<String>> structureMap = getStructure(addedData);
			String[] partIds = structureMap.keySet().toArray(new String[structureMap.size()]);
			ArrayList<String> parentList = new ArrayList();
			for( int i = 0; partIds != null && i < partIds.length; i++){
				parentList.add(partIds[i]);
			}
			int idx = 0;
			while(!parentList.isEmpty()){
				String parentId = parentList.get(idx);

				//Parent BOM Line이 존재하지 않을 경우, 다른 구조부터 생성한다.
				if( !createStructure(addedData, structureMap, parentId)){
					if( idx + 1 < parentList.size()){
						idx++;
						continue;
					}else{
						throw new Exception("Could not find Parent Part[" + parentId + "]");
					}
				}else{
					parentList.remove(idx);
					idx = 0;
				}
			}
			//윈도우가 닫히는 경우도 발생하므로. 변경 사항을 우선 저장
			topLine.window().save();

			/* ************************************************************************************************
			 * 3-5. Delete BOM Line
			 * ************************************************************************************************ */
			//Parent 가 바뀐 경우도 delete or add 가 발생 할 수 있다.
			ArrayList<String> clonedDeleteData = (ArrayList<String>)deleteData.clone();
			//			HashMap<String, ArrayList<String>> parentChildRowKeyMap = dataMapper.getChildRowKeyMap();

			//자파트를 포함하여 여러 행을 선택 후 삭제시,
			//하위를 분해하지 않기 위해.
			//			for( String deleteKey : deleteData){
			//				ArrayList<String> childKeys = getChildRowKeys(deleteKey);
			//				clonedDeleteData.removeAll(childKeys);
			//			}
			remove(topLine, clonedDeleteData);

			//삭제된 BOM Line 하위에 있던 Child Line의 정보를 삭제함.
			deleteData.removeAll(clonedDeleteData);
			HashMap<String, HashMap<String, ArrayList<TCComponentBOMLine>>> bomLineMap = dataMapper.getBomlineMap();
			if( !deleteData.isEmpty()){
				for( String deleteKey : deleteData){
					HashMap<String, ArrayList<TCComponentBOMLine>> map = bomLineMap.get(deleteKey);
					String[] occThreads = map.keySet().toArray(new String[map.size()]);
					for( String occThread : occThreads){
						ArrayList<TCComponentBOMLine> list = map.get(occThread);
						if( list != null){
							for( int i = list.size() - 1; i >= 0; i--){
								TCComponentBOMLine line = list.get(i);
								if( line == null || !line.isValidUid()){
									list.remove(i);
									if( list.isEmpty()){
										map.remove(occThread);
									}
								}
							}
						}
					}
					if( map.isEmpty()){
						dataMapper.removePropertMap(deleteKey, false);
					}
				}
			}

			/* ************************************************************************************************
			 * 3-6. 변경 완료
			 * ************************************************************************************************ */
			// 3-6-1. 변경 완료된 내용을 keyRowMapper에 반영.
			String[] keys = masterKeyRowMapper.keySet().toArray(new String[masterKeyRowMapper.size()]);
			keyRowMapper.clear();
			for(int i = 0; keys != null && i < keys.length; i++){
				keyRowMapper.put(keys[i], masterKeyRowMapper.get(keys[i]));
			}

			// 3-6-2. set CCN No
			if( !CustomUtil.isReleased( fmpRevision )){
				fmpRevision.refresh();
				fmpRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);
				//				TCProperty tcProp = fmpRevision.getTCProperty("s7_CCN_NO");
				//				tcProp.setReferenceValue(ccnRevision);
			}

			topLine.window().save();
			waitBar.dispose();
			//markPoint.forget();

		}catch(Exception e){
			//markPoint.rollBack();

			e.printStackTrace();
			waitBar.setShowButton(true);
			waitBar.setStatus(e.getMessage());
		}finally{
		}
	}

	private HashMap<String, Object> getChangedProp(HashMap<String, Object> masterMap, HashMap<String, Object> propMap){
		HashMap<String, Object> changedProp = new HashMap(); 
		String[] masterKeys = masterMap.keySet().toArray(new String[masterMap.size()]);

		for( String key : masterKeys){
			if( !propMap.containsKey(key)){
				changedProp.put(key,  masterMap.get(key));
			}

			Object masterValue = masterMap.get(key);
			if( masterValue == null){
				masterValue = "";
			}
			Object releaseValue = propMap.get(key);
			if( releaseValue == null){
				releaseValue = "";
			}

			if( !masterValue.equals(releaseValue)){
				changedProp.put(key, masterValue);
			}
		}
		return changedProp;
	}

	private String getBomKey(HashMap<String, Object> map) throws TCException{
		return (String)map.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
	}

	private boolean setItemProp(HashMap<String, Object> map, TCComponentItem item) throws TCException{

		HashMap<String, String> propMap = new HashMap();
		if( map.containsKey(PropertyConstant.ATTR_NAME_UOMTAG)){
			propMap.put(PropertyConstant.ATTR_NAME_UOMTAG, BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_UOMTAG)));
		}

		if( !propMap.isEmpty()){
			// [20160705][ymjang] Lock/Save/Unlock 구문 삭제
			//item.lock();
			item.setProperties(propMap);
			//item.save();
			//item.unlock();
			return true;
		}

		return false;
	}

	/**
	 * Revision 속성 업데이트. 추가적으로 Item의 UOM 수정.
	 * 
	 * @param map
	 * @param revision
	 * @param isBasicPropSet
	 * @return
	 * @throws Exception
	 */
	private boolean setRevProp(HashMap<String, Object> map, TCComponentItemRevision revision, boolean isBasicPropSet) throws Exception{
		boolean isApplied = false;
		HashMap<String, String> propMap = new HashMap();

		if( isBasicPropSet ){
			propMap.put(PropertyConstant.ATTR_NAME_MATURITY, "In Work");
			propMap.put(PropertyConstant.ATTR_NAME_PARTTYPE, "K");
			propMap.put(PropertyConstant.ATTR_NAME_STAGE, "C");
			propMap.put(PropertyConstant.ATTR_NAME_PROJCODE, project);
			propMap.put(PropertyConstant.ATTR_NAME_COLORID, "");
			propMap.put(PropertyConstant.ATTR_NAME_SELECTIVEPART, "");
			propMap.put(PropertyConstant.ATTR_NAME_REGULATION, ".");
		}

		if( map.containsKey(PropertyConstant.ATTR_NAME_CONTENTS)){
			propMap.put(PropertyConstant.ATTR_NAME_CONTENTS, BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_CONTENTS)));
		}

		if( map.containsKey(PropertyConstant.ATTR_NAME_OLD_PART_NO)){
			propMap.put(PropertyConstant.ATTR_NAME_OLD_PART_NO, BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_OLD_PART_NO)));
		}

		// Part/NO ==> Display No로 셋.
		if( map.containsKey(PropertyConstant.ATTR_NAME_DISPLAYPARTNO)){
			propMap.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO)));
		}
		//아이템 명
		if( map.containsKey(PropertyConstant.ATTR_NAME_ITEMNAME)){
			propMap.put(PropertyConstant.ATTR_NAME_ITEMNAME, BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_ITEMNAME)));
		}
		//System Code
//		[20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
//		if( map.containsKey(PropertyConstant.ATTR_NAME_BUDGETCODE)){
//			propMap.put(PropertyConstant.ATTR_NAME_BUDGETCODE, BomUtil.convertToString(  map.get(PropertyConstant.ATTR_NAME_BUDGETCODE)));
//		}
		//change Desc
		if( map.containsKey(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION)){
			propMap.put(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION) ));
		}
		//Estimate Cost
		if( map.containsKey(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL)){
			propMap.put(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL) ));
		}
		//Target Cost
		if( map.containsKey(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL)){
			propMap.put(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL) ));
		}
		//Master list의 NMCD값 중 NM일 경우, 아이템 리비전 속성으로 저장.
		if( map.containsKey(PropertyConstant.ATTR_NAME_CHG_TYPE_NM)){
			propMap.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_CHG_TYPE_NM) ));
		}
		//양산차종
		if( map.containsKey(PropertyConstant.ATTR_NAME_PROJCODE)){
			propMap.put(PropertyConstant.ATTR_NAME_PROJCODE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_PROJCODE) ));

			//NMCD값이 M인 경우는 Prd Project Code에도 입력함.
			// 1. Item Revision 속성이 M이었는지 확인
			String sChangeTypeNM = revision.getProperty(PropertyConstant.ATTR_NAME_CHG_TYPE_NM);

			if (sChangeTypeNM.contains("M")) {
				// 2. NMCD값이 기존에 M이었으면서 다른 NMCD로 변경되지 않았을 경우 (M을 계속 유지해 나갈 경우) 양산 프로젝트 코드를 함께 변경해준다.
				if( !propMap.containsKey(PropertyConstant.ATTR_NAME_CHG_TYPE_NM)) {
					propMap.put(PropertyConstant.ATTR_NAME_PRD_PROJ_CODE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_PROJCODE)));
				}
			} else {
				// 3. NMCD값이 기존에 M이 아니었으면서 새롭게 M으로 변경되는 경우 양산 프로젝트 코드를 함께 변경해준다.
				if( propMap.containsKey(PropertyConstant.ATTR_NAME_CHG_TYPE_NM) && propMap.get(PropertyConstant.ATTR_NAME_CHG_TYPE_NM).contains("M")){
					propMap.put(PropertyConstant.ATTR_NAME_PRD_PROJ_CODE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_PROJCODE)));
				}
			}
		}
		//현재 예상 Weight
		if( map.containsKey(PropertyConstant.ATTR_NAME_ESTWEIGHT)){
			propMap.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_ESTWEIGHT) ));
		}
		//목표 Weight
		if( map.containsKey(PropertyConstant.ATTR_NAME_TARGET_WEIGHT)){
			propMap.put(PropertyConstant.ATTR_NAME_TARGET_WEIGHT, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_TARGET_WEIGHT) ));
		}
		//DR
		if( map.containsKey(PropertyConstant.ATTR_NAME_DR)){
			propMap.put(PropertyConstant.ATTR_NAME_DR, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_DR) ));
		}
		//BOX
		if( map.containsKey(PropertyConstant.ATTR_NAME_BOX)){
			propMap.put(PropertyConstant.ATTR_NAME_BOX, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BOX) ));
		}

		//Concept Dwg 계획
		if( map.containsKey(PropertyConstant.ATTR_NAME_CON_DWG_PLAN)){
			propMap.put(PropertyConstant.ATTR_NAME_CON_DWG_PLAN, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_CON_DWG_PLAN) ));
		}
		//Concept Dwg 실적
		if( map.containsKey(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE)){
			propMap.put(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE) ));
		}
		//Concept Dwg 2D/3D
		if( map.containsKey(PropertyConstant.ATTR_NAME_CON_DWG_TYPE)){
			propMap.put(PropertyConstant.ATTR_NAME_CON_DWG_TYPE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_CON_DWG_TYPE) ));
		}
		//도면 배포 예정일 셋.
		if( map.containsKey(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE)){
			// [20161209][ymjang] not null --> null 로 변경시 저장오류 수정
			propMap.put(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE) ));
			//			Object obj = map.get(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE);
			//			if( obj != null && !obj.equals("")){
			//				propMap.put(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE) ));
			//			}
		}
		//도면 작성(양산) 계획
		if( map.containsKey(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN)){
			propMap.put(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN) ));
		}
		//도면 작성(양산) 실적
		if( map.containsKey(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE)){
			propMap.put(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE) ));
		}
		//도면 작성(양산) ECO/NO
		if( map.containsKey(PropertyConstant.ATTR_NAME_ECO_NO)){
			propMap.put(PropertyConstant.ATTR_NAME_ECO_NO, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_ECO_NO) ));
		}	
		/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
		//		//DVP SAMPLE 필요수량
		//		if( map.containsKey(PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY)){
		//			propMap.put(PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY) ));
		//		}
		//		//용도
		//		if( map.containsKey(PropertyConstant.ATTR_NAME_DVP_USE)){
		//			propMap.put(PropertyConstant.ATTR_NAME_DVP_USE, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_DVP_USE) ));
		//		}
		//		//요청 부서
		//		if( map.containsKey(PropertyConstant.ATTR_NAME_DVP_REQ_DEPT)){
		//			propMap.put(PropertyConstant.ATTR_NAME_DVP_REQ_DEPT, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_DVP_REQ_DEPT) ));
		//		}

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		//		//설계담당 팀
		//		if( map.containsKey(PropertyConstant.ATTR_NAME_ENG_DEPT_NM)){
		//			propMap.put(PropertyConstant.ATTR_NAME_ENG_DEPT_NM, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_ENG_DEPT_NM) ));
		//		}
		//		//설계담당 담당
		//		if( map.containsKey(PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY)){
		//			propMap.put(PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY, BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY) ));
		//		}

		if( !propMap.isEmpty()){
			if( CustomUtil.isReleased(revision)){
				revision = revise(revision);
			}
			/* [20160715][ymjang] CCN NO 저장 안된 Part 가 존재함. --> 오류 개선
			else{
				TCProperty tcProp = revision.getTCProperty(PropertyConstant.ATTR_NAME_CCNNO);
				tcProp.setReferenceValue(ccnRevision);
			}
			 */

			if (waitBar != null) {
				waitBar.setStatus(revision.getProperty(PropertyConstant.ATTR_NAME_ITEMID) 
						+ "/" + revision.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID) + " Revision Properties Updating...");
			}

			if( propMap.containsKey(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE)){
				TCProperty tcProp = revision.getTCProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE);
				SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
				String dateStr = propMap.get(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE).toString();
				if( dateStr.equals("")){
					tcProp.setPropertyData(null);
				}else{
					tcProp.setPropertyData(sdf.parse(dateStr));
				}
				propMap.remove(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE);
			}

			if( propMap.containsKey(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL) || propMap.containsKey(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL)){
				TCComponent refComp = revision.getReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF);
				if( refComp != null){
					if( propMap.containsKey(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL)){
						refComp.setProperty(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, propMap.get(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL));
					}

					if( propMap.containsKey(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL)){
						refComp.setProperty(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, propMap.get(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL));
					}
				}else{
					ArrayList<String> attrNames = new ArrayList();
					ArrayList<String> attrValues = new ArrayList();
					if( propMap.containsKey(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL)){
						attrNames.add(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL);
						attrValues.add(propMap.get(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL));
					}
					if( propMap.containsKey(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL)){
						attrNames.add(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL);
						attrValues.add(propMap.get(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL));
					}
					refComp = SYMTcUtil.createApplicationObject(revision.getSession(), TypeConstant.S7_PREVEHTYPEDREFERENCE, attrNames.toArray(new String[attrNames.size()]), attrValues.toArray(new String[attrValues.size()]));
					revision.setReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, refComp);

					// [20160705][ymjang] Lock/Save/Unlock 구문 삭제
					//revision.lock();
					//TCProperty tcProp = revision.getTCProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF);
					//tcProp.setReferenceValue(refComp);
					//revision.save();
					//revision.unlock();
				}
				propMap.remove(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL);
				propMap.remove(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL);
			}

			//날짜 셋팅이 삭제되어 빈값일 수도 있다.
			if( !propMap.isEmpty()){

				// [20160705][ymjang] Lock/Save/Unlock 구문 삭제
				//revision.lock();
				revision.refresh();
				revision.setProperties(propMap);

				// [20170513][ljg] nmcd 값이 null이면 그전 리비전의 nmcd를 가져오도록
				// nmcd 값은 null일 수 없음
				//[20180104][ljg]다시 주석 처리함 cd 값이 있을땐 nm은 null이어야함
//				if(CustomUtil.isNullString(propMap.get(PropertyConstant.ATTR_NAME_CHG_TYPE_NM))){
//					if(!revision.getStringProperty("item_revision_id").equals("000")){
//						String nm = CustomUtil.getPreviousRevision(revision).getStringProperty(PropertyConstant.ATTR_NAME_CHG_TYPE_NM);
//						revision.setStringProperty(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, nm);
//					}
//				}

				// [20160715][ymjang] CCN NO 저장 안된 Part 가 존재함. --> 오류 개선
				revision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);

				//revision.save();
				//revision.unlock();
				isApplied = true;
			}
		}

		setItemProp(map, revision.getItem());

		return isApplied;
	}

	private HashMap<String, String> getBOMLineProp(TCComponentBOMLine line) throws TCException{
		HashMap<String, String> propMap = new HashMap();

		line.refresh();
		String systemRowKey = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY));
		if( systemRowKey != null && !systemRowKey.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, systemRowKey);
		}

		String module = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_MODULE_CODE));
		if( module != null && !module.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_MODULE_CODE, module);
		}

		String supplyMode = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE));
		if( supplyMode != null && !supplyMode.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE, supplyMode);
		}

		String sequenceNo = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO));
		if( sequenceNo != null && !sequenceNo.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO, sequenceNo);
		}

		String cd = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_CHG_CD));
		if( cd != null && !cd.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_CHG_CD, cd);
		}

		String alterPart = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_ALTER_PART));
		if( alterPart != null && !alterPart.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_ALTER_PART, alterPart);
		}

		String reqOpt = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_REQ_OPT));
		if( reqOpt != null && !reqOpt.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_REQ_OPT, reqOpt);
		}

		String levM = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_LEV_M));
		if( levM != null && !levM.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_LEV_M, levM);
		}

		String specDesc = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_SPEC_DESC));
		if( specDesc != null && !specDesc.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SPEC_DESC, specDesc);
		}

		/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
		String dvpNeedQty = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY));
		if( dvpNeedQty != null && !dvpNeedQty.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY, dvpNeedQty);
		}

		String dvpUse = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_DVP_USE));
		if( dvpUse != null && !dvpUse.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_DVP_USE, dvpUse);
		}

		String dvpReqDept = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT));
		if( dvpReqDept != null && !dvpReqDept.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT, dvpReqDept);
		}

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		String engDept = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM));
		if( engDept != null && !engDept.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM, engDept);
		}

		String engResponsibility = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY));
		if( engResponsibility != null && !engResponsibility.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY, engResponsibility);
		}
		
		/* [SR없음][20180424][CSH] 수량 증가시 System Code 값 누락으로 인한 오류 수정 */
		String systemCode = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE));
		if( systemCode != null && !systemCode.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_BUDGETCODE, systemCode);
		}
		
		/* [SR없음][20180424][CSH] 수량 증가시 Proto Tooling 값 누락으로 인한 오류 수정 */
		String protoTooling = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING));
		if( protoTooling != null && !protoTooling.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING, protoTooling);
		}

		//20200924 seho EJS Column 추가...
		String ejs = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_EJS));
		if( ejs != null && !ejs.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_EJS, ejs);
		}
		
		//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)
		String weightManagement = BomUtil.convertToString( line.getProperty(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT));
		if( weightManagement != null && !weightManagement.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT, weightManagement);
		}
		
		return propMap;
	}

	/**
	 * [20161209][ymjang] not null --> null 로 변경시 저장오류 수정
	 * @param map
	 * @param line
	 * @param rtnMap
	 * @return
	 * @throws Exception
	 */
	private boolean setBOMLineProp(HashMap<String, Object> map, TCComponentBOMLine line, HashMap<String, Boolean> rtnMap) throws Exception{
		HashMap<String, String> propMap = new HashMap();
		boolean isApplied = false;
		boolean isKeyContains = false;

		if( line.isPacked() ) {
			line.unpack();
		}

		String str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY));
		if( str != null && !str.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, str);
		}

		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_QUANTITY));
		if( str != null && !str.equals("")){
			propMap.put(PropertyConstant.ATTR_NAME_BL_QUANTITY, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_MODULE_CODE);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_MODULE_CODE));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_MODULE_CODE, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_CHG_CD);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_CHG_CD));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_CHG_CD, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_ALTER_PART);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_ALTER_PART));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_ALTER_PART, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_REQ_OPT);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_REQ_OPT));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_REQ_OPT, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_LEV_M);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_LEV_M));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_LEV_M, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_SPEC_DESC);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_SPEC_DESC));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_SPEC_DESC, str);
		}

		/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_DVP_USE);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_DVP_USE));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_DVP_USE, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT, str);
		}

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM, str);
		}

		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY);
		str = BomUtil.convertToString( map.get(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY, str);
		}
		
		//[SR170703-020][LJG]Proto Tooling 컬럼 추가
		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING);
		str = BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING, str);
		}

		//[20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);
		str = BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_BL_BUDGETCODE));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_BUDGETCODE, str);
		}
		
		//20200924 seho EJS Column 추가...
		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_EJS);
		str = BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_BL_EJS));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_EJS, str);
		}
		
		//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)
		isKeyContains = map.keySet().contains(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT);
		str = BomUtil.convertToString(map.get(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT));
		if( isKeyContains || (str != null && !str.equals(""))){
			propMap.put(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT, str);
		}

		rtnMap.put("isFmpRevised", false);
		if( !propMap.isEmpty()){
			TCComponentBOMLine parentLine = line.parent();

			//BOM Window를 새로 생성해야함.
			if( CustomUtil.isReleased( parentLine.getItem().getLatestItemRevision() )){

				if( fmpRevision.equals(parentLine.getItemRevision())){

					String systemRowKey = line.getProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
					String occThread = line.getProperty( PropertyConstant.ATTR_NAME_BL_OCC_THREAD);

					fmpRevision = revise(fmpRevision);

					//FIXME: 2016.12.07 FMP 개정시 Refresh 만 한다. TEST(OK)
					topLine.window().newIrfWhereConfigured(fmpRevision);
					topLine.window().fireChangeEvent();
					dlg.setWorkingFmpTopLine(topLine);

					//					//Command에서 생성한 BOM Window 닫기.
					//					dlg.getWorkingFmpTopLine().window().save();
					//					dlg.getWorkingFmpTopLine().window().close();
					//					topLine = CustomUtil.getBomline(fmpRevision, session);
					//					dlg.setWorkingFmpTopLine(topLine);
					//					topLine.window().refresh();
					//
					//					// dataMapper 다시 셋팅.
					//					dataMapper.initialize(topLine, dlg.getOspec(), dlg.getEssentialNames(), true);
					//					BOMLoadOperation.loadChildMap(dataMapper, topLine, storedOptionSetMap);

					HashMap<String, ArrayList<TCComponentBOMLine>> bomLineMap = dataMapper.getBomlineMap().get(systemRowKey);
					ArrayList<TCComponentBOMLine> lines = bomLineMap.get(occThread);
					line = lines.get(0);
					rtnMap.put("isFmpRevised", true);
				}else{
					TCComponentItemRevision newParentRevision = revise(parentLine.getItem().getLatestItemRevision());
					//FIXME: BOM Window refresh 수정. TEST(OK)
					//parentLine.window().refresh();
					parentLine.window().newIrfWhereConfigured(newParentRevision);
					parentLine.window().fireChangeEvent();
				}				
			}

			if (waitBar != null) {
				waitBar.setStatus(line.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID) + " BOM Line Properties Updating...");
			}

			line.refresh();
			Iterator<String> it = propMap.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				String value = propMap.get(key);
				line.setProperty(key, value);
			}
//			line.setProperties(propMap); 
			isApplied = true;
		}

		Object obj = map.get("SPEC");
		if( obj != null){
			TCComponentBOMLine parentLine = line.parent();
			if( CustomUtil.isReleased( parentLine.getItem().getLatestItemRevision() )){

				if( fmpRevision.equals(parentLine.getItemRevision())){

					String systemRowKey = line.getProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
					String occThread = line.getProperty( PropertyConstant.ATTR_NAME_BL_OCC_THREAD);

					fmpRevision = revise(fmpRevision);

					//FIXME: 2016.12.07 FMP 개정시 Refresh 만 한다. TEST(OK)
					topLine.window().newIrfWhereConfigured(fmpRevision);
					topLine.window().fireChangeEvent();
					dlg.setWorkingFmpTopLine(topLine);

					//					//Command에서 생성한 BOM Window 닫기.
					//					dlg.getWorkingFmpTopLine().window().save();
					//					dlg.getWorkingFmpTopLine().window().close();
					//					topLine = CustomUtil.getBomline(fmpRevision, session);
					//					dlg.setWorkingFmpTopLine(topLine);
					//					
					//					// dataMapper 다시 셋팅.
					//					dataMapper.initialize(topLine, dlg.getOspec(), dlg.getEssentialNames(), true);
					//					BOMLoadOperation.loadChildMap(dataMapper, topLine, storedOptionSetMap);

					HashMap<String, ArrayList<TCComponentBOMLine>> bomLineMap = dataMapper.getBomlineMap().get(systemRowKey);
					ArrayList<TCComponentBOMLine> lines = bomLineMap.get(occThread);
					line = lines.get(0);
				}else{
					TCComponentItemRevision newParentRevision = revise(parentLine.getItem().getLatestItemRevision());
					//FIXME: 2016.12.07 BOM Window refresh 수정. TEST(OK)
					//parentLine.window().refresh();
					topLine.window().newIrfWhereConfigured(newParentRevision);
					topLine.window().fireChangeEvent();
				}
			}

			if (waitBar != null) {
				waitBar.setStatus(line.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID) + " BOM Line Condition Updating...");
			}

			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue)obj;
				if( cellValue.getData() != null){
					setCondition(line, (Vector<ConditionVector>) cellValue.getData().get("SPEC_DATA"));
					isApplied = true;
				}else{
					setCondition(line, (String)cellValue.getValue());
					isApplied = true;
				}
			}else if( obj instanceof String){
				setCondition(line, (String)obj);
				isApplied = true;
			}
		}
		return isApplied;
	}
	
	/**
	 * MLM 한개의 Row를 Save 하기 위한 Property 를 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 7. 22.
	 * @param model
	 * @param row
	 * @return
	 * @throws Exception
	 */
	private HashMap<String, Object> getProp(DefaultTableModel model, int row) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();

		Object obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PROJECT_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PROJCODE, obj.toString().trim());

		Object parentObj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX).toString());
		map.put( "PARENT_NO", parentObj.toString().trim());

		obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
		map.put( PropertyConstant.ATTR_NAME_ITEMID, obj.toString().trim());

		//Item ID Cell에 포함된 SYSTEM_ROW_KEY를 가져온다.
		if( obj instanceof CellValue){
			CellValue cellValue = (CellValue)obj;
			HashMap<String, Object> cellData = cellValue.getData();
			if( cellData != null){
				map.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, cellData.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY));
				map.put(PropertyConstant.ATTR_NAME_UOMTAG, cellData.get(PropertyConstant.ATTR_NAME_UOMTAG));
			}
		}else{
			throw new Exception("Could not found System Row Key.");
		}

		//contents
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_CONTENTS_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_CONTENTS, obj.toString().trim());

		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SYSTEM_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_BUDGETCODE, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_LEV_MAN_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_LEV_M, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SEQUENCE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO, obj.toString().trim());

		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_OLD_PART_ID_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_OLD_PART_NO, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_DISPLAYPARTNO, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_NAME_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_ITEMNAME, obj.toString().trim());

		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_REQ_OPT, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SPEC_DESC_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_SPEC_DESC, obj.toString().trim());

		Object specObj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SPEC_IDX);
		map.put("SPEC_DISP", specObj);
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_MODULE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_MODULE_CODE, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_SUPPLY_MODE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX).toString());
		map.put( MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, obj.toString().trim());
		
		//Usage 정보 Load..
		int selectedCnt = 0;
		String specStr = null;
		int startIdx = dlg.getMasterListTablePanel().getFixedColumnPre().length -1;
		int endIdx = dlg.getMasterListTablePanel().getFixedColumnPre().length + dlg.getOspec().getTrimList().size() - 1;
		for( int i = startIdx; i < endIdx; i++){
			String trimName = dlg.getMasterListTablePanel().getColumnName(i + 1);
			Object cellObj = model.getValueAt(row, i);
			String tmpStr = cellObj.toString().trim();
			boolean isOpt = false;
			if( cellObj.toString().indexOf("(") > -1 || cellObj.toString().indexOf(")") > -1){
				isOpt = true;
				tmpStr = tmpStr.replaceAll("\\(", "");
				tmpStr = tmpStr.replaceAll("\\)", "");
			}
			map.put( trimName, BomUtil.convertToString(cellObj));

			//Spec 이 NULL이면
			// 각각의 TRIM을 옵션 값으로 입력한다.
			if( tmpStr.length() > 0){
				double cnt = Double.parseDouble(tmpStr);
				if( cnt > 0 && (specObj == null || specObj.equals(""))){
					if( specStr == null){
						specStr = trimName + ( isOpt ? "_OPT":"_STD");
					}else{
						specStr += " or " + trimName + ( isOpt ? "_OPT":"_STD");
					}
					selectedCnt++;
				}
			}
		}
		if( specStr != null){
			//모든 Trim에서 모두 사용중이라면 옵션값을 넣을 필요 없다.
			//Parent와 동일한 옵션이면 값변경이 아니라 상속받은 옵션이다.
			String parentAllSpec = getParentSpec(masterListTablePanel.getTable(), fmpId, row, parentObj.toString().trim(), null, true);
			String parentSpec = getParentSpec(masterListTablePanel.getTable(), fmpId, row, parentObj.toString().trim(), null, false);
			if( selectedCnt != dlg.getOspec().getTrimList().size()){
				if( ("(" + specStr + ")").equals(parentSpec) || ("(" + specStr + ")").equals(parentAllSpec)){
					// [TEMP][20151216][jclee] Parent의 Option을 상속받은 것이 어떤 문제를 일으키는지 모르겠음. 이 Source때문에 하위 2Lv Part의 Usage가 정확히 나타나지 않는 문제 발생.
					// 일단 Option을 제대로 SetValue한 후 추후 문제 발생 시 원복할 것
					//					map.put("SPEC", "");
					map.put("SPEC", specStr); 
				}else{
					map.put("SPEC", specStr);
				}
			}else{
				//				map.put("SPEC", "");
				map.put("SPEC", specStr);
			}
		}else{
			if( specObj != null && !specObj.equals("")){
				map.put("SPEC", specObj.toString());
			}
		}

		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_ALTER_PART_IDX).toString());
		map.put(PropertyConstant.ATTR_NAME_BL_ALTER_PART, obj);
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DR_IDX).toString());
		map.put(PropertyConstant.ATTR_NAME_DR, obj);
		//20200924 seho EJS Column 추가. 저장하기 위해 table에서 값을 가지고 오는 부분
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_EJS_IDX).toString());
		map.put(PropertyConstant.ATTR_NAME_BL_EJS, obj);
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_RESPONSIBILITY_IDX).toString());
		map.put(PropertyConstant.ATTR_NAME_BOX, obj);
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_CHANGE_DESCRIPTION_IDX).toString());
		map.put(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION, obj);

		//설계 컨셉
		//N, M1,M2,M3, C, D 값 셋팅
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_NMCD_IDX).toString());
		if( obj.equals("N") || obj.toString().contains("M") || obj.equals("T")){
			map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, obj);
			map.put(PropertyConstant.ATTR_NAME_BL_CHG_CD, "");
		}else if( obj.equals("C") || obj.equals("D") ){
			map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, "");
			map.put(PropertyConstant.ATTR_NAME_BL_CHG_CD, obj);
		}else{
			map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, "");
			map.put(PropertyConstant.ATTR_NAME_BL_CHG_CD, "");
		}
		//양산 차종
		//		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PROJECT_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_ORIGIN_PROJECT, obj.toString().trim());

		//Weight(현재예상, 목표)
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_EST_WEIGHT_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_ESTWEIGHT, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_TARGET_WEIGHT_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_TARGET_WEIGHT, obj.toString().trim());
		
		//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가. 저장하기 위해 table에서 값을 가지고 오는 부분. by 전성용(20201223)
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_WEIGHT_MANAGEMENT_IDX).toString());
		map.put(PropertyConstant.ATTR_NAME_BL_WEIGHT_MANAGEMENT, obj);

		//개발단계(추정재료비, 목표재료비)
		//[CSH][20180523]원가 자료에 대한 화면 View 전체 금지 (연구기획팀 권종원)
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_EST_COST_MATERIAL_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_TARGET_COST_MATERIAL_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, obj.toString().trim());

		//[SR170703-020][LJG]Proto Tooling 컬럼 추가
		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PROTO_TOOLING_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING, obj.toString().trim());
		
		//선정업체(업체명)
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_SELECTED_COMPANY_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_SELECTED_COMPANY, obj.toString().trim());

		//CONCEPT DWG
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_CON_DWG_PLAN_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_CON_DWG_PLAN, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_CON_DWG_PERFORMANCE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_CON_DWG_TYPE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_CON_DWG_TYPE, obj.toString().trim());

		//도면배포 예정일
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DWG_DEPLOYABLE_DATE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, obj.toString().trim());

		//도면 작성(양산)
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PRD_DWG_PERFORMANCE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PRD_DWG_PLAN_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PRD_DWG_PLAN, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_ECO_NO_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_ECO_NO, obj.toString().trim());

		//DVP SAMPLE
		/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
		//		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_NEEDED_QTY_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY, obj.toString().trim());
		//		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_USE_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_DVP_USE, obj.toString().trim());
		//		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_REQ_DEPT_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_DVP_REQ_DEPT, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_NEEDED_QTY_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_USE_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_DVP_USE, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_REQ_DEPT_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT, obj.toString().trim());

		//설계담당
		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		//		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_DEPT_NM_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_ENG_DEPT_NM, obj.toString().trim());
		//		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_RESPONSIBILITY_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_DEPT_NM_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_RESPONSIBILITY_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY, obj.toString().trim());

		//CIC
		//		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_CIC_DEPT_NM_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_CIC_DEPT_NM, obj.toString().trim());

		//예상투자비
		//[CSH][20180523]원가 자료에 대한 화면 View 전체 금지 (연구기획팀 권종원)
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PRT_TOOLG_INVESTMENT_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, obj.toString().trim());

		//부품개발
		//[CSH][20180523]원가 자료에 대한 화면 View 전체 금지 (연구기획팀 권종원)
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PRD_TOOL_COST_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PRD_TOOL_COST, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PRD_SERVICE_COST_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PRD_SERVICE_COST, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PRD_SAMPLE_COST_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PRD_SUM_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_TOTAL, obj.toString().trim());

		//구매담당
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PUR_TEAM_IDX).toString());
		// [20160907][ymjang] 컬럼명 오류 수정
		map.put( PropertyConstant.ATTR_NAME_PUR_DEPT_NM, obj.toString().trim());
		obj = StringUtil.nullToString( model.getValueAt(row, masterListTablePanel.MASTER_LIST_PUR_RESPONSIBILITY_IDX).toString());
		map.put( PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY, obj.toString().trim());
		//		obj = StringUtil.nullToString( model.getValueAt(row, MasterListTablePanel.MASTER_LIST_EMPLOYEE_NO_IDX).toString());
		//		map.put( PropertyConstant.ATTR_NAME_EMPLOYEE_NO, obj.toString().trim());

		return map;
	}

	/**
	 * BOM을 구성하기 전에 미리 아이템을 생성하여, Parent가 존재하지 않는 경우가 발생하지 않도록 함.
	 * 
	 * @param addedData
	 * @throws Exception
	 */
	private void createItems(HashMap<String, HashMap<String, Object>> addedData) throws Exception{

		String[] keys = addedData.keySet().toArray(new String[addedData.size()]);
		for( int i = 0; keys != null && i < keys.length; i++){
			HashMap<String, Object> properties = addedData.get(keys[i]);
			String project = dlg.getProject();
			//			String cd = (String)properties.get(PropertyConstant.ATTR_NAME_BL_CHG_CD);

			String partId = (String)properties.get(PropertyConstant.ATTR_NAME_ITEMID);
			String partName = BomUtil.convertToString( properties.get(PropertyConstant.ATTR_NAME_ITEMNAME));
			String partDesc = partName;

			if( partId == null || partId.equals("")){
				String systemCode = (String)properties.get(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);
				int entireLength = 13;
				if( project.length() > 4){
					entireLength = 13;
				}else{
					entireLength = 11;
				}

				// [SR없음][20150811][jclee] 채번 방법 변경
				partId = SYMTcUtil.getNewID(project + systemCode, entireLength);
				//				partId = SYMTcUtil.getNextID(project + systemCode, entireLength);

				properties.put(PropertyConstant.ATTR_NAME_ITEMID, partId);

				TCComponentItem item = SYMTcUtil.createItem(session, partId, partName, partDesc, TypeConstant.S7_PREVEHICLEPARTTYPE, "000");
				// [20161017][ymjang] 아이템 생성 및 Revise 이후 Refresh 구문 추가 (setProperties 시에 저장이 안됨)
				item.refresh();
				TCComponentFolder preBomFolder = BomUtil.getPreBomFolder();
				preBomFolder.add("contents", item);
				item.setProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, BomUtil.convertToString( properties.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO)));
				item.setProperty(PropertyConstant.ATTR_NAME_UOMTAG, "EA");
				TCComponentItemRevision childRevision = item.getLatestItemRevision();
				childRevision.refresh();
				if(setRevProp(properties, childRevision, true)){
					//FIXME: 2016.12.07 할 필요없음. TEST(OK)
					//dlg.getWorkingFmpTopLine().window().refresh();
					dataMapper.setPropertyMap(keys[i], properties);
				}
				Vector row = masterKeyRowMapper.get(keys[i]);
				row.setElementAt(new CellValue(partId), MasterListTablePanel.MASTER_LIST_PART_ID_IDX + 1);
//				//CSH 20180405 OSPEC_NO 추가
				String ospec_no = ccnRevision.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO);
				row.setElementAt(new CellValue(ospec_no), masterListTablePanel.MASTER_LIST_OSPEC_NO_IDX + 1);
			}

			//			TCComponentItemRevision childRevision = BomUtil.findLatestItemRevision(TypeConstant.S7_PREVEHICLEPARTTYPE, partId);
			//			if( childRevision == null){
			//				childRevision = BomUtil.findLatestItemRevision(SYMCClass.S7_STDPARTTYPE, partId);
			//				if( childRevision == null){
			//					childRevision = BomUtil.findLatestItemRevision(SYMCClass.S7_VEHPARTTYPE, partId);
			//					if( childRevision == null){
			//						// Std Part에도 없고, Veh Part에도 없으면 pre-VehPart type으로 생성한다.
			//						TCComponentItem item = SYMTcUtil.createItem(session, partId, partName, partDesc, TypeConstant.S7_PREVEHICLEPARTTYPE, "000");
			//						TCComponentFolder preBomFolder = BomUtil.getPreBomFolder();
			//						preBomFolder.add("contents", item);
			//						item.setProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, BomUtil.convertToString( properties.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO)));
			//						item.setProperty(PropertyConstant.ATTR_NAME_UOMTAG, "EA");
			//						childRevision = item.getLatestItemRevision();
			//						if(setRevProp(properties, childRevision, true)){
			//							dataMapper.setPropertyMap(keys[i], properties);
			//						}
			//						Vector row = masterKeyRowMapper.get(keys[i]);
			//						row.setElementAt(new CellValue(partId), MasterListTablePanel.MASTER_LIST_PART_ID_IDX + 1);
			//					}
			//				}
			//			}
		}

	}

	/**
	 * Paret ID 와 key를 묶어 리턴.
	 * @param addedData
	 * @return
	 * @throws TCException
	 */
	private HashMap<String, ArrayList<String>> getStructure(HashMap<String, HashMap<String, Object>> addedData) throws TCException{
		HashMap<String, ArrayList<String>> structureMap = new HashMap();
		String[] keys = addedData.keySet().toArray(new String[addedData.size()]);
		for( int i = 0; keys != null && i < keys.length; i++){
			HashMap<String, Object> properties = addedData.get(keys[i]);
			String parentId = BomUtil.convertToString( properties.get("PARENT_NO"));
			if( parentId == null || parentId.equals("")){
				parentId = fmpRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
			}
			ArrayList<String> keyList = structureMap.get(parentId);
			if( keyList == null){
				keyList = new ArrayList();
				keyList.add(keys[i]);
				structureMap.put(parentId, keyList);
			}else{
				keyList.add(keys[i]);
			}
		}

		return structureMap;
	}

	public boolean createStructure(HashMap<String, HashMap<String, Object>> addedData, 
			HashMap<String, ArrayList<String>> structureMap, String parentId) throws Exception{
		//		HashMap<String, ArrayList<TCComponentBOMLine>> bomLineMap = dataMapper.getBomlineMap();
		//Parent가 BOM에 존재하지 않을수도(아직 생성되지 않아) 있으므로
		//최상위부터 생성하도록 한다. 

		//BOM 상에 존재하지 않는 경우
		TCComponentBOMLine parentLine = null;
		if( parentId != null && !parentId.trim().equals("")){
			//FIXME: 2016.12.07 TopBOMLine에서 Parent 찾는 방법 변경. TEST(OK)
			// parentLine = BomUtil.findBOMLine(topLine, parentId);
			// if( parentLine == null){
			//	 return false;
			//   }
			ArrayList<String> parentIdList = new ArrayList<String>(Arrays.asList(parentId));
			LinkedList<TCComponentBOMLine> parentBOMList = BomUtil.findBOMLinesWithId(parentIdList, topLine);
			if(parentBOMList.size() == 0)
				return false;
			parentLine = parentBOMList.get(0);
		}

		HashMap<String, Boolean> rtnMap = null;
		ArrayList<String> keyList = structureMap.get(parentId);
		for(String key : keyList){
			HashMap<String, Object> properties = addedData.get(key);
			String partId = BomUtil.convertToString( properties.get(PropertyConstant.ATTR_NAME_ITEMID));
			//			String partName = BomUtil.convertToString( properties.get(PropertyConstant.ATTR_NAME_ITEMNAME));
			//			String partDesc = partName;

			TCComponentBOMLine childLine = null;
			TCComponentItemRevision childRevision = null;

			childRevision = BomUtil.findLatestItemRevision(TypeConstant.S7_PREVEHICLEPARTTYPE, partId);
			if( childRevision == null){

				childRevision = BomUtil.findLatestItemRevision(SYMCClass.S7_STDPARTTYPE, partId);
				if( childRevision == null){
					childRevision = BomUtil.findLatestItemRevision(SYMCClass.S7_VEHPARTTYPE, partId);
					if( childRevision == null){
						// Std Part에도 없고, Veh Part에도 없으면 pre-VehPart type으로 생성한다.
						// createItems에서 미리 생성하므로 이 부분에서 아이템을 생성하는 경우는 없슴.
						return false;
					}
				}
			}else{
				//				if( setRevProp(properties, childRevision, false)){
				//key에 해당하는 데이타가 없으며, dataMapper.addBomLine2로 추가됨.
				//					dataMapper.setPropertyMap(key, properties);
				//				};
			}

			int qty = 0;
			//			double realQty = 1;
			String strQty = (String)properties.get(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY);
			if( strQty == null || strQty.trim().equals("")){
				qty = 1;
			}else{
				try{
					double dNum = Double.parseDouble(strQty.trim());
					int iNum = (int)dNum;
					if( dNum == iNum){
						strQty = "1";
						qty = iNum;
					}else{
						strQty = dNum + "";
						qty = 1;
					}

				}catch(NumberFormatException nfe){
					qty = 1;
					double realQty = Double.parseDouble(strQty.trim());
					strQty = realQty + "";
				}
			}

			//			String systemRowKey = BomUtil.getNewSystemRowKey();
			//수량만큼 BOM Line 생성
			for( int i = 0; i < qty; i++){

				int modelRow = systemKeyMapper.get((String)properties.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY));
				Object obj = model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);

				if( parentId == null || parentId.trim().equals("") || parentId.trim().equals(fmpId)){

					if( CustomUtil.isReleased( topLine.getItem().getLatestItemRevision())){
						fmpRevision = revise(fmpRevision);

						//FIXME: 2016.12.07 FMP 개정시 Refresh만 한다. TEST(OK)
						topLine.window().newIrfWhereConfigured(fmpRevision);
						topLine.window().fireChangeEvent();
						dlg.setWorkingFmpTopLine(topLine);

						//						//Command에서 생성한 BOM Window 닫기.
						//						dlg.getWorkingFmpTopLine().window().save();
						//						dlg.getWorkingFmpTopLine().window().close();
						//						topLine = CustomUtil.getBomline(fmpRevision, session);
						//						dlg.setWorkingFmpTopLine(topLine);
						//						topLine.window().refresh();
						//						
						//						// dataMapper 다시 셋팅.
						//						dataMapper.initialize(topLine, dlg.getOspec(), dlg.getEssentialNames(), true);
						//						BOMLoadOperation.loadChildMap(dataMapper, topLine, storedOptionSetMap);

					}

					((SYMCBOMWindow)topLine.window()).skipHistory(true);
					childLine = topLine.add(childRevision.getItem(), null);
				}else{

					//Parent가 Fmp이면.
					if( parentLine.getItem().equals(fmpRevision.getItem())){
						if( CustomUtil.isReleased( topLine.getItem().getLatestItemRevision())){
							fmpRevision = revise(fmpRevision);

							//FIXME: 2016.12.07 FMP 개정시 Refresh만 한다. TEST(NOT)
							topLine.window().newIrfWhereConfigured(fmpRevision);
							topLine.window().fireChangeEvent();
							dlg.setWorkingFmpTopLine(topLine);

							//							//Command에서 생성한 BOM Window 닫기.
							//							dlg.getWorkingFmpTopLine().window().save();
							//							dlg.getWorkingFmpTopLine().window().close();
							//							topLine = CustomUtil.getBomline(fmpRevision, session);
							//							dlg.setWorkingFmpTopLine(topLine);
							//							topLine.window().refresh();
							//							
							//							// dataMapper 다시 셋팅.
							//							dataMapper.initialize(topLine, dlg.getOspec(), dlg.getEssentialNames(), true);
							//							BOMLoadOperation.loadChildMap(dataMapper, topLine, storedOptionSetMap);

							parentLine = topLine;
						}
					}else{
						if( CustomUtil.isReleased( parentLine.getItem().getLatestItemRevision())){
							TCComponentItemRevision newParentRevision = revise(parentLine.getItemRevision());
							//FIXME: 2016.12.07 BOM Window refresh 수정. TEST(OK)
							//parentLine.refresh();
							//parentLine.window().refresh();
							topLine.window().newIrfWhereConfigured(newParentRevision);
							topLine.window().fireChangeEvent();
						}
					}

					((SYMCBOMWindow)topLine.window()).skipHistory(true);
					childLine = parentLine.add(childRevision.getItem(), null);
				}
				childLine.setProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, (String)properties.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY));
				//추가된 라인이므로 키를 새로 생성함.
				//				properties.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, systemRowKey);

				CellValue partIdCellValue = null;
				if( obj instanceof CellValue){
					partIdCellValue = (CellValue)obj;
					partIdCellValue.setValue(partId);
					HashMap<String, Object> cellData = partIdCellValue.getData();
					cellData.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, properties.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY));
				}else{
					throw new Exception("Invalid Type");
				}

				//Level을 업데이트 함.
				CellValue levelCellValue = null;
				obj = model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_LEV_A_IDX);
				if( obj instanceof CellValue){
					levelCellValue = (CellValue)obj;
					levelCellValue.setValue("" + MasterListDataMapper.getLevel(childLine, 1));
				}else{
					levelCellValue = new CellValue("" + MasterListDataMapper.getLevel(childLine, 1));
					model.setValueAt(levelCellValue, modelRow, MasterListTablePanel.MASTER_LIST_LEV_A_IDX);
				}
				
				//CSH 20180405 OSPEC_NO 업데이트
				CellValue ospecCellValue = null;
				obj = model.getValueAt(modelRow, masterListTablePanel.MASTER_LIST_OSPEC_NO_IDX);
				if( obj instanceof CellValue){
					ospecCellValue = (CellValue)obj;
					ospecCellValue.setValue(ccnRevision.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO));
				} else {
					ospecCellValue = new CellValue(ccnRevision.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO));
					model.setValueAt(ospecCellValue, modelRow, masterListTablePanel.MASTER_LIST_OSPEC_NO_IDX);
				}
				
				//Cutomizing한 결과로 UOM을 bl_quantity보다 먼저 설정할 경우 에러 발생함.
				childLine.setProperty("bl_quantity", strQty);

				if (rtnMap == null) {
					rtnMap = new HashMap<String, Boolean> ();	
				} else {
					rtnMap.clear();
				}

				if( setBOMLineProp(properties, childLine, rtnMap)){
					TCComponentItemRevision parentRevision = childLine.parent().getItem().getLatestItemRevision();
					parentRevision.refresh();
					parentRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);
					//					TCProperty tcProp = parentRevision.getTCProperty("s7_CCN_NO");
					//					tcProp.setReferenceValue(ccnRevision);
					dataMapper.setPropertyMap(key, properties);
				};

				dataMapper.addBomLine(childLine, storedOptionSetMap, product_project_code);
				//하위 정보도 로딩함.
				BOMLoadOperation.loadChildMap(dataMapper, childLine, storedOptionSetMap, product_project_code);
			}
		}

		return true;
	}

	private void changeQty(HashMap<String, HashMap<String, Object>> changedData) throws Exception{

		HashMap<String, HashMap<String, ArrayList<TCComponentBOMLine>>> bomLineMap = dataMapper.getBomlineMap();
		String[] keys = changedData.keySet().toArray(new String[changedData.size()]);
		for( int i = 0; keys != null && i < keys.length; i++){
			HashMap<String, Object> changedProperties = changedData.get(keys[i]);
			HashMap<String, ArrayList<TCComponentBOMLine>> bomLines = bomLineMap.get(keys[i]);
			if( bomLines == null || bomLines.isEmpty()){
				// 속성 변경이므로 기존에 BOM line이 반드시 있어야함.
				throw new TCException("[Change Property] Could not found BOM Line.");
			}
			String[] occThreads = bomLines.keySet().toArray(new String[bomLines.size()]);
			ArrayList<TCComponentBOMLine> baseLines = bomLines.get(occThreads[0]);
			TCComponentBOMLine baseLine = baseLines.get(0);
			TCComponentBOMLine parentLine = baseLine.parent();

			//Parent도 바뀌고 수량도 바뀐 경우 이므로 이동 후 처리함.
			if( changedProperties.containsKey("PARENT_NO")){
				continue;
			}

			// 수량이 변경 된 경우.
			if( changedProperties.containsKey(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY)){
				Object obj = changedProperties.get(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY);
				String str = obj.toString().trim();
				if( !str.equals("" + bomLines.size())){

					//Parent가 Release일 경우 Revise함.
					if( CustomUtil.isReleased( parentLine.getItem().getLatestItemRevision())){

						if( fmpRevision.equals(parentLine.getItemRevision())){
							fmpRevision = revise(fmpRevision);

							//FIXME: 2016.12.07 FMP 개정시 Refresh 만 한다. TEST(OK)
							topLine.window().newIrfWhereConfigured(fmpRevision);
							topLine.window().fireChangeEvent();
							dlg.setWorkingFmpTopLine(topLine);

							//							//Command에서 생성한 BOM Window 닫기.
							//							dlg.getWorkingFmpTopLine().window().save();
							//							dlg.getWorkingFmpTopLine().window().close();
							//							topLine = CustomUtil.getBomline(fmpRevision, session);
							//							dlg.setWorkingFmpTopLine(topLine);
							//							topLine.refresh();
							//							
							//							// dataMapper 다시 셋팅.
							//							dataMapper.initialize(topLine, dlg.getOspec(), dlg.getEssentialNames(), true);
							//							BOMLoadOperation.loadChildMap(dataMapper, topLine, storedOptionSetMap);

							parentLine = topLine;

							// [20161019][ymjang] FMP Revise 될 경우, BOM Line 속성 Update 시 Invalid Tag 오류 발생
							// FMP 가 Revise 된 경우, BOM Line 정보를 다시 읽어옴.
							bomLineMap = dataMapper.getBomlineMap();
							bomLines = bomLineMap.get(keys[i]);
							if( bomLines == null || bomLines.isEmpty()){
								// 속성 변경이므로 기존에 BOM line이 반드시 있어야함.
								throw new TCException("[Change Property] Could not found BOM Line.");
							}
							occThreads = bomLines.keySet().toArray(new String[bomLines.size()]);
							baseLines = bomLines.get(occThreads[0]);
							baseLine = baseLines.get(0);

						}else{
							TCComponentItemRevision newParentRevision = revise(parentLine.getItemRevision());
							// BOM View Revision이 Revise된 Revision으로 Refresh 되지 않는 현상 수정
							//FIXME: 2016.12.07 BOM Window refresh 수정. TEST(OK)
							//parentLine.parent().refresh();
							//parentLine.window().refresh();
							//parentLine.refresh();
							topLine.window().newIrfWhereConfigured(newParentRevision);
							topLine.window().fireChangeEvent();
						}
					}

					int masterListRepQty = Integer.parseInt(str);

					//수량 증가.
					if( masterListRepQty > bomLines.size()){
						int gap = masterListRepQty - bomLines.size();
						for( int j = 0; j < gap; j++){
							TCComponentBOMLine addedLine = parentLine.add(baseLine.getItem(), null);
							HashMap<String, String> bomLinePropMap = getBOMLineProp(baseLine);
							if( bomLinePropMap != null && !bomLinePropMap.isEmpty()){
								addedLine.setProperties(bomLinePropMap);
							}
							String conditionStr = baseLine.getProperty("bl_occ_mvl_condition");
							if( conditionStr != null && !conditionStr.equals("")){
								((SYMCBOMLine)addedLine).setMVLCondition(conditionStr);
							}

							String occThread = addedLine.getProperty(PropertyConstant.ATTR_NAME_BL_OCC_THREAD);
							ArrayList<TCComponentBOMLine> lines = bomLines.get(occThread);
							if( lines == null){
								lines = new ArrayList();
								lines.add(addedLine);
								bomLines.put(occThread, lines);
							}else{
								if( !lines.contains(addedLine)){
									lines.add(addedLine);
								}
							}
							//							bomLines.put(occThread, addedLine);
							HashMap<String, Object> propMap = dataMapper.getPropertyMap(keys[i]);
							propMap.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, "" + bomLines.size());
						}
						//수량 감소	
					}else if( masterListRepQty < bomLines.size()){
						int gap = bomLines.size() - masterListRepQty;

						//						occThreads

						int tmpIdx = 0;
						while( tmpIdx < gap){
							ArrayList<TCComponentBOMLine> lines = bomLines.get(occThreads[tmpIdx]);
							for( TCComponentBOMLine line : lines){
								line.refresh();
								if( line.isPacked()){
									TCComponentBOMLine[] packedLines = line.getPackedLines();
									for( int j = 0; tmpIdx < gap && j < packedLines.length; j++){
										String occThread = packedLines[j].getProperty(PropertyConstant.ATTR_NAME_BL_OCC_THREAD);
										bomLines.remove(occThread);
										parentLine.remove(null, packedLines[j]);
										tmpIdx++;
									}
								}else{
									bomLines.remove(occThreads[tmpIdx]);
									parentLine.remove(null, line);
									tmpIdx++;
								}
							}

						}

						//						int tmpIdx = bomLines.size() - 1;
						//						for( int j = 0; j < gap; j++){
						//							TCComponentBOMLine line = bomLines.get(tmpIdx--);
						//							bomLines.remove(line);
						//							parentLine.remove(null, line);
						//						}

						HashMap<String, Object> propMap = dataMapper.getPropertyMap(keys[i]);
						if( bomLines.isEmpty()){
							propMap.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, "");
						}else{
							propMap.put(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY, "" + bomLines.size());
						}

					}

				}
			}

		}

	}

	public void change(HashMap<String, HashMap<String, Object>> changedData) throws Exception{
		HashMap<String, HashMap<String, ArrayList<TCComponentBOMLine>>> bomLineMap = dataMapper.getBomlineMap();

		HashMap<String, Boolean> rtnMap = null;
		String[] keys = changedData.keySet().toArray(new String[changedData.size()]);
		for( int i = 0; keys != null && i < keys.length; i++){
			HashMap<String, Object> changedProperties = changedData.get(keys[i]);
			HashMap<String, ArrayList<TCComponentBOMLine>> bomLines = bomLineMap.get(keys[i]);
			if( bomLines == null || bomLines.isEmpty()){
				// 속성 변경이므로 기존에 BOM line이 반드시 있어야함.
				throw new TCException("[Change Property] Could not found BOM Line.");
			}

			// 수량이 변경 된 경우.
			//			if( changedProperties.containsKey(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY)){
			//				Object obj = changedProperties.get(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY);
			//				String str = obj.toString().trim();
			//				if( !str.equals("" + bomLines.size())){
			//					int masterListRepQty = Integer.parseInt(str);
			//					System.out.println("Quantity Changed....");
			//				}
			//			}

			String[] occThreads = bomLines.keySet().toArray(new String[bomLines.size()]);
			ArrayList<TCComponentBOMLine> childLines = bomLines.get(occThreads[0]);
			TCComponentBOMLine childLine = childLines.get(0);
			TCComponentItemRevision childRevision = childLine.getItemRevision();
			TCComponentItem childItem = childLine.getItem();
			String childType = childItem.getType();

			if( changedProperties.containsKey("SPEC")){
				String parentCondition = MasterListDataMapper.getConditionSet(childLine.parent(), null);
				//상위 파트의 컨디션과 'and'
				String simpleCondition = changedProperties.get("SPEC").toString();
				if( parentCondition != null && !parentCondition.equals("")){
					simpleCondition = parentCondition + (simpleCondition.equals("") ? "" : " and (" + simpleCondition + ")");
				}
				//				Spec이 변경되면 COMPLEX_SPEC(상위 SPeC과의 조합)도 변경함.
				changedProperties.put("COMPLEX_SPEC", simpleCondition);
			}

			if( childType.equals(SYMCClass.S7_STDPARTTYPE) || childType.equals(SYMCClass.S7_VEHPARTTYPE)){
				//속성은 변경 할 수 없고, BOM Line속성만 변경.
				for( String occThread : occThreads){
					ArrayList<TCComponentBOMLine> bomlines = bomLines.get(occThread);

					if (rtnMap == null) {
						rtnMap = new HashMap<String, Boolean> ();	
					} else {
						rtnMap.clear();
					}
					if( setBOMLineProp(changedProperties, bomlines.get(0), rtnMap)){
						TCComponentItemRevision parentRevision = childLine.parent().getItem().getLatestItemRevision();
						parentRevision.refresh();
						parentRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);
						//						TCProperty tcProp = parentRevision.getTCProperty("s7_CCN_NO");
						//						tcProp.setReferenceValue(ccnRevision);				
						dataMapper.setPropertyMap(keys[i], changedProperties);
						//						parentRevision.save();
					}

					// [20161019][ymjang] FMP Revise 될 경우, BOM Line 속성 Update 시 Invalid Tag 오류 발생
					// FMP 가 Revise 된 경우, BOM Line 정보를 다시 읽어옴.
					Boolean isFmpRevised = (Boolean) rtnMap.get("isFmpRevised");
					if (isFmpRevised) {
						bomLineMap = dataMapper.getBomlineMap();
						bomLines = bomLineMap.get(keys[i]);
					}
				}
				continue;
			}else if( childType.equals(TypeConstant.S7_PREVEHICLEPARTTYPE)){

				childRevision.refresh();

				/**
				 * [20170215] CARRY OVER 로 변경될 경우, Carry Over Revision으로 Replace함
				 */
				boolean isReplacedToCarryOverPart =changedProperties.containsKey(PropertyConstant.ATTR_NAME_BL_CHG_CD) && 
						"C".equals(changedProperties.get(PropertyConstant.ATTR_NAME_BL_CHG_CD));	
				if( isReplacedToCarryOverPart)
				{
					TCComponentItem replaceItem = null;
					TCComponentItemRevision replaceItemRevision = null;
					//Item Id 가 입력되어 있는 경우 -> 사용자가 Key을 해서 자동입력이 된 경우
					if(changedProperties.get(PropertyConstant.ATTR_NAME_ITEMID) !=null )
					{
						String replaceId = (String) changedProperties.get(PropertyConstant.ATTR_NAME_ITEMID);
						HashMap<String, String> resultMap = WebUtil.getPart(replaceId);
						replaceItem = (TCComponentItem)session.stringToComponent(resultMap.get("PUID"));
						replaceItemRevision = replaceItem.getLatestItemRevision();
						// Item Id가 입력되지 않을 경우 -> Copy & Paste를 이용하여 입력해서 자동 입력이 안된 경우	
					}else
					{ 
						Object displayNameObj = changedProperties.get(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
						if(displayNameObj !=null)
						{
							String displayName = (String)displayNameObj;
							String replaceId = displayName.replace(" ","");
							HashMap<String, String> resultMap = WebUtil.getPart(replaceId);
							if(resultMap !=null)
							{
								replaceItem = (TCComponentItem)session.stringToComponent(resultMap.get("PUID"));
								replaceItemRevision = replaceItem.getLatestItemRevision();
								String systemCode = childLine.getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);
								String itemName = replaceItem.getProperty("object_name");
								String itemId = replaceItem.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
								String projectCode = replaceItemRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
								String dispPartNo = replaceItemRevision.getProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);

								int modelRow = systemKeyMapper.get(keys[i]);

								//화면 UI 정보 UPDATE 
								Object partIdObj = model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
								if( partIdObj instanceof CellValue){
									CellValue partIdCellValue = (CellValue)partIdObj;
									partIdCellValue.setValue(itemId);
								}
								model.setValueAt(systemCode, modelRow, MasterListTablePanel.MASTER_LIST_SYSTEM_IDX);
								model.setValueAt(itemName, modelRow, MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
								model.setValueAt(projectCode, modelRow, MasterListTablePanel.MASTER_LIST_PROJECT_IDX);
								model.setValueAt(dispPartNo, modelRow, MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX);
								

								//변경정보 속성 UPDATE : Carry Over 속성으로 변경함
								Object regOpt = model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX); //REQ. OPT
								changedProperties.put(PropertyConstant.ATTR_NAME_ITEMID, itemId);
								changedProperties.put(PropertyConstant.ATTR_NAME_ITEMNAME, itemName);
								changedProperties.put(PropertyConstant.ATTR_NAME_BL_BUDGETCODE, systemCode);
								changedProperties.put(PropertyConstant.ATTR_NAME_PROJCODE, projectCode);
								changedProperties.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, dispPartNo);
								changedProperties.put(PropertyConstant.ATTR_NAME_BL_REQ_OPT, regOpt !=null ? regOpt.toString():"");
								dataMapper.setPropertyMap(keys[i], changedProperties);

								//Row Mapper 정보 UPDATE: 오렌지 색에서 흰색으로 돌리기 위함
								Vector rowValueVec = masterKeyRowMapper.get(keys[i]);
								rowValueVec.setElementAt(new CellValue(itemId), MasterListTablePanel.MASTER_LIST_PART_ID_IDX + 1);
								rowValueVec.setElementAt(new CellValue(systemCode), MasterListTablePanel.MASTER_LIST_SYSTEM_IDX + 1);
								rowValueVec.setElementAt(new CellValue(itemName), MasterListTablePanel.MASTER_LIST_PART_NAME_IDX + 1);
								rowValueVec.setElementAt(new CellValue(projectCode), MasterListTablePanel.MASTER_LIST_PROJECT_IDX + 1);
								rowValueVec.setElementAt(new CellValue(dispPartNo), MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX + 1);
								rowValueVec.setElementAt(MasterListDataMapper.convertToCellValue(model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX)),
										MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX + 1);

							}							
						}						
					}

					/**
					 * Occurrence 수량만큼 Replace를 한다.
					 */
					for(String occThread : occThreads)
					{
						ArrayList<TCComponentBOMLine> targetList = bomLines.get(occThread);
						TCComponentBOMLine targetBOMLine = targetList.get(0);
						if(replaceItemRevision ==null)
							continue;
						replaceBOM(targetBOMLine, null, replaceItemRevision, null, 0);
						dlg.getWorkingFmpTopLine().window().newIrfWhereConfigured(replaceItemRevision);
						dlg.getWorkingFmpTopLine().window().fireChangeEvent();
					}
				}else
				{
					if(setRevProp(changedProperties, childRevision, false)){
						//FIXME: 2016.12.07 BOM Window refresh 수정. TEST(OK)
						//dlg.getWorkingFmpTopLine().window().refresh();
						dlg.getWorkingFmpTopLine().window().newIrfWhereConfigured(childRevision.getItem().getLatestItemRevision());
						dlg.getWorkingFmpTopLine().window().fireChangeEvent();
						dataMapper.setPropertyMap(keys[i], changedProperties);
					};
				}


				for( String occThread : occThreads){
					ArrayList<TCComponentBOMLine> bomlines = bomLines.get(occThread);

					if (rtnMap == null) {
						rtnMap = new HashMap<String, Boolean> ();	
					} else {
						rtnMap.clear();
					}

					if( setBOMLineProp(changedProperties, bomlines.get(0), rtnMap)) {
						TCComponentItemRevision parentRevision = childLine.parent().getItem().getLatestItemRevision();
						parentRevision.refresh();
						parentRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);
						//						TCProperty tcProp = parentRevision.getTCProperty("s7_CCN_NO");
						//						tcProp.setReferenceValue(ccnRevision);
						dataMapper.setPropertyMap(keys[i], changedProperties);
						//						parentRevision.save();
					}

					// [20161019][ymjang] FMP Revise 될 경우, BOM Line 속성 Update 시 Invalid Tag 오류 발생
					// FMP 가 Revise 된 경우, BOM Line 정보를 다시 읽어옴.
					Boolean isFmpRevised = (Boolean) rtnMap.get("isFmpRevised");
					if (isFmpRevised) {
						bomLineMap = dataMapper.getBomlineMap();
						bomLines = bomLineMap.get(keys[i]);
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	public void remove(TCComponentBOMLine fmpTopLine, ArrayList<String> deleteData) throws Exception{
		HashMap<String, HashMap<String, ArrayList<TCComponentBOMLine>>> bomLineMap = dataMapper.getBomlineMap();
		if( bomLineMap.isEmpty()){
			return;
		}
		//		String[] tmpStrArray = bomLineMap.keySet().toArray(new String[bomLineMap.size()]);
		//		List<String> keys = Arrays.asList(tmpStrArray);

		for( String key : deleteData){
			HashMap<String, ArrayList<TCComponentBOMLine>> bomLines = bomLineMap.get(key);

			//bomLines가 존
			if( bomLines == null){
				continue;
			}
			//			boolean isDeleted = false;
			String[] occThreads = bomLines.keySet().toArray(new String[bomLines.size()]);
			for( int i = bomLines.size() - 1; i >= 0; i--){
				ArrayList<TCComponentBOMLine> lines = bomLines.get(occThreads[i]);
				//				isDeleted = false;
				for( TCComponentBOMLine line : lines){
					//Packed Line is alread removed.
					if( line == null || !line.isValidUid()){
						continue;
					}
					line.refresh();
					TCComponentItem item = line.getItem();
					//					String type = item.getType();

					TCComponentBOMLine parentLine = line.parent();
					//					parentLine.refresh();
					TCComponentItemRevision parentRevision = parentLine.getItemRevision();

					if( parentLine == null){
						throw new TCException("Could not found " + item.getProperty(PropertyConstant.ATTR_NAME_ITEMID) + "'Parent");
					}
					String parentType = parentLine.getItem().getType();

					if( parentType.equals(TypeConstant.S7_PREFUNCMASTERTYPE) || parentType.equals(TypeConstant.S7_PREVEHICLEPARTTYPE)){
						//Parent Line은 PreVehicle 또는 PreFMP이다.
						if( CustomUtil.isReleased(parentLine.getItem().getLatestItemRevision())){

							//Release되어 있고 FMP일 경우, 
							if( fmpRevision.getItem().equals(parentLine.getItem())){
								String systemRowKey = line.getProperty(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
								String occThread = line.getProperty( PropertyConstant.ATTR_NAME_BL_OCC_THREAD);

								fmpRevision = revise(fmpRevision);

								//FIXME: 2016.12.07 FMP 개정시 Refresh 만 한다. TEST(OK)
								topLine.window().newIrfWhereConfigured(fmpRevision);
								topLine.window().fireChangeEvent();
								dlg.setWorkingFmpTopLine(topLine);

								//								//Command에서 생성한 BOM Window 닫기.
								//								dlg.getWorkingFmpTopLine().window().save();
								//								dlg.getWorkingFmpTopLine().window().close();
								//								topLine = CustomUtil.getBomline(fmpRevision, session);
								//								dlg.setWorkingFmpTopLine(topLine);
								//								
								//								// dataMapper 다시 셋팅.
								//								dataMapper.initialize(topLine, dlg.getOspec(), dlg.getEssentialNames(), true);
								//								BOMLoadOperation.loadChildMap(dataMapper, topLine, storedOptionSetMap);

								bomLines = dataMapper.getBomlineMap().get(systemRowKey);
								ArrayList<TCComponentBOMLine> tLines = bomLines.get(occThread);
								for( int j = 0; j < tLines.size();j++){
									line = tLines.get(j);
									if( line != null && line.isValidUid()){
										parentLine = line.parent();
										break;
									}
								}
							}else{
								parentRevision = revise(parentRevision);
								//FIXME: 2016.12.07 BOM Window refresh 수정. TEST(OK)
								//parentLine.window().refresh();
								topLine.window().newIrfWhereConfigured(parentRevision);
								topLine.window().fireChangeEvent();
							}
						}

						parentLine.remove(null, line);
						//						isDeleted = true;
						//						break;
						//Condition = "NONE" ==> 무조건 삭제로 수정.
						//[20150610]
						//						if( type.equals(TypeConstant.S7_PREVEHICLEPARTTYPE)){
						//							// Condition의 Trim정보를 NONE로 셋팅
						//							String lineMvl = fmpTopLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID) + ":TRIM = \"NONE\""; 
						//							((SYMCBOMLine)line).setMVLCondition(lineMvl);
						//						}else{
						//							parentLine.remove(null, line);
						//						}
					}else{
						throw new TCException("Parent Part is not PRE-BOM Item Type.");
					}
				}

				//				if( isDeleted){
				//					break;
				//				}

			}

			dataMapper.removePropertMap(key, false);

			//			HashMap<String, Object> changedProp = changedData.get(key);
			//			if( changedProp != null){
			//				//삭제가 아닌 Parent No가 바뀐 경우는 변경 사항이 이미 반영되어 있으므로. 키를 삭제하지 않음.
			//				if( !changedProp.containsKey("PARENT_NO")){
			//					dataMapper.removePropertMap(key, false);
			//				}else{
			//					dataMapper.removePropertMap(key, true);
			//				}
			//			}else{
			//				dataMapper.removePropertMap(key, false);
			//			}

		}

	}

	public TCComponentItemRevision revise(TCComponentItemRevision revision) throws Exception{

		if (waitBar != null) {
			waitBar.setStatus(revision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID) + " revising...");
		}
		String nextRevId = CustomUtil.getNextRevID(revision.getItem(), "Item");
		TCComponentItemRevision newRevision = revision.saveAs(nextRevId);

		//		[20170523][ljg] FMP 최종 리비전이 Release상태 일때, 개정 되지 않는 경우 발생
		if(CustomUtil.isReleased(fmpRevision.getItem().getLatestItemRevision())){
			fmpRevision = revise(fmpRevision);
		}
		//		TCComponentItemRevision newRevision = revision.getItem().revise(nextRevId, revision.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME), revision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC));

		// [20160705][ymjang] Lock/Save/Unlock 구문 삭제
		//newRevision.lock();
		HashMap<String, String> propMap = new HashMap();
		propMap.put(PropertyConstant.ATTR_NAME_MATURITY, "In Work");

		if( newRevision.getType().equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE)){
			propMap.put(PropertyConstant.ATTR_NAME_PARTTYPE, "K");
			propMap.put(PropertyConstant.ATTR_NAME_STAGE, "C");
			//[20170519][ljg] 다른 프로젝트의 CarryOver 파트를 리바이즈 하면, 프로젝트 코드가 변경 되는 문제 가 있어서 수정 함
			if(CustomUtil.isNullString(revision.getStringProperty(PropertyConstant.ATTR_NAME_PROJCODE))){
				propMap.put(PropertyConstant.ATTR_NAME_PROJCODE, project);
			}else{
				propMap.put(PropertyConstant.ATTR_NAME_PROJCODE, revision.getStringProperty(PropertyConstant.ATTR_NAME_PROJCODE));
			}
			propMap.put(PropertyConstant.ATTR_NAME_SELECTIVEPART, "");

			// [NoSR][20160216][jclee] Revise UP 시 Color ID, Regulation은 기존 Revision의 속성을 가져온다.
			//			propMap.put(PropertyConstant.ATTR_NAME_COLORID, "");
			//			propMap.put(PropertyConstant.ATTR_NAME_REGULATION, ".");
			propMap.put(PropertyConstant.ATTR_NAME_COLORID, revision.getProperty(PropertyConstant.ATTR_NAME_COLORID));
			propMap.put(PropertyConstant.ATTR_NAME_REGULATION, revision.getProperty(PropertyConstant.ATTR_NAME_REGULATION));

			String[] refPropNames = new String[]{PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL
					//					, PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, PropertyConstant.ATTR_NAME_PRD_PROJ_CODE
					//					, PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST, PropertyConstant.ATTR_NAME_PRD_SERVICE_COST
					//					, PropertyConstant.ATTR_NAME_PRD_TOOL_COST
			};
			String[] refPropValues = null;
			TCComponent oldRefComp = revision.getReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF);
			if( oldRefComp != null){
				refPropValues = oldRefComp.getProperties(refPropNames);
			}else{
				//				refPropValues = new String[]{"","","","","","",""};
				refPropValues = new String[]{"",""};
			}

			TCComponent refComp = SYMTcUtil.createApplicationObject(revision.getSession(), TypeConstant.S7_PREVEHTYPEDREFERENCE, refPropNames, refPropValues);
			/* [20160715][ymjang] CCN NO 저장 안된 Part 가 존재함. --> 오류 개선
			TCProperty tcProp = newRevision.getTCProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF);
			tcProp.setReferenceValue(refComp);
			 */
			newRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, refComp);
		}

		newRevision.refresh();
		newRevision.setProperties(propMap);

		// [NoSR][2016.01.08][jclee] Revise된 Part에 CCN No 입력 오류 수정
		/* [20160715][ymjang] CCN NO 저장 안된 Part 가 존재함. --> 오류 개선
		TCProperty tcProp = newRevision.getTCProperty(PropertyConstant.ATTR_NAME_CCNNO);
		tcProp.setReferenceValue(ccnRevision);
		 */
		newRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);

		//newRevision.save();
		//newRevision.unlock();
		return newRevision;
	}

	/**
	 * Condition을 적용함.
	 * 
	 * @throws TCException
	 */
	private void setCondition(TCComponentBOMLine bomLine, Vector<ConditionVector> conditions) throws TCException{

		String lineMvl = "";
		for( int i = 0; conditions != null && i < conditions.size(); i++){
			ConditionVector condition = conditions.get(i);
			if( condition == null) continue;

			String tmpStr = "";
			for( int j = 0; j < condition.size(); j++ ){
				ConditionElement elm = condition.get(j);
				tmpStr += ( j>0 ? " and ":"") + elm.item + ":" + MVLLexer.mvlQuoteId(elm.option, false) + " = " +  MVLLexer.mvlQuoteString(elm.value);
			}

			lineMvl += ( !lineMvl.equals("") ? " or ":"") + tmpStr;
		}

		/**
		 *  옵션 사이즈 체크.
		 * [SR140722-022][20140708] swyoon 옵션 사이즈 체크.
		 */	
		int convertedLength = SYMStringUtil.getConvertedLength(lineMvl);
		if( convertedLength > 4000){
			throw new TCException("Option length limit is exceeded.");
		}		

		//이력을 남기기 위해 아래로 수정
		((SYMCBOMLine)bomLine).setMVLCondition(lineMvl);
	}

	/**
	 * condition 파라미터가 문자열로 오는 경우는 Usage를 사용자가 직접 입력한 경우이고, 이 경우 트림 옵션만 존재하며,
	 *  "OR"로 묶여 있다.
	 * 
	 * @param bomLine
	 * @param condition
	 * @throws TCException
	 */
	private void setCondition(TCComponentBOMLine bomLine, String condition) throws TCException{

		String lineMvl = "";

		condition = condition + " ";
		ArrayList<String> foundOpValueList = new ArrayList();
		Pattern p = Pattern.compile("[a-zA-Z0-9]{4} |[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT|and |or ");
		Matcher m = p.matcher(condition);
		while (m.find()) {
			String str = m.group().trim();
			if( str.equalsIgnoreCase("OR") || str.equalsIgnoreCase("AND")){
				foundOpValueList.add(m.group());
			}else{
				// A and B or A and C 와 같은 Option을 넣을 경우 A and B or and C로 뒷쪽 A가 누락되는 현상으로 인해 로직 수정.
				// 아래와 같은 로직이 왜 구현되어있는지 알수 없음...모니터링 필요
				//				if( !foundOpValueList.contains(m.group())){
				//					foundOpValueList.add(m.group());
				//				}
				foundOpValueList.add(m.group());
			}
		}

		String fmpId = fmpRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
		//F620XA2015:B00 = "B00L" and F620XA2015:C00 = "C00Q" or F620XA2015:B00 = "B00R" and F620XA2015:C00 = "C00Q"
		for( int i = 0; i < foundOpValueList.size(); i++){
			String opValue = foundOpValueList.get(i).trim();
			if( opValue.equalsIgnoreCase("and") || opValue.equalsIgnoreCase("or")){
				lineMvl += " " + opValue + " ";
				continue;
			}
			String category = BomUtil.getCategory(opValue);
			lineMvl += fmpId + ":" + category + " = " + "\"" + opValue + "\"";
			//			if( lineMvl.equals("")){
			//				lineMvl = fmpId + ":" + category + " = " + "\"" + opValue + "\"";
			//			}else{
			//				lineMvl += " or " + fmpId + ":" + category + " = " + "\"" + opValue + "\"";
			//			}
		}

		/**
		 *  옵션 사이즈 체크.
		 * [SR140722-022][20140708] swyoon 옵션 사이즈 체크.
		 */	
		int convertedLength = SYMStringUtil.getConvertedLength(lineMvl);
		if( convertedLength > 4000){
			throw new TCException("Option length limit is exceeded.");
		}		

		//이력을 남기기 위해 아래로 수정
		((SYMCBOMLine)bomLine).setMVLCondition(lineMvl);
	}


	/**
	 * 순환 구조 인지 확인.
	 * 
	 * @param childParentMap
	 * @param orgPartId
	 * @param partId
	 * @return
	 */
	private boolean isCircleStructure(HashMap<String, ArrayList<String>> childParentMap, String curPartId){

		if (childParentMap == null) 
			return false;

		if (curPartId == null || curPartId.equals("")) 
			return false;

		ArrayList<String> outerParentList = childParentMap.get(curPartId);
		if (outerParentList != null) {
			for( String outerParent:outerParentList) {
				ArrayList<String> innerParentList = childParentMap.get(outerParent);
				if (innerParentList != null) {
					for( String innerParent:innerParentList) {
						if (innerParent.equals(curPartId))						
							return true;
					}
				}
			}
		}

		return false;
	}

	/* [20160707][ymjang] BOM Saving 시 Validation 최소화
	private boolean isCircleStructure(HashMap<String, ArrayList<String>> childParentMap, String orgPartId, String partId){

		if( childParentMap.containsKey(partId)){
			ArrayList<String> parentList = childParentMap.get(partId);
			for( String parent:parentList){
				if( parent.equals(orgPartId)){
					return true;
				}

				if( isCircleStructure(childParentMap, orgPartId, parent)){
					return true;
				}
			}

			return false;
		}

		return false;
	}
	 */
	
	private int getByte(Object o){
		int txtByte = 0;

		if ( o.toString().trim() != null && !o.toString().trim().equals("")) {
			
			 // 바이트 체크 (영문 1, 한글 2, 특문 1)
	        int en = 0;
	        int ko = 0;
	        int etc = 0;
	 
	        char[] txtChar = o.toString().trim().toCharArray();
	        for (int j = 0; j < txtChar.length; j++) {
	            if (txtChar[j] >= 'A' && txtChar[j] <= 'z') {
	                en++;
	            } else if (txtChar[j] >= '\uAC00' && txtChar[j] <= '\uD7A3') {
	                ko++;
	                ko++;
	            } else {
	                etc++;
	            }
	        }
	 
	        txtByte = en + ko + etc;
			
		}
		
		return txtByte;
	}
	
	private boolean isIncludeList(Object o, String[] strs){
		boolean isInclude = false;
		if ( o.toString().trim() != null && !o.toString().trim().equals("")) {
			if(strs == null){
				return false;
			}
			
			for( int i = 0; i < strs.length; i ++ ) {
				if( o.toString().trim().equals(strs[i])) {
					isInclude  = true;
					break;
				}
				
			}
		} else {
			isInclude  = true;
		}
		
		return isInclude;
	}

	/**
	 * [20160707][ymjang] BOM Saving 시 Validation 최소화
	 * @param validateResult
	 * @return
	 * @throws Exception
	 */
	private boolean validateModelInfo(Vector<Vector> validateResult) throws Exception {
		MasterListTablePanel masterListTablePanel = dlg.getMasterListTablePanel();
		masterListTablePanel.clearAllFilter();

		boolean result = true;
		ArrayList<String> sequenceList = new ArrayList();
		ArrayList<String> partIdList = new ArrayList();
		Object obj = null;
		JTable table = masterListTablePanel.getTable();
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		
		
		/* *********************************************************************************
		 * Validation 을 위한 데이터 준비
		 * ********************************************************************************* */
		HashMap<String, ArrayList<String>> childParentMap = new HashMap<String, ArrayList<String>>();		
		for( int row = 0; row < model.getRowCount(); row++) {
			ArrayList<String> parentList = null;

			// Part No
			String partId = null, parentId = null;
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue)obj;
				HashMap<String, Object> cellData = cellValue.getData();
				if( cellData != null){
					partId = obj.toString().trim();
				}
			}

			// Parent No
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX);
			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue)obj;
				HashMap<String, Object> cellData = cellValue.getData();
				if( cellData != null){
					parentId = obj.toString().trim().replaceAll(" ", "").replaceAll("-",  "");	
				}
			}

			if( parentId == null || parentId.equals("")){
				parentId = fmpId;
			}

			if (partId != null && !partId.equals("")) {

				if(!partIdList.contains(partId))
					partIdList.add(partId);

				/**
				 * 1. 무한 순환구조 체크를 위한 Parent Map 생성
				 */
				if(childParentMap.containsKey(partId)){
					parentList = (ArrayList<String>) childParentMap.get(partId);
					for( String parent:parentList) {
						if( parent.equals(parentId)) {
							continue;
						}
						parentList.add(parentId);
					}
				} else {
					parentList = new ArrayList<String> ();
					parentList.add(parentId);
				}
				childParentMap.put(partId, parentList);
			}
		}

		/**
		 * 2. Option 유효값 체크를 위한 사용가능한 Option Category List 생성
		 */
		ArrayList<String> categories = new ArrayList();
		ArrayList<VariantOption> enableOptionList = dlg.getEnableOptionSet();
		for( int i = 0; i < enableOptionList.size(); i++){
			String str = enableOptionList.get(i).getOptionName();
			if( !categories.contains(str)){
				categories.add(str);
			}
		}

		// [NoSR][20160427][jclee] Interface 시 담당팀, 담당자 Validation을 위해 Vision NET 사용자 정보를 수집
		// CALS.SYSA02TB@LINK_001_VNET
		/*
		CustomECODao dao = new CustomECODao();	
		ArrayList<HashMap<String, String>> alUserOnVNet = dao.searchUserOnVnet(null, null);
		 */

		/**
		 * 5. 사용자 목록 생성
		 */   
		DataSet ds = new DataSet();
		ds.setObject("PARAM", null);
		List<HashMap<String, Object>> alUserOnVNet = DCSCommonUtil.selectVNetUserList(ds);

		/* *********************************************************************************
		 * Validation 수행
		 * ********************************************************************************* */
		
		String partId = null, parentId = null, nmcd = null, options = null;
		for( int row = 0; row < model.getRowCount(); row++) {
			waitBar.setStatus("Checking Validation....(" + (row+1) + "/" + model.getRowCount() + ")");
			// Part No, System_Row_Key, 단위 필수입력 체크
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
			if( obj instanceof CellValue) {
				CellValue cellValue = (CellValue)obj;
				HashMap<String, Object> cellData = cellValue.getData();
				if( cellData == null) {
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
					rowVec.add("Invalid Cell Type");
					validateResult.add(rowVec);
					result = false;
				} else {

					if( !cellData.containsKey(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY)){
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
						rowVec.add("Invalid Cell Type (System Row Key)");
						validateResult.add(rowVec);
						result = false;
					}

					String uom = (String)cellData.get(PropertyConstant.ATTR_NAME_UOMTAG);
					if( uom == null || uom.equals("")){
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
						rowVec.add("Select a Unit of measure.");
						validateResult.add(rowVec);
						result = false;
					}

					partId = obj.toString().trim();
				}
			} else {
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
				rowVec.add("Invalid Cell Type");
				validateResult.add(rowVec);
				result = false;
			}

			// Parent No
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX);
			if( !obj.toString().trim().equals("")){
				parentId = obj.toString().trim().replaceAll(" ", "").replaceAll("-",  "");				
			}

			if( parentId == null || parentId.equals("")){
				parentId = fmpId;
			}

			// nmcd
			nmcd = "";
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_NMCD_IDX);
			if(!obj.toString().equals("")){
				nmcd = obj.toString().trim();
			}

			/**
			 * 1. 순환구조체크
			 */
			if(isCircleStructure(childParentMap, partId)){

				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_PART_ID_IDX);

				int viewRow = table.convertRowIndexToView(row);
				if( viewRow < 0 ){
					rowVec.add(MSG_CIRCLE_REFERENCE + "(hidden Row)" );
				}else{
					rowVec.add(MSG_CIRCLE_REFERENCE);
				}

				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 2. Parent ID 필수 입력 체크
			 * --> 삭제함.(Parent ID 가 Null 일 경우, FMP ID 를 넣어 줌.)
			 */
			/*
			if( parentId.equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX);
				rowVec.add("Input a Parent No.");
				validateResult.add(rowVec);
				result = false;
			}
			 */
			
			/*
			 * contents  30 바이트 초과 입력 검사
			 */
			obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_CONTENTS_IDX);
			int inputByte = getByte(obj);
			if(inputByte > 30){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add( masterListTablePanel.MASTER_LIST_CONTENTS_IDX);
				rowVec.add("The entered CONTENTS value exceeds 30 bytes. (" + inputByte + "/30)");
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 3. Option 유효값 체크
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SPEC_IDX);
			if(!obj.toString().equals("")){
				options = obj.toString();
			}

			String key = getKey(model, row);			
			if( options != null && !options.equals("")) { 

				String conditionStr = obj.toString();				
				Pattern p = Pattern.compile("[a-zA-Z0-9]{5,}_STD|[a-zA-Z0-9]{5,}_OPT|[a-zA-Z0-9]{4}");
				Matcher m = p.matcher(conditionStr);
				while (m.find()) {
					String tmpOpValue = m.group().trim();
					String category = OpUtil.getCategory(tmpOpValue);
					if( !categories.contains(category)){
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(MasterListTablePanel.MASTER_LIST_SPEC_IDX);
						rowVec.add("Invalid Option Type");
						validateResult.add(rowVec);
						result = false;
						break;
					}
				}
			}

			/**
			 * 4. Parent 하위에 PreVehicle Part 중복 할당 체크
			 *    --> Vehicle Part 또는 Std part가 아닌 경우만 중복 체크를 함.
			 */   
			HashMap<String, ArrayList<String>> childMap = new HashMap<String, ArrayList<String>>();
			if( partId != null && !partId.equals("")) {
				HashMap<String, String> stdVehPartMap = WebUtil.getPart(partId);
				if( stdVehPartMap == null || stdVehPartMap.isEmpty() ){
					ArrayList childList = childMap.get(parentId);
					if( childList == null) {
						childList = new ArrayList();
						childList.add(partId);						
					} else {
						if( childList.contains(partId)) {
							Vector rowVec = new Vector();
							rowVec.add(row);
							rowVec.add(MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
							rowVec.add(MSG_DUPLICATED_PART_ID);
							validateResult.add(rowVec);
							result = false;
						} else {
							childList.add(partId);
						}
					}
					childMap.put(parentId, childList);
				}				
			}

			if( partId == null || partId.equals("")) {
				/**
				 * 5. Carry Over Part 인데 Part Unique No 가 없을 경우 
				 */
				if (nmcd.equalsIgnoreCase("C")){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX);
					rowVec.add(MSG_INPUT_CARRY_OVER_PART_ID);
					validateResult.add(rowVec);
					result = false;
				}
			} else {
				/**
				 * 6. PART UNIQUE NO 존재여부
				 */
				if( !WebUtil.isExistPartNo(partId)) {
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
					rowVec.add(MSG_NOT_EXIST_PART_ID);
					validateResult.add(rowVec);
					result = false;
				}
			}

			/**
			 * 7. Carry Over Part 가 아닌 경우, 시스템 코드 필수 입력 체크
			 * [20180223][LJG] 무조건 system code 는 필수 - 송대영 차장 요청
			 */ 
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SYSTEM_IDX);
			//[20180223][LJG] 무조건 system code 는 필수 - 송대영 차장 요청
			if( CustomUtil.isNullString(obj.toString()) /** && !nmcd.equalsIgnoreCase("C") */){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_SYSTEM_IDX);
				rowVec.add(MSG_INPUT_SYSTEM_CODE);
				validateResult.add(rowVec);
				result = false;
			}
			
			/******************************************************************************************************/
			
			if( fmpRevision.getProperty("item_revision_id").toString().trim().equals("001")) {
				/****************************************************************************************************/
				// Pre BOM 생성전에 Validation 추가
				String[] projectArray = CustomUtil.getLOVDisplayValues(session, "S7_PROJECT_CODE");
				String[] systemcodeArray = CustomUtil.getLOVDisplayValues(session, "S7_SYSTEM_CODE");
				String[] reqOtpArray = new String[]{"Y", "N"};  // Req : Y, N
				String[] nmcdArray = new String[]{"N", "M1", "M2", "M3", "C", "D", "T"};
				String[] protoToolingArray = new String[]{"", "Y"};
				String[] smodeArray = CustomUtil.getLOVDisplayValues(session, "S7_SUPPLY_MODE");
				String[] moduleArray = CustomUtil.getLOVDisplayValues(session, "S7_MODULE_CODE");
				String[] drArray = CustomUtil.getLOVDisplayValues(session, "S7_CATEGORY");
				String[] responsibilityArray = CustomUtil.getLOVDisplayValues(session, "s7_RESPONSIBILITY");
				String[] dwgTypeArray  = new String[]{"2D","3D", "BOTH"};
//				String pValue = "(";
				/****************************************************************************************************/
				
				/*
				 *  Project Code 값이 ComboBox 값에 포함 되는지 확인
				 */
				obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PROJECT_IDX);
				if(!isIncludeList(obj, projectArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_PROJECT_IDX);
					rowVec.add("The Project_Code value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				
				/*
				 *  System Code 값이 ComboBox 값에 포함 되는지 확인
				 */
				obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SYSTEM_IDX);
				if(!isIncludeList(obj, systemcodeArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_SYSTEM_IDX);
					rowVec.add("The System_Code value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 *  NMCD 값이 ComboBox 값에 포함 되는지 확인
				 */
//				pValue = "(";
				obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_NMCD_IDX);
				if(!isIncludeList(obj, nmcdArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_NMCD_IDX);
					rowVec.add("The NMCD value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 *  REQ 값이 ComboBox 값에 포함 되는지 확인
				 */
//				pValue = "(";
				obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX);
				if(!isIncludeList(obj, reqOtpArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX);
					rowVec.add("The REQ.OPT. value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 *  Proto Tooling 값이 ComboBox 값에 포함 되는지 확인
				 */
//				pValue = "(";
				obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PROTO_TOOLING_IDX);
				if(!isIncludeList(obj, protoToolingArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_PROTO_TOOLING_IDX);
					rowVec.add("The Proto Tooling value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
			
				/*
				 *   Smode 값이 ComboBox 값에 포함 되는지 확인
				 */
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_SUPPLY_MODE_IDX);
				if(!isIncludeList(obj, smodeArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_SUPPLY_MODE_IDX);
					rowVec.add("The S/Mode value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 *   Module 값이 ComboBox 값에 포함 되는지 확인
				 */
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_MODULE_IDX);
				if(!isIncludeList(obj, moduleArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_MODULE_IDX);
					rowVec.add("The Module value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 *   DR 값이 ComboBox 값에 포함 되는지 확인
				 */
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_DR_IDX);
				if(!isIncludeList(obj, drArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_DR_IDX);
					rowVec.add("The DR value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 *   Responssibility 값이 ComboBox 값에 포함 되는지 확인
				 */
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_RESPONSIBILITY_IDX);
				if(!isIncludeList(obj, responsibilityArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_RESPONSIBILITY_IDX);
					rowVec.add("The Responsibility value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 *   2D/3D 값이 ComboBox 값에 포함 되는지 확인
				 */
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_CON_DWG_TYPE_IDX);
				if(!isIncludeList(obj, dwgTypeArray)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_CON_DWG_TYPE_IDX);
					rowVec.add("The 2D/3D value entered does not match the LOV value.");
					validateResult.add(rowVec);
					result = false;
				}
				
				/*
				 * DPV USE  30 바이트 초과 입력 검사
				 */
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_USE_IDX);
				inputByte = getByte(obj);
				if(inputByte > 30){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add( masterListTablePanel.MASTER_LIST_DVP_USE_IDX);
					rowVec.add("The entered DPV USE value exceeds 30 bytes. (" + inputByte + "/30)");
					validateResult.add(rowVec);
					result = false;
				}
			}
				
				/******************************************************************************************************/
				

			/**
			 * 8. LEVEL 필수 입력 체크
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_LEV_MAN_IDX);
			if( obj.toString().equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_LEV_MAN_IDX);
				rowVec.add(MSG_INPUT_LEV_MAN);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 9. SEQ 필수 입력 체크
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SEQUENCE_IDX);
			String str = obj.toString().trim(); 
			if( str.equals("")){

				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_SEQUENCE_IDX);
				rowVec.add(MSG_INPUT_SEQ_NO);
				validateResult.add(rowVec);
				result = false;				
			} else {

				/**
				 * 10. SEQ 중복 체크
				 */   
				if( sequenceList.contains(key)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_SEQUENCE_IDX);
					rowVec.add(MSG_DUPLICATED_SEQ_NO);
					validateResult.add(rowVec);
					result = false;
				} else {
					//[20191216][CSH]seq check 안되고 있었음. 송대영 책임 확인 결과 필요하다고 함.
					sequenceList.add(key);
					try{
						Integer.parseInt(str);
//						sequenceList.add(str);
					}catch(NumberFormatException nfe){
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(MasterListTablePanel.MASTER_LIST_SEQUENCE_IDX);
						rowVec.add(MSG_INVALID_SEQ_NO);
						validateResult.add(rowVec);
						result = false;
					}
				}				
			}

			/**
			 * 11. Parent 입력 오류 체크 
			 */   
			if( !parentId.equals(fmpId)){
				// Master List 상에 존재하지 않는 Parent 가 있는지 
				if( !partIdList.contains(parentId)){
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX);
					rowVec.add(MSG_NOT_FOUND_PARENT_NO);
					validateResult.add(rowVec);
					result = false;
				}

				// Parent Part는 Standard 또는 Vehicle Part일 수 없다.
				HashMap<String, String> stdVehPartMap = WebUtil.getPart(parentId);
				if( stdVehPartMap != null && !stdVehPartMap.isEmpty()) {
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX);
					rowVec.add(MSG_IS_STD_VEH_PART);
					validateResult.add(rowVec);
					result = false;
				}
			}

			/**
			 * 12. Display Part No 필수 입력 체크 
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX);
			if( obj.toString().equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX);
				rowVec.add(MSG_INPUT_DISPLAY_PART_ID);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 13. Part Name 필수 입력 체크 
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
			if( obj.toString().equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
				rowVec.add(MSG_INPUT_PART_NAME);
				validateResult.add(rowVec);
				result = false;
			} 
			/* [20160713][ymjang] Part Name 존재 여부 Validation 삭제
			else {

				// 14. Part Name 존재여부
				if ( obj instanceof CellValue) {
					CellValue partNameCellValue = (CellValue)obj;
					HashMap<String, Object> nameData = partNameCellValue.getData();
					if( nameData == null){
						nameData = new HashMap();
					}
					Object opObj = nameData.get("IS_OPEN_DLG");
					if( opObj == null){
						// Name Validation
						if( !WebUtil.isExistPartName(obj.toString())){
							Vector rowVec = new Vector();
							rowVec.add(row);
							rowVec.add(MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
							rowVec.add(MSG_INVALID_PART_NAME);
							validateResult.add(rowVec);
							result = false;
						}
					} else {
						if( opObj instanceof Boolean){
							boolean b = (boolean)opObj;
							if( !b ){
								if( !WebUtil.isExistPartName(obj.toString())){
									Vector rowVec = new Vector();
									rowVec.add(row);
									rowVec.add(MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
									rowVec.add(MSG_INVALID_PART_NAME);
									validateResult.add(rowVec);
									result = false;
								}
							}
						} else {
							if( !WebUtil.isExistPartName(obj.toString())){
								Vector rowVec = new Vector();
								rowVec.add(row);
								rowVec.add(MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
								rowVec.add(MSG_INVALID_PART_NAME);
								validateResult.add(rowVec);
								result = false;
							}
						}
					}
				} else {
					// Name Validation
					if( !WebUtil.isExistPartName(obj.toString())){
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
						rowVec.add(MSG_INVALID_PART_NAME);
						validateResult.add(rowVec);
						result = false;
					}
				}
			}
			 */

			/**
			 * 15. SPEC Desc. 길이 제한 체크 (500자)
			 * [20170312][ymjang] SPEC Desc. 길이 제한 체크
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SPEC_DESC_IDX);
			str = obj.toString().trim();
			if( obj.toString().length() > 500){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_SPEC_DESC_IDX);
				rowVec.add(MSG_EXCEED_MAXIMUM_SIZE);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 16. NMCD 필수 입력 체크
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_NMCD_IDX);
			if( obj.toString().trim().equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_NMCD_IDX);
				rowVec.add(MSG_SELECT_TYPE);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 17. Project 필수 입력 체크
			 */   
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PROJECT_IDX);
			if( obj.toString().trim().equals("") &&  !nmcd.equalsIgnoreCase("C")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_PROJECT_IDX);
				rowVec.add(MSG_SELECT_PROJECT_TYPE);
				validateResult.add(rowVec);
				result = false;
			}
			
			/**
			 * 18. 대표수량 필수 입력 체크
			 */   
			// 대표수량 필수입력이고, 입력하지 않을 시 오류.
			CellValue partIdCellValue = (CellValue) model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
			obj = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
			double repQty = 0d;
			str = obj.toString().trim();
			if( str.equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
				rowVec.add(MSG_INPUT_REPRESENTATIVE_QUANTITY);
				validateResult.add(rowVec);
				result = false;
			} else {
				try{
					repQty = Double.parseDouble(str);
					Integer.parseInt(str);
				}catch(NumberFormatException nfe){
					try {
						Double.parseDouble(str);
						HashMap dataMap = partIdCellValue.getData();
						String uom = (String)dataMap.get(PropertyConstant.ATTR_NAME_UOMTAG);
						if( uom != null){
							if( uom.equalsIgnoreCase("EA")){
								Vector rowVec = new Vector();
								rowVec.add(row);
								rowVec.add(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
								rowVec.add(MSG_INVALID_UOM);
								validateResult.add(rowVec);
								result = false;	
							}
						}
					} catch (NumberFormatException nfe2) {
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
						rowVec.add(MSG_INPUT_REPRESENTATIVE_QUANTITY);
						validateResult.add(rowVec);
						result = false;	
					}
				}

				// [20160622][ymjang] 대표수량과 USAGE 수량이 다를 경우 Validation 

//				repQty = Double.parseDouble(str);

			}

			/**
			 * 19. 대표수량과 USAGE 수량이 다를 경우 Validation 
			 */   
			// Usage 수량을 하나도 입력하지 않을 경우 오류 메시지.
			double maxQty = 0d;
			int startIdx = masterListTablePanel.getFixedColumnPre().length - 1;
			int endIdx = masterListTablePanel.getFixedColumnPre().length + dlg.getOspec().getTrimList().size() - 1;
			for( int column = startIdx; column < endIdx; column++){
				obj = model.getValueAt(row, column);
				str = obj.toString();
				str = str.replaceAll("\\(", "");
				str = str.replaceAll("\\)", "");
				try{
					double qty = (str == null || str.equals("")) ? 0d : Double.parseDouble(str);
					if( qty > maxQty){
						maxQty = qty;
					}
					// [20160622][ymjang] 대표수량과 USAGE 수량이 다를 경우 Validation 
					if (qty != 0d && repQty != qty) {
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(startIdx);
						rowVec.add(MSG_INPUT_ERROR_USAGE_QUANTITY);
						validateResult.add(rowVec);
						result = false;
						break;
					}
				}catch( NumberFormatException nfe){
					// [20160622][ymjang] Number Formaat 이 아닐 경우, Validation 체크
					Vector rowVec = new Vector();
					rowVec.add(row);
					//[CSH][20180425]usage 부분 오류 위치 정확하게 표현
					rowVec.add(column);
					rowVec.add(MSG_INPUT_USAGE_QUANTITY);
					validateResult.add(rowVec);
					result = false;
					break;
					//continue;
				}
			}

			/**
			 * 20. Usage 수량 필수 입력 체크
			 */
			//[20170215] 옵션이 있으면 Usage 수량 필수 입력 체크 PASS
			boolean isOptionExist = options != null && !options.equals("");
			if( maxQty == 0 && !isOptionExist){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(startIdx);
				rowVec.add(MSG_INPUT_USAGE_QUANTITY);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 21. Supply Mode 필수 입력 체크 및 Supply Mode에 따른 예상중량 필수 입력 체크
			 */   
			obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_SUPPLY_MODE_IDX);
			String supplyMode = obj.toString().trim();
			// [20161004][ymjang] S/Mode 필수입력 체크 추가
			if (supplyMode.equals("")) {
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(masterListTablePanel.MASTER_LIST_SUPPLY_MODE_IDX);
				rowVec.add(MSG_INPUT_SUPPLY_MODE);
				validateResult.add(rowVec);
				result = false;
			}

			if( !nmcd.equalsIgnoreCase("C") && !supplyMode.equals("")){				
				// C0 P7,P1,PD,P7,P7MP8,P7YP8,P7CP8,PDMP8,PDYP8,C1,C7,CD 은 예상중량 필수 입력
				if( supplyMode.equals("C0") || supplyMode.equals("P7") || supplyMode.equals("P1") || supplyMode.equals("PD") || supplyMode.equals("P7") || 
						supplyMode.equals("P7MP8") || supplyMode.equals("P7YP8") || supplyMode.equals("P7CP8") || supplyMode.equals("PDMP8") || supplyMode.equals("PDYP8") || 
						supplyMode.equals("C1") || supplyMode.equals("C7") || supplyMode.equals("CD")){

					obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_EST_WEIGHT_IDX);
					if( obj.toString().trim().equals("")) {
						Vector rowVec = new Vector();
						rowVec.add(row);
						rowVec.add(masterListTablePanel.MASTER_LIST_EST_WEIGHT_IDX);
						rowVec.add(MSG_INPUT_EST_WEIGHT);
						validateResult.add(rowVec);
						result = false;
					}
				}
			}

			//20201021 seho EJS column 체크로직..
			//NMCD 필드가 "C" 이면서 Supply mode 가 C0, C1, C7, CD, C7UC8, C7YC8, P0, P1, P7, PD, P7UP8, P7YP8, P7MP8 인 경우 필수 입력 항목 지정
			//20201106 seho EJS 체크로직 수정.
			//DR이 DR1,DR2,DR3 일 경우... 
			Object drObject = model.getValueAt(row, masterListTablePanel.MASTER_LIST_DR_IDX);
			String drString = drObject == null ? "" : drObject.toString().trim();
			if (nmcd.equalsIgnoreCase("C")
					&& (supplyMode.equals("C0") || supplyMode.equals("C1") || supplyMode.equals("C7") || supplyMode.equals("CD") || supplyMode.equals("C7UC8") || supplyMode.equals("C7YC8") || supplyMode.equals("P0") || supplyMode.equals("P1") || supplyMode.equals("P7") || supplyMode.equals("PD") || supplyMode.equals("P7UP8") || supplyMode.equals("P7YP8") || supplyMode.equals("P7MP8"))
					&& (drString.equals("DR1") || drString.equals("DR2") || drString.equals("DR3")))
			{
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_EJS_IDX);
				if (obj.toString().trim().equals(""))
				{
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_EJS_IDX);
					rowVec.add(MSG_INPUT_EJS);
					validateResult.add(rowVec);
					result = false;
				}
			}
			//20201105 seho EJS column 체크 로직 추가.
			//위 필수가 아닌 경우 EJS 속성에 값이 있으면 안됨.
			else
			{
				obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_EJS_IDX);
				if (!obj.toString().trim().equals(""))
				{
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_EJS_IDX);
					rowVec.add(MSG_INPUT_NOT_EJS);
					validateResult.add(rowVec);
					result = false;
				}
			}

			/**
			 * 22. Responsibility 필수 입력 체크
			 */   
			obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_RESPONSIBILITY_IDX);
			if( !nmcd.toString().equalsIgnoreCase("C") && obj.toString().trim().equals("") ){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(masterListTablePanel.MASTER_LIST_RESPONSIBILITY_IDX);
				rowVec.add(MSG_INPUT_RESPONSIBILITY);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 23. DPV NEED QTY 수량 Number 유효성 체크
			 * [20170312][ymjang] DPV NEED QTY Number Formaat 이 아닐 경우, Validation 체크
			 */   
			obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_DVP_NEEDED_QTY_IDX);
			str = obj.toString().trim();
			try{
				double qty = (str == null || str.equals("")) ? 0d : Double.parseDouble(str);
			}catch( NumberFormatException nfe){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(masterListTablePanel.MASTER_LIST_DVP_NEEDED_QTY_IDX);
				rowVec.add(MSG_INVALID_DVP_NEEDED_QTY);
				validateResult.add(rowVec);
				result = false;
				//continue;
			}

			/**
			 * 24. 도면 배포일자 필수 입력 체크
			 */   
			obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_DWG_DEPLOYABLE_DATE_IDX);
			if( !obj.toString().equals("")){

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					sdf.parse(obj.toString());
				} catch (ParseException e) {
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_DWG_DEPLOYABLE_DATE_IDX);
					rowVec.add(MSG_INVALID_DATE_TYPE);
					validateResult.add(rowVec);
					result = false;
				}

			}

			/**
			 * 25. 설계부서 필수입력 체크
			 */   
			// [NoSR][20160428][jclee] 담당팀, 담당자 Validation.
			// - 동명이인
			// - 정확한 담당팀, 담당자 입력을 통한 사번 체크가 가능한지 여부 확인 (Full BOM Interface 시 사번을 넘겨주기 위해 사용)
			// * NMCD에 따른 입력 여부 Validation Check는 제거한다.

			//자동 입력됨
			//담당부서 필수 입력(PA6값)
			obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_DEPT_NM_IDX);
			if( obj.toString().trim().equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(masterListTablePanel.MASTER_LIST_ENG_DEPT_NM_IDX);
				rowVec.add(MSG_INPUT_RESPONSIBILITY_DEPT);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 26. 설계자 필수입력 체크
			 */   
			//담당자 필수 입력
			obj = model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_RESPONSIBILITY_IDX);
			if( obj.toString().trim().equals("")){
				Vector rowVec = new Vector();
				rowVec.add(row);
				rowVec.add(masterListTablePanel.MASTER_LIST_ENG_RESPONSIBILITY_IDX);
				rowVec.add(MSG_INPUT_PERSON_IN_CHARGE);
				validateResult.add(rowVec);
				result = false;
			}

			/**
			 * 27. 설계자 및 설계부서 입력 오류 체크
			 */   
			String sEngResponsibility = "";
			String sEngDeptName = "";

			Object oEngResponsibility = model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_RESPONSIBILITY_IDX);
			Object oEngDeptName = model.getValueAt(row, masterListTablePanel.MASTER_LIST_ENG_DEPT_NM_IDX);

			sEngResponsibility = oEngResponsibility == null ? "" : oEngResponsibility.toString().trim();
			sEngDeptName = oEngDeptName == null ? "" : oEngDeptName.toString().trim();

			if (!sEngResponsibility.equals("") && !sEngDeptName.equals("")) {
				boolean isMatch = false;

				// Vision Net 사용자 정보 내에 입력한 담당팀, 담당자의 사용자가 존재할 경우에만 Pass
				for (int inx = 0; inx < alUserOnVNet.size(); inx++) {
					HashMap<String, Object> hmUserOnVNet = alUserOnVNet.get(inx);

					String sUserName = hmUserOnVNet.get("USER_NAME").toString();
					String sTeamName = hmUserOnVNet.get("TEAM_NAME").toString();

					if (sEngResponsibility.equals(sUserName) && sEngDeptName.equalsIgnoreCase(sTeamName)) {
						isMatch = true;
						break;
					}
				}
				
				if (!isMatch) {
					Vector rowVec = new Vector();
					rowVec.add(row);
					rowVec.add(masterListTablePanel.MASTER_LIST_ENG_DEPT_NM_IDX);
					rowVec.add(MSG_INVALID_PERSON_IN_CHARGE);
					validateResult.add(rowVec);
					result = false;
				}
			}

		}

		return result;
	}

	private String getKey(DefaultTableModel model, int row){
		String key = model.getValueAt(row, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX)
				+ "_" + model.getValueAt(row, MasterListTablePanel.MASTER_LIST_SEQUENCE_IDX);

		return key;
	}

	public static int getParentRowIdx(JTable table, int rowIdx, String parentId){
		DefaultTableModel model = (DefaultTableModel)table.getModel();

		//위로 검색
		for( int i = rowIdx - 1; rowIdx > 0 && i >= 0; i--){
			String partId = model.getValueAt(i, MasterListTablePanel.MASTER_LIST_PART_ID_IDX).toString();
			if( parentId.equals(partId)){
				return i;
			}
		}

		//아래로 검색.
		for( int i = rowIdx + 1; rowIdx < model.getRowCount() && i < model.getRowCount(); i++){
			String partId = model.getValueAt(i, MasterListTablePanel.MASTER_LIST_PART_ID_IDX).toString();
			if( parentId.equals(partId)){
				return i;
			}
		}

		return -1;
	}

	public static String getParentSpec(JTable table, String fmpId, int rowIdx, String parentId, String complexSpec, boolean isAllSpec){

		if( complexSpec == null){
			complexSpec = "";
		}

		Object parentSpecObj = null;
		String parentSpecStr = "";

		DefaultTableModel model = (DefaultTableModel)table.getModel();

		int parentRowIdx = getParentRowIdx(table, rowIdx, parentId);
		if( parentRowIdx > -1){
			parentSpecObj =  model.getValueAt(parentRowIdx, MasterListTablePanel.MASTER_LIST_SPEC_IDX);
			if( parentSpecObj.toString().equals("")){
				if( parentSpecObj instanceof CellValue){
					CellValue specCellValue = (CellValue)parentSpecObj;
					HashMap<String,Object> dataMap = specCellValue.getData();
					if( dataMap != null){
						Object tmpObj = dataMap.get("SPEC");
						if( tmpObj != null){
							parentSpecStr = tmpObj.toString();
						}
					}
				}
			}else{
				parentSpecStr = parentSpecObj.toString();
			}
			if( parentSpecStr != null && !parentSpecStr.equals("")){
				if( complexSpec == null || complexSpec.equals("")){
					complexSpec = "(" + parentSpecStr + ")";
				}else{
					complexSpec += " and (" + parentSpecStr + ")";
				}
			}
			String pParentId = model.getValueAt(parentRowIdx, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX).toString();
			if( isAllSpec && !pParentId.equals("") && !pParentId.equals(fmpId)){
				return getParentSpec(table, fmpId, parentRowIdx, pParentId, complexSpec, isAllSpec);
			}else{
				return complexSpec;
			}
		}

		return complexSpec;
	}

	/**
	 * 대상 BOMLine을 Replace Item Revision 으로 교체한다.
	 * @param targetBOMLine 대상 BOMLine
	 * @param replaceItem Replace 되는 Item
	 * @param replaceItemRevision Replace 되는 ItemRevision
	 * @param viewType BOM View Type
	 * @param replaceOption replace Option
	 * @throws Exception
	 */
	private void replaceBOM(TCComponentBOMLine targetBOMLine , TCComponentItem replaceItem, TCComponentItemRevision replaceItemRevision,TCComponent viewType, int replaceOption) throws Exception
	{
		RestructureService localRestructureService = RestructureService.getService(targetBOMLine.getSession());
		ReplaceItemsParameter localReplaceItemsParameter = new ReplaceItemsParameter();
		localReplaceItemsParameter.bomLine = targetBOMLine;
		localReplaceItemsParameter.itemRevision = replaceItemRevision;
		localReplaceItemsParameter.item = replaceItem;
		localReplaceItemsParameter.viewType = viewType;
		localReplaceItemsParameter.replaceOption = replaceOption;
		ReplaceItemsParameter[] arrayOfReplaceItemsParameter = new ReplaceItemsParameter[1];
		arrayOfReplaceItemsParameter[0] = localReplaceItemsParameter;

		localRestructureService.replaceItems(arrayOfReplaceItemsParameter);

	}
}
