package com.symc.plm.me.sdv.excel.common;

import java.io.File;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;

public class ProcessSheetExcelHelper {

    private final static String PS_A_SHEET_NAME = "ASHEET";

    public static File getImageTemplateFile(TCComponentBOPLine operation) throws TCException {
        File file = null;
        TCComponentItemRevision operationRev = operation.getItemRevision();
        TCComponent comp = operationRev.getRelatedComponent("M7_PROCESS_SHEET_KO_REL");
        if(comp != null && comp instanceof TCComponentDataset) {
            TCComponentDataset dataSet = (TCComponentDataset) comp;
            TCComponentTcFile tcFile = dataSet.getTcFiles()[0];
            if(tcFile != null) {

            }
        }

        return file;
    }

    public static Workbook addASheets(Workbook workbook, int cnt, int configId) {
        Sheet sheetA = workbook.getSheet(PS_A_SHEET_NAME);
        boolean pageOrientation = sheetA.getPrintSetup().getLandscape();
        
        if(sheetA != null) {
            for(int i = 1; i <= cnt; i++) {
                Sheet copiedSheet = workbook.cloneSheet(workbook.getSheetIndex(sheetA));
                String sheetName = null;
                if(configId == 0) {
                    sheetName = "갑" + (i + 1);
                } else {
                    sheetName = "A" + (i + 1);
                }
                workbook.setSheetOrder(copiedSheet.getSheetName(), i);
                workbook.setSheetName(i, sheetName);
                
                /*
                 * 20140416
                 * sheet 복사 후 인쇄방향 가로로 설정(Template 파일에 지정된 가로 설정과 동일하게 설정)
                 */
                if(copiedSheet.getPrintSetup().getLandscape() != pageOrientation) {
                    copiedSheet.getPrintSetup().setLandscape(pageOrientation);
                }
            }
        }

        return workbook;
    }

    public static void downloadProcessSheet(TCComponentItemRevision revision, String filePath, String fileName) throws TCException {
        TCComponent comp = revision.getRelatedComponent("IMAN_specification");
        if(comp != null && comp instanceof TCComponentDataset) {
            TCComponentDataset dataSet = (TCComponentDataset) comp;
            TCComponentTcFile tcFile = dataSet.getTcFiles()[0];
            if(tcFile != null) {
                tcFile.getFile(filePath, fileName);
            }
        }
    }

}
