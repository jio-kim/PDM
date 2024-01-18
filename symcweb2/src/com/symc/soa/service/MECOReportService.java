package com.symc.soa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.util.TcConstants;
import com.symc.work.model.SYMCBOPEditData;
import com.symc.work.service.MECOService;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedInfo;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Person;
import com.teamcenter.soa.client.model.strong.Signoff;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

public class MECOReportService {

    private Session tcSession;
    private SDVTCDataManager dataManager;

    private final String MECO_TEMPLATE = "ME_DOCTEMP_19";

    private final String SERVER_DIRECTORY = "\\\\150.1.11.105\\s4c3\\SYMC_PLM_FTP";

    private final int HEADER_CELL_COUNT = 15;
    private final int DATA_START_ROW_INDEX = 5;

    public MECOReportService(Session session) {
        this.tcSession = session;
        this.dataManager = new SDVTCDataManager(tcSession);
        try {
            this.dataManager.setByPass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFileMECOReport(String mecoId) throws Exception {
        File file = null;

        ItemRevision mecoRevision = null;
        ItemRevision documentRevision = null;
        try {
            mecoRevision = dataManager.getLatestItemRevision(mecoId);
            documentRevision = dataManager.getReleasedLatestItemRevision(MECO_TEMPLATE);
            if (mecoRevision == null) {
                throw new Exception(mecoId + "가 존재하지 않습니다.");
            }
            if (documentRevision == null) {
                throw new Exception(MECO_TEMPLATE + " 템플릿이 존재하지 않습니다.");
            }

            Dataset[] datasets = dataManager.getAllDatasets(documentRevision);
            ImanFile[] imanFiles = null;
            if (datasets != null && datasets.length > 0) {
                for (Dataset temp : datasets) {
                    imanFiles = dataManager.getNamedReferenceFile(temp, TcConstants.TYPE_NR_EXCEL_2007);
                }
                if (imanFiles != null && imanFiles.length > 0) {
                    File template = dataManager.getFiles(imanFiles[0]);
                    if (template != null) {
                        String defaultFileName = mecoId + "_" + getToday("yyyyMMdd");
                        String absolutePath = template.getAbsolutePath();
                        String parentPath = template.getParent();
                        String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
                        file = new File(parentPath + "\\" + defaultFileName + extension);
                        if (file.exists()) {
                            if (file.delete()) {
                                file = new File(parentPath + "\\" + defaultFileName + extension);
                            }
                        }
                        template.renameTo(file);
                        printMECOReport(file, mecoRevision);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());

            throw e;
        }

        // SERVER_DIRECTORY(s4c3)에 해당 파일이 존재하면 삭제
        if (file != null) {
            File server_file_path = new File(SERVER_DIRECTORY + File.separator + file.getName());
            if (server_file_path.exists() && server_file_path.isFile()) {
                server_file_path.delete();
            }
        }

        return file;
    }

    public void printMECOReport(File file, ItemRevision mecoRevision) throws Exception {
        System.out.println("-----------------------------------------------------");
        System.out.println("--- Print MECO Report Start ---");

        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));

        printMECOASheet(workbook, mecoRevision);
        printMECOEPLSheet(workbook, mecoRevision);

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        System.out.println("--- Print MECO Report End ---");
        System.out.println("-----------------------------------------------------");
    }

    /**
     * MECO A
     *
     * @method printMECOASheet
     * @date 2014. 2. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void printMECOASheet(Workbook workbook, ItemRevision mecoRevision) throws Exception {
        Sheet currentSheet = workbook.getSheetAt(0);
        Row row = null;
        Cell cell = null;

        mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision, new String[] { TcConstants.PROP_DATE_RELEASED, TcConstants.PROP_ITEM_ID, TcConstants.MECO_PROJECT, TcConstants.MECO_MATURITY, TcConstants.MECO_EFFECT_DATE, TcConstants.MECO_EFFECT_EVENT, TcConstants.MECO_CHANGE_REASON, TcConstants.PROP_OBJECT_DESC });

        // Print Date
        String printDate = getToday("yyyy-MM-dd HH:mm:ss");
        row = currentSheet.getRow(1);
        cell = row.getCell(7);
        cell.setCellValue(printDate);

        // MECO Date
        if (mecoRevision.getPropertyObject(TcConstants.PROP_DATE_RELEASED).getCalendarValue() != null) {
            String date_released = getFormatDate(mecoRevision.getPropertyObject(TcConstants.PROP_DATE_RELEASED).getCalendarValue().getTime(), "yyyy-MM-dd");
            row = currentSheet.getRow(1);
            cell = row.getCell(6);
            cell.setCellValue(date_released);
        }

        // MECO Number
        String mecoId = mecoRevision.getPropertyObject(TcConstants.PROP_ITEM_ID).getStringValue();
        row = currentSheet.getRow(3);
        cell = row.getCell(2);
        cell.setCellValue(mecoId);

        // MECO Dept
        User user = (User) mecoRevision.get_owning_user();
        user = (User) dataManager.loadObjectWithProperties(user, new String[] { "person" });
        Person person = user.get_person();
        person = (Person) dataManager.loadObjectWithProperties(person, new String[] { TcConstants.PROP_DEPT_NAME });
        String dept = person.get_PA6();
        row = currentSheet.getRow(3);
        cell = row.getCell(4);
        cell.setCellValue(dept);

        // 차종
        String project = mecoRevision.getPropertyObject(TcConstants.MECO_PROJECT).getStringValue();
        row = currentSheet.getRow(5);
        cell = row.getCell(2);
        cell.setCellValue(project);

        // Status
        String maturity = mecoRevision.getPropertyObject("m7_MECO_MATURITY").getStringValue();
        row = currentSheet.getRow(6);
        cell = row.getCell(2);
        cell.setCellValue(maturity);

        // Eff.Date
        String effDate = mecoRevision.getPropertyObject(TcConstants.MECO_EFFECT_DATE).getStringValue();
        row = currentSheet.getRow(7);
        cell = row.getCell(2);
        cell.setCellValue(effDate);

        // Eff.Event
        String effEvent = mecoRevision.getPropertyObject(TcConstants.MECO_EFFECT_EVENT).getStringValue();
        row = currentSheet.getRow(7);
        cell = row.getCell(4);
        cell.setCellValue(effEvent);

        // Change Reason
        String changeReason = mecoRevision.getPropertyObject(TcConstants.MECO_CHANGE_REASON).getStringValue();
        if (changeReason.equals("01")) {
            changeReason = "Reflection of E-BOM";
        } else if (changeReason.equals("02")) {
            changeReason = "Correction of Process";
        } else if (changeReason.equals("03")) {
            changeReason = "Regulation";
        } else if (changeReason.equals("04")) {
            changeReason = "Revision for Project";
        } else if (changeReason.equals("05")) {
            changeReason = "Initial for Running Change";
        } else if (changeReason.equals("06")) {
            changeReason = "Initial for Project";
        } else if (changeReason.equals("07")) {
            changeReason = "BOP Reallocation";
        } else if (changeReason.equals("08")) {
            changeReason = "The Others";
        }
        row = currentSheet.getRow(8);
        cell = row.getCell(2);
        cell.setCellValue(changeReason);

        // List<LovValue> lovValues = mecoRevision.getTypeObject().getPropDesc(TcConstants.MECO_CHANGE_REASON).getLovReference().getLovInfo().getValues();
        // if (lovValues != null) {
        // for (LovValue lovValue : lovValues) {
        // Object object = lovValue.getValue();
        // if (object != null) {
        // if (changeReason.equals(object.toString())) {
        // row = currentSheet.getRow(8);
        // cell = row.getCell(2);
        // cell.setCellValue(lovValue.getDescription());
        // }
        // }
        // }
        // } else {
        // row = currentSheet.getRow(8);
        // cell = row.getCell(2);
        // cell.setCellValue(changeReason);
        // }

        // Change Description
        String description = mecoRevision.getPropertyObject(TcConstants.PROP_OBJECT_DESC).getStringValue();
        row = currentSheet.getRow(9);
        cell = row.getCell(2);
        cell.setCellValue(description);

        // Signature
        WorkspaceObject[] objects = { mecoRevision };
        WhereReferencedInfo[] info = dataManager.getWhereReferenced(objects, 1);
        if (info != null) {
            for (WhereReferencedInfo whereReferencedInfo : info) {
                WorkspaceObject object = whereReferencedInfo.referencer;
                if (object.get_object_type().equals("EPMTask")) {
                    EPMTask rootTask = (EPMTask) object;
                    rootTask = (EPMTask) dataManager.loadObjectWithProperties(rootTask, new String[] { TcConstants.PROP_PROCESS_CHILD_TASKS });
                    ModelObject[] modelObjects = rootTask.get_child_tasks();
                    for (ModelObject modelObject : modelObjects) {
                        String taskName = modelObject.getPropertyDisplayableValue("object_string");
                        if (taskName.equals("Creator")) {
                            Map<String, String> dataMap = getSignatureInfo((EPMTask) modelObject);

                            row = currentSheet.getRow(11);
                            cell = row.getCell(3);
                            cell.setCellValue(dataMap.get("approver"));

                            cell = row.getCell(4);
                            cell.setCellValue(dataMap.get("tel"));

                            cell = row.getCell(5);
                            cell.setCellValue(dataMap.get(TcConstants.PROP_LAST_MOD_DATE));

                            cell = row.getCell(6);
                            cell.setCellValue(dataMap.get("dept"));

                            cell = row.getCell(7);
                            cell.setCellValue(dataMap.get("comments"));
                        } else if (taskName.equals("Sub Team Leader")) {
                            Map<String, String> dataMap = getSignatureInfo((EPMTask) modelObject);

                            row = currentSheet.getRow(12);
                            cell = row.getCell(3);
                            cell.setCellValue(dataMap.get("approver"));

                            cell = row.getCell(4);
                            cell.setCellValue(dataMap.get("tel"));

                            cell = row.getCell(5);
                            cell.setCellValue(dataMap.get(TcConstants.PROP_LAST_MOD_DATE));

                            cell = row.getCell(6);
                            cell.setCellValue(dataMap.get("dept"));

                            cell = row.getCell(7);
                            cell.setCellValue(dataMap.get("comments"));
                        } else if (taskName.equals("Team Leader")) {
                            Map<String, String> dataMap = getSignatureInfo((EPMTask) modelObject);

                            row = currentSheet.getRow(13);
                            cell = row.getCell(3);
                            cell.setCellValue(dataMap.get("approver"));

                            cell = row.getCell(4);
                            cell.setCellValue(dataMap.get("tel"));

                            cell = row.getCell(5);
                            cell.setCellValue(dataMap.get(TcConstants.PROP_LAST_MOD_DATE));

                            cell = row.getCell(6);
                            cell.setCellValue(dataMap.get("dept"));

                            cell = row.getCell(7);
                            cell.setCellValue(dataMap.get("comments"));
                        } else if (taskName.equals("BOP ADMIN")) {
                            Map<String, String> dataMap = getSignatureInfo((EPMTask) modelObject);

                            row = currentSheet.getRow(14);
                            cell = row.getCell(3);
                            cell.setCellValue(dataMap.get("approver"));

                            cell = row.getCell(4);
                            cell.setCellValue(dataMap.get("tel"));

                            cell = row.getCell(5);
                            cell.setCellValue(dataMap.get(TcConstants.PROP_LAST_MOD_DATE));

                            cell = row.getCell(6);
                            cell.setCellValue(dataMap.get("dept"));

                            cell = row.getCell(7);
                            cell.setCellValue(dataMap.get("comments"));
                        }
                    }
                }
            }
        }
    }

    public Map<String, String> getSignatureInfo(EPMTask subTask) throws Exception {
        Map<String, String> dataMap = new HashMap<String, String>();

        subTask = (EPMTask) dataManager.loadObjectWithProperties(subTask, new String[] { TcConstants.PROP_PROCESS_VALID_SIGNOFFS });
        ModelObject[] signoffs = subTask.get_valid_signoffs();
        if (signoffs != null && signoffs.length > 0) {
            Signoff signoff = (Signoff) signoffs[0];
            GroupMember member = signoff.get_group_member();
            member = (GroupMember) dataManager.loadObjectWithProperties(member, new String[] { TcConstants.PROP_USER });
            User user = (User) member.get_user();
            user = (User) dataManager.loadObjectWithProperties(user, new String[] { "person", TcConstants.PROP_USER_NAME });
            Person person = user.get_person();
            person = (Person) dataManager.loadObjectWithProperties(person, new String[] { TcConstants.PROP_DEPT_NAME, TcConstants.PROP_TEL });

            dataMap.put("approver", member.get_user().get_user_name());
            dataMap.put("dept", person.get_PA6());
            dataMap.put("tel", person.get_PA10());
            dataMap.put(TcConstants.PROP_LAST_MOD_DATE, getFormatDate(signoff.get_last_mod_date().getTime(), "yyyy-MM-dd"));
            dataMap.put("comments", signoff.get_comments());
        }

        return dataMap;
    }

    /**
     * MECO EPL
     *
     * @method printMECOEPLSheet
     * @date 2014. 2. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void printMECOEPLSheet(Workbook workbook, ItemRevision mecoRevision) throws Exception {
        Sheet currentSheet = workbook.getSheetAt(1);
        Row row = null;
        Cell cell = null;

        // Print Date
        String printDate = getToday("yyyy-MM-dd HH:mm:ss");
        row = currentSheet.getRow(1);
        cell = row.getCell(14);
        cell.setCellValue(printDate);

        // MECO Date
        if (mecoRevision.getPropertyObject(TcConstants.PROP_DATE_RELEASED).getCalendarValue() != null) {
            String date_released = getFormatDate(mecoRevision.getPropertyObject(TcConstants.PROP_DATE_RELEASED).getCalendarValue().getTime(), "yyyy-MM-dd");
            row = currentSheet.getRow(1);
            cell = row.getCell(13);
            cell.setCellValue(date_released);
        }

        // MECO No.
        String mecoId = mecoRevision.getPropertyObject(TcConstants.PROP_ITEM_ID).getStringValue();
        row = currentSheet.getRow(3);
        cell = row.getCell(3);
        cell.setCellValue(mecoId);

        // MECO EPL
        MECOService mecoService = new MECOService();
        ArrayList<SYMCBOPEditData> dataList = mecoService.selectMECOEplList(mecoRevision.get_item_id());
        if (dataList != null && dataList.size() > 0) {
            ArrayList<String> addedEPLs = new ArrayList<String>();
            ArrayList<Row> changeBackgroundRow = new ArrayList<Row>();
            for (int i = 0; i < dataList.size(); i++) {
                SYMCBOPEditData data = dataList.get(i);
                String eplId = data.getEplId();
                if (!addedEPLs.contains(eplId)) {
                    Row oldDataRow = currentSheet.createRow(currentSheet.getLastRowNum() + 1);
                    Row newDataRow = currentSheet.createRow(currentSheet.getLastRowNum() + 1);
                    printOldEPLData(addedEPLs.size() + 1, oldDataRow, data);
                    printNewEPLData(addedEPLs.size() + 1, newDataRow, data);

                    if ((addedEPLs.size() + 1) % 2 == 0) {
                        changeBackgroundRow.add(oldDataRow);
                        changeBackgroundRow.add(newDataRow);
                    }

                    addedEPLs.add(eplId);
                }
            }

            setCellStyleOfContents(currentSheet, changeBackgroundRow);
        }
    }

    /**
     * [SR140611-055][20140609] byKim ② MECO EPL에 내용 유무 상관없이 Old/New에 No. 모두 채워줄 것, No. 필드 다음에 Old/New 표시하는 필드 추가 필요
     */
    private void printOldEPLData(int no, Row row, SYMCBOPEditData data) throws Exception {
        for (int i = 0; i < HEADER_CELL_COUNT; i++) {
            row.createCell(i);
        }

        // No
        Cell cell = row.getCell(0);
        cell.setCellValue(no);

        // Old/New
        cell = row.getCell(1);
        cell.setCellValue("Old");

        if (data.getOld_child_no() == null || data.getOld_child_no().equals("")) {
            return;
        }

        // P_TYPE
        cell = row.getCell(2);
        cell.setCellValue(data.getParentType());

        // Parent Part No.
        cell = row.getCell(3);
        cell.setCellValue(data.getParentNo());

        // Rev
        cell = row.getCell(4);
        cell.setCellValue(data.getParentRev());

        // Parent Part Name
        cell = row.getCell(5);
        cell.setCellValue(data.getParentName());

        // SEQ
        cell = row.getCell(6);
        cell.setCellValue(data.getSeq());

        // C_TYPE
        cell = row.getCell(7);
        cell.setCellValue(data.getOld_child_type());

        // Child Part No.
        cell = row.getCell(8);
        cell.setCellValue(data.getOld_child_no());

        // Rev
        cell = row.getCell(9);
        cell.setCellValue(data.getOld_child_rev());

        // Child Part Name
        cell = row.getCell(10);
        cell.setCellValue(data.getOld_child_name());

        // QTY
        cell = row.getCell(11);
        cell.setCellValue(data.getOld_qty());

        // Shown_On
        cell = row.getCell(12);
        cell.setCellValue(data.getOld_shown_no_no());

        // ECO No
        cell = row.getCell(13);
        cell.setCellValue(data.getEcoNo());

        // Option
        cell = row.getCell(14);
        cell.setCellValue(data.getOld_vc());
    }

    /**
     * [SR140611-055][20140609] byKim ② MECO EPL에 내용 유무 상관없이 Old/New에 No. 모두 채워줄 것, No. 필드 다음에 Old/New 표시하는 필드 추가 필요
     */
    private void printNewEPLData(int no, Row row, SYMCBOPEditData data) throws Exception {
        for (int i = 0; i < HEADER_CELL_COUNT; i++) {
            row.createCell(i);
        }

        // No
        Cell cell = row.getCell(0);
        cell.setCellValue(no);

        // Old/New
        cell = row.getCell(1);
        cell.setCellValue("New");

        if (data.getNew_child_no() == null || data.getNew_child_no().equals("")) {
            return;
        }

        // P_TYPE
        cell = row.getCell(2);
        cell.setCellValue(data.getParentType());

        // Parent Part No.
        cell = row.getCell(3);
        cell.setCellValue(data.getParentNo());

        // Rev
        cell = row.getCell(4);
        cell.setCellValue(data.getParentRev());

        // Parent Part Name
        cell = row.getCell(5);
        cell.setCellValue(data.getParentName());

        // SEQ
        cell = row.getCell(6);
        cell.setCellValue(data.getSeq());

        // C_TYPE
        cell = row.getCell(7);
        cell.setCellValue(data.getNew_child_type());

        // Child Part No.
        cell = row.getCell(8);
        cell.setCellValue(data.getNew_child_no());

        // Rev
        cell = row.getCell(9);
        cell.setCellValue(data.getNew_child_rev());

        // Child Part Name
        cell = row.getCell(10);
        cell.setCellValue(data.getNew_child_name());

        // QTY
        cell = row.getCell(11);
        cell.setCellValue(data.getNew_qty());

        // Shown_On
        cell = row.getCell(12);
        cell.setCellValue(data.getNew_shown_no_no());

        // ECO No
        cell = row.getCell(13);
        cell.setCellValue(data.getEcoNo());

        // Option
        cell = row.getCell(14);
        cell.setCellValue(data.getNew_vc());
    }

    /**
     * Contents Cell Style
     *
     * @method setCellStyleOfContents
     * @date 2014. 2. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCellStyleOfContents(Sheet currentSheet, ArrayList<Row> changeBackgroundRow) throws Exception {
        Workbook workbook = currentSheet.getWorkbook();

        Font font = workbook.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 10);

        // 가운데 정렬 column
        int[] center_columnArray = new int[] { 0, 1, 4, 6, 9, 11 };
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        int lastRowNum = currentSheet.getLastRowNum();
        for (int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            Row row = currentSheet.getRow(i);
            for (int j = 0; j < HEADER_CELL_COUNT; j++) {
                Cell cell = row.getCell(j);

                XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
                style.setFont(font);
                style.setBorderTop(XSSFCellStyle.BORDER_THIN);
                style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
                style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
                style.setBorderRight(XSSFCellStyle.BORDER_THIN);

                if (changeBackgroundRow.contains(row)) {
                    style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                }

                if (center_columnList.contains(j)) {
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                }

                cell.setCellStyle(style);
            }
        }
    }

    /**
     * 오늘 날짜 가져오기
     *
     * @method getToday
     * @date 2014. 2. 17.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getToday(String format) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date(System.currentTimeMillis());
        String today = dateFormat.format(date);

        return today;
    }

    /**
     * 날짜 포맷 변경
     *
     * @method getFormatDate
     * @date 2014. 2. 17.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getFormatDate(Date date, String format) throws Exception {
        String strDate = "";

        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            strDate = simpleDateFormat.format(date);
        }

        return strDate;
    }

}
