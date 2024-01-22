/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.export.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.ImportExcelServce;
import com.symc.plm.me.sdv.service.migration.util.PEExcelConstants;
import com.symc.plm.me.sdv.service.migration.work.peif.PEIFJobWork;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

/**
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 * 
 * Class Name : ExportProgressDialog
 * Class Description :
 * 
 * @date 2013. 12. 24.
 * 
 */
public class ExportProgressDialog extends ProgressBarDialog {
    private TCComponentMfgBvrProcess processLine;
    TCComponentBOMLine[] operations;
    String exportFolderPath;
    TCComponentBOMWindow mBOMWindow;
    // Operation
    ArrayList<ArrayList<String>> operationRowBOMList;
    ArrayList<ArrayList<String>> operationRowMasterList;
    ArrayList<TCComponentBOMLine> operationBOMLineList;
    // Activity
    ArrayList<ArrayList<String>> activityRowBOMList;
    ArrayList<ArrayList<String>> activityRowMasterList;
    // EndItem
    ArrayList<ArrayList<String>> endItemRowBOMList;
    // Subsidiary
    ArrayList<ArrayList<String>> subsidiaryRowBOMList;
    // Tool
    ArrayList<ArrayList<String>> toolRowBOMList;
    ArrayList<ArrayList<String>> toolRowMasterList;
    // Equipment
    ArrayList<ArrayList<String>> equipmentRowBOMList;
    ArrayList<ArrayList<String>> equipmentRowMasterList;
    // PW WorkSheet Path
//    private static final String PE_WORKSHEET_PATH = "Z:\\TcM_Interface\\WorkSheet";
    private static final String PE_WORKSHEET_PATH = "X:\\TcM_Interface\\WorkSheet";
    // Excel 파일 확장자
    private static final String EXCEL_FILE_EXT = ".xlsx";

    /**
     * @param parent
     */
    public ExportProgressDialog(Shell parent, TCComponentMfgBvrProcess processLine, String exportFolderPath) {
        super(parent);
        this.processLine = processLine;
        this.exportFolderPath = exportFolderPath;
        // Export Folder 체크 및 생성
        checkExportFolderPath(exportFolderPath);
    }

    /**
     * Export Excel 데이터 초기화
     * 
     * @method initExcelExport
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initExport() throws Exception {
        // Line 하위 공법 리스트
        operations = getChildBOMLines(processLine);
        operationRowBOMList = new ArrayList<ArrayList<String>>();
        operationBOMLineList = new ArrayList<TCComponentBOMLine>();
        operationRowMasterList = new ArrayList<ArrayList<String>>();
        activityRowBOMList = new ArrayList<ArrayList<String>>();
        activityRowMasterList = new ArrayList<ArrayList<String>>();
        endItemRowBOMList = new ArrayList<ArrayList<String>>();
        subsidiaryRowBOMList = new ArrayList<ArrayList<String>>();
        toolRowBOMList = new ArrayList<ArrayList<String>>();
        toolRowMasterList = new ArrayList<ArrayList<String>>();
        equipmentRowBOMList = new ArrayList<ArrayList<String>>();
        equipmentRowMasterList = new ArrayList<ArrayList<String>>();
        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // 현재 BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
        // M Product 윈도우
        // [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
        mBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(bomWindow.getTopBOMLine().getItemRevision());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.work.export.ui.dialog.ProgressBarDialog#initGuage()
     */
    @Override
    public void initGuage() {
        try {
            initExport();
            this.setExecuteTime(operations.length);
            this.setMayCancel(true);
            this.setProcessMessage("please waiting....");
            this.setShellTitle("BOP Excel Export");
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "Export", MessageBox.ERROR);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.work.export.ui.dialog.ProgressBarDialog#process(int)
     */
    @Override
    protected String process(int times) {
        String message = "";
        try {
            message = printMessage(times);
            exportRowOperation(operations[times - 1]);
        } catch (Exception e) {
            e.printStackTrace();
            if (mBOMWindow != null) {
                try {
                    mBOMWindow.close();
                } catch (TCException te) {
                    te.printStackTrace();
                }
            }
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "Export", MessageBox.ERROR);
            // process 중단
            isClosed = true;
        } finally {

        }
        return message;
    }

    /**
     * 
     * @method exportRowOperation
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void exportRowOperation(TCComponentBOMLine operationBOMLine) throws Exception {
        // Operation
        // BOM
        ArrayList<String> operationBOMInfo = getOperationBOMInfo(operationBOMLine);
        operationRowBOMList.add(operationBOMInfo);
        operationBOMLineList.add(operationBOMLine);
        // Master
        operationRowMasterList.add(getOperationMasterInfo(operationBOMLine, operationBOMInfo));
        // Activity
        setActivity(operationBOMLine, operationBOMInfo);
        // 공법 하위 OCCURRENCE BOMLine 조회
        TCComponentBOMLine[] childUnduerOperationBOMLines = getChildBOMLines(operationBOMLine);
        // EndItem
        setEndItem(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
        // Subsidiary
        setSubsidiary(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
        // Tool
        setTool(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
        // Equipment
        setEquipment(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
    }

    /**
     * Operation BOM
     * 
     * @method getOperationBOMInfo
     * @date 2013. 12. 24.
     * @param
     * @return ArrayList<String>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<String> getOperationBOMInfo(TCComponentBOMLine operationBOMLine) throws Exception {
        ArrayList<String> bomInfo = new ArrayList<String>();
        String lineItemId = processLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String operationItemId = operationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] splitIds = operationItemId.split("-");
        bomInfo.add("");// 0 : empty
        bomInfo.add(getPlantCode(lineItemId)); // 1 : Plant
        bomInfo.add(getShopCode(lineItemId)); // 2 : Shop Code
        bomInfo.add(getProductNo(lineItemId)); // 3 : Product No.
        bomInfo.add(getLineCode(lineItemId)); // 4 : Line Code
        // (6)-(7)-(8)-(8)-(5,9)
        bomInfo.add(splitIds[4]); // 5 : 공정편성버젼
        bomInfo.add(splitIds[0]); // 6 : 관리번호-차종
        bomInfo.add(splitIds[1]); // 7 : 관리번호-라인
        bomInfo.add(splitIds[2] + "-" + splitIds[3]); // 8 : 관리번호
        bomInfo.add(splitIds[4]); // 9 : 공정편성버젼
        String peCondition = ImportCoreService.conversionOptionConditionFormTC(operationBOMLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
        bomInfo.add(peCondition); // 10 : OPTION
        return bomInfo;
    }

    /**
     * Operation Master
     * 
     * @method setOperationMasterInfo
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private ArrayList<String> getOperationMasterInfo(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo) throws Exception {
        ArrayList<String> masterInfo = new ArrayList<String>();
        masterInfo.add(""); // 0 : empty
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 1 : 관리번호-차종
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 2 : 관리번호-라인
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 3 : 관리번호
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 4 : 공정편성버젼
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKAREA));// 5 : 작업위치
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));// 6 : 공법명-국문
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));// 7 : 공법명-영문
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE));// 8 : 작업자구분코드
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_ITEM_UL));// 9 : 자재투입위치-상하
        String[] installDwgNoList = operationBOMLine.getItemRevision().getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getStringArrayValue();
        String installDwgNo = "";
        if (installDwgNoList != null) {
            for (int i = 0; i < installDwgNoList.length; i++) {
                installDwgNo = installDwgNo + ((i == 0) ? installDwgNoList[i] : "/" + installDwgNoList[i]);
            }
        }
        masterInfo.add(installDwgNo);// 10 : 장착도면번호
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));// 11 : Station No.
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR));// 12 : 보안 (DR1,DR2,DR3)
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM));// 13 : 시스템
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 14 : 관리번호
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ));// 15 : Sequence
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_WORK_UBODY));// 16 : 하체작업 여부 (N/Y)
        boolean repVhicleCheck = operationBOMLine.getItem().getTCProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK).getBoolValue();
        masterInfo.add((repVhicleCheck == true) ? "Y" : "N");// 17 : 대표차종 유무 (N/Y)
        masterInfo.add(PE_WORKSHEET_PATH + "\\" + operationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID) + ".xlsx");// 18 : 국문작업표준서 파일경로
        masterInfo.add("FALSE");// 19 : 국문작업표준서 I/F 유무
        return masterInfo;
    }

    /**
     * 
     * @method setActivity
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setActivity(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo) throws Exception {
        List<HashMap<String, Object>> activityList = SYMTcUtil.getActivityList((TCComponentMfgBvrOperation) operationBOMLine);
        if (activityList == null || activityList.size() == 0) {
            return;
        }
        for (HashMap<String, Object> activity : activityList) {
            // Activity BOM
            ArrayList<String> activityBomInfo = new ArrayList<String>();
            activityBomInfo.add("");
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : 공정편성버젼
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : 관리번호-차종
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : 관리번호-라인
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : 관리번호
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : 공정편성버젼
            activityBomInfo.add((String) activity.get("SEQ")); // 10 : 작업순서
            activityRowBOMList.add(activityBomInfo);
            // Activity MASTER
            ArrayList<String> activityMasterInfo = new ArrayList<String>();
            activityMasterInfo.add(""); // 0 : empty
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 1 : 관리번호-차종
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 2 : 관리번호-라인
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 3 : 관리번호
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 4 : 공정편성버젼
            activityMasterInfo.add((String) activity.get("SEQ"));// 5 : 작업순서
            String activitysystemCode = (String) activity.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
            String activityMainSystemCode = "";
            String activitySubSystemCode = "";
            if (!StringUtils.isEmpty(activitysystemCode)) {
                String[] activitySplits = activitysystemCode.split("-");
                activityMainSystemCode = activitySplits[0].trim();
                if (activitySplits.length == 2) {
                    activitySubSystemCode = activitySplits[1].trim();
                } else if (activitySplits.length == 3) {
                    activitySubSystemCode = activitySplits[1].trim() + "," + activitySplits[2].trim();
                }
            }
            activityMasterInfo.add(activityMainSystemCode); // 6 : 작업약어
            activityMasterInfo.add(activitySubSystemCode); // 7 : 변수
            activityMasterInfo.add(activity.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).toString()); // 8 : 난이도
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ITEM_OBJECT_NAME)); // 9 : 작업내용(국문)
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ACTIVITY_ENG_NAME)); // 10 : 작업내용(영문)
            activityMasterInfo.add(activity.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).toString()); // 11 : 작업시간
            String activitySystemCategory = (String) activity.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
            // '작업자정미' -> '정미'로 변환 (나머지 자동/보조는 TC - PE간 같음)
            if ("작업자정미".equals(activitySystemCategory)) {
                activitySystemCategory = "정미";
            }
            activityMasterInfo.add(activitySystemCategory); // 12 : 자동/정미/보조
            TCComponentBOMLine[] tools = (TCComponentBOMLine[]) activity.get(SDVPropertyConstant.ACTIVITY_TOOL_LIST);
            String tooIds = "";
            if (tools != null) {
                for (TCComponentBOMLine tcComponentBOMLine : tools) {
                    if ("".equals(tooIds)) {
                        tooIds = tcComponentBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    } else {
                        tooIds += "," + tcComponentBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    }
                }
            }
            activityMasterInfo.add(tooIds); // 13 : 공구ID
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ACTIVITY_CONTROL_POINT)); // 14 : KPC
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS)); // 15 : KPC관리기준
            activityRowMasterList.add(activityMasterInfo);
        }
    }

    /**
     * 
     * @method setEndItem
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setEndItem(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.EBOM_STD_PART.equals(childBOMLine.getItem().getType()) || SDVTypeConstant.EBOM_VEH_PART.equals(childBOMLine.getItem().getType())) {
                ArrayList<String> endItemBomInfo = new ArrayList<String>();
                endItemBomInfo.add("");
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : 공정편성버젼
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : 관리번호-차종
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : 관리번호-라인
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : 관리번호
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : 공정편성버젼
                endItemBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : Part No.
                StringBuffer eBomAbsOoccUid = new StringBuffer();
                String eBomOccUid = "";
                String eBomFunctionItemId = "";
                TCComponentBOMLine[] findBOPEndItemBOMLineList = SDVBOPUtilities.getAssignSrcBomLineList(mBOMWindow, childBOMLine);
                // E-BOM ABS OCC PUID를 알아낸다.
                if (findBOPEndItemBOMLineList.length > 0) {
                    eBomOccUid = findBOPEndItemBOMLineList[0].getProperty("bl_occurrence_uid");
                    eBomAbsOoccUid.append(eBomOccUid);
                    TCComponentBOMLine parentBOMLine = findBOPEndItemBOMLineList[0].parent();
                    while (true) {
                        if (parentBOMLine == null) {
                            // clear
                            eBomAbsOoccUid.delete(0, eBomAbsOoccUid.length());
                            break;
                        }
                        // E-BOMfunction Item을 알아낸다.
                        if (SDVTypeConstant.EBOM_FUNCTION.equals(parentBOMLine.getItem().getType())) {
                            eBomFunctionItemId = parentBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                            break;
                        }
                        String parentOccUid = parentBOMLine.getProperty("bl_occurrence_uid");
                        eBomAbsOoccUid.insert(0, "+");
                        eBomAbsOoccUid.insert(0, parentOccUid);
                        // set parent
                        parentBOMLine = parentBOMLine.parent();
                    }
                }
                endItemBomInfo.add(eBomAbsOoccUid.toString()); // 11 : EBOM ABS Occurrence PUID
                endItemBomInfo.add(eBomOccUid);// 12 : EBOM Occurrence PUID
                endItemBomInfo.add(eBomFunctionItemId); // 13 : Function Part Number
                endItemBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));// 14 : 자재SEQ
                endItemRowBOMList.add(endItemBomInfo);
            }
        }
    }

    /**
     * 
     * @method setSubsidiary
     * @date 2013. 12. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setSubsidiary(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(childBOMLine.getItem().getType())) {
                ArrayList<String> subsidiaryBomInfo = new ArrayList<String>();
                subsidiaryBomInfo.add("");
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : 공정편성버젼
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : 관리번호-차종
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : 관리번호-라인
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : 관리번호
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : 공정편성버젼
                subsidiaryBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : 부품번호
                String peCondition = ImportCoreService.conversionOptionConditionFormTC(childBOMLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
                subsidiaryBomInfo.add(peCondition);// 11 : OPTION
                subsidiaryBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY).toString());// 12 : 소요량
                subsidiaryBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT));// 13 : 조구분
                subsidiaryBomInfo.add(ImportCoreService.conversionSubsidiaryFindNoToTc(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO)));// 14 : 자재SEQ
                subsidiaryRowBOMList.add(subsidiaryBomInfo);
            }
        }
    }

    /**
     * 
     * @method setTool
     * @date 2013. 12. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setTool(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(childBOMLine.getItem().getType())) {
                // BOM
                ArrayList<String> toolBomInfo = new ArrayList<String>();
                toolBomInfo.add("");
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : 공정편성버젼
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : 관리번호-차종
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : 관리번호-라인
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : 관리번호
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : 공정편성버젼
                toolBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : 공구번호
                toolBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY).toString());// 11 : 수량
                // 12 : Torque = TorqueType + TorqueValue
                String torque = "";
                String torqueType = BundleUtil.nullToString(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE));
                String torqueValue = BundleUtil.nullToString(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE));
                torque = torqueType + " " + torqueValue;
                torque = torque.trim();
                toolBomInfo.add(torque);// 12 : Torque
                toolBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));// 13 : 설비SEQ
                toolRowBOMList.add(toolBomInfo);
                // MASTER
                // TODO : MASTER 설정...
            }
        }
    }

    /**
     * 
     * @method setEquipment
     * @date 2013. 12. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setEquipment(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM.equals(childBOMLine.getItem().getType()) || SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(childBOMLine.getItem().getType())) {
                // BOM
                ArrayList<String> equipmentBomInfo = new ArrayList<String>();
                equipmentBomInfo.add("");
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : 공정편성버젼
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : 관리번호-차종
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : 관리번호-라인
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : 관리번호
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : 공정편성버젼
                equipmentBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : 설비번호
                equipmentBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY).toString());// 11 : 설비 수량
                equipmentBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));// 12 : SEQ
                equipmentRowBOMList.add(equipmentBomInfo);
                // MASTER
                // TODO : MASTER 설정...
            }
        }
    }

    /**
     * 
     * @method printMessage
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String printMessage(int times) throws Exception {
        String message = "[" + times + "/" + operations.length + "] ";
        message += operations[times - 1].getProperty(SDVPropertyConstant.BL_ITEM_ID) + "\t" + operations[times - 1].getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
        return message;
    }

    /**
     * 
     * @method createExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createExcel() throws Exception {
        createOperationBOMExcel();
        createOperationMasterExcel();
        createActivityBOMExcel();
        createActivityMasterExcel();
        createEndItemBOMExcel();
        createSubsidiaryBOMExcel();
        createToolBOMExcel();
        createToolMasterExcel();
        createEquipmentBOMExcel();
        createEquipmentMasterExcel();
    }

    /**
     * 
     * @method createOperationBOMExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createOperationBOMExcel() throws Exception {
        ArrayList<String> operationBOMHeader = new ArrayList<String>();
        operationBOMHeader.add("");
        operationBOMHeader.add("Plant");
        operationBOMHeader.add("Shop Code");
        operationBOMHeader.add("Product No.");
        operationBOMHeader.add("Line Code.");
        operationBOMHeader.add("공정편성버젼");
        operationBOMHeader.add("관리번호-차종");
        operationBOMHeader.add("관리번호-라인");
        operationBOMHeader.add("관리번호");
        operationBOMHeader.add("공정편성버젼\n");
        operationBOMHeader.add("OPTION");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_OPERATION[2] + EXCEL_FILE_EXT, operationBOMHeader, operationRowBOMList);
    }

    /**
     * 
     * @method createOperationMasterExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createOperationMasterExcel() throws Exception {
        ArrayList<String> operationMasterHeader = new ArrayList<String>();
        operationMasterHeader.add("");
        operationMasterHeader.add("관리번호-차종");
        operationMasterHeader.add("관리번호-라인");
        operationMasterHeader.add("관리번호");
        operationMasterHeader.add("공정편성버젼");
        operationMasterHeader.add("작업위치");
        operationMasterHeader.add("공법명-국문");
        operationMasterHeader.add("공법명-영문");
        operationMasterHeader.add("작업자구분코드");
        operationMasterHeader.add("자재투입위치-상하");
        operationMasterHeader.add("장착도면번호");
        operationMasterHeader.add("Station No.");
        operationMasterHeader.add("보안 (DR1,DR2,DR3)");
        operationMasterHeader.add("시스템");
        operationMasterHeader.add("관리번호");
        operationMasterHeader.add("Sequence");
        operationMasterHeader.add("하체작업 여부 (N/Y)");
        operationMasterHeader.add("대표차종 유무 (N/Y)");
        operationMasterHeader.add("국문작업표준서 파일경로");
        operationMasterHeader.add("국문작업표준서 I/F 유무");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_OPERATION[1] + EXCEL_FILE_EXT, operationMasterHeader, operationRowMasterList);
    }

    /**
     * 
     * @method createActivityBOMExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createActivityBOMExcel() throws Exception {
        ArrayList<String> activityBOMHeader = new ArrayList<String>();
        activityBOMHeader.add("");
        activityBOMHeader.add("Plant");
        activityBOMHeader.add("Shop Code");
        activityBOMHeader.add("Product No.");
        activityBOMHeader.add("Line Code");
        activityBOMHeader.add("공정편성버젼");
        activityBOMHeader.add("관리번호-차종");
        activityBOMHeader.add("관리번호-라인");
        activityBOMHeader.add("관리번호");
        activityBOMHeader.add("공정편성버젼");
        activityBOMHeader.add("작업순서");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_ACTIVITY[2] + EXCEL_FILE_EXT, activityBOMHeader, activityRowBOMList);
    }

    /**
     * 
     * @method createActivityMasterExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createActivityMasterExcel() throws Exception {
        ArrayList<String> activityMasterHeader = new ArrayList<String>();
        activityMasterHeader.add("");
        activityMasterHeader.add("관리번호-차종");
        activityMasterHeader.add("관리번호-라인");
        activityMasterHeader.add("관리번호");
        activityMasterHeader.add("공정편성버젼");
        activityMasterHeader.add("작업순서");
        activityMasterHeader.add("작업약어");
        activityMasterHeader.add("변수");
        activityMasterHeader.add("난이도");
        activityMasterHeader.add("작업내용(국문)");
        activityMasterHeader.add("작업내용(영문)");
        activityMasterHeader.add("작업시간");
        activityMasterHeader.add("자동/정미/보조");
        activityMasterHeader.add("공구ID");
        activityMasterHeader.add("KPC");
        activityMasterHeader.add("KPC관리기준");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_ACTIVITY[1] + EXCEL_FILE_EXT, activityMasterHeader, activityRowMasterList);
    }

    /**
     * 
     * @method createEndItemBOMExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createEndItemBOMExcel() throws Exception {
        ArrayList<String> endItemBOMHeader = new ArrayList<String>();
        endItemBOMHeader.add("");
        endItemBOMHeader.add("Plant");
        endItemBOMHeader.add("Shop Code");
        endItemBOMHeader.add("Product No.");
        endItemBOMHeader.add("Line Code");
        endItemBOMHeader.add("공정편성버젼");
        endItemBOMHeader.add("관리번호-차종");
        endItemBOMHeader.add("관리번호-라인");
        endItemBOMHeader.add("관리번호");
        endItemBOMHeader.add("공정편성버젼");
        endItemBOMHeader.add("Part NO.");
        endItemBOMHeader.add("EBOM ABS Occurrence PUID");
        endItemBOMHeader.add("EBOM Occurrence PUID");
        endItemBOMHeader.add("Function Part Number");
        endItemBOMHeader.add("자재SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_END_ITEM[2] + EXCEL_FILE_EXT, endItemBOMHeader, endItemRowBOMList);
    }

    /**
     * 
     * @method createSubsidiaryBOMExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createSubsidiaryBOMExcel() throws Exception {
        ArrayList<String> subsidiaryBOMHeader = new ArrayList<String>();
        subsidiaryBOMHeader.add("");
        subsidiaryBOMHeader.add("Plant");
        subsidiaryBOMHeader.add("Shop Code");
        subsidiaryBOMHeader.add("Product No.");
        subsidiaryBOMHeader.add("Line Code");
        subsidiaryBOMHeader.add("공정편성버젼");
        subsidiaryBOMHeader.add("관리번호-차종");
        subsidiaryBOMHeader.add("관리번호-라인");
        subsidiaryBOMHeader.add("관리번호");
        subsidiaryBOMHeader.add("공정편성버젼");
        subsidiaryBOMHeader.add("부품번호");
        subsidiaryBOMHeader.add("OPTION");
        subsidiaryBOMHeader.add("소요량");
        subsidiaryBOMHeader.add("조구분");
        subsidiaryBOMHeader.add("자재SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_SUBSIDIARY[2] + EXCEL_FILE_EXT, subsidiaryBOMHeader, subsidiaryRowBOMList);
    }

    /**
     * 
     * @method createToolBOMExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createToolBOMExcel() throws Exception {
        ArrayList<String> toolBOMHeader = new ArrayList<String>();
        toolBOMHeader.add("");
        toolBOMHeader.add("Plant");
        toolBOMHeader.add("Shop Code");
        toolBOMHeader.add("Product No.");
        toolBOMHeader.add("Line Code");
        toolBOMHeader.add("공정편성버젼");
        toolBOMHeader.add("관리번호-차종");
        toolBOMHeader.add("관리번호-라인");
        toolBOMHeader.add("관리번호");
        toolBOMHeader.add("공정편성버젼");
        toolBOMHeader.add("공구번호");
        toolBOMHeader.add("수량");
        toolBOMHeader.add("Torque");
        toolBOMHeader.add("설비SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_TOOL[2] + EXCEL_FILE_EXT, toolBOMHeader, toolRowBOMList);
    }

    /**
     * 
     * @method createToolMasterExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createToolMasterExcel() throws Exception {
        ArrayList<String> toolMasterHeader = new ArrayList<String>();
        toolMasterHeader.add("");
        toolMasterHeader.add("공구번호");
        toolMasterHeader.add("공구명-국문");
        toolMasterHeader.add("공구명-영문");
        toolMasterHeader.add("대분류");
        toolMasterHeader.add("중분류");
        toolMasterHeader.add("공구 용도");
        toolMasterHeader.add("사양코드");
        toolMasterHeader.add("기술 사양-국문");
        toolMasterHeader.add("기술 사양-영문");
        toolMasterHeader.add("소요량 단위");
        toolMasterHeader.add("공구 재질");
        toolMasterHeader.add("토크값");
        toolMasterHeader.add("제작사");
        toolMasterHeader.add("업체/AF");
        toolMasterHeader.add("형상분류");
        toolMasterHeader.add("길이");
        toolMasterHeader.add("연결부 Size");
        toolMasterHeader.add("자석삽입여부");
        toolMasterHeader.add("Remark");
        toolMasterHeader.add("CAD파일경로");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_TOOL[1] + EXCEL_FILE_EXT, toolMasterHeader, toolRowMasterList);
    }

    /**
     * 
     * @method createEquipmentBOMExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createEquipmentBOMExcel() throws Exception {
        ArrayList<String> equipmentBOMHeader = new ArrayList<String>();
        equipmentBOMHeader.add("");
        equipmentBOMHeader.add("Plant");
        equipmentBOMHeader.add("Shop Code");
        equipmentBOMHeader.add("Product No.");
        equipmentBOMHeader.add("Line Code");
        equipmentBOMHeader.add("공정편성버젼");
        equipmentBOMHeader.add("관리번호-차종");
        equipmentBOMHeader.add("관리번호-라인");
        equipmentBOMHeader.add("관리번호");
        equipmentBOMHeader.add("공정편성버젼");
        equipmentBOMHeader.add("설비번호");
        equipmentBOMHeader.add("설비 수량");
        equipmentBOMHeader.add("SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_EQUIPMENT[2] + EXCEL_FILE_EXT, equipmentBOMHeader, equipmentRowBOMList);
    }

    /**
     * 
     * @method createEquipmentMasterExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createEquipmentMasterExcel() throws Exception {
        ArrayList<String> equipmentMasterHeader = new ArrayList<String>();
        equipmentMasterHeader.add("");
        equipmentMasterHeader.add("공장");
        equipmentMasterHeader.add("설비번호");
        equipmentMasterHeader.add("대분류");
        equipmentMasterHeader.add("중분류");
        equipmentMasterHeader.add("설비 사양-국문");
        equipmentMasterHeader.add("설비 사양-영문");
        equipmentMasterHeader.add("Main Name(국문)");
        equipmentMasterHeader.add("Main Name(영문)");
        equipmentMasterHeader.add("처리능력");
        equipmentMasterHeader.add("제작사");
        equipmentMasterHeader.add("도입국가");
        equipmentMasterHeader.add("설치년도");
        equipmentMasterHeader.add("사용 용도-국문");
        equipmentMasterHeader.add("사용 용도-영문");
        equipmentMasterHeader.add("변경내역문자");
        equipmentMasterHeader.add("차종코드");
        equipmentMasterHeader.add("CAD파일경로");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_EQUIPMENT[1] + EXCEL_FILE_EXT, equipmentMasterHeader, equipmentRowMasterList);
    }

    /**
     * 종료 처리
     */
    @Override
    protected void doAfter() {
        try {
            createExcel();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "Export", MessageBox.ERROR);
            // process 중단
            isClosed = true;
        } finally {
            if (mBOMWindow != null) {
                try {
                    mBOMWindow.close();
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Cancel Cleanup
     * 
     * @see com.symc.plm.me.sdv.service.migration.work.export.ui.dialog.ProgressBarDialog#cleanUp()
     */
    @Override
    protected void cleanUp() {
        if (mBOMWindow != null) {
            try {
                mBOMWindow.close();
            } catch (TCException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Find BOM Child
     * 
     * @method getChildBOMLines
     * @date 2013. 12. 26.
     * @param
     * @return TCComponentBOMLine[]
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine[] getChildBOMLines(TCComponentBOMLine bomLine) throws Exception {
        AIFComponentContext contexts[] = bomLine.getChildren();
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            childLines[i] = (TCComponentBOMLine) contexts[i].getComponent();
        }
        return childLines;
    }

    /**
     * LINE Item ID를 가지고 PLANT Code를 알아낸다.
     * 
     * @method getPlantCode
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getPlantCode(String lineItemId) {
        return lineItemId.split("-")[0];
    }

    /**
     * LINE Item ID를 가지고 SHOP Code를 알아낸다.
     * 
     * @method getShopCode
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getShopCode(String lineItemId) {
        return lineItemId.split("-")[1];
    }

    /**
     * LINE Item ID를 가지고 LINE Code를 알아낸다.
     * 
     * @method getLineCode
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getLineCode(String lineItemId) {
        return lineItemId.split("-")[2];
    }

    /**
     * LINE Item ID를 가지고 ProductNo를 알아낸다.
     * 
     * @method getProductNo
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getProductNo(String lineItemId) {
        return lineItemId.split("-")[3];
    }

    /**
     * Export Folder 체크 - Folder가 존재하지않으면 생성
     * 
     * @method checkExportFolderPath
     * @date 2014. 1. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkExportFolderPath(String folderPath) {
        File folderFile = new File(folderPath);
        // Folder가 없으면 생성한다.
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
    }
}
