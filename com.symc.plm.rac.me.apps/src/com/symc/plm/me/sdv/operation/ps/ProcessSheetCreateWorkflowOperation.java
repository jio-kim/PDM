package com.symc.plm.me.sdv.operation.ps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.sdv.core.common.ISDVActionOperation;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.kgm.common.WaitProgressBar;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.Registry;

/**
 *  [P0069(영문작업표준서 이슈 번호 )] [20150109] ymjang, 영문작업표준서 검색 창에서 검색된 미결재 공법을 재Publish하지 않고 바로 결재 할 수 있는 버튼이 없음.
 *  
 */
public class ProcessSheetCreateWorkflowOperation extends AbstractSDVActionOperation {
    private String processType;
//    private String productCode;
    private File file;
    private int configId = 0;
    private TCComponentItemRevision mecoRevision;

    private WaitProgressBar progress;

    private Registry registry = Registry.getRegistry(this);

    public ProcessSheetCreateWorkflowOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public ProcessSheetCreateWorkflowOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public ProcessSheetCreateWorkflowOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    public ProcessSheetCreateWorkflowOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void executeOperation() throws Exception {
    	
        progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
        progress.setWindowSize(500, 400);
        progress.start();
        progress.setShowButton(true);
        progress.setStatus("[" + new Date() + "]" + "Process Sheet Workflow start.");
        progress.setAlwaysOnTop(true);

        try {
            IDataSet dataset = getDataSet();
            List<HashMap<String, Object>> opList = null;

            progress.setStatus("[" + new Date() + "]" + "Getting target operations..");

            if(dataset != null) {
                Collection<IDataMap> dataMaps = dataset.getAllDataMaps();
                if(dataMaps != null) {
                    for(IDataMap dataMap : dataMaps) {
                        if(dataMap.containsKey("targetOperationList")) {
                            opList = dataMap.getTableValue("targetOperationList");
                            if(dataMap.containsKey("configId")) {
                                configId = dataMap.getIntValue("configId");
                            }
                        }
                    }
                }
            }

            if(opList == null) {
                progress.setStatus("[" + new Date() + "]" + "Target Operations is null.");
                return;
            }

            int size = opList.size();
            progress.setStatus("[" + new Date() + "]" + "Number of target operations : " + size);

            AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
            TCComponentBOPLine shopLine = null;
            if(application instanceof MFGLegacyApplication) {
                TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
                shopLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
                this.processType = shopLine.getItemRevision().getTCProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).getStringValue();
            }

            List<InterfaceAIFComponent> publishItemRevs = new ArrayList<InterfaceAIFComponent>();
            for(int i = 0; i < size; i++) {
                HashMap<String, Object> operation = opList.get(i);
                String itemId = (String) operation.get(SDVPropertyConstant.ITEM_ITEM_ID);
                String revId = (String) operation.get(SDVPropertyConstant.ITEM_REVISION_ID);

//                TCComponentBOPLine bopLine = (TCComponentBOPLine) operation.get("OPERATION_BOPLINE");
//                TCComponentItemRevision itemRevision = bopLine.getItemRevision();

                // Publish 된 아이템 리비전 가져오기
                TCComponentItemRevision psItemRevision = getProcessSheetItemRev(itemId, revId);
                                
                if(psItemRevision == null) {
                	progress.setStatus("[" + new Date() + "]" + itemId + "/" + revId + " is processing workflow or was not Published.");
                    return;
                }
                
                // 작업 표준서가 이미 릴리즈되어 있는지 체크.
                TCComponent[] releaseStatusList = psItemRevision.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                if(releaseStatusList != null && releaseStatusList.length > 0) {
                	progress.setStatus("[" + new Date() + "]" + itemId + "/" + revId + " was already released.");
                    return;
                }
                
                publishItemRevs.add(psItemRevision);
            }

            progress.setStatus("[" + new Date() + "]" + "Validating is completed.");
            progress.close();

            setDataSet(dataset);
            
            int retVal = ConfirmDialog.prompt(UIManager.getCurrentDialog().getShell(), "Create Workflow", registry.getString("ProcessSheetWorkflowRequest.Message"));
            if(retVal == IDialogConstants.YES_ID) {
                InterfaceAIFComponent[] comps = new InterfaceAIFComponent[publishItemRevs.size()];
                for(int i = 0; i < publishItemRevs.size(); i++) {
                    comps[i] = publishItemRevs.get(i);
                }
                new SDVNewProcessCommand(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), comps,
                        				 registry.getString("PublishProcessSheet_WorkflowTemplateNamePreference_EN"));
            }
            
        } catch(Exception e) {
            setExecuteResult(ISDVActionOperation.FAIL);
            setExecuteError(e);

            e.printStackTrace();
            progress.setStatus("[" + new Date() + "]" + "Error : " + e.getMessage());
//            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Publish", MessageBox.ERROR);
        }
    }

    /**
     * [P0069(영문작업표준서 이슈 번호 )] [20150109] ymjang, 영문작업표준서 검색 창에서 검색된 미결재 공법을 재Publish하지 않고 바로 결재 할 수 있는 버튼이 없음.
     * 
     * @param itemId
     * @param revId
     * @return
     * @throws Exception
     */
    private TCComponentItemRevision getProcessSheetItemRev(String itemId, String revId) throws Exception {
    	
        TCComponentItemRevision psItemRevision = null;
        TCComponentItem psItem = SDVBOPUtilities.FindItem(getProcessSheetPrefix() + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);

        if(psItem == null) 
        	return null;
        
        for(TCComponent revision : psItem.getRelatedComponents("revision_list")) {
            revision.refresh();
        }

        TCComponentItemRevision[] revisions = psItem.getWorkingItemRevisions();
        if(revisions != null && revisions.length > 0) {
            for(TCComponentItemRevision revision : revisions) {
                if(revision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(revId)) {
                    psItemRevision = revision;
                }
            }
        }

        if(psItemRevision == null) 
        	return null;
        
        return psItemRevision;
    }

    private String getProcessSheetPrefix() {
        return registry.getString("ProcessSheetItemIDPrefix." + this.configId);
    }

	@Override
	public void endOperation() {
		// TODO Auto-generated method stub
		
	}


}
