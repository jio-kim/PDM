package com.kgm.commands.ec.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.rac.kernel.SYMCPartListData;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20140417] Project code Old/New 분리 대응
 * [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
 * @author bs
 *
 */
public class ECOReportOperation extends AbstractAIFOperation {

	private TCSession session;
	
	private TCComponentChangeItemRevision changeRevision;
	private File templateFile;
	private File reportFile;
	private HashMap<String, String> propertyMap;
//	private CustomECODao dao = new CustomECODao();
	
	public ECOReportOperation(TCSession session, TCComponentChangeItemRevision changeRevision, File reportFile) {
		this.session = session;
		this.changeRevision = changeRevision;
		this.reportFile = reportFile;
	}

	@Override
	public void executeOperation() throws Exception {

		session.setStatus("ECO reporting...");
		try{
			String datasetname = "ECO_Report_template";

			//##
			session.setStatus(">> 1/4 Report file create.");
			if(reportFile == null){
				reportFile = new File("C:\\Temp\\"+changeRevision.getProperty("item_id")+"_Report.xls");
			}
			
			//##
			session.setStatus(">> 2/4 Template file download.");
			if(!templateFileDownload(datasetname)) return;

			//##
			session.setStatus(">> 3/4 Create sheet.");
			if(!createReport()) return;
			
			session.setStatus(">> 4/4 Open.");
			if(JOptionPane.showConfirmDialog(AIFUtility.getActiveDesktop()
					, "Do you want to open the file?"
					, "Excel Export", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
				openLFile();
			}

		} catch(Exception e){
			e.printStackTrace();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "ERROR", MessageBox.ERROR);
		}finally{
			session.setReadyStatus();
			if(templateFile != null && templateFile.exists())
				templateFile.delete();
		}
	}	
	
	/**
	 * 템플릿 다운받기
	 * @param type
	 * @return file
	 * @throws TCException 
	 * @throws Exception
	 */
	private boolean templateFileDownload(String datasetname) throws Exception{
		TCComponent[] searchedDatasetList = CustomUtil.queryComponent("Dataset...",
				new String[] {"Name", "DatasetType"},
				new String[] { datasetname, "MSExcel" });
		
		if(searchedDatasetList != null && searchedDatasetList.length != 0){
			TCComponentDataset ecnTemp = (TCComponentDataset)searchedDatasetList[0];
			File[] file =  ecnTemp.getFiles("excel");
			templateFile = file[0];
			return true;
		}else{
			throw new Exception("Cannot find excelTemplate file");
		}
	}
	
	private boolean createReport() throws Exception{
		Workbook workbookTemp = Workbook.getWorkbook(templateFile);
		WritableWorkbook workBook = Workbook.createWorkbook(reportFile, workbookTemp);

		if(!createASheet(workBook.getSheet(0))) return false;
		if(!createBSheet(workBook.getSheet(1))) return false;
		if(!createCSheet(workBook.getSheet(2))) return false;
		if(!createDSheet(workBook.getSheet(3))) return false;

		workBook.write();
		workBook.close();
		return true;
	}

	/**
	 * ECO-A
	 * @param sheet
	 * @return
	 * @throws Exception
	 */
	private boolean createASheet(WritableSheet sheet) throws Exception {
		String[] wantProperties = new String[]{"item_id","date_released","owning_group","s7_ECO_MATURITY","s7_PLANT_CODE","object_desc","s7_CHANGE_REASON","s7_EFFECT_POINT","s7_EFFECT_POINT_DATE","s7_DESIGN_REG_CATG","s7_PROJECT"};
		String[] valueProperties = changeRevision.getProperties(wantProperties);
		propertyMap = new HashMap<String, String>();
		for(int i=0; i < wantProperties.length ; i++){
			propertyMap.put(wantProperties[i], valueProperties[i]);
		}
		
		sheet.addCell( new Label(6, 1, propertyMap.get("date_released"), sheet.getCell(6, 1).getCellFormat()));
		sheet.addCell( new Label(7, 1, CustomUtil.getTODAY(), sheet.getCell(7, 1).getCellFormat()));
		sheet.addCell( new Label(2, 3, propertyMap.get("item_id"), sheet.getCell(2, 3).getCellFormat()));
		sheet.addCell( new Label(4, 3, propertyMap.get("date_released"), sheet.getCell(4, 3).getCellFormat()));
		sheet.addCell( new Label(6, 3, propertyMap.get("owning_group"), sheet.getCell(6, 3).getCellFormat()));
		sheet.addCell( new Label(2, 5, propertyMap.get("s7_PLANT_CODE"), sheet.getCell(2, 5).getCellFormat()));
		sheet.addCell( new Label(2, 6, propertyMap.get("s7_ECO_MATURITY"), sheet.getCell(2, 6).getCellFormat()));
		
		// [SR없음][20151124][jclee] ECR 정보 입력
		String sECRNos = propertyMap.get("s7_ECR_NO");
		sheet.addCell( new Label(2, 7, sECRNos, sheet.getCell(2, 7).getCellFormat()));
		
		String[] saECRNos = StringUtil.getSplitString(sECRNos, ",");
		String sECRDate = "";
		String sECRDept = "";
		for (int inx = 0; inx < saECRNos.length; inx++) {
			String sECRNo = saECRNos[inx];
			CustomECODao dao = new CustomECODao();
			
			// [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
			ArrayList<HashMap<String, String>> searchResult = dao.searchECREAI("", "", sECRNo, "", "", "");
			//ArrayList<HashMap<String, String>> searchResult = dao.searchECR("", "", sECRNo, "", "", "");
			
			if (searchResult.size() == 1) {
				HashMap<String, String> hmResult = searchResult.get(0);
				String sECRDateTemp = hmResult.get("CDATE").toString();
				String sECRDeptTemp = hmResult.get("CTEAM").toString();
				
				if (sECRDate.equals("")) {
					sECRDate = sECRDateTemp;
				} else {
					sECRDate = sECRDate + "," + sECRDateTemp;
				}
				
				if (sECRDept.equals("")) {
					sECRDept = sECRDeptTemp;
				} else {
					sECRDept = sECRDept + "," + sECRDeptTemp;
				}
			}
		}
//		sheet.addCell( new Label(4, 7, "", sheet.getCell(4, 7).getCellFormat()));
//		sheet.addCell( new Label(6, 7, "", sheet.getCell(6, 7).getCellFormat()));
		
		sheet.addCell( new Label(4, 7, sECRDate, sheet.getCell(4, 7).getCellFormat()));
		sheet.addCell( new Label(6, 7, sECRDept, sheet.getCell(6, 7).getCellFormat()));
		
		int rowID = 8;
		TCComponent[] relatedECIs = changeRevision.getRelatedComponents(SYMCECConstant.IMPLEMENTS_REL);
		int eciCount = relatedECIs.length;
		if(eciCount > 0){
			for(int i = 0 ; i < eciCount ; i++){
				if(i > 0){
					sheet.insertRow(rowID);
					for(int c = 1 ; c < 8 ; c++){
						sheet.mergeCells(6, rowID, 7, rowID);
						sheet.addCell( new Label(c, rowID, "", sheet.getCell(c, rowID-1).getCellFormat()));
					}
				}
				String[] eciProperties = relatedECIs[i].getProperties(new String[]{"item_id", "date_released", "owning_group"});
				sheet.addCell( new Label(2, rowID, eciProperties[0], sheet.getCell(2, rowID).getCellFormat()));
				sheet.addCell( new Label(4, rowID, eciProperties[1], sheet.getCell(2, rowID).getCellFormat()));
				sheet.addCell( new Label(6, rowID, eciProperties[2], sheet.getCell(2, rowID).getCellFormat()));

				rowID++;
			}
			sheet.mergeCells(1, rowID-eciCount, 1, rowID-1);
			sheet.mergeCells(3, rowID-eciCount, 1, rowID-1);
			sheet.mergeCells(5, rowID-eciCount, 1, rowID-1);
		}else{
			rowID++;
		}
		sheet.addCell( new Label(2, rowID, propertyMap.get("s7_CHANGE_REASON"), sheet.getCell(2, rowID).getCellFormat()));
		rowID++;
		sheet.addCell( new Label(2, rowID, propertyMap.get("s7_DESIGN_REG_CATG"), sheet.getCell(2, rowID).getCellFormat()));
		rowID++;
		String value = "";
		if(propertyMap.get("s7_EFFECT_POINT").equals("Y")){
			value= "ASAP";
		}else{
			value= propertyMap.get("s7_EFFECT_POINT_DATE");
		}
		sheet.addCell( new Label(2, rowID, value, sheet.getCell(2, rowID).getCellFormat()));
		rowID++;
		
		TCComponent[] concurrentECOs = changeRevision.getRelatedComponents(SYMCECConstant.CONCURRENT_ECO);
		String concurrentECOLists = "";
		for(TCComponent concurrentECO : concurrentECOs){
			// Reference Items에 붙은 Item 중 Concurrent ECO Revision에 대해서만 Report
			if (!concurrentECO.getType().equals("S7_ECORevision")) {
				continue;
			}
			
			if(concurrentECOLists.equals("")){
				concurrentECOLists = concurrentECO.getProperty("item_id");
			}else{
				concurrentECOLists = concurrentECOLists + ", " + concurrentECO.getProperty("item_id");
			}
		}
		sheet.addCell( new Label(2, rowID, concurrentECOLists, sheet.getCell(2, rowID).getCellFormat()));
		rowID++;
		sheet.addCell( new Label(2, rowID, propertyMap.get("object_desc"), sheet.getCell(2, rowID).getCellFormat()));
		rowID++;
		sheet.addCell( new Label(2, rowID, propertyMap.get("s7_PROJECT"), sheet.getCell(2, rowID).getCellFormat()));
		rowID++;
		
		if(changeRevision.getCurrentJob() != null){
			TCComponentTask rootTask = changeRevision.getCurrentJob().getRootTask();
			TCComponentTask[] subTasks = rootTask.getSubtasks();
			for(TCComponentTask subTask : subTasks) {
				if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) || subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
					String reviewTaskName = subTask.getName();
					if(reviewTaskName.equals("Creator")) continue;
					TCComponentTask performSignoffTask = subTask.getSubtask("perform-signoffs");
					TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
					if(signoffs.length == 0 ){
						sheet.addCell( new Label(2, rowID, reviewTaskName, sheet.getCell(2, rowID).getCellFormat()));
						rowID++;
					}else{
						for(int i = 0 ; i < signoffs.length ; i++) {
							if(i > 0){
								sheet.insertRow(rowID);
								for(int c = 1 ; c < 8 ; c++){
									sheet.addCell( new Label(c, rowID, "", sheet.getCell(c, rowID-1).getCellFormat()));
								}
							}
							TCComponentGroupMember groupMember = signoffs[i].getGroupMember();
							String[] groupMemberProperties = groupMember.getProperties(new String[]{"the_group","the_user"});
							String[] signoffProperties = signoffs[i].getProperties(new String[]{"decision_date","comments"});
							sheet.addCell( new Label(2, rowID, reviewTaskName, sheet.getCell(2, rowID).getCellFormat()));
							sheet.addCell( new Label(3, rowID, groupMemberProperties[0], sheet.getCell(3, rowID).getCellFormat()));
							sheet.addCell( new Label(4, rowID, groupMemberProperties[1], sheet.getCell(4, rowID).getCellFormat()));
							sheet.addCell( new Label(5, rowID, signoffProperties[0], sheet.getCell(5, rowID).getCellFormat()));
							sheet.addCell( new Label(6, rowID, signoffProperties[1], sheet.getCell(6, rowID).getCellFormat()));
							rowID++;
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param sheet
	 * @return
	 * @throws TCException 
	 * @throws WriteException 
	 * @throws RowsExceededException 
	 */
	@SuppressWarnings("unchecked")
	private boolean createBSheet(WritableSheet sheet) throws Exception {
		sheet.addCell( new Label(12, 1, propertyMap.get("date_released"), sheet.getCell(12, 1).getCellFormat()));
		sheet.addCell( new Label(13, 1, CustomUtil.getTODAY(), sheet.getCell(13, 1).getCellFormat()));
		
//		ArrayList<SYMCECODwgData> resultList = dao.selectECODwgList(propertyMap.get("item_id"));
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
        DataSet ds = new DataSet();
        ds.put("ecoNo", propertyMap.get("item_id"));
		ArrayList<SYMCECODwgData> resultList = (ArrayList<SYMCECODwgData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECODwgList", ds);
		
		int formatRow = 6;
		int rowNo = 5; // 엑셀 시작 ROW
		int insertRowNo = 1; // 엑셀 삽입 갯수
		
		if(resultList != null)
			for(SYMCECODwgData data : resultList){
				sheet.setRowView(rowNo, 320); // 입력 행의 높이 조절

				sheet.addCell( new Label(0, rowNo, insertRowNo+"", sheet.getCell(0, formatRow).getCellFormat()));
				sheet.addCell( new Label(1, rowNo, data.getProject(), sheet.getCell(1, formatRow).getCellFormat()));
				sheet.addCell( new Label(2, rowNo, data.getModelType(), sheet.getCell(2, formatRow).getCellFormat()));
				sheet.addCell( new Label(3, rowNo, data.getPartNo(), sheet.getCell(3, formatRow).getCellFormat()));
				sheet.addCell( new Label(4, rowNo, data.getPartName(), sheet.getCell(4, formatRow).getCellFormat()));
				sheet.addCell( new Label(5, rowNo, data.getCatProduct(), sheet.getCell(5, formatRow).getCellFormat()));
				sheet.addCell( new Label(6, rowNo, data.getHas2d(), sheet.getCell(6, formatRow).getCellFormat()));
				sheet.addCell( new Label(7, rowNo, data.getHas3d(), sheet.getCell(7, formatRow).getCellFormat()));			
				sheet.addCell( new Label(8, rowNo, data.getZip(), sheet.getCell(8, formatRow).getCellFormat()));
				sheet.addCell( new Label(9, rowNo, data.getsMode(), sheet.getCell(9, formatRow).getCellFormat()));
				sheet.addCell( new Label(10, rowNo, data.getChangeDesc(), sheet.getCell(10, formatRow).getCellFormat()));

				insertRowNo++;
				rowNo++;
			}
		return true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean createCSheet(WritableSheet sheet) throws Exception {
		sheet.addCell( new Label(17, 1, propertyMap.get("date_released"), sheet.getCell(17, 1).getCellFormat()));
		sheet.addCell( new Label(20, 1, CustomUtil.getTODAY(), sheet.getCell(20, 1).getCellFormat()));
		
//		ArrayList<SYMCBOMEditData> resultList = dao.selectECOEplList(propertyMap.get("item_id"));
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        DataSet ds = new DataSet();
        ds.put("ecoNo", propertyMap.get("item_id"));
        ArrayList<SYMCBOMEditData> resultList = (ArrayList<SYMCBOMEditData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOEplList", ds);
        
        HashMap map = new HashMap();
        map.put("F0", sheet.getCell(0, 5).getCellFormat());
        map.put("F1", sheet.getCell(1, 5).getCellFormat());
        map.put("F2", sheet.getCell(2, 5).getCellFormat());
        map.put("F3", sheet.getCell(3, 5).getCellFormat());
        map.put("F4", sheet.getCell(4, 5).getCellFormat());
        map.put("F5", sheet.getCell(5, 5).getCellFormat());
        map.put("F6", sheet.getCell(6, 5).getCellFormat());
        map.put("F7", sheet.getCell(7, 5).getCellFormat());
        map.put("F8", sheet.getCell(8, 5).getCellFormat());
        map.put("F9", sheet.getCell(9, 5).getCellFormat());
        map.put("F10", sheet.getCell(10, 5).getCellFormat());
        map.put("F11", sheet.getCell(11, 5).getCellFormat());
        map.put("F12", sheet.getCell(12, 5).getCellFormat());
        map.put("F13", sheet.getCell(13, 5).getCellFormat());
        map.put("F14", sheet.getCell(14, 5).getCellFormat());
        map.put("F15", sheet.getCell(15, 5).getCellFormat());
        map.put("F16", sheet.getCell(16, 5).getCellFormat());
        map.put("F17", sheet.getCell(17, 5).getCellFormat());
        map.put("F18", sheet.getCell(18, 5).getCellFormat());
        map.put("F19", sheet.getCell(19, 5).getCellFormat());
        map.put("F20", sheet.getCell(20, 5).getCellFormat());
        map.put("F21", sheet.getCell(21, 5).getCellFormat());
        map.put("CO0", sheet.getCell(0, 7).getCellFormat());
        map.put("CO1", sheet.getCell(1, 7).getCellFormat());
        map.put("CO2", sheet.getCell(2, 7).getCellFormat());
        map.put("CO3", sheet.getCell(3, 7).getCellFormat());
        map.put("CO4", sheet.getCell(4, 7).getCellFormat());
        map.put("CO5", sheet.getCell(5, 7).getCellFormat());
        map.put("CO6", sheet.getCell(6, 7).getCellFormat());
        map.put("CO7", sheet.getCell(7, 7).getCellFormat());
        map.put("CO8", sheet.getCell(8, 7).getCellFormat());
        map.put("CO9", sheet.getCell(9, 7).getCellFormat());
        map.put("CO10", sheet.getCell(10, 7).getCellFormat());
        map.put("CO11", sheet.getCell(11, 7).getCellFormat());
        map.put("CO12", sheet.getCell(12, 7).getCellFormat());
        map.put("CO13", sheet.getCell(13, 7).getCellFormat());
        map.put("CO14", sheet.getCell(14, 7).getCellFormat());
        map.put("CO15", sheet.getCell(15, 7).getCellFormat());
        map.put("CO16", sheet.getCell(16, 7).getCellFormat());
        map.put("CO17", sheet.getCell(17, 7).getCellFormat());
        map.put("CO18", sheet.getCell(18, 7).getCellFormat());
        map.put("CO19", sheet.getCell(19, 7).getCellFormat());
        map.put("CO20", sheet.getCell(20, 7).getCellFormat());
        map.put("CO21", sheet.getCell(21, 7).getCellFormat());
        map.put("CN0", sheet.getCell(0, 8).getCellFormat());
        map.put("CN1", sheet.getCell(1, 8).getCellFormat());
        map.put("CN2", sheet.getCell(2, 8).getCellFormat());
        map.put("CN3", sheet.getCell(3, 8).getCellFormat());
        map.put("CN4", sheet.getCell(4, 8).getCellFormat());
        map.put("CN5", sheet.getCell(5, 8).getCellFormat());
        map.put("CN6", sheet.getCell(6, 8).getCellFormat());
        map.put("CN7", sheet.getCell(7, 8).getCellFormat());
        map.put("CN8", sheet.getCell(8, 8).getCellFormat());
        map.put("CN9", sheet.getCell(9, 8).getCellFormat());
        map.put("CN10", sheet.getCell(10, 8).getCellFormat());
        map.put("CN11", sheet.getCell(11, 8).getCellFormat());
        map.put("CN12", sheet.getCell(12, 8).getCellFormat());
        map.put("CN13", sheet.getCell(13, 8).getCellFormat());
        map.put("CN14", sheet.getCell(14, 8).getCellFormat());
        map.put("CN15", sheet.getCell(15, 8).getCellFormat());
        map.put("CN16", sheet.getCell(16, 8).getCellFormat());
        map.put("CN17", sheet.getCell(17, 8).getCellFormat());
        map.put("CN18", sheet.getCell(18, 8).getCellFormat());
        map.put("CN19", sheet.getCell(19, 8).getCellFormat());
        map.put("CN20", sheet.getCell(20, 8).getCellFormat());
        map.put("CN21", sheet.getCell(21, 8).getCellFormat());
        map.put("WN0", sheet.getCell(0, 10).getCellFormat());
        map.put("WN1", sheet.getCell(1, 10).getCellFormat());
        map.put("WN2", sheet.getCell(2, 10).getCellFormat());
        map.put("WN3", sheet.getCell(3, 10).getCellFormat());
        map.put("WN4", sheet.getCell(4, 10).getCellFormat());
        map.put("WN5", sheet.getCell(5, 10).getCellFormat());
        map.put("WN6", sheet.getCell(6, 10).getCellFormat());
        map.put("WN7", sheet.getCell(7, 10).getCellFormat());
        map.put("WN8", sheet.getCell(8, 10).getCellFormat());
        map.put("WN9", sheet.getCell(9, 10).getCellFormat());
        map.put("WN10", sheet.getCell(10, 10).getCellFormat());
        map.put("WN11", sheet.getCell(11, 10).getCellFormat());
        map.put("WN12", sheet.getCell(12, 10).getCellFormat());
        map.put("WN13", sheet.getCell(13, 10).getCellFormat());
        map.put("WN14", sheet.getCell(14, 10).getCellFormat());
        map.put("WN15", sheet.getCell(15, 10).getCellFormat());
        map.put("WN16", sheet.getCell(16, 10).getCellFormat());
        map.put("WN17", sheet.getCell(17, 10).getCellFormat());
        map.put("WN18", sheet.getCell(18, 10).getCellFormat());
        map.put("WN19", sheet.getCell(19, 10).getCellFormat());
        map.put("WN20", sheet.getCell(20, 10).getCellFormat());
        map.put("WN21", sheet.getCell(21, 10).getCellFormat());
        map.put("WO0", sheet.getCell(0, 9).getCellFormat());
        map.put("WO1", sheet.getCell(1, 9).getCellFormat());
        map.put("WO2", sheet.getCell(2, 9).getCellFormat());
        map.put("WO3", sheet.getCell(3, 9).getCellFormat());
        map.put("WO4", sheet.getCell(4, 9).getCellFormat());
        map.put("WO5", sheet.getCell(5, 9).getCellFormat());
        map.put("WO6", sheet.getCell(6, 9).getCellFormat());
        map.put("WO7", sheet.getCell(7, 9).getCellFormat());
        map.put("WO8", sheet.getCell(8, 9).getCellFormat());
        map.put("WO9", sheet.getCell(9, 9).getCellFormat());
        map.put("WO10", sheet.getCell(10, 9).getCellFormat());
        map.put("WO11", sheet.getCell(11, 9).getCellFormat());
        map.put("WO12", sheet.getCell(12, 9).getCellFormat());
        map.put("WO13", sheet.getCell(13, 9).getCellFormat());
        map.put("WO14", sheet.getCell(14, 9).getCellFormat());
        map.put("WO15", sheet.getCell(15, 9).getCellFormat());
        map.put("WO16", sheet.getCell(16, 9).getCellFormat());
        map.put("WO17", sheet.getCell(17, 9).getCellFormat());
        map.put("WO18", sheet.getCell(18, 9).getCellFormat());
        map.put("WO19", sheet.getCell(19, 9).getCellFormat());
        map.put("WO20", sheet.getCell(20, 9).getCellFormat());
        map.put("WO21", sheet.getCell(21, 9).getCellFormat());

		int rowNo = 5;
		int insertRowNo = 1; // 엑셀 삽입 갯수

		int colCnt = 22;
		CellFormat[] oldFormat = new CellFormat[colCnt];
		CellFormat[] newFormat = new CellFormat[colCnt];
		
		if(resultList != null)
			for(SYMCBOMEditData data : resultList){
				sheet.setRowView(rowNo, 270); // 입력 행의 높이 조절

				if(insertRowNo == 1){
					for(int i = 0 ; i < colCnt ; i++){
						oldFormat[i] = (CellFormat) map.get("F"+i+"");
						newFormat[i] = (CellFormat) map.get("WN"+i+"");
					}
				}else{
					if(insertRowNo%2 == 0){
						for(int i = 0 ; i < colCnt ; i++){
							oldFormat[i] = (CellFormat) map.get("CO"+i+"");
							newFormat[i] = (CellFormat) map.get("CN"+i+"");
						}
					}else{
						for(int i = 0 ; i < colCnt ; i++){
							oldFormat[i] = (CellFormat) map.get("WO"+i+"");
							newFormat[i] = (CellFormat) map.get("WN"+i+"");
						}
					}
				}

				sheet.addCell( new Label(0, rowNo, insertRowNo+"", oldFormat[0]));
				// [20140417] Project code Old/New 분리 대응
				sheet.addCell( new Label(1, rowNo, data.getProjectOld(), oldFormat[1]));
				sheet.addCell( new Label(2, rowNo, data.getSeqOld(), oldFormat[2]));
				if(!"".equals(StringUtil.nullToString(data.getPartNoOld()))) {
					sheet.addCell( new Label(3, rowNo, data.getChangeType(), oldFormat[3]));
					sheet.addCell( new Label(4, rowNo, data.getParentNo(), oldFormat[4]));
				}else{
					sheet.addCell( new Label(3, rowNo, "", oldFormat[3]));
					sheet.addCell( new Label(4, rowNo, "", oldFormat[4]));
				}
				sheet.addCell( new Label(5, rowNo, data.getPartNoOld(), oldFormat[5]));
				sheet.addCell( new Label(6, rowNo, data.getPartNameOld(), oldFormat[6]));
				sheet.addCell( new Label(7, rowNo, data.getIcOld(), oldFormat[7]));
				sheet.addCell( new Label(8, rowNo, data.getSupplyModeOld(), oldFormat[8]));
				sheet.addCell( new Label(9, rowNo, data.getQtyOld(), oldFormat[9]));
				sheet.addCell( new Label(10, rowNo, data.getAltOld(), oldFormat[10]));
				sheet.addCell( new Label(11, rowNo, data.getSelOld(), oldFormat[11]));
				sheet.addCell( new Label(12, rowNo, data.getCatOld(), oldFormat[12]));
				sheet.addCell( new Label(13, rowNo, data.getColorSectionOld(), oldFormat[13]));
				sheet.addCell( new Label(14, rowNo, data.getModuleCodeOld(), oldFormat[14]));
				sheet.addCell( new Label(15, rowNo, data.getPltStkOld(), oldFormat[15]));
				sheet.addCell( new Label(16, rowNo, data.getAsStkOld(), oldFormat[16]));
				sheet.addCell( new Label(17, rowNo, "", oldFormat[17]));
				sheet.addCell( new Label(18, rowNo, "", oldFormat[18]));
				sheet.addCell( new Label(19, rowNo, data.getShownOnOld(), oldFormat[19]));
				sheet.addCell( new Label(20, rowNo, (data.getVcOld() != null)?data.getVcOld().toString():"", oldFormat[20]));
				sheet.addCell( new Label(21, rowNo, data.getChgDesc(), oldFormat[21]));

				rowNo++;

				sheet.addCell( new Label(0, rowNo, insertRowNo+"", newFormat[0]));
				// [20140417] Project code Old/New 분리 대응
				sheet.addCell( new Label(1, rowNo, data.getProjectNew(), newFormat[1]));
				sheet.addCell( new Label(2, rowNo, data.getSeqNew(), newFormat[2]));
				if(!"".equals(StringUtil.nullToString(data.getPartNoNew()))) {
					sheet.addCell( new Label(3, rowNo, data.getChangeType(), newFormat[3]));
					sheet.addCell( new Label(4, rowNo, data.getParentNo(), newFormat[4]));
				}else{
					sheet.addCell( new Label(3, rowNo, "", newFormat[3]));
					sheet.addCell( new Label(4, rowNo, "", newFormat[4]));
				}
				sheet.addCell( new Label(5, rowNo, data.getPartNoNew(), newFormat[5]));
				sheet.addCell( new Label(6, rowNo, data.getPartNameNew(), newFormat[6]));
				sheet.addCell( new Label(7, rowNo, data.getIcNew(), newFormat[7]));
				sheet.addCell( new Label(8, rowNo, data.getSupplyModeNew(), newFormat[8]));
				sheet.addCell( new Label(9, rowNo, data.getQtyNew(), newFormat[9]));
				sheet.addCell( new Label(10, rowNo, data.getAltNew(), newFormat[10]));
				sheet.addCell( new Label(11, rowNo, data.getSelNew(), newFormat[11]));
				sheet.addCell( new Label(12, rowNo, data.getCatNew(), newFormat[12]));
				sheet.addCell( new Label(13, rowNo, data.getColorSectionNew(), newFormat[13]));
				sheet.addCell( new Label(14, rowNo, data.getModuleCodeNew(), newFormat[14]));
				sheet.addCell( new Label(15, rowNo, "", newFormat[15]));
				sheet.addCell( new Label(16, rowNo, "", newFormat[16]));
				sheet.addCell( new Label(17, rowNo, data.getCostNew(), newFormat[17]));
				sheet.addCell( new Label(18, rowNo, data.getToolNew(), newFormat[18]));
				sheet.addCell( new Label(19, rowNo, data.getShownOnNew(), newFormat[19]));
				sheet.addCell( new Label(20, rowNo, (data.getVcNew() != null)?data.getVcNew().toString():"", newFormat[20]));
				sheet.addCell( new Label(21, rowNo, data.getChgDesc(), newFormat[21]));

				insertRowNo++;
				rowNo++;
			}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private boolean createDSheet(WritableSheet sheet) throws Exception {
		sheet.addCell( new Label(17, 1, propertyMap.get("date_released"), sheet.getCell(17, 1).getCellFormat()));
		sheet.addCell( new Label(20, 1, CustomUtil.getTODAY(), sheet.getCell(20, 1).getCellFormat()));
		
//		ArrayList<SYMCBOMEditData> resultList = dao.selectECOEplList(propertyMap.get("item_id"));
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        DataSet ds = new DataSet();
        ds.put("ecoNo", propertyMap.get("item_id"));
        ArrayList<SYMCPartListData> resultList = (ArrayList<SYMCPartListData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOPartList", ds);
         
		int formatRow = 6;
		int rowNo = 5; // 엑셀 시작 ROW
		int insertRowNo = 1; // 엑셀 삽입 갯수
//		int rowheight = sheet.getRowHeight(rowNo); // 템플릿의 입력 시작 행의 높이를 가져 옴

		if(resultList != null)
			for(SYMCPartListData data : resultList){

				sheet.addCell( new Label(0, rowNo, insertRowNo+"", sheet.getCell(0, formatRow).getCellFormat()));
				sheet.addCell( new Label(1, rowNo, data.getProject(), sheet.getCell(1, formatRow).getCellFormat()));
				sheet.addCell( new Label(2, rowNo, data.getSeq(), sheet.getCell(2, formatRow).getCellFormat()));
				sheet.addCell( new Label(3, rowNo, data.getChangeType(), sheet.getCell(3, formatRow).getCellFormat()));
				sheet.addCell( new Label(4, rowNo, data.getParentNo(), sheet.getCell(4, formatRow).getCellFormat()));
				sheet.addCell( new Label(5, rowNo, data.getPartNo(), sheet.getCell(5, formatRow).getCellFormat()));
				sheet.addCell( new Label(6, rowNo, data.getPartName(), sheet.getCell(6, formatRow).getCellFormat()));
				sheet.addCell( new Label(7, rowNo, "", sheet.getCell(7, formatRow).getCellFormat()));
				sheet.addCell( new Label(8, rowNo, data.getSupplyMode(), sheet.getCell(8, formatRow).getCellFormat()));
				sheet.addCell( new Label(9, rowNo, data.getQty(), sheet.getCell(9, formatRow).getCellFormat()));
				sheet.addCell( new Label(10, rowNo, data.getAlt(), sheet.getCell(10, formatRow).getCellFormat()));
				sheet.addCell( new Label(11, rowNo, data.getSel(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(12, rowNo, data.getCat(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(13, rowNo, data.getAsStk(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(14, rowNo, data.getCost(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(15, rowNo, data.getTool(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(16, rowNo, "", sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(17, rowNo, data.getCat(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(18, rowNo, data.getColorSection(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(19, rowNo, data.getShownOn(), sheet.getCell(11, formatRow).getCellFormat()));
				sheet.addCell( new Label(21, rowNo, data.getDesc(), sheet.getCell(11, formatRow).getCellFormat()));

				insertRowNo++;
				rowNo++;
			}
		return true;
	}
	
	private void openLFile(){
		if(reportFile.exists()){
			Thread thread = new Thread(new Runnable(){
				public void run(){
					try{
						String[] commandString = {"CMD", "/C", reportFile.getPath()};
						com.teamcenter.rac.util.Shell ishell = new com.teamcenter.rac.util.Shell(commandString);
						ishell.run();
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}
	}
}
