/**
 *
 */
package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawData;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;
import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VectorHelper;

/**
 * Class Name : LineBalanceSheet4BodyOperation
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 10. 29.
 *
 */
public class LineBalanceSheet4BodyOperation extends SimpleSDVExcelOperation {
    Registry registry;
    TCSession session;
    Shell parentShell;
    TCComponentBOPLine []targetBOPLines;

    /**
     * 작업자별 정미시간과 불가피 대기시간 클레스
     *
     * Class Name : WorkerTimeInfo
     * Class Description :
     * @date 2013. 10. 31.
     *
     */
    class WorkerTimeInfo {
        // 작업자 코드
        public String workerCode;
        // 작업자 작업시간
        public double workTime;
        // 작업자 대기 시간
        public double waitTime;
    }

    /**
     * 공정하위 최다작업시간 공법을 찾아 리턴하기 위해 필요한 클레스
     * (정미시간 및 불가피 대기시간 등을 다시 찾지 않기 위해 찾은 결과 값을 리턴받기 위해 사용함)
     *
     * Class Name : MaxTimeOpInfo
     * Class Description :
     *
     * @date 2013. 10. 17.
     *
     */
    class MaxTimeOpInfo {
        // 공정하위 Activity의 정미/불가피 대기시간의 총합이 가장 긴 공법
        public TCComponentBOPLine opLine;
        // 정미시간의 총합
        public double maxUserWorkTime;
        // 불가피 대기시간의 총합
        public double maxUserWaitTime;
        // 작업자 코드 리스트
        public ArrayList<WorkerTimeInfo> workerList;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            // MPP에서 선택한 대상 및 초기 정보 로드
            initOperation();

            // 선택한 BOP 아이템 하위 모든 공정의 최대시간 공법 조회 및 액셀파일로 저장할 정보 처리
            IDataSet dataList = getData();

            // 저장위치 및 템플릿에서 파일 다운로드
            String defaultFileName = registry.getString("exportLineBalacingBody.FileName", "LineBalancingListBody") + "_" + ExcelTemplateHelper.getToday("yyMMdd");
            transformer.print(ExcelTemplateHelper.EXCEL_SAVE, getTemplatePreference(), defaultFileName, dataList);

            setExecuteResult(SUCCESS);
        } catch(Exception e) {
        	setErrorMessage(e.getMessage());
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            //setErrorMessage("");
        }
    }

    /* (non-Javadoc)
     * @see com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation#getData()
     */
    @Override
    protected IDataSet getData() throws TCException {
        // MPP에서 선택한 값 가져오기 및 체크
        // 하위 공정들의 공법 정보 가져오기

        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String,Object>>();
        ArrayList<MaxTimeOpInfo> maxTimeOPForStationList = new ArrayList<MaxTimeOpInfo>();;
        IDataSet dataSet = null;
        try
        {
            // 공정 하위 공법들을 검색하여 작업시간이 제일 긴 공법리스트를 발췌한다.
            getStationList(targetBOPLines, maxTimeOPForStationList);

            // 발췌한 공법 리스트를 액셀로 출력하기 위한 형태로 데이타를 변환한다.
            setExportDataInfo(maxTimeOPForStationList, dataList);

            // 데이타를 다시 변환
            dataSet = new DataSet();
            IDataMap dataMap = new RawDataMap();
            dataMap.put(registry.getString("BODYIDataBodyName"), dataList, IData.TABLE_FIELD);
            dataSet.addDataMap(registry.getString("BODYIDataBodyName"), dataMap);

            if (targetBOPLines != null && targetBOPLines.length > 0)
            {
                // 데이타의 헤드정보 설정 Shop JPH, Product Code
                IDataMap headMap = new RawDataMap();
                
                //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
                TCComponentBOPLine shopLine = (TCComponentBOPLine) targetBOPLines[0].window().getTopBOMLine();
                String str_shop_jph = shopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_JPH);
                double shopJPH = 0.0;
                if (str_shop_jph == null || str_shop_jph.trim().length() == 0)
                    shopJPH = 0.0;
                else
                    shopJPH = Double.parseDouble(str_shop_jph);

                String prodCode = shopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);


                IData jphData = new RawData();
                jphData.setValue(shopJPH);
                headMap.put(SDVPropertyConstant.SHOP_REV_JPH, jphData);

                IData prodCodeData = new RawData();
                prodCodeData.setValue(prodCode);
                headMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, prodCodeData);

                dataSet.addDataMap(registry.getString("BODYIDataHeadName"), headMap);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            throw new TCException(ex);
        }

        return dataSet;
    }

    /**
     * 액셀로 데이타를 출력하기 위한 정보 처리 함수
     * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
     *
     * @method setExportDataInfo
     * @date 2013. 10. 30.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setExportDataInfo(List<MaxTimeOpInfo> maxTimeOPForStationList, List<HashMap<String, Object>> dataList) throws Exception {
        double shop_JPH = 0.0;
        HashMap<String, List<String>> workers_station = new HashMap<String, List<String>>();

        if (maxTimeOPForStationList == null || maxTimeOPForStationList.size() == 0)
            throw new NullPointerException("Station List is null.");

        if (dataList == null)
            dataList = new ArrayList<HashMap<String,Object>>();

        // Shop JPH 가져오기
        String str_shop_jph = maxTimeOPForStationList.get(0).opLine.window().getTopBOMLine().getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_JPH);
        if (str_shop_jph == null || str_shop_jph.trim().length() == 0)
            shop_JPH = 0.0;
        else
            shop_JPH = Double.parseDouble(str_shop_jph);

        for (MaxTimeOpInfo opInfo : maxTimeOPForStationList)
        {
            HashMap<String, Object> propertyMap = new HashMap<String, Object>();

            String station_code = null;
            String str_worker = null;

            // 공정코드 가져오기
            station_code = opInfo.opLine.parent().getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);

            // 여기에 작업자 코드를 기록하고 체크해서 문자로 설정하고 '()'(중복 작업자)를 치는 것을 해야 한다.
            int dup_worker_count = 0;
            int in_worker_count = 0;
            if (opInfo.workerList != null)
            {
                for (WorkerTimeInfo worker : opInfo.workerList) {
                    if (workers_station.containsKey(worker.workerCode)) {
                        // 작업자 리스트에 있으면 중복작업자
                        if (! workers_station.get(worker.workerCode).contains(station_code))
                            workers_station.get(worker.workerCode).add(station_code);

                        dup_worker_count++;
                    } else {
                        // 작업자 리스트에 없으면 중복작업자가 아니다.
                        ArrayList<String> worker_station = new ArrayList<String>();
                        worker_station.add(station_code);
                        workers_station.put(worker.workerCode, worker_station);

                        in_worker_count++;
                    }
                }
                // 액셀의 작업자 수를 처리하는 부분
                if (dup_worker_count + in_worker_count > 0)
                {
                    if (dup_worker_count > 0)
                        str_worker = in_worker_count + "(" + dup_worker_count + ")";
                    else
                        str_worker = in_worker_count + "";
                }
            }
            // 작업자 코드를 읽지 못하거나 해서 값을 못 넣을 수도 있다. 이렇게 되면 공법의 작업자 수에 있는 값을 읽어 설정하자.
            if (dup_worker_count + in_worker_count == 0)
            {
                if (opInfo.opLine.getItemRevision().isValidPropertyName(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT))
                {
                    String str_worker_count = opInfo.opLine.getItemRevision().getProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);
                    if (str_worker_count == null || str_worker_count.trim().length() == 0)
                        in_worker_count = 0;
                    else
                        in_worker_count = Integer.valueOf(str_worker_count);
                    str_worker = in_worker_count + "";
                }
            }

            if (str_worker == null || str_worker.equals("0"))
            	continue;

            // 공정 코드
            propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, station_code);
            // 공법 명
            propertyMap.put(SDVPropertyConstant.BL_OBJECT_NAME, opInfo.opLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));
            // 작업자 수
            propertyMap.put(registry.getString("BODYOperationWorkerCount.ATTR.NAME", "WorkerCount"), str_worker);
            // 비고(Shop의 JPH와 라인의 JPH값이 다를 때만 출력)
            double line_JPH = 0.0;
            String str_line_jph = opInfo.opLine.parent().parent().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_JPH);
            if (str_line_jph == null || str_line_jph.trim().length() == 0)
                line_JPH = 0.0;
            else
                line_JPH = Double.parseDouble(str_line_jph);
            propertyMap.put(registry.getString("BODYPrintETC.ATTR.NAME", "LineJPHOfETC"), line_JPH != 0 && line_JPH != shop_JPH ? line_JPH + "" : "");
            // 작업자 정미시간
            double workTime = opInfo.maxUserWorkTime / (opInfo.workerList != null ? (double) opInfo.workerList.size() : 0);
            propertyMap.put(registry.getString("BODYUserWorkTime.ATTR.NAME", "UserWorkTime"), Double.isNaN(workTime) ? 0 : workTime);
            // 불가피 대기시간
            double waitTime = opInfo.maxUserWaitTime / (opInfo.workerList != null ? (double) opInfo.workerList.size() : 0);
            propertyMap.put(registry.getString("BODYUserWaitTime.ATTR.NAME", "UserWaitTime"), Double.isNaN(waitTime) ? 0 : waitTime);
            // 라인 JPH
            propertyMap.put(SDVPropertyConstant.LINE_REV_JPH, line_JPH == 0 ? shop_JPH : line_JPH);

            dataList.add(propertyMap);
        }

    }

    /**
     * 초기 데이타 로드
     * @method initOperation
     * @date 2013. 10. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initOperation() throws Exception {
        TCComponentBOPLine selected_target = null;
        ArrayList<TCComponentBOPLine> target_BOP_list = new ArrayList<TCComponentBOPLine>();
        registry = Registry.getRegistry(LineBalanceSheet4BodyOperation.class);

        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
        session = (TCSession) abstractaifuiapplication.getSession();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

        boolean flag = false;
        boolean typeFlag = false;

        parentShell = AIFDesktop.getActiveDesktop().getShell();

        for (int i = 0; i < aaifcomponentcontext.length; i++) {
            if (aaifcomponentcontext != null && aaifcomponentcontext.length > 0 && (aaifcomponentcontext[i].getComponent() instanceof TCComponentBOPLine)) {
                selected_target = (TCComponentBOPLine) aaifcomponentcontext[i].getComponent();
                target_BOP_list.add((TCComponentBOPLine) aaifcomponentcontext[i].getComponent());
                String itemType = "";
                try {
                    itemType = selected_target.getItem().getType();
                } catch (TCException e) {
                    itemType = "";
                }
                if (itemType.equals("M7_BOPShop") || itemType.equals("M7_BOPLine") || itemType.equals("M7_BOPStation"))
                    typeFlag = true;
//                MFGStructureType mfgstructuretype = MFGStructureTypeUtil.getStructureType(selected_target);
//                if (mfgstructuretype.isProcess())
//                    flag = true;
                if(MFGStructureTypeUtil.isProcess(selected_target))
                	flag = true;
            } else {
                selected_target = null;
            }

            if (!flag || !typeFlag) {
                target_BOP_list.clear();
                //MessageBox.post(parentShell, registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."), registry.getString("error.TITLE", "Error"), MessageBox.ERROR);
                throw new Exception(registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."));
            }
        }

        if (target_BOP_list.size() == 0)
            targetBOPLines = null;
        else
            targetBOPLines = target_BOP_list.toArray(new TCComponentBOPLine[target_BOP_list.size()]);

        if (targetBOPLines == null)
        {
            //MessageBox.post(parentShell, registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."), registry.getString("error.TITLE", "Error"), MessageBox.ERROR);
            throw new Exception(registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."));
        }
    }

    /**
     * 선택한 BOP라인 하위 모든 공정리스트를 가져오기 위한 재귀호출 함수
     *
     * @method getStationList
     * @date 2013. 10. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getStationList(TCComponentBOPLine[] targetBOPLines, List<MaxTimeOpInfo> maxOPList) throws Exception {
        // 공정리스트 초기설정
        if (maxOPList == null)
            maxOPList = new ArrayList<MaxTimeOpInfo>();

        for (TCComponentBOPLine targetBOPLine : targetBOPLines)
        {
            // Shop, 라인, 공정인지 체크 다른형태는 체크하지 않는다.
            String item_type = targetBOPLine.getItem().getType();
            if (! (item_type.equals("M7_BOPShop") || item_type.equals("M7_BOPLine") || item_type.equals("M7_BOPStation")))
                continue;

            if (! item_type.equals("M7_BOPStation"))
            {
                // 공정이 아니면 Shop이거나 라인이기 때문에 하위 검색
                AIFComponentContext[] child_lines = targetBOPLine.getChildren();
                ArrayList<TCComponentBOPLine> child_line_list = new ArrayList<TCComponentBOPLine>();

                for (AIFComponentContext child_line : child_lines)
                {
                    child_line_list.add((TCComponentBOPLine) child_line.getComponent());
                }

                // 하위에서 공정이 있는지 라인인지 체크 및 리스트에 담기 위해 재귀 호출
                if (child_line_list.size() > 0)
                    getStationList(child_line_list.toArray(new TCComponentBOPLine[child_line_list.size()]), maxOPList);
            }
            else
            {
                // 최장시간 공법 조회 및 데이타 저장
                getMaxTimeOperationOfStation(targetBOPLine, maxOPList);
            }
        }
    }

    /**
     * 공정하위 공법들 중 작업자 정미시간과 불가피 대기시간의 합이 제일 많은 공법을 발췌하여 리턴하는 함수
     *
     * @method getMaxTimeOperationOfStation
     * @date 2013. 10. 17.
     * @param
     * @return MaxTimeOpInfo
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void getMaxTimeOperationOfStation(TCComponentBOPLine tcComponentBOPLine, List<MaxTimeOpInfo> maxOPList) throws Exception {
        AIFComponentContext[] child_lines = tcComponentBOPLine.getChildren();
        TCComponentBOPLine max_time_op_line = null;
        double max_op_time = 0;
        double max_user_worktime = 0;
        double max_user_waittime = 0;
        double shop_allowance = 0.0;
        double line_allowance = 0.0;
        double op_allowance = 0.0;
        ArrayList<WorkerTimeInfo> max_worker_list = new ArrayList<WorkerTimeInfo>();

        // Shop의 부대계수를 읽는다.
        if (tcComponentBOPLine.window().getTopBOMLine().getItemRevision().isValidPropertyName(SDVPropertyConstant.SHOP_REV_ALLOWANCE))
        {
            String allowance = tcComponentBOPLine.window().getTopBOMLine().getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_ALLOWANCE);
            if (allowance == null || allowance.trim().equals(""))
                shop_allowance = 0.0;
            else
                shop_allowance = Double.valueOf(allowance);
        }

        // 라인의 부대계수를 읽는다.
        if (tcComponentBOPLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.LINE_REV_ALLOWANCE))
        {
            String allowance = tcComponentBOPLine.window().getTopBOMLine().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_ALLOWANCE);
            if (allowance == null || allowance.trim().equals(""))
                line_allowance = 0.0;
            else
                line_allowance = Double.valueOf(allowance);
        }

        // 라인의 부대계수가 없으면 SHOP의 부대계수를 사용한다. 둘다 없으면 기본 값인 0.108을 사용한다.(기본값은 Properties에서 설정한다.)
        if (line_allowance == 0.0)
            if (shop_allowance == 0.0)
                op_allowance = Double.valueOf(registry.getString("BODYShopDefaultAllowance", "0.108"));
            else
                op_allowance = shop_allowance;
        else
            op_allowance = line_allowance;

        // 작업시간을 체크하기 위한 Category 값을 Properties에서 읽는다.
        String[] time_check_categories = registry.getStringArray("BODYBOPActivity.Time.Check.Category");
        ArrayList<String> time_check_category_list = new ArrayList<String>();
        // 작업자 정미시간을 처리하기 위한 Category값을 Properties에서 읽는다. - 기준 변경으로 삭제 처리 - 2013-10-27
//        String check_user_time_category = registry.getString("BODYBOPActivity.Time.User.Category");
        // 불가피 대기시간을 체크하기 위한 작업처리 형태 값을 Properties에서 읽는다.
        String check_user_wait_category = registry.getString("BODYBOPActivity.Time.Wait.Category", "STANDBY");

        time_check_category_list.addAll(VectorHelper.toVector(time_check_categories));

        for (AIFComponentContext childLine : child_lines) {
            TCComponentBOPLine op_line = (TCComponentBOPLine) childLine.getComponent();

            // 공법인지 체크
            if (op_line.getItem().getType().equals("M7_BOPBodyOp"))
            {
                // 공법 작업자 정미 및 불가피 대기 총 합
                double work_total_time = 0;
                // 공법 작업자 정미 총 합
                double work_user_work_time = 0;
                // 공법 불가피 대기 총 합
                double work_user_wait_time = 0;
                // 공법의 작업자별 정미 및 불가피 정보
                HashMap<String, WorkerTimeInfo> worker_list = new HashMap<String, WorkerTimeInfo>();

                // 공법 리비전
                TCComponentItemRevision op_rev = op_line.getItemRevision();
                // 공법 하위 엑티비티 루트
                TCComponentMEActivity root_activity = (TCComponentMEActivity) op_rev.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
                // 엑티비티 하위 전체 엑티비티 리스트
                TCComponentMEActivity[] child_activities = root_activity.listAllActivities();
                for (TCComponentMEActivity child_activity : child_activities) {
                    double work_time = 0;
                    String work_category = "";
                    String work_sub_category = "";
                    String []worker_code = null;

                    // 엑티비티 타입(작업자 정미, 자동, 보조) 체크
                    if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY))
                        work_category = child_activity.getStringProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);

                    if (time_check_category_list.contains(work_category)) {
                        // 엑티비티 서브 타입(불가피 대기, 중복)
                        if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_SUB_CATEGORY))
                            work_sub_category = child_activity.getProperty(SDVPropertyConstant.ACTIVITY_SUB_CATEGORY);

                        // 엑티비티 시간
                        if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME))
                            work_time = child_activity.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);

                        // 작업자 코드
                        if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_WORKER))
                        {
                            TCProperty worker_property = child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER);

                            if (worker_property.isNotArray())
                                worker_code = new String[]{worker_property.getStringValue()};
                            else
                                worker_code = worker_property.getStringArrayValue();
                        }

                        // 작업자별 시간을 계산하기 때문에 작업자 코드가 없으면 시간을 체크하지 않는다.
                        // 만약 작업자가 없으면 전체 작업자에게 시간을 더해야 한다면 전체 작업자 리스트가 어디(공법)엔가 먼저 정의가 되어야 한다.
                        // 첫 Activity에 작업자 코드가 없으면 어떻게 전체 작업자에게 시간을 더해 줄 것인가 고민해야 함.
                        if (worker_code != null)
                        {
                            for (String worker : worker_code)
                            {
                                if (worker == null)
                                    continue;

                                // 작업자별로 작업시간과 대기시간을 저장한다.
                                if (! worker_list.containsKey(worker))
                                {
                                    WorkerTimeInfo worker_info = new WorkerTimeInfo();
                                    worker_info.workerCode = worker;
                                    worker_info.workTime = work_sub_category.equals(check_user_wait_category) ? 0.0 : work_time;
                                    worker_info.waitTime = work_sub_category.equals(check_user_wait_category) ? work_time : 0.0;

                                    worker_list.put(worker, worker_info);
                                }
                                else
                                {
                                    worker_list.get(worker).workTime += work_sub_category.equals(check_user_wait_category) ? 0.0 : work_time;
                                    worker_list.get(worker).waitTime += work_sub_category.equals(check_user_wait_category) ? work_time : 0.0;
                                }
                            }
                        }
                    }
                }

                // 작업자별 불가피 대기 시간을 부대계수와 체크하여 재적용한다. 불가피 대기시간이 작업시간의 10.8%보다 작으면 작업시간의 10.8%를 대기시간으로 설정
                for (String op_worker : worker_list.keySet())
                {
                    if (worker_list.get(op_worker).waitTime == 0 || worker_list.get(op_worker).workTime * op_allowance > worker_list.get(op_worker).waitTime)
                    {
                        worker_list.get(op_worker).waitTime = worker_list.get(op_worker).workTime * op_allowance;
                    }

                    // 공법의 전체 작업시간 및 불가피 대기시간을 계산한다. 작업자별로 출력한다면 필요없는 계산
                    work_total_time += worker_list.get(op_worker).workTime + worker_list.get(op_worker).waitTime;
                    work_user_work_time += worker_list.get(op_worker).workTime;
                    work_user_wait_time += worker_list.get(op_worker).waitTime;
                }

                // 공정하위 공법들 중 작업시간이 제일 긴 공법을 발췌한다.
                if (max_op_time == 0 || max_op_time < work_total_time) {
                    // 최장 작업시간 공법
                    max_time_op_line = op_line;
                    // 작업 시간
                    max_op_time = work_total_time;
                    // 작업자 정미시간
                    max_user_worktime = work_user_work_time;
                    // 불가피 대기시간
                    max_user_waittime = work_user_wait_time;

                    // 작업자 리스트 삭제
                    if (max_worker_list.size() > 0)
                        max_worker_list.clear();

                    // 작업자 코드 전체 추가
                    max_worker_list.addAll(worker_list.values());
                }

                worker_list.clear();
            }
        }

        // 최장시간 공법을 저장소에 저장.
        if (max_time_op_line != null) {
            MaxTimeOpInfo max_op_info = new MaxTimeOpInfo();

            max_op_info.opLine = max_time_op_line;
            max_op_info.maxUserWorkTime = max_user_worktime;
            max_op_info.maxUserWaitTime = max_user_waittime;
            if (max_worker_list != null && max_worker_list.size() > 0)
            {
                max_op_info.workerList = new ArrayList<WorkerTimeInfo>();
                max_op_info.workerList.addAll(max_worker_list);

                max_worker_list.clear();
            }
            if (time_check_category_list.size() > 0)
                time_check_category_list.clear();

            maxOPList.add(max_op_info);
        } else {
            max_time_op_line = null;
            max_user_worktime = 0;
            max_user_waittime = 0;
            if (max_worker_list != null)
                max_worker_list.clear();
            if (time_check_category_list.size() > 0)
                time_check_category_list.clear();
        }
    }

}
