package com.kgm.soa.service.wp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kgm.soa.biz.Session;
import com.kgm.soa.biz.TcItemUtil;
import com.kgm.soa.service.sdv.SDVTCDataManager;
import com.kgm.soa.util.TcConstants;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Signoff;

public class WeldOPExcelUpdateService {

    private Session tcSession;
    private SDVTCDataManager dataManager;

    public WeldOPExcelUpdateService(Session session) {
        this.tcSession = session;
        this.dataManager = new SDVTCDataManager(tcSession);
    }

    public void updateMECOReleaseInfo(String mecoId) {
        try {
            ItemRevision mecoRevision = dataManager.getLatestItemRevision(mecoId);
            if (mecoRevision != null) {
                mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision, new String[] { TcConstants.MECO_TYPE, TcConstants.WELD_CONDITIOIN_SHEET_RELATION, "CMHasSolutionItem", "item_id", "item_revision_id" });
                String mecoType = mecoRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
//                String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");
                //TODOS 여기서 바꾸니까 테스트 해 볼것.
                String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");

                String approver = null;
                ModelObject[] processes = dataManager.getProcess(mecoRevision);
                if (processes != null) {
                    for (ModelObject process : processes) {
                        process = dataManager.loadObjectWithProperties(process, new String[] { TcConstants.PROP_OBJECT_NAME });
                        String processName = ((EPMTask) process).get_object_name();
                        for (String prefValue : prefValues) {
                            if (prefValue.equals(processName)) {
                                EPMTask rootTask = (EPMTask) process;
                                approver = getSignoffName(rootTask, "Team Leader");
                                break;
                            }
                        }
                    }
                }

                if (approver != null) {
                    ModelObject[] wpRevisions = mecoRevision.getPropertyObject("CMHasSolutionItem").getModelObjectArrayValue();
                    if (wpRevisions != null) {
                        for (ModelObject wpRevision : wpRevisions) {
                            String type = wpRevision.getTypeObject().getName();
                            if (type.equals("M7_BOPWeldOPRevision")) {
                                // MECO NO
                                List<String> mecoList = getMECONOList(wpRevision);
                                // MECO Info
                                List<HashMap<String, Object>> mecoInfoList = getMECOInfoList(mecoList);

                                Dataset[] datasets = dataManager.getAllDatasets((ItemRevision) wpRevision);
                                if (datasets != null && datasets.length > 0) {
                                    ImanFile[] imanFiles = dataManager.getNamedReferenceFile(datasets[0], TcConstants.TYPE_NR_EXCEL_2007);
                                    if (imanFiles != null && imanFiles.length > 0) {
                                        File file = dataManager.getFiles(imanFiles[0]);
                                        file = updateFile(mecoId, approver, file, mecoInfoList);

                                        String filePath = file.getAbsolutePath();
                                        String fileName = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + imanFiles[0].get_original_file_name();
                                        dataManager.removeNamedReferenceFromDataset(datasets[0], imanFiles[0]);
                                        dataManager.uploadNamedReferenceFileToDataSet(datasets[0], filePath, fileName, false);
                                    }
                                }
                            }
                        }

                        // TODO : MECO 상태 속성 업데이트
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File updateFile(String mecoId, String approver, File file, List<HashMap<String, Object>> mecoInfoList) throws Exception {
        final String DEFAULT_BASE_SHEET = "1";
        final int DEFAULT_MECO_LIST_ROW_COUNT = 5;
        final int DEFAULT_MECO_START_ROW_INDEX = 83;
        final int[] DEFAULT_MECO_CELL = { 0, 2, 5, 9, 17, 19, 21, 23, 26, 30, 38, 40 };

        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        int sheetCnt = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetCnt; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            if (sheetName.equals("MECOList")) {
                workbook.removeSheetAt(i);
            }
        }

        //
        for (int i = 0; i < DEFAULT_MECO_LIST_ROW_COUNT; i++) {
            Sheet sheet = workbook.getSheet(DEFAULT_BASE_SHEET);
            Row row = sheet.getRow(DEFAULT_MECO_START_ROW_INDEX + i);

            for (int j = 0; j < DEFAULT_MECO_CELL.length; j++) {
                Cell cell = row.getCell(DEFAULT_MECO_CELL[j]);
                cell.setCellValue("");
            }
        }

        // MECO 가 10개가 넘어갈경우 MECOList Sheet 추가 하고 List 를 출력한다
        /*if (mecoInfoList.size() > 10)
        {
            int mecoSheetIndex = workbook.getSheetIndex("MECO_List");
            int sheetTotalCount = workbook.getNumberOfSheets();
            Sheet mecoSheet = workbook.cloneSheet(mecoSheetIndex);
            workbook.setSheetName((sheetTotalCount - 1), "MECOList");
            for (int i = 0; i < mecoInfoList.size(); i++)
            {
                PreviewWeldConditionSheetExcelTransformer.mecoListPrintRow(mecoSheet, mecoListRow, mecoDataList.get(mecoListRow));
            }
            PreviewWeldConditionSheetExcelTransformer.setBorderAndAlign(workbook, mecoSheet);
        }*/

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

    public List<String> getMECONOList(ModelObject wpRevision) {
        List<String> mecoList = new ArrayList<String>();

        try {
            wpRevision = (ItemRevision) dataManager.loadObjectWithProperties(wpRevision, new String[] { "item_id" });
            String item_id = ((ItemRevision) wpRevision).get_item_id();
            Item item = new TcItemUtil(tcSession).getItem(item_id);
            ModelObject[] revisions = item.get_revision_list();
            for (ModelObject revision : revisions) {
                revision = dataManager.loadObjectWithProperties(wpRevision, new String[] { "m7_MECO_NO" });
                mecoList.add(revision.getPropertyObject("m7_MECO_NO").getStringValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mecoList;
    }

    public List<HashMap<String, Object>> getMECOInfoList(List<String> mecoList) {
        List<HashMap<String, Object>> mecoInfoList = new ArrayList<HashMap<String, Object>>();

        try {
            for (String mecoNo : mecoList) {
                HashMap<String, Object> mecoInfoMap = new HashMap<String, Object>();

                Item item = new TcItemUtil(tcSession).getItem(mecoNo);
                ModelObject[] revisions = item.get_revision_list();

                mecoInfoMap.put("item_id", ((ItemRevision) revisions[0]).getPropertyObject("item_id").getStringValue());
                mecoInfoMap.put("object_desc", ((ItemRevision) revisions[0]).getPropertyObject("object_desc").getStringValue());
                mecoInfoMap.put("owning_user", ((ItemRevision) revisions[0]).getPropertyObject("owning_user").getDisplayableValue());
                mecoInfoList.add(mecoInfoMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mecoInfoList;
    }

    public String getSignoffName(EPMTask rootTask, String taskName) throws Exception {
        String approver = null;

        ModelObject[] signoffs = dataManager.getTaskSignoffs(rootTask, taskName);
        if (signoffs != null && signoffs.length > 0) {
            Signoff signoff = (Signoff) signoffs[0];
            GroupMember member = signoff.get_group_member();
            member = (GroupMember) dataManager.loadObjectWithProperties(member, new String[] { TcConstants.PROP_USER });
            approver = member.get_user().get_user_name();
        }

        return approver;
    }

}
