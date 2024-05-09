package com.kgm.commands.ec.report;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import jxl.write.Label;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DateUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.dto.TCEcoModel;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.rac.kernel.SYMCPartListData;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20140417] Project code Old/New 분리 대응
 * @author bs
 *
 */
public class ReportEPLOperation extends AbstractAIFOperation {

	private TCSession session;
	
	private TCComponentChangeItemRevision changeRevision;
	private File reportFile;
	private CustomECODao dao = new CustomECODao();
	
	public ReportEPLOperation(TCSession session, TCComponentChangeItemRevision changeRevision, File reportFile) {
		this.session = session;
		this.changeRevision = changeRevision;
		this.reportFile = reportFile;
	}

	@Override
	public void executeOperation() throws Exception {

		session.setStatus("ECO reporting...");
		try{

			//##
			session.setStatus(">> 1/3 Report file create.");
			if(reportFile == null){
				reportFile = new File("C:\\Temp\\"+changeRevision.getProperty("item_id")+"_Report.xls");
			}

			//##
			session.setStatus(">> 2/3 Create sheet.");
			if(!createReport()) return;
			
			session.setStatus(">> 3/3 Open.");
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
		}
	}

	private boolean createReport() {
        ExcelReportWithPoi ctrl = null;
        try{
            String[] sheetList = {"ECO_A", "ECO_B", "ECO_C", "ECO_D"};
            
            ctrl = new ExcelReportWithPoi(sheetList);
            
            for(int sheetNum = 0; sheetNum < sheetList.length; sheetNum++) {
                ctrl.setRepeatingRows(sheetNum, -1, -1, 0, 4);
            }
            
            short[] colWidthArray = {(short) 1, (short) 35, (short) 20, (short) 16, (short) 13, (short) 16, (short) 19, (short) 19 };
            
            for (int i = 0; i < colWidthArray.length; ++i) {
                ctrl.SetColWidthSetting(0, i, (short) (colWidthArray[i] * 256));
            }
            
            String ecoNo = changeRevision.getProperty("item_id");
            // [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
            TCEcoModel ecoInfo = dao.getEcoInfoEAI(ecoNo);
            //TCEcoModel ecoInfo = dao.getEcoInfo(ecoNo);
            
            setECRInfo(ecoInfo);
            ArrayList<HashMap<String, String>> workflowList = dao.getEcoWorkflowInfo(ecoNo);
            ArrayList<SYMCECODwgData> ecoDwgList = dao.selectECODwgList(ecoNo);
            ArrayList<SYMCBOMEditData> ecoEplList = dao.selectECOEplList(ecoNo);
            ArrayList<SYMCPartListData> ecoEndList = dao.selectECOPartList(ecoNo);
            
            //  Start of ECO_A
            this.excelEcoA(ctrl, ecoInfo, workflowList, 0);
            
            // Start of ECO_B
            this.excelEcoB(ctrl, ecoInfo, ecoDwgList, 1);
            
            // Start of ECO_C
            this.excelEcoC(ctrl, ecoInfo, ecoEplList, 2);
            
            // Start of ECO_D
            this.excelEcoD(ctrl, ecoInfo, ecoEndList, 3);
            
            ctrl.createExcelFile(reportFile.getAbsolutePath());
            
        } catch(Exception e){
			e.printStackTrace();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "ERROR", MessageBox.ERROR);
			return false;
        } finally{
            ctrl.distroy();
            ctrl = null;
            Runtime.getRuntime().gc();
        }
        
        return true;
    }
	
	/**
	 * ECR Info Setting
	 * @param ecoInfo
	 */
	private void setECRInfo(TCEcoModel ecoInfo) throws Exception {
		String sECRNos = ecoInfo.getEcrNo();
		
		if (sECRNos == null || sECRNos.equals("") || sECRNos.length() == 0) {
			return;
		}
		
		String[] saECRNos = StringUtil.getSplitString(sECRNos, ",");
		String sECRDate = "";
		String sECRDept = "";
		for (int inx = 0; inx < saECRNos.length; inx++) {
			String sECRNo = saECRNos[inx];
			CustomECODao dao = new CustomECODao();
			Calendar calDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA);
			
			// [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
			ArrayList<HashMap<String, String>> searchResult = dao.searchECREAI(null, sdf.format(calDate.getTime()), sECRNo, "%", "%", "%");
			//ArrayList<HashMap<String, String>> searchResult = dao.searchECR(null, sdf.format(calDate.getTime()), sECRNo, "%", "%", "%");
			
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
		
		ecoInfo.setEcrDate(sECRDate);
		ecoInfo.setEcrDept(sECRDept);
	}
	
	private void excelEcoA(ExcelReportWithPoi ctrl, TCEcoModel ecoInfo, ArrayList<HashMap<String, String>> workflowList, int sheetNum) {
        ctrl.createFont("13B_Blue");
        ctrl.setBoldWeight("13B_Blue", true);
        ctrl.setFontColor("13B_Blue", HSSFColor.BLUE.index);
        ctrl.setFontSize("13B_Blue", (short) 13);

        ctrl.createFont("10B_Blue");
        ctrl.setBoldWeight("10B_Blue", true);
        ctrl.setFontColor("10B_Blue", HSSFColor.BLUE.index);
        ctrl.setFontSize("10B_Blue", (short) 10);

        ctrl.createFont("9B_Blue");
        ctrl.setBoldWeight("9B_Blue", true);
        ctrl.setFontColor("9B_Blue", HSSFColor.BLUE.index);
        ctrl.setFontSize("9B_Blue", (short) 9);

        ctrl.createFont("11B_Black");
        ctrl.setBoldWeight("11B_Black", true);
        ctrl.setFontColor("11B_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("11B_Black", (short) 11);

        ctrl.createFont("9B_Black");
        ctrl.setBoldWeight("9B_Black", true);
        ctrl.setFontColor("9B_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("9B_Black", (short) 9);

        ctrl.createFont("9_Black");
        ctrl.setFontColor("9_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("9_Black", (short) 9);

        ctrl.createFont("12B_Red");
        ctrl.setBoldWeight("12B_Red", true);
        ctrl.setFontColor("12B_Red", HSSFColor.RED.index);
        ctrl.setFontSize("12B_Red", (short) 12);

        ctrl.createFont("11B_Blue");
        ctrl.setBoldWeight("11B_Blue", true);
        ctrl.setFontColor("11B_Blue", HSSFColor.BLUE.index);
        ctrl.setFontSize("11B_Blue", (short) 11);

        ctrl.createFont("11_Blue");
        ctrl.setFontColor("11_Blue", HSSFColor.BLUE.index);
        ctrl.setFontSize("11_Blue", (short) 11);

        ctrl.createStyleWithFont("Grey_T2B1L2R1_10B_Blue", "10B_Blue");
        ctrl.setBorderStyle("Grey_T2B1L2R1_10B_Blue", 2, 1, 2, 1);
        ctrl.setCellColor("Grey_T2B1L2R1_10B_Blue", HSSFColor.GREY_25_PERCENT.index);

        ctrl.createStyleWithFont("Grey_T1B2L2R1_10B_Blue", "10B_Blue");
        ctrl.setBorderStyle("Grey_T1B2L2R1_10B_Blue", 1, 2, 2, 1);
        ctrl.setCellColor("Grey_T1B2L2R1_10B_Blue", HSSFColor.GREY_25_PERCENT.index);

        ctrl.createStyleWithFont("Grey_T2B2L1R1_10B_Blue_Center", "10B_Blue");
        ctrl.setBorderStyle("Grey_T2B2L1R1_10B_Blue_Center", 2, 2, 1, 1);
        ctrl.setCellColor("Grey_T2B2L1R1_10B_Blue_Center", HSSFColor.GREY_25_PERCENT.index);
        ctrl.setAlignment("Grey_T2B2L1R1_10B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Grey_T2B1L1R1_10B_Blue_Center", "10B_Blue");
        ctrl.setBorderStyle("Grey_T2B1L1R1_10B_Blue_Center", 2, 1, 1, 1);
        ctrl.setCellColor("Grey_T2B1L1R1_10B_Blue_Center", HSSFColor.GREY_25_PERCENT.index);
        ctrl.setAlignment("Grey_T2B1L1R1_10B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Grey_T2B1L1R2_10B_Blue_Center", "10B_Blue");
        ctrl.setBorderStyle("Grey_T2B1L1R2_10B_Blue_Center", 2, 1, 1, 2);
        ctrl.setCellColor("Grey_T2B1L1R2_10B_Blue_Center", HSSFColor.GREY_25_PERCENT.index);
        ctrl.setAlignment("Grey_T2B1L1R2_10B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Grey_T1B2L1R1_9B_Black_Center", "9B_Black");
        ctrl.setBorderStyle("Grey_T1B2L1R1_9B_Black_Center", 1, 2, 1, 1);
        ctrl.setCellColor("Grey_T1B2L1R1_9B_Black_Center", HSSFColor.GREY_25_PERCENT.index);
        ctrl.setAlignment("Grey_T1B2L1R1_9B_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Grey_T1B2L1R2_9B_Black_Center", "9B_Black");
        ctrl.setBorderStyle("Grey_T1B2L1R2_9B_Black_Center", 1, 2, 1, 2);
        ctrl.setCellColor("Grey_T1B2L1R2_9B_Black_Center", HSSFColor.GREY_25_PERCENT.index);
        ctrl.setAlignment("Grey_T1B2L1R2_9B_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T2B2L2R2_12B_Red_Center", "12B_Red");
        ctrl.setBorderStyle("T2B2L2R2_12B_Red_Center", 2, 2, 2, 2);
        ctrl.setAlignment("T2B2L2R2_12B_Red_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Green_T2B2L2R1_11B_Blue_Center", "11B_Blue");
        ctrl.setBorderStyle("Green_T2B2L2R1_11B_Blue_Center", 2, 2, 2, 1);
        ctrl.setCellColor("Green_T2B2L2R1_11B_Blue_Center", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L2R1_11B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Green_T2B2L1R1_11B_Blue_Center", "11B_Blue");
        ctrl.setBorderStyle("Green_T2B2L1R1_11B_Blue_Center", 2, 2, 1, 1);
        ctrl.setCellColor("Green_T2B2L1R1_11B_Blue_Center", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L1R1_11B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T2B2L1R1_11B_Black_Center", "11B_Black");
        ctrl.setBorderStyle("T2B2L1R1_11B_Black_Center", 2, 2, 1, 1);
        ctrl.setAlignment("T2B2L1R1_11B_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T2B2L1R2_11B_Black_Center", "11B_Black");
        ctrl.setBorderStyle("T2B2L1R2_11B_Black_Center", 2, 2, 1, 2);
        ctrl.setAlignment("T2B2L1R2_11B_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Green_T2B1L2R1_10B_Blue", "10B_Blue");
        ctrl.setBorderStyle("Green_T2B1L2R1_10B_Blue", 2, 1, 2, 1);
        ctrl.setCellColor("Green_T2B1L2R1_10B_Blue", HSSFColor.LIGHT_GREEN.index);

        ctrl.createStyleWithFont("Green_T1B1L2R1_10B_Blue", "10B_Blue");
        ctrl.setBorderStyle("Green_T1B1L2R1_10B_Blue", 1, 1, 2, 1);
        ctrl.setCellColor("Green_T1B1L2R1_10B_Blue", HSSFColor.LIGHT_GREEN.index);

        ctrl.createStyleWithFont("Green_T1B2L2R1_10B_Blue", "10B_Blue");
        ctrl.setBorderStyle("Green_T1B2L2R1_10B_Blue", 1, 2, 2, 1);
        ctrl.setCellColor("Green_T1B2L2R1_10B_Blue", HSSFColor.LIGHT_GREEN.index);

        ctrl.createStyleWithFont("Green_T1B1L1R1_10B_Blue_Center", "10B_Blue");
        ctrl.setBorderStyle("Green_T1B1L1R1_10B_Blue_Center", 1, 1, 1, 1);
        ctrl.setCellColor("Green_T1B1L1R1_10B_Blue_Center", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T1B1L1R1_10B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Green_T1B1L1R1_9B_Blue", "9B_Blue");
        ctrl.setBorderStyle("Green_T1B1L1R1_9B_Blue", 1, 1, 1, 1);
        ctrl.setCellColor("Green_T1B1L1R1_9B_Blue", HSSFColor.LIGHT_GREEN.index);

        ctrl.createStyleWithFont("Green_T1B2L1R1_9B_Blue", "9B_Blue");
        ctrl.setBorderStyle("Green_T1B2L1R1_9B_Blue", 1, 2, 1, 1);
        ctrl.setCellColor("Green_T1B2L1R1_9B_Blue", HSSFColor.LIGHT_GREEN.index);

        ctrl.createStyleWithFont("T2B1L1R2_9_Black", "9_Black");
        ctrl.setBorderStyle("T2B1L1R2_9_Black", 2, 1, 1, 2);

        ctrl.createStyleWithFont("T1B1L1R2_9_Black", "9_Black");
        ctrl.setBorderStyle("T1B1L1R2_9_Black", 1, 1, 1, 2);

        ctrl.createStyleWithFont("T1B1L1R1_9_Black", "9_Black");
        ctrl.setBorderStyle("T1B1L1R1_9_Black", 1, 1, 1, 1);

        ctrl.createStyleWithFont("T1B1L1R1_9_Black_Center", "9_Black");
        ctrl.setBorderStyle("T1B1L1R1_9_Black_Center", 1, 1, 1, 1);
        ctrl.setAlignment("T1B1L1R1_9_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B1L1R2_9_Black_Center", "9_Black");
        ctrl.setBorderStyle("T1B1L1R2_9_Black_Center", 1, 1, 1, 2);
        ctrl.setAlignment("T1B1L1R2_9_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B2L1R1_9_Black_Center", "9_Black");
        ctrl.setBorderStyle("T1B2L1R1_9_Black_Center", 1, 2, 1, 1);
        ctrl.setAlignment("T1B2L1R1_9_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B2L1R2_9_Black_Center", "9_Black");
        ctrl.setBorderStyle("T1B2L1R2_9_Black_Center", 1, 2, 1, 2);
        ctrl.setAlignment("T1B2L1R2_9_Black_Center", HSSFCellStyle.ALIGN_CENTER);
        
        ctrl.createStyleWithFont("T1B2L1R2_9_Black", "9_Black");
        ctrl.setBorderStyle("T1B2L1R2_9_Black", 1, 2, 1, 2);
        
        int startRow = 0;
        int endRow = 1;

        /*
         *  Row No. 1-2
         */
        ctrl.fillDataWithStyle("Grey_T2B1L2R1_10B_Blue", sheetNum, startRow, 1, "     KG MOBILITY");
        ctrl.fillDataWithStyle("Grey_T1B2L2R1_10B_Blue", sheetNum, endRow, 1, "     Research & Develpoment Center");
        ctrl.fillDataWithStyleAndMerge("Grey_T2B2L1R1_10B_Blue_Center", sheetNum, startRow, 1, 2, 5, "All Rights Reserved by KG MOBILITY");
        ctrl.fillDataWithStyle("Grey_T2B1L1R1_10B_Blue_Center", sheetNum, startRow, 6, "ECO Date");
        ctrl.fillDataWithStyle("Grey_T2B1L1R2_10B_Blue_Center", sheetNum, startRow, 7, "Print Date");
        ctrl.fillDataWithStyle("Grey_T1B2L1R1_9B_Black_Center", sheetNum, endRow, 6, ecoInfo.getReleaseDate().equals("") ? "" : ecoInfo.getReleaseDate().substring(0, 10));
        ctrl.fillDataWithStyle("Grey_T1B2L1R2_9B_Black_Center", sheetNum, endRow, 7, CustomUtil.getTODAY());

        ctrl.setRowSize(0, startRow, endRow, 480); // 1 pixel : 15

        /*
         *  Row No. 3
         */
        
        startRow = endRow+1;
        endRow = startRow;

        ctrl.fillDataWithStyleAndMerge("T2B2L2R2_12B_Red_Center", sheetNum, startRow, 2, 1, 7, "*** ENGINEERING CHANGE ORDER-A (Information) ***");

        ctrl.setRowSize(0, startRow, 900);

        /*
         *  Row No. 4
         */
        
        startRow = endRow+1;
        endRow = startRow;

        ctrl.fillDataWithStyle("Green_T2B2L2R1_11B_Blue_Center", sheetNum, startRow, 1, "ECO Number");
        ctrl.fillDataWithStyle("T2B2L1R1_11B_Black_Center", sheetNum, startRow, 2, ecoInfo.getEcoNo());
        ctrl.fillDataWithStyle("Green_T2B2L1R1_11B_Blue_Center", sheetNum, startRow, 3, "ECO Date");
        ctrl.fillDataWithStyle("T2B2L1R1_11B_Black_Center", sheetNum, startRow, 4, ecoInfo.getReleaseDate().equals("") ? "" : ecoInfo.getReleaseDate().substring(0, 10));
        ctrl.fillDataWithStyle("Green_T2B2L1R1_11B_Blue_Center", sheetNum, startRow, 5, "ECO Dept");
        ctrl.fillDataWithStyleAndMerge("T2B2L1R2_11B_Black_Center", sheetNum, startRow, 3, 6, 7, ecoInfo.getOwningTeam());

        ctrl.setRowSize(sheetNum, startRow, 690);

        /*
         *  Row No. 5 - 18
         */
        startRow = endRow+1;
        endRow = startRow;

        ctrl.fillDataWithStyleAndMerge("T2B2L2R2_12B_Red_Center", sheetNum, startRow, 4, 1, 7, "");

        ctrl.setRowSize(sheetNum, 4, 360);
        
        startRow = endRow+1;
        endRow = startRow;

        ctrl.fillDataWithStyle("Green_T2B1L2R1_10B_Blue", sheetNum, endRow, 1, "           Plant Code");
        ctrl.fillDataWithStyleAndMerge("T2B1L1R2_9_Black", sheetNum, endRow, 5, 2, 7, ecoInfo.getPlantCode());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Status");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, 6, 2, 7, ecoInfo.getEcoStatus());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           ECR Number");
        ctrl.fillDataWithStyle("T1B1L1R1_9_Black", sheetNum, endRow, 2, ecoInfo.getEcrNo());
        ctrl.fillDataWithStyle("Green_T1B1L1R1_10B_Blue_Center", sheetNum, endRow, 3, "ECR Date");
        ctrl.fillDataWithStyle("T1B1L1R1_9_Black", sheetNum, endRow, 4, ecoInfo.getEcrDate());
        ctrl.fillDataWithStyle("Green_T1B1L1R1_10B_Blue_Center", sheetNum, endRow, 5, "ECR Dept");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 6, 7, ecoInfo.getEcrDept());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           ECI Number");
        ctrl.fillDataWithStyle("T1B1L1R1_9_Black", sheetNum, endRow, 2, ecoInfo.getEciNo());
        ctrl.fillDataWithStyle("Green_T1B1L1R1_10B_Blue_Center", sheetNum, endRow, 3, "ECI Date");
        ctrl.fillDataWithStyle("T1B1L1R1_9_Black", sheetNum, endRow, 4, ecoInfo.getEciReleaseDate());
        ctrl.fillDataWithStyle("Green_T1B1L1R1_10B_Blue_Center", sheetNum, endRow, 5, "ECI Dept");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 6, 7, ecoInfo.getEciDept());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Regulation & Safety");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 2, 7, ecoInfo.getRegNsafe());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Change Reason");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 2, 7, ecoInfo.getChangeReason());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Change Effective Point");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 2, 7, ecoInfo.getCfgEffectPoint());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Concurrent Implementation");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 2, 7, ecoInfo.getConcurrentImpl());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Change Description");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 2, 7, ecoInfo.getEcoTitle());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Affected Project");
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 2, 7, ecoInfo.getAffectedProject());
        ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, "           Signature");
        ctrl.fillDataWithStyle("Green_T1B1L1R1_9B_Blue", sheetNum, endRow, 2, "Prepared ");
        ctrl.fillDataWithStyle("T1B1L1R1_9_Black_Center", sheetNum, endRow, 3, ecoInfo.getOwningUser());
        ctrl.fillDataWithStyle("T1B1L1R1_9_Black_Center", sheetNum, endRow, 4, ecoInfo.getOwnerTel());
        ctrl.fillDataWithStyle("T1B1L1R1_9_Black_Center", sheetNum, endRow, 5, ecoInfo.getRequestDate());
        ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 6, 7, ecoInfo.getOwningTeam());
        
        for(int wfCnt=0 ; wfCnt < workflowList.size()-1 ; wfCnt++){
        	HashMap<String, String> workflowInfo = workflowList.get(wfCnt);
            ctrl.fillDataWithStyle("Green_T1B1L2R1_10B_Blue", sheetNum, ++endRow, 1, " ");
            ctrl.fillDataWithStyle("Green_T1B1L1R1_9B_Blue", sheetNum, endRow, 2, workflowInfo.get("TN"));
            ctrl.fillDataWithStyle("T1B1L1R1_9_Black_Center", sheetNum, endRow, 3, workflowInfo.get("UN"));
            ctrl.fillDataWithStyle("T1B1L1R1_9_Black_Center", sheetNum, endRow, 4, workflowInfo.get("TEL"));
            ctrl.fillDataWithStyle("T1B1L1R1_9_Black_Center", sheetNum, endRow, 5, workflowInfo.get("DCDATE"));
            ctrl.fillDataWithStyleAndMerge("T1B1L1R2_9_Black", sheetNum, endRow, endRow, 6, 7, workflowInfo.get("GN"));
        }
        if(workflowList.size() > 0){
        	HashMap<String, String> workflowInfo = workflowList.get(workflowList.size()-1);
            ctrl.fillDataWithStyle("Green_T1B2L2R1_10B_Blue", sheetNum, ++endRow, 1, " ");
            ctrl.fillDataWithStyle("Green_T1B2L1R1_9B_Blue", sheetNum, endRow, 2, workflowInfo.get("TN"));
            ctrl.fillDataWithStyle("T1B2L1R1_9_Black_Center", sheetNum, endRow, 3, workflowInfo.get("UN"));
            ctrl.fillDataWithStyle("T1B2L1R1_9_Black_Center", sheetNum, endRow, 4, workflowInfo.get("TEL"));
            ctrl.fillDataWithStyle("T1B2L1R1_9_Black_Center", sheetNum, endRow, 5, workflowInfo.get("DCDATE"));
            ctrl.fillDataWithStyleAndMerge("T1B2L1R2_9_Black", sheetNum, endRow, endRow, 6, 7, workflowInfo.get("GN"));
        }else{
            ctrl.fillDataWithStyle("Green_T1B2L2R1_10B_Blue", sheetNum, ++endRow, 1, " ");
            ctrl.fillDataWithStyle("Green_T1B2L1R1_9B_Blue", sheetNum, endRow, 2, " ");
            ctrl.fillDataWithStyle("T1B2L1R1_9_Black_Center", sheetNum, endRow, 3, " ");
            ctrl.fillDataWithStyle("T1B2L1R1_9_Black_Center", sheetNum, endRow, 4, " ");
            ctrl.fillDataWithStyle("T1B2L1R1_9_Black_Center", sheetNum, endRow, 5, " ");
            ctrl.fillDataWithStyleAndMerge("T1B2L1R2_9_Black", sheetNum, endRow, endRow, 6, 7, " ");
        }
        
        ctrl.setRowSize(sheetNum, startRow, endRow, 360);

        ctrl.setLeftMargin(sheetNum, 0.4);
        ctrl.setRightMargin(sheetNum, 0.4);
        ctrl.setTopMargin(sheetNum, 0.51);
        ctrl.setBottomMargin(sheetNum, 0.47);

        ctrl.setFooter(sheetNum, "Report form ECO A Type", (short) 8);
    }
    
    private void excelEcoB(ExcelReportWithPoi ctrl, TCEcoModel ecoInfo, ArrayList<SYMCECODwgData> ecoDwgList, int sheetNum) {
        ctrl.createFont("11B_Black");
        ctrl.setBoldWeight("11B_Black", true);
        ctrl.setFontColor("11B_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("11B_Black", (short) 11);

        ctrl.createFont("8_Black");
        ctrl.setFontColor("8_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("8_Black", (short) 8);

        ctrl.createStyleWithFont("Green_T2B2L1R2_11B_Black", "11B_Black");
        ctrl.setBorderStyle("Green_T2B2L1R2_11B_Black", 2, 2, 1, 2);
        ctrl.setCellColor("Green_T2B2L1R2_11B_Black", HSSFColor.LIGHT_GREEN.index);

        ctrl.createStyleWithFont("Green_T2B2L2R1_9B_Blue_Center", "9B_Blue");
        ctrl.setBorderStyle("Green_T2B2L2R1_9B_Blue_Center", 2, 2, 2, 1);
        ctrl.setCellColor("Green_T2B2L2R1_9B_Blue_Center", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L2R1_9B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Green_T2B2L1R1_9B_Blue_Center", "9B_Blue");
        ctrl.setBorderStyle("Green_T2B2L1R1_9B_Blue_Center", 2, 2, 1, 1);
        ctrl.setCellColor("Green_T2B2L1R1_9B_Blue_Center", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L1R1_9B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Green_T2B2L1R2_9B_Blue_Center", "9B_Blue");
        ctrl.setBorderStyle("Green_T2B2L1R2_9B_Blue_Center", 2, 2, 1, 2);
        ctrl.setCellColor("Green_T2B2L1R2_9B_Blue_Center", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L1R2_9B_Blue_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B7L2R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T1B7L2R1_8_Black_Center", 1, 7, 2, 1);
        ctrl.setAlignment("T1B7L2R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B7L1R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T1B7L1R1_8_Black_Center", 1, 7, 1, 1);
        ctrl.setAlignment("T1B7L1R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B7L1R1_8_Black", "8_Black");
        ctrl.setBorderStyle("T1B7L1R1_8_Black", 1, 7, 1, 1);

        ctrl.createStyleWithFont("T1B7L1R1_8_Black_Wrap", "8_Black");
        ctrl.setBorderStyle("T1B7L1R1_8_Black_Wrap", 1, 7, 1, 1);
        ctrl.setWrapText("T1B7L1R1_8_Black_Wrap", true);

        ctrl.createStyleWithFont("T1B7L1R2_8_Black", "8_Black");
        ctrl.setBorderStyle("T1B7L1R2_8_Black", 1, 7, 1, 2);

        ctrl.createStyleWithFont("T7B7L2R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T7B7L2R1_8_Black_Center", 7, 7, 2, 1);
        ctrl.setAlignment("T7B7L2R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B7L1R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T7B7L1R1_8_Black_Center", 7, 7, 1, 1);
        ctrl.setAlignment("T7B7L1R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B7L1R1_8_Black", "8_Black");
        ctrl.setBorderStyle("T7B7L1R1_8_Black", 7, 7, 1, 1);

        ctrl.createStyleWithFont("T7B7L1R1_8_Black_Wrap", "8_Black");
        ctrl.setBorderStyle("T7B7L1R1_8_Black_Wrap", 7, 7, 1, 1);
        ctrl.setWrapText("T7B7L1R1_8_Black_Wrap", true);

        ctrl.createStyleWithFont("T7B7L1R2_8_Black", "8_Black");
        ctrl.setBorderStyle("T7B7L1R2_8_Black", 7, 7, 1, 2);

        ctrl.createStyleWithFont("T7B2L2R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T7B2L2R1_8_Black_Center", 7, 2, 2, 1);
        ctrl.setAlignment("T7B2L2R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B2L1R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T7B2L1R1_8_Black_Center", 7, 2, 1, 1);
        ctrl.setAlignment("T7B2L1R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B2L1R1_8_Black", "8_Black");
        ctrl.setBorderStyle("T7B2L1R1_8_Black", 7, 2, 1, 1);

        ctrl.createStyleWithFont("T7B2L1R1_8_Black_Wrap", "8_Black");
        ctrl.setBorderStyle("T7B2L1R1_8_Black_Wrap", 7, 2, 1, 1);
        ctrl.setWrapText("T7B2L1R1_8_Black_Wrap", true);

        ctrl.createStyleWithFont("T7B2L1R2_8_Black", "8_Black");
        ctrl.setBorderStyle("T7B2L1R2_8_Black", 7, 2, 1, 2);

        ctrl.createStyleWithFont("T1B2L2R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T1B2L2R1_8_Black_Center", 1, 2, 2, 1);
        ctrl.setAlignment("T1B2L2R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B2L1R1_8_Black_Center", "8_Black");
        ctrl.setBorderStyle("T1B2L1R1_8_Black_Center", 1, 2, 1, 1);
        ctrl.setAlignment("T1B2L1R1_8_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B2L1R1_8_Black", "8_Black");
        ctrl.setBorderStyle("T1B2L1R1_8_Black", 1, 2, 1, 1);

        ctrl.createStyleWithFont("T1B2L1R1_8_Black_Wrap", "8_Black");
        ctrl.setBorderStyle("T1B2L1R1_8_Black_Wrap", 1, 2, 1, 1);
        ctrl.setWrapText("T1B2L1R1_8_Black_Wrap", true);

        ctrl.createStyleWithFont("T1B2L1R2_8_Black", "8_Black");
        ctrl.setBorderStyle("T1B2L1R2_8_Black", 1, 2, 1, 2);

        /*
         *  Row No. 1-2
         */

        ctrl.fillDataWithStyleAndMerge("Grey_T2B1L2R1_10B_Blue", sheetNum, 0, 0, 0, 4, "     KG MOBILITY");
        ctrl.fillDataWithStyleAndMerge("Grey_T1B2L2R1_10B_Blue", sheetNum, 1, 1, 0, 4, "     Research & Development Center");
        ctrl.fillDataWithStyleAndMerge("Grey_T2B2L1R1_10B_Blue_Center", sheetNum, 0, 1, 5, 12, "All Rights Reserved by KG MOBILITY");
        ctrl.fillDataWithStyle("Grey_T2B1L1R1_10B_Blue_Center", sheetNum, 0, 13, "ECO Date");
        ctrl.fillDataWithStyle("Grey_T2B1L1R2_10B_Blue_Center", sheetNum, 0, 14, "Print Date");
        ctrl.fillDataWithStyle("Grey_T1B2L1R1_9B_Black_Center", sheetNum, 1, 13, ecoInfo.getReleaseDate().equals("") ? "" : ecoInfo.getReleaseDate().substring(0, 10));
        ctrl.fillDataWithStyle("Grey_T1B2L1R2_9B_Black_Center", sheetNum, 1, 14, CustomUtil.getTODAY());

        ctrl.setRowSizeByPixel(sheetNum, 0, 1, 26);

        /*
         *  Row No. 3
         */
        ctrl.fillDataWithStyleAndMerge("T2B2L2R2_12B_Red_Center", sheetNum, 2, 2, 0, 14, "*** ENGINEERING CHANGE ORDER - B (DWG List) ***");

        ctrl.setRowSizeByPixel(sheetNum, 2, 40);

        /*
         *  Row No. 4
         */
        String eco = "    " + ecoInfo.getEcoNo();

        ctrl.fillDataWithStyleAndMerge("Green_T2B2L2R1_11B_Blue_Center", sheetNum, 3, 3, 0, 1, "ECO No.");
        ctrl.fillDataWithStyleAndMerge("Green_T2B2L1R2_11B_Black", sheetNum, 3, 3, 2, 14, eco);

        ctrl.setRowSizeByPixel(sheetNum, 3, 26);

        /*
         *  Row No. 5-
         */

        int[] dwgColWidthArray = { 30, 70, 50, 30, 90, 30, 240, 60, 60, 60, 60, 40, 60, 135, 135 };

        for (int i = 0; i < dwgColWidthArray.length; ++i) {
            ctrl.SetColWidthSettingByPixel(sheetNum, i, dwgColWidthArray[i]);
        }

        ctrl.fillDataWithStyle("Green_T2B2L2R1_9B_Blue_Center", sheetNum, 4, 0, "No");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 1, "Proj");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 2, "M/Type");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 3, "P/O");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 4, "Part No");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 5, "Rev.");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 6, "Part Name");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 7, "Response");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 8, "Product");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 9, "2D");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 10, "3D");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 11, "ZIP");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_9B_Blue_Center", sheetNum, 4, 12, "S/Mode");
        ctrl.fillDataWithStyleAndMerge("Green_T2B2L1R2_9B_Blue_Center", sheetNum, 4, 4, 13, 14, "Detail Changed Description");

        ctrl.setRowSizeByPixel(sheetNum, 4, 21);

        if (ecoDwgList != null && ecoDwgList.size() > 1) {
            SYMCECODwgData dwgData = (SYMCECODwgData) ecoDwgList.get(0);
            int j = 0;

            ctrl.fillDataWithStyle("T1B7L2R1_8_Black_Center", sheetNum, 5, j++, "1");
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getProject());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getModelType());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getPartOrigin());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, 5, j++, dwgData.getPartNo());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getRevisionNo());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, 5, j++, dwgData.getPartName());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, 5, j++, dwgData.getResponsibility());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getCatProduct());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getHas2d());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getHas3d());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getZip());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getsMode());
            ctrl.fillDataWithStyleAndMerge("T1B7L1R2_8_Black", sheetNum, 5, 5, j, (j + 1), dwgData.getChangeDesc());

            ctrl.setRowSizeByPixel(sheetNum, 5, 26);

            for (int i = 1; i < ecoDwgList.size() - 1; ++i) {
                dwgData = (SYMCECODwgData) ecoDwgList.get(i);
                j = 0;

                ctrl.fillDataWithStyle("T7B7L2R1_8_Black_Center", sheetNum, (5 + i), j++, Integer.toString(i + 1));
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getProject());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getModelType());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getPartOrigin());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, (5 + i), j++, dwgData.getPartNo());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getRevisionNo());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, (5 + i), j++, dwgData.getPartName());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, (5 + i), j++, dwgData.getResponsibility());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getCatProduct());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getHas2d());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getHas3d());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getZip());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, (5 + i), j++, dwgData.getsMode());
                ctrl.fillDataWithStyleAndMerge("T7B7L1R2_8_Black", sheetNum, (5 + i), (5 + i), j, (j + 1), dwgData.getChangeDesc());

                ctrl.setRowSizeByPixel(sheetNum, (5 + i), 26);
            }

            dwgData = (SYMCECODwgData) ecoDwgList.get(ecoDwgList.size() - 1);
            j = 0;

            ctrl.fillDataWithStyle("T7B2L2R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, ecoDwgList.size()+"");
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getProject());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getModelType());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getPartOrigin());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getPartNo());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getRevisionNo());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getPartName());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getResponsibility());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getCatProduct());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getHas2d());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getHas3d());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getZip());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, (5 + ecoDwgList.size() - 1), j++, dwgData.getsMode());
            ctrl.fillDataWithStyleAndMerge("T7B2L1R2_8_Black", sheetNum, (5 + ecoDwgList.size() - 1), (5 + ecoDwgList.size() - 1), j, (j + 1), dwgData.getChangeDesc());

            ctrl.setRowSizeByPixel(sheetNum, (5 + ecoDwgList.size() - 1), 26);

        } else if (ecoDwgList != null && ecoDwgList.size() == 1) { // dwgList.size() = 1
            SYMCECODwgData dwgData = (SYMCECODwgData) ecoDwgList.get(0);
            int j = 0;

            ctrl.fillDataWithStyle("T1B2L2R1_8_Black_Center", sheetNum, 5, j++, "1");
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getProject());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getModelType());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getPartOrigin());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black", sheetNum, 5, j++, dwgData.getPartNo());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getRevisionNo());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black", sheetNum, 5, j++, dwgData.getPartName());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black", sheetNum, 5, j++, dwgData.getResponsibility());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getCatProduct());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getHas2d());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getHas3d());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getZip());
            ctrl.fillDataWithStyle("T1B2L1R1_8_Black_Center", sheetNum, 5, j++, dwgData.getsMode());
            ctrl.fillDataWithStyleAndMerge("T1B2L1R2_8_Black", sheetNum, 5, 5, j, (j + 1), dwgData.getChangeDesc());

            ctrl.setRowSizeByPixel(sheetNum, 5, 26);

        }

        ctrl.setLeftMargin(sheetNum, 0.4);
        ctrl.setRightMargin(sheetNum, 0.4);
        ctrl.setTopMargin(sheetNum, 0.51);
        ctrl.setBottomMargin(sheetNum, 0.47);

        ctrl.setFooter(sheetNum, "Report form ECO B Type");
        ctrl.setScale(sheetNum, (short) 86);
    }
    
    private void excelEcoC(ExcelReportWithPoi ctrl, TCEcoModel ecoInfo, ArrayList<SYMCBOMEditData> ecoEplList, int sheetNum) {
        ctrl.createFont("7B_Blue");
        ctrl.setBoldWeight("7B_Blue", true);
        ctrl.setFontColor("7B_Blue", HSSFColor.BLUE.index);
        ctrl.setFontSize("7B_Blue", (short) 7);

        ctrl.createFont("7_Black");
        ctrl.setFontColor("7_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("7_Black", (short) 7);

        ctrl.createStyleWithFont("Green_T2B2L1R1_11B_Black", "11B_Black");
        ctrl.setBorderStyle("Green_T2B2L1R1_11B_Black", 2, 2, 1, 1);
        ctrl.setCellColor("Green_T2B2L1R1_11B_Black", HSSFColor.LIGHT_GREEN.index);

        ctrl.createStyleWithFont("Green_T2B2L2R1_7B_Blue_Center_Wrap", "7B_Blue");
        ctrl.setBorderStyle("Green_T2B2L2R1_7B_Blue_Center_Wrap", 2, 2, 2, 1);
        ctrl.setCellColor("Green_T2B2L2R1_7B_Blue_Center_Wrap", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L2R1_7B_Blue_Center_Wrap", HSSFCellStyle.ALIGN_CENTER);
        ctrl.setWrapText("Green_T2B2L2R1_7B_Blue_Center_Wrap", true);

        ctrl.createStyleWithFont("Green_T2B2L1R1_7B_Blue_Center_Wrap", "7B_Blue");
        ctrl.setBorderStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", 2, 2, 1, 1);
        ctrl.setCellColor("Green_T2B2L1R1_7B_Blue_Center_Wrap", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L1R1_7B_Blue_Center_Wrap", HSSFCellStyle.ALIGN_CENTER);
        ctrl.setWrapText("Green_T2B2L1R1_7B_Blue_Center_Wrap", true);

        ctrl.createStyleWithFont("Green_T2B2L1R2_7B_Blue_Center_Wrap", "7B_Blue");
        ctrl.setBorderStyle("Green_T2B2L1R2_7B_Blue_Center_Wrap", 2, 2, 1, 2);
        ctrl.setCellColor("Green_T2B2L1R2_7B_Blue_Center_Wrap", HSSFColor.LIGHT_GREEN.index);
        ctrl.setAlignment("Green_T2B2L1R2_7B_Blue_Center_Wrap", HSSFCellStyle.ALIGN_CENTER);
        ctrl.setWrapText("Green_T2B2L1R2_7B_Blue_Center_Wrap", true);

        ctrl.createStyleWithFont("Turquoise_T2B7L2R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L2R1_7_Black_Center", 2, 7, 2, 1);
        ctrl.setCellColor("Turquoise_T2B7L2R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T2B7L2R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T2B7L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L1R1_7_Black_Center", 2, 7, 1, 1);
        ctrl.setCellColor("Turquoise_T2B7L1R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T2B7L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T1B7L2R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T1B7L2R1_7_Black_Center", 1, 7, 2, 1);
        ctrl.setCellColor("Turquoise_T1B7L2R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T1B7L2R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T7B1L2R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B1L2R1_7_Black_Center", 7, 1, 2, 1);
        ctrl.setCellColor("Turquoise_T7B1L2R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B1L2R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T7B2L2R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B2L2R1_7_Black_Center", 7, 2, 2, 1);
        ctrl.setCellColor("Turquoise_T7B2L2R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B2L2R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T2B7L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L1R1_7_Black", 2, 7, 1, 1);
        ctrl.setCellColor("Turquoise_T2B7L1R1_7_Black", HSSFColor.LIGHT_TURQUOISE.index);

        ctrl.createStyleWithFont("Turquoise_T2B7L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L1R1_7_Black_Wrap", 2, 7, 1, 1);
        ctrl.setCellColor("Turquoise_T2B7L1R1_7_Black_Wrap", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setWrapText("Turquoise_T2B7L1R1_7_Black_Wrap", true);

        ctrl.createStyleWithFont("Turquoise_T2B7L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L1R1_7_Black_Center", 2, 7, 1, 1);
        ctrl.setCellColor("Turquoise_T2B7L1R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T2B7L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T2B7L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L1R2_7_Black", 2, 7, 1, 2);
        ctrl.setCellColor("Turquoise_T2B7L1R2_7_Black", HSSFColor.LIGHT_TURQUOISE.index);
        
        ctrl.createStyleWithFont("Turquoise_T2B7L1R2_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L1R2_7_Black_Center", 2, 7, 1, 2);
        ctrl.setCellColor("Turquoise_T2B7L1R2_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T2B7L1R2_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T1B7L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T1B7L1R1_7_Black", 1, 7, 1, 1);
        ctrl.setCellColor("Turquoise_T1B7L1R1_7_Black", HSSFColor.LIGHT_TURQUOISE.index);

        ctrl.createStyleWithFont("Turquoise_T1B7L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T1B7L1R2_7_Black", 1, 7, 1, 2);
        ctrl.setCellColor("Turquoise_T1B7L1R2_7_Black", HSSFColor.LIGHT_TURQUOISE.index);
        
        ctrl.createStyleWithFont("Turquoise_T1B7L1R2_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T1B7L1R2_7_Black_Center", 1, 7, 1, 2);
        ctrl.setCellColor("Turquoise_T1B7L1R2_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T1B7L1R2_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T7B1L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B1L1R1_7_Black", 7, 1, 1, 1);
        ctrl.setCellColor("Turquoise_T7B1L1R1_7_Black", HSSFColor.LIGHT_TURQUOISE.index);

        ctrl.createStyleWithFont("Turquoise_T7B1L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B1L1R1_7_Black_Wrap", 7, 1, 1, 1);
        ctrl.setCellColor("Turquoise_T7B1L1R1_7_Black_Wrap", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setWrapText("Turquoise_T7B1L1R1_7_Black_Wrap", true);

        ctrl.createStyleWithFont("Turquoise_T7B1L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B1L1R1_7_Black_Center", 7, 1, 1, 1);
        ctrl.setCellColor("Turquoise_T7B1L1R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B1L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T7B1L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B1L1R2_7_Black", 7, 1, 1, 2);
        ctrl.setCellColor("Turquoise_T7B1L1R2_7_Black", HSSFColor.LIGHT_TURQUOISE.index);
        
        ctrl.createStyleWithFont("Turquoise_T7B1L1R2_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B1L1R2_7_Black_Center", 7, 1, 1, 2);
        ctrl.setCellColor("Turquoise_T7B1L1R2_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B1L1R2_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T7B2L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B2L1R1_7_Black", 7, 2, 1, 1);
        ctrl.setCellColor("Turquoise_T7B2L1R1_7_Black", HSSFColor.LIGHT_TURQUOISE.index);

        ctrl.createStyleWithFont("Turquoise_T7B2L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B2L1R1_7_Black_Wrap", 7, 2, 1, 1);
        ctrl.setCellColor("Turquoise_T7B2L1R1_7_Black_Wrap", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setWrapText("Turquoise_T7B2L1R1_7_Black_Wrap", true);

        ctrl.createStyleWithFont("Turquoise_T7B2L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B2L1R2_7_Black", 7, 2, 1, 2);
        ctrl.setCellColor("Turquoise_T7B2L1R2_7_Black", HSSFColor.LIGHT_TURQUOISE.index);
        
        ctrl.createStyleWithFont("Turquoise_T7B2L1R2_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B2L1R2_7_Black_Center", 7, 2, 1, 2);
        ctrl.setCellColor("Turquoise_T7B2L1R2_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B2L1R2_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T2B7L2R1_7_Black_Right", "7_Black");
        ctrl.setBorderStyle("Turquoise_T2B7L2R1_7_Black_Right", 2, 7, 2, 1);
        ctrl.setCellColor("Turquoise_T2B7L2R1_7_Black_Right", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T2B7L2R1_7_Black_Right", HSSFCellStyle.ALIGN_RIGHT);

        ctrl.createStyleWithFont("Turquoise_T1B7L2R1_7_Black_Right", "7_Black");
        ctrl.setBorderStyle("Turquoise_T1B7L2R1_7_Black_Right", 1, 7, 2, 1);
        ctrl.setCellColor("Turquoise_T1B7L2R1_7_Black_Right", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T1B7L2R1_7_Black_Right", HSSFCellStyle.ALIGN_RIGHT);

        ctrl.createStyleWithFont("Turquoise_T7B1L2R1_7_Black_Right", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B1L2R1_7_Black_Right", 7, 1, 2, 1);
        ctrl.setCellColor("Turquoise_T7B1L2R1_7_Black_Right", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B1L2R1_7_Black_Right", HSSFCellStyle.ALIGN_RIGHT);

        ctrl.createStyleWithFont("Turquoise_T7B2L2R1_7_Black_Right", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B2L2R1_7_Black_Right", 7, 2, 1, 1);
        ctrl.setCellColor("Turquoise_T7B2L2R1_7_Black_Right", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B2L2R1_7_Black_Right", HSSFCellStyle.ALIGN_RIGHT);

        ctrl.createStyleWithFont("Turquoise_T1B7L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("Turquoise_T1B7L1R1_7_Black_Wrap", 1, 7, 1, 1);
        ctrl.setCellColor("Turquoise_T1B7L1R1_7_Black_Wrap", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setWrapText("Turquoise_T1B7L1R1_7_Black_Wrap", true);
        
        ctrl.createStyleWithFont("Turquoise_T1B7L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T1B7L1R1_7_Black_Center", 1, 7, 1, 1);
        ctrl.setCellColor("Turquoise_T1B7L1R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T1B7L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Turquoise_T7B2L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("Turquoise_T7B2L1R1_7_Black_Center", 7, 2, 1, 1);
        ctrl.setCellColor("Turquoise_T7B2L1R1_7_Black_Center", HSSFColor.LIGHT_TURQUOISE.index);
        ctrl.setAlignment("Turquoise_T7B2L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B1L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B1L1R1_7_Black", 7, 1, 1, 1);

        ctrl.createStyleWithFont("T7B1L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("T7B1L1R1_7_Black_Wrap", 7, 1, 1, 1);
        ctrl.setWrapText("T7B1L1R1_7_Black_Wrap", true);

        ctrl.createStyleWithFont("T7B1L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T7B1L1R1_7_Black_Center", 7, 1, 1, 1);
        ctrl.setAlignment("T7B1L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B1L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B1L1R2_7_Black", 7, 1, 1, 2);
        
        ctrl.createStyleWithFont("T7B1L1R2_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T7B1L1R2_7_Black_Center", 7, 1, 1, 2);
        ctrl.setAlignment("T7B1L1R2_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);       

        ctrl.createStyleWithFont("T7B2L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B2L1R1_7_Black", 7, 2, 1, 1);

        ctrl.createStyleWithFont("T7B2L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("T7B2L1R1_7_Black_Wrap", 7, 2, 1, 1);
        ctrl.setWrapText("T7B2L1R1_7_Black_Wrap", true);

        ctrl.createStyleWithFont("T7B2L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B2L1R2_7_Black", 7, 2, 1, 2);
        
        ctrl.createStyleWithFont("T7B2L1R2_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T7B2L1R2_7_Black_Center", 7, 2, 1, 2);
        ctrl.setAlignment("T7B2L1R2_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B1L2R1_7_Black_Right", "7_Black");
        ctrl.setBorderStyle("T7B1L2R1_7_Black_Right", 7, 1, 2, 1);
        ctrl.setAlignment("T7B1L2R1_7_Black_Right", HSSFCellStyle.ALIGN_RIGHT);

        ctrl.createStyleWithFont("T7B2L2R1_7_Black_Right", "7_Black");
        ctrl.setBorderStyle("T7B2L2R1_7_Black_Right", 7, 2, 1, 1);
        ctrl.setAlignment("T7B2L2R1_7_Black_Right", HSSFCellStyle.ALIGN_RIGHT);

        ctrl.createStyleWithFont("T1B7L2R1_7_Black", "7_Black");
        ctrl.setBorderStyle("T1B7L2R1_7_Black", 1, 7, 2, 1);

        ctrl.createStyleWithFont("T1B7L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("T1B7L1R1_7_Black", 1, 7, 1, 1);

        ctrl.createStyleWithFont("T1B7L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("T1B7L1R1_7_Black_Wrap", 1, 7, 1, 1);
        ctrl.setWrapText("T1B7L1R1_7_Black_Wrap", true);

        ctrl.createStyleWithFont("T1B7L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("T1B7L1R2_7_Black", 1, 7, 1, 2);
        
        ctrl.createStyleWithFont("T1B7L1R2_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T1B7L1R2_7_Black_Center", 1, 7, 1, 2);
        ctrl.setAlignment("T1B7L1R2_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B1L2R1_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B1L2R1_7_Black", 7, 1, 2, 1);

        ctrl.createStyleWithFont("T7B2L2R1_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B2L2R1_7_Black", 7, 2, 2, 1);

        ctrl.createStyleWithFont("T1B7L2R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T1B7L2R1_7_Black_Center", 1, 7, 2, 1);
        ctrl.setAlignment("T1B7L2R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B7L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T1B7L1R1_7_Black_Center", 1, 7, 1, 1);
        ctrl.setAlignment("T1B7L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B1L2R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T7B1L2R1_7_Black_Center", 7, 1, 2, 1);
        ctrl.setAlignment("T7B1L2R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B2L1R1_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B2L1R1_7_Black", 7, 2, 1, 1);

        ctrl.createStyleWithFont("T7B2L1R1_7_Black_Wrap", "7_Black");
        ctrl.setBorderStyle("T7B2L1R1_7_Black_Wrap", 7, 2, 1, 1);
        ctrl.setWrapText("T7B2L1R1_7_Black_Wrap", true);

        ctrl.createStyleWithFont("T7B2L1R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T7B2L1R1_7_Black_Center", 7, 2, 1, 1);
        ctrl.setAlignment("T7B2L1R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T7B2L1R2_7_Black", "7_Black");
        ctrl.setBorderStyle("T7B2L1R2_7_Black", 7, 2, 1, 2);

        ctrl.createStyleWithFont("T7B2L2R1_7_Black_Center", "7_Black");
        ctrl.setBorderStyle("T7B2L2R1_7_Black_Center", 7, 2, 2, 1);
        ctrl.setAlignment("T7B2L2R1_7_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("Green_T2B2L1R2_11B_Black", "11B_Black");
        ctrl.setBorderStyle("Green_T2B2L1R2_11B_Black", 2, 2, 1, 2);
        ctrl.setCellColor("Green_T2B2L1R2_11B_Black", HSSFColor.LIGHT_GREEN.index);
        /*
         *  Row No. 1-2
         */

        ctrl.fillDataWithStyleAndMerge("Grey_T2B1L2R1_10B_Blue", sheetNum, 0, 0, 0, 5, "     KG MOBILITY");
        ctrl.fillDataWithStyleAndMerge("Grey_T1B2L2R1_10B_Blue", sheetNum, 1, 1, 0, 5, "     Research & Development Center");
        ctrl.fillDataWithStyleAndMerge("Grey_T2B2L1R1_10B_Blue_Center", sheetNum, 0, 1, 6, 24, "All Rights Reserved by KG MOBILITY");
        ctrl.fillDataWithStyle("Grey_T2B1L1R1_10B_Blue_Center", sheetNum, 0, 25, "ECO Date");
        ctrl.fillDataWithStyle("Grey_T2B1L1R2_10B_Blue_Center", sheetNum, 0, 26, "Print Date");
        ctrl.fillDataWithStyle("Grey_T1B2L1R1_9B_Black_Center", sheetNum, 1, 25, ecoInfo.getReleaseDate().equals("") ? "" : ecoInfo.getReleaseDate().substring(0, 10));
        ctrl.fillDataWithStyle("Grey_T1B2L1R2_9B_Black_Center", sheetNum, 1, 26, CustomUtil.getTODAY());

        ctrl.setRowSizeByPixel(sheetNum, 0, 1, 26);

        /*
         *  Row No. 3
         */

        ctrl.fillDataWithStyleAndMerge("T2B2L2R2_12B_Red_Center", sheetNum, 2, 2, 0, 26, "*** ENGINEERING CHANGE ORDER - C (Changed EPL) ***");

        ctrl.setRowSizeByPixel(sheetNum, 2, 40);

        /*
         *  Row No. 4
         */

        ctrl.fillDataWithStyleAndMerge("Green_T2B2L2R1_11B_Blue_Center", sheetNum, 3, 3, 0, 1, "ECO No.");
        ctrl.fillDataWithStyleAndMerge("Green_T2B2L1R2_11B_Black", sheetNum, 3, 3, 2, 26, ecoInfo.getEcoNo());

        ctrl.setRowSizeByPixel(sheetNum, 3, 26);

        int[] EPLColWidthArray = new int[] { 
                  30, 50, 30, 30, 90
                , 22, 22, 80, 22, 168
                , 14, 40, 21, 14, 14
                , 28, 30, 30, 40
                , 28, 28, 28, 28, 63
                , 120, 121, 121 };

        for (int i = 0; i < EPLColWidthArray.length; i++) {
            ctrl.SetColWidthSettingByPixel(sheetNum, i, EPLColWidthArray[i]);
        }

        /*
         *  Row No. 5
         */
        int idx = 0;

        ctrl.fillDataWithStyle("Green_T2B2L2R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "No");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Proj");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "SEQ");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "C/T");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Parent No");
        
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Rev");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "P/O");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Part No");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Rev");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Part Name");
        
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "IC");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "S/Mode");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Qty");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "A L T");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "S E L");
        
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "CAT");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Color");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Color Sec.");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Module Code");
        
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "PLT Stk");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "A/S Stk");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Cost");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Tool");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "ShownOnNo");
        
        ctrl.fillDataWithStyleAndMerge("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, 4, idx++, idx++, "Options");
        ctrl.fillDataWithStyle("Green_T2B2L1R2_7B_Blue_Center_Wrap", sheetNum, 4, idx, "Change Description");

        ctrl.setRowSizeByPixel(sheetNum, 4, 46);
        
        String firstLeftThick = null;
        String firstAlignLeft = null;
        String firstAlignLeftWrap = null;
        String firstAlignCenter = null;
        String firstRightThick = null;

        String secondLeftThick = null;
        String secondAlignLeft = null;
        String secondAlignLeftWrap = null;
        String secondAlignCenter = null;
        String secondRightThick = null;

        SYMCBOMEditData eplData = null;
        /* 
         *  Row No. 6 - TotalRowCount -2
         */
        
        int row = 5;
        ArrayList<String> addedEPLs = new ArrayList<String>();

        if (ecoEplList != null && ecoEplList.size() > 0) {

            eplData = (SYMCBOMEditData) ecoEplList.get(0);
            String type = eplData.getChangeType();
            String eplId = eplData.getEplId();
            addedEPLs.add(eplId);
            
            idx = 0;

            ctrl.fillDataWithStyle("Turquoise_T2B7L2R1_7_Black_Center", sheetNum, row, idx++, "1");
            if(type.equals("D") || eplData.getPartNoOld() != null){
            	// [20140417] Project code Old/New 분리 대응  getProject() -> getProjectOld()
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getProjectOld());
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getSeqOld());
                if(type.equals("D")){
                	ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getChangeType());
                }else{
                	ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, "");
                }
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getParentNo());
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getParentRev());
            }else{
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, "");
            }
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getPartOriginOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getPartNoOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getPartRevOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black", 		sheetNum, row, idx++, eplData.getPartNameOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getIcOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getSupplyModeOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getQtyOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getAltOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getSelOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getCatOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getColorIdOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getColorSectionOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getModuleCodeOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getPltStkOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getAsStkOld());
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, "");
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black_Center", sheetNum, row, idx++, "");
            ctrl.fillDataWithStyle("Turquoise_T2B7L1R1_7_Black",        sheetNum, row, idx++, eplData.getShownOnOld());
            ctrl.fillDataWithStyleAndMerge("Turquoise_T2B7L1R1_7_Black_Wrap",   sheetNum, row, row, idx++, idx++, getDecodedOption(eplData.getVcOld()));
            if(type.equals("D")){
            	ctrl.fillDataWithStyle("Turquoise_T2B7L1R2_7_Black",        sheetNum, row, idx, eplData.getChgDesc());
            }else{
            	ctrl.fillDataWithStyle("Turquoise_T2B7L1R2_7_Black",        sheetNum, row, idx, "");
            }
            

            ctrl.setRowSizeByPixel(sheetNum, row, getRowHeight(eplData.getVcOld()));
            row++;

            if (ecoEplList.size() == 1) {
                idx = 0;
                
                ctrl.fillDataWithStyle("Turquoise_T7B2L2R1_7_Black_Center", sheetNum, row, idx++, "1");
                if(type.equals("D")){
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, "");
                }else{
                	// [20140417] Project code Old/New 분리 대응 getProject() -> getProjectNew()
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getProjectNew());
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getSeqNew());
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getChangeType());
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getParentNo());
                    ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getParentRev());
                }
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getPartOriginNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getPartNoNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getPartRevNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black", 		sheetNum, row, idx++, eplData.getPartNameNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getIcNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getSupplyModeNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getQtyNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getAltNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getSelNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getCatNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getColorIdNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getColorSectionNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getModuleCodeNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getCostNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getToolNew());
                ctrl.fillDataWithStyle("Turquoise_T7B2L1R1_7_Black",        sheetNum, row, idx++, eplData.getShownOnNew());
                ctrl.fillDataWithStyleAndMerge("Turquoise_T7B2L1R1_7_Black_Wrap",   sheetNum, row, row, idx++, idx++, getDecodedOption(eplData.getVcNew()));
                if(type.equals("D")){
                	ctrl.fillDataWithStyle("Turquoise_T7B2L1R2_7_Black",        sheetNum, row, idx, "");
                }else{
                	ctrl.fillDataWithStyle("Turquoise_T7B2L1R2_7_Black",        sheetNum, row, idx, eplData.getChgDesc());
                }

                ctrl.setRowSizeByPixel(sheetNum, row, getRowHeight(eplData.getVcNew()));
                row++;

            } else {
                idx = 0;
                
                ctrl.fillDataWithStyle("Turquoise_T7B1L2R1_7_Black_Center", sheetNum, row, idx++, "1");
                if(type.equals("D")){
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, "");
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center",        sheetNum, row, idx++, "");
                }else{
                	// [20140417] Project code Old/New 분리 대응 getProject() -> getProjectNew()
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getProjectNew());
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getSeqNew());
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getChangeType());
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getParentNo());
                    ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center",        sheetNum, row, idx++, eplData.getParentRev());
                }
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center",        sheetNum, row, idx++, eplData.getPartOriginNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getPartNoNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getPartRevNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black", sheetNum, row, idx++, eplData.getPartNameNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getIcNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getSupplyModeNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getQtyNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getAltNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getSelNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getCatNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getColorIdNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getColorSectionNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getModuleCodeNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, "");
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getCostNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black_Center", sheetNum, row, idx++, eplData.getToolNew());
                ctrl.fillDataWithStyle("Turquoise_T7B1L1R1_7_Black",        sheetNum, row, idx++, eplData.getShownOnNew());
                ctrl.fillDataWithStyleAndMerge("Turquoise_T7B1L1R1_7_Black_Wrap",   sheetNum, row, row, idx++, idx++, getDecodedOption(eplData.getVcNew()));
                if(type.equals("D")){
                	ctrl.fillDataWithStyle("Turquoise_T7B1L1R2_7_Black",        sheetNum, row, idx, "");
                }else{
                	ctrl.fillDataWithStyle("Turquoise_T7B1L1R2_7_Black",        sheetNum, row, idx, eplData.getChgDesc());
                }
                

                ctrl.setRowSizeByPixel(sheetNum, row, getRowHeight(eplData.getVcNew()));
                row++;

                boolean curStyle = true; // 현재 스타일 => 음영 적용
                int j = 0;
                for (int i = 1; i < ecoEplList.size() - 1; ++i) {
                    eplData = (SYMCBOMEditData) ecoEplList.get(i);
                    type = eplData.getChangeType();
                    // 중복은 제거하고 보여줌
                    eplId = eplData.getEplId();
                    if(addedEPLs.contains(eplId)) {
                    	continue;
                    }
                    addedEPLs.add(eplId);
                    if (curStyle) {
                        firstLeftThick = "T1B7L2R1_7_Black_Center";
                        firstAlignLeft = "T1B7L1R1_7_Black";
                        firstAlignLeftWrap = "T1B7L1R1_7_Black_Wrap";
                        firstAlignCenter = "T1B7L1R1_7_Black_Center";
                        firstRightThick = "T1B7L1R2_7_Black";

                        secondLeftThick = "T7B1L2R1_7_Black_Center";
                        secondAlignLeft = "T7B1L1R1_7_Black";
                        secondAlignLeftWrap = "T7B1L1R1_7_Black_Wrap";
                        secondAlignCenter = "T7B1L1R1_7_Black_Center";
                        secondRightThick = "T7B1L1R2_7_Black";
                        
                    } else {

                        firstLeftThick = "Turquoise_T1B7L2R1_7_Black_Center";
                        firstAlignLeft = "Turquoise_T1B7L1R1_7_Black";
                        firstAlignLeftWrap = "Turquoise_T1B7L1R1_7_Black_Wrap";
                        firstAlignCenter = "Turquoise_T1B7L1R1_7_Black_Center";
                        firstRightThick = "Turquoise_T1B7L1R2_7_Black";

                        secondLeftThick = "Turquoise_T7B1L2R1_7_Black_Center";
                        secondAlignLeft = "Turquoise_T7B1L1R1_7_Black";
                        secondAlignLeftWrap = "Turquoise_T7B1L1R1_7_Black_Wrap";
                        secondAlignCenter = "Turquoise_T7B1L1R1_7_Black_Center";
                        secondRightThick = "Turquoise_T7B1L1R2_7_Black";
                    }

                    curStyle = !curStyle;

                    j = 0;

                    ctrl.fillDataWithStyle(firstLeftThick,     sheetNum, row, j++, addedEPLs.size()+"");
                    if(type.equals("D") || eplData.getPartNoOld() != null){
                    	// [20140417] Project code Old/New 분리 대응 getProject() -> getProjectOld()
                        ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getProjectOld());
                        ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getSeqOld());
                        if(type.equals("D")){
                        	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getChangeType());
                        }else{
                        	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                        }
                        ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getParentNo());
                        ctrl.fillDataWithStyle(firstAlignCenter,     sheetNum, row, j++, eplData.getParentRev());
                    }else{
                        ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(firstAlignCenter,     sheetNum, row, j++, "");
                    }
                    ctrl.fillDataWithStyle(firstAlignCenter,     sheetNum, row, j++, eplData.getPartOriginOld());
                    ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getPartNoOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getPartRevOld());
                    ctrl.fillDataWithStyle(firstAlignLeft,   sheetNum, row, j++, eplData.getPartNameOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getIcOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getSupplyModeOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getQtyOld());
                    ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getAltOld());
                    ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getSelOld());
                    ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getCatOld());
                    ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getColorIdOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getColorSectionOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getModuleCodeOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getPltStkOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getAsStkOld());
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                    ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                    ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getShownOnOld());
                    ctrl.fillDataWithStyleAndMerge(firstAlignLeftWrap, sheetNum, row, row, j++, j++, getDecodedOption(eplData.getVcOld()));
                    if(type.equals("D")){
                    	ctrl.fillDataWithStyle(firstRightThick,   sheetNum, row, j, eplData.getChgDesc());
                    }else{
                    	ctrl.fillDataWithStyle(firstRightThick,   sheetNum, row, j, "");
                    }
                    

                    ctrl.setRowSizeByPixel(sheetNum, row, getRowHeight(eplData.getVcOld()));
                    row++;
                    
                    j = 0;

                    ctrl.fillDataWithStyle(secondLeftThick,     sheetNum, row, j++, addedEPLs.size()+"");
                    if(type.equals("D")){
                        ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, "");
                        ctrl.fillDataWithStyle(secondAlignCenter,     sheetNum, row, j++, "");
                    }else{
                    	// [20140417] Project code Old/New 분리 대응 getProject() -> getProjectNew()
                        ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getProjectNew());
                        ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getSeqNew());
                        ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getChangeType());
                        ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getParentNo());
                        ctrl.fillDataWithStyle(secondAlignCenter,     sheetNum, row, j++, eplData.getParentRev());
                    }
                    ctrl.fillDataWithStyle(secondAlignCenter,     sheetNum, row, j++, eplData.getPartOriginNew());
                    ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getPartNoNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getPartRevNew());
                    ctrl.fillDataWithStyle(secondAlignLeft,   sheetNum, row, j++, eplData.getPartNameNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getIcNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getSupplyModeNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getQtyNew());
                    ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getAltNew());
                    ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getSelNew());
                    ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getCatNew());
                    ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getColorIdNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getColorSectionNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getModuleCodeNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getCostNew());
                    ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getToolNew());
                    ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getShownOnNew());
                    ctrl.fillDataWithStyleAndMerge(secondAlignLeftWrap, sheetNum, row, row, j++, j++, getDecodedOption(eplData.getVcNew()));
                    if(type.equals("D")){
                    	ctrl.fillDataWithStyle(secondRightThick,    sheetNum, row, j, "");
                    }else{
                    	ctrl.fillDataWithStyle(secondRightThick,    sheetNum, row, j, eplData.getChgDesc());
                    }
                    
                    ctrl.setRowSizeByPixel(sheetNum, row, getRowHeight(eplData.getVcNew()));
                    row++;
                }

                /* 
                 *   TotalRowCount -2 - End
                 */

                if (curStyle) {
                    firstLeftThick = "T1B7L2R1_7_Black_Center";
                    firstAlignLeft = "T1B7L1R1_7_Black";
                    firstAlignLeftWrap = "T1B7L1R1_7_Black_Wrap";
                    firstAlignCenter = "T1B7L1R1_7_Black_Center";
                    firstRightThick = "T1B7L1R2_7_Black";

                    secondLeftThick = "T7B2L2R1_7_Black_Center";
                    secondAlignLeft = "T7B2L1R1_7_Black";
                    secondAlignLeftWrap = "T7B2L1R1_7_Black_Wrap";
                    secondAlignCenter = "T7B2L1R1_7_Black_Center";
                    secondRightThick = "T7B2L1R2_7_Black";
                } else {
                    firstLeftThick = "Turquoise_T1B7L2R1_7_Black_Center";
                    firstAlignLeft = "Turquoise_T1B7L1R1_7_Black";
                    firstAlignLeftWrap = "Turquoise_T1B7L1R1_7_Black_Wrap";
                    firstAlignCenter = "Turquoise_T1B7L1R1_7_Black_Center";
                    firstRightThick = "Turquoise_T1B7L1R2_7_Black";

                    secondLeftThick = "Turquoise_T7B2L2R1_7_Black_Center";
                    secondAlignLeft = "Turquoise_T7B2L1R1_7_Black";
                    secondAlignLeftWrap = "Turquoise_T7B2L1R1_7_Black_Wrap";
                    secondAlignCenter = "Turquoise_T7B2L1R1_7_Black_Center";
                    secondRightThick = "Turquoise_T7B2L1R2_7_Black";
                }

                curStyle = !curStyle;

                eplData = (SYMCBOMEditData) ecoEplList.get(ecoEplList.size()-1);
                type = eplData.getChangeType();
                eplId = eplData.getEplId();
                /**
                 * [SR없음][20141104][jclee] 마지막 ROW가 이전에 추가되었던 Part인지 확인. 박태훈주임 요청.
                 */
                if (!addedEPLs.contains(eplId)) {
					
                	addedEPLs.add(eplId);
                	
                	j = 0;
                	
                	ctrl.fillDataWithStyle(firstLeftThick,     sheetNum, row, j++, addedEPLs.size()+"");
                	if(type.equals("D") || eplData.getPartNoOld() != null){
                		// [20140417] Project code Old/New 분리 대응 getProject() -> getProjectOld()
                		ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getProjectOld());
                		ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getSeqOld());
                		if(type.equals("D")){
                			ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getChangeType());
                		}else{
                			ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                		}
                		ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getParentNo());
                		ctrl.fillDataWithStyle(firstAlignCenter,     sheetNum, row, j++, eplData.getParentRev());
                	}else{
                		ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(firstAlignCenter,     sheetNum, row, j++, "");
                	}
                	ctrl.fillDataWithStyle(firstAlignCenter,     sheetNum, row, j++, eplData.getPartOriginOld());
                	ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getPartNoOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getPartRevOld());
                	ctrl.fillDataWithStyle(firstAlignLeft,   sheetNum, row, j++, eplData.getPartNameOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getIcOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getSupplyModeOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getQtyOld());
                	ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getAltOld());
                	ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getSelOld());
                	ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getCatOld());
                	ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getColorIdOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getColorSectionOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getModuleCodeOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getPltStkOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, eplData.getAsStkOld());
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                	ctrl.fillDataWithStyle(firstAlignCenter,   sheetNum, row, j++, "");
                	ctrl.fillDataWithStyle(firstAlignLeft,     sheetNum, row, j++, eplData.getShownOnOld());
                	ctrl.fillDataWithStyleAndMerge(firstAlignLeftWrap, sheetNum, row, row, j++, j++, getDecodedOption(eplData.getVcOld()));
                	if(type.equals("D")){
                		ctrl.fillDataWithStyle(firstRightThick,    sheetNum, row, j, eplData.getChgDesc());
                	}else{
                		ctrl.fillDataWithStyle(firstRightThick,    sheetNum, row, j, "");
                	}
                	
                	
                	ctrl.setRowSizeByPixel(sheetNum, row, getRowHeight(eplData.getVcOld()));
                	row++;
                	
                	j = 0;
                	
                	ctrl.fillDataWithStyle(secondLeftThick,     sheetNum, row, j++, addedEPLs.size()+"");
                	if(type.equals("D")){
                		ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, "");
                		ctrl.fillDataWithStyle(secondAlignCenter,     sheetNum, row, j++, "");
                	}else{
                		// [20140417] Project code Old/New 분리 대응 getProject() -> getProjectNew()
                		ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getProjectNew());
                		ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getSeqNew());
                		ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getChangeType());
                		ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getParentNo());
                		ctrl.fillDataWithStyle(secondAlignCenter,     sheetNum, row, j++, eplData.getParentRev());
                	}
                	ctrl.fillDataWithStyle(secondAlignCenter,     sheetNum, row, j++, eplData.getPartOriginNew());
                	ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getPartNoNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getPartRevNew());
                	ctrl.fillDataWithStyle(secondAlignLeft,   sheetNum, row, j++, eplData.getPartNameNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getIcNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getSupplyModeNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getQtyNew());
                	ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getAltNew());
                	ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getSelNew());
                	ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getCatNew());
                	ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getColorIdNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getColorSectionNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getModuleCodeNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, "");
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getCostNew());
                	ctrl.fillDataWithStyle(secondAlignCenter,   sheetNum, row, j++, eplData.getToolNew());
                	ctrl.fillDataWithStyle(secondAlignLeft,     sheetNum, row, j++, eplData.getShownOnNew());
                	ctrl.fillDataWithStyleAndMerge(secondAlignLeftWrap, sheetNum, row, row, j++, j++, getDecodedOption(eplData.getVcNew()));
                	if(type.equals("D")){
                		ctrl.fillDataWithStyle(secondRightThick,    sheetNum, row, j, "");
                	}else{
                		ctrl.fillDataWithStyle(secondRightThick,    sheetNum, row, j, eplData.getChgDesc());
                	}
                	ctrl.setRowSizeByPixel(sheetNum, row, getRowHeight(eplData.getVcNew()));
				}
            }
        }

        ctrl.setPrintColumns(sheetNum, 0, 26, 0, ctrl.getPhysicalNumberOfRows(2) - 1);

        ctrl.setLeftMargin(sheetNum, 0.4);
        ctrl.setRightMargin(sheetNum, 0.4);
        ctrl.setTopMargin(sheetNum, 0.4);
        ctrl.setBottomMargin(sheetNum, 0.4);

        ctrl.setFooter(sheetNum, "Report form ECO C Type");
        ctrl.setScale(sheetNum, (short) 75);
    }
    
    private void excelEcoD(ExcelReportWithPoi ctrl, TCEcoModel ecoInfo, ArrayList<SYMCPartListData> ecoEndList, int sheetNum) {
        /*
         *  Row No. 1-2
         */

        ctrl.fillDataWithStyleAndMerge("Grey_T2B1L2R1_10B_Blue", sheetNum, 0, 0, 0, 5, "     KG MOBILITY");
        ctrl.fillDataWithStyleAndMerge("Grey_T1B2L2R1_10B_Blue", sheetNum, 1, 1, 0, 5, "     Research & Development Center");
        ctrl.fillDataWithStyleAndMerge("Grey_T2B2L1R1_10B_Blue_Center", sheetNum, 0, 1, 6, 22, "All Rights Reserved by KG MOBILITY");
        ctrl.fillDataWithStyle("Grey_T2B1L1R1_10B_Blue_Center", sheetNum, 0, 23, "ECO Date");
        ctrl.fillDataWithStyle("Grey_T2B1L1R2_10B_Blue_Center", sheetNum, 0, 24, "Print Date");
        ctrl.fillDataWithStyle("Grey_T1B2L1R1_9B_Black_Center", sheetNum, 1, 23, ecoInfo.getReleaseDate().equals("") ? "" : ecoInfo.getReleaseDate().substring(0, 10));
        ctrl.fillDataWithStyle("Grey_T1B2L1R2_9B_Black_Center", sheetNum, 1, 24, CustomUtil.getTODAY());

        ctrl.setRowSizeByPixel(sheetNum, 0, 1, 26);

        /*
         *  Row No. 3
         */

        ctrl.fillDataWithStyleAndMerge("T2B2L2R2_12B_Red_Center", sheetNum, 2, 2, 0, 24, "*** ENGINEERING CHANGE ORDER - D (Part List) ***");

        ctrl.setRowSizeByPixel(sheetNum, 2, 40);

        /*
         *  Row No. 4
         */

        ctrl.fillDataWithStyleAndMerge("Green_T2B2L2R1_11B_Blue_Center", sheetNum, 3, 3, 0, 1, "ECO No.");
        ctrl.fillDataWithStyleAndMerge("Green_T2B2L1R2_11B_Black", sheetNum, 3, 3, 2, 24, ecoInfo.getEcoNo());

        ctrl.setRowSizeByPixel(sheetNum, 3, 26);

        int[] EPLColWidthArray = new int[] { 
                  30, 40, 30, 30, 90
                , 22, 22, 80, 22, 168, 40
                , 21, 14, 14, 28, 30
                , 30, 40, 28, 28, 28
                , 28, 63, 121, 121 };

        for (int i = 0; i < EPLColWidthArray.length; i++) {
            ctrl.SetColWidthSettingByPixel(sheetNum, i, EPLColWidthArray[i]);
        }

        /*
         *  Row No. 5
         */
        int idx = 0;

        ctrl.fillDataWithStyle("Green_T2B2L2R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "No");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Proj");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "SEQ");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "C/T");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Parent No");
        
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Rev");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "P/O");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Part No");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Rev");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Part Name");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "S/Mode");
        
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Qty");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "A L T");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "S E L");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "CAT");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Color");
        
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Color Sec.");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Module Code");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "PLT Stk");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "A/S Stk");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Cost");

        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "Tool");
        ctrl.fillDataWithStyle("Green_T2B2L1R1_7B_Blue_Center_Wrap", sheetNum, 4, idx++, "ShownOnNo");
        ctrl.fillDataWithStyleAndMerge("Green_T2B2L1R2_7B_Blue_Center_Wrap", sheetNum, 4, 4, idx, idx+1, "Change Description");

        ctrl.setRowSizeByPixel(sheetNum, 4, 46);

        SYMCPartListData getEcoEnd = null;
        /* 
         *  Row No. 6 - TotalRowCount -2
         */
        
        int row = 5;
        
        if (ecoEndList != null && ecoEndList.size() > 1) {
            getEcoEnd = (SYMCPartListData) ecoEndList.get(0);
            int j = 0;
            
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, row, j++,  "1");
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Wrap", sheetNum, row, j++,  getEcoEnd.getProject());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black_Center", sheetNum, row, j++,  getEcoEnd.getSeq());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getChangeType());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentRev());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartOrigin());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartRev());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartName());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSupplyMode());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getQty());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAlt());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSel());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCat());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorId());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorSection());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getModuleCode());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPltStk());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAsStk());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCost());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getTool());
            ctrl.fillDataWithStyle("T1B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getShownOn());
            ctrl.fillDataWithStyleAndMerge("T1B7L1R2_8_Black", sheetNum, row, row, j, (j + 1),  getEcoEnd.getDesc());
            
            ctrl.setRowSizeByPixel(sheetNum, row, 26);
            row++;
            
            for (int i = 1; i < ecoEndList.size() - 1; ++i) {
                getEcoEnd = (SYMCPartListData) ecoEndList.get(i);
                j = 0;
                
                ctrl.fillDataWithStyle("T7B7L2R1_8_Black_Center", sheetNum, row, j++,  (i+1)+"");
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Wrap", sheetNum, row, j++,  getEcoEnd.getProject());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black_Center", sheetNum, row, j++,  getEcoEnd.getSeq());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getChangeType());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentRev());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartOrigin());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartRev());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartName());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSupplyMode());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getQty());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAlt());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSel());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCat());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorId());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorSection());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getModuleCode());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPltStk());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAsStk());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCost());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getTool());
                ctrl.fillDataWithStyle("T7B7L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getShownOn());
                ctrl.fillDataWithStyleAndMerge("T7B7L1R2_8_Black", sheetNum, row, row, j, (j + 1),  getEcoEnd.getDesc());

                ctrl.setRowSizeByPixel(sheetNum, row, 26);
                row++;
            }
            
            getEcoEnd = (SYMCPartListData) ecoEndList.get(ecoEndList.size() - 1);
            j = 0;
            
            ctrl.fillDataWithStyle("T7B2L2R1_8_Black_Center", sheetNum, row, j++,  (row-4)+"");
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Wrap", sheetNum, row, j++,  getEcoEnd.getProject());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, row, j++,  getEcoEnd.getSeq());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getChangeType());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentRev());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartOrigin());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartRev());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartName());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSupplyMode());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getQty());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAlt());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSel());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCat());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorId());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorSection());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getModuleCode());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPltStk());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAsStk());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCost());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getTool());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getShownOn());
            ctrl.fillDataWithStyleAndMerge("T7B2L1R2_8_Black", sheetNum, row, row, j, (j + 1),  getEcoEnd.getDesc());

            ctrl.setRowSizeByPixel(sheetNum, row, 26);
            row++;
            
        } else if (ecoEndList != null && ecoEndList.size() == 1) {
            getEcoEnd = (SYMCPartListData) ecoEndList.get(0);
            int j = 0;
            
            ctrl.fillDataWithStyle("T7B2L2R1_8_Black_Center", sheetNum, row, j++, "1");
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Wrap", sheetNum, row, j++,  getEcoEnd.getProject());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black_Center", sheetNum, row, j++,  getEcoEnd.getSeq());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getChangeType());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentRev());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartOrigin());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getParentNo());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartRev());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPartName());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSupplyMode());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getQty());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAlt());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getSel());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCat());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorId());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getColorSection());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getModuleCode());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getPltStk());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getAsStk());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getCost());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getTool());
            ctrl.fillDataWithStyle("T7B2L1R1_8_Black", sheetNum, row, j++,  getEcoEnd.getShownOn());
            ctrl.fillDataWithStyleAndMerge("T7B2L1R2_8_Black", sheetNum, row, row, j, (j + 1),  getEcoEnd.getDesc());
            
            ctrl.setRowSizeByPixel(sheetNum, row, 26);
            
        }

        ctrl.setPrintColumns(sheetNum, 0, 24, 0, ctrl.getPhysicalNumberOfRows(2) - 1);

        ctrl.setLeftMargin(sheetNum, 0.4);
        ctrl.setRightMargin(sheetNum, 0.4);
        ctrl.setTopMargin(sheetNum, 0.4);
        ctrl.setBottomMargin(sheetNum, 0.4);

        ctrl.setFooter(sheetNum, "Report form ECO D Type");
        ctrl.setScale(sheetNum, (short) 85);
    }
    
    private int getRowHeight(String option) {
        int rowHeight = 0;
        int rowCapacity = 46;
        int stdPixel = 18;

        if (option != null && !option.equals("")) {
            Pattern p = Pattern.compile("[ ]"+"OR"+"[ ]");
        	String[] result = p.split(option.trim());

        	for (int i=0; i<result.length; i++){
        		int len = result[i].length();
        		
        		rowHeight += (len / rowCapacity) + 1;
        	}
        } else {
            rowHeight = 1;
        }
        return rowHeight * stdPixel;
    }
    
    public static String getDecodedOption(String option) {
    	if(option == null || option.equals(""))
    		return "";
    	String sortOption = sortingCondition(option);
    	Pattern p = Pattern.compile("[ ]"+"OR"+"[ ]");
    	String[] result = p.split(sortOption.trim());

    	String desc = "";
    	for (int i=0; i<result.length; i++){
    		if(i == 0){
    			desc = result[0];
    		}else{
    			desc += "\n" + result[i];
    		}
    	}
    	return desc;
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
	
	public static String sortingCondition(String vccondition) {
        String sorintCondition= "";
        
        if(vccondition == null || vccondition.equals("")){
            sorintCondition= "";
        }else{
            vccondition = vccondition.replaceAll("\"", "");
            ArrayList<String> condOrList = new ArrayList<String>();
            String[] stn = vccondition.split(" OR ");
            for(int or=0; or < stn.length; or++){
                ArrayList<String> conditionList = new ArrayList<String>();
                String[] con = stn[or].trim().split(" AND ");
                for(int and=0; and < con.length; and++){
                    conditionList.add(con[and].trim());
                }
                condOrList.add(toString(conditionList, " AND "));
            }
            sorintCondition = toString(condOrList, " OR ");
        }
        return sorintCondition;
    }

    public static String toString(ArrayList<String> conditionList, String sperator) {

        String list = "";

        Collections.sort(conditionList);

        for(int i = 0 ; i < conditionList.size() ; i++){
            String condition = (String) conditionList.get(i);
            if(list.equals("")){
                list = condition;
            }else{
                list = list + sperator + condition;
            }
        }

        return list;
    }
}
