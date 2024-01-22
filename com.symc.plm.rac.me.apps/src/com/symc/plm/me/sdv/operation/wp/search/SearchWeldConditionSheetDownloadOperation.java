package com.symc.plm.me.sdv.operation.wp.search;

import java.util.Date;
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
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class SearchWeldConditionSheetDownloadOperation extends AbstractSDVActionOperation {

    protected int configId = 0;
    protected Registry registry;

    private static String filePath;

    public SearchWeldConditionSheetDownloadOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public SearchWeldConditionSheetDownloadOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public SearchWeldConditionSheetDownloadOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void executeOperation() throws Exception {
        try {
            boolean flag = false;
            IDataSet dataset = getDataSet();
            IDataMap dataMap = null;
            if(dataset != null && dataset.containsMap("listView"))
            {
                dataMap = dataset.getDataMap("listView");
                if(dataMap.containsKey("configId"))
                {
                    configId = dataMap.getIntValue("configId");
                }
                flag = true;
            }

            if(flag) {
                List<HashMap<String, Object>> weldOpList = null;
                List<String> weldOpRevList = null;
                if(dataMap.containsKey("targetWeldOperationList") && dataMap.containsKey("targetWeldOperationRevList"))
                {
                    weldOpList = (List<HashMap<String, Object>>) dataMap.getListValue("targetWeldOperationList");
                    weldOpRevList = (List<String>) dataMap.getListValue("targetWeldOperationRevList");
                }

                if(weldOpList != null && weldOpList.size() > 0 && weldOpRevList != null)
                {
                    openDirectoryDialog();

                    for(int i = 0; i < weldOpList.size(); i++) {
                        String itemId = (String) weldOpList.get(i).get(SDVPropertyConstant.ITEM_ITEM_ID);
                        //String revisionId = (String) weldOpList.get(i).get(SDVPropertyConstant.ITEM_REVISION_ID);
                        String revisionId = (String) weldOpRevList.get(i);
                        TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
                        TCComponentItemRevision revision = null;
                        if(item != null)
                        {
                            TCComponent[] revisions = item.getRelatedComponents("revision_list");
                            for(int j = 0; j < revisions.length; j++)
                            {
                                if(revisions[j].getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(revisionId))
                                {
                                    revision = (TCComponentItemRevision) revisions[j];
                                    break;
                                }
                            }
                            String fileName = itemId + "_" + SDVStringUtiles.dateToString(new Date(), "yyyy-MM-dd") + ".xlsx";
                            PreviewWeldConditionSheetExcelHelper.downloadProcessSheet(revision, filePath, fileName);
                        }
                    }
                }
            }
        } catch(Exception e) {
            setExecuteResult(ISDVActionOperation.FAIL);
            setExecuteError(e);

            e.printStackTrace();
            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Download", MessageBox.ERROR);
        }
    }

    public static void openDirectoryDialog()
    {
        Display.getDefault().syncExec(new Runnable()
        {
            @Override
            public void run()
            {
                Shell shell = AIFUtility.getActiveDesktop().getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SAVE);
                filePath = dialog.open();
            }
        });
    }

    @Override
    public void endOperation()
    {
        if(getExecuteResult() == ISDVActionOperation.SUCCESS)
        {
//            MessageBox.post(UIManager.getCurrentDialog().getShell(),
//                    registry.getString("DownloadProcessSheetComplete.Message"), "Download", MessageBox.INFORMATION);
            MessageBox.post(UIManager.getCurrentDialog().getShell(),
                    "Complete to download WeldCondition Sheet.", "Download", MessageBox.INFORMATION);
        }
    }

}

