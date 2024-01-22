/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.peif;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.ValidateSDVException;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.exception.SkipException;
import com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob;
import com.symc.plm.me.sdv.service.migration.job.peif.PEIFTCDataExecuteJob;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.ItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivitySubData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EquipmentData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OccurrenceData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SheetDatasetData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SubsidiaryData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ToolData;
import com.symc.plm.me.sdv.service.migration.util.PEExcelConstants;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * Class Name : PEValidation
 * Class Description :
 * 
 * 
 * 공법하위 할당 FindNo 규칙 :
 * 1 ~ 9 : 일반자재(END-ITEM)
 * 10 ~ 200 : 공구(TOOL)
 * 210 ~ 500 : 설비(EQUIPMENT)
 * 510 ~ 800 : 부자재(SUBSIDIARY)
 * 
 * @date 2013. 11. 25.
 * 
 */
public class PEValidation extends PEDataWork {

    /**
     * @param shell
     * @param tcDataMigrationJob
     * @param processLine
     * @param mecoNo
     * @param isOverride
     */
    public PEValidation(Shell shell, TCDataMigrationJob tcDataMigrationJob, TCComponentMfgBvrProcess processLine, String mecoNo, boolean isOverride) {
        super(shell, tcDataMigrationJob, processLine, mecoNo, isOverride);
    }

    public void rowValidate(final int index, final TCData tcData) throws Exception {
    	
        final ArrayList<Exception> exception = new ArrayList<Exception>();
        
        Runnable aValidationRun = new Runnable() {
            public void run() {
                String message = "";
                String logMessage = "";
                String differLog = "";
                try {
                    // Progress 메세지
                    tcData.setStatus(TCData.STATUS_INPROGRESS);
                    getTcDataMigrationJob().getTree().setSelection(tcData);
                    // LINE validate
                    if (tcData instanceof LineItemData) {
                        validateLine(processLine, (LineItemData) tcData);
                    } else
                    // OPERATION Validate
                    if (tcData instanceof OperationItemData) {
                        // check Operation
                        differLog = printDifferenceResult(tcData, validateOperation((OperationItemData) tcData));
                    }
                    // Activity 처리
                    else if (tcData instanceof ActivityMasterData) {
                        // check Activity
                        differLog = validateActivity((ActivityMasterData) tcData);
                    }
                    // Resource - EquipmentData 처리
                    else if (tcData instanceof EquipmentData) {
                        // check EquipmentData
                        differLog = printDifferenceResult(tcData, validateEquipmentData((EquipmentData) tcData));
                    }
                    // Resource - ToolData 처리
                    else if (tcData instanceof ToolData) {
                        // check ToolData
                        differLog = printDifferenceResult(tcData, validateToolData((ToolData) tcData));
                    }
                    // Resource - EndItemData 처리
                    else if (tcData instanceof EndItemData) {
                        // check EndItemData
                        differLog = printDifferenceResult(tcData, validateEndItemData((EndItemData) tcData));
                    }
                    // Resource - SubsidiaryData 처리
                    else if (tcData instanceof SubsidiaryData) {
                        // check SubsidiaryData
                        differLog = printDifferenceResult(tcData, validateSubsidiaryData((SubsidiaryData) tcData));
                    }
                    // 작업표준서 DatasetData 처리
                    else if (tcData instanceof SheetDatasetData) {
                        // SheetDatasetData
                        differLog = printDifferenceResult(tcData, validateSheetDatasetData((SheetDatasetData) tcData));
                    }
                } catch (Exception e) {
                	if((e instanceof SkipException)==false){
                		e.printStackTrace();
                	}
                    exception.add(e);
                } finally {
                    // Error 메세지 처리
                    if (exception.size() > 0) {
                        message = exception.get(0).getMessage();
                        // Skip Exception 처리
                        if (exception.get(0) instanceof SkipException) {
                            SkipException skipException = (SkipException) exception.get(0);
                            tcData.setStatus(skipException.getStatus(), message);
                        } else {
                            tcData.setStatus(TCData.STATUS_ERROR, message);
                        }
                        logMessage = "{" + tcData.getText() + "} : " + tcData.getStatusMessage();
                    }
                    // 메세지 처리
                    else {
                        logMessage = processingMessage(tcData);
                        if (!StringUtils.isEmpty(differLog)) {
                            logMessage += ("\n" + differLog);
                        }
                    }
                    // Log 처리
                    saveLog(tcData, exception, logMessage);
                }
            }

            /**
             * CLASS Type별 검증처리(Status 변경), 메세지 처리
             * 
             * @method processingMessage
             * @date 2013. 11. 27.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            private String processingMessage(final TCData tcData) {
                String message;
                String logMessage;
                int rowStatus = TCData.STATUS_VALIDATE_COMPLETED;
                message = COMPLETED_MESSAGE;
                // 공법(Operation) DataType 처리
                if (tcData instanceof OperationItemData) {
                    // Operation 생성일 경우 메세지 처리
                    if (!((OperationItemData) tcData).isExistItem()) {
                        message = "Createable Operation";
                    }
                }
                // Activity DataType 처리
                else if (tcData instanceof ActivityMasterData) {
                    if (((ActivityMasterData) tcData).isCreateable()) {
                        message = "Activity가 변경되어 재등록 대상입니다.";
                        // //상위 공법이 VALIDATE_COMPLETED 상태이면 STATUS_WARNING로 변경한다.
                        if (TCData.STATUS_VALIDATE_COMPLETED == ((TCData) tcData.getParentItem()).getStatus()) {
                            ((TCData) tcData.getParentItem()).setStatus(TCData.STATUS_WARNING, message);
                        }
                    } else {
                        message = "Activity 변경 사항이 없어 Skip 처리합니다.";
                        rowStatus = TCData.STATUS_SKIP;
                    }
                }
                // Activity DataType 처리
                else if (tcData instanceof ActivitySubData) {
                    // Master가 수정이면 하위 Sub도 수정 대상
                    if (((ActivitySubData) tcData).isCreateable()) {
                        message = "신규 또는 변경항목";
                    } else {
                        rowStatus = TCData.STATUS_SKIP;
                    }
                }
                // OccurrenceData - EquipmentData, ToolData, EndItemData, SubsidiaryData 처리
                else if (tcData instanceof OccurrenceData) {
                    if (((OccurrenceData) tcData).getResourceItem() == null) {
                        message = "Resource Item이 존재하지않아 Master 정보 등록 후 처리합니다.";
                    }
                    if (((OccurrenceData) tcData).getBopBomLine() != null) {
                        // BOM 업데이트가 없으면 Skip
                        if (!((OccurrenceData) tcData).isBOMLineModifiable()) {
                            rowStatus = TCData.STATUS_SKIP;
                        }
                    }
                    // Resource - EquipmentData 처리
                    if (tcData instanceof EquipmentData) {

                    }
                    // Resource - ToolData 처리
                    if (tcData instanceof ToolData) {

                    }
                    // Resource - EndItemData 처리
                    if (tcData instanceof EndItemData) {

                    }
                    // Resource - SubsidiaryData 처리
                    if (tcData instanceof SubsidiaryData) {

                    }
                }
                tcData.setStatus(rowStatus, message);
                logMessage = "{" + tcData.getText() + "} : " + tcData.getStatusMessage();
                return logMessage;
            }
        };

        // [NON-SR][2016.01.07] taeku.jeong PE->TC Interface 수행중 Down되는 문제의 해결을 위해 Thread를 명시적으로 소멸 할 수 있는방법으로 변경함 
//        Thread aValidationThread = new Thread(aValidationRun);
        shell.getDisplay().syncExec(aValidationRun);
//        aValidationThread.stop();
//        aValidationThread = null;
        
        if (exception.size() > 0) {
            // SKIP Exception 이면 에러를 Throw하지 않는다.
            if (!(exception.get(0) instanceof SkipException)) {
                throw exception.get(0);
            }
        }
    }

    /**
     * 공법(Operation)하위 Child Item을 검색한다.
     * 
     * @method findOperationUnderData
     * @date 2013. 11. 26.
     * @param
     * @return ArrayList<TCComponentItem>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<TCComponentBOMLine> findOperationUnderDataItem(OccurrenceData occurrenceData) throws Exception {
        ArrayList<TCComponentBOMLine> findBOMLineList = new ArrayList<TCComponentBOMLine>();
        TCComponentBOMLine[] childBOMLine = ((OperationItemData) occurrenceData.getParentItem()).getOperationChildComponent();
        for (int i = 0; i < childBOMLine.length; i++) {
            String tcItemId = childBOMLine[i].getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            if (occurrenceData.getItemId().equals(tcItemId)) {
                findBOMLineList.add(childBOMLine[i]);
            }
        }
        return findBOMLineList;
    }

    /**
     * 공법 Activity 유효성 비교
     * 
     * m7_CONTROL_BASIS=체결
     * m7_ENG_NAME=,
     * time_system_category=03,
     * m7_WORK_OVERLAP_TYPE=,
     * time_system_frequency=1.0,
     * m7_CONTROL_POINT=체결,
     * object_name=결합,
     * SEQ=60,
     * time_system_unit_time=10.0,
     * time_system_code=Test 03
     * 
     * @method validateActivity
     * @date 2013. 11. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private String validateActivity(ActivityMasterData activityMasterData) throws Exception {
        StringBuffer log = new StringBuffer();
        boolean creteable = false;
        // ActivityMasterData의 Parent에서 TC에서 조회한 Activity List를 가져온다.
        List<HashMap<String, Object>> activityList = ((OperationItemData) activityMasterData.getParentItem()).getOperationActivityList();
        TreeItem[] activitySubDatas = activityMasterData.getItems();
        for (int i = 0; i < activitySubDatas.length; i++) {
            // TC-PE 간 Activity 속성이 틀리면 재등록
            HashMap<String, Object> tcActivityData = findActivity(activityList, (ActivitySubData) activitySubDatas[i]);
            if (tcActivityData == null) {
                // ActivityMasterData 변경 사항 등록 - Activity 항목이 1개라도 변경되면 전체 재등록
                creteable = setActivityModify(creteable, (ActivitySubData) activitySubDatas[i]);
            } else {
                ArrayList<String> activitySubRowData = (ArrayList<String>) ((ActivitySubData) activitySubDatas[i]).getData();
                // 변경 사항 Log
                LinkedHashMap<String, String[]> differenceResult = validateActivitySubAttributes(tcActivityData, activitySubRowData);
                if (differenceResult.size() > 0) {
                    // ActivityMasterData 변경 사항 등록 - Activity 항목이 1개라도 변경되면 전체 재등록
                    creteable = setActivityModify(creteable, (ActivitySubData) activitySubDatas[i]);
                    log.append(printDifferenceResult((TCData) activitySubDatas[i], differenceResult));
                } else {
                    ((ActivitySubData) activitySubDatas[i]).setCreateable(false);
                }
                // 만약 같은 Activity이면 영문명을 TC에서 가져와 PE에 다시 셋팅한다. (PE I/F 영문명이 null일 경우 설정, 영문명이 존재할 경우 PE 영문명을 사용)
                // 이유는 PE I/F에는 거의 영문명이 없기 때문에 다시 업데이트 시 영문명이 사라지는 문제가 발생하는 이유 임.
                // if (StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_ENG_NAME_COLUMN_INDEX))) {
                // Activity 국문명이 변경 사항이 없으면 (differenceResult의 SDVPropertyConstant.ACTIVITY_OBJECT_NAME(Activity 국문명) 비교 결과가 'null' -> 국문명 변경이 없으므로 영문명을 그대로 사용)
                // 기존 TC의 영문명을 그대로 사용한다.
                if (!differenceResult.containsKey(SDVPropertyConstant.ACTIVITY_OBJECT_NAME)) {
                    activitySubRowData.set(PEExcelConstants.ACTIVITY_MASTER_ENG_NAME_COLUMN_INDEX, BundleUtil.nullToString((String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_ENG_NAME)));
                }
            }
        }
        // Activity를 재등록 여부
        activityMasterData.setCreateable(creteable);
        // TC Activity 수와 PE I/F Activity수가 같지않으면 무조건 재등록
        if (activitySubDatas.length != activityList.size()) {
            log.append("Activity의 수가 TC-PE 간에 일치하지 않아 전체 재등록합니다. \n");
            activityMasterData.setCreateable(true);
        }
        return log.toString();
    }

    /**
     * ActivityMasterData 변경 사항 등록 - Activity 항목이 1개라도 변경되면 전체 재등록
     * 
     * @method setActivityModify
     * @date 2013. 12. 11.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean setActivityModify(boolean creteable, ActivitySubData activitySubData) {
        // ActivityMasterData 변경 사항 등록 - Activity 항목이 1개라도 변경되면 전체 재등록
        if (creteable == false) {
            creteable = true;
        }
        // ActivitySubData 변경 사항 등록
        activitySubData.setCreateable(true);
        return creteable;
    }

    /**
     * TC Activity List 에서 PE에서 가져온 Activity 검색한다.
     * 
     * @method findActivity
     * @date 2013. 11. 27.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private HashMap<String, Object> findActivity(List<HashMap<String, Object>> activityList, ActivitySubData activitySubData) throws Exception {
        for (int i = 0; i < activityList.size(); i++) {
            // SEQ=60 --> TC-PE시퀀스 검증
            ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
            String seq = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_SEQ_COLUMN_INDEX);
            if (seq.equals(activityList.get(i).get("SEQ"))) {
                return activityList.get(i);
            }
        }
        return null;
    }

    /**
     * Activity 작업순서별 속성 비교를 한다.
     * 
     * @method validateActivitySubAttributes
     * @date 2013. 12. 10.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateActivitySubAttributes(HashMap<String, Object> tcActivityData, ArrayList<String> activitySubRowData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        String tcActivityKorName = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_OBJECT_NAME);
        String peActivityKorName = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KOR_NAME_COLUMN_INDEX);
        String[] defferActivityKorName = getDefferenceData(tcActivityKorName, peActivityKorName);
        if (defferActivityKorName != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_OBJECT_NAME, defferActivityKorName);
        }
        String tcCategory = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
        String peCategory = ImportCoreService.getPEActivityCategolyLOV(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_CATEGORY_COLUMN_INDEX)); // LOV value로 변환
        // PE LOV를 TC String으로 재변환(정미(PE) -> 01 -> 작업자정미(TC))
        peCategory = ImportCoreService.getActivityCategolyLOVToString(peCategory);
        String[] defferCategory = getDefferenceData(tcCategory, peCategory);
        if (defferCategory != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY, defferCategory);
        }
        String tcWorkCode = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
        String peWorkCode = (String) activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_MAIN_COLUMN_INDEX);
        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX))) {
            peWorkCode = peWorkCode + "-" + activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX).replace(",", "-");
        }
        String[] defferWorkCode = getDefferenceData(tcWorkCode, peWorkCode);
        if (defferWorkCode != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE, defferWorkCode);
        }
        double tcTimeSystemUnitTime = (Double) tcActivityData.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
        double peTimeSystemUnitTime = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX));
        String[] defferTimeSystemUnitTime = getDefferenceData(tcTimeSystemUnitTime, peTimeSystemUnitTime);
        if (defferTimeSystemUnitTime != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME, defferTimeSystemUnitTime);
        }
        double tcTimeSystemFrequency = (Double) tcActivityData.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
        double peTimeSystemFrequency = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX));
        String[] defferTimeSystemFrequency = getDefferenceData(tcTimeSystemFrequency, peTimeSystemFrequency);
        if (defferTimeSystemFrequency != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY, defferTimeSystemFrequency);
        }
        String tcControlPoint = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
        String peControlPoint = (String) activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_COLUMN_INDEX);
        String[] defferControlPoint = getDefferenceData(tcControlPoint, peControlPoint);
        if (defferControlPoint != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_CONTROL_POINT, defferControlPoint);
        }
        String tcControlBasis = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS);
        String peControlBasis = (String) activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_BASIS_COLUMN_INDEX);
        String[] defferControlBasis = getDefferenceData(tcControlBasis, peControlBasis);
        if (defferControlBasis != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS, defferControlBasis);
        }
        // checkActivityTool
        checkActivityTool(tcActivityData, activitySubRowData, differenceResult);
        return differenceResult;
    }

    /**
     * checkActivityTool
     * 
     * @method checkActivityTool
     * @date 2013. 12. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void checkActivityTool(HashMap<String, Object> tcActivityData, ArrayList<String> activitySubRowData, LinkedHashMap<String, String[]> differenceResult) throws TCException {
        String peToolIds = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TOOL_ID_COLUMN_INDEX);
        peToolIds = BundleUtil.nullToString(peToolIds);
        String[] peToolIdList = peToolIds.split(",");
        // split 후 [0]데이터가 empty("") 이면 String[0] 배열 처리
        if (peToolIdList != null && peToolIdList.length == 1) {
            if ("".equals(BundleUtil.nullToString(peToolIdList[0]).trim())) {
                peToolIdList = new String[0];
            }
        }
        TCComponentBOMLine[] tcToolList = (TCComponentBOMLine[]) tcActivityData.get(SDVPropertyConstant.ACTIVITY_TOOL_LIST);
        if (tcToolList == null) {
            tcToolList = new TCComponentBOMLine[0];
        }
        String tcToolIds = "";
        for (int i = 0; i < tcToolList.length; i++) {
            String itemId = tcToolList[i].getProperty(SDVPropertyConstant.BL_ITEM_ID);
            tcToolIds += (i == 0) ? itemId : "," + itemId;
        }
        if (peToolIdList.length != tcToolList.length) {
            differenceResult.put("Activity Tool mismatch", new String[] { tcToolIds, peToolIds });
        } else {
            HashMap<String, TCComponentBOMLine> peMappingList = new HashMap<String, TCComponentBOMLine>();
            for (int i = 0; i < peToolIdList.length; i++) {
                String peToolItemId = peToolIdList[i];
                peMappingList.put(peToolItemId, null);
                for (int j = 0; j < tcToolList.length; j++) {
                    String tcToolItemId = tcToolList[j].getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    if (peToolItemId.equals(tcToolItemId)) {
                        peMappingList.put(peToolItemId, tcToolList[j]);
                        break;
                    }
                }
            }
            if (peMappingList.containsValue(null)) {
                differenceResult.put("Activity Tool mismatch", new String[] { tcToolIds, peToolIds });
            }
        }
    }

    /**
     * Validate Line
     * 
     * @method validateLine
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void validateLine(TCComponentMfgBvrProcess processLine, LineItemData lineItemData) throws TCException, Exception, ValidateSDVException {
        String tcLineItemId = processLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String dataLineItemId = lineItemData.getItemId();
        // TC에서 선택한 Line정보가 Import 대상인 Line정보와 맞는지 검증한다.
        if (!tcLineItemId.equals(dataLineItemId)) {
            throw new ValidateSDVException("Line정보가 맞지않습니다. (TC = '" + tcLineItemId + "' / IMP_DATE = '" + dataLineItemId + "')");
        }
        // 쓰기, Released 체크
        checkPermissions(lineItemData);
    }

    /**
     * Validate Operation
     * 
     * @method validateOperation
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> validateOperation(OperationItemData operationItemData) throws Exception {
        // BOP Line하위 공법에는 존재하지 않으나 다른 Line하위에 존재하는 경우
        if (operationItemData.getBopBomLine() == null && operationItemData.isExistItem() == true) {
            operationPaste(operationItemData);
        }
        if (!isOverride) {
            // 초기생성이 아닌 경우 쓰기권한체크 Release
            checkPermissions(operationItemData);
        }
        // TC에 등록 할 DataSet 생성 - 업데이트, Revise용 데이터셋
        operationItemData.setData(PEExcelConstants.DATASET, generateOperationCreateDataset(operationItemData));
        // 공법 옵션컨디션 TC에 맞게 컨버젼
        String condition = ((ArrayList<String>) operationItemData.getData(PEExcelConstants.BOM)).get(PEExcelConstants.OPERATION_BOM_OPTION_COLUMN_INDEX);
        operationItemData.setConversionOptionCondition(getConversionOptionCondition(condition));
        // TC 공법 Master(Item, ItemRevision) 정보와 PE I/F 공법 Master 정보와 비교하여 업데이트 대상인지 확인한다.
        LinkedHashMap<String, String[]> differenceResult = compareOperationData(operationItemData);
        return differenceResult;
    }

    /**
     * BOP Line하위 공법에는 존재하지 않으나 다른 Line하위에 존재하는 경우
     * 
     * @method operationPaste
     * @date 2014. 1. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void operationPaste(OperationItemData operationItemData) throws Exception {
        // throw new ValidateSDVException(operationItemData.getItemId() + " - 공법(Operation)이 다른 Line에 존재합니다. 해당 Line에 작업 후 다시 진행해 주세요.");

        // Line에 OperationItem BOMLine Paste
        TCComponentBOMLine operationBOMLine = processLine.add(null, SYMTcUtil.getLatestedRevItem(operationItemData.getItemId()), null, false);
        operationItemData.setBopBomLine(operationBOMLine);
        // 로그 출력
        saveLog(operationItemData, operationItemData.getItemId() + " - Line에 하위에 없는 공법(Operation) Item이 존재합니다. 해당 Line에 붙여넣기(Paste) 하였습니다.", true);
        // 미할당 라인 아래에 공법이 존재하면 cut
        TCComponentBOMLine shopBOMLine = processLine.parent();
        TCComponentBOMLine[] lineBOMLines = SDVBOPUtilities.getUnpackChildrenBOMLine(shopBOMLine);
        String tempBOPLineId = getTempBOPLineId(shopBOMLine);
        if (StringUtils.isEmpty(tempBOPLineId)) {
            throw new ValidateSDVException("SHOP에서 미할당 Line을 찾을 수 없습니다.");
        }
        // 미할당 라인 검색
        for (TCComponentBOMLine lineBOMLine : lineBOMLines) {
            if (tempBOPLineId.equals(lineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                TCComponentBOMLine[] tempOperationBOMLines = SDVBOPUtilities.getUnpackChildrenBOMLine(lineBOMLine);
                ArrayList<TCComponentBOMLine> removeList = new ArrayList<TCComponentBOMLine>();
                String cutTempOperationMsg = "";
                // Paste된 미할당 공법 검색
                for (TCComponentBOMLine tempOperationBOMLine : tempOperationBOMLines) {
                    // PE Operation Item ID가 미할당 라인에 존재하면 cut.
                    if (operationItemData.getItemId().equals(tempOperationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                        removeList.add(tempOperationBOMLine);
                        cutTempOperationMsg = tempOperationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    }
                }
                // 미할당 라인에서 해당 공법을 Cut
                if (removeList.size() > 0) {
                    SDVBOPUtilities.disconnectObjects(lineBOMLine, removeList);
                    saveLog(operationItemData, cutTempOperationMsg + " - 미할당 Line에 하위에 있는 공법을 Cut 하였습니다.", true);
                }
            }
        }
    }

    /**
     * SHOP BOMLine으로 미할당 Line 검색
     * 
     * @method getTempBOPLineId
     * @date 2014. 1. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getTempBOPLineId(TCComponentBOMLine topBOMLine) throws Exception {
        String tempLineId = "";
        String shopId = topBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] idSplit = shopId.split("-");
        // 미할당 LINE ID
        tempLineId = idSplit[0] + "-" + idSplit[1] + "-TEMP-" + idSplit[2];
        return tempLineId;
    }

    /**
     * Create / Update Operation Dataset
     * 
     * @method generateOperationCreateDataset
     * @date 2013. 12. 10.
     * @param
     * @return IDataSet
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private IDataSet generateOperationCreateDataset(OperationItemData operationItemData) throws Exception {
        // (34) - (1D) - (862-015R) - (00)
        IDataSet dataSet = new DataSet();
        IDataMap opInformDataMap = new RawDataMap();
        dataSet.addDataMap("opInform", opInformDataMap);
        ArrayList<String> masterData = (ArrayList<String>) operationItemData.getData(PEExcelConstants.MASTER);
        ArrayList<String> bomData = (ArrayList<String>) operationItemData.getData(PEExcelConstants.BOM);
        if (masterData == null) {
            throw new ValidateSDVException("공법(Operation) Master 정보가 없습니다.");
        }
        // [, 34, 1D, 862-015R, 00, 10R, 쿼터 인너판넬에 리어 씨트벨트 리트렉터 장착2,우, , T1-240, , , null, null, null, null, null]
        // 공법정보
        // MASTER
        String vechicleCode = masterData.get(PEExcelConstants.COMMON_MASTER_PROJECT_NO_COLUMN_INDEX);
        String lineCode = masterData.get(PEExcelConstants.COMMON_MASTER_SHOP_LINE_COLUMN_INDEX);
        String sheetNo = masterData.get(PEExcelConstants.COMMON_MASTER_SHEET_NO_COLUMN_INDEX);
        String functionCode = sheetNo.split("-")[0];
        String opCode = sheetNo.split("-")[1];
        String bopVersion = masterData.get(PEExcelConstants.COMMON_MASTER_PLANNING_VERSION_COLUMN_INDEX);
        // BOM
        String productNo = bomData.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX);
        // Item Id 조합
        // String itemId = vechicleCode + "-" + lineCode + "-" + functionCode + "-" + opCode + "-" + bopVersion;
        String korName = masterData.get(PEExcelConstants.OPERATION_MASTER_KOR_NAME_COLUMN_INDEX);
        String opEngName = masterData.get(PEExcelConstants.OPERATION_MASTER_ENG_NAME_COLUMN_INDEX);
        String workerCode = masterData.get(PEExcelConstants.OPERATION_MASTER_WORKER_CODE_COLUMN_INDEX);// 작업자구분코드
        String processSeq = masterData.get(PEExcelConstants.OPERATION_MASTER_PROCESS_SEQ_COLUMN_INDEX);// Sequence No.
        String workArea = masterData.get(PEExcelConstants.OPERATION_MASTER_WORK_AREA_COLUMN_INDEX);
        String itemUL = masterData.get(PEExcelConstants.OPERATION_MASTER_ITEM_UL_COLUMN_INDEX); // 자재투입위치
        String stationNo = masterData.get(PEExcelConstants.OPERATION_MASTER_STATION_NO_COLUMN_INDEX);// Station No.
        String dr = masterData.get(PEExcelConstants.OPERATION_MASTER_DR_COLUMN_INDEX); // 보안
        String assySystem = masterData.get(PEExcelConstants.OPERATION_MASTER_ASSEMBLY_SYSTEM_COLUMN_INDEX);// 조립시스템
        String workUbody = masterData.get(PEExcelConstants.OPERATION_MASTER_WORK_UBODY_COLUMN_INDEX); // 상하구분
        String vehicleCheckStr = masterData.get(PEExcelConstants.OPERATION_MASTER_REP_VHICLE_CHECK_COLUMN_INDEX); // 대표차종 유무 (N/Y)
        /*
         *  특별특성 속성 추가 
         */
        String specialCharic = masterData.get(PEExcelConstants.OPERATION_MASTER_SHEET_SPECIAL_CHARICTORISTIC);
        
        boolean vehicleCheck = ("Y".equals(vehicleCheckStr)) ? true : false;
        boolean maxWorkTimeCheck = false;
        String dwgNo = masterData.get(PEExcelConstants.OPERATION_MASTER_DWG_NO_COLUMN_INDEX);
        String[] dwgNoArray = dwgNo.split("/");
        ArrayList<String> dwgNoList = new ArrayList<String>();
        if (dwgNoArray != null && dwgNoArray.length > 0) {
            for (String dwg : dwgNoArray) {
                dwg = BundleUtil.nullToString(dwg).trim();
                if (!"".equals(dwg)) {
                    dwgNoList.add(dwgNo);
                }
            }
        }
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vechicleCode);// 차종
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP, lineCode);// 라인
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, functionCode); //
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);// 공법 코드
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);// 공법 리비전
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, korName);// 공법 이름 (한글)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opEngName); // 공법 이름 (영문)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, workerCode);// 작업자 구분코드
        opInformDataMap.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, processSeq); // Sequence No.
        opInformDataMap.put(SDVPropertyConstant.OPERATION_WORKAREA, workArea); // 작업위치
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, stationNo); // Station No.
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_DR, dr); // 보안
        opInformDataMap.put(SDVPropertyConstant.OPERATION_WORK_UBODY, workUbody); // WORK_UBODY 하체작업여부(N/Y)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_ITEM_UL, itemUL); // 자재투입위치
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, dwgNoList, IData.LIST_FIELD); // 장착도면번호 리스트
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, assySystem); // 조립시스템
        opInformDataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, maxWorkTimeCheck, IData.BOOLEAN_FIELD);
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, vehicleCheck, IData.BOOLEAN_FIELD); // 대표차종 유무(N/Y)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productNo); // Product No.
        /*
         * 특별 특성 속성 추가
         */
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialCharic);// 특별 특성 
        
        // MECO 설정
        IDataMap mecoSelectDataMap = new RawDataMap();
        dataSet.addDataMap("mecoSelect", mecoSelectDataMap);
        mecoSelectDataMap.put("mecoNo", mecoNo);
        mecoSelectDataMap.put("mecoRev", ImportCoreService.getMecoRevision(mecoNo), IData.OBJECT_FIELD);

        return dataSet;
    }

    /**
     * TC 공법 Master(Item, ItemRevision) 정보와 PE I/F 공법 Master, BOMLine 정보와 비교하여 업데이트 대상인지 확인한다.
     * 
     * @method compareOperationData
     * @date 2013. 12. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> compareOperationData(OperationItemData operationItemData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        // 초기생성은 무조건 Master정보 Create
        if (operationItemData.getBopBomLine() == null) {
            operationItemData.setMasterModifiable(true);
            return differenceResult;
        }
        // Update 대상 비교 - PEExecution.updateOperationItem 업데이트 dataset 기준 비교
        // Excel 공법 등록 대상 Dataset 가져오기
        IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
        IDataMap opInformDataMap = dataSet.getDataMap("opInform");
        // 비교대상 TC Operation Item
        TCComponentItem item = operationItemData.getBopBomLine().getItem();

        String tcOperationWorkerCode = BundleUtil.nullToString(item.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE));
        String peOperationWorkerCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKER_CODE).getStringValue());
        String[] defferOperationWorkerCode = getDefferenceData(tcOperationWorkerCode, peOperationWorkerCode);
        if (defferOperationWorkerCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_WORKER_CODE, defferOperationWorkerCode);
        }

        String tcOperationProcessSeq = BundleUtil.nullToString(item.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ));
        String peOperationProcessSeq = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_PROCESS_SEQ).getStringValue());
        String[] defferOperationProcessSeq = getDefferenceData(tcOperationProcessSeq, peOperationProcessSeq);
        if (defferOperationProcessSeq != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, defferOperationProcessSeq);
        }

        String tcOperationWorkArea = BundleUtil.nullToString(item.getProperty(SDVPropertyConstant.OPERATION_WORKAREA));
        String peOperationWorkArea = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKAREA).getStringValue());
        String[] defferOperationWorkArea = getDefferenceData(tcOperationWorkArea, peOperationWorkArea);
        if (defferOperationWorkArea != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_WORKAREA, defferOperationWorkArea);
        }

        // 비교대상 TC Operation ItemRevision
        TCComponentItemRevision itemRevision = operationItemData.getBopBomLine().getItemRevision();

        String tcOperationKorName = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
        String peOperationKorName = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME).getStringValue());
        String[] defferOperationKorName = getDefferenceData(tcOperationKorName, peOperationKorName);
        if (defferOperationKorName != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, defferOperationKorName);
        }

        String tcOperationEngName = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));
        String peOperationEngName = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_ENG_NAME).getStringValue());
        // PE에 영문명이 null이면 업데이트 처리하지 않는다.
        if (!StringUtils.isEmpty(peOperationEngName)) {
            String[] defferOperationEngName = getDefferenceData(tcOperationEngName, peOperationEngName);
            if (defferOperationEngName != null) {
                differenceResult.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, defferOperationEngName);
            }
        }

        String tcOperationrevVehicleCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE));
        String peOperationrevVehicleCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE).getStringValue());
        String[] defferOperationrevVehicleCode = getDefferenceData(tcOperationrevVehicleCode, peOperationrevVehicleCode);
        if (defferOperationrevVehicleCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, defferOperationrevVehicleCode);
        }

        String tcOperationRevShop = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SHOP));
        String peOperationRevShop = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_SHOP).getStringValue());
        String[] defferOperationRevShop = getDefferenceData(tcOperationRevShop, peOperationRevShop);
        if (defferOperationRevShop != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_SHOP, defferOperationRevShop);
        }

        String tcOperationRevFunctionCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE));
        String peOperationRevFunctionCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE).getStringValue());
        String[] defferOperationRevFunctionCode = getDefferenceData(tcOperationRevFunctionCode, peOperationRevFunctionCode);
        if (defferOperationRevFunctionCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, defferOperationRevFunctionCode);
        }

        String tcOperationRevOperationCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE));
        String peOperationRevOperationCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE).getStringValue());
        String[] defferOperationRevOperationCode = getDefferenceData(tcOperationRevOperationCode, peOperationRevOperationCode);
        if (defferOperationRevOperationCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, defferOperationRevOperationCode);
        }

        itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_BOP_VERSION).getStringValue();
        String tcOperationRevBopVersion = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION));
        String peOperationRevBopVersion = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_BOP_VERSION).getStringValue());
        String[] defferOperationRevBopVersion = getDefferenceData(tcOperationRevBopVersion, peOperationRevBopVersion);
        if (defferOperationRevBopVersion != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, defferOperationRevBopVersion);
        }

        itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
        opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue();
        String tcOperationRevStationNo = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
        String peOperationRevStationNo = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue());
        String[] defferOperationRevStationNo = getDefferenceData(tcOperationRevStationNo, peOperationRevStationNo);
        if (defferOperationRevStationNo != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, defferOperationRevStationNo);
        }

        String tcOperationRevProductCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE));
        String peOperationRevProductCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE).getStringValue());
        String[] defferOperationRevProductCode = getDefferenceData(tcOperationRevProductCode, peOperationRevProductCode);
        if (defferOperationRevProductCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, defferOperationRevProductCode);
        }
        /*
         * 특별 특성 속성 추가
         */
        String tcOperationRevSpecialCharic = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC));
        String peOperationRevSpecialCharic = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC).getStringValue());
        String[] defferOperationRevSpecialCharic = getDefferenceData(tcOperationRevSpecialCharic, peOperationRevSpecialCharic);
        if (defferOperationRevSpecialCharic != null) {
        	differenceResult.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, defferOperationRevSpecialCharic);
        }
        
        // OPERATION_REV_INSTALL_DRW_NO 는 TC에서 배열 타입으로 존재, PE에서는 단건으로 존재하므로 1건으로 TC에서 추출하여 PE와 비교한다.
        String tcOperationRevInstallDwgNo[] = null;
        if (itemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO) != null) {
            tcOperationRevInstallDwgNo = itemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getStringValueArray();
        }
        if (tcOperationRevInstallDwgNo == null) {
            tcOperationRevInstallDwgNo = new String[0];
        }
        String[] peOperationRevInstallDwgNo = null;
        if (opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getValue() != null) {
            ArrayList<String> dwgList = (ArrayList<String>) opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getValue();
            if (dwgList != null && dwgList.size() > 0) {
                peOperationRevInstallDwgNo = dwgList.toArray(new String[dwgList.size()]);
            }
        }
        if (peOperationRevInstallDwgNo == null) {
            peOperationRevInstallDwgNo = new String[0];
        }
        String[] defferOperationRevInstallDwgNo = getDefferenceData(tcOperationRevInstallDwgNo, peOperationRevInstallDwgNo);
        if (defferOperationRevInstallDwgNo != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, defferOperationRevInstallDwgNo);
        }

        // MASTER 변경 정보가 있으므로 업데이트
        if (differenceResult.size() > 0) {
            operationItemData.setMasterModifiable(true);
        }
        // 비교대상 TC Operation BOMLine
        // Option Condition
        String tcOpertationBOMLineMVLCondition = BundleUtil.nullToString(operationItemData.getBopBomLine().getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).trim();
        String peOpertationBOMLineMVLCondition = BundleUtil.nullToString(operationItemData.getConversionOptionCondition()).trim();
        String[] defferBOMLineMVLCondition = getDefferenceData(tcOpertationBOMLineMVLCondition, peOpertationBOMLineMVLCondition);
        if (defferBOMLineMVLCondition != null) {
            differenceResult.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, defferBOMLineMVLCondition);
            operationItemData.setBOMLineModifiable(true);
        }
        // Find No. 비교
        // 공정편성번호 입력
        String tcOpertationSeqNo = BundleUtil.nullToString(operationItemData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO)).trim();
        String peOpertationSeqNo = "";
        String stationNo = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue()).replace("-", "");// 공정번호
        String workerCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKER_CODE).getStringValue()).replace("-", "");// 작업자코드
        String seq = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_PROCESS_SEQ).getStringValue()); // Process Seq
        boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty() || seq.isEmpty(); // 하나라도 값이 없으면 반영안함
        String findNo = stationNo.concat("|").concat(workerCode).concat("|").concat(seq);
        if (!(findNo.length() > 15 || isExistEmptyValue)) {
            peOpertationSeqNo = findNo;
            opInformDataMap.put(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
        }
        String[] defferBOMLineOpertationSeqNo = getDefferenceData(tcOpertationSeqNo, peOpertationSeqNo);
        if (defferBOMLineOpertationSeqNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferBOMLineOpertationSeqNo);
            operationItemData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * 데이터 비교
     * 
     * 주의 : double인 경우 소숫점 10자리 이하 절삭
     * 
     * @method getDefferenceData
     * @date 2013. 12. 11.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    private String[] getDefferenceData(Object tcData, Object peData) {
        // double인 경우 소숫점 10자리 이하 절삭
        if (tcData instanceof Double) {
            tcData = (new Double(Double.parseDouble(longDouble2String(10, (Double) tcData)))).toString();
            peData = (new Double(Double.parseDouble(longDouble2String(10, (Double) peData)))).toString();
            if (!(tcData.equals(peData))) {
                String[] defferData = new String[2];
                defferData[0] = tcData.toString();
                defferData[1] = peData.toString();
                return defferData;
            }
        } else if (tcData instanceof String[]) {
            String[] tcStrings = (String[]) tcData;
            String[] peStrings = (String[]) peData;
            if (tcStrings.length != peStrings.length) {
                String[] defferData = new String[2];
                defferData[0] = Arrays.toString(tcStrings);
                defferData[1] = Arrays.toString(peStrings);
                return defferData;
            } else {
                for (int i = 0; i < tcStrings.length; i++) {
                    String tcValue = BundleUtil.nullToString(tcStrings[i]);
                    boolean isSameData = false;
                    for (int j = 0; j < peStrings.length; j++) {
                        String peValue = BundleUtil.nullToString(peStrings[j]);
                        if (tcValue.equals(peValue)) {
                            isSameData = true;
                            break;
                        }
                    }
                    if (isSameData == false) {
                        String[] defferData = new String[2];
                        defferData[0] = Arrays.toString(tcStrings);
                        defferData[1] = Arrays.toString(peStrings);
                        return defferData;
                    }
                }
                return null;
            }
        } else {
            // null 초기화
            if (tcData == null) {
                tcData = "";
            }
            if (peData == null) {
                peData = "";
            }
            if (!(tcData.equals(peData))) {
                String[] defferData = new String[2];
                defferData[0] = tcData.toString();
                defferData[1] = peData.toString();
                return defferData;
            }
        }
        return null;
    }

    /**
     * 적당한 길이로 자른다.
     * 
     * @param size
     * @param value
     * @return
     */
    private String longDouble2String(int size, double value) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(size);
        nf.setGroupingUsed(false);
        return nf.format(value);
    }

    /**
     * 쓰기, Released 체크
     * 
     * @method checkPermissions
     * @date 2013. 11. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkPermissions(ItemData itemData) throws Exception {
        try {
            if (itemData.isExistItem()) {
                boolean isReleased = SYMTcUtil.isLatestedRevItemReleased(itemData.getBopBomLine().getItemRevision());
                if (isReleased) {
                    throw new ValidateSDVException("'" + itemData.getClassType() + "' is Released", null);
                }
                boolean isWritable = SYMTcUtil.isBOMWritable(itemData.getBopBomLine());
                if (!isWritable) {
                    throw new ValidateSDVException("'" + itemData.getClassType() + "' BOMLine 쓰기 권한이 없습니다.", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidateSDVException(e.getMessage(), e);
        }

    }

    /**
     * Validate EquipmentData
     * 
     * @method validateEquipmentData
     * @date 2013. 12. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateEquipmentData(EquipmentData equipmentData) throws Exception {
        // 자원(Resource) 아이템 조회
        equipmentData.setResourceItem(SYMTcUtil.getItem(equipmentData.getItemId()));
        setBOMLineMappingOccurrenceData(equipmentData);
        return compareEquipmentData(equipmentData);
    }

    /**
     * Equipment BOMLine 정보와 비교하여 BOMLine 업데이트 대상인지 확인한다.
     * 
     * @method compareEquipmentData
     * @date 2013. 12. 23.
     * @param
     * @return LinkedHashMap<String,String[]>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> compareEquipmentData(EquipmentData equipmentData) throws Exception {
        ArrayList<String> equipmentBOMData = (ArrayList<String>) equipmentData.getData(PEExcelConstants.BOM);
        equipmentBOMData.set(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX, ImportCoreService.getFindNoFromSeq(equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX))); // 컨버젼된 Find No.를 다시 Dataset에 등록
        equipmentData.setFindNo(equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX)); // FindNo 등록
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        if (equipmentData.getResourceItem() == null) {
            return differenceResult;
        }
        if (equipmentData.getBopBomLine() == null) {
            return differenceResult;
        }
        // Quantity 체크
        Double tcEquipmentQuantity = Double.parseDouble(BundleUtil.nullToString(equipmentData.getBopBomLine().getProperty(SDVPropertyConstant.BL_QUANTITY)));
        Double peEquipmentQuantity = Double.parseDouble(BundleUtil.nullToString(equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_QUANTITY_COLUMN_INDEX)));
        String[] defferEquipmentQuantity = getDefferenceData(tcEquipmentQuantity, peEquipmentQuantity);
        if (defferEquipmentQuantity != null) {
            differenceResult.put(SDVPropertyConstant.BL_QUANTITY, defferEquipmentQuantity);
        }
        // Seq No. 체크
        String tcEquipmentSeqNo = BundleUtil.nullToString(equipmentData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
        String peEquipmentSeqNo = equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX); // Find No.
        String[] defferEquipmentSeqNo = getDefferenceData(tcEquipmentSeqNo, peEquipmentSeqNo);
        if (defferEquipmentSeqNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferEquipmentSeqNo);
        }
        // BOMLine 변경 정보가 있으므로 업데이트
        if (differenceResult.size() > 0) {
            equipmentData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * Validate ToolData
     * 
     * @method validateToolData
     * @date 2013. 12. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateToolData(ToolData toolData) throws Exception {
        // 자원(Resource) 아이템 조회
        toolData.setResourceItem(SYMTcUtil.getItem(toolData.getItemId()));
        setBOMLineMappingOccurrenceData(toolData);
        return compareToolData(toolData);
    }

    /**
     * TOOL BOMLine 정보와 비교하여 BOMLine 업데이트 대상인지 확인한다.
     * 
     * @method compareToolData
     * @date 2013. 12. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> compareToolData(ToolData toolData) throws Exception {
        ArrayList<String> toolBOMData = (ArrayList<String>) toolData.getData(PEExcelConstants.BOM);
        toolBOMData.set(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX, ImportCoreService.getFindNoFromSeq(toolBOMData.get(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX))); // 컨버젼된 Find No.를 다시 Dataset에 등록
        toolData.setFindNo(toolBOMData.get(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX)); // FindNo 등록
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        if (toolData.getResourceItem() == null) {
            return differenceResult;
        }
        if (toolData.getBopBomLine() == null) {
            return differenceResult;
        }
        // Quantity 체크
        Double tcToolQuantity = Double.parseDouble(BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_QUANTITY)));
        Double peToolQuantity = Double.parseDouble(BundleUtil.nullToString(toolBOMData.get(PEExcelConstants.TOOL_BOM_QUANTITY_INDEX)));
        String[] defferToolQuantity = getDefferenceData(tcToolQuantity, peToolQuantity);
        if (defferToolQuantity != null) {
            differenceResult.put(SDVPropertyConstant.BL_QUANTITY, defferToolQuantity);
        }
        // Torque 체크
        String tcToolTorQueType = BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_TORQUE));
        String tcToolTorQueValue = BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE));
        String tcToolTorQue = "";
        if (!StringUtils.isEmpty(tcToolTorQueValue)) {
            tcToolTorQue = tcToolTorQueType + " " + tcToolTorQueValue;
        }
        String peToolTorQue = BundleUtil.nullToString(toolBOMData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX));
        String[] defferToolTorQue = getDefferenceData(tcToolTorQue, peToolTorQue);
        if (defferToolTorQue != null) {
            differenceResult.put(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, defferToolTorQue);
        }
        // Seq No. 체크
        String tcToolSeqNo = BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
        String peToolSeqNo = toolBOMData.get(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX); // Find No.
        String[] defferToolSeqNo = getDefferenceData(tcToolSeqNo, peToolSeqNo);
        if (defferToolSeqNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferToolSeqNo);
        }
        // BOMLine 변경 정보가 있으므로 업데이트
        if (differenceResult.size() > 0) {
            toolData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * Validate OccurrenceData
     * 
     * @method validateOccurrenceData
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateEndItemData(EndItemData endItemData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        // 자원(Resource) 아이템 조회
        try {
            endItemData.setResourceItem(SYMTcUtil.getItem(endItemData.getItemId()));
        } catch (Exception e) {
            SkipException skipException = new SkipException("E-BOM END-ITEM이 존재하지 않습니다.", e);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        // END-ITEM 처리
        try {
            OperationItemData operationItemData = (OperationItemData) endItemData.getParentItem();
            // PE 정보에서 M-BOM정보를 추출한다.
            TCComponentBOMLine findPEmBOMEndItemBOMLine = findEndItemBOMLine(endItemData.getFunctionItemId(), endItemData.getAbsOccPuids());
            if (findPEmBOMEndItemBOMLine == null) {
                SkipException skipException = new SkipException("E-BOM END-ITEM을 M-BOM에서 찾을 수 없습니다.");
                skipException.setStatus(TCData.STATUS_ERROR);
                throw skipException;
            }
            // M-BOM END-ITEM BOMLine정보를 등록한다.
            endItemData.setEndItemMBOMLine(findPEmBOMEndItemBOMLine);
            // M-BOM END-ITEM BOMLine정보를 가지고 BOP END-ITEM BOMLine정보를 찾는다.
            TCComponentBOMLine[] findBOPEndItemBOMLineList = SDVBOPUtilities.getAssignSrcBomLineList(processLine.window(), findPEmBOMEndItemBOMLine);
            for (int i = 0; i < findBOPEndItemBOMLineList.length; i++) {
                TCComponentBOMLine parentBOMLine = findBOPEndItemBOMLineList[i].parent();
                if (operationItemData.getBopBomLine() == parentBOMLine) {
                    // BOP END-ITEM BOMLine정보를 등록한다.
                    endItemData.setBopBomLine(findBOPEndItemBOMLineList[i]);
                    // Find No. 체크
                    // Find No. 설정
                    endItemData.setFindNo(ImportCoreService.getFindNoFromSeq(endItemData.getFindNo()));
                    String tcEndItemFindNo = BundleUtil.nullToString(endItemData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
                    String peEndItemFindNo = BundleUtil.nullToString(endItemData.getFindNo());
                    String[] defferEndItemFindNo = getDefferenceData(tcEndItemFindNo, peEndItemFindNo);
                    if (defferEndItemFindNo != null) {
                        differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferEndItemFindNo);
                        endItemData.setBOMLineModifiable(true);
                    }
                }
            }
        } catch (Exception e) {
            // END-ITEM Exception은 중단한지않고 Skip처리한다.
            SkipException skipException = new SkipException(e.getMessage(), e);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        return differenceResult;
    }

    /**
     * Validate SubsidiaryData
     * 
     * @method validateSubsidiaryData
     * @date 2013. 12. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> validateSubsidiaryData(SubsidiaryData subsidiaryData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        // 자원(Resource) 아이템 조회
        subsidiaryData.setResourceItem(SYMTcUtil.getItem(subsidiaryData.getItemId()));
        if (subsidiaryData.getResourceItem() == null) {
            SkipException skipException = new SkipException("부자재 Item이 존재하지않습니다. - " + subsidiaryData.getItemId());
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        setBOMLineMappingOccurrenceData(subsidiaryData);
        ArrayList<String> subsidiaryBomData = (ArrayList<String>) subsidiaryData.getData(PEExcelConstants.BOM);
        // 부자재 옵션컨디션 비교
        // 부자재 옵션 컨디션을 PE->TC에 맞게 컨버젼
        subsidiaryData.setConversionOptionCondition(getConversionOptionCondition(subsidiaryBomData.get(PEExcelConstants.SUBSIDIARY_BOM_OPTION_COLUMN_INDEX)));
        String tcSubsidiaryMVLCondition = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryMVLCondition = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).trim();
        }
        String peSubsidiaryMVLCondition = BundleUtil.nullToString(subsidiaryData.getConversionOptionCondition()).trim();
        if (subsidiaryData.getBopBomLine() != null) {
            // 부자재 옵션 컨디션이 없을 경우
            if (StringUtils.isEmpty(peSubsidiaryMVLCondition)) {
                // 부자재 옵션 컨디션을 Parent(공법)에서 가져온다.
                peSubsidiaryMVLCondition = BundleUtil.nullToString(subsidiaryData.getBopBomLine().parent().getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).trim();
                subsidiaryData.setConversionOptionCondition(peSubsidiaryMVLCondition);
            }
        }
        String[] defferSubsidiaryMVLCondition = getDefferenceData(tcSubsidiaryMVLCondition, peSubsidiaryMVLCondition);
        if (defferSubsidiaryMVLCondition != null) {
            differenceResult.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, defferSubsidiaryMVLCondition);
        }
        // Find No. 체크
        // Find No. 설정
        subsidiaryData.setFindNo(ImportCoreService.conversionSubsidiaryFindNo(subsidiaryData.getFindNo())); // 부자재 Seq No. -> Find No.로 변환
        String tcSubsidiaryFindNo = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryFindNo = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
        }
        String peSubsidiaryFindNo = BundleUtil.nullToString(subsidiaryData.getFindNo());
        String[] defferSubsidiaryFindNo = getDefferenceData(tcSubsidiaryFindNo, peSubsidiaryFindNo);
        if (defferSubsidiaryFindNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferSubsidiaryFindNo);
        }
        // 소요량 체크
        String tcSubsidiaryDemeanQuantity = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryDemeanQuantity = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY));
        }
        String peSubsidiaryDemeanQuantity = BundleUtil.nullToString(subsidiaryBomData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_QUANTITY_COLUMN_INDEX));
        String[] defferSubsidiaryDemeanQuantity = getDefferenceData(tcSubsidiaryDemeanQuantity, peSubsidiaryDemeanQuantity);
        if (defferSubsidiaryDemeanQuantity != null) {
            differenceResult.put(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY, defferSubsidiaryDemeanQuantity);
        }
        // 조구분(Day or Night - LOV ==> 주간:A 야간:B)
        String tcSubsidiaryDayOrNight = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryDayOrNight = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT));
        }
        String peSubsidiaryDayOrNight = BundleUtil.nullToString(subsidiaryBomData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_DIVIDE_GROUP_COLUMN_INDEX));
        String[] defferSubsidiaryDayOrNight = getDefferenceData(tcSubsidiaryDayOrNight, peSubsidiaryDayOrNight);
        if (defferSubsidiaryDayOrNight != null) {
            differenceResult.put(SDVPropertyConstant.BL_NOTE_DAYORNIGHT, defferSubsidiaryDayOrNight);
        }
        // 변경 정보가 있으므로 업데이트
        if (differenceResult.size() > 0) {
            subsidiaryData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * validateSheetDatasetData
     * 
     * @method validateDatasetData
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> validateSheetDatasetData(SheetDatasetData tcData) throws Exception {
        File uploadSheetExcelFile = ImportCoreService.getPathFile((String) tcData.getData());
        if (uploadSheetExcelFile == null) {
            SkipException skipException = new SkipException("업로드 대상 작업표준서 파일이 존재하지 않습니다.");
            skipException.setStatus(TCData.STATUS_SKIP);
            throw skipException;
        }
        OperationItemData operationItemData = (OperationItemData) tcData.getParentItem();
        if (operationItemData.isExistItem()) {
            ArrayList<String> operationMasterRowData = (ArrayList<String>) operationItemData.getData(PEExcelConstants.MASTER);
            // I/F 유무 설정
            String isIf = BundleUtil.nullToString(operationMasterRowData.get(PEExcelConstants.OPERATION_MASTER_SHEET_KO_YN_COLUMN_INDEX)).toUpperCase();
            if (isIf!=null && isIf.trim().toUpperCase().indexOf("TRUE")>=0) {
                tcData.setIf(true);
            } else {
                tcData.setIf(false);
                SkipException skipException = new SkipException("작업표준서 I/F 대상이 아닙니다.");
                skipException.setStatus(TCData.STATUS_SKIP);
                throw skipException;
            }
        }
        // 공법 생성시 에는 무조건 작업표준서 등록
        else {
            tcData.setIf(true);
        }
        return null;
    }

    /**
     * Option Condition Conversion
     * 
     * @method setConversionOptionCondition
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private String getConversionOptionCondition(String condition) throws TCException, Exception {
        TCComponentBOMWindow window = processLine.window();
        return ImportCoreService.conversionOptionCondition(window, condition);
    }

    /**
     * OccurrenceData와 BOPBOMLine Mapping
     * 
     * @method setBOMLineMappingOccurrenceData
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setBOMLineMappingOccurrenceData(OccurrenceData occurrenceData) throws Exception {
        ArrayList<TCComponentBOMLine> findBOPBOMLineList = findOperationUnderDataItem(occurrenceData);
        if (findBOPBOMLineList.size() > 0) {
            occurrenceData.setBopBomLine(findBOPBOMLineList.get(0));
        }
    }

    /**
     * M-BOM PRODUCT 하위에서 PE I/F EndItem의 TCComponentMEAppearancePathNode PUID를 검색한다.
     * 
     * @method findEndItemBOMLine
     * @date 2013. 12. 3.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOMLine findEndItemBOMLine(String peFunctionItemId, String absOccPuids) throws Exception {
        if (((PEIFTCDataExecuteJob) tcDataMigrationJob).getPeIFJobWork().functionContexts == null) {
            throw new ValidateSDVException("M-BOM BOMWindow가 초기화되지않았습니다.");
        }
        if (StringUtils.isEmpty(peFunctionItemId)) {
            SkipException skipException = new SkipException("E-BOM의 Function Item 정보가 'NULL' 입니다.");
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }

        // PRODUCT 하위 FUNCTION BOMLINE을 검색
        TCComponentBOMLine findedFunctionBOMLine = null;
        for (AIFComponentContext functionContext : ((PEIFTCDataExecuteJob) tcDataMigrationJob).getPeIFJobWork().functionContexts) {
            TCComponentBOMLine functionBOMLine = (TCComponentBOMLine) functionContext.getComponent();
            String functionItemId = (functionBOMLine.getItem() != null) ? functionBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) : null;
            if (peFunctionItemId.equals(functionItemId)) {
                findedFunctionBOMLine = functionBOMLine;
                break;
            }
        }
        if (findedFunctionBOMLine == null) {
            SkipException skipException = new SkipException("E-BOM과 매치되는 Function 정보가 없습니다. - I/F PE FN ID : " + peFunctionItemId);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        String[] absOccPuidList = splitAbsOccPuids(absOccPuids);
        if (absOccPuidList.length == 0) {
            SkipException skipException = new SkipException("E-BOM ABS Occthread puid 정보가 'NULL' 입니다. - I/F PE FN ID : " + peFunctionItemId);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        TCComponentBOMLine endItemBOMLine = findEndItemBOMLine(absOccPuidList, findedFunctionBOMLine, 0);
        if (endItemBOMLine == null) {
            SkipException skipException = new SkipException("E-BOM과 매치되는 END-ITEM 정보가 없습니다. - I/F PE FN ID : " + peFunctionItemId);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        // Pack인 경우 Unpack한다.
        /*
         * if (endItemBOMLine.isPacked()) {
         * TCComponentBOMLine unpackEndItemBOMLine = null;
         * TCComponentBOMLine[] unpackLines = SDVBOPUtilities.getUnpackBOMLines(endItemBOMLine);
         * if (unpackLines != null) {
         * for (TCComponentBOMLine unpackLine : unpackLines) {
         * String occpuid = unpackLine.getProperty("bl_occurrence_uid");
         * if (absOccPuidList[absOccPuidList.length - 1].equals(occpuid)) {
         * unpackEndItemBOMLine = unpackLine;
         * }
         * }
         * }
         * if (unpackEndItemBOMLine == null) {
         * SkipException skipException = new SkipException("END-ITEM unpack 정보가 없습니다. - I/F PE FN ID : " + peFunctionItemId + " @@ OCCPUID : " + absOccPuidList[absOccPuidList.length - 1]);
         * skipException.setStatus(TCData.STATUS_ERROR);
         * throw skipException;
         * }
         * return unpackEndItemBOMLine;
         * } else {
         * return endItemBOMLine;
         * }
         */
        return endItemBOMLine;
    }

    /**
     * M-BOM PRODUCT 하위에서 Function별 PE I/F EndItem을 검색한다.
     * 
     * @method findEndItemBOMLine
     * @date 2014. 3. 2.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOMLine findEndItemBOMLine(String[] absOccPuidList, TCComponentBOMLine parentBOMLine, int level) throws Exception {
        // pack인 경우 unpack하여 unpack 별로 조회한다.
        try {
            if (parentBOMLine.isPacked()) {
                return parentFindEndItemBOMLine(absOccPuidList, parentBOMLine, level);
            }
            ArrayList<TCComponentBOMLine> unpackChidrenList = unpackChidren(parentBOMLine);
            for (int i = 0; i < unpackChidrenList.size(); i++) {
                String occpuid = unpackChidrenList.get(i).getProperty("bl_occurrence_uid");
                if (absOccPuidList[level].equals(occpuid)) {
                    if (absOccPuidList.length - 1 == level) {
                        return unpackChidrenList.get(i);
                    } else {
                        return findEndItemBOMLine(absOccPuidList, unpackChidrenList.get(i), level + 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    /**
     * Target BOMLine이 packed된 상태이면 unpack후 unpack BOMLine 수 만큼 loop를 하여 endItem을 검색한다.
     * 
     * @method parentFindEndItemBOMLine
     * @date 2014. 3. 2.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine parentFindEndItemBOMLine(String[] absOccPuidList, TCComponentBOMLine parentBOMLine, int level) throws Exception {
        TCComponentBOMLine[] unpackLines = SDVBOPUtilities.getUnpackBOMLines(parentBOMLine);
        if (unpackLines != null) {
            for (TCComponentBOMLine unpackLine : unpackLines) {
                return findEndItemBOMLine(absOccPuidList, unpackLine, level);
            }
        }
        return null;
    }

    /**
     * endItem getChidren시 unpack하여 가져온다.
     * 
     * @method unpackChidren
     * @date 2014. 3. 2.
     * @param
     * @return ArrayList<TCComponentBOMLine>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<TCComponentBOMLine> unpackChidren(TCComponentBOMLine parentBOMLine) throws Exception {
        ArrayList<TCComponentBOMLine> unpackChidrenList = new ArrayList<TCComponentBOMLine>();
        AIFComponentContext contexts[] = parentBOMLine.getChildren();
        for (int i = 0; i < contexts.length; i++) {
            TCComponentBOMLine childLine = (TCComponentBOMLine) contexts[i].getComponent();
            if (childLine == null) {
                continue;
            }
            if (childLine.isPacked()) {
                TCComponentBOMLine[] unpackLines = SDVBOPUtilities.getUnpackBOMLines(childLine);
                if (unpackLines != null) {
                    for (TCComponentBOMLine unpackLine : unpackLines) {
                        unpackChidrenList.add(unpackLine);
                    }
                }
            } else {
                unpackChidrenList.add(childLine);
            }
        }
        return unpackChidrenList;
    }

    /**
     * ABS OCC PUID를 "+" delimit을 구분자로 배열로 반환한다.
     * 
     * @method splitAbsOccPuids
     * @date 2013. 12. 3.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    public String[] splitAbsOccPuids(String absOccPuids) {
        if (StringUtils.isEmpty(absOccPuids)) {
            return new String[0];
        }
        return absOccPuids.split("\\+");
    }

    /**
     * Row Expand 시작 처리
     * 
     * @method expandAllTCDataItemPre
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllTCDataItemPre() throws Exception {

    }

    /**
     * Row Expand 종료 처리
     * 
     * @method expandAllTCDataItemPost
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllTCDataItemPost(ArrayList<TCData> expandAllItems) throws Exception {

    }

    /**
     * 속성 변경정보를 Log에 출력한다.
     * 
     * @method printOperationDifferenceResult
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private String printDifferenceResult(TCData tcData, LinkedHashMap<String, String[]> differenceResult) throws Exception {
        StringBuffer logString = new StringBuffer();
        if (differenceResult == null || differenceResult.size() == 0) {
            return "";
        }
        String[] attrIds = differenceResult.keySet().toArray(new String[differenceResult.size()]);
        logString.append("\t※  <" + tcData.getText() + "> 변경 속성 \n");
        for (String attrId : attrIds) {
            String[] difference = differenceResult.get(attrId);
            logString.append("\t\t[" + attrId + "] > TC: '" + difference[0] + "'   @@   PE: '" + difference[1] + "' \n");
        }
        return logString.toString();
    }

    /**
     * 로그 처리..
     * 
     * @method saveLog
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveLog(final TCData tcData, final ArrayList<Exception> exception, String logMessage) {
        // Log 파일 처리..
        try {
            saveLog(tcData, logMessage, true);
        } catch (Exception e) {
            exception.add(new ValidateSDVException(e.getMessage(), e));
        }
    }

    /**
     * 로그 처리..
     * 
     * @method saveLog
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void saveLog(final TCData tcData, String logMessage, boolean outputClassType) throws Exception {
        Text logText = getTcDataMigrationJob().getLogText();
        String logFilePath = ((PEIFTCDataExecuteJob) getTcDataMigrationJob()).getPeIFJobWork().getLogFilePath();
        ImportCoreService.saveLog(shell, tcData, logText, logFilePath, logMessage, outputClassType);
    }

}
