/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.peif;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.ImportExcelServce;
import com.symc.plm.me.sdv.service.migration.exception.SkipException;
import com.symc.plm.me.sdv.service.migration.exception.StopException;
import com.symc.plm.me.sdv.service.migration.job.peif.PEIFTCDataExecuteJob;
import com.symc.plm.me.sdv.service.migration.model.TreeColumnInfo;
import com.symc.plm.me.sdv.service.migration.model.TypeObject;
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
import com.symc.plm.me.sdv.service.migration.util.FileUtil;
import com.symc.plm.me.sdv.service.migration.util.PEExcelConstants;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Utilities;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;

/**
 * Class Name : PEIFJobWork
 * Class Description :
 * 
 * Parsing 순서 - 공법을 기준으로 순차적으로 BOM을 구성하여 등록 (Excel -> Tree Node 구성)
 * 
 * 1. 공법 - BOM, MASTER
 * 2. END-ITEM - OCCERERNCE
 * 3. 공구 - OCCERERNCE, MASTER
 * 4. 설비 - OCCERERNCE, MASTER
 * 5. 부자재 - OCCERERNCE
 * 6. Activate - Object
 * 
 * @date 2013. 11. 22.
 * 
 */
public class PEIFJobWork {

    protected Shell shell;
    protected PEIFTCDataExecuteJob peIFTCDataExecuteJob;
    protected Tree tree;
    protected String mecoNo;
    protected String folderPath;
    protected TCComponentMfgBvrProcess processLine;
    protected boolean isOverride;

    protected PEValidation peValidation;
    protected PEExecution peExecution;

    private ArrayList<TCData> problemItemList;
    private ArrayList<OperationItemData> operationItemDataList;

    private int operationUnderRowCount = 0;

    public static final int TC_TYPE_INDEX_OPERATION = 0;
    public static final int TC_TYPE_INDEX_ACTIVITY = 1;
    public static final int TC_TYPE_INDEX_TOOL = 2;
    public static final int TC_TYPE_INDEX_EQUIPMENT = 3;
    public static final int TC_TYPE_INDEX_END_ITEM = 4;
    public static final int TC_TYPE_INDEX_SUBSIDIARY = 5;

    public static final String TC_TYPE_CLASS_NAME_SHOP = "SHOP"; // SHOP CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_LINE = "LINE"; // LINE CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_OPERATION = "OPERATION"; // 공법 CLASS TYPE
    public static final Object[] TC_TYPE_OPERATION = new Object[] { TC_TYPE_INDEX_OPERATION, "Master-20공법", "21-라인-공법" };
    public String[] TC_TYPE_OPERATION_FILEPATH = new String[2];
    public static final String TC_TYPE_CLASS_SHEET = "SHEET"; // 작업표준서 CLASS TYPE

    public static final String TC_TYPE_CLASS_NAME_ACTIVITY = "ACTIVITY";
    public static final String TC_TYPE_CLASS_NAME_ACTIVITY_SUB = "ACTIVITY_SUB";
    public static final Object[] TC_TYPE_ACTIVITY = new Object[] { TC_TYPE_INDEX_ACTIVITY, "Master-20Activity", "20-공법-Activity" };
    public String[] TC_TYPE_ACTIVITY_FILEPATH = new String[2];

    public static final String TC_TYPE_CLASS_NAME_TOOL = "TOOL";
    public static final Object[] TC_TYPE_TOOL = new Object[] { TC_TYPE_INDEX_TOOL, "Master-40공구", "43-공법-공구" };
    public String[] TC_TYPE_TOOL_FILEPATH = new String[2];

    public static final String TC_TYPE_CLASS_NAME_EQUIPMENT = "EQUIPMENT";
    public static final Object[] TC_TYPE_EQUIPMENT = new Object[] { TC_TYPE_INDEX_EQUIPMENT, "Master-30설비", "44-공법-설비" };
    public String[] TC_TYPE_EQUIPMENT_FILEPATH = new String[2];

    public static final String TC_TYPE_CLASS_NAME_END_ITEM = "END_ITEM";
    public static final Object[] TC_TYPE_END_ITEM = new Object[] { TC_TYPE_INDEX_END_ITEM, "", "41-공법-일반자재" };
    public String[] TC_TYPE_END_ITEM_FILEPATH = new String[2];

    public static final String TC_TYPE_CLASS_NAME_SUBSIDIARY = "SUBSIDIARY";
    public static final Object[] TC_TYPE_SUBSIDIARY = new Object[] { TC_TYPE_INDEX_SUBSIDIARY, "", "42-공법-부자재" };
    public String[] TC_TYPE_SUBSIDIARY_FILEPATH = new String[2];

    HashMap<Integer, TypeObject> typeFileParsingStrMaps;
    ArrayList<Object[]> typeObjects;

    Workbook operationBOMWb;
    Workbook operationMasterWb;

    // Log File 경로
    String logFilePath;

    TCComponentBOMLine topLineMproduct = null;
    AIFComponentContext[] functionContexts = null;
    
//    private boolean isReinterface = false;

	public PEIFJobWork(Shell shell, PEIFTCDataExecuteJob peIFTCDataExecuteJob, TCComponentMfgBvrProcess processLine, String folderPath, String mecoNo, boolean isOverride) {
        this.shell = shell;
        this.peIFTCDataExecuteJob = peIFTCDataExecuteJob;
        this.processLine = processLine;
        this.tree = peIFTCDataExecuteJob.getTree();
        this.folderPath = folderPath;
        this.mecoNo = mecoNo;
        this.isOverride = isOverride;
        this.operationItemDataList = new ArrayList<OperationItemData>();
        // TreeItem Validator
        peValidation = new PEValidation(shell, peIFTCDataExecuteJob, processLine, mecoNo, isOverride);
        // TreeItem Execute
        peExecution = new PEExecution(shell, peIFTCDataExecuteJob, processLine, mecoNo, isOverride);
        problemItemList = new ArrayList<TCData>();
        typeFileParsingStrMaps = new HashMap<Integer, TypeObject>();
        typeObjects = new ArrayList<Object[]>();
        typeObjects.add(TC_TYPE_OPERATION);
        typeObjects.add(TC_TYPE_ACTIVITY);
        typeObjects.add(TC_TYPE_TOOL);
        typeObjects.add(TC_TYPE_EQUIPMENT);
        typeObjects.add(TC_TYPE_END_ITEM);
        typeObjects.add(TC_TYPE_SUBSIDIARY);
        // Log 파일 생성
        createLogiFile(folderPath);
    }

    /**
     * Log 파일 생성
     * 
     * @method createLogiFile
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createLogiFile(String folderPath) {
        Calendar c = Calendar.getInstance();
        String fileName = addNumberZero(c.get(Calendar.YEAR)) + "_" + addNumberZero(c.get(Calendar.MONTH) + 1) + "_" + addNumberZero(c.get(Calendar.DAY_OF_MONTH)) + "_" + addNumberZero(c.get(Calendar.HOUR_OF_DAY)) + "_" + addNumberZero(c.get(Calendar.MINUTE)) + "_" + addNumberZero(c.get(Calendar.SECOND)) + "_" + addNumberZero(c.get(Calendar.MILLISECOND)) + ".txt";
        logFilePath = folderPath + "\\" + fileName;
    }

    /**
     * 9이하 숫자앞에 0을 붙여준다.
     * 
     * @method addNumberZero
     * @date 2013. 11. 28.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String addNumberZero(int dateData) {
        if (dateData < 10) {
            return "0" + dateData;
        } else {
            return dateData + "";
        }
    }

    /**
     * Work 실행
     * 
     * @method executeWork
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void executeWork() throws Exception {
        if (StringUtils.isEmpty(folderPath)) {
            throw new Exception("Folder 정보가 없습니다.");
        }
        // Validate MECO
        checkMECO();
        // Type별 Master, BOM 파일 설정.
        initFilePath(this.folderPath);
        makeBatchOperation();
    }

    /**
     * MECO가 Valide한지 검증한다.
     * 
     * @method checkMECO
     * @date 2013. 11. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected void checkMECO() throws Exception {
        mecoNo.getBytes();
    }

    /**
     * 공법 Excel Import
     * 
     * @method makeBatchOperation
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void makeBatchOperation() throws Exception {
        // 공법(Operation) Workbook 초기화 및 생성
        operationMasterWb = ImportExcelServce.getWorkBook(TC_TYPE_OPERATION_FILEPATH[0]); // OPERATION Master file path
        operationBOMWb = ImportExcelServce.getWorkBook(TC_TYPE_OPERATION_FILEPATH[1]); // OPERATION BOM file path
        rowBatchOpearionInfos();
        // 후처리
        postMakeBatchOperation();
    }

    /**
     * 공법(Operation) BOM 정보를 batch 처리 후 실행
     * 
     * @method postMakeBatchOperation
     * @date 2013. 12. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void postMakeBatchOperation() throws Exception {
        // Line 하위 불필요 공법 삭제
        removeNotIfOperationBOMLines();
    }

    /**
     * Line 하위 불필요 공법 삭제
     * 
     * @method removeNotIfOperationBOMLines
     * @date 2013. 12. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void removeNotIfOperationBOMLines() throws Exception {
        ArrayList<TCComponentBOMLine> removeList = getNotIfOperationList();
        if (removeList.size() > 0) {
            StringBuffer removeLogMsg = new StringBuffer();
            for (int i = 0; i < removeList.size(); i++) {
                removeLogMsg.append("\t" + (i + 1) + " : " + removeList.get(i).getProperty(SDVPropertyConstant.BL_ITEM_ID) + "\t\t" + removeList.get(i).getProperty(SDVPropertyConstant.BL_OBJECT_NAME) + "\n");
            }
            saveLog("\n\n\n");
            saveLog("######################################################\n");
            saveLog("◎ PE I/F 대상 제외 공법(Operation) 리스트 \n");
            saveLog(removeLogMsg.toString());
            removeOperationList(removeList, removeLogMsg);
            saveLog("######################################################\n");
        }
    }

    /**
     * Remove Operation BOMLines
     * 
     * @method removeOperationList
     * @date 2013. 12. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void removeOperationList(ArrayList<TCComponentBOMLine> removeList, StringBuffer removeLogMsg) throws Exception {
        int confirmRet = ConfirmDialog.prompt(shell, "Confirm", "다음 공법(Operation)들을 Line에서 잘라냅니다. 실행하시겠습니까? \n" + removeLogMsg.toString());
        // Cut Logic
        if (confirmRet == 2) {
            for (int i = 0; i < removeList.size(); i++) {
                SDVBOPUtilities.disconnectObjects(removeList.get(0).parent(), removeList);
                // removeList.get(i).cut();
            }
            saveLog(" -> [Remove BOMLine] 공법(Operation)들을 Line에서 잘라내었습니다. \n");
        }
    }

    /**
     * PE I/F 되지않은 Operation List를 가지고온다.
     * 
     * @method getNotIfOperationList
     * @date 2013. 12. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private ArrayList<TCComponentBOMLine> getNotIfOperationList() throws Exception {
        ArrayList<TCComponentBOMLine> notIfOperationList = new ArrayList<TCComponentBOMLine>();
        if (operationItemDataList.size() == 0) {
            return notIfOperationList;
        }
        AIFComponentContext contexts[] = processLine.getChildren();
        if (contexts == null || contexts.length == 0) {
            return notIfOperationList;
        }
        // I/F Match되지않는 BOMLine 검색
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            childLines[i] = (TCComponentBOMLine) contexts[i].getComponent();
            if (childLines[i] != null) {
                for (OperationItemData operationItemData : operationItemDataList) {
                    TCComponentBOMLine ifOperationBOMLine = operationItemData.getBopBomLine();
                    if (childLines[i] == ifOperationBOMLine) {
                        childLines[i] = null;
                    }
                }
            }
        }
        // I/F Match 되지않는 BOMLine List 추출
        for (TCComponentBOMLine tcComponentBOMLine : childLines) {
            if (tcComponentBOMLine != null) {
                notIfOperationList.add(tcComponentBOMLine);
            }
        }
        return notIfOperationList;
    }

    /**
     * CLASS Type별 MASTER, BOM 절대경로를 알아낸다.
     * 
     * @method getFilePath
     * @date 2013. 11. 25.
     * @param String
     *            folderParh - 다이얼로그에서 선택한 Folder경로
     * @param int typeIndex - TC_TYPE_INDEX_... (CLASS Type index)
     * @return String[]
     *         String[0] Master File 절대경로
     *         String[1] BOM File 절대경로
     * @exception
     * @throws
     * @see
     */
    private void initFilePath(String folderPath) {
        // Type별 Master File path 설정
        setFilePath(folderPath, PEExcelConstants.MASTER);
        // Type별 BOM File path 설정
        setFilePath(folderPath, PEExcelConstants.BOM);
    }

    /**
     * Type별 Master, BOM File path 설정
     * 
     * @method getFilePath
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setFilePath(String folderPath, String folderName) {
        ArrayList<String> files = FileUtil.getFileList(folderPath + "\\" + folderName);
        for (String fileName : files) {
            String name = FileUtil.getExculsiveExtFileName(fileName);
            for (int i = 0; i < 6; i++) {
                String templateFileName = "";
                if (PEExcelConstants.MASTER.equals(folderName)) {
                    templateFileName = (String) typeObjects.get(i)[1];
                } else if (PEExcelConstants.BOM.equals(folderName)) {
                    templateFileName = (String) typeObjects.get(i)[2];
                }
                // Template File과 이름이 같은지 비교
                if (name.equals(templateFileName)) {
                    String absFilePath = folderPath + "\\" + folderName + "\\" + fileName;
                    switch (i) {
                    case TC_TYPE_INDEX_OPERATION:
                        if (PEExcelConstants.MASTER.equals(folderName)) {
                            TC_TYPE_OPERATION_FILEPATH[0] = absFilePath;
                        } else if (PEExcelConstants.BOM.equals(folderName)) {
                            TC_TYPE_OPERATION_FILEPATH[1] = absFilePath;
                        }
                        break;

                    case TC_TYPE_INDEX_ACTIVITY:
                        if (PEExcelConstants.MASTER.equals(folderName)) {
                            TC_TYPE_ACTIVITY_FILEPATH[0] = absFilePath;
                        } else if (PEExcelConstants.BOM.equals(folderName)) {
                            TC_TYPE_ACTIVITY_FILEPATH[1] = absFilePath;
                        }
                        break;

                    case TC_TYPE_INDEX_TOOL:
                        if (PEExcelConstants.MASTER.equals(folderName)) {
                            TC_TYPE_TOOL_FILEPATH[0] = absFilePath;
                        } else if (PEExcelConstants.BOM.equals(folderName)) {
                            TC_TYPE_TOOL_FILEPATH[1] = absFilePath;
                        }
                        break;

                    case TC_TYPE_INDEX_EQUIPMENT:
                        if (PEExcelConstants.MASTER.equals(folderName)) {
                            TC_TYPE_EQUIPMENT_FILEPATH[0] = absFilePath;
                        } else if (PEExcelConstants.BOM.equals(folderName)) {
                            TC_TYPE_EQUIPMENT_FILEPATH[1] = absFilePath;
                        }
                        break;

                    case TC_TYPE_INDEX_END_ITEM:
                        if (PEExcelConstants.MASTER.equals(folderName)) {
                            TC_TYPE_END_ITEM_FILEPATH[0] = absFilePath;
                        } else if (PEExcelConstants.BOM.equals(folderName)) {
                            TC_TYPE_END_ITEM_FILEPATH[1] = absFilePath;
                        }
                        break;

                    case TC_TYPE_INDEX_SUBSIDIARY:
                        if (PEExcelConstants.MASTER.equals(folderName)) {
                            TC_TYPE_SUBSIDIARY_FILEPATH[0] = absFilePath;
                        } else if (PEExcelConstants.BOM.equals(folderName)) {
                            TC_TYPE_SUBSIDIARY_FILEPATH[1] = absFilePath;
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 공법 Relation정보 Excel를 가지고 순차적으로 공법 Tree를 생성 후 배치 처리한다.
     * 
     * @method rowBatchOpearionInfos
     * @date 2013. 11. 22.
     * @param
     * @return ArrayList<TCData>
     * @exception
     * @throws
     * @see
     */
    protected void rowBatchOpearionInfos() throws Exception {
        // Start..
        Sheet sheet = operationBOMWb.getSheetAt(0);
        // M-BOM FunctionList BOMWindow 초기화
        initFunctionList();
        int totalRowCount = sheet.getPhysicalNumberOfRows();
        int strarIndex = PEExcelConstants.START_ROW_INDEX;
    	if(!peIFTCDataExecuteJob.startIndex.isEmpty())
    	{
    		try
			{
				int _startIndex = Integer.parseInt(peIFTCDataExecuteJob.startIndex);
				if(_startIndex > PEExcelConstants.START_ROW_INDEX)
				{
					strarIndex = _startIndex;
				}
			} catch (Exception e)
			{
			}
    	}
    	if(!peIFTCDataExecuteJob.totalRowCount.isEmpty())
    	{
    		try
			{
				int _totalRowCount = Integer.parseInt(peIFTCDataExecuteJob.totalRowCount);
				if(_totalRowCount > strarIndex)
				{
					totalRowCount = _totalRowCount;
				}
			} catch (Exception e)
			{
			}
    	}
        
        // [NON-SR][20160120] Interface 과정에 중단된 경우 중단된 부분부터 다시 시작 하도록 하는 처리를 추가한다.
//        if(isReinterface==true){
        	
//        	String latestInterfacedRowIndx = Utilities.getCookie("BOPCustomCookie", "Pe2TCM_LineProcessId.Index", true);
//        	
//        	int latestInterfacedRowIndxNumber = 0;
//        	if(latestInterfacedRowIndx!=null && latestInterfacedRowIndx.trim().length()>0){
//        		try {
//        			latestInterfacedRowIndxNumber = Integer.parseInt(latestInterfacedRowIndx);
//				} catch (NumberFormatException e) {
//
//				}
//        	}
//        }
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<Integer> rowNumArray = new ArrayList<Integer>();
        for (int i = strarIndex; i < totalRowCount; i++) {
        	ArrayList<String> opearionRowData = new ArrayList<String>();
        	Row row = sheet.getRow(i);
	        for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.OPERATION_BOM_END_COLUMN_INDEX; j++) {
	            Cell cell = row.getCell(j);
	            String cellText = ImportExcelServce.getCellText(cell);
	            opearionRowData.add(cellText);
	        }
	        String selectLineNum = processLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).substring(7, 9);
	        if (opearionRowData.get(4).equals(selectLineNum)) {
	        	saveLog("Excel Number All Data ======> "+i);
	        	rowNumArray.add(i);
	        }
	        
        }
        saveLog("Excel Number Count ======> "+ rowNumArray.size());
//###################################################################
//        strarIndex = 137; // 특정 row 부터 실행할때.. 주석 해제하고 쓰기...
//        totalRowCount = strarIndex + 1; // 특정 row 한 행만 실행하려면 주석 해제하고 쓰면 됨. 윗줄이랑 같이 써야 함.
//###################################################################
        for (int i = strarIndex; i < totalRowCount; i++) {
        	// [NON-SR][2016.01.07] taeku.jeong  진행 정보를 파악 할 수 있도록 Index를 Log에 남기도록 수정함.
        	saveLog("Excel Data Row Index : "+i+"/"+totalRowCount);
        	
//            Utilities.setCookie("BOPCustomCookie", true, "Pe2TCM_LineProcessId.Index", ""+i);
        	
            // Waiting; (전체 Thread가 증가되 상황에서 Thread 숫자를 일시적으로 떨어뜨려 주기는 하는데 곧바로 올라감)
//        	if(i%100==0){
//        		Thread.sleep(20000);
//        	}else if(i%30==0){
//        		Thread.sleep(10000);
//        	}else{
//        		Thread.sleep(500);	
//        	}
//        		Thread.sleep(500);	
            
            Row row = sheet.getRow(i);
            ArrayList<String> opearionRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.OPERATION_BOM_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                opearionRowData.add(cellText);
            }
            //특정 공법 아이템만 실행하려고 할때...
//            String aa = opearionRowData.get(8);
//            if(!aa.equals("80C-240R"))
//            {
//            	continue;
//            }
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(opearionRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                break;
            }
            //아주 위험한 프로그램임.. 쓰레드 남발로 인한 java vm을 죽이는 프로그램
            if (!processLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).equals(getLineItemId(opearionRowData)))
            {
                continue;
            }
//        	if(i%100==0){
//        		Thread.sleep(20000);
//        	}else if(i%30==0){
//        		Thread.sleep(10000);
//        	}else{
//        		Thread.sleep(500);	
//        	}
            // Row별 공법(Operation) 처리
            startBatchOpearion(opearionRowData);
            
            // [NON-SR][2016.01.07] taeku.jeong Heap 메모리 확보를 위해 GC를 주기적으로 하도록 함
            // 이후에 PE-TC Interface 과정에 1G 조금 넘는 수준을 초과 하지 않음.
            // Teamcenter 수행 Java VM 실행 환경 변수는 다음과 같이 설정 했음.
            // -Xms4096m -Xmx4096m -XX:PermSize=256m -XX:MaxPermSize=256m
            
//            System.gc();

        }
    }

    /**
     * 공법(Operation)별 Update 시작 (실제 Tree Node 구성)
     * 
     * @method startBatchOpearion
     * @date 2013. 11. 22.
     * @param opearionRowData
     *            (Excel Row데이터를 Text형식으로 가져온 컬럼 데이터 List - ArrayList)
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected synchronized void startBatchOpearion(final ArrayList<String> opearionRowData) throws Exception {
    	
    	// [NON-SR][20160112] taeku.jeong PE->TC Interface 도중에 여전히 죽는 문제가 발생되고
    	// Update 된 결과가 다르게 나오는 경우가 있어서 Thrad의 동기화를 위해 synchronized Keyword를 추가해
    	// Operation 별 Batch Operation이 동기화 되어 진행 되도록 수정함.
    	
        final ArrayList<Exception> exceptionList = new ArrayList<Exception>();
        
        // [NON-SR][2016/]
//        shell.getDisplay().syncExec(new Runnable() {
//            public void run() {
//                try {
//                    boolean isMissmatchLine = runBatchOperation(opearionRowData);
//                    // 선택한 BOP Line Item ID가 공법의 Line Item이 다른 경우 skip
//                    if (isMissmatchLine) {
//                        return;
//                    }
//                    // validate 실행
//                    validateTreeExpand();
//                    // execute 실행
//                    executeTreeExpand();
//                } catch (Exception e) {
//                	if((e instanceof SkipException)==false){
//                		e.printStackTrace();
//                	}
//                    // Log 출력
//                    String exceptionString = ImportCoreService.getStackTraceString(e);
//                    saveLog(exceptionString);
//                    exceptionList.add(e);
//                }
//            }
//        });
        
        // [NON-SR][2016.01.07] taeku.jeong Thread를 강제로 소멸 시키도록 수정함.
        // 이후에 2회 Test 수행시 Teamcenter가 Interface 도중에 죽어 버리는 문제 발생 않음
        Runnable operationMigratoinThread = new Runnable() {
            public void run() {
                try {
                    boolean isMissmatchLine = runBatchOperation(opearionRowData);
                    // 선택한 BOP Line Item ID가 공법의 Line Item이 다른 경우 skip
                    if (isMissmatchLine) {
                        return;
                    }
                    // validate 실행
                    validateTreeExpand();
                    // execute 실행
                    executeTreeExpand();
                } catch (Exception e) {
                	if((e instanceof SkipException)==false){
                		e.printStackTrace();
                	}
                    // Log 출력
                    String exceptionString = ImportCoreService.getStackTraceString(e);
                    saveLog(exceptionString);
                    exceptionList.add(e);
                }finally{
                	
                }
            }
        };
        
//        Thread aThread = new Thread(operationMigratoinThread);
        shell.getDisplay().syncExec(operationMigratoinThread);
//        aThread.stop();
//        aThread = null;
//        operationMigratoinThread = null;
        
        // TODO : Check Point!!..
        // 현재 Exception 발생시 임포트를 중단하지 않는다. - Exception Skip..
        // if (exceptionList.size() > 0) {
        // throw exceptionList.get(0);
        // }

        // 강제종료만 Sop함.
        if (exceptionList.size() > 0) {
            if (exceptionList.get(0) instanceof StopException) {
                throw exceptionList.get(0);
            }
        }
    }

    /**
     * 공법별 배치 실행
     * 
     * @method runBatchOperation
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private boolean runBatchOperation(ArrayList<String> opearionRowData) throws Exception {
        // Tree 전체 삭제..
        tree.removeAll();
        // 컬럼 정보를 가지고 Tree View 컬럼 생성
        createTreeColumn();
        // Tree 재구성
        LineItemData itemLineData = new LineItemData(tree, 0, TC_TYPE_CLASS_NAME_LINE, tree.getColumns());
        itemLineData.setText(new String[] { getLineItemId(opearionRowData), itemLineData.getClassType() });
        itemLineData.setItemId(getLineItemId(opearionRowData));
        // 선택한 BOP Line Item ID가 공법의 Line Item이 다른 경우 'true' return
        if (!itemLineData.getItemId().equals(processLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
            return true;
        }
        itemLineData.setExistItem(true); // Line 정보는 항상 true
        // LINE Item정보에 BOMLine정보 등록
        itemLineData.setBopBomLine(processLine);
        // 공법하위 TreeIndex 초기화
        operationUnderRowCount = 0;
        // 공법 Item 생성
        OperationItemData operationItemData = new OperationItemData(itemLineData, 0, TC_TYPE_CLASS_NAME_OPERATION, tree.getColumns());
        operationItemData.setData(PEExcelConstants.BOM, opearionRowData);
        operationItemData.setText(new String[] { getOperationItemId(opearionRowData), operationItemData.getClassType() });
        operationItemData.setItemId(getOperationItemId(opearionRowData));
        // 공법 List에 추가 - Line 하위 공법 List (I/F 대상 삭제 검증용)
        operationItemDataList.add(operationItemData);
        // 공법 BOM, Master 속성 추가
        setOperation(operationItemData);
        // 공법 작업표준서 File DataSet 추가
        setOperationSheetDatset(operationItemData);
        // END-ITEM(일반자재) 추가
        setEndItem(operationItemData);
        // Tools(공구) 추가
        setTool(operationItemData);
        // Equipment(설비) 추가
        setEquipment(operationItemData);
        // Tools(부자재) 추가
        setSubsidiaryData(operationItemData);
        // Activity 추가
        setActivity(operationItemData);
        return false;
    }

    /**
     * 공법(Operation) Item ID를 가지고 Line 하위에있는 공법(Operation) BOMLine을 찾는다.
     * 
     * @method findLineUnderOperationBomLine
     * @date 2013. 11. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine findLineUnderOperationBomLine(TCComponentBOMLine processLine, OperationItemData operationItemData) throws Exception {
        AIFComponentContext contexts[] = processLine.getChildren();
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            childLines[i] = (TCComponentBOMLine) contexts[i].getComponent();
            if (childLines[i] != null) {
                if (childLines[i].getItem() != null) {
                    if (childLines[i].getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).equals(operationItemData.getItemId())) {
                        return childLines[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * validate 실행 (Validate Trigger)
     * 
     * @method validateTreeExpand
     * @date 2013. 11. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void validateTreeExpand() throws Exception, InterruptedException {
        // Validate Expand..
        // 상태를 VALDATE로 변경
        peIFTCDataExecuteJob.setStatus(PEIFTCDataExecuteJob.STATUS_VALIDATE);
        // Expand All 하여 Row별로 Validation한다.
        peIFTCDataExecuteJob.expandAllTCDataItem();
    }

    /**
     * Execute 실행 (Execute Trigger)
     * 
     * @method executeTreeExpand
     * @date 2013. 11. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void executeTreeExpand() throws Exception, InterruptedException {
        // 상태를 EXECUTE로 변경
        peIFTCDataExecuteJob.setStatus(PEIFTCDataExecuteJob.STATUS_EXECUTE);
        // Expand All 하여 Row별로 Validation한다.
        peIFTCDataExecuteJob.expandAllTCDataItem();
    }

    /**
     * 1. 공법 하위 할당 TC 조회 List 초기화
     * 2. 공법 Activity 조회 정보 초기화
     * 
     * @method setOperationChildComponentList
     * @date 2013. 11. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setOperation(OperationItemData operationItemData) throws Exception {
        if (StringUtils.isEmpty(operationItemData.getItemId())) {
            throw new ValidateSDVException("Operation Item ID is null");
        }
        // 생성되는 공법은 구조비교를 제외한다.
        TCComponentBOMLine operationBOMLine = findLineUnderOperationBomLine(processLine, operationItemData);
        // Operation 정보가 BOP에 존재하는지 확인
        if (operationBOMLine != null) {
            // TC에 ITEM으로 존재
            operationItemData.setExistItem(true);
            // BOMLine set
            operationItemData.setBopBomLine(operationBOMLine);
            // Operation 하위 Component BOMLine List 설정
            operationItemData.setOperationChildComponent(SDVBOPUtilities.getUnpackChildrenBOMLine(operationBOMLine));
            // TC Activity List 설정
            operationItemData.setOperationActivityList(SYMTcUtil.getActivityList((TCComponentMfgBvrOperation) operationItemData.getBopBomLine()));
        } else {
            // 해당 Line에는 존재하지 않으나 다른 Line하위에 공법이 존재할 경우
            if (SYMTcUtil.getLatestedRevItem(operationItemData.getItemId()) != null) {
                operationItemData.setExistItem(true);
            } else {
                operationItemData.setExistItem(false);
            }
            // BOP데이터가 없으므로 초기화
            operationItemData.setOperationChildComponent(new TCComponentBOMLine[0]);
            operationItemData.setOperationActivityList(new ArrayList<HashMap<String, Object>>());
        }
        // Operation Master 정보 설정
        setOperationMaster(operationItemData);
    }

    /**
     * 공법 작업표준서 Dataset 생성
     * 
     * @method setOperationDatSet
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void setOperationSheetDatset(OperationItemData operationItemData) {
        ArrayList<String> operationMasterRowData = (ArrayList<String>) operationItemData.getData(PEExcelConstants.MASTER);
        String sheetFilePath = operationMasterRowData.get(PEExcelConstants.OPERATION_MASTER_SHEET_KO_FILE_PATH_COLUMN_INDEX);
        if (StringUtils.isEmpty(sheetFilePath)) {
            return;
        }
        SheetDatasetData sheetDatasetData = new SheetDatasetData(operationItemData, 0, TC_TYPE_CLASS_SHEET, tree.getColumns());
        sheetDatasetData.setData(sheetFilePath);
        sheetDatasetData.setText(new String[] { sheetFilePath, sheetDatasetData.getClassType() });
    }

    /**
     * Operation Relation 정보에서 Operation Master 속성을 가져온다.
     * 
     * @method getOperationMaster
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setOperationMaster(OperationItemData operationItemData) throws Exception {
        Sheet sheet = operationMasterWb.getSheetAt(0);
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            ArrayList<String> operationMasterRowData = new ArrayList<String>();
            Row row = sheet.getRow(i);
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.OPERATION_MASTER_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                operationMasterRowData.add(cellText);
            }
            // EOF이면 break..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(operationMasterRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                break;
            }
            // 같은 Operation이 아니면 Continue...
            if (getOperationMasterItemId(operationMasterRowData).equals(operationItemData.getItemId())) {
                operationItemData.setData(PEExcelConstants.MASTER, operationMasterRowData);
                break;
            }
        }
        if (operationItemData.getData(PEExcelConstants.MASTER) == null) {
            throw new ValidateSDVException("[" + operationItemData.getClassType() + "] Master 정보가 없습니다. - " + operationItemData.getItemId());
        }
    }

    /**
     * 마스터 데이터에서 공법 ItemID를 생성하여 return
     * 
     * @method getOperationMasterItemId
     * @date 2013. 12. 5.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getOperationMasterItemId(ArrayList<String> masterRowData) throws Exception {
        StringBuffer operationItemId = new StringBuffer();
        operationItemId.append(getItemRowString(masterRowData, PEExcelConstants.COMMON_MASTER_PROJECT_NO_COLUMN_INDEX));
        operationItemId.append("-");
        operationItemId.append(getItemRowString(masterRowData, PEExcelConstants.COMMON_MASTER_SHOP_LINE_COLUMN_INDEX));
        operationItemId.append("-");
        operationItemId.append(getItemRowString(masterRowData, PEExcelConstants.COMMON_MASTER_SHEET_NO_COLUMN_INDEX));
        operationItemId.append("-");
        operationItemId.append(getItemRowString(masterRowData, PEExcelConstants.COMMON_MASTER_PLANNING_VERSION_COLUMN_INDEX));
        return operationItemId.toString();
    }

    /**
     * 공법(Operation) Activity 정보를 생성한다.
     * 
     * @method setActivity
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setActivity(OperationItemData operationItemData) throws Exception {
        Workbook activityMasterWb = ImportExcelServce.getWorkBook(TC_TYPE_ACTIVITY_FILEPATH[0]); // ACTIVITY Master file path
        Workbook activityBOMWb = ImportExcelServce.getWorkBook(TC_TYPE_ACTIVITY_FILEPATH[1]); // ACTIVITY BOM file path
        Sheet sheet = activityBOMWb.getSheetAt(0);
        ArrayList<ArrayList<String>> activityBOMList = new ArrayList<ArrayList<String>>();
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            ArrayList<String> activityRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.ACTIVITY_BOM_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                activityRowData.add(cellText);
            }
            // EOF이면 break..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(activityRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                break;
            }
            // 같은 Operation이 아니면 Continue...
            if (!getOperationItemId(activityRowData).equals(operationItemData.getItemId())) {
                continue;
            }
            activityBOMList.add(activityRowData);

        }
        if (activityBOMList.size() > 0) {
            ActivityMasterData activityMasterData = new ActivityMasterData(operationItemData, operationUnderRowCount, TC_TYPE_CLASS_NAME_ACTIVITY, tree.getColumns());
            activityMasterData.setText(new String[] { "Activity Master", activityMasterData.getClassType() });
            activityMasterData.setData(operationItemData.getItemId());
            ++operationUnderRowCount;
            for (ArrayList<String> activityBOMData : activityBOMList) {
                setSubActivity(activityMasterData, activityBOMData, activityMasterWb);
            }
        }
    }

    /**
     * 공법(Operation) Sub Activity 정보를 생성한다.
     * 
     * @method setSubActivity
     * @date 2013. 11. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setSubActivity(ActivityMasterData activityMasterData, ArrayList<String> activityBOMData, Workbook activityMasterWb) throws Exception {
        String operationId = (String) activityMasterData.getData();
        Sheet sheet = activityMasterWb.getSheetAt(0);
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            ArrayList<String> activitySubRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.ACTIVITY_MASTER_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                activitySubRowData.add(cellText);
            }
            // EOF이면 return..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(activitySubRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                return;
            }
            // 같은 Operation이 아니면 Continue...
            if (!operationId.equals(getOperationMasterItemId(activitySubRowData))) {
                continue;
            }
            // BOM - MASTER가 같은 작업순서(SEQ)가 아니면 Continue...
            if (!activityBOMData.get(PEExcelConstants.ACTIVITY_BOM_SEQ_COLUMN_INDEX).equals(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_SEQ_COLUMN_INDEX))) {
                continue;
            }
            ActivitySubData activitySubData = new ActivitySubData(activityMasterData, activityMasterData.getItemCount(), TC_TYPE_CLASS_NAME_ACTIVITY_SUB, tree.getColumns());
            activitySubData.setText(new String[] { activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_SEQ_COLUMN_INDEX), activitySubData.getClassType() });
            activitySubData.setData(activitySubRowData);
        }
    }

    /**
     * 공법(Operation)에 할당될 설비(EQUIPMENT) 정보를 생성한다. - BOM
     * 
     * @method setEquipment
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setEquipment(OperationItemData operationItemData) throws Exception {
        Workbook equipmentBOMWb = ImportExcelServce.getWorkBook(TC_TYPE_EQUIPMENT_FILEPATH[1]); // EQUIPMENT BOM file path
        Sheet sheet = equipmentBOMWb.getSheetAt(0);
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            ArrayList<String> equipmentRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.EQUIPMENT_BOM_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                equipmentRowData.add(cellText);
            }
            // EOF이면 return..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(equipmentRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                return;
            }
            // 같은 Operation이 아니면 Continue...
            if (!getOperationItemId(equipmentRowData).equals(operationItemData.getItemId())) {
                continue;
            }
            EquipmentData equipmentData = new EquipmentData(operationItemData, operationUnderRowCount, TC_TYPE_CLASS_NAME_EQUIPMENT, tree.getColumns());
            equipmentData.setText(new String[] { equipmentRowData.get(PEExcelConstants.EQUIPMENT_BOM_TEM_ID_COLUMN_INDEX), equipmentData.getClassType() });
            equipmentData.setItemId(equipmentRowData.get(PEExcelConstants.EQUIPMENT_BOM_TEM_ID_COLUMN_INDEX));
            equipmentData.setData(PEExcelConstants.BOM, equipmentRowData);
            // Resource Master 데이터 설정
            setResourceMaster(TC_TYPE_EQUIPMENT_FILEPATH[0], PEExcelConstants.EQUIPMENT_MASTER_END_COLUMN_INDEX, PEExcelConstants.EQUIPMENT_MASTER_ITEM_ID_COLUMN_INDEX, equipmentData);
            ++operationUnderRowCount;
        }
    }

    /**
     * 공법(Operation)에 할당될 Tool 정보를 생성한다. - BOM
     * 
     * @method setTool
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setTool(OperationItemData operationItemData) throws Exception {
        // Workbook toolMasterWb = ImportExcelServce.getWorkBook(TC_TYPE_TOOL_FILEPATH[0]); // TOOLS Master file path
        Workbook toolBOMWb = ImportExcelServce.getWorkBook(TC_TYPE_TOOL_FILEPATH[1]); // TOOLS BOM file path
        Sheet sheet = toolBOMWb.getSheetAt(0);
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            ArrayList<String> toolRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.TOOL_BOM_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                toolRowData.add(cellText);
            }
            // EOF이면 return..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(toolRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                return;
            }
            // 같은 Operation이 아니면 Continue...
            if (!getOperationItemId(toolRowData).equals(operationItemData.getItemId())) {
                continue;
            }
            ToolData toolData = new ToolData(operationItemData, operationUnderRowCount, TC_TYPE_CLASS_NAME_TOOL, tree.getColumns());
            toolData.setText(new String[] { toolRowData.get(PEExcelConstants.TOOL_BOM_TOOL_NO_COLUMN_INDEX), toolData.getClassType() });
            toolData.setItemId(toolRowData.get(PEExcelConstants.TOOL_BOM_TOOL_NO_COLUMN_INDEX));
            toolData.setData(PEExcelConstants.BOM, toolRowData);
            // Resource Master 데이터 설정
            setResourceMaster(TC_TYPE_TOOL_FILEPATH[0], PEExcelConstants.TOOL_MASTER_END_COLUMN_INDEX, PEExcelConstants.TOOL_MASTER_ITEM_ID_COLUMN_INDEX, toolData);
            ++operationUnderRowCount;
        }
    }

    /**
     * 공법(Operation)에 할당될 설비(EQUIPMENT), 공구(Tool) 정보를 생성한다. - MASTER
     * 
     * @method setResourceMaster
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setResourceMaster(String execlFilePath, int endColumnIndexIndex, int masterItemIndex, OccurrenceData occurrenceData) throws Exception {
        Workbook resourceMasterWb = ImportExcelServce.getWorkBook(execlFilePath); // Master file path
        Sheet sheet = resourceMasterWb.getSheetAt(0);
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            ArrayList<String> occurrenceDataRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= endColumnIndexIndex; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                occurrenceDataRowData.add(cellText);
            }
            // EOF이면 return..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(occurrenceDataRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                return;
            }
            // 같은 Operation이 아니면 Continue...
            if (occurrenceDataRowData.get(masterItemIndex).equals(occurrenceData.getItemId())) {
                occurrenceData.setData(PEExcelConstants.MASTER, occurrenceDataRowData);
            }
        }
    }

    /**
     * 공법(Operation)에 할당될 End Item (일반자재) 정보를 생성한다.
     * 
     * @method setEndItem
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setEndItem(OperationItemData operationItemData) throws Exception {
        // Workbook endItemMasterWb = ImportExcelServce.getWorkBook(TC_TYPE_END_ITEM_FILEPATH[0]); // END ITEM Master file path
        Workbook endItemBOMWb = ImportExcelServce.getWorkBook(TC_TYPE_END_ITEM_FILEPATH[1]); // END ITEM BOM file path
        Sheet sheet = endItemBOMWb.getSheetAt(0);
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            ArrayList<String> endItemRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.END_ITEM_BOM_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                endItemRowData.add(cellText);
            }
            // EOF이면 return..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(endItemRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                return;
            }
            // 같은 Operation이 아니면 Continue...
            if (!getOperationItemId(endItemRowData).equals(operationItemData.getItemId())) {
                continue;
            }
            EndItemData endItemData = new EndItemData(operationItemData, operationUnderRowCount, TC_TYPE_CLASS_NAME_END_ITEM, tree.getColumns());
            endItemData.setText(new String[] { endItemRowData.get(PEExcelConstants.END_ITEM_BOM_PART_NO_COLUMN_INDEX), endItemData.getClassType() });
            endItemData.setItemId(endItemRowData.get(PEExcelConstants.END_ITEM_BOM_PART_NO_COLUMN_INDEX));
            endItemData.setAbsOccPuids(endItemRowData.get(PEExcelConstants.END_ITEM_BOM_ABS_OCCPUIDS_COLUMN_INDEX));
            endItemData.setOccPuid(endItemRowData.get(PEExcelConstants.END_ITEM_BOM_OCCPUID_COLUMN_INDEX));
            endItemData.setFunctionItemId(endItemRowData.get(PEExcelConstants.END_ITEM_BOM_FUNCTION_PART_NO_COLUMN_INDEX));
            endItemData.setFindNo(endItemRowData.get(PEExcelConstants.END_ITEM_BOM_SEQ_COLUMN_INDEX));
            ++operationUnderRowCount;
        }
    }

    /**
     * 공법(Operation)에 SubsidiaryMaterial (부자재) 정보를 생성한다.
     * 
     * @method setSubsidiaryData
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setSubsidiaryData(OperationItemData operationItemData) throws Exception {
        // Workbook subsidiaryMasterWb = ImportExcelServce.getWorkBook(TC_TYPE_SUBSIDIARY_FILEPATH[0]); // SubsidiaryMaterial Master file path
        Workbook subsidiaryBOMWb = ImportExcelServce.getWorkBook(TC_TYPE_SUBSIDIARY_FILEPATH[1]); // SubsidiaryMaterial BOM file path
        Sheet sheet = subsidiaryBOMWb.getSheetAt(0);
        for (int i = PEExcelConstants.START_ROW_INDEX; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            ArrayList<String> subsidiaryRowData = new ArrayList<String>();
            for (int j = PEExcelConstants.START_COLUMN_INDEX; j <= PEExcelConstants.SUBSIDIARY_BOM_END_COLUMN_INDEX; j++) {
                Cell cell = row.getCell(j);
                String cellText = ImportExcelServce.getCellText(cell);
                subsidiaryRowData.add(cellText);
            }
            // EOF이면 return..
            if (PEExcelConstants.END_CHAR_ROW_STRING.equals(subsidiaryRowData.get(PEExcelConstants.END_CHAR_COLUMN_INDEX))) {
                return;
            }
            // 같은 Operation이 아니면 Continue...
            if (!getOperationItemId(subsidiaryRowData).equals(operationItemData.getItemId())) {
                continue;
            }
            SubsidiaryData subsidiaryData = new SubsidiaryData(operationItemData, operationUnderRowCount, TC_TYPE_CLASS_NAME_SUBSIDIARY, tree.getColumns());
            subsidiaryData.setText(new String[] { subsidiaryRowData.get(PEExcelConstants.SUBSIDIARY_BOM_ITEM_ID_COLUMN_INDEX), subsidiaryData.getClassType() });
            subsidiaryData.setItemId(subsidiaryRowData.get(PEExcelConstants.SUBSIDIARY_BOM_ITEM_ID_COLUMN_INDEX));
            subsidiaryData.setData(PEExcelConstants.BOM, subsidiaryRowData);
            subsidiaryData.setFindNo(subsidiaryRowData.get(PEExcelConstants.SUBSIDIARY_BOM_SEQ_COLUMN_INDEX));
            ++operationUnderRowCount;
        }
    }

    /**
     * Tree Column 생성
     * 
     * @method createTreeColumn
     * @date 2013. 11. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createTreeColumn() {
        // Column 초기화
        while (tree.getColumns().length > 0) {
            tree.getColumns()[0].dispose();
        }

        // Tree Column 생성
        // Column Info 설정.
        ArrayList<TreeColumnInfo> treeColumnInfoList = new ArrayList<TreeColumnInfo>();
        TreeColumnInfo objectIdInfo = new TreeColumnInfo();
        objectIdInfo.setId("object_id");
        objectIdInfo.setName("Object ID");
        objectIdInfo.setWidth(300);
        treeColumnInfoList.add(objectIdInfo);

        TreeColumnInfo classTypeInfo = new TreeColumnInfo();
        classTypeInfo.setId("class_type");
        classTypeInfo.setName("Class Type");
        classTypeInfo.setWidth(105);
        treeColumnInfoList.add(classTypeInfo);

        // TreeColumnInfo objectNameInfo = new TreeColumnInfo();
        // objectNameInfo.setId("object_name");
        // objectNameInfo.setName("Object Name");
        // objectNameInfo.setWidth(210);
        // treeColumnInfoList.add(objectNameInfo);

        for (int i = 0; i < treeColumnInfoList.size(); i++) {
            TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
            treeColumn.setData(treeColumnInfoList.get(i).getId());
            treeColumn.setWidth(treeColumnInfoList.get(i).getWidth());
            treeColumn.setText(treeColumnInfoList.get(i).getName());
        }
        // Status Column Add
        TreeColumn statusColumn = new TreeColumn(tree, SWT.NONE);
        statusColumn.setData("column_status_info");
        statusColumn.setWidth(400);
        statusColumn.setText("Status");
        tree.setHeaderVisible(true);
    }

    /**
     * Row Expand All 실행전 초기화 처리
     * 
     * @method expandAllTCDataItemPre
     * @date 2013. 11. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllTCDataItemPre(int status) throws Exception {
        // 경고 List Clear
        problemItemList.clear();
        if (status == PEIFTCDataExecuteJob.STATUS_VALIDATE) {
            peValidation.expandAllTCDataItemPre();
        } else if (status == PEIFTCDataExecuteJob.STATUS_EXECUTE) {
            peExecution.expandAllTCDataItemPre();
        }
        // 시작Log
        printChangeStatusLog(status, true);
    }

    /**
     * Row Expand All 실행 후 처리
     * 
     * @method expandAllTCDataItemPost
     * @date 2013. 11. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllTCDataItemPost(int status, ArrayList<TCData> expandAllItems) throws Exception {
        if (status == PEIFTCDataExecuteJob.STATUS_VALIDATE) {
            peValidation.expandAllTCDataItemPost(expandAllItems);
        } else if (status == PEIFTCDataExecuteJob.STATUS_EXECUTE) {
            peExecution.expandAllTCDataItemPost(expandAllItems);
        }
        // 종료 Log
        printChangeStatusLog(status, false);
    }

    /**
     * 
     * @method printChangeStatusLog
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printChangeStatusLog(int status, boolean isStart) throws Exception {
        StringBuffer startLog = new StringBuffer();
        // 시작
        if (isStart) {
            // 상태가 VALIDATION 상태일때 ExpandAll 시 Row 별로 실행되는 메소드
            if (status == PEIFTCDataExecuteJob.STATUS_VALIDATE) {
                startLog.append("---------------------------------------------------------------------------------------\n");
                startLog.append("**************** Step 1. 검증 Start... ****************\n");
            }
            // 상태가 EXECUTE 상태일때 ExpandAll 시 Row 별로 실행되는 메소드
            else if (status == PEIFTCDataExecuteJob.STATUS_EXECUTE) {
                startLog.append("**************** Step 2. 실행 Start... ****************\n");
            }
        }
        // 종료
        else {
            // 에러, 경고 메세지 처리
            startLog.append(printWaringExpandAllComplete(peIFTCDataExecuteJob.getStatus()));
            // 상태가 VALIDATION 상태일때 ExpandAll 시 Row 별로 실행되는 메소드
            if (status == PEIFTCDataExecuteJob.STATUS_VALIDATE) {
                startLog.append("**************** Step 1. 검증 End... ******************\n");
            }
            // 상태가 EXECUTE 상태일때 ExpandAll 시 Row 별로 실행되는 메소드
            else if (status == PEIFTCDataExecuteJob.STATUS_EXECUTE) {
                startLog.append("**************** Step 2. 실행 End... ******************\n");
                startLog.append("---------------------------------------------------------------------------------------\n");
            }
        }
        // Log Text 처리
       // ImportCoreService.syncSetItemTextField(shell, peIFTCDataExecuteJob.getLogText(), startLog.toString());
        peIFTCDataExecuteJob.getLogText().append(startLog.toString() + "\n"); 
        // Log 파일 처리
        ImportCoreService.saveLogFile(getLogFilePath(), startLog.toString());
    }

    /**
     * 경고 메세지 처리
     * 
     * @method printWaringExpandAllComplete
     * @date 2013. 11. 29.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String printWaringExpandAllComplete(int status) throws Exception {
        StringBuffer problemLog = new StringBuffer();
        // 경고 항목이 있으면 출력..
        // if (problemItemList.size() > 0) {
        ArrayList<TCData> errorList = new ArrayList<TCData>();
        ArrayList<TCData> waringList = new ArrayList<TCData>();
        for (int i = 0; i < problemItemList.size(); i++) {
            if (TCData.STATUS_ERROR == problemItemList.get(i).getStatus()) {
                errorList.add(problemItemList.get(i));
            } else {
                waringList.add(problemItemList.get(i));
            }
        }
        // 각 에러, 경고 메세지 출력
        problemLog.append("\n\n\n");
        problemLog.append(printErrorList(errorList));
        problemLog.append("\n");
        problemLog.append(printWaringList(waringList));
        // }
        return problemLog.toString();
    }

    /**
     * Total Error 메세지 출력
     * 
     * @method printErrorList
     * @date 2013. 12. 3.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String printErrorList(ArrayList<TCData> errorList) {
        StringBuffer errorLog = new StringBuffer();
        errorLog.append("[에러] 총 : '" + errorList.size() + "' 개\n");
        for (int i = 0; i < errorList.size(); i++) {
            String message = (errorList.get(i)).getSyncStatusMessage(shell);
            String objectName = (errorList.get(i)).getSyncText(shell);
            errorLog.append("\t" + (i + 1) + ") " + objectName + " - " + message + "\n");
        }
        return errorLog.toString();
    }

    /**
     * Total Waring 메세지 출력
     * 
     * @method printWaringList
     * @date 2013. 12. 3.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String printWaringList(ArrayList<TCData> waringList) {
        StringBuffer waringLog = new StringBuffer();
        waringLog.append("[경고] 총 : '" + waringList.size() + "' 개\n");
        for (int i = 0; i < waringList.size(); i++) {
            String message = (waringList.get(i)).getSyncStatusMessage(shell);
            String objectName = (waringList.get(i)).getSyncText(shell);
            waringLog.append("\t" + (i + 1) + ") " + objectName + " - " + message + "\n");
        }
        return waringLog.toString();
    }

    /**
     * Expan All시 Row 별로 Call 되는 메소드
     * 
     * @method setDataItem
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setDataItem(int status, int index, TCData tcData) throws Exception {
        // 상태가 VALIDATION 상태일때 ExpandAll 시 Row 별로 실행되는 메소드
        if (status == PEIFTCDataExecuteJob.STATUS_VALIDATE) {
            rowValidate(index, tcData);
        }
        // 상태가 EXECUTE 상태일때 ExpandAll 시 Row 별로 실행되는 메소드
        else if (status == PEIFTCDataExecuteJob.STATUS_EXECUTE) {
            rowExecute(index, tcData);
        }
        // 에러(STATUS_ERROR), 경고(STATUS_WARNING)이면 상태 List에 추가
        if (TCData.STATUS_ERROR == tcData.getStatus() || TCData.STATUS_WARNING == tcData.getStatus()) {
            problemItemList.add(tcData);
        }
    }

    /**
     * STATUS = VALIDATE >> Expand All 시 실행
     * 
     * @method rowValidate
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void rowValidate(int index, TCData tcData) throws Exception {
        peValidation.rowValidate(index, tcData);
    }

    /**
     * STATUS = EXECUTE >> Expand All 시 실행
     * 
     * @method rowExecute
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void rowExecute(int index, TCData tcData) throws Exception {
        peExecution.rowExecute(index, tcData);
    }

    /**
     * @return the logFilePath
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * M-Product를 Top으로 가지는 BOM Window 생성 과 하위 Function BOMLine List 초기화
     * 
     * @method initFunctionList
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initFunctionList() throws Exception {
        try {
            // 관련정보 초기화
            topLineMproduct = null;
            functionContexts = null;
            // TODO: M-BOM Released 기준으로 BOM을 조회하여야하나 Option Condition 등록시 속도 저하가 발생하여 수정 후 테스트 할 것..
            // mBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(processLine.window().getTopBOMLine(), "Latest Working", "bom_view");
            // // mBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(processLine.window().getTopBOMLine(), "Latest Released", "bom_view");
            // topLineMproduct = mBOMWindow.getTopBOMLine();

            IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
            for (IViewReference viewRerence : arrayOfIViewReference) {
                IViewPart localIViewPart = viewRerence.getView(false);
                if (localIViewPart == null)
                    continue;
                CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
                if (cmeBOMTreeTable == null)
                    continue;
                TCComponentBOMLine rootBomline = cmeBOMTreeTable.getBOMRoot();
//                MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(rootBomline);
//                // Occurrence 그룹 일 경우
//                if (mfgType == MFGStructureType.Product) {
//                    topLineMproduct = rootBomline;
//                    break;
//                }
                boolean isProduct = MFGStructureTypeUtil.isProduct(rootBomline);
                if(isProduct) {
                	topLineMproduct = rootBomline;
                	break;
                }
            }
            if (topLineMproduct == null) {
                throw new ValidateSDVException("M-PRODUCT View 화면을 Open하세요.");
            }
            // PRODUCT 하위 FUNCTION BOMLINE을 검색
            functionContexts = topLineMproduct.getChildren();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * ROW Data를 가지고 Line Item ID를 조합하여 가져온다.
     * 
     * @method getLineItemId
     * @date 2013. 12. 5.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getLineItemId(ArrayList<String> rowData) throws Exception {
        // COMMON_BOM_PLANT_CODE_COLOUMN_INDEX + "-" + COMMON_BOM_SHOP_CODE_COLOUMN_INDEX + "-" + COMMON_BOM_LINE_CODE_COLOUMN_INDEX
        // + "-" + COMMON_BOM_PRODUCT_NO_COLOUMN_INDEX + "-" + COMMON_BOM_PLANNING_VERSION_COLOUMN_INDEX
        StringBuffer lineItemId = new StringBuffer();
        lineItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX));
        lineItemId.append("-");
        lineItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX));
        lineItemId.append("-");
        lineItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));
        lineItemId.append("-");
        lineItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));
        lineItemId.append("-");
        lineItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));
        return lineItemId.toString();
    }

    /**
     * ROW Data를 가지고 Operation Item ID를 조합하여 가져온다.
     * 
     * @method getOperationItemId
     * @date 2013. 12. 5.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getOperationItemId(ArrayList<String> rowData) throws Exception {
        // OPERATION ITEM ID :
        // COMMON_BOM_PROJECT_NO_COLOUMN_INDEX + "-" + COMMON_BOM_LINE_CODE_COLOUMN_INDEX
        // + "-" + COMMON_BOM_SHEET_NO_COLOUMN_INDEX + "-" + COMMON_BOM_PLANNING_VERSION_COLOUMN_INDEX
        StringBuffer operationItemId = new StringBuffer();
        operationItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));
        operationItemId.append("-");
        operationItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));
        operationItemId.append("-");
        operationItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));
        operationItemId.append("-");
        operationItemId.append(getItemRowString(rowData, PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));
        return operationItemId.toString();
    }

    /**
     * Row Data를 가져오고 검증한다.
     * 
     * @method getItemRowString
     * @date 2013. 12. 5.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getItemRowString(ArrayList<String> rowData, int rowIndex) throws ValidateSDVException {
        try {
            if (StringUtils.isEmpty(rowData.get(rowIndex))) {
                throw new ValidateSDVException("ROW 데이터를 읽는 중 필요데이터가 null 입니다.");
            }
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage() + " > rowIndex : " + rowIndex, e);
        }
        return rowData.get(rowIndex);
    }

    /**
     * 
     * @method saveLog
     * @date 2013. 12. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveLog(String logString) {
        ImportCoreService.syncSetItemTextField(shell, peIFTCDataExecuteJob.getLogText(), logString + "\n");
        try {
            ImportCoreService.saveLogFile(getLogFilePath(), logString);
        } catch (Exception logEx) {
            logEx.printStackTrace();
        }
    }
    
//    public boolean isReinterface() {
//		return isReinterface;
//	}
//
//	public void setReinterface(boolean isReinterface) {
//		this.isReinterface = isReinterface;
//	}

}
