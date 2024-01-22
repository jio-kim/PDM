/**
 * 
 */
package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : LineBalanceSheetAssemblyOperation
 * Class Description : 조립 공정 편성표 정보를 추출하는 클래스
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 12. 16.
 * 
 */
public class LineBalanceSheetAssemblyOperation extends SimpleSDVExcelOperation {

    private Registry registry = null;
    private boolean isWorkMaxTime = false;
    private TCComponentBOMLine selectedBOMLine = null; // 선택된 BOMLine

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation#getData()
     */
    @Override
    protected IDataSet getData() throws Exception {

        int mainStationCount = 0, subStationCount = 0; // 메인 공정 수량, 서브 공정 수량
        int mainStationWorkerCount = 0, subStationWorkerCount = 0; // 메인 공정 작업자 수량, 서브 공정 작업자 수량
        double mainStationTotalTime = 0, mainStationTotalRate = 0; // 메인 공정 작업시간 합계, 메인 공정 편성률 합계
        double subStationTotalTime = 0, subStationTotalRate = 0; // 서브 공정 작업시간 합계, 서브 공정 편성률 합계
        ArrayList<String> stationDupCheck = new ArrayList<String>(); // 중복된 공정 체크
        ArrayList<String> workCodeDupCheck = new ArrayList<String>(); // 중복된 작업자 코드 체크

        TCComponentBOMLine shopBOPLine = selectedBOMLine.window().getTopBOMLine();
        TCComponentItemRevision shopItemRevision = shopBOPLine.getItemRevision();
        String lineName = selectedBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
        String lineRevId = selectedBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);

        String productCode = shopItemRevision.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
        TCComponentPerson personComp = (TCComponentPerson) SDVBOPUtilities.getTCSession().getUser().getReferenceProperty("person");
        String loginGroup = personComp.getProperty(TCComponentPerson.PROP_PA6);

        //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
        double jph = shopItemRevision.getTCProperty(SDVPropertyConstant.SHOP_REV_JPH).getDoubleValue();
        double tackTime = 3600 / jph; // Tack Time

        String baseOnPrint = isWorkMaxTime ? "최대 작업 시간 기준" : "대표 공법 기준";

        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();

        // 공정 편성표 Row Data 정보
        LinkedHashMap<String, RowDataBean> rowDataMap = new LinkedHashMap<String, RowDataBean>();
        // 공법별 공정정보 Row Data 리스트를 만듬
        getOperationList(selectedBOMLine, rowDataMap);
        // 공정 정보 Row 리스트 형태로 만듬
        ArrayList<RowDataBean> assyLineRowList = new ArrayList<RowDataBean>(rowDataMap.values());

        // 공정 합계 정보를 구성함
        for (String key : rowDataMap.keySet()) {
            RowDataBean rowData = rowDataMap.get(key);
            String stationNo = rowData.getStationNo();
            String workCode = rowData.getWorkerCode();
            double workTime = rowData.getMaxTime();
            double rate = (workTime / tackTime) * 100;
            // 메인,서브 별 공정 수, 공정시간 합산, 공정 편성 시간 합산
            if (!stationDupCheck.contains(stationNo)) {
                stationDupCheck.add(stationNo);
                // Sub 공정 일 경우
                if (stationNo.substring(1, 1).equals("S")) {
                    subStationCount++;
                    subStationTotalTime += workTime;
                    subStationTotalRate += rate;
                } else {
                    mainStationCount++;
                    mainStationTotalTime += workTime;
                    mainStationTotalRate += rate;
                }
            }
            // 작업자 수
            if (!workCodeDupCheck.contains(workCode)) {
                if (stationNo.substring(1, 1).equals("S"))
                    subStationWorkerCount++;
                else
                    mainStationWorkerCount++;
                workCodeDupCheck.add(workCode);
            }
        }

        // 작업그룹 순으로 Sort
        Collections.sort(assyLineRowList, new RowDataComparator());

        //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
        dataMap.put(SDVPropertyConstant.SHOP_REV_JPH, jph, IData.STRING_FIELD); // JPH
        dataMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, productCode); // Product Code
        dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, lineName); // Line 명
        dataMap.put(SDVPropertyConstant.ITEM_REVISION_ID, lineRevId); // Line Revision ID

        dataMap.put("MAIN_STAION_COUNT", mainStationCount, IData.INTEGER_FIELD); // 메인 공정 수량
        dataMap.put("MAIN_STATION_TOTAL_TIME", mainStationTotalTime, IData.OBJECT_FIELD); // 메인 공정 시간 합계
        dataMap.put("MAIN_STATION_TOTAL_RATE", mainStationTotalRate, IData.OBJECT_FIELD); // 메인 공정 편성률 합계

        dataMap.put("SUB_STAION_COUNT", subStationCount, IData.INTEGER_FIELD); // 서브 공정 수량
        dataMap.put("SUB_STATION_TOTAL_TIME", subStationTotalTime, IData.OBJECT_FIELD); // 서브 공정 시간 합계
        dataMap.put("SUB_STATION_TOTAL_RATE", subStationTotalRate, IData.OBJECT_FIELD); // 서브 공정 편성률 합계

        dataMap.put("MAIN_STAION_WORKER_COUNT", mainStationWorkerCount, IData.INTEGER_FIELD); // 메인 작업자 수
        dataMap.put("SUB_STAION_WORKER_COUNT", subStationWorkerCount, IData.INTEGER_FIELD); // 서브 작업자 수

        dataMap.put("STATION_ROW_LIST", assyLineRowList, IData.OBJECT_FIELD); // Row 별 공정정보 리스트

        dataMap.put("BASED_ON_PRINT", baseOnPrint); // 출력 기준
        dataMap.put("LOGIN_GROUP", loginGroup); // 작성 부서

        dataSet.addDataMap("ASSY_LINE_DATA", dataMap);
        return dataSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        registry = Registry.getRegistry(this);

        isWorkMaxTime = (Boolean) localDataMap.get(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK).getValue();

        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        selectedBOMLine = mfgApp.getSelectedBOMLines()[0]; // 선택된 BOMLINE

        IDataSet dataSet = getData();

        String defaultFileName = registry.getString("exportLineBalancingAssembly.FileName") + "_" + ExcelTemplateHelper.getToday("yyMMdd");
        transformer.print(mode, templatePreference, defaultFileName, dataSet);

    }

    /**
     * 공법별 공정 정보를 가져옴
     * 
     * @method getOperationList
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getOperationList(TCComponentBOMLine parentBOMLine, LinkedHashMap<String, RowDataBean> rowDataMap) throws Exception {

        AIFComponentContext[] aifContexts = parentBOMLine.getChildren();
        for (AIFComponentContext aifcomp : aifContexts) {
            TCComponentBOMLine childBOMLine = (TCComponentBOMLine) aifcomp.getComponent();
            String childItemType = childBOMLine.getItem().getType();
            if (!childItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM))
                continue;
            getOpRowData(childBOMLine, rowDataMap);
        }
    }

    /**
     * Operation 정보를 저장함
     * 
     * @method getOpRowData
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getOpRowData(TCComponentBOMLine opBOMLine, LinkedHashMap<String, RowDataBean> rowDataMap) throws Exception {
        TCComponentItem opItem = opBOMLine.getItem();
        TCComponentItemRevision opRevision = opBOMLine.getItemRevision();

        boolean isMaxWorkTime = opItem.getLogicalProperty(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK);// 최대작업시간 유무
        boolean isRepVehicle = opItem.getLogicalProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK); // 대표 차종 유무

        String opName = opItem.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME); // 공법명
        String workerCode = opItem.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE); // 작업자 코드
        String stationNo = opRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);// 공정 번호

        // 최대 작업 시간,
        if (isWorkMaxTime) {
            if (!isMaxWorkTime)
                return;
        } else {
            if (!isRepVehicle)
                return;
        }

        // 작업자 코드, 공정 코드 가 없으면 Pass
        if (workerCode.isEmpty() || stationNo.isEmpty())
            return;

        // 작업시간 합을 가져옴
        double workTime = getSumWorkTime(opBOMLine);

        RowDataBean rowDataValue = new RowDataBean(stationNo, workerCode, opName, workTime);
        if (!rowDataMap.containsKey(rowDataValue.toString())) {
            rowDataMap.put(rowDataValue.toString(), rowDataValue);
            // 중복된 공정코드, 작업자 코드 조합이 있으면
        } else {
            rowDataValue = rowDataMap.get(rowDataValue.toString());
            double maxTime = rowDataValue.getMaxTime();
            double currentTime = rowDataValue.getCurrentTime();

            // 최대 시간일 경우 대표 공법명을 저장함
            if (workTime > currentTime) {
                rowDataValue.setOpName(opName);
                rowDataValue.setCurrentTime(workTime);
            }
            // (공정코드 ,작업자 코드) 별로 시간을 더하여 저장함
            workTime += maxTime;
            rowDataValue.setMaxTime(workTime);
        }

    }

    // 공정번호, 작업인원, 작업시간, 대표 작업명

    /**
     * 공법의 작업시간을 가져옴
     * 
     * @method getWorkTime
     * @date 2013. 12. 17.
     * @param
     * @return double
     * @exception
     * @throws
     * @see
     */
    private double getSumWorkTime(TCComponentBOMLine opBOMLine) throws Exception {

        double sumWorkTime = 0;
        TCComponent actRootComp = opBOMLine.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);

        if (actRootComp == null || !(actRootComp instanceof TCComponentCfgActivityLine))
            return sumWorkTime;

        TCComponentMEActivity rootActivity = (TCComponentMEActivity) actRootComp.getUnderlyingComponent();
        TCComponent[] activities = ActivityUtils.getSortedActivityChildren(rootActivity);
        for (TCComponent activity : activities) {
            activity.refresh();
            double systemUnitTime = activity.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
            double frequency = activity.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
            sumWorkTime += (systemUnitTime * frequency);
        }

        return sumWorkTime;
    }

    /**
     * 공정코드, 작업자코드별 Row Data 를 저장하는 Data 클래스
     * Class Name : RowDataBean
     * Class Description :
     * 
     * @date 2013. 12. 17.
     * 
     */
    public class RowDataBean {
        private String opName = "";
        private String stationNo = "";
        private String workerCode = "";
        private double maxTime = 0;
        private double currentTime = 0;

        public RowDataBean(String stationNo, String workerCode, String opName, double currentTime) {
            this.stationNo = stationNo;
            this.workerCode = workerCode;
            this.opName = opName;
            this.currentTime = currentTime;
            this.maxTime = currentTime;
        }

        /**
         * @return the opName
         */
        public String getOpName() {
            return opName;
        }

        /**
         * @param opName
         *            the opName to set
         */
        public void setOpName(String opName) {
            this.opName = opName;
        }

        /**
         * @return the stationNo
         */
        public String getStationNo() {
            return stationNo;
        }

        /**
         * @param stationNo
         *            the stationNo to set
         */
        public void setStationNo(String stationNo) {
            this.stationNo = stationNo;
        }

        /**
         * @return the workerCode
         */
        public String getWorkerCode() {
            return workerCode;
        }

        /**
         * @param workerCode
         *            the workerCode to set
         */
        public void setWorkerCode(String workerCode) {
            this.workerCode = workerCode;
        }

        /**
         * @return the maxTime
         */
        public double getMaxTime() {
            return maxTime;
        }

        /**
         * @param maxTime
         *            the maxTime to set
         */
        public void setMaxTime(double maxTime) {
            this.maxTime = maxTime;
        }

        /**
         * @return the currentTime
         */
        public double getCurrentTime() {
            return currentTime;
        }

        /**
         * @param currentTime
         *            the currentTime to set
         */
        public void setCurrentTime(double currentTime) {
            this.currentTime = currentTime;
        }

        public String toString() {
            return stationNo + ";" + workerCode;
        }
    }

    /**
     * Row Data 작업자 코드 순으로 Sort
     * Class Name : RowDataComparator
     * Class Description :
     * 
     * @date 2013. 12. 23.
     * 
     */
    public class RowDataComparator implements Comparator<RowDataBean> {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(RowDataBean paramT1, RowDataBean paramT2) {
            String workerCode1 = "", workerCode2 = "";
            workerCode1 = paramT1.getWorkerCode();
            workerCode2 = paramT2.getWorkerCode();
            return workerCode1.compareTo(workerCode2);
        }
    }
}
