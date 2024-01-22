package com.symc.plm.me.sdv.operation.ps;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleEvent;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleListener;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookProtection;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomActivityDao;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.view.excel.DatasetCloseWaiter;
import com.symc.plm.me.sdv.view.excel.DatasetOpenForEditManager;
import com.symc.plm.me.sdv.view.excel.ExcelView;
import com.symc.plm.me.sdv.view.ps.PreviewView;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [P0074] [20150127] ymjang, Edit Activity 로 excel 띄운 상태에서 수정후 저장해도 수정 사항 반영 안됨      
 * [NO-SR][20150214] shcho, current revision이 old revision보다 activity수가 적을 경우, NullPointerException 발생 하는 버그 수정
 * [NO-SR][20150323] shcho, Edit Activity 작업 후 엑셀파일 종료 시 Edit Activity 버튼이 비활성인 상태로 남아있는 오류 수정
 * 
 */
public class ProcessSheetEditActivityOperation extends AbstractSDVActionOperation {

    private Registry registry;

    private boolean saveFlag = true;
    private String filePath;
    private OleControlSite appControlSite;
    
    public ProcessSheetEditActivityOperation(String actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
        registry = Registry.getRegistry(this);
    }

    public ProcessSheetEditActivityOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void executeOperation() throws Exception {
        IDataSet dataset = getDataSet();
        if(dataset != null) {
            Collection<IDataMap> dataMaps = dataset.getAllDataMaps();
            if(dataMaps != null) {
                for(IDataMap dataMap : dataMaps) {
                    if(dataMap.containsKey("targetOperationList")) {
                        List<HashMap<String, Object>> opList = dataMap.getTableValue("targetOperationList");
                        if(opList != null) {
                            String viewId = null;
                            if(dataMap.containsKey("viewId")) {
                                viewId = dataMap.getStringValue("viewId");
                            }
                            editActivity(viewId, opList);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void editActivity(final String viewId, List<HashMap<String, Object>> opList) {
       final File templateFile = ExcelTemplateHelper.getTemplateFile(ExcelTemplateHelper.EXCEL_OPEN, registry.getString("EditEnglishActivityPreference.Name"), null);
        
        if(templateFile != null) {
            try {
                Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
                Sheet sheet = workbook.getSheetAt(0);
                int operationCnt = opList.size();
                int index = 2;
                for(int i = 0; i < operationCnt; i++) {
                    HashMap<String, Object> dataMap = opList.get(i);
                    String itemId = (String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
                    String revisionId = (String) dataMap.get(SDVPropertyConstant.ITEM_REVISION_ID);

                    TCComponentItemRevision itemRevision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, itemId, revisionId);
                    if(itemRevision == null) {
                        throw new NullPointerException("공법 " + itemId + "/" + revisionId + "가 존재하지 않습니다.");
                    }
                    // [P0074] [20150127] ymjang, Edit Activity 로 excel 띄운 상태에서 수정후 저장해도 수정 사항 반영 안됨
                    itemRevision.refresh();
                    
                    TCComponent root = itemRevision.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
                    if(root != null) {
                        TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                        
                        // [P0074] [20150127] ymjang, Edit Activity 로 excel 띄운 상태에서 수정후 저장해도 수정 사항 반영 안됨
                        rootActivity.refresh();
                        
                        TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                        if(children != null) {
                            TCComponent[] preActivities = getPreviousRevisionActivities(itemRevision);
                            int childCnt = children.length;
                            if(preActivities != null && preActivities.length > childCnt) {
                                childCnt = preActivities.length;
                            }

                            for(int j = 0; j < childCnt; j++) {
                                Row row = sheet.createRow(index++);
                                
                                if(children.length > j && children[j] != null) {
                                    // [P0074] [20150127] ymjang, Edit Activity 로 excel 띄운 상태에서 수정후 저장해도 수정 사항 반영 안됨
                                    children[j].refresh();
//                                if(children[j] != null) {
                                    row.createCell(0).setCellValue(itemId);
                                    row.createCell(1).setCellValue(revisionId);
                                    row.createCell(2).setCellValue((j + 1) * 10);
                                    String activityName = children[j].getProperty(SDVPropertyConstant.ACTIVITY_OBJECT_NAME);
                                    row.createCell(3).setCellValue(activityName);
                                    row.createCell(4).setCellValue((j + 1) * 10);
                                    Cell cell = row.createCell(5);
                                    //cell.getCellStyle().setLocked(false);
                                    String engActivityName = children[j].getProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME);
                                    
                                    if(engActivityName != null && engActivityName.length() > 0) {
                                        cell.setCellValue(engActivityName);
                                    } else {
                                        cell.setCellValue(activityName);
                                    }
                                }

                                if(preActivities != null && preActivities.length > j && preActivities[j] != null) {
//                                if(null != preActivities && null != preActivities[j]) {
                                    row.createCell(6).setCellValue((j + 1) * 10);
                                    row.createCell(7).setCellValue(preActivities[j].getProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME));
                                }
                            }
                        }
                    }
                }

                FileOutputStream fos = new FileOutputStream(templateFile);
                workbook.write(fos);
                fos.flush();
                fos.close();

                
                
                /*
                 * [20170525] bc.k 기존의 OLE 라이브러리가 적용된 소스에 문제가 있어 
                 * OLE를 쓰지 않는 방법으로 로직 변환
                 * ExcelView클래스의 OpenExcel 메서드의 로직을 이용하여 작성
                 * 기능 : Excel 오픈 기능과 Excel 수정 저장후 종료시 WorkSheet에 반영 로직으로 구성
                 */
                final TCComponentDataset tempDataset  = SDVBOPUtilities.createDataset(templateFile.getAbsolutePath(), templateFile.getName());
                final DatasetOpenForEditManager aDatasetOpenForEditManager = new DatasetOpenForEditManager(tempDataset);
                 
                // 공법의 Activity의 내용을 한글로 저장한 Excel Sheet 를 Open 하는 로직
                Display.getDefault().syncExec(new Runnable() {
                	
                	@Override
                    public void run() {
                		DatasetCloseWaiter datasetCloseWaiter = null;
                		ExcelView excelView = ((ExcelView)(AbstractSDVViewPane) UIManager.getCurrentDialog().getView("excelView"));
                		if( excelView != null ) {
                			datasetCloseWaiter = new DatasetCloseWaiter(aDatasetOpenForEditManager, excelView);
                		}
                
		                try {
		        			aDatasetOpenForEditManager.start();
		        			Thread.sleep(500);
		        			if( excelView != null ) {
		        				datasetCloseWaiter.start();
		        			} else {
		        				
		        			}
		        			// Thread가 완료 될때 까지 기다린다.
//		        			aDatasetOpenForEditManager.join();
		        		} catch (InterruptedException e) {
		        			e.printStackTrace();
		        		}
		                
                	   }
                });
                
                
                // Open 된 Excel이 수정되고 종료 된 후 Dataset에 CheckIn 되는 순간 실행되며
                // Excel 내용을 Sheet에 반영하는 로직
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                    	try {
                    		Thread.sleep(1500);
		                    while ( true ) {
//		                    	System.out.println("aaaaaaa=================>");
            				// Dataset이 CheckIn 되는 순간 while문 실행 종료
            				if ( aDatasetOpenForEditManager.isExitFlag() ) {
//            					Thread.sleep(500);
            					break;
            				}
            				Thread.sleep(1500);
                    }
                    } catch (InterruptedException e) {
                    	e.printStackTrace();
                    }
                    	try {
                    	Thread.sleep(4000);
                    	// Dataset 에서 파일을 추출하여 filePath 변수에 절대경로 입력
                    	setFile( tempDataset );
                    	
                    	// 영작된 Activity 내용을 DB에 반영하는 로직
                    	if(updateActivity(viewId)) {
                    		
                    			Thread.sleep(1500);
                    			tempDataset.delete();
//                    		MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Edit Activity", MessageBox.INFORMATION);
                    	}
                    	} catch (Exception e) {
                    		// TODO Auto-generated catch block
                    		e.printStackTrace();
                    	}
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally
            {
                // Edit 버튼 Disable
                // [NO-SR][20150323] shcho, Edit Activity 작업 후 엑셀파일 종료 시 Edit Activity 버튼이 비활성인 상태로 남아있는 오류 수정
                Display.getDefault().syncExec(new Runnable() {      
                    
                    @Override
                    public void run() {
                        enableEditButtons(viewId, true);
                    }
                });
            }
        }
    }

    private TCComponent[] getPreviousRevisionActivities(TCComponentItemRevision revision) throws TCException {
//        TCComponentItem item = revision.getItem();
//        TCComponent[] revisions = item.getRelatedComponents("revision_list");
//        String revisionId = revision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
//        for(int i = revisions.length - 1; i >= 0; i--) {
//            if(revisionId.equals(revisions[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID))) {
//                if(i > 0) {
//                    TCComponentItemRevision preRevision = (TCComponentItemRevision) revisions[i - 1];
//                    TCComponent root = preRevision.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
//                    TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
//                    return ActivityUtils.getSortedActivityChildren(rootActivity);
//                }
//            }
//        }

    	//[NON-SR][20161004] taeku.jeong PreviousRevision 찾는 방법 변경으로 인한 Function 수정
    	TCComponent[]  activityChildren = null;
    	TCComponentItemRevision preRevision = getPreviousRevision(revision);
    	if(preRevision!=null){
    		TCComponentMEActivity rootActivity = null;
    		TCComponent root = null;
			try {
				root = preRevision.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
			} catch (TCException e) {
				throw e;
			}
    		if(root!=null){
    			try {
					rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
				} catch (TCException e) {
					throw e;
				}
    		}
    		if(rootActivity!=null){
    			try {
					activityChildren = ActivityUtils.getSortedActivityChildren(rootActivity);
				} catch (TCException e) {
					throw e;
				}
    		}
    	}

        return activityChildren;
    }
    
    private TCComponentItemRevision getPreviousRevision(TCComponentItemRevision itemRevision){
    	
    	TCComponentItemRevision  previousRevision = null;
    	
    	try {
    		TCComponentItem currentItem = itemRevision.getItem();
    		TCComponentItemRevision  tempRev = itemRevision.basedOn();
    		if(tempRev!=null){
    			TCComponentItem tempItem = tempRev.getItem();
    			if(tempItem.equals(currentItem)==true){
    				previousRevision = tempRev;
    			}
    		}
		} catch (TCException e) {
			e.printStackTrace();
		}
    	
    	return previousRevision;
    }

    private boolean updateActivity(String viewId) {
        IDataSet dataSet = getDataSet();
        IDataMap dataMap = dataSet.getDataMap(viewId);
        dataMap.put("actionId", "UpdateActivity", IData.STRING_FIELD);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(this.filePath));
            Sheet sheet = workbook.getSheetAt(0);
            int endRow = sheet.getLastRowNum();

            TCComponent[] activities = null;
            String itemId = "";
            String revisionId = "";
            int activityIndex = 0;

            CustomActivityDao activitydao = new CustomActivityDao();
            String userId = ((TCSession) getSession()).getUser().getUserId();
            
            for(int i = 2; i <= endRow; i++) {
                Row row = sheet.getRow(i);
                //[SR없음][20150214]shcho, current revision이 old revision보다 activity수가 적을 경우, NullPointerException 발생 하는 버그 수정
                if(row.getCell(0) == null) {
                    continue;
                }
                
                if(!itemId.equals(row.getCell(0).getStringCellValue())) {
                    activityIndex = 0;
                    itemId = row.getCell(0).getStringCellValue();
                    revisionId = row.getCell(1).getStringCellValue();
                    TCComponentItemRevision revision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, itemId, revisionId);
                    if(revision == null) {
                        throw new NullPointerException("공법 " + itemId + "/" + revisionId + "가 존재하지 않습니다.");
                    }

                    TCComponent root = revision.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
                    if(root != null) {
                        TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                        activities = ActivityUtils.getSortedActivityChildren(rootActivity);

                        activitydao.updateActivityEnglishName(activities[activityIndex].getUid(), row.getCell(5).getStringCellValue(), userId, "ACTIVITY");
                        activityIndex++;
                    }
                } else {
                	System.out.println("activityIndex : "  + activityIndex);
                	System.out.println("i : "  + i);
                	System.out.println("itemId : "  + itemId);
                	if ( activities.length > activityIndex ) {
                		if(activities[activityIndex] != null) {
                			activitydao.updateActivityEnglishName(activities[activityIndex].getUid(), row.getCell(5).getStringCellValue(), userId, "ACTIVITY");
                			activityIndex++;
                		}
                	}
                }
            }

            AbstractSDVViewPane viewPane = (AbstractSDVViewPane) UIManager.getCurrentDialog().getView(viewId);
            if(viewPane != null) {
                viewPane.setLocalDataMap(dataMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Edit Activity", MessageBox.ERROR);
        }

        return true;
    }

    // [20170530] 이 메서드 안씀
    private void openExcelFile(final String viewId, OleListener oleListener, File tempFile) {
    	
    	
    	/*
    	 * 잠금된 Excel 파일 잠금 해제
    	 */
    	 try {
    		 
    		 Workbook workbook = new XSSFWorkbook(new FileInputStream(tempFile));
    		 
    		 for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
    			 XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
    			 CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
    			 if (sheetProtection != null) {
    				 sheet.getCTWorksheet().unsetSheetProtection();
    			 }
    		 }
    		 
    		 CTWorkbookProtection workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().getWorkbookProtection();
    		 if (workbookProtection != null) {
    			 ((XSSFWorkbook) workbook).getCTWorkbook().unsetWorkbookProtection();
    		 }
    		 
    		 FileOutputStream fos = new FileOutputStream(tempFile);
    	        workbook.write(fos);
    	        fos.flush();
    	        fos.close();
    		 
    	 } catch(Exception e) {
    		 e.getStackTrace();
    	 }
         /////////////////////////////////////////////////////////////////////////////////////////
    	
        Composite oleComposite = null;
        if(viewId == null) {
            oleComposite = new Composite((AbstractSDVViewPane) UIManager.getCurrentDialog().getViews().get(0), SWT.NONE);
        } else {
            oleComposite = new Composite((AbstractSDVViewPane) UIManager.getCurrentDialog().getView(viewId), SWT.NONE);
        }

        oleComposite.setLayoutData(BorderLayout.CENTER);
        oleComposite.setLayout(new FillLayout());
//        String EXCELAPPLICATION = "Excel.Application.15";
        appControlSite = new OleControlSite(new OleFrame(oleComposite, SWT.NONE), SWT.NONE, "Excel.Sheet", tempFile );
        appControlSite.doVerb(OLE.OLEIVERB_OPEN);

        OleAutomation application = new OleAutomation(appControlSite);
        application.setProperty(application.getIDsOfNames(new String[] {"Visible"})[0], new Variant(true));
        OleAutomation workbooks = application.getProperty(application.getIDsOfNames(new String[] {"Workbooks"})[0]).getAutomation();
        Variant varResult = workbooks.invoke(workbooks.getIDsOfNames(new String[] {"Open"})[0], new Variant[] {new Variant(filePath)});
        if(varResult != null) {
            System.out.println(" copy invoke result of BSHEET = " + varResult);
            varResult.dispose();

            // Edit 버튼 Disable
            //요거 필요
            enableEditButtons(viewId, false);
        } else {
            System.out.println("=====failed invoke copySheet method ====");
        }

        OleListener windowDeactivateListener = new OleListener() {

            @Override
            public void handleEvent(OleEvent event) {
                enableEditButtons(viewId, true);
            }
        };
        appControlSite.addEventListener(application, ExcelView.IID_AppEvents, ExcelView.WindowDeactivate, windowDeactivateListener);

        if(oleListener != null) {
            appControlSite.addEventListener(application, ExcelView.IID_AppEvents, ExcelView.WorkbookBeforeSave, oleListener);
        }

        workbooks.dispose();
        application.dispose();
    }

    private void enableEditButtons(String viewId, boolean flag) {
        LinkedHashMap<String, IButtonInfo> actionButtons = null;
        if(viewId == null) {
            actionButtons = ((AbstractSDVSWTDialog) UIManager.getCurrentDialog()).getCommandToolButtons();
        } else {
            actionButtons = ((AbstractSDVViewPane) UIManager.getCurrentDialog().getView(viewId)).getActionToolButtons();
        }

        if(actionButtons != null) {
            for(String key : actionButtons.keySet()) {
                if(actionButtons.get(key).getActionId().startsWith("Edit")) {
                    actionButtons.get(key).getButton().setEnabled(flag);
                }
            }
        }
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, String name) {
        return getAutoProperty(auto, name, null);
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, String name, Variant[] values) {
        return getAutoProperty(auto, property(auto, name), values);
    }

    /**
     *
     * @method getAutoProperty
     * @date 2013. 9. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected OleAutomation getAutoProperty(OleAutomation auto, int dispId, Variant[] values) {
        Variant varResult = null;
        varResult = (values != null) ? auto.getProperty(dispId, values) : auto.getProperty(dispId);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            OleAutomation result = varResult.getAutomation();
            varResult.dispose();
            return result;
        }
        return null;
    }

    protected int property(OleAutomation auto, String name) {
        return auto.getIDsOfNames(new String[] { name })[0];
    }

    @Override
    public void endOperation() {

    }
    
    
    public void setFile(TCComponentDataset targetDataset) {
        try {
        	targetDataset.refresh();
            TCComponentTcFile[] files = targetDataset.getTcFiles();
            if(files != null && files.length > 0) {
                File file = files[0].getFile(null);
                if(file != null) {
                    this.filePath = file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}
