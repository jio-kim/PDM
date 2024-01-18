package com.symc.soa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.util.TcConstants;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMPerformSignoffTask;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Signoff;

/**
 * [SR140611-027][20140611] byKim ① 용접조건표 을지 추가
 * [SR140716-041][20140710] shcho,  MECO 결재시 사용자 승인 후 System에서  용접조건표에 MECO 날짜 업데이트를 수행하지 않는 오류 수정(작업표준서와 용접조건표의 ExcelUpdateService에서 중복적으로 처리하고 있어서 오류 발생. MECOService에서 일원화 함)
 * [SR150522-029] [20150610] ymjang 결재 완료된 용접조건표의 작성일, 결재자 정보 표기 안됨.
 * 1. 기 결재된 MECO 에 대해서는 진행중인 Workflow 가 없기 때문에 오류 발생함. --> MECO 와 관련된 Workflow의 결재라인을 찾도록 개선함.
 *
*/
public class WeldOPExcelUpdateService {

    private Session tcSession;
    private SDVTCDataManager dataManager;
    private TcItemUtil tcItemUtil;

    private static final String DEFAULT_BASE_SHEET = "1";
    private static final int DEFAULT_MECO_LIST_ROW_COUNT = 5;
    private static final int DEFAULT_MECO_START_ROW_INDEX = 87;
    private static final int[] DEFAULT_MECO_CELL = { 0, 2, 5, 9, 17, 19, 21, 23, 26, 30, 38, 40 };

    public WeldOPExcelUpdateService(Session session) {
        this.tcSession = session;
        this.dataManager = new SDVTCDataManager(tcSession);
        this.tcItemUtil = new TcItemUtil(tcSession);
        try {
            this.dataManager.setByPass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMECOReleaseInfo(String mecoId) {
        String result = "END";
        ItemRevision mecoRevision = null;
        try {
            mecoRevision = dataManager.getLatestItemRevision(mecoId);
            if (mecoRevision == null) {
                throw new Exception(mecoId + "가 존재하지 않습니다.");
            }
            mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision, new String[] { TcConstants.MECO_TYPE, TcConstants.WELD_CONDITIOIN_SHEET_RELATION, TcConstants.CMHAS_SOLUTION_ITEM });
            String mecoType = mecoRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
            String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");

            Date signoffDate = null;
            String approver = null;
            ModelObject[] processes = dataManager.getProcess(mecoRevision);
            if (processes == null || processes.length == 0) {
                throw new Exception("MECO " + mecoId + "에 진행중인 결재 프로세스가 존재하지 않습니다.");
            }
            for (ModelObject process : processes) {
                process = dataManager.loadObjectWithProperties(process, new String[] { TcConstants.PROP_OBJECT_NAME });
                String processName = ((EPMTask) process).get_object_name();
                for (String prefValue : prefValues) {
                    if (prefValue.equals(processName)) {
                        EPMTask rootTask = (EPMTask) process;
                        EPMPerformSignoffTask signoffTask = dataManager.getPerformSignoffTask(rootTask, "Team Leader");
                        signoffTask = (EPMPerformSignoffTask) dataManager.loadObjectWithProperties(signoffTask, new String[] { TcConstants.PROP_PROCESS_VALID_SIGNOFFS, TcConstants.PROP_LAST_MOD_DATE });
                        approver = getSignoffName(signoffTask);
                        signoffDate = signoffTask.get_last_mod_date().getTime();
                        break;
                    }
                }
            }

            if (approver == null) {
                throw new Exception("MECO " + mecoId + "의 결재 프로세스에 Team Leader 결재 정보가 존재하지 않습니다.");
            }
            
            /*
            // [SR150522-029] [20150610] ymjang 결재 완료된 용접조건표의 작성일, 결재자 정보 표기 안됨.
            EPMTask rootTask = dataManager.getEPMTask(mecoId, mecoRevision, prefValues);
            EPMPerformSignoffTask signoffTask = dataManager.getPerformSignoffTask(rootTask, "Team Leader");
            signoffTask = (EPMPerformSignoffTask) dataManager.loadObjectWithProperties(signoffTask, new String[] {TcConstants.PROP_PROCESS_VALID_SIGNOFFS, TcConstants.PROP_LAST_MOD_DATE});
            Date signoffDate = signoffTask.get_last_mod_date().getTime();
            String approver = getSignoffName(signoffTask);

            if (approver == null) {
                throw new Exception("MECO " + mecoId + " 의 결재 프로세스에 Team Leader 결재 정보가 존재하지 않습니다.");
            }
            */
            
            ModelObject[] wpRevisions = mecoRevision.getPropertyObject(TcConstants.CMHAS_SOLUTION_ITEM).getModelObjectArrayValue();
            if (wpRevisions == null) {
                throw new Exception("MECO " + mecoId + "에 첨부된 용접공법이 없습니다.");
            }

            for (ModelObject wpRevision : wpRevisions) {
                String type = wpRevision.getTypeObject().getName();
                if (type.equals("M7_BOPWeldOPRevision")) {
                    // MECO No
                    List<String> mecoList = getMECONoList(wpRevision);
                    // MECO Info
                    List<HashMap<String, Object>> mecoInfoList = getMECOInfoList(mecoList);

                    Dataset[] datasets = dataManager.getAllDatasets((ItemRevision) wpRevision);
                    Dataset dataset = null;
                    ImanFile[] imanFiles = null;
                    if (datasets != null && datasets.length > 0) {
                        for (Dataset temp : datasets) {
                            imanFiles = dataManager.getNamedReferenceFile(temp, TcConstants.TYPE_NR_EXCEL_2007);
                            if (imanFiles != null && imanFiles.length > 0) {
                                dataset = temp;
                            }
                        }
                        if (imanFiles != null && imanFiles.length > 0) {
                            File file = dataManager.getFiles(imanFiles[0]);
                            file = updateFile(mecoId, signoffDate, file, mecoInfoList);

                            String filePath = file.getAbsolutePath();
                            String fileName = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + imanFiles[0].get_original_file_name();
                            dataManager.removeNamedReferenceFromDataset(dataset, imanFiles[0]);
                            dataManager.uploadNamedReferenceFileToDataSet(dataset, filePath, fileName, false);

                            file.delete();
                        }
                    }
                }
            }

            // MECO Release
            // [SR140716-041][20140710] shcho,  MECO 결재시 사용자 승인 후 System에서  용접조건표에 MECO 날짜 업데이트를 수행하지 않는 오류 수정(작업표준서와 용접조건표의 ExcelUpdateService에서 중복적으로 처리하고 있어서 오류 발생. MECOService에서 일원화 함)
            // WorkflowCompleteService completeService = new WorkflowCompleteService();
            // completeService.startServiceForMeco(mecoRevision.getUid());
        } catch (Exception e) {
            e.printStackTrace();
            result = "FAIL";
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            propertyMap.put(TcConstants.MECO_IS_COMPLETED, result);
            try {
                dataManager.setProperty(mecoRevision, propertyMap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public File updateFile(String mecoId, Date signoffDate, File file, List<HashMap<String, Object>> mecoInfoList) throws Exception {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));

        // MECOList Sheet 삭제
        removeMECOListSheet(workbook);

        // MECOInfo Cell 초기화
        initMECOInfoCell(workbook);

        // MECOList Sheet 추가
        addMECOListSheet(workbook, mecoInfoList);

        // MECO List 출력
        setMECOInfo(workbook, mecoInfoList);

        // 작성일자, page number
        setETCInfo(workbook, signoffDate);

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

    /**
     * MECO No
     * 
     * @method getMECONoList
     * @date 2014. 1. 6.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    public List<String> getMECONoList(ModelObject wpRevision) {
        List<String> mecoList = new ArrayList<String>();

        try {
            wpRevision = (ItemRevision) dataManager.loadObjectWithProperties(wpRevision, new String[] { TcConstants.PROP_ITEM_ID });
            String item_id = ((ItemRevision) wpRevision).get_item_id();
            Item item = tcItemUtil.getItem(item_id);
            ModelObject[] revisions = item.get_revision_list();
            for (ModelObject revision : revisions) {
                revision = dataManager.loadObjectWithProperties(revision, new String[] { TcConstants.MECO_NO });
                ItemRevision mecoRevision = (ItemRevision) revision.getPropertyObject(TcConstants.MECO_NO).getModelObjectValue();
                mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision, new String[] { TcConstants.PROP_ITEM_ID });
                mecoList.add(mecoRevision.get_item_id());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mecoList;
    }

    /**
     * MECO Info
     * 
     * @method getMECOInfoList
     * @date 2014. 1. 6.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    public List<HashMap<String, Object>> getMECOInfoList(List<String> mecoList) {
        List<HashMap<String, Object>> mecoInfoList = new ArrayList<HashMap<String, Object>>();

        char lowerAlphabat = 97;
        char upperAlphabat = 65;
        int number = 0;

        try {
            for (String mecoNo : mecoList) {
                HashMap<String, Object> mecoInfoMap = new HashMap<String, Object>();

                ItemRevision mecoRevision = dataManager.getLatestItemRevision(mecoNo);
                mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision, new String[] { TcConstants.PROP_ITEM_ID, TcConstants.PROP_OBJECT_DESC, TcConstants.PROP_OWNING_USER, TcConstants.MECO_TYPE });

                mecoInfoMap.put(TcConstants.PROP_ITEM_ID, mecoRevision.getPropertyObject(TcConstants.PROP_ITEM_ID).getStringValue());
                mecoInfoMap.put(TcConstants.PROP_OBJECT_DESC, mecoRevision.getPropertyObject(TcConstants.PROP_OBJECT_DESC).getStringValue());
                mecoInfoMap.put(TcConstants.PROP_OWNING_USER, mecoRevision.getPropertyObject(TcConstants.PROP_OWNING_USER).getDisplayableValue());

                String meco_type = mecoRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
                if (meco_type.equals("PBI")) {
                    mecoInfoMap.put("changeNo", Integer.toString(number));
                    number++;
                } else if (meco_type.equals("MEW")) {
                    mecoInfoMap.put("changeNo", Character.toString(upperAlphabat));
                    upperAlphabat++;
                } else {
                    mecoInfoMap.put("changeNo", Character.toString(lowerAlphabat));
                    lowerAlphabat++;
                }

                // [SR150522-029] [20150610] ymjang 결재 완료된 용접조건표의 작성일, 결재자 정보 표기 안됨.
                String mecoType = mecoRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
                String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");
                EPMTask rootTask = dataManager.getEPMTask(mecoNo, mecoRevision, prefValues);
                EPMPerformSignoffTask signoffTask = dataManager.getPerformSignoffTask(rootTask, "Team Leader");
                signoffTask = (EPMPerformSignoffTask) dataManager.loadObjectWithProperties(signoffTask, new String[] {TcConstants.PROP_PROCESS_VALID_SIGNOFFS, TcConstants.PROP_LAST_MOD_DATE});
                String approver = getSignoffName(signoffTask);
                Date signoffDate = signoffTask.get_last_mod_date().getTime();

                if (approver == null) {
                    throw new Exception("MECO " + mecoNo + " 의 결재 프로세스에 Team Leader 결재 정보가 존재하지 않습니다.");
                }

                mecoInfoMap.put("approver", approver);
                mecoInfoMap.put("signoffDate", signoffDate);
                
                //mecoInfoMap = getSignoffInfo(mecoRevision, mecoInfoMap);
                
                mecoInfoList.add(mecoInfoMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mecoInfoList;
    }

    /**
     * MECO List 출력
     * 
     * @method setMECOInfo
     * @date 2014. 1. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setMECOInfo(Workbook workbook, List<HashMap<String, Object>> mecoInfoList) {
        Sheet sheet = workbook.getSheet(DEFAULT_BASE_SHEET);

        int mecoInfoSize = mecoInfoList.size();
        for (int i = 0; i < mecoInfoSize; i++) {
            HashMap<String, Object> mecoInfoMap = mecoInfoList.get(i);
            if (mecoInfoSize > 10) {
                mecoInfoMap = mecoInfoList.get(mecoInfoSize + i - 10);
                // 초도 MECO
                if (i == 0) {
                    mecoInfoMap = mecoInfoList.get(0);
                }
            }
            int additionalRowIndex = 0;
            int additionalCellIndex = 0;

            if (i > 4) {
                additionalRowIndex = 5;
                additionalCellIndex = 6;
            }

            Row row = sheet.getRow(DEFAULT_MECO_START_ROW_INDEX - i + additionalRowIndex);

            // 변경
            Cell cell = row.getCell(DEFAULT_MECO_CELL[0 + additionalCellIndex]);
            cell.setCellValue((String) mecoInfoMap.get("changeNo"));

            // 일자
            cell = row.getCell(DEFAULT_MECO_CELL[1 + additionalCellIndex]);
            cell.setCellValue(getFormatDate((Date) mecoInfoMap.get("signoffDate"), "yyyy-MM-dd"));

            // MECO ID
            cell = row.getCell(DEFAULT_MECO_CELL[2 + additionalCellIndex]);
            cell.setCellValue((String) mecoInfoMap.get(TcConstants.PROP_ITEM_ID));

            // 변경내용
            cell = row.getCell(DEFAULT_MECO_CELL[3 + additionalCellIndex]);
            cell.setCellValue((String) mecoInfoMap.get(TcConstants.PROP_OBJECT_DESC));

            // 담당
            cell = row.getCell(DEFAULT_MECO_CELL[4 + additionalCellIndex]);
            cell.setCellValue(getNameProperty((String) mecoInfoMap.get(TcConstants.PROP_OWNING_USER)));

            // 팀장
            cell = row.getCell(DEFAULT_MECO_CELL[5 + additionalCellIndex]);
            cell.setCellValue((String) mecoInfoMap.get("approver"));

            // MECO 10개만 출력
            if (i == 9) {
                break;
            }
        }
    }

    /**
     * MECOList Sheet 추가
     * 
     * @method addMECOListSheet
     * @date 2014. 1. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void addMECOListSheet(Workbook workbook, List<HashMap<String, Object>> mecoInfoList) {
        // MECO 가 10개가 넘어갈경우 MECOList Sheet 추가하고 List를 출력한다.
        if (mecoInfoList.size() > 10) {
            int mecoSheetIndex = workbook.getSheetIndex("MECO_List");
            Sheet mecoSheet = workbook.cloneSheet(mecoSheetIndex);
            workbook.setSheetName(workbook.getSheetIndex(mecoSheet), "MECOList");

            for (int i = 0; i < mecoInfoList.size(); i++) {
                HashMap<String, Object> mecoInfoMap = mecoInfoList.get(i);

                Row row = mecoSheet.createRow(2 + i);

                // 변경
                Cell cell = row.createCell(0);
                cell.setCellValue((String) mecoInfoMap.get("changeNo"));

                // 일자
                cell = row.createCell(1);
                cell.setCellValue(getFormatDate((Date) mecoInfoMap.get("signoffDate"), "yyyy-MM-dd"));

                // MECO ID
                cell = row.createCell(2);
                cell.setCellValue((String) mecoInfoMap.get(TcConstants.PROP_ITEM_ID));

                // 변경내용
                cell = row.createCell(3);
                cell.setCellValue((String) mecoInfoMap.get(TcConstants.PROP_OBJECT_DESC));

                // 담당
                cell = row.createCell(4);
                cell.setCellValue(getNameProperty((String) mecoInfoMap.get(TcConstants.PROP_OWNING_USER)));

                // 팀장
                cell = row.createCell(5);
                cell.setCellValue((String) mecoInfoMap.get("approver"));
            }
        }
    }

    /**
     * MECOList Sheet 삭제
     * 
     * @method removeMECOListSheet
     * @date 2014. 1. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void removeMECOListSheet(Workbook workbook) {
        int sheetIndex = workbook.getSheetIndex("MECOList");
        if (sheetIndex != -1) {
            workbook.removeSheetAt(sheetIndex);
        }
    }

    /**
     * MECOInfo Cell 초기화
     * 
     * @method initMECOInfoCell
     * @date 2014. 1. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void initMECOInfoCell(Workbook workbook) {
        for (int i = 0; i < DEFAULT_MECO_LIST_ROW_COUNT; i++) {
            Sheet sheet = workbook.getSheet(DEFAULT_BASE_SHEET);
            Row row = sheet.getRow(DEFAULT_MECO_START_ROW_INDEX - i);

            for (int j = 0; j < DEFAULT_MECO_CELL.length; j++) {
                Cell cell = row.getCell(DEFAULT_MECO_CELL[j]);
                cell.setCellValue("");
            }
        }
    }

    /**
     * 작성일자, page number [SR140611-027][20140611] byKim ① 용접조건표 을지 추가
     * 
     * @method setETCInfo
     * @date 2014. 1. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setETCInfo(Workbook workbook, Date signoffDate) {
        int numberOfSheets = workbook.getNumberOfSheets();
        int exclusionNumberOfSheets = 3;
        if (workbook.getSheetIndex("MECOList") != -1) {
            exclusionNumberOfSheets++;
        }

        String totalPageNo = Integer.toString(numberOfSheets - exclusionNumberOfSheets);
        int currentPageNo = 0;
        for (int j = 0; j < numberOfSheets; j++) {
            Sheet sheet = workbook.getSheetAt(j);
            String sheetName = sheet.getSheetName();

            if (!sheetName.equals("MECOList") && !sheetName.equals("MECO_List") && !sheetName.equals("copySheet") && !sheetName.equals("systemSheet")) {
                // 작성일자
                sheet.getRow(1).getCell(37).setCellValue(getFormatDate(signoffDate, "yyyy-MM-dd"));

                // page number
                currentPageNo++;
                if (sheetName.startsWith("UserSheet")) {
                    sheet.getRow(68).getCell(0).setCellValue(currentPageNo + " / " + totalPageNo);
                } else {
                    sheet.getRow(89).getCell(0).setCellValue(currentPageNo + " / " + totalPageNo);
                }
            }
        }
    }

    /**
     * mecoRevision의 결재 정보
     * 
     * @method getSignoffInfo
     * @date 2014. 1. 13.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    public HashMap<String, Object> getSignoffInfo(ItemRevision mecoRevision, HashMap<String, Object> mecoInfoMap) throws Exception {
        String mecoType = mecoRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
        String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");
        String approver = null;
        Date signoffDate = null;

        ModelObject[] processes = dataManager.getProcess(mecoRevision);
        for (ModelObject process : processes) {
            process = dataManager.loadObjectWithProperties(process, new String[] { TcConstants.PROP_OBJECT_NAME });
            String processName = ((EPMTask) process).get_object_name();
            for (String prefValue : prefValues) {
                if (prefValue.equals(processName)) {
                    EPMTask rootTask = (EPMTask) process;
                    EPMPerformSignoffTask signoffTask = dataManager.getPerformSignoffTask(rootTask, "Team Leader");
                    signoffTask = (EPMPerformSignoffTask) dataManager.loadObjectWithProperties(signoffTask, new String[] { TcConstants.PROP_PROCESS_VALID_SIGNOFFS, TcConstants.PROP_LAST_MOD_DATE });
                    approver = getSignoffName(signoffTask);
                    signoffDate = signoffTask.get_last_mod_date().getTime();
                    break;
                }
            }
        }

        mecoInfoMap.put("approver", approver);
        mecoInfoMap.put("signoffDate", signoffDate);

        return mecoInfoMap;
    }

    /**
     * 
     * 
     * @method getSignoffName
     * @date 2014. 6. 23.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getSignoffName(EPMPerformSignoffTask performSignoffTask) throws Exception {
        String approver = null;

        performSignoffTask = (EPMPerformSignoffTask) dataManager.loadObjectWithProperties(performSignoffTask, new String[] { TcConstants.PROP_PROCESS_VALID_SIGNOFFS });
        ModelObject[] signoffs = performSignoffTask.get_valid_signoffs();
        if (signoffs != null && signoffs.length > 0) {
            Signoff signoff = (Signoff) signoffs[0];
            GroupMember member = signoff.get_group_member();
            member = (GroupMember) dataManager.loadObjectWithProperties(member, new String[] { TcConstants.PROP_USER, TcConstants.PROP_USER_NAME });
            approver = member.get_user().get_user_name();
        }

        return approver;
    }

    /**
     * owning_user 값에서 이름만 가져오기
     * 
     * @method getNameProperty
     * @date 2014. 1. 6.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getNameProperty(String owning_user) {
        String name = owning_user.split(" ")[0];

        return name;
    }

    /**
     * Date -> String
     * 
     * @method getFormatDate
     * @date 2014. 1. 7.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getFormatDate(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        String strDate = simpleDateFormat.format(date);

        return strDate;
    }
}
