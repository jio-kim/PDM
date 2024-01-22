/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.peif;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.ExecuteSDVException;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.assembly.CreateAssemblyOPOperation;
import com.symc.plm.me.sdv.operation.common.ReviseActionOperation;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.exception.SkipException;
import com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob;
import com.symc.plm.me.sdv.service.migration.job.peif.PEIFTCDataExecuteJob;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
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
import com.symc.plm.me.sdv.service.resource.service.create.CreateEquipmentItemService;
import com.symc.plm.me.sdv.service.resource.service.create.CreateToolItemService;
import com.symc.plm.me.sdv.view.resource.CreateResourceViewPane;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.cme.time.common.CommonUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentWindow;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentWindowType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : PEExecution
 * Class Description :
 * 
 * [SR없음][20150224]shcho, Revise시 BOPLine에서 newRevision을 인지하지 못하는 경우가 드물게 발생하여, 명시적으로 refresh 수행
 * [SR없음][20150224]shcho, activity 삭제 후 refresh 할때 The specified tag  has been deleted, can not find requested object 에러가 발생하여 공법BOMLine은 refresh에서 제외
 * 
 * @date 2013. 11. 25.
 * 
 */
public class NEWPEExecution extends PEDataWork {

    private static String DEFAULT_REV_ID = "000";
    private static String EMPTY_DATA = "-";

    /**
     * @param shell
     * @param tcDataMigrationJob
     * @param processLine
     * @param mecoNo
     * @param isOverride
     */
    public NEWPEExecution(Shell shell, TCDataMigrationJob tcDataMigrationJob, TCComponentMfgBvrProcess processLine, String mecoNo, boolean isOverride) {
        super(shell, tcDataMigrationJob, processLine, mecoNo, isOverride);
    }

    public void rowExecute(final int index, final TCData tcData) throws Exception {
        final ArrayList<Exception> exception = new ArrayList<Exception>();
        
        Runnable aExecutionRun = new Runnable() {
            public void run() {
                String message = "";
                String logMessage = "";
                String differLog = "";
                try {
                    getTcDataMigrationJob().getTree().setSelection(tcData);
                    // Master 변경 사항이 있으면 SubActivity가 전체 재등록이므로 상태체크를 'STATUS_VALIDATE_COMPLETED' 상태로 변경한다.
                    if (tcData instanceof ActivityMasterData) {
                        if (((ActivityMasterData) tcData).isCreateable()) {
                            setSubActivityStatus((ActivityMasterData) tcData);
                        }
                    }
                    // Validate 상태 체크
                    if (TCData.STATUS_ERROR == tcData.getStatus() && (tcData instanceof SheetDatasetData)==false) {
                        SkipException errorSkipException = new SkipException("Validate시 Error발생으로 인해 실행을 Skip 합니다. - " + tcData.getStatusMessage());
                        errorSkipException.setStatus(TCData.STATUS_ERROR);
                        throw errorSkipException;
                    } else if (TCData.STATUS_SKIP == tcData.getStatus() && (tcData instanceof SheetDatasetData)==false) {
                        SkipException skipSkipException = new SkipException("Validate시 Skip발생으로 인해 실행을 Skip 합니다. - " + tcData.getStatusMessage());
                        skipSkipException.setStatus(TCData.STATUS_SKIP);
                        throw skipSkipException;
                    }
                    // Progress 메세지
                    tcData.setStatus(TCData.STATUS_INPROGRESS);
                    // LINE Execute
                    if (tcData instanceof LineItemData) {
                        executeLine(processLine, (LineItemData) tcData);
                    }
                    // OPERATION 처리
                    else if (tcData instanceof OperationItemData) {
                        // Operation
                        executeOperation((OperationItemData) tcData);
                    }
                    // Activity 처리
                    else if (tcData instanceof ActivityMasterData) {
                        // Activity
                        executeActivity((ActivityMasterData) tcData);
                    }
                    // Resource - OccurrenceData 처리
                    else if (tcData instanceof OccurrenceData) {
                        // Resource - EquipmentData 처리
                        if (tcData instanceof EquipmentData) {
                            differLog = executeEquipmentData((EquipmentData) tcData);
                        }
                        // Resource - ToolData 처리
                        if (tcData instanceof ToolData) {
                            differLog = executeToolData((ToolData) tcData);
                        }
                        // Resource - EndItemData 처리
                        if (tcData instanceof EndItemData) {
                            differLog = executeEndItemData((EndItemData) tcData);
                        }
                        // Resource - SubsidiaryData 처리
                        if (tcData instanceof SubsidiaryData) {
                            differLog = executeSubsidiaryData((SubsidiaryData) tcData);
                        }
                    } // SheetDatasetData 처리
                    else if (tcData instanceof SheetDatasetData) {
                        // DatasetData
                        differLog = executeSheetDatasetData((SheetDatasetData) tcData);
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
             * Master 변경 사항이 있으면 SubActivity가 전체 재등록이므로 상태체크를 'STATUS_VALIDATE_COMPLETED' 상태로 변경한다.
             * 
             * @method setSubActivityStatus
             * @date 2013. 12. 18.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void setSubActivityStatus(ActivityMasterData activityMasterData) {
                if (!activityMasterData.isCreateable()) {
                    return;
                }
                TreeItem[] subActivityItems = activityMasterData.getItems();
                for (TreeItem treeItem : subActivityItems) {
                    ((ActivitySubData) treeItem).setStatus(TCData.STATUS_VALIDATE_COMPLETED, "");
                }
            }

            /**
             * Line Execute
             * 
             * @method executeLine
             * @date 2013. 12. 5.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void executeLine(TCComponentMfgBvrProcess processLine, LineItemData tcData) throws Exception {
            }

            /**
             * Revise Operation Dataset
             * 
             * @method generateOperationReviseDataset
             * @date 2013. 12. 17.
             * @param
             * @return IDataSet
             * @exception
             * @throws
             * @see
             */
            private IDataSet generateOperationReviseDataset(OperationItemData operationItemData) throws Exception {
                IDataSet dataSet = new DataSet();
                // MECO 설정
                IDataMap reviseMecoViewDataMap = new RawDataMap();
                reviseMecoViewDataMap.put(SDVPropertyConstant.SHOP_REV_MECO_NO, ImportCoreService.getMecoRevision(mecoNo), IData.OBJECT_FIELD);
                dataSet.addDataMap("reviseMecoView", reviseMecoViewDataMap);
                // Revise BOMLine 설정
                ArrayList<TCComponentBOMLine> operationBOMLineList = new ArrayList<TCComponentBOMLine>();
                operationBOMLineList.add(operationItemData.getBopBomLine());
                IDataMap reviseViewDataMap = new RawDataMap();
                reviseViewDataMap.put("reviseView", operationBOMLineList, IData.LIST_FIELD);
                dataSet.addDataMap("reviseView", reviseViewDataMap);
                return dataSet;
            }

            /**
             * Operation Execute
             * 
             * @method executeOperation
             * @date 2013. 12. 5.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void executeOperation(OperationItemData operationItemData) throws Exception {
                // 공법 생성
                if (!operationItemData.isExistItem()) {
                    createOperationItem(operationItemData);
                } else {
                    // 1. isOverride = true인 경우 Released 상태이면 공법을 Revise한다.
                    // 2. 공법 마스터가 변경 사항이 있는 경우 or 공법 하위가 변경 사항이 있는 경우

                    // 1. isOverride = true인 경우 Released 상태이면 공법을 Revise한다.
                    if (isOverride) {
                        // 2. 공법 마스터가 변경 사항이 있는 경우 or 공법 하위가 변경 사항이 있는 경우
                        if (operationItemData.isMasterModifiable() || checkOperationUnderModify(operationItemData)) {
                            if (SYMTcUtil.isLatestedRevItemReleased(operationItemData.getBopBomLine().getItemRevision())) {
                                // Revise 실행
                                IDataSet dataSet = generateOperationReviseDataset(operationItemData);
                                ReviseActionOperation ReviseActionOperation = new ReviseActionOperation(100, this.getClass().getName(), dataSet, true);
                                ReviseActionOperation.revise(dataSet);
                                // 로그 출력
                                saveLog(operationItemData, "공법(Operation) ITEM이 Released 상태이므로 Revise 하였습니다. \n");
                                
                                //[SR없음][20150224]shcho, Revise시 BOPLine에서 newRevision을 인지하지 못하는 경우가 드물게 발생하여, 명시적으로 refresh 수행 
                                refreshBOMLine(operationItemData.getBopBomLine());
                                refreshBOMLine(processLine);
                            }
                        }
                    }
                    // Master 속성 변경정보 체크
                    if (operationItemData.isMasterModifiable()) {
                        updateOperationItem(operationItemData);
                    }
                }
                // BOMLine 속성 변경정보 체크
                if (operationItemData.isBOMLineModifiable()) {
                    // 1. Find No. update
                    IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
                    IDataMap opInformDataMap = dataSet.getDataMap("opInform");
                    if (!StringUtils.isEmpty(opInformDataMap.getStringValue(SDVPropertyConstant.BL_SEQUENCE_NO))) {
                        operationItemData.getBopBomLine().getTCProperty(SDVPropertyConstant.BL_SEQUENCE_NO).setStringValue(opInformDataMap.getStringValue(SDVPropertyConstant.BL_SEQUENCE_NO));
                    }
                    // 2. 수량(QUANTITY) update
                    //[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
                    //operationItemData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
                    // 3. Option condition update (Exception 발생시 오류 중단 처리를 하지않는다.)
                    try {
                        SDVBOPUtilities.updateOptionCondition(operationItemData.getBopBomLine(), operationItemData.getConversionOptionCondition());
                    } catch (Exception e) {
                        SkipException skipException = new SkipException(e.getMessage(), e);
                        throw skipException;
                    }
                }
            }

            /**
             * 공법하위 BOMLine이 변경 사항이 있는지 체크한다.
             * 
             * @method checkOperationUnderModify
             * @date 2014. 1. 14.
             * @param
             * @return boolean
             * @exception
             * @throws
             * @see
             */
            private boolean checkOperationUnderModify(OperationItemData operationItemData) {
                TreeItem[] items = operationItemData.getItems();
                for (TreeItem item : items) {
                    // Skip이 아닌경우가 1개라도 나오면 수정사항이 있는 걸로 간주
                    if (TCData.STATUS_SKIP != ((TCData) item).getStatus()) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * 공법 생성
             * 
             * @method createOperationItem
             * @date 2013. 12. 5.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void createOperationItem(OperationItemData operationItemData) throws Exception {
                // Dataset 가져오기
                IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
                CreateAssemblyOPOperation createOperation = new CreateAssemblyOPOperation(99, this.getClass().getName(), dataSet, true);
                // 실행 후 Operation BOMLine을 Set
                operationItemData.setBopBomLine(createOperation.createOperation());
                // BOMLine Modifiable = true
                operationItemData.setBOMLineModifiable(true);
            }

            /**
             * 공법 업데이트
             * 
             * @method updateOperationItem
             * @date 2013. 12. 10.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void updateOperationItem(OperationItemData operationItemData) throws Exception {
                // Dataset 가져오기
                IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
                IDataMap opInformDataMap = dataSet.getDataMap("opInform");
                TCComponentItem item = operationItemData.getBopBomLine().getItem();
                // item.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME).getStringValue());
                // item.setProperty(SDVPropertyConstant.OPERATION_ENG_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_ENG_NAME).getStringValue());
                item.setProperty(SDVPropertyConstant.OPERATION_WORKER_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKER_CODE).getStringValue());
                item.setProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ, opInformDataMap.get(SDVPropertyConstant.OPERATION_PROCESS_SEQ).getStringValue());
                item.setProperty(SDVPropertyConstant.OPERATION_WORKAREA, opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKAREA).getStringValue());
                TCComponentItemRevision itemRevision = operationItemData.getBopBomLine().getItemRevision();
                // REV object_name을 Item과 동일하게 맞춘다.
                itemRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_ENG_NAME).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_SHOP).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_BOP_VERSION).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE).getStringValue());
            }

            /**
             * Activity 생성
             * 
             * @method executeActivity
             * @date 2013. 12. 9.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void executeActivity(ActivityMasterData tcData) throws Exception {
                // 생성 Flag가 이니면 return
                if (!tcData.isCreateable()) {
                    return;
                }
                OperationItemData operationItemData = (OperationItemData) tcData.getParentItem();
                // 1, 2 기능적으로 정상 동작하나 Window 생성 부하로 인해 BOMLine Reference검색 하여 생성하는 1으로 사용
                //
                // 1. <현재 사용중> Activity 생성 (BOMWindow 생성하지않고 ActivityLine 구성)
                createActivities(tcData, operationItemData);
                // 2. <현재 사용하지 않음> Activity 생성 (BOMWindow 생성 후 ActivityLine 구성)
                // createActivitysWithWindow(tcData, operationItemData);
            }

            /**
             * <현재 사용하지 않음> Activity 생성 (BOMWindow 생성 후 ActivityLine 구성)
             * 
             * @deprecated
             * @method createActivitysWithWindow
             * @date 2014. 1. 8.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings({ "unchecked", "unused" })
            private void createActivitysWithWindow(ActivityMasterData tcData, OperationItemData operationItemData) throws Exception {
                TCComponentCfgAttachmentWindow activityWindow = null;
                try {
                    // Activity Window 생성
                    activityWindow = createActivityWindow(operationItemData.getBopBomLine());
                    // Activity Window에 Activity Root Top Line 설정
                    TCComponentCfgAttachmentLine rootTCComponentCfgAttachmentLine = setActivityWindowTopLine(operationItemData, activityWindow);
                    // Activity 전체 삭제
                    removeAllActivity(rootTCComponentCfgAttachmentLine);
                    // activity root refresh
                    refreshBOMLine(rootTCComponentCfgAttachmentLine);
                    // Activity 생성
                    TreeItem[] activitySubDatas = tcData.getItems();
                    for (TreeItem activitySubData : activitySubDatas) {
                        ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
                        TCComponent[] afterTCComponents = ActivityUtils.createActivitiesBelow(new TCComponent[] { rootTCComponentCfgAttachmentLine }, activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KOR_NAME_COLUMN_INDEX));
                        TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) afterTCComponents[0];
                        TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();
                        // Activity Time
                        double timeSystemUnitTime = 0.0;
                        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX))) {
                            timeSystemUnitTime = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX));
                        }
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).setDoubleValue(timeSystemUnitTime);
                        // Category
                        String category = ImportCoreService.getPEActivityCategolyLOV(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_CATEGORY_COLUMN_INDEX));
                        if (category != null) {
                            activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).setStringValue(category);
                        }
                        // Work Code (SYSTEM Code)
                        // 작업약어
                        String workCode = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_MAIN_COLUMN_INDEX);
                        // 변수
                        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX))) {
                            workCode = workCode + "-" + activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX).replace(",", "-");
                        }
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).setStringValue(workCode);
                        // Time System Frequency
                        double timeSystemFrequency = 0.0;
                        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX))) {
                            timeSystemFrequency = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX));
                        }
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).setDoubleValue(timeSystemFrequency);
                        // Activity 공구자원 할당
                        addActivityTools(operationItemData.getBopBomLine(), activityLine, (ActivitySubData) activitySubData);
                        // KPC
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_COLUMN_INDEX));
                        // KPC 관리기준
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_BASIS_COLUMN_INDEX));
                        // activity sub refresh
                        refreshBOMLine(activityLine);
                    }
                    // activity root refresh
                    refreshBOMLine(rootTCComponentCfgAttachmentLine);
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (activityWindow != null) {
                        activityWindow.closeWindow();
                    }
                }
            }

            /**
             * <현재 사용중> Activity 생성 (BOMWindow 생성하지않고 ActivityLine 구성)
             * 
             * @method createActivities
             * @date 2014. 1. 8.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            public void createActivities(ActivityMasterData tcData, OperationItemData operationItemData) throws Exception, TCException {
                TCComponentCfgActivityLine[] childActivityList = getChildActivityList(operationItemData.getBopBomLine());
                for (TCComponentCfgActivityLine meActivityLine : childActivityList) {
                    TCComponentMECfgLine parentLine = meActivityLine.parent();
                    ActivityUtils.removeActivity(meActivityLine);
                    parentLine.save();
                }
                TCComponent root = operationItemData.getBopBomLine().getReferenceProperty("bl_me_activity_lines");
                // activity refresh
                refreshBOMLine(root);
                //[SR없음][20150224]shcho, activity 삭제 후 refresh 할때 The specified tag  has been deleted, can not find requested object 에러가 발생하여 공법BOMLine은 refresh에서 제외
                //refreshBOMLine(operationItemData.getBopBomLine());

                // Activity 생성
                TreeItem[] activitySubDatas = tcData.getItems();
                for (TreeItem activitySubData : activitySubDatas) {
                    ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
                    TCComponent[] afterTCComponents = ActivityUtils.createActivitiesBelow(new TCComponent[] { root }, activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KOR_NAME_COLUMN_INDEX));
                    TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) afterTCComponents[0];
                    TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();
                    // Activity Time
                    double timeSystemUnitTime = 0.0;
                    if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX))) {
                        timeSystemUnitTime = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX));
                    }
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).setDoubleValue(timeSystemUnitTime);
                    // Category
                    String category = ImportCoreService.getPEActivityCategolyLOV(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_CATEGORY_COLUMN_INDEX));
                    if (category != null) {
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).setStringValue(category);
                    }
                    // Work Code (SYSTEM Code)
                    // 작업약어
                    String workCode = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_MAIN_COLUMN_INDEX);
                    // 변수
                    if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX))) {
                        workCode = workCode + "-" + activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX).replace(",", "-");
                    }
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).setStringValue(workCode);
                    // Time System Frequency
                    double timeSystemFrequency = 0.0;
                    if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX))) {
                        timeSystemFrequency = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX));
                    }
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).setDoubleValue(timeSystemFrequency);
                    // Activity 공구자원 할당
                    addActivityTools(operationItemData.getBopBomLine(), activityLine, (ActivitySubData) activitySubData);
                    // KPC
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_COLUMN_INDEX));
                    // KPC 관리기준
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_BASIS_COLUMN_INDEX));
                    activity.save();
                    root.save();
                }
            }

            private TCComponentCfgActivityLine[] getChildActivityList(TCComponentBOMLine bomLine) throws Exception {
                ArrayList<TCComponentCfgActivityLine> childActivityList = new ArrayList<TCComponentCfgActivityLine>();
                TCComponent root = bomLine.getReferenceProperty("bl_me_activity_lines");
                if (root != null) {
                    if (root instanceof TCComponentCfgActivityLine) {
                        TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) root);
                        for (TCComponent childLine : childLines) {
                            if (childLine instanceof TCComponentCfgActivityLine) {
                                childActivityList.add((TCComponentCfgActivityLine) childLine);
                            }
                        }
                    }
                }
                return childActivityList.toArray(new TCComponentCfgActivityLine[childActivityList.size()]);
            }

            /**
             * Activity 공구자원 할당
             * 
             * @method addActivityTools
             * @date 2013. 12. 19.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private void addActivityTools(TCComponentBOMLine operationBOMLine, TCComponentCfgActivityLine activityLine, ActivitySubData activitySubData) throws Exception {
                TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();
                ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
                String toolId = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TOOL_ID_COLUMN_INDEX);
                if (StringUtils.isEmpty(toolId)) {
                    return;
                }
                String[] toolIds = toolId.split(",");
                HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = findAssignToolBOMLine(operationBOMLine, toolIds);
                for (String itemId : findedAssignToolBOMLine.keySet()) {
                    TCComponentBOMLine bomLine = findedAssignToolBOMLine.get(itemId);
                    if (bomLine != null) {
                        activity.addReferenceTools(operationBOMLine, new TCComponentBOMLine[] { bomLine });
                    }
                    // Activity 공구할당 로그
                    saveLogAcvivityAssingTool(itemId, activitySubData, bomLine);
                }
            }

            /**
             * Activity 공구할당 로그
             * 
             * @method saveLogAcvivityAssingTool
             * @date 2013. 12. 19.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void saveLogAcvivityAssingTool(String toolItemId, ActivitySubData activitySubData, TCComponentBOMLine bomLine) throws Exception {
                StringBuffer log = new StringBuffer();
                if (bomLine == null) {
                    String errorMsg = "'" + toolItemId + "' 가 공법(Operation)에 존재하지 않아 Activity 할당을 Skip 하였습니다.";
                    log.append("~ assign Activity : [" + activitySubData.getText() + "] " + errorMsg);
                    activitySubData.setStatus(TCData.STATUS_ERROR, errorMsg);
                } else {
                    log.append("~ assign Activity : [" + activitySubData.getText() + "] '" + toolItemId + "' 를(을) 공법(Operation) Activity에 할당 하였습니다.");
                }
                saveLog(activitySubData, log.toString());
            }

            /**
             * 할당 대상 공구(Tool) 공법(Operation)에서 검색
             * 
             * @method findAssignToolBOMLine
             * @date 2013. 12. 19.
             * @param
             * @return HashMap<String,TCComponentBOMLine>
             * @exception
             * @throws
             * @see
             */
            private HashMap<String, TCComponentBOMLine> findAssignToolBOMLine(TCComponentBOMLine operationBOMLine, String[] toolIds) throws Exception {
                HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = new HashMap<String, TCComponentBOMLine>();
                if (toolIds == null || toolIds.length == 0) {
                    return findedAssignToolBOMLine;
                }
                // 초기화
                for (int i = 0; i < toolIds.length; i++) {
                    toolIds[i] = (toolIds[i] == null) ? "" : toolIds[i].trim();
                }
                for (String toolId : toolIds) {
                    findedAssignToolBOMLine.put(toolId, null);
                }
                TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(operationBOMLine);
                for (TCComponentBOMLine operationUnderBOMLine : childs) {
                    String itemId = operationUnderBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    // PE I/F 공구ID를 가지고 공법하위 공구 검색
                    if (findedAssignToolBOMLine.containsKey(itemId)) {
                        findedAssignToolBOMLine.put(itemId, operationUnderBOMLine);
                    }
                }
                return findedAssignToolBOMLine;
            }

            /**
             * Activity Window에 Activity Root Top Line 설정
             * 
             * @method setActivityWindowTopLine
             * @date 2013. 12. 13.
             * @param
             * @return TCComponentCfgAttachmentLine
             * @exception
             * @throws
             * @see
             */
            private TCComponentCfgAttachmentLine setActivityWindowTopLine(OperationItemData operationItemData, TCComponentCfgAttachmentWindow activityWindow) throws Exception {
                // TOP Line 설정
                TCComponentItemRevision localTCComponentItemRevision = operationItemData.getBopBomLine().getItemRevision();
                if (!(localTCComponentItemRevision instanceof TCComponentMEOPRevision)) {
                    throw new ExecuteSDVException("공법(Operation)의  ItemRevision이 없습니다.");
                }
                TCComponentMEOPRevision localTCComponentMEOPRevision = (TCComponentMEOPRevision) localTCComponentItemRevision;
                TCComponentMEActivity localTCComponentMEActivity = (TCComponentMEActivity) localTCComponentMEOPRevision.getRelatedComponent("root_activity");
                if (localTCComponentMEActivity == null) {
                    throw new ExecuteSDVException("공법(Operation)의 Root Activity 정보가 없습니다.");
                }
                TCComponentCfgAttachmentLine rootTCComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) activityWindow.createTopLine(localTCComponentMEActivity);
                rootTCComponentCfgAttachmentLine.setReferenceProperty("al_activity_oper_bl", operationItemData.getBopBomLine());
                return rootTCComponentCfgAttachmentLine;
            }

            /**
             * Activity 전체 삭제
             * 
             * @method removeAllActivity
             * @date 2013. 12. 13.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void removeAllActivity(TCComponentCfgAttachmentLine rootTCComponentCfgAttachmentLine) throws TCException {
                TCComponent[] childComps = ActivityUtils.getSortedActivityChildren(rootTCComponentCfgAttachmentLine);
                for (TCComponent activityChildComp : childComps) {
                    TCComponentCfgAttachmentLine activityBOMLine = (TCComponentCfgAttachmentLine) activityChildComp;
                    // rootTCComponentCfgAttachmentLine.deleteChild(activityBOMLine, "me_cl_child_lines");
                    ActivityUtils.removeActivity(activityBOMLine);
                }
            }

            /**
             * 자원할당 실행 - EquipmentData
             * 
             * @method executeEquipmentData
             * @date 2013. 12. 12.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private String executeEquipmentData(EquipmentData equipmentData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) equipmentData.getParentItem()).getBopBomLine();
                if (equipmentData.getBopBomLine() == null) {
                    // 설비(Equipment) Item 생성
                    if (equipmentData.getResourceItem() == null) {
                        equipmentData.setResourceItem(createResourceItem(log, equipmentData));
                    }
                    ArrayList<InterfaceAIFComponent> toolDataList = new ArrayList<InterfaceAIFComponent>();
                    toolDataList.add(equipmentData.getResourceItem());
                    TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, toolDataList, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
                    // BOMLine 설정
                    equipmentData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                    // BOMLine Modifiable = true
                    equipmentData.setBOMLineModifiable(true);
                    log.append("\t ~ 설비(EQUIPMENT) 할당 완료 - " + equipmentData.getItemId() + "\n");
                }
                if (equipmentData.isBOMLineModifiable()) {
                    ArrayList<String> bomRowData = (ArrayList<String>) equipmentData.getData(PEExcelConstants.BOM);
                    equipmentData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, bomRowData.get(PEExcelConstants.EQUIPMENT_BOM_QUANTITY_COLUMN_INDEX));
                    equipmentData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, equipmentData.getFindNo());
                    log.append("\t ~ 설비(EQUIPMENT) BOMLine 업데이트 완료 - " + equipmentData.getItemId());
                }
                return log.toString();
            }

            /**
             * 자원할당 실행 - ToolData
             * 
             * @method executeToolData
             * @date 2013. 12. 10.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private String executeToolData(ToolData toolData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) toolData.getParentItem()).getBopBomLine();
                if (toolData.getBopBomLine() == null) {
                    // 공구(Tool) Item 생성
                    if (toolData.getResourceItem() == null) {
                        toolData.setResourceItem(createResourceItem(log, toolData));
                    }
                    ArrayList<InterfaceAIFComponent> toolDataList = new ArrayList<InterfaceAIFComponent>();
                    toolDataList.add(toolData.getResourceItem());
                    TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, toolDataList, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL);
                    // BOMLine 설정
                    toolData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                    // BOMLine Modifiable = true
                    toolData.setBOMLineModifiable(true);
                    log.append("\t ~ 공구(TOOL) 할당 완료 - " + toolData.getItemId() + "\n");
                }
                if (toolData.isBOMLineModifiable()) {
                    ArrayList<String> bomRowData = (ArrayList<String>) toolData.getData(PEExcelConstants.BOM);
                    toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, bomRowData.get(PEExcelConstants.TOOL_BOM_QUANTITY_INDEX));
                    String torqueType = "";
                    String torqueValue = "";
                    if (StringUtils.isEmpty(bomRowData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX))) {
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE, torqueType);
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, torqueValue);
                    } else {
                        // String[] torqueDelimits = bomRowData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX).split(" ");
                        // torqueType = torqueDelimits[0].trim();
                        // torqueValue = torqueDelimits[1].trim();
                        String peTorque = bomRowData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX).trim();
                        // Torque Type : 앞자리 2자리 무조건 substring 
                        torqueType = peTorque.substring(0, 2);
                        // Torque Value : 2자리 이후 나머지 자릿수
                        torqueValue = peTorque.substring(2, peTorque.length());
                        torqueValue = torqueValue.trim();
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE, torqueType);
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, torqueValue);
                    }
                    toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, toolData.getFindNo());
                    log.append("\t ~ 공구(TOOL) BOMLine 업데이트 완료 - " + toolData.getItemId());
                }
                return log.toString();
            }

            /**
             * 설비(Equipment), 공구(Tool) Item 생성
             * 
             * (생성 시 오류발생은 Skip 처리한다.)
             * 
             * @method createResourceEquipmentItem
             * @date 2013. 12. 13.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private TCComponentItem createResourceItem(StringBuffer log, OccurrenceData occurrenceData) throws Exception {
                TCComponentItem resourceItem = null;
                try {
                    if (occurrenceData.getResourceItem() == null) {
                        occurrenceData.setResourceItem(createResourceItem(occurrenceData));
                        log.append("\t ~ [" + occurrenceData.getClassType() + "] Resource가 존재하지않아 생성하였습니다. - " + occurrenceData.getItemId() + "\n");
                    }
                    if (occurrenceData.getData(PEExcelConstants.MASTER) == null) {
                        throw new ExecuteSDVException("[" + occurrenceData.getClassType() + "] PE I/F ㅡMASTER 정보가 없습니다. - " + occurrenceData.getItemId());
                    }
                    resourceItem = occurrenceData.getResourceItem();
                } catch (Exception e) {
                    SkipException skipException = new SkipException(e.getMessage(), e);
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
                return resourceItem;
            }

            /**
             * Resource Type별 Item 생성
             * 
             * @method createResourceItem
             * @date 2013. 12. 13.
             * @param
             * @return TCComponentItem
             * @exception
             * @throws
             * @see
             */
            private TCComponentItem createResourceItem(OccurrenceData occurrenceData) throws Exception {
                if (occurrenceData instanceof ToolData) {
                    return createResourceToolItem((ToolData) occurrenceData);
                } else if (occurrenceData instanceof EquipmentData) {
                    return createResourceEquipmentItem((EquipmentData) occurrenceData);
                } else {
                    SkipException skipException = new SkipException("생성 Resource Item Type이 아닙니다. - " + occurrenceData.getClassType());
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
            }

            /**
             * 공구(Tool) Resource Item 생성
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
            public TCComponentItem createResourceToolItem(ToolData toolData) throws Exception {
                ArrayList<String> masterRowData = (ArrayList<String>) toolData.getData(PEExcelConstants.MASTER);
                IDataMap datamap = new RawDataMap();
                datamap.put("createMode", true, IData.BOOLEAN_FIELD);
                datamap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_TOOL_ITEM, IData.STRING_FIELD);
                // Map<String, String> itemProperties
                // public static final String TOOL_ENG_NAME = "m7_ENG_NAME";
                Map<String, String> itemProperties = new HashMap<String, String>();
                itemProperties.put(SDVPropertyConstant.ITEM_ITEM_ID, masterRowData.get(PEExcelConstants.TOOL_MASTER_ITEM_ID_COLUMN_INDEX));
                itemProperties.put(SDVPropertyConstant.TOOL_ENG_NAME, masterRowData.get(PEExcelConstants.TOOL_MASTER_ENG_NAME_COLUMN_INDEX));
                datamap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);
                // Map<String, String> revisionProperties
                // public static final String TOOL_RESOURCE_CATEGORY = "m7_RESOURCE_CATEGORY";
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
                revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);
                revisionProperties.put(SDVPropertyConstant.ITEM_OBJECT_NAME, masterRowData.get(PEExcelConstants.TOOL_MASTER_KOR_NAME_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY, getToolResourceCateGory(masterRowData.get(PEExcelConstants.TOOL_MASTER_ITEM_ID_COLUMN_INDEX)));
                revisionProperties.put(SDVPropertyConstant.TOOL_MAIN_CLASS, masterRowData.get(PEExcelConstants.TOOL_MASTER_MAIN_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_SUB_CLASS, masterRowData.get(PEExcelConstants.TOOL_MASTER_SUB_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_PURPOSE, masterRowData.get(PEExcelConstants.TOOL_MASTER_PURPOSE_KOR_COLUMN_INDEX));
                // 제약조건 제외 초기화 ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.TOOL_PURPOSE))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_PURPOSE, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_KOR, masterRowData.get(PEExcelConstants.TOOL_MASTER_SPEC_KOR_COLUMN_INDEX));
                // 제약조건 제외 초기화 ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.TOOL_SPEC_KOR))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_KOR, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_ENG, masterRowData.get(PEExcelConstants.TOOL_MASTER_SPEC_ENG_COLUMN_INDEX));
                // 제약조건 제외 초기화 ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.TOOL_SPEC_ENG))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_ENG, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.TOOL_TORQUE_VALUE, masterRowData.get(PEExcelConstants.TOOL_MASTER_PURPOSE_KOR_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_UNIT_USAGE, masterRowData.get(PEExcelConstants.TOOL_MASTER_TORQUE_VALUE_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_MATERIAL, masterRowData.get(PEExcelConstants.TOOL_MASTER_MATERIAL_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_MAKER, masterRowData.get(PEExcelConstants.TOOL_MASTER_MAKER_COLUMN_INDEX));
                // Socket 공구 처리
                if (isToolSocket(revisionProperties.get(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_SHAPE, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_SHAPE_COLUMN_INDEX));
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_LENGTH, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_LENGTH_COLUMN_INDEX));
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_SIZE, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_SIZE_COLUMN_INDEX));
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_MAGNET, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_MAGNET_COLUMN_INDEX));
                }
                // Socket 공구가 아닌 경우에만 처리
                else {
                    revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_CODE, masterRowData.get(PEExcelConstants.TOOL_MASTER_SPEC_CODE_COLUMN_INDEX));
                    String makerAf = BundleUtil.nullToString(masterRowData.get(PEExcelConstants.TOOL_MASTER_MAKER_AF_CODE_COLUMN_INDEX)).trim();
                    // empty -> 00
                    if (StringUtils.isEmpty(makerAf)) {
                        revisionProperties.put(SDVPropertyConstant.TOOL_MAKER_AF_CODE, "00");
                    }
                    // 3자리에서 앞의 두자리만 처리 - 000 -> 00
                    else if (makerAf.length() == 3) {
                        revisionProperties.put(SDVPropertyConstant.TOOL_MAKER_AF_CODE, makerAf.substring(0, 1));
                    }
                }

                // 도장
                // revisionProperties.put(SDVPropertyConstant.TOOL_VEHICLE_CODE, masterRowData.get(0));
                // revisionProperties.put(SDVPropertyConstant.TOOL_STAY_TYPE, masterRowData.get(0));
                // revisionProperties.put(SDVPropertyConstant.TOOL_STAY_AREA, masterRowData.get(0));
                datamap.put("revisionProperties", revisionProperties, IData.OBJECT_FIELD);
                // CAD File List
                RawDataMap fileDataMap = new RawDataMap();
                if (!StringUtils.isEmpty(masterRowData.get(PEExcelConstants.TOOL_MASTER_CAD_FILE_PATH_COLUMN_INDEX))) {
                    fileDataMap.put("isModified", true, IData.BOOLEAN_FIELD);
                    fileDataMap.put("CATPart", masterRowData.get(PEExcelConstants.TOOL_MASTER_CAD_FILE_PATH_COLUMN_INDEX), IData.STRING_FIELD);
                } else {
                    fileDataMap.put("isModified", false, IData.BOOLEAN_FIELD);
                }
                datamap.put("File", fileDataMap, IData.OBJECT_FIELD);

                CreateToolItemService createItemService = new CreateToolItemService(datamap);
                return createItemService.create().getItem();
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
                    SkipException skipException = new SkipException("TOOL ITEM ID가 존재 하지 않습니다.");
                    skipException.setStatus(TCData.STATUS_ERROR);
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
                if (category.equals(registry.getString("Resource.Category.SOC"))) {
                    return true;
                } else {
                    return false;
                }
            }

            /**
             * 설비(Equipment) Resource Item 생성
             * 
             * @method createResourceEquipmentItem
             * @date 2013. 12. 26.
             * @param
             * @return TCComponentItem
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private TCComponentItem createResourceEquipmentItem(EquipmentData equipmentData) throws Exception {
                ArrayList<String> masterRowData = (ArrayList<String>) equipmentData.getData(PEExcelConstants.MASTER);
                IDataMap datamap = new RawDataMap();
                datamap.put("createMode", true, IData.BOOLEAN_FIELD);
                boolean isJIG = false;
                if (!StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_JIG_VEHICLE_CODE_COLUMN_INDEX))) {
                    isJIG = true;
                    datamap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM, IData.STRING_FIELD);
                } else {
                    datamap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM, IData.STRING_FIELD);
                }
                // Map<String, String> itemProperties
                // public static final String EQUIP_ENG_NAME = "m7_ENG_NAME";
                Map<String, String> itemProperties = new HashMap<String, String>();
                itemProperties.put(SDVPropertyConstant.ITEM_ITEM_ID, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_ITEM_ID_COLUMN_INDEX));
                itemProperties.put(SDVPropertyConstant.EQUIP_ENG_NAME, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_ENG_NAME_COLUMN_INDEX));
                datamap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);
                // Map<String, String> revisionProperties
                // public static final String EQUIP_SHOP_CODE = "m7_SHOP";
                // public static final String EQUIP_RESOURCE_CATEGORY = "m7_RESOURCE_CATEGORY";
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
                revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);
                revisionProperties.put(SDVPropertyConstant.ITEM_OBJECT_NAME, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_KOR_NAME_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_SHOP_CODE, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SHOP_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_MAIN_CLASS, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_MAIN_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_SUB_CLASS, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SUB_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_KOR, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SPEC_KOR_COLUMN_INDEX));
                // 제약조건 제외 초기화 ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_SPEC_KOR))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_KOR, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_ENG, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SPEC_ENG_COLUMN_INDEX));
                // 제약조건 제외 초기화 ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_SPEC_ENG))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_ENG, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_CAPACITY, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_CAPACITY_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_MAKER, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_MAKER_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_NATION, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_NATION_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_INSTALL_YEAR, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_INSTALL_YEAR_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_KOR, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_PURPOSE_KOR_COLUMN_INDEX));
                // 제약조건 제외 초기화 ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_PURPOSE_KOR))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_KOR, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_PURPOSE_ENG_COLUMN_INDEX));
                // 제약조건 제외 초기화 ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_PURPOSE_ENG))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_REV_DESC, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_REV_DESC_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY, getEquipmentResourceCateGory(masterRowData));
                // JIG
                if (isJIG) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_VEHICLE_CODE, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_JIG_VEHICLE_CODE_COLUMN_INDEX));
                }
                // 차체 JIG
                // revisionProperties.put(SDVPropertyConstant.EQUIP_STATION_CODE, masterRowData.get(0));
                // revisionProperties.put(SDVPropertyConstant.EQUIP_POSITION_CODE, masterRowData.get(0));
                // 도장
                // revisionProperties.put(SDVPropertyConstant.EQUIP_LINE_CODE, masterRowData.get(0));

                datamap.put("revisionProperties", revisionProperties, IData.OBJECT_FIELD);
                // CAD File List
                RawDataMap fileDataMap = new RawDataMap();
                if (!StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_CAD_FILE_PATH_COLUMN_INDEX))) {
                    fileDataMap.put("isModified", true, IData.BOOLEAN_FIELD);
                    fileDataMap.put("CATPart", masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_CAD_FILE_PATH_COLUMN_INDEX), IData.STRING_FIELD);
                } else {
                    fileDataMap.put("isModified", false, IData.BOOLEAN_FIELD);
                }
                datamap.put("File", fileDataMap, IData.OBJECT_FIELD);

                CreateEquipmentItemService createEquipmentItemService = new CreateEquipmentItemService(datamap);
                return createEquipmentItemService.create().getItem();
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
            private String getEquipmentResourceCateGory(ArrayList<String> masterRowData) throws Exception {
                if (StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_ITEM_ID_COLUMN_INDEX))) {
                    SkipException skipException = new SkipException("EQUIPMENT ITEM ID가 존재 하지 않습니다.");
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
                Registry registry = Registry.getRegistry(CreateResourceViewPane.class);
                if (StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_JIG_VEHICLE_CODE_COLUMN_INDEX))) {
                    return registry.getString("Resource.Category.EXT");
                } else {
                    return registry.getString("Resource.Category.JIG");
                }
            }

            /**
             * 자원할당 실행 - EndItemData
             * 
             * @method executeEndItemData
             * @date 2013. 12. 10.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private String executeEndItemData(EndItemData endItemData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) endItemData.getParentItem()).getBopBomLine();
                if (endItemData.getEndItemMBOMLine() == null) {
                    SkipException skipException = new SkipException("할당대상인 END-ITEM BOMLine정보가 없습니다.");
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
                if (endItemData.getBopBomLine() == null) {
                    try {
                        ArrayList<InterfaceAIFComponent> endItemBOMLineList = new ArrayList<InterfaceAIFComponent>();
                        endItemBOMLineList.add(endItemData.getEndItemMBOMLine());
                        TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, endItemBOMLineList, null);
                        endItemData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                        endItemData.setBOMLineModifiable(true);
                    } catch (Exception e) {
                        throw new ExecuteSDVException(e.getMessage(), e);
                    }
                }
                if (endItemData.isBOMLineModifiable()) {
                    updateEndItemBOMLine(endItemData, log);
                }
                return log.toString();
            }

            /**
             * 일반자재 BOMLine 정보를 업데이트한다.
             * 
             * @method updateEndItemBOMLine
             * @date 2014. 1. 6.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            public void updateEndItemBOMLine(EndItemData endItemData, StringBuffer log) throws TCException {
                // SEQ No.를 가지고 변환한 Find No.를 등록 (1 -> 001)
                endItemData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, endItemData.getFindNo());
                log.append("\t ~ 일반자재(END-ITEM) 할당 완료 - " + endItemData.getItemId());
            }

            /**
             * 자원할당 실행 - SubsidiaryData
             * 
             * @method executeSubsidiaryData
             * @date 2013. 12. 12.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private String executeSubsidiaryData(SubsidiaryData subsidiaryData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) subsidiaryData.getParentItem()).getBopBomLine();
                if (subsidiaryData.getBopBomLine() == null) {
                    try {
                        if (subsidiaryData.getResourceItem() == null) {
                            SkipException skipException = new SkipException("할당대상인 부자재 Item이 존재하지 않습니다.");
                            skipException.setStatus(TCData.STATUS_ERROR);
                            throw skipException;
                        }
                        ArrayList<InterfaceAIFComponent> toolDataList = new ArrayList<InterfaceAIFComponent>();
                        toolDataList.add(subsidiaryData.getResourceItem());
                        TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, toolDataList, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY);
                        subsidiaryData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                        log.append("\t ~ 부자재(Subsidiary) 할당 완료 - " + subsidiaryData.getItemId() + "\n");
                    } catch (Exception e) {
                        throw new ExecuteSDVException(e.getMessage(), e);
                    }
                }
                if (subsidiaryData.isBOMLineModifiable()) {
                    ArrayList<String> bomRowData = (ArrayList<String>) subsidiaryData.getData(PEExcelConstants.BOM);
                    // Qty = 1
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
                    // 소요량 등록
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY, bomRowData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_QUANTITY_COLUMN_INDEX));
                    // 부자재 Option condition 등록
                    SDVBOPUtilities.updateAssiginOptionCondition(subsidiaryData.getBopBomLine(), subsidiaryData.getConversionOptionCondition());
                    // Find No. 등록
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, subsidiaryData.getFindNo());
                    // 조구분 등록
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT, bomRowData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_DIVIDE_GROUP_COLUMN_INDEX));
                    log.append("\t ~ 부자재(Subsidiary) BOMLine 업데이트 완료 - " + subsidiaryData.getItemId());
                }
                return log.toString();
            }

            /**
             * 작업표준서 SheetDatasetData 처리
             * 
             * @method executeSheetDatasetData
             * @date 2013. 12. 11.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private String executeSheetDatasetData(SheetDatasetData tcData) throws Exception {
                StringBuffer log = new StringBuffer();
                OperationItemData operationItemData = (OperationItemData) tcData.getParentItem();
                TCComponentBOMLine bopOperationBOMLine = operationItemData.getBopBomLine();
                TCComponentDataset korSheetDataset = null;
                Vector<File> uploadFiles = new Vector<File>();
                if (tcData instanceof SheetDatasetData) {
                    SheetDatasetData sheetDatasetData = (SheetDatasetData) tcData;
                    
//                    System.out.println("# ---------------------------------------------------------------------------");
//                    System.out.println("# operationItemData.getItemId()="+operationItemData.getItemId());
//                    System.out.println("# operationItemData.getRevId()="+operationItemData.getRevId());
//                    System.out.println("# operationItemData.isReleased() = "+operationItemData.isReleased());
//                    System.out.println("# operationItemData.isExistItem() = "+operationItemData.isExistItem());
//                    System.out.println("# sheetDatasetData.isIf() = "+sheetDatasetData.isIf());
                    boolean isReleased = false;
                    if(bopOperationBOMLine!=null){
                    	TCComponentReleaseStatus status=(TCComponentReleaseStatus)bopOperationBOMLine.getItemRevision().getRelatedComponent("release_status_list");
                    	if(status!=null){
                    		isReleased = true;
                    	}
                    	
                        // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
                    	BOPLineUtility.updateLineToOperationAbsOccId(bopOperationBOMLine);
                    	
                    }
                    
                    // 작업표준서 I/F 대상인 경우 Update - 단 공법초도 생성인 경우는 무조건 작업표준서 등록
                    //if(operationItemData.isExistItem()==false && sheetDatasetData.isIf()==false){
                    if(isReleased==true || sheetDatasetData.isIf()==false){
                        SkipException skipException = new SkipException("작업표준서 I/F 대상이 아닙니다.");
                        skipException.setStatus(TCData.STATUS_SKIP);
                        throw skipException;
                    }
                    
                    // Dataset upload대상 File
                    String uploadFilePath = (String) sheetDatasetData.getData();
                    if (StringUtils.isEmpty(uploadFilePath)) {
                        SkipException skipException = new SkipException("작업표준서 파일 경로가 없어 Skip 합니다.");
                        skipException.setStatus(TCData.STATUS_ERROR);
                        throw skipException;
                    }
                    File uploadSheetExcelFile = ImportCoreService.getPathFile(uploadFilePath);
                    if (uploadSheetExcelFile == null) {
                        SkipException skipException = new SkipException("업로드 대상 작업표준서 파일이 존재하지 않습니다.");
                        skipException.setStatus(TCData.STATUS_ERROR);
                        throw skipException;
                    }
                    uploadFiles.add(uploadSheetExcelFile);
                    ArrayList<TCComponentDataset> korSheetDatastList = getKorSheetDatastList(bopOperationBOMLine);
                    if (korSheetDatastList.size() > 0) {
                        korSheetDataset = korSheetDatastList.get(0);
                        String oldDatasetName = korSheetDataset.toDisplayString();
                        // SYMTcUtil.removeAllNamedReference(korSheetDataset);
                        bopOperationBOMLine.getItemRevision().remove(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, korSheetDataset);
                        korSheetDataset.delete();
                        // SYMTcUtil.importFiles(korSheetDataset, uploadFiles);
                        bopOperationBOMLine.clearCache();
                        bopOperationBOMLine.refresh();
                        log.append("\t ~ 기존 작업표준서 Dataset을 삭제하였습니다. - " + oldDatasetName + "\n");
                    }
                    SDVBOPUtilities.createService(bopOperationBOMLine.getSession());
                    String datasetName = bopOperationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "/" + bopOperationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                    korSheetDataset = SDVBOPUtilities.createDataset(uploadFilePath);
                    // Dataset 이름 변경
                    korSheetDataset.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, datasetName);
                    bopOperationBOMLine.getItemRevision().add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, korSheetDataset);
                    // SYMTcUtil.importFiles(korSheetDataset, uploadFiles);
                    log.append("\t ~ 작업표준서 Dataset에 파일업로드를 완료하였습니다. - " + korSheetDataset.toDisplayString() + "\n");
                }
                return log.toString();
            }

            /**
             * 국문 작업표준서 Dataset을 검색한다.
             * 
             * @method getKorSheetDatastList
             * @date 2013. 12. 11.
             * @param
             * @return ArrayList<TCComponentDataset>
             * @exception
             * @throws
             * @see
             */
            private ArrayList<TCComponentDataset> getKorSheetDatastList(TCComponentBOMLine operationBOMLine) throws Exception {
                ArrayList<TCComponentDataset> korSheetDatastList = new ArrayList<TCComponentDataset>();
                TCComponent[] comps = operationBOMLine.getItemRevision().getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                if (comps != null) {
                    for (TCComponent comp : comps) {
                        if (comp instanceof TCComponentDataset) {
                            korSheetDatastList.add((TCComponentDataset) comp);
                        }
                    }
                }
                return korSheetDatastList;
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
                int rowStatus = TCData.STATUS_EXECUTE_COMPLETED;
                message = COMPLETED_MESSAGE;
                tcData.setStatus(rowStatus, message);
                logMessage = "{" + tcData.getText() + "} : " + tcData.getStatusMessage();
                return logMessage;
            }
        };

        // [NON-SR][2016.01.07] taeku.jeong PE->TC Interface 수행중 Down되는 문제의 해결을 위해 Thread를 명시적으로 소멸 할 수 있는방법으로 변경함 
        //Thread aExecutionThread = new Thread(aExecutionRun);
        shell.getDisplay().syncExec(aExecutionRun);
        //aExecutionThread.stop();
        //aExecutionThread = null;
        aExecutionRun = null;
        
        if (exception.size() > 0) {
            // SKIP Exception 이면 에러를 Throw하지 않는다.
            if (!(exception.get(0) instanceof SkipException)) {
                throw exception.get(0);
            }
        }
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
        // Class Type log 출력
        logMessage = "<" + tcData.getClassType() + ">\t\t\t" + logMessage;
        // Log 파일 처리..
        try {
            saveLog(logMessage);
        } catch (Exception e) {
            exception.add(new ExecuteSDVException(e.getMessage(), e));
        }
    }

    /**
     * 로그 처리..
     * 
     * @method saveLog
     * @date 2013. 12. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveLog(final TCData tcData, String logMessage) throws Exception {
        // Class Type log 출력
        logMessage = "<" + tcData.getClassType() + ">\t\t\t" + logMessage;
        saveLog(logMessage);
    }

    /**
     * 로그 처리
     * 
     * @method saveLog
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveLog(String logMessage) throws Exception {
        // Log 파일 처리..
        try {
            //ImportCoreService.syncSetItemTextField(shell, getTcDataMigrationJob().getLogText(), logMessage);
        	getTcDataMigrationJob().getLogText().append(logMessage + "\n");
            ImportCoreService.saveLogFile(((PEIFTCDataExecuteJob) getTcDataMigrationJob()).getPeIFJobWork().getLogFilePath(), logMessage);
        } catch (Exception e) {
            throw e;
        }
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
        OperationItemData operationItemData = null;
        for (TCData tcData : expandAllItems) {
            if (tcData instanceof OperationItemData) {
                operationItemData = (OperationItemData) tcData;
            }
        }
        removeNotPeIfBOMLine(operationItemData);
    }

    /**
     * 공법(Operation) 하위 할당정보가 IF 대상에 없는 정보를 삭제한다.
     * 
     * @method removeNotPeIfBOMLine
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void removeNotPeIfBOMLine(OperationItemData operationItemData) throws Exception, TCException {
        HashMap<TCComponentBOMLine, TCComponentBOMLine> deleteBOMList = findNotIFBOMLines(operationItemData);
        if (deleteBOMList.size() == 0) {
            return;
        }
        // Log 등록
        StringBuffer log = new StringBuffer();
        log.append("\n ◎  I/F 대상 제외 공법 하위항목 삭제\n");
        TCComponentMfgBvrBOPLine[] deleteLines = deleteBOMList.keySet().toArray(new TCComponentMfgBvrBOPLine[deleteBOMList.size()]);
        int count = 0;
        for (TCComponentMfgBvrBOPLine bopBOMLine : deleteLines) {
            // I/F 제외대상 BOP BOMLine 삭제
            ArrayList<TCComponentBOMLine> deleteBopLines = new ArrayList<TCComponentBOMLine>();
            deleteBopLines.add(bopBOMLine);
            SDVBOPUtilities.disconnectObjects(bopBOMLine.parent(), deleteBopLines);
            log.append(++count + " : '" + bopBOMLine.toDisplayString() + "' is removed\n");
        }
        // Log 출력
        saveLog(log.toString());
        // BOMLine Refresh
        refreshBOMLine(operationItemData.getBopBomLine());
    }

    /**
     * 공법(Operation) 하위 할당정보가 IF 대상에 없는 정보를 추출한다. (불필요 할당자원 조회)
     * 
     * @method findNotIFBOMLines
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private HashMap<TCComponentBOMLine, TCComponentBOMLine> findNotIFBOMLines(final OperationItemData operationItemData) throws Exception {
        final ArrayList<Exception> exception = new ArrayList<Exception>();
        final HashMap<TCComponentBOMLine, TCComponentBOMLine> notIFBOMLines = new HashMap<TCComponentBOMLine, TCComponentBOMLine>();
        Runnable operationMigratoinThread = new Runnable() {
        	public void run() {
                try {
                    TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(operationItemData.getBopBomLine());
                    for (TCComponentBOMLine tcComponentBOMLine : childs) {
                        notIFBOMLines.put(tcComponentBOMLine, tcComponentBOMLine);
                        TreeItem[] items = operationItemData.getItems();
                        for (TreeItem item : items) {
                            if (item instanceof OccurrenceData) {
                                // 존재하면 삭제
                                if (notIFBOMLines.containsKey((((OccurrenceData) item).getBopBomLine()))) {
                                    notIFBOMLines.remove(((OccurrenceData) item).getBopBomLine());
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    exception.add(e);
                }
        	}
        };
        
        //Thread aThread = new Thread(operationMigratoinThread);
        shell.getDisplay().syncExec(operationMigratoinThread);
        //aThread.stop();
        //aThread = null;
        operationMigratoinThread = null;
        
//        shell.getDisplay().syncExec(new Runnable() {
//            public void run() {
//                try {
//                    TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(operationItemData.getBopBomLine());
//                    for (TCComponentBOMLine tcComponentBOMLine : childs) {
//                        notIFBOMLines.put(tcComponentBOMLine, tcComponentBOMLine);
//                        TreeItem[] items = operationItemData.getItems();
//                        for (TreeItem item : items) {
//                            if (item instanceof OccurrenceData) {
//                                // 존재하면 삭제
//                                if (notIFBOMLines.containsKey((((OccurrenceData) item).getBopBomLine()))) {
//                                    notIFBOMLines.remove(((OccurrenceData) item).getBopBomLine());
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    exception.add(e);
//                }
//            }
//        });
        
        if (exception.size() > 0) {
            // SKIP Exception 이면 에러를 Throw하지 않는다.
            if (!(exception.get(0) instanceof SkipException)) {
                throw exception.get(0);
            }
        }
        return notIFBOMLines;
    }

    /**
     * BOMLine refresh
     * 
     * @method refreshBOMLine
     * @date 2013. 12. 12.
     * @param
     * @exception
     * @return void
     * @throws
     * @see
     */
    private void refreshBOMLine(Object tcComponent) throws Exception {
        // Activity Refresh
        if (tcComponent instanceof TCComponentMECfgLine) {
            TCComponentMECfgLine tcComponentMECfgLine = (TCComponentMECfgLine) tcComponent;
            tcComponentMECfgLine.clearCache();
            tcComponentMECfgLine.window().fireChangeEvent();
            tcComponentMECfgLine.refresh();
        } else if (tcComponent instanceof TCComponentCfgAttachmentLine) {
            TCComponentCfgAttachmentLine tcComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) tcComponent;
            tcComponentCfgAttachmentLine.clearCache();
            tcComponentCfgAttachmentLine.window().fireChangeEvent();
            tcComponentCfgAttachmentLine.refresh();
        }
        // BOMLine Refresh
        else if (tcComponent instanceof TCComponentBOMLine) {
            TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) tcComponent;
            tcComponentBOMLine.clearCache();
            tcComponentBOMLine.refresh();
            tcComponentBOMLine.window().newIrfWhereConfigured(tcComponentBOMLine.getItemRevision());
            tcComponentBOMLine.window().fireComponentChangeEvent();
        }
    }

    /**
     * Activity Window 생성
     * 
     * @method createActivityWindow
     * @date 2013. 12. 31.
     * @param
     * @return TCComponentCfgAttachmentWindow
     * @exception
     * @throws
     * @see
     */
    private TCComponentCfgAttachmentWindow createActivityWindow(TCComponentBOMLine operationBOMLine) throws Exception {
        TCComponentBOMWindow bopBOMWindow = operationBOMLine.window();
        TCComponentCfgAttachmentWindow attachmentWindow = null;
        TCComponentRevisionRule localTCComponentRevisionRule = (TCComponentRevisionRule) (bopBOMWindow).getReferenceProperty("revision_rule");
        TCComponentCfgAttachmentWindowType attachmentWindowType = ((TCComponentCfgAttachmentWindowType) CommonUtils.getSession().getTypeComponent("CfgAttachmentWindow"));
        attachmentWindow = attachmentWindowType.createAttachmentWindow(localTCComponentRevisionRule, bopBOMWindow, true);
        TCPreferenceService localObject2 = CommonUtils.getSession().getPreferenceService();
        TCProperty localObject3 = attachmentWindow.getTCProperty("me_cfg_icm_mode");
        // ((TCProperty) localObject3).setLogicalValue(((TCPreferenceService) localObject2).isTrue(4, "Incremental_Change_Management"));
        Boolean flag = ((TCPreferenceService) localObject2).getLogicalValueAtLocation("Incremental_Change_Management", TCPreferenceLocation.OVERLAY_LOCATION);
        if(flag == null) {
        	flag = false;
        }
        
        ((TCProperty) localObject3).setLogicalValue(flag);
        
        return attachmentWindow;
    }

}
