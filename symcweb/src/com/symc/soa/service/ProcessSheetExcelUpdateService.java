package com.symc.soa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.util.TcConstants;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedInfo;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMPerformSignoffTask;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Signoff;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

/**
 * [SR140716-041][20140710] shcho,  MECO 결재시 사용자 승인 후 System에서  용접조건표에 MECO 날짜 업데이트를 수행하지 않는 오류 수정(작업표준서와 용접조건표의 ExcelUpdateService에서 중복적으로 처리하고 있어서 오류 발생. MECOService에서 일원화 함)
 * [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰 
 * [NON-SR][20150225] ymjang, selectList --> update 로 변경
 */
public class ProcessSheetExcelUpdateService {

    private Session tcSession;
    private SDVTCDataManager dataManager;

    public ProcessSheetExcelUpdateService(Session session) {
        this.tcSession = session;
        this.dataManager = new SDVTCDataManager(tcSession);
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
            if(mecoRevision == null) {
                throw new Exception(mecoId + "가 존재하지 않습니다.");
            }
            mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision, new String[] {TcConstants.MECO_TYPE, TcConstants.PROCESS_SHEET_KO_RELATION});
            String mecoType = mecoRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
            String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, mecoType + ".Workflow.Template");

            String approver = null;
            Date signoffDate = null;
            ModelObject[] processes = dataManager.getProcess(mecoRevision);
            if(processes == null || processes.length == 0) {
                throw new Exception("MECO " + mecoId + "에 진행중인 결재 프로세스가 존재하지 않습니다.");
            }
            for(ModelObject process : processes) {
                process = dataManager.loadObjectWithProperties(process, new String[] {TcConstants.PROP_OBJECT_NAME});
                String processName = ((EPMTask) process).get_object_name();
                for(String prefValue : prefValues) {
                    if(prefValue.equals(processName)) {
                        EPMTask rootTask = (EPMTask) process;
                        EPMPerformSignoffTask signoffTask = dataManager.getPerformSignoffTask(rootTask, "Team Leader");
                        signoffTask = (EPMPerformSignoffTask) dataManager.loadObjectWithProperties(signoffTask, new String[] {TcConstants.PROP_PROCESS_VALID_SIGNOFFS, TcConstants.PROP_LAST_MOD_DATE});
                        approver = getSignoffName(signoffTask);
                        signoffDate = signoffTask.get_last_mod_date().getTime();
                        break;
                    }
                }
            }

            if(approver == null) {
                throw new Exception("MECO " + mecoId + "의 결재 프로세스에 Team Leader 결재 정보가 존재하지 않습니다.");
            }

            ModelObject[] docRevisions = mecoRevision.getPropertyObject(TcConstants.PROCESS_SHEET_KO_RELATION).getModelObjectArrayValue();
            if(docRevisions == null) {
                throw new Exception("MECO " + mecoId + "에 첨부된 작업표준서가 없습니다.");
            }

            for(ModelObject docRevision : docRevisions) {
                Dataset[] datasets = dataManager.getAllDatasets((ItemRevision) docRevision);
                if(datasets != null && datasets.length > 0) {
                    ImanFile[] imanFiles = dataManager.getNamedReferenceFile(datasets[0], TcConstants.TYPE_NR_EXCEL_2007);
                    if(imanFiles != null && imanFiles.length > 0) {
                        File file = dataManager.getFiles(imanFiles[0]);
                        file = updateFile(mecoId, approver, signoffDate, file);

                        String filePath = file.getAbsolutePath();
                        String fileName = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + imanFiles[0].get_original_file_name();
                        dataManager.removeNamedReferenceFromDataset(datasets[0], imanFiles[0]);
                        dataManager.uploadNamedReferenceFileToDataSet(datasets[0], filePath, fileName, false);
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

    /**
     * [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰 
     * 영문 작업 표준서 결재자 정보 입력
     * [SR150105-027][20150209]shcho, 작업표준서 변경란의 변경일자 Logic을 팀장 결재일자에서  Released Date를 표시하는 것으로 변경
     * 
     * @method updatePSReleaseInfo 
     * @date 2015. 1. 19.
     * @param
     * @return void
     * @throws Exception 
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    public void updatePSReleaseInfo(String psItemId, String ps_item_puid) {
        String result = "END";
        ItemRevision psRevision = null;
        HashMap<String, String> parmaMap = null;
        boolean isFoundProcess = false;
        try {
            psRevision = dataManager.getReleasedLatestItemRevision(psItemId);
            if(psRevision == null) {
                throw new Exception(psItemId + "가 존재하지 않습니다.");
            }
           
            //String mecoType = psRevision.getPropertyObject(TcConstants.MECO_TYPE).getStringValue();
            String[] prefValues = dataManager.getPreferenceStringArrayValue(TcConstants.TC_PREF_SCOPE_SITE, "M7_PublishProcessSheet_WFTemplateName_EN");

            String approver = null;
            Date signoffDate = null;
            //[SR150105-027][20150209]shcho, 작업표준서 변경란의 변경일자 Logic을 팀장 결재일자에서  Released Date를 표시하는 것으로 변경
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            
            WhereReferencedInfo[]  whereReferencedInfos = dataManager.getWhereReferenced(new WorkspaceObject[] {psRevision}, 1);
            for( int i = 0; i < whereReferencedInfos.length; i++){
                
                if( whereReferencedInfos[i].referencer instanceof EPMTask)
                {
                    isFoundProcess = true;
                    EPMTask process = (EPMTask)whereReferencedInfos[i].referencer;
                    
                    process = (EPMTask) dataManager.loadObjectWithProperties(process, new String[] {TcConstants.PROP_OBJECT_NAME});
                    String processName = process.get_object_name();
                    
                    for(String prefValue : prefValues) {
                        if(prefValue.equals(processName)) {
                            EPMTask rootTask = (EPMTask) process;
                            EPMPerformSignoffTask signoffTask = dataManager.getPerformSignoffTask(rootTask, "Team Leader");
                            signoffTask = (EPMPerformSignoffTask) dataManager.loadObjectWithProperties(signoffTask, new String[] {TcConstants.PROP_PROCESS_VALID_SIGNOFFS, TcConstants.PROP_LAST_MOD_DATE});
                            approver = getSignoffOSName(signoffTask);
                            signoffDate = signoffTask.get_last_mod_date().getTime();
                            break;
                        }
                    }
                }
                
            }
            
            if(!isFoundProcess) {
                throw new Exception("Process Sheet " + psItemId + "에 진행중인 결재 프로세스가 존재하지 않습니다.");
            }
            
            if(approver == null) {
                throw new Exception("PS " + psItemId + "의 결재 프로세스에 Team Leader 결재 정보가 존재하지 않습니다.");
            }

            Dataset[] datasets = dataManager.getAllDatasets((ItemRevision) psRevision);
            if(datasets != null && datasets.length > 0) {
                ImanFile[] imanFiles = dataManager.getNamedReferenceFile(datasets[0], TcConstants.TYPE_NR_EXCEL_2007);
                if(imanFiles != null && imanFiles.length > 0) {
                    File file = dataManager.getFiles(imanFiles[0]);
                    file = updateFile(psItemId, approver, date, file);

                    String filePath = file.getAbsolutePath();
                    String fileName = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + imanFiles[0].get_original_file_name();
                    dataManager.removeNamedReferenceFromDataset(datasets[0], imanFiles[0]);
                    dataManager.uploadNamedReferenceFileToDataSet(datasets[0], filePath, fileName, false);
                }
            }
            
            result = "Completed";
    		parmaMap = new HashMap<String, String>();
    		parmaMap.put("ps7_maturity", result);   // Maturity 'Completed'
    		parmaMap.put("rev_puid", ps_item_puid); // 영문작업표순서 아이템 리비전 PUID
    		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
    		//[NON-SR][20150225] ymjang, selectList --> update 로 변경
    		commonDao.update("com.symc.ps.updateRevisionMauturity", parmaMap);    		
    		//commonDao.selectList("com.symc.ps.updateRevisionMauturity", parmaMap); 
            
    		/*
            result = "Completed";
			psRevision = (ItemRevision) dataManager.loadObjectWithProperties(psRevision, new String[] {TcConstants.MECO_MATURITY});
            
			HashMap<String, Object> propertyMap = new HashMap<String, Object>();
			propertyMap.put(TcConstants.MECO_MATURITY, result);
            
            dataManager.setProperty(psRevision, propertyMap);
            
            dataManager.save(tcSession.getConnection(), psRevision);
            dataManager.refresh(tcSession.getConnection(), psRevision, 0);
            */
    		
        } catch (Exception e) {
            e.printStackTrace();
            try {
                
            	result = "FAIL";
        		parmaMap = new HashMap<String, String>();
        		parmaMap.put("ps7_maturity", result);   // Maturity 'Completed'
        		parmaMap.put("rev_puid", ps_item_puid);      // 영문작업표순서 아이템 리비전 PUID
        		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
        		//[NON-SR][20150225] ymjang, selectList --> update 로 변경
        		commonDao.update("com.symc.ps.updateRevisionMauturity", parmaMap);    		
        		//commonDao.selectList("com.symc.ps.updateRevisionMauturity", parmaMap); 
                
            	/*
                psRevision = (ItemRevision) dataManager.loadObjectWithProperties(psRevision, new String[] {TcConstants.MECO_MATURITY});
                
                result = "FAIL";
                propertyMap = new HashMap<String, Object>();
                propertyMap.put(TcConstants.MECO_MATURITY, result);
                dataManager.setProperty(psRevision, propertyMap);
                
                dataManager.save(tcSession.getConnection(), psRevision);
                dataManager.refresh(tcSession.getConnection(), psRevision, 0);
                */
            	
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public String getSignoffName(EPMPerformSignoffTask performSignoffTask) throws Exception {
        String approver = null;

        ModelObject[] signoffs = performSignoffTask.get_valid_signoffs();
        if(signoffs != null && signoffs.length > 0) {
            Signoff signoff = (Signoff) signoffs[0];
            GroupMember member = signoff.get_group_member();
            member = (GroupMember) dataManager.loadObjectWithProperties(member, new String[] {TcConstants.PROP_USER});
            approver = member.get_user().get_user_name();
        }

        return approver;
    }
    
    public String getSignoffOSName(EPMPerformSignoffTask performSignoffTask) throws Exception {
         
        String userName = "";
        ModelObject[] signoffs = performSignoffTask.get_valid_signoffs();
        if(signoffs != null && signoffs.length > 0) {
            Signoff signoff = (Signoff) signoffs[0];
            GroupMember member = signoff.get_group_member();
            member = (GroupMember) dataManager.loadObjectWithProperties(member, new String[] {TcConstants.PROP_USER});
            User approver = (User) member.get_user();
            approver = (User) dataManager.loadObjectWithProperties(approver, new String[] {TcConstants.PROP_OS_USER}); 
            
            String[] names = approver.get_os_username().split(" ");
            if(names != null && names.length > 0) {
                for(int i = 0; i < names.length; i++) {
                    userName += names[i].substring(0, 1);
                }
            }
        }
        
        return userName;
    }

    public File updateFile(String mecoId, String approver, Date date, File file) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String strToday = df.format(date);

        try {
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
                //[CF-4178] 영문 작업표준서 결재란(승인자) 표기 누락 관련 보완 요청
                // > 요청 내용...
                // 현재 영문 작업표준서를 생성 시 다수의 갑지 또는 을지가 포함되어 있는 경우
                // 맨 처음 갑지에만 결재자의 영문 이니셜이 표기되고 있음
                // ☞ 국문과 동일하게 결재자가 결재를 완료하면 모든 작업표준서에 결재자 이니셜이 표기되도록 프로그램 수정을 요청합니다
                // 아래 A1 sheet에만 값 입력 후 빠져나가는 부분을 막음.
                // 승인자 입력은 아래쪽에서 입력하도록 되어 있음.
                // 이 부분은 MECO 릴리즈시에도 호출이 되기 때문에 '갑'으로 시작하는 시트와 MECO 시트에 대해서 입력 부분이 있다.
                // 영문 작업 표준서는 좌측 하단의 결재자는 별도로 입력하지 않음.
//                }
//                else if(sheetName.startsWith("A1")) {
//                    // [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰 
//                    // 영문 작업 표준서 승인란에 결재자 정보 입력
//                    Row row = sheet.getRow(0);
//                    String APPR = row.getCell(99).getStringCellValue();
//                    if ("APPR".equals(APPR))
//                    {
//                        row = sheet.getRow(1);
//                        row.getCell(99).setCellValue(approver);
//                        break;
//                    }
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

                if(!workbook.isSheetHidden(i)) {
                    Row row = sheet.getRow(1);
                    row.getCell(99).setCellValue(approver);
                }
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.flush();
            fos.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return file;
    }

}
