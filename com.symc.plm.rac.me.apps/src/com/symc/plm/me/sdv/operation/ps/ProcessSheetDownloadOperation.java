package com.symc.plm.me.sdv.operation.ps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.ISDVActionOperation;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ProcessSheetExcelHelper;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ProcessSheetDownloadOperation extends AbstractSDVActionOperation {

    private int configId = 0;
    private Registry registry = Registry.getRegistry(this);

    private static String filePath;

    public ProcessSheetDownloadOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public ProcessSheetDownloadOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public ProcessSheetDownloadOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    public ProcessSheetDownloadOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void executeOperation() throws Exception {
        try {
            boolean flag = false;
            IDataSet dataset = getDataSet();
            IDataMap dataMap = null;
            if(dataset != null && (dataset.containsMap("previewView"))) {
                dataMap = dataset.getDataMap("previewView");
                if(dataMap.containsKey("configId")) {
                    configId = dataMap.getIntValue("configId");
                }
                flag = true;
            } else if(dataset != null && dataset.containsMap("searchResultView")) {
                dataMap = dataset.getDataMap("searchResultView");
                if(dataMap.containsKey("configId")) {
                    configId = dataMap.getIntValue("configId");
                }
                flag = true;
            }

            if(flag) {
                List<HashMap<String, Object>> opList = null;
                if(dataMap.containsKey("targetOperationList")) {
                    opList = dataMap.getTableValue("targetOperationList");
                }

                if(opList != null && opList.size() > 0) {
                    openDirectoryDialog();
                    if(filePath == null) {
                        setExecuteResult(ISDVActionOperation.CANCEL);
                        return;
                    }

                    for(int i = 0; i < opList.size(); i++) {
                        String itemId = (String) opList.get(i).get(SDVPropertyConstant.ITEM_ITEM_ID);
                        String publishItemId = registry.getString("ProcessSheetItemIDPrefix." + configId) + itemId;
                        String revisionId = (String) opList.get(i).get(SDVPropertyConstant.ITEM_REVISION_ID);

                        String publishRev = null;
                        HashMap<String, Object> operationMap = opList.get(i);
                        if(operationMap.containsKey("selected_publish_rev")) {
                            publishRev = (String) operationMap.get("selected_publish_rev");
                        }

                        TCComponentItem item = SDVBOPUtilities.FindItem(publishItemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
                        TCComponentItemRevision revision = null;

                        if(item == null) {
                            throw new Exception("공법 " + itemId + "/" + revisionId +
                                    registry.getString("NoExistPublishedProcessSheet.Message." + configId));
                        }

                        if(item != null) {
                            TCComponent[] revisions = item.getRelatedComponents("revision_list");

                            // Preview에서 다운로드를 실행했을 경우에는 해당 공법 리비전의 Publish 된 작업표준서를 다운로드한다.
                            if(publishRev == null) {
                                publishRev = revisionId;
                            }

                            for(int j = revisions.length - 1; j >= 0; j--) {
                                if(revisions[j].getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(publishRev)) {
                                    revision = (TCComponentItemRevision) revisions[j];
                                    break;
                                }
                            }

                            if(revision == null) {
                                throw new Exception("공법 " + itemId + "/" + revisionId +
                                        registry.getString("NoExistPublishedProcessSheet.Message." + configId));
                            }

                            ProcessSheetExcelHelper.downloadProcessSheet(revision, filePath, null);
                        }
                    }
                }

                MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Download", MessageBox.INFORMATION);
            }
        } catch(Exception e) {
//            setExecuteResult(ISDVActionOperation.FAIL);
//            setExecuteError(e);

            e.printStackTrace();
            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Download", MessageBox.ERROR);
        }
    }

    public void openDirectoryDialog() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Shell shell = AIFUtility.getActiveDesktop().getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SAVE);
                filePath = dialog.open();
            }
        });
    }

    @Override
    public void endOperation() {
//        if(getExecuteResult() == ISDVActionOperation.SUCCESS) {
//            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Download", MessageBox.INFORMATION);
//        }
    }

}
