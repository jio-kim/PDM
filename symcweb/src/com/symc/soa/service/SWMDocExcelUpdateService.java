/**
 *
 */
package com.symc.soa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcSessionUtil;
import com.symc.common.soa.util.TcConstants;
import com.symc.work.service.WorkflowCompleteService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Signoff;

public class SWMDocExcelUpdateService {

    private Session tcSession;
    private SDVTCDataManager dataManager;
    private TcItemUtil tcItemUtil;
    private TcSessionUtil sessionUtil;

    public SWMDocExcelUpdateService(Session session) {
        this.tcSession = session;
        this.dataManager = new SDVTCDataManager(tcSession);
        sessionUtil = new TcSessionUtil(session);
        tcItemUtil = new TcItemUtil(session);
        try {
            sessionUtil.setByPass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSWMDOCReleaseInvoker(List<HashMap<String, String>> swmItemRevList) {
        HashMap<String, String> targetUidMap = new HashMap<String, String>();

        for (HashMap<String, String> swmItemRev : swmItemRevList) {
            String processUid = updateSWMDOCReleaseInfo(swmItemRev.get("MECO_NO"));
            if (!targetUidMap.containsKey(processUid)) {
                targetUidMap.put(processUid, swmItemRev.get("MECO_UID"));
            }
        }
        try {
            WorkflowCompleteService completeService = new WorkflowCompleteService();
            for (String processUid : targetUidMap.keySet()) {
                completeService.startServiceForSWMD(targetUidMap.get(processUid));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String updateSWMDOCReleaseInfo(String swmDOCId) {
        String processUid = null;
        try {
            ItemRevision swmDOCRevision = dataManager.getLatestItemRevision(swmDOCId);
            HashMap<String, Object> updateMap = null;
            if (swmDOCRevision != null) {
                String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, "SWM" + ".Workflow.Template");

                String approver = null;
                ModelObject[] processes = dataManager.getProcess(swmDOCRevision);
                if (processes != null) {
                    for (ModelObject process : processes) {
                        process = dataManager.loadObjectWithProperties(process, new String[] { TcConstants.PROP_OBJECT_NAME });
                        String processName = ((EPMTask) process).get_object_name();
                        for (String prefValue : prefValues) {
                            if (prefValue.equals(processName)) {
                                processUid = process.getUid();
                                EPMTask rootTask = (EPMTask) process;
                                approver = getSignoffName(rootTask, "Team Leader");
                            }
                            break;
                        }
                    }
                }

                if (approver != null) {
                    Dataset[] datasets = dataManager.getAllDatasets((ItemRevision) swmDOCRevision);
                    if (datasets != null && datasets.length > 0) {
                        ImanFile[] imanFiles = dataManager.getNamedReferenceFile(datasets[0], TcConstants.TYPE_NR_EXCEL_2007);
                        if (imanFiles != null && imanFiles.length > 0) {
                            File file = dataManager.getFiles(imanFiles[0]);
                            file = updateFile(swmDOCId, approver, file);

                            String filePath = file.getAbsolutePath();
                            String fileName = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + imanFiles[0].get_original_file_name();
                            dataManager.removeNamedReferenceFromDataset(datasets[0], imanFiles[0]);
                            if (dataManager.uploadNamedReferenceFileToDataSet(datasets[0], filePath, fileName, false) != null) {
                                updateMap = new HashMap<String, Object>();
                                updateMap.put("s7_MATURITY", "END");
                                tcItemUtil.setAttributes((ModelObject) swmDOCRevision, updateMap);
                            } else {
                                updateMap = new HashMap<String, Object>();
                                updateMap.put("s7_MATURITY", "FAIL");
                                tcItemUtil.setAttributes((ModelObject) swmDOCRevision, updateMap);
                                throw new Exception("Fail to update release info.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processUid;
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

    public File updateFile(String swmDOCId, String approver, File file) throws Exception {

        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(3);
        row.getCell(30).setCellValue(approver);

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

}
