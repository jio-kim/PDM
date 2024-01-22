package com.symc.plm.me.sdv.operation.ps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVProcessUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEWorkarea;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

public class ProcessSheetUtils {

    public static final String PROCESS_SHEET_EN_DEFAULT_FONT = "Tahoma";

    /**
     * 장비 타입인지 체크한다.
     *
     * [SR140702-043][20140702] shcho 차체 작업표준서 자원(설비/공구)목록에서 GUN 제거
     *
     * @method isEquipment
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isEquipment(TCComponentBOPLine bopLine, String processType) throws TCException {
        String type = bopLine.getItem().getType();
        boolean retVal = false;

        if("B".equals(processType)) {
            retVal = SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(type);
            //retVal = SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(type);
        } else {
            retVal = SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(type);
        }

        return retVal;
    }

    /**
     * 공구 타입인지 체크한다.
     *
     * @method isTool
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isTool(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(type)) {
            return true;
        }

        return false;
    }

    /**
     * End Item 타입인지 체크한다.
     *
     * @method isEndItem
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isEndItem(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.EBOM_STD_PART.equals(type) || SDVTypeConstant.EBOM_VEH_PART.equals(type)) {
            return true;
        }

        return false;
    }

    /**
     * 부자재 타입인지 체크한다.
     *
     * @method isSubsidiary
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isSubsidiary(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(type)) {
            return true;
        }

        return false;
    }

    /**
     * 작업영역 타입인지 체크한다.
     *
     * @method isPlant
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isPlant(TCComponentBOPLine bopLine) throws TCException {
        if (bopLine.getItem() instanceof TCComponentMEWorkarea) {
            return true;
        }

        return false;
    }

    /**
     * 공정 타입인지 체크한다.
     *
     * @method isStation
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isStation(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_STATION_ITEM.equals(type)) {
            return true;
        }

        return false;
    }

    /**
     * 공법 타입인지 체크한다.
     *
     * @method isOperation
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isOperation(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM.equals(type)) {
            return true;
        }

        return false;
    }

    /**
     * Shop 타입인지 체크한다.
     *
     * @method isShop
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isShop(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_SHOP_ITEM.equals(type)) {
            return true;
        }

        return false;
    }

    /**
     * Line 타입인지 체크한다.
     *
     * @method isLine
     * @date 2013. 11. 22.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isLine(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_LINE_ITEM.equals(type)) {
            return true;
        }

        return false;
    }

    public static String changeToEnglishOperationId(String operationId, String processType) {
        String englishOpId = null;
        if("B".equals(processType)) {
            englishOpId = operationId.substring(0, 4) + "E" + operationId.substring(5);
        } else if("P".equals(processType)) {
            englishOpId = operationId + "E";
        } else {
            englishOpId = operationId.substring(0, 3) + "E" + operationId.substring(4);
        }

        return englishOpId;
    }

    public static String getUserName(int configId, TCComponentUser user) throws TCException {
        String userName = "";
        if(configId == 0) {
            TCComponentPerson person = (TCComponentPerson) user.getUserInformation().get(0);
            if(person != null) {
                userName = person.getProperty("user_name");
            }
        } else {
            String[] names = user.getOSUserName().split(" ");
            if(names != null && names.length > 0) {
                for(int i = 0; i < names.length; i++) {
                    userName += names[i].substring(0, 1);
                }
            }
        }

        return userName;
    }

    public static TCComponentItemRevision getLatestPublishedItemRev(int configId, String operationId, String operationRev) throws Exception {
        Registry registry = Registry.getRegistry(ProcessSheetUtils.class);
        String itemId = registry.getString("ProcessSheetItemIDPrefix." + configId) + operationId;
        TCComponentItem operationItem = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
        if(operationItem != null) {
            TCComponent[] revisions = operationItem.getRelatedComponents("revision_list");
            for(int i = revisions.length - 1; i >= 0; i--) {
                if(revisions[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(operationRev)) {
                    return (TCComponentItemRevision) revisions[i];
                }
            }
        }

        return null;
    }

    /**
     * MECO 결재자 중 팀장 이름을 조회한다.
     *
     * @method getMECOTeamLeaderSignoff
     * @date 2013. 11. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getTeamLeaderSignoff(int configId, TCComponentItemRevision itemRev) throws TCException {
        HashMap<String, TCComponent[]> signoffs = new HashMap<String, TCComponent[]>();
        AIFComponentContext[] ctx = itemRev.whereReferenced();
        if(ctx != null) {
            for(int i = 0; i < ctx.length; i++) {
                TCComponent component = (TCComponent) ctx[i].getComponent();
                if(component instanceof TCComponentTask) {
                    TCComponentTask task = (TCComponentTask) component;
                    signoffs = SDVProcessUtils.getSignOffs(signoffs, task);
                    if(signoffs.containsKey("Team Leader")) {
                        TCComponentSignoff reader = (TCComponentSignoff) signoffs.get("Team Leader")[0];
                        TCComponentUser user = reader.getGroupMember().getUser();
                        return ProcessSheetUtils.getUserName(configId, user);
                    }
                }
            }
        }

        return "";
    }

    public static File getLatestPublishProcessSheet(int configId, String operationId, String operationRev) throws Exception {
        TCComponentItemRevision itemRevision = getLatestPublishedItemRev(configId, operationId, operationRev);
        if(itemRevision != null) {
            TCComponent dataset = itemRevision.getRelatedComponent(SDVTypeConstant.IMAN_SPECIFICATION_RELATION);
            if(dataset != null && dataset instanceof TCComponentDataset) {
                TCComponentTcFile[] files = ((TCComponentDataset) dataset).getTcFiles();
                if(files != null && files.length > 0) {
                    return files[0].getFile(null);
                }
            }
        }

        return null;
    }

    /**
     * MECO Release시 첨부된 작업표준서 엑셀 파일에 결재일과 결재자 이름을 업데이트 한다.
     * 배치 프로그램에서 호출함.
     *
     * @method updateMECOReleaseInfo
     * @date 2013. 12. 31.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void updateMECOReleaseInfo(String mecoId) {
        try {
            TCComponentItem mecoItem = SDVBOPUtilities.FindItem(mecoId, SDVTypeConstant.MECO_ITEM);
            if(mecoItem != null) {
                TCComponentItemRevision mecoRev = mecoItem.getLatestItemRevision();
                TCComponent[] components = mecoRev.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                if(components != null) {
                    for(TCComponent component : components) {
                        TCComponent dataset = component.getRelatedComponent(SDVTypeConstant.IMAN_SPECIFICATION_RELATION);
                        if(dataset != null) {
                            TCComponentTcFile[] tcFiles = ((TCComponentDataset) dataset).getTcFiles();
                            if(tcFiles != null && tcFiles.length > 0) {
                                File file = updateFile(mecoRev, tcFiles[0].getFile(null));

                                Vector<File> files = new Vector<File>();
                                files.add(file);
                                SYMTcUtil.removeAllNamedReference((TCComponentDataset) dataset);
                                SYMTcUtil.importFiles((TCComponentDataset) dataset, files);

                                file.delete();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File updateFile(TCComponentItemRevision mecoRev, File file) throws Exception {
        String mecoId = mecoRev.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String approver = getTeamLeaderSignoff(0, mecoRev);

        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        int sheetCnt = workbook.getNumberOfSheets();
        for(int i = 0; i < sheetCnt; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            if(sheetName.startsWith("갑")) {
                for(int j = 39; j >= 34; j--) {
                    Row row = sheet.getRow(j);
                    String mecoDesc = row.getCell(28).getStringCellValue();
                    if(mecoDesc != null && mecoDesc.contains(mecoId)) {
                        row.getCell(24).setCellValue(SDVStringUtiles.dateToString(new Date(), "yyyy-MM-dd"));
                        row.getCell(40).setCellValue(approver);
                        break;
                    }
                }
            } else if(sheetName.startsWith("MECO")) {
                if(!workbook.isSheetHidden(i)) {
                    for(int j = 40; j >= 4; j--) {
                        Row row = sheet.getRow(j);
                        String mecoDesc = row.getCell(12).getStringCellValue();
                        if(mecoDesc != null && mecoDesc.contains(mecoId)) {
                            row.getCell(5).setCellValue(SDVStringUtiles.dateToString(new Date(), "yyyy-MM-dd"));
                            row.getCell(37).setCellValue(approver);
                            break;
                        }
                    }
                }
            }
        }

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

    public static TCComponentDataset translateProcessSheet(TCComponentItemRevision revision, String processType) throws Exception {
        TCComponent dataset = revision.getRelatedComponent(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
        TCComponentDataset newDataset = null;
        if(dataset != null && dataset instanceof TCComponentDataset) {
            File korFile = getFile((TCComponentDataset) dataset);
            File engFile = ExcelTemplateHelper.getTemplateFile(0, "M7_TEM_DocItemID_ProcessSheet_Eng", null);
            if(korFile != null && engFile != null) {
                Workbook korWorkbook = new XSSFWorkbook(new FileInputStream(korFile));
                Workbook engWorkbook = new XSSFWorkbook(new FileInputStream(engFile));
                int sheetCnt = korWorkbook.getNumberOfSheets();

                Sheet engASheet = engWorkbook.getSheet("ASHEET");
                Sheet engBSheet = engWorkbook.getSheet("BSHEET");
                Sheet mecoSheet = engWorkbook.getSheet("MECO");

                for(int i = 0; i < sheetCnt; i++) {
                    Sheet korSheet = korWorkbook.getSheetAt(i);

                    replaceImage(korSheet, engASheet);

                    String sheetName = korSheet.getSheetName();
                    if(sheetName.startsWith("갑") || sheetName.equals("ASHEET")) {
                        copySheet(korSheet, engASheet, 0);
                        sheetName = sheetName.replaceAll("갑", "A");
                        korWorkbook.setSheetName(i, sheetName);
                    } else if(sheetName.startsWith("을") || sheetName.equals("BSHEET")) {
                        copySheet(korSheet, engBSheet, 1);
                        sheetName = sheetName.replaceAll("을", "B");
                        korWorkbook.setSheetName(i, sheetName);
                    } else if(sheetName.equals("MECO")) {
                        copySheet(korSheet, mecoSheet, 2);
                    }
                }
                FileOutputStream fos = new FileOutputStream(korFile.getAbsolutePath());
                korWorkbook.write(fos);
                fos.flush();
                fos.close();

                String datasetName = revision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "/" +
                        revision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                newDataset = SDVBOPUtilities.createDataset(korFile.getAbsolutePath(), datasetName);
            }
        }

        return newDataset;
    }

    public static void replaceImage(Sheet targetSheet, Sheet sourceSheet) {
        Drawing drawingTarget = targetSheet.createDrawingPatriarch();
        Drawing drawingSource = sourceSheet.createDrawingPatriarch();

        if(drawingTarget instanceof XSSFDrawing) {
            // 국문 이미지 삭제
            List<XSSFShape> shapes = ((XSSFDrawing) drawingTarget).getShapes();
            for(int i = 0; i < shapes.size(); i++) {
                XSSFShape shape = shapes.get(i);
                if(shape instanceof XSSFPicture) {
                    XSSFPicture pic = (XSSFPicture) shape;
                    XSSFClientAnchor sourceAnchor = (XSSFClientAnchor) pic.getAnchor();

                    int fromCol = sourceAnchor.getCol1();
                    int toCol = sourceAnchor.getCol2();
                    int fromRow = sourceAnchor.getRow1();
                    int toRow = sourceAnchor.getRow2();
                    if((fromCol == 0 && toCol == 4 && fromRow == 0 && toRow == 2) ||
                            (fromCol == 35 && toCol == 52 && fromRow == 41 && toRow == 42)) {
                        ((XSSFPicture) shape).resize(0);
                    }
                }
            }

            // 영문 이미지 복사
            shapes = ((XSSFDrawing) drawingSource).getShapes();
            for(int i = 0; i < shapes.size(); i++) {
                XSSFShape shape = shapes.get(i);
                if(shape instanceof XSSFPicture) {
                    XSSFPicture pic = (XSSFPicture) shapes.get(i);
                    XSSFPictureData picdata = pic.getPictureData();
                    int pictureIndex = targetSheet.getWorkbook().addPicture(picdata.getData(), picdata.getPictureType());

                    XSSFClientAnchor sourceAnchor = (XSSFClientAnchor) pic.getAnchor();
                    XSSFClientAnchor targetAnchor = (XSSFClientAnchor) targetSheet.getWorkbook().getCreationHelper().createClientAnchor();

                    int fromCol = sourceAnchor.getCol1();
                    int toCol = sourceAnchor.getCol2();
                    int fromRow = sourceAnchor.getRow1();
                    int toRow = sourceAnchor.getRow2();

                    if((fromCol == 0 && toCol == 4 && fromRow == 0 && toRow == 2) ||
                            (fromCol == 30 && toCol == 56 && fromRow == 41 && toRow == 42)) {
                        targetAnchor.setDx1(sourceAnchor.getDx1());
                        targetAnchor.setDy1(sourceAnchor.getDy1());
                        targetAnchor.setDx2(sourceAnchor.getDx2());
                        targetAnchor.setDy2(sourceAnchor.getDy2());
                        targetAnchor.setCol1(sourceAnchor.getCol1());
                        targetAnchor.setRow1(sourceAnchor.getRow1());
                        targetAnchor.setCol2(sourceAnchor.getCol2());
                        targetAnchor.setRow2(sourceAnchor.getRow2());

                        drawingTarget.createPicture(targetAnchor, pictureIndex);
                    }
                }
            }
        }
    }

    public static void copySheet(Sheet targetSheet, Sheet sourceSheet, int sheetType) {
        cellCopy(targetSheet, sourceSheet, 0, 2, 0, 103);

        if(sheetType == 0) {
            cellCopy(targetSheet, sourceSheet, 3, 33, 42, 103);
            cellCopy(targetSheet, sourceSheet, 34, 40, 0, 103);
            Row targetRow = targetSheet.getRow(3);
            Row sourceRow = sourceSheet.getRow(3);
            String cellValue = sourceRow.getCell(0).getStringCellValue();
            targetRow.getCell(0).setCellValue(cellValue);

            targetRow = targetSheet.getRow(21);
            sourceRow = sourceSheet.getRow(21);
            cellValue = sourceRow.getCell(0).getStringCellValue();
            targetRow.getCell(0).setCellValue(cellValue);
        } else if(sheetType == 1) {
            cellCopy(targetSheet, sourceSheet, 34, 40, 42, 103);
            Row targetRow = targetSheet.getRow(3);
            Row sourceRow = sourceSheet.getRow(3);
            String cellValue = sourceRow.getCell(0).getStringCellValue();
            targetRow.getCell(0).setCellValue(cellValue);
        } else if(sheetType == 2) {
            cellCopy(targetSheet, sourceSheet, 3, 40, 0, 41);
            cellCopy(targetSheet, sourceSheet, 34, 40, 42, 103);
        }

        // Font 적용
        for(int i = 0; i <= 40; i++) {
            Row targetRow = targetSheet.getRow(i);
            for(int j = 0; j <= 103; j++) {
                Cell cell = targetRow.getCell(j);
               
                if( null != cell ) {
                	((XSSFCellStyle) cell.getCellStyle()).getFont().setFontName(PROCESS_SHEET_EN_DEFAULT_FONT);
                }
               
            }
        }
    }

    public static void cellCopy(Sheet targetSheet, Sheet sourceSheet, int startRow, int endRow, int startCell, int endCell) {
        for(int i = startRow; i <= endRow; i++) {
            Row targetRow = targetSheet.getRow(i);
            Row sourceRow = sourceSheet.getRow(i);
            for(int j = startCell; j <= endCell; j++) {
                Cell targetCell = targetRow.getCell(j);
                String cellValue = sourceRow.getCell(j).getStringCellValue();
                targetCell.setCellValue(cellValue);
            }
        }
    }

    public static File getFile(TCComponentDataset dataset) throws TCException {
        TCComponentTcFile[] files = dataset.getTcFiles();
        if(files != null && files.length > 0) {
            return files[0].getFile(null);
        }

        return null;
    }

    public static TCComponentBOPLine getLine(TCComponentBOPLine bopLine) throws TCException {
        TCComponentBOPLine parent = (TCComponentBOPLine) bopLine.parent();
        if(parent instanceof TCComponentAppGroupBOPLine) {
            parent = (TCComponentBOPLine) bopLine.getReferenceProperty(SDVPropertyConstant.BL_MFG0IMPLEMENTS);
        }

        if(isLine(parent)) {
            return parent;
        } else {
            return getLine(parent);
        }
    }

    public static TCComponentBOPLine getShop() throws TCException {
        AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
        if(application instanceof MFGLegacyApplication) {
            TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
            TCComponentBOPLine topLine = null;
            if(bopWindow.getBaseView() != null) {
                topLine = (TCComponentBOPLine) bopWindow.getBaseView().getTopBOMLine();
            } else {
                topLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
            }

            if(isShop(topLine)) {
                return topLine;
            }
        }

        return null;
    }

    public static TCComponentBOPLine getParent(TCComponentBOPLine bopLine, String type) throws TCException {
        TCComponentBOPLine parent = (TCComponentBOPLine) bopLine.parent();

        if(parent.getItem().getType().equals(type)) {
            return parent;
        } else {
            return getParent(parent, type);
        }
    }

    public static String getProcessType(TCComponentItemRevision revision) {
        String processType = null;
        String itemType = revision.getType();
        if(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV.equals(itemType)) {
            processType = "A";
        } else if(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV.equals(itemType)) {
            processType = "B";
        } else if(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV.equals(itemType)) {
            processType = "P";
        }

        return processType;
    }

    public static void openExcleFile(String filePath) throws IOException {
    	AIFShell aif = new AIFShell("application/vnd.ms-excel", filePath);
    	aif.start();
    	// 영문 작업표준서 Preview, Search 화면에서 Open Ko, Open 버튼으로 엑셀 파일 Open 시 
    	// DSBSInfo.dll 가 없어 프로그램을 시작 할 수 없습니다. 라는 메세지 발생 (Open은 됨)
    	// Excel 파일 Open 방식 변경
//        String command = "cmd.exe /c start excel /x " + filePath;
//        Runtime.getRuntime().exec(command);
      
    }
}
