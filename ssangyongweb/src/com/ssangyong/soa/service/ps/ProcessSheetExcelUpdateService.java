package com.ssangyong.soa.service.ps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.service.sdv.SDVTCDataManager;
import com.ssangyong.soa.util.TcConstants;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Signoff;

public class ProcessSheetExcelUpdateService {

    private Session tcSession;
    private SDVTCDataManager dataManager;

    public ProcessSheetExcelUpdateService(Session session) {
        this.tcSession = session;
        this.dataManager = new SDVTCDataManager(tcSession);
    }

    public void updateMECOReleaseInfo(String mecoId) {
        try {
            ItemRevision mecoRevision = dataManager.getLatestItemRevision(mecoId);
            if(mecoRevision != null) {
                mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision, new String[] {TcConstants.MECO_TYPE, TcConstants.PROCESS_SHEET_KO_RELATION});
                String mecoType = mecoRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
//                String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");
                //TODOS 여기서 바꾸니까 테스트 해 볼것.
                String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");

                String approver = null;
                ModelObject[] processes = dataManager.getProcess(mecoRevision);
                if(processes != null) {
                    for(ModelObject process : processes) {
                        process = dataManager.loadObjectWithProperties(process, new String[] {TcConstants.PROP_OBJECT_NAME});
                        String processName = ((EPMTask) process).get_object_name();
                        for(String prefValue : prefValues) {
                            if(prefValue.equals(processName)) {
                                EPMTask rootTask = (EPMTask) process;
                                approver = getSignoffName(rootTask, "Team Leader");
                            }
                            break;
                        }
                    }
                }

                if(approver != null) {
                    ModelObject[] docRevisions = mecoRevision.getPropertyObject(TcConstants.PROCESS_SHEET_KO_RELATION).getModelObjectArrayValue();
                    if(docRevisions != null) {
                        for(ModelObject docRevision : docRevisions) {
                            Dataset[] datasets = dataManager.getAllDatasets((ItemRevision) docRevision);
                            if(datasets != null && datasets.length > 0) {
                                ImanFile[] imanFiles = dataManager.getNamedReferenceFile(datasets[0], TcConstants.TYPE_NR_EXCEL_2007);
                                if(imanFiles != null && imanFiles.length > 0) {
                                    File file = dataManager.getFiles(imanFiles[0]);
                                    file = updateFile(mecoId, approver, file);

                                    String filePath = file.getAbsolutePath();
                                    String fileName = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + imanFiles[0].get_original_file_name();
                                    dataManager.removeNamedReferenceFromDataset(datasets[0], imanFiles[0]);
                                    dataManager.uploadNamedReferenceFileToDataSet(datasets[0], filePath, fileName, false);
                                }
                            }
                        }
                        //TODO : MECO 상태 속성 업데이트

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSignoffName(EPMTask rootTask, String taskName) throws Exception {
        String approver = null;

        ModelObject[] signoffs = dataManager.getTaskSignoffs(rootTask, taskName);
        if(signoffs != null && signoffs.length > 0) {
            Signoff signoff = (Signoff) signoffs[0];
            GroupMember member = signoff.get_group_member();
            member = (GroupMember) dataManager.loadObjectWithProperties(member, new String[] {TcConstants.PROP_USER});
            approver = member.get_user().get_user_name();
        }

        return approver;
    }

    public File updateFile(String mecoId, String approver, File file) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String strToday = df.format(new Date());

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
                        row.getCell(24).setCellValue(strToday);
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
                            row.getCell(5).setCellValue(strToday);
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

}
